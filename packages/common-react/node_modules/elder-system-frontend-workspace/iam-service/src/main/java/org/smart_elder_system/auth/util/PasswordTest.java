package org.smart_elder_system.auth.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordTest {
    public static void main(String[] args) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        
        // 测试密码
        String password = "admin123";
        String hashedPassword = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKITi";
        
        // 验证密码
        boolean matches = passwordEncoder.matches(password, hashedPassword);
        System.out.println("Password '" + password + "' matches hash: " + matches);
        
        // 生成新的哈希值
        String newHash = passwordEncoder.encode(password);
        System.out.println("New hash for '" + password + "': " + newHash);
    }
}