package com.reactive.reviewback.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("product_reviews")
public class ProductReviewR2DBC implements Serializable{
    
    @Id
    @Column("id")
    private Long id;

    @Column("name")
    private String name;

    private List<Review> reviews;

    @Column("rating")
    public Double rating;

    public ProductReviewR2DBC() {
        this.name = "";
        this.reviews = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    
}
