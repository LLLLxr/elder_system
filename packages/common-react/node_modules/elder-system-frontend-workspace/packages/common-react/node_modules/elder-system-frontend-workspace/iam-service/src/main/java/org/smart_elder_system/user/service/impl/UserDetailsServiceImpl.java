package org.smart_elder_system.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.smart_elder_system.user.constant.UserConstants;
import org.smart_elder_system.user.po.PermissionPo;
import org.smart_elder_system.user.po.UserPo;
import org.smart_elder_system.user.repository.UserRepository;
import org.smart_elder_system.user.service.PermissionService;
import org.smart_elder_system.user.service.RoleService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户详情服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PermissionService permissionService;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查询用户信息
        UserPo user = userRepository.findByUsernameAndStatusAndDeleteFlag(
                username, UserConstants.STATUS_NORMAL, UserConstants.DELETE_FLAG_NORMAL);

        if (user == null) {
            log.warn("用户不存在: {}", username);
            throw new UsernameNotFoundException("用户不存在或已被禁用");
        }

        // 查询用户角色
        List<String> roles = roleService.getRolesByUserId(user.getId());

        // 查询用户权限
        List<PermissionPo> permissions = permissionService.getPermissionsByUserId(user.getId());
        List<String> permissionCodes = permissions.stream()
                .map(PermissionPo::getPermissionCode)
                .collect(Collectors.toList());

        // 构建权限列表
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        
        // 添加角色权限，角色需要添加ROLE_前缀
        roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
        
        // 添加操作权限
        permissionCodes.forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));

        // 返回UserDetails
        return new CustomUserDetails(user, roles, permissionCodes, authorities);
    }
    
    /**
     * 自定义用户详情
     */
    public static class CustomUserDetails implements UserDetails {
        private final UserPo user;
        private final List<String> roles;
        private final List<String> permissions;
        private final List<SimpleGrantedAuthority> authorities;

        public CustomUserDetails(UserPo user, List<String> roles, List<String> permissions,
                                List<SimpleGrantedAuthority> authorities) {
            this.user = user;
            this.roles = roles;
            this.permissions = permissions;
            this.authorities = authorities;
        }

        @Override
        public String getUsername() {
            return user.getUsername();
        }

        @Override
        public String getPassword() {
            return user.getPassword();
        }

        public UserPo getUser() {
            return user;
        }

        public List<String> getRoles() {
            return roles;
        }

        public List<String> getPermissions() {
            return permissions;
        }

        @Override
        public List<SimpleGrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return UserConstants.STATUS_NORMAL.equals(user.getStatus());
        }
    }
}