package org.glassfish.microprofile.health;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.Readiness;

public class HealthServlet extends HttpServlet {


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Instance<HealthCheck> select = CDI.current().select(HealthCheck.class);
        if (req.getRequestURI().endsWith("health/live")) {
            select = select.select(Liveness.Literal.INSTANCE);
        } else if (req.getRequestURI().endsWith("health/ready")) {
            select = select.select(Readiness.Literal.INSTANCE);
        }

        List<HealthCheckResponse> healthCheckResults = select
            .stream()
            .map(HealthCheck::call)
            .toList();

        HealthCheckResponse.Status overallStatus = healthCheckResults.stream()
            .map(HealthCheckResponse::getStatus)
            .filter(HealthCheckResponse.Status.DOWN::equals)
            .findFirst().orElse(HealthCheckResponse.Status.UP);

        Output output = new Output(overallStatus, healthCheckResults);
        try (Jsonb jsonb = JsonbBuilder.create()) {
            String json = jsonb.toJson(output);
            resp.getWriter().println(json);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
