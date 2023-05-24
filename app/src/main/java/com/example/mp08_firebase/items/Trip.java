package com.example.mp08_firebase.items;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Trip implements Parcelable {
    private String name;
    private String departurePlanet;
    private String arrivalPlanet;
    private String company;
    private String spacecraft;
    private String spacecraftDescription;
    private String duration;
    private String description;
    private String departureDate;
    private String returnDate;
    private int capacity;
    private float price;
    private ArrayList<String> images;

    public Trip(){}

    public Trip(String name,String departurePlanet, String arrivalPlanet, String company, String spacecraft, String spacecraftDescription, String duration, String description, String departureDate, String returnDate, int capacity, float price, ArrayList<String> images) {
        this.name = name;
        this.departurePlanet = departurePlanet;
        this.arrivalPlanet = arrivalPlanet;
        this.company = company;
        this.spacecraft = spacecraft;
        this.spacecraftDescription = spacecraftDescription;
        this.duration = duration;
        this.description = description;
        this.departureDate = departureDate;
        this.returnDate = returnDate;
        this.capacity = capacity;
        this.price = price;
        this.images = images;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeparturePlanet() {
        return departurePlanet;
    }

    public void setDeparturePlanet(String departurePlanet) {
        this.departurePlanet = departurePlanet;
    }

    public String getArrivalPlanet() {
        return arrivalPlanet;
    }

    public void setArrivalPlanet(String arrivalPlanet) {
        this.arrivalPlanet = arrivalPlanet;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getSpacecraft() {
        return spacecraft;
    }

    public void setSpacecraft(String spacecraft) {
        this.spacecraft = spacecraft;
    }

    public String getSpacecraftDescription() {
        return spacecraftDescription;
    }

    public void setSpacecraftDescription(String spacecraftDescription) {
        this.spacecraftDescription = spacecraftDescription;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public ArrayList<String> getImages() {
        return images;
    }

    public void setImages(ArrayList<String> images) {
        this.images = images;
    }

    // Parcelable implementation
    protected Trip(Parcel in) {
        name = in.readString();
        departurePlanet = in.readString();
        arrivalPlanet = in.readString();
        company = in.readString();
        spacecraft = in.readString();
        spacecraftDescription = in.readString();
        duration = in.readString();
        description = in.readString();
        departureDate = in.readString();
        returnDate = in.readString();
        capacity = in.readInt();
        price = in.readFloat();
        images = in.createStringArrayList();
    }

    public static final Creator<Trip> CREATOR = new Creator<Trip>() {
        @Override
        public Trip createFromParcel(Parcel in) {
            return new Trip(in);
        }

        @Override
        public Trip[] newArray(int size) {
            return new Trip[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(departurePlanet);
        parcel.writeString(arrivalPlanet);
        parcel.writeString(company);
        parcel.writeString(spacecraft);
        parcel.writeString(spacecraftDescription);
        parcel.writeString(duration);
        parcel.writeString(description);
        parcel.writeString(departureDate);
        parcel.writeString(returnDate);
        parcel.writeInt(capacity);
        parcel.writeFloat(price);
        parcel.writeStringList(images);
    }
}