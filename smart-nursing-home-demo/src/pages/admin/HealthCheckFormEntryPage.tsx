import { useEffect, useState } from 'react';
import {
  Alert,
  Button,
  Card,
  Checkbox,
  Collapse,
  Col,
  Form,
  Input,
  InputNumber,
  Row,
  Select,
  message,
} from 'antd';
import { listApplicationsByStatus } from '../../api/admissionApi';
import { extractApiErrorMessage } from '../../api/client';
import { createDraftAgreement, getAgreementByApplication } from '../../api/contractApi';
import { createAdminHealthCheckForm } from '../../api/healthApi';
import AdminPageScaffold from '../../components/AdminPageScaffold';
import type { HealthCheckFormCreateRequest, ServiceApplication } from '../../types/care';

type HealthCheckFormEntryValues = {
  applicationId: number;
  elderId: number;
  agreementId: number;
  elderName: string;
  formCode?: string;
  checkDate?: string;
  responsibleDoctor: string;
  symptomSelected?: string[];
  symptomOther?: string;
  temperature?: number;
  pulse?: number;
  respirationRate?: number;
  bloodPressureLeft?: string;
  bloodPressureRight?: string;
  height?: number;
  weight?: number;
  waist?: number;
  bmi?: number;
  healthSelfEvaluation?: string;
  adlSelfEvaluation?: string;
  cognitionScreening?: string;
  mmseScoreLevel?: string;
  emotionScreening?: string;
  depressionScoreLevel?: string;
  exerciseFrequency?: string;
  exerciseDurationLevel?: string;
  exerciseYearsLevel?: string;
  exerciseTypes?: string[];
  dietHabits?: string[];
  smokingStatus?: string;
  cigarettesPerDayLevel?: string;
  smokingStartAgeRange?: string;
  smokingQuitAgeRange?: string;
  drinkingFrequency?: string;
  alcoholPerDayLevel?: string;
  quittingAlcohol?: string;
  alcoholQuitAgeRange?: string;
  nursingConclusionSummary?: string;
  nursingRecommendation?: string;
  followUpPlan?: string;
  chronicDiseaseSummary?: string;
  allergySummary?: string;
  riskLevel?: string;
  score?: number;
  conclusion?: string;
};

const symptomNoneValue = '无症状';
const symptomOtherValue = '其他';
const symptomOptions = [
  symptomNoneValue,
  '头痛',
  '头晕',
  '心悸',
  '胸闷',
  '胸痛',
  '慢性咳嗽',
  '咳痰',
  '呼吸困难',
  '多饮',
  '多尿',
  '体重下降',
  '乏力',
  '关节肿痛',
  '视力模糊',
  '手脚麻木',
  '尿急',
  '尿痛',
  '便秘',
  '腹泻',
  '恶心呕吐',
  '眼花',
  '耳鸣',
  '乳房胀痛',
  symptomOtherValue,
];
const exerciseTypeOptions = ['散步', '慢跑', '太极', '广场舞', '器械训练', '康复训练'];
const dietHabitOptions = ['荤素均衡', '荤食为主', '素食为主', '嗜盐', '嗜油', '嗜糖'];
const healthSelfEvaluationOptions = [
  { label: '满意', value: '满意' },
  { label: '基本满意', value: '基本满意' },
  { label: '说不清楚', value: '说不清楚' },
  { label: '不太满意', value: '不太满意' },
  { label: '不满意', value: '不满意' },
];
const adlSelfEvaluationOptions = [
  { label: '可自理（0~3分）', value: '可自理（0~3分）' },
  { label: '轻度依赖（4~8分）', value: '轻度依赖（4~8分）' },
  { label: '中度依赖（9~18分）', value: '中度依赖（9~18分）' },
  { label: '不能自理（≥19分）', value: '不能自理（≥19分）' },
];
const cognitionScreeningOptions = [
  { label: '粗筛阴性', value: '粗筛阴性' },
  { label: '粗筛阳性', value: '粗筛阳性' },
  { label: '粗筛阳性，简易智力状态检查', value: '粗筛阳性，简易智力状态检查' },
];
const emotionScreeningOptions = [
  { label: '粗筛阴性', value: '粗筛阴性' },
  { label: '粗筛阳性', value: '粗筛阳性' },
  { label: '粗筛阳性，老年人抑郁评分检查', value: '粗筛阳性，老年人抑郁评分检查' },
];
const exerciseFrequencyOptions = [
  { label: '每天', value: '每天' },
  { label: '每周一次以上', value: '每周一次以上' },
  { label: '偶尔', value: '偶尔' },
  { label: '不锻炼', value: '不锻炼' },
];
const smokingStatusOptions = [
  { label: '从不吸烟', value: '从不吸烟' },
  { label: '已戒烟', value: '已戒烟' },
  { label: '吸烟', value: '吸烟' },
];
const drinkingFrequencyOptions = [
  { label: '从不', value: '从不' },
  { label: '偶尔', value: '偶尔' },
  { label: '经常', value: '经常' },
  { label: '每天', value: '每天' },
];
const quittingAlcoholOptions = [
  { label: '未戒酒', value: '未戒酒' },
  { label: '已戒酒', value: '已戒酒' },
];
const riskLevelOptions = [
  { label: '低风险', value: 'LOW' },
  { label: '中风险', value: 'MEDIUM' },
  { label: '高风险', value: 'HIGH' },
];

function compactObject<T extends Record<string, unknown>>(value: T) {
  return Object.fromEntries(
    Object.entries(value).filter(([, fieldValue]) => {
      if (fieldValue == null) {
        return false;
      }
      if (Array.isArray(fieldValue)) {
        return fieldValue.length > 0;
      }
      if (typeof fieldValue === 'string') {
        return fieldValue.trim().length > 0;
      }
      return true;
    }),
  );
}

function formatLocalDateInput(date = new Date()) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

export default function HealthCheckFormEntryPage() {
  const [form] = Form.useForm<HealthCheckFormEntryValues>();
  const [submitting, setSubmitting] = useState(false);
  const [loadingApplications, setLoadingApplications] = useState(false);
  const [loadingAgreement, setLoadingAgreement] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [latestSavedId, setLatestSavedId] = useState<number | null>(null);
  const [passedApplications, setPassedApplications] = useState<ServiceApplication[]>([]);

  const selectedApplicationId = Form.useWatch('applicationId', form);
  const symptomSelected = Form.useWatch('symptomSelected', form) ?? [];
  const symptomHasOther = symptomSelected.includes(symptomOtherValue);

  const handleSymptomChange = (checkedValues: Array<string | number>) => {
    const normalizedValues = checkedValues.filter((value): value is string => typeof value === 'string');
    const hasNone = normalizedValues.includes(symptomNoneValue);
    const previousHasNone = symptomSelected.includes(symptomNoneValue);

    let nextValues = normalizedValues;
    if (hasNone) {
      nextValues = previousHasNone ? normalizedValues.filter((item) => item !== symptomNoneValue) : [symptomNoneValue];
    } else {
      nextValues = normalizedValues.filter((item) => item !== symptomNoneValue);
    }

    form.setFieldValue('symptomSelected', nextValues);
    if (!nextValues.includes(symptomOtherValue)) {
      form.setFieldValue('symptomOther', undefined);
    }
  };

  useEffect(() => {
    const loadPassedApplications = async () => {
      setLoadingApplications(true);
      try {
        const list = await listApplicationsByStatus('PASSED');
        setPassedApplications(list);
      } catch (error) {
        setErrorMessage(extractApiErrorMessage(error, '加载已通过申请失败'));
      } finally {
        setLoadingApplications(false);
      }
    };

    void loadPassedApplications();
  }, []);

  useEffect(() => {
    const application = passedApplications.find((item) => item.applicationId === selectedApplicationId);
    if (!selectedApplicationId || !application) {
      form.setFieldsValue({ elderId: undefined, agreementId: undefined, elderName: undefined });
      return;
    }

    form.setFieldsValue({ elderId: application.elderId, elderName: undefined });

    const loadAgreement = async () => {
      setLoadingAgreement(true);
      setErrorMessage(null);
      try {
        const agreement = await getAgreementByApplication(selectedApplicationId);
        form.setFieldsValue({ agreementId: agreement.agreementId });
      } catch (_error) {
        try {
          const draftAgreement = await createDraftAgreement({
            applicationId: selectedApplicationId,
            elderId: application.elderId,
            serviceScene: application.serviceScene,
          });
          form.setFieldsValue({ agreementId: draftAgreement.agreementId });
          message.success('当前申请缺少服务协议，已自动补建草稿协议');
        } catch (fallbackError) {
          form.setFieldsValue({ agreementId: undefined });
          setErrorMessage(extractApiErrorMessage(fallbackError, '加载或补建协议失败'));
        }
      } finally {
        setLoadingAgreement(false);
      }
    };

    void loadAgreement();
  }, [selectedApplicationId, passedApplications, form]);

  const handleSubmit = async (values: HealthCheckFormEntryValues) => {
    setSubmitting(true);
    setErrorMessage(null);

    try {
      const symptomSection = JSON.stringify(
        compactObject({
          selected: values.symptomSelected,
          other: values.symptomSelected?.includes(symptomOtherValue) ? values.symptomOther : undefined,
        }),
      );

      const vitalSignSection = JSON.stringify(
        compactObject({
          temperature: values.temperature,
          pulse: values.pulse,
          respirationRate: values.respirationRate,
          bloodPressureLeft: values.bloodPressureLeft,
          bloodPressureRight: values.bloodPressureRight,
          height: values.height,
          weight: values.weight,
          waist: values.waist,
          bmi: values.bmi,
        }),
      );

      const selfEvaluationSection = JSON.stringify(
        compactObject({
          healthSelfEvaluation: values.healthSelfEvaluation,
          adlSelfEvaluation: values.adlSelfEvaluation,
        }),
      );

      const cognitiveEmotionSection = JSON.stringify(
        compactObject({
          cognitionScreening: values.cognitionScreening,
          mmseScoreLevel: values.mmseScoreLevel,
          emotionScreening: values.emotionScreening,
          depressionScoreLevel: values.depressionScoreLevel,
        }),
      );

      const lifestyleSection = JSON.stringify(
        compactObject({
          exerciseFrequency: values.exerciseFrequency,
          exerciseDurationLevel: values.exerciseDurationLevel,
          exerciseYearsLevel: values.exerciseYearsLevel,
          exerciseTypes: values.exerciseTypes,
          dietHabits: values.dietHabits,
          smokingStatus: values.smokingStatus,
          cigarettesPerDayLevel: values.cigarettesPerDayLevel,
          smokingStartAgeRange: values.smokingStartAgeRange,
          smokingQuitAgeRange: values.smokingQuitAgeRange,
          drinkingFrequency: values.drinkingFrequency,
          alcoholPerDayLevel: values.alcoholPerDayLevel,
          quittingAlcohol: values.quittingAlcohol,
          alcoholQuitAgeRange: values.alcoholQuitAgeRange,
        }),
      );

      const nursingConclusionSection = JSON.stringify(
        compactObject({
          summary: values.nursingConclusionSummary,
          recommendation: values.nursingRecommendation,
          followUpPlan: values.followUpPlan,
        }),
      );

      const payload: HealthCheckFormCreateRequest = {
        elderId: values.elderId,
        agreementId: values.agreementId,
        elderName: values.elderName,
        formCode: values.formCode,
        checkDate: values.checkDate,
        responsibleDoctor: values.responsibleDoctor,
        formVersion: 'PAPER_V1',
        symptomSection: symptomSection === '{}' ? undefined : symptomSection,
        vitalSignSection: vitalSignSection === '{}' ? undefined : vitalSignSection,
        selfEvaluationSection: selfEvaluationSection === '{}' ? undefined : selfEvaluationSection,
        cognitiveEmotionSection: cognitiveEmotionSection === '{}' ? undefined : cognitiveEmotionSection,
        lifestyleSection: lifestyleSection === '{}' ? undefined : lifestyleSection,
        nursingConclusionSection: nursingConclusionSection === '{}' ? undefined : nursingConclusionSection,
        chronicDiseaseSummary: values.chronicDiseaseSummary,
        allergySummary: values.allergySummary,
        riskLevel: values.riskLevel,
        score: values.score,
        conclusion: values.conclusion,
      };

      const result = await createAdminHealthCheckForm(payload);
      setLatestSavedId(result.formId ?? null);
      message.success('健康体检表已保存');
      const currentApplication = passedApplications.find((item) => item.applicationId === values.applicationId);
      form.resetFields([
        'formCode',
        'symptomSelected',
        'symptomOther',
        'temperature',
        'pulse',
        'respirationRate',
        'bloodPressureLeft',
        'bloodPressureRight',
        'height',
        'weight',
        'waist',
        'bmi',
        'healthSelfEvaluation',
        'adlSelfEvaluation',
        'cognitionScreening',
        'mmseScoreLevel',
        'emotionScreening',
        'depressionScoreLevel',
        'exerciseFrequency',
        'exerciseDurationLevel',
        'exerciseYearsLevel',
        'exerciseTypes',
        'dietHabits',
        'smokingStatus',
        'cigarettesPerDayLevel',
        'smokingStartAgeRange',
        'smokingQuitAgeRange',
        'drinkingFrequency',
        'alcoholPerDayLevel',
        'quittingAlcohol',
        'alcoholQuitAgeRange',
        'nursingConclusionSummary',
        'nursingRecommendation',
        'followUpPlan',
        'chronicDiseaseSummary',
        'allergySummary',
        'riskLevel',
        'score',
        'conclusion',
      ]);
      form.setFieldsValue({
        applicationId: values.applicationId,
        elderId: currentApplication?.elderId,
        agreementId: values.agreementId,
      });
    } catch (error) {
      setErrorMessage(extractApiErrorMessage(error, '保存健康体检表失败'));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AdminPageScaffold
      title="体检表录入"
      description="填写结构化健康体检表，内容将用于后续健康评估与签约判断。"
    >
      <Alert
        type="info"
        showIcon
        message="请先选择一条已通过需求评估的服务申请，系统会自动带出老人编号和协议编号，避免录入到错误流程。"
      />

      {errorMessage ? <Alert type="error" showIcon message={errorMessage} /> : null}
      {latestSavedId ? <Alert type="success" showIcon message={`最近保存表单编号：${latestSavedId}`} /> : null}

      <Collapse
        items={[
          {
            key: 'health-check-form',
            label: '健康体检表',
            children: (
              <Form<HealthCheckFormEntryValues>
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
          initialValues={{ checkDate: formatLocalDateInput() }}
        >
          <Card size="small" title="基本信息" style={{ marginBottom: 16 }}>
            <Row gutter={16}>
              <Col span={12}>
                <Form.Item
                  label="服务申请"
                  name="applicationId"
                  rules={[{ required: true, message: '请选择已通过需求评估的服务申请' }]}
                >
                  <Select
                    showSearch
                    loading={loadingApplications}
                    placeholder="请选择已通过需求评估的服务申请"
                    optionFilterProp="label"
                    options={passedApplications.map((item) => ({
                      value: item.applicationId,
                      label: `申请#${item.applicationId} / 老人#${item.elderId} / ${item.applicantName}`,
                    }))}
                  />
                </Form.Item>
              </Col>
              <Col span={6}>
                <Form.Item label="老人ID" name="elderId" rules={[{ required: true, message: '请选择服务申请' }]}>
                  <InputNumber style={{ width: '100%' }} min={1} disabled placeholder="选择申请后自动带出" />
                </Form.Item>
              </Col>
              <Col span={6}>
                <Form.Item label="协议ID" name="agreementId" rules={[{ required: true, message: '请选择已生成协议的申请' }]}>
                  <InputNumber
                    style={{ width: '100%' }}
                    min={1}
                    disabled
                    placeholder={loadingAgreement ? '正在加载协议' : '选择申请后自动带出'}
                  />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item label="老人姓名" name="elderName" rules={[{ required: true, message: '请输入老人姓名' }]}>
                  <Input placeholder="请输入老人姓名" />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item label="表单编号" name="formCode">
                  <Input placeholder="如 HC-2026-0001" />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item label="体检日期" name="checkDate">
                  <Input type="date" />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item label="责任医生" name="responsibleDoctor" rules={[{ required: true, message: '请输入责任医生' }]}>
                  <Input placeholder="请输入责任医生姓名" />
                </Form.Item>
              </Col>
            </Row>
          </Card>

          <Card size="small" title="症状" style={{ marginBottom: 16 }}>
            <Form.Item label="症状勾选" name="symptomSelected">
              <Checkbox.Group options={symptomOptions} onChange={handleSymptomChange} />
            </Form.Item>
            <Form.Item label="其他症状补充" name="symptomOther">
              <Input disabled={!symptomHasOther} placeholder="仅在勾选“其他”时填写" />
            </Form.Item>
          </Card>

          <Card size="small" title="一般状况" style={{ marginBottom: 16 }}>
            <Row gutter={16}>
              <Col span={8}>
                <Form.Item label="体温(℃)" name="temperature">
                  <InputNumber style={{ width: '100%' }} min={30} max={45} step={0.1} />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item label="脉率(次/分)" name="pulse">
                  <InputNumber style={{ width: '100%' }} min={20} max={240} />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item label="呼吸频率(次/分)" name="respirationRate">
                  <InputNumber style={{ width: '100%' }} min={5} max={80} />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item label="左侧血压(mmHg)" name="bloodPressureLeft">
                  <Input placeholder="如 120/80" />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item label="右侧血压(mmHg)" name="bloodPressureRight">
                  <Input placeholder="如 118/78" />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item label="身高(cm)" name="height">
                  <InputNumber style={{ width: '100%' }} min={50} max={250} step={0.1} />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item label="体重(kg)" name="weight">
                  <InputNumber style={{ width: '100%' }} min={10} max={300} step={0.1} />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item label="腰围(cm)" name="waist">
                  <InputNumber style={{ width: '100%' }} min={20} max={200} step={0.1} />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item label="BMI" name="bmi">
                  <InputNumber style={{ width: '100%' }} min={5} max={80} step={0.1} />
                </Form.Item>
              </Col>
            </Row>
          </Card>

          <Card size="small" title="自我评估" style={{ marginBottom: 16 }}>
            <Row gutter={16}>
              <Col span={12}>
                <Form.Item label="健康状态自评" name="healthSelfEvaluation">
                  <Select allowClear options={healthSelfEvaluationOptions} placeholder="请选择健康状态自评" />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item label="生活自理能力自评" name="adlSelfEvaluation">
                  <Select allowClear options={adlSelfEvaluationOptions} placeholder="请选择生活自理能力" />
                </Form.Item>
              </Col>
            </Row>
          </Card>

          <Card size="small" title="认知/情感" style={{ marginBottom: 16 }}>
            <Row gutter={16}>
              <Col span={12}>
                <Form.Item label="认知粗筛" name="cognitionScreening">
                  <Select allowClear options={cognitionScreeningOptions} placeholder="请选择认知粗筛结果" />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item label="MMSE分级" name="mmseScoreLevel">
                  <Select
                    allowClear
                    options={[
                      { label: 'MMSE 24-30（正常）', value: '24-30' },
                      { label: 'MMSE 18-23（轻度异常）', value: '18-23' },
                      { label: 'MMSE 0-17（明显异常）', value: '0-17' },
                    ]}
                  />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item label="情感粗筛" name="emotionScreening">
                  <Select allowClear options={emotionScreeningOptions} placeholder="请选择情感粗筛结果" />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item label="抑郁分级" name="depressionScoreLevel">
                  <Select
                    allowClear
                    options={[
                      { label: '抑郁评分 0-4（正常）', value: '0-4' },
                      { label: '抑郁评分 5-9（轻度）', value: '5-9' },
                      { label: '抑郁评分 10+（中重度）', value: '10+' },
                    ]}
                  />
                </Form.Item>
              </Col>
            </Row>
          </Card>

          <Card size="small" title="生活方式" style={{ marginBottom: 16 }}>
            <Row gutter={16}>
              <Col span={12}>
                <Form.Item label="锻炼频率" name="exerciseFrequency">
                  <Select allowClear options={exerciseFrequencyOptions} placeholder="请选择锻炼频率" />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item label="每次锻炼时间" name="exerciseDurationLevel">
                  <Select
                    allowClear
                    options={[
                      { label: '<15 分钟', value: '<15' },
                      { label: '15-30 分钟', value: '15-30' },
                      { label: '30-60 分钟', value: '30-60' },
                      { label: '>60 分钟', value: '>60' },
                    ]}
                  />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item label="坚持锻炼时间" name="exerciseYearsLevel">
                  <Select
                    allowClear
                    options={[
                      { label: '<1 年', value: '<1y' },
                      { label: '1-3 年', value: '1-3y' },
                      { label: '3-5 年', value: '3-5y' },
                      { label: '>5 年', value: '>5y' },
                    ]}
                  />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item label="锻炼方式" name="exerciseTypes">
                  <Checkbox.Group options={exerciseTypeOptions} />
                </Form.Item>
              </Col>
              <Col span={24}>
                <Form.Item label="饮食习惯" name="dietHabits">
                  <Checkbox.Group options={dietHabitOptions} />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item label="吸烟状态" name="smokingStatus">
                  <Select allowClear options={smokingStatusOptions} placeholder="请选择吸烟状态" />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item label="日吸烟量" name="cigarettesPerDayLevel">
                  <Select
                    allowClear
                    options={[
                      { label: '0', value: '0' },
                      { label: '1-5支', value: '1-5' },
                      { label: '6-10支', value: '6-10' },
                      { label: '10支以上', value: '10+' },
                    ]}
                  />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item label="开始吸烟年龄" name="smokingStartAgeRange">
                  <Select
                    allowClear
                    options={[
                      { label: '<20岁', value: '<20' },
                      { label: '20-39岁', value: '20-39' },
                      { label: '40-59岁', value: '40-59' },
                      { label: '60岁以上', value: '60+' },
                    ]}
                  />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item label="戒烟年龄" name="smokingQuitAgeRange">
                  <Select
                    allowClear
                    options={[
                      { label: '未戒烟', value: '未戒烟' },
                      { label: '<40岁', value: '<40' },
                      { label: '40-59岁', value: '40-59' },
                      { label: '60岁以上', value: '60+' },
                    ]}
                  />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item label="饮酒频率" name="drinkingFrequency">
                  <Select allowClear options={drinkingFrequencyOptions} placeholder="请选择饮酒频率" />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item label="日饮酒量" name="alcoholPerDayLevel">
                  <Select
                    allowClear
                    options={[
                      { label: '0两', value: '0' },
                      { label: '<1两', value: '<1' },
                      { label: '1-2两', value: '1-2' },
                      { label: '>2两', value: '>2' },
                    ]}
                  />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item label="戒酒情况" name="quittingAlcohol">
                  <Select allowClear options={quittingAlcoholOptions} placeholder="请选择戒酒情况" />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item label="戒酒年龄" name="alcoholQuitAgeRange">
                  <Select
                    allowClear
                    options={[
                      { label: '未戒酒', value: '未戒酒' },
                      { label: '<40岁', value: '<40' },
                      { label: '40-59岁', value: '40-59' },
                      { label: '60岁以上', value: '60+' },
                    ]}
                  />
                </Form.Item>
              </Col>
            </Row>
          </Card>

          <Card size="small" title="护理结论" style={{ marginBottom: 16 }}>
            <Form.Item label="护理结论摘要" name="nursingConclusionSummary">
              <Select
                allowClear
                options={[
                  { label: '生命体征基本平稳', value: '生命体征基本平稳' },
                  { label: '存在慢病管理需求', value: '存在慢病管理需求' },
                  { label: '存在跌倒风险', value: '存在跌倒风险' },
                  { label: '存在营养风险', value: '存在营养风险' },
                  { label: '需进一步专科评估', value: '需进一步专科评估' },
                ]}
                placeholder="请选择护理结论摘要"
              />
            </Form.Item>
            <Form.Item label="护理建议" name="nursingRecommendation">
              <Input.TextArea rows={3} placeholder="如：加强夜间巡视、持续监测血压、规律服药" />
            </Form.Item>
            <Form.Item label="随访计划" name="followUpPlan">
              <Input.TextArea rows={3} placeholder="如：2周后复测血压血糖，1个月后复评" />
            </Form.Item>
          </Card>

          <Card size="small" title="摘要信息" style={{ marginBottom: 16 }}>
            <Row gutter={16}>
              <Col span={12}>
                <Form.Item label="慢病摘要" name="chronicDiseaseSummary">
                  <Input.TextArea rows={3} placeholder="如：高血压、糖尿病、冠心病" />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item label="过敏摘要" name="allergySummary">
                  <Input.TextArea rows={3} placeholder="如：青霉素、海鲜" />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item label="风险等级" name="riskLevel">
                  <Select allowClear options={riskLevelOptions} placeholder="请选择风险等级" />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item label="评分" name="score">
                  <InputNumber style={{ width: '100%' }} min={0} max={100} />
                </Form.Item>
              </Col>
              <Col span={24}>
                <Form.Item label="综合结论" name="conclusion">
                  <Input.TextArea rows={3} placeholder="如：建议签约后纳入慢病重点随访与跌倒风险干预" />
                </Form.Item>
              </Col>
            </Row>
          </Card>

          <Button type="primary" htmlType="submit" loading={submitting}>
            保存体检表
          </Button>
              </Form>
            ),
          },
        ]}
      />
    </AdminPageScaffold>
  );
}
