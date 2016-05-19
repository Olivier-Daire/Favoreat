package com.olivierdaire.favoreat;

/**
 * Describe a restaurant with its name, type, average price, rating and photos
 *
 * @author Olivier Daire
 * @version 1.0
 * @since 26/04/16
 */
public class Restaurant {
    private String name;
    private double latitude;
    private double longitude;
    private String type; // TODO Use enum ?
    private int averagePrice;
    private int rating;
    public static final int INT_NO_VALUE = -1;

    public Restaurant(String name, double latitude, double longitude) {
        this(name, latitude, longitude,  "", INT_NO_VALUE, INT_NO_VALUE);
    }

    public Restaurant(String name, double latitude, double longitude, String type) {
        this(name, latitude, longitude,  type, INT_NO_VALUE, INT_NO_VALUE);
    }

    public Restaurant(String name, double latitude, double longitude, String type, int averagePrice) {
        this(name, latitude, longitude, type, averagePrice, INT_NO_VALUE);
    }

    public Restaurant(String name, double latitude, double longitude, String type, int averagePrice, int rating) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = type;
        this.averagePrice = averagePrice;
        this.rating = rating;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getAveragePrice() {
        return averagePrice;
    }

    public void setAveragePrice(int averagePrice) {
        this.averagePrice = averagePrice;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}
