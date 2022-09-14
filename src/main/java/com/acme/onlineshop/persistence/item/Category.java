package com.acme.onlineshop.persistence.item;

import com.acme.onlineshop.persistence.validation.NoneWhitespace;
import com.acme.onlineshop.web.URL;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Locale;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class Category {

    protected final static String ROOT_URL = URL.CATEGORY.url;
    private final static String identifier = """
            ID: %d,
            Name: %s,
            Description: %s
            """;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;
    @NotBlank
    protected String name;
    @NoneWhitespace
    @Column(unique = true)
    protected String nameLowerCase;
    @NotBlank
    protected String description;

    public Category(String name, String description) {
        this.name = name;
        this.description = description;
        prepare();
    }

    public Category() {
        this("", "");
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameLowerCase() {
        return nameLowerCase;
    }

    public void setNameLowerCase(String nameLowerCase) {
        this.nameLowerCase = nameLowerCase;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public abstract String getLink();

    @PrePersist
    @PreUpdate
    private void prepare(){
        this.nameLowerCase = (name == null) ? "" : name.toLowerCase(Locale.getDefault()).replace(' ', '_');
    }

    @Override
    public String toString() {
        return identifier.formatted(this.id, this.name, this.description);
    }
}
