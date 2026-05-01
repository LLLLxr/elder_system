package org.smart_elder_system.careorchestration.controller;

import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.smart_elder_system.careorchestration.dto.CareAnalyticsOverviewDTO;
import org.smart_elder_system.exception.GlobalExceptionHandler;
import org.smart_elder_system.careorchestration.dto.ServiceJourneyTaskItemDTO;
import org.smart_elder_system.careorchestration.dto.ServiceJourneyTaskOverviewDTO;
import org.smart_elder_system.careorchestration.dto.ServiceJourneyTransitionLogItemDTO;
import org.smart_elder_system.careorchestration.journey.ServiceJourneyState;
import org.smart_elder_system.careorchestration.service.CareOrchestrationService;
import org.smart_elder_system.common.dto.care.IntakeRecordDTO;
import org.smart_elder_system.common.dto.care.ServiceJourneyResultDTO;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CareOrchestrationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CareOrchestrationService careOrchestrationService;

    @InjectMocks
    private CareOrchestrationController careOrchestrationController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(careOrchestrationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    @Test
    void shouldListJourneyTasks() throws Exception {
        ServiceJourneyTaskItemDTO item = new ServiceJourneyTaskItemDTO();
        item.setTaskId(9L);
        item.setApplicationId(1001L);
        item.setTaskType("ADMISSION_ASSESSMENT");
        item.setCurrentState("PENDING_ASSESSMENT");
        item.setStatus("PENDING");
        item.setCreatedAt(LocalDateTime.now());

        when(careOrchestrationService.listJourneyTasks(
                1001L,
                null,
                null,
                null,
                List.of("PENDING", "OVERDUE"),
                null,
                0,
                20,
                "dueAt",
                "asc"))
                .thenReturn(new PageImpl<>(List.of(item), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/care-orchestration/journey-tasks")
                        .param("applicationId", "1001")
                        .param("statuses", "PENDING", "OVERDUE")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].taskId").value(9))
                .andExpect(jsonPath("$.content[0].taskType").value("ADMISSION_ASSESSMENT"))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(20));
    }

    @Test
    void shouldListJourneyTaskTimeline() throws Exception {
        ServiceJourneyTaskItemDTO item = new ServiceJourneyTaskItemDTO();
        item.setTaskId(9L);
        item.setApplicationId(1001L);
        item.setTaskType("ADMISSION_ASSESSMENT");
        item.setStatus("COMPLETED");
        item.setCreatedAt(LocalDateTime.now().minusDays(1));

        when(careOrchestrationService.listJourneyTaskTimeline(1001L)).thenReturn(List.of(item));

        mockMvc.perform(get("/care-orchestration/journey-tasks/timeline")
                        .param("applicationId", "1001")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].taskId").value(9))
                .andExpect(jsonPath("$[0].applicationId").value(1001))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"));
    }

    @Test
    void shouldReturnTaskOverview() throws Exception {
        ServiceJourneyTaskOverviewDTO overview = new ServiceJourneyTaskOverviewDTO();
        overview.setPendingCount(3);
        overview.setOverdueCount(1);
        overview.setCompletedCount(4);
        overview.setCancelledCount(0);

        CareAnalyticsOverviewDTO.StagePoint point = new CareAnalyticsOverviewDTO.StagePoint();
        point.setName("PENDING");
        point.setValue(3);
        overview.setStatusDistribution(List.of(point));

        when(careOrchestrationService.getJourneyTaskOverview(
                null,
                null,
                null,
                null,
                List.of("PENDING", "OVERDUE"),
                null)).thenReturn(overview);

        mockMvc.perform(get("/care-orchestration/journey-tasks/overview")
                        .param("statuses", "PENDING", "OVERDUE")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingCount").value(3))
                .andExpect(jsonPath("$.overdueCount").value(1))
                .andExpect(jsonPath("$.statusDistribution[0].name").value("PENDING"));
    }

    @Test
    void shouldListTransitionLogsByApplication() throws Exception {
        ServiceJourneyTransitionLogItemDTO item = new ServiceJourneyTransitionLogItemDTO();
        item.setLogId(1L);
        item.setApplicationId(1001L);
        item.setJourneyEvent("RETURN_TO_ASSESSMENT");
        item.setToState("PENDING_ASSESSMENT");
        item.setReason("资料补充");
        item.setTransitionTime(LocalDateTime.now());

        when(careOrchestrationService.listJourneyTransitionLogsByApplication(1001L)).thenReturn(List.of(item));

        mockMvc.perform(get("/care-orchestration/journey-transition-logs/by-application")
                        .param("applicationId", "1001")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].logId").value(1))
                .andExpect(jsonPath("$[0].journeyEvent").value("RETURN_TO_ASSESSMENT"))
                .andExpect(jsonPath("$[0].reason").value("资料补充"));
    }

    @Test
    void shouldListTransitionLogsByAgreement() throws Exception {
        ServiceJourneyTransitionLogItemDTO item = new ServiceJourneyTransitionLogItemDTO();
        item.setLogId(2L);
        item.setAgreementId(2002L);
        item.setJourneyEvent("RETURN_TO_HEALTH_ASSESSMENT");
        item.setToState("PENDING_HEALTH_ASSESSMENT");
        item.setReason("补充评估资料");
        item.setTransitionTime(LocalDateTime.now());

        when(careOrchestrationService.listJourneyTransitionLogsByAgreement(2002L)).thenReturn(List.of(item));

        mockMvc.perform(get("/care-orchestration/journey-transition-logs/by-agreement")
                        .param("agreementId", "2002")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].logId").value(2))
                .andExpect(jsonPath("$[0].agreementId").value(2002))
                .andExpect(jsonPath("$[0].journeyEvent").value("RETURN_TO_HEALTH_ASSESSMENT"));
    }

    @Test
    void shouldReturnJourneyStep() throws Exception {
        ServiceJourneyResultDTO result = new ServiceJourneyResultDTO();
        result.setApplicationId(1001L);
        result.setFinalStatus("PENDING_ASSESSMENT");
        result.setMessage("申请已提交，待管理端完成需求评估");

        when(careOrchestrationService.returnJourneyStep(
                eq(1001L),
                eq(ServiceJourneyState.PENDING_ASSESSMENT),
                eq("资料补充")))
                .thenReturn(result);

        mockMvc.perform(post("/care-orchestration/journeys/return")
                        .param("applicationId", "1001")
                        .param("targetState", "PENDING_ASSESSMENT")
                        .param("reason", "资料补充")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationId").value(1001))
                .andExpect(jsonPath("$.finalStatus").value("PENDING_ASSESSMENT"));

        verify(careOrchestrationService).returnJourneyStep(1001L, ServiceJourneyState.PENDING_ASSESSMENT, "资料补充");
    }

    @Test
    void shouldStartServiceJourney() throws Exception {
        ServiceJourneyResultDTO result = new ServiceJourneyResultDTO();
        result.setApplicationId(1001L);
        result.setElderId(3003L);
        result.setFinalStatus("PENDING_ASSESSMENT");
        result.setMessage("申请已提交，待管理端完成需求评估");

        when(careOrchestrationService.startServiceJourney(
                3003L,
                4004L,
                "张三",
                "13800000000",
                "HOME",
                "助餐")).thenReturn(result);

        mockMvc.perform(post("/care-orchestration/journeys/start")
                        .param("elderId", "3003")
                        .param("guardianId", "4004")
                        .param("applicantName", "张三")
                        .param("contactPhone", "13800000000")
                        .param("serviceScene", "HOME")
                        .param("serviceRequest", "助餐")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationId").value(1001))
                .andExpect(jsonPath("$.elderId").value(3003))
                .andExpect(jsonPath("$.finalStatus").value("PENDING_ASSESSMENT"));
    }

    @Test
    void shouldContinueJourneyAfterAssessment() throws Exception {
        ServiceJourneyResultDTO result = new ServiceJourneyResultDTO();
        result.setApplicationId(1001L);
        result.setElderId(3003L);
        result.setFinalStatus("PENDING_HEALTH_ASSESSMENT");
        result.setMessage("需求评估已通过，待完成健康评估后继续签约");

        when(careOrchestrationService.continueAfterAssessment(1001L)).thenReturn(result);

        mockMvc.perform(post("/care-orchestration/journeys/continue")
                        .param("applicationId", "1001")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationId").value(1001))
                .andExpect(jsonPath("$.finalStatus").value("PENDING_HEALTH_ASSESSMENT"));
    }

    @Test
    void shouldRejectAdmissionJourney() throws Exception {
        ServiceJourneyResultDTO result = new ServiceJourneyResultDTO();
        result.setApplicationId(1001L);
        result.setFinalStatus("TERMINATED");
        result.setMessage("需求评估未通过，服务终止");

        when(careOrchestrationService.rejectAdmissionJourney(1001L, "不符合准入条件", "tester"))
                .thenReturn(result);

        mockMvc.perform(post("/care-orchestration/journeys/reject-admission")
                        .param("applicationId", "1001")
                        .param("assessmentConclusion", "不符合准入条件")
                        .param("assessor", "tester")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationId").value(1001))
                .andExpect(jsonPath("$.finalStatus").value("TERMINATED"));
    }

    @Test
    void shouldRejectHealthJourney() throws Exception {
        ServiceJourneyResultDTO result = new ServiceJourneyResultDTO();
        result.setApplicationId(1001L);
        result.setFinalStatus("TERMINATED");
        result.setMessage("健康评估未通过，服务终止");

        when(careOrchestrationService.rejectHealthJourney(1001L, "高风险不适合签约", "nurse", "doctor", 40))
                .thenReturn(result);

        mockMvc.perform(post("/care-orchestration/journeys/reject-health")
                        .param("applicationId", "1001")
                        .param("assessmentConclusion", "高风险不适合签约")
                        .param("assessor", "nurse")
                        .param("responsibleDoctor", "doctor")
                        .param("score", "40")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationId").value(1001))
                .andExpect(jsonPath("$.finalStatus").value("TERMINATED"));
    }

    @Test
    void shouldWithdrawJourney() throws Exception {
        ServiceJourneyResultDTO result = new ServiceJourneyResultDTO();
        result.setApplicationId(1001L);
        result.setFinalStatus("TERMINATED");
        result.setMessage("申请已撤回");

        when(careOrchestrationService.withdrawServiceJourney(1001L, "用户主动撤回"))
                .thenReturn(result);

        mockMvc.perform(post("/care-orchestration/journeys/withdraw")
                        .param("applicationId", "1001")
                        .param("reason", "用户主动撤回")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationId").value(1001))
                .andExpect(jsonPath("$.finalStatus").value("TERMINATED"));
    }

    @Test
    void shouldReviewAndFinalizeJourney() throws Exception {
        ServiceJourneyResultDTO result = new ServiceJourneyResultDTO();
        result.setApplicationId(1001L);
        result.setAgreementId(2002L);
        result.setFinalStatus("RENEWED");
        result.setMessage("服务评价结果为续约，协议已续约");

        when(careOrchestrationService.reviewAndFinalize(2002L, 3003L, 85, "改善后续约"))
                .thenReturn(result);

        mockMvc.perform(post("/care-orchestration/journeys/review")
                        .param("agreementId", "2002")
                        .param("elderId", "3003")
                        .param("satisfactionScore", "85")
                        .param("reviewComment", "改善后续约")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agreementId").value(2002))
                .andExpect(jsonPath("$.finalStatus").value("RENEWED"));
    }

    @Test
    void shouldListIntakeRecords() throws Exception {
        IntakeRecordDTO record = new IntakeRecordDTO();
        record.setApplicationId(1001L);
        record.setElderId(3003L);
        record.setApplicantName("张三");
        record.setJourneyStatus("PENDING_HEALTH_ASSESSMENT");
        record.setMessage("需求评估已通过，待完成健康评估与签约");

        when(careOrchestrationService.listIntakeRecords(3003L)).thenReturn(List.of(record));

        mockMvc.perform(get("/care-orchestration/journeys/intake-records")
                        .param("elderId", "3003")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].applicationId").value(1001))
                .andExpect(jsonPath("$[0].elderId").value(3003))
                .andExpect(jsonPath("$[0].journeyStatus").value("PENDING_HEALTH_ASSESSMENT"));
    }

    @Test
    void shouldGetLatestJourneyResultByApplicant() throws Exception {
        ServiceJourneyResultDTO result = new ServiceJourneyResultDTO();
        result.setApplicationId(1001L);
        result.setElderId(3003L);
        result.setAgreementId(2002L);
        result.setFinalStatus("IN_SERVICE");
        result.setMessage("已签订有效协议，服务执行中");

        when(careOrchestrationService.getLatestJourneyResultByApplicant("张三")).thenReturn(result);

        mockMvc.perform(get("/care-orchestration/journeys/latest-result/by-applicant")
                        .param("applicantName", "张三")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationId").value(1001))
                .andExpect(jsonPath("$.agreementId").value(2002))
                .andExpect(jsonPath("$.finalStatus").value("IN_SERVICE"));
    }

    @Test
    void shouldRejectInvalidReturnRequest() throws Exception {
        mockMvc.perform(post("/care-orchestration/journeys/return")
                        .param("applicationId", "1001")
                        .param("targetState", "INVALID_STATE")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("参数绑定失败"));
    }

    @Test
    void shouldRejectInvalidTimelineRequest() throws Exception {
        mockMvc.perform(get("/care-orchestration/journey-tasks/timeline")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("缺少必要参数: applicationId"));
    }

    @Test
    void shouldRejectInvalidAgreementTransitionLogRequest() throws Exception {
        mockMvc.perform(get("/care-orchestration/journey-transition-logs/by-agreement")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("缺少必要参数: agreementId"));
    }
}
