import { useEffect, useState } from 'react';
import { Alert, Button, Card, Form, Input, Modal, Space, Table, Typography } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { listMyElderBindings, createSelfBinding, type ElderBindingDto, type SelfBindingRequest } from '../api/elderBindingApi';
import { extractApiErrorMessage } from '../api/client';
import { useUserStore } from '../stores/userStore';

export default function MyEldersPage() {
  const [bindings, setBindings] = useState<ElderBindingDto[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [form] = Form.useForm<SelfBindingRequest>();
  const { refreshElderBindings } = useUserStore();

  const loadBindings = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await listMyElderBindings();
      setBindings(data);
    } catch (err) {
      setError(extractApiErrorMessage(err, '加载绑定列表失败'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadBindings();
  }, []);

  const handleSubmit = async (values: SelfBindingRequest) => {
    setSubmitting(true);
    setError(null);
    try {
      await createSelfBinding(values);
      setModalVisible(false);
      form.resetFields();
      await loadBindings();
      await refreshElderBindings();
    } catch (err) {
      setError(extractApiErrorMessage(err, '绑定失败'));
    } finally {
      setSubmitting(false);
    }
  };

  const columns = [
    { title: '老人姓名', dataIndex: 'elderName', key: 'elderName' },
    { title: '老人ID', dataIndex: 'elderId', key: 'elderId' },
    { 
      title: '绑定类型', 
      dataIndex: 'bindingType', 
      key: 'bindingType',
      render: (type: string) => type === 'SELF' ? '本人' : '家属'
    },
    { title: '关系', dataIndex: 'relationToElder', key: 'relationToElder' },
    { title: '状态', dataIndex: 'status', key: 'status' },
  ];

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Card>
        <Space direction="vertical" size="middle" style={{ width: '100%' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Typography.Title level={4} style={{ margin: 0 }}>我的老人</Typography.Title>
            <Button type="primary" icon={<PlusOutlined />} onClick={() => setModalVisible(true)}>
              绑定本人
            </Button>
          </div>

          {error && <Alert type="error" message={error} showIcon closable onClose={() => setError(null)} />}

          <Table
            dataSource={bindings}
            columns={columns}
            loading={loading}
            rowKey="id"
            pagination={false}
          />
        </Space>
      </Card>

      <Modal
        title="绑定本人为老人"
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        footer={null}
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item label="姓名" name="elderName" rules={[{ required: true, message: '请输入姓名' }]}>
            <Input placeholder="请输入姓名" />
          </Form.Item>
          <Form.Item label="身份证号" name="idCard" rules={[{ required: true, message: '请输入身份证号' }]}>
            <Input placeholder="请输入身份证号" />
          </Form.Item>
          <Form.Item label="手机号" name="phone">
            <Input placeholder="请输入手机号" />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" loading={submitting}>提交</Button>
              <Button onClick={() => setModalVisible(false)}>取消</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </Space>
  );
}
