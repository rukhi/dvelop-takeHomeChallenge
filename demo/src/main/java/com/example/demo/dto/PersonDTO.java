package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PersonDTO {

    @JsonProperty("PersonFirstName")
    private String firstName;

    @JsonProperty("PersonLastName")
    private String lastName;

    @JsonProperty("PersonDOB")
    private String birthDate;

    public PersonDTO(String firstName, String lastName, String birthDate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
    }

    // Getter & Setter
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }
}
