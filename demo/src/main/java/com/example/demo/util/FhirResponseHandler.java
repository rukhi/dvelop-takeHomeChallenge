package com.example.demo.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import ca.uhn.fhir.context.FhirContext;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Utility class to handle FHIR-compliant responses, including error handling and success messages.
 * Provides standardized OperationOutcome responses for errors and success cases.
 */
@Component
public class FhirResponseHandler {

    private static final Logger logger = Logger.getLogger(FhirResponseHandler.class.getName());
    private final FhirContext fhirContext;

    /**
     * Constructor injection for FhirContext to ensure singleton usage.
     *
     * @param fhirContext Singleton instance of FhirContext
     */
    @Autowired 
    public FhirResponseHandler(FhirContext fhirContext) {
        this.fhirContext = fhirContext;
    }

    /**
     * Handles unexpected exceptions by logging and returning a standardized FHIR OperationOutcome error response.
     *
     * @param e       The exception that occurred.
     * @param message The custom error message to return.
     * @return A ResponseEntity containing a FHIR OperationOutcome error message with HTTP 500 status.
     */
    public ResponseEntity<String> handleException(Exception e, String message) {
        logger.log(Level.SEVERE, message, e);
        return createOperationOutcomeResponse(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles validation failures, typically returning HTTP 400 Bad Request.
     *
     * @param message The validation error message.
     * @return A ResponseEntity containing a FHIR OperationOutcome validation error with HTTP 400 status.
     */
    public ResponseEntity<String> handleValidationFailure(String message) {
        logger.warning(message);
        return createOperationOutcomeResponse(message, HttpStatus.BAD_REQUEST);
    }

    /**
     * Generates a FHIR-compliant success response using OperationOutcome.
     *
     * @param message The success message to return.
     * @return A ResponseEntity containing a FHIR OperationOutcome success message with HTTP 201 status.
     */
    public ResponseEntity<String> createSuccessResponse(String message) {
        OperationOutcome outcome = new OperationOutcome();
        outcome.addIssue()
            .setSeverity(IssueSeverity.INFORMATION)
            .setCode(IssueType.INFORMATIONAL)
            .setDiagnostics(message);

        String outcomeJson = fhirContext.newJsonParser().encodeResourceToString(outcome);
        return ResponseEntity.status(HttpStatus.CREATED).body(outcomeJson);
    }

    /**
     * Helper method to generate a FHIR OperationOutcome response with a given HTTP status.
     *
     * @param message The error message.
     * @param status  The HTTP status code to return.
     * @return A ResponseEntity containing a FHIR OperationOutcome error message with the specified status.
     */
    private ResponseEntity<String> createOperationOutcomeResponse(String message, HttpStatus status) {
        OperationOutcome outcome = new OperationOutcome();
        outcome.addIssue()
            .setSeverity(IssueSeverity.ERROR)
            .setCode(IssueType.EXCEPTION)
            .setDiagnostics(message);

        String outcomeJson = fhirContext.newJsonParser().encodeResourceToString(outcome);
        return ResponseEntity.status(status).body(outcomeJson);
    }
}
