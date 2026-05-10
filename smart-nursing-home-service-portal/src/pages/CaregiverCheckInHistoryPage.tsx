import { useEffect, useMemo, useState } from 'react';
import { Alert, Button, DatePicker, Descriptions, Space, Table, Typography } from 'antd';
import { Dayjs } from 'dayjs';
import type { ColumnsType } from 'antd/es/table';
import { listMyCheckIns } from '../api/careDeliveryApi';
import { extractApiErrorMessage } from '../api/client';
import { CaregiverCheckInStatusTag } from '../components/CareStatusTag';
import DefaultCollapsedSection from '../components/DefaultCollapsedSection';
import { formatDateTime } from '../utils/dateFormat';
import type { CaregiverCheckInRecord } from '../types/care';

export default function CaregiverCheckInHistoryPage() {
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [taskDate, setTaskDate] = useState<Dayjs | null>(null);
  const [records, setRecords] = useState<CaregiverCheckInRecord[]>([]);
  const [selectedRecord, setSelectedRecord] = useState<CaregiverCheckInRecord | null>(null);

  const loadRecords = async (dateText?: string) => {
    setLoading(true);
    setErrorMessage(null);
    try {
      const data = await listMyCheckIns(undefined, dateText);
      setRecords(data);
      setSelectedRecord(data[0] ?? null);
    } catch (error) {
      setRecords([]);
      setSelectedRecord(null);
      setErrorMessage(extractApiErrorMessage(error, '加载打卡历史失败'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadRecords();
  }, []);

  const columns = useMemo<ColumnsType<CaregiverCheckInRecord>>(
    () => [
      { title: '记录ID', dataIndex: 'checkInRecordId', key: 'checkInRecordId', width: 100 },
      { title: '老人', dataIndex: 'elderName', key: 'elderName', width: 140, render: (value?: string) => value || '-' },
      { title: '服务计划ID', dataIndex: 'servicePlanId', key: 'servicePlanId', width: 110 },
      { title: '任务日期', dataIndex: 'taskDate', key: 'taskDate', width: 130, render: (value?: string) => value || '-' },
      {
        title: '状态',
        dataIndex: 'completionStatus',
        key: 'completionStatus',
        width: 120,
        render: (value?: string) => <CaregiverCheckInStatusTag status={value} />,
      },
      { title: '提交时间', dataIndex: 'completionTime', key: 'completionTime', width: 180, render: (value?: string) => formatDateTime(value) },
    ],
    [],
  );

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Space style={{ width: '100%', justifyContent: 'space-between' }}>
        <Typography.Title level={4} style={{ margin: 0 }}>
          打卡历史
        </Typography.Title>
        <Space>
          <DatePicker value={taskDate} onChange={setTaskDate} />
          <Button onClick={() => void loadRecords(taskDate?.format('YYYY-MM-DD'))}>查询</Button>
        </Space>
      </Space>

      <Alert type="info" showIcon message="可按日期筛选自己已提交的护理打卡记录。" />
      {errorMessage ? <Alert type="error" showIcon message={errorMessage} /> : null}

      <DefaultCollapsedSection title="打卡记录">
        <Table<CaregiverCheckInRecord>
          rowKey={(record) => String(record.checkInRecordId)}
          loading={loading}
          columns={columns}
          dataSource={records}
          pagination={{ pageSize: 8 }}
          onRow={(record) => ({ onClick: () => setSelectedRecord(record) })}
          locale={{ emptyText: loading ? '正在加载打卡记录...' : '暂无打卡记录' }}
        />
      </DefaultCollapsedSection>

      <DefaultCollapsedSection title="记录详情">
        {selectedRecord ? (
          <Descriptions bordered size="small" column={2}>
            <Descriptions.Item label="记录ID">{selectedRecord.checkInRecordId ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="状态"><CaregiverCheckInStatusTag status={selectedRecord.completionStatus} /></Descriptions.Item>
            <Descriptions.Item label="老人姓名">{selectedRecord.elderName ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="任务日期">{selectedRecord.taskDate ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="提交时间" span={2}>{formatDateTime(selectedRecord.completionTime)}</Descriptions.Item>
            <Descriptions.Item label="任务项" span={2}>
              <Space direction="vertical">
                {(selectedRecord.taskItems ?? []).map((item) => (
                  <span key={item.itemCode}>{item.itemName}：{item.completed ? '已完成' : '未完成'}</span>
                ))}
              </Space>
            </Descriptions.Item>
          </Descriptions>
        ) : (
          <Typography.Text type="secondary">暂无可查看的打卡详情。</Typography.Text>
        )}
      </DefaultCollapsedSection>
    </Space>
  );
}
