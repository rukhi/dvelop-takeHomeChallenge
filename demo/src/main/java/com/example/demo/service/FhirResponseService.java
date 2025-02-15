package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import ca.uhn.fhir.context.FhirContext;

/**
 * Service-Klasse zur Erstellung standardisierter FHIR-konformer Antworten.
 * Diese Klasse stellt Methoden zur Fehler- und Erfolgsmeldung bereit, die 
 * als FHIR `OperationOutcome` zurückgegeben werden.
 *
 * Da wir hier eine Service-Klasse verwenden, injizieren wir den FhirContext
 * ins Feld, anstatt einen statischen Context zu verwenden.
 */
@Service
public class FhirResponseService {

    private static final Logger logger = LoggerFactory.getLogger(FhirResponseService.class);

    private final FhirContext fhirContext;

    /**
     * Konstruktor-Injection. Spring injiziert hier den (globalen) FhirContext
     * aus Deiner FhirConfig-Klasse.
     */
    public FhirResponseService(FhirContext fhirContext) {
        this.fhirContext = fhirContext;
    }

    /**
     * Behandelt unerwartete Ausnahmen, indem sie protokolliert und eine
     * standardisierte FHIR `OperationOutcome`-Fehlermeldung zurückgegeben wird.
     *
     * @param e       Die aufgetretene Ausnahme.
     * @param message Die benutzerdefinierte Fehlermeldung.
     * @return Eine ResponseEntity mit `OperationOutcome` und HTTP-Status 500.
     */
    public ResponseEntity<String> handleException(Exception e, String message) {
        logger.error("Exception occurred - HTTP 500: {}", message, e);
        return createOperationOutcomeResponse(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Behandelt Validierungsfehler und gibt einen HTTP 400 (Bad Request) zurück.
     *
     * @param message Die Fehlermeldung zur Validierung.
     * @return Eine ResponseEntity mit `OperationOutcome` und HTTP-Status 400.
     */
    public ResponseEntity<String> handleValidationFailure(String message) {
        logger.warn("Validation failed - HTTP 400: {}", message);
        return createOperationOutcomeResponse(message, HttpStatus.BAD_REQUEST);
    }

    /**
     * Erstellt eine FHIR-konforme Erfolgsmeldung in Form eines `OperationOutcome`.
     *
     * @param message Die Erfolgsmeldung.
     * @return Eine ResponseEntity mit `OperationOutcome` und HTTP-Status 201 (Created).
     */
    public ResponseEntity<String> createSuccessResponse(String message) {
        OperationOutcome outcome = new OperationOutcome();
        outcome.addIssue()
            .setSeverity(IssueSeverity.INFORMATION)
            .setCode(IssueType.INFORMATIONAL)
            .setDiagnostics(message);

        String outcomeJson = fhirContext.newJsonParser().encodeResourceToString(outcome);
        logger.info("FHIR response created successfully - HTTP 201: {}", message);
        return ResponseEntity.status(HttpStatus.CREATED).body(outcomeJson);
    }

    /**
     * Hilfsmethode zur Erstellung einer FHIR `OperationOutcome`-Antwort mit 
     * bestimmtem HTTP-Status.
     *
     * @param message Die Fehlermeldung.
     * @param status  Der HTTP-Statuscode.
     * @return Eine ResponseEntity mit `OperationOutcome` und dem Status.
     */
    private ResponseEntity<String> createOperationOutcomeResponse(String message, HttpStatus status) {
        OperationOutcome outcome = new OperationOutcome();
        outcome.addIssue()
            .setSeverity(IssueSeverity.ERROR)
            .setCode(IssueType.EXCEPTION)
            .setDiagnostics(message);

        String outcomeJson = fhirContext.newJsonParser().encodeResourceToString(outcome);
        logger.debug("Created OperationOutcome response: status={}, message={}", status, message);
        return ResponseEntity.status(status).body(outcomeJson);
    }
}
