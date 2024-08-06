/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.results;

import com.sun.enterprise.v3.common.ActionReporter;

import org.glassfish.admin.rest.resources.LeafResource;
import org.glassfish.admin.rest.resources.LeafResource.LeafContent;
import org.glassfish.admin.rest.utils.xml.RestActionReporter;
import org.jvnet.hk2.config.ConfigBean;

/**
 * Response information object. Returned on call to GET methods on command resources. Information used by provider to
 * generate the appropriate output.
 *
 * @author Ludovic Champenois
 */
public class ActionReportResult extends Result {
    private final RestActionReporter message;
    private final OptionsResult metaData;
    private ConfigBean entity;
    private String commandDisplayName;
    private LeafResource.LeafContent leafContent;

    /**
     * Constructor
     */
    public ActionReportResult(RestActionReporter r) {
        this(null, r);
    }

    public ActionReportResult(RestActionReporter r, OptionsResult metaData) {
        this(null, r, metaData);
    }

    public ActionReportResult(RestActionReporter r, ConfigBean entity, OptionsResult metaData) {
        this(r, metaData);
        this.entity = entity;
    }

    public ActionReportResult(String name, RestActionReporter r) {
        this(name, r, new OptionsResult());
    }

    public ActionReportResult(String name, RestActionReporter r, OptionsResult metaData) {
        super(name, false, null);
        this.message = r;
        this.metaData = metaData;
    }

    public ActionReportResult(String name, RestActionReporter r, OptionsResult metaData, String displayName) {
        super(name, false, null);
        this.message = r;
        this.metaData = metaData;
        this.commandDisplayName = displayName;
    }

    /**
     * Returns the result string this object represents
     */
    public ActionReporter getActionReport() {
        return message;
    }

    /**
     * Returns display name for command associated with the command resource.
     */
    public String getCommandDisplayName() {
        return commandDisplayName;
    }

    /**
     * change display name for command associated with the command resource.
     */
    public void setCommandDisplayName(String s) {
        commandDisplayName = s;
    }

    /**
     * Returns OptionsResult - the meta-data of this resource.
     */
    public OptionsResult getMetaData() {
        return metaData;
    }

    public ConfigBean getEntity() {
        return entity;
    }

    public LeafContent getLeafContent() {
        return leafContent;
    }

    public void setLeafContent(LeafContent leafContent) {
        this.leafContent = leafContent;
    }
}
