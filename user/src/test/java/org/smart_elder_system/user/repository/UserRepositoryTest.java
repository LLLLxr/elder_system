package org.smart_elder_system.user.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import org.smart_elder_system.user.entity.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserRepository测试类
 */
@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void whenFindByUsername_thenReturnUser() {
        // given
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setEmail("test@example.com");
        user.setStatus(1);
        user.setDeleteFlag(0);
        user.setCreateTime(LocalDateTime.now());
        entityManager.persist(user);
        entityManager.flush();

        // when
        User foundUser = userRepository.findByUsername("testuser").orElse(null);

        // then
        assertNotNull(foundUser);
        assertEquals(foundUser.getUsername(), "testuser");
    }

    @Test
    void whenFindByEmail_thenReturnUser() {
        // given
        User user = new User();
        user.setUsername("testuser2");
        user.setPassword("password");
        user.setEmail("test2@example.com");
        user.setStatus(1);
        user.setDeleteFlag(0);
        user.setCreateTime(LocalDateTime.now());
        entityManager.persist(user);
        entityManager.flush();

        // when
        User foundUser = userRepository.findByEmail("test2@example.com").orElse(null);

        // then
        assertNotNull(foundUser);
        assertEquals(foundUser.getEmail(), "test2@example.com");
    }

    @Test
    void whenExistsByUsername_thenReturnTrue() {
        // given
        User user = new User();
        user.setUsername("testuser3");
        user.setPassword("password");
        user.setEmail("test3@example.com");
        user.setStatus(1);
        user.setDeleteFlag(0);
        user.setCreateTime(LocalDateTime.now());
        entityManager.persist(user);
        entityManager.flush();

        // when
        boolean exists = userRepository.existsByUsername("testuser3");

        // then
        assertTrue(exists);
    }

    @Test
    void whenExistsByEmail_thenReturnTrue() {
        // given
        User user = new User();
        user.setUsername("testuser4");
        user.setPassword("password");
        user.setEmail("test4@example.com");
        user.setStatus(1);
        user.setDeleteFlag(0);
        user.setCreateTime(LocalDateTime.now());
        entityManager.persist(user);
        entityManager.flush();

        // when
        boolean exists = userRepository.existsByEmail("test4@example.com");

        // then
        assertTrue(exists);
    }
}