/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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

/**
 *
 * @author Ondro Mihalyi
 */
public class GlassFishHealthCheckResponse extends HealthCheckResponse {

    private Map<String, Object> data;

    public GlassFishHealthCheckResponse(String name, Status status, Optional<Map<String, Object>> data) {
        super(name, status, null);
        this.data = data.orElse(null);
    }

    public GlassFishHealthCheckResponse() {
        this(null, null, null);
    }

    public GlassFishHealthCheckResponse addData(String key, Object value) {
        if (data == null) {
            data = new HashMap<>();
        }
        data.put(key, value);
        return this;
    }

    @Override
    public Optional<Map<String, Object>> getData() {
        return Optional.ofNullable(data);
    }



}
