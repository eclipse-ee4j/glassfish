/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.config.support;

import com.sun.enterprise.config.serverbeans.AccessLog;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.util.ConfigApiLoggerInfo;

import org.glassfish.api.admin.AdminCommandContext;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

import jakarta.inject.Inject;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

@Service
public class HttpServicePropertiesUpgrade extends BaseLegacyConfigurationUpgrade {
    @Inject
    private Configs configs;

    public void execute(AdminCommandContext context) {
        for (Config config : configs.getConfig()) {
            HttpService service = config.getHttpService();
            if (service == null)
                continue;
            boolean done = false;
            try {
                final List<Property> properties = service.getProperty();
                final Iterator<Property> iterator = properties.iterator();
                while (!done && iterator.hasNext()) {
                    final Property property = iterator.next();
                    String name = property.getName();
                    if ("accessLoggingEnabled".equals(name) || "accessLogBufferSize".equals(name) || "accessLogWriteInterval".equals(name)
                            || "sso-enabled".equals(name)) {
                        done = true;
                        upgrade(context, property, service);
                    }
                }
            } catch (TransactionFailure tf) {
                ConfigApiLoggerInfo.getLogger().log(Level.SEVERE, ConfigApiLoggerInfo.ERR_UPGRADE_HTTP_SVC_PROPS, tf);
                throw new RuntimeException(tf);
            }
        }
    }

    private void upgrade(final AdminCommandContext context, final Property property, final HttpService service) throws TransactionFailure {
        if ("accessLoggingEnabled".equals(property.getName())) {
            updatePropertyToAttribute(context, service, "accessLoggingEnabled", "accessLoggingEnabled");
        } else if ("accessLogBufferSize".equals(property.getName())) {
            ConfigSupport.apply(new SingleConfigCode<AccessLog>() {
                @Override
                public Object run(AccessLog param) {
                    param.setBufferSizeBytes(property.getValue());
                    return param;
                }
            }, service.getAccessLog());
            removeProperty(service, "accessLogBufferSize");
            report(context, "Moved http-service.property.accessLogBufferSize to http-service.access-log.buffer-size-bytes");
        } else if ("accessLogWriteInterval".equals(property.getName())) {
            ConfigSupport.apply(new SingleConfigCode<AccessLog>() {
                @Override
                public Object run(AccessLog param) {
                    param.setWriteIntervalSeconds(property.getValue());
                    return param;
                }
            }, service.getAccessLog());
            removeProperty(service, "accessLogWriteInterval");
            report(context, "Moved http-service.property.accessLogWriteInterval to http-service.access-log.write-interval-seconds");
        } else if ("sso-enabled".equals(property.getName())) {
            updatePropertyToAttribute(context, service, "sso-enabled", "ssoEnabled");
        }
    }

}
