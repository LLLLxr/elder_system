import { useEffect, useMemo, useState } from 'react';
import { Alert, Button, Card, Descriptions, Form, Input, Select, Space, Table, Tag, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  approveCaregiverQualificationApplication,
  getCaregiverQualificationApplicationDetail,
  listCaregiverQualificationApplications,
  rejectCaregiverQualificationApplication,
} from '../../api/caregiverQualificationApi';
import { extractApiErrorMessage } from '../../api/client';
import type { CaregiverQualificationApplication } from '../../types/care';

interface ReviewFormValues {
  reviewComment?: string;
}

const statusOptions = [
  { label: '待审核', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已驳回', value: 'REJECTED' },
];

function renderQualificationStatus(status?: string) {
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

export default function CaregiverQualificationReviewPage() {
  const [form] = Form.useForm<ReviewFormValues>();
  const [status, setStatus] = useState('PENDING');
  const [listLoading, setListLoading] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [submitLoading, setSubmitLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [applications, setApplications] = useState<CaregiverQualificationApplication[]>([]);
  const [selectedApplicationId, setSelectedApplicationId] = useState<number | null>(null);
  const [selectedApplication, setSelectedApplication] = useState<CaregiverQualificationApplication | null>(null);

  const columns = useMemo<ColumnsType<CaregiverQualificationApplication>>(
    () => [
      { title: '申请ID', dataIndex: 'applicationId', key: 'applicationId', width: 100 },
      { title: '护理员账号', dataIndex: 'caregiverUsername', key: 'caregiverUsername', width: 140 },
      { title: '姓名', dataIndex: 'realName', key: 'realName', width: 120 },
      { title: '手机号', dataIndex: 'phone', key: 'phone', width: 140 },
      { title: '证书编号', dataIndex: 'certificateNo', key: 'certificateNo', width: 180 },
      { title: '证书类型', dataIndex: 'certificateType', key: 'certificateType', width: 140 },
      { title: '从业年限', dataIndex: 'yearsOfExperience', key: 'yearsOfExperience', width: 120 },
      {
        title: '状态',
        dataIndex: 'status',
        key: 'status',
        width: 120,
        render: (value?: string) => renderQualificationStatus(value),
      },
    ],
    [],
  );

  const loadApplications = async (nextStatus: string, keepSelectedId?: number | null) => {
    setListLoading(true);
    setErrorMessage(null);
    try {
      const data = await listCaregiverQualificationApplications(nextStatus);
      setApplications(data);
      const preservedId = keepSelectedId ?? selectedApplicationId;
      if (!preservedId || !data.some((item) => item.applicationId === preservedId)) {
        setSelectedApplicationId(null);
        setSelectedApplication(null);
      }
    } catch (error) {
      setApplications([]);
      setErrorMessage(extractApiErrorMessage(error, '加载护理员资质审核列表失败'));
    } finally {
      setListLoading(false);
    }
  };

  const loadDetail = async (applicationId: number) => {
    setDetailLoading(true);
    setErrorMessage(null);
    try {
      const detail = await getCaregiverQualificationApplicationDetail(applicationId);
      setSelectedApplication(detail);
      setSelectedApplicationId(applicationId);
      form.setFieldsValue({ reviewComment: detail.reviewComment });
    } catch (error) {
      setSelectedApplication(null);
      setErrorMessage(extractApiErrorMessage(error, '加载资质申请详情失败'));
    } finally {
      setDetailLoading(false);
    }
  };

  useEffect(() => {
    void loadApplications(status);
  }, [status]);

  const handleReview = async (action: 'approve' | 'reject') => {
    if (!selectedApplicationId) {
      setErrorMessage('请先在列表中选择一条护理员资质申请记录');
      return;
    }

    setSubmitLoading(true);
    setErrorMessage(null);
    setSuccessMessage(null);
    try {
      const payload = form.getFieldsValue();
      const updated = action === 'approve'
        ? await approveCaregiverQualificationApplication(selectedApplicationId, payload)
        : await rejectCaregiverQualificationApplication(selectedApplicationId, payload);
      setSelectedApplication(updated);
      setSuccessMessage(action === 'approve' ? '护理员资质申请已审核通过' : '护理员资质申请已审核驳回');
      await loadApplications(status, selectedApplicationId);
    } catch (error) {
      setErrorMessage(extractApiErrorMessage(error, action === 'approve' ? '资质审核通过失败' : '资质审核驳回失败'));
    } finally {
      setSubmitLoading(false);
    }
  };

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Typography.Title level={4} style={{ margin: 0 }}>
        护理员资质审核
      </Typography.Title>

      <Alert type="info" showIcon message="医护人员可在此查看护理员资质申请，并完成通过或驳回审核。" />
      {errorMessage ? <Alert type="error" showIcon message={errorMessage} /> : null}
      {successMessage ? <Alert type="success" showIcon message={successMessage} /> : null}

      <Card title="审核列表" extra={<Select value={status} options={statusOptions} style={{ width: 160 }} onChange={setStatus} />}>
        <Table<CaregiverQualificationApplication>
          rowKey={(record) => String(record.applicationId)}
          loading={listLoading}
          columns={columns}
          dataSource={applications}
          pagination={{ pageSize: 8 }}
          rowSelection={{
            type: 'radio',
            selectedRowKeys: selectedApplicationId ? [String(selectedApplicationId)] : [],
            onChange: (selectedRowKeys) => {
              const nextId = Number(selectedRowKeys[0]);
              if (Number.isFinite(nextId)) {
                void loadDetail(nextId);
              }
            },
          }}
          onRow={(record) => ({
            onClick: () => {
              if (typeof record.applicationId === 'number') {
                void loadDetail(record.applicationId);
              }
            },
          })}
          scroll={{ x: 1100 }}
          locale={{ emptyText: listLoading ? '正在加载资质列表...' : '暂无资质申请数据' }}
        />
      </Card>

      <Card title="申请详情" loading={detailLoading}>
        {selectedApplication ? (
          <Space direction="vertical" size="large" style={{ width: '100%' }}>
            <Descriptions bordered column={2} size="small">
              <Descriptions.Item label="申请ID">{selectedApplication.applicationId ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="状态">{renderQualificationStatus(selectedApplication.status)}</Descriptions.Item>
              <Descriptions.Item label="护理员账号">{selectedApplication.caregiverUsername ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="护理员ID">{selectedApplication.caregiverUserId ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="姓名">{selectedApplication.realName}</Descriptions.Item>
              <Descriptions.Item label="手机号">{selectedApplication.phone}</Descriptions.Item>
              <Descriptions.Item label="身份证号">{selectedApplication.idCardNo}</Descriptions.Item>
              <Descriptions.Item label="从业年限">{selectedApplication.yearsOfExperience}</Descriptions.Item>
              <Descriptions.Item label="证书编号">{selectedApplication.certificateNo}</Descriptions.Item>
              <Descriptions.Item label="证书类型">{selectedApplication.certificateType}</Descriptions.Item>
              <Descriptions.Item label="技能说明" span={2}>{selectedApplication.skillSummary}</Descriptions.Item>
              <Descriptions.Item label="审核人">{selectedApplication.reviewedBy ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="审核时间">{selectedApplication.reviewedAt ?? '-'}</Descriptions.Item>
            </Descriptions>

            <Form<ReviewFormValues> form={form} layout="vertical">
              <Form.Item label="审核意见" name="reviewComment">
                <Input.TextArea rows={4} placeholder="可填写审核意见，驳回时建议说明需补充的材料" />
              </Form.Item>
              <Space>
                <Button type="primary" onClick={() => void handleReview('approve')} loading={submitLoading} disabled={selectedApplication.status !== 'PENDING'}>
                  审核通过
                </Button>
                <Button danger onClick={() => void handleReview('reject')} loading={submitLoading} disabled={selectedApplication.status !== 'PENDING'}>
                  审核驳回
                </Button>
              </Space>
            </Form>
          </Space>
        ) : (
          <Typography.Text type="secondary">请先从上方列表中选择一条护理员资质申请记录。</Typography.Text>
        )}
      </Card>
    </Space>
  );
}
