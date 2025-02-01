package com.example.demo.service;

import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Service;

import com.example.demo.dto.PersonDTO;

@Service // Kennzeichnet diese Klasse als Spring Service-Komponente
public class PatientService {

    private static final Logger logger = Logger.getLogger(PatientService.class.getName());

    // Validierung der Daten im Patientenobjekt
    public boolean isValid(Patient patient) {
        // Als Beispiel mal so rudimentär umgestzt
        // eine Validierung gegen die Implementation Guides wäre hier sinnvoll,
        // in der Kürze der 24h für mich aber nicht sinnvoll umsetzbar ;)
        // falls am Ende doch Zeit ist, komme ich hier nochmal darauf zurück
        //
        // würde es dann auch auslagern in eine eigene Klasse !
        if (patient == null) {
            logger.warning("Patient is null");
            return false;
        }

        if (patient.getName() == null || patient.getName().isEmpty()) {
            logger.warning("Patient name is missing");
            return false;
        }

        if (patient.getName().get(0).getGiven() == null || patient.getName().get(0).getGiven().isEmpty()) {
            logger.warning("Patient given name is missing");
            return false;
        }

        if (patient.getName().get(0).getFamily() == null || patient.getName().get(0).getFamily().isEmpty()) {
            logger.warning("Patient family name is missing");
            return false;
        }

        if (patient.getBirthDate() == null) {
            logger.warning("Patient birth date is missing");
            return false;
        }

        return true;
    }

    // Konvertiert ein gültiges Patient-Objekt in ein PersonDTO-Objekt
    public PersonDTO processPatient(Patient patient) {
        // Extrahieren des Vornamens aus der Patient-Ressource
        String firstName = patient.getName().get(0).getGiven().stream()
                .map(namePart -> namePart.getValue())
                .collect(Collectors.joining(" "));

        // Extrahieren des Nachnamens aus der Patient-Ressource
        String lastName = patient.getName().get(0).getFamily();

        // Extrahieren des Geburtsdatums aus der Patient-Ressource und konvertieren
        String birthDate = convertDate(patient.getBirthDateElement().getValueAsString());

        // Loggt die extrahierten und konvertierten Patientendaten
        // Achtung: nur zum Testen auskommentieren, sensible Patientendaten sollten
        // nicht geloggt werden
        // logger.info("Parsed patient data: " + firstName + " " + lastName + ",
        // Birthdate: " + birthDate);

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