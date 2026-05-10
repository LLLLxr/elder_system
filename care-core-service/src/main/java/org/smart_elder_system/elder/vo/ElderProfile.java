package org.smart_elder_system.elder.vo;

import org.smart_elder_system.common.dto.elder.ElderProfileDto;
import org.smart_elder_system.elder.po.ElderProfilePo;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ElderProfile {

    public static final String STATUS_ACTIVE = "ACTIVE";

    private Long elderId;
    private String elderName;
    private String idCard;
    private String phone;
    private String gender;
    private LocalDate birthDate;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ElderProfile fromDto(ElderProfileDto dto) {
        ElderProfile profile = new ElderProfile();
        profile.setElderId(dto.getElderId());
        profile.setElderName(dto.getElderName());
        profile.setIdCard(dto.getIdCard());
        profile.setPhone(dto.getPhone());
        profile.setGender(dto.getGender());
        profile.setBirthDate(dto.getBirthDate());
        profile.setStatus(dto.getStatus());
        profile.setCreatedAt(dto.getCreatedAt());
        profile.setUpdatedAt(dto.getUpdatedAt());
        return profile;
    }

    public static ElderProfile fromPo(ElderProfilePo po) {
        ElderProfile profile = new ElderProfile();
        profile.setElderId(po.getId());
        profile.setElderName(po.getElderName());
        profile.setIdCard(po.getIdCard());
        profile.setPhone(po.getPhone());
        profile.setGender(po.getGender());
        profile.setBirthDate(po.getBirthDate());
        profile.setStatus(po.getStatus());
        profile.setCreatedAt(po.getCreatedDateTimeUtc());
        profile.setUpdatedAt(po.getLastModifiedDateTimeUtc());
        return profile;
    }

    public ElderProfileDto toDto() {
        ElderProfileDto dto = new ElderProfileDto();
        dto.setElderId(elderId);
        dto.setElderName(elderName);
        dto.setIdCard(idCard);
        dto.setPhone(phone);
        dto.setGender(gender);
        dto.setBirthDate(birthDate);
        dto.setStatus(status);
        dto.setCreatedAt(createdAt);
        dto.setUpdatedAt(updatedAt);
        return dto;
    }

    public ElderProfilePo toPo() {
        ElderProfilePo po = new ElderProfilePo();
        po.setId(elderId);
        po.setElderName(elderName);
        po.setIdCard(idCard);
        po.setPhone(phone);
        po.setGender(gender);
        po.setBirthDate(birthDate);
        po.setStatus(status);
        return po;
    }

    public void initialize() {
        if (status == null || status.isBlank()) {
            status = STATUS_ACTIVE;
        }
    }

    public Long getElderId() {
        return elderId;
    }

    public void setElderId(Long elderId) {
        this.elderId = elderId;
    }

    public String getElderName() {
        return elderName;
    }

    public void setElderName(String elderName) {
        this.elderName = elderName;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
