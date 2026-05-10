import { Button, Space, Table } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { ApplicationStepItem, ApplicationStepStatus } from '../utils/domain/journeyTypes';
import { renderStepStatusTag } from '../utils/domain/journeyTypes';

interface JourneyStepsTableProps {
  steps: ApplicationStepItem[];
  loading: boolean;
  onViewDetail: () => void;
  onViewAgreement: () => void;
}

export default function JourneyStepsTable({
  steps,
  loading,
  onViewDetail,
  onViewAgreement,
}: JourneyStepsTableProps) {
  const columns: ColumnsType<ApplicationStepItem> = [
    { title: '阶段', dataIndex: 'title', key: 'title', width: 160 },
    { title: '当前状态', dataIndex: 'summary', key: 'summary', width: 180 },
    {
      title: '进度情况',
      dataIndex: 'status',
      key: 'status',
      width: 120,
      render: (value: ApplicationStepStatus) => renderStepStatusTag(value),
    },
    { title: '时间说明', dataIndex: 'timeText', key: 'timeText', width: 240 },
    { title: '下一步提示', dataIndex: 'hint', key: 'hint' },
    {
      title: '操作',
      key: 'actions',
      fixed: 'right',
      width: 160,
      render: (_value, record) => (
        <Space size="small">
          <Button type="link" onClick={onViewDetail}>
            查看详情
          </Button>
          {record.key === 'AGREEMENT' && record.status === 'process' ? (
            <Button type="link" onClick={onViewAgreement}>
              签约信息
            </Button>
          ) : null}
        </Space>
      ),
    },
  ];

  return (
    <Table<ApplicationStepItem>
      rowKey="key"
      loading={loading}
      columns={columns}
      dataSource={steps}
      pagination={false}
      scroll={{ x: 1080 }}
      locale={{ emptyText: loading ? '正在加载申请进度...' : '暂无进度数据' }}
    />
  );
}
