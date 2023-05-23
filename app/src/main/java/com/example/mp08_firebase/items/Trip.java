package com.example.mp08_firebase.items;

public class Trip {
    private Planet departurePlanet;
    private Planet arrivalPlanet;
    private Company company;
    private Spacecraft spacecraft;
    private String duration;
    private String departureDate;
    private String returnDate;
    private int capacity;
    private float price;

    public Trip(Planet departurePlanet, Planet arrivalPlanet, Company company, Spacecraft spacecraft, String duration, String departureDate, String returnDate, int capacity, float price) {
        this.departurePlanet = departurePlanet;
        this.arrivalPlanet = arrivalPlanet;
        this.company = company;
        this.spacecraft = spacecraft;
        this.duration = duration;
        this.departureDate = departureDate;
        this.returnDate = returnDate;
        this.capacity = capacity;
        this.price = price;
    }

    public Planet getDeparturePlanet() {
        return departurePlanet;
    }

    public void setDeparturePlanet(Planet departurePlanet) {
        this.departurePlanet = departurePlanet;
    }

    public Planet getArrivalPlanet() {
        return arrivalPlanet;
    }

    public void setArrivalPlanet(Planet arrivalPlanet) {
        this.arrivalPlanet = arrivalPlanet;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Spacecraft getSpacecraft() {
        return spacecraft;
    }

    public void setSpacecraft(Spacecraft spacecraft) {
        this.spacecraft = spacecraft;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(String departureDate) {
        this.departureDate = departureDate;
    }

    public String getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(String returnDate) {
        this.returnDate = returnDate;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }
}
