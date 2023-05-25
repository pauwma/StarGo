package com.example.mp08_firebase.items;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Cabin implements Parcelable {
    private String name;
    private String description;
    private int capacity;
    private float price;
    private ArrayList<String> images;


    public Cabin(){}

    public Cabin(String name, String description, int capacity, float price, ArrayList<String> images) {
        this.name = name;
        this.description = description;
        this.capacity = capacity;
        this.price = price;
        this.images = images;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
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

    protected Cabin(Parcel in) {
        name = in.readString();
        description = in.readString();
        price = in.readFloat();
        capacity = in.readInt();
        images = in.createStringArrayList();
    }

    public static final Creator<Cabin> CREATOR = new Creator<Cabin>() {
        @Override
        public Cabin createFromParcel(Parcel in) {
            return new Cabin(in);
        }

        @Override
        public Cabin[] newArray(int size) {
            return new Cabin[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(description);
        parcel.writeInt(capacity);
        parcel.writeFloat(price);
        parcel.writeStringList(images);
    }
}