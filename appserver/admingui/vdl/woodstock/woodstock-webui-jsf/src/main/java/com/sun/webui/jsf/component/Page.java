/*
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.webui.jsf.component;

import com.sun.faces.annotation.Component;
import com.sun.faces.annotation.Property;

import jakarta.el.ValueExpression;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.context.FacesContext;

/**
 * The Page component is used to indicate the beginning of the part of the JSP
 * page that is used by the Sun Java Web UI Components.
 */
@Component(type = "com.sun.webui.jsf.Page",
        family = "com.sun.webui.jsf.Page",
        displayName = "Page", tagName = "page",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_page",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_page_props")
        //CHECKSTYLE:ON
public final class Page extends UIComponentBase {

    /**
     * <span>Use the frame attribute to indicate whether the page should render
     * frames. If this attribute is true, the rendered HTML page includes a
     * </span>{@code &lt;frameset&gt;}<span> element. If false, the rendered
     * page uses a
     * </span>{@code &lt;body&gt;}<span> tag.&nbsp; This attribute also
     * influences the rendering of the {@code &lt;!DOCTYPE&gt;} attribute. If
     * frame set is true, the {@code &lt;!DOCTYPE&gt;} will be one of the
     * following, depending on the setting of {@code xhtml}
     * attribute.<br></span>
     * <pre>
     * &lt;!DOCTYPE html <br>
     * PUBLIC "-//W3C//DTD XHTML 1.0 Frameset//EN"<br>
     * "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd"&gt;<br>
     * &lt;!DOCTYPE html <br>
     * PUBLIC "-//W3C//DTD HTML 4.01 Frameset//EN"<br>
     * "http://www.w3.org/TR/html4/DTD/frameset.dtd"&gt;<br>
     * </pre>
     */
    @Property(name = "frame",
            displayName = "Needs Frame", category = "Appearance")
    private boolean frame = false;

    /**
     * frame set flag.
     */
    private boolean frameSet = false;

    /**
     * <span>XHTML transitional page or HTML
     * transitional page. This attribute influences the rendering of the
     * {@code &lt;!DOCTYPE&gt;} attribute. If {@code xhtml} is true, the
     * {@code &lt;!DOCTYPE&gt;} will be one of the following, depending on
     * the setting of {@code frameset} attribute.<br>
     * </span>
     * <pre>
     * &lt;!DOCTYPE html <br>
     * PUBLIC "-//W3C//DTD XHTML 1.0 Frameset//EN"<br>
     * "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd"
     * </pre>
     * <pre>
     * &lt;!DOCTYPE html <br>
     * PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"<br>
     * "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"&gt;
     * </pre>
     */
    @Property(name = "xhtml",
            displayName = "XHTML Transitional",
            category = "Appearance")
    private boolean xhtml = false;

    /**
     * xmhtml set flag.
     */
    private boolean xhtmlSet = false;

    /**
     * Create a new instance.
     */
    public Page() {
        super();
        setRendererType("com.sun.webui.jsf.Page");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.Page";
    }

    @Property(name = "id")
    @Override
    public void setId(final String id) {
        super.setId(id);
    }

    @Property(name = "rendered")
    @Override
    public void setRendered(final boolean rendered) {
        super.setRendered(rendered);
    }

    /**
     * <span>Use the frame attribute to indicate whether the page should render
     * frames.If this attribute is true, the rendered HTML page includes a
     * </span>{@code &lt;frameset&gt;}
     * <span>element. If false, the rendered page uses a
     * </span>{@code &lt;body&gt;}<span> tag.&nbsp; This attribute also
     * influences the rendering of the {@code &lt;!DOCTYPE&gt;} attribute. If
     * frame set is true, the {@code &lt;!DOCTYPE&gt;} will be one of the
     * following, depending on the setting of xhtml attribute.<br></span>
     * <pre>
     * &lt;!DOCTYPE html <br>
     * "PUBLIC-//W3C//DTD XHTML 1.0 Frameset//EN"<br>
     * "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd"&gt;<br>
     * &lt;!DOCTYPE html <br>
     * PUBLIC "-//W3C//DTD HTML 4.01 Frameset//EN"<br>
     * "http://www.w3.org/TR/html4/DTD/frameset.dtd"&gt;<br>
     * </pre>
     *
     * @return {@code boolean}
     */
    public boolean isFrame() {
        if (this.frameSet) {
            return this.frame;
        }
        ValueExpression vb = getValueExpression("frame");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return false;
            } else {
                return ((Boolean) result);
            }
        }
        return false;
    }

    /**
     * <span>Use the frame attribute to indicate
     * whether the page should render frames. If this attribute is true, the
     * rendered HTML page includes a </span>
     * {@code &lt;frameset&gt;}
     * <span>element. If false, the rendered page
     * uses a</span>{@code &lt;body&gt;}
     * <span> tag.&nbsp; This attribute also
     * influences the rendering of the {@code &lt;!DOCTYPE&gt;} attribute.
     * If frame set is true, the {@code &lt;!DOCTYPE&gt;} will be one of
     * the following, depending on the setting of {@code xhtml}
     * attribute.<br></span>
     * <pre>&lt;!DOCTYPE html <br>
     * PUBLIC "-//W3C//DTD XHTML 1.0 Frameset//EN"<br>
     * "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd"&gt;<br>
     * &lt;!DOCTYPE html<br>
     * PUBLIC "-//W3C//DTD HTML 4.01 Frameset//EN"<br>
     * "http://www.w3.org/TR/html4/DTD/frameset.dtd"&gt;<br>
     * </pre>
     *
     * @see #isFrame()
     * @param newFrame frame
     */
    public void setFrame(final boolean newFrame) {
        this.frame = newFrame;
        this.frameSet = true;
    }

    /**
     * <span>XHTML transitional page or HTML
     * transitional page.This attribute influences the rendering of the
     * {@code &lt;!DOCTYPE&gt;} attribute. If {@code xhtml} is true, the
     * {@code &lt;!DOCTYPE&gt;} will be one of the following, depending on
     * the setting of {@code frameset} attribute.<br>
     * </span>
     * <pre>
     * &lt;!DOCTYPE html <br>
     * PUBLIC "-//W3C//DTD XHTML 1.0 Frameset//EN"<br>
     * "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd"
     * </pre>
     * <pre>
     * &lt;!DOCTYPE html <br>
     * PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"<br>
     * "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"&gt;
     * </pre>
     *
     * @return {@code boolean}
     */
    public boolean isXhtml() {
        if (this.xhtmlSet) {
            return this.xhtml;
        }
        ValueExpression vb = getValueExpression("xhtml");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return false;
            } else {
                return ((Boolean) result);
            }
        }
        return true;
    }

    /**
     * <span>XHTML transitional page or HTML
     * transitional page.This attribute influences the rendering of the
     * {@code &lt;!DOCTYPE&gt;} attribute. If {@code xhtml} is true, the
     * {@code &lt;!DOCTYPE&gt;} will be one of the following, depending on
     * the setting of {@code frameset} attribute.<br>
     * </span>
     * <pre>
     * &lt;!DOCTYPE html <br>
     * PUBLIC "-//W3C//DTD XHTML 1.0 Frameset//EN"<br>
     * "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd"
     * </pre>
     * <pre>&lt;!DOCTYPE html<br>
     * PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"<br>
     * "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"&gt;
     * </pre>
     *
     * @param newXhtml xhtml
     * @see #isXhtml()
     */
    public void setXhtml(final boolean newXhtml) {
        this.xhtml = newXhtml;
        this.xhtmlSet = true;
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.frame = ((Boolean) values[1]);
        this.frameSet = ((Boolean) values[2]);
        this.xhtml = ((Boolean) values[3]);
        this.xhtmlSet = ((Boolean) values[4]);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[5];
        values[0] = super.saveState(context);
        if (this.frame) {
            values[1] = Boolean.TRUE;
        } else {
            values[1] = Boolean.FALSE;
        }
        if (this.frameSet) {
            values[2] = Boolean.TRUE;
        } else {
            values[2] = Boolean.FALSE;
        }
        if (this.xhtml) {
            values[3] = Boolean.TRUE;
        } else {
            values[3] = Boolean.FALSE;
        }
        if (this.xhtmlSet) {
            values[4] = Boolean.TRUE;
        } else {
            values[4] = Boolean.FALSE;
        }
        return values;
    }
}
