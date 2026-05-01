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
