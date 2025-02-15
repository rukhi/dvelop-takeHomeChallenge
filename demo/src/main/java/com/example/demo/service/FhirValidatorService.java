package com.example.demo.service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import ca.uhn.fhir.validation.SingleValidationMessage;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.ValueSet;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.common.hapi.validation.support.PrePopulatedValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.CommonCodeSystemsTerminologyService;
import org.hl7.fhir.common.hapi.validation.support.SnapshotGeneratingValidationSupport;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;

/**
 * FhirValidatorService ist eine Spring-Service-Komponente, die dazu dient,
 * FHIR-Ressourcen gegen lokal gespeicherte Validierungsartefakte (ValueSets,
 * CodeSystems und StructureDefinitions) zu überprüfen.
 * 
 * Diese Validierungen basieren auf HAPI FHIR (FHIR R4) und ermöglichen eine
 * erweiterte Terminology-Validierung im lokalen Entwicklungsumfeld.
 */
@Service
public class FhirValidatorService {

    private static final Logger logger = Logger.getLogger(FhirValidatorService.class.getName());

    private final FhirContext fhirContext;
    private final IParser jsonParser;

    @Autowired
    public FhirValidatorService(FhirContext fhirContext, IParser jsonParser) {
        this.fhirContext = fhirContext;
        this.jsonParser = jsonParser;
    }

    /**
     * Liest eine Datei aus dem Classpath und gibt deren Inhalt als String zurück.
     *
     * @param resourcePath Pfad zur Resource (z. B. "fhir/codesystems/codesystem-kdl-2021.json")
     * @return Den Inhalt der Datei als String.
     */
    private String readResourceFile(String resourcePath) {
        try (InputStream is = new ClassPathResource(resourcePath).getInputStream()) {
            return StreamUtils.copyToString(is, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.severe("Error reading resource file: " + resourcePath);
            throw new RuntimeException("Error reading resource file: " + resourcePath, e);
        }
    }

    /**
     * Validiert eine Ressource gegen ein lokales ValueSet.
     *
     * @param resource     Die zu validierende FHIR-Ressource.
     * @param valueSetPath Der Classpath-Pfad zum ValueSet (z. B. "fhir/valuesets/valueset-kdl-2021.json").
     * @throws IllegalArgumentException falls die Validierung fehlschlägt.
     */
    public void validateResourceAgainstValueSet(IBaseResource resource, String valueSetPath) {
        String valueSetContent = readResourceFile(valueSetPath);
        ValueSet valueSet = (ValueSet) jsonParser.parseResource(valueSetContent);

        PrePopulatedValidationSupport prePopulatedSupport = new PrePopulatedValidationSupport(fhirContext);
        prePopulatedSupport.addValueSet(valueSet);

        ValidationSupportChain supportChain = new ValidationSupportChain(
                new DefaultProfileValidationSupport(fhirContext),
                new SnapshotGeneratingValidationSupport(fhirContext),
                prePopulatedSupport,
                new CommonCodeSystemsTerminologyService(fhirContext),
                new InMemoryTerminologyServerValidationSupport(fhirContext)
        );

        FhirInstanceValidator instanceValidator = new FhirInstanceValidator(supportChain);
        FhirValidator validator = fhirContext.newValidator();
        validator.registerValidatorModule(instanceValidator);

        ValidationResult result = validator.validateWithResult(resource);
        if (!result.isSuccessful()) {
            StringBuilder errorMessages = new StringBuilder();
            for (SingleValidationMessage message : result.getMessages()) {
                errorMessages.append(message.getSeverity())
                        .append(" - ")
                        .append(message.getLocationString())
                        .append(" : ")
                        .append(message.getMessage())
                        .append("\n");
            }
            throw new IllegalArgumentException("FHIR validation failed against ValueSet:\n" + errorMessages.toString());
        }
    }

    /**
     * Validiert eine Ressource gegen ein lokales StructureDefinition.
     *
     * @param resource                Die zu validierende FHIR-Ressource.
     * @param structureDefinitionPath Der Classpath-Pfad zur StructureDefinition (z. B. "fhir/profiles/ISiKDokumentenMetadaten.json").
     * @throws IllegalArgumentException falls die Validierung fehlschlägt.
     */
    public void validateResourceAgainstStructureDefinition(IBaseResource resource, String structureDefinitionPath) {
        String sdContent = readResourceFile(structureDefinitionPath);
        StructureDefinition sd = (StructureDefinition) jsonParser.parseResource(sdContent);

        PrePopulatedValidationSupport prePopulatedSupport = new PrePopulatedValidationSupport(fhirContext);
        prePopulatedSupport.addStructureDefinition(sd);

        ValidationSupportChain supportChain = new ValidationSupportChain(
                new DefaultProfileValidationSupport(fhirContext),
                new SnapshotGeneratingValidationSupport(fhirContext),
                prePopulatedSupport,
                new CommonCodeSystemsTerminologyService(fhirContext),
                new InMemoryTerminologyServerValidationSupport(fhirContext)
        );

        FhirInstanceValidator instanceValidator = new FhirInstanceValidator(supportChain);
        FhirValidator validator = fhirContext.newValidator();
        validator.registerValidatorModule(instanceValidator);

        ValidationResult result = validator.validateWithResult(resource);
        if (!result.isSuccessful()) {
            StringBuilder errorMessages = new StringBuilder();
            for (SingleValidationMessage message : result.getMessages()) {
                errorMessages.append(message.getSeverity())
                        .append(" - ")
                        .append(message.getLocationString())
                        .append(" : ")
                        .append(message.getMessage())
                        .append("\n");
            }
            throw new IllegalArgumentException("FHIR validation failed against StructureDefinition:\n" + errorMessages);
        }
    }

    /**
     * Prüft, ob ein gegebener KDL-Code sowohl im lokalen CodeSystem als auch im ValueSet vorhanden ist.
     *
     * @param code Der zu überprüfende KDL-Code.
     * @throws IllegalArgumentException wenn der Code nicht im CodeSystem oder ValueSet gefunden wird.
     */
    public void ensureKdlCodeIsValid(String code) {
        // (1) Prüfe im CodeSystem
        String csContent = readResourceFile("/fhir/codesystems/codesystem-kdl-2021.json");
        CodeSystem kdlCodeSystem = (CodeSystem) jsonParser.parseResource(csContent);

        logger.info("KDL CodeSystem URL: " + kdlCodeSystem.getUrl());
        logger.info("KDL CodeSystem Version: " + kdlCodeSystem.getVersion());

        boolean foundInCS = checkCodeInCodeSystem(kdlCodeSystem, code);
        if (!foundInCS) {
            throw new IllegalArgumentException("KDL-Code '" + code + "' ist nicht im CodeSystem definiert!");
        }

        // (2) Prüfe im ValueSet
        String vsContent = readResourceFile("/fhir/valuesets/valueset-kdl-2021.json");
        ValueSet kdlValueSet = (ValueSet) jsonParser.parseResource(vsContent);

        logger.info("KDL ValueSet URL: " + kdlValueSet.getUrl());
        logger.info("KDL ValueSet Version: " + kdlValueSet.getVersion());

        boolean foundInVS = checkCodeInValueSet(kdlValueSet, code);
        if (!foundInVS) {
            throw new IllegalArgumentException("KDL-Code '" + code + "' ist nicht im ValueSet enthalten!");
        }

        logger.info("KDL-Code '" + code + "' ist gültig (in CodeSystem und ValueSet vorhanden).");
    }

    /**
     * Durchsucht das übergebene CodeSystem (inklusive aller Unterkonzepte) nach dem Code.
     *
     * @param cs   Das CodeSystem, das durchsucht werden soll.
     * @param code Der zu suchende Code.
     * @return true, wenn der Code gefunden wurde; andernfalls false.
     */
    private boolean checkCodeInCodeSystem(CodeSystem cs, String code) {
        if (cs.hasConcept()) {
            for (CodeSystem.ConceptDefinitionComponent concept : cs.getConcept()) {
                if (code.equals(concept.getCode())) {
                    return true;
                }
                if (concept.hasConcept() && checkCodeInConceptList(concept.getConcept(), code)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Rekursive Hilfsmethode zum Durchsuchen einer Liste von Konzepten.
     *
     * @param concepts Die Liste von Konzepten.
     * @param code     Der zu suchende Code.
     * @return true, wenn der Code gefunden wurde; andernfalls false.
     */
    private boolean checkCodeInConceptList(List<CodeSystem.ConceptDefinitionComponent> concepts, String code) {
        for (CodeSystem.ConceptDefinitionComponent concept : concepts) {
            if (code.equals(concept.getCode())) {
                return true;
            }
            if (concept.hasConcept() && checkCodeInConceptList(concept.getConcept(), code)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Prüft, ob der übergebene Code in der Expansion des ValueSets enthalten ist.
     * 
     * Falls eine Expansion vorhanden ist, wird diese durchsucht.
     *
     * @param vs   Das ValueSet, das geprüft werden soll.
     * @param code Der zu suchende Code.
     * @return true, wenn der Code in der Expansion gefunden wurde; andernfalls false.
     */
    private boolean checkCodeInValueSet(ValueSet vs, String code) {
        if (vs.hasExpansion() && vs.getExpansion().hasContains()) {
            for (ValueSet.ValueSetExpansionContainsComponent contains : vs.getExpansion().getContains()) {
                if (code.equals(contains.getCode())) {
                    return true;
                }
            }
        }
        // Falls keine Expansion vorhanden ist, könnte alternativ über compose/include geprüft werden.
        return false;
    }

    /**
     * Validiert eine Ressource gegen die ISiKDokumentenMetadaten StructureDefinition.
     */
    public void validateIsikDocumentReference(IBaseResource resource) {
        validateResourceAgainstStructureDefinition(resource, "/fhir/profiles/ISiKDokumentenMetadaten.json");
    }

    /**
     * Validiert eine Ressource gegen die ISiKPatient StructureDefinition.
     */
    public void validateIsikPatient(IBaseResource resource) {
        validateResourceAgainstStructureDefinition(resource, "/fhir/profiles/ISiKPatient.json");
    }
}
