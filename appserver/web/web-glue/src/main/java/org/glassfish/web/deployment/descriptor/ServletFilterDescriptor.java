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

package org.glassfish.web.deployment.descriptor;

import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.deployment.web.InitializationParameter;
import com.sun.enterprise.deployment.web.ServletFilter;
import org.glassfish.deployment.common.Descriptor;

import java.util.Collection;
import java.util.Vector;

/**
 * Deployment object representing the servlet filter spec
 * @author Martin D. Flynn
 */
public class ServletFilterDescriptor
extends Descriptor
implements ServletFilter
{

    /** class name */
    private String className = "";

    /** display name */
    private String displayName = "";

    /** filter name */
    private String filterName = "";

    /** initialization parameters */
    private Vector<InitializationParameter> initParms = new Vector<InitializationParameter>();

    /** async supported */
    private Boolean asyncSupported = null;

    private boolean conflict = false;

    /* ----
     */

    /** generic constructor */
    public ServletFilterDescriptor() {
        super("", ""/*description*/);
        this.setClassName("");
    }

    /** constructor specifying descriptor name (Filter name) & displayName */
    public ServletFilterDescriptor(String className, String name) {
        super(name, ""/*description*/);
        this.setClassName(className);
    }

    /* ----
     */

    /** set class name */
    public void setClassName(String name) {
        this.className = (name != null)? name : "";
    }

    /** get class name */
    public String getClassName() {
        if (this.className == null) {
            this.className = "";
        }
        return this.className;
    }

    /** set display name */
    public void setDisplayName(String name) {
        this.displayName = (name != null)? name : "";
    }

    /** get display name */
    public String getDisplayName() {
        String n = this.displayName;
        if ((n == null) || n.equals("")) {
            n = this.getName();
        }
        return n;
    }

    public boolean hasSetDisplayName() {
        return (displayName != null && displayName.length() > 0);
    }

    /** set filter name */
    public void setName(String filterName) {
        this.filterName = filterName;
    }

    /** get filter name */
    public String getName() {
        if ((filterName == null) || filterName.equals("")) {
            String c = this.getClassName();
            int p = c.lastIndexOf('.');
            filterName = (p < 0)? c : c.substring(p + 1);
        }
        return filterName;
    }

    /* ----
     */

    /* set initialization parameters */
    public void setInitializationParameters(Collection<InitializationParameter> c) {
        this.initParms.clear();
        this.initParms.addAll(c);
    }

    /* get initialization parameters */
    @SuppressWarnings("unchecked")
    public Vector<InitializationParameter> getInitializationParameters() {
        return (Vector<InitializationParameter>)this.initParms.clone();
    }

    /* add a single initialization parameter */
    public void addInitializationParameter(InitializationParameter ref) {
        this.initParms.addElement(ref);
    }

    /* add a single initialization parameter */
    public void addInitializationParameter(EnvironmentProperty ref) {
        addInitializationParameter((InitializationParameter) ref);
    }

    /* remove a single initialization parameter */
    public void removeInitializationParameter(InitializationParameter ref) {
        this.initParms.removeElement(ref);
    }

    /* set asyncSupported */
    public void setAsyncSupported(Boolean asyncSupported) {
        this.asyncSupported = asyncSupported;
    }

    public Boolean isAsyncSupported() {
        return asyncSupported;
    }

    public void setConflict(boolean conflict) {
        this.conflict = conflict;
    }

    public boolean isConflict() {
        return conflict;
    }

    public boolean isConflict(ServletFilterDescriptor other) {
        if (conflict || other.isConflict()) {
            return true;
        }

        if (!getName().equals(other.getName())) {
            return false;
        }

        // getClassName() cannot be null
        boolean matchClassName = (getClassName().length() == 0 ||
            other.getClassName().length() == 0 || getClassName().equals(other.getClassName()));

        boolean otherAsyncSupported = (other.isAsyncSupported() != null) ? other.isAsyncSupported() : false;
        boolean thisAsyncSupported = (asyncSupported != null) ? asyncSupported : false;
        boolean matchAsyncSupported = (thisAsyncSupported == otherAsyncSupported);

        return !(matchClassName && matchAsyncSupported);
    }

    /* ----
     */

    /** compare equals */
    public boolean equals(Object obj) {
        //Should allow a filter with different name mapping
        //to the same class.
        if (obj instanceof ServletFilter) {
            if (this.getClassName().equals(((ServletFilter) obj).getClassName())
                && this.getName().equals(((ServletFilter) obj).getName())) {
                return true;
            }
        }

        return false;
    }

    public int hashCode() {
        int result = 17;
        result = 37*result + getClassName().hashCode();
        result = 37*result + getName().hashCode();
        return result;
    }
}
