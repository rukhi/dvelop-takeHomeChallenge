package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.demo.dto.DocumentDTO;
import com.example.demo.dto.PersonDTO;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

@Service // Kennzeichnet diese Klasse als Spring Service-Komponente
public class ProprietaryApiService {

    private static final Logger logger = Logger.getLogger(ProprietaryApiService.class.getName());

    // WebClient für nicht-blockierende API-Aufrufe
    private final WebClient.Builder webClientBuilder;
    private WebClient webClient;

    // API-URL aus Konfigurationsdatei beziehen
    @Value("${api.proprietary.url}")
    private String apiUrl;

    public ProprietaryApiService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @PostConstruct
    private void initWebClient() {
        // Fehler abfangen: Überprüfen, ob `apiUrl` korrekt gesetzt wurde
        if (apiUrl == null || apiUrl.isBlank()) {
            throw new IllegalStateException("API URL is not set! Check application.properties.");
        }

        this.webClient = webClientBuilder.baseUrl(apiUrl).build();
        logger.info("WebClient initialized with base URL: " + apiUrl);
    }

    /**
     * Sendet Patientendaten an eine proprietäre API.
     *
     * @param personDTO Das PersonDTO-Objekt mit den Patientendaten
     * @return true, wenn die API-Anfrage erfolgreich war; false, wenn ein Fehler
     *         aufgetreten ist
     */
    public boolean sendPatientData(PersonDTO personDTO) {
        return sendData(personDTO, "/Patient");
    }

    /**
     * Sendet Dokumentendaten an eine proprietäre API.
     *
     * @param documentDTO Das DocumentDTO-Objekt mit den Dokumentdaten
     * @return true, wenn die API-Anfrage erfolgreich war; false, wenn ein Fehler
     *         aufgetreten ist
     */
    public boolean sendDocumentData(DocumentDTO documentDTO) {
        return sendData(documentDTO, "/Document");
    }

    /**
     * Generische Methode zum Senden von Daten an die proprietäre API.
     *
     * @param data     Das zu sendende Objekt
     * @param endpoint Das API-Endpoint, an das die Daten gesendet werden sollen
     * @return true, wenn die API-Anfrage erfolgreich war; false, wenn ein Fehler
     *         aufgetreten ist
     */
    private boolean sendData(Object data, String endpoint) {
        try {
            // Loggt die URL und den Anfragekörper
            String requestUrl = apiUrl + endpoint;
            logger.info("Sending request to proprietary API: " + requestUrl);
            // logger.info("Request body: " + data); // Auskommentiert, da der Anfragekörper
            // sensibele Daten enhalten könnte, nur zum Testen auskommentieren

            // Sendet eine POST-Anfrage an die proprietäre API mit WebClient
            ResponseEntity<String> response = webClient.post()
                    .uri(requestUrl)
                    .bodyValue(data)
                    .retrieve()
                    .toEntity(String.class)
                    .block();

            // Loggt den Statuscode der Antwort
            logger.info("Response from proprietary API: " + response.getStatusCode());
            HttpStatus statusCode = response.getStatusCode();

            // Akzeptiere sowohl 200 (OK) als auch 201 (Created) als erfolgreichen Status
            if (statusCode == HttpStatus.OK || statusCode == HttpStatus.CREATED) {
                return true;
            } else {
                // Loggt einen Fehler, wenn der Statuscode nicht 200 oder 201 ist
                logger.severe("Proprietary API returned an error: " + response.getBody());
                return false;
            }
        } catch (Exception e) {
            // Loggt eine Ausnahme, falls eine auftritt
            logger.log(Level.SEVERE, "Exception occurred while sending data", e);
            return false;
        }
    }
}