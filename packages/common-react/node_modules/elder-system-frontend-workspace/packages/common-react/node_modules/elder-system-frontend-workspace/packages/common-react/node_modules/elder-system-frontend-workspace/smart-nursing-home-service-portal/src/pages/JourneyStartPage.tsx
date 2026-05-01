import { ExclamationCircleFilled } from '@ant-design/icons';
import { useEffect, useMemo, useState } from 'react';
import { Alert, Button, Card, Collapse, Form, Input, InputNumber, Select, Space, Table, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useNavigate } from 'react-router-dom';
import { listIntakeRecords, listIntakeRecordsByApplicant, startServiceJourney } from '../api/careOrchestrationApi';
import { extractApiErrorMessage } from '../api/client';
import { useUserStore } from '../stores/userStore';
import { StatusTag } from 'common-react';
import { ROUTE_PATHS } from '../constants/routes';
import type { IntakeRecord, StartServiceJourneyRequest } from '../types/care';

const OCCUPYING_STATUSES = new Set(['PENDING_ASSESSMENT', 'PENDING_HEALTH_ASSESSMENT', 'IN_SERVICE']);

function formatDateTime(value?: string): string {
  if (!value) {
    return '-';
  }
  return value.replace('T', ' ').slice(0, 19);
}

export default function JourneyStartPage() {
  const navigate = useNavigate();
  const [form] = Form.useForm<StartServiceJourneyRequest>();
  const { username: loginUsername } = useUserStore();
  const formElderId = Form.useWatch('elderId', form);

  const [loading, setLoading] = useState(false);
  const [intakeLoading, setIntakeLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [intakeRecords, setIntakeRecords] = useState<IntakeRecord[]>([]);
  const [queriedElderId, setQueriedElderId] = useState<number | null>(null);
  const [elderIdHint, setElderIdHint] = useState<string | null>(null);

  const handleElderIdChange = (value: number | null) => {
    if (value !== queriedElderId) {
      setQueriedElderId(null);
    }
    setElderIdHint(null);
  };

  const handleElderIdBlur = () => {
    if (typeof formElderId === 'number' && Number.isFinite(formElderId) && formElderId > 0) {
      setQueriedElderId(formElderId);
      return;
    }
    setQueriedElderId(null);
    setElderIdHint(null);
  };

  useEffect(() => {
    if (loginUsername) {
      form.setFieldsValue({ applicantName: loginUsername });
    }
  }, [form, loginUsername]);

  useEffect(() => {
    if (!loginUsername) {
      setIntakeRecords([]);
      setErrorMessage('未获取到当前登录用户，请重新登录');
      return;
    }

    let canceled = false;

    const loadRecords = async () => {
      setIntakeLoading(true);
      try {
        const records = queriedElderId
          ? await listIntakeRecords(queriedElderId)
          : await listIntakeRecordsByApplicant(loginUsername);
        if (!canceled) {
          setIntakeRecords(records);
          setErrorMessage(null);
          if (queriedElderId) {
            if (records.length > 0) {
              setElderIdHint(`已找到老人ID=${queriedElderId} 的受理记录（${records.length}条）`);
            } else {
              setElderIdHint(null);
            }
          }
        }
      } catch (error) {
        if (!canceled) {
          setIntakeRecords([]);
          if (queriedElderId) {
            setElderIdHint(null);
          }
          const message = extractApiErrorMessage(error, '查询受理记录失败');
          setErrorMessage(message);
        }
      } finally {
        if (!canceled) {
          setIntakeLoading(false);
        }
      }
    };

    void loadRecords();

    return () => {
      canceled = true;
    };
  }, [loginUsername, queriedElderId]);

  const hasOccupyingRecord = useMemo(
    () =>
      Boolean(queriedElderId && queriedElderId === formElderId) &&
      intakeRecords.some((record) => Boolean(record.journeyStatus && OCCUPYING_STATUSES.has(record.journeyStatus))),
    [formElderId, intakeRecords, queriedElderId],
  );

  const columns: ColumnsType<IntakeRecord> = [
    {
      title: '申请单ID',
      dataIndex: 'applicationId',
      key: 'applicationId',
      width: 140,
    },
    {
      title: '申请人',
      dataIndex: 'applicantName',
      key: 'applicantName',
      width: 160,
    },
    {
      title: '提交时间',
      dataIndex: 'submittedAt',
      key: 'submittedAt',
      width: 200,
      render: (value?: string) => formatDateTime(value),
    },
    {
      title: '旅程状态',
      dataIndex: 'journeyStatus',
      key: 'journeyStatus',
      width: 180,
      render: (status?: string) => <StatusTag status={status} />,
    },
    {
      title: '说明',
      dataIndex: 'message',
      key: 'message',
    },
  ];

  const handleSubmit = async (values: StartServiceJourneyRequest) => {
    setLoading(true);
    setErrorMessage(null);

    try {
      if (!loginUsername) {
        setErrorMessage('未获取到当前登录用户，请重新登录');
        return;
      }

      const latestRecords = await listIntakeRecords(values.elderId);
      setIntakeRecords(latestRecords);

      const blocked = latestRecords.some((record) =>
        Boolean(record.journeyStatus && OCCUPYING_STATUSES.has(record.journeyStatus)),
      );
      if (blocked) {
        setErrorMessage('该老人存在进行中的受理记录，不可重复发起申请');
        return;
      }

      const result = await startServiceJourney(values);
      sessionStorage.setItem('journeyResult', JSON.stringify(result));
      sessionStorage.setItem(
        'journeyContext',
        JSON.stringify({
          elderId: values.elderId,
          applicationId: result.applicationId,
          agreementId: result.agreementId,
          elderName: values.applicantName,
        }),
      );
      navigate(ROUTE_PATHS.HEALTH_CHECK, {
        replace: true,
        state: {
          elderId: values.elderId,
          agreementId: result.agreementId,
          elderName: values.applicantName,
          fromJourney: true,
        },
      });
    } catch (error) {
      const message = extractApiErrorMessage(error, '发起受理登记失败');
      setErrorMessage(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Typography.Title level={4} style={{ margin: 0 }}>
        申请受理登记（发起服务申请）
      </Typography.Title>

      <Card>
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          {errorMessage ? <Alert type="error" message={errorMessage} showIcon /> : null}
          <Alert
            type="info"
            showIcon
            message={`此页为申请受理登记唯一入口：提交申请后由管理端护士/责任医生录入健康体检表并继续评估。当前申请人：${loginUsername ?? '-'}`}
          />

          {hasOccupyingRecord ? (
            <Alert type="warning" showIcon message="存在进行中的受理记录，当前不可发起新的受理登记。" />
          ) : null}

          <Form<StartServiceJourneyRequest> form={form} layout="vertical" onFinish={handleSubmit}>
            <div style={{ marginBottom: 24 }}>
              <Collapse
                items={[
                  {
                    key: 'intake-records',
                    label: `正在受理（${intakeRecords.length}）`,
                    children: (
                      <Table<IntakeRecord>
                        rowKey={(record) => String(record.applicationId ?? `${record.elderId}-${record.submittedAt}`)}
                        loading={intakeLoading}
                        columns={columns}
                        dataSource={intakeRecords}
                        pagination={false}
                        locale={{
                          emptyText: !loginUsername
                            ? '未获取到登录用户'
                            : queriedElderId
                              ? '该老人暂无受理记录'
                              : '当前申请人暂无申请记录',
                        }}
                        scroll={{ x: 900 }}
                      />
                    ),
                  },
                ]}
              />
            </div>

            <Form.Item
              label="老人ID"
              name="elderId"
              rules={[{ required: true, message: '请输入老人ID' }]}
              extra={
                elderIdHint ? (
                  <Space size={6} style={{ color: '#cf1322' }}>
                    <ExclamationCircleFilled style={{ color: '#ff4d4f' }} />
                    <span>{elderIdHint}</span>
                  </Space>
                ) : undefined
              }
            >
              <InputNumber
                style={{ width: '100%' }}
                min={1}
                placeholder="例如：10001"
                onChange={handleElderIdChange}
                onBlur={handleElderIdBlur}
              />
            </Form.Item>

            <Form.Item label="监护人ID（可选）" name="guardianId">
              <InputNumber style={{ width: '100%' }} min={1} placeholder="例如：20001" />
            </Form.Item>

            <Form.Item
              label="申请人姓名"
              name="applicantName"
              rules={[{ required: true, message: '请输入申请人姓名' }]}
              initialValue={loginUsername ?? undefined}
            >
              <Input placeholder="请输入申请人姓名" />
            </Form.Item>

            <Form.Item
              label="联系电话"
              name="contactPhone"
              rules={[{ required: true, message: '请输入联系电话' }]}
            >
              <Input placeholder="请输入联系电话" />
            </Form.Item>

            <Form.Item
              label="服务场景"
              name="serviceScene"
              rules={[{ required: true, message: '请选择服务场景' }]}
            >
              <Select
                options={[
                  { label: '机构照护', value: 'INSTITUTION' },
                  { label: '居家照护', value: 'HOME' },
                  { label: '社区照护', value: 'COMMUNITY' },
                ]}
              />
            </Form.Item>

            <Form.Item
              label="服务诉求"
              name="serviceRequest"
              rules={[{ required: true, message: '请输入服务诉求' }]}
            >
              <Input.TextArea rows={4} placeholder="请描述护理服务诉求" />
            </Form.Item>

            <Button
              type="primary"
              htmlType="submit"
              loading={loading}
              disabled={!formElderId || intakeLoading || hasOccupyingRecord}
            >
              提交并发起旅程
            </Button>
          </Form>
        </Space>
      </Card>
    </Space>
  );
}
