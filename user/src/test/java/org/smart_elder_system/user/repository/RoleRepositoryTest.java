package org.smart_elder_system.user.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import org.smart_elder_system.user.entity.Role;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RoleRepository测试类
 */
@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class RoleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void whenFindByRoleCode_thenReturnRole() {
        // given
        Role role = new Role();
        role.setRoleName("测试角色");
        role.setRoleCode("ROLE_TEST");
        role.setDescription("这是一个测试角色");
        role.setStatus(1);
        role.setDeleteFlag(0);
        role.setCreateTime(LocalDateTime.now());
        entityManager.persist(role);
        entityManager.flush();

        // when
        Role foundRole = roleRepository.findByRoleCode("ROLE_TEST").orElse(null);

        // then
        assertNotNull(foundRole);
        assertEquals(foundRole.getRoleCode(), "ROLE_TEST");
        assertEquals(foundRole.getRoleName(), "测试角色");
    }

    @Test
    void whenFindByRoleName_thenReturnRole() {
        // given
        Role role = new Role();
        role.setRoleName("管理员");
        role.setRoleCode("ROLE_ADMIN");
        role.setDescription("管理员角色");
        role.setStatus(1);
        role.setDeleteFlag(0);
        role.setCreateTime(LocalDateTime.now());
        entityManager.persist(role);
        entityManager.flush();

        // when
        Role foundRole = roleRepository.findByRoleName("管理员").orElse(null);

        // then
        assertNotNull(foundRole);
        assertEquals(foundRole.getRoleName(), "管理员");
        assertEquals(foundRole.getRoleCode(), "ROLE_ADMIN");
    }

    @Test
    void whenExistsByRoleCode_thenReturnTrue() {
        // given
        Role role = new Role();
        role.setRoleName("用户");
        role.setRoleCode("ROLE_USER");
        role.setDescription("普通用户角色");
        role.setStatus(1);
        role.setDeleteFlag(0);
        role.setCreateTime(LocalDateTime.now());
        entityManager.persist(role);
        entityManager.flush();

        // when
        boolean exists = roleRepository.existsByRoleCode("ROLE_USER");

        // then
        assertTrue(exists);
    }

    @Test
    void whenExistsByRoleName_thenReturnTrue() {
        // given
        Role role = new Role();
        role.setRoleName("访客");
        role.setRoleCode("ROLE_GUEST");
        role.setDescription("访客角色");
        role.setStatus(1);
        role.setDeleteFlag(0);
        role.setCreateTime(LocalDateTime.now());
        entityManager.persist(role);
        entityManager.flush();

        // when
        boolean exists = roleRepository.existsByRoleName("访客");

        // then
        assertTrue(exists);
    }
}