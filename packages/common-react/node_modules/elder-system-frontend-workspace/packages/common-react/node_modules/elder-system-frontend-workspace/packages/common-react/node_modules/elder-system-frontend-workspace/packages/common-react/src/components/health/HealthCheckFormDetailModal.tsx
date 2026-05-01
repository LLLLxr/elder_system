import { Alert, Card, Descriptions, Modal, Space } from 'antd';

export interface HealthCheckFormDetail {
  formId?: number;
  elderId: number;
  agreementId: number;
  elderName: string;
  formCode?: string;
  checkDate?: string;
  formVersion?: string;
  symptomSection?: string;
  vitalSignSection?: string;
  selfEvaluationSection?: string;
  cognitiveEmotionSection?: string;
  lifestyleSection?: string;
  chronicDiseaseSummary?: string;
  allergySummary?: string;
}

export interface HealthCheckFormDetailModalProps {
  detail: HealthCheckFormDetail | null;
  error: string | null;
  loading: boolean;
  onCancel: () => void;
  open: boolean;
  title?: string;
}

type ParsedSection = {
  data: Record<string, unknown> | null;
  raw?: string;
  parseError: boolean;
};

const mmseScoreLevelLabels: Record<string, string> = {
  '24-30': 'MMSE 24-30（正常）',
  '18-23': 'MMSE 18-23（轻度异常）',
  '0-17': 'MMSE 0-17（明显异常）',
};

const depressionScoreLevelLabels: Record<string, string> = {
  '0-4': '抑郁评分 0-4（正常）',
  '5-9': '抑郁评分 5-9（轻度）',
  '10+': '抑郁评分 10+（中重度）',
};

const exerciseDurationLevelLabels: Record<string, string> = {
  '<15': '<15 分钟',
  '15-30': '15-30 分钟',
  '30-60': '30-60 分钟',
  '>60': '>60 分钟',
};

const exerciseYearsLevelLabels: Record<string, string> = {
  '<1y': '<1 年',
  '1-3y': '1-3 年',
  '3-5y': '3-5 年',
  '>5y': '>5 年',
};

const cigarettesPerDayLevelLabels: Record<string, string> = {
  '0': '0',
  '1-5': '1-5支',
  '6-10': '6-10支',
  '10+': '10支以上',
};

const smokingStartAgeRangeLabels: Record<string, string> = {
  '<20': '<20岁',
  '20-39': '20-39岁',
  '40-59': '40-59岁',
  '60+': '60岁以上',
};

const smokingQuitAgeRangeLabels: Record<string, string> = {
  '未戒烟': '未戒烟',
  '<40': '<40岁',
  '40-59': '40-59岁',
  '60+': '60岁以上',
};

const alcoholPerDayLevelLabels: Record<string, string> = {
  '0': '0两',
  '<1': '<1两',
  '1-2': '1-2两',
  '>2': '>2两',
};

const alcoholQuitAgeRangeLabels: Record<string, string> = {
  '未戒酒': '未戒酒',
  '<40': '<40岁',
  '40-59': '40-59岁',
  '60+': '60岁以上',
};

function asObject(value: unknown): Record<string, unknown> | null {
  if (value && typeof value === 'object' && !Array.isArray(value)) {
    return value as Record<string, unknown>;
  }
  return null;
}

function parseSection(raw?: string): ParsedSection {
  if (!raw) {
    return { data: null, raw, parseError: false };
  }

  try {
    const parsed = JSON.parse(raw) as unknown;
    const object = asObject(parsed);
    if (!object) {
      return { data: null, raw, parseError: true };
    }
    return { data: object, raw, parseError: false };
  } catch {
    return { data: null, raw, parseError: true };
  }
}

function formatArray(value: unknown): string {
  if (!Array.isArray(value)) {
    return '-';
  }

  const list = value
    .map((item) => String(item ?? '').trim())
    .filter((item) => item.length > 0);

  return list.length > 0 ? list.join('、') : '-';
}

function formatValue(value: unknown): string {
  if (value == null) {
    return '-';
  }

  const text = String(value).trim();
  return text.length > 0 ? text : '-';
}

function formatByMap(value: unknown, dict: Record<string, string>): string {
  if (value == null) {
    return '-';
  }

  const key = String(value).trim();
  if (!key) {
    return '-';
  }
  return dict[key] ?? key;
}

function SectionFallback({ label, section }: { label: string; section: ParsedSection }) {
  if (!section.parseError) {
    return null;
  }

  return (
    <Alert
      type="warning"
      showIcon
      message={`${label}解析失败，展示原始内容`}
      description={section.raw || '-'}
    />
  );
}

export default function HealthCheckFormDetailModal({
  detail,
  error,
  loading,
  onCancel,
  open,
  title,
}: HealthCheckFormDetailModalProps) {
  const symptomSection = parseSection(detail?.symptomSection);
  const vitalSignSection = parseSection(detail?.vitalSignSection);
  const selfEvaluationSection = parseSection(detail?.selfEvaluationSection);
  const cognitiveEmotionSection = parseSection(detail?.cognitiveEmotionSection);
  const lifestyleSection = parseSection(detail?.lifestyleSection);

  const symptom = symptomSection.data ?? {};
  const vital = vitalSignSection.data ?? {};
  const selfEvaluation = selfEvaluationSection.data ?? {};
  const cognitiveEmotion = cognitiveEmotionSection.data ?? {};
  const lifestyle = lifestyleSection.data ?? {};

  return (
    <Modal
      title={title ?? `体检表详情（表单ID：${detail?.formId ?? '-'}）`}
      open={open}
      onCancel={onCancel}
      footer={null}
      width={900}
    >
      {loading ? <Alert type="info" showIcon message="正在加载体检表详情..." /> : null}
      {!loading && error ? <Alert type="error" showIcon message={error} /> : null}

      {!loading && !error && detail ? (
        <Space direction="vertical" size="middle" style={{ width: '100%' }}>
          <Descriptions bordered column={2} size="small">
            <Descriptions.Item label="表单ID">{detail.formId ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="老人ID">{detail.elderId ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="协议ID">{detail.agreementId ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="姓名">{detail.elderName ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="编号">{detail.formCode ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="体检日期">{detail.checkDate ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="表单版本" span={2}>
              {detail.formVersion ?? '-'}
            </Descriptions.Item>
          </Descriptions>

          <Card size="small" title="症状">
            <SectionFallback label="症状区" section={symptomSection} />
            <Descriptions bordered column={1} size="small">
              <Descriptions.Item label="症状勾选">{formatArray(symptom.selected)}</Descriptions.Item>
            </Descriptions>
          </Card>

          <Card size="small" title="一般状况">
            <SectionFallback label="一般状况区" section={vitalSignSection} />
            <Descriptions bordered column={2} size="small">
              <Descriptions.Item label="体温(℃)">{formatValue(vital.temperature)}</Descriptions.Item>
              <Descriptions.Item label="脉率(次/分)">{formatValue(vital.pulse)}</Descriptions.Item>
              <Descriptions.Item label="呼吸频率(次/分)">{formatValue(vital.respirationRate)}</Descriptions.Item>
              <Descriptions.Item label="左侧血压(mmHg)">{formatValue(vital.bloodPressureLeft)}</Descriptions.Item>
              <Descriptions.Item label="右侧血压(mmHg)">{formatValue(vital.bloodPressureRight)}</Descriptions.Item>
              <Descriptions.Item label="身高">{formatValue(vital.height)}</Descriptions.Item>
              <Descriptions.Item label="体重">{formatValue(vital.weight)}</Descriptions.Item>
              <Descriptions.Item label="腰围">{formatValue(vital.waist)}</Descriptions.Item>
              <Descriptions.Item label="BMI" span={2}>
                {formatValue(vital.bmi)}
              </Descriptions.Item>
            </Descriptions>
          </Card>

          <Card size="small" title="自我评估">
            <SectionFallback label="自我评估区" section={selfEvaluationSection} />
            <Descriptions bordered column={1} size="small">
              <Descriptions.Item label="健康状态自评">{formatValue(selfEvaluation.healthSelfEvaluation)}</Descriptions.Item>
              <Descriptions.Item label="生活自理能力自评">{formatValue(selfEvaluation.adlSelfEvaluation)}</Descriptions.Item>
            </Descriptions>
          </Card>

          <Card size="small" title="认知/情感">
            <SectionFallback label="认知/情感区" section={cognitiveEmotionSection} />
            <Descriptions bordered column={2} size="small">
              <Descriptions.Item label="认知粗筛">{formatValue(cognitiveEmotion.cognitionScreening)}</Descriptions.Item>
              <Descriptions.Item label="MMSE分级">
                {formatByMap(cognitiveEmotion.mmseScoreLevel, mmseScoreLevelLabels)}
              </Descriptions.Item>
              <Descriptions.Item label="情感粗筛">{formatValue(cognitiveEmotion.emotionScreening)}</Descriptions.Item>
              <Descriptions.Item label="抑郁分级">
                {formatByMap(cognitiveEmotion.depressionScoreLevel, depressionScoreLevelLabels)}
              </Descriptions.Item>
            </Descriptions>
          </Card>

          <Card size="small" title="生活方式">
            <SectionFallback label="生活方式区" section={lifestyleSection} />
            <Descriptions bordered column={2} size="small">
              <Descriptions.Item label="锻炼频率">{formatValue(lifestyle.exerciseFrequency)}</Descriptions.Item>
              <Descriptions.Item label="每次锻炼时间">
                {formatByMap(lifestyle.exerciseDurationLevel, exerciseDurationLevelLabels)}
              </Descriptions.Item>
              <Descriptions.Item label="坚持锻炼时间">
                {formatByMap(lifestyle.exerciseYearsLevel, exerciseYearsLevelLabels)}
              </Descriptions.Item>
              <Descriptions.Item label="锻炼方式">{formatArray(lifestyle.exerciseTypes)}</Descriptions.Item>
              <Descriptions.Item label="饮食习惯" span={2}>
                {formatArray(lifestyle.dietHabits)}
              </Descriptions.Item>
              <Descriptions.Item label="吸烟状态">{formatValue(lifestyle.smokingStatus)}</Descriptions.Item>
              <Descriptions.Item label="日吸烟量">
                {formatByMap(lifestyle.cigarettesPerDayLevel, cigarettesPerDayLevelLabels)}
              </Descriptions.Item>
              <Descriptions.Item label="开始吸烟年龄">
                {formatByMap(lifestyle.smokingStartAgeRange, smokingStartAgeRangeLabels)}
              </Descriptions.Item>
              <Descriptions.Item label="戒烟年龄">
                {formatByMap(lifestyle.smokingQuitAgeRange, smokingQuitAgeRangeLabels)}
              </Descriptions.Item>
              <Descriptions.Item label="饮酒频率">{formatValue(lifestyle.drinkingFrequency)}</Descriptions.Item>
              <Descriptions.Item label="日饮酒量">
                {formatByMap(lifestyle.alcoholPerDayLevel, alcoholPerDayLevelLabels)}
              </Descriptions.Item>
              <Descriptions.Item label="戒酒情况">{formatValue(lifestyle.quittingAlcohol)}</Descriptions.Item>
              <Descriptions.Item label="戒酒年龄">
                {formatByMap(lifestyle.alcoholQuitAgeRange, alcoholQuitAgeRangeLabels)}
              </Descriptions.Item>
            </Descriptions>
          </Card>

          <Card size="small" title="摘要信息">
            <Descriptions bordered column={1} size="small">
              <Descriptions.Item label="慢病摘要">{detail.chronicDiseaseSummary ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="过敏摘要">{detail.allergySummary ?? '-'}</Descriptions.Item>
            </Descriptions>
          </Card>
        </Space>
      ) : null}
    </Modal>
  );
}
