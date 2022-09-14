package com.acme.onlineshop.controller;

import com.acme.onlineshop.persistence.item.MainCategory;
import com.acme.onlineshop.service.CategoryService;
import com.acme.onlineshop.service.ImageService;
import com.acme.onlineshop.service.UserService;
import com.acme.onlineshop.web.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class AdminController {

    private final UserService userService;
    private final ImageService imageService;
    private final CategoryService categoryService;

    @Autowired
    public AdminController(UserService userService, ImageService imageService, CategoryService categoryService) {
        this.userService = userService;
        this.imageService = imageService;
        this.categoryService = categoryService;
    }

    @GetMapping(URL.Path.ADMIN)
    public String homePage(Model model, HttpSession session) {
        model.addAttribute("logo", imageService.getLogo());
        return URL.ADMIN.html;
    }

    @GetMapping(URL.Path.ADMIN_CATEGORIES)
    public String categories(Model model, HttpSession session) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return URL.ADMIN_CATEGORIES.html;
    }

    @GetMapping(URL.Path.ADMIN_CUSTOMERS)
    public String customers(Model model, HttpSession session) {
        return URL.ADMIN_CUSTOMERS.html;
    }

    @GetMapping(URL.Path.ADMIN_OVERVIEW)
    public String overview(Model model, HttpSession session) {
        return URL.ADMIN_OVERVIEW.html;
    }

    @GetMapping(URL.Path.ADMIN_INVENTORY)
    public String inventory(Model model, HttpSession session) {
        return URL.ADMIN_INVENTORY.html;
    }
}
