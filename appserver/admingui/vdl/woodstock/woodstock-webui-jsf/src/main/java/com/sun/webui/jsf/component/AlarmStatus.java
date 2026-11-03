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
import jakarta.faces.context.FacesContext;

/**
 * The AlarmStatus component is used to display alarm information in the
 * Masthead component.
 */
@Component(type = "com.sun.webui.jsf.AlarmStatus",
        family = "com.sun.webui.jsf.AlarmStatus",
        displayName = "Alarm Status",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_alarm_status",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_alarm_status_props")
        //CHECKSTYLE:ON
public final class AlarmStatus extends ImageHyperlink {

    /**
     * Specify if the critical alarm count should be displayed. Set to false to
     * prevent display of critical alarm count and icon.
     */
    @Property(name = "criticalAlarms", displayName = "Critical Alarms")
    private boolean criticalAlarms = false;

    /**
     * criticalArlams set flag.
     */
    private boolean criticalAlarmsSet = false;

    /**
     * The key identifier for the theme image to use for the critical alarms
     * icon.
     */
    @Property(name = "criticalIcon", displayName = "Critical Icon")
    private String criticalIcon = null;

    /**
     * Specify if the down alarm count should be displayed.
     * Set to false to prevent display of down alarm count and icon.
     */
    @Property(name = "downAlarms", displayName = "Down Alarms")
    private boolean downAlarms = false;

    /**
     * downAlarms set flag.
     */
    private boolean downAlarmsSet = false;

    /**
     * The key identifier for the theme image to use for the down alarms icon.
     */
    @Property(name = "downIcon", displayName = "Down Icon")
    private String downIcon = null;

    /**
     * Specify if the major alarm count should be displayed. Set to false to
     * prevent display of minor alarm count and icon.
     */
    @Property(name = "majorAlarms", displayName = "Major Alarms")
    private boolean majorAlarms = false;

    /**
     * majorAlarms set flag.
     */
    private boolean majorAlarmsSet = false;

    /**
     * The key identifier for the theme image to use for the major alarms icon.
     */
    @Property(name = "majorIcon", displayName = "Major Icon")
    private String majorIcon = null;

    /**
     * Specify if the minor alarm count should be displayed. Set to false to
     * prevent display of minor alarm count and icon.
     */
    @Property(name = "minorAlarms", displayName = "Minor Alarms")
    private boolean minorAlarms = false;

    /**
     * minorAlarms set flag.
     */
    private boolean minorAlarmsSet = false;

    /**
     * The key identifier for the theme image to use for the minor alarms icon.
     */
    @Property(name = "minorIcon", displayName = "Minor Icon")
    private String minorIcon = null;

    /**
     * The number of critical alarms, to display next to the appropriate icon.
     */
    @Property(name = "numCriticalAlarms",
            displayName = "Number of Critical Alarms")
    private int numCriticalAlarms = Integer.MIN_VALUE;

    /**
     * numCriticalAlarams set flag.
     */
    private boolean numCriticalAlarmsSet = false;

    /**
     * The number of down alarms, to display next to the appropriate icon.
     */
    @Property(name = "numDownAlarms", displayName = "Number of Down Alarms")
    private int numDownAlarms = Integer.MIN_VALUE;

    /**
     * numDownAlaramsSet flag.
     */
    private boolean numDownAlarmsSet = false;

    /**
     * The number of major alarms, to display next to the appropriate icon.
     */
    @Property(name = "numMajorAlarms", displayName = "Number of Major Alarms")
    private int numMajorAlarms = Integer.MIN_VALUE;

    /**
     * numMajorAlarms set flag.
     */
    private boolean numMajorAlarmsSet = false;

    /**
     * The number of minor alarms, to display next to the appropriate icon.
     */
    @Property(name = "numMinorAlarms", displayName = "Number of Minor Alarms")
    private int numMinorAlarms = Integer.MIN_VALUE;

    /**
     * numMinorAlarms set flag.
     */
    private boolean numMinorAlarmsSet = false;

    /**
     * CSS style(s) to be applied to the outermost HTML element when this
     * component is rendered.
     */
    @Property(name = "style", displayName = "CSS Style(s)")
    private String style = null;

    /**
     * CSS style class(es) to be applied to the outermost HTML element when this
     * component is rendered.
     */
    @Property(name = "styleClass", displayName = "CSS Style Class(es)")
    private String styleClass = null;

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
     * visibleSet flag.
     */
    private boolean visibleSet = false;

    /**
     * Creates a new instance of AlarmStatus.
     */
    public AlarmStatus() {
        super();
        setRendererType("com.sun.webui.jsf.AlarmStatus");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.AlarmStatus";
    }

    /**
     * Scripting code executed when a mouse double click occurs over this
     * component.
     * @return String
     */
    @Property(name = "onDblClick", isHidden = false, isAttribute = true)
    @Override
    public String getOnDblClick() {
        return super.getOnDblClick();
    }

    /**
     * The current value of this component.
     * @return Object
     */
    @Property(name = "value", isHidden = false, isAttribute = true)
    @Override
    public Object getValue() {
        return super.getValue();
    }

    // Hide textPosition
    @Property(name = "textPosition", isHidden = true, isAttribute = false)
    @Override
    public String getTextPosition() {
        return super.getTextPosition();
    }

    // Hide icon
    @Property(name = "icon", isHidden = true, isAttribute = false)
    @Override
    public String getIcon() {
        return super.getIcon();
    }

    /**
     * Specify if the critical alarm count should be displayed. Set to false to
     * prevent display of critical alarm count and icon.
     * @return {@code boolean}
     */
    public boolean isCriticalAlarms() {
        if (this.criticalAlarmsSet) {
            return this.criticalAlarms;
        }
        ValueExpression vb = getValueExpression("criticalAlarms");
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
     * Specify if the critical alarm count should be displayed.Set to false to
     * prevent display of critical alarm count and icon.
     *
     * @param newCriticalAlarms criticalAlarms
     * @see #isCriticalAlarms()
     */
    public void setCriticalAlarms(final boolean newCriticalAlarms) {
        this.criticalAlarms = newCriticalAlarms;
        this.criticalAlarmsSet = true;
    }

    /**
     * The key identifier for the theme image to use for the critical alarms
     * icon.
     *
     * @return String
     */
    public String getCriticalIcon() {
        if (this.criticalIcon != null) {
            return this.criticalIcon;
        }
        ValueExpression vb = getValueExpression("criticalIcon");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The key identifier for the theme image to use for the critical alarms
     * icon.
     *
     * @see #getCriticalIcon()
     * @param newCriticalIcon criticalIcon
     */
    public void setCriticalIcon(final String newCriticalIcon) {
        this.criticalIcon = newCriticalIcon;
    }

    /**
     * Specify if the down alarm count should be displayed.
     * Set to false to prevent display of down alarm count and icon.
     * @return {@code boolean}
     */
    public boolean isDownAlarms() {
        if (this.downAlarmsSet) {
            return this.downAlarms;
        }
        ValueExpression vb = getValueExpression("downAlarms");
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
     * Specify if the down alarm count should be displayed.
     * Set to false to prevent display of down alarm count and icon.
     * @see #isDownAlarms()
     * @param newDownAlarms downAlarams
     */
    public void setDownAlarms(final boolean newDownAlarms) {
        this.downAlarms = newDownAlarms;
        this.downAlarmsSet = true;
    }

    /**
     * The key identifier for the theme image to use for the down alarms icon.
     * @return String
     */
    public String getDownIcon() {
        if (this.downIcon != null) {
            return this.downIcon;
        }
        ValueExpression vb = getValueExpression("downIcon");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The key identifier for the theme image to use for the down alarms icon.
     * @see #getDownIcon()
     * @param newDownIcon downIcon
     */
    public void setDownIcon(final String newDownIcon) {
        this.downIcon = newDownIcon;
    }

    /**
     * Specify if the major alarm count should be displayed. Set to false to
     * prevent display of minor alarm count and icon.
     * @return boolean
     */
    public boolean isMajorAlarms() {
        if (this.majorAlarmsSet) {
            return this.majorAlarms;
        }
        ValueExpression vb = getValueExpression("majorAlarms");
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
     * Specify if the major alarm count should be displayed. Set to false to
     * prevent display of minor alarm count and icon.
     * @see #isMajorAlarms()
     * @param newMajorAlarms majorAlarms
     */
    public void setMajorAlarms(final boolean newMajorAlarms) {
        this.majorAlarms = newMajorAlarms;
        this.majorAlarmsSet = true;
    }

    /**
     * The key identifier for the theme image to use for the major alarms icon.
     * @return String
     */
    public String getMajorIcon() {
        if (this.majorIcon != null) {
            return this.majorIcon;
        }
        ValueExpression vb = getValueExpression("majorIcon");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The key identifier for the theme image to use for the major alarms icon.
     * @see #getMajorIcon()
     * @param newMajorIcon majorIcon
     */
    public void setMajorIcon(final String newMajorIcon) {
        this.majorIcon = newMajorIcon;
    }

    /**
     * Specify if the minor alarm count should be displayed. Set to false to
     * prevent display of minor alarm count and icon.
     * @return {@code boolean}
     */
    public boolean isMinorAlarms() {
        if (this.minorAlarmsSet) {
            return this.minorAlarms;
        }
        ValueExpression vb = getValueExpression("minorAlarms");
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
     * Specify if the minor alarm count should be displayed.Set to false to
     * prevent display of minor alarm count and icon.
     *
     * @param neWMinorAlarms minorAlarams
     * @see #isMinorAlarms()
     */
    public void setMinorAlarms(final boolean neWMinorAlarms) {
        this.minorAlarms = neWMinorAlarms;
        this.minorAlarmsSet = true;
    }

    /**
     * The key identifier for the theme image to use for the minor alarms icon.
     * @return String
     */
    public String getMinorIcon() {
        if (this.minorIcon != null) {
            return this.minorIcon;
        }
        ValueExpression vb = getValueExpression("minorIcon");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The key identifier for the theme image to use for the minor alarms icon.
     * @see #getMinorIcon()
     * @param newMinorIcon minorIcon
     */
    public void setMinorIcon(final String newMinorIcon) {
        this.minorIcon = newMinorIcon;
    }

    /**
     * The number of critical alarms, to display next to the appropriate icon.
     * @return int
     */
    public int getNumCriticalAlarms() {
        if (this.numCriticalAlarmsSet) {
            return this.numCriticalAlarms;
        }
        ValueExpression vb = getValueExpression("numCriticalAlarms");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return Integer.MIN_VALUE;
            } else {
                return ((Integer) result);
            }
        }
        return 0;
    }

    /**
     * The number of critical alarms, to display next to the appropriate icon.
     * @see #getNumCriticalAlarms()
     * @param newNumCriticalAlarms numCriticalAlarms
     */
    public void setNumCriticalAlarms(final int newNumCriticalAlarms) {
        this.numCriticalAlarms = newNumCriticalAlarms;
        this.numCriticalAlarmsSet = true;
    }

    /**
     * The number of down alarms, to display next to the appropriate icon.
     * @return int
     */
    public int getNumDownAlarms() {
        if (this.numDownAlarmsSet) {
            return this.numDownAlarms;
        }
        ValueExpression vb = getValueExpression("numDownAlarms");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return Integer.MIN_VALUE;
            } else {
                return ((Integer) result);
            }
        }
        return 0;
    }

    /**
     * The number of down alarms, to display next to the appropriate icon.
     * @see #getNumDownAlarms()
     * @param newNumDownAlarms numDownAlarams
     */
    public void setNumDownAlarms(final int newNumDownAlarms) {
        this.numDownAlarms = newNumDownAlarms;
        this.numDownAlarmsSet = true;
    }

    /**
     * The number of major alarms, to display next to the appropriate icon.
     * @return int
     */
    public int getNumMajorAlarms() {
        if (this.numMajorAlarmsSet) {
            return this.numMajorAlarms;
        }
        ValueExpression vb = getValueExpression("numMajorAlarms");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return Integer.MIN_VALUE;
            } else {
                return ((Integer) result);
            }
        }
        return 0;
    }

    /**
     * The number of major alarms, to display next to the appropriate icon.
     * @see #getNumMajorAlarms()
     * @param newNumMajorAlarms numMajorAlarms
     */
    public void setNumMajorAlarms(final int newNumMajorAlarms) {
        this.numMajorAlarms = newNumMajorAlarms;
        this.numMajorAlarmsSet = true;
    }

    /**
     * The number of minor alarms, to display next to the appropriate icon.
     * @return int
     */
    public int getNumMinorAlarms() {
        if (this.numMinorAlarmsSet) {
            return this.numMinorAlarms;
        }
        ValueExpression vb = getValueExpression("numMinorAlarms");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return Integer.MIN_VALUE;
            } else {
                return ((Integer) result);
            }
        }
        return 0;
    }

    /**
     * The number of minor alarms, to display next to the appropriate icon.
     * @see #getNumMinorAlarms()
     * @param newNumMinorAlarms numMinorAlarms
     */
    public void setNumMinorAlarms(final int newNumMinorAlarms) {
        this.numMinorAlarms = newNumMinorAlarms;
        this.numMinorAlarmsSet = true;
    }

    @Override
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

    @Override
    public void setStyle(final String newStyle) {
        this.style = newStyle;
    }

    @Override
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

    @Override
    public void setStyleClass(final String newStyleClass) {
        this.styleClass = newStyleClass;
    }

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

    @Override
    public void setVisible(final boolean newVisible) {
        this.visible = newVisible;
        this.visibleSet = true;
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.criticalAlarms = ((Boolean) values[1]);
        this.criticalAlarmsSet = ((Boolean) values[2]);
        this.criticalIcon = (String) values[3];
        this.downAlarms = ((Boolean) values[4]);
        this.downAlarmsSet = ((Boolean) values[5]);
        this.downIcon = (String) values[6];
        this.majorAlarms = ((Boolean) values[7]);
        this.majorAlarmsSet = ((Boolean) values[8]);
        this.majorIcon = (String) values[9];
        this.minorAlarms = ((Boolean) values[10]);
        this.minorAlarmsSet = ((Boolean) values[11]);
        this.minorIcon = (String) values[12];
        this.numCriticalAlarms = ((Integer) values[13]);
        this.numCriticalAlarmsSet = ((Boolean) values[14]);
        this.numDownAlarms = ((Integer) values[15]);
        this.numDownAlarmsSet = ((Boolean) values[16]);
        this.numMajorAlarms = ((Integer) values[17]);
        this.numMajorAlarmsSet = ((Boolean) values[18]);
        this.numMinorAlarms = ((Integer) values[19]);
        this.numMinorAlarmsSet = ((Boolean) values[20]);
        this.style = (String) values[21];
        this.styleClass = (String) values[22];
        this.visible = ((Boolean) values[23]);
        this.visibleSet = ((Boolean) values[24]);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[25];
        values[0] = super.saveState(context);
        if (this.criticalAlarms) {
            values[1] = Boolean.TRUE;
        } else {
            values[1] = Boolean.FALSE;
        }
        if (this.criticalAlarmsSet) {
            values[2] = Boolean.TRUE;
        } else {
            values[2] = Boolean.FALSE;
        }
        values[3] = this.criticalIcon;
        if (this.downAlarms) {
            values[4] = Boolean.TRUE;
        } else {
            values[4] = Boolean.FALSE;
        }
        if (this.downAlarmsSet) {
            values[5] = Boolean.TRUE;
        } else {
            values[5] = Boolean.FALSE;
        }
        values[6] = this.downIcon;
        if (this.majorAlarms) {
            values[7] = Boolean.TRUE;
        } else {
            values[7] = Boolean.FALSE;
        }
        if (this.majorAlarmsSet) {
            values[8] = Boolean.TRUE;
        } else {
            values[8] = Boolean.FALSE;
        }
        values[9] = this.majorIcon;
        if (this.minorAlarms) {
            values[10] = Boolean.TRUE;
        } else {
            values[10] = Boolean.FALSE;
        }
        if (this.minorAlarmsSet) {
            values[11] = Boolean.TRUE;
        } else {
            values[11] = Boolean.FALSE;
        }
        values[12] = this.minorIcon;
        values[13] = this.numCriticalAlarms;
        if (this.numCriticalAlarmsSet) {
            values[14] = Boolean.TRUE;
        } else {
            values[14] = Boolean.FALSE;
        }
        values[15] = this.numDownAlarms;
        if (this.numDownAlarmsSet) {
            values[16] = Boolean.TRUE;
        } else {
            values[16] = Boolean.FALSE;
        }
        values[17] = this.numMajorAlarms;
        if (this.numMajorAlarmsSet) {
            values[18] = Boolean.TRUE;
        } else {
            values[18] = Boolean.FALSE;
        }
        values[19] = this.numMinorAlarms;
        if (this.numMinorAlarmsSet) {
            values[20] = Boolean.TRUE;
        } else {
            values[20] = Boolean.FALSE;
        }
        values[21] = this.style;
        values[22] = this.styleClass;
        if (this.visible) {
            values[23] = Boolean.TRUE;
        } else {
            values[23] = Boolean.FALSE;
        }
        if (this.visibleSet) {
            values[24] = Boolean.TRUE;
        } else {
            values[24] = Boolean.FALSE;
        }
        return values;
    }
}
