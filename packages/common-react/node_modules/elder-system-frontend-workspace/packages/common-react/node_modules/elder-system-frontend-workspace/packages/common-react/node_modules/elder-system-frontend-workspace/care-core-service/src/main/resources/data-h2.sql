INSERT INTO care_service_application (
    id, elder_id, guardian_id, applicant_name, contact_phone, service_scene,
    service_request, status, intake_at, submitted_at, assessed_at, assessment_conclusion, active_flag,
    created_by, last_modified_by, created_date_time_utc, last_modified_date_time_utc, version
) VALUES
(1, 1001, 2001, '张三', '13800000001', 'HOME_CARE', '基础护理与康复跟踪', 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '满足准入条件', 1, 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1);

INSERT INTO care_service_agreement (
    id, application_id, elder_id, service_scene, status, effective_date, expiry_date, signed_by,
    created_by, last_modified_by, created_date_time_utc, last_modified_date_time_utc, version
) VALUES
(1, 1, 1001, 'HOME_CARE', 'ACTIVE', CURRENT_DATE, DATEADD('YEAR', 1, CURRENT_DATE), '系统管理员', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1);

INSERT INTO care_plan (
    id, agreement_id, elder_id, plan_name, service_scene, personalization_note, status, plan_date,
    created_by, last_modified_by, created_date_time_utc, last_modified_date_time_utc, version
) VALUES
(1, 1, 1001, '居家照护基础计划', 'HOME_CARE', '每日生命体征记录与每周康复训练', 'ACTIVE', CURRENT_DATE, 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1);

INSERT INTO care_health_profile (
    id, elder_id, agreement_id, blood_type, chronic_disease_summary, allergy_summary, risk_level, profile_date,
    created_by, last_modified_by, created_date_time_utc, last_modified_date_time_utc, version
) VALUES
(1, 1001, 1, 'A', '高血压', '无', 'MEDIUM', CURRENT_DATE, 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1);

INSERT INTO care_health_assessment (
    id, elder_id, agreement_id, assessment_type, conclusion, score, assessed_at,
    created_by, last_modified_by, created_date_time_utc, last_modified_date_time_utc, version
) VALUES
(1, 1001, 1, 'MONTHLY', '整体状态稳定，建议继续当前方案', 85, CURRENT_TIMESTAMP, 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1);

INSERT INTO care_health_check_form (
    id, elder_id, author_user_id, agreement_id, elder_name, form_code, check_date, responsible_doctor, form_version,
    symptom_section, vital_sign_section, self_evaluation_section, cognitive_emotion_section,
    lifestyle_section, nursing_conclusion_section, chronic_disease_summary, allergy_summary,
    risk_level, score, conclusion, created_by, last_modified_by, created_date_time_utc, last_modified_date_time_utc, version
) VALUES
(1, 1001, 2001, 1, '张三', 'B.1-2026-0001', CURRENT_DATE, '李医生', 'PAPER_V1',
 '{"headache":false,"dizziness":true,"chronicCough":false}',
 '{"temperature":36.7,"pulse":76,"respiration":18,"bpLeft":"128/76","bpRight":"126/74","height":168,"weight":64,"waist":82,"bmi":22.7}',
 '{"healthSelfEval":"基本满意","adlSelfEval":"可自理"}',
 '{"cognitionPositive":false,"mmseScore":27,"depressionScore":3}',
 '{"exerciseFrequency":"每周一次以上","diet":"荤素均衡","smoking":"不吸烟","drinking":"偶尔"}',
 '{"riskLevel":"MEDIUM","intervention":"保持当前锻炼与饮食"}',
 '高血压', '无', 'MEDIUM', 78, '入服详细评估完成，建议维持当前干预', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1);

INSERT INTO care_service_review (
    id, agreement_id, elder_id, satisfaction_score, review_comment, review_conclusion, reviewed_at,
    created_by, last_modified_by, created_date_time_utc, last_modified_date_time_utc, version
) VALUES
(1, 1, 1001, 90, '服务响应及时，体验良好', 'IN_SERVICE', CURRENT_TIMESTAMP, 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1);
