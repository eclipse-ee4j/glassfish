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

package com.sun.jsftemplating.layout.descriptors.handler;

import com.sun.jsftemplating.util.Util;

import jakarta.faces.context.FacesContext;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * <code>OutputTypeManager</code> manages the various {@link OutputType}s that can be used. The {@link OutputType}s are
 * managed statically.
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class OutputTypeManager {

    /**
     * Constructor.
     */
    protected OutputTypeManager() {
    }

    /**
     * <p>
     * Attempts to get the <code>FacesContext</code> and returns the results from {@link #getManager(FacesContext)}.
     * </p>
     */
    public static OutputTypeManager getInstance() {
        return getManager(FacesContext.getCurrentInstance());
    }

    /**
     * <p>
     * This is a factory method for obtaining an OutputTypeManager instance. This implementation uses the external context's
     * initParams to look for the OutputTypeManager class. If it exists, the specified concrete OutputTypeManager class will
     * be used. Otherwise, the default will be used -- which is an instance of this class. The initParam key is:
     * {@link #OUTPUT_TYPE_MANAGER_KEY}.
     * </p>
     *
     * @param context The FacesContext
     *
     * @see #OUTPUT_TYPE_MANAGER_KEY
     */
    public static OutputTypeManager getManager(FacesContext context) {
        if (context == null) {
            return _defaultInstance;
        }

        // If the context is non-null, check for init parameter specifying
        // the Manager
        String className = null;
        Map initParams = context.getExternalContext().getInitParameterMap();
        if (initParams.containsKey(OUTPUT_TYPE_MANAGER_KEY)) {
            className = (String) initParams.get(OUTPUT_TYPE_MANAGER_KEY);
        }
        return getManager(context, className);
    }

    /**
     * <p>
     * This method is a singleton factory method for obtaining an instance of an <code>OutputTypeManager</code>. It is
     * possible that multiple different implementations of <code>OutputTypeManager</code>s will be used within the same
     * application. This is fine. Someone may provide a different <code>OutputTypeManager</code> to locate
     * <code>OutputType</code>'s in a different way (XML, database, file, java code, etc.).
     * </p>
     */
    public static OutputTypeManager getManager(FacesContext ctx, String className) {
        if (className == null) {
            // Default case...
            return _defaultInstance;
        }
        OutputTypeManager ldm = null;

        // FacesContext
        if (ctx == null) {
            ctx = FacesContext.getCurrentInstance();
        }
        Map<String, OutputTypeManager> instances = null;
        if (ctx != null) {
            instances = (Map<String, OutputTypeManager>) ctx.getExternalContext().getApplicationMap().get(OTM_INSTANCES);
        }
        if (instances == null) {
            // NO instances defined yet...
            instances = new HashMap<>(2);
            if (ctx != null) {
                ctx.getExternalContext().getApplicationMap().put(OTM_INSTANCES, instances);
            }
        } else {
            // See if we've found this before...
            ldm = instances.get(className);
        }
        if (ldm == null) {
            // Not found yet, try to find it...
            try {
                ldm = (OutputTypeManager) Util.loadClass(className, className).getMethod("getInstance", (Class[]) null).invoke((Object) null, (Object[]) null);
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            } catch (NoSuchMethodException ex) {
                throw new RuntimeException(ex);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            } catch (InvocationTargetException ex) {
                throw new RuntimeException(ex);
            } catch (NullPointerException ex) {
                throw new RuntimeException(ex);
            } catch (ClassCastException ex) {
                throw new RuntimeException(ex);
            }

            // We found it
            instances.put(className, ldm);
        }
        return ldm;
    }

    /**
     * <p>
     * This method retrieves a <code>List</code> of {@link OutputType}. Changes to this <code>List</code> have no effect.
     * </p>
     *
     * @return The {@link OutputType}s.
     */
    public List<OutputType> getOutputTypes(FacesContext ctx) {
        return new ArrayList<>(getOutputTypeMap(ctx).values());
    }

    /**
     * <p>
     * Returns the application scope <code>Map</code> which holds all the {@link OutputType}s.
     * </p>
     */
    private Map<String, OutputType> getOutputTypeMap(FacesContext ctx) {
        if (ctx == null) {
            ctx = FacesContext.getCurrentInstance();
        }
        Map<String, OutputType> outputTypeMap = null;
        if (ctx != null) {
            outputTypeMap = (Map<String, OutputType>) ctx.getExternalContext().getApplicationMap().get(OTM_TYPE_MAP);
        }
        if (outputTypeMap == null) {
            // 1st time for this app... initialize it
            outputTypeMap = new HashMap<>(8);
            PageAttributeOutputType pageType = new PageAttributeOutputType();
            outputTypeMap.put(EL_TYPE, new ELOutputType());
            outputTypeMap.put(PAGE_ATTRIBUTE_TYPE, pageType);
            outputTypeMap.put(PAGE_ATTRIBUTE_TYPE2, pageType);
            outputTypeMap.put(APP_ATTRIBUTE_TYPE, new ApplicationAttributeOutputType());
            outputTypeMap.put(REQUEST_ATTRIBUTE_TYPE, new RequestAttributeOutputType());
            outputTypeMap.put(SESSION_ATTRIBUTE_TYPE, new SessionAttributeOutputType());
            if (ctx != null) {
                ctx.getExternalContext().getApplicationMap().put(OTM_TYPE_MAP, outputTypeMap);
            }
        }

        // Return the OutputType Map
        return outputTypeMap;
    }

    /**
     * <p>
     * This method retrieves an OutputType.
     * </p>
     *
     * @param name The name of the OutputType.
     *
     * @return The requested OutputType.
     */
    public OutputType getOutputType(FacesContext ctx, String name) {
        return getOutputTypeMap(ctx).get(name);
    }

    /**
     * <p>
     * This method sets an OutputType.
     * </p>
     *
     * @param name The name of the OutputType.
     * @param outputType The OutputType.
     */
    public void setOutputType(FacesContext ctx, String name, OutputType outputType) {
        // Not thread safe...
        getOutputTypeMap(ctx).put(name, outputType);
    }

    /**
     * <p>
     * This is the default implementation of the OutputTypeManager, which happens to be an instance of this class (because
     * I'm too lazy to do this right).
     * </p>
     */
    private static final OutputTypeManager _defaultInstance = new OutputTypeManager();

    /**
     * <p>
     * This constant defines the layout definition manager implementation key for initParams. The value for this initParam
     * should be the full class name of an {@link OutputTypeManager}. ("outputTypeManagerImpl")
     * </p>
     */
    public static final String OUTPUT_TYPE_MANAGER_KEY = "outputTypeManagerImpl";
    private static final String OTM_INSTANCES = "__jsft_OutputTypeManagers";
    private static final String OTM_TYPE_MAP = "__jsft_OutputType_map";

    public static final String REQUEST_ATTRIBUTE_TYPE = "attribute";
    public static final String PAGE_ATTRIBUTE_TYPE = "page";
    public static final String PAGE_ATTRIBUTE_TYPE2 = "pageSession";
    public static final String SESSION_ATTRIBUTE_TYPE = "session";
    public static final String APP_ATTRIBUTE_TYPE = "application";
    public static final String EL_TYPE = "el";
}
