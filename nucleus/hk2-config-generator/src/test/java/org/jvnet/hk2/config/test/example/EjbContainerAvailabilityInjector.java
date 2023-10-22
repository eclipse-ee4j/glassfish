/*
 * Copyright (c) 2021, 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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
    name = "ejb-container-availability",
    metadata = "target=org.jvnet.hk2.config.test.example.EjbContainerAvailability,"
        + "@availability-enabled=optional,"
        + "@availability-enabled=default:true,"
        + "@availability-enabled=datatype:java.lang.String,"
        + "@availability-enabled=leaf,"
        + "@sfsb-ha-persistence-type=optional,"
        + "@sfsb-ha-persistence-type=default:replicated,"
        + "@sfsb-ha-persistence-type=datatype:java.lang.String,"
        + "@sfsb-ha-persistence-type=leaf,"
        + "@sfsb-persistence-type=optional,"
        + "@sfsb-persistence-type=default:file,"
        + "@sfsb-persistence-type=datatype:java.lang.String,"
        + "@sfsb-persistence-type=leaf,"
        + "@sfsb-checkpoint-enabled=optional,"
        + "@sfsb-checkpoint-enabled=datatype:java.lang.String,"
        + "@sfsb-checkpoint-enabled=leaf,"
        + "@sfsb-quick-checkpoint-enabled=optional,"
        + "@sfsb-quick-checkpoint-enabled=datatype:java.lang.String,"
        + "@sfsb-quick-checkpoint-enabled=leaf,"
        + "@sfsb-store-pool-name=optional,"
        + "@sfsb-store-pool-name=datatype:java.lang.String,"
        + "@sfsb-store-pool-name=leaf"
)
@InjectionTarget(EjbContainerAvailability.class)
public class EjbContainerAvailabilityInjector extends NoopConfigInjector {

}
