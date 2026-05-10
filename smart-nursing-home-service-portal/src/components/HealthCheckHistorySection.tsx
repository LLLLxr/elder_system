import { useCallback, useMemo, useState } from 'react';
import type { MouseEvent } from 'react';
import { Alert, Button, Collapse, InputNumber, Space, Table } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { formatDate } from '../utils/dateFormat';
import type { HealthCheckForm } from '../types/care';

const historyBaseColumns: ColumnsType<HealthCheckForm> = [
  {
    title: '表单编号',
    dataIndex: 'formId',
    key: 'formId',
    width: 110,
  },
  {
    title: '老人编号',
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
    title: '协议编号',
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

interface HealthCheckHistorySectionProps {
  historyLoading: boolean;
  historyList: HealthCheckForm[];
  historyError: string | null;
  onViewDetail: (record: HealthCheckForm) => void;
}

export default function HealthCheckHistorySection({
  historyLoading,
  historyList,
  historyError,
  onViewDetail,
}: HealthCheckHistorySectionProps) {
  const [historyFilterElderId, setHistoryFilterElderId] = useState<number | null>(null);

  const filteredHistoryList = useMemo(
    () =>
      historyFilterElderId == null
        ? historyList
        : historyList.filter((item) => item.elderId === historyFilterElderId),
    [historyFilterElderId, historyList],
  );

  const historyColumns = useMemo<ColumnsType<HealthCheckForm>>(
    () => [
      ...historyBaseColumns,
      {
        title: '操作',
        key: 'actions',
        width: 120,
        fixed: 'right',
        render: (_value, record) => (
          <Button type="link" disabled={!record.formId} onClick={() => onViewDetail(record)}>
            查看详情
          </Button>
        ),
      },
    ],
    [onViewDetail],
  );

  return (
    <>
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
                    placeholder="输入老人编号过滤"
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
                locale={{ emptyText: historyFilterElderId ? '该老人编号暂无历史体检表记录' : '暂无历史体检表记录' }}
                scroll={{ x: 900 }}
              />
            ),
          },
        ]}
      />
    </>
  );
}
