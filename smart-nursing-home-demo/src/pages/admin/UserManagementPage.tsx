import { useEffect, useMemo, useState } from 'react';
import {
  Alert,
  Button,
  Card,
  Form,
  Input,
  InputNumber,
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
  createUser,
  deleteUser,
  getUsers,
  resetUserPassword,
  updateUser,
  updateUserStatus,
} from '../../api/userAdminApi';
import type { UserCreatePayload, UserItem, UserQueryParams, UserUpdatePayload } from '../../types/admin';

interface UserFormValues {
  username?: string;
  password?: string;
  realName?: string;
  email?: string;
  phone?: string;
  idCard?: string;
}

export default function UserManagementPage() {
  const [messageApi, contextHolder] = message.useMessage();
  const [list, setList] = useState<UserItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [forbidden, setForbidden] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [current, setCurrent] = useState(1);
  const [size, setSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [filters, setFilters] = useState<Pick<UserQueryParams, 'username' | 'realName' | 'phone' | 'status'>>({});

  const [createOpen, setCreateOpen] = useState(false);
  const [editOpen, setEditOpen] = useState(false);
  const [resetOpen, setResetOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [editingUser, setEditingUser] = useState<UserItem | null>(null);
  const [resetUser, setResetUser] = useState<UserItem | null>(null);

  const [searchForm] = Form.useForm<UserFormValues>();
  const [createForm] = Form.useForm<UserFormValues>();
  const [editForm] = Form.useForm<UserFormValues>();
  const [resetForm] = Form.useForm<{ newPassword: string }>();

  const loadUsers = async (page = current, pageSize = size, nextFilters = filters) => {
    setLoading(true);
    setErrorMessage(null);
    setForbidden(false);

    try {
      const data = await getUsers({
        current: page,
        size: pageSize,
        username: nextFilters.username,
        realName: nextFilters.realName,
        phone: nextFilters.phone,
        status: nextFilters.status,
      });
      setList(data.content ?? []);
      setTotal(data.totalElements ?? 0);
      setCurrent((data.number ?? page - 1) + 1);
      setSize(data.size ?? pageSize);
    } catch (error: unknown) {
      const maybeStatus = (error as { response?: { status?: number } })?.response?.status;
      if (maybeStatus === 403) {
        setForbidden(true);
      } else {
        setErrorMessage(extractApiErrorMessage(error, '加载用户列表失败'));
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadUsers(1, 10, filters);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const columns: ColumnsType<UserItem> = useMemo(
    () => [
      { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
      { title: '用户名', dataIndex: 'username', key: 'username' },
      { title: '姓名', dataIndex: 'realName', key: 'realName', render: (value) => value || '-' },
      { title: '手机号', dataIndex: 'phone', key: 'phone', render: (value) => value || '-' },
      { title: '邮箱', dataIndex: 'email', key: 'email', render: (value) => value || '-' },
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
        width: 320,
        render: (_, record) => (
          <Space wrap>
            <Button
              size="small"
              onClick={() => {
                setEditingUser(record);
                editForm.setFieldsValue({
                  realName: record.realName,
                  email: record.email,
                  phone: record.phone,
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
                  await updateUserStatus(record.id, record.status === 1 ? 0 : 1);
                  messageApi.success('状态更新成功');
                  void loadUsers(current, size, filters);
                } catch (error) {
                  messageApi.error(extractApiErrorMessage(error, '状态更新失败'));
                }
              }}
            >
              {record.status === 1 ? '禁用' : '启用'}
            </Button>
            <Button
              size="small"
              onClick={() => {
                setResetUser(record);
                resetForm.resetFields();
                setResetOpen(true);
              }}
            >
              重置密码
            </Button>
            <Popconfirm
              title="确认删除该用户吗？"
              onConfirm={async () => {
                try {
                  await deleteUser(record.id);
                  messageApi.success('删除成功');
                  void loadUsers(current, size, filters);
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
    [current, editForm, filters, messageApi, resetForm, size],
  );

  const handleSearch = () => {
    const values = searchForm.getFieldsValue();
    const nextFilters = {
      username: values.username?.trim() || undefined,
      realName: values.realName?.trim() || undefined,
      phone: values.phone?.trim() || undefined,
      status: values.status,
    };
    setFilters(nextFilters);
    void loadUsers(1, size, nextFilters);
  };

  const handleCreate = async () => {
    const values = await createForm.validateFields();
    setSubmitting(true);
    try {
      const payload: UserCreatePayload = {
        username: values.username!.trim(),
        password: values.password!.trim(),
        realName: values.realName?.trim(),
        email: values.email?.trim(),
        phone: values.phone?.trim(),
        idCard: values.idCard?.trim(),
      };
      await createUser(payload);
      messageApi.success('创建用户成功');
      setCreateOpen(false);
      createForm.resetFields();
      void loadUsers(1, size, filters);
    } catch (error) {
      messageApi.error(extractApiErrorMessage(error, '创建用户失败'));
    } finally {
      setSubmitting(false);
    }
  };

  const handleEdit = async () => {
    if (!editingUser) return;
    const values = await editForm.validateFields();
    setSubmitting(true);
    try {
      const payload: UserUpdatePayload = {
        realName: values.realName?.trim(),
        email: values.email?.trim(),
        phone: values.phone?.trim(),
        idCard: values.idCard?.trim(),
      };
      await updateUser(editingUser.id, payload);
      messageApi.success('更新用户成功');
      setEditOpen(false);
      setEditingUser(null);
      void loadUsers(current, size, filters);
    } catch (error) {
      messageApi.error(extractApiErrorMessage(error, '更新用户失败'));
    } finally {
      setSubmitting(false);
    }
  };

  const handleResetPassword = async () => {
    if (!resetUser) return;
    const values = await resetForm.validateFields();
    setSubmitting(true);
    try {
      await resetUserPassword(resetUser.id, values.newPassword.trim());
      messageApi.success('密码重置成功');
      setResetOpen(false);
      setResetUser(null);
      resetForm.resetFields();
    } catch (error) {
      messageApi.error(extractApiErrorMessage(error, '重置密码失败'));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      {contextHolder}
      <Typography.Title level={4} style={{ margin: 0 }}>
        用户管理
      </Typography.Title>

      {forbidden ? <Alert type="warning" message="无权限访问用户管理（403）" showIcon /> : null}
      {errorMessage ? <Alert type="error" message={errorMessage} showIcon /> : null}

      <Card>
        <Form form={searchForm} layout="inline">
          <Form.Item name="username" label="用户名">
            <Input placeholder="按用户名搜索" allowClear />
          </Form.Item>
          <Form.Item name="realName" label="姓名">
            <Input placeholder="按姓名搜索" allowClear />
          </Form.Item>
          <Form.Item name="phone" label="手机号">
            <Input placeholder="按手机号搜索" allowClear />
          </Form.Item>
          <Form.Item name="status" label="状态">
            <Select
              style={{ width: 120 }}
              allowClear
              options={[
                { label: '启用', value: 1 },
                { label: '禁用', value: 0 },
              ]}
            />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" onClick={handleSearch}>
                查询
              </Button>
              <Button
                onClick={() => {
                  searchForm.resetFields();
                  const nextFilters = {};
                  setFilters(nextFilters);
                  void loadUsers(1, 10, nextFilters);
                }}
              >
                重置
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>

      <Card
        title="用户列表"
        extra={
          <Button
            type="primary"
            onClick={() => {
              createForm.resetFields();
              setCreateOpen(true);
            }}
          >
            新增用户
          </Button>
        }
      >
        <Table<UserItem>
          rowKey="id"
          loading={loading}
          columns={columns}
          dataSource={list}
          pagination={{
            current,
            pageSize: size,
            total,
            showSizeChanger: true,
            onChange: (page, pageSize) => {
              void loadUsers(page, pageSize, filters);
            },
          }}
        />
      </Card>

      <Modal
        title="新增用户"
        open={createOpen}
        onCancel={() => setCreateOpen(false)}
        onOk={() => {
          void handleCreate();
        }}
        confirmLoading={submitting}
      >
        <Form form={createForm} layout="vertical">
          <Form.Item name="username" label="用户名" rules={[{ required: true, message: '请输入用户名' }]}>
            <Input placeholder="4-50位用户名" />
          </Form.Item>
          <Form.Item name="password" label="密码" rules={[{ required: true, message: '请输入密码' }]}>
            <Input.Password placeholder="至少6位" />
          </Form.Item>
          <Form.Item name="realName" label="姓名">
            <Input />
          </Form.Item>
          <Form.Item name="phone" label="手机号">
            <Input />
          </Form.Item>
          <Form.Item name="email" label="邮箱">
            <Input />
          </Form.Item>
          <Form.Item name="idCard" label="身份证号">
            <Input />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="编辑用户"
        open={editOpen}
        onCancel={() => {
          setEditOpen(false);
          setEditingUser(null);
        }}
        onOk={() => {
          void handleEdit();
        }}
        confirmLoading={submitting}
      >
        <Form form={editForm} layout="vertical">
          <Form.Item name="realName" label="姓名">
            <Input />
          </Form.Item>
          <Form.Item name="phone" label="手机号">
            <Input />
          </Form.Item>
          <Form.Item name="email" label="邮箱">
            <Input />
          </Form.Item>
          <Form.Item name="idCard" label="身份证号">
            <Input />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title={`重置密码：${resetUser?.username ?? ''}`}
        open={resetOpen}
        onCancel={() => {
          setResetOpen(false);
          setResetUser(null);
        }}
        onOk={() => {
          void handleResetPassword();
        }}
        confirmLoading={submitting}
      >
        <Form form={resetForm} layout="vertical">
          <Form.Item
            name="newPassword"
            label="新密码"
            rules={[{ required: true, message: '请输入新密码' }, { min: 6, message: '至少6位' }]}
          >
            <Input.Password placeholder="请输入新密码" />
          </Form.Item>
        </Form>
      </Modal>
    </Space>
  );
}
