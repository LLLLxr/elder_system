import { useState } from 'react';
import {
  Alert,
  Button,
  Card,
  Divider,
  Form,
  Input,
  InputNumber,
  Select,
  Space,
  Typography,
} from 'antd';
import {
  assessServiceApplication,
  submitServiceApplication,
} from '../../api/admissionApi';
import { extractApiErrorMessage } from '../../api/client';
import type {
  EligibilityAssessmentRequest,
  ServiceApplication,
} from '../../types/care';

const sceneOptions = [
  { label: '机构照护', value: 'INSTITUTION' },
  { label: '居家照护', value: 'HOME' },
  { label: '社区照护', value: 'COMMUNITY' },
];

export default function AdmissionProcessingPage() {
  const [submitting, setSubmitting] = useState(false);
  const [assessing, setAssessing] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [lastApplication, setLastApplication] = useState<ServiceApplication | null>(null);
  const [lastAssessment, setLastAssessment] = useState<ServiceApplication | null>(null);

  const [applicationForm] = Form.useForm<ServiceApplication>();
  const [assessmentForm] = Form.useForm<EligibilityAssessmentRequest>();

  const handleSubmitApplication = async (values: ServiceApplication) => {
    setSubmitting(true);
    setErrorMessage(null);
    try {
      const result = await submitServiceApplication(values);
      setLastApplication(result);
      assessmentForm.setFieldsValue({ applicationId: result.applicationId });
      applicationForm.resetFields(['serviceRequest']);
    } catch (error) {
      setErrorMessage(extractApiErrorMessage(error, '提交申请失败'));
    } finally {
      setSubmitting(false);
    }
  };

  const handleAssess = async (values: EligibilityAssessmentRequest) => {
    setAssessing(true);
    setErrorMessage(null);
    try {
      const result = await assessServiceApplication(values);
      setLastAssessment(result);
    } catch (error) {
      setErrorMessage(extractApiErrorMessage(error, '受理评估失败'));
    } finally {
      setAssessing(false);
    }
  };

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Typography.Title level={4} style={{ margin: 0 }}>
        申请受理
      </Typography.Title>

      {errorMessage ? <Alert type="error" message={errorMessage} showIcon /> : null}

      <Card title="1) 申请受理登记">
        <Form<ServiceApplication> form={applicationForm} layout="vertical" onFinish={handleSubmitApplication}>
          <Form.Item label="老人ID" name="elderId" rules={[{ required: true, message: '请输入老人ID' }]}>
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item label="监护人ID（可选）" name="guardianId">
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item
            label="申请人姓名"
            name="applicantName"
            rules={[{ required: true, message: '请输入申请人姓名' }]}
          >
            <Input />
          </Form.Item>

          <Form.Item
            label="联系电话"
            name="contactPhone"
            rules={[{ required: true, message: '请输入联系电话' }]}
          >
            <Input />
          </Form.Item>

          <Form.Item label="服务场景" name="serviceScene" rules={[{ required: true, message: '请选择服务场景' }]}>
            <Select options={sceneOptions} />
          </Form.Item>

          <Form.Item
            label="服务诉求"
            name="serviceRequest"
            rules={[{ required: true, message: '请输入服务诉求' }]}
          >
            <Input.TextArea rows={4} />
          </Form.Item>

          <Button type="primary" htmlType="submit" loading={submitting}>
            提交申请
          </Button>
        </Form>
      </Card>

      {lastApplication ? (
        <Alert
          type="success"
          showIcon
          message={`已生成申请单：${lastApplication.applicationId ?? '-'}，当前状态：${lastApplication.status ?? '-'}`}
        />
      ) : null}

      <Divider style={{ margin: 0 }} />

      <Card title="2) 受理初审（需求评估页继续处理）">
        <Form<EligibilityAssessmentRequest> form={assessmentForm} layout="vertical" onFinish={handleAssess}>
          <Form.Item
            label="申请单ID"
            name="applicationId"
            rules={[{ required: true, message: '请输入申请单ID' }]}
          >
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item
            label="受理结论"
            name="eligible"
            rules={[{ required: true, message: '请选择受理结论' }]}
          >
            <Select
              options={[
                { label: '通过（进入下一阶段）', value: true },
                { label: '不通过（终止）', value: false },
              ]}
            />
          </Form.Item>

          <Form.Item
            label="评估结论"
            name="assessmentConclusion"
            rules={[{ required: true, message: '请输入评估结论' }]}
          >
            <Input.TextArea rows={3} />
          </Form.Item>

          <Form.Item label="受理人" name="assessor" rules={[{ required: true, message: '请输入受理人' }]}>
            <Input placeholder="例如：前台接待-张三" />
          </Form.Item>

          <Button type="primary" htmlType="submit" loading={assessing}>
            提交受理评估
          </Button>
        </Form>
      </Card>

      {lastAssessment ? (
        <Alert
          type="success"
          showIcon
          message={`受理评估完成：申请单 ${lastAssessment.applicationId ?? '-'}，状态 ${lastAssessment.status ?? '-'}`}
        />
      ) : null}
    </Space>
  );
}
