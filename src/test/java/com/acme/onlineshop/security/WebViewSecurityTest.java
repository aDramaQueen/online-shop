package com.acme.onlineshop.security;

import com.acme.onlineshop.Constants;
import com.acme.onlineshop.controller.WebController;
import com.acme.onlineshop.filters.JWTFilter;
import com.acme.onlineshop.service.UserService;
import com.acme.onlineshop.web.URL;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.Assume.assumeTrue;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {SecurityConfig.class, ViewResolverConfig.class, BCryptPasswordEncoder.class})
@MockBeans({ @MockBean(UserService.class), @MockBean(JWTFilter.class), @MockBean(ServerProperties.class) })
@WebAppConfiguration
@Import({WebController.class})
public class WebViewSecurityTest {

    @Autowired
    private WebApplicationContext context;

    protected MockMvc mvc;

    @BeforeClass
    public static void init() {
        assumeTrue(Constants.WEB_SECURITY);
    }

    @Before
    public void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }
}
