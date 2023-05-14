package com.reactive.reviewback.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Document(collection = "reviews")
public class Review {
    
    @Id
    private String id;
    private String productName;
    private String review;
    private Double rating;
    @JsonIgnore
    @DocumentReference
    private ProductReview productReview;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
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
    public ProductReview getProductReview() {
        return productReview;
    }
    public void setProductReview(ProductReview productReview) {
        this.productReview = productReview;
    }
    
    
}
