package org.glassfish.microprofile.health;

import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.spi.HealthCheckResponseProvider;

public class HealthCheckResponseProviderImpl implements HealthCheckResponseProvider {
    @Override
    public HealthCheckResponseBuilder createResponseBuilder() {
        return new HealthCheckResponseBuilderImpl();
    }
}
