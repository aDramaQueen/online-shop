package com.acme.onlineshop.persistence.item;

import com.acme.onlineshop.Constants;
import com.acme.onlineshop.persistence.validation.NoneWhitespace;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

@Entity
public class LargeImage extends Image {

    public static final int MAX_SIZE_IN_KILO_BYTES = 2048;

    static {
        if (LargeImage.MAX_SIZE_IN_KILO_BYTES > Constants.MAX_FILE_SIZE_IN_KB) {
            throw new IllegalStateException("You can't have larger images, than max allowed file size.");
        }
    }

    @ManyToMany(fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Item> items;

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }
}
