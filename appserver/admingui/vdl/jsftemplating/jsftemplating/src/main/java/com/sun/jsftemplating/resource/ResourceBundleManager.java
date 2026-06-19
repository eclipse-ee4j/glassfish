/*
 * Copyright (c) 2006, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jsftemplating.resource;

import com.sun.jsftemplating.util.Util;

import jakarta.faces.context.FacesContext;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * <p>
 * This class caches <code>ResourceBundle</code> objects per locale.
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class ResourceBundleManager {

    /**
     * <p>
     * Use {@link #getInstance()} to obtain an instance.
     * </p>
     */
    protected ResourceBundleManager() {
    }

    /**
     * <p>
     * Use this method to get the instance of this class.
     * </p>
     *
     * @deprecated Use ResourceBundleManager#getInstance(FacesContext).
     */
    @Deprecated
    public static ResourceBundleManager getInstance() {
        return getInstance(null);
    }

    /**
     * <p>
     * Use this method to get the instance of this class.
     * </p>
     */
    public static ResourceBundleManager getInstance(FacesContext ctx) {
        if (ctx == null) {
            ctx = FacesContext.getCurrentInstance();
        }
        ResourceBundleManager mgr = null;
        if (ctx != null) {
            // Look in application scope for it...
            mgr = (ResourceBundleManager) ctx.getExternalContext().getApplicationMap().get(RB_MGR);
        }
        if (mgr == null) {
            // 1st time... create / initialize it
            mgr = new ResourceBundleManager();
            if (ctx != null) {
                ctx.getExternalContext().getApplicationMap().put(RB_MGR, mgr);
            }
        }

        // Return the map...
        return mgr;
    }

    /**
     * <p>
     * This method checks the cache for the requested <code>ResourceBundle</code>.
     * </p>
     *
     * @param baseName Name of the bundle.
     * @param locale The <code>Locale</code>.
     *
     * @return The requested <code>ResourceBundle</code> in the most appropriate <code>Locale</code>.
     */
    protected ResourceBundle getCachedBundle(String baseName, Locale locale) {
        return _cache.get(getCacheKey(baseName, locale));
    }

    /**
     * <p>
     * This method generates a unique key for setting / getting <code>ResourceBundle</code>s from the cache. It is important
     * to have different keys per locale (obviously).
     * </p>
     */
    protected String getCacheKey(String baseName, Locale locale) {
        return baseName + "__" + locale.toString();
    }

    /**
     * <p>
     * This method adds a <code>ResourceBundle</code> to the cache.
     * </p>
     */
    protected void addCachedBundle(String baseName, Locale locale, ResourceBundle bundle) {
        // Copy the old Map to prevent changing a Map while someone is
        // accessing it.
        Map<String, ResourceBundle> map = new HashMap<>(_cache);

        // Add the new bundle
        map.put(getCacheKey(baseName, locale), bundle);

        // Set this new Map as the shared cache Map
        _cache = map;
    }

    /**
     * <p>
     * This method obtains the requested <code>ResourceBundle</code> as specified by the given <code>basename</code> and
     * <code>locale</code>.
     * </p>
     *
     * @param baseName The base name for the <code>ResourceBundle</code>.
     * @param locale The desired <code>Locale</code>.
     */
    public ResourceBundle getBundle(String baseName, Locale locale) {
        ResourceBundle bundle = getCachedBundle(baseName, locale);
        if (bundle == null) {
            try {
                bundle = ResourceBundle.getBundle(baseName, locale, Util.getClassLoader(baseName));
            } catch (MissingResourceException ex) {
                // Use System.out.println b/c we don't want infinite loop and
                // we're too lazy to do more...
                System.out.println("Can't find bundle: " + baseName);
                ex.printStackTrace();
            }
            if (bundle != null) {
                addCachedBundle(baseName, locale, bundle);
            }
        }
        return bundle;
    }

    /**
     * <p>
     * This method obtains the requested <code>ResourceBundle</code> as
     * specified by the given basename, locale, and classloader.</p>
     *
     * @param baseName The base name for the <code>ResourceBundle</code>.
     * @param locale The desired <code>Locale</code>.
     * @param loader The <code>ClassLoader</code> that should be used.
     */
    public ResourceBundle getBundle(String baseName, Locale locale, ClassLoader loader) {
        ResourceBundle bundle = getCachedBundle(baseName, locale);
        if (bundle == null) {
            bundle = ResourceBundle.getBundle(baseName, locale, loader);
            if (bundle != null) {
                addCachedBundle(baseName, locale, bundle);
            }
        }

        return bundle;
    }

    /**
     * <p>
     * Application scope key which stores the <code>ResourceBundleManager</code> instance for this application.
     * </p>
     */
    private static final String RB_MGR = "__jsft_ResourceBundleMgr";

    /**
     * <p>
     * The cache of <code>ResourceBundle</code>s.
     * </p>
     */
    private Map<String, ResourceBundle> _cache = new HashMap<>();
}
