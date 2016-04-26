package com.olivierdaire.favoreat;

import java.io.File;

/**
 * Describe a restaurant with its name, type, average price, rating and photos
 *
 * @author Olivier Daire
 * @version 1.0
 * @since 26/04/16
 */
public class Restaurant {
    private String name;
    private String type; // TODO Use enum ?
    private int averagePrice;
    private int rating;
    private File photo; // TODO Handle multiple files ?
    public static final int INT_NO_VALUE = -1;

    public Restaurant(String name, String type) {
        this(name, type, INT_NO_VALUE, INT_NO_VALUE, null);
    }

    public Restaurant(String name, String type, int averagePrice) {
        this(name, type, averagePrice, INT_NO_VALUE, null);
    }

    public Restaurant(String name, String type, int averagePrice, int rating) {
        this(name, type, averagePrice, rating, null);
    }

    public Restaurant(String name, String type, int averagePrice, int rating, File photo) {
        this.name = name;
        this.type = type;
        this.averagePrice = averagePrice;
        this.rating = rating;
        this.photo = photo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public File getPhoto() {
        return photo;
    }

    public void setPhoto(File photo) {
        this.photo = photo;
    }
}
