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

package com.sun.appserv.web.cache;

import com.sun.appserv.web.cache.mapping.CacheMapping;
import com.sun.appserv.web.cache.mapping.ConstraintField;
import com.sun.appserv.web.cache.mapping.Field;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.web.LogFacade;

/** DefaultCacheHelper interface is the built-in implementation of the
 *  <code>CacheHelper</code> interface to aide in:
 *  a) the key generation b) whether to cache the response.
 *  There is one CacheHelper instance per web application.
 */
public class DefaultCacheHelper implements CacheHelper {

    private static final String[] KEY_PREFIXES = {
        "", "ca.", "rh.", "rp.", "rc.", "ra.", "sa.", "si." };

    public static final String ATTR_CACHING_FILTER_NAME =
                                "com.sun.ias.web.cachingFilterName";
    public static final String PROP_KEY_GENERATOR_ATTR_NAME =
                                "cacheKeyGeneratorAttrName";

    private static final Logger _logger = LogFacade.getLogger();

    ServletContext context;

    // cache manager
    CacheManager manager;

    String attrKeyGenerator = null;
    boolean isKeyGeneratorChecked = false;
    CacheKeyGenerator keyGenerator;

    /**
     * set the CacheManager for this application
     * @param manager associated with this application
     */
    public void setCacheManager(CacheManager manager) {
        this.manager = manager;
    }

    /***         CacheHelper methods          **/

    /**
     * initialize this helper
     * @param context the web application context this helper belongs to
     * @param props helper properties
     */
    public void init(ServletContext context, Map<String, String> props) {
        this.context = context;
        attrKeyGenerator = props.get(PROP_KEY_GENERATOR_ATTR_NAME);
    }

    /**
     * cache-mapping for this servlet-name or the URLpattern
     * @param request incoming request
     * @return cache-mapping object; uses servlet name or the URL pattern
     * to lookup in the CacheManager for the mapping.
     */
    private CacheMapping lookupCacheMapping(HttpServletRequest request) {
        String name = (String)request.getAttribute(ATTR_CACHING_FILTER_NAME);
        if (manager != null) {
            return manager.getCacheMapping(name);
        } else {
            return null;
        }
    }

    /** getCacheKey: generate the key to be used to cache this request
     *  @param request incoming <code>HttpServletRequest</code>
     *  @return key string used to access the cache entry.
     *  Key is composed of: servletPath + a concatenation of the field values in
     *  the request; all key field names must be found in the appropriate scope.
     */
    public String getCacheKey(HttpServletRequest request) {

        // cache mapping associated with the request
        CacheMapping mapping = lookupCacheMapping(request);

        if (isKeyGeneratorChecked == false && attrKeyGenerator != null) {
            try {
                keyGenerator = (CacheKeyGenerator)
                                context.getAttribute(attrKeyGenerator);
            } catch (ClassCastException cce){
                _logger.log(Level.WARNING, LogFacade.CACHE_DEFAULT_HELP_ILLEGAL_KET_GENERATOR, cce);
            }

            isKeyGeneratorChecked = true;
        }

        if (keyGenerator != null) {
            String key = keyGenerator.getCacheKey(context, request);
            if (key != null)
                return key;
        }

        StringBuilder sb = new StringBuilder(128);
        sb.append(request.getServletPath());

        // append the key fields
        Field[] keys = mapping.getKeyFields();
        for (int i = 0; i < keys.length; i++) {
            Object value = keys[i].getValue(context, request);

            // all defined key field must be present
            if (value == null) {
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.log(Level.FINE, LogFacade.REQUIRED_KEY_FIELDS_NOT_FOUND, request.getServletPath());
                }
                return null;
            }

            sb.append(";");
            sb.append(KEY_PREFIXES[keys[i].getScope()]);
            sb.append(keys[i].getName());
            sb.append("=");
            sb.append(value);
        }

        return sb.toString();
    }

    /** isCacheable: is the response to given request cachebale?
     *  @param request incoming <code>HttpServletRequest</code> object
     *  @return <code>true</code> if the response could be cached.
     *  or return <code>false</code> if the results of this request
     *  must not be cached.
     *
     *  Applies pre-configured cacheability constraints in the cache-mapping;
     *  all constraints must pass for this to be cacheable.
     */
    public boolean isCacheable(HttpServletRequest request) {
        boolean result = false;

        // cache mapping associated with the request
        CacheMapping mapping = lookupCacheMapping(request);

        // check if the method is in the allowed methods list
        if (mapping.findMethod(request.getMethod())) {
            result = true;

            ConstraintField fields[] = mapping.getConstraintFields();
            // apply all the constraints
            for (int i = 0; i < fields.length; i++) {
                if (!fields[i].applyConstraints(context, request)) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    /** isRefreshNeeded: is the response to given request be refreshed?
     *  @param request incoming <code>HttpServletRequest</code> object
     *  @return <code>true</code> if the response needs to be refreshed.
     *  or return <code>false</code> if the results of this request
     *  don't need to be refreshed.
     *
     *  XXX: 04/16/02 right now there is no configurability for this in
     *  ias-web.xml; should add a refresh-field element there:
     *  <refresh-field name="refresh" scope="request.parameter" />
     */
    public boolean isRefreshNeeded(HttpServletRequest request) {
        boolean result = false;

        // cache mapping associated with the request
        CacheMapping mapping = lookupCacheMapping(request);
        Field field = mapping.getRefreshField();
        if (field != null) {
            Object value = field.getValue(context, request);
            // the field's string representation must be "true" or "false"
            if (value != null && "true".equals(value.toString())) {
                result = true;
            }
        }
        return result;
    }

    /** get timeout for the cacheable data in this request
     *  @param request incoming <code>HttpServletRequest</code> object
     *  @return either the statically specified value or from the request
     *  fields. If not specified, get the timeout defined for the
     *  cache element.
     */
    public int getTimeout(HttpServletRequest request) {
        // cache mapping associated with the request
        CacheMapping mapping = lookupCacheMapping(request);

        // get the statically configured value, if any
        int result = mapping.getTimeout();

        // if the field is not defined, return the configured value
        Field field = mapping.getTimeoutField();
        if (field != null) {
            Object value = field.getValue(context, request);
            if (value != null) {
                try {
                    // Integer type timeout object
                    Integer timeoutAttr = Integer.valueOf(value.toString());
                    result = timeoutAttr.intValue();
                } catch (NumberFormatException cce) { }
            }
        }

        // Note: this could be CacheHelper.TIMEOUT_NOT_SET
        return result;
    }

    /**
     * Stop this Context component.
     * @exception Exception if a shutdown error occurs
     */
    public void destroy() throws Exception {
    }
}
