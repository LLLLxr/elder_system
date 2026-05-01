package org.smart_elder_system.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.smart_elder_system.user.constant.UserConstants;
import org.smart_elder_system.user.dto.PermissionDTO;
import org.smart_elder_system.user.po.PermissionPo;
import org.smart_elder_system.user.repository.PermissionRepository;
import org.smart_elder_system.user.repository.RoleRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private PermissionService permissionService;

    @Test
    void shouldCreatePermissionUsingExplicitFieldMapping() {
        PermissionDTO request = new PermissionDTO();
        request.setPermissionName("权限查看");
        request.setPermissionCode("permission:view");
        request.setDescription("查看权限");

        when(permissionRepository.existsByPermissionNameAndDeleteFlag("权限查看", UserConstants.DELETE_FLAG_NORMAL))
                .thenReturn(false);
        when(permissionRepository.existsByPermissionCodeAndDeleteFlag("permission:view", UserConstants.DELETE_FLAG_NORMAL))
                .thenReturn(false);
        when(permissionRepository.save(any())).thenAnswer(invocation -> {
            PermissionPo po = invocation.getArgument(0);
            po.setId(1L);
            return po;
        });

        permissionService.createPermission(request);
    }

    @Test
    void shouldUpdateOnlyAllowedFields() {
        PermissionPo existingPermission = new PermissionPo();
        existingPermission.setId(1L);
        existingPermission.setPermissionName("旧权限");
        existingPermission.setPermissionCode("permission:old");
        existingPermission.setDescription("旧描述");
        existingPermission.setStatus(UserConstants.STATUS_NORMAL);
        existingPermission.setDeleteFlag(UserConstants.DELETE_FLAG_NORMAL);

        PermissionDTO request = new PermissionDTO();
        request.setId(1L);
        request.setPermissionName("新权限");
        request.setPermissionCode("permission:new");
        request.setDescription("新描述");
        request.setStatus(UserConstants.STATUS_DISABLED);

        when(permissionRepository.findById(1L)).thenReturn(Optional.of(existingPermission));
        when(permissionRepository.existsByPermissionNameAndDeleteFlagAndIdNot(
                "新权限", UserConstants.DELETE_FLAG_NORMAL, 1L)).thenReturn(false);
        when(permissionRepository.existsByPermissionCodeAndDeleteFlagAndIdNot(
                "permission:new", UserConstants.DELETE_FLAG_NORMAL, 1L)).thenReturn(false);
        when(permissionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        permissionService.updatePermission(request);

        assertEquals("新权限", existingPermission.getPermissionName());
        assertEquals("permission:new", existingPermission.getPermissionCode());
        assertEquals("新描述", existingPermission.getDescription());
        assertEquals(UserConstants.STATUS_DISABLED, existingPermission.getStatus());
        assertEquals(UserConstants.DELETE_FLAG_NORMAL, existingPermission.getDeleteFlag());
    }
}
