/*
 * Copyright (c) 2007, 2019 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.webui.jsf.model;

/**
 * Specialized version of {@link Markup} that automatically surrounds
 * any accumulated markup in this element with the required prolog and
 * epilogue strings for an embedded script element.
 */
public final class ScriptMarkup extends Markup {

    /**
     * The CDATA wrapping flag for this markup.
     */
    private boolean cdata = false;

    /**
     * Return the current state of CDATA wrapping for this markup.
     * @return {@code true} if wrapping CDATA, {@code false} otherwise
     */
    public boolean isCdata() {
        return this.cdata;
    }

    /**
     * Set the new state of CDATA wrapping for this markup.
     * @param newCdata New wrapping flag
     */
    public void setCdata(final boolean newCdata) {
        this.cdata = newCdata;
    }

    /**
     * Return the accumulated markup for this element, surrounded by the
     * required prolog and epilog strings for an embedded script element.
     * @return String
     */
    @Override
    public String getMarkup() {
        StringBuilder sb =
                new StringBuilder("<script type=\"text/javascript\">");
        if (isCdata()) {
            sb.append("<![CDATA[");
        }
        sb.append("\n");
        sb.append(super.getMarkup());
        sb.append("\n");
        if (isCdata()) {
            sb.append("]]>");
        }
        sb.append("</script>\n");
        return sb.toString();
    }
}
