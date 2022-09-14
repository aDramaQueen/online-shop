package com.acme.onlineshop.controller;

import com.acme.onlineshop.dto.FavouriteCategory;
import com.acme.onlineshop.service.CategoryService;
import com.acme.onlineshop.service.ImageService;
import com.acme.onlineshop.service.UserService;
import com.acme.onlineshop.web.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomepageController {

    private final UserService userService;
    private final CategoryService categoryService;
    private final ImageService imageService;

    @Autowired
    public HomepageController(UserService userService, CategoryService categoryService, ImageService imageService) {
        this.userService = userService;
        this.categoryService = categoryService;
        this.imageService = imageService;
    }

    @GetMapping(URL.Path.HOME)
    public String homePage(Model model, HttpSession session) {
        model.addAttribute("logo", imageService.getLogo());
        model.addAttribute("favourites", getFavouriteCategories());
        return URL.HOME.html;
    }

    @GetMapping(URL.Path.CATEGORY+"/{category}")
    public String category(Model model, HttpSession session, @PathVariable String category) {
        model.addAttribute("category", category);
        return URL.CATEGORY.html;
    }

    @GetMapping(URL.Path.SEARCH)
    public String search(Model model, HttpSession session, @RequestParam String search) {
        model.addAttribute("searchText", search);
        System.out.println("SEARCH: "+ search);
        return URL.SEARCH.html;
    }

    //TODO: Add User ID to parameter, make DB request, build users favourite categories
    private List<FavouriteCategory> getFavouriteCategories() {
        return categoryService.getAllMainCategories().stream().map(category -> new FavouriteCategory(category.getName(), category.getLink())).collect(Collectors.toList());
    }
}
