package org.glassfish.microprofile.health;

import java.util.List;

import org.eclipse.microprofile.health.HealthCheckResponse;

record Output(HealthCheckResponse.Status getStatus, List<HealthCheckResponse> getChecks) {
}
