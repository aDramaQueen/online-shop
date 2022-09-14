package com.acme.onlineshop.controller;

import com.acme.onlineshop.web.URL;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping(value = URL.Path.BASKET)
public class BasketController {

    @GetMapping("")
    public String getBasket(Model model, HttpSession session) {
        //TODO...
        return URL.BASKET.html;
    }
}
