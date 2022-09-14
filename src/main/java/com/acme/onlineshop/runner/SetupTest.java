package com.acme.onlineshop.runner;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * This runner sets up the application for test mode
 */
@Component
@Order(3)
@Profile(com.acme.onlineshop.utils.Profile.Tag.TEST)
public class SetupTest extends Setup implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {
        setCodeProfile(com.acme.onlineshop.utils.Profile.TEST);
    }
}
