package com.acme.onlineshop.persistence.item;

import javax.persistence.Entity;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
public class MainCategory extends Category {

    @NotNull
    @OneToMany(mappedBy = "mainCategory", orphanRemoval = true)
    private List<SubCategory> subCategories;

    public MainCategory(String name, String description, List<SubCategory> subCategories) {
        super(name, description);
        this.subCategories = subCategories;
    }

    public MainCategory(String name, String description) {
        this(name, description, new ArrayList<>());
    }

    public MainCategory() {
        this("", "", new ArrayList<>());
    }

    public List<SubCategory> getSubCategories() {
        return subCategories;
    }

    public void setSubCategories(List<SubCategory> subCategories) {
        this.subCategories = subCategories;
    }

    @Override
    public String getLink() {
        return ROOT_URL+"/"+this.nameLowerCase;
    }
}
