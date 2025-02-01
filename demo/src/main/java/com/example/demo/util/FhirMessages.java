package com.example.demo.util;

/**
 * Diese Klasse enthält vordefinierte Nachrichten für Fehler- und Erfolgsmeldungen.
 * Durch die zentrale Verwaltung können Änderungen einfach vorgenommen werden,
 * ohne den Code in verschiedenen Klassen anpassen zu müssen.
 */
public final class FhirMessages {

     private FhirMessages() {
          throw new UnsupportedOperationException("Utility class - cannot be instantiated.");
      }      

     // Fehlermeldungen
     public static final String INTERNAL_SERVER_ERROR = "Ein interner Serverfehler ist aufgetreten.";
     public static final String INVALID_PATIENT_RESOURCE = "Die übermittelte Patientenressource ist ungültig.";
     public static final String INVALID_DOCUMENT_RESOURCE = "Die übermittelte DocumentReference-Ressource ist ungültig.";
     public static final String API_FAILURE = "Fehler beim Senden der Daten an die proprietaere API.";
 
     // Erfolgsmeldungen
     public static final String PATIENT_CREATED = "Patient wurde erfolgreich erstellt.";
     public static final String DOCUMENT_CREATED = "DocumentReference wurde erfolgreich erstellt.";
}
