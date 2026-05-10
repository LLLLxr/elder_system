import { useEffect, useMemo, useState } from 'react';
import { Button, Descriptions, Table, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { listMyFamilyVisitReservations } from '../api/familyVisitApi';
import { extractApiErrorMessage } from '../api/client';
import FamilyPageScaffold from '../components/FamilyPageScaffold';
import { FamilyVisitReservationStatusTag } from '../components/CareStatusTag';
import type { FamilyVisitReservation } from '../types/care';

function formatSlot(record: FamilyVisitReservation) {
  return [record.slotDate, record.startTime, record.endTime ? `- ${record.endTime}` : undefined].filter(Boolean).join(' ') || '-';
}

export default function MyFamilyVisitReservationsPage() {
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [reservations, setReservations] = useState<FamilyVisitReservation[]>([]);
  const [selectedReservation, setSelectedReservation] = useState<FamilyVisitReservation | null>(null);

  const columns = useMemo<ColumnsType<FamilyVisitReservation>>(
    () => [
      { title: '预约编号', dataIndex: 'reservationId', key: 'reservationId', width: 100 },
      { title: '老人编号', dataIndex: 'elderId', key: 'elderId', width: 100 },
      { title: '来访人', dataIndex: 'visitorName', key: 'visitorName', width: 120 },
      { title: '联系电话', dataIndex: 'visitorPhone', key: 'visitorPhone', width: 140 },
      {
        title: '预约时段',
        key: 'slotRange',
        width: 240,
        render: (_value, record) => formatSlot(record),
      },
      {
        title: '状态',
        dataIndex: 'status',
        key: 'status',
        width: 120,
        render: (value?: string) => <FamilyVisitReservationStatusTag status={value} />,
      },
    ],
    [],
  );

  const loadReservations = async () => {
    setLoading(true);
    setErrorMessage(null);
    try {
      const data = await listMyFamilyVisitReservations();
      setReservations(data);
      if (data.length > 0) {
        setSelectedReservation(data[0]);
      } else {
        setSelectedReservation(null);
      }
    } catch (error) {
      setReservations([]);
      setSelectedReservation(null);
      setErrorMessage(extractApiErrorMessage(error, '加载我的预约失败'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadReservations();
  }, []);

  return (
    <FamilyPageScaffold
      title="我的预约"
      actions={<Button onClick={() => void loadReservations()}>刷新</Button>}
      infoMessage="可在此查看家属预约的最新审核状态与审核意见。"
      errorMessage={errorMessage}
      listTitle="预约记录"
      listContent={
        <Table<FamilyVisitReservation>
          rowKey={(record) => String(record.reservationId)}
          loading={loading}
          columns={columns}
          dataSource={reservations}
          pagination={{ pageSize: 8 }}
          onRow={(record) => ({
            onClick: () => setSelectedReservation(record),
          })}
          locale={{ emptyText: loading ? '正在加载预约记录...' : '暂无预约记录' }}
        />
      }
      detailTitle="预约详情"
      detailContent={
        selectedReservation ? (
          <Descriptions bordered column={2} size="small">
            <Descriptions.Item label="预约编号">{selectedReservation.reservationId ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="状态"><FamilyVisitReservationStatusTag status={selectedReservation.status} /></Descriptions.Item>
            <Descriptions.Item label="老人编号">{selectedReservation.elderId}</Descriptions.Item>
            <Descriptions.Item label="来访人">{selectedReservation.visitorName}</Descriptions.Item>
            <Descriptions.Item label="联系电话">{selectedReservation.visitorPhone}</Descriptions.Item>
            <Descriptions.Item label="与老人关系">{selectedReservation.relationToElder}</Descriptions.Item>
            <Descriptions.Item label="预约时段" span={2}>{formatSlot(selectedReservation)}</Descriptions.Item>
            <Descriptions.Item label="来访目的" span={2}>{selectedReservation.visitPurpose}</Descriptions.Item>
            <Descriptions.Item label="审核人">{selectedReservation.reviewedBy ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="审核时间">{selectedReservation.reviewedAt ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="审核意见" span={2}>{selectedReservation.reviewComment ?? '-'}</Descriptions.Item>
          </Descriptions>
        ) : (
          <Typography.Text type="secondary">暂无可查看的预约详情。</Typography.Text>
        )
      }
    />
  );
}
