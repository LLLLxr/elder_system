import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  Alert,
  Button,
  Card,
  Col,
  Drawer,
  Form,
  Row,
  Select,
  Space,
  Statistic,
  Steps,
  Table,
  Tag,
  Typography,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { getJourneyTaskOverview, listIntakeRecordsByApplicant, listJourneyTaskTimeline } from '../api/careOrchestrationApi';
import { extractApiErrorMessage } from '../api/client';
import { useUserStore } from '../stores/userStore';
import type { IntakeRecord, JourneyTaskItem, JourneyTaskOverview } from '../types/care';

interface FilterFormValues {
  applicationId?: number;
}

interface JourneyBoardQuery {
  applicationId?: number;
}

interface MineOption {
  label: string;
  value: number;
}

type ApplicationStepKey =
  | 'APPLICATION_SUBMITTED'
  | 'ADMISSION_ASSESSMENT'
  | 'HEALTH_ASSESSMENT'
  | 'AGREEMENT'
  | 'IN_SERVICE';

type ApplicationStepStatus = 'wait' | 'process' | 'finish' | 'error';

type AlertType = 'success' | 'info' | 'warning' | 'error';

interface ApplicationStepItem {
  key: ApplicationStepKey;
  title: string;
  status: ApplicationStepStatus;
  summary: string;
  timeText: string;
  hint: string;
}

interface JourneyAlert {
  type: AlertType;
  message: string;
}

interface JourneyProgressData {
  steps: ApplicationStepItem[];
  currentStepKey: ApplicationStepKey;
  overallAlert: JourneyAlert;
  currentSummary: string;
  currentHint: string;
}

const ALL_TASK_STATUSES = ['PENDING', 'OVERDUE', 'COMPLETED', 'CANCELLED'];
const AUTO_REFRESH_INTERVAL_MS = 15000;
const DEFAULT_QUERY: JourneyBoardQuery = {};

const STEP_ORDER: ApplicationStepKey[] = [
  'APPLICATION_SUBMITTED',
  'ADMISSION_ASSESSMENT',
  'HEALTH_ASSESSMENT',
  'AGREEMENT',
  'IN_SERVICE',
];

const STEP_TITLES: Record<ApplicationStepKey, string> = {
  APPLICATION_SUBMITTED: '提交申请',
  ADMISSION_ASSESSMENT: '需求评估',
  HEALTH_ASSESSMENT: '健康评估',
  AGREEMENT: '签约',
  IN_SERVICE: '服务中',
};

const STEP_STATUS_LABELS: Record<ApplicationStepStatus, string> = {
  wait: '未开始',
  process: '进行中',
  finish: '已完成',
  error: '已结束',
};

const STEP_STATUS_COLORS: Record<ApplicationStepStatus, string> = {
  wait: 'default',
  process: 'processing',
  finish: 'success',
  error: 'error',
};

const STATE_LABELS: Record<string, string> = {
  PENDING_ASSESSMENT: '等待需求评估',
  PENDING_HEALTH_ASSESSMENT: '等待健康评估',
  PENDING_AGREEMENT: '等待签约',
  IN_SERVICE: '服务中',
  IMPROVEMENT_REQUIRED: '待改进',
  RENEWED: '已续约',
  TERMINATED: '已结束',
};

const OPEN_TASK_STATUSES = new Set(['PENDING', 'OVERDUE']);
const STEP_INDEX = STEP_ORDER.reduce<Record<ApplicationStepKey, number>>((accumulator, key, index) => {
  accumulator[key] = index;
  return accumulator;
}, {} as Record<ApplicationStepKey, number>);

function formatDateTime(value?: string): string {
  if (!value) {
    return '-';
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return date.toLocaleString();
}

function getStateLabel(state?: string): string {
  if (!state) {
    return '-';
  }
  return STATE_LABELS[state] ?? state;
}

function renderStepStatusTag(status: ApplicationStepStatus) {
  return <Tag color={STEP_STATUS_COLORS[status]}>{STEP_STATUS_LABELS[status]}</Tag>;
}

function getSortTimestamp(value?: string) {
  if (!value) {
    return Number.MAX_SAFE_INTEGER;
  }
  const time = new Date(value).getTime();
  return Number.isNaN(time) ? Number.MAX_SAFE_INTEGER : time;
}

function includesKeyword(text: string | undefined, keyword: string) {
  return Boolean(text && text.includes(keyword));
}

function getLatestTaskOfType(timeline: JourneyTaskItem[], taskType: string): JourneyTaskItem | undefined {
  for (let index = timeline.length - 1; index >= 0; index -= 1) {
    const task = timeline[index];
    if (task.taskType === taskType) {
      return task;
    }
  }
  return undefined;
}

function isStepBefore(left: ApplicationStepKey, right: ApplicationStepKey) {
  return STEP_INDEX[left] < STEP_INDEX[right];
}

function isStepAfter(left: ApplicationStepKey, right: ApplicationStepKey) {
  return STEP_INDEX[left] > STEP_INDEX[right];
}

function deriveJourneyProgress(record: IntakeRecord | undefined, timeline: JourneyTaskItem[]): JourneyProgressData {
  const admissionTask = getLatestTaskOfType(timeline, 'ADMISSION_ASSESSMENT');
  const healthTask = getLatestTaskOfType(timeline, 'HEALTH_ASSESSMENT');
  const journeyStatus = record?.journeyStatus;
  const admissionStatus = record?.admissionStatus;
  const message = record?.message;

  const admissionCompleted = admissionTask?.status === 'COMPLETED';
  const healthCompleted = healthTask?.status === 'COMPLETED';
  const admissionActive = OPEN_TASK_STATUSES.has(admissionTask?.status ?? '');
  const healthActive = OPEN_TASK_STATUSES.has(healthTask?.status ?? '');

  const isWithdrawn = admissionStatus === 'WITHDRAWN' || includesKeyword(message, '撤回');
  const isImprovementRequired = journeyStatus === 'IMPROVEMENT_REQUIRED';
  const isRenewed = journeyStatus === 'RENEWED';
  const isInService = journeyStatus === 'IN_SERVICE';

  let failureStepKey: ApplicationStepKey | undefined;
  if (journeyStatus === 'TERMINATED' || admissionStatus === 'FAILED' || isWithdrawn) {
    if (isWithdrawn) {
      failureStepKey = healthCompleted ? 'AGREEMENT' : healthTask ? 'HEALTH_ASSESSMENT' : 'ADMISSION_ASSESSMENT';
    } else if (includesKeyword(message, '健康评估未通过')) {
      failureStepKey = 'HEALTH_ASSESSMENT';
    } else if (includesKeyword(message, '需求评估未通过')) {
      failureStepKey = 'ADMISSION_ASSESSMENT';
    } else if (healthCompleted) {
      failureStepKey = 'AGREEMENT';
    } else if (healthTask) {
      failureStepKey = 'HEALTH_ASSESSMENT';
    } else {
      failureStepKey = 'ADMISSION_ASSESSMENT';
    }
  }

  const isAgreementStage =
    !failureStepKey &&
    !isInService &&
    !isImprovementRequired &&
    !isRenewed &&
    healthCompleted &&
    (journeyStatus === 'PENDING_HEALTH_ASSESSMENT' || journeyStatus === 'PENDING_AGREEMENT' || !journeyStatus);

  let currentStepKey: ApplicationStepKey;
  if (failureStepKey) {
    currentStepKey = failureStepKey;
  } else if (isInService || isImprovementRequired || isRenewed) {
    currentStepKey = 'IN_SERVICE';
  } else if (isAgreementStage) {
    currentStepKey = 'AGREEMENT';
  } else if (healthActive || journeyStatus === 'PENDING_HEALTH_ASSESSMENT' || (admissionCompleted && !healthTask)) {
    currentStepKey = 'HEALTH_ASSESSMENT';
  } else if (admissionActive || journeyStatus === 'PENDING_ASSESSMENT' || !admissionCompleted) {
    currentStepKey = 'ADMISSION_ASSESSMENT';
  } else {
    currentStepKey = 'ADMISSION_ASSESSMENT';
  }

  let overallAlert: JourneyAlert;
  if (isWithdrawn) {
    overallAlert = { type: 'warning', message: message ?? '申请已撤回。' };
  } else if (failureStepKey === 'ADMISSION_ASSESSMENT') {
    overallAlert = { type: 'error', message: message ?? '需求评估未通过，当前申请已结束。' };
  } else if (failureStepKey === 'HEALTH_ASSESSMENT') {
    overallAlert = { type: 'error', message: message ?? '健康评估未通过，当前申请已结束。' };
  } else if (failureStepKey === 'AGREEMENT') {
    overallAlert = { type: 'warning', message: message ?? '签约阶段已结束，请以最新旅程结果为准。' };
  } else if (isRenewed) {
    overallAlert = { type: 'success', message: message ?? '服务已续约，可继续关注后续安排。' };
  } else if (isImprovementRequired) {
    overallAlert = { type: 'warning', message: message ?? '当前服务进入改进阶段，请留意平台后续通知。' };
  } else if (isInService) {
    overallAlert = { type: 'success', message: message ?? '已进入服务阶段，可关注后续护理安排。' };
  } else if (isAgreementStage) {
    overallAlert = { type: 'info', message: message ?? '健康评估已完成，正在等待签约安排。' };
  } else if (currentStepKey === 'HEALTH_ASSESSMENT') {
    overallAlert = { type: 'info', message: message ?? '需求评估已完成，正在等待健康评估。' };
  } else {
    overallAlert = { type: 'info', message: message ?? '申请已提交，正在等待平台完成需求评估。' };
  }

  const resolveStepStatus = (stepKey: ApplicationStepKey): ApplicationStepStatus => {
    if (stepKey === 'APPLICATION_SUBMITTED') {
      return 'finish';
    }

    if (failureStepKey) {
      if (stepKey === failureStepKey) {
        return 'error';
      }
      return isStepBefore(stepKey, failureStepKey) ? 'finish' : 'wait';
    }

    if (stepKey === 'IN_SERVICE' && (isImprovementRequired || isRenewed)) {
      return 'finish';
    }

    if (stepKey === currentStepKey) {
      return 'process';
    }

    return isStepBefore(stepKey, currentStepKey) ? 'finish' : 'wait';
  };

  const buildStepSummary = (stepKey: ApplicationStepKey, status: ApplicationStepStatus): string => {
    if (stepKey === 'APPLICATION_SUBMITTED') {
      return '申请已提交';
    }

    if (stepKey === 'ADMISSION_ASSESSMENT') {
      if (status === 'process') {
        return '等待需求评估';
      }
      if (status === 'finish') {
        return '需求评估已完成';
      }
      if (status === 'error') {
        return isWithdrawn ? '申请已撤回' : '需求评估未通过';
      }
      return '等待进入需求评估';
    }

    if (stepKey === 'HEALTH_ASSESSMENT') {
      if (status === 'process') {
        return '等待健康评估';
      }
      if (status === 'finish') {
        return '健康评估已完成';
      }
      if (status === 'error') {
        return '健康评估未通过';
      }
      return '等待完成需求评估';
    }

    if (stepKey === 'AGREEMENT') {
      if (status === 'process') {
        return '等待签约安排';
      }
      if (status === 'finish') {
        return '已完成签约';
      }
      if (status === 'error') {
        return isWithdrawn ? '申请已撤回' : '签约阶段已结束';
      }
      return '等待完成健康评估';
    }

    if (status === 'process') {
      return '服务进行中';
    }
    if (status === 'finish') {
      if (isRenewed) {
        return '服务已续约';
      }
      if (isImprovementRequired) {
        return '进入改进阶段';
      }
      return '服务已完成';
    }
    if (status === 'error') {
      return '服务已结束';
    }
    return '等待完成签约';
  };

  const buildTimeText = (stepKey: ApplicationStepKey, status: ApplicationStepStatus): string => {
    if (stepKey === 'APPLICATION_SUBMITTED') {
      return record?.submittedAt ? `提交于 ${formatDateTime(record.submittedAt)}` : '-';
    }

    if (stepKey === 'ADMISSION_ASSESSMENT') {
      if (admissionTask?.completedAt) {
        return `完成于 ${formatDateTime(admissionTask.completedAt)}`;
      }
      if (admissionTask?.dueAt && status === 'process') {
        return `预计处理至 ${formatDateTime(admissionTask.dueAt)}`;
      }
      if (admissionTask?.createdAt) {
        return `开始于 ${formatDateTime(admissionTask.createdAt)}`;
      }
      return record?.submittedAt ? `提交于 ${formatDateTime(record.submittedAt)}` : '-';
    }

    if (stepKey === 'HEALTH_ASSESSMENT') {
      if (healthTask?.completedAt) {
        return `完成于 ${formatDateTime(healthTask.completedAt)}`;
      }
      if (healthTask?.dueAt && status === 'process') {
        return `预计处理至 ${formatDateTime(healthTask.dueAt)}`;
      }
      if (healthTask?.createdAt) {
        return `开始于 ${formatDateTime(healthTask.createdAt)}`;
      }
      return status === 'wait' ? '-' : '等待平台安排';
    }

    if (stepKey === 'AGREEMENT') {
      if (status === 'finish') {
        return isInService || isImprovementRequired || isRenewed ? '已完成签约并进入服务阶段' : '签约已完成';
      }
      if (status === 'process') {
        return healthTask?.completedAt ? `健康评估完成于 ${formatDateTime(healthTask.completedAt)}` : '等待平台通知';
      }
      if (status === 'error') {
        return '当前申请未进入签约完成阶段';
      }
      return '-';
    }

    if (status === 'process') {
      return '当前服务执行中';
    }
    if (status === 'finish') {
      if (isRenewed) {
        return '已续约';
      }
      if (isImprovementRequired) {
        return '进入改进阶段';
      }
      return '服务阶段已完成';
    }
    if (status === 'error') {
      return '服务阶段已结束';
    }
    return '-';
  };

  const buildHint = (stepKey: ApplicationStepKey, status: ApplicationStepStatus): string => {
    if (stepKey === 'APPLICATION_SUBMITTED') {
      return '申请已经提交成功，平台会按旅程步骤继续处理。';
    }

    if (stepKey === 'ADMISSION_ASSESSMENT') {
      if (status === 'process') {
        return '平台正在处理需求评估，请耐心等待。';
      }
      if (status === 'finish') {
        return '需求评估已通过，后续将进入健康评估。';
      }
      if (status === 'error') {
        return isWithdrawn ? '申请已经撤回，请以最新结果为准。' : '需求评估阶段已结束，请留意平台通知。';
      }
      return '提交申请后，会先进入需求评估阶段。';
    }

    if (stepKey === 'HEALTH_ASSESSMENT') {
      if (status === 'process') {
        return '请留意健康评估和体检相关通知。';
      }
      if (status === 'finish') {
        return '健康评估已通过，平台将继续安排签约。';
      }
      if (status === 'error') {
        return '健康评估阶段已结束，请留意平台通知。';
      }
      return '需求评估完成后，会进入健康评估阶段。';
    }

    if (stepKey === 'AGREEMENT') {
      if (status === 'process') {
        return '健康评估已完成，请留意签约通知。';
      }
      if (status === 'finish') {
        return '签约已完成，可继续关注服务安排。';
      }
      if (status === 'error') {
        return '当前申请未进入签约完成阶段，请以最新旅程结果为准。';
      }
      return '健康评估通过后，平台会继续安排签约。';
    }

    if (status === 'process') {
      return '服务已经开始，可关注后续护理安排。';
    }
    if (status === 'finish') {
      if (isRenewed) {
        return '当前服务已续约，请关注新的服务安排。';
      }
      if (isImprovementRequired) {
        return '当前服务进入改进阶段，请留意平台后续通知。';
      }
      return '服务阶段已完成，请以最新结果为准。';
    }
    if (status === 'error') {
      return '当前服务阶段已结束，请以最新结果为准。';
    }
    return '签约完成后，将正式进入服务阶段。';
  };

  const steps: ApplicationStepItem[] = STEP_ORDER.map((stepKey) => {
    const status = resolveStepStatus(stepKey);
    return {
      key: stepKey,
      title: STEP_TITLES[stepKey],
      status,
      summary: buildStepSummary(stepKey, status),
      timeText: buildTimeText(stepKey, status),
      hint: buildHint(stepKey, status),
    };
  });

  const currentStep = steps.find((item) => item.key === currentStepKey) ?? steps[1];

  return {
    steps,
    currentStepKey,
    overallAlert,
    currentSummary: currentStep.summary,
    currentHint: currentStep.hint,
  };
}

export default function JourneyTaskBoardPage() {
  const [filterForm] = Form.useForm<FilterFormValues>();
  const { username: loginUsername } = useUserStore();

  const [mineOptions, setMineOptions] = useState<MineOption[]>([]);
  const [mineRecords, setMineRecords] = useState<IntakeRecord[]>([]);
  const [query, setQuery] = useState<JourneyBoardQuery>(DEFAULT_QUERY);
  const [overview, setOverview] = useState<JourneyTaskOverview | null>(null);
  const [overviewLoading, setOverviewLoading] = useState(false);
  const [overviewError, setOverviewError] = useState<string | null>(null);

  const [timeline, setTimeline] = useState<JourneyTaskItem[]>([]);
  const [timelineLoading, setTimelineLoading] = useState(false);
  const [timelineError, setTimelineError] = useState<string | null>(null);

  const [detailOpen, setDetailOpen] = useState(false);

  const selectedRecord = useMemo(
    () => mineRecords.find((record) => record.applicationId === query.applicationId),
    [mineRecords, query.applicationId],
  );

  const progress = useMemo(() => deriveJourneyProgress(selectedRecord, timeline), [selectedRecord, timeline]);

  const loadMineRecords = useCallback(
    async (preferredApplicationId?: number) => {
      if (!loginUsername) {
        setMineOptions([]);
        setMineRecords([]);
        return;
      }

      try {
        const records = await listIntakeRecordsByApplicant(loginUsername);
        const filteredRecords = records.filter(
          (record): record is IntakeRecord & { applicationId: number } => typeof record.applicationId === 'number',
        );
        const orderedRecords = [...filteredRecords].sort(
          (left, right) => getSortTimestamp(left.submittedAt) - getSortTimestamp(right.submittedAt),
        );
        const options = orderedRecords.map((record, index) => ({
          value: record.applicationId,
          label: `我的第 ${index + 1} 次申请（提交时间：${formatDateTime(record.submittedAt)}）`,
        }));
        setMineRecords(orderedRecords);
        setMineOptions(options);

        if (options.length === 0) {
          filterForm.setFieldsValue({ applicationId: undefined });
          setQuery(DEFAULT_QUERY);
          return;
        }

        const targetApplicationId = options.some((item) => item.value === preferredApplicationId)
          ? preferredApplicationId
          : options[options.length - 1].value;

        filterForm.setFieldsValue({ applicationId: targetApplicationId });
        if (query.applicationId !== targetApplicationId) {
          setQuery({ applicationId: targetApplicationId });
        }
      } catch {
        setMineOptions([]);
        setMineRecords([]);
      }
    },
    [filterForm, loginUsername, query.applicationId],
  );

  const loadOverview = useCallback(async (currentQuery: JourneyBoardQuery) => {
    if (!currentQuery.applicationId) {
      setOverview(null);
      return;
    }

    setOverviewLoading(true);
    setOverviewError(null);
    try {
      const data = await getJourneyTaskOverview({
        applicationId: currentQuery.applicationId,
        statuses: ALL_TASK_STATUSES,
      });
      setOverview(data);
    } catch (error) {
      setOverview(null);
      setOverviewError(extractApiErrorMessage(error, '加载任务概览失败'));
    } finally {
      setOverviewLoading(false);
    }
  }, []);

  const loadTimeline = useCallback(async (currentQuery: JourneyBoardQuery) => {
    if (!currentQuery.applicationId) {
      setTimeline([]);
      return;
    }

    setTimelineLoading(true);
    setTimelineError(null);
    try {
      const data = await listJourneyTaskTimeline(currentQuery.applicationId);
      setTimeline(data);
    } catch (error) {
      setTimeline([]);
      setTimelineError(extractApiErrorMessage(error, '加载申请进度失败'));
    } finally {
      setTimelineLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadMineRecords(query.applicationId);
  }, [loadMineRecords, query.applicationId]);

  useEffect(() => {
    if (!query.applicationId) {
      setOverview(null);
      setTimeline([]);
      return;
    }
    void loadOverview(query);
    void loadTimeline(query);
  }, [loadOverview, loadTimeline, query]);

  const handleApplicationChange = (applicationId?: number) => {
    const nextQuery = { applicationId };
    setQuery(nextQuery);
    if (applicationId) {
      void loadMineRecords(applicationId);
      void loadOverview(nextQuery);
      void loadTimeline(nextQuery);
    }
  };

  const refreshCurrentApplication = useCallback(() => {
    if (!query.applicationId) {
      return;
    }
    void loadMineRecords(query.applicationId);
    void loadOverview(query);
    void loadTimeline(query);
  }, [loadMineRecords, loadOverview, loadTimeline, query]);

  useEffect(() => {
    if (!query.applicationId) {
      return;
    }

    const timer = window.setInterval(() => {
      if (document.visibilityState === 'visible') {
        refreshCurrentApplication();
      }
    }, AUTO_REFRESH_INTERVAL_MS);

    return () => window.clearInterval(timer);
  }, [query.applicationId, refreshCurrentApplication]);

  const handleRefresh = () => {
    refreshCurrentApplication();
  };

  const summaryCards = useMemo(
    () => [
      { key: 'pending', title: '待处理', value: overview?.pendingCount ?? 0 },
      { key: 'overdue', title: '需关注', value: overview?.overdueCount ?? 0 },
      { key: 'completed', title: '已完成', value: overview?.completedCount ?? 0 },
      { key: 'cancelled', title: '已取消', value: overview?.cancelledCount ?? 0 },
    ],
    [overview],
  );

  const columns = useMemo<ColumnsType<ApplicationStepItem>>(
    () => [
      {
        title: '阶段',
        dataIndex: 'title',
        key: 'title',
        width: 160,
      },
      {
        title: '当前状态',
        dataIndex: 'summary',
        key: 'summary',
        width: 180,
      },
      {
        title: '进度情况',
        dataIndex: 'status',
        key: 'status',
        width: 120,
        render: (value: ApplicationStepStatus) => renderStepStatusTag(value),
      },
      {
        title: '时间说明',
        dataIndex: 'timeText',
        key: 'timeText',
        width: 240,
      },
      {
        title: '下一步提示',
        dataIndex: 'hint',
        key: 'hint',
      },
      {
        title: '操作',
        key: 'actions',
        fixed: 'right',
        width: 120,
        render: () => (
          <Button type="link" onClick={() => setDetailOpen(true)}>
            查看详情
          </Button>
        ),
      },
    ],
    [],
  );

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Space style={{ width: '100%', justifyContent: 'space-between' }} align="start">
        <div>
          <Typography.Title level={4} style={{ margin: 0 }}>
            我的任务
          </Typography.Title>
          <Typography.Text type="secondary">
            按申请查看当前旅程进度，重点关注现在走到哪一步、下一步要等待什么。
          </Typography.Text>
        </div>
        <Button onClick={handleRefresh} disabled={!query.applicationId}>
          刷新
        </Button>
      </Space>

      {selectedRecord ? <Alert type={progress.overallAlert.type} message={progress.overallAlert.message} showIcon /> : null}
      {overviewError ? <Alert type="error" message={overviewError} showIcon /> : null}
      {timelineError ? <Alert type="error" message={timelineError} showIcon /> : null}

      <Row gutter={[16, 16]}>
        {summaryCards.map((item) => (
          <Col key={item.key} xs={24} sm={12} lg={6}>
            <Card loading={overviewLoading}>
              <Statistic title={item.title} value={item.value} />
            </Card>
          </Col>
        ))}
      </Row>

      <Card title="当前申请">
        <Form<FilterFormValues> form={filterForm} layout="vertical" initialValues={{ applicationId: mineOptions[mineOptions.length - 1]?.value }}>
          <Row gutter={16}>
            <Col xs={24} md={16} lg={12}>
              <Form.Item name="applicationId" label="我的申请" rules={[{ required: true, message: '请选择申请' }]}>
                <Select
                  options={mineOptions}
                  placeholder="选择当前登录用户的申请"
                  onChange={(value) => handleApplicationChange(value)}
                />
              </Form.Item>
            </Col>
          </Row>
        </Form>
      </Card>

      {!mineOptions.length ? (
        <Alert type="info" showIcon message="当前登录用户名下暂无申请记录，暂时没有可查看的任务。" />
      ) : null}

      <Card title="申请进度">
        <Space direction="vertical" size="middle" style={{ width: '100%' }}>
          <Card size="small" type="inner" title="当前阶段">
            <Space direction="vertical" size="small" style={{ width: '100%' }}>
              <Typography.Text strong>{STEP_TITLES[progress.currentStepKey]}</Typography.Text>
              <Typography.Text>{progress.currentSummary}</Typography.Text>
              <Typography.Text type="secondary">{progress.currentHint}</Typography.Text>
              <Typography.Text type="secondary">
                当前旅程状态：{getStateLabel(selectedRecord?.journeyStatus)}
              </Typography.Text>
            </Space>
          </Card>

          <Table<ApplicationStepItem>
            rowKey="key"
            loading={timelineLoading}
            columns={columns}
            dataSource={query.applicationId ? progress.steps : []}
            pagination={false}
            scroll={{ x: 1080 }}
            locale={{ emptyText: !query.applicationId ? '请先选择我的申请' : timelineLoading ? '正在加载申请进度...' : '暂无进度数据' }}
          />
        </Space>
      </Card>

      <Drawer title="我的进度详情" width={900} open={detailOpen} onClose={() => setDetailOpen(false)} destroyOnClose>
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          <Alert type={progress.overallAlert.type} showIcon message={progress.overallAlert.message} />

          <Card size="small" title="当前阶段摘要">
            <Row gutter={[16, 16]}>
              <Col span={12}>
                <Typography.Text>当前阶段：{STEP_TITLES[progress.currentStepKey]}</Typography.Text>
              </Col>
              <Col span={12}>
                <Typography.Text>当前状态：{progress.currentSummary}</Typography.Text>
              </Col>
              <Col span={12}>
                <Typography.Text>旅程状态：{getStateLabel(selectedRecord?.journeyStatus)}</Typography.Text>
              </Col>
              <Col span={12}>
                <Typography.Text>提交时间：{formatDateTime(selectedRecord?.submittedAt)}</Typography.Text>
              </Col>
            </Row>
          </Card>

          <Card size="small" title="旅程步骤">
            {progress.steps.length ? (
              <Steps
                direction="vertical"
                items={progress.steps.map((item) => ({
                  title: item.title,
                  description: `${item.summary} · ${item.timeText}`,
                  status: item.status,
                }))}
              />
            ) : (
              <Typography.Text type="secondary">
                {timelineLoading ? '正在加载旅程步骤...' : '暂无旅程步骤'}
              </Typography.Text>
            )}
          </Card>

          <Card size="small" title="阶段说明">
            <Space direction="vertical" size="middle" style={{ width: '100%' }}>
              {progress.steps.map((item) => (
                <Card key={item.key} size="small">
                  <Space direction="vertical" size="small" style={{ width: '100%' }}>
                    <Space style={{ justifyContent: 'space-between', width: '100%' }} wrap>
                      <Typography.Text strong>{item.title}</Typography.Text>
                      {renderStepStatusTag(item.status)}
                    </Space>
                    <Typography.Text>{item.summary}</Typography.Text>
                    <Typography.Text type="secondary">{item.hint}</Typography.Text>
                    <Typography.Text type="secondary">{item.timeText}</Typography.Text>
                  </Space>
                </Card>
              ))}
            </Space>
          </Card>
        </Space>
      </Drawer>
    </Space>
  );
}
