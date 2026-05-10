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
import {
  createDoctorRoundRecord,
  getDoctorRoundRecord,
  listDoctorRoundRecords,
  updateDoctorRoundRecord,
} from '../../api/healthApi';
import { extractApiErrorMessage } from '../../api/client';
import AdminPageScaffold from '../../components/AdminPageScaffold';
import type { DoctorRoundRecord, DoctorRoundRecordSaveRequest } from '../../types/care';

interface FilterFormValues {
  elderId?: number;
  doctorId?: number;
  roundDate?: string;
}

interface EditFormValues {
  elderId: number;
  content: string;
  riskFlag: boolean;
  roundTime: string;
}

function formatLocalDateInput(date = new Date()) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function formatLocalDatetimeInput(date = new Date()) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  return `${year}-${month}-${day}T${hours}:${minutes}`;
}

const initialFilterValues: FilterFormValues = {
  roundDate: formatLocalDateInput(),
};

function renderRiskFlag(value?: boolean) {
  if (value) {
    return <Tag color="error">有风险</Tag>;
  }
  return <Tag color="success">无风险</Tag>;
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

function summarizeContent(value?: string) {
  if (!value || !value.trim()) {
    return '-';
  }
  const normalized = value.replace(/\s+/g, ' ').trim();
  return normalized.length > 24 ? `${normalized.slice(0, 24)}...` : normalized;
}

const columns: ColumnsType<DoctorRoundRecord> = [
  { title: '记录ID', dataIndex: 'roundRecordId', key: 'roundRecordId', width: 100 },
  { title: '老人ID', dataIndex: 'elderId', key: 'elderId', width: 100 },
  { title: '老人姓名', dataIndex: 'elderName', key: 'elderName', width: 120, render: (value?: string) => value || '-' },
  { title: '医生', dataIndex: 'doctorName', key: 'doctorName', width: 140, render: (value?: string) => value || '-' },
  { title: '风险标记', dataIndex: 'riskFlag', key: 'riskFlag', width: 120, render: (value?: boolean) => renderRiskFlag(value) },
  {
    title: '查房摘要',
    dataIndex: 'content',
    key: 'contentSummary',
    width: 220,
    render: (value?: string) => summarizeContent(value),
  },
  { title: '查房时间', dataIndex: 'roundTime', key: 'roundTime', width: 180, render: (value?: string) => formatDateTime(value) },
  { title: '更新时间', dataIndex: 'updatedAt', key: 'updatedAt', width: 180, render: (value?: string) => formatDateTime(value) },
];

function toDatetimeLocalInput(value?: string) {
  if (!value) {
    return '';
  }
  return value.slice(0, 16);
}

function buildDefaultRoundTime() {
  return formatLocalDatetimeInput();
}

export default function DoctorRoundRecordsPage() {
  const [filterForm] = Form.useForm<FilterFormValues>();
  const [editForm] = Form.useForm<EditFormValues>();
  const [listLoading, setListLoading] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [submitLoading, setSubmitLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [records, setRecords] = useState<DoctorRoundRecord[]>([]);
  const [selectedRecordId, setSelectedRecordId] = useState<number | null>(null);
  const [selectedRecord, setSelectedRecord] = useState<DoctorRoundRecord | null>(null);
  const [queryParams, setQueryParams] = useState<FilterFormValues>(initialFilterValues);

  const loadList = async (params: FilterFormValues = queryParams, keepSelectedId?: number | null) => {
    setListLoading(true);
    setErrorMessage(null);
    try {
      const data = await listDoctorRoundRecords(params);
      setRecords(data);
      const preservedId = keepSelectedId ?? selectedRecordId;
      if (preservedId && data.some((item) => item.roundRecordId === preservedId)) {
        return;
      }

      const firstRecordId = data[0]?.roundRecordId;
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
      setErrorMessage(extractApiErrorMessage(error, '加载查房记录列表失败'));
    } finally {
      setListLoading(false);
    }
  };

  const loadDetail = async (recordId: number) => {
    setDetailLoading(true);
    setErrorMessage(null);
    try {
      const detail = await getDoctorRoundRecord(recordId);
      setSelectedRecordId(recordId);
      setSelectedRecord(detail);
      editForm.setFieldsValue({
        elderId: detail.elderId ?? undefined,
        content: detail.content ?? '',
        riskFlag: Boolean(detail.riskFlag),
        roundTime: toDatetimeLocalInput(detail.roundTime),
      });
    } catch (error) {
      setSelectedRecord(null);
      setErrorMessage(extractApiErrorMessage(error, '加载查房记录详情失败'));
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
    editForm.setFieldsValue({
      elderId: undefined,
      content: '',
      riskFlag: false,
      roundTime: buildDefaultRoundTime(),
    });
  };

  const handleSubmit = async (values: EditFormValues) => {
    setSubmitLoading(true);
    setErrorMessage(null);
    setSuccessMessage(null);

    try {
      const payload: DoctorRoundRecordSaveRequest = {
        elderId: values.elderId,
        content: values.content,
        riskFlag: values.riskFlag,
        roundTime: values.roundTime,
      };

      const saved = selectedRecordId
        ? await updateDoctorRoundRecord(selectedRecordId, payload)
        : await createDoctorRoundRecord(payload);

      setSuccessMessage(selectedRecordId ? '查房记录已更新' : '查房记录已创建');
      await loadList(queryParams, saved.roundRecordId ?? selectedRecordId ?? undefined);
      if (typeof saved.roundRecordId === 'number') {
        await loadDetail(saved.roundRecordId);
      }
    } catch (error) {
      setErrorMessage(extractApiErrorMessage(error, '保存查房记录失败'));
    } finally {
      setSubmitLoading(false);
    }
  };

  return (
    <AdminPageScaffold
      title="查房记录管理"
      description="按老人、医生和日期查询查房记录，维护查房内容、风险标记和处理建议。"
      extra={<Button onClick={handleCreate}>新建记录</Button>}
    >
      <Alert type="info" showIcon message="后台可按老人、医生和日期查询查房记录，并支持维护医生查房结论与风险标记。" />
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
                    <Form.Item label="医生ID" name="doctorId">
                      <InputNumber min={1} placeholder="可选" style={{ width: '100%' }} />
                    </Form.Item>
                  </Col>
                  <Col xs={24} md={8} lg={6}>
                    <Form.Item label="查房日期" name="roundDate">
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
            label: `查房记录列表（${records.length}）`,
            children: (
              <Table<DoctorRoundRecord>
                rowKey={(record) => String(record.roundRecordId)}
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
                    if (typeof record.roundRecordId === 'number') {
                      void loadDetail(record.roundRecordId);
                    }
                  },
                })}
                scroll={{ x: 1400 }}
                locale={{ emptyText: listLoading ? '正在加载查房记录...' : '暂无查房记录' }}
              />
            ),
          },
          {
            key: 'editor',
            label: selectedRecordId ? '编辑查房记录' : '新建查房记录',
            children: (
              <Space direction="vertical" size="large" style={{ width: '100%' }}>
                {selectedRecord ? (
                  <Descriptions bordered size="small" column={2}>
                    <Descriptions.Item label="记录ID">{selectedRecord.roundRecordId ?? '-'}</Descriptions.Item>
                    <Descriptions.Item label="医生">{selectedRecord.doctorName ?? '-'}</Descriptions.Item>
                    <Descriptions.Item label="老人姓名">{selectedRecord.elderName ?? '-'}</Descriptions.Item>
                    <Descriptions.Item label="风险标记">{renderRiskFlag(selectedRecord.riskFlag)}</Descriptions.Item>
                    <Descriptions.Item label="查房时间">{formatDateTime(selectedRecord.roundTime)}</Descriptions.Item>
                    <Descriptions.Item label="更新时间">{formatDateTime(selectedRecord.updatedAt)}</Descriptions.Item>
                    <Descriptions.Item label="查房内容" span={2}>{selectedRecord.content ?? '-'}</Descriptions.Item>
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
                <Form.Item label="查房时间" name="roundTime" rules={[{ required: true, message: '请选择查房时间' }]}>
                  <Input type="datetime-local" />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item label="风险标记" name="riskFlag" rules={[{ required: true, message: '请选择风险标记' }]}>
                  <Select
                    options={[
                      { label: '无风险', value: false },
                      { label: '有风险', value: true },
                    ]}
                  />
                </Form.Item>
              </Col>
              <Col span={24}>
                <Form.Item label="查房内容" name="content" rules={[{ required: true, message: '请输入查房内容' }]}>
                  <Input.TextArea rows={8} placeholder="请输入查房经过、观察结果、风险判断与处理建议" />
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
