package org.smart_elder_system.iam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@ComponentScan(
        basePackages = {
                "org.smart_elder_system.iam",
                "org.smart_elder_system.user",
                "org.smart_elder_system.common.jwt.config"
        },
        nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = {
                        "org\\.smart_elder_system\\..*Application",
                        "org\\.smart_elder_system\\.user\\.config\\.SecurityConfig"
                }
        )
)
@EnableFeignClients(basePackages = {"org.smart_elder_system.user.feign"})
@EnableJpaRepositories(basePackages = {"org.smart_elder_system.user.repository"})
@EntityScan(basePackages = {"org.smart_elder_system.user.po"})
public class IamServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IamServiceApplication.class, args);
    }
}
