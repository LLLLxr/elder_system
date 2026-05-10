package org.smart_elder_system.user.po;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.smart_elder_system.common.po.JpaUserAuditablePo;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "user_elder_binding", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_elder_binding_user_elder_type", columnNames = {"user_id", "elder_id", "binding_type"})
})
public class UserElderBindingPo extends JpaUserAuditablePo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "elder_id", nullable = false)
    private Long elderId;

    @Column(name = "binding_type", nullable = false, length = 32)
    private String bindingType;

    @Column(name = "relation_to_elder", length = 64)
    private String relationToElder;
}
