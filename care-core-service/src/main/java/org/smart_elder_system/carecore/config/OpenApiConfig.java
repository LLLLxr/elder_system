package org.smart_elder_system.carecore.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("智慧养老护理院核心服务 API")
                        .version("1.0.0")
                        .description("提供入院管理、护理执行、健康管理、质量管理等核心业务接口")
                        .contact(new Contact()
                                .name("API Support")
                                .email("support@smart-elder-system.org")))
                .servers(List.of(
                        new Server().url("http://localhost:8081").description("本地开发环境"),
                        new Server().url("https://api.smart-elder-system.org").description("生产环境")
                ));
    }
}
