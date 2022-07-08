package com.acme.onlineshop.web;

import com.acme.onlineshop.Constants;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * Utility class to overwrite OpenAPI 3.0 root URL with hard coded values from {@link URL} and {@link Constants}
 *
 * @see <a href="https://springdoc.org/faq.html">Spring DOC</a>
 */
@Component
public class SwaggerConfiguration implements ApplicationListener<ApplicationPreparedEvent> {

    @Override
    public void onApplicationEvent(final ApplicationPreparedEvent event) {
        ConfigurableEnvironment environment = event.getApplicationContext().getEnvironment();
        Properties props = new Properties();
        props.put("springdoc.api-docs.enabled", Constants.OPEN_API);
        props.put("springdoc.swagger-ui.path", URL.OPEN_API_WEB.url);
        props.put("springdoc.api-docs.path", URL.OPEN_API_JSON.url);
        environment.getPropertySources().addFirst(new PropertiesPropertySource("programmatically", props));
    }
}
