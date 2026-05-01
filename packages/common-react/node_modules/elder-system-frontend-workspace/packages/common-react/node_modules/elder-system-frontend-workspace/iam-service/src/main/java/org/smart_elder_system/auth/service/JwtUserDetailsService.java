package org.smart_elder_system.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JwtUserDetailsService implements UserDetailsService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.users.admin.password}")
    private String adminPassword;

    @Value("${app.users.user.password}")
    private String userPassword;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if ("admin".equals(username)) {
            return User.withUsername("admin")
                    .password(passwordEncoder.encode(adminPassword))
                    .authorities(getAuthority())
                    .build();
        } else if ("user".equals(username)) {
            return User.withUsername("user")
                    .password(passwordEncoder.encode(userPassword))
                    .authorities(getAuthority())
                    .build();
        } else {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
    }

    private List<SimpleGrantedAuthority> getAuthority() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }
}