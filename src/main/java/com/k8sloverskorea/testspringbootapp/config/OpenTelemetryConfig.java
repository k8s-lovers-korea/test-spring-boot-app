package com.k8sloverskorea.testspringbootapp.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenTelemetryConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(OpenTelemetryConfig.class);
    
    @Value("${spring.application.name:test-spring-boot-app}")
    private String serviceName;
    
    @Value("${otel.exporter.otlp.endpoint:http://localhost:4317}")
    private String otlpEndpoint;
    
    @Bean
    public OpenTelemetry openTelemetry() {
        logger.info("Configuring OpenTelemetry with service name: {} and OTLP endpoint: {}", 
                   serviceName, otlpEndpoint);
        
        Resource resource = Resource.getDefault()
                .merge(Resource.create(
                        Attributes.of(
                            AttributeKey.stringKey("service.name"), serviceName,
                            AttributeKey.stringKey("service.version"), "1.0.0"
                        )));
        
        try {
            OtlpGrpcSpanExporter otlpExporter = OtlpGrpcSpanExporter.builder()
                    .setEndpoint(otlpEndpoint)
                    .build();
            
            SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                    .addSpanProcessor(BatchSpanProcessor.builder(otlpExporter).build())
                    .setResource(resource)
                    .build();
            
            OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder()
                    .setTracerProvider(tracerProvider)
                    .build();
            
            logger.info("OpenTelemetry configured successfully");
            return openTelemetry;
            
        } catch (Exception e) {
            logger.error("Failed to configure OpenTelemetry with OTLP, falling back to no-op", e);
            // Fallback to no-op implementation if exporter is not available
            return OpenTelemetry.noop();
        }
    }
    
    @Bean
    public Tracer tracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer(serviceName, "1.0.0");
    }
}