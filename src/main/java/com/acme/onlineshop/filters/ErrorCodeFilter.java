package com.acme.onlineshop.filters;

import com.acme.onlineshop.controller.errors.ErrorResponseCodes;
import com.acme.onlineshop.web.URL;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(URL.Path.REST_API +"/*")
public class ErrorCodeFilter extends OncePerRequestFilter {

    public static final String ERROR_CODE_FIELD = "Error-Code";
    public static final int DEFAULT_ERROR_CODE = ErrorResponseCodes.OK.errorCode;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        response.setIntHeader(ERROR_CODE_FIELD, DEFAULT_ERROR_CODE);
        // Continue filter chain
        filterChain.doFilter(request, response);
    }
}
