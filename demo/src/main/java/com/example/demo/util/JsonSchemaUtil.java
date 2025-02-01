package com.example.demo.util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class JsonSchemaUtil {

    private static final Logger logger = Logger.getLogger(JsonSchemaUtil.class.getName());
    private static final String PERSON_SCHEMA_FILE = "schemas/Person-JSON-Scheme.json";
    private static final String DOCUMENT_SCHEMA_FILE = "schemas/DocumentJsonScheme.json";

    private JsonSchemaUtil() {
        // Privater Konstruktor, um Instanzierung zu verhindern (Utility-Klasse)
    }

    /**
     * Lädt das JSON-Schema für den angegebenen Schema-Typ aus dem Ressourcenordner.
     *
     * @param schemaType Der Typ des Schemas ("person" oder "document")
     * @return Das geladene JSON-Schema als String
     * @throws IllegalArgumentException Falls ein nicht unterstützter Schema-Typ angegeben wurde
     * @throws RuntimeException Falls ein Fehler beim Laden der Datei auftritt
     */
    public static String getSchema(String schemaType) {
        String schemaFile;
         // Bestimmen der passenden Datei basierend auf dem Schema-Typ
        switch (schemaType.toLowerCase()) {
            case "person":
                schemaFile = PERSON_SCHEMA_FILE;
                break;
            case "documentreference":
                schemaFile = DOCUMENT_SCHEMA_FILE;
                break;
            default:
                throw new IllegalArgumentException("Unsupported schema type: " + schemaType);
        }
        
        try {
             // Laden der Datei aus dem Ressourcenverzeichnis
            ClassPathResource schemaResource = new ClassPathResource(schemaFile);
            return StreamUtils.copyToString(schemaResource.getInputStream(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            // Fehler beim Laden der Datei
            throw new RuntimeException("Error loading schema from file: " + schemaFile, e);
        }
    }

      /**
     * Erstellt eine HTTP-Antwort mit dem JSON-Schema für den angegebenen Schema-Typ.
     *
     * @param schemaType Der Typ des Schemas ("person" oder "document")
     * @return Eine ResponseEntity mit dem JSON-Schema oder einer Fehlermeldung
     */
    public static ResponseEntity<String> getSchemaResponse(String schemaType) {
        try {
            // Versucht, das Schema zu laden und gibt es als HTTP-OK zurück
            String schema = getSchema(schemaType);
            return ResponseEntity.ok(schema);
        } catch (IllegalArgumentException e) {
             // Falls der Schema-Typ ungültig ist, gibt es eine 400 Bad Request Antwort
            logger.warning("Invalid schema request: " + e.getMessage());
            return ResponseEntity.badRequest().body("Unsupported schema type: " + schemaType);
        } catch (Exception e) {
            // Falls das Schema nicht geladen werden kann, gibt es eine 500 Internal Server Error Antwort
            logger.severe("Error retrieving schema: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Could not load schema.");
        }
    }
}
