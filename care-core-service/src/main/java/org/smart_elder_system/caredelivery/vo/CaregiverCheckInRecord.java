package org.smart_elder_system.caredelivery.vo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.smart_elder_system.caredelivery.po.CaregiverCheckInRecordPo;
import org.smart_elder_system.common.dto.caredelivery.CaregiverCheckInRecordDto;
import org.smart_elder_system.common.dto.caredelivery.CaregiverCheckInSubmitDto;
import org.smart_elder_system.common.dto.caredelivery.DailyCareTaskItemDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaregiverCheckInRecord {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_PARTIAL = "PARTIAL";
    public static final String STATUS_COMPLETED = "COMPLETED";

    private static final TypeReference<List<DailyCareTaskItemDto>> TASK_ITEM_LIST_TYPE = new TypeReference<>() {
    };

    private Long checkInRecordId;
    private Long elderId;
    private String elderName;
    private Long caregiverId;
    private String caregiverName;
    private Long servicePlanId;
    private LocalDate taskDate;
    private List<DailyCareTaskItemDto> taskItems;
    private String completionStatus;
    private LocalDateTime completionTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CaregiverCheckInRecord create(Long servicePlanId, Long caregiverId, String caregiverName, String elderName, CaregiverCheckInSubmitDto dto) {
        CaregiverCheckInRecord record = new CaregiverCheckInRecord();
        record.servicePlanId = servicePlanId;
        record.caregiverId = caregiverId;
        record.caregiverName = caregiverName;
        record.elderId = dto.getElderId();
        record.elderName = elderName;
        record.taskDate = dto.getTaskDate();
        record.taskItems = dto.getTaskItems();
        record.refreshCompletion();
        return record;
    }

    public static CaregiverCheckInRecord fromPo(CaregiverCheckInRecordPo po, ObjectMapper objectMapper) {
        return CaregiverCheckInRecord.builder()
                .checkInRecordId(po.getId())
                .elderId(po.getElderId())
                .elderName(po.getElderName())
                .caregiverId(po.getCaregiverId())
                .caregiverName(po.getCaregiverName())
                .servicePlanId(po.getServicePlanId())
                .taskDate(po.getTaskDate())
                .taskItems(readTaskItems(po.getTaskItemsJson(), objectMapper))
                .completionStatus(po.getCompletionStatus())
                .completionTime(po.getCompletionTime())
                .createdAt(po.getCreatedDateTimeUtc())
                .updatedAt(po.getLastModifiedDateTimeUtc())
                .build();
    }

    public CaregiverCheckInRecordDto toDto() {
        return CaregiverCheckInRecordDto.builder()
                .checkInRecordId(checkInRecordId)
                .elderId(elderId)
                .elderName(elderName)
                .caregiverId(caregiverId)
                .caregiverName(caregiverName)
                .servicePlanId(servicePlanId)
                .taskDate(taskDate)
                .taskItems(taskItems)
                .completionStatus(completionStatus)
                .completionTime(completionTime)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public CaregiverCheckInRecordPo toPo(ObjectMapper objectMapper) {
        return CaregiverCheckInRecordPo.builder()
                .id(checkInRecordId)
                .elderId(elderId)
                .elderName(elderName)
                .caregiverId(caregiverId)
                .caregiverName(caregiverName)
                .servicePlanId(servicePlanId)
                .taskDate(taskDate)
                .taskItemsJson(writeTaskItems(taskItems, objectMapper))
                .completionStatus(completionStatus)
                .completionTime(completionTime)
                .build();
    }

    public void applyTo(CaregiverCheckInRecordPo po, ObjectMapper objectMapper) {
        po.setId(checkInRecordId);
        po.setElderId(elderId);
        po.setElderName(elderName);
        po.setCaregiverId(caregiverId);
        po.setCaregiverName(caregiverName);
        po.setServicePlanId(servicePlanId);
        po.setTaskDate(taskDate);
        po.setTaskItemsJson(writeTaskItems(taskItems, objectMapper));
        po.setCompletionStatus(completionStatus);
        po.setCompletionTime(completionTime);
    }

    public void resubmit(List<DailyCareTaskItemDto> taskItems) {
        this.taskItems = taskItems;
        refreshCompletion();
    }

    private void refreshCompletion() {
        boolean anyCompleted = taskItems != null && taskItems.stream().anyMatch(item -> Boolean.TRUE.equals(item.getCompleted()));
        boolean allCompleted = taskItems != null && !taskItems.isEmpty() && taskItems.stream().allMatch(item -> Boolean.TRUE.equals(item.getCompleted()));
        if (allCompleted) {
            this.completionStatus = STATUS_COMPLETED;
            this.completionTime = LocalDateTime.now();
            return;
        }
        if (anyCompleted) {
            this.completionStatus = STATUS_PARTIAL;
            this.completionTime = LocalDateTime.now();
            return;
        }
        this.completionStatus = STATUS_PENDING;
        this.completionTime = null;
    }

    private static List<DailyCareTaskItemDto> readTaskItems(String json, ObjectMapper objectMapper) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, TASK_ITEM_LIST_TYPE);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("护理员打卡任务项数据格式错误");
        }
    }

    private static String writeTaskItems(List<DailyCareTaskItemDto> taskItems, ObjectMapper objectMapper) {
        try {
            return objectMapper.writeValueAsString(taskItems == null ? List.of() : taskItems);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("护理员打卡任务项序列化失败");
        }
    }
}
