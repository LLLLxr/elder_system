import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  Alert,
  Button,
  Card,
  Col,
  Drawer,
  Form,
  Input,
  InputNumber,
  Modal,
  Row,
  Select,
  Space,
  Statistic,
  Table,
  Tabs,
  Tag,
  Typography,
  message,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { TablePaginationConfig } from 'antd/es/table';
import type { FilterValue, SorterResult } from 'antd/es/table/interface';
import {
  getJourneyTaskOverview,
  listJourneyTaskTimeline,
  listJourneyTasks,
  listJourneyTransitionLogsByAgreement,
  listJourneyTransitionLogsByApplication,
  returnJourneyStep,
} from '../../api/careOrchestrationApi';
import { extractApiErrorMessage } from '../../api/client';
import type {
  JourneyTaskItem,
  JourneyTaskOverview,
  JourneyTransitionLogItem,
  ListJourneyTasksParams,
  ReturnJourneyStepRequest,
} from '../../types/care';

interface FilterFormValues {
  applicationId?: number;
  elderId?: number;
  agreementId?: number;
  taskType?: string;
  assigneeRole?: string;
  statuses?: string[];
}

interface TaskQueryState extends ListJourneyTasksParams {
  statuses: string[];
  page: number;
  size: number;
  sortBy: string;
  sortOrder: 'asc' | 'desc';
}

const DEFAULT_STATUSES = ['PENDING', 'OVERDUE'];
const DEFAULT_QUERY: TaskQueryState = {
  statuses: DEFAULT_STATUSES,
  page: 0,
  size: 20,
  sortBy: 'dueAt',
  sortOrder: 'asc',
};

const taskTypeOptions = [
  { label: '需求评估', value: 'ADMISSION_ASSESSMENT' },
  { label: '健康评估', value: 'HEALTH_ASSESSMENT' },
];

const statusOptions = [
  { label: '待处理', value: 'PENDING' },
  { label: '已逾期', value: 'OVERDUE' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' },
];

const assigneeRoleOptions = [
  { label: '客服', value: 'CUSTOMER_SERVICE' },
  { label: '医生', value: 'DOCTOR' },
  { label: '护士', value: 'NURSE' },
  { label: '运营', value: 'OPERATOR' },
  { label: '管理员', value: 'ADMIN' },
];

const targetStateOptions = [
  { label: '待需求评估', value: 'PENDING_ASSESSMENT' },
  { label: '待健康评估', value: 'PENDING_HEALTH_ASSESSMENT' },
  { label: '待签约', value: 'PENDING_AGREEMENT' },
  { label: '服务中', value: 'IN_SERVICE' },
  { label: '待改进', value: 'IMPROVEMENT_REQUIRED' },
  { label: '已续约', value: 'RENEWED' },
  { label: '已终止', value: 'TERMINATED' },
];

const sortFieldMap: Record<string, string> = {
  taskType: 'taskType',
  status: 'status',
  dueAt: 'dueAt',
  completedAt: 'completedAt',
  createdAt: 'createdAt',
};

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

function renderText(value?: string | number | null): string | number {
  if (value == null || value === '') {
    return '-';
  }
  return value;
}

function renderStatusTag(status?: string) {
  if (!status) {
    return '-';
  }

  const colorMap: Record<string, string> = {
    PENDING: 'processing',
    OVERDUE: 'error',
    COMPLETED: 'success',
    CANCELLED: 'default',
  };

  return <Tag color={colorMap[status] ?? 'default'}>{status}</Tag>;
}

function toSortOrder(sortBy: string, currentSortBy: string, sortOrder: 'asc' | 'desc') {
  if (sortBy !== currentSortBy) {
    return undefined;
  }
  return sortOrder === 'asc' ? 'ascend' : 'descend';
}

export default function JourneyTaskBoardPage() {
  const [filterForm] = Form.useForm<FilterFormValues>();
  const [returnForm] = Form.useForm<ReturnJourneyStepRequest>();

  const [query, setQuery] = useState<TaskQueryState>(DEFAULT_QUERY);
  const [overview, setOverview] = useState<JourneyTaskOverview | null>(null);
  const [overviewLoading, setOverviewLoading] = useState(false);
  const [overviewError, setOverviewError] = useState<string | null>(null);

  const [tasks, setTasks] = useState<JourneyTaskItem[]>([]);
  const [total, setTotal] = useState(0);
  const [listLoading, setListLoading] = useState(false);
  const [listError, setListError] = useState<string | null>(null);

  const [detailOpen, setDetailOpen] = useState(false);
  const [selectedTask, setSelectedTask] = useState<JourneyTaskItem | null>(null);
  const [timeline, setTimeline] = useState<JourneyTaskItem[]>([]);
  const [applicationLogs, setApplicationLogs] = useState<JourneyTransitionLogItem[]>([]);
  const [agreementLogs, setAgreementLogs] = useState<JourneyTransitionLogItem[]>([]);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detailError, setDetailError] = useState<string | null>(null);

  const [returnModalOpen, setReturnModalOpen] = useState(false);
  const [returnLoading, setReturnLoading] = useState(false);

  const loadOverview = useCallback(async (currentQuery: TaskQueryState) => {
    setOverviewLoading(true);
    setOverviewError(null);
    try {
      const data = await getJourneyTaskOverview({
        applicationId: currentQuery.applicationId,
        elderId: currentQuery.elderId,
        agreementId: currentQuery.agreementId,
        taskType: currentQuery.taskType,
        assigneeRole: currentQuery.assigneeRole,
        statuses: currentQuery.statuses,
      });
      setOverview(data);
    } catch (error) {
      setOverview(null);
      setOverviewError(extractApiErrorMessage(error, '加载任务概览失败'));
    } finally {
      setOverviewLoading(false);
    }
  }, []);

  const loadTasks = useCallback(async (currentQuery: TaskQueryState) => {
    setListLoading(true);
    setListError(null);
    try {
      const data = await listJourneyTasks(currentQuery);
      setTasks(data.items);
      setTotal(data.total);
    } catch (error) {
      setTasks([]);
      setTotal(0);
      setListError(extractApiErrorMessage(error, '加载任务列表失败'));
    } finally {
      setListLoading(false);
    }
  }, []);

  const loadTaskDetail = useCallback(async (task: JourneyTaskItem) => {
    if (!task.applicationId) {
      setTimeline([]);
      setApplicationLogs([]);
      setAgreementLogs([]);
      setDetailError('当前任务缺少申请ID，无法查看详情');
      setDetailLoading(false);
      return;
    }

    setDetailLoading(true);
    setDetailError(null);
    try {
      const [timelineData, applicationLogData, agreementLogData] = await Promise.all([
        listJourneyTaskTimeline(task.applicationId),
        listJourneyTransitionLogsByApplication(task.applicationId),
        task.agreementId ? listJourneyTransitionLogsByAgreement(task.agreementId) : Promise.resolve([]),
      ]);
      setTimeline(timelineData);
      setApplicationLogs(applicationLogData);
      setAgreementLogs(agreementLogData);
    } catch (error) {
      setTimeline([]);
      setApplicationLogs([]);
      setAgreementLogs([]);
      setDetailError(extractApiErrorMessage(error, '加载任务详情失败'));
    } finally {
      setDetailLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadOverview(query);
    void loadTasks(query);
  }, [loadOverview, loadTasks, query]);

  const handleSearch = async () => {
    try {
      const values = await filterForm.validateFields();
      setQuery((current) => ({
        ...current,
        applicationId: values.applicationId,
        elderId: values.elderId,
        agreementId: values.agreementId,
        taskType: values.taskType,
        assigneeRole: values.assigneeRole,
        statuses: values.statuses?.length ? values.statuses : DEFAULT_STATUSES,
        page: 0,
      }));
    } catch {
      return;
    }
  };

  const handleReset = () => {
    const values: FilterFormValues = {
      statuses: DEFAULT_STATUSES,
    };
    filterForm.setFieldsValue(values);
    setQuery({ ...DEFAULT_QUERY, statuses: [...DEFAULT_STATUSES] });
  };

  const handleRefresh = () => {
    void loadOverview(query);
    void loadTasks(query);
    if (detailOpen && selectedTask) {
      void loadTaskDetail(selectedTask);
    }
  };

  const handleTableChange = (
    pagination: TablePaginationConfig,
    _filters: Record<string, FilterValue | null>,
    sorter: SorterResult<JourneyTaskItem> | SorterResult<JourneyTaskItem>[],
  ) => {
    const singleSorter = Array.isArray(sorter) ? sorter[0] : sorter;
    const nextSortBy =
      singleSorter && typeof singleSorter.field === 'string' && sortFieldMap[singleSorter.field]
        ? sortFieldMap[singleSorter.field]
        : query.sortBy;
    const nextSortOrder = singleSorter?.order === 'descend' ? 'desc' : 'asc';

    setQuery((current) => ({
      ...current,
      page: (pagination.current ?? 1) - 1,
      size: pagination.pageSize ?? current.size,
      sortBy: nextSortBy,
      sortOrder: singleSorter?.order ? nextSortOrder : current.sortOrder,
    }));
  };

  const handleOpenDetail = (task: JourneyTaskItem) => {
    setSelectedTask(task);
    setDetailOpen(true);
    void loadTaskDetail(task);
  };

  const handleOpenReturn = (task: JourneyTaskItem) => {
    if (!task.applicationId) {
      message.error('当前任务缺少申请ID，无法回退');
      return;
    }
    setSelectedTask(task);
    returnForm.setFieldsValue({
      applicationId: task.applicationId,
      targetState: task.currentState,
      reason: undefined,
    });
    setReturnModalOpen(true);
  };

  const handleSubmitReturn = async () => {
    try {
      const values = await returnForm.validateFields();
      setReturnLoading(true);
      const result = await returnJourneyStep(values);
      message.success(result.message || '回退成功');
      setReturnModalOpen(false);
      await Promise.all([loadOverview(query), loadTasks(query)]);
      if (detailOpen && selectedTask?.applicationId === values.applicationId && selectedTask) {
        await loadTaskDetail(selectedTask);
      }
    } catch (error) {
      if (error && typeof error === 'object' && 'errorFields' in error) {
        return;
      }
      message.error(extractApiErrorMessage(error, '执行回退失败'));
    } finally {
      setReturnLoading(false);
    }
  };

  const summaryCards = useMemo(
    () => [
      { key: 'pending', title: '待处理', value: overview?.pendingCount ?? 0 },
      { key: 'overdue', title: '已逾期', value: overview?.overdueCount ?? 0 },
      { key: 'completed', title: '已完成', value: overview?.completedCount ?? 0 },
      { key: 'cancelled', title: '已取消', value: overview?.cancelledCount ?? 0 },
    ],
    [overview],
  );

  const columns = useMemo<ColumnsType<JourneyTaskItem>>(
    () => [
      {
        title: '任务ID',
        dataIndex: 'taskId',
        key: 'taskId',
        width: 100,
        render: (value?: number) => renderText(value),
      },
      {
        title: '申请ID',
        dataIndex: 'applicationId',
        key: 'applicationId',
        width: 110,
        render: (value?: number) => renderText(value),
      },
      {
        title: '协议ID',
        dataIndex: 'agreementId',
        key: 'agreementId',
        width: 110,
        render: (value?: number) => renderText(value),
      },
      {
        title: '老人ID',
        dataIndex: 'elderId',
        key: 'elderId',
        width: 110,
        render: (value?: number) => renderText(value),
      },
      {
        title: '任务类型',
        dataIndex: 'taskType',
        key: 'taskType',
        width: 180,
        sorter: true,
        sortOrder: toSortOrder('taskType', query.sortBy, query.sortOrder),
        render: (value?: string) => renderText(value),
      },
      {
        title: '当前状态',
        dataIndex: 'currentState',
        key: 'currentState',
        width: 190,
        render: (value?: string) => renderText(value),
      },
      {
        title: '处理角色',
        dataIndex: 'assigneeRole',
        key: 'assigneeRole',
        width: 140,
        render: (value?: string) => renderText(value),
      },
      {
        title: '任务状态',
        dataIndex: 'status',
        key: 'status',
        width: 120,
        sorter: true,
        sortOrder: toSortOrder('status', query.sortBy, query.sortOrder),
        render: (value?: string) => renderStatusTag(value),
      },
      {
        title: '截止时间',
        dataIndex: 'dueAt',
        key: 'dueAt',
        width: 180,
        sorter: true,
        sortOrder: toSortOrder('dueAt', query.sortBy, query.sortOrder),
        render: (value?: string) => formatDateTime(value),
      },
      {
        title: '完成时间',
        dataIndex: 'completedAt',
        key: 'completedAt',
        width: 180,
        sorter: true,
        sortOrder: toSortOrder('completedAt', query.sortBy, query.sortOrder),
        render: (value?: string) => formatDateTime(value),
      },
      {
        title: '创建时间',
        dataIndex: 'createdAt',
        key: 'createdAt',
        width: 180,
        sorter: true,
        sortOrder: toSortOrder('createdAt', query.sortBy, query.sortOrder),
        render: (value?: string) => formatDateTime(value),
      },
      {
        title: '操作',
        key: 'actions',
        fixed: 'right',
        width: 180,
        render: (_value, record) => (
          <Space size="small" wrap>
            <Button type="link" onClick={() => handleOpenDetail(record)}>
              查看详情
            </Button>
            <Button type="link" onClick={() => handleOpenReturn(record)} disabled={!record.applicationId}>
              回退
            </Button>
          </Space>
        ),
      },
    ],
    [query.sortBy, query.sortOrder],
  );

  const timelineColumns = useMemo<ColumnsType<JourneyTaskItem>>(
    () => [
      {
        title: '任务ID',
        dataIndex: 'taskId',
        key: 'taskId',
        width: 100,
        render: (value?: number) => renderText(value),
      },
      {
        title: '任务类型',
        dataIndex: 'taskType',
        key: 'taskType',
        width: 180,
        render: (value?: string) => renderText(value),
      },
      {
        title: '当前状态',
        dataIndex: 'currentState',
        key: 'currentState',
        width: 180,
        render: (value?: string) => renderText(value),
      },
      {
        title: '任务状态',
        dataIndex: 'status',
        key: 'status',
        width: 120,
        render: (value?: string) => renderStatusTag(value),
      },
      {
        title: '创建时间',
        dataIndex: 'createdAt',
        key: 'createdAt',
        width: 180,
        render: (value?: string) => formatDateTime(value),
      },
      {
        title: '完成时间',
        dataIndex: 'completedAt',
        key: 'completedAt',
        width: 180,
        render: (value?: string) => formatDateTime(value),
      },
    ],
    [],
  );

  const logColumns = useMemo<ColumnsType<JourneyTransitionLogItem>>(
    () => [
      {
        title: '日志ID',
        dataIndex: 'logId',
        key: 'logId',
        width: 100,
        render: (value?: number) => renderText(value),
      },
      {
        title: '事件',
        dataIndex: 'journeyEvent',
        key: 'journeyEvent',
        width: 200,
        render: (value?: string) => renderText(value),
      },
      {
        title: '来源状态',
        dataIndex: 'fromState',
        key: 'fromState',
        width: 180,
        render: (value?: string) => renderText(value),
      },
      {
        title: '目标状态',
        dataIndex: 'toState',
        key: 'toState',
        width: 180,
        render: (value?: string) => renderText(value),
      },
      {
        title: '原因',
        dataIndex: 'reason',
        key: 'reason',
        width: 200,
        render: (value?: string) => renderText(value),
      },
      {
        title: '操作人',
        dataIndex: 'createdBy',
        key: 'createdBy',
        width: 140,
        render: (value?: string) => renderText(value),
      },
      {
        title: '时间',
        dataIndex: 'transitionTime',
        key: 'transitionTime',
        width: 180,
        render: (value?: string) => formatDateTime(value),
      },
    ],
    [],
  );

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Space style={{ width: '100%', justifyContent: 'space-between' }} align="start">
        <div>
          <Typography.Title level={4} style={{ margin: 0 }}>
            服务旅程任务看板
          </Typography.Title>
          <Typography.Text type="secondary">后台管理端可按申请、协议、角色与状态筛选全部旅程任务，并支持查看详情与回退。</Typography.Text>
        </div>
        <Button onClick={handleRefresh}>刷新</Button>
      </Space>

      {overviewError ? <Alert type="error" message={overviewError} showIcon /> : null}
      {listError ? <Alert type="error" message={listError} showIcon /> : null}

      <Row gutter={[16, 16]}>
        {summaryCards.map((item) => (
          <Col key={item.key} xs={24} sm={12} lg={6}>
            <Card loading={overviewLoading}>
              <Statistic title={item.title} value={item.value} />
            </Card>
          </Col>
        ))}
      </Row>

      <Card title="筛选条件">
        <Form<FilterFormValues>
          form={filterForm}
          layout="vertical"
          initialValues={{ statuses: DEFAULT_STATUSES }}
        >
          <Row gutter={16}>
            <Col xs={24} md={8} lg={6}>
              <Form.Item name="applicationId" label="申请ID">
                <InputNumber min={1} style={{ width: '100%' }} placeholder="输入申请ID" />
              </Form.Item>
            </Col>
            <Col xs={24} md={8} lg={6}>
              <Form.Item name="agreementId" label="协议ID">
                <InputNumber min={1} style={{ width: '100%' }} placeholder="输入协议ID" />
              </Form.Item>
            </Col>
            <Col xs={24} md={8} lg={6}>
              <Form.Item name="elderId" label="老人ID">
                <InputNumber min={1} style={{ width: '100%' }} placeholder="输入老人ID" />
              </Form.Item>
            </Col>
            <Col xs={24} md={8} lg={6}>
              <Form.Item name="taskType" label="任务类型">
                <Select allowClear options={taskTypeOptions} placeholder="选择任务类型" />
              </Form.Item>
            </Col>
            <Col xs={24} md={8} lg={6}>
              <Form.Item name="assigneeRole" label="处理角色">
                <Select allowClear options={assigneeRoleOptions} placeholder="选择处理角色" />
              </Form.Item>
            </Col>
            <Col xs={24} md={16} lg={12}>
              <Form.Item name="statuses" label="任务状态">
                <Select mode="multiple" options={statusOptions} placeholder="选择任务状态" />
              </Form.Item>
            </Col>
          </Row>
          <Space>
            <Button type="primary" onClick={() => void handleSearch()}>
              查询
            </Button>
            <Button onClick={handleReset}>重置</Button>
          </Space>
        </Form>
      </Card>

      <Card title="任务列表">
        <Table<JourneyTaskItem>
          rowKey={(record) => String(record.taskId ?? `${record.applicationId ?? 'app'}-${record.createdAt ?? 'created'}`)}
          loading={listLoading}
          columns={columns}
          dataSource={tasks}
          onChange={handleTableChange}
          pagination={{
            current: query.page + 1,
            pageSize: query.size,
            total,
            showSizeChanger: true,
            showTotal: (currentTotal) => `共 ${currentTotal} 条`,
          }}
          scroll={{ x: 1800 }}
          locale={{ emptyText: listLoading ? '正在加载任务列表...' : '暂无任务数据' }}
        />
      </Card>

      <Drawer
        title={selectedTask?.taskId ? `任务详情 #${selectedTask.taskId}` : '任务详情'}
        width={960}
        open={detailOpen}
        onClose={() => setDetailOpen(false)}
        destroyOnClose
      >
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          {selectedTask ? (
            <Card size="small" title="任务信息">
              <Row gutter={[16, 16]}>
                <Col span={8}><Typography.Text>申请ID：{renderText(selectedTask.applicationId)}</Typography.Text></Col>
                <Col span={8}><Typography.Text>协议ID：{renderText(selectedTask.agreementId)}</Typography.Text></Col>
                <Col span={8}><Typography.Text>老人ID：{renderText(selectedTask.elderId)}</Typography.Text></Col>
                <Col span={8}><Typography.Text>任务类型：{renderText(selectedTask.taskType)}</Typography.Text></Col>
                <Col span={8}><Typography.Text>当前状态：{renderText(selectedTask.currentState)}</Typography.Text></Col>
                <Col span={8}><Typography.Text>任务状态：{selectedTask.status ? renderStatusTag(selectedTask.status) : '-'}</Typography.Text></Col>
              </Row>
            </Card>
          ) : null}

          {detailError ? <Alert type="error" message={detailError} showIcon /> : null}

          <Tabs
            items={[
              {
                key: 'timeline',
                label: '任务时间线',
                children: (
                  <Table<JourneyTaskItem>
                    rowKey={(record) => String(record.taskId ?? `${record.taskType ?? 'task'}-${record.createdAt ?? 'created'}`)}
                    loading={detailLoading}
                    columns={timelineColumns}
                    dataSource={timeline}
                    pagination={false}
                    scroll={{ x: 900 }}
                    locale={{ emptyText: detailLoading ? '正在加载时间线...' : '暂无时间线数据' }}
                  />
                ),
              },
              {
                key: 'logs',
                label: '迁移日志',
                children: (
                  <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                    <Card size="small" title="按申请日志">
                      <Table<JourneyTransitionLogItem>
                        rowKey={(record) => String(record.logId ?? `${record.transitionTime ?? 'time'}-${record.journeyEvent ?? 'event'}`)}
                        loading={detailLoading}
                        columns={logColumns}
                        dataSource={applicationLogs}
                        pagination={false}
                        scroll={{ x: 1200 }}
                        locale={{ emptyText: detailLoading ? '正在加载申请日志...' : '暂无申请日志数据' }}
                      />
                    </Card>
                    {selectedTask?.agreementId ? (
                      <Card size="small" title="按协议日志">
                        <Table<JourneyTransitionLogItem>
                          rowKey={(record) => String(record.logId ?? `${record.transitionTime ?? 'time'}-${record.toState ?? 'state'}`)}
                          loading={detailLoading}
                          columns={logColumns}
                          dataSource={agreementLogs}
                          pagination={false}
                          scroll={{ x: 1200 }}
                          locale={{ emptyText: detailLoading ? '正在加载协议日志...' : '暂无协议日志数据' }}
                        />
                      </Card>
                    ) : null}
                  </Space>
                ),
              },
            ]}
          />
        </Space>
      </Drawer>

      <Modal
        title={selectedTask?.taskId ? `回退任务 #${selectedTask.taskId}` : '回退任务'}
        open={returnModalOpen}
        onCancel={() => setReturnModalOpen(false)}
        onOk={() => void handleSubmitReturn()}
        confirmLoading={returnLoading}
        destroyOnClose
      >
        <Space direction="vertical" style={{ width: '100%' }}>
          {selectedTask ? (
            <Alert
              type="info"
              showIcon
              message={`当前任务状态：${selectedTask.currentState ?? '-'}，申请ID：${selectedTask.applicationId ?? '-'}`}
            />
          ) : null}
          <Form<ReturnJourneyStepRequest> form={returnForm} layout="vertical">
            <Form.Item name="applicationId" hidden>
              <InputNumber />
            </Form.Item>
            <Form.Item name="targetState" label="回退目标状态" rules={[{ required: true, message: '请选择目标状态' }]}>
              <Select options={targetStateOptions} placeholder="选择回退目标状态" />
            </Form.Item>
            <Form.Item name="reason" label="回退原因">
              <Input.TextArea rows={4} placeholder="输入回退原因，便于日志追踪" />
            </Form.Item>
          </Form>
        </Space>
      </Modal>
    </Space>
  );
}
