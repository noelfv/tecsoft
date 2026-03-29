package com.bbva.orchestrator;

import com.bbva.gateway.dialogcontrol.DialogControlHandler;
import com.bbva.gateway.dialogcontrol.GrpcDialogControlService;
import com.bbva.gateway.dialogcontrol.IDialogControl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@ConfigurationPropertiesScan
@ComponentScan(basePackages = {"com.bbva.orchlib", "com.bbva.orchestrator", "com.bbva.gateway"},
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {DialogControlHandler.class, IDialogControl.class, GrpcDialogControlService.class}))
public class OrchestratorApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrchestratorApplication.class, args);
    }
}