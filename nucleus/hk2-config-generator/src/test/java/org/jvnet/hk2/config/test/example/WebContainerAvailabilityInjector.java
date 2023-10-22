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
    name = "web-container-availability",
    metadata = "target=org.jvnet.hk2.config.test.example.WebContainerAvailability,"
        + "@availability-enabled=optional,"
        + "@availability-enabled=default:true,"
        + "@availability-enabled=datatype:java.lang.String,"
        + "@availability-enabled=leaf,"
        + "@persistence-type=optional,"
        + "@persistence-type=default:replicated,"
        + "@persistence-type=datatype:java.lang.String,"
        + "@persistence-type=leaf,"
        + "@persistence-frequency=optional,"
        + "@persistence-frequency=default:web-method,"
        + "@persistence-frequency=datatype:java.lang.String,"
        + "@persistence-frequency=leaf,"
        + "@persistence-scope=optional,"
        + "@persistence-scope=default:session,"
        + "@persistence-scope=datatype:java.lang.String,"
        + "@persistence-scope=leaf,"
        + "@persistence-store-health-check-enabled=optional,"
        + "@persistence-store-health-check-enabled=default:false,"
        + "@persistence-store-health-check-enabled=datatype:java.lang.Boolean,"
        + "@persistence-store-health-check-enabled=leaf,"
        + "@sso-failover-enabled=optional,"
        + "@sso-failover-enabled=default:false,"
        + "@sso-failover-enabled=datatype:java.lang.Boolean,"
        + "@sso-failover-enabled=leaf,"
        + "@http-session-store-pool-name=optional,"
        + "@http-session-store-pool-name=datatype:java.lang.String,"
        + "@http-session-store-pool-name=leaf,"
        + "@disable-jreplica=optional,"
        + "@disable-jreplica=default:false,"
        + "@disable-jreplica=datatype:java.lang.Boolean,"
        + "@disable-jreplica=leaf")
@InjectionTarget(WebContainerAvailability.class)
public class WebContainerAvailabilityInjector extends NoopConfigInjector {

}
