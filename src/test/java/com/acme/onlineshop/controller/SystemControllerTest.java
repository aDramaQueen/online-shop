package com.acme.onlineshop.controller;

import com.acme.onlineshop.condition.SkipWhenSecurityDeactivated;
import com.acme.onlineshop.service.SystemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SystemController.class)
public class SystemControllerTest extends RestApiTest {

    private final static String ROOT_URL = "/api/system/v1";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private SystemService service;

    @Test
    public void shutDown() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post(ROOT_URL+"/shutdown").header(HttpHeaders.AUTHORIZATION, tokenAllPrivileges))
                .andExpect(status().isOk());
    }

    @Test
    @SkipWhenSecurityDeactivated
    public void shutDownForbidden() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post(ROOT_URL+"/shutdown"))
                .andExpect(status().isForbidden());
    }

}
