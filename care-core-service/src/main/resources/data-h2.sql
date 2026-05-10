INSERT INTO elder_profile (
    id, elder_name, id_card, phone, gender, birth_date, status,
    created_by, last_modified_by, created_date_time_utc, last_modified_date_time_utc, version
) VALUES
(5001, '张三', '110101194903150011', '13800138002', '男', DATE '1949-03-15', 'ACTIVE',
 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1);

INSERT INTO care_service_application (
    id, elder_id, guardian_id, applicant_name, contact_phone, service_scene,
    service_request, status, intake_at, submitted_at, assessed_at, assessment_conclusion, active_flag,
    created_by, last_modified_by, created_date_time_utc, last_modified_date_time_utc, version
) VALUES
(1, 5001, 2001, '张三', '13800000001', 'HOME_CARE', '基础护理与康复跟踪', 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '满足准入条件', 1, 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1);

INSERT INTO care_service_agreement (
    id, application_id, elder_id, service_scene, status, effective_date, expiry_date, signed_by,
    created_by, last_modified_by, created_date_time_utc, last_modified_date_time_utc, version
) VALUES
(1, 1, 5001, 'HOME_CARE', 'ACTIVE', CURRENT_DATE, DATEADD('YEAR', 1, CURRENT_DATE), '系统管理员', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1);

INSERT INTO care_plan (
    id, agreement_id, elder_id, plan_name, service_scene, personalization_note, status, plan_date,
    plan_items_json, assigned_caregiver_id, assigned_caregiver_name,
    created_by, last_modified_by, created_date_time_utc, last_modified_date_time_utc, version
) VALUES
(1, 1, 5001, '居家照护基础计划', 'HOME_CARE', '每日生命体征记录与每周康复训练', 'ACTIVE', CURRENT_DATE,
 '[{"itemCode":"MEAL","itemName":"送餐","completed":false},{"itemCode":"CLEAN","itemName":"清洁护理","completed":false},{"itemCode":"VITALS","itemName":"生命体征观察","completed":false}]',
 5, 'caregiver1', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1);

INSERT INTO caregiver_check_in_record (
    id, elder_id, elder_name, caregiver_id, caregiver_name, service_plan_id, task_date,
    task_items_json, completion_status, completion_time,
    created_by, last_modified_by, created_date_time_utc, last_modified_date_time_utc, version
) VALUES
(1, 5001, '张三', 5, 'caregiver1', 1, CURRENT_DATE,
 '[{"itemCode":"MEAL","itemName":"送餐","completed":true},{"itemCode":"CLEAN","itemName":"清洁护理","completed":true},{"itemCode":"VITALS","itemName":"生命体征观察","completed":false}]',
 'PARTIAL', CURRENT_TIMESTAMP,
 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1);

INSERT INTO nurse_care_record (
    id, elder_id, elder_name, nurse_id, nurse_name, service_plan_id, record_date, record_form_data,
    created_by, last_modified_by, created_date_time_utc, last_modified_date_time_utc, version
) VALUES
(1, 5001, '张三', 2, 'nurse1', 1, CURRENT_DATE,
 '{"dietStatus":"正常","sleepStatus":"一般","remark":"今日情绪平稳"}',
 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1);

INSERT INTO care_health_profile (
    id, elder_id, agreement_id, blood_type, chronic_disease_summary, allergy_summary, risk_level, profile_date,
    created_by, last_modified_by, created_date_time_utc, last_modified_date_time_utc, version
) VALUES
(1, 5001, 1, 'A', '高血压', '无', 'MEDIUM', CURRENT_DATE, 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1);

INSERT INTO care_health_assessment (
    id, elder_id, agreement_id, assessment_type, conclusion, score, assessed_at,
    created_by, last_modified_by, created_date_time_utc, last_modified_date_time_utc, version
) VALUES
(1, 5001, 1, 'MONTHLY', '整体状态稳定，建议继续当前方案', 85, CURRENT_TIMESTAMP, 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1);

INSERT INTO care_health_check_form (
    id, elder_id, author_user_id, agreement_id, elder_name, form_code, check_date, responsible_doctor, form_version,
    symptom_section, vital_sign_section, self_evaluation_section, cognitive_emotion_section,
    lifestyle_section, nursing_conclusion_section, chronic_disease_summary, allergy_summary,
    risk_level, score, conclusion, created_by, last_modified_by, created_date_time_utc, last_modified_date_time_utc, version
) VALUES
(1, 5001, 2001, 1, '张三', 'B.1-2026-0001', CURRENT_DATE, '李医生', 'PAPER_V1',
 '{"headache":false,"dizziness":true,"chronicCough":false}',
 '{"temperature":36.7,"pulse":76,"respiration":18,"bpLeft":"128/76","bpRight":"126/74","height":168,"weight":64,"waist":82,"bmi":22.7}',
 '{"healthSelfEval":"基本满意","adlSelfEval":"可自理"}',
 '{"cognitionPositive":false,"mmseScore":27,"depressionScore":3}',
 '{"exerciseFrequency":"每周一次以上","diet":"荤素均衡","smoking":"不吸烟","drinking":"偶尔"}',
 '{"riskLevel":"MEDIUM","intervention":"保持当前锻炼与饮食"}',
 '高血压', '无', 'MEDIUM', 78, '入服详细评估完成，建议维持当前干预', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1);

INSERT INTO doctor_round_record (
    id, elder_id, elder_name, doctor_id, doctor_name, content, risk_flag, round_time,
    created_by, last_modified_by, created_date_time_utc, last_modified_date_time_utc, version
) VALUES
(1, 5001, '张三', 3, 'doctor1', '今日生命体征平稳，无明显异常', FALSE, CURRENT_TIMESTAMP,
 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1);

INSERT INTO care_service_review (
    id, agreement_id, elder_id, satisfaction_score, review_comment, review_conclusion, reviewed_at,
    created_by, last_modified_by, created_date_time_utc, last_modified_date_time_utc, version
) VALUES
(1, 1, 5001, 90, '服务响应及时，体验良好', 'IN_SERVICE', CURRENT_TIMESTAMP, 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1);

INSERT INTO admission_family_visit_slot (
    id, slot_date, start_time, end_time, capacity, reserved_count, status,
    created_by, last_modified_by, created_date_time_utc, last_modified_date_time_utc, version
) VALUES
(1, DATEADD('DAY', CASE DAY_OF_WEEK(CURRENT_DATE) WHEN 6 THEN 3 WHEN 7 THEN 2 ELSE 1 END, CURRENT_DATE), TIME '09:00:00', TIME '10:00:00', 2, 1, 'OPEN', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
(2, DATEADD('DAY', CASE DAY_OF_WEEK(CURRENT_DATE) WHEN 6 THEN 3 WHEN 7 THEN 2 ELSE 1 END, CURRENT_DATE), TIME '10:00:00', TIME '11:00:00', 1, 1, 'OPEN', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
(3, DATEADD('DAY', CASE DAY_OF_WEEK(CURRENT_DATE) WHEN 6 THEN 4 WHEN 7 THEN 3 ELSE 2 END, CURRENT_DATE), TIME '14:00:00', TIME '15:00:00', 3, 0, 'OPEN', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1);

INSERT INTO admission_family_visit_reservation (
    id, slot_id, elder_id, family_user_id, family_username, visitor_name, visitor_phone, relation_to_elder,
    visit_purpose, status, reviewed_by, review_comment, reviewed_at,
    created_by, last_modified_by, created_date_time_utc, last_modified_date_time_utc, version
) VALUES
(1, 1, 5001, 4, 'family1', '李家属', '13800138004', '儿子', '预约首次探视', 'PENDING', NULL, NULL, NULL, 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
(2, 2, 5001, 4, 'family1', '李家属', '13800138004', '儿子', '确认入住前探视', 'APPROVED', 'medic1', '可以按时到访', CURRENT_TIMESTAMP, 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1);

INSERT INTO quality_caregiver_qualification_application (
    id, caregiver_user_id, caregiver_username, real_name, phone, id_card_no, certificate_no, certificate_type,
    years_of_experience, skill_summary, status, reviewed_by, review_comment, reviewed_at, active_flag,
    created_by, last_modified_by, created_date_time_utc, last_modified_date_time_utc, version
) VALUES
(1, 5, 'caregiver1', '护理员甲', '13800138005', '110101199001050011', 'ZGHL-2026-0001', '护理员证', 6, '擅长失能老人基础照护与翻身拍背', 'PENDING', NULL, NULL, NULL, 1, 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
(2, 5, 'caregiver1', '护理员甲', '13800138005', '110101199001050011', 'ZGHL-2025-0099', '护理员证', 3, '有基础养老机构护理经验', 'REJECTED', 'medic1', '补充近一年培训证明后可重新提交', CURRENT_TIMESTAMP, NULL, 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1);
