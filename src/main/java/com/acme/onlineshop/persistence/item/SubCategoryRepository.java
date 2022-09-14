package com.acme.onlineshop.persistence.item;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubCategoryRepository extends JpaRepository<SubCategory, Long> {

    List<SubCategory> findAllByOrderByName();

    List<SubCategory> findAllByMainCategoryOrderByName(MainCategory mainCategory);
}