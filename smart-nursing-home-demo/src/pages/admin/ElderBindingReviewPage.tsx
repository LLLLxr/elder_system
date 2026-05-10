import { useEffect, useMemo, useState } from 'react';
import { Alert, Button, Card, Descriptions, Form, Input, Select, Space, Table, Tag, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  approveElderBindingRequest,
  getElderBindingRequestDetail,
  listElderBindingRequests,
  rejectElderBindingRequest,
} from '../../api/elderBindingReviewApi';
import { extractApiErrorMessage } from '../../api/client';
import type { ElderBindingRequestItem, ElderBindingReviewPayload } from '../../types/admin';

const statusOptions = [
  { label: '待审核', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已驳回', value: 'REJECTED' },
];

function renderBindingRequestStatus(status?: string) {
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

function renderBindingType(bindingType?: string) {
  if (bindingType === 'SELF') {
    return <Tag color="success">老人本人</Tag>;
  }
  if (bindingType === 'FAMILY') {
    return <Tag color="blue">家属/关联人</Tag>;
  }
  return <Tag>{bindingType ?? '-'}</Tag>;
}

function renderRelation(bindingType?: string, relationToElder?: string) {
  if (bindingType === 'SELF') {
    return '老人本人';
  }
  return relationToElder?.trim() || '-';
}

export default function ElderBindingReviewPage() {
  const [form] = Form.useForm<ElderBindingReviewPayload>();
  const [status, setStatus] = useState('PENDING');
  const [listLoading, setListLoading] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [submitLoading, setSubmitLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [requests, setRequests] = useState<ElderBindingRequestItem[]>([]);
  const [selectedRequestId, setSelectedRequestId] = useState<number | null>(null);
  const [selectedRequest, setSelectedRequest] = useState<ElderBindingRequestItem | null>(null);

  const columns = useMemo<ColumnsType<ElderBindingRequestItem>>(
    () => [
      { title: '申请编号', dataIndex: 'requestId', key: 'requestId', width: 100 },
      { title: '申请人账号编号', dataIndex: 'applicantUserId', key: 'applicantUserId', width: 130 },
      { title: '绑定类型', dataIndex: 'bindingType', key: 'bindingType', width: 120, render: (value?: string) => renderBindingType(value) },
      { title: '老人姓名', dataIndex: 'elderName', key: 'elderName', width: 120 },
      { title: '老人身份证号', dataIndex: 'elderIdCard', key: 'elderIdCard', width: 220 },
      { title: '老人手机号', dataIndex: 'elderPhone', key: 'elderPhone', width: 140, render: (value?: string) => value || '-' },
      { title: '与老人关系', key: 'relationToElder', width: 120, render: (_value, record) => renderRelation(record.bindingType, record.relationToElder) },
      {
        title: '状态',
        dataIndex: 'status',
        key: 'status',
        width: 120,
        render: (value?: string) => renderBindingRequestStatus(value),
      },
    ],
    [],
  );

  const loadRequests = async (nextStatus: string, keepSelectedId?: number | null) => {
    setListLoading(true);
    setErrorMessage(null);
    try {
      const data = await listElderBindingRequests(nextStatus);
      setRequests(data);
      const preservedId = keepSelectedId ?? selectedRequestId;
      if (!preservedId || !data.some((item) => item.requestId === preservedId)) {
        setSelectedRequestId(null);
        setSelectedRequest(null);
      }
    } catch (error) {
      setRequests([]);
      setErrorMessage(extractApiErrorMessage(error, '加载老人绑定审核列表失败'));
    } finally {
      setListLoading(false);
    }
  };

  const loadDetail = async (requestId: number) => {
    setDetailLoading(true);
    setErrorMessage(null);
    try {
      const detail = await getElderBindingRequestDetail(requestId);
      setSelectedRequest(detail);
      setSelectedRequestId(requestId);
      form.setFieldsValue({ reviewComment: detail.reviewComment });
    } catch (error) {
      setSelectedRequest(null);
      setErrorMessage(extractApiErrorMessage(error, '加载老人绑定申请详情失败'));
    } finally {
      setDetailLoading(false);
    }
  };

  useEffect(() => {
    void loadRequests(status);
  }, [status]);

  const handleReview = async (action: 'approve' | 'reject') => {
    if (!selectedRequestId) {
      setErrorMessage('请先在列表中选择一条老人绑定申请');
      return;
    }

    setSubmitLoading(true);
    setErrorMessage(null);
    setSuccessMessage(null);
    try {
      const payload = form.getFieldsValue();
      const updated = action === 'approve'
        ? await approveElderBindingRequest(selectedRequestId, payload)
        : await rejectElderBindingRequest(selectedRequestId, payload);
      setSelectedRequest(updated);
      setSuccessMessage(action === 'approve' ? '老人绑定申请已审核通过' : '老人绑定申请已审核驳回');
      await loadRequests(status, selectedRequestId);
    } catch (error) {
      setErrorMessage(
        extractApiErrorMessage(error, action === 'approve' ? '老人绑定审核通过失败' : '老人绑定审核驳回失败'),
      );
    } finally {
      setSubmitLoading(false);
    }
  };

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Typography.Title level={4} style={{ margin: 0 }}>
        老人绑定审核
      </Typography.Title>

      <Alert type="info" showIcon message="医护人员可在此审核家属/关联人提交的老人绑定申请。申请人账号编号与老人编号是两套独立编号；“本人”只表示老人本人绑定，不作为家属关系值。" />
      {errorMessage ? <Alert type="error" showIcon message={errorMessage} /> : null}
      {successMessage ? <Alert type="success" showIcon message={successMessage} /> : null}

      <Card title="审核列表" extra={<Select value={status} options={statusOptions} style={{ width: 160 }} onChange={setStatus} />}>
        <Table<ElderBindingRequestItem>
          rowKey={(record) => String(record.requestId)}
          loading={listLoading}
          columns={columns}
          dataSource={requests}
          pagination={{ pageSize: 8 }}
          rowSelection={{
            type: 'radio',
            selectedRowKeys: selectedRequestId ? [String(selectedRequestId)] : [],
            onChange: (selectedRowKeys) => {
              const nextId = Number(selectedRowKeys[0]);
              if (Number.isFinite(nextId)) {
                void loadDetail(nextId);
              }
            },
          }}
          onRow={(record) => ({
            onClick: () => {
              if (typeof record.requestId === 'number') {
                void loadDetail(record.requestId);
              }
            },
          })}
          scroll={{ x: 1200 }}
          locale={{ emptyText: listLoading ? '正在加载绑定申请列表...' : '暂无绑定申请数据' }}
        />
      </Card>

      <Card title="申请详情" loading={detailLoading}>
        {selectedRequest ? (
          <Space direction="vertical" size="large" style={{ width: '100%' }}>
            <Descriptions bordered column={2} size="small">
              <Descriptions.Item label="申请编号">{selectedRequest.requestId ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="状态">{renderBindingRequestStatus(selectedRequest.status)}</Descriptions.Item>
              <Descriptions.Item label="申请人账号编号">{selectedRequest.applicantUserId ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="老人编号">{selectedRequest.elderId ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="绑定类型">{renderBindingType(selectedRequest.bindingType)}</Descriptions.Item>
              <Descriptions.Item label="老人姓名">{selectedRequest.elderName}</Descriptions.Item>
              <Descriptions.Item label="与老人关系">{renderRelation(selectedRequest.bindingType, selectedRequest.relationToElder)}</Descriptions.Item>
              <Descriptions.Item label="老人身份证号">{selectedRequest.elderIdCard}</Descriptions.Item>
              <Descriptions.Item label="老人手机号">{selectedRequest.elderPhone ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="审核人">{selectedRequest.reviewedBy ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="审核时间">{selectedRequest.reviewedAt ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="审核意见" span={2}>{selectedRequest.reviewComment ?? '-'}</Descriptions.Item>
            </Descriptions>

            <Form<ElderBindingReviewPayload> form={form} layout="vertical">
              <Form.Item label="审核意见" name="reviewComment">
                <Input.TextArea rows={4} placeholder="驳回时请说明原因；通过时可选填备注。家属关系中不允许填写“本人”。" />
              </Form.Item>
              <Space>
                <Button
                  type="primary"
                  onClick={() => void handleReview('approve')}
                  loading={submitLoading}
                  disabled={selectedRequest.status !== 'PENDING'}
                >
                  审核通过
                </Button>
                <Button
                  danger
                  onClick={() => void handleReview('reject')}
                  loading={submitLoading}
                  disabled={selectedRequest.status !== 'PENDING'}
                >
                  审核驳回
                </Button>
              </Space>
            </Form>
          </Space>
        ) : (
          <Typography.Text type="secondary">请先从上方列表中选择一条老人绑定申请。</Typography.Text>
        )}
      </Card>
    </Space>
  );
}
