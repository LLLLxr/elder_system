package org.smart_elder_system.common.po;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
@SuperBuilder(toBuilder = true)
public abstract class JpaUserAuditablePo extends JpaAuditablePo implements Serializable {

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    @NotNull
    private String createdBy;

    @LastModifiedBy
    @Column(name = "last_modified_by")
    @NotNull
    private String lastModifiedBy;
}
