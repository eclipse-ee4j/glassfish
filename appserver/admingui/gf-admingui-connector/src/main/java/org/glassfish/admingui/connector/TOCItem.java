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
@Configured(name="tocitem")
public class TOCItem {

    /**
     * <p> Accessor for child {@link TOCItem}s.</p>
     */
    public List<TOCItem> getTOCItems() {
        return this.tocItems;
    }

    /**
     * <p> {@link IntegrationPoint}s setter.</p>
     */
    @Element("tocitem")
    public void setTOCItems(List<TOCItem> tocItems) {
        this.tocItems = tocItems;
    }

    /**
     *
     */
    public boolean isExpand() {
        return this.expand;
    }

    /**
     *
     */
    @Attribute(required=true)
    void setExpand(boolean expand) {
        this.expand = expand;
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
     * <p> This method returns the path to the target HTML page, starting
     *     with the moduleId.  It does not add anything before the module id,
     *     and does not have a leading '/' character.  It does append ".html"
     *     to the end of the target.</p>
     */
    public String getTargetPath() {
        return this.targetPath;
    }

    /**
     * <p> Sets the target path.  If the "target" is <code>foo</code>, the
     *     target path should look something like:
     *     <code>moduleId/en/help/foo.html</code></p>.  This value is NOT
     *     automatically set, it must be calculated and set during
     *     initialization code.</p>
     */
    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
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

    /**
     * <p> This method provides the "equals" functionality for TOCItem.  The
     *     behavior of equals ONLY compares the <code>target</code> value.
     *     The <code>text</code> and <code>expand</code> values are not used
     *     to test for equality.</p>
     */
    @Override
    public boolean equals(Object obj) {
        boolean result = false;
        if (obj instanceof TOCItem) {
            result = ((TOCItem) obj).getTarget().equals(getTarget());
        }
        return result;
    }

    /**
     * <p> This method is overriden to help ensure consistency for equals()
     *     comparisons.  As such it simply returns the hashCode of the String
     *     (target) that is used in the equals comparison.</p>
     */
    @Override
    public int hashCode() {
        return getTarget().hashCode();
    }

    @Override
    public String toString() {
        return getText() + " " + getTarget();
    }


    private boolean expand;
    private String target;
    private String targetPath;
    private String text;
    private List<TOCItem> tocItems;
}
