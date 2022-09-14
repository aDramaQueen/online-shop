package com.acme.onlineshop.persistence.item;

import com.acme.onlineshop.Constants;
import com.acme.onlineshop.persistence.validation.NoneWhitespace;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Entity
public class SmallImage extends Image {

    public static final int MAX_SIZE_IN_KILO_BYTES = 512;

    static {
        if (SmallImage.MAX_SIZE_IN_KILO_BYTES > Constants.MAX_FILE_SIZE_IN_KB) {
            throw new IllegalStateException("You can't have larger images, than max allowed file size.");
        }
    }

    @OneToOne
    @MapsId
    @JoinColumn(name = "item_id")
    @JsonIgnore
    private Item item;

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }
}
