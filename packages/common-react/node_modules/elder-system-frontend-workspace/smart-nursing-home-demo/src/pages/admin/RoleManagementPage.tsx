import { useEffect, useMemo, useState } from 'react';
import {
  Alert,
  Button,
  Card,
  Form,
  InputNumber,
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
  assignPermissionToRole,
  assignRoleToUser,
  getRolesWithPermissions,
  removePermissionFromRole,
  removeRoleFromUser,
} from '../../api/roleAdminApi';
import { getPermissions } from '../../api/permissionAdminApi';
import type { PermissionItem, RoleItem } from '../../types/admin';

export default function RoleManagementPage() {
  const [messageApi, contextHolder] = message.useMessage();
  const [list, setList] = useState<RoleItem[]>([]);
  const [permissions, setPermissions] = useState<PermissionItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [forbidden, setForbidden] = useState(false);

  const [bindForm] = Form.useForm<{ userId: number; roleId: number }>();
  const [permissionForm] = Form.useForm<{ roleId: number; permissionId: number }>();

  const load = async () => {
    setLoading(true);
    setErrorMessage(null);
    setForbidden(false);
    try {
      const [roles, perms] = await Promise.all([getRolesWithPermissions(), getPermissions()]);
      setList(roles ?? []);
      setPermissions(perms ?? []);
    } catch (error: unknown) {
      const maybeStatus = (error as { response?: { status?: number } })?.response?.status;
      if (maybeStatus === 403) {
        setForbidden(true);
      } else {
        setErrorMessage(extractApiErrorMessage(error, '加载角色权限数据失败'));
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void load();
  }, []);

  const columns: ColumnsType<RoleItem> = useMemo(
    () => [
      { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
      { title: '角色名称', dataIndex: 'roleName', key: 'roleName' },
      { title: '角色编码', dataIndex: 'roleCode', key: 'roleCode' },
      { title: '描述', dataIndex: 'description', key: 'description', render: (value) => value || '-' },
      {
        title: '状态',
        dataIndex: 'status',
        key: 'status',
        render: (status) =>
          status === 1 ? <Tag color="green">启用</Tag> : <Tag color="default">禁用</Tag>,
      },
      {
        title: '权限',
        dataIndex: 'permissions',
        key: 'permissions',
        render: (value: PermissionItem[] | undefined, record) => (
          <Space wrap>
            {(value ?? []).map((item) => (
              <Tag key={item.id} closable onClose={(e) => {
                e.preventDefault();
                void (async () => {
                  try {
                    await removePermissionFromRole(record.id, item.id);
                    messageApi.success('移除权限成功');
                    await load();
                  } catch (error) {
                    messageApi.error(extractApiErrorMessage(error, '移除权限失败'));
                  }
                })();
              }}>
                {item.permissionName}
              </Tag>
            ))}
          </Space>
        ),
      },
    ],
    [messageApi],
  );

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      {contextHolder}
      <Typography.Title level={4} style={{ margin: 0 }}>
        角色管理
      </Typography.Title>

      {forbidden ? <Alert type="warning" message="无权限访问角色管理（403）" showIcon /> : null}
      {errorMessage ? <Alert type="error" message={errorMessage} showIcon /> : null}

      <Card title="为用户分配/移除角色">
        <Form
          form={bindForm}
          layout="inline"
          onFinish={async (values) => {
            try {
              await assignRoleToUser(values.userId, values.roleId);
              messageApi.success('分配角色成功');
              bindForm.resetFields();
            } catch (error) {
              messageApi.error(extractApiErrorMessage(error, '分配角色失败'));
            }
          }}
        >
          <Form.Item name="userId" label="用户ID" rules={[{ required: true, message: '请输入用户ID' }]}>
            <InputNumber min={1} />
          </Form.Item>
          <Form.Item name="roleId" label="角色" rules={[{ required: true, message: '请选择角色' }]}>
            <Select
              style={{ width: 240 }}
              options={list.map((role) => ({
                label: `${role.roleName}(${role.roleCode})`,
                value: role.id,
              }))}
            />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">
                分配
              </Button>
              <Popconfirm
                title="确认移除该用户角色吗？"
                onConfirm={async () => {
                  const values = await bindForm.validateFields();
                  try {
                    await removeRoleFromUser(values.userId, values.roleId);
                    messageApi.success('移除角色成功');
                    bindForm.resetFields();
                  } catch (error) {
                    messageApi.error(extractApiErrorMessage(error, '移除角色失败'));
                  }
                }}
              >
                <Button danger>移除</Button>
              </Popconfirm>
            </Space>
          </Form.Item>
        </Form>
      </Card>

      <Card title="为角色分配权限">
        <Form
          form={permissionForm}
          layout="inline"
          onFinish={async (values) => {
            try {
              await assignPermissionToRole(values.roleId, values.permissionId);
              messageApi.success('分配权限成功');
              permissionForm.resetFields();
              await load();
            } catch (error) {
              messageApi.error(extractApiErrorMessage(error, '分配权限失败'));
            }
          }}
        >
          <Form.Item name="roleId" label="角色" rules={[{ required: true, message: '请选择角色' }]}>
            <Select
              style={{ width: 220 }}
              options={list.map((role) => ({ label: role.roleName, value: role.id }))}
            />
          </Form.Item>
          <Form.Item
            name="permissionId"
            label="权限"
            rules={[{ required: true, message: '请选择权限' }]}
          >
            <Select
              style={{ width: 300 }}
              showSearch
              optionFilterProp="label"
              options={permissions.map((permission) => ({
                label: `${permission.permissionName}(${permission.permissionCode})`,
                value: permission.id,
              }))}
            />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit">
              绑定权限
            </Button>
          </Form.Item>
        </Form>
      </Card>

      <Card title="角色与权限列表">
        <Table<RoleItem> rowKey="id" columns={columns} dataSource={list} loading={loading} pagination={false} />
      </Card>
    </Space>
  );
}
