package org.glassfish.microprofile.health;

import java.util.List;

import org.eclipse.microprofile.health.HealthCheckResponse;

public record Output(HealthCheckResponse.Status status, List<HealthCheckResponse> checks) {
}
