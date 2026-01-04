package com.contractmonitor.contractmonitor.service;

import com.contractmonitor.contractmonitor.entity.AnalysisReport;
import com.contractmonitor.contractmonitor.entity.ApiSpec;
import com.contractmonitor.contractmonitor.entity.BreakingChange;
import com.contractmonitor.contractmonitor.repository.AnalysisReportRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AnalysisService {
    
    private final ApiSpecService apiSpecService;
    private final BreakingChangeService breakingChangeService;
    private final AnalysisReportRepository analysisReportRepository;
    private final AiService aiService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Analyze a service by comparing its latest spec with the previous one
     */
    public AnalysisReport analyzeService(String serviceName) {
        log.info("Starting analysis for {}", serviceName);
        
        // Fetch and save current spec
        ApiSpec currentSpec = apiSpecService.fetchAndSaveSpec(serviceName);
        
        Optional<ApiSpec> comparisonSpecOpt = apiSpecService.getComparisonSpec(serviceName);

        if (comparisonSpecOpt.isEmpty()) {
            log.info("Not enough history to analyze {}. Creating initial baseline.", serviceName);
            // Auto-set first spec as baseline
            apiSpecService.setBaseline(serviceName, currentSpec.getId());
            return createBaselineReport(serviceName, currentSpec);
        }

        ApiSpec comparisonSpec = comparisonSpecOpt.get();

        // Log which spec we're comparing against
        if (apiSpecService.getBaselineSpec(serviceName).isPresent()) {
            log.info("Comparing {} against BASELINE version: {}", serviceName, comparisonSpec.getVersion());
        } else {
            log.info("No baseline set. Comparing {} against PREVIOUS version: {}", serviceName,
                    comparisonSpec.getVersion());
        }
        
        // Compare specs and detect breaking changes
        List<BreakingChange> breakingChanges = compareSpecs(comparisonSpec, currentSpec);

        // Generate AI insights for each breaking change
        if (!breakingChanges.isEmpty()) {
            log.info("Generating AI insights for {} breaking changes", breakingChanges.size());

            // Get all latest specs for impact prediction
            List<ApiSpec> allSpecs = apiSpecService.getAllLatestSpecs();

            for (BreakingChange change : breakingChanges) {
                try {
                    log.info("Generating AI insights for: {}", change.getDescription());

                    // Generate backward-compatible suggestion
                    String suggestion = aiService.generateSuggestion(change);
                    change.setAiSuggestion(suggestion);

                    // Predict impact on other services
                    String impact = aiService.predictImpact(change, allSpecs);
                    change.setPredictedImpact(impact);

                    // Generate plain English explanation
                    String explanation = aiService.explainInPlainEnglish(change);
                    change.setPlainEnglishExplanation(explanation);

                    log.info("AI insights generated successfully for change ID: {}", change.getId());

                } catch (Exception e) {
                    log.error("Failed to generate AI insights for change: {}. Error: {}",
                            change.getDescription(), e.getMessage());
                    // Continue with other changes even if AI fails for one
                    change.setAiSuggestion("AI analysis unavailable: " + e.getMessage());
                    change.setPredictedImpact("AI analysis unavailable: " + e.getMessage());
                    change.setPlainEnglishExplanation("AI analysis unavailable: " + e.getMessage());
                }
            }

            // Save breaking changes (now with AI insights)
            breakingChangeService.saveAll(breakingChanges);
        }
        
        // Create and save analysis report
        AnalysisReport report = createAnalysisReport(serviceName, comparisonSpec, currentSpec, breakingChanges);
        analysisReportRepository.save(report);
        
        log.info("Analysis complete for {}. Found {} breaking changes. Compared against: {}",
                serviceName,
                breakingChanges.size(),
                apiSpecService.getBaselineSpec(serviceName).isPresent() ? "BASELINE" : "previous version");

        return report;
    }
    
    /**
     * Compare two API specs and detect breaking changes
     */
    private List<BreakingChange> compareSpecs(ApiSpec oldSpec, ApiSpec newSpec) {
        List<BreakingChange> changes = new ArrayList<>();
        
        try {
            JsonNode oldJson = objectMapper.readTree(oldSpec.getSpecContent());
            JsonNode newJson = objectMapper.readTree(newSpec.getSpecContent());
            
            // Compare paths (endpoints)
            changes.addAll(comparePaths(oldJson, newJson, oldSpec, newSpec));
            
            // Compare schemas (data models)
            changes.addAll(compareSchemas(oldJson, newJson, oldSpec, newSpec));
            
        } catch (Exception e) {
            log.error("Error comparing specs: {}", e.getMessage(), e);
        }
        
        return changes;
    }
    
    /**
     * Compare API paths (endpoints)
     */
    private List<BreakingChange> comparePaths(JsonNode oldJson, JsonNode newJson, 
                                               ApiSpec oldSpec, ApiSpec newSpec) {
        List<BreakingChange> changes = new ArrayList<>();
        
        JsonNode oldPaths = oldJson.path("paths");
        JsonNode newPaths = newJson.path("paths");
        
        if (oldPaths.isMissingNode() || newPaths.isMissingNode()) {
            return changes;
        }
        
        // Check for removed endpoints
        Iterator<String> fieldNames = oldPaths.fieldNames();
        while (fieldNames.hasNext()) {
            String path = fieldNames.next();
            
            if (!newPaths.has(path)) {
                // Endpoint was removed - BREAKING CHANGE
                BreakingChange change = new BreakingChange();
                change.setServiceName(oldSpec.getServiceName());
                change.setChangeType(BreakingChange.ChangeType.ENDPOINT_REMOVED);
                change.setPath(path);
                change.setDescription("Endpoint '" + path + "' was removed");
                change.setOldVersion(oldSpec.getVersion());
                change.setNewVersion(newSpec.getVersion());
                changes.add(change);
                
                log.warn("BREAKING: Endpoint removed: {}", path);
            } else {
                // Endpoint exists, check HTTP methods
                changes.addAll(compareMethods(path, oldPaths.get(path), newPaths.get(path), 
                                             oldSpec, newSpec));
            }
        }
        
        return changes;
    }
    
    /**
     * Compare HTTP methods within an endpoint
     */
    private List<BreakingChange> compareMethods(String path, JsonNode oldEndpoint, 
                                                 JsonNode newEndpoint, 
                                                 ApiSpec oldSpec, ApiSpec newSpec) {
        List<BreakingChange> changes = new ArrayList<>();
        
        String[] methods = {"get", "post", "put", "delete", "patch"};
        
        for (String method : methods) {
            if (oldEndpoint.has(method) && !newEndpoint.has(method)) {
                // HTTP method was removed - BREAKING CHANGE
                BreakingChange change = new BreakingChange();
                change.setServiceName(oldSpec.getServiceName());
                change.setChangeType(BreakingChange.ChangeType.METHOD_REMOVED);
                change.setPath(path);
                change.setDescription("HTTP method '" + method.toUpperCase() + "' removed from '" + path + "'");
                change.setOldVersion(oldSpec.getVersion());
                change.setNewVersion(newSpec.getVersion());
                changes.add(change);
                
                log.warn("BREAKING: Method removed: {} {}", method.toUpperCase(), path);
            }
        }
        
        return changes;
    }
    
    /**
     * Compare schemas (data models)
     */
    private List<BreakingChange> compareSchemas(JsonNode oldJson, JsonNode newJson, 
                                                 ApiSpec oldSpec, ApiSpec newSpec) {
        List<BreakingChange> changes = new ArrayList<>();
        
        JsonNode oldSchemas = oldJson.path("components").path("schemas");
        JsonNode newSchemas = newJson.path("components").path("schemas");
        
        if (oldSchemas.isMissingNode() || newSchemas.isMissingNode()) {
            return changes;
        }
        
        // Check each schema
        Iterator<String> schemaNames = oldSchemas.fieldNames();
        while (schemaNames.hasNext()) {
            String schemaName = schemaNames.next();
            
            if (!newSchemas.has(schemaName)) {
                // Schema was removed - BREAKING CHANGE
                BreakingChange change = new BreakingChange();
                change.setServiceName(oldSpec.getServiceName());
                change.setChangeType(BreakingChange.ChangeType.SCHEMA_REMOVED);
                change.setPath("/components/schemas/" + schemaName);
                change.setDescription("Schema '" + schemaName + "' was removed");
                change.setOldVersion(oldSpec.getVersion());
                change.setNewVersion(newSpec.getVersion());
                changes.add(change);
                
                log.warn("BREAKING: Schema removed: {}", schemaName);
            } else {
                // Schema exists, check properties
                changes.addAll(compareSchemaProperties(schemaName, 
                                                      oldSchemas.get(schemaName), 
                                                      newSchemas.get(schemaName), 
                                                      oldSpec, newSpec));
            }
        }
        
        return changes;
    }
    
    /**
     * Compare properties within a schema
     */
    private List<BreakingChange> compareSchemaProperties(String schemaName, 
                                                         JsonNode oldSchema, 
                                                         JsonNode newSchema, 
                                                         ApiSpec oldSpec, 
                                                         ApiSpec newSpec) {
        List<BreakingChange> changes = new ArrayList<>();
        
        JsonNode oldProperties = oldSchema.path("properties");
        JsonNode newProperties = newSchema.path("properties");
        
        if (oldProperties.isMissingNode()) {
            return changes;
        }
        
        // Check for removed or changed properties
        Iterator<String> propertyNames = oldProperties.fieldNames();
        while (propertyNames.hasNext()) {
            String propertyName = propertyNames.next();
            
            if (!newProperties.has(propertyName)) {
                // Property was removed - BREAKING CHANGE
                BreakingChange change = new BreakingChange();
                change.setServiceName(oldSpec.getServiceName());
                change.setChangeType(BreakingChange.ChangeType.FIELD_REMOVED);
                change.setPath("/components/schemas/" + schemaName);
                change.setDescription("Field '" + propertyName + "' removed from '" + schemaName + "' schema");
                change.setOldVersion(oldSpec.getVersion());
                change.setNewVersion(newSpec.getVersion());
                changes.add(change);
                
                log.warn("BREAKING: Field removed: {}.{}", schemaName, propertyName);
            } else {
                // Check if type changed
                JsonNode oldProperty = oldProperties.get(propertyName);
                JsonNode newProperty = newProperties.get(propertyName);
                
                String oldType = oldProperty.path("type").asText("");
                String newType = newProperty.path("type").asText("");
                
                if (!oldType.isEmpty() && !newType.isEmpty() && !oldType.equals(newType)) {
                    // Type changed - BREAKING CHANGE
                    BreakingChange change = new BreakingChange();
                    change.setServiceName(oldSpec.getServiceName());
                    change.setChangeType(BreakingChange.ChangeType.TYPE_CHANGED);
                    change.setPath("/components/schemas/" + schemaName);
                    change.setDescription("Field '" + propertyName + "' type changed from '" + 
                                        oldType + "' to '" + newType + "' in '" + schemaName + "' schema");
                    change.setOldVersion(oldSpec.getVersion());
                    change.setNewVersion(newSpec.getVersion());
                    changes.add(change);
                    
                    log.warn("BREAKING: Type changed: {}.{} from {} to {}", 
                            schemaName, propertyName, oldType, newType);
                }
            }
        }
        
        return changes;
    }
    
    /**
     * Create analysis report
     */
    private AnalysisReport createAnalysisReport(String serviceName, ApiSpec oldSpec, 
                                               ApiSpec newSpec, List<BreakingChange> changes) {
        AnalysisReport report = new AnalysisReport();
        report.setServiceName(serviceName);
        report.setBreakingChangesCount(changes.size());
        report.setNonBreakingChangesCount(0);  // TODO: Implement non-breaking change detection
        
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("Analysis of %s: %s → %s\n", 
                                    serviceName, oldSpec.getVersion(), newSpec.getVersion()));
        summary.append(String.format("Breaking changes: %d\n", changes.size()));
        
        if (!changes.isEmpty()) {
            summary.append("\nBreaking changes detected:\n");
            changes.forEach(change -> 
                summary.append(String.format("- %s at %s: %s\n", 
                                           change.getChangeType(), 
                                           change.getPath(), 
                                           change.getDescription()))
            );
        }
        
        report.setSummary(summary.toString());
        return report;
    }
    
    /**
     * Create baseline report for first analysis
     */
    private AnalysisReport createBaselineReport(String serviceName, ApiSpec spec) {
        AnalysisReport report = new AnalysisReport();
        report.setServiceName(serviceName);
        report.setBreakingChangesCount(0);
        report.setNonBreakingChangesCount(0);
        report.setSummary("Baseline spec saved for " + serviceName + ". Version: " + spec.getVersion());
        
        return analysisReportRepository.save(report);
    }
    
    /**
     * Get latest analysis report for a service
     */
    public AnalysisReport getLatestReport(String serviceName) {
        return analysisReportRepository.findTopByServiceNameOrderByAnalyzedAtDesc(serviceName)
                .orElse(null);
    }
    
    /**
     * Get all analysis reports for a service
     */
    public List<AnalysisReport> getReportHistory(String serviceName) {
        return analysisReportRepository.findByServiceNameOrderByAnalyzedAtDesc(serviceName);
    }
}