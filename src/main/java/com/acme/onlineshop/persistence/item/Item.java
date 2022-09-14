package com.acme.onlineshop.persistence.item;

import io.swagger.v3.oas.annotations.media.Schema;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Item {

    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;
    @ManyToOne
    @NotNull
    @Schema(description = "Category of this item")
    protected Category category;
    @ManyToOne
    @Schema(description = "Sub-Category of this item")
    protected SubCategory subCategory;
    @ManyToOne(cascade = CascadeType.ALL)
    SmallImage thumbnail;
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "items", cascade = CascadeType.ALL)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<LargeImage> images;

    public Item(Category category, SubCategory subCategory, SmallImage thumbnail, List<LargeImage> images) {
        this.category = category;
        this.subCategory = subCategory;
        this.thumbnail = thumbnail;
        this.images = images;
    }

    public Item() {
        this(null, null, null, new ArrayList<>());
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public SubCategory getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(SubCategory subCategory) {
        this.subCategory = subCategory;
    }

    public SmallImage getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(SmallImage thumbnail) {
        this.thumbnail = thumbnail;
    }

    public List<LargeImage> getImages() {
        return images;
    }

    public void setImages(List<LargeImage> images) {
        this.images = images;
    }
}
