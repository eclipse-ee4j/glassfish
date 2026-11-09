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
import jakarta.faces.component.NamingContainer;
import jakarta.faces.context.FacesContext;

/**
 * Mastfooter component.
 */
@Component(type = "com.sun.webui.jsf.MastFooter",
        family = "com.sun.webui.jsf.MastFooter",
        displayName = "MastFooter Section",
        instanceName = "mastFooter",
        tagName = "mastFooter")
public final class MastFooter extends jakarta.faces.component.UIComponentBase
        implements NamingContainer {

    /**
     * CSS style(s) to be applied to the outermost HTML element when this
     * component is rendered.
     */
    @Property(name = "style",
            displayName = "CSS Style(s)")
    private String style;

    /**
     * CSS style class(es) to be applied to the outermost HTML element when this
     * component is rendered.
     */
    @Property(name = "styleClass",
            displayName = "CSS Style Class(es)")
    private String styleClass;

    /**
     * The URL to the image file to use for the Corporate Image. Use this
     * attribute to override the corporate image that is set in the theme.
     */
    @Property(name = "corporateImageURL",
            displayName = "Corporate Image URL",
            category = "Navigation",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.SunWebUrlPropertyEditor")
            //CHECKSTYLE:ON
    private String corporateImageURL;

    /**
     * The description for the Corporate Image, used as alt text for the image.
     */
    @Property(name = "corporateImageDescription",
            displayName = "Corporate Image Description",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String corporateImageDescription;

    /**
     * The height to use for the Corporate Image, in pixels. Use this attribute
     * when specifying the corporateImageURL, along with the corporateImageWidth
     * attribute, to specify dimensions of PNG images for use in Internet
     * Explorer.
     */
    @Property(name = "corporateImageHeight",
            displayName = "Corporate Image Height",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    private int corporateImageHeight = Integer.MIN_VALUE;

    /**
     * corporateImageHeight set flag.
     */
    private boolean corporateImageHeightSet;

    /**
     * The width to use for the Corporate Image URL, in pixels. Use this
     * attribute along with the corporateImageHeight attribute to specify
     * dimensions of PNG images for use in Internet Explorer.
     */
    @Property(name = "corporateImageWidth",
            displayName = "Corporate Image Width",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    private int corporateImageWidth = Integer.MIN_VALUE;

    /**
     * corporateImageWidth set flag.
     */
    private boolean corporateImageWidthSet;

    /**
     * Use the visible attribute to indicate whether the component should be
     * viewable by the user in the rendered HTML page. If set to false, the HTML
     * code for the component is present in the page, but the component is
     * hidden with style attributes. By default, visible is set to true, so HTML
     * for the component HTML is included and visible to the user. If the
     * component is not visible, it can still be processed on subsequent form
     * submissions because the HTML is present.
     */
    @Property(name = "visible",
            displayName = "Visible")
    private boolean visible;

    /**
     * visible set flag.
     */
    private boolean visibleSet;

    /**
     * Creates a new instance of MastFooter.
     */
    public MastFooter() {
        super();
        setRendererType("com.sun.webui.jsf.MastFooter");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.MastFooter";
    }

    /**
     * The component identifier for this component. This value must be unique
     * within the closest parent component that is a naming container.
     * @param id id
     */
    @Property(name = "id")
    @Override
    public void setId(final String id) {
        super.setId(id);
    }

    /**
     * Use the rendered attribute to indicate whether the HTML code for the
     * component should be included in the rendered HTML page. If set to false,
     * the rendered HTML page does not include the HTML for the component. If
     * the component is not rendered, it is also not processed on any subsequent
     * form submission.
     * @param rendered rendered
     */
    @Property(name = "rendered")
    @Override
    public void setRendered(final boolean rendered) {
        super.setRendered(rendered);
    }

    /**
     * CSS style(s) to be applied to the outermost HTML element when this
     * component is rendered.
     * @return String
     */
    public String getStyle() {
        if (this.style != null) {
            return this.style;
        }
        ValueExpression vb = getValueExpression("style");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * CSS style(s) to be applied to the outermost HTML element when this
     * component is rendered.
     *
     * @see #getStyle()
     * @param newStyle style
     */
    public void setStyle(final String newStyle) {
        this.style = newStyle;
    }

    /**
     * CSS style class(es) to be applied to the outermost HTML element when this
     * component is rendered.
     * @return String
     */
    public String getStyleClass() {
        if (this.styleClass != null) {
            return this.styleClass;
        }
        ValueExpression vb = getValueExpression("styleClass");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * CSS style class(es) to be applied to the outermost HTML element when this
     * component is rendered.
     *
     * @see #getStyleClass()
     * @param newStyleClass styleClass
     */
    public void setStyleClass(final String newStyleClass) {
        this.styleClass = newStyleClass;
    }

    /**
     * The URL to the image file to use for the Corporate Image. Use this
     * attribute to override the corporate image that is set in the theme.
     * @return String
     */
    public String getCorporateImageURL() {
        if (this.corporateImageURL != null) {
            return this.corporateImageURL;
        }
        ValueExpression vb = getValueExpression("corporateImageURL");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The URL to the image file to use for the Corporate Image. Use this
     * attribute to override the corporate image that is set in the theme.
     *
     * @see #getCorporateImageURL()
     * @param newCorporateImageURL corporateImageURL
     */
    public void setCorporateImageURL(final String newCorporateImageURL) {
        this.corporateImageURL = newCorporateImageURL;
    }

    /**
     * The description for the Corporate Image, used as alt text for the
     * image.
     * @return String
     */
    public String getCorporateImageDescription() {
        if (this.corporateImageDescription != null) {
            return this.corporateImageDescription;
        }
        ValueExpression vb = getValueExpression("corporateImageDescription");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The description for the Corporate Image, used as alt text for the
     * image.
     *
     * @see #getCorporateImageDescription()
     * @param newCorporateImageDescription corporateImageDescription
     */
    public void setCorporateImageDescription(
            final String newCorporateImageDescription) {
        this.corporateImageDescription = newCorporateImageDescription;
    }

    /**
     * The height to use for the Corporate Image, in pixels. Use this attribute
     * when specifying the corporateImageURL, along with the corporateImageWidth
     * attribute, to specify dimensions of PNG images for use in Internet
     * Explorer.
     * @return int
     */
    public int getCorporateImageHeight() {
        if (this.corporateImageHeightSet) {
            return this.corporateImageHeight;
        }
        ValueExpression vb = getValueExpression("corporateImageHeight");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return Integer.MIN_VALUE;
            } else {
                return ((Integer) result);
            }
        }
        return Integer.MIN_VALUE;
    }

    /**
     * The height to use for the Corporate Image, in pixels. Use this attribute
     * when specifying the corporateImageURL, along with the corporateImageWidth
     * attribute, to specify dimensions of PNG images for use in Internet
     * Explorer.
     *
     * @see #getCorporateImageHeight()
     * @param newCorporateImageHeight corporateImageHeight
     */
    public void setCorporateImageHeight(final int newCorporateImageHeight) {
        this.corporateImageHeight = newCorporateImageHeight;
        this.corporateImageHeightSet = true;
    }

    /**
     * The width to use for the Corporate Image URL, in pixels. Use this
     * attribute along with the corporateImageHeight attribute to specify
     * dimensions of PNG images for use in Internet Explorer.
     * @return int
     */
    public int getCorporateImageWidth() {
        if (this.corporateImageWidthSet) {
            return this.corporateImageWidth;
        }
        ValueExpression vb = getValueExpression("corporateImageWidth");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return Integer.MIN_VALUE;
            } else {
                return ((Integer) result);
            }
        }
        return Integer.MIN_VALUE;
    }

    /**
     * The width to use for the Corporate Image URL, in pixels. Use this
     * attribute along with the corporateImageHeight attribute to specify
     * dimensions of PNG images for use in Internet Explorer.
     *
     * @see #getCorporateImageWidth()
     * @param newCorporateImageWidth corporateImageWidth
     */
    public void setCorporateImageWidth(final int newCorporateImageWidth) {
        this.corporateImageWidth = newCorporateImageWidth;
        this.corporateImageWidthSet = true;
    }

    /**
     * Use the visible attribute to indicate whether the component should be
     * viewable by the user in the rendered HTML page. If set to false, the HTML
     * code for the component is present in the page, but the component is
     * hidden with style attributes. By default, visible is set to true, so HTML
     * for the component HTML is included and visible to the user. If the
     * component is not visible, it can still be processed on subsequent form
     * submissions because the HTML is present.
     * @return {@code boolean}
     */
    public boolean isVisible() {
        if (this.visibleSet) {
            return this.visible;
        }
        ValueExpression vb = getValueExpression("visible");
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
     * Use the visible attribute to indicate whether the component should be
     * viewable by the user in the rendered HTML page. If set to false, the HTML
     * code for the component is present in the page, but the component is
     * hidden with style attributes. By default, visible is set to true, so HTML
     * for the component HTML is included and visible to the user. If the
     * component is not visible, it can still be processed on subsequent form
     * submissions because the HTML is present.
     *
     * @see #isVisible()
     * @param newVisible visible
     */
    public void setVisible(final boolean newVisible) {
        this.visible = newVisible;
        this.visibleSet = true;
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.style = (String) values[1];
        this.styleClass = (String) values[2];
        this.corporateImageURL = (String) values[3];
        this.visible = ((Boolean) values[4]);
        this.visibleSet = ((Boolean) values[5]);
        this.corporateImageDescription = (String) values[6];
        this.corporateImageHeight = ((Integer) values[7]);
        this.corporateImageHeightSet = ((Boolean) values[8]);
        this.corporateImageWidth = ((Integer) values[9]);
        this.corporateImageWidthSet = ((Boolean) values[10]);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[11];
        values[0] = super.saveState(context);
        values[1] = this.style;
        values[2] = this.styleClass;
        values[3] = this.corporateImageURL;
        if (this.visible) {
            values[4] = Boolean.TRUE;
        } else {
            values[4] = Boolean.FALSE;
        }
        if (this.visibleSet) {
            values[5] = Boolean.TRUE;
        } else {
            values[5] = Boolean.FALSE;
        }
        values[6] = this.corporateImageDescription;
        values[7] = this.corporateImageHeight;
        if (this.corporateImageHeightSet) {
            values[8] = Boolean.TRUE;
        } else {
            values[8] = Boolean.FALSE;
        }
        values[9] = this.corporateImageWidth;
        if (this.corporateImageWidthSet) {
            values[10] = Boolean.TRUE;
        } else {
            values[10] = Boolean.FALSE;
        }
        return values;
    }
}
