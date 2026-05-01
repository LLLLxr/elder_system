package org.smart_elder_system.ops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;

@SpringBootApplication
@ComponentScan(
        basePackages = {
                "org.smart_elder_system.ops",
                "org.smart_elder_system.business",
                "org.smart_elder_system.resourcescheduling",
                "org.smart_elder_system.safetyemergency",
                "org.smart_elder_system.billing"
        },
        nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "org\\.smart_elder_system\\..*Application"
        )
)
@EnableFeignClients(basePackages = {"org.smart_elder_system.business.feign"})
public class OpsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpsServiceApplication.class, args);
    }
}
