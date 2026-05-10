import { Descriptions, Tabs, Typography } from 'antd';
import { formatDateTime } from '../utils/dateFormat';
import { getRecordValue, renderRiskFlag, summarizeDoctorContent, summarizeNurseRecord } from '../utils/careRecordUtils';
import type { DoctorRoundRecord, NurseCareRecord } from '../types/care';

interface CareRecordsDetailTabsProps {
  selectedNurseRecord: NurseCareRecord | null;
  selectedDoctorRecord: DoctorRoundRecord | null;
  hasActiveElder: boolean;
  guardMessage: string;
}

export default function CareRecordsDetailTabs({
  selectedNurseRecord,
  selectedDoctorRecord,
  hasActiveElder,
  guardMessage,
}: CareRecordsDetailTabsProps) {
  return (
    <Tabs
      items={[
        {
          key: 'nurse-detail',
          label: '护理记录详情',
          children: selectedNurseRecord ? (
            <Descriptions bordered size="small" column={2}>
              <Descriptions.Item label="记录ID">{selectedNurseRecord.recordId ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="老人姓名">{selectedNurseRecord.elderName ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="护士">{selectedNurseRecord.nurseName ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="服务计划ID">{selectedNurseRecord.servicePlanId ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="记录日期">{selectedNurseRecord.recordDate ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="更新时间">{formatDateTime(selectedNurseRecord.updatedAt)}</Descriptions.Item>
              <Descriptions.Item label="护理摘要" span={2}>{summarizeNurseRecord(selectedNurseRecord)}</Descriptions.Item>
              <Descriptions.Item label="饮食情况">{getRecordValue(selectedNurseRecord, 'dietStatus')}</Descriptions.Item>
              <Descriptions.Item label="睡眠情况">{getRecordValue(selectedNurseRecord, 'sleepStatus')}</Descriptions.Item>
              <Descriptions.Item label="生命体征">{getRecordValue(selectedNurseRecord, 'vitalsStatus')}</Descriptions.Item>
              <Descriptions.Item label="皮肤情况">{getRecordValue(selectedNurseRecord, 'skinStatus')}</Descriptions.Item>
              <Descriptions.Item label="情绪状态">{getRecordValue(selectedNurseRecord, 'emotionStatus')}</Descriptions.Item>
              <Descriptions.Item label="用药情况">{getRecordValue(selectedNurseRecord, 'medicationStatus')}</Descriptions.Item>
              <Descriptions.Item label="康复训练">{getRecordValue(selectedNurseRecord, 'rehabilitationStatus')}</Descriptions.Item>
              <Descriptions.Item label="备注" span={2}>{getRecordValue(selectedNurseRecord, 'remark')}</Descriptions.Item>
            </Descriptions>
          ) : (
            <Typography.Text type="secondary">
              {hasActiveElder ? '暂无可查看的护理记录详情。' : guardMessage}
            </Typography.Text>
          ),
        },
        {
          key: 'doctor-detail',
          label: '查房记录详情',
          children: selectedDoctorRecord ? (
            <Descriptions bordered size="small" column={2}>
              <Descriptions.Item label="记录ID">{selectedDoctorRecord.roundRecordId ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="老人姓名">{selectedDoctorRecord.elderName ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="医生">{selectedDoctorRecord.doctorName ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="风险标记">{renderRiskFlag(selectedDoctorRecord.riskFlag)}</Descriptions.Item>
              <Descriptions.Item label="查房时间">{formatDateTime(selectedDoctorRecord.roundTime)}</Descriptions.Item>
              <Descriptions.Item label="更新时间">{formatDateTime(selectedDoctorRecord.updatedAt)}</Descriptions.Item>
              <Descriptions.Item label="查房摘要">{summarizeDoctorContent(selectedDoctorRecord.content)}</Descriptions.Item>
              <Descriptions.Item label="查房内容" span={2}>{selectedDoctorRecord.content ?? '-'}</Descriptions.Item>
            </Descriptions>
          ) : (
            <Typography.Text type="secondary">
              {hasActiveElder ? '暂无可查看的查房记录详情。' : guardMessage}
            </Typography.Text>
          ),
        },
      ]}
    />
  );
}
