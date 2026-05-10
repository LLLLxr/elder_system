CREATE TABLE elder_profile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    elder_name VARCHAR(100) NOT NULL,
    id_card VARCHAR(32) NOT NULL,
    phone VARCHAR(32),
    gender VARCHAR(16),
    birth_date DATE,
    status VARCHAR(32) NOT NULL,
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    last_modified_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_date_time_utc TIMESTAMP NOT NULL,
    last_modified_date_time_utc TIMESTAMP NOT NULL,
    version BIGINT NOT NULL,
    CONSTRAINT uk_elder_profile_id_card UNIQUE (id_card)
);

CREATE TABLE care_service_application (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    elder_id BIGINT NOT NULL,
    guardian_id BIGINT,
    applicant_name VARCHAR(100),
    contact_phone VARCHAR(32),
    service_scene VARCHAR(32),
    service_request VARCHAR(500),
    status VARCHAR(32),
    intake_at TIMESTAMP,
    submitted_at TIMESTAMP,
    assessed_at TIMESTAMP,
    assessment_conclusion VARCHAR(500),
    active_flag INTEGER,
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    last_modified_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_date_time_utc TIMESTAMP NOT NULL,
    last_modified_date_time_utc TIMESTAMP NOT NULL,
    version BIGINT NOT NULL,
    CONSTRAINT uk_care_service_application_elder_active UNIQUE (elder_id, active_flag)
);

CREATE TABLE care_service_agreement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL,
    elder_id BIGINT NOT NULL,
    service_scene VARCHAR(32),
    status VARCHAR(32),
    effective_date DATE,
    expiry_date DATE,
    signed_by VARCHAR(100),
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    last_modified_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_date_time_utc TIMESTAMP NOT NULL,
    last_modified_date_time_utc TIMESTAMP NOT NULL,
    version BIGINT NOT NULL,
    CONSTRAINT uk_care_service_agreement_application UNIQUE (application_id)
);

CREATE TABLE care_plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    agreement_id BIGINT NOT NULL,
    elder_id BIGINT NOT NULL,
    plan_name VARCHAR(100),
    service_scene VARCHAR(32),
    personalization_note VARCHAR(500),
    status VARCHAR(32),
    plan_date DATE,
    plan_items_json VARCHAR(5000),
    assigned_caregiver_id BIGINT,
    assigned_caregiver_name VARCHAR(100),
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    last_modified_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_date_time_utc TIMESTAMP NOT NULL,
    last_modified_date_time_utc TIMESTAMP NOT NULL,
    version BIGINT NOT NULL
);

CREATE TABLE caregiver_check_in_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    elder_id BIGINT NOT NULL,
    elder_name VARCHAR(100),
    caregiver_id BIGINT NOT NULL,
    caregiver_name VARCHAR(100),
    service_plan_id BIGINT NOT NULL,
    task_date DATE NOT NULL,
    task_items_json VARCHAR(5000) NOT NULL,
    completion_status VARCHAR(32) NOT NULL,
    completion_time TIMESTAMP,
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    last_modified_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_date_time_utc TIMESTAMP NOT NULL,
    last_modified_date_time_utc TIMESTAMP NOT NULL,
    version BIGINT NOT NULL,
    CONSTRAINT uk_caregiver_check_in_record_daily UNIQUE (service_plan_id, caregiver_id, elder_id, task_date)
);

CREATE TABLE nurse_care_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    elder_id BIGINT NOT NULL,
    elder_name VARCHAR(100),
    nurse_id BIGINT NOT NULL,
    nurse_name VARCHAR(100),
    service_plan_id BIGINT,
    record_date DATE NOT NULL,
    record_form_data VARCHAR(5000) NOT NULL,
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    last_modified_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_date_time_utc TIMESTAMP NOT NULL,
    last_modified_date_time_utc TIMESTAMP NOT NULL,
    version BIGINT NOT NULL
);

CREATE TABLE care_health_profile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    elder_id BIGINT NOT NULL,
    agreement_id BIGINT NOT NULL,
    blood_type VARCHAR(10),
    chronic_disease_summary VARCHAR(500),
    allergy_summary VARCHAR(500),
    risk_level VARCHAR(32),
    profile_date DATE,
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    last_modified_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_date_time_utc TIMESTAMP NOT NULL,
    last_modified_date_time_utc TIMESTAMP NOT NULL,
    version BIGINT NOT NULL,
    CONSTRAINT uk_care_health_profile_elder_agreement UNIQUE (elder_id, agreement_id)
);

CREATE TABLE care_health_assessment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT,
    elder_id BIGINT NOT NULL,
    agreement_id BIGINT NOT NULL,
    assessment_type VARCHAR(64),
    conclusion VARCHAR(500),
    score INTEGER,
    assessed_at TIMESTAMP,
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    last_modified_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_date_time_utc TIMESTAMP NOT NULL,
    last_modified_date_time_utc TIMESTAMP NOT NULL,
    version BIGINT NOT NULL,
    CONSTRAINT uk_care_health_assessment_application_type UNIQUE (application_id, assessment_type)
);

CREATE TABLE care_health_check_form (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    elder_id BIGINT NOT NULL,
    author_user_id BIGINT,
    agreement_id BIGINT NOT NULL,
    elder_name VARCHAR(100),
    form_code VARCHAR(64),
    check_date DATE,
    responsible_doctor VARCHAR(100),
    form_version VARCHAR(32),
    symptom_section VARCHAR(5000),
    vital_sign_section VARCHAR(5000),
    self_evaluation_section VARCHAR(5000),
    cognitive_emotion_section VARCHAR(5000),
    lifestyle_section VARCHAR(5000),
    nursing_conclusion_section VARCHAR(5000),
    chronic_disease_summary VARCHAR(500),
    allergy_summary VARCHAR(500),
    risk_level VARCHAR(32),
    score INTEGER,
    conclusion VARCHAR(500),
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    last_modified_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_date_time_utc TIMESTAMP NOT NULL,
    last_modified_date_time_utc TIMESTAMP NOT NULL,
    version BIGINT NOT NULL
);

CREATE TABLE doctor_round_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    elder_id BIGINT NOT NULL,
    elder_name VARCHAR(100),
    doctor_id BIGINT NOT NULL,
    doctor_name VARCHAR(100),
    content VARCHAR(2000) NOT NULL,
    risk_flag BOOLEAN NOT NULL,
    round_time TIMESTAMP NOT NULL,
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    last_modified_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_date_time_utc TIMESTAMP NOT NULL,
    last_modified_date_time_utc TIMESTAMP NOT NULL,
    version BIGINT NOT NULL
);

CREATE TABLE care_service_review (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    agreement_id BIGINT NOT NULL,
    elder_id BIGINT NOT NULL,
    satisfaction_score INTEGER,
    review_comment VARCHAR(500),
    review_conclusion VARCHAR(32),
    reviewed_at TIMESTAMP,
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    last_modified_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_date_time_utc TIMESTAMP NOT NULL,
    last_modified_date_time_utc TIMESTAMP NOT NULL,
    version BIGINT NOT NULL
);

CREATE TABLE care_service_journey_transition_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT,
    agreement_id BIGINT,
    elder_id BIGINT,
    from_state VARCHAR(64),
    journey_event VARCHAR(64) NOT NULL,
    to_state VARCHAR(64) NOT NULL,
    reason VARCHAR(500),
    request_snapshot VARCHAR(2000),
    transition_time TIMESTAMP NOT NULL,
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    last_modified_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_date_time_utc TIMESTAMP NOT NULL,
    last_modified_date_time_utc TIMESTAMP NOT NULL,
    version BIGINT NOT NULL
);

CREATE TABLE care_service_journey_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT,
    agreement_id BIGINT,
    elder_id BIGINT,
    task_type VARCHAR(64) NOT NULL,
    current_state VARCHAR(64) NOT NULL,
    assignee_role VARCHAR(64),
    status VARCHAR(32) NOT NULL,
    open_flag INTEGER,
    due_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    last_modified_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_date_time_utc TIMESTAMP NOT NULL,
    last_modified_date_time_utc TIMESTAMP NOT NULL,
    version BIGINT NOT NULL,
    CONSTRAINT uk_care_service_journey_task_open UNIQUE (application_id, task_type, open_flag)
);

CREATE TABLE admission_family_visit_slot (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    slot_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    capacity INTEGER NOT NULL,
    reserved_count INTEGER NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    last_modified_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_date_time_utc TIMESTAMP NOT NULL,
    last_modified_date_time_utc TIMESTAMP NOT NULL,
    version BIGINT NOT NULL,
    CONSTRAINT uk_admission_family_visit_slot_date_time UNIQUE (slot_date, start_time, end_time)
);

CREATE TABLE admission_family_visit_reservation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    slot_id BIGINT NOT NULL,
    elder_id BIGINT NOT NULL,
    family_user_id BIGINT NOT NULL,
    family_username VARCHAR(100) NOT NULL,
    visitor_name VARCHAR(100) NOT NULL,
    visitor_phone VARCHAR(32) NOT NULL,
    relation_to_elder VARCHAR(64) NOT NULL,
    visit_purpose VARCHAR(500) NOT NULL,
    status VARCHAR(32) NOT NULL,
    reviewed_by VARCHAR(100),
    review_comment VARCHAR(500),
    reviewed_at TIMESTAMP,
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    last_modified_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_date_time_utc TIMESTAMP NOT NULL,
    last_modified_date_time_utc TIMESTAMP NOT NULL,
    version BIGINT NOT NULL,
    CONSTRAINT uk_admission_family_visit_reservation_slot_family_elder UNIQUE (slot_id, family_user_id, elder_id)
);

CREATE TABLE quality_caregiver_qualification_application (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    caregiver_user_id BIGINT NOT NULL,
    caregiver_username VARCHAR(100) NOT NULL,
    real_name VARCHAR(100) NOT NULL,
    phone VARCHAR(32) NOT NULL,
    id_card_no VARCHAR(32) NOT NULL,
    certificate_no VARCHAR(64) NOT NULL,
    certificate_type VARCHAR(64) NOT NULL,
    years_of_experience INTEGER,
    skill_summary VARCHAR(1000) NOT NULL,
    status VARCHAR(32) NOT NULL,
    reviewed_by VARCHAR(100),
    review_comment VARCHAR(500),
    reviewed_at TIMESTAMP,
    active_flag INTEGER,
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    last_modified_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_date_time_utc TIMESTAMP NOT NULL,
    last_modified_date_time_utc TIMESTAMP NOT NULL,
    version BIGINT NOT NULL,
    CONSTRAINT uk_quality_caregiver_qualification_active UNIQUE (caregiver_user_id, active_flag)
);
