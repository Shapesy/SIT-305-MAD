package com.example.lostandfound;

public class Item {
    private final int id;
    private final String postType;
    private final String name;
    private final String phone;
    private final String description;
    private final String date;
    private final String location;
    private final double latitude;
    private final double longitude;

    public Item(int id, String postType, String name, String phone,
                String description, String date, String location,
                double latitude, double longitude) {
        this.id = id;
        this.postType = postType;
        this.name = name;
        this.phone = phone;
        this.description = description;
        this.date = date;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getId()          { return id; }
    public String getPostType() { return postType; }
    public String getName()     { return name; }
    public String getPhone()    { return phone; }
    public String getDescription() { return description; }
    public String getDate()     { return date; }
    public String getLocation() { return location; }
    public double getLatitude() { return latitude; }
    public double getLongitude(){ return longitude; }
}
