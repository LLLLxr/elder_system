package org.smart_elder_system.carecore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
@ComponentScan(
        basePackages = {
                "org.smart_elder_system.carecore",
                "org.smart_elder_system.careorchestration",
                "org.smart_elder_system.admission",
                "org.smart_elder_system.contract",
                "org.smart_elder_system.caredelivery",
                "org.smart_elder_system.health",
                "org.smart_elder_system.quality"
        },
        nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "org\\.smart_elder_system\\..*Application"
        )
)
@EnableFeignClients(basePackages = {"org.smart_elder_system.careorchestration.feign"})
@EnableJpaRepositories(basePackages = {
        "org.smart_elder_system.careorchestration.repository",
        "org.smart_elder_system.admission.repository",
        "org.smart_elder_system.contract.repository",
        "org.smart_elder_system.caredelivery.repository",
        "org.smart_elder_system.health.repository",
        "org.smart_elder_system.quality.repository"
})
@EntityScan(basePackages = {
        "org.smart_elder_system.careorchestration.po",
        "org.smart_elder_system.admission.po",
        "org.smart_elder_system.contract.po",
        "org.smart_elder_system.caredelivery.po",
        "org.smart_elder_system.health.po",
        "org.smart_elder_system.quality.po"
})
public class CareCoreServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CareCoreServiceApplication.class, args);
    }
}
