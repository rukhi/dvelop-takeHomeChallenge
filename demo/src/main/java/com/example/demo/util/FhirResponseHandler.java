package com.example.demo.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import ca.uhn.fhir.context.FhirContext;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

/**
 * Utility-Klasse zur Erstellung standardisierter FHIR-konformer Antworten.
 * Diese Klasse stellt Methoden zur Fehler- und Erfolgsmeldung bereit, die 
 * als FHIR `OperationOutcome` zurückgegeben werden. 
 *
 * Diese Klasse ist als reine Utility-Klasse konzipiert und kann nicht instanziiert werden.
 */
public final class FhirResponseHandler {

    private static final Logger logger = Logger.getLogger(FhirResponseHandler.class.getName());
    private static final FhirContext fhirContext = FhirContext.forR4(); // Singleton Instanz von FhirContext
    
    private FhirResponseHandler() {
        throw new UnsupportedOperationException("Utility class - cannot be instantiated.");
    }

     /**
     * Behandelt unerwartete Ausnahmen, indem sie protokolliert und eine standardisierte 
     * FHIR `OperationOutcome`-Fehlermeldung zurückgegeben wird.
     *
     * @param e       Die aufgetretene Ausnahme.
     * @param message Die benutzerdefinierte Fehlermeldung, die zurückgegeben werden soll.
     * @return Eine ResponseEntity mit einer FHIR `OperationOutcome`-Fehlermeldung und HTTP-Status 500 (Interner Serverfehler).
     */
    public static ResponseEntity<String> handleException(Exception e, String message) {
        logger.log(Level.SEVERE, message, e);
        return createOperationOutcomeResponse(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Behandelt Validierungsfehler und gibt in der Regel HTTP 400 (Bad Request) zurück.
     *
     * @param message Die Fehlermeldung zur Validierung.
     * @return Eine ResponseEntity mit einer FHIR `OperationOutcome`-Validierungsfehlermeldung und HTTP-Status 400 (Bad Request).
     */
    public static ResponseEntity<String> handleValidationFailure(String message) {
        logger.warning(message);
        return createOperationOutcomeResponse(message, HttpStatus.BAD_REQUEST);
    }

    /**
     * Erstellt eine FHIR-konforme Erfolgsmeldung in Form eines `OperationOutcome`.
     *
     * @param message Die Erfolgsmeldung, die zurückgegeben werden soll.
     * @return Eine ResponseEntity mit einer FHIR `OperationOutcome`-Erfolgsmeldung und HTTP-Status 201 (Created).
     */
    public static ResponseEntity<String> createSuccessResponse(String message) {
        OperationOutcome outcome = new OperationOutcome();
        outcome.addIssue()
            .setSeverity(IssueSeverity.INFORMATION)
            .setCode(IssueType.INFORMATIONAL)
            .setDiagnostics(message);

        String outcomeJson = fhirContext.newJsonParser().encodeResourceToString(outcome);
        return ResponseEntity.status(HttpStatus.CREATED).body(outcomeJson);
    }

    /**
     * Helper Methode zur Erstellung einer FHIR `OperationOutcome`-Antwort mit einem bestimmten HTTP-Status.
     *
     * @param message Die Fehlermeldung.
     * @param status  Der HTTP-Statuscode, der zurückgegeben werden soll.
     * @return Eine ResponseEntity mit einer FHIR `OperationOutcome`-Fehlermeldung und dem angegebenen Statuscode.
     */
    private static ResponseEntity<String> createOperationOutcomeResponse(String message, HttpStatus status) {
        OperationOutcome outcome = new OperationOutcome();
        outcome.addIssue()
            .setSeverity(IssueSeverity.ERROR)
            .setCode(IssueType.EXCEPTION)
            .setDiagnostics(message);

        String outcomeJson = fhirContext.newJsonParser().encodeResourceToString(outcome);
        return ResponseEntity.status(status).body(outcomeJson);
    }
}
