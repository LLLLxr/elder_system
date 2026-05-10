import type { IntakeRecord, JourneyTaskItem } from '../../types/care';
import { formatDateTime } from '../dateFormat';
import {
  type ApplicationStepKey,
  type ApplicationStepStatus,
  type ApplicationStepItem,
  type JourneyAlert,
  type JourneyProgressData,
  STEP_ORDER,
  STEP_TITLES,
  OPEN_TASK_STATUSES,
  getLatestTaskOfType,
  includesKeyword,
  isStepBefore,
} from './journeyTypes';

export class JourneyStateMachine {
  private admissionTask?: JourneyTaskItem;
  private healthTask?: JourneyTaskItem;
  private journeyStatus?: string;
  private admissionStatus?: string;
  private message?: string;
  private admissionCompleted: boolean;
  private healthCompleted: boolean;
  private admissionActive: boolean;
  private healthActive: boolean;
  private isWithdrawn: boolean;
  private isImprovementRequired: boolean;
  private isRenewed: boolean;
  private isInService: boolean;
  private failureStepKey?: ApplicationStepKey;
  private isAgreementStage: boolean;
  private currentStepKey: ApplicationStepKey;

  constructor(
    private record: IntakeRecord | undefined,
    private timeline: JourneyTaskItem[]
  ) {
    this.admissionTask = getLatestTaskOfType(timeline, 'ADMISSION_ASSESSMENT');
    this.healthTask = getLatestTaskOfType(timeline, 'HEALTH_ASSESSMENT');
    this.journeyStatus = record?.journeyStatus;
    this.admissionStatus = record?.admissionStatus;
    this.message = record?.message;

    this.admissionCompleted = this.admissionTask?.status === 'COMPLETED';
    this.healthCompleted = this.healthTask?.status === 'COMPLETED';
    this.admissionActive = OPEN_TASK_STATUSES.has(this.admissionTask?.status ?? '');
    this.healthActive = OPEN_TASK_STATUSES.has(this.healthTask?.status ?? '');

    this.isWithdrawn = this.admissionStatus === 'WITHDRAWN' || includesKeyword(this.message, '撤回');
    this.isImprovementRequired = this.journeyStatus === 'IMPROVEMENT_REQUIRED';
    this.isRenewed = this.journeyStatus === 'RENEWED';
    this.isInService = this.journeyStatus === 'IN_SERVICE';

    this.failureStepKey = this.determineFailureStep();
    this.isAgreementStage = this.determineAgreementStage();
    this.currentStepKey = this.determineCurrentStep();
  }

  private determineFailureStep(): ApplicationStepKey | undefined {
    if (this.journeyStatus === 'TERMINATED' || this.admissionStatus === 'FAILED' || this.isWithdrawn) {
      if (this.isWithdrawn) {
        return this.healthCompleted ? 'AGREEMENT' : this.healthTask ? 'HEALTH_ASSESSMENT' : 'ADMISSION_ASSESSMENT';
      }
      if (includesKeyword(this.message, '健康评估未通过')) return 'HEALTH_ASSESSMENT';
      if (includesKeyword(this.message, '需求评估未通过')) return 'ADMISSION_ASSESSMENT';
      if (this.healthCompleted) return 'AGREEMENT';
      if (this.healthTask) return 'HEALTH_ASSESSMENT';
      return 'ADMISSION_ASSESSMENT';
    }
    return undefined;
  }

  private determineAgreementStage(): boolean {
    return (
      !this.failureStepKey &&
      !this.isInService &&
      !this.isImprovementRequired &&
      !this.isRenewed &&
      this.healthCompleted &&
      (this.journeyStatus === 'PENDING_HEALTH_ASSESSMENT' ||
       this.journeyStatus === 'PENDING_AGREEMENT' ||
       !this.journeyStatus)
    );
  }

  private determineCurrentStep(): ApplicationStepKey {
    if (this.failureStepKey) return this.failureStepKey;
    if (this.isInService || this.isImprovementRequired || this.isRenewed) return 'IN_SERVICE';
    if (this.isAgreementStage) return 'AGREEMENT';
    if (this.healthActive || this.journeyStatus === 'PENDING_HEALTH_ASSESSMENT' ||
        (this.admissionCompleted && !this.healthTask)) return 'HEALTH_ASSESSMENT';
    return 'ADMISSION_ASSESSMENT';
  }

  getOverallAlert(): JourneyAlert {
    if (this.isWithdrawn) {
      return { type: 'warning', message: this.message ?? '申请已撤回。' };
    }
    if (this.failureStepKey === 'ADMISSION_ASSESSMENT') {
      return { type: 'error', message: this.message ?? '需求评估未通过，当前申请已结束。' };
    }
    if (this.failureStepKey === 'HEALTH_ASSESSMENT') {
      return { type: 'error', message: this.message ?? '健康评估未通过，当前申请已结束。' };
    }
    if (this.failureStepKey === 'AGREEMENT') {
      return { type: 'warning', message: this.message ?? '签约阶段已结束，请以最新旅程结果为准。' };
    }
    if (this.isRenewed) {
      return { type: 'success', message: this.message ?? '服务已续约，可继续关注后续安排。' };
    }
    if (this.isImprovementRequired) {
      return { type: 'warning', message: this.message ?? '当前服务进入改进阶段，请留意平台后续通知。' };
    }
    if (this.isInService) {
      return { type: 'success', message: this.message ?? '已进入服务阶段，可关注后续护理安排。' };
    }
    if (this.isAgreementStage) {
      return { type: 'info', message: this.message ?? '健康评估已完成，正在等待签约安排。' };
    }
    if (this.currentStepKey === 'HEALTH_ASSESSMENT') {
      return { type: 'info', message: this.message ?? '需求评估已完成，正在等待健康评估。' };
    }
    return { type: 'info', message: this.message ?? '申请已提交，正在等待平台完成需求评估。' };
  }

  getStepStatus(stepKey: ApplicationStepKey): ApplicationStepStatus {
    if (stepKey === 'APPLICATION_SUBMITTED') return 'finish';

    if (this.failureStepKey) {
      if (stepKey === this.failureStepKey) return 'error';
      return isStepBefore(stepKey, this.failureStepKey) ? 'finish' : 'wait';
    }

    if (stepKey === 'IN_SERVICE' && (this.isImprovementRequired || this.isRenewed)) return 'finish';
    if (stepKey === this.currentStepKey) return 'process';
    return isStepBefore(stepKey, this.currentStepKey) ? 'finish' : 'wait';
  }

  getProgress(): JourneyProgressData {
    const steps: ApplicationStepItem[] = STEP_ORDER.map((stepKey) => {
      const status = this.getStepStatus(stepKey);
      return {
        key: stepKey,
        title: STEP_TITLES[stepKey],
        status,
        summary: this.buildStepSummary(stepKey, status),
        timeText: this.buildTimeText(stepKey, status),
        hint: this.buildHint(stepKey, status),
      };
    });

    const currentStep = steps.find((item) => item.key === this.currentStepKey) ?? steps[1];

    return {
      steps,
      currentStepKey: this.currentStepKey,
      overallAlert: this.getOverallAlert(),
      currentSummary: currentStep.summary,
      currentHint: currentStep.hint,
    };
  }

  private buildStepSummary(stepKey: ApplicationStepKey, status: ApplicationStepStatus): string {
    if (stepKey === 'APPLICATION_SUBMITTED') return '申请已提交';

    if (stepKey === 'ADMISSION_ASSESSMENT') {
      if (status === 'process') return '等待需求评估';
      if (status === 'finish') return '需求评估已完成';
      if (status === 'error') return this.isWithdrawn ? '申请已撤回' : '需求评估未通过';
      return '等待进入需求评估';
    }

    if (stepKey === 'HEALTH_ASSESSMENT') {
      if (status === 'process') return '等待健康评估';
      if (status === 'finish') return '健康评估已完成';
      if (status === 'error') return '健康评估未通过';
      return '等待完成需求评估';
    }

    if (stepKey === 'AGREEMENT') {
      if (status === 'process') return '等待签约安排';
      if (status === 'finish') return '已完成签约';
      if (status === 'error') return this.isWithdrawn ? '申请已撤回' : '签约阶段已结束';
      return '等待完成健康评估';
    }

    if (status === 'process') return '服务进行中';
    if (status === 'finish') {
      if (this.isRenewed) return '服务已续约';
      if (this.isImprovementRequired) return '进入改进阶段';
      return '服务已完成';
    }
    if (status === 'error') return '服务已结束';
    return '等待完成签约';
  }

  private buildTimeText(stepKey: ApplicationStepKey, status: ApplicationStepStatus): string {
    if (stepKey === 'APPLICATION_SUBMITTED') {
      return this.record?.submittedAt ? `提交于 ${formatDateTime(this.record.submittedAt)}` : '-';
    }

    if (stepKey === 'ADMISSION_ASSESSMENT') {
      if (this.admissionTask?.completedAt) return `完成于 ${formatDateTime(this.admissionTask.completedAt)}`;
      if (this.admissionTask?.dueAt && status === 'process') return `预计处理至 ${formatDateTime(this.admissionTask.dueAt)}`;
      if (this.admissionTask?.createdAt) return `开始于 ${formatDateTime(this.admissionTask.createdAt)}`;
      return this.record?.submittedAt ? `提交于 ${formatDateTime(this.record.submittedAt)}` : '-';
    }

    if (stepKey === 'HEALTH_ASSESSMENT') {
      if (this.healthTask?.completedAt) return `完成于 ${formatDateTime(this.healthTask.completedAt)}`;
      if (this.healthTask?.dueAt && status === 'process') return `预计处理至 ${formatDateTime(this.healthTask.dueAt)}`;
      if (this.healthTask?.createdAt) return `开始于 ${formatDateTime(this.healthTask.createdAt)}`;
      return status === 'wait' ? '-' : '等待平台安排';
    }

    if (stepKey === 'AGREEMENT') {
      if (status === 'finish') {
        return this.isInService || this.isImprovementRequired || this.isRenewed
          ? '已完成签约并进入服务阶段'
          : '签约已完成';
      }
      if (status === 'process') {
        return this.healthTask?.completedAt
          ? `健康评估完成于 ${formatDateTime(this.healthTask.completedAt)}`
          : '等待平台通知';
      }
      if (status === 'error') return '当前申请未进入签约完成阶段';
      return '-';
    }

    if (status === 'process') return '当前服务执行中';
    if (status === 'finish') {
      if (this.isRenewed) return '已续约';
      if (this.isImprovementRequired) return '进入改进阶段';
      return '服务阶段已完成';
    }
    if (status === 'error') return '服务阶段已结束';
    return '-';
  }

  private buildHint(stepKey: ApplicationStepKey, status: ApplicationStepStatus): string {
    if (stepKey === 'APPLICATION_SUBMITTED') return '申请已经提交成功，平台会按旅程步骤继续处理。';

    if (stepKey === 'ADMISSION_ASSESSMENT') {
      if (status === 'process') return '平台正在处理需求评估，请耐心等待。';
      if (status === 'finish') return '需求评估已通过，后续将进入健康评估。';
      if (status === 'error') return this.isWithdrawn ? '申请已经撤回，请以最新结果为准。' : '需求评估阶段已结束，请留意平台通知。';
      return '提交申请后，会先进入需求评估阶段。';
    }

    if (stepKey === 'HEALTH_ASSESSMENT') {
      if (status === 'process') return '请留意健康评估和体检相关通知。';
      if (status === 'finish') return '健康评估已通过，平台将继续安排签约。';
      if (status === 'error') return '健康评估阶段已结束，请留意平台通知。';
      return '需求评估完成后，会进入健康评估阶段。';
    }

    if (stepKey === 'AGREEMENT') {
      if (status === 'process') return '健康评估已完成，请留意签约通知。';
      if (status === 'finish') return '签约已完成，可继续关注服务安排。';
      if (status === 'error') return '当前申请未进入签约完成阶段，请以最新旅程结果为准。';
      return '健康评估通过后，平台会继续安排签约。';
    }

    if (status === 'process') return '服务已经开始，可关注后续护理安排。';
    if (status === 'finish') {
      if (this.isRenewed) return '当前服务已续约，请关注新的服务安排。';
      if (this.isImprovementRequired) return '当前服务进入改进阶段，请留意平台后续通知。';
      return '服务阶段已完成，请以最新结果为准。';
    }
    if (status === 'error') return '当前服务阶段已结束，请以最新结果为准。';
    return '签约完成后，将正式进入服务阶段。';
  }
}
