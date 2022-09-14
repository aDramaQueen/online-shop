package com.acme.onlineshop.persistence.item;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

@Entity
public class SubCategory extends Category {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private MainCategory mainCategory;

    public SubCategory(String name, String description, MainCategory mainCategory) {
        super(name, description);
        this.mainCategory = mainCategory;
    }

    public SubCategory() {
        this("", "", null);
    }

    public Category getCategory() {
        return mainCategory;
    }

    public void setCategory(MainCategory mainCategory) {
        this.mainCategory = mainCategory;
    }

    @Override
    public String getLink() {
        return mainCategory.getLink()+"/"+this.nameLowerCase;
    }
}
