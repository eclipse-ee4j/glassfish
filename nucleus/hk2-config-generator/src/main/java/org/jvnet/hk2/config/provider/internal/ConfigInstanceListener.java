/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.hk2.config.provider.internal;

import jakarta.inject.Singleton;

import java.util.Map;

import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.InstanceLifecycleEvent;
import org.glassfish.hk2.api.InstanceLifecycleEventType;
import org.glassfish.hk2.api.InstanceLifecycleListener;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.ObservableBean;

/**
 * @author jwells
 *
 */
@Service @Singleton
public class ConfigInstanceListener implements InstanceLifecycleListener {
    private final Filter filter = BuilderHelper.createContractFilter(ConfigListener.class.getName());

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.InstanceLifecycleListener#getFilter()
     */
    @Override
    public Filter getFilter() {
        return filter;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.InstanceLifecycleListener#lifecycleEvent(org.glassfish.hk2.api.InstanceLifecycleEvent)
     */
    @Override
    public void lifecycleEvent(InstanceLifecycleEvent lifecycleEvent) {
        if (!lifecycleEvent.getEventType().equals(InstanceLifecycleEventType.POST_PRODUCTION)) {
            return;
        }

        Map<Injectee, Object> injectees = lifecycleEvent.getKnownInjectees();
        if (injectees == null) {
            return;
        }

        ConfigListener listener = (ConfigListener) lifecycleEvent.getLifecycleObject();
        for (Object injectee : injectees.values()) {
            if (!(injectee instanceof ConfigBeanProxy)) {
                continue;
            }

            ConfigBeanProxy configBeanProxy = (ConfigBeanProxy) injectee;
            Object impl = ConfigSupport.getImpl(configBeanProxy);

            if (!(impl instanceof ObservableBean)) {
                continue;
            }

            ObservableBean ob = (ObservableBean) impl;

            ob.addListener(listener);
        }

    }

}
