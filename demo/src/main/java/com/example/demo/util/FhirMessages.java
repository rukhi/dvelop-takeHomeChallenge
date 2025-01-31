package com.example.demo.util;

/**
 * Diese Klasse enthält vordefinierte Nachrichten für Fehler- und Erfolgsmeldungen.
 * Durch die zentrale Verwaltung können Änderungen einfach vorgenommen werden,
 * ohne den Code in verschiedenen Klassen anpassen zu müssen.
 */
public class FhirMessages {

    // Fehlermeldungen
    public static final String INTERNAL_SERVER_ERROR = "Ein interner Serverfehler ist aufgetreten.";
    public static final String INVALID_PATIENT_RESOURCE = "Die übermittelte Patientenressource ist ungültig.";
    public static final String API_FAILURE = "Fehler beim Senden der Patientendaten an die proprietäre API.";

    // Erfolgsmeldungen
    public static final String PATIENT_CREATED = "Patient wurde erfolgreich erstellt.";
}
