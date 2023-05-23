package com.example.mp08_firebase.items;

public class Planet {
    private String name;
    private String description;
    private String temperature;
    private String gravity;
    private String image;
    private String banner;

    public Planet() {}

    public Planet(String name, String description, String temperature, String gravity, String image, String banner) {
        this.name = name;
        this.description = description;
        this.temperature = temperature;
        this.gravity = gravity;
        this.image = image;
        this.banner = banner;
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

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getGravity() {
        return gravity;
    }

    public void setGravity(String gravity) {
        this.gravity = gravity;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String imageUrl) {
        this.image = imageUrl;
    }

    public String getBanner() {
        return banner;
    }

    public void setBanner(String bannerUrl) {
        this.banner = bannerUrl;
    }
}
