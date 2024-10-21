package org.glassfish.microprofile.health;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;

public class HealthCheckResponseBuilderImpl extends HealthCheckResponseBuilder {

    private String name;
    private HealthCheckResponse.Status status;
    private Map<String, Object> data = new HashMap<>();

    @Override
    public HealthCheckResponseBuilder name(String s) {
        name = s;
        return this;
    }

    @Override
    public HealthCheckResponseBuilder withData(String s, String s1) {
        data.put(s, s1);
        return this;
    }

    @Override
    public HealthCheckResponseBuilder withData(String s, long l) {
        data.put(s, l);
        return this;
    }

    @Override
    public HealthCheckResponseBuilder withData(String s, boolean b) {
        data.put(s, b);
        return this;
    }

    @Override
    public HealthCheckResponseBuilder up() {
        return status(true);
    }

    @Override
    public HealthCheckResponseBuilder down() {
        return status(false);
    }

    @Override
    public HealthCheckResponseBuilder status(boolean b) {
        status = b ? HealthCheckResponse.Status.UP : HealthCheckResponse.Status.DOWN;
        return this;
    }

    @Override
    public HealthCheckResponse build() {
        return new HealthCheckResponse(name, status, data.isEmpty() ? null : Optional.of(data));
    }
}
