import { useEffect, useMemo, useState } from 'react';
import {
  Alert,
  Button,
  Card,
  Form,
  Input,
  Modal,
  Popconfirm,
  Select,
  Space,
  Table,
  Tag,
  Typography,
  message,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { extractApiErrorMessage } from '../../api/client';
import {
  createPermission,
  deletePermission,
  getPermissions,
  updatePermission,
  updatePermissionStatus,
} from '../../api/permissionAdminApi';
import type { PermissionCreatePayload, PermissionItem, PermissionUpdatePayload } from '../../types/admin';

interface PermissionFormValues {
  permissionName?: string;
  permissionCode?: string;
  description?: string;
  status?: number;
}

export default function PermissionManagementPage() {
  const [messageApi, contextHolder] = message.useMessage();
  const [list, setList] = useState<PermissionItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [forbidden, setForbidden] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const [keyword, setKeyword] = useState<string>();
  const [createOpen, setCreateOpen] = useState(false);
  const [editOpen, setEditOpen] = useState(false);
  const [editingItem, setEditingItem] = useState<PermissionItem | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const [createForm] = Form.useForm<PermissionFormValues>();
  const [editForm] = Form.useForm<PermissionFormValues>();

  const load = async (nextKeyword?: string) => {
    setLoading(true);
    setForbidden(false);
    setErrorMessage(null);
    try {
      const data = await getPermissions(nextKeyword);
      setList(data ?? []);
    } catch (error: unknown) {
      const maybeStatus = (error as { response?: { status?: number } })?.response?.status;
      if (maybeStatus === 403) {
        setForbidden(true);
      } else {
        setErrorMessage(extractApiErrorMessage(error, '加载权限列表失败'));
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void load();
  }, []);

  const columns: ColumnsType<PermissionItem> = useMemo(
    () => [
      { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
      { title: '权限名称', dataIndex: 'permissionName', key: 'permissionName' },
      { title: '权限编码', dataIndex: 'permissionCode', key: 'permissionCode' },
      { title: '描述', dataIndex: 'description', key: 'description', render: (value) => value || '-' },
      {
        title: '状态',
        dataIndex: 'status',
        key: 'status',
        render: (status) =>
          status === 1 ? <Tag color="green">启用</Tag> : <Tag color="default">禁用</Tag>,
      },
      {
        title: '操作',
        key: 'actions',
        width: 280,
        render: (_, record) => (
          <Space wrap>
            <Button
              size="small"
              onClick={() => {
                setEditingItem(record);
                editForm.setFieldsValue({
                  permissionName: record.permissionName,
                  permissionCode: record.permissionCode,
                  description: record.description,
                  status: record.status ?? 1,
                });
                setEditOpen(true);
              }}
            >
              编辑
            </Button>
            <Button
              size="small"
              onClick={async () => {
                try {
                  await updatePermissionStatus(record.id, record.status === 1 ? 0 : 1);
                  messageApi.success('状态更新成功');
                  await load(keyword);
                } catch (error) {
                  messageApi.error(extractApiErrorMessage(error, '状态更新失败'));
                }
              }}
            >
              {record.status === 1 ? '禁用' : '启用'}
            </Button>
            <Popconfirm
              title="确认删除该权限吗？"
              onConfirm={async () => {
                try {
                  await deletePermission(record.id);
                  messageApi.success('删除成功');
                  await load(keyword);
                } catch (error) {
                  messageApi.error(extractApiErrorMessage(error, '删除失败'));
                }
              }}
            >
              <Button size="small" danger>
                删除
              </Button>
            </Popconfirm>
          </Space>
        ),
      },
    ],
    [editForm, keyword, messageApi],
  );

  const handleCreate = async () => {
    const values = await createForm.validateFields();
    setSubmitting(true);
    try {
      const payload: PermissionCreatePayload = {
        permissionName: values.permissionName!.trim(),
        permissionCode: values.permissionCode!.trim(),
        description: values.description?.trim(),
        status: values.status ?? 1,
      };
      await createPermission(payload);
      messageApi.success('创建权限成功');
      setCreateOpen(false);
      createForm.resetFields();
      await load(keyword);
    } catch (error) {
      messageApi.error(extractApiErrorMessage(error, '创建权限失败'));
    } finally {
      setSubmitting(false);
    }
  };

  const handleEdit = async () => {
    if (!editingItem) return;
    const values = await editForm.validateFields();
    setSubmitting(true);
    try {
      const payload: PermissionUpdatePayload = {
        permissionName: values.permissionName?.trim(),
        permissionCode: values.permissionCode?.trim(),
        description: values.description?.trim(),
        status: values.status,
      };
      await updatePermission(editingItem.id, payload);
      messageApi.success('更新权限成功');
      setEditOpen(false);
      setEditingItem(null);
      await load(keyword);
    } catch (error) {
      messageApi.error(extractApiErrorMessage(error, '更新权限失败'));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      {contextHolder}
      <Typography.Title level={4} style={{ margin: 0 }}>
        权限管理
      </Typography.Title>

      {forbidden ? <Alert type="warning" message="无权限访问权限管理（403）" showIcon /> : null}
      {errorMessage ? <Alert type="error" message={errorMessage} showIcon /> : null}

      <Card>
        <Space>
          <Input.Search
            placeholder="按权限名称/编码搜索"
            allowClear
            onSearch={(value) => {
              const next = value.trim() || undefined;
              setKeyword(next);
              void load(next);
            }}
            style={{ width: 320 }}
          />
          <Button
            type="primary"
            onClick={() => {
              createForm.setFieldsValue({ status: 1 });
              setCreateOpen(true);
            }}
          >
            新增权限
          </Button>
        </Space>
      </Card>

      <Card title="权限列表">
        <Table<PermissionItem> rowKey="id" columns={columns} dataSource={list} loading={loading} />
      </Card>

      <Modal
        title="新增权限"
        open={createOpen}
        onCancel={() => setCreateOpen(false)}
        onOk={() => {
          void handleCreate();
        }}
        confirmLoading={submitting}
      >
        <Form form={createForm} layout="vertical" initialValues={{ status: 1 }}>
          <Form.Item
            name="permissionName"
            label="权限名称"
            rules={[{ required: true, message: '请输入权限名称' }]}
          >
            <Input />
          </Form.Item>
          <Form.Item
            name="permissionCode"
            label="权限编码"
            rules={[{ required: true, message: '请输入权限编码' }]}
          >
            <Input />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea rows={3} />
          </Form.Item>
          <Form.Item name="status" label="状态">
            <Select options={[{ label: '启用', value: 1 }, { label: '禁用', value: 0 }]} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="编辑权限"
        open={editOpen}
        onCancel={() => {
          setEditOpen(false);
          setEditingItem(null);
        }}
        onOk={() => {
          void handleEdit();
        }}
        confirmLoading={submitting}
      >
        <Form form={editForm} layout="vertical">
          <Form.Item
            name="permissionName"
            label="权限名称"
            rules={[{ required: true, message: '请输入权限名称' }]}
          >
            <Input />
          </Form.Item>
          <Form.Item
            name="permissionCode"
            label="权限编码"
            rules={[{ required: true, message: '请输入权限编码' }]}
          >
            <Input />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea rows={3} />
          </Form.Item>
          <Form.Item name="status" label="状态">
            <Select options={[{ label: '启用', value: 1 }, { label: '禁用', value: 0 }]} />
          </Form.Item>
        </Form>
      </Modal>
    </Space>
  );
}
