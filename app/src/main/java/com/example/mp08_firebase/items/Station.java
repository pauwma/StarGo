package com.example.mp08_firebase.items;

import java.util.ArrayList;

public class Station {

    private String name;
    private String description;
    private Planet planet;
    private int capacity;
    private ArrayList<String> images;
    private ArrayList<Cabin> cabins;

    public Station(String name, String description, Planet planet, int capacity, ArrayList<String> images, ArrayList<Cabin> cabins) {
        this.name = name;
        this.description = description;
        this.planet = planet;
        this.capacity = capacity;
        this.images = images;
        this.cabins = cabins;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Planet getPlanet() {
        return planet;
    }

    public void setPlanet(Planet planet) {
        this.planet = planet;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public ArrayList<String> getImages() {
        return images;
    }

    public void setImages(ArrayList<String> images) {
        this.images = images;
    }

    public ArrayList<Cabin> getCabins() {
        return cabins;
    }

    public void setCabins(ArrayList<Cabin> cabins) {
        this.cabins = cabins;
    }

    public void addCabin(Cabin cabin){
        this.cabins.add(cabin);
    }
}