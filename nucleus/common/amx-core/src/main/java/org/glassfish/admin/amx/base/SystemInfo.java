/*
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

package org.glassfish.admin.amx.base;

import java.util.Map;

import org.glassfish.admin.amx.annotation.ManagedAttribute;
import org.glassfish.admin.amx.core.AMXMBeanMetadata;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.external.arc.Stability;
import org.glassfish.external.arc.Taxonomy;

/**
Provides information about the capabilities of the running server.
Callers should check only for specific capabilities, never whether
the server is PE/SE/EE, since the feature assortment could vary with
release.
 */
@Taxonomy(stability = Stability.UNCOMMITTED)
@AMXMBeanMetadata(singleton = true, globalSingleton = true, leaf = true)
public interface SystemInfo extends AMXProxy, Singleton, Utility {

    /**
     * Call supportsFeature() with this value to determine if the server
     * supports clusters.
     */
    public final String CLUSTERS_FEATURE = "SupportsClusters";

    /**
     * Call supportsFeature() with this value to determine if the server
     * supports more than one server.
     */
    public final String MULTIPLE_SERVERS_FEATURE = "SupportsMultipleServers";

    /**
     * Call supportsFeature() with this value to determine if this MBean
     * is running in the Domain Admin Server.
     */
    public final String RUNNING_IN_DAS_FEATURE = "RunningInDomainAdminServer";

    /**
     * Call supportsFeature() with this value to determine if the
     * high availability feature (HADB) is available.
     */
    public final String HADB_CONFIG_FEATURE = "HighAvailabilityDatabase";

    /**
     * Query whether a feature is supported. Features require the use
     * of a key, which may be any of:
     * <ul>
     * <li>#CLUSTERS_FEATURE</li>
     * <li>#MULTIPLE_SERVERS_FEATURE</li>
     * <li>#RUNNING_IN_DAS_FEATURE</li>
     * <li>any dynamically-added feature (see {@link #getFeatureNames})</li>
     * </ul>
     * Other features might also be added dynamically, see {@link #getFeatureNames}.
     *
     * @param key the feature name to query
     */
    public boolean supportsFeature(String key);


    /**
     * Return all features names.
     *
     * @return Set
     */
    @ManagedAttribute
    public String[] getFeatureNames();

    /**
     * Key for time for server to complete its startup sequence. The presence of this item
     * in the Map returned by {@link #getPerformanceMillis} indicates that the server has
     * completed its startup sequence. However, some server features might still be initializing
     * asynchronously, or might be lazily loaded.
     *
     * @see #getPerformanceMillis
     */
    public static final String STARTUP_SEQUENCE_MILLIS_KEY = "StartupMillis";

    /**
     * Return a Map keyed by an arbitrary String denoting some feature. The value
     * is the time in milliseconds. Code should not rely on the keys as they are subject to
     * changes, additions, or removal at any time, except as otherwise documented.
     * Even documented items should be used only for informational purposes,
     * such as assessing performance.
     *
     * @return Map<String,Long>
     */
    @ManagedAttribute
    public Map<String, Long> getPerformanceMillis();

}


