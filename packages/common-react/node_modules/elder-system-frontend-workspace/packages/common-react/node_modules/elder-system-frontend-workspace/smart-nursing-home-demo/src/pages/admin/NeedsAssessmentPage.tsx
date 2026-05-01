import { useEffect, useState } from 'react';
import {
  Alert,
  Button,
  Card,
  Collapse,
  Form,
  Input,
  Select,
  Space,
  Table,
  Tag,
  Typography,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  assessServiceApplication,
  continueJourneyAfterAssessment,
  listPendingAssessments,
} from '../../api/admissionApi';
import { getCurrentAdminUsername } from '../../api/authApi';
import { extractApiErrorMessage } from '../../api/client';
import type {
  EligibilityAssessmentRequest,
  ServiceApplication,
  ServiceJourneyResult,
} from '../../types/care';

interface AssessmentFormValues {
  eligible: boolean;
  assessmentConclusion: string;
  assessor: string;
}

const columns: ColumnsType<ServiceApplication> = [
  {
    title: '申请单ID',
    dataIndex: 'applicationId',
    key: 'applicationId',
    width: 120,
  },
  {
    title: '申请人',
    dataIndex: 'applicantName',
    key: 'applicantName',
    width: 120,
  },
  {
    title: '老人ID',
    dataIndex: 'elderId',
    key: 'elderId',
    width: 100,
  },
  {
    title: '场景',
    dataIndex: 'serviceScene',
    key: 'serviceScene',
    width: 120,
    render: (value: string) => {
      if (value === 'INSTITUTION') {
        return '机构服务';
      }
      if (value === 'HOME') {
        return '居家服务';
      }
      if (value === 'COMMUNITY') {
        return '社区服务';
      }
      return value;
    },
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 120,
    render: (value: string | undefined) => {
      if (value === 'SUBMITTED') {
        return <Tag color="processing">待评估</Tag>;
      }
      if (value === 'PASSED') {
        return <Tag color="success">评估通过</Tag>;
      }
      if (value === 'FAILED') {
        return <Tag color="error">评估未通过</Tag>;
      }
      return <Tag>{value ?? '-'}</Tag>;
    },
  },
  {
    title: '服务诉求',
    dataIndex: 'serviceRequest',
    key: 'serviceRequest',
  },
];

export default function NeedsAssessmentPage() {
  const [loadingList, setLoadingList] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [pendingList, setPendingList] = useState<ServiceApplication[]>([]);
  const [selectedApplicationId, setSelectedApplicationId] = useState<number | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [lastResult, setLastResult] = useState<ServiceJourneyResult | null>(null);
  const [form] = Form.useForm<AssessmentFormValues>();
  const currentAdminUsername = getCurrentAdminUsername() || 'UNKNOWN_ADMIN';

  const loadPending = async () => {
    setLoadingList(true);
    setErrorMessage(null);
    try {
      const list = await listPendingAssessments();
      setPendingList(list);
      if (!selectedApplicationId || !list.some((item) => item.applicationId === selectedApplicationId)) {
        setSelectedApplicationId(null);
      }
    } catch (error) {
      setErrorMessage(extractApiErrorMessage(error, '加载待评估需求失败'));
    } finally {
      setLoadingList(false);
    }
  };

  useEffect(() => {
    form.setFieldsValue({ assessor: currentAdminUsername });
    void loadPending();
  }, [currentAdminUsername, form]);

  const handleSubmit = async (values: AssessmentFormValues) => {
    if (!selectedApplicationId) {
      setErrorMessage('请先在待评估需求列表中选择一条记录');
      return;
    }

    setSubmitting(true);
    setErrorMessage(null);
    setLastResult(null);
    try {
      const assessmentPayload: EligibilityAssessmentRequest = {
        applicationId: selectedApplicationId,
        eligible: values.eligible,
        assessmentConclusion: values.assessmentConclusion,
        assessor: currentAdminUsername,
      };

      const assessed = await assessServiceApplication(assessmentPayload);

      if (assessed.status === 'PASSED') {
        const journey = await continueJourneyAfterAssessment(selectedApplicationId);
        setLastResult(journey);
      }

      await loadPending();
      setSelectedApplicationId(null);
      form.resetFields(['assessmentConclusion']);
    } catch (error) {
      setErrorMessage(extractApiErrorMessage(error, '需求评估提交失败'));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Typography.Title level={4} style={{ margin: 0 }}>
        需求评估
      </Typography.Title>

      <Alert
        type="info"
        showIcon
        message="本页仅处理用户端已提交申请的需求评估，不提供申请受理登记。"
      />
      <Alert
        type="info"
        showIcon
        message="请先在待评估列表点选一行（高亮）后再提交，下方表单将按该高亮行完成评估。"
      />

      {errorMessage ? <Alert type="error" message={errorMessage} showIcon /> : null}

      {lastResult ? (
        <Alert
          type="success"
          showIcon
          message={`评估通过，用户可继续签约。协议ID：${lastResult.agreementId ?? '-'}，状态：${lastResult.finalStatus ?? '-'}`}
        />
      ) : null}

      <Collapse
        items={[
          {
            key: 'pending-list',
            label: `待评估需求列表（${pendingList.length}）`,
            children: (
              <Table<ServiceApplication>
                rowKey={(record) => String(record.applicationId)}
                loading={loadingList}
                columns={columns}
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
        ]}
      />

      <Card title="提交需求评估">
        <Form<AssessmentFormValues> form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item
            label="当前选中申请"
          >
            <Input value={selectedApplicationId ?? ''} disabled placeholder="请先在上方待评估列表选择申请单" />
          </Form.Item>

          <Form.Item
            label="评估结论"
            name="eligible"
            rules={[{ required: true, message: '请选择评估结论' }]}
          >
            <Select
              options={[
                { label: '通过（允许继续签订服务协议）', value: true },
                { label: '不通过（终止服务）', value: false },
              ]}
            />
          </Form.Item>

          <Form.Item
            label="评估说明"
            name="assessmentConclusion"
            rules={[{ required: true, message: '请输入评估说明' }]}
          >
            <Input.TextArea rows={4} placeholder="记录评估依据与结论" />
          </Form.Item>

          <Form.Item
            label="评估人"
            name="assessor"
            rules={[{ required: true, message: '未获取到当前登录用户' }]}
          >
            <Input disabled />
          </Form.Item>

          <Button type="primary" htmlType="submit" loading={submitting}>
            完成需求评估
          </Button>
        </Form>
      </Card>
    </Space>
  );
}
