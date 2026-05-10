import { useEffect, useMemo, useState } from 'react';
import { Button, Descriptions, Table, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { listFamilyServicePlans } from '../api/careDeliveryApi';
import { extractApiErrorMessage } from '../api/client';
import FamilyPageScaffold from '../components/FamilyPageScaffold';
import { FamilyServicePlanStatusTag } from '../components/CareStatusTag';
import { useActiveElderGuard } from '../components/useActiveElderGuard';
import type { FamilyServicePlan } from '../types/care';

export default function FamilyServicePlansPage() {
  const { activeElderId, hasActiveElder, guardMessage } = useActiveElderGuard();
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [plans, setPlans] = useState<FamilyServicePlan[]>([]);
  const [selectedPlan, setSelectedPlan] = useState<FamilyServicePlan | null>(null);

  const loadPlans = async () => {
    if (!activeElderId) {
      setPlans([]);
      setSelectedPlan(null);
      setErrorMessage(guardMessage);
      return;
    }

    setLoading(true);
    setErrorMessage(null);
    try {
      const data = await listFamilyServicePlans(activeElderId);
      setPlans(data);
      setSelectedPlan(data[0] ?? null);
    } catch (error) {
      setPlans([]);
      setSelectedPlan(null);
      setErrorMessage(extractApiErrorMessage(error, '加载服务清单失败'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadPlans();
  }, [activeElderId]);

  const columns = useMemo<ColumnsType<FamilyServicePlan>>(
    () => [
      { title: '服务计划ID', dataIndex: 'servicePlanId', key: 'servicePlanId', width: 120 },
      { title: '计划名称', dataIndex: 'planName', key: 'planName', width: 180, render: (value?: string) => value || '-' },
      { title: '护理员', dataIndex: 'assignedCaregiverName', key: 'assignedCaregiverName', width: 140, render: (value?: string) => value || '-' },
      { title: '生效日期', dataIndex: 'effectiveDate', key: 'effectiveDate', width: 140, render: (value?: string) => value || '-' },
      {
        title: '状态',
        dataIndex: 'status',
        key: 'status',
        width: 120,
        render: (value?: string) => <FamilyServicePlanStatusTag status={value} />,
      },
    ],
    [],
  );

  return (
    <FamilyPageScaffold
      title="服务清单查看"
      actions={<Button onClick={() => void loadPlans()} disabled={!hasActiveElder}>刷新</Button>}
      infoMessage="家属可查看老人当前在服护理计划、护理员分配和计划项目。"
      errorMessage={errorMessage}
      listTitle="服务计划列表"
      listContent={
        <Table<FamilyServicePlan>
          rowKey={(record) => String(record.servicePlanId)}
          loading={loading}
          columns={columns}
          dataSource={plans}
          pagination={{ pageSize: 6 }}
          onRow={(record) => ({ onClick: () => setSelectedPlan(record) })}
          locale={{ emptyText: loading ? '正在加载服务计划...' : hasActiveElder ? '暂无服务计划' : guardMessage }}
        />
      }
      detailTitle="服务计划详情"
      detailContent={
        selectedPlan ? (
          <Descriptions bordered size="small" column={2}>
            <Descriptions.Item label="服务计划ID">{selectedPlan.servicePlanId ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="状态"><FamilyServicePlanStatusTag status={selectedPlan.status} /></Descriptions.Item>
            <Descriptions.Item label="计划名称">{selectedPlan.planName ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="护理员">{selectedPlan.assignedCaregiverName ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="生效日期">{selectedPlan.effectiveDate ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="失效日期">{selectedPlan.expireDate ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="服务项目" span={2}>
              <Typography.Paragraph style={{ marginBottom: 0 }}>
                {(selectedPlan.planItems ?? []).map((item) => item.itemName).filter(Boolean).join('、') || '-'}
              </Typography.Paragraph>
            </Descriptions.Item>
          </Descriptions>
        ) : (
          <Typography.Text type="secondary">
            {hasActiveElder ? '暂无可查看的服务计划详情。' : guardMessage}
          </Typography.Text>
        )
      }
    />
  );
}
