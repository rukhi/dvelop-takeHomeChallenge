package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DocumentDTO {
    @JsonProperty("kdlCode")
    private String kdlCode;

    @JsonProperty("patientId")
    private String patientId;

    @JsonProperty("visitNumber")
    private String visitNumber;

    @JsonProperty("dateCreated")
    private String dateCreated;

    @JsonProperty("contentB64")
    private String contentB64;

    public DocumentDTO() {
    }

    public DocumentDTO(String kdlCode, String patientId, String visitNumber, String dateCreated, String contentB64) {
        this.kdlCode = kdlCode;
        this.patientId = patientId;
        this.visitNumber = visitNumber;
        this.dateCreated = dateCreated;
        this.contentB64 = contentB64;
    }

    public String getKdlCode() {
        return kdlCode;
    }

    public void setKdlCode(String kdlCode) {
        this.kdlCode = kdlCode;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getVisitNumber() {
        return visitNumber;
    }

    public void setVisitNumber(String visitNumber) {
        this.visitNumber = visitNumber;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getContentB64() {
        return contentB64;
    }

    public void setContentB64(String contentB64) {
        this.contentB64 = contentB64;
    }

    @Override
    public String toString() {
        return "DocumentDTO{" +
                "kdlCode='" + kdlCode + '\'' +
                ", patientId=" + patientId +
                ", visitNumber=" + visitNumber +
                ", dateCreated='" + dateCreated + '\'' +
                ", contentB64='" + contentB64 + '\'' +
                '}';
    }
}
