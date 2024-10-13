package org.glassfish.microprofile.health;

import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

import java.util.Set;

public class HealthServletInitializer implements ServletContainerInitializer {
    static {
        System.err.println("Class loaded!");
    }

    @Override
    public void onStartup(Set<Class<?>> set, ServletContext servletContext) throws ServletException {
        System.err.println("onStartup: " + servletContext.getContextPath());
        // for now
//        if (!servletContext.getContextPath().isEmpty()) {
//            return;
//        }
        servletContext.addServlet("health", HealthServlet.class)
            .addMapping("/health", "/health/live", "/health/ready");
//        servletContext.addServlet("health-liveness", HealthLivenessServlet.class).addMapping("/health/live");
//        servletContext.addServlet("health-readness", HealthReadinessServlet.class).addMapping("/health/ready");
    }
}
