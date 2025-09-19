package com.k8sloverskorea.testspringbootapp.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "Test Spring Boot App API",
                version = "v1",
                description = "테스트용 스프링 부트 애플리케이션의 공개 API 문서",
                contact = @Contact(name = "K8s Lovers Korea", url = "https://github.com/K8sLoversKorea"),
                license = @License(name = "MIT")
        ),
        servers = {
                @Server(url = "/", description = "기본 서버")
        }
)
@Configuration
public class OpenApiConfig {
}
