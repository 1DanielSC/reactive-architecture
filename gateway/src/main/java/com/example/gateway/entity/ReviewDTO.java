package com.example.gateway.entity;

import java.io.Serializable;

public class ReviewDTO implements Serializable{
    private String productName;
    private String review;
    private Double rating;
    public String getProductName() {
        return productName;
    }
    public void setProductName(String productName) {
        this.productName = productName;
    }
    public String getReview() {
        return review;
    }
    public void setReview(String review) {
        this.review = review;
    }
    public Double getRating() {
        return rating;
    }
    public void setRating(Double rating) {
        this.rating = rating;
    }
    
}
