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
import com.sun.webui.jsf.util.ComponentUtilities;

import jakarta.el.ValueExpression;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

import java.util.Map;

/**
 * The ImageHyperlink component is used to display a hyperlinked image.
 */
@Component(type = "com.sun.webui.jsf.ImageHyperlink",
        family = "com.sun.webui.jsf.ImageHyperlink",
        displayName = "Image Hyperlink", tagName = "imageHyperlink",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_image_hyperlink",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_image_hyperlink_props")
        //CHECKSTYLE:ON
public class ImageHyperlink extends Hyperlink implements NamingContainer {

    /**
     * Image facet.
     */
    private static final String IMAGE_FACET = "image";

    /**
     * Used for identifying the facet in the facet map associated with this
     * component This is used as a suffix combined with the id of the component.
     */
    protected static final String IMAGE_FACET_SUFFIX = "_" + IMAGE_FACET;

    /**
     * Specifies the position of the image with respect to its context. Valid
     * values are: bottom (the default); middle; top; left; right.
     */
    @Property(name = "align",
            displayName = "Align",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.HtmlAlignEditor")
            //CHECKSTYLE:ON
    private String align;

    /**
     * Alternative textual description of the image rendered by this component.
     * The alt text can be used by screen readers and in tool tips, and when
     * image display is turned off in the web browser.
     */
    @Property(name = "alt",
            displayName = "Alt Text",
            category = "Accessibility",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String alt;

    /**
     * Specifies the width of the img border in pixels. The default value for
     * this attribute depends on the client browser.
     */
    @Property(name = "border",
            displayName = "Border",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    private int border = Integer.MIN_VALUE;

    /**
     * border set flag.
     */
    private boolean borderSet;

    /**
     * When specified, the width and height attributes tell the client browser
     * to override the natural image or object size in favor of these values,
     * specified in pixels. Some browsers might not support this behavior.
     */
    @Property(name = "height",
            displayName = "Height",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    private int height = Integer.MIN_VALUE;

    /**
     * height set flag.
     */
    private boolean heightSet;

    /**
     * Specifies the amount of white space in pixels to be inserted to the left
     * and right of the image. The default value is not specified but is
     * generally a small, non-zero size.
     */
    @Property(name = "hspace",
            displayName = "Horizontal Space",
            category = "Advanced",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    private int hspace = Integer.MIN_VALUE;

    /**
     * hspace set flag.
     */
    private boolean hspaceSet;

    /**
     * The identifier of the desired theme image.
     */
    @Property(name = "icon",
            displayName = "Icon",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.ThemeIconsEditor")
            //CHECKSTYLE:ON
    private String icon;

    /**
     * Absolute or relative URL to the image to be rendered.
     */
    @Property(name = "imageURL",
            displayName = "Image Url",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.ImageUrlPropertyEditor")
            //CHECKSTYLE:ON
    private String imageURL;

    /**
     * Specifies where the text will be placed relative to the image. The valid
     * values currently are "right" or "left".
     */
    @Property(name = "textPosition",
            displayName = "Text Position",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.HtmlHorizontalAlignEditor")
            //CHECKSTYLE:ON
    private String textPosition;

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
            displayName = "Visible",
            category = "Behavior")
    private boolean visible;

    /**
     * visible set flag.
     */
    private boolean visibleSet;

    /**
     * Specifies the amount of white space in pixels to be inserted above and
     * below the image. The default value is not specified but is generally a
     * small, non-zero size.
     */
    @Property(name = "vspace",
            displayName = "Vertical Space",
            category = "Advanced",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    private int vspace = Integer.MIN_VALUE;

    /**
     * vspace set flag.
     */
    private boolean vspaceSet;

    /**
     * Image width override. When specified, the width and height attributes
     * tell user agents to override the natural image or object size in favor of
     * these values.
     */
    @Property(name = "width",
            displayName = "Width",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    private int width = Integer.MIN_VALUE;

    /**
     * width set flag.
     */
    private boolean widthSet;

    /**
     * Default constructor.
     */
    public ImageHyperlink() {
        super();
        setRendererType("com.sun.webui.jsf.ImageHyperlink");
    }

    /**
     * This implementation returns {@code "com.sun.webui.jsf.ImageHyperlink"}.
     * @return String
     */
    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.ImageHyperlink";
    }

    // This facet is not meant to be overridden
    // by others, but is only used as a storage bin for keeping the image
    // associated with the hyperlink
    // This component is actually created every time and is not
    // stored in the facet map.
    //
    /**
     * Return a component that implements an image or an icon. If
     * {@code getImageURL()} returns a value that is not null, an
     * {@code ImageComponent} is returned. If {@code getImageURL()}
     * returns null and {@code getIcon()} returns a value that is not null,
     * an {@code Icon} component is returned. If both methods return null,
     * null is returned. The returned instance is initialized with the values
     * from
     *
     * <ul>
     * <li>{@code getImageURL()}</li>
     * <li>{@code getIcon()}</li>
     * <li>{@code getAlign()}</li>
     * <li>{@code getBorder()}</li>
     * <li>{@code getAlt()}</li>
     * <li>{@code getHeight()}</li>
     * <li>{@code getHspace()}</li>
     * <li>{@code getVspace()}</li>
     * <li>{@code getWidth()}</li>
     * <li>{@code getDisabled}</li>
     * <li>{@code getType()}</li>
     * </ul>
     *
     * <p>
     * The returned {@code ImageComponent} or {@code Icon} component
     * is created every time this method is called.
     * </p>
     *
     * @return ImageComponent or Icon instance
     */
    public UIComponent getImageFacet() {
        UIComponent image = ComponentUtilities
                .getPrivateFacet(this, IMAGE_FACET, false);
        if (image != null) {
            return image;
        }

        String url = getImageURL();
        String imgIcon = getIcon();

        if (url == null && imgIcon == null) {
            ComponentUtilities.removePrivateFacet(this,
                    IMAGE_FACET);
            return null;
        }

        // ImageURL takes precedence
        if (url != null) {
            image = new ImageComponent();
        } else {
            image = new Icon();
        }

        Map<String, Object> map = image.getAttributes();
        if (imgIcon != null) {
            map.put("icon", imgIcon);
        }
        if (url != null) {
            map.put("url", url);
        }

        setAttributes(ComponentUtilities
                .createPrivateFacetId(this, IMAGE_FACET), image);
        return image;
    }

    /**
     * Set the link attributes.
     * @param facetId facet id
     * @param image UI component containing the link attributes
     */
    protected void setAttributes(final String facetId,
            final UIComponent image) {

        //must reset the id always due to a side effect in JSF and putting
        //components in a table.
        image.setId(facetId);
        image.setParent(this);

        // align
        String alignAttr = getAlign();
        Map<String, Object> atts = image.getAttributes();
        if (alignAttr != null) {
            atts.put("align", alignAttr);
        }
        // border
        int dim = getBorder();
        if (dim >= 0) {
            atts.put("border", dim);
        }
        // description
        String description = getAlt();
        if (description != null) {
            atts.put("alt", description);
        }
        // height
        dim = getHeight();
        if (dim >= 0) {
            atts.put("height", dim);
        }
        // hspace
        dim = getHspace();
        if (dim >= 0) {
            atts.put("hspace", dim);
        }
        // vspace
        dim = getVspace();
        if (dim >= 0) {
            atts.put("vspace", dim);
        }
        // width
        dim = getWidth();
        if (dim >= 0) {
            atts.put("width", dim);
        }
        // disabled (based on parent)
        Boolean disabled = (Boolean) getAttributes().get("disabled");
        if (disabled != null) {
            atts.put("disabled", String.valueOf(disabled));
        }
    }

    /**
     * Specifies the position of the image with respect to its context. Valid
     * values are: bottom (the default); middle; top; left; right.
     * This implementation invokes {@code super.getOnDbleClick}.
     * @return String
     */
    @Property(name = "onDblClick", isHidden = true, isAttribute = true)
    @Override
    public String getOnDblClick() {
        return super.getOnDblClick();
    }

    /**
     * Specifies the position of the image with respect to its context. Valid
     * values are: bottom (the default); middle; top; left; right.
     * @return String
     */
    public String getAlign() {
        if (this.align != null) {
            return this.align;
        }
        ValueExpression vb = getValueExpression("align");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Specifies the position of the image with respect to its context. Valid
     * values are: bottom (the default); middle; top; left; right.
     *
     * @see #getAlign()
     * @param newAlign align
     */
    public void setAlign(final String newAlign) {
        this.align = newAlign;
    }

    /**
     * Alternative textual description of the image rendered by this component.
     * The alt text can be used by screen readers and in tool tips, and when
     * image display is turned off in the web browser.
     * @return String
     */
    public String getAlt() {
        if (this.alt != null) {
            return this.alt;
        }
        ValueExpression vb = getValueExpression("alt");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Alternative textual description of the image rendered by this component.
     * The alt text can be used by screen readers and in tool tips, and when
     * image display is turned off in the web browser.
     *
     * @see #getAlt()
     * @param newAlt alt
     */
    public void setAlt(final String newAlt) {
        this.alt = newAlt;
    }

    /**
     * Specifies the width of the img border in pixels. The default value for
     * this attribute depends on the client browser
     * @return int
     */
    public int getBorder() {
        if (this.borderSet) {
            return this.border;
        }
        ValueExpression vb = getValueExpression("border");
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
     * Specifies the width of the img border in pixels. The default value for
     * this attribute depends on the client browser
     *
     * @see #getBorder()
     * @param newBorder border
     */
    public void setBorder(final int newBorder) {
        this.border = newBorder;
        this.borderSet = true;
    }

    /**
     * When specified, the width and height attributes tell the client browser
     * to override the natural image or object size in favor of these values,
     * specified in pixels. Some browsers might not support this behavior.
     * @return int
     */
    public int getHeight() {
        if (this.heightSet) {
            return this.height;
        }
        ValueExpression vb = getValueExpression("height");
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
     * When specified, the width and height attributes tell the client browser
     * to override the natural image or object size in favor of these values,
     * specified in pixels. Some browsers might not support this behavior.
     *
     * @see #getHeight()
     * @param newHeight height
     */
    public void setHeight(final int newHeight) {
        this.height = newHeight;
        this.heightSet = true;
    }

    /**
     * Specifies the amount of white space in pixels to be inserted to the left
     * and right of the image. The default value is not specified but is
     * generally a small, non-zero size.
     * @return int
     */
    public int getHspace() {
        if (this.hspaceSet) {
            return this.hspace;
        }
        ValueExpression vb = getValueExpression("hspace");
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
     * Specifies the amount of white space in pixels to be inserted to the left
     * and right of the image. The default value is not specified but is
     * generally a small, non-zero size.
     *
     * @see #getHspace()
     * @param newHspace hspace
     */
    public void setHspace(final int newHspace) {
        this.hspace = newHspace;
        this.hspaceSet = true;
    }

    /**
     * The identifier of the desired theme image.
     * @return String
     */
    public String getIcon() {
        if (this.icon != null) {
            return this.icon;
        }
        ValueExpression vb = getValueExpression("icon");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The identifier of the desired theme image.
     *
     * @see #getIcon()
     * @param newIcon icon
     */
    public void setIcon(final String newIcon) {
        this.icon = newIcon;
    }

    /**
     * Absolute or relative URL to the image to be rendered.
     * @return String
     */
    public String getImageURL() {
        if (this.imageURL != null) {
            return this.imageURL;
        }
        ValueExpression vb = getValueExpression("imageURL");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Absolute or relative URL to the image to be rendered.
     *
     * @see #getImageURL()
     * @param newImageURL imageURL
     */
    public void setImageURL(final String newImageURL) {
        this.imageURL = newImageURL;
    }

    /**
     * Specifies where the text will be placed relative to the image. The valid
     * values currently are "right" or "left".
     * @return String
     */
    public String getTextPosition() {
        if (this.textPosition != null) {
            return this.textPosition;
        }
        ValueExpression vb = getValueExpression("textPosition");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return "right";
    }

    /**
     * Specifies where the text will be placed relative to the image. The valid
     * values currently are "right" or "left".
     *
     * @see #getTextPosition()
     * @param newTextPosition textPosition
     */
    public void setTextPosition(final String newTextPosition) {
        this.textPosition = newTextPosition;
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
    @Override
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
    @Override
    public void setVisible(final boolean newVisible) {
        this.visible = newVisible;
        this.visibleSet = true;
    }

    /**
     * Specifies the amount of white space in pixels to be inserted above and
     * below the image. The default value is not specified but is generally a
     * small, non-zero size.
     * @return int
     */
    public int getVspace() {
        if (this.vspaceSet) {
            return this.vspace;
        }
        ValueExpression vb = getValueExpression("vspace");
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
     * Specifies the amount of white space in pixels to be inserted above and
     * below the image. The default value is not specified but is generally a
     * small, non-zero size.
     *
     * @see #getVspace()
     * @param newVspace vspace
     */
    public void setVspace(final int newVspace) {
        this.vspace = newVspace;
        this.vspaceSet = true;
    }

    /**
     * Image width override. When specified, the width and height attributes
     * tell user agents to override the natural image or object size in favor of
     * these values.
     * @return int
     */
    public int getWidth() {
        if (this.widthSet) {
            return this.width;
        }
        ValueExpression vb = getValueExpression("width");
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
     * Image width override. When specified, the width and height attributes
     * tell user agents to override the natural image or object size in favor of
     * these values.
     *
     * @see #getWidth()
     * @param newWidth width
     */
    public void setWidth(final int newWidth) {
        this.width = newWidth;
        this.widthSet = true;
    }

    /**
     * This implementation restores the state of all properties.
     * @param context faces context
     * @param state state object
     */
    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.align = (String) values[1];
        this.alt = (String) values[2];
        this.border = ((Integer) values[3]);
        this.borderSet = ((Boolean) values[4]);
        this.height = ((Integer) values[5]);
        this.heightSet = ((Boolean) values[6]);
        this.hspace = ((Integer) values[7]);
        this.hspaceSet = ((Boolean) values[8]);
        this.icon = (String) values[9];
        this.imageURL = (String) values[10];
        this.textPosition = (String) values[11];
        this.visible = ((Boolean) values[12]);
        this.visibleSet = ((Boolean) values[13]);
        this.vspace = ((Integer) values[14]);
        this.vspaceSet = ((Boolean) values[15]);
        this.width = ((Integer) values[16]);
        this.widthSet = ((Boolean) values[17]);
    }

    /**
     * This implementation saves the state of all properties.
     * @param context faces context
     * @return Object
     */
    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[18];
        values[0] = super.saveState(context);
        values[1] = this.align;
        values[2] = this.alt;
        values[3] = this.border;
        if (this.borderSet) {
            values[4] = Boolean.TRUE;
        } else {
            values[4] = Boolean.FALSE;
        }
        values[5] = this.height;
        if (this.heightSet) {
            values[6] = Boolean.TRUE;
        } else {
            values[6] = Boolean.FALSE;
        }
        values[7] = this.hspace;
        if (this.hspaceSet) {
            values[8] = Boolean.TRUE;
        } else {
            values[8] = Boolean.FALSE;
        }
        values[9] = this.icon;
        values[10] = this.imageURL;
        values[11] = this.textPosition;
        if (this.visible) {
            values[12] = Boolean.TRUE;
        } else {
            values[12] = Boolean.FALSE;
        }
        if (this.visibleSet) {
            values[13] = Boolean.TRUE;
        } else {
            values[13] = Boolean.FALSE;
        }
        values[14] = this.vspace;
        if (this.vspaceSet) {
            values[15] = Boolean.TRUE;
        } else {
            values[15] = Boolean.FALSE;
        }
        values[16] = this.width;
        if (this.widthSet) {
            values[17] = Boolean.TRUE;
        } else {
            values[17] = Boolean.FALSE;
        }
        return values;
    }
}
