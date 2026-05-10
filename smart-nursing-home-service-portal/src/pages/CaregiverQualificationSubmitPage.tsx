import { useState } from 'react';
import { Alert, Button, Form, Input, InputNumber, Space, Typography } from 'antd';
import { useNavigate } from 'react-router-dom';
import { submitCaregiverQualificationApplication } from '../api/caregiverQualificationApi';
import { extractApiErrorMessage } from '../api/client';
import DefaultCollapsedSection from '../components/DefaultCollapsedSection';
import { ROUTE_PATHS } from '../constants/routes';
import type { CaregiverQualificationApplication } from '../types/care';

export default function CaregiverQualificationSubmitPage() {
  const navigate = useNavigate();
  const [form] = Form.useForm<CaregiverQualificationApplication>();
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const handleSubmit = async (values: CaregiverQualificationApplication) => {
    setSubmitting(true);
    setErrorMessage(null);
    setSuccessMessage(null);
    try {
      await submitCaregiverQualificationApplication(values);
      setSuccessMessage('护理员资质申请提交成功');
      form.resetFields();
      navigate(ROUTE_PATHS.CAREGIVER_QUALIFICATION_STATUS, { replace: true });
    } catch (error) {
      setErrorMessage(extractApiErrorMessage(error, '提交护理员资质申请失败'));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Typography.Title level={4} style={{ margin: 0 }}>
        护理员资质申请
      </Typography.Title>

      <Alert type="info" showIcon message="请完整填写身份与证书信息，提交后等待医护人员审核。" />
      {errorMessage ? <Alert type="error" showIcon message={errorMessage} /> : null}
      {successMessage ? <Alert type="success" showIcon message={successMessage} /> : null}

      <DefaultCollapsedSection title="资质申请信息填写">
        <Form<CaregiverQualificationApplication> form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item label="姓名" name="realName" rules={[{ required: true, message: '请输入姓名' }]}>
            <Input placeholder="请输入姓名" />
          </Form.Item>
          <Form.Item label="手机号" name="phone" rules={[{ required: true, message: '请输入手机号' }]}>
            <Input placeholder="请输入手机号" />
          </Form.Item>
          <Form.Item label="身份证号" name="idCardNo" rules={[{ required: true, message: '请输入身份证号' }]}>
            <Input placeholder="请输入身份证号" />
          </Form.Item>
          <Form.Item label="证书编号" name="certificateNo" rules={[{ required: true, message: '请输入证书编号' }]}>
            <Input placeholder="请输入证书编号" />
          </Form.Item>
          <Form.Item label="证书类型" name="certificateType" rules={[{ required: true, message: '请输入证书类型' }]}>
            <Input placeholder="例如：护理员证" />
          </Form.Item>
          <Form.Item label="从业年限" name="yearsOfExperience" rules={[{ required: true, message: '请输入从业年限' }]}>
            <InputNumber min={0} style={{ width: '100%' }} placeholder="请输入从业年限" />
          </Form.Item>
          <Form.Item label="技能说明" name="skillSummary" rules={[{ required: true, message: '请输入技能说明' }]}>
            <Input.TextArea rows={5} placeholder="请说明照护经验、技能特长等" />
          </Form.Item>
          <Button type="primary" htmlType="submit" loading={submitting}>
            提交资质申请
          </Button>
        </Form>
      </DefaultCollapsedSection>
    </Space>
  );
}
