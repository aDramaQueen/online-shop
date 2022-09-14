package com.acme.onlineshop.service;

import com.acme.onlineshop.persistence.item.MainCategory;
import com.acme.onlineshop.persistence.item.MainCategoryRepository;
import com.acme.onlineshop.persistence.item.SubCategory;
import com.acme.onlineshop.persistence.item.SubCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final MainCategoryRepository mainCategoryRepository;
    private final SubCategoryRepository subCategoryRepository;

    @Autowired
    public CategoryService(MainCategoryRepository mainCategoryRepository, SubCategoryRepository subCategoryRepository) {
        this.mainCategoryRepository = mainCategoryRepository;
        this.subCategoryRepository = subCategoryRepository;
    }

    public MainCategory addNewMainCategory(MainCategory mainCategory) {
        return mainCategoryRepository.save(mainCategory);
    }

    public List<MainCategory> addNewMainCategories(Iterable<MainCategory> mainCategories) {
        return mainCategoryRepository.saveAll(mainCategories);
    }

    public MainCategory addNewMainCategory(String name, String description) {
        return addNewMainCategory(name, description, new ArrayList<>());
    }

    public MainCategory addNewMainCategory(String name, String description, List<SubCategory> subCategories) {
        return mainCategoryRepository.save(new MainCategory(name, description, subCategories));
    }

    public SubCategory addNewSubCategory(SubCategory subCategory) {
        return subCategoryRepository.save(subCategory);
    }

    public List<SubCategory> addNewSubCategories(Iterable<SubCategory> subCategories) {
        return subCategoryRepository.saveAll(subCategories);
    }

    public SubCategory addNewSubCategory(String name, String description, MainCategory mainCategory) {
        return null;
    }

    /**
     * Returns <b>ALL</b> categories, defined within this application, by loading all subcategories eager within the
     * main categories.
     *
     * @return Lis of "main" categories. Each "main" category holds it's "sub" categories.
     */
    public List<MainCategory> getAllCategories() {
        return mainCategoryRepository.findEagerAllByOrderByName();
    }

    public List<MainCategory> getAllMainCategories() {
        return mainCategoryRepository.findAllByOrderByName();
    }

    public List<SubCategory> getAllSubCategories() {
        return subCategoryRepository.findAllByOrderByName();
    }

    public List<SubCategory> getAllSubCategoriesOfMainCategory(MainCategory mainCategory) {
        return subCategoryRepository.findAllByMainCategoryOrderByName(mainCategory);
    }
}
