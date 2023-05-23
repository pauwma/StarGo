package com.example.mp08_firebase.items;

import java.util.ArrayList;

public class Spacecraft {
    private String name;
    private String description;
    private int capacity;
    private ArrayList<String> images;
    private Company company;

    public Spacecraft(String name, String description, int capacity, ArrayList<String> images, Company company) {
        this.name = name;
        this.description = description;
        this.capacity = capacity;
        this.images = images;
        this.company = company;
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

    public void addImage(String imageUrl){
        this.images.add(imageUrl);
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
