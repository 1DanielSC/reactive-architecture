package com.reactive.reviewback.model;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("review")
public class ReviewR2DBC implements Serializable{
    
    
    @Id
    @Column("id")
    private Long id;

    @Column("product_name")
    private String productName;

    @Column("review")
    private String review;

    @Column("rating")
    private Double rating;

    // Removido @JsonIgnore e @DocumentReference, pois não são necessários para o PostgreSQL com R2DBC
    private ProductReview productReview;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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
