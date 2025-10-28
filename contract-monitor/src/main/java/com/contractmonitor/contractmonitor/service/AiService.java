package com.contractmonitor.contractmonitor.service;

import com.contractmonitor.contractmonitor.entity.BreakingChange;
import com.contractmonitor.contractmonitor.entity.ApiSpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiService {

    private final ChatClient.Builder chatClientBuilder;

    /**
     * Generate a backward-compatible suggestion for a breaking change
     */
    public String generateSuggestion(BreakingChange change) {
        log.info("Generating AI suggestion for breaking change: {}", change.getId());

        String promptText = """
                A breaking change was detected in a microservices API:
                
                Change Type: {changeType}
                Location: {path}
                Description: {description}
                Old Version: {oldVersion}
                New Version: {newVersion}
                
                As an expert software architect, suggest a backward-compatible alternative approach.
                Provide specific, actionable steps that allow gradual migration without breaking existing clients.
                Format your response as a numbered list with clear implementation steps.
                """;

        PromptTemplate promptTemplate = new PromptTemplate(promptText);
        Prompt prompt = promptTemplate.create(Map.of(
                "changeType", change.getChangeType().toString(),
                "path", change.getPath(),
                "description", change.getDescription(),
                "oldVersion", change.getOldVersion(),
                "newVersion", change.getNewVersion()
        ));

        ChatClient chatClient = chatClientBuilder.build();
        String response = chatClient.prompt(prompt).call().content();

        log.info("AI suggestion generated successfully");
        return response;
    }

    /**
     * Predict which services might be impacted by this breaking change
     */
    public String predictImpact(BreakingChange change, List<ApiSpec> allSpecs) {
        log.info("Predicting impact for breaking change: {}", change.getId());

        StringBuilder servicesInfo = new StringBuilder();
        for (ApiSpec spec : allSpecs) {
            servicesInfo.append(String.format("- %s (version %s)\n", 
                spec.getServiceName(), spec.getVersion()));
        }

        String promptText = """
                A breaking change occurred in the {serviceName} microservice:
                
                Change Type: {changeType}
                Location: {path}
                Description: {description}
                
                Available microservices in the system:
                {servicesInfo}
                
                Based on common microservice communication patterns and the nature of this change:
                1. Predict which services are most likely to be affected
                2. Assign a confidence score (0-100%) for each potentially affected service
                3. Explain why each service might be impacted
                
                Format your response as:
                Service Name | Confidence | Reason
                """;

        PromptTemplate promptTemplate = new PromptTemplate(promptText);
        Prompt prompt = promptTemplate.create(Map.of(
                "serviceName", change.getServiceName(),
                "changeType", change.getChangeType().toString(),
                "path", change.getPath(),
                "description", change.getDescription(),
                "servicesInfo", servicesInfo.toString()
        ));

        ChatClient chatClient = chatClientBuilder.build();
        String response = chatClient.prompt(prompt).call().content();

        log.info("Impact prediction generated successfully");
        return response;
    }

    /**
     * Generate a plain English explanation of the breaking change
     */
    public String explainInPlainEnglish(BreakingChange change) {
        log.info("Generating plain English explanation for breaking change: {}", change.getId());

        String promptText = """
                Translate this technical API breaking change into plain English that a non-technical 
                product manager or stakeholder can understand:
                
                Change Type: {changeType}
                Location: {path}
                Technical Description: {description}
                Old Version: {oldVersion}
                New Version: {newVersion}
                
                Provide:
                1. A simple one-sentence summary (no jargon)
                2. What this means for users/clients of the API
                3. The business impact
                
                Keep it concise and avoid technical terminology.
                """;

        PromptTemplate promptTemplate = new PromptTemplate(promptText);
        Prompt prompt = promptTemplate.create(Map.of(
                "changeType", change.getChangeType().toString(),
                "path", change.getPath(),
                "description", change.getDescription(),
                "oldVersion", change.getOldVersion(),
                "newVersion", change.getNewVersion()
        ));

        ChatClient chatClient = chatClientBuilder.build();
        String response = chatClient.prompt(prompt).call().content();

        log.info("Plain English explanation generated successfully");
        return response;
    }
}