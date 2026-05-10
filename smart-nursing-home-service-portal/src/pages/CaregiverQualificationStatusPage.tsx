import { useEffect, useMemo, useState } from 'react';
import { Alert, Button, Descriptions, Space, Table, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { listMyCaregiverQualificationApplications } from '../api/caregiverQualificationApi';
import { extractApiErrorMessage } from '../api/client';
import { CaregiverQualificationStatusTag } from '../components/CareStatusTag';
import DefaultCollapsedSection from '../components/DefaultCollapsedSection';
import type { CaregiverQualificationApplication } from '../types/care';

export default function CaregiverQualificationStatusPage() {
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [applications, setApplications] = useState<CaregiverQualificationApplication[]>([]);
  const [selectedApplication, setSelectedApplication] = useState<CaregiverQualificationApplication | null>(null);

  const columns = useMemo<ColumnsType<CaregiverQualificationApplication>>(
    () => [
      { title: '申请ID', dataIndex: 'applicationId', key: 'applicationId', width: 100 },
      { title: '姓名', dataIndex: 'realName', key: 'realName', width: 120 },
      { title: '手机号', dataIndex: 'phone', key: 'phone', width: 140 },
      { title: '证书编号', dataIndex: 'certificateNo', key: 'certificateNo', width: 180 },
      { title: '证书类型', dataIndex: 'certificateType', key: 'certificateType', width: 140 },
      {
        title: '状态',
        dataIndex: 'status',
        key: 'status',
        width: 120,
        render: (value?: string) => <CaregiverQualificationStatusTag status={value} />,
      },
    ],
    [],
  );

  const loadApplications = async () => {
    setLoading(true);
    setErrorMessage(null);
    try {
      const data = await listMyCaregiverQualificationApplications();
      setApplications(data);
      if (data.length > 0) {
        setSelectedApplication(data[0]);
      } else {
        setSelectedApplication(null);
      }
    } catch (error) {
      setApplications([]);
      setSelectedApplication(null);
      setErrorMessage(extractApiErrorMessage(error, '加载资质状态失败'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadApplications();
  }, []);

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Space style={{ width: '100%', justifyContent: 'space-between' }}>
        <Typography.Title level={4} style={{ margin: 0 }}>
          资质状态
        </Typography.Title>
        <Button onClick={() => void loadApplications()}>刷新</Button>
      </Space>

      <Alert type="info" showIcon message="可在此查看护理员资质申请的审核状态与审核意见。" />
      {errorMessage ? <Alert type="error" showIcon message={errorMessage} /> : null}

      <DefaultCollapsedSection title="申请记录">
        <Table<CaregiverQualificationApplication>
          rowKey={(record) => String(record.applicationId)}
          loading={loading}
          columns={columns}
          dataSource={applications}
          pagination={{ pageSize: 8 }}
          onRow={(record) => ({
            onClick: () => setSelectedApplication(record),
          })}
          locale={{ emptyText: loading ? '正在加载资质记录...' : '暂无资质申请记录' }}
        />
      </DefaultCollapsedSection>

      <DefaultCollapsedSection title="申请详情">
        {selectedApplication ? (
          <Descriptions bordered column={2} size="small">
            <Descriptions.Item label="申请ID">{selectedApplication.applicationId ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="状态"><CaregiverQualificationStatusTag status={selectedApplication.status} /></Descriptions.Item>
            <Descriptions.Item label="姓名">{selectedApplication.realName}</Descriptions.Item>
            <Descriptions.Item label="手机号">{selectedApplication.phone}</Descriptions.Item>
            <Descriptions.Item label="身份证号">{selectedApplication.idCardNo}</Descriptions.Item>
            <Descriptions.Item label="从业年限">{selectedApplication.yearsOfExperience}</Descriptions.Item>
            <Descriptions.Item label="证书编号">{selectedApplication.certificateNo}</Descriptions.Item>
            <Descriptions.Item label="证书类型">{selectedApplication.certificateType}</Descriptions.Item>
            <Descriptions.Item label="技能说明" span={2}>{selectedApplication.skillSummary}</Descriptions.Item>
            <Descriptions.Item label="审核人">{selectedApplication.reviewedBy ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="审核时间">{selectedApplication.reviewedAt ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="审核意见" span={2}>{selectedApplication.reviewComment ?? '-'}</Descriptions.Item>
          </Descriptions>
        ) : (
          <Typography.Text type="secondary">暂无可查看的资质申请详情。</Typography.Text>
        )}
      </DefaultCollapsedSection>
    </Space>
  );
}
