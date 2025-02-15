# FHIR to Proprietary API Facade

### Diese Anwendung implementiert eine Fassade, die Endpunkte zur Erstellung von Patientendaten und Dokumenten gemäß der HL7 FHIR R4 Spezifikation bereitstellt. 

### Bei der Anlieferung eines FHIR Objekts werden bestimmte Eigenschaften extrahiert und an eine proprietäre REST-API weitergeleitet, um einen Eintrag in einem externen System anzulegen. Abhängig von der Antwort des externen Systems gibt die Fassade eine entsprechende Rückmeldung an den FHIR-Client.

# Derzeit unterstützte FHIR-Ressourcen
    - Patient
    - DocumentReference

# Installation

## Voraussetzungen

    - Java Development Kit (JDK) 17 oder höher
    - Maven
    - Mockoon oder Postman (für die Simulation der proprietären API)

1. Repository klonen <br />
    Klonen Sie das Repository auf Ihren lokalen Rechner: <br />
        `git clone https://github.com/rukhi/dvelop-takeHomeChallenge`
   
2. In das Projektverzeichnis wechseln <br />
    Wechseln Sie in das Verzeichnis des geklonten Projekts: <br />
        `cd demo`
   
3. Projekt bauen <br />
    Bauen Sie das Projekt mit Maven: <br />
        Run `mvn clean install`
   
4. Anwendung starten <br />
    Starten Sie die Spring Boot Anwendung: <br />
        `mvn spring-boot:run`

## Nutzung

### Mockoon zur Simulation der proprietären API
Um die proprietäre API zu simulieren, verwenden wir Mockoon. Folgen Sie diesen Schritten: <br />

1. Mockoon starten: <br />
    Laden Sie Mockoon herunter und installieren Sie es von mockoon.com <br />
    Starten Sie Mockoon und importieren Sie die bereitgestellte Datei 
        für Patient Endpunkt -> `Test-Patient-API_Dev.json`
        für Document Endpunkt -> `Test-Document-API_Dev.json`

2. Mock-Server starten: <br />
    Starten Sie den Mock-Server auf Port 3001.

### Postman zur API-Testung
Verwenden Sie Postman, um Ihre API-Endpunkte zu testen. Folgen Sie diesen Schritten:

1. Postman öffnen: <br />
    Starten Sie Postman nach der Installation.

2. Neue Anfrage erstellen: <br />
    Klicken Sie auf `New` und wählen Sie `Request` aus. <br />
    Geben Sie der Anfrage einen Namen, z.B. "Create Patient". <br />
    Erstellen Sie eine neue Sammlung (Collection) oder wählen Sie eine bestehende aus, um Ihre Anfrage zu speichern.

## Patient Endpunkt testen

3. Patient erstellen: <br />
    Wählen Sie den HTTP-Methodentyp `POST` aus. <br />
    Geben Sie die URL Ihres Endpunkts ein: `http://localhost:8080/fhir/Patient`. <br />
    Stellen Sie sicher, dass der Endpunkt Ihrer laufenden Spring Boot Anwendung entspricht.

4. Header hinzufügen: <br />
    Gehen Sie zum Tab `Headers`. <br />
    Fügen Sie einen neuen Header hinzu: `Content-Type` mit dem Wert `application/json`.

5. Wechseln Sie zum Tab Body. <br />
    Wählen Sie die Option raw aus. <br />
    Stellen Sie sicher, dass der Datentyp auf JSON gesetzt ist. <br />
    Fügen Sie den Inhalt Ihrer Beispiel-FHIR-Ressource `Beispiel-FHIR-Ressource-Patient.json` in das Textfeld ein als Anfragekörper.

    Hinweis: um das JSON-Schema für den Endpunkt zu prüfen, steht die GET-Methode `http://localhost:8080/fhir/Patient\schema` zur Verfügung.  

6. Anfrage senden: <br />
    Klicken Sie auf `Send`, um die Anfrage zu senden. Postman wird die Anfrage an Ihren Endpunkt schicken und die Antwort anzeigen.

7. Überprüfen der Antwort: <br />
    Postman zeigt die Antwort des Servers im unteren Bereich des Fensters an. Überprüfen Sie die folgenden Punkte: <br />

    Statuscode: Stellen Sie sicher, dass Sie den erwarteten Statuscode zurückerhalten. Bei erfolgreicher Erstellung sollte dies `201 Created` sein. <br />
    Antwortinhalt: Überprüfen Sie den Inhalt der Antwortnachricht, um sicherzustellen, dass die Patientenerstellung erfolgreich war.

## Document Endpunkt testen

3. Dokument erstellen: <br />
    Wählen Sie den HTTP-Methodentyp `POST` aus. <br />
    Geben Sie die URL Ihres Endpunkts ein: `http://localhost:8080/fhir/DocumentReference`. <br />
    Stellen Sie sicher, dass der Endpunkt Ihrer laufenden Spring Boot Anwendung entspricht.

4. Header hinzufügen: <br />
    Gehen Sie zum Tab `Headers`. <br />
    Fügen Sie einen neuen Header hinzu: `Content-Type` mit dem Wert `application/json`.

5. Wechseln Sie zum Tab Body. <br />
    Wählen Sie die Option raw aus. <br />
    Stellen Sie sicher, dass der Datentyp auf JSON gesetzt ist. <br />
    Fügen Sie den Inhalt Ihrer Beispiel-FHIR-Ressource `Beispiel-DocumentReferenceBody.json` in das Textfeld ein als Anfragekörper.

    Hinweis: um das JSON-Schema für den Endpunkt zu prüfen, steht die GET-Methode `http://localhost:8080/fhir/DocumentReference\schema` zur Verfügung.  

6. Anfrage senden: <br />
    Klicken Sie auf `Send`, um die Anfrage zu senden. Postman wird die Anfrage an Ihren Endpunkt schicken und die Antwort anzeigen.

7. Überprüfen der Antwort: <br />
    Postman zeigt die Antwort des Servers im unteren Bereich des Fensters an. Überprüfen Sie die folgenden Punkte: <br />

    Statuscode: Stellen Sie sicher, dass Sie den erwarteten Statuscode zurückerhalten. Bei erfolgreicher Erstellung sollte dies `201 Created` sein. <br />
    Antwortinhalt: Überprüfen Sie den Inhalt der Antwortnachricht, um sicherzustellen, dass die Dokumenterstellung erfolgreich war.

### Beispiel-FHIR-Ressource
Verwenden Sie die Beispiel-FHIR-Ressourcen im "resources" Ordner als Anfragekörper.


# Architektur und Codeerläuterung

## Projektstruktur

Das Projekt ist in mehrere Pakete unterteilt, um eine klare Trennung der Verantwortlichkeiten zu gewährleisten: <br />

    config: Beinhaltet Konfigurationsklassen.
        - FhirConfig.java: globale Instanzen erstellen um Ressourcen zu sparen (FHIRContext, jsonParser)  

    controller: Enthält den FHIR Controller.  
        - FhirController.java: Verarbeitet FHIR-Anfragen und leitet sie an die Service-Schicht weiter.  

    dto: Beinhaltet Data Transfer Objects (DTOs) für den Datentransfer.  
        - PersonDTO.java: Repräsentiert eine Person für die API-Kommunikation.  
        - DocumentDTO.java: Repräsentiert ein Dokument für die API-Kommunikation. 

    service: Beinhaltet die Geschäftslogik zur Verarbeitung von FHIR-Ressourcen und externen APIs.  
        - DocumentReferenceService.java: 
        - PatientService.java: Verarbeitet Patientendaten und validiert FHIR-Objekte.  
        - PersonSchemaService.java: 
        - ProprietaryApiService.java: Kommuniziert mit einer externen proprietären API. 
        - FhirResponseService.java: Erstellt standardisierte FHIR-Antworten.   
        - FhirValidatorService.java: Validierung von FHIR Resourcen, ValueSets, CodeSysteme...

    util: Helferklassen zur Unterstützung der Hauptlogik.  
        - FhirMessages.java: Enthält vordefinierte FHIR-Fehlermeldungen.  
        - JsonSchemaUtil.java: Stellt JSON-Schema für proprietäre API Endpunkte zur Verfügung

    resources: Enthält Konfigurations- und Schema-Dateien.  
        - application.properties: Konfigurationsdatei für die Anwendung.  
        - example: Anfragekörper für Beispiel-FHIR-Ressourcen
        - schemas: JSON-Schema für proprietäre API Endpunkte 
        - fhir: Spezielle Ressourcen (Structure Definition, CodeSystems etc..) zur Validierung, die nicht
                in den HAPI Fhir Bibliotheken bekannt sind (Gematik, dvmd...)


## Fehlerbehandlung
Die Anwendung behandelt Fehlerfälle, indem sie Statuscodes interpretiert und entsprechende Nachrichten zurückgibt: <br />

    Erfolgreiche Anlage: Statuscode 201 
    Fehlerhafte Anlage: Statuscode 500 oder andere Fehlercodes ≥ 400

