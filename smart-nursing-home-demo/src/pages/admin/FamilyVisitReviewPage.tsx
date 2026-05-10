import { useEffect, useMemo, useState } from 'react';
import { Alert, Button, Card, Descriptions, Form, Input, Select, Space, Table, Tag, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { approveFamilyVisitReservation, getFamilyVisitReservationDetail, listFamilyVisitReservations, rejectFamilyVisitReservation } from '../../api/familyVisitApi';
import { extractApiErrorMessage } from '../../api/client';
import type { FamilyVisitReservation } from '../../types/care';

interface ReviewFormValues {
  reviewComment?: string;
}

const statusOptions = [
  { label: '待审核', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已驳回', value: 'REJECTED' },
];

function renderReservationStatus(status?: string) {
  if (status === 'PENDING') {
    return <Tag color="processing">待审核</Tag>;
  }
  if (status === 'APPROVED') {
    return <Tag color="success">已通过</Tag>;
  }
  if (status === 'REJECTED') {
    return <Tag color="error">已驳回</Tag>;
  }
  return <Tag>{status ?? '-'}</Tag>;
}

function formatDate(date?: string, startTime?: string, endTime?: string) {
  const parts = [date, startTime, endTime ? `- ${endTime}` : undefined].filter(Boolean);
  return parts.length ? parts.join(' ') : '-';
}

export default function FamilyVisitReviewPage() {
  const [form] = Form.useForm<ReviewFormValues>();
  const [status, setStatus] = useState('PENDING');
  const [listLoading, setListLoading] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [submitLoading, setSubmitLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [reservations, setReservations] = useState<FamilyVisitReservation[]>([]);
  const [selectedReservationId, setSelectedReservationId] = useState<number | null>(null);
  const [selectedReservation, setSelectedReservation] = useState<FamilyVisitReservation | null>(null);

  const columns = useMemo<ColumnsType<FamilyVisitReservation>>(
    () => [
      { title: '预约ID', dataIndex: 'reservationId', key: 'reservationId', width: 100 },
      { title: '老人ID', dataIndex: 'elderId', key: 'elderId', width: 100 },
      { title: '家属账号', dataIndex: 'familyUsername', key: 'familyUsername', width: 140 },
      { title: '来访人', dataIndex: 'visitorName', key: 'visitorName', width: 120 },
      { title: '联系电话', dataIndex: 'visitorPhone', key: 'visitorPhone', width: 140 },
      { title: '关系', dataIndex: 'relationToElder', key: 'relationToElder', width: 120 },
      {
        title: '预约时段',
        key: 'slot',
        width: 240,
        render: (_value, record) => formatDate(record.slotDate, record.startTime, record.endTime),
      },
      {
        title: '状态',
        dataIndex: 'status',
        key: 'status',
        width: 120,
        render: (value?: string) => renderReservationStatus(value),
      },
    ],
    [],
  );

  const loadReservations = async (nextStatus: string, keepSelectedId?: number | null) => {
    setListLoading(true);
    setErrorMessage(null);
    try {
      const data = await listFamilyVisitReservations(nextStatus);
      setReservations(data);
      const preservedId = keepSelectedId ?? selectedReservationId;
      if (!preservedId || !data.some((item) => item.reservationId === preservedId)) {
        setSelectedReservationId(null);
        setSelectedReservation(null);
      }
    } catch (error) {
      setReservations([]);
      setErrorMessage(extractApiErrorMessage(error, '加载家属预约审核列表失败'));
    } finally {
      setListLoading(false);
    }
  };

  const loadDetail = async (reservationId: number) => {
    setDetailLoading(true);
    setErrorMessage(null);
    try {
      const detail = await getFamilyVisitReservationDetail(reservationId);
      setSelectedReservation(detail);
      setSelectedReservationId(reservationId);
      form.setFieldsValue({ reviewComment: detail.reviewComment });
    } catch (error) {
      setSelectedReservation(null);
      setErrorMessage(extractApiErrorMessage(error, '加载预约详情失败'));
    } finally {
      setDetailLoading(false);
    }
  };

  useEffect(() => {
    void loadReservations(status);
  }, [status]);

  const handleReview = async (action: 'approve' | 'reject') => {
    if (!selectedReservationId) {
      setErrorMessage('请先在列表中选择一条家属预约记录');
      return;
    }

    setSubmitLoading(true);
    setErrorMessage(null);
    setSuccessMessage(null);
    try {
      const payload = form.getFieldsValue();
      const updated = action === 'approve'
        ? await approveFamilyVisitReservation(selectedReservationId, payload)
        : await rejectFamilyVisitReservation(selectedReservationId, payload);
      setSelectedReservation(updated);
      setSuccessMessage(action === 'approve' ? '家属预约已审核通过' : '家属预约已审核驳回');
      await loadReservations(status, selectedReservationId);
    } catch (error) {
      setErrorMessage(extractApiErrorMessage(error, action === 'approve' ? '预约审核通过失败' : '预约审核驳回失败'));
    } finally {
      setSubmitLoading(false);
    }
  };

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Typography.Title level={4} style={{ margin: 0 }}>
        家属预约审核
      </Typography.Title>

      <Alert type="info" showIcon message="医护人员可在此查看家属预约申请，并完成通过或驳回审核。" />
      {errorMessage ? <Alert type="error" showIcon message={errorMessage} /> : null}
      {successMessage ? <Alert type="success" showIcon message={successMessage} /> : null}

      <Card title="审核列表" extra={<Select value={status} options={statusOptions} style={{ width: 160 }} onChange={setStatus} />}>
        <Table<FamilyVisitReservation>
          rowKey={(record) => String(record.reservationId)}
          loading={listLoading}
          columns={columns}
          dataSource={reservations}
          pagination={{ pageSize: 8 }}
          rowSelection={{
            type: 'radio',
            selectedRowKeys: selectedReservationId ? [String(selectedReservationId)] : [],
            onChange: (selectedRowKeys) => {
              const nextId = Number(selectedRowKeys[0]);
              if (Number.isFinite(nextId)) {
                void loadDetail(nextId);
              }
            },
          }}
          onRow={(record) => ({
            onClick: () => {
              if (typeof record.reservationId === 'number') {
                void loadDetail(record.reservationId);
              }
            },
          })}
          scroll={{ x: 1200 }}
          locale={{ emptyText: listLoading ? '正在加载预约列表...' : '暂无预约数据' }}
        />
      </Card>

      <Card title="预约详情" loading={detailLoading}>
        {selectedReservation ? (
          <Space direction="vertical" size="large" style={{ width: '100%' }}>
            <Descriptions bordered column={2} size="small">
              <Descriptions.Item label="预约ID">{selectedReservation.reservationId ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="状态">{renderReservationStatus(selectedReservation.status)}</Descriptions.Item>
              <Descriptions.Item label="老人ID">{selectedReservation.elderId}</Descriptions.Item>
              <Descriptions.Item label="家属账号">{selectedReservation.familyUsername ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="来访人">{selectedReservation.visitorName}</Descriptions.Item>
              <Descriptions.Item label="联系电话">{selectedReservation.visitorPhone}</Descriptions.Item>
              <Descriptions.Item label="关系">{selectedReservation.relationToElder}</Descriptions.Item>
              <Descriptions.Item label="预约时段">{formatDate(selectedReservation.slotDate, selectedReservation.startTime, selectedReservation.endTime)}</Descriptions.Item>
              <Descriptions.Item label="来访目的" span={2}>{selectedReservation.visitPurpose}</Descriptions.Item>
              <Descriptions.Item label="审核人">{selectedReservation.reviewedBy ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="审核时间">{selectedReservation.reviewedAt ?? '-'}</Descriptions.Item>
            </Descriptions>

            <Form<ReviewFormValues> form={form} layout="vertical">
              <Form.Item label="审核意见" name="reviewComment">
                <Input.TextArea rows={4} placeholder="可填写审核意见，驳回时建议说明原因" />
              </Form.Item>
              <Space>
                <Button type="primary" onClick={() => void handleReview('approve')} loading={submitLoading} disabled={selectedReservation.status !== 'PENDING'}>
                  审核通过
                </Button>
                <Button danger onClick={() => void handleReview('reject')} loading={submitLoading} disabled={selectedReservation.status !== 'PENDING'}>
                  审核驳回
                </Button>
              </Space>
            </Form>
          </Space>
        ) : (
          <Typography.Text type="secondary">请先从上方列表中选择一条家属预约记录。</Typography.Text>
        )}
      </Card>
    </Space>
  );
}
