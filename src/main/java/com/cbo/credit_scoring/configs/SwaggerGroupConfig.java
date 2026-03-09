package com.cbo.credit_scoring.configs;

import io.swagger.v3.oas.models.OpenAPI;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerGroupConfig {

    @Bean
    public GroupedOpenApi merchandiseApi() {
        return GroupedOpenApi.builder()
                .group("merchandise-turnover")
                .pathsToMatch("/api/v1/merchandise/**")
                .packagesToScan("com.cbo.credit_scoring.controllers")
                .build();
    }

    @Bean
    public GroupedOpenApi preShipmentApi() {
        return GroupedOpenApi.builder()
                .group("pre-shipment")
                .pathsToMatch("/api/v1/pre-shipment/**")
                .packagesToScan("com.cbo.credit_scoring.controllers")
                .build();
    }

    @Bean
    public GroupedOpenApi odTurnoverApi() {
        return GroupedOpenApi.builder()
                .group("od-turnover")
                .pathsToMatch("/api/v1/od/**")
                .packagesToScan("com.cbo.credit_scoring.controllers")
                .build();
    }

    @Bean
    public GroupedOpenApi odSwingApi() {
        return GroupedOpenApi.builder()
                .group("od-swing")
                .pathsToMatch("/api/v1/od-swing/**")
                .packagesToScan("com.cbo.credit_scoring.controllers")
                .build();
    }

    @Bean
    public GroupedOpenApi accountStatementApi() {
        return GroupedOpenApi.builder()
                .group("account-statement")
                .pathsToMatch("/api/v1/account-statement/**")
                .packagesToScan("com.cbo.credit_scoring.controllers")
                .build();
    }

    @Bean
    public GroupedOpenApi financialStatementApi() {
        return GroupedOpenApi.builder()
                .group("financial-statement")
                .pathsToMatch("/api/v1/financial-statements/**")
                .packagesToScan("com.cbo.credit_scoring.controllers")
                .build();
    }

    @Bean
    public GroupedOpenApi createCreditInformationApi () {
        return GroupedOpenApi.builder()
                .group("credit-information")
                .pathsToMatch("/api/v1/credit-information/**")
                .packagesToScan("com.cbo.credit_scoring.controllers")
                .build();
    }
    @Bean
    public GroupedOpenApi createCollateralApi () {
        return GroupedOpenApi.builder()
                .group("collateral")
                .pathsToMatch("/api/v1/collateral/**")
                .packagesToScan("com.cbo.credit_scoring.controllers")
                .build();
    }
}