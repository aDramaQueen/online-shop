package com.acme.onlineshop.persistence.configuration;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationConfigRepository extends JpaRepository<ApplicationConfig, Integer> { }