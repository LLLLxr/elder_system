package org.smart_elder_system.careorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceJourneyDeadlineService {

    private final ServiceJourneyTaskService serviceJourneyTaskService;

    @Scheduled(fixedDelayString = "${care.journey.task.deadline-scan-ms:60000}")
    public void markOverdueTasks() {
        int updated = serviceJourneyTaskService.markOverdueTasks(LocalDateTime.now());
        if (updated > 0) {
            log.info("已标记 {} 条旅程任务为逾期", updated);
        }
    }
}
