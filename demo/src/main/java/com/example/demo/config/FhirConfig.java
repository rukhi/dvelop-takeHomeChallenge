package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

@Configuration
public class FhirConfig {

    @Bean
    public FhirContext fhirContext() {
        // FHIR-Kontext für FHIR R4 erstellen
        // nur einmal da sehr resourcenintensiv
        return FhirContext.forR4();
    }

    @Bean
    public IParser fhirJsonParser(FhirContext fhirContext) {
        return fhirContext.newJsonParser();
    }
}
