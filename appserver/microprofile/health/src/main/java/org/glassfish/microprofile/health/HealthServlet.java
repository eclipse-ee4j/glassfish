package org.glassfish.microprofile.health;

import io.helidon.config.mp.MpConfigProviderResolver;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.health.Startup;

public class HealthServlet extends HttpServlet {


    private static HealthCheckResponse callHealthCheck(HealthCheck healthCheck) {
        try {
            return healthCheck.call();
        } catch (RuntimeException e) {
            e.printStackTrace();
            return HealthCheckResponse.builder()
                    .down()
                    .name(healthCheck.getClass().getName())
                    .withData("rootCause", e.getMessage())
                    .build();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try (Jsonb jsonb = JsonbBuilder.create()) {

            HealthCheckResponse.Status emptyResponse = HealthCheckResponse.Status.UP;

            Instance<HealthCheck> select = CDI.current().select(HealthCheck.class);
            Stream<HealthCheck> healthChecks;
            String requestURI = req.getRequestURI();
            if (requestURI.endsWith("health/live")) {
                healthChecks = select.select(Liveness.Literal.INSTANCE).stream();
            } else if (requestURI.endsWith("health/ready")) {
                healthChecks = select.select(Readiness.Literal.INSTANCE).stream();
                emptyResponse = Objects.equals(getConfig().getOptionalValue("mp.health.default.readiness.empty.response", String.class).orElse("DOWN"), "UP") ? HealthCheckResponse.Status.UP : HealthCheckResponse.Status.DOWN;
            } else if (requestURI.endsWith("health/started")) {
                healthChecks = select.select(Startup.Literal.INSTANCE).stream();
                emptyResponse = Objects.equals(getConfig().getOptionalValue("mp.health.default.startup.empty.response", String.class).orElse("DOWN"), "UP") ? HealthCheckResponse.Status.UP : HealthCheckResponse.Status.DOWN;
            } else {
                healthChecks = Stream.of(
                        select.select(Liveness.Literal.INSTANCE).stream(),
                        select.select(Readiness.Literal.INSTANCE).stream(),
                        select.select(Startup.Literal.INSTANCE).stream()
                ).flatMap(s -> s);
            }


            List<HealthCheckResponse> healthCheckResults = healthChecks
                    .map(HealthServlet::callHealthCheck)
                    .toList();

            HealthCheckResponse.Status overallStatus =
                    healthCheckResults.isEmpty() ? emptyResponse :
                    healthCheckResults.stream()
                        .map(HealthCheckResponse::getStatus)
                        .filter(HealthCheckResponse.Status.DOWN::equals)
                        .findFirst()
                        .orElse(HealthCheckResponse.Status.UP);

            Output output = new Output(overallStatus, healthCheckResults);

            String json = jsonb.toJson(output);

            resp.setStatus(overallStatus == HealthCheckResponse.Status.UP ?
                    HttpServletResponse.SC_OK :
                    HttpServletResponse.SC_SERVICE_UNAVAILABLE);

            resp.getWriter().println(json);
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().println("Internal server error: ");
            e.printStackTrace(resp.getWriter());
        }

    }

    private static Config getConfig() {
        //  ConfigProvider.getConfig() does not work in the Arquillian deployment
        return new MpConfigProviderResolver().getConfig();
    }
}
