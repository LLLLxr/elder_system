package org.smart_elder_system.user.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.smart_elder_system.user.dto.UserDTO;
import org.smart_elder_system.user.po.UserPo;
import org.smart_elder_system.user.service.UserService;
import org.smart_elder_system.user.vo.User;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void shouldReturnUserViewAfterCreateUser() {
        UserDTO request = new UserDTO();
        request.setUsername("user01");

        UserPo savedUser = new UserPo();
        savedUser.setId(1L);

        User userView = new User();
        userView.setId(1L);
        userView.setUsername("user01");
        userView.setPassword(null);

        when(userService.createUser(request)).thenReturn(savedUser);
        when(userService.getUserDetail(1L)).thenReturn(userView);

        ResponseEntity<User> response = userController.createUser(request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("user01", response.getBody().getUsername());
        assertNull(response.getBody().getPassword());
    }

    @Test
    void shouldReturnUserViewAfterUpdateUser() {
        UserDTO request = new UserDTO();
        request.setRealName("新姓名");

        UserPo savedUser = new UserPo();
        savedUser.setId(2L);

        User userView = new User();
        userView.setId(2L);
        userView.setRealName("新姓名");
        userView.setPassword(null);

        when(userService.updateUser(request)).thenReturn(savedUser);
        when(userService.getUserDetail(2L)).thenReturn(userView);

        ResponseEntity<User> response = userController.updateUser(2L, request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(2L, request.getId());
        assertEquals("新姓名", response.getBody().getRealName());
        assertNull(response.getBody().getPassword());
    }
}
