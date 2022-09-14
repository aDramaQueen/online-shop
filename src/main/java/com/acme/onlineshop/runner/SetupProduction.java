package com.acme.onlineshop.runner;

import com.acme.onlineshop.ApplicationConfiguration;
import com.acme.onlineshop.service.UserService;
import com.acme.onlineshop.web.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * This runner sets up the application for production mode
 */
@Component
@Order(1)
@Profile(com.acme.onlineshop.utils.Profile.Tag.PRODUCTION)
public class SetupProduction extends Setup implements ApplicationRunner {

    private final UserService userService;

    @Autowired
    public SetupProduction(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        setCodeProfile(com.acme.onlineshop.utils.Profile.PRODUCTION);
        checkForAdminUser(userService);
        System.out.printf("%n"+(starterText)+"%n", ApplicationConfiguration.getWebInterfaceURL(), ApplicationConfiguration.getWebInterfaceURL(), URL.OPEN_API_WEB.url);
    }
}
