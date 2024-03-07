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
@Configured(name="ModulePrefs")
public class GadgetModulePrefs {

    /**
     * <p> Accessor for the known Admin Console
     *     {@link IntegrationPoint}s.<?p>
    public List<IntegrationPoint> getIntegrationPoints() {
        return this.integrationPoints;
    }
     */

    /**
     * <p> {@link IntegrationPoint}s setter.</p>
    @Element("integration-point")
    void setIntegrationPoints(List<IntegrationPoint> integrationPoints) {
        this.integrationPoints = integrationPoints;
    }
     */

    /**
     * <p> A unique identifier for the GadgetModule instance.</p>
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * <p> Setter for the title.</p>
     */
    @Attribute(value="title", required=false)
    void setTitle(String title) {
        this.title = title;
    }

    private String title;

    /**
     * <p> A unique identifier for the GadgetModule instance.</p>
     */
    public String getTitleUrl() {
        return this.titleUrl;
    }

    /**
     * <p> Setter for the titleUrl.</p>
     */
    @Attribute(value="title_url", required=false)
    void setTitleUrl(String titleUrl) {
        this.titleUrl = titleUrl;
    }

    private String titleUrl;

    /**
     * <p> A unique identifier for the GadgetModule instance.</p>
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * <p> Setter for the description.</p>
     */
    @Attribute(value="description", required=false)
    void setDescription(String description) {
        this.description = description;
    }

    private String description;

    /**
     * <p> A unique identifier for the GadgetModule instance.</p>
     */
    public String getAuthor() {
        return this.author;
    }

    /**
     * <p> Setter for the author.</p>
     */
    @Attribute(value="author", required=false)
    void setAuthor(String author) {
        this.author = author;
    }

    private String author;

    /**
     * <p> A unique identifier for the GadgetModule instance.</p>
     */
    public String getAuthorEmail() {
        return this.authorEmail;
    }

    /**
     * <p> Setter for the authorEmail.</p>
     */
    @Attribute(value="author_email", required=false)
    void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    private String authorEmail;

    /**
     * <p> A unique identifier for the GadgetModule instance.</p>
     */
    public String getScreenshot() {
        return this.screenshot;
    }

    /**
     * <p> Setter for the screenshot.</p>
     */
    @Attribute(value="screenshot", required=false)
    void setScreenshot(String screenshot) {
        this.screenshot = screenshot;
    }

    private String screenshot;

    /**
     * <p> A unique identifier for the GadgetModule instance.</p>
     */
    public String getThumbnail() {
        return this.thumbnail;
    }

    /**
     * <p> Setter for the thumbnail.</p>
     */
    @Attribute(value="thumbnail", required=false)
    void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    private String thumbnail;
}
