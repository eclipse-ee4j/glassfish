/*
 * Copyright (c) 2021, 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.hk2.config.test.example;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.InjectionTarget;
import org.jvnet.hk2.config.NoopConfigInjector;

@Service(
    name = "generic-container",
    metadata = "target=org.jvnet.hk2.config.test.example.GenericContainer,"
        + "@max-pool-size=optional,"
        + "@max-pool-size=default:32,"
        + "@max-pool-size=datatype:java.lang.String,"
        + "@max-pool-size=leaf,"
        + "@startup-time=optional,"
        + "@startup-time=default:1234,"
        + "@startup-time=datatype:java.lang.String,"
        + "@startup-time=leaf,"
        + "@int-value=optional,"
        + "@int-value=default:1234,"
        + "@int-value=datatype:java.lang.String,"
        + "@int-value=leaf,"
        + "<web-container-availability>=org.jvnet.hk2.config.test.example.WebContainerAvailability,"
        + "<web-container-availability>=@jakarta.validation.constraints.NotNull,"
        + "<*>=collection:org.jvnet.hk2.config.test.example.GenericConfig"
)
@InjectionTarget(GenericContainer.class)
public class GenericContainerInjector extends NoopConfigInjector {

}
