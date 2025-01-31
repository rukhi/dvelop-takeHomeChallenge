package com.example.demo.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

@Service
public class PersonSchemaService {

    private static final String SCHEMA_FILE = "schemas/Person-JSON-Scheme.json";

    public String getPersonSchema() {
        try {
            ClassPathResource schemaResource = new ClassPathResource(SCHEMA_FILE);
            return StreamUtils.copyToString(schemaResource.getInputStream(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error loading schema from file: " + SCHEMA_FILE, e);
        }
    }
}
