package com.acme.onlineshop.security;

import com.acme.onlineshop.controller.WebController;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Handler to redirect user after successful login approach, to his previous site.
 */
public class RedirectionAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    public RedirectionAuthenticationSuccessHandler(String defaultRedirectURL) {
        super(defaultRedirectURL);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
        HttpSession session = request.getSession();
        if (session != null) {
            String redirectUrl = (String) session.getAttribute(WebController.REDIRECT_LOGIN_SESSION_ATTRIBUTE);
            if (redirectUrl != null) {
                //remove redirect from session
                session.removeAttribute(WebController.REDIRECT_LOGIN_SESSION_ATTRIBUTE);
                // redirect to previous page
                getRedirectStrategy().sendRedirect(request, response, redirectUrl);
            } else {
                super.onAuthenticationSuccess(request, response, authentication);
            }
        } else {
            super.onAuthenticationSuccess(request, response, authentication);
        }
    }

}
