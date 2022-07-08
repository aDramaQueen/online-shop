package com.acme.onlineshop.web;

import com.acme.onlineshop.Main;
import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatConfig {

    @Value("${server.ssl.enabled:false}")
    private boolean ssl_tls;

    @Bean
    public TomcatServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        if (Main.getAdditionalPort() > -1) {
            tomcat.addAdditionalTomcatConnectors(buildAdditionalConnectors(Main.getAdditionalPort()));
        }
        return tomcat;
    }

    private Connector buildAdditionalConnectors(int port) {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        if (ssl_tls) {
            if (port == 80) {
                connector.setScheme("http");
            } else {
                connector.setScheme("https");
            }
        } else {
            connector.setScheme("http");
        }
        connector.setPort(port);
        return connector;
    }

}
