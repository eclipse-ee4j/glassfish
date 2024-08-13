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

import java.util.List;

import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;


/**
 *  <p>        This class is configured via XML.  This is done via the HK2
 * <code>ConfigParser</code>.  This is the root node of a "gadget".
 * The "text" can be retrived via getText (or #{module.text} via
 * EL).</p>
 *
 *  @author Ken Paulsen        (ken.paulsen@sun.com)
 */
@Configured(name="Module")
public class GadgetModule {

    /**
     * <p> Accessor for the {@link GadgetUserPref}s.</p>
     */
    public List<GadgetUserPref> getGadgetUserPrefs() {
        return this.userPrefs;
    }

    /**
     * <p> {@link GadgetUserPref}s setter.</p>
     */
    @Element("UserPref")
    void setGadgetUserPref(List<GadgetUserPref> userPrefs) {
        this.userPrefs = userPrefs;
    }

    private List<GadgetUserPref> userPrefs = null;

    /**
     * <p> Accessor for the {@link GadgetModulePrefs}.</p>
     */
    public GadgetModulePrefs getGadgetModulePrefs() {
        return this.prefs;
    }

    /**
     * <p> {@link GadgetModulePrefs} setter.</p>
     */
    @Element("ModulePrefs")
    void setGadgetModulePrefs(GadgetModulePrefs prefs) {
        this.prefs = prefs;
    }

    private GadgetModulePrefs prefs = null;

    /**
     * <p> A unique identifier for the content.</p>
    public GadgetContent getContent() {
        return this.content;
    }
     */

    /**
     * <p> Setter for the content.</p>
FIXME: I can't seem to get the attributes while also getting the body content...
    @Element("Content")
    void setContent(GadgetContent content) {
        this.content = content;
    }

    private GadgetContent content;
     */

    /**
     * <p> A unique identifier for the text.</p>
     */
    public String getText() {
        return this.text;
    }

    /**
     * <p> Setter for the text.</p>
     */
    @Element("Content")
    void setText(String text) {
        this.text = text;
    }

    private String text;
}
