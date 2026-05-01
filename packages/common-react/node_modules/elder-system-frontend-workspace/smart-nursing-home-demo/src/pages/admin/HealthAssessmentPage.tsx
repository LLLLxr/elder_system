import { useEffect, useState } from 'react';
import {
  Alert,
  Button,
  Card,
  Collapse,
  Form,
  Input,
  InputNumber,
  Select,
  Space,
  Table,
  Tag,
  Typography,
} from 'antd';
import { HealthCheckFormDetailModal } from 'common-react';
import type { ColumnsType } from 'antd/es/table';
import { continueJourneyAfterAssessment } from '../../api/admissionApi';
import { getCurrentAdminUsername } from '../../api/authApi';
import { extractApiErrorMessage } from '../../api/client';
import {
  getLatestAdminHealthCheckForm,
  listHealthAssessmentHistory,
  listPendingHealthAssessments,
  submitHealthAssessment,
} from '../../api/healthApi';
import type {
  HealthAssessmentRequest,
  HealthAssessmentSubmitRequest,
  HealthCheckForm,
  ServiceJourneyResult,
} from '../../types/care';

interface HealthAssessmentFormValues {
  passed: boolean;
  assessmentConclusion: string;
  assessor: string;
  responsibleDoctor: string;
  score: number;
}

const pendingColumns: ColumnsType<HealthAssessmentRequest> = [
  {
    title: '申请单ID',
    dataIndex: 'applicationId',
    key: 'applicationId',
    width: 120,
  },
  {
    title: '老人ID',
    dataIndex: 'elderId',
    key: 'elderId',
    width: 110,
  },
  {
    title: '协议ID',
    dataIndex: 'agreementId',
    key: 'agreementId',
    width: 120,
  },
  {
    title: '申请人',
    dataIndex: 'applicantName',
    key: 'applicantName',
    width: 120,
  },
  {
    title: '服务场景',
    dataIndex: 'serviceScene',
    key: 'serviceScene',
    width: 120,
    render: (value: string | undefined) => {
      if (value === 'INSTITUTION') {
        return '机构照护';
      }
      if (value === 'HOME') {
        return '居家照护';
      }
      if (value === 'COMMUNITY') {
        return '社区照护';
      }
      return value ?? '-';
    },
  },
  {
    title: '状态',
    dataIndex: 'assessmentStatus',
    key: 'assessmentStatus',
    width: 120,
    render: () => <Tag color="processing">待评估</Tag>,
  },
];

const historyColumns: ColumnsType<HealthAssessmentRequest> = [
  {
    title: '申请单ID',
    dataIndex: 'applicationId',
    key: 'applicationId',
    width: 120,
  },
  {
    title: '老人ID',
    dataIndex: 'elderId',
    key: 'elderId',
    width: 110,
  },
  {
    title: '状态',
    dataIndex: 'assessmentStatus',
    key: 'assessmentStatus',
    width: 120,
    render: (value: string | undefined) => {
      if (value === 'PASSED') {
        return <Tag color="success">通过</Tag>;
      }
      if (value === 'FAILED') {
        return <Tag color="error">未通过</Tag>;
      }
      return <Tag>{value ?? '-'}</Tag>;
    },
  },
  {
    title: '评分',
    dataIndex: 'score',
    key: 'score',
    width: 90,
  },
  {
    title: '评估结论',
    dataIndex: 'assessmentConclusion',
    key: 'assessmentConclusion',
  },
];

export default function HealthAssessmentPage() {
  const [loadingPending, setLoadingPending] = useState(false);
  const [loadingHistory, setLoadingHistory] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [pendingList, setPendingList] = useState<HealthAssessmentRequest[]>([]);
  const [historyList, setHistoryList] = useState<HealthAssessmentRequest[]>([]);
  const [selectedApplicationId, setSelectedApplicationId] = useState<number | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [lastResult, setLastResult] = useState<ServiceJourneyResult | null>(null);
  const [healthFormModalOpen, setHealthFormModalOpen] = useState(false);
  const [healthFormLoading, setHealthFormLoading] = useState(false);
  const [healthFormError, setHealthFormError] = useState<string | null>(null);
  const [healthFormDetail, setHealthFormDetail] = useState<HealthCheckForm | null>(null);
  const [activeRecord, setActiveRecord] = useState<HealthAssessmentRequest | null>(null);
  const [form] = Form.useForm<HealthAssessmentFormValues>();
  const currentAdminUsername = getCurrentAdminUsername() || 'UNKNOWN_ADMIN';

  const loadPending = async () => {
    setLoadingPending(true);
    try {
      const list = await listPendingHealthAssessments();
      setPendingList(list);
      if (!selectedApplicationId || !list.some((item) => item.applicationId === selectedApplicationId)) {
        setSelectedApplicationId(null);
      }
    } finally {
      setLoadingPending(false);
    }
  };

  const loadHistory = async () => {
    setLoadingHistory(true);
    try {
      const list = await listHealthAssessmentHistory();
      setHistoryList(list);
    } finally {
      setLoadingHistory(false);
    }
  };

  const loadAll = async () => {
    setErrorMessage(null);
    try {
      await Promise.all([loadPending(), loadHistory()]);
    } catch (error) {
      setErrorMessage(extractApiErrorMessage(error, '加载健康评估数据失败'));
    }
  };

  useEffect(() => {
    form.setFieldsValue({ assessor: currentAdminUsername });
    void loadAll();
  }, [currentAdminUsername, form]);

  const handleOpenHealthFormDetail = async (record: HealthAssessmentRequest) => {
    setActiveRecord(record);
    setHealthFormModalOpen(true);
    setHealthFormLoading(true);
    setHealthFormError(null);
    setHealthFormDetail(null);

    try {
      const detail = await getLatestAdminHealthCheckForm(record.elderId, record.agreementId);
      setHealthFormDetail(detail);
    } catch (error) {
      setHealthFormError(extractApiErrorMessage(error, '加载健康体检表失败'));
    } finally {
      setHealthFormLoading(false);
    }
  };

  const pendingColumnsWithActions: ColumnsType<HealthAssessmentRequest> = [
    ...pendingColumns,
    {
      title: '操作',
      key: 'actions',
      width: 140,
      fixed: 'right',
      render: (_value, record) => (
        <Button
          type="link"
          onClick={(event) => {
            event.stopPropagation();
            void handleOpenHealthFormDetail(record);
          }}
        >
          查看体检表
        </Button>
      ),
    },
  ];

  const handleSubmit = async (values: HealthAssessmentFormValues) => {
    if (!selectedApplicationId) {
      setErrorMessage('请先在待评估健康评估请求列表中选择一条记录');
      return;
    }

    setSubmitting(true);
    setErrorMessage(null);
    setLastResult(null);

    try {
      const payload: HealthAssessmentSubmitRequest = {
        applicationId: selectedApplicationId,
        passed: values.passed,
        assessmentConclusion: values.assessmentConclusion,
        assessor: currentAdminUsername,
        responsibleDoctor: values.responsibleDoctor,
        score: values.score,
      };

      const assessed = await submitHealthAssessment(payload);

      if (assessed.assessmentStatus === 'PASSED') {
        const result = await continueJourneyAfterAssessment(selectedApplicationId);
        setLastResult(result);
      }

      await loadAll();
      setSelectedApplicationId(null);
      form.resetFields(['assessmentConclusion', 'score']);
    } catch (error) {
      setErrorMessage(extractApiErrorMessage(error, '提交健康评估失败'));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Typography.Title level={4} style={{ margin: 0 }}>
        健康评估
      </Typography.Title>

      <Alert
        type="info"
        showIcon
        message="本页仅处理用户端已提交申请的健康评估，不提供申请受理登记。"
      />
      <Alert
        type="info"
        showIcon
        message="请先在待评估健康评估请求列表点选一行（高亮）后再提交，下方表单将按该高亮行完成评估。"
      />

      {errorMessage ? <Alert type="error" message={errorMessage} showIcon /> : null}

      {lastResult ? (
        <Alert
          type="success"
          showIcon
          message={`健康评估通过，状态：${lastResult.finalStatus ?? '-'}，协议ID：${lastResult.agreementId ?? '-'}`}
        />
      ) : null}

      <Collapse
        items={[
          {
            key: 'pending-health-assessment',
            label: `待评估健康评估请求（${pendingList.length}）`,
            children: (
              <Table<HealthAssessmentRequest>
                rowKey={(record) => String(record.applicationId)}
                loading={loadingPending}
                columns={pendingColumnsWithActions}
                dataSource={pendingList}
                pagination={{ pageSize: 6 }}
                rowSelection={{
                  type: 'radio',
                  selectedRowKeys: selectedApplicationId ? [String(selectedApplicationId)] : [],
                  onChange: (selectedRowKeys) => {
                    const selected = Number(selectedRowKeys[0]);
                    if (Number.isFinite(selected)) {
                      setErrorMessage(null);
                      setSelectedApplicationId(selected);
                    }
                  },
                }}
                onRow={(record) => ({
                  onClick: () => {
                    const selected = record.applicationId as number;
                    setErrorMessage(null);
                    setSelectedApplicationId(selected);
                  },
                })}
                rowClassName={(record) =>
                  record.applicationId === selectedApplicationId ? 'ant-table-row-selected' : ''
                }
              />
            ),
          },
          {
            key: 'health-assessment-history',
            label: '已评估请求历史',
            children: (
              <Table<HealthAssessmentRequest>
                rowKey={(record) => `${record.applicationId}-${record.healthAssessedAt ?? ''}`}
                loading={loadingHistory}
                columns={historyColumns}
                dataSource={historyList}
                pagination={{ pageSize: 6 }}
              />
            ),
          },
        ]}
      />

      <Card title="提交健康评估">
        <Form<HealthAssessmentFormValues> form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item
            label="当前选中申请"
          >
            <Input value={selectedApplicationId ?? ''} disabled placeholder="请先在上方待评估健康评估请求列表选择申请单" />
          </Form.Item>

          <Form.Item
            label="评估结论"
            name="passed"
            rules={[{ required: true, message: '请选择评估结论' }]}
          >
            <Select
              options={[
                { label: '通过（允许继续签约）', value: true },
                { label: '不通过（终止服务）', value: false },
              ]}
            />
          </Form.Item>

          <Form.Item
            label="评估说明"
            name="assessmentConclusion"
            rules={[{ required: true, message: '请输入评估说明' }]}
          >
            <Input.TextArea rows={4} placeholder="记录健康评估依据与结论" />
          </Form.Item>

          <Form.Item
            label="评估人"
            name="assessor"
            rules={[{ required: true, message: '未获取到当前登录用户' }]}
          >
            <Input disabled />
          </Form.Item>

          <Form.Item
            label="责任医生"
            name="responsibleDoctor"
            rules={[{ required: true, message: '请输入责任医生' }]}
          >
            <Input placeholder="请输入责任医生姓名" />
          </Form.Item>

          <Form.Item
            label="评分"
            name="score"
            rules={[{ required: true, message: '请输入评分' }]}
            initialValue={75}
          >
            <InputNumber min={0} max={100} style={{ width: '100%' }} />
          </Form.Item>

          <Button type="primary" htmlType="submit" loading={submitting}>
            完成健康评估
          </Button>
        </Form>
      </Card>

      <HealthCheckFormDetailModal
        title={`体检表详情（申请单ID：${activeRecord?.applicationId ?? '-'}）`}
        open={healthFormModalOpen}
        onCancel={() => setHealthFormModalOpen(false)}
        loading={healthFormLoading}
        error={healthFormError}
        detail={healthFormDetail}
      />
    </Space>
  );
}
