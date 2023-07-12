package com.reactive.reviewback.model.dtos;

import java.io.Serializable;

public class ProductReviewDTO implements Serializable{
    private String productName;
    private Double rating;
    private String review;
    public String getProductName() {
        return productName;
    }
    public void setProductName(String productName) {
        this.productName = productName;
    }
    public Double getRating() {
        return rating;
    }
    public void setRating(Double rating) {
        this.rating = rating;
    }
    public String getReview() {
        return review;
    }
    public void setReview(String review) {
        this.review = review;
    }
}
