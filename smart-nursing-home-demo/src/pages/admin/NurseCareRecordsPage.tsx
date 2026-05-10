import { useEffect, useState } from 'react';
import {
  Alert,
  Button,
  Collapse,
  Col,
  Descriptions,
  Form,
  Input,
  InputNumber,
  Row,
  Select,
  Space,
  Table,
  Tag,
  Typography,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { createNurseCareRecord, getNurseCareRecord, listNurseCareRecords, updateNurseCareRecord } from '../../api/careDeliveryApi';
import { extractApiErrorMessage } from '../../api/client';
import AdminPageScaffold from '../../components/AdminPageScaffold';
import type { NurseCareRecord, NurseCareRecordSaveRequest } from '../../types/care';

interface FilterFormValues {
  elderId?: number;
  nurseId?: number;
  recordDate?: string;
}

interface EditFormValues {
  elderId: number;
  servicePlanId?: number;
  recordDate: string;
  dietStatus?: string;
  sleepStatus?: string;
  vitalsStatus?: string;
  skinStatus?: string;
  emotionStatus?: string;
  medicationStatus?: string;
  rehabilitationStatus?: string;
  remark?: string;
}

const statusOptions = [
  { label: '正常', value: '正常' },
  { label: '一般', value: '一般' },
  { label: '需观察', value: '需观察' },
  { label: '异常', value: '异常' },
];

function formatLocalDateInput(date = new Date()) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

const initialFilterValues: FilterFormValues = {
  recordDate: formatLocalDateInput(),
};

function getRecordValue(record: NurseCareRecord | null, key: string) {
  const value = record?.recordFormData?.[key];
  return typeof value === 'string' && value.trim() ? value : '-';
}

function getRecordStatus(record: NurseCareRecord, key: string) {
  const value = record.recordFormData?.[key];
  return typeof value === 'string' ? value : undefined;
}

function renderStatusTag(value?: string) {
  if (value === '异常') {
    return <Tag color="error">异常</Tag>;
  }
  if (value === '需观察') {
    return <Tag color="warning">需观察</Tag>;
  }
  if (value === '一般') {
    return <Tag color="processing">一般</Tag>;
  }
  if (value === '正常') {
    return <Tag color="success">正常</Tag>;
  }
  return <Tag>{value ?? '-'}</Tag>;
}

function formatDateTime(value?: string) {
  if (!value) {
    return '-';
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return date.toLocaleString();
}

function summarizeRecord(record: NurseCareRecord) {
  const fieldPairs = [
    ['dietStatus', '饮食'],
    ['sleepStatus', '睡眠'],
    ['vitalsStatus', '生命体征'],
    ['skinStatus', '皮肤'],
    ['emotionStatus', '情绪'],
    ['medicationStatus', '用药'],
    ['rehabilitationStatus', '康复'],
  ] as const;

  const abnormalFields = fieldPairs
    .filter(([key]) => {
      const value = getRecordStatus(record, key);
      return value === '异常' || value === '需观察';
    })
    .map(([, label]) => label);

  if (abnormalFields.length > 0) {
    return `${abnormalFields.join('、')}需重点关注`;
  }

  const nonNormalFields = fieldPairs
    .filter(([key]) => getRecordStatus(record, key) === '一般')
    .map(([, label]) => label);
  if (nonNormalFields.length > 0) {
    return `${nonNormalFields.join('、')}情况一般`;
  }

  const remark = getRecordStatus(record, 'remark');
  if (remark && remark.trim()) {
    return remark.length > 16 ? `${remark.slice(0, 16)}...` : remark;
  }

  const hasStructuredData = Object.keys(record.recordFormData ?? {}).length > 0;
  return hasStructuredData ? '已记录护理情况' : '情况平稳';
}

function compactRecordFormData(values: EditFormValues) {
  return Object.fromEntries(
    Object.entries({
      dietStatus: values.dietStatus,
      sleepStatus: values.sleepStatus,
      vitalsStatus: values.vitalsStatus,
      skinStatus: values.skinStatus,
      emotionStatus: values.emotionStatus,
      medicationStatus: values.medicationStatus,
      rehabilitationStatus: values.rehabilitationStatus,
      remark: values.remark,
    }).filter(([, value]) => typeof value === 'string' && value.trim().length > 0),
  );
}

function buildEditValues(record?: NurseCareRecord | null): EditFormValues {
  const formData = record?.recordFormData ?? {};
  return {
    elderId: record?.elderId ?? undefined,
    servicePlanId: record?.servicePlanId ?? undefined,
    recordDate: record?.recordDate ?? formatLocalDateInput(),
    dietStatus: typeof formData.dietStatus === 'string' ? formData.dietStatus : undefined,
    sleepStatus: typeof formData.sleepStatus === 'string' ? formData.sleepStatus : undefined,
    vitalsStatus: typeof formData.vitalsStatus === 'string' ? formData.vitalsStatus : undefined,
    skinStatus: typeof formData.skinStatus === 'string' ? formData.skinStatus : undefined,
    emotionStatus: typeof formData.emotionStatus === 'string' ? formData.emotionStatus : undefined,
    medicationStatus: typeof formData.medicationStatus === 'string' ? formData.medicationStatus : undefined,
    rehabilitationStatus: typeof formData.rehabilitationStatus === 'string' ? formData.rehabilitationStatus : undefined,
    remark: typeof formData.remark === 'string' ? formData.remark : undefined,
  };
}

const columns: ColumnsType<NurseCareRecord> = [
  { title: '记录ID', dataIndex: 'recordId', key: 'recordId', width: 100 },
  { title: '老人ID', dataIndex: 'elderId', key: 'elderId', width: 100 },
  { title: '老人姓名', dataIndex: 'elderName', key: 'elderName', width: 120, render: (value?: string) => value || '-' },
  { title: '护士', dataIndex: 'nurseName', key: 'nurseName', width: 140, render: (value?: string) => value || '-' },
  { title: '服务计划ID', dataIndex: 'servicePlanId', key: 'servicePlanId', width: 120, render: (value?: number) => value ?? '-' },
  {
    title: '生命体征',
    key: 'vitalsStatus',
    width: 120,
    render: (_value, record) => renderStatusTag(getRecordStatus(record, 'vitalsStatus')),
  },
  {
    title: '护理摘要',
    key: 'summary',
    width: 180,
    render: (_value, record) => summarizeRecord(record),
  },
  { title: '记录日期', dataIndex: 'recordDate', key: 'recordDate', width: 140, render: (value?: string) => value || '-' },
  { title: '更新时间', dataIndex: 'updatedAt', key: 'updatedAt', width: 180, render: (value?: string) => formatDateTime(value) },
];

export default function NurseCareRecordsPage() {
  const [filterForm] = Form.useForm<FilterFormValues>();
  const [editForm] = Form.useForm<EditFormValues>();
  const [listLoading, setListLoading] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [submitLoading, setSubmitLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [records, setRecords] = useState<NurseCareRecord[]>([]);
  const [selectedRecordId, setSelectedRecordId] = useState<number | null>(null);
  const [selectedRecord, setSelectedRecord] = useState<NurseCareRecord | null>(null);
  const [queryParams, setQueryParams] = useState<FilterFormValues>(initialFilterValues);

  const loadList = async (params: FilterFormValues = queryParams, keepSelectedId?: number | null) => {
    setListLoading(true);
    setErrorMessage(null);
    try {
      const data = await listNurseCareRecords(params);
      setRecords(data);
      const preservedId = keepSelectedId ?? selectedRecordId;
      if (preservedId && data.some((item) => item.recordId === preservedId)) {
        return;
      }

      const firstRecordId = data[0]?.recordId;
      if (typeof firstRecordId === 'number') {
        void loadDetail(firstRecordId);
      } else {
        setSelectedRecordId(null);
        setSelectedRecord(null);
      }
    } catch (error) {
      setRecords([]);
      setSelectedRecordId(null);
      setSelectedRecord(null);
      setErrorMessage(extractApiErrorMessage(error, '加载护理记录列表失败'));
    } finally {
      setListLoading(false);
    }
  };

  const loadDetail = async (recordId: number) => {
    setDetailLoading(true);
    setErrorMessage(null);
    try {
      const detail = await getNurseCareRecord(recordId);
      setSelectedRecordId(recordId);
      setSelectedRecord(detail);
      editForm.setFieldsValue(buildEditValues(detail));
    } catch (error) {
      setSelectedRecord(null);
      setErrorMessage(extractApiErrorMessage(error, '加载护理记录详情失败'));
    } finally {
      setDetailLoading(false);
    }
  };

  useEffect(() => {
    filterForm.setFieldsValue(initialFilterValues);
    void loadList(initialFilterValues);
  }, []);

  const handleSearch = async () => {
    const nextParams = filterForm.getFieldsValue();
    setQueryParams(nextParams);
    await loadList(nextParams);
  };

  const handleReset = async () => {
    filterForm.resetFields();
    filterForm.setFieldsValue(initialFilterValues);
    setQueryParams(initialFilterValues);
    await loadList(initialFilterValues);
  };

  const handleCreate = () => {
    setSelectedRecordId(null);
    setSelectedRecord(null);
    setSuccessMessage(null);
    setErrorMessage(null);
    editForm.setFieldsValue(buildEditValues(null));
  };

  const handleSubmit = async (values: EditFormValues) => {
    setSubmitLoading(true);
    setErrorMessage(null);
    setSuccessMessage(null);

    try {
      const payload: NurseCareRecordSaveRequest = {
        elderId: values.elderId,
        servicePlanId: values.servicePlanId,
        recordDate: values.recordDate,
        recordFormData: compactRecordFormData(values),
      };

      const saved = selectedRecordId
        ? await updateNurseCareRecord(selectedRecordId, payload)
        : await createNurseCareRecord(payload);

      setSuccessMessage(selectedRecordId ? '护理记录已更新' : '护理记录已创建');
      await loadList(queryParams, saved.recordId ?? selectedRecordId ?? undefined);
      if (typeof saved.recordId === 'number') {
        await loadDetail(saved.recordId);
      }
    } catch (error) {
      setErrorMessage(extractApiErrorMessage(error, '保存护理记录失败'));
    } finally {
      setSubmitLoading(false);
    }
  };

  return (
    <AdminPageScaffold
      title="护理记录管理"
      description="按老人、护士和日期查询护理记录，维护饮食、睡眠、生命体征、用药和康复等结构化护理观察。"
      extra={<Button onClick={handleCreate}>新建记录</Button>}
    >
      <Alert type="info" showIcon message="后台可按老人、护士和日期查询护理记录，并以结构化表单维护护士护理记录。" />
      {errorMessage ? <Alert type="error" showIcon message={errorMessage} /> : null}
      {successMessage ? <Alert type="success" showIcon message={successMessage} /> : null}

      <Collapse
        items={[
          {
            key: 'filters',
            label: '筛选条件',
            children: (
              <Form<FilterFormValues> form={filterForm} layout="vertical" initialValues={initialFilterValues}>
                <Row gutter={16}>
                  <Col xs={24} md={8} lg={6}>
                    <Form.Item label="老人ID" name="elderId">
                      <InputNumber min={1} placeholder="可选" style={{ width: '100%' }} />
                    </Form.Item>
                  </Col>
                  <Col xs={24} md={8} lg={6}>
                    <Form.Item label="护士ID" name="nurseId">
                      <InputNumber min={1} placeholder="可选" style={{ width: '100%' }} />
                    </Form.Item>
                  </Col>
                  <Col xs={24} md={8} lg={6}>
                    <Form.Item label="记录日期" name="recordDate">
                      <Input type="date" />
                    </Form.Item>
                  </Col>
                </Row>
                <Space>
                  <Button type="primary" onClick={() => void handleSearch()} loading={listLoading}>
                    查询
                  </Button>
                  <Button onClick={() => void handleReset()} disabled={listLoading}>
                    重置
                  </Button>
                </Space>
              </Form>
            ),
          },
          {
            key: 'records',
            label: `护理记录列表（${records.length}）`,
            children: (
              <Table<NurseCareRecord>
                rowKey={(record) => String(record.recordId)}
                loading={listLoading}
                columns={columns}
                dataSource={records}
                pagination={{ pageSize: 8 }}
                rowSelection={{
                  type: 'radio',
                  selectedRowKeys: selectedRecordId ? [String(selectedRecordId)] : [],
                  onChange: (selectedRowKeys) => {
                    const nextId = Number(selectedRowKeys[0]);
                    if (Number.isFinite(nextId)) {
                      void loadDetail(nextId);
                    }
                  },
                }}
                onRow={(record) => ({
                  onClick: () => {
                    if (typeof record.recordId === 'number') {
                      void loadDetail(record.recordId);
                    }
                  },
                })}
                scroll={{ x: 1400 }}
                locale={{ emptyText: listLoading ? '正在加载护理记录...' : '暂无护理记录' }}
              />
            ),
          },
          {
            key: 'editor',
            label: selectedRecordId ? '编辑护理记录' : '新建护理记录',
            children: (
              <Space direction="vertical" size="large" style={{ width: '100%' }}>
                {selectedRecord ? (
                  <Descriptions bordered size="small" column={2}>
                    <Descriptions.Item label="记录ID">{selectedRecord.recordId ?? '-'}</Descriptions.Item>
                    <Descriptions.Item label="护士">{selectedRecord.nurseName ?? '-'}</Descriptions.Item>
                    <Descriptions.Item label="老人姓名">{selectedRecord.elderName ?? '-'}</Descriptions.Item>
                    <Descriptions.Item label="更新时间">{formatDateTime(selectedRecord.updatedAt)}</Descriptions.Item>
                    <Descriptions.Item label="饮食情况">{getRecordValue(selectedRecord, 'dietStatus')}</Descriptions.Item>
                    <Descriptions.Item label="睡眠情况">{getRecordValue(selectedRecord, 'sleepStatus')}</Descriptions.Item>
                    <Descriptions.Item label="生命体征">{getRecordValue(selectedRecord, 'vitalsStatus')}</Descriptions.Item>
                    <Descriptions.Item label="皮肤情况">{getRecordValue(selectedRecord, 'skinStatus')}</Descriptions.Item>
                    <Descriptions.Item label="情绪状态">{getRecordValue(selectedRecord, 'emotionStatus')}</Descriptions.Item>
                    <Descriptions.Item label="用药情况">{getRecordValue(selectedRecord, 'medicationStatus')}</Descriptions.Item>
                    <Descriptions.Item label="康复训练">{getRecordValue(selectedRecord, 'rehabilitationStatus')}</Descriptions.Item>
                    <Descriptions.Item label="备注" span={2}>{getRecordValue(selectedRecord, 'remark')}</Descriptions.Item>
                  </Descriptions>
                ) : (
                  <Typography.Text type="secondary">当前为新建模式，可直接填写并保存。</Typography.Text>
                )}

                <Form<EditFormValues> form={editForm} layout="vertical" onFinish={handleSubmit}>
            <Row gutter={16}>
              <Col span={8}>
                <Form.Item label="老人ID" name="elderId" rules={[{ required: true, message: '请输入老人编号' }]}>
                  <InputNumber style={{ width: '100%' }} min={1} />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item label="服务计划ID" name="servicePlanId">
                  <InputNumber style={{ width: '100%' }} min={1} />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item label="记录日期" name="recordDate" rules={[{ required: true, message: '请选择记录日期' }]}>
                  <Input type="date" />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item label="饮食情况" name="dietStatus">
                  <Select allowClear options={statusOptions} placeholder="请选择饮食情况" />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item label="睡眠情况" name="sleepStatus">
                  <Select allowClear options={statusOptions} placeholder="请选择睡眠情况" />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item label="生命体征" name="vitalsStatus">
                  <Select allowClear options={statusOptions} placeholder="请选择生命体征情况" />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item label="皮肤情况" name="skinStatus">
                  <Select allowClear options={statusOptions} placeholder="请选择皮肤情况" />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item label="情绪状态" name="emotionStatus">
                  <Select allowClear options={statusOptions} placeholder="请选择情绪状态" />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item label="用药情况" name="medicationStatus">
                  <Select allowClear options={statusOptions} placeholder="请选择用药情况" />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item label="康复训练" name="rehabilitationStatus">
                  <Select allowClear options={statusOptions} placeholder="请选择康复训练情况" />
                </Form.Item>
              </Col>
              <Col span={24}>
                <Form.Item label="护理备注" name="remark">
                  <Input.TextArea rows={5} placeholder="请输入护理观察、异常情况与处理意见" />
                </Form.Item>
              </Col>
            </Row>
                  <Button type="primary" htmlType="submit" loading={submitLoading}>
                    {selectedRecordId ? '保存修改' : '创建记录'}
                  </Button>
                </Form>
              </Space>
            ),
          },
        ]}
      />
    </AdminPageScaffold>
  );
}
