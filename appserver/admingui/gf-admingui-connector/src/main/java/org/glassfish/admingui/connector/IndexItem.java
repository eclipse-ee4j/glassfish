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

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;


/**
 *  <p>        This class is configured via XML (i.e. a console-config.xml file).
 *   This is done via the HK2 <code>ConfigParser</code>.</p>
 *
 *  @author Ken Paulsen        (ken.paulsen@sun.com)
 */
@Configured(name="indexitem")
public class IndexItem {

    /**
     * <p> Accessor for child {@link TOCItem}s.</p>
     */
    public List<IndexItem> getIndexItems() {
        return this.indexItems;
    }

    /**
     * <p> {@link IntegrationPoint}s setter.</p>
     */
    @Element("indexitem")
    public void setIndexItems(List<IndexItem> indexItems) {
        this.indexItems = indexItems;
    }

    /**
     *
     */
    public String getTarget() {
        return this.target;
    }

    /**
     *
     */
    @Attribute(required=true)
    void setTarget(String target) {
        this.target = target;
    }


    /**
     *
     */
    public String getText() {
        return this.text;
    }

    /**
     *
     */
    @Attribute(required=true)
    void setText(String text) {
        this.text = text;
    }

    public String getHtmlFileForTarget() {
        return htmlFileForTarget;
    }

    public void setHtmlFileForTarget(String htmlFileForTarget) {
        this.htmlFileForTarget = htmlFileForTarget;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IndexItem other = (IndexItem) obj;
        if ((this.target == null) ? (other.target != null) : !this.target.equals(other.target)) {
            return false;
        }
        if ((this.text == null) ? (other.text != null) : !this.text.equals(other.text)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + (this.target != null ? this.target.hashCode() : 0);
        hash = 89 * hash + (this.text != null ? this.text.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return getText() + " " + getTarget();
    }

    private String htmlFileForTarget;
    private String target;
    private String text;
    private List<IndexItem> indexItems;

}
