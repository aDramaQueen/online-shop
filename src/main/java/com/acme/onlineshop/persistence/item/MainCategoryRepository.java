package com.acme.onlineshop.persistence.item;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MainCategoryRepository extends JpaRepository<MainCategory, Long> {

    List<MainCategory> findAllByOrderByName();

    @EntityGraph(attributePaths = {"subCategories"}, type = EntityGraph.EntityGraphType.LOAD)
    List<MainCategory> findEagerAllByOrderByName();
}