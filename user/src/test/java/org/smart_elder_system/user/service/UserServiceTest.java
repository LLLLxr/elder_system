package org.smart_elder_system.user.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.smart_elder_system.user.dto.RegisterDTO;
import org.smart_elder_system.user.dto.UserDTO;
import org.smart_elder_system.user.entity.User;
import org.smart_elder_system.user.vo.UserVO;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户服务测试类
 */
@SpringBootTest
@Transactional
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void testCreateUser() {
        // 创建测试数据
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("testuser");
        userDTO.setPassword("123456");
        userDTO.setRealName("测试用户");
        userDTO.setPhone("13800138000");
        userDTO.setEmail("test@example.com");

        // 调用方法
        User user = userService.createUser(userDTO);

        // 验证结果
        assertNotNull(user);
        assertNotNull(user.getId());
        assertEquals("testuser", user.getUsername());
        assertTrue(passwordEncoder.matches("123456", user.getPassword()));
        assertEquals("测试用户", user.getRealName());
        assertEquals("13800138000", user.getPhone());
        assertEquals("test@example.com", user.getEmail());
    }

    @Test
    public void testGetUserByUsername() {
        // 准备测试数据
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("getuser");
        userDTO.setPassword("123456");
        userDTO.setRealName("获取用户");
        userService.createUser(userDTO);

        // 调用方法
        User user = userService.getUserByUsername("getuser");

        // 验证结果
        assertNotNull(user);
        assertEquals("getuser", user.getUsername());
        assertEquals("获取用户", user.getRealName());
    }

    @Test
    public void testCheckUsernameExists() {
        // 准备测试数据
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("checkuser");
        userDTO.setPassword("123456");
        userService.createUser(userDTO);

        // 调用方法
        boolean exists = userService.checkUsernameExists("checkuser");

        // 验证结果
        assertTrue(exists);

        // 测试不存在的用户名
        boolean notExists = userService.checkUsernameExists("notexists");
        assertFalse(notExists);
    }

    @Test
    public void testGetUserPage() {
        // 准备测试数据
        for (int i = 1; i <= 15; i++) {
            UserDTO userDTO = new UserDTO();
            userDTO.setUsername("pageuser" + i);
            userDTO.setPassword("123456");
            userDTO.setRealName("分页用户" + i);
            userService.createUser(userDTO);
        }

        // 调用方法
        Pageable pageable = PageRequest.of(0, 10);
        UserDTO queryDto = new UserDTO();
        Page<UserVO> result = userService.getUserPage(pageable, queryDto);

        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.getNumber());
        assertEquals(10, result.getSize());
        assertTrue(result.getTotalElements() >= 15);
        assertEquals(10, result.getContent().size());
    }

    @Test
    public void testRegister() {
        // 准备测试数据
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername("registeruser");
        registerDTO.setPassword("123456");
        registerDTO.setRealName("注册用户");
        registerDTO.setPhone("13800138001");
        registerDTO.setEmail("register@example.com");

        // 调用方法
        UserVO userVO = userService.register(registerDTO);

        // 验证结果
        assertNotNull(userVO);
        assertEquals("registeruser", userVO.getUsername());
        assertEquals("注册用户", userVO.getRealName());
        assertEquals("13800138001", userVO.getPhone());
        assertEquals("register@example.com", userVO.getEmail());
    }

    @Test
    public void testUpdateUserStatus() {
        // 准备测试数据
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("statususer");
        userDTO.setPassword("123456");
        userDTO.setStatus(1); // 启用状态
        User user = userService.createUser(userDTO);

        // 调用方法 - 禁用用户
        userService.updateUserStatus(user.getId(), 0);

        // 验证结果
        User updatedUser = userService.getUserByUsername("statususer");
        assertNotNull(updatedUser);
        assertEquals(0, updatedUser.getStatus());
    }

    @Test
    public void testDeleteUser() {
        // 准备测试数据
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("deleteuser");
        userDTO.setPassword("123456");
        User user = userService.createUser(userDTO);

        // 调用方法
        userService.deleteUser(user.getId());

        // 验证结果
        User deletedUser = userService.getUserByUsername("deleteuser");
        assertNull(deletedUser);
    }
}