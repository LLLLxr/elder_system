import { useCallback, useEffect, useMemo, useState } from 'react';
import { Alert, Button, Card, Collapse, InputNumber, Space, Table, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useLocation, useNavigate } from 'react-router-dom';
import type { MouseEvent } from 'react';
import { getHealthCheckForm, getLatestHealthCheckForm, listHealthCheckForms } from '../api/healthApi';
import { extractApiErrorMessage } from '../api/client';
import { ROUTE_PATHS } from '../constants/routes';
import { HealthCheckFormDetailModal } from 'common-react';
import type { HealthCheckForm } from '../types/care';

function formatDate(value?: string): string {
  if (!value) {
    return '-';
  }
  return value;
}

const historyBaseColumns: ColumnsType<HealthCheckForm> = [
  {
    title: '表单ID',
    dataIndex: 'formId',
    key: 'formId',
    width: 110,
  },
  {
    title: '老人ID',
    dataIndex: 'elderId',
    key: 'elderId',
    width: 120,
    render: (value?: number) => value ?? '-',
  },
  {
    title: '体检日期',
    dataIndex: 'checkDate',
    key: 'checkDate',
    width: 140,
    render: (value?: string) => formatDate(value),
  },
  {
    title: '协议ID',
    dataIndex: 'agreementId',
    key: 'agreementId',
    width: 120,
    render: (value?: number) => value ?? '-',
  },
  {
    title: '责任医生',
    dataIndex: 'responsibleDoctor',
    key: 'responsibleDoctor',
    width: 160,
    render: (value?: string) => value || '-',
  },
  {
    title: '编号',
    dataIndex: 'formCode',
    key: 'formCode',
    render: (value?: string) => value || '-',
  },
];

export default function HealthCheckFormPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const [loading, setLoading] = useState(false);
  const [historyLoading, setHistoryLoading] = useState(false);
  const [latestForm, setLatestForm] = useState<HealthCheckForm | null>(null);
  const [historyList, setHistoryList] = useState<HealthCheckForm[]>([]);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [historyError, setHistoryError] = useState<string | null>(null);
  const [historyFilterElderId, setHistoryFilterElderId] = useState<number | null>(null);
  const [historyDetailOpen, setHistoryDetailOpen] = useState(false);
  const [historyDetailLoading, setHistoryDetailLoading] = useState(false);
  const [historyDetailError, setHistoryDetailError] = useState<string | null>(null);
  const [historyDetail, setHistoryDetail] = useState<HealthCheckForm | null>(null);

  const journeyState = location.state as
    | { elderId?: number; agreementId?: number; elderName?: string; fromJourney?: boolean }
    | undefined;

  const elderId = journeyState?.elderId;
  const agreementId = journeyState?.agreementId;
  const elderName = journeyState?.elderName;
  const fromJourney = Boolean(journeyState?.fromJourney);

  const filteredHistoryList = useMemo(
    () =>
      historyFilterElderId == null
        ? historyList
        : historyList.filter((item) => item.elderId === historyFilterElderId),
    [historyFilterElderId, historyList],
  );

  const handleOpenHistoryDetail = useCallback(async (record: HealthCheckForm) => {
    if (!record.formId) {
      setHistoryDetailLoading(false);
      setHistoryDetail(null);
      setHistoryDetailError('该历史记录缺少表单ID，无法查看详情');
      setHistoryDetailOpen(true);
      return;
    }

    setHistoryDetailOpen(true);
    setHistoryDetailLoading(true);
    setHistoryDetailError(null);
    setHistoryDetail(null);

    try {
      const detail = await getHealthCheckForm(record.formId);
      setHistoryDetail(detail);
    } catch (error) {
      setHistoryDetailError(extractApiErrorMessage(error, '加载体检表详情失败'));
    } finally {
      setHistoryDetailLoading(false);
    }
  }, []);

  const historyColumns = useMemo<ColumnsType<HealthCheckForm>>(
    () => [
      ...historyBaseColumns,
      {
        title: '操作',
        key: 'actions',
        width: 120,
        fixed: 'right',
        render: (_value, record) => (
          <Button type="link" disabled={!record.formId} onClick={() => void handleOpenHistoryDetail(record)}>
            查看详情
          </Button>
        ),
      },
    ],
    [handleOpenHistoryDetail],
  );

  useEffect(() => {
    if (!elderId) {
      setLatestForm(null);
      setHistoryList([]);
      setErrorMessage('未获取到老人ID，无法查看健康体检结果');
      return;
    }

    let canceled = false;

    const loadLatest = async () => {
      setLoading(true);
      setErrorMessage(null);
      try {
        const detail = await getLatestHealthCheckForm(elderId, agreementId);
        if (!canceled) {
          setLatestForm(detail);
        }
      } catch (error) {
        if (!canceled) {
          setLatestForm(null);
          setErrorMessage(extractApiErrorMessage(error, '管理端暂未录入健康体检表'));
        }
      } finally {
        if (!canceled) {
          setLoading(false);
        }
      }
    };

    void loadLatest();

    return () => {
      canceled = true;
    };
  }, [agreementId, elderId]);

  useEffect(() => {
    if (!elderId) {
      setHistoryList([]);
      setHistoryError(null);
      return;
    }

    let canceled = false;

    const loadHistory = async () => {
      setHistoryLoading(true);
      setHistoryError(null);
      try {
        const forms = await listHealthCheckForms(elderId, agreementId);
        if (!canceled) {
          setHistoryList(forms);
        }
      } catch (error) {
        if (!canceled) {
          setHistoryList([]);
          setHistoryError(extractApiErrorMessage(error, '加载历史体检表失败'));
        }
      } finally {
        if (!canceled) {
          setHistoryLoading(false);
        }
      }
    };

    void loadHistory();

    return () => {
      canceled = true;
    };
  }, [agreementId, elderId]);

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <div>
        <Typography.Title level={4} style={{ margin: 0 }}>
          健康体检结果
        </Typography.Title>
      </div>

      <Card>
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          <Alert
            type="info"
            showIcon
            message="健康体检表由管理端护士或责任医生填写，当前页面仅供查看结果。"
          />

          {fromJourney ? (
            <Alert
              type="warning"
              showIcon
              message="当前服务申请需要等待管理端录入健康体检表后，才能继续后续评估流程。"
            />
          ) : null}

          {errorMessage ? <Alert type="warning" message={errorMessage} showIcon /> : null}

          {loading ? <Alert type="info" message="正在加载最新健康体检结果..." showIcon /> : null}

          {!loading && latestForm ? (
            <Card size="small" title="最新健康体检表">
              <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                <Typography.Text>表单ID：{latestForm.formId ?? '-'}</Typography.Text>
                <Typography.Text>老人姓名：{latestForm.elderName ?? elderName ?? '-'}</Typography.Text>
                <Typography.Text>老人ID：{latestForm.elderId ?? elderId ?? '-'}</Typography.Text>
                <Typography.Text>协议ID：{latestForm.agreementId ?? agreementId ?? '-'}</Typography.Text>
                <Typography.Text>体检日期：{latestForm.checkDate ?? '-'}</Typography.Text>
                <Typography.Text>责任医生：{latestForm.responsibleDoctor ?? '-'}</Typography.Text>
                <Space>
                  <Button
                    type="primary"
                    onClick={() => {
                      void handleOpenHistoryDetail(latestForm);
                    }}
                  >
                    查看详情
                  </Button>
                  {fromJourney ? (
                    <Button onClick={() => navigate(ROUTE_PATHS.JOURNEY_RESULT, { replace: true })}>返回旅程结果</Button>
                  ) : null}
                </Space>
              </Space>
            </Card>
          ) : null}

          {historyError ? <Alert type="error" message={historyError} showIcon /> : null}

          <Collapse
            items={[
              {
                key: 'health-check-history',
                label: (
                  <div
                    style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 12, width: '100%' }}
                    onClick={(event) => event.stopPropagation()}
                  >
                    <span>{`历史体检表（${filteredHistoryList.length}/${historyList.length}）`}</span>
                    <Space wrap>
                      <InputNumber
                        min={1}
                        placeholder="输入老人ID过滤"
                        value={historyFilterElderId ?? undefined}
                        onChange={(value) => setHistoryFilterElderId(typeof value === 'number' ? value : null)}
                        onClick={(event) => event.stopPropagation()}
                        onFocus={(event) => event.stopPropagation()}
                      />
                      <Button
                        onClick={(event: MouseEvent<HTMLElement>) => {
                          event.stopPropagation();
                          setHistoryFilterElderId(null);
                        }}
                      >
                        清空过滤
                      </Button>
                    </Space>
                  </div>
                ),
                children: (
                  <Table<HealthCheckForm>
                    rowKey={(record) => String(record.formId ?? `${record.elderId}-${record.checkDate ?? ''}`)}
                    loading={historyLoading}
                    columns={historyColumns}
                    dataSource={filteredHistoryList}
                    pagination={{ pageSize: 6 }}
                    locale={{ emptyText: historyFilterElderId ? '该老人ID暂无历史体检表记录' : '暂无历史体检表记录' }}
                    scroll={{ x: 900 }}
                  />
                ),
              },
            ]}
          />

          <HealthCheckFormDetailModal
            open={historyDetailOpen}
            loading={historyDetailLoading}
            error={historyDetailError}
            detail={historyDetail}
            onCancel={() => setHistoryDetailOpen(false)}
          />
        </Space>
      </Card>
    </Space>
  );
}
