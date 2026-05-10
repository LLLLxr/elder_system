import { useEffect, useMemo, useState } from 'react';
import { Alert, Button, Checkbox, DatePicker, Space, Table, Tag, Typography, message } from 'antd';
import dayjs, { Dayjs } from 'dayjs';
import type { ColumnsType } from 'antd/es/table';
import { extractApiErrorMessage } from '../api/client';
import { listMyDailyTasks, submitDailyTaskCheckIn } from '../api/careDeliveryApi';
import DefaultCollapsedSection from '../components/DefaultCollapsedSection';
import type { CareTaskItem, DailyCareTask } from '../types/care';

function summarizeCompletion(taskItems?: CareTaskItem[]) {
  const items = taskItems ?? [];
  const completedCount = items.filter((item) => item.completed).length;
  return `${completedCount}/${items.length}`;
}

export default function CaregiverDailyTasksPage() {
  const [loading, setLoading] = useState(false);
  const [submittingPlanId, setSubmittingPlanId] = useState<number | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [taskDate, setTaskDate] = useState<Dayjs>(dayjs());
  const [tasks, setTasks] = useState<DailyCareTask[]>([]);
  const [drafts, setDrafts] = useState<Record<number, CareTaskItem[]>>({});

  const loadTasks = async (dateText: string) => {
    setLoading(true);
    setErrorMessage(null);
    try {
      const data = await listMyDailyTasks(dateText);
      setTasks(data);
      setDrafts(
        Object.fromEntries(
          data.map((task) => [task.servicePlanId, (task.taskItems ?? []).map((item) => ({ ...item }))]),
        ),
      );
    } catch (error) {
      setTasks([]);
      setDrafts({});
      setErrorMessage(extractApiErrorMessage(error, '加载护理任务失败'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadTasks(taskDate.format('YYYY-MM-DD'));
  }, []);

  const columns = useMemo<ColumnsType<DailyCareTask>>(
    () => [
      { title: '老人ID', dataIndex: 'elderId', key: 'elderId', width: 100 },
      { title: '老人姓名', dataIndex: 'elderName', key: 'elderName', width: 140, render: (value?: string) => value || '-' },
      { title: '服务计划ID', dataIndex: 'servicePlanId', key: 'servicePlanId', width: 120 },
      {
        title: '任务项',
        key: 'taskItems',
        render: (_value, record) => {
          const taskItems = drafts[record.servicePlanId] ?? record.taskItems ?? [];
          return (
            <Space direction="vertical" size="small">
              {taskItems.map((item) => (
                <Checkbox
                  key={item.itemCode}
                  checked={Boolean(item.completed)}
                  onChange={(event) => {
                    setDrafts((current) => ({
                      ...current,
                      [record.servicePlanId]: taskItems.map((taskItem) =>
                        taskItem.itemCode === item.itemCode ? { ...taskItem, completed: event.target.checked } : taskItem,
                      ),
                    }));
                  }}
                >
                  {item.itemName}
                </Checkbox>
              ))}
            </Space>
          );
        },
      },
      {
        title: '完成度',
        key: 'completion',
        width: 100,
        render: (_value, record) => summarizeCompletion(drafts[record.servicePlanId] ?? record.taskItems),
      },
      {
        title: '操作',
        key: 'actions',
        width: 140,
        render: (_value, record) => (
          <Button
            type="primary"
            loading={submittingPlanId === record.servicePlanId}
            onClick={async () => {
              setSubmittingPlanId(record.servicePlanId);
              try {
                await submitDailyTaskCheckIn(record.servicePlanId, {
                  elderId: record.elderId,
                  taskDate: taskDate.format('YYYY-MM-DD'),
                  taskItems: drafts[record.servicePlanId] ?? record.taskItems,
                });
                message.success('打卡提交成功');
                await loadTasks(taskDate.format('YYYY-MM-DD'));
              } catch (error) {
                message.error(extractApiErrorMessage(error, '提交打卡失败'));
              } finally {
                setSubmittingPlanId(null);
              }
            }}
          >
            提交打卡
          </Button>
        ),
      },
    ],
    [drafts, submittingPlanId, taskDate],
  );

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Space style={{ width: '100%', justifyContent: 'space-between' }}>
        <Typography.Title level={4} style={{ margin: 0 }}>
          我的护理任务
        </Typography.Title>
        <Space>
          <DatePicker value={taskDate} onChange={(value) => value && setTaskDate(value)} allowClear={false} />
          <Button onClick={() => void loadTasks(taskDate.format('YYYY-MM-DD'))}>刷新</Button>
        </Space>
      </Space>

      <Alert type="info" showIcon message="勾选当日已完成项目后提交打卡，系统会记录护理执行情况。" />
      {errorMessage ? <Alert type="error" showIcon message={errorMessage} /> : null}

      <DefaultCollapsedSection title="护理任务列表">
        <Table<DailyCareTask>
          rowKey={(record) => String(record.servicePlanId)}
          loading={loading}
          columns={columns}
          dataSource={tasks}
          pagination={{ pageSize: 6 }}
          locale={{ emptyText: loading ? '正在加载任务...' : '当天暂无护理任务' }}
        />
      </DefaultCollapsedSection>

      <DefaultCollapsedSection title="任务状态说明">
        <Space wrap>
          <Tag color="processing">未完成</Tag>
          <Tag color="success">已勾选后可提交</Tag>
        </Space>
      </DefaultCollapsedSection>
    </Space>
  );
}
