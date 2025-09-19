package com.k8sloverskorea.testspringbootapp.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.ResourceAttributes;
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
    
    @Value("${otel.exporter.jaeger.endpoint:http://localhost:14250}")
    private String jaegerEndpoint;
    
    @Bean
    public OpenTelemetry openTelemetry() {
        logger.info("Configuring OpenTelemetry with service name: {} and Jaeger endpoint: {}", 
                   serviceName, jaegerEndpoint);
        
        Resource resource = Resource.getDefault()
                .merge(Resource.create(
                        Attributes.of(ResourceAttributes.SERVICE_NAME, serviceName,
                                     ResourceAttributes.SERVICE_VERSION, "1.0.0")));
        
        try {
            JaegerGrpcSpanExporter jaegerExporter = JaegerGrpcSpanExporter.builder()
                    .setEndpoint(jaegerEndpoint)
                    .build();
            
            SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                    .addSpanProcessor(BatchSpanProcessor.builder(jaegerExporter).build())
                    .setResource(resource)
                    .build();
            
            OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder()
                    .setTracerProvider(tracerProvider)
                    .build();
            
            logger.info("OpenTelemetry configured successfully");
            return openTelemetry;
            
        } catch (Exception e) {
            logger.error("Failed to configure OpenTelemetry with Jaeger, falling back to no-op", e);
            // Fallback to no-op implementation if Jaeger is not available
            return OpenTelemetry.noop();
        }
    }
    
    @Bean
    public Tracer tracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer(serviceName, "1.0.0");
    }
}