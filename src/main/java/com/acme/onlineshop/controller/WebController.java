package com.acme.onlineshop.controller;

import com.acme.onlineshop.Constants;
import com.acme.onlineshop.dto.ErrorResponse;
import com.acme.onlineshop.exception.EnablingException;
import com.acme.onlineshop.exception.PasswordStrengthException;
import com.acme.onlineshop.exception.RoleChangeException;
import com.acme.onlineshop.persistence.user.User;
import com.acme.onlineshop.persistence.user.UserPermissionConverter;
import com.acme.onlineshop.security.PermissionOperation;
import com.acme.onlineshop.service.ImageService;
import com.acme.onlineshop.service.UserService;
import com.acme.onlineshop.web.RESTVersionURL;
import com.acme.onlineshop.web.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@Controller
public class WebController {

    private final static Logger LOGGER = LoggerFactory.getLogger(WebController.class);
    public final static String REDIRECT_LOGIN_SESSION_ATTRIBUTE = "redirect_login";
    private final static String thymeleafOldPasswordError = "oldPasswordError";
    private final static String thymeleafPasswordStrengthError = "passwordStrengthError";

    private final UserService userService;
    private final ImageService imageService;

    @Autowired
    public WebController(UserService userService, ImageService imageService) {
        this.userService = userService;
        this.imageService = imageService;
    }

    /**
     * Handles all {@link Exception}s thrown inside this controller which are not explicitly handled by other
     * {@link ExceptionHandler}s
     *
     * @param exc         Exception that was thrown after a method was called, that is not yet implemented
     * @param request     Request URI with REST endpoint &amp; client address
     * @return            A new response entity that holds an error code &amp; an error errorMessage
     * @throws Exception  Original {@link Exception} if exception will be handled separately
     * @see               <a href="https://spring.io/blog/2013/11/01/exception-handling-in-spring-mvc">Exception Handling in Spring MVC</a>
     */
    @ExceptionHandler(value = Exception.class)
    public ModelAndView handleException(HttpServletRequest request, Exception exc) throws Exception {
        // If the exception is annotated with @ResponseStatus rethrow it and let the framework handle it
        if (AnnotationUtils.findAnnotation(exc.getClass(), ResponseStatus.class) != null) {
            throw exc;
        } else {
            ModelAndView mav = new ModelAndView();
            if (Constants.DEBUG) {
                // You would never generate a page that displays a Java exception to an end-user in production mode
                mav.addObject("exception", exc);
            } else {
                mav.addObject("message", exc.getMessage());
            }
            mav.addObject("url", request.getRequestURL());
            // Otherwise setup and send the user to default error-view
            Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
            if (status != null) {
                int statusCode = Integer.parseInt(status.toString());
                if (statusCode == HttpStatus.NOT_FOUND.value()) {
                    mav.setViewName("error/404");
                } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                    LOGGER.error("Error on following request URL path: " + request.getRequestURI(), exc);
                    mav.setViewName("error/500");
                }
            } else {
                LOGGER.error("Error on following request URL path: " + request.getRequestURI(), exc);
                mav.setViewName("error");
            }
            return mav;
        }
    }

    /**
     * Is called if any listed {@link Exception} is thrown from any method inside this {@link Controller}
     *
     * @param exc Any listed exception for this {@link ExceptionHandler}
     * @return {@link ErrorResponse} with exception error message
     */
    @ExceptionHandler({IllegalStateException.class})
    protected ResponseEntity<ErrorResponse> handleUserExceptions(Exception exc) {
        ErrorResponse responseBody = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), exc.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
    }

    @GetMapping(URL.Path.INDEX)
    public String homePage(Model model, HttpSession session) {
        return URL.INDEX.html;
    }

    @GetMapping(URL.Path.ABOUT)
    public String aboutPage(Model model, HttpSession session) {
        return URL.ABOUT.html;
    }

    @GetMapping(URL.Path.CONTACT)
    public String contactPage(Model model, HttpSession session) {
        return URL.CONTACT.html;
    }

    // ------------------------------------------------- Login/Register ------------------------------------------------

    @GetMapping(URL.Path.LOGIN)
    public String login(Model model, HttpServletRequest request, HttpSession session) {
        session.setAttribute(REDIRECT_LOGIN_SESSION_ATTRIBUTE, request.getHeader(HttpHeaders.REFERER));
        if(model.getAttribute("user") == null) {
            model.addAttribute("user", new User());
        }
        return URL.LOGIN.html;
    }

    @PostMapping(URL.Path.REGISTER)
    public String register(User user, Model model, HttpServletRequest request, HttpSession session) {
        try {
            userService.addNewUser(user, !Constants.DEBUG);
        } catch (Exception exc) {
            model.addAttribute("user", new User());
            return login(model, request, session);
        }
        return URL.REGISTER_SUCCESS.html;
    }

    // ------------------------------------------------------ User -----------------------------------------------------

    @GetMapping(URL.Path.USERS)
    public String usersView(Model model, HttpSession session) {
        //Do your thing...
        return URL.USERS.html;
    }

    @GetMapping(URL.Path.USERS_CHANGE+"/{username}")
    public String updateUserView(@PathVariable("username") String username, Model model, HttpSession session) {
        //Do your thing...
        return URL.USERS_CHANGE.html;
    }

    @PostMapping(URL.Path.USERS_EDIT_ATTRIBUTES +"/{username}")
    public String updateUserAttributes(@PathVariable("username") String username, @Valid User modifiedUser, BindingResult result, Model model) {
        model.addAttribute("appOperations", PermissionOperation.getNoneRestEndpointPermissionOperations());
        if (result.hasErrors()) {
            modifiedUser.setUsername(username);
            return URL.USERS_CHANGE.html;
        } else {
            User originalUser = userService.loadUserByUsername(username);
            try {
                userService.updateUser(originalUser, modifiedUser);
            } catch (RoleChangeException exc) {
                result.addError(new FieldError("user", "role", exc.getMessage()));
                modifiedUser.setUsername(username);
                return URL.USERS_CHANGE.html;
            } catch (EnablingException exc) {
                result.addError(new FieldError("user", "enabled", exc.getMessage()));
                modifiedUser.setUsername(username);
                return URL.USERS_CHANGE.html;
            }
            return "redirect:" + URL.USERS.url;
        }
    }

    @PostMapping(URL.Path.USERS_EDIT_PASSWORD +"/{username}")
    public String updateUserPassword(@PathVariable("username") String username, @RequestParam(value = "oldPassword") String oldPassword, @RequestParam(value = "newPassword") String newPassword, Model model, HttpSession session) {
        model.addAttribute("appOperations", PermissionOperation.getNoneRestEndpointPermissionOperations());
        try {
            userService.updateUserPassword(username, oldPassword, newPassword);
        } catch (BadCredentialsException exc) {
            session.setAttribute(thymeleafOldPasswordError, exc.getMessage());
        } catch (PasswordStrengthException exc) {
            session.setAttribute(thymeleafPasswordStrengthError, exc.getErrorMessages());
        }
        return "redirect:%s/%s".formatted(URL.USERS_CHANGE.url, username);
    }

    @GetMapping(URL.Path.USERS_ADD)
    public String addUserView(User user, Model model) {
        user.setPassword(UserService.generateRandomPassword());
        user.setEnabled(true);
        model.addAttribute("postLink", URL.USERS_ADD.url);
        model.addAttribute("appOperations", PermissionOperation.getNoneRestEndpointPermissionOperations());
        model.addAttribute("separator", UserPermissionConverter.separator);
        return URL.USERS_ADD.html;
    }

    @PostMapping(URL.Path.USERS_ADD)
    public String addUser(@Valid User user, BindingResult result, Model model) {
        model.addAttribute("appOperations", PermissionOperation.getNoneRestEndpointPermissionOperations());
        if (!result.hasErrors()) {
            if(userService.doesUserExist(user.getUsername())){
                result.addError(new FieldError("user", "username", "Username already exists"));
            } else {
                try {
                    userService.addNewUser(user, true);
                    return "redirect:" + URL.USERS.url;
                } catch (PasswordStrengthException exc) {
                    for(String error: exc.getErrorMessages()) {
                        result.addError(new FieldError("user", "password", error));
                    }
                } catch (IOException exc) {
                    throw new RuntimeException(exc);
                }
            }
        }
        return URL.USERS_ADD.html;
    }

    @DeleteMapping(URL.Path.USERS +"/delete")
    public ResponseEntity<String> deleteUser(@RequestBody List<String> usernames) {
        userService.deleteUsers(usernames);
        return ResponseEntity.status(HttpStatus.OK).body("OK");
    }

    // -------------------------------------------- User - Profile/Settings --------------------------------------------

    @GetMapping(URL.Path.PROFILE)
    public String profile(Model model, HttpSession session) {
        //Do your thing...
        return URL.PROFILE.html;
    }

    @GetMapping(URL.Path.SETTINGS)
    public String settings(Model model, HttpSession session) {
        //Do your thing...
        return URL.SETTINGS.html;
    }
}
