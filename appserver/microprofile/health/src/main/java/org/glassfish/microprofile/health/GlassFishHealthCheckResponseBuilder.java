/*
 * Copyright (c) 2024 Contributors to Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */
package org.glassfish.microprofile.health;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;

public class GlassFishHealthCheckResponseBuilder extends HealthCheckResponseBuilder {

    private final Map<String, Object> data = new HashMap<>();
    private String name;
    private HealthCheckResponse.Status status;

    @Override
    public HealthCheckResponseBuilder name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public HealthCheckResponseBuilder withData(String key, String value) {
        data.put(key, value);
        return this;
    }

    @Override
    public HealthCheckResponseBuilder withData(String key, long value) {
        data.put(key, value);
        return this;
    }

    @Override
    public HealthCheckResponseBuilder withData(String key, boolean value) {
        data.put(key, value);
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
    public HealthCheckResponseBuilder status(boolean up) {
        status = up ? HealthCheckResponse.Status.UP : HealthCheckResponse.Status.DOWN;
        return this;
    }

    @Override
    public HealthCheckResponse build() {
        return new GlassFishHealthCheckResponse(name, status, data.isEmpty() ? Optional.empty() : Optional.of(data));
    }
}
