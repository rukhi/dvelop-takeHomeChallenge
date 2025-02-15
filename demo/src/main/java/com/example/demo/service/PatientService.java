package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.stream.Collectors;

import org.hl7.fhir.r4.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dto.PersonDTO;

@Service // Kennzeichnet diese Klasse als Spring Service-Komponente
public class PatientService {

    private static final Logger logger = LoggerFactory.getLogger(PatientService.class);

    // Hier injizieren wir unsere "echte" Validator-Bean
    private final FhirValidatorService fhirValidatorService;

    @Autowired
    public PatientService(FhirValidatorService fhirValidatorService) {
        this.fhirValidatorService = fhirValidatorService;
    }

    // Überprüft ob das Patientenobjekt gültige Daten enthält
    public boolean isValid(Patient patient) {
        if (patient == null) {
            logger.warn("Patient is null");
            return false;
        }

        if (patient.getName() == null || patient.getName().isEmpty()) {
            logger.warn("Patient name is missing");
            return false;
        }

        if (patient.getName().get(0).getGiven() == null || patient.getName().get(0).getGiven().isEmpty()) {
            logger.warn("Patient given name is missing");
            return false;
        }

        if (patient.getName().get(0).getFamily() == null || patient.getName().get(0).getFamily().isEmpty()) {
            logger.warn("Patient family name is missing");
            return false;
        }

        if (patient.getBirthDate() == null) {
            logger.warn("Patient birth date is missing");
            return false;
        }

        return true;
    }

    // Konvertiert ein gültiges Patient-Objekt in ein PersonDTO-Objekt
    public PersonDTO processPatient(Patient patient) {
         if (!isValid(patient)) {
            throw new IllegalArgumentException("Invalid Patient data");
        }
        
        // Erweiterte Validierung: Überprüfung gegen die ISiKPatient StructureDefinition.
        // Wird die Validierung nicht bestanden, wirft FhirInstanceValidatorUtil eine Exception.
        fhirValidatorService.validateIsikPatient(patient);

        // Extrahieren des Vornamens aus der Patient-Ressource
        String firstName = patient.getName().get(0).getGiven().stream()
                .map(namePart -> namePart.getValue())
                .collect(Collectors.joining(" "));

        // Extrahieren des Nachnamens aus der Patient-Ressource
        String lastName = patient.getName().get(0).getFamily();

        // Extrahieren des Geburtsdatums aus der Patient-Ressource und konvertieren
        String birthDate = convertDate(patient.getBirthDateElement().getValueAsString());

        // Loggt die extrahierten und konvertierten Patientendaten
        logger.debug("Parsed patient data: {} {}, Birthdate: {}", firstName, lastName, birthDate);

        // Rückgabe eines PersonDTO-Objekts
        return new PersonDTO(firstName, lastName, birthDate);
    }

    // Konvertierung des Geburtsdatums von YYYY-MM-DD zu DD.MM.YYYY
    private String convertDate(String birthDate) {
        // Validierung des Eingabeformats
        if (birthDate == null || !birthDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new IllegalArgumentException("Invalid birth date format. Expected format: YYYY-MM-DD");
        }

        String[] dateParts = birthDate.split("-");
        String day = dateParts[2];
        String month = dateParts[1];
        String year = dateParts[0];

        return day + "." + month + "." + year;
    }
}