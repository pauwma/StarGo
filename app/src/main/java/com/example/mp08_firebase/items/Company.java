package com.example.mp08_firebase.items;

public class Company {
    private String name;
    private String abbreviation;
    private String description;
    private String logoUrl;

    public Company(String name, String abbreviation, String description, String logoUrl) {
        this.name = name;
        this.abbreviation = abbreviation;
        this.description = description;
        this.logoUrl = logoUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }
}