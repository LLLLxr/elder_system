package org.smart_elder_system.elder.po;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.smart_elder_system.common.po.JpaUserAuditablePo;

import java.time.LocalDate;

@Entity
@Table(name = "elder_profile",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_elder_profile_id_card", columnNames = "id_card")
    },
    indexes = {
        @Index(name = "idx_status", columnList = "status")
    }
)
public class ElderProfilePo extends JpaUserAuditablePo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "elder_name", nullable = false, length = 100)
    private String elderName;

    @Column(name = "id_card", nullable = false, length = 32)
    private String idCard;

    @Column(name = "phone", length = 32)
    private String phone;

    @Column(name = "gender", length = 16)
    private String gender;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}
