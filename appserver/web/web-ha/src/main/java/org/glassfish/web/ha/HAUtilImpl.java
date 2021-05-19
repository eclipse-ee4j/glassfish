/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.web.ha;


import com.sun.enterprise.config.serverbeans.AvailabilityService;
import com.sun.enterprise.config.serverbeans.Config;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.gms.bootstrap.GMSAdapterService;
import org.glassfish.security.common.HAUtil;
import jakarta.inject.Inject;
import org.jvnet.hk2.annotations.Optional;
import jakarta.inject.Named;

import org.jvnet.hk2.annotations.Service;
import org.glassfish.hk2.api.PerLookup;

/**
 *
 * @author vbkumarjayanti
 */
@Service
@PerLookup
public class HAUtilImpl implements HAUtil {

    @Inject @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    private Config config;

    @Inject @Optional
    private GMSAdapterService gmsAdapterService;

    public String getClusterName() {
        return (this.gmsAdapterService != null) ?
            gmsAdapterService.getGMSAdapter().getClusterName() : null;
    }
    public String getInstanceName() {
         return (this.gmsAdapterService != null) ?
            gmsAdapterService.getGMSAdapter().getModule().getInstanceName(): null;
    }
    public boolean isHAEnabled() {
         AvailabilityService availabilityService = config.getAvailabilityService();
         if (availabilityService != null && gmsAdapterService != null && gmsAdapterService.isGmsEnabled()) {
             return Boolean.valueOf(availabilityService.getAvailabilityEnabled());
         }
         return false;
    }
}
