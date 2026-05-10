import { Collapse, Table } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { StatusTag } from 'common-react';
import type { IntakeRecord } from '../types/care';
import { formatCompactDateTime } from '../utils/dateFormat';

interface IntakeRecordsTableProps {
  intakeRecords: IntakeRecord[];
  intakeLoading: boolean;
  loginUsername: string | undefined;
  queriedElderId: number | null;
}

const columns: ColumnsType<IntakeRecord> = [
  { title: '申请单编号', dataIndex: 'applicationId', key: 'applicationId', width: 140 },
  { title: '申请人', dataIndex: 'applicantName', key: 'applicantName', width: 160 },
  {
    title: '提交时间',
    dataIndex: 'submittedAt',
    key: 'submittedAt',
    width: 200,
    render: (value?: string) => formatCompactDateTime(value),
  },
  {
    title: '旅程状态',
    dataIndex: 'journeyStatus',
    key: 'journeyStatus',
    width: 180,
    render: (status?: string) => <StatusTag status={status} />,
  },
  { title: '说明', dataIndex: 'message', key: 'message' },
];

export default function IntakeRecordsTable({
  intakeRecords,
  intakeLoading,
  loginUsername,
  queriedElderId,
}: IntakeRecordsTableProps) {
  return (
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
  );
}
