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

package org.glassfish.admingui.connector;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;


/**
 *  <p>        This class is configured via XML.  This is done via the HK2
 * <code>ConfigParser</code>.</p>
 *
 *  @author Ken Paulsen        (ken.paulsen@sun.com)
 */
@Configured(name="UserPref")
public class GadgetUserPref {

    /**
     * <p> Getter for the name.</p>
     */
    public String getName() {
        return this.name;
    }

    /**
     * <p> Required name of the user preference. Displayed during editing if
     *     no "display_name" is defined. Must only contain letters, number and
     *     underscores. The value must be unique for this gadget.</p>
     */
    @Attribute(value="name", required=true, key=true)
    void setName(String name) {
        this.name = name;
    }

    private String name;

    /**
     * <p> Getter for the display_name.</p>
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * <p> Optional string to display in the user preferences edit window.</p>
     */
    @Attribute(value="display_name", required=false)
    void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    private String displayName;

    /**
     * <p> Getter for the urlparam property.</p>
     */
    public String getURLParam() {
        return this.urlparam;
    }

    /**
     * <p> Optional string to pass as the parameter name for content
     *     type="url" (currently not supported).</p>
     */
    @Attribute(value="urlparam", required=false)
    void setURLParam(String urlparam) {
        this.urlparam = urlparam;
    }

    private String urlparam;

    /**
     * <p> Getter for the datatype property.</p>
     */
    public String getDataType() {
        return this.datatype;
    }

    /**
     * <p> Optional string that indicates the data type of this attribute.
     *     Can be string, bool, enum, hidden (not shown to user), or list
     *     (dynamic array generated from user input). The default is
     *     string.</p>
     */
    @Attribute(value="datatype", required=false)
    void setDataType(String datatype) {
        this.datatype = datatype;
    }

    private String datatype;

    /**
     * <p> Getter for the required property.</p>
     */
    public boolean getRequired() {
        return this.required;
    }

    /**
     * <p> Boolean property indicating if the preference is required. The
     *     default is false. </p>
     */
    @Attribute(value="required", required=false, dataType=Boolean.class, defaultValue="false")
    void setRequired(boolean required) {
        this.required = required;
    }

    private boolean required;

    /**
     * <p> Getter for the default value of this preference.</p>
     */
    public String getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * <p> Setter for the defaultValue.</p>
     */
    @Attribute(value="default_value", required=false)
    void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    private String defaultValue;
}
