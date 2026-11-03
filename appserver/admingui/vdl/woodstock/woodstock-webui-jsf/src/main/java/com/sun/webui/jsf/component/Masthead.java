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
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.ComponentUtilities;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.el.ValueExpression;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIOutput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;

/**
 * The Masthead component displays a masthead or page banner at the top of the
 * page.
 */
@Component(type = "com.sun.webui.jsf.Masthead",
        family = "com.sun.webui.jsf.Masthead",
        displayName = "Masthead",
        tagName = "masthead",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_masthead",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_masthead_props")
        //CHECKSTYLE:ON
public final class Masthead extends UIOutput implements NamingContainer {

    /**
     * A JavaServer Faces EL expression that resolves to a backing bean or a
     * backing bean property that is an array of integers that specify the down,
     * critical, major, and minor alarm counts.
     */
    @Property(name = "alarmCounts",
            displayName = "Alarm Counts",
            category = "Behavior",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.binding.ValueBindingPropertyEditor")
            //CHECKSTYLE:ON
    private int[] alarmCounts = null;

    /**
     * The description to use for the Brand Image, used as alt text for the
     * image.
     */
    @Property(name = "brandImageDescription",
            displayName = "Brand Image Description",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String brandImageDescription = null;

    /**
     * The height to use for the Brand Image, in pixels. Use this attribute
     * along with the brandImageWidth attribute to specify dimensions of PNG
     * images for use in Internet Explorer.
     */
    @Property(name = "brandImageHeight",
            displayName = "Brand Image Height",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    private int brandImageHeight = Integer.MIN_VALUE;

    /**
     * brandImageHeight set flag.
     */
    private boolean brandImageHeightSet = false;

    /**
     * The URL to the image file to use as the Brand Image. Use this attribute
     * to override the brand image that is set in the theme.
     */
    @Property(name = "brandImageURL",
            displayName = "Brand Image URL",
            category = "Navigation",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.SunWebUrlPropertyEditor")
            //CHECKSTYLE:ON
    private String brandImageURL = null;

    /**
     * The width to use for the Brand Image, in pixels. Use this attribute when
     * specifying the brandImageURL, along with the brandImageHeight attribute,
     * to specify dimensions of PNG images for use in Internet Explorer.
     */
    @Property(name = "brandImageWidth",
            displayName = "Brand Image Width",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    private int brandImageWidth = Integer.MIN_VALUE;

    /**
     * brandImageWidth set flag.
     */
    private boolean brandImageWidthSet = false;

    /**
     * Set to true to display a date and time stamp in the status area.
     */
    @Property(name = "dateTime",
            displayName = "Date Time",
            category = "Advanced")
    private boolean dateTime = false;

    /**
     * dateTime set flag.
     */
    private boolean dateTimeSet = false;

    /**
     * The number of currently executing jobs or tasks. A JavaServer Faces EL
     * expression that resolves to a backing bean or a backing bean property
     * that is an integer.
     */
    @Property(name = "jobCount",
            displayName = "Job Count",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    private int jobCount = Integer.MIN_VALUE;

    /**
     * jobCount set flag.
     */
    private boolean jobCountSet = false;

    /**
     * Text to display for the notification info in the status area.
     */
    @Property(name = "notificationMsg",
            displayName = "Notification Message",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String notificationMsg = null;

    /**
     * The description for the product name image, used as alt text for the
     * image.
     */
    @Property(name = "productImageDescription",
            displayName = "Product Image Description",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String productImageDescription = null;

    /**
     * The height to use for the Product Name Image, in pixels. For mastheads
     * that are used in secondary windows, you might need to specify the
     * productImageHeight and productImageWidth for correct display on Internet
     * Explorer.
     */
    @Property(name = "productImageHeight",
            displayName = "Product Image Height",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    private int productImageHeight = Integer.MIN_VALUE;

    /**
     * productImageHeight set flag.
     */
    private boolean productImageHeightSet = false;

    /**
     * The URL to the image file to use for the Product Name Image.
     */
    @Property(name = "productImageURL",
            displayName = "Product Image URL",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.SunWebUrlPropertyEditor")
            //CHECKSTYLE:ON
    private String productImageURL = null;

    /**
     * The width to use for the Product Name Image, in pixels. For mastheads
     * that are used in secondary windows, you might need to specify the
     * productImageHeight and productImageWidth for correct display on Internet
     * Explorer.
     */
    @Property(name = "productImageWidth",
            displayName = "Product Image Width",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    private int productImageWidth = Integer.MIN_VALUE;

    /**
     * productImageWidth set flag.
     */
    private boolean productImageWidthSet = false;

    /**
     * Set to true to indicate that the masthead is to be used in a
     * secondary/popup window.
     */
    @Property(name = "secondary",
            displayName = "Is Secondary",
            category = "Advanced")
    private boolean secondary = false;

    /**
     * secondary set flag.
     */
    private boolean secondarySet = false;

    /**
     * Text to display for the current Server information, such as the name of
     * the server whose data is being displayed.
     */
    @Property(name = "serverInfo",
            displayName = "Current Server Info",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String serverInfo = null;

    /**
     * The label text to display for the current Server information.
     */
    @Property(name = "serverInfoLabel",
            displayName = "Current Server Info Label",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String serverInfoLabel = null;

    /**
     * CSS style(s) to be applied to the outermost HTML element when this
     * component is rendered.
     */
    @Property(name = "style",
            displayName = "CSS Style(s)",
            category = "Appearance",
            editorClassName = "com.sun.jsfcl.std.css.CssStylePropertyEditor")
    private String style = null;

    /**
     * CSS style class(es) to be applied to the outermost HTML element when this
     * component is rendered.
     */
    @Property(name = "styleClass",
            displayName = "CSS Style Class(es)",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StyleClassPropertyEditor")
            //CHECKSTYLE:ON
    private String styleClass = null;

    /**
     * Text to display for the current User information, such as the name of the
     * user who is running the application.
     */
    @Property(name = "userInfo",
            displayName = "Current User Info",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String userInfo = null;

    /**
     * The label text to display for the current User information.
     */
    @Property(name = "userInfoLabel",
            displayName = "Current User Info Label",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String userInfoLabel = null;

    /**
     * Text to display for the current Role information, such as the name of the
     * user who is running the application.
     */
    @Property(name = "roleInfo",
            displayName = "Current Role Info",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String roleInfo = null;

    /**
     * The label text to display for the current User information.
     */
    @Property(name = "roleInfoLabel",
            displayName = "Current Role Info Label",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String roleInfoLabel = null;

    /**
     * A JavaServer Faces EL expression that resolves to a backing bean or a
     * backing bean property that is an array of one or more custom Hyperlink
     * components to display in the utility bar. The Hyperlink components must
     * be given ids.
     */
    @Property(name = "utilities",
            displayName = "Utility Bar Links",
            category = "Behavior",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.binding.ValueBindingPropertyEditor")
            //CHECKSTYLE:ON
    private com.sun.webui.jsf.component.Hyperlink[] utilities = null;

    /**
     * Use the visible attribute to indicate whether the component should be
     * viewable by the user in the rendered HTML page. If set to false, the HTML
     * code for the component is present in the page, but the component is
     * hidden with style attributes. By default, visible is set to true, so HTML
     * for the component HTML is included and visible to the user. If the
     * component is not visible, it can still be processed on subsequent form
     * submissions because the HTML is present.
     */
    @Property(name = "visible", displayName = "Visible")
    private boolean visible = false;

    /**
     * visible set flag.
     */
    private boolean visibleSet = false;

    /**
     * Default constructor.
     */
    public Masthead() {
        super();
        setRendererType("com.sun.webui.jsf.Masthead");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.Masthead";
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
     */
    @Property(name = "rendered")
    @Override
    public void setRendered(final boolean rendered) {
        super.setRendered(rendered);
    }

    // Hide converter
    @Property(name = "converter", isHidden = true, isAttribute = false)
    @Override
    public Converter getConverter() {
        return super.getConverter();
    }

    // Hide value
    @Property(name = "value", isHidden = true, isAttribute = false)
    @Override
    public Object getValue() {
        return super.getValue();
    }

    /**
     * A JavaServer Faces EL expression that resolves to a backing bean or a
     * backing bean property that is an array of integers that specify the down,
     * critical, major, and minor alarm counts.
     * @return int[]
     */
    public int[] getAlarmCounts() {
        if (this.alarmCounts != null) {
            return this.alarmCounts;
        }
        ValueExpression vb = getValueExpression("alarmCounts");
        if (vb != null) {
            return (int[]) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * A JavaServer Faces EL expression that resolves to a backing bean or a
     * backing bean property that is an array of integers that specify the down,
     * critical, major, and minor alarm counts.
     *
     * @see #getAlarmCounts()
     * @param newAlarmCounts alarmCounts
     */
    public void setAlarmCounts(final int[] newAlarmCounts) {
        this.alarmCounts = newAlarmCounts;
    }

    /**
     * The description to use for the Brand Image, used as alt text for the
     * image.
     * @return String
     */
    public String getBrandImageDescription() {
        if (this.brandImageDescription != null) {
            return this.brandImageDescription;
        }
        ValueExpression vb = getValueExpression("brandImageDescription");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The description to use for the Brand Image, used as alt text for the
     * image.
     *
     * @see #getBrandImageDescription()
     * @param newBrandImageDescription brandImageDescription
     */
    public void setBrandImageDescription(
            final String newBrandImageDescription) {

        this.brandImageDescription = newBrandImageDescription;
    }

    /**
     * The height to use for the Brand Image, in pixels. Use this attribute
     * along with the brandImageWidth attribute to specify dimensions of PNG
     * images for use in Internet Explorer.
     * @return int
     */
    public int getBrandImageHeight() {
        if (this.brandImageHeightSet) {
            return this.brandImageHeight;
        }
        ValueExpression vb = getValueExpression("brandImageHeight");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return Integer.MIN_VALUE;
            } else {
                return ((Integer) result).intValue();
            }
        }
        return Integer.MIN_VALUE;
    }

    /**
     * The height to use for the Brand Image, in pixels. Use this attribute
     * along with the brandImageWidth attribute to specify dimensions of PNG
     * images for use in Internet Explorer.
     *
     * @see #getBrandImageHeight()
     * @param newBrandImageHeight brandImageHeight
     */
    public void setBrandImageHeight(final int newBrandImageHeight) {
        this.brandImageHeight = newBrandImageHeight;
        this.brandImageHeightSet = true;
    }

    /**
     * The URL to the image file to use as the Brand Image. Use this attribute
     * to override the brand image that is set in the theme.
     * @return String
     */
    public String getBrandImageURL() {
        if (this.brandImageURL != null) {
            return this.brandImageURL;
        }
        ValueExpression vb = getValueExpression("brandImageURL");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The URL to the image file to use as the Brand Image. Use this attribute
     * to override the brand image that is set in the theme.
     *
     * @see #getBrandImageURL()
     * @param newBrandImageURL brandImageURL
     */
    public void setBrandImageURL(final String newBrandImageURL) {
        this.brandImageURL = newBrandImageURL;
    }

    /**
     * The width to use for the Brand Image, in pixels. Use this attribute when
     * specifying the brandImageURL, along with the brandImageHeight attribute,
     * to specify dimensions of PNG images for use in Internet Explorer.
     * @return int
     */
    public int getBrandImageWidth() {
        if (this.brandImageWidthSet) {
            return this.brandImageWidth;
        }
        ValueExpression vb = getValueExpression("brandImageWidth");
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
     * The width to use for the Brand Image, in pixels. Use this attribute when
     * specifying the brandImageURL, along with the brandImageHeight attribute,
     * to specify dimensions of PNG images for use in Internet Explorer.
     *
     * @see #getBrandImageWidth()
     * @param newBrandImageWidth brandImageWidth
     */
    public void setBrandImageWidth(final int newBrandImageWidth) {
        this.brandImageWidth = newBrandImageWidth;
        this.brandImageWidthSet = true;
    }

    /**
     * Set to true to display a date and time stamp in the status area.
     * @return {@code boolean}
     */
    public boolean isDateTime() {
        if (this.dateTimeSet) {
            return this.dateTime;
        }
        ValueExpression vb = getValueExpression("dateTime");
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
     * Set to true to display a date and time stamp in the status area.
     *
     * @see #isDateTime()
     * @param newDateTime dateTime
     */
    public void setDateTime(final boolean newDateTime) {
        this.dateTime = newDateTime;
        this.dateTimeSet = true;
    }

    /**
     * The number of currently executing jobs or tasks. A JavaServer Faces EL
     * expression that resolves to a backing bean or a backing bean property
     * that is an integer.
     * @return int
     */
    public int getJobCount() {
        if (this.jobCountSet) {
            return this.jobCount;
        }
        ValueExpression vb = getValueExpression("jobCount");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return Integer.MIN_VALUE;
            } else {
                return ((Integer) result);
            }
        }
        return -1;
    }

    /**
     * The number of currently executing jobs or tasks. A JavaServer Faces EL
     * expression that resolves to a backing bean or a backing bean property
     * that is an integer.
     *
     * @see #getJobCount()
     * @param newJobCount jobCount
     */
    public void setJobCount(final int newJobCount) {
        this.jobCount = newJobCount;
        this.jobCountSet = true;
    }

    /**
     * Text to display for the notification info in the status area.
     * @return String
     */
    public String getNotificationMsg() {
        if (this.notificationMsg != null) {
            return this.notificationMsg;
        }
        ValueExpression vb = getValueExpression("notificationMsg");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Text to display for the notification info in the status area.
     *
     * @see #getNotificationMsg()
     * @param newNotificationMsg notificationMsg
     */
    public void setNotificationMsg(final String newNotificationMsg) {
        this.notificationMsg = newNotificationMsg;
    }

    /**
     * The description for the product name image, used as alt text for the
     * image.
     * @return String
     */
    public String getProductImageDescription() {
        if (this.productImageDescription != null) {
            return this.productImageDescription;
        }
        ValueExpression vb = getValueExpression("productImageDescription");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The description for the product name Image, used as alt text for the
     * image.
     *
     * @see #getProductImageDescription()
     * @param newProductImageDescription productImageDescription
     */
    public void setProductImageDescription(
            final String newProductImageDescription) {

        this.productImageDescription = newProductImageDescription;
    }

    /**
     * The height to use for the Product Name Image, in pixels. For mastheads
     * that are used in secondary windows, you might need to specify the
     * productImageHeight and productImageWidth for correct display on Internet
     * Explorer.
     * @return int
     */
    public int getProductImageHeight() {
        if (this.productImageHeightSet) {
            return this.productImageHeight;
        }
        ValueExpression vb = getValueExpression("productImageHeight");
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
     * The height to use for the Product Name Image, in pixels. For mastheads
     * that are used in secondary windows, you might need to specify the
     * productImageHeight and productImageWidth for correct display on Internet
     * Explorer.
     *
     * @see #getProductImageHeight()
     * @param newProductImageHeight productImageHeight
     */
    public void setProductImageHeight(final int newProductImageHeight) {
        this.productImageHeight = newProductImageHeight;
        this.productImageHeightSet = true;
    }

    /**
     * The URL to the image file to use for the Product Name Image.
     * @return String
     */
    public String getProductImageURL() {
        if (this.productImageURL != null) {
            return this.productImageURL;
        }
        ValueExpression vb = getValueExpression("productImageURL");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The URL to the image file to use for the Product Name Image.
     *
     * @see #getProductImageURL()
     * @param newProductImageURL productImageURL
     */
    public void setProductImageURL(final String newProductImageURL) {
        this.productImageURL = newProductImageURL;
    }

    /**
     * The width to use for the Product Name Image, in pixels. For mastheads
     * that are used in secondary windows, you might need to specify the
     * productImageHeight and productImageWidth for correct display on Internet
     * Explorer.
     * @return int
     */
    public int getProductImageWidth() {
        if (this.productImageWidthSet) {
            return this.productImageWidth;
        }
        ValueExpression vb = getValueExpression("productImageWidth");
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
     * The width to use for the Product Name Image, in pixels. For mastheads
     * that are used in secondary windows, you might need to specify the
     * productImageHeight and productImageWidth for correct display on Internet
     * Explorer.
     *
     * @see #getProductImageWidth()
     * @param newProductImageWidth productImageWidth
     */
    public void setProductImageWidth(final int newProductImageWidth) {
        this.productImageWidth = newProductImageWidth;
        this.productImageWidthSet = true;
    }

    /**
     * Set to true to indicate that the masthead is to be used in a
     * secondary/popup window.
     * @return {@code boolean}
     */
    public boolean isSecondary() {
        if (this.secondarySet) {
            return this.secondary;
        }
        ValueExpression vb = getValueExpression("secondary");
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
     * Set to true to indicate that the masthead is to be used in a
     * secondary/popup window.
     *
     * @see #isSecondary()
     * @param newSecondary secondary
     */
    public void setSecondary(final boolean newSecondary) {
        this.secondary = newSecondary;
        this.secondarySet = true;
    }

    /**
     * Text to display for the current Server information, such as the name of
     * the server whose data is being displayed.
     * @return String
     */
    public String getServerInfo() {
        if (this.serverInfo != null) {
            return this.serverInfo;
        }
        ValueExpression vb = getValueExpression("serverInfo");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Text to display for the current Server information, such as the name of
     * the server whose data is being displayed.
     *
     * @see #getServerInfo()
     * @param newServerInfo serverInfo
     */
    public void setServerInfo(final String newServerInfo) {
        this.serverInfo = newServerInfo;
    }

    /**
     * The label text to display for the current Server information.
     * @return String
     */
    public String getServerInfoLabel() {
        if (this.serverInfoLabel != null) {
            return this.serverInfoLabel;
        }
        ValueExpression vb = getValueExpression("serverInfoLabel");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The label text to display for the current Server information.
     *
     * @see #getServerInfoLabel()
     * @param newServerInfoLabel serverInfoLabel
     */
    public void setServerInfoLabel(final String newServerInfoLabel) {
        this.serverInfoLabel = newServerInfoLabel;
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
     * Text to display for the current User information, such as the name of the
     * user who is running the application.
     * @return String
     */
    public String getUserInfo() {
        if (this.userInfo != null) {
            return this.userInfo;
        }
        ValueExpression vb = getValueExpression("userInfo");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Text to display for the current User information, such as the name of the
     * user who is running the application.
     *
     * @see #getUserInfo()
     * @param newUserInfo userInfo
     */
    public void setUserInfo(final String newUserInfo) {
        this.userInfo = newUserInfo;
    }

    /**
     * The label text to display for the current User information.
     * @return String
     */
    public String getUserInfoLabel() {
        if (this.userInfoLabel != null) {
            return this.userInfoLabel;
        }
        ValueExpression vb = getValueExpression("userInfoLabel");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The label text to display for the current User information.
     *
     * @see #getUserInfoLabel()
     * @param newUserInfoLabel userInfoLabel
     */
    public void setUserInfoLabel(final String newUserInfoLabel) {
        this.userInfoLabel = newUserInfoLabel;
    }

    /**
     * Getter method to get Role information, such as the name of the role who
     * is running the application.
     * @return String
     */
    public String getRoleInfo() {
        if (this.roleInfo != null) {
            return this.roleInfo;
        }
        ValueExpression vb = getValueExpression("roleInfo");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Set the current Role information, such as the name of the role who is
     * running the application.
     *
     * @see #getRoleInfo()
     * @param newRoleInfo roleInfo
     */
    public void setRoleInfo(final String newRoleInfo) {
        this.roleInfo = newRoleInfo;
    }

    /**
     * The label text to display for the current Role information.
     * @return String
     */
    public String getRoleInfoLabel() {
        if (this.roleInfoLabel != null) {
            return this.roleInfoLabel;
        }
        ValueExpression vb = getValueExpression("roleInfoLabel");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The label text to display for the current Role information.
     *
     * @see #getRoleInfoLabel()
     * @param newRoleInfoLabel roleInfoLabel
     */
    public void setRoleInfoLabel(final String newRoleInfoLabel) {
        this.roleInfoLabel = newRoleInfoLabel;
    }

    /**
     * A JavaServer Faces EL expression that resolves to a backing bean or a
     * backing bean property that is an array of one or more custom Hyperlink
     * components to display in the utility bar. The Hyperlink components must
     * be given ids.
     * @return {@code com.sun.webui.jsf.component.Hyperlink[]}
     */
    public com.sun.webui.jsf.component.Hyperlink[] getUtilities() {
        if (this.utilities != null) {
            return this.utilities;
        }
        ValueExpression vb = getValueExpression("utilities");
        if (vb != null) {
            return (com.sun.webui.jsf.component.Hyperlink[]) vb
                    .getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * A JavaServer Faces EL expression that resolves to a backing bean or a
     * backing bean property that is an array of one or more custom Hyperlink
     * components to display in the utility bar. The Hyperlink components must
     * be given ids.
     *
     * @see #getUtilities()
     * @param newUtilities utilities
     */
    public void setUtilities(
            final com.sun.webui.jsf.component.Hyperlink[] newUtilities) {

        this.utilities = newUtilities;
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

    /**
     * Get the masthead jobcount hyperlink.
     *
     * @return UIComponent
     */
    public UIComponent getJobCountLink() {
        FacesContext context = FacesContext.getCurrentInstance();
        Theme theme = ThemeUtilities.getTheme(context);
        Hyperlink jcLink = (Hyperlink) ComponentUtilities
                .getPrivateFacet(this, "jobCountHyperlink", true);
        if (jcLink == null) {
            jcLink = new Hyperlink();
            jcLink.setId(ComponentUtilities
                    .createPrivateFacetId(this, "jobCountHyperlink"));
            ComponentUtilities
                    .putPrivateFacet(this, "jobCountHyperlink", jcLink);
        }
        jcLink.setStyleClass(theme
                .getStyleClass(ThemeStyles.MASTHEAD_PROGRESS_LINK));
        jcLink.setText(theme.getMessage("masthead.tasksRunning")
                + " " + getJobCount());
        return jcLink;
    }

    @Override
    public void processDecodes(final FacesContext context) {
        if (this.isRendered()) {
            if (this.getUtilities() != null) {
                for (Hyperlink hyperlink : this.getUtilities()) {
                    UIComponent parent = hyperlink.getParent();
                    hyperlink.setParent(this);
                    hyperlink.processDecodes(context);
                    hyperlink.setParent(parent);
                }
            }
            super.processDecodes(context);
        }
    }

    @Override
    public void processValidators(final FacesContext context) {
        if (this.isRendered()) {
            if (this.getUtilities() != null) {
                for (Hyperlink hyperlink : this.getUtilities()) {
                    UIComponent parent = hyperlink.getParent();
                    hyperlink.setParent(this);
                    hyperlink.processValidators(context);
                    hyperlink.setParent(parent);
                }
            }
            super.processValidators(context);
        }
    }

    @Override
    public void processUpdates(final FacesContext context) {
        if (this.isRendered()) {
            if (this.getUtilities() != null) {
                for (Hyperlink hyperlink : this.getUtilities()) {
                    UIComponent parent = hyperlink.getParent();
                    hyperlink.setParent(this);
                    hyperlink.processUpdates(context);
                    hyperlink.setParent(parent);
                }
            }
            super.processUpdates(context);
        }
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.alarmCounts = (int[]) values[1];
        this.brandImageDescription = (String) values[2];
        this.brandImageHeight = ((Integer) values[3]);
        this.brandImageHeightSet = ((Boolean) values[4]);
        this.brandImageURL = (String) values[5];
        this.brandImageWidth = ((Integer) values[6]);
        this.brandImageWidthSet = ((Boolean) values[7]);
        this.dateTime = ((Boolean) values[8]);
        this.dateTimeSet = ((Boolean) values[9]);
        this.jobCount = ((Integer) values[10]);
        this.jobCountSet = ((Boolean) values[11]);
        this.notificationMsg = (String) values[12];
        this.productImageDescription = (String) values[13];
        this.productImageHeight = ((Integer) values[14]);
        this.productImageHeightSet = ((Boolean) values[15]);
        this.productImageURL = (String) values[16];
        this.productImageWidth = ((Integer) values[17]);
        this.productImageWidthSet = ((Boolean) values[18]);
        this.secondary = ((Boolean) values[19]);
        this.secondarySet = ((Boolean) values[20]);
        this.serverInfo = (String) values[21];
        this.serverInfoLabel = (String) values[22];
        this.style = (String) values[23];
        this.styleClass = (String) values[24];
        this.userInfo = (String) values[25];
        this.userInfoLabel = (String) values[26];
        if (values[27] != null) {
            Object[] linkValues = (Object[]) values[27];
            this.utilities = new Hyperlink[linkValues.length];
            for (int i = 0; i < linkValues.length; i++) {
                this.utilities[i] = new Hyperlink();
                this.utilities[i].restoreState(context, linkValues[i]);
            }
        }
        this.visible = ((Boolean) values[28]);
        this.visibleSet = ((Boolean) values[29]);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[36];
        values[0] = super.saveState(context);
        values[1] = this.alarmCounts;
        values[2] = this.brandImageDescription;
        values[3] = this.brandImageHeight;
        if (this.brandImageHeightSet) {
            values[4] = Boolean.TRUE;
        } else {
            values[4] = Boolean.FALSE;
        }
        values[5] = this.brandImageURL;
        values[6] = this.brandImageWidth;
        if (this.brandImageWidthSet) {
            values[7] = Boolean.TRUE;
        } else {
            values[7] = Boolean.FALSE;
        }
        if (this.dateTime) {
            values[8] = Boolean.TRUE;
        } else {
            values[8] = Boolean.FALSE;
        }
        if (this.dateTimeSet) {
            values[9] = Boolean.TRUE;
        } else {
            values[9] = Boolean.FALSE;
        }
        values[10] = this.jobCount;
        if (this.jobCountSet) {
            values[11] = Boolean.TRUE;
        } else {
            values[11] = Boolean.FALSE;
        }
        values[12] = this.notificationMsg;
        values[13] = this.productImageDescription;
        values[14] = this.productImageHeight;
        if (this.productImageHeightSet) {
            values[15] = Boolean.TRUE;
        } else {
            values[15] = Boolean.FALSE;
        }
        values[16] = this.productImageURL;
        values[17] = this.productImageWidth;
        if (this.productImageHeightSet) {
            values[18] = Boolean.TRUE;
        } else {
            values[18] = Boolean.FALSE;
        }
        if (this.secondary) {
            values[19] = Boolean.TRUE;
        } else {
            values[19] = Boolean.FALSE;
        }
        if (this.secondarySet) {
            values[20] = Boolean.TRUE;
        } else {
            values[20] = Boolean.FALSE;
        }
        values[21] = this.serverInfo;
        values[22] = this.serverInfoLabel;
        values[23] = this.style;
        values[34] = this.styleClass;
        values[25] = this.userInfo;
        values[26] = this.userInfoLabel;
        if (this.utilities != null) {
            Object[] linkValues = new Object[this.utilities.length];
            for (int i = 0; i < this.utilities.length; i++) {
                linkValues[i] = this.utilities[i].saveState(context);
            }
            values[27] = linkValues;
        }
        if (this.visible) {
            values[28] = Boolean.TRUE;
        } else {
            values[28] = Boolean.FALSE;
        }
        if (this.visibleSet) {
            values[29] = Boolean.TRUE;
        } else {
            values[29] = Boolean.FALSE;
        }
        return values;
    }
}
