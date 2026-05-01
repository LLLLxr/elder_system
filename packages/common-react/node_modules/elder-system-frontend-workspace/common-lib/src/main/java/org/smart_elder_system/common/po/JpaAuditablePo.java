package org.smart_elder_system.common.po;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
@SuperBuilder(toBuilder = true)
public abstract class JpaAuditablePo implements Serializable {

    @CreatedDate
    @Column(name = "created_date_time_utc", updatable = false)
    @NotNull
    private LocalDateTime createdDateTimeUtc;

    @LastModifiedDate
    @Column(name = "last_modified_date_time_utc")
    @NotNull
    private LocalDateTime lastModifiedDateTimeUtc;

    @Builder.Default
    @Version
    @Column(name = "version")
    @NotNull
    private long version = 1;
}
