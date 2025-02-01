package com.example.demo.controller;

import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ca.uhn.fhir.parser.IParser;

import com.example.demo.service.ProprietaryApiService;
import com.example.demo.service.DocumentReferenceService;
import com.example.demo.service.PatientService;
import com.example.demo.dto.DocumentDTO;
import com.example.demo.dto.PersonDTO;

import com.example.demo.util.FhirResponseHandler;
import com.example.demo.util.JsonSchemaUtil;
import com.example.demo.util.FhirMessages;

import java.util.logging.Logger;

/**
 * REST Controller für FHIR-Anfragen
 */
@RestController // Kennzeichnet diese Klasse als Spring REST Controller
@RequestMapping("/fhir") // Basis-URL für alle Endpunkte in dieser Klasse
public class FhirController {

    private static final Logger logger = Logger.getLogger(FhirController.class.getName());

    private final ProprietaryApiService proprietaryApiService;
    private final PatientService patientService;
    private final DocumentReferenceService documentReferenceService;
    private final IParser jsonParser;

    /**
     * Konstruktor-Injektion für bessere Testbarkeit
     * Spring Boot injiziert automatisch die Abhängigkeiten.
     *
     * @param fhirContext           Singleton FHIR-Context für JSON-Parsing, spart
     *                              Ressourcen
     * @param proprietaryApiService Service für Kommunikation mit proprietärer API
     * @param patientService        Service für Patientenlogik
     */
    @Autowired
    public FhirController(IParser jsonParser,
            ProprietaryApiService proprietaryApiService,
            PatientService patientService,
            DocumentReferenceService documentReferenceService) {
        this.jsonParser = jsonParser;
        this.proprietaryApiService = proprietaryApiService;
        this.patientService = patientService;
        this.documentReferenceService = documentReferenceService;
    }

    /**
     * Erstellt einen neuen Patienten in der proprietären API.
     *
     * @param patientResource Die Patienten-Ressource als JSON-String
     * @return Eine HTTP-Antwort, die den Erfolg oder Misserfolg der Anfrage anzeigt
     * 
     *         Beispiel: POST http://localhost:8080/fhir/Person
     */
    @PostMapping("/Person") // Mapped HTTP POST-Anfragen auf diesen Endpunkt
    public ResponseEntity<String> createPatient(@RequestBody String patientResource) {
        logger.info("Received request to create a new patient. ");

        try {
            // Parsen des Patient-Ressource-Strings in ein Patient-Objekt
            Patient patient = jsonParser.parseResource(Patient.class, patientResource);

            // ->neu // Validierung der Patient-Ressource
            if (!patientService.isValid(patient)) {
                return FhirResponseHandler.handleValidationFailure(FhirMessages.INVALID_PATIENT_RESOURCE);
            }

            // Verarbeitung der Patient-Ressource mit DTO
            PersonDTO personDTO = patientService.processPatient(patient);

            // Sendet die Patientendaten an die proprietäre API
            boolean apiSuccess = proprietaryApiService.sendPatientData(personDTO);

            if (apiSuccess) {
                // Loggt und gibt eine Erfolgsantwort zurück, wenn die API-Anfrage erfolgreich
                // war
                logger.info("Patient data sent successfully.");
                return FhirResponseHandler.createSuccessResponse(FhirMessages.PATIENT_CREATED);
            } else {
                // Loggt und gibt eine Fehlerantwort zurück, wenn die API-Anfrage fehlschlägt
                return FhirResponseHandler.handleException(
                        new RuntimeException("API failure"),
                        FhirMessages.API_FAILURE);
            }
        } catch (Exception e) {
            // Loggt und gibt eine Fehlerantwort zurück, wenn eine Ausnahme auftritt
            return FhirResponseHandler.handleException(e, FhirMessages.INTERNAL_SERVER_ERROR);
            // *Bianca Rech:
            // *mein innerer Monk würde hier gerne genauere Fehlerbehandlung einbauen,
            // *aber das wurde ja in den Anforderungen explizit nicht gefordert ;)
            // *ich lasse die Blöcke trotzdem mal einzeln hier drin, falls man das mal machen möchte
        }
    }

    /**
     * Erstellt ein neues Dokument in der proprietären API.
     */
    @PostMapping("/DocumentReference")
    public ResponseEntity<String> createDocumentReference(@RequestBody String documentResource) {
        logger.info("Received request to create a new document.");
        try {
            DocumentReference documentReference = jsonParser.parseResource(DocumentReference.class, documentResource);
            if (!documentReferenceService.isValid(documentReference)) {
                return FhirResponseHandler.handleValidationFailure(FhirMessages.INVALID_DOCUMENT_RESOURCE);
            }
            DocumentDTO documentDTO = documentReferenceService.processDocumentReference(documentReference);
            boolean apiSuccess = proprietaryApiService.sendDocumentData(documentDTO);
            if (apiSuccess) {
                logger.info("DocumentReference data sent successfully.");
                return FhirResponseHandler.createSuccessResponse(FhirMessages.DOCUMENT_CREATED);
            } else {
                return FhirResponseHandler.handleException(new RuntimeException("API failure"),
                        FhirMessages.API_FAILURE);
            }
        } catch (Exception e) {
            return FhirResponseHandler.handleException(e, FhirMessages.INTERNAL_SERVER_ERROR);
             // *Bianca Rech:
            // *mein innerer Monk würde hier gerne genauere Fehlerbehandlung einbauen,
            // *aber das wurde ja in den Anforderungen explizit nicht gefordert ;)
            // *ich lasse die Blöcke trotzdem mal einzeln hier drin, falls man das mal machen möchte
        }
    }

    /**
     * Gibt das JSON-Schema für die Person-Ressource zurück.
     * 
     * @return
     * "quick & dirty" in anbetracht der Zeit ;) fühlt sich aber sinnvoll an
     *  Beispiel GET http://localhost:8080/fhir/Person/schema
     */
    
    @GetMapping("/Person/schema")
    public ResponseEntity<String> getPersonSchema() {
        return JsonSchemaUtil.getSchemaResponse("person");
    }

    /**
     * Gibt das JSON-Schema für die DocumentReference-Ressource zurück.
     * "quick & dirty" in anbetracht der Zeit ;) fühlt sich aber sinnvoll an
     * Beispiel: GET http://localhost:8080/fhir/DocumentReference/schema
     */
    @GetMapping("/DocumentReference/schema")
    public ResponseEntity<String> getDocumentSchema() {
        return JsonSchemaUtil.getSchemaResponse("documentReference");
    }
}