package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.demo.dto.PersonDTO;

import java.util.logging.Level;
import java.util.logging.Logger;

@Service // Kennzeichnet diese Klasse als Spring Service-Komponente
public class ProprietaryApiService {

    private static final Logger logger = Logger.getLogger(ProprietaryApiService.class.getName());

     // WebClient für nicht-blockierende API-Aufrufe
    private final WebClient webClient;

    // API-URL aus Konfigurationsdatei beziehen
    @Value("${api.proprietary.url}")
    private String apiUrl;

    public ProprietaryApiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(apiUrl).build();
    }

     /**
     * Sendet Patientendaten an eine proprietäre API.
     * 
     * @param firstName Der Vorname des Patienten
     * @param lastName Der Nachname des Patienten
     * @param birthDate Das Geburtsdatum des Patienten
     * @return true, wenn die API-Anfrage erfolgreich war; false, wenn ein Fehler aufgetreten ist
     */

    public boolean sendPatientData(PersonDTO personDTO) {
        try {
            // Loggt die URL und den Anfragekörper
            logger.info("Sending request to proprietary API: " + apiUrl);
            logger.info("Request body: " + personDTO);

            // Sendet eine POST-Anfrage an die proprietäre API mit WebClient
            ResponseEntity<String> response = webClient.post()
                    .uri(apiUrl)
                    .bodyValue(personDTO)
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
            logger.log(Level.SEVERE, "Exception occurred while sending patient data", e);
            return false;
        }
    }
}