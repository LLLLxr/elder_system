import { useState } from 'react';
import { Alert, Button, Card, Form, Input, InputNumber, Space, Typography, message } from 'antd';
import { extractApiErrorMessage } from '../../api/client';
import { createAdminHealthCheckForm } from '../../api/healthApi';
import type { HealthCheckFormCreateRequest } from '../../types/care';

interface HealthCheckFormEntryValues {
  elderId: number;
  agreementId: number;
  elderName: string;
  formCode?: string;
  checkDate?: string;
  responsibleDoctor: string;
  symptomSection?: string;
  vitalSignSection?: string;
  selfEvaluationSection?: string;
  cognitiveEmotionSection?: string;
  lifestyleSection?: string;
  chronicDiseaseSummary?: string;
  allergySummary?: string;
}

export default function HealthCheckFormEntryPage() {
  const [form] = Form.useForm<HealthCheckFormEntryValues>();
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [latestSavedId, setLatestSavedId] = useState<number | null>(null);

  const handleSubmit = async (values: HealthCheckFormEntryValues) => {
    setSubmitting(true);
    setErrorMessage(null);

    try {
      const payload: HealthCheckFormCreateRequest = {
        elderId: values.elderId,
        agreementId: values.agreementId,
        elderName: values.elderName,
        formCode: values.formCode,
        checkDate: values.checkDate,
        responsibleDoctor: values.responsibleDoctor,
        formVersion: 'PAPER_V1',
        symptomSection: values.symptomSection,
        vitalSignSection: values.vitalSignSection,
        selfEvaluationSection: values.selfEvaluationSection,
        cognitiveEmotionSection: values.cognitiveEmotionSection,
        lifestyleSection: values.lifestyleSection,
        chronicDiseaseSummary: values.chronicDiseaseSummary,
        allergySummary: values.allergySummary,
      };

      const result = await createAdminHealthCheckForm(payload);
      setLatestSavedId(result.formId ?? null);
      message.success('健康体检表已保存');
      form.resetFields(['formCode', 'symptomSection', 'vitalSignSection', 'selfEvaluationSection', 'cognitiveEmotionSection', 'lifestyleSection', 'chronicDiseaseSummary', 'allergySummary']);
    } catch (error) {
      setErrorMessage(extractApiErrorMessage(error, '保存健康体检表失败'));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Typography.Title level={4} style={{ margin: 0 }}>
        体检表录入
      </Typography.Title>

      <Alert
        type="info"
        showIcon
        message="本页用于护士、责任医生或管理员录入健康体检表，填写人身份由后端按当前登录账号自动记录。"
      />

      {errorMessage ? <Alert type="error" showIcon message={errorMessage} /> : null}
      {latestSavedId ? <Alert type="success" showIcon message={`最近保存表单ID：${latestSavedId}`} /> : null}

      <Card>
        <Form<HealthCheckFormEntryValues>
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
          initialValues={{ checkDate: new Date().toISOString().slice(0, 10) }}
        >
          <Form.Item label="老人ID" name="elderId" rules={[{ required: true, message: '请输入老人ID' }]}>
            <InputNumber style={{ width: '100%' }} min={1} placeholder="例如：10001" />
          </Form.Item>

          <Form.Item label="协议ID" name="agreementId" rules={[{ required: true, message: '请输入协议ID' }]}>
            <InputNumber style={{ width: '100%' }} min={1} placeholder="例如：30001" />
          </Form.Item>

          <Form.Item label="老人姓名" name="elderName" rules={[{ required: true, message: '请输入老人姓名' }]}>
            <Input placeholder="请输入老人姓名" />
          </Form.Item>

          <Form.Item label="表单编号" name="formCode">
            <Input placeholder="如 HC-2026-0001" />
          </Form.Item>

          <Form.Item label="体检日期" name="checkDate">
            <Input type="date" />
          </Form.Item>

          <Form.Item label="责任医生" name="responsibleDoctor" rules={[{ required: true, message: '请输入责任医生' }]}>
            <Input placeholder="请输入责任医生姓名" />
          </Form.Item>

          <Form.Item label="症状信息" name="symptomSection">
            <Input.TextArea rows={3} placeholder="可录入结构化JSON或文本摘要" />
          </Form.Item>

          <Form.Item label="一般状况" name="vitalSignSection">
            <Input.TextArea rows={3} placeholder="可录入结构化JSON或文本摘要" />
          </Form.Item>

          <Form.Item label="自我评估" name="selfEvaluationSection">
            <Input.TextArea rows={3} placeholder="可录入结构化JSON或文本摘要" />
          </Form.Item>

          <Form.Item label="认知与情感" name="cognitiveEmotionSection">
            <Input.TextArea rows={3} placeholder="可录入结构化JSON或文本摘要" />
          </Form.Item>

          <Form.Item label="生活方式" name="lifestyleSection">
            <Input.TextArea rows={3} placeholder="可录入结构化JSON或文本摘要" />
          </Form.Item>

          <Form.Item label="慢病摘要" name="chronicDiseaseSummary">
            <Input.TextArea rows={2} placeholder="如：高血压、糖尿病" />
          </Form.Item>

          <Form.Item label="过敏摘要" name="allergySummary">
            <Input.TextArea rows={2} placeholder="如：青霉素" />
          </Form.Item>

          <Button type="primary" htmlType="submit" loading={submitting}>
            保存体检表
          </Button>
        </Form>
      </Card>
    </Space>
  );
}
