package org.smart_elder_system.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.smart_elder_system.user.constant.UserConstants;
import org.smart_elder_system.user.dto.UserDTO;
import org.smart_elder_system.user.po.UserPo;
import org.smart_elder_system.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldCreateUserUsingExplicitFieldMapping() {
        UserDTO request = new UserDTO();
        request.setUsername("user01");
        request.setPassword("plain-pass");
        request.setRealName("张三");
        request.setEmail("user01@test.com");
        request.setPhone("13800000000");
        request.setAvatar("avatar.png");
        request.setIdCard("11010519491231002X");

        when(passwordEncoder.encode("plain-pass")).thenReturn("encoded-pass");
        when(userRepository.save(any())).thenAnswer(invocation -> {
            UserPo po = invocation.getArgument(0);
            po.setId(1L);
            return po;
        });
        when(roleService.getByRoleCode(UserConstants.ROLE_USER)).thenReturn(null);

        UserPo result = userService.createUser(request);

        assertEquals(1L, result.getId());
        assertEquals("user01", result.getUsername());
        assertEquals("encoded-pass", result.getPassword());
        assertEquals("11010519491231002X", result.getIdCard());
        assertEquals(UserConstants.STATUS_NORMAL, result.getStatus());
    }

    @Test
    void shouldUpdateOnlyAllowedFields() {
        UserPo existingUser = new UserPo();
        existingUser.setId(1L);
        existingUser.setUsername("locked-name");
        existingUser.setPassword("encoded-pass");
        existingUser.setIdCardVerified(UserConstants.ID_CARD_VERIFIED);
        existingUser.setFaceVerified(UserConstants.FACE_VERIFIED);
        existingUser.setDeleteFlag(UserConstants.DELETE_FLAG_NORMAL);
        existingUser.setStatus(UserConstants.STATUS_NORMAL);
        existingUser.setRealName("旧姓名");
        existingUser.setEmail("old@test.com");
        existingUser.setPhone("13800000000");
        existingUser.setAvatar("old.png");
        existingUser.setIdCard("11010519491231002X");

        UserDTO request = new UserDTO();
        request.setId(1L);
        request.setRealName("新姓名");
        request.setEmail("new@test.com");
        request.setPhone("13900000000");
        request.setAvatar("new.png");
        request.setIdCard("110105194912310038");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UserPo result = userService.updateUser(request);

        assertEquals("locked-name", result.getUsername());
        assertEquals("encoded-pass", result.getPassword());
        assertEquals(UserConstants.ID_CARD_VERIFIED, result.getIdCardVerified());
        assertEquals(UserConstants.FACE_VERIFIED, result.getFaceVerified());
        assertEquals(UserConstants.DELETE_FLAG_NORMAL, result.getDeleteFlag());
        assertEquals("新姓名", result.getRealName());
        assertEquals("new@test.com", result.getEmail());
        assertEquals("13900000000", result.getPhone());
        assertEquals("new.png", result.getAvatar());
        assertEquals("110105194912310038", result.getIdCard());
    }

    @Test
    void shouldMapUserToViewWithoutPasswordLeak() {
        UserPo user = new UserPo();
        user.setId(1L);
        user.setUsername("user01");
        user.setPassword("encoded-pass");
        user.setRealName("张三");
        user.setEmail("user01@test.com");
        user.setPhone("13800000000");
        user.setAvatar("avatar.png");
        user.setIdCard("11010519491231002X");
        user.setIdCardVerified(UserConstants.ID_CARD_VERIFIED);
        user.setFaceVerified(UserConstants.FACE_VERIFIED);
        user.setDeleteFlag(UserConstants.DELETE_FLAG_NORMAL);
        user.setStatus(UserConstants.STATUS_NORMAL);

        when(userRepository.findByUsername("user01")).thenReturn(Optional.of(user));
        when(roleService.getRolesByUserId(1L)).thenReturn(java.util.List.of("USER"));

        org.smart_elder_system.user.vo.User result = userService.findByUsername("user01");

        assertEquals("user01", result.getUsername());
        assertEquals("11010519491231002X", result.getIdCardNo());
        assertEquals(java.util.List.of("USER"), result.getRoles());
        assertNull(result.getPassword());
    }
}
