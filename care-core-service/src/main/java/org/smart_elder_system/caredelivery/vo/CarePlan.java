package org.smart_elder_system.caredelivery.vo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.smart_elder_system.caredelivery.po.CarePlanPo;
import org.smart_elder_system.common.dto.caredelivery.CarePlanDto;
import org.smart_elder_system.common.dto.caredelivery.DailyCareTaskItemDto;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarePlan {

    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_CLOSED = "CLOSED";

    private static final ObjectMapper DEFAULT_OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<List<DailyCareTaskItemDto>> TASK_ITEM_LIST_TYPE = new TypeReference<>() {
    };

    private Long planId;
    private Long agreementId;
    private Long elderId;
    private String planName;
    private String serviceScene;
    private String personalizationNote;
    private String status;
    private LocalDate planDate;
    private List<DailyCareTaskItemDto> planItems;
    private Long assignedCaregiverId;
    private String assignedCaregiverName;

    public static CarePlan fromDto(CarePlanDto dto) {
        return CarePlan.builder()
                .planId(dto.getPlanId())
                .agreementId(dto.getAgreementId())
                .elderId(dto.getElderId())
                .planName(dto.getPlanName())
                .serviceScene(dto.getServiceScene())
                .personalizationNote(dto.getPersonalizationNote())
                .status(dto.getStatus())
                .planDate(dto.getPlanDate())
                .planItems(dto.getPlanItems())
                .assignedCaregiverId(dto.getAssignedCaregiverId())
                .assignedCaregiverName(dto.getAssignedCaregiverName())
                .build();
    }

    public static CarePlan fromPo(CarePlanPo po) {
        return fromPo(po, DEFAULT_OBJECT_MAPPER);
    }

    public static CarePlan fromPo(CarePlanPo po, ObjectMapper objectMapper) {
        return CarePlan.builder()
                .planId(po.getId())
                .agreementId(po.getAgreementId())
                .elderId(po.getElderId())
                .planName(po.getPlanName())
                .serviceScene(po.getServiceScene())
                .personalizationNote(po.getPersonalizationNote())
                .status(po.getStatus())
                .planDate(po.getPlanDate())
                .planItems(readTaskItems(po.getPlanItemsJson(), objectMapper))
                .assignedCaregiverId(po.getAssignedCaregiverId())
                .assignedCaregiverName(po.getAssignedCaregiverName())
                .build();
    }

    public CarePlanDto toDto() {
        return CarePlanDto.builder()
                .planId(planId)
                .agreementId(agreementId)
                .elderId(elderId)
                .planName(planName)
                .serviceScene(serviceScene)
                .personalizationNote(personalizationNote)
                .status(status)
                .planDate(planDate)
                .planItems(planItems)
                .assignedCaregiverId(assignedCaregiverId)
                .assignedCaregiverName(assignedCaregiverName)
                .build();
    }

    public CarePlanPo toPo() {
        return toPo(DEFAULT_OBJECT_MAPPER);
    }

    public CarePlanPo toPo(ObjectMapper objectMapper) {
        return CarePlanPo.builder()
                .id(planId)
                .agreementId(agreementId)
                .elderId(elderId)
                .planName(planName)
                .serviceScene(serviceScene)
                .personalizationNote(personalizationNote)
                .status(status)
                .planDate(planDate)
                .planItemsJson(writeTaskItems(planItems, objectMapper))
                .assignedCaregiverId(assignedCaregiverId)
                .assignedCaregiverName(assignedCaregiverName)
                .build();
    }

    public void applyTo(CarePlanPo po) {
        applyTo(po, DEFAULT_OBJECT_MAPPER);
    }

    public void applyTo(CarePlanPo po, ObjectMapper objectMapper) {
        po.setId(planId);
        po.setAgreementId(agreementId);
        po.setElderId(elderId);
        po.setPlanName(planName);
        po.setServiceScene(serviceScene);
        po.setPersonalizationNote(personalizationNote);
        po.setStatus(status);
        po.setPlanDate(planDate);
        po.setPlanItemsJson(writeTaskItems(planItems, objectMapper));
        po.setAssignedCaregiverId(assignedCaregiverId);
        po.setAssignedCaregiverName(assignedCaregiverName);
    }

    public void create() {
        this.status = STATUS_CREATED;
        if (this.planDate == null) {
            this.planDate = LocalDate.now();
        }
        if (this.planItems == null || this.planItems.isEmpty()) {
            this.planItems = defaultPlanItems();
        }
    }

    public void start() {
        this.status = STATUS_IN_PROGRESS;
    }

    public void close() {
        this.status = STATUS_CLOSED;
    }

    public static List<DailyCareTaskItemDto> defaultPlanItems() {
        return List.of(
                DailyCareTaskItemDto.builder().itemCode("MEAL").itemName("送餐").completed(false).build(),
                DailyCareTaskItemDto.builder().itemCode("CLEAN").itemName("清洁护理").completed(false).build(),
                DailyCareTaskItemDto.builder().itemCode("VITALS").itemName("生命体征观察").completed(false).build()
        );
    }

    private static List<DailyCareTaskItemDto> readTaskItems(String json, ObjectMapper objectMapper) {
        if (json == null || json.isBlank()) {
            return defaultPlanItems();
        }
        try {
            return resolveObjectMapper(objectMapper).readValue(json, TASK_ITEM_LIST_TYPE);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("护理计划任务项数据格式错误");
        }
    }

    private static String writeTaskItems(List<DailyCareTaskItemDto> taskItems, ObjectMapper objectMapper) {
        try {
            return resolveObjectMapper(objectMapper).writeValueAsString(taskItems == null || taskItems.isEmpty() ? defaultPlanItems() : taskItems);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("护理计划任务项序列化失败");
        }
    }

    private static ObjectMapper resolveObjectMapper(ObjectMapper objectMapper) {
        return objectMapper == null ? DEFAULT_OBJECT_MAPPER : objectMapper;
    }
}
