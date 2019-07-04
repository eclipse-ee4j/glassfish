/*
 * Copyright (c) 1997, 2019 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment;


import com.sun.enterprise.deployment.util.DOLUtils;
import org.glassfish.internal.api.RelativePathResolver;

import java.beans.Introspector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectorConfigProperty extends EnvironmentProperty {

    private boolean ignore = false;
    private boolean supportsDynamicUpdates = false;
    private boolean confidential = false;
    private boolean setIgnoreCalled = false;
    private boolean setConfidentialCalled = false;
    private boolean setSupportsDynamicUpdatesCalled = false;

    private final static Logger _logger = DOLUtils.getDefaultLogger();

    /**
    ** Construct an connector  config-property if type String and empty string value and no description.
    */

    public ConnectorConfigProperty() {
    }  

     /**
    ** Construct an connector config-property of given name value and description.
    */

    public ConnectorConfigProperty(String name, String value, String description) {
	    this(name, value, description, null);
    }

    /**
    ** Construct an connector config-property of given name value and description and type.
    ** Throws an IllegalArgumentException if bounds checking is true and the value cannot be
    ** reconciled with the given type.
    */

    public ConnectorConfigProperty(String name, String value, String description, String type) {
        super(asPropertyName(name), value, description, type);
    }

    private static String asPropertyName(String s) {
        return Introspector.decapitalize(s);
    }

    public boolean isIgnore() {
        return ignore;
    }

    public void setIgnore(boolean ignore) {
        this.ignore = ignore;
        setSetIgnoreCalled(true);
    }

    public String getValue() {
        String value = super.getValue();
        if(confidential){
            try {
                return RelativePathResolver.getRealPasswordFromAlias(value);
            } catch (Exception e) {
                _logger.log(Level.WARNING,"Unable to resolve alias value [ "+value+" ] " +
                        "for connector config-property [ "+getName()+" ]", e);
            }
        }
        return value;
    }
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    @Override
    public boolean equals(Object other) {
       return super.equals(other);
    }

    public boolean isSupportsDynamicUpdates() {
        return supportsDynamicUpdates;
    }

    public void setSupportsDynamicUpdates(boolean supportsDynamicUpdates) {
        this.supportsDynamicUpdates = supportsDynamicUpdates;
        setSetSupportsDynamicUpdatesCalled(true);
    }

    public boolean isConfidential() {
        return confidential;
    }

    public void setConfidential(boolean confidential) {
        this.confidential = confidential;
        setSetConfidentialCalled(true);
    }

    public boolean isSetIgnoreCalled() {
        return setIgnoreCalled;
    }

    public void setSetIgnoreCalled(boolean setIgnoreCalled) {
        this.setIgnoreCalled = setIgnoreCalled;
    }

    public boolean isSetConfidentialCalled() {
        return setConfidentialCalled;
    }

    public void setSetConfidentialCalled(boolean setConfidentialCalled) {
        this.setConfidentialCalled = setConfidentialCalled;
    }

    public boolean isSetSupportsDynamicUpdatesCalled() {
        return setSupportsDynamicUpdatesCalled;
    }

    public void setSetSupportsDynamicUpdatesCalled(boolean setSupportsDynamicUpdatesCalled) {
        this.setSupportsDynamicUpdatesCalled = setSupportsDynamicUpdatesCalled;
    }

    @Override
    public void setName(String n) {
        super.setName(asPropertyName(n));
    }
}
