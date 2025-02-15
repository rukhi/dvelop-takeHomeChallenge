package com.example.demo.service;

import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Attachment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dto.DocumentDTO;

@Service // Kennzeichnet diese Klasse als Spring Service-Komponente
public class DocumentReferenceService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentReferenceService.class);
    private static final String KDL_SYSTEM = "http://dvmd.de/fhir/CodeSystem/kdl";

    // Hier injizieren wir unsere "echte" Bean für die FHIR-Validierung
    private final FhirValidatorService fhirValidatorService;

    @Autowired
    public DocumentReferenceService(FhirValidatorService fhirValidatorService) {
        this.fhirValidatorService = fhirValidatorService;
    }

    // Überprüft, ob das DocumentReference-Objekt gültige Daten enthält
    public boolean isValid(DocumentReference documentReference) {
        if (documentReference == null) {
            logger.warn("DocumentReference is null");
            return false;
        }

        if (!documentReference.hasType() || documentReference.getType().getCoding().isEmpty()) {
            logger.warn("DocumentReference type is missing");
            return false;
        }

        if (!documentReference.hasSubject() || !documentReference.getSubject().hasReference()) {
            logger.warn("DocumentReference subject (patientId) is missing");
            return false;
        }

        if (!documentReference.hasContext() || !documentReference.getContext().hasEncounter()
                || !documentReference.getContext().getEncounterFirstRep().hasReference()) {
            logger.warn("DocumentReference encounter (visitNumber) is missing");
            return false;
        }

        if (!documentReference.hasContent() || documentReference.getContent().isEmpty()) {
            logger.warn("DocumentReference content is missing");
            return false;
        }

        Attachment attachment = documentReference.getContentFirstRep().getAttachment();
        if (!attachment.hasData()) {
            logger.warn("DocumentReference attachment data is missing");
            return false;
        }

        if (!attachment.hasCreation()) {
            logger.warn("DocumentReference creation date is missing");
            return false;
        }

        return true;
    }

    // Konvertiert ein gültiges DocumentReference-Objekt in ein DocumentDTO-Objekt
    public DocumentDTO processDocumentReference(DocumentReference documentReference) {
        if (!isValid(documentReference)) {
            logger.error("Invalid DocumentReference data");
            throw new IllegalArgumentException("Invalid DocumentReference data");
        }

        // Erweiterte Validierung: gegen ISiK-Strukturdefinition (hier
        // ISiKDokumentenMetadaten)
        // Wird die Validierung nicht bestanden, wirft FhirInstanceValidatorUtil eine
        // Exception.
        fhirValidatorService.validateIsikDocumentReference(documentReference);

        String kdlCode = extractKdlCode(documentReference);
        String patientId = extractPatientId(documentReference);
        String visitNumber = extractVisitNumber(documentReference);
        String dateCreated = extractDateCreated(documentReference);
        String contentB64 = extractContentBase64(documentReference);

        logger.info(
                "Extracted DocumentReference Data: KDL Code: {}, Patient ID: {}, Visit Number: {}, Date Created: {}",
                kdlCode, patientId, visitNumber, dateCreated);

        logger.debug("Document content extracted (size: {} bytes)", contentB64.length());

        // logger.debug("Content (Base64): " + (contentB64.length() > 50 ?
        // contentB64.substring(0, 50) + "..." : contentB64)); // Auskommentiert, da der
        // Anfragekörper sensibele Daten enhalten könnte, nur zum Testen reinkommentieren

        return new DocumentDTO(kdlCode, patientId, visitNumber, dateCreated, contentB64);
    }

    // Extrahiert den KDL-Code aus der Coding-Liste des DocumentReference-Typs
    private String extractKdlCode(DocumentReference documentReference) {
        for (Coding coding : documentReference.getType().getCoding()) {
            if (isValidKdlCodeSystem(coding)) {
                String code = coding.getCode();
                logger.debug("Extracted KDL Code: {}", code);

                // Prüfe zusätzlich Mitgliedschaft im CodeSystem/ValueSet
                fhirValidatorService.ensureKdlCodeIsValid(code);

                // Wenn die Validierung erfolgreich war, wird der Code zurückgegeben
                return code;
            }
        }
        logger.warn("No valid KDL Code found");
        return null;
    }

    // Validiert ob das CodeSystem KDL entspricht
    private boolean isValidKdlCodeSystem(Coding coding) {
        return coding != null && coding.hasSystem() && coding.hasCode() && KDL_SYSTEM.equals(coding.getSystem());
    }

    // Extrahiert die Patient-ID aus der Subject-Referenz
    private String extractPatientId(DocumentReference documentReference) {
        String patientId = extractIdFromReference(documentReference.getSubject().getReference());
        logger.debug("Extracted Patient ID: {}", patientId);
        return patientId;
    }

    // Extrahiert die Besuchsnummer aus der Encounter-Referenz
    private String extractVisitNumber(DocumentReference documentReference) {
        String visitNumber = extractIdFromReference(
                documentReference.getContext().getEncounterFirstRep().getReference());
        logger.debug("Extracted Visit Number: {}", visitNumber);
        return visitNumber;
    }

    // Extrahiert das Erstellungsdatum aus dem Content-Attachment
    private String extractDateCreated(DocumentReference documentReference) {
        String rawDateCreated = documentReference.getContentFirstRep().getAttachment().getCreationElement()
                .asStringValue();
        String formattedDate = convertDate(rawDateCreated);
        logger.debug("Formatted Creation Date: {}", formattedDate);
        return formattedDate;
    }

    // Extrahiert den Base64-kodierten Inhalt des Dokumentes
    private String extractContentBase64(DocumentReference documentReference) {
        byte[] contentBytes = documentReference.getContentFirstRep().getAttachment().getData();
        if (contentBytes == null || contentBytes.length == 0) {
            logger.warn("Document content is empty or null.");
            return "";
        }
        return Base64.getEncoder().encodeToString(contentBytes);
    }

    // Extrahiert die ID aus einer generischen FHIR-Referenz (entfernt alles vor dem
    // /)
    private String extractIdFromReference(String reference) {
        if (reference.contains("/")) {
            String id = reference.substring(reference.lastIndexOf("/") + 1);
            logger.debug("Extracted ID from Reference: {} -> {}", reference, id);
            return id;
        }
        return reference;
    }

    // Konvertiert das Erstellungsdatum von ISO-8601 in dd.MM.yyyy
    // (Datumsformat der API war in der Aufgabenstellung nicht angegeben,
    // konvertieren fühlte sich aber sinnvoll an ;) )
    private String convertDate(String creationDate) {
        if (creationDate == null || !creationDate.matches("\\d{4}-\\d{2}-\\d{2}.*")) {
            logger.warn("Invalid creation date format: {}", creationDate);
            throw new IllegalArgumentException("Invalid creation date format. Expected format: YYYY-MM-DD");
        }

        String[] dateComponents = creationDate.split("T")[0].split("-");
        String day = dateComponents[2];
        String month = dateComponents[1];
        String year = dateComponents[0];

        return day + "." + month + "." + year;
    }

}
