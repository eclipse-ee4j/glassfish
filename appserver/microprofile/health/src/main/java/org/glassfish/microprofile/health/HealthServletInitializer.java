package org.glassfish.microprofile.health;

import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;

import java.util.Set;

public class HealthServletInitializer implements ServletContainerInitializer {

    @Override
    public void onStartup(Set<Class<?>> set, ServletContext servletContext) {
        if (servletContext.getContextPath().isEmpty()) {
            return;
        }
        servletContext.addServlet("health", HealthServlet.class)
            .addMapping("/health", "/health/live", "/health/ready", "/health/started");
    }
}
