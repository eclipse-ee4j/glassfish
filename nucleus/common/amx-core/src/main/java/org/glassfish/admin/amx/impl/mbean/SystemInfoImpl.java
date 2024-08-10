/*
 * Copyright (c) 2022 Contributors to Eclipse Foundation.
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.amx.impl.mbean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.management.MBeanServer;

import org.glassfish.admin.amx.base.SystemInfo;
import org.glassfish.external.amx.AMXGlassfish;

/**
 * Loaded as MBean <code>amx:j2eeType=X-SystemInfo,name=na</code>
 */
public final class SystemInfoImpl extends AMXImplBase {

    private final ConcurrentMap<String, Boolean> mFeatures;

    public SystemInfoImpl(final MBeanServer server) {
        super(AMXGlassfish.DEFAULT.domainRoot(), SystemInfo.class);

        // must be thread-safe, because features can be added at a later time
        mFeatures = new ConcurrentHashMap<String, Boolean>();
    }


    /**
     * Advertise the presence of a feature. For consistency, feature names should normally be
     * of the form <code>[description]_FEATURE</code>. For example: <code>HELLO-WORLD_FEATURE</code>
     * <p>
     * To change a feature's availability to unavailable, pass 'false' for 'available' (there is no
     * removeFeature() call). This is discouraged unless dynamic presence/absence is an inherent
     * characteristic of the feature; clients might check only once for presence or absence.
     *
     * @param featureName name of the feature
     * @param available should be 'true' unless an explicit 'false' (unavailable) is desired
     */
    public void addFeature(final String featureName, final boolean available) {
        if (featureName == null || featureName.length() == 0) {
            throw new IllegalArgumentException();
        }

        mFeatures.put(featureName, Boolean.valueOf(available));
    }

    public String[] getFeatureNames() {
        // make a copy so that we can reliably call List.size()
        // According to Brian Goetz, this approach is thread safe for using the keySet.
        final List<String> nameList = new ArrayList<String>(mFeatures.keySet());

        final String[] names = new String[nameList.size()];
        nameList.toArray(names);
        return names;
    }

    public boolean supportsFeature(final String key) {
        Boolean result = mFeatures.get(key);
        if (result == null) {
            result = Boolean.FALSE;
        }

        return (result);
    }


    /**
     * Return a Map keyed by an arbitrary String denoting some feature. The value
     * is the time in milliseconds. Code should not rely on the keys as they are subject to
     * changes, additions, or removal at any time, except as otherwise documented.
     * Even documented items should be used only for informational purposes,
     * such as assessing performance.
     */
    public Map<String, Long> getPerformanceMillis() {
        return new HashMap<String, Long>();
    }
}
