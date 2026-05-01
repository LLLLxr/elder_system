package org.smart_elder_system.careorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.smart_elder_system.careorchestration.journey.ServiceJourneyEvent;
import org.smart_elder_system.careorchestration.journey.ServiceJourneyState;
import org.smart_elder_system.careorchestration.po.ServiceJourneyTaskPo;
import org.smart_elder_system.careorchestration.po.ServiceJourneyTransitionLogPo;
import org.smart_elder_system.careorchestration.repository.ServiceJourneyTaskRepository;
import org.smart_elder_system.careorchestration.repository.ServiceJourneyTransitionLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never",
        "spring.jpa.show-sql=false"
})
@Import({
        ServiceJourneyTaskService.class,
        ServiceJourneyTransitionLogService.class,
        ServiceJourneyPersistenceIntegrationTest.TestApplication.class
})
class ServiceJourneyPersistenceIntegrationTest {

    @Autowired
    private ServiceJourneyTaskService serviceJourneyTaskService;

    @Autowired
    private ServiceJourneyTransitionLogService transitionLogService;

    @Autowired
    private ServiceJourneyTaskRepository serviceJourneyTaskRepository;

    @Autowired
    private ServiceJourneyTransitionLogRepository transitionLogRepository;

    @Test
    void shouldPersistAndCompleteJourneyTaskWithAuditing() {
        serviceJourneyTaskService.createAdmissionAssessmentTask(1001L, 3003L);
        serviceJourneyTaskService.createAdmissionAssessmentTask(1001L, 3003L);

        List<ServiceJourneyTaskPo> createdTasks = serviceJourneyTaskRepository
                .findByApplicationIdOrderByCreatedDateTimeUtcAsc(1001L);
        assertEquals(1, createdTasks.size());

        ServiceJourneyTaskPo created = createdTasks.get(0);
        assertEquals(ServiceJourneyTaskService.TASK_TYPE_ADMISSION_ASSESSMENT, created.getTaskType());
        assertEquals(ServiceJourneyState.PENDING_ASSESSMENT.name(), created.getCurrentState());
        assertEquals(ServiceJourneyTaskPo.STATUS_PENDING, created.getStatus());
        assertEquals(1, created.getOpenFlag());
        assertEquals("integration-tester", created.getCreatedBy());
        assertEquals("integration-tester", created.getLastModifiedBy());
        assertNotNull(created.getCreatedDateTimeUtc());
        assertNotNull(created.getDueAt());

        serviceJourneyTaskService.completeOpenTask(1001L, ServiceJourneyTaskService.TASK_TYPE_ADMISSION_ASSESSMENT);

        ServiceJourneyTaskPo completed = serviceJourneyTaskRepository.findById(created.getId()).orElseThrow();
        assertEquals(ServiceJourneyTaskPo.STATUS_COMPLETED, completed.getStatus());
        assertNull(completed.getOpenFlag());
        assertNotNull(completed.getCompletedAt());
        assertEquals("integration-tester", completed.getLastModifiedBy());
    }

    @Test
    void shouldPersistTransitionLogsAndSupportStateQueries() {
        transitionLogService.logTransition(
                1001L,
                2002L,
                3003L,
                ServiceJourneyState.PENDING_ASSESSMENT,
                ServiceJourneyEvent.ADMISSION_APPROVED,
                ServiceJourneyState.PENDING_HEALTH_ASSESSMENT,
                "需求评估通过",
                Map.of("applicationId", 1001L, "operator", "tester")
        );

        transitionLogService.logTransition(
                1001L,
                2002L,
                3003L,
                ServiceJourneyState.PENDING_AGREEMENT,
                ServiceJourneyEvent.RETURN_TO_HEALTH_ASSESSMENT,
                ServiceJourneyState.PENDING_HEALTH_ASSESSMENT,
                "补充评估资料",
                "x".repeat(2500)
        );

        List<ServiceJourneyTransitionLogPo> logs = transitionLogRepository
                .findByApplicationIdOrderByTransitionTimeAscIdAsc(1001L);
        assertEquals(2, logs.size());

        ServiceJourneyTransitionLogPo firstLog = logs.get(0);
        assertEquals(ServiceJourneyEvent.ADMISSION_APPROVED.name(), firstLog.getJourneyEvent());
        assertEquals("integration-tester", firstLog.getCreatedBy());
        assertTrue(firstLog.getRequestSnapshot().contains("applicationId"));
        assertNotNull(firstLog.getTransitionTime());

        ServiceJourneyTransitionLogPo secondLog = logs.get(1);
        assertEquals(ServiceJourneyEvent.RETURN_TO_HEALTH_ASSESSMENT.name(), secondLog.getJourneyEvent());
        assertEquals(2000, secondLog.getRequestSnapshot().length());

        assertTrue(transitionLogService.hasTransition(
                1001L,
                ServiceJourneyEvent.ADMISSION_APPROVED,
                ServiceJourneyState.PENDING_HEALTH_ASSESSMENT));
        assertTrue(transitionLogService.hasReturnToHealthAfter(
                1001L,
                secondLog.getTransitionTime().minusSeconds(1)));
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EnableJpaAuditing(auditorAwareRef = "auditorAware")
    @EnableJpaRepositories(basePackageClasses = {
            ServiceJourneyTaskRepository.class,
            ServiceJourneyTransitionLogRepository.class
    })
    @EntityScan(basePackageClasses = {
            ServiceJourneyTaskPo.class,
            ServiceJourneyTransitionLogPo.class
    })
    static class TestApplication {

        @Bean
        AuditorAware<String> auditorAware() {
            return () -> Optional.of("integration-tester");
        }

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }
}
