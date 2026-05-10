import { useEffect, useMemo, useState } from 'react';
import { Button, DatePicker, Descriptions, Space, Table, Typography } from 'antd';
import { Dayjs } from 'dayjs';
import type { ColumnsType } from 'antd/es/table';
import { listFamilyCheckIns } from '../api/careDeliveryApi';
import { extractApiErrorMessage } from '../api/client';
import FamilyPageScaffold from '../components/FamilyPageScaffold';
import { CaregiverCheckInStatusTag } from '../components/CareStatusTag';
import { useActiveElderGuard } from '../components/useActiveElderGuard';
import { formatDateTime } from '../utils/dateFormat';
import type { CaregiverCheckInRecord } from '../types/care';

function summarizeTaskItems(record: CaregiverCheckInRecord) {
  const items = record.taskItems ?? [];
  if (!items.length) {
    return '无任务项';
  }
  const completedCount = items.filter((item) => item.completed).length;
  return `${completedCount}/${items.length} 项已完成`;
}

export default function FamilyCheckInRecordsPage() {
  const { activeElderId, hasActiveElder, guardMessage } = useActiveElderGuard();
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [taskDate, setTaskDate] = useState<Dayjs | null>(null);
  const [records, setRecords] = useState<CaregiverCheckInRecord[]>([]);
  const [selectedRecord, setSelectedRecord] = useState<CaregiverCheckInRecord | null>(null);

  const loadRecords = async (dateText?: string) => {
    if (!activeElderId) {
      setRecords([]);
      setSelectedRecord(null);
      setErrorMessage(guardMessage);
      return;
    }

    setLoading(true);
    setErrorMessage(null);
    try {
      const data = await listFamilyCheckIns(activeElderId, dateText);
      const currentRecordId = selectedRecord?.checkInRecordId;
      setRecords(data);
      setSelectedRecord(data.find((item) => item.checkInRecordId === currentRecordId) ?? data[0] ?? null);
    } catch (error) {
      setRecords([]);
      setSelectedRecord(null);
      setErrorMessage(extractApiErrorMessage(error, '加载打卡记录失败'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadRecords();
  }, [activeElderId]);

  const columns = useMemo<ColumnsType<CaregiverCheckInRecord>>(
    () => [
      { title: '记录ID', dataIndex: 'checkInRecordId', key: 'checkInRecordId', width: 100 },
      { title: '护理员', dataIndex: 'caregiverName', key: 'caregiverName', width: 140, render: (value?: string) => value || '-' },
      { title: '任务日期', dataIndex: 'taskDate', key: 'taskDate', width: 130, render: (value?: string) => value || '-' },
      {
        title: '状态',
        dataIndex: 'completionStatus',
        key: 'completionStatus',
        width: 120,
        render: (value?: string) => <CaregiverCheckInStatusTag status={value} />,
      },
      {
        title: '执行摘要',
        key: 'summary',
        width: 160,
        render: (_value, record) => summarizeTaskItems(record),
      },
      { title: '提交时间', dataIndex: 'completionTime', key: 'completionTime', width: 180, render: (value?: string) => formatDateTime(value) },
    ],
    [],
  );

  return (
    <FamilyPageScaffold
      title="打卡记录查看"
      actions={
        <Space>
          <DatePicker value={taskDate} onChange={setTaskDate} />
          <Button onClick={() => void loadRecords(taskDate?.format('YYYY-MM-DD'))} disabled={!hasActiveElder}>
            查询
          </Button>
        </Space>
      }
      infoMessage="家属可按日期查看护理员的日常执行打卡情况，列表将默认展示首条记录详情。"
      errorMessage={errorMessage}
      listTitle="打卡记录列表"
      listContent={
        <Table<CaregiverCheckInRecord>
          rowKey={(record) => String(record.checkInRecordId)}
          loading={loading}
          columns={columns}
          dataSource={records}
          pagination={{ pageSize: 8 }}
          onRow={(record) => ({ onClick: () => setSelectedRecord(record) })}
          locale={{ emptyText: loading ? '正在加载打卡记录...' : hasActiveElder ? '暂无打卡记录' : guardMessage }}
          rowClassName={(record) =>
            record.checkInRecordId === selectedRecord?.checkInRecordId ? 'ant-table-row-selected' : ''
          }
        />
      }
      detailTitle="打卡详情"
      detailContent={
        selectedRecord ? (
          <Descriptions bordered size="small" column={2}>
            <Descriptions.Item label="记录ID">{selectedRecord.checkInRecordId ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="老人姓名">{selectedRecord.elderName ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="护理员">{selectedRecord.caregiverName ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="服务计划ID">{selectedRecord.servicePlanId ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="状态"><CaregiverCheckInStatusTag status={selectedRecord.completionStatus} /></Descriptions.Item>
            <Descriptions.Item label="任务日期">{selectedRecord.taskDate ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="提交时间">{formatDateTime(selectedRecord.completionTime)}</Descriptions.Item>
            <Descriptions.Item label="更新时间">{formatDateTime(selectedRecord.updatedAt)}</Descriptions.Item>
            <Descriptions.Item label="执行摘要" span={2}>{summarizeTaskItems(selectedRecord)}</Descriptions.Item>
            <Descriptions.Item label="任务项" span={2}>
              {(selectedRecord.taskItems ?? []).length > 0 ? (
                <Space direction="vertical">
                  {(selectedRecord.taskItems ?? []).map((item) => (
                    <span key={item.itemCode}>{item.itemName}：{item.completed ? '已完成' : '未完成'}</span>
                  ))}
                </Space>
              ) : (
                <Typography.Text type="secondary">暂无任务项明细。</Typography.Text>
              )}
            </Descriptions.Item>
          </Descriptions>
        ) : (
          <Typography.Text type="secondary">
            {hasActiveElder ? '暂无可查看的打卡详情。' : guardMessage}
          </Typography.Text>
        )
      }
    />
  );
}
