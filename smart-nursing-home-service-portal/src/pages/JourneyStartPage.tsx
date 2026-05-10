import { ExclamationCircleFilled } from '@ant-design/icons';
import { useEffect, useMemo, useState } from 'react';
import { Alert, Button, Card, Form, Input, InputNumber, Select, Space, Steps } from 'antd';
import { useNavigate } from 'react-router-dom';
import { listIntakeRecords, startServiceJourney } from '../api/careOrchestrationApi';
import { extractApiErrorMessage } from '../api/client';
import JourneyPageScaffold from '../components/JourneyPageScaffold';
import IntakeRecordsTable from '../components/IntakeRecordsTable';
import { ensureActiveElderId, useUserStore } from '../stores/userStore';
import { ROUTE_PATHS } from '../constants/routes';
import type { StartServiceJourneyRequest } from '../types/care';
import { useIntakeRecords } from '../hooks/useIntakeRecords';

const OCCUPYING_STATUSES = new Set(['PENDING_ASSESSMENT', 'PENDING_HEALTH_ASSESSMENT', 'IN_SERVICE']);

export default function JourneyStartPage() {
  const navigate = useNavigate();
  const [form] = Form.useForm<StartServiceJourneyRequest>();
  const { username: loginUsername, activeElderId, elderBindings } = useUserStore();
  const formElderId = Form.useWatch('elderId', form);

  const [currentStep, setCurrentStep] = useState(0);
  const [loading, setLoading] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [queriedElderId, setQueriedElderId] = useState<number | null>(null);

  const { intakeRecords, intakeLoading, errorMessage, elderIdHint } = useIntakeRecords(loginUsername ?? undefined, queriedElderId);

  const selectedBinding = useMemo(
    () => elderBindings.find((item) => item.elderId === formElderId) ?? null,
    [elderBindings, formElderId],
  );

  const handleElderIdChange = (value: number | null) => {
    if (value !== queriedElderId) {
      setQueriedElderId(null);
    }
  };

  const handleElderIdBlur = () => {
    if (typeof formElderId === 'number' && Number.isFinite(formElderId) && formElderId > 0) {
      setQueriedElderId(formElderId);
      return;
    }
    setQueriedElderId(null);
  };

  useEffect(() => {
    const preferredElderId = form.getFieldValue('elderId') ?? activeElderId ?? undefined;
    if (loginUsername || preferredElderId) {
      form.setFieldsValue({
        applicantName: loginUsername ?? undefined,
        elderId: preferredElderId,
      });
      if (typeof preferredElderId === 'number' && preferredElderId > 0) {
        setQueriedElderId(preferredElderId);
      }
    }
  }, [activeElderId, form, loginUsername]);

  useEffect(() => {
    if (activeElderId != null) return;
    void ensureActiveElderId();
  }, [activeElderId]);

  const hasOccupyingRecord = useMemo(
    () =>
      Boolean(queriedElderId && queriedElderId === formElderId) &&
      intakeRecords.some((record) => Boolean(record.journeyStatus && OCCUPYING_STATUSES.has(record.journeyStatus))),
    [formElderId, intakeRecords, queriedElderId],
  );

  const handleSubmit = async (values: StartServiceJourneyRequest) => {
    setLoading(true);
    setSubmitError(null);

    try {
      if (!loginUsername) {
        setSubmitError('未获取到当前登录用户，请重新登录');
        return;
      }

      const latestRecords = await listIntakeRecords(values.elderId);

      const blocked = latestRecords.some((record) =>
        Boolean(record.journeyStatus && OCCUPYING_STATUSES.has(record.journeyStatus)),
      );
      if (blocked) {
        setSubmitError('该老人存在进行中的受理记录，不可重复发起申请');
        return;
      }

      const result = await startServiceJourney(values);
      sessionStorage.setItem('journeyResult', JSON.stringify(result));
      sessionStorage.setItem(
        'journeyContext',
        JSON.stringify({
          elderId: values.elderId,
          applicationId: result.applicationId,
          agreementId: result.agreementId,
          elderName: selectedBinding?.elderName ?? `老人 ${values.elderId}`,
        }),
      );
      navigate(ROUTE_PATHS.JOURNEY_TASKS, { replace: true });
    } catch (error) {
      const message = extractApiErrorMessage(error, '发起受理登记失败');
      setSubmitError(message);
    } finally {
      setLoading(false);
    }
  };

  const handleNext = async () => {
    try {
      if (currentStep === 0) {
        await form.validateFields(['elderId']);
      } else if (currentStep === 1) {
        await form.validateFields(['applicantName', 'contactPhone']);
      }
      setCurrentStep(currentStep + 1);
    } catch (error) {
      // 验证失败，不进入下一步
    }
  };

  const handlePrev = () => {
    setCurrentStep(currentStep - 1);
  };

  return (
    <JourneyPageScaffold
      title="发起申请"
      description="选择本次申请目标老人，提交后由平台继续完成需求评估、健康评估与签约安排。"
    >
      <Card>
        <Steps
          current={currentStep}
          items={[
            { title: '选择老人' },
            { title: '申请信息' },
            { title: '服务需求' },
          ]}
          style={{ marginBottom: 32 }}
        />

        {(errorMessage || submitError) && (
          <Alert type="error" message={errorMessage || submitError} showIcon style={{ marginBottom: 16 }} />
        )}

        <Form<StartServiceJourneyRequest> form={form} layout="vertical" onFinish={handleSubmit}>
          {currentStep === 0 && (
            <Space direction="vertical" size="large" style={{ width: '100%' }}>
              <Alert
                type="info"
                showIcon
                message={`当前申请人：${loginUsername ?? '-'}；当前查看老人：${elderBindings.find((item) => item.elderId === activeElderId)?.elderName ?? '-'}；本次申请目标老人可单独选择。`}
              />

              {elderBindings.length === 0 && (
                <Alert type="warning" showIcon message='当前账号还没有已绑定老人，请先前往"我的老人"完成绑定。' />
              )}

              <Form.Item
                label="申请目标老人"
                name="elderId"
                rules={[{ required: true, message: '请选择本次申请的目标老人' }]}
                extra={
                  elderIdHint ? (
                    <Space size={6} style={{ color: '#cf1322' }}>
                      <ExclamationCircleFilled style={{ color: '#ff4d4f' }} />
                      <span>{elderIdHint}</span>
                    </Space>
                  ) : undefined
                }
              >
                <Select
                  placeholder="请选择本次申请的目标老人"
                  options={elderBindings.map((binding) => {
                    const isSelf = binding.bindingType === 'SELF';

                    // 关系映射：从用户视角描述"这个老人是我的XXX"
                    const relationMap: Record<string, string> = {
                      'CHILD': '我的父母',
                      'SPOUSE': '我的配偶',
                      'SIBLING': '我的兄弟姐妹',
                      'PARENT': '我的子女',
                      'OTHER': '我的亲属',
                    };

                    let relationText: string;
                    if (isSelf) {
                      relationText = '本人';
                    } else if (binding.relationToElder && relationMap[binding.relationToElder]) {
                      relationText = relationMap[binding.relationToElder];
                    } else if (binding.relationToElder) {
                      // 如果后端返回的是中文或其他值，直接显示
                      relationText = binding.relationToElder;
                    } else {
                      relationText = '家属';
                    }

                    const elderName = binding.elderName || `老人${binding.elderId || ''}`;
                    return {
                      value: binding.elderId,
                      label: `${elderName}（${relationText}，编号：${binding.elderId ?? '-'}）`,
                    };
                  })}
                  onChange={(value) => {
                    handleElderIdChange(value ?? null);
                    if (typeof value === 'number' && Number.isFinite(value) && value > 0) {
                      setQueriedElderId(value);
                      return;
                    }
                    setQueriedElderId(null);
                  }}
                  onBlur={handleElderIdBlur}
                  disabled={elderBindings.length === 0}
                />
              </Form.Item>

              {hasOccupyingRecord && (
                <Alert type="warning" showIcon message="存在进行中的受理记录，当前不可发起新的受理登记。" />
              )}

              <IntakeRecordsTable
                intakeRecords={intakeRecords}
                intakeLoading={intakeLoading}
                loginUsername={loginUsername ?? undefined}
                queriedElderId={queriedElderId}
              />
            </Space>
          )}

          {currentStep === 1 && (
            <Space direction="vertical" size="large" style={{ width: '100%' }}>
              <Form.Item
                label="申请人姓名"
                name="applicantName"
                rules={[{ required: true, message: '请输入申请人姓名' }]}
                initialValue={loginUsername ?? undefined}
              >
                <Input placeholder="请输入申请人姓名" />
              </Form.Item>

              <Form.Item
                label="联系电话"
                name="contactPhone"
                rules={[{ required: true, message: '请输入联系电话' }]}
              >
                <Input placeholder="请输入联系电话" />
              </Form.Item>

              <Form.Item label="监护人编号（可选）" name="guardianId">
                <InputNumber style={{ width: '100%' }} min={1} placeholder="例如：20001" />
              </Form.Item>
            </Space>
          )}

          {currentStep === 2 && (
            <Space direction="vertical" size="large" style={{ width: '100%' }}>
              <Form.Item
                label="服务场景"
                name="serviceScene"
                rules={[{ required: true, message: '请选择服务场景' }]}
              >
                <Select
                  options={[
                    { label: '机构照护', value: 'INSTITUTION' },
                    { label: '居家照护', value: 'HOME' },
                    { label: '社区照护', value: 'COMMUNITY' },
                  ]}
                />
              </Form.Item>

              <Form.Item
                label="服务诉求"
                name="serviceRequest"
                rules={[{ required: true, message: '请输入服务诉求' }]}
              >
                <Input.TextArea rows={4} placeholder="请描述护理服务诉求" />
              </Form.Item>
            </Space>
          )}

          <div style={{ marginTop: 24 }}>
            <Space>
              {currentStep > 0 && (
                <Button onClick={handlePrev}>上一步</Button>
              )}
              {currentStep < 2 && (
                <Button type="primary" onClick={handleNext} disabled={elderBindings.length === 0 || hasOccupyingRecord}>
                  下一步
                </Button>
              )}
              {currentStep === 2 && (
                <Button
                  type="primary"
                  htmlType="submit"
                  loading={loading}
                  disabled={!formElderId || elderBindings.length === 0 || hasOccupyingRecord}
                >
                  提交并发起旅程
                </Button>
              )}
            </Space>
          </div>
        </Form>
      </Card>
    </JourneyPageScaffold>
  );
}
