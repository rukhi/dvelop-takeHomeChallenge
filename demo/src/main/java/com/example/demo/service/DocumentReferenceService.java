package com.example.demo.service;

import java.util.Base64;
import java.util.logging.Logger;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Attachment;
import org.springframework.stereotype.Service;

import com.example.demo.dto.DocumentDTO;

@Service // Kennzeichnet diese Klasse als Spring Service-Komponente
public class DocumentReferenceService {
    
    private static final Logger logger = Logger.getLogger(DocumentReferenceService.class.getName());
    private static final String KDL_SYSTEM = "http://dvmd.de/fhir/CodeSystem/kdl";

    // Überprüft, ob das DocumentReference-Objekt gültige Daten enthält
    public boolean isValid(DocumentReference documentReference) {
        // Validierung der Daten im DocumentReference-Objekt
        // -----------------------------------------------//
        // Als Beispiel mal so rudimentär umgesetzt
        // eine Validierung gegen die Implementation Guides wäre hier sinnvoll,
        // in der Kürze der 24h für mich aber nicht sinnvoll umsetzbar ;) 
        // falls am Ende doch Zeit ist, komme ich hier nochmal darauf zurück 
        //
        // würde es dann auch auslagern in eine eigene Klasse !
        if (documentReference == null) {
            logger.warning("DocumentReference is null");
            return false;
        }

        if (!documentReference.hasType() || documentReference.getType().getCoding().isEmpty()) {
            logger.warning("DocumentReference type is missing");
            return false;
        }

        if (!documentReference.hasSubject() || !documentReference.getSubject().hasReference()) {
            logger.warning("DocumentReference subject (patientId) is missing");
            return false;
        }

        if (!documentReference.hasContext() || !documentReference.getContext().hasEncounter() || !documentReference.getContext().getEncounterFirstRep().hasReference()) {
            logger.warning("DocumentReference encounter (visitNumber) is missing");
            return false;
        }

        if (!documentReference.hasContent() || documentReference.getContent().isEmpty()) {
            logger.warning("DocumentReference content is missing");
            return false;
        }

        Attachment attachment = documentReference.getContentFirstRep().getAttachment();
        if (!attachment.hasData()) {
            logger.warning("DocumentReference attachment data is missing");
            return false;
        }
        
        if (!attachment.hasCreation()) {
            logger.warning("DocumentReference creation date is missing");
            return false;
        }
        
        return true;
    }
    
    // Konvertiert ein gültiges DocumentReference-Objekt in ein DocumentDTO-Objekt
    public DocumentDTO processDocumentReference(DocumentReference documentReference) {
        if (!isValid(documentReference)) {
            throw new IllegalArgumentException("Invalid DocumentReference data");
        }

        String kdlCode = extractKdlCode(documentReference);
        String patientId = extractPatientId(documentReference);
        String visitNumber = extractVisitNumber(documentReference);
        String dateCreated = extractDateCreated(documentReference);
        String contentB64 = extractContentBase64(documentReference);
        
        logger.info("Extracted DocumentReference Data:");
        logger.info("KDL Code: " + kdlCode);
        logger.info("Patient ID: " + patientId);
        logger.info("Visit Number: " + visitNumber);
        logger.info("Date Created: " + dateCreated);
        //logger.info("Content (Base64): " + (contentB64.length() > 50 ? contentB64.substring(0, 50) + "..." : contentB64)); // Auskommentiert, da der Anfragekörper sensibele Daten enhalten könnte, nur zum Testen auskommentieren
        
        return new DocumentDTO(kdlCode, patientId, visitNumber, dateCreated, contentB64);
    }
    
    // Extrahiert den KDL-Code aus der Coding-Liste des DocumentReference-Typs
    private String extractKdlCode(DocumentReference documentReference) {
        for (Coding coding : documentReference.getType().getCoding()) {
            if (isValidKdlCode(coding)) {
                logger.info("Extracted KDL Code: " + coding.getCode());
                return coding.getCode();
            }
        }
        logger.warning("No valid KDL Code found");
        return null;
    }
    
    // Validiert ob das CodeSystem KDL entspricht
    private boolean isValidKdlCode(Coding coding) {
        return coding != null && coding.hasSystem() && coding.hasCode() && KDL_SYSTEM.equals(coding.getSystem());
    }
    
    // Extrahiert die Patient-ID aus der Subject-Referenz
    private String extractPatientId(DocumentReference documentReference) {
        String patientId = extractIdFromReference(documentReference.getSubject().getReference());
        logger.info("Extracted Patient ID: " + patientId);
        return patientId;
    }
    
    // Extrahiert die Besuchsnummer aus der Encounter-Referenz
    private String extractVisitNumber(DocumentReference documentReference) {
        String visitNumber = extractIdFromReference(documentReference.getContext().getEncounterFirstRep().getReference());
        logger.info("Extracted Visit Number: " + visitNumber);
        return visitNumber;
    }
    
    // Extrahiert das Erstellungsdatum aus dem Content-Attachment
    private String extractDateCreated(DocumentReference documentReference) {
        String rawDateCreated = documentReference.getContentFirstRep().getAttachment().getCreationElement().asStringValue();
        String formattedDate = convertDate(rawDateCreated);
        logger.info("Formatted Creation Date: " + formattedDate);
        return formattedDate;
    }
    
    // Extrahiert den Base64-kodierten Inhalt des Dokumentes
    private String extractContentBase64(DocumentReference documentReference) {
        byte[] contentBytes = documentReference.getContentFirstRep().getAttachment().getData();
        String contentB64 = Base64.getEncoder().encodeToString(contentBytes);
        return contentB64;
    }
    
     // Extrahiert die ID aus einer generischen FHIR-Referenz (entfernt alles vor dem /)
     private String extractIdFromReference(String reference) {
        if (reference.contains("/")) {
            String id = reference.substring(reference.lastIndexOf("/") + 1);
            logger.info("Extracted ID from Reference: " + reference + " -> " + id);
            return id;
        }
        return reference;
    }

    // Konvertiert das Erstellungsdatum von ISO-8601 in dd.MM.yyyy
    // (Datumsformat der API war in der Aufgabenstellung nicht angegeben, konvertieren fühlte sich aber sinnvoll an ;) )
    private String convertDate(String creationDate) {
        if (creationDate == null || !creationDate.matches("\\d{4}-\\d{2}-\\d{2}.*")) {
            throw new IllegalArgumentException("Invalid creation date format. Expected format: YYYY-MM-DD");
        }
        
        String[] dateComponents = creationDate.split("T")[0].split("-");
        String day = dateComponents[2];
        String month = dateComponents[1];
        String year = dateComponents[0];
        
        return day + "." + month + "." + year;
    }
    
}
