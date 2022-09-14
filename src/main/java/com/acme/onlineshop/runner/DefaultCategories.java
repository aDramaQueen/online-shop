package com.acme.onlineshop.runner;

import com.acme.onlineshop.persistence.item.MainCategory;
import com.acme.onlineshop.persistence.item.SubCategory;
import com.acme.onlineshop.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * This runner sets up some basic categories
 */
@Component
@Order(4)
public class DefaultCategories implements ApplicationRunner {

    private final CategoryService categoryService;

    @Autowired
    public DefaultCategories(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Set<MainCategory> mainCategories = getDefaultMainCategories();
        Set<SubCategory> subCategories;
        int amountSubCategories = 0;
        for(MainCategory mainCategory: categoryService.addNewMainCategories(mainCategories)) {
            subCategories = getDefaultSubCategories(mainCategory);
            categoryService.addNewSubCategories(getDefaultSubCategories(mainCategory));
            amountSubCategories += subCategories.size();
        }

        System.out.printf("ADDED %d new main categories.%n", mainCategories.size());
        System.out.printf("ADDED %d new sub categories.%n", amountSubCategories);
    }

    private Set<MainCategory> getDefaultMainCategories() {
        return Set.of(
                new MainCategory("Music", "CDs, DVDs, Vinyls,..."),
                new MainCategory("Books", "Text Books, Educational Books, Audio Books,..."),
                new MainCategory("Electronics", "TVs, Computers, Phones,..."),
                new MainCategory("Fashion", "Clothes, Shoes, Bags,..."),
                new MainCategory("Food", "Burgers, Noodles, Pizzas,...")
        );
    }

    private Set<SubCategory> getDefaultSubCategories(MainCategory mainCategory) {
        switch (mainCategory.getName()) {
            case "Music" -> {
                return Set.of(
                        new SubCategory("CD", "...", mainCategory),
                        new SubCategory("DVD", "...", mainCategory),
                        new SubCategory("Vinyl", "...", mainCategory)
                );
            }
            case "Books" -> {
                return Set.of(
                        new SubCategory("Fantasy", "...", mainCategory),
                        new SubCategory("Crime", "...", mainCategory),
                        new SubCategory("Science", "...", mainCategory),
                        new SubCategory("Audio Book", "...", mainCategory)
                );
            }
            case "Electronics" -> {
                return Set.of(
                        new SubCategory("TV", "...", mainCategory),
                        new SubCategory("Computer", "...", mainCategory),
                        new SubCategory("Smartphone", "...", mainCategory)
                );
            }
            case "Fashion" -> {
                return Set.of(
                        new SubCategory("Clothes", "...", mainCategory),
                        new SubCategory("Shoes", "...", mainCategory),
                        new SubCategory("Bags", "...", mainCategory),
                        new SubCategory("Accessories", "...", mainCategory)
                );
            }
            case "Food" -> {
                return Set.of(
                        new SubCategory("Burger", "...", mainCategory),
                        new SubCategory("Noodles", "...", mainCategory),
                        new SubCategory("Pizza", "...", mainCategory)
                );
            }
            default -> throw new IllegalArgumentException("Unknown default main category:\n"+mainCategory);
        }
    }
}
