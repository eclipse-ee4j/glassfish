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

import com.sun.data.provider.SortCriteria;
import com.sun.faces.annotation.Component;
import com.sun.faces.annotation.Property;
import com.sun.webui.jsf.event.TableSortActionListener;
import com.sun.webui.jsf.model.Option;
import com.sun.webui.jsf.util.LogUtil;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.el.ValueExpression;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.context.FacesContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Component that represents an embedded panel.
 * <p>
 * Note: Column headers and footers are rendered by TableRowGroupRenderer. Table
 * column footers are rendered by TableRenderer.
 * </p>
 * <p>
 * <pre>
 * java.util.logging.ConsoleHandler.level = FINE
 * com.sun.webui.jsf.component.TablePanels.level = FINE
 * </pre>
 * </p>
 */
@Component(type = "com.sun.webui.jsf.TablePanels",
        family = "com.sun.webui.jsf.TablePanels",
        displayName = "Panels",
        isTag = false)
public final class TablePanels extends UIComponentBase
        implements NamingContainer {

    /**
     * The facet name for the filter panel.
     */
    public static final String FILTER_PANEL_ID = "_filterPanel";

    /**
     * The facet name for the preferences panel.
     */
    public static final String PREFERENCES_PANEL_ID =
            "_preferencesPanel";

    /**
     * The component id for the primary sort column menu.
     */
    public static final String PRIMARY_SORT_COLUMN_MENU_ID =
            "_primarySortColumnMenu";

    /**
     * The facet name for the primary sort column menu.
     */
    public static final String PRIMARY_SORT_COLUMN_MENU_FACET =
            "primarySortColumnMenu";

    /**
     * The component id for the primary sort column menu label.
     */
    public static final String PRIMARY_SORT_COLUMN_MENU_LABEL_ID =
            "_primarySortColumnMenuLabel";

    /**
     * The facet name for the primary sort column menu label.
     */
    public static final String PRIMARY_SORT_COLUMN_MENU_LABEL_FACET =
            "primarySortColumnMenuLabel";

    /**
     * The component id for the primary sort order menu.
     */
    public static final String PRIMARY_SORT_ORDER_MENU_ID =
            "_primarySortOrderMenu";

    /**
     * The facet name for the primary sort order menu.
     */
    public static final String PRIMARY_SORT_ORDER_MENU_FACET =
            "primarySortOrderMenu";

    /**
     * The component id for the secondary sort column menu.
     */
    public static final String SECONDARY_SORT_COLUMN_MENU_ID =
            "_secondarySortColumnMenu";

    /**
     * The facet name for the secondary sort column menu.
     */
    public static final String SECONDARY_SORT_COLUMN_MENU_FACET =
            "secondarySortColumnMenu";

    /**
     * The component id for the secondary sort column menu label.
     */
    public static final String SECONDARY_SORT_COLUMN_MENU_LABEL_ID =
            "_secondarySortColumnMenuLabel";

    /**
     * The facet name for the secondary sort column menu label.
     */
    public static final String SECONDARY_SORT_COLUMN_MENU_LABEL_FACET =
            "secondarySortColumnMenuLabel";

    /**
     * The component id for the secondary sort order menu.
     */
    public static final String SECONDARY_SORT_ORDER_MENU_ID =
            "_secondarySortOrderMenu";

    /**
     * The facet name for the secondary sort order menu.
     */
    public static final String SECONDARY_SORT_ORDER_MENU_FACET =
            "secondarySortOrderMenu";

    /**
     * The facet name for the sort panel.
     */
    public static final String SORT_PANEL_ID = "_sortPanel";

    /**
     * The component id for the sort panel cancel button.
     */
    public static final String SORT_PANEL_CANCEL_BUTTON_ID =
            "_sortPanelCancelButton";

    /**
     * The facet name for the sort panel cancel button.
     */
    public static final String SORT_PANEL_CANCEL_BUTTON_FACET =
            "sortPanelCancelButton";

    /**
     * The component id for the sort panel submit button.
     */
    public static final String SORT_PANEL_SUBMIT_BUTTON_ID =
            "_sortPanelSubmitButton";

    /**
     * The facet name for the sort panel submit button.
     */
    public static final String SORT_PANEL_SUBMIT_BUTTON_FACET =
            "sortPanelSubmitButton";

    /**
     * The component id for the tertiary sort column menu.
     */
    public static final String TERTIARY_SORT_COLUMN_MENU_ID =
            "_tertiarySortColumnMenu";

    /**
     * The facet name for the tertiary sort column menu.
     */
    public static final String TERTIARY_SORT_COLUMN_MENU_FACET =
            "tertiarySortColumnMenu";

    /**
     * The component id for the tertiary sort column menu label.
     */
    public static final String TERTIARY_SORT_COLUMN_MENU_LABEL_ID =
            "_tertiarySortColumnMenuLabel";

    /**
     * The facet name for the tertiary sort column menu label.
     */
    public static final String TERTIARY_SORT_COLUMN_MENU_LABEL_FACET =
            "tertiarySortColumnMenuLabel";

    /**
     * The component id for the tertiary sort order menu.
     */
    public static final String TERTIARY_SORT_ORDER_MENU_ID =
            "_tertiarySortOrderMenu";

    /**
     * The facet name for the tertiary sort order menu.
     */
    public static final String TERTIARY_SORT_ORDER_MENU_FACET =
            "tertiarySortOrderMenu";

    /**
     * The Table ancestor enclosing this component.
     */
    private Table table = null;

    /**
     * ABBR gives an abbreviated version of the cell's content. This allows
     * visual browsers to use the short form if space is limited, and non-visual
     * browsers can give a cell's header information in an abbreviated form
     * before rendering each cell.
     */
    @Property(name = "abbr",
            displayName = "Abbreviation for Header Cell")
    private String abbr = null;

    /**
     * Use the {@code align} attribute to specify the horizontal alignment
     * for the content of each cell in the column. Valid values are
     * {@code left}, {@code center}, {@code right},
     * {@code justify}, and {@code char}. The default alignment is
     * {@code left}. Setting the {@code align} attribute to
     * {@code char} causes the cell's contents to be aligned on the
     * character that you specify with the {@code char} attribute. For
     * example, to align cell contents on colons, set {@code align="char"}
     * and {@code char=":" }Some browsers do not support aligning on the
     * character.
     */
    @Property(name = "align",
            displayName = "Horizontal Alignment")
    private String align = null;

    /**
     * The AXIS attribute provides a method of categorizing cells. The
     * attribute's value is a comma-separated list of category names. See the
     * HTML 4.0 Recommendation's section on categorizing cells for an
     * application of AXIS.
     */
    @Property(name = "axis",
            displayName = "Category of Header Cell")
    private String axis = null;

    /**
     * The BGCOLOR attribute suggests a background color for the cell. The
     * combination of this attribute with &lt;FONT COLOR=...&gt; can leave
     * invisible or unreadable text on Netscape Navigator 2.x, which does not
     * support BGCOLOR on table elements. BGCOLOR is dangerous even on
     * supporting browsers, since most fail to override it when overriding other
     * author-specified colors. Style sheets provide a safer, more flexible
     * method of specifying a table's background color. This attribute is
     * deprecated (in HTML 4.0) in favor of style sheets.
     */
    @Property(name = "bgColor",
            displayName = "Cell Background Color")
    private String bgColor = null;

    /**
     * Use the {@code char }attribute to specify a character to use for
     * horizontal alignment in each cell in the row. You must also set the
     * {@code align} attribute to {@code char} to enable character
     * alignment to be used. The default value for the {@code char}
     * attribute is the decimal point of the current language, such as a period
     * in English. The {@code char} HTML property is not supported by all
     * browsers.
     */
    @Property(name = "char",
            displayName = "Alignment Character")
    private String charFor = null;

    /**
     * Use the {@code charOff }attribute to specify the offset of the first
     * occurrence of the alignment character that is specified with the
     * {@code char} attribute. The offset is the distance from the left
     * cell border, in locales that read from left to right. The
     * {@code charOff} attribute's value can be a number of pixels or a
     * percentage of the cell's width. For example, {@code charOff="50%"}
     * centers the alignment character horizontally in a cell. If
     * {@code charOff="25%"}, the first instance of the alignment character
     * is placed at one fourth of the width of the cell.
     */
    @Property(name = "charOff",
            displayName = "Alignment Character Offset")
    private String charOff = null;

    /**
     * The COLSPAN attribute of TD specifies the number of columns that are
     * spanned by the cell. The default value is 1. The special value 0
     * indicates that the cell spans all columns to the end of the table. The
     * value 0 is ignored by most browsers, so authors may wish to calculate the
     * exact number of rows or columns spanned and use that value.
     */
    @Property(name = "colSpan",
            displayName = "Columns Spanned By the Cell")
    private int colSpan = Integer.MIN_VALUE;

    /**
     * colSpan set flag.
     */
    private boolean colSpanSet = false;

    /**
     * Extra HTML to be appended to the tag output by this renderer.
     */
    @Property(name = "extraHtml",
            displayName = "Extra HTML")
    private String extraHtml = null;

    /**
     * Flag indicating this component should also render a filter panel, in
     * addition to the sort and preferences panels. The default renders a sort
     * panel.
     */
    @Property(name = "filterPanel",
            displayName = "Is Filter Panel",
            isAttribute = false)
    private boolean filterPanel = false;

    /**
     * filterPanel set flag.
     */
    private boolean filterPanelSet = false;

    /**
     * The HEADERS attribute specifies the header cells that apply to the TD.
     * The value is a space-separated list of the header cells' ID attribute
     * values. The HEADERS attribute allows non-visual browsers to render the
     * header information for a given cell.
     */
    @Property(name = "headers",
            displayName = "List of Header Cells for Current Cell")
    private String headers = null;

    /**
     * The number of pixels for the cell's height. Styles should be used to
     * specify cell height when possible because the height attribute is
     * deprecated in HTML 4.0.
     */
    @Property(name = "height",
            displayName = "Height")
    private String height = null;

    /**
     * Use the {@code noWrap} attribute to disable word wrapping of this
     * column's cells in visual browsers. Word wrap can cause unnecessary
     * horizontal scrolling when the browser window is small in relation to the
     * font size. Styles should be used to disable word wrap when possible
     * because the nowrap attribute is deprecated in HTML 4.0.
     */
    @Property(name = "noWrap",
            displayName = "Suppress Word Wrap")
    private boolean noWrap = false;

    /**
     * noWrap set flag.
     */
    private boolean noWrapSet = false;

    /**
     * Scripting code executed when a mouse click occurs over this component.
     */
    @Property(name = "onClick",
            category = "Javascript")
    private String onClick = null;

    /**
     * Scripting code executed when a mouse double click occurs over this
     * component.
     */
    @Property(name = "onDblClick",
            displayName = "Double Click Script")
    private String onDblClick = null;

    /**
     * Scripting code executed when the user presses down on a key while the
     * component has focus.
     */
    @Property(name = "onKeyDown",
            displayName = "Key Down Script")
    private String onKeyDown = null;

    /**
     * Scripting code executed when the user presses and releases a key while
     * the component has focus.
     */
    @Property(name = "onKeyPress",
            displayName = "Key Press Script")
    private String onKeyPress = null;

    /**
     * Scripting code executed when the user releases a key while the component
     * has focus.
     */
    @Property(name = "onKeyUp",
            displayName = "Key Up Script")
    private String onKeyUp = null;

    /**
     * Scripting code executed when the user presses a mouse button while the
     * mouse pointer is on the component.
     */
    @Property(name = "onMouseDown",
            displayName = "Mouse Down Script")
    private String onMouseDown = null;

    /**
     * Scripting code executed when the user moves the mouse pointer while over
     * the component.
     */
    @Property(name = "onMouseMove",
            displayName = "Mouse Move Script")
    private String onMouseMove = null;

    /**
     * Scripting code executed when a mouse out movement occurs over this
     * component.
     */
    @Property(name = "onMouseOut",
            displayName = "Mouse Out Script")

    private String onMouseOut = null;
    /**
     * Scripting code executed when the user moves the mouse pointer into the
     * boundary of this component.
     */
    @Property(name = "onMouseOver",
            displayName = "Mouse In Script")
    private String onMouseOver = null;

    /**
     * Scripting code executed when the user releases a mouse button while the
     * mouse pointer is on the component.
     */
    @Property(name = "onMouseUp",
            displayName = "Mouse Up Script")
    private String onMouseUp = null;

    /**
     * Flag indicating this component should also render a preferences panel, in
     * addition to the sort and filter panels. The default renders a sort panel.
     */
    @Property(name = "preferencesPanel",
            displayName = "Is Preferences Panel",
            isAttribute = false)
    private boolean preferencesPanel = false;

    /**
     * preferencesPanel set flag.
     */
    private boolean preferencesPanelSet = false;

    /**
     * The ROWSPAN attribute of TD specifies the number of rows that are spanned
     * by the cell. The default value is 1. The special value 0 indicates that
     * the cell spans all rows to the end of the table. The value 0 is ignored
     * by most browsers, so authors may wish to calculate the exact number of
     * rows or columns spanned and use that value.
     */
    @Property(name = "rowSpan",
            displayName = "Rows Spanned By the Cell")
    private int rowSpan = Integer.MIN_VALUE;

    /**
     * rowSpan set flag.
     */
    private boolean rowSpanSet = false;

    /**
     * Use the {@code scope} attribute to specify that the data cells of
     * the column are also acting as headers for rows or other columns of the
     * table. This attribute supports assistive technologies by enabling them to
     * determine the order in which to read the cells. Valid values include:
     * <ul>
     * <li>{@code row}, when the cells provide header information for the
     * row</li>
     * <li>{@code col}, when the cells provide header information for the
     * column</li>
     * <li>{@code rowgroup}, when the cells provide header information for
     * the row group</li>
     * <li>{@code colgroup}, when the cells provide header information for
     * the column group</li>
     * </ul>
     */
    @Property(name = "scope",
            displayName = "Cells Covered By Header Cell")
    private String scope = null;

    /**
     * CSS style(s) to be applied to the outermost HTML element when this
     * component is rendered.
     */
    @Property(name = "style",
            displayName = "CSS Style(s)")
    private String style = null;

    /**
     * CSS style class(es) to be applied to the outermost HTML element when this
     * component is rendered.
     */
    @Property(name = "styleClass",
            displayName = "CSS Style Class(es)")
    private String styleClass = null;

    /**
     * Sets the value of the title attribute for the HTML element. The specified
     * text will display as a tool tip if the mouse cursor hovers over the HTML
     * element.
     */
    @Property(name = "toolTip",
            displayName = "Tool Tip",
            category = "Behavior")
    private String toolTip = null;

    /**
     * Use the {@code valign} attribute to specify the vertical alignment
     * for the content of each cell in the column. Valid values are
     * {@code top}, {@code middle}, {@code bottom}, and
     * {@code baseline}. The default vertical alignment is
     * {@code middle}. Setting the {@code valign} attribute to {@code baseline
     * }causes the first line of each cell's content to be aligned on the
     * text baseline, the invisible line on which text characters rest.
     */
    @Property(name = "valign",
            displayName = "Vertical Position")
    private String valign = null;

    /**
     * Use the visible attribute to indicate whether the component should be
     * viewable by the user in the rendered HTML page.
     */
    @Property(name = "visible", displayName = "Visible")
    private boolean visible = false;

    /**
     * visible set flag.
     */
    private boolean visibleSet = false;

    /**
     * Use the {@code width} attribute to specify the width of the cells of
     * the column. The width can be specified as the number of pixels or the
     * percentage of the table width, and is especially useful for spacer
     * columns. This attribute is deprecated in HTML 4.0 in favor of style
     * sheets.
     */
    @Property(name = "width", displayName = "Width")
    private String width = null;

    /**
     * Default constructor.
     */
    public TablePanels() {
        super();
        setRendererType("com.sun.webui.jsf.TablePanels");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.TablePanels";
    }

    /**
     * Get the closest Table ancestor that encloses this component.
     *
     * @return The Table ancestor.
     */
    public Table getTableAncestor() {
        if (table == null) {
            UIComponent component = this;
            while (component != null) {
                component = component.getParent();
                if (component instanceof Table) {
                    table = (Table) component;
                    break;
                }
            }
        }
        return table;
    }

    /**
     * Get primary sort column menu used in the sort panel.
     *
     * @return The primary sort column menu.
     */
    public UIComponent getPrimarySortColumnMenu() {
        UIComponent facet = getFacet(PRIMARY_SORT_COLUMN_MENU_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        DropDown child = new DropDown();
        child.setId(PRIMARY_SORT_COLUMN_MENU_ID);
        child.setItems(getSortColumnMenuOptions());
        child.setSelected(getSelectedSortColumnMenuOption(1));

        // Set JS to initialize the sort column menu.
        Table tableAncestor = getTableAncestor();
        if (tableAncestor != null) {
            child.setTabIndex(tableAncestor.getTabIndex());
            child.setOnChange("document.getElementById('"
                    + tableAncestor.getClientId(getFacesContext())
                    + "').initPrimarySortOrderMenu()");
        } else {
            log("getPrimarySortColumnMenu",
                    "Tab index & onChange not set, Table is null");
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get primary sort column menu label used in the sort panel.
     *
     * @return The primary sort column menu label.
     */
    public UIComponent getPrimarySortColumnMenuLabel() {
        UIComponent facet = getFacet(PRIMARY_SORT_COLUMN_MENU_LABEL_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        Label child = new Label();
        child.setId(PRIMARY_SORT_COLUMN_MENU_LABEL_ID);
        child.setText(getTheme().getMessage("table.panel.primarySortColumn"));
        child.setLabelLevel(2);

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get primary sort order menu used in the sort panel.
     *
     * @return The primary sort order menu.
     */
    public UIComponent getPrimarySortOrderMenu() {
        UIComponent facet = getFacet(PRIMARY_SORT_ORDER_MENU_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        Theme theme = getTheme();
        DropDown child = new DropDown();
        child.setId(PRIMARY_SORT_ORDER_MENU_ID);
        child.setItems(getSortOrderMenuOptions());
        child.setSelected(getSelectedSortOrderMenuOption(1));

        // Set JS to initialize the sort order menu.
        Table tableAncestor = getTableAncestor();
        if (tableAncestor != null) {
            child.setTabIndex(tableAncestor.getTabIndex());
            child.setOnChange("document.getElementById('"
                    + tableAncestor.getClientId(getFacesContext())
                    + "').initPrimarySortOrderMenuToolTip()");
        } else {
            log("getPrimarySortOrderMenu",
                    "Tab index & onChange not set, Table is null");
        }

        // Note: The tooltip is only set here for 508 compliance tools. The
        // actual tooltip is set dynamically, when the embedded panel is open.
        child.setToolTip(theme.getMessage("table.panel.primarySortOrder",
                new String[]{
                    theme.getMessage("table.sort.augment.undeterminedAscending")
                }));

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get secondary sort column menu used in the sort panel.
     *
     * @return The secondary sort column menu.
     */
    public UIComponent getSecondarySortColumnMenu() {
        UIComponent facet = getFacet(SECONDARY_SORT_COLUMN_MENU_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        DropDown child = new DropDown();
        child.setId(SECONDARY_SORT_COLUMN_MENU_ID);
        child.setItems(getSortColumnMenuOptions());
        child.setSelected(getSelectedSortColumnMenuOption(2));

        // Set JS to initialize the sort column menu.
        Table tableAncestor = getTableAncestor();
        if (tableAncestor != null) {
            child.setTabIndex(tableAncestor.getTabIndex());
            child.setOnChange("document.getElementById('"
                    + tableAncestor.getClientId(getFacesContext())
                    + "').initSecondarySortOrderMenu()");
        } else {
            log("getSecondarySortColumnMenu",
                    "Tab index & onChange not set, Table is null");
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get secondary sort column menu label used in the sort panel.
     *
     * @return The secondary sort column menu label.
     */
    public UIComponent getSecondarySortColumnMenuLabel() {
        UIComponent facet = getFacet(SECONDARY_SORT_COLUMN_MENU_LABEL_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        Label child = new Label();
        child.setId(SECONDARY_SORT_COLUMN_MENU_LABEL_ID);
        child.setText(getTheme().getMessage("table.panel.secondarySortColumn"));
        child.setLabelLevel(2);

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get secondary sort order menu used in the sort panel.
     *
     * @return The secondary sort order menu.
     */
    public UIComponent getSecondarySortOrderMenu() {
        UIComponent facet = getFacet(SECONDARY_SORT_ORDER_MENU_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        Theme theme = getTheme();
        DropDown child = new DropDown();
        child.setId(SECONDARY_SORT_ORDER_MENU_ID);
        child.setItems(getSortOrderMenuOptions());
        child.setSelected(getSelectedSortOrderMenuOption(2));

        // Set JS to initialize the sort order menu.
        Table tableAncestor = getTableAncestor();
        if (tableAncestor != null) {
            child.setTabIndex(tableAncestor.getTabIndex());
            child.setOnChange("document.getElementById('"
                    + tableAncestor.getClientId(getFacesContext())
                    + "').initSecondarySortOrderMenuToolTip()");
        } else {
            log("getSecondarySortOrderMenu",
                    "Tab index & onChange not set, Table is null");
        }

        // Note: The tooltip is only set here for 508 compliance tools. The
        // actual tooltip is set dynamically, when the embedded panel is open.
        child.setToolTip(theme.getMessage("table.panel.secondarySortOrder",
                new String[]{
                    theme.getMessage("table.sort.augment.undeterminedAscending")
                }));

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get sort panel cancel button.
     *
     * @return The sort panel cancel button.
     */
    public UIComponent getSortPanelCancelButton() {
        UIComponent facet = getFacet(SORT_PANEL_CANCEL_BUTTON_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        Button child = new Button();
        child.setId(SORT_PANEL_CANCEL_BUTTON_ID);
        child.setMini(true);
        child.setText(getTheme().getMessage("table.panel.cancel"));
        child.setToolTip(getTheme().getMessage("table.panel.cancelChanges"));

        // Set JS to close the sort panel.
        Table tableAncestor = getTableAncestor();
        if (tableAncestor != null) {
            child.setTabIndex(tableAncestor.getTabIndex());
            child.setOnClick("document.getElementById('"
                    +
                    tableAncestor.getClientId(getFacesContext())
                    + "').toggleSortPanel(); return false");
        } else {
            log("getSortPanelCancelButton",
                    "Tab index & onClick not set, Table is null");
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get sort panel submit button.
     *
     * @return The sort panel submit button.
     */
    public UIComponent getSortPanelSubmitButton() {
        UIComponent facet = getFacet(SORT_PANEL_SUBMIT_BUTTON_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        Button child = new Button();
        child.setId(SORT_PANEL_SUBMIT_BUTTON_ID);
        child.setMini(true);
        child.setPrimary(true);
        child.setText(getTheme().getMessage("table.panel.submit"));
        child.setToolTip(getTheme().getMessage("table.panel.applyChanges"));
        child.addActionListener(new TableSortActionListener());

        // Set JS to validate user selections.
        Table tableAncestor = getTableAncestor();
        if (tableAncestor != null) {
            child.setTabIndex(tableAncestor.getTabIndex());
            child.setOnClick("return document.getElementById('"
                    + tableAncestor.getClientId(getFacesContext())
                    + "').validateSortPanel()");
        } else {
            log("getSortPanelSubmitButton",
                    "Tab index & onClick not set, Table is null");
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get tertiary sort column menu used in the sort panel.
     *
     * @return The tertiary sort column menu.
     */
    @SuppressWarnings("checkstyle:magicnumber")
    public UIComponent getTertiarySortColumnMenu() {
        UIComponent facet = getFacet(TERTIARY_SORT_COLUMN_MENU_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        DropDown child = new DropDown();
        child.setId(TERTIARY_SORT_COLUMN_MENU_ID);
        child.setItems(getSortColumnMenuOptions());
        child.setSelected(getSelectedSortColumnMenuOption(3));

        // Set JS to initialize the sort column menu.
        Table tableAncestor = getTableAncestor();
        if (tableAncestor != null) {
            child.setTabIndex(tableAncestor.getTabIndex());
            child.setOnChange("document.getElementById('"
                    + tableAncestor.getClientId(getFacesContext())
                    + "').initTertiarySortOrderMenu()");
        } else {
            log("getTertiarySortColumnMenu",
                    "Tab index & onChange not set, Table is null");
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get tertiary sort column menu label used in the sort panel.
     *
     * @return The tertiary sort column menu label.
     */
    public UIComponent getTertiarySortColumnMenuLabel() {
        UIComponent facet = getFacet(TERTIARY_SORT_COLUMN_MENU_LABEL_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        Label child = new Label();
        child.setId(TERTIARY_SORT_COLUMN_MENU_LABEL_ID);
        child.setText(getTheme().getMessage("table.panel.tertiarySortColumn"));
        child.setLabelLevel(2);

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get tertiary sort order menu used in the sort panel.
     *
     * @return The tertiary sort order menu.
     */
    @SuppressWarnings("checkstyle:magicnumber")
    public UIComponent getTertiarySortOrderMenu() {
        UIComponent facet = getFacet(TERTIARY_SORT_ORDER_MENU_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        Theme theme = getTheme();
        DropDown child = new DropDown();
        child.setId(TERTIARY_SORT_ORDER_MENU_ID);
        child.setItems(getSortOrderMenuOptions());
        child.setSelected(getSelectedSortOrderMenuOption(3));

        // Set JS to initialize the sort order menu.
        Table tableAncestor = getTableAncestor();
        if (tableAncestor != null) {
            child.setTabIndex(tableAncestor.getTabIndex());
            child.setOnChange("document.getElementById('"
                    + tableAncestor.getClientId(getFacesContext())
                    + "').initTertiarySortOrderMenuToolTip()");
        } else {
            log("getTertiarySortOrderMenu",
                    "Tab index & onChange not set, Table is null");
        }

        // Note: The tooltip is only set here for 508 compliance tools. The
        // actual tooltip is set dynamically, when the embedded panel is open.
        child.setToolTip(theme.getMessage("table.panel.tertiarySortOrder",
                new String[]{
                    theme.getMessage("table.sort.augment.undeterminedAscending")
                }));

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    @Override
    public void encodeBegin(final FacesContext context) throws IOException {
        // Clear cached variables -- bugtraq #6300020.
        table = null;
        super.encodeBegin(context);
    }

    /**
     * ABBR gives an abbreviated version of the cell's content. This allows
     * visual browsers to use the short form if space is limited, and non-visual
     * browsers can give a cell's header information in an abbreviated form
     * before rendering each cell.
     * @return String
     */
    public String getAbbr() {
        if (this.abbr != null) {
            return this.abbr;
        }
        ValueExpression vb = getValueExpression("abbr");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * ABBR gives an abbreviated version of the cell's content. This allows
     * visual browsers to use the short form if space is limited, and non-visual
     * browsers can give a cell's header information in an abbreviated form
     * before rendering each cell.
     * @param newAbbr abbr
     */
    public void setAbbr(final String newAbbr) {
        this.abbr = newAbbr;
    }

    /**
     * Use the {@code align} attribute to specify the horizontal alignment
     * for the content of each cell in the column. Valid values are
     * {@code left}, {@code center}, {@code right},
     * {@code justify}, and {@code char}. The default alignment is
     * {@code left}. Setting the {@code align} attribute to
     * {@code char} causes the cell's contents to be aligned on the
     * character that you specify with the {@code char} attribute. For
     * example, to align cell contents on colons, set {@code align="char"}
     * and {@code char=":" }Some browsers do not support aligning on the
     * character.
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
     * Use the {@code align} attribute to specify the horizontal alignment
     * for the content of each cell in the column. Valid values are
     * {@code left}, {@code center}, {@code right},
     * {@code justify}, and {@code char}. The default alignment is
     * {@code left}. Setting the {@code align} attribute to
     * {@code char} causes the cell's contents to be aligned on the
     * character that you specify with the {@code char} attribute. For
     * example, to align cell contents on colons, set {@code align="char"}
     * and {@code char=":" }Some browsers do not support aligning on the
     * character.
     * @param newAlign align
     */
    public void setAlign(final String newAlign) {
        this.align = newAlign;
    }

    /**
     * The AXIS attribute provides a method of categorizing cells. The
     * attribute's value is a comma-separated list of category names. See the
     * HTML 4.0 Recommendation's section on categorizing cells for an
     * application of AXIS.
     * @return String
     */
    public String getAxis() {
        if (this.axis != null) {
            return this.axis;
        }
        ValueExpression vb = getValueExpression("axis");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The AXIS attribute provides a method of categorizing cells. The
     * attribute's value is a comma-separated list of category names. See the
     * HTML 4.0 Recommendation's section on categorizing cells for an
     * application of AXIS.
     * @param newAxis axis
     */
    public void setAxis(final String newAxis) {
        this.axis = newAxis;
    }

    /**
     * The BGCOLOR attribute suggests a background color for the cell. The
     * combination of this attribute with &lt;FONT COLOR=...&gt; can leave
     * invisible or unreadable text on Netscape Navigator 2.x, which does not
     * support BGCOLOR on table elements. BGCOLOR is dangerous even on
     * supporting browsers, since most fail to override it when overriding other
     * author-specified colors. Style sheets provide a safer, more flexible
     * method of specifying a table's background color. This attribute is
     * deprecated (in HTML 4.0) in favor of style sheets.
     * @return String
     */
    public String getBgColor() {
        if (this.bgColor != null) {
            return this.bgColor;
        }
        ValueExpression vb = getValueExpression("bgColor");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The BGCOLOR attribute suggests a background color for the cell. The
     * combination of this attribute with &lt;FONT COLOR=...&gt; can leave
     * invisible or unreadable text on Netscape Navigator 2.x, which does not
     * support BGCOLOR on table elements. BGCOLOR is dangerous even on
     * supporting browsers, since most fail to override it when overriding other
     * author-specified colors. Style sheets provide a safer, more flexible
     * method of specifying a table's background color. This attribute is
     * deprecated (in HTML 4.0) in favor of style sheets.
     * @param newGgColor bgColor
     */
    public void setBgColor(final String newGgColor) {
        this.bgColor = newGgColor;
    }

    /**
     * Use the {@code char }attribute to specify a character to use for
     * horizontal alignment in each cell in the row. You must also set the
     * {@code align} attribute to {@code char} to enable character
     * alignment to be used. The default value for the {@code char}
     * attribute is the decimal point of the current language, such as a period
     * in English. The {@code char} HTML property is not supported by all
     * browsers.
     * @return String
     */
    public String getChar() {
        if (this.charFor != null) {
            return this.charFor;
        }
        ValueExpression vb = getValueExpression("char");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Use the {@code char }attribute to specify a character to use for
     * horizontal alignment in each cell in the row. You must also set the
     * {@code align} attribute to {@code char} to enable character
     * alignment to be used. The default value for the {@code char}
     * attribute is the decimal point of the current language, such as a period
     * in English. The {@code char} HTML property is not supported by all
     * browsers.
     * @param newCharFor charFor
     */
    public void setChar(final String newCharFor) {
        this.charFor = newCharFor;
    }

    /**
     * Use the {@code charOff }attribute to specify the offset of the first
     * occurrence of the alignment character that is specified with the
     * {@code char} attribute. The offset is the distance from the left
     * cell border, in locales that read from left to right. The
     * {@code charOff} attribute's value can be a number of pixels or a
     * percentage of the cell's width. For example, {@code charOff="50%"}
     * centers the alignment character horizontally in a cell. If
     * {@code charOff="25%"}, the first instance of the alignment character
     * is placed at one fourth of the width of the cell.
     * @return String
     */
    public String getCharOff() {
        if (this.charOff != null) {
            return this.charOff;
        }
        ValueExpression vb = getValueExpression("charOff");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Use the {@code charOff }attribute to specify the offset of the first
     * occurrence of the alignment character that is specified with the
     * {@code char} attribute. The offset is the distance from the left
     * cell border, in locales that read from left to right. The
     * {@code charOff} attribute's value can be a number of pixels or a
     * percentage of the cell's width. For example, {@code charOff="50%"}
     * centers the alignment character horizontally in a cell. If
     * {@code charOff="25%"}, the first instance of the alignment character
     * is placed at one fourth of the width of the cell.
     * @param newCharOff charOff
     */
    public void setCharOff(final String newCharOff) {
        this.charOff = newCharOff;
    }

    /**
     * The COLSPAN attribute of TD specifies the number of columns that are
     * spanned by the cell. The default value is 1. The special value 0
     * indicates that the cell spans all columns to the end of the table. The
     * value 0 is ignored by most browsers, so authors may wish to calculate the
     * exact number of rows or columns spanned and use that value.
     * @return int
     */
    public int getColSpan() {
        if (this.colSpanSet) {
            return this.colSpan;
        }
        ValueExpression vb = getValueExpression("colSpan");
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
     * The COLSPAN attribute of TD specifies the number of columns that are
     * spanned by the cell. The default value is 1. The special value 0
     * indicates that the cell spans all columns to the end of the table. The
     * value 0 is ignored by most browsers, so authors may wish to calculate the
     * exact number of rows or columns spanned and use that value.
     * @param newColSpan colSpan
     */
    public void setColSpan(final int newColSpan) {
        this.colSpan = newColSpan;
        this.colSpanSet = true;
    }

    /**
     * Extra HTML to be appended to the tag output by this renderer.
     * @return String
     */
    public String getExtraHtml() {
        if (this.extraHtml != null) {
            return this.extraHtml;
        }
        ValueExpression vb = getValueExpression("extraHtml");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Extra HTML to be appended to the tag output by this renderer.
     * @param newExtraHtml extraHtml
     */
    public void setExtraHtml(final String newExtraHtml) {
        this.extraHtml = newExtraHtml;
    }

    /**
     * Flag indicating this component should also render a filter panel, in
     * addition to the sort and preferences panels. The default renders a sort
     * panel.
     * @return {@code boolean}
     */
    public boolean isFilterPanel() {
        if (this.filterPanelSet) {
            return this.filterPanel;
        }
        ValueExpression vb = getValueExpression("filterPanel");
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
     * Flag indicating this component should also render a filter panel, in
     * addition to the sort and preferences panels. The default renders a sort
     * panel.
     * @param newFilterPanel filterPanel
     */
    public void setFilterPanel(final boolean newFilterPanel) {
        this.filterPanel = newFilterPanel;
        this.filterPanelSet = true;
    }

    /**
     * The HEADERS attribute specifies the header cells that apply to the TD.
     * The value is a space-separated list of the header cells' ID attribute
     * values. The HEADERS attribute allows non-visual browsers to render the
     * header information for a given cell.
     * @return String
     */
    public String getHeaders() {
        if (this.headers != null) {
            return this.headers;
        }
        ValueExpression vb = getValueExpression("headers");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The HEADERS attribute specifies the header cells that apply to the TD.
     * The value is a space-separated list of the header cells' ID attribute
     * values. The HEADERS attribute allows non-visual browsers to render the
     * header information for a given cell.
     * @param newHeaders headers
     */
    public void setHeaders(final String newHeaders) {
        this.headers = newHeaders;
    }

    /**
     * The number of pixels for the cell's height. Styles should be used to
     * specify cell height when possible because the height attribute is
     * deprecated in HTML 4.0.
     * @return String
     */
    public String getHeight() {
        if (this.height != null) {
            return this.height;
        }
        ValueExpression vb = getValueExpression("height");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The number of pixels for the cell's height. Styles should be used to
     * specify cell height when possible because the height attribute is
     * deprecated in HTML 4.0.
     * @param newHeight height
     */
    public void setHeight(final String newHeight) {
        this.height = newHeight;
    }

    /**
     * Use the {@code noWrap} attribute to disable word wrapping of this
     * column's cells in visual browsers. Word wrap can cause unnecessary
     * horizontal scrolling when the browser window is small in relation to the
     * font size. Styles should be used to disable word wrap when possible
     * because the nowrap attribute is deprecated in HTML 4.0.
     * @return {@code boolean}
     */
    public boolean isNoWrap() {
        if (this.noWrapSet) {
            return this.noWrap;
        }
        ValueExpression vb = getValueExpression("noWrap");
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
     * Use the {@code noWrap} attribute to disable word wrapping of this
     * column's cells in visual browsers. Word wrap can cause unnecessary
     * horizontal scrolling when the browser window is small in relation to the
     * font size. Styles should be used to disable word wrap when possible
     * because the nowrap attribute is deprecated in HTML 4.0.
     * @param newNoWrap noWrap
     */
    public void setNoWrap(final boolean newNoWrap) {
        this.noWrap = newNoWrap;
        this.noWrapSet = true;
    }

    /**
     * Scripting code executed when a mouse click occurs over this component.
     * @return String
     */
    public String getOnClick() {
        if (this.onClick != null) {
            return this.onClick;
        }
        ValueExpression vb = getValueExpression("onClick");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when a mouse click occurs over this component.
     * @param newOnClick onClick
     */
    public void setOnClick(final String newOnClick) {
        this.onClick = newOnClick;
    }

    /**
     * Scripting code executed when a mouse double click occurs over this
     * component.
     * @return String
     */
    public String getOnDblClick() {
        if (this.onDblClick != null) {
            return this.onDblClick;
        }
        ValueExpression vb = getValueExpression("onDblClick");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when a mouse double click occurs over this
     * component.
     * @param newOnDblClick onDblClick
     */
    public void setOnDblClick(final String newOnDblClick) {
        this.onDblClick = newOnDblClick;
    }

    /**
     * Scripting code executed when the user presses down on a key while the
     * component has focus.
     * @return String
     */
    public String getOnKeyDown() {
        if (this.onKeyDown != null) {
            return this.onKeyDown;
        }
        ValueExpression vb = getValueExpression("onKeyDown");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when the user presses down on a key while the
     * component has focus.
     * @param newOnKeyDown onKeyDown
     */
    public void setOnKeyDown(final String newOnKeyDown) {
        this.onKeyDown = newOnKeyDown;
    }

    /**
     * Scripting code executed when the user presses and releases a key while
     * the component has focus.
     * @return String
     */
    public String getOnKeyPress() {
        if (this.onKeyPress != null) {
            return this.onKeyPress;
        }
        ValueExpression vb = getValueExpression("onKeyPress");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when the user presses and releases a key while
     * the component has focus.
     * @param newOnKeyPress onKeyPress
     */
    public void setOnKeyPress(final String newOnKeyPress) {
        this.onKeyPress = newOnKeyPress;
    }

    /**
     * Scripting code executed when the user releases a key while the component
     * has focus.
     * @return String
     */
    public String getOnKeyUp() {
        if (this.onKeyUp != null) {
            return this.onKeyUp;
        }
        ValueExpression vb = getValueExpression("onKeyUp");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when the user releases a key while the component
     * has focus.
     * @param newOnKeyUp onKeyUp
     */
    public void setOnKeyUp(final String newOnKeyUp) {
        this.onKeyUp = newOnKeyUp;
    }

    /**
     * Scripting code executed when the user presses a mouse button while the
     * mouse pointer is on the component.
     * @return String
     */
    public String getOnMouseDown() {
        if (this.onMouseDown != null) {
            return this.onMouseDown;
        }
        ValueExpression vb = getValueExpression("onMouseDown");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when the user presses a mouse button while the
     * mouse pointer is on the component.
     * @param newOnMouseDown onMouseDown
     */
    public void setOnMouseDown(final String newOnMouseDown) {
        this.onMouseDown = newOnMouseDown;
    }

    /**
     * Scripting code executed when the user moves the mouse pointer while over
     * the component.
     * @return String
     */
    public String getOnMouseMove() {
        if (this.onMouseMove != null) {
            return this.onMouseMove;
        }
        ValueExpression vb = getValueExpression("onMouseMove");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when the user moves the mouse pointer while over
     * the component.
     * @param newOnMouseMove onMouseMove
     */
    public void setOnMouseMove(final String newOnMouseMove) {
        this.onMouseMove = newOnMouseMove;
    }

    /**
     * Scripting code executed when a mouse out movement occurs over this
     * component.
     * @return String
     */
    public String getOnMouseOut() {
        if (this.onMouseOut != null) {
            return this.onMouseOut;
        }
        ValueExpression vb = getValueExpression("onMouseOut");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when a mouse out movement occurs over this
     * component.
     * @param newOnMouseOut onMouseOut
     */
    public void setOnMouseOut(final String newOnMouseOut) {
        this.onMouseOut = newOnMouseOut;
    }

    /**
     * Scripting code executed when the user moves the mouse pointer into the
     * boundary of this component.
     * @return String
     */
    public String getOnMouseOver() {
        if (this.onMouseOver != null) {
            return this.onMouseOver;
        }
        ValueExpression vb = getValueExpression("onMouseOver");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when the user moves the mouse pointer into the
     * boundary of this component.
     * @param newOnMouseOver onMouseOver
     */
    public void setOnMouseOver(final String newOnMouseOver) {
        this.onMouseOver = newOnMouseOver;
    }

    /**
     * Scripting code executed when the user releases a mouse button while the
     * mouse pointer is on the component.
     * @return String
     */
    public String getOnMouseUp() {
        if (this.onMouseUp != null) {
            return this.onMouseUp;
        }
        ValueExpression vb = getValueExpression("onMouseUp");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when the user releases a mouse button while the
     * mouse pointer is on the component.
     * @param newOnMouseUp onMouseUp
     */
    public void setOnMouseUp(final String newOnMouseUp) {
        this.onMouseUp = newOnMouseUp;
    }

    /**
     * Flag indicating this component should also render a preferences panel, in
     * addition to the sort and filter panels. The default renders a sort panel.
     * @return {@code boolean}
     */
    public boolean isPreferencesPanel() {
        if (this.preferencesPanelSet) {
            return this.preferencesPanel;
        }
        ValueExpression vb = getValueExpression("preferencesPanel");
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
     * Flag indicating this component should also render a preferences panel, in
     * addition to the sort and filter panels. The default renders a sort panel.
     * @param newPreferencesPanel preferencesPanel
     */
    public void setPreferencesPanel(final boolean newPreferencesPanel) {
        this.preferencesPanel = newPreferencesPanel;
        this.preferencesPanelSet = true;
    }

    /**
     * The ROWSPAN attribute of TD specifies the number of rows that are spanned
     * by the cell. The default value is 1. The special value 0 indicates that
     * the cell spans all rows to the end of the table. The value 0 is ignored
     * by most browsers, so authors may wish to calculate the exact number of
     * rows or columns spanned and use that value.
     * @return int
     */
    public int getRowSpan() {
        if (this.rowSpanSet) {
            return this.rowSpan;
        }
        ValueExpression vb = getValueExpression("rowSpan");
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
     * The ROWSPAN attribute of TD specifies the number of rows that are spanned
     * by the cell. The default value is 1. The special value 0 indicates that
     * the cell spans all rows to the end of the table. The value 0 is ignored
     * by most browsers, so authors may wish to calculate the exact number of
     * rows or columns spanned and use that value.
     * @param newRowSpan rowSpan
     */
    public void setRowSpan(final int newRowSpan) {
        this.rowSpan = newRowSpan;
        this.rowSpanSet = true;
    }

    /**
     * Use the {@code scope} attribute to specify that the data cells of
     * the column are also acting as headers for rows or other columns of the
     * table. This attribute supports assistive technologies by enabling them to
     * determine the order in which to read the cells. Valid values include:
     * <ul>
     * <li>{@code row}, when the cells provide header information for the
     * row</li>
     * <li>{@code col}, when the cells provide header information for the
     * column</li>
     * <li>{@code rowgroup}, when the cells provide header information for
     * the row group</li>
     * <li>{@code colgroup}, when the cells provide header information for
     * the column group</li>
     * </ul>
     * @return String
     */
    public String getScope() {
        if (this.scope != null) {
            return this.scope;
        }
        ValueExpression vb = getValueExpression("scope");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Use the {@code scope} attribute to specify that the data cells of
     * the column are also acting as headers for rows or other columns of the
     * table. This attribute supports assistive technologies by enabling them to
     * determine the order in which to read the cells. Valid values include:
     * <ul>
     * <li>{@code row}, when the cells provide header information for the
     * row</li>
     * <li>{@code col}, when the cells provide header information for the
     * column</li>
     * <li>{@code rowgroup}, when the cells provide header information for
     * the row group</li>
     * <li>{@code colgroup}, when the cells provide header information for
     * the column group</li>
     * </ul>
     * @param newScope scope
     */
    public void setScope(final String newScope) {
        this.scope = newScope;
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
     * @param newStyleClass styleClass
     */
    public void setStyleClass(final String newStyleClass) {
        this.styleClass = newStyleClass;
    }

    /**
     * Sets the value of the title attribute for the HTML element. The specified
     * text will display as a tool tip if the mouse cursor hovers over the HTML
     * element.
     * @return String
     */
    public String getToolTip() {
        if (this.toolTip != null) {
            return this.toolTip;
        }
        ValueExpression vb = getValueExpression("toolTip");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Sets the value of the title attribute for the HTML element. The specified
     * text will display as a tool tip if the mouse cursor hovers over the HTML
     * element.
     * @param newToolTip tool tip
     */
    public void setToolTip(final String newToolTip) {
        this.toolTip = newToolTip;
    }

    /**
     * Use the {@code valign} attribute to specify the vertical alignment
     * for the content of each cell in the column. Valid values are
     * {@code top}, {@code middle}, {@code bottom}, and
     * {@code baseline}. The default vertical alignment is
     * {@code middle}. Setting the {@code valign} attribute to {@code baseline
     * }causes the first line of each cell's content to be aligned on the
     * text baseline, the invisible line on which text characters rest.
     * @return String
     */
    public String getValign() {
        if (this.valign != null) {
            return this.valign;
        }
        ValueExpression vb = getValueExpression("valign");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Use the {@code valign} attribute to specify the vertical alignment
     * for the content of each cell in the column. Valid values are
     * {@code top}, {@code middle}, {@code bottom}, and
     * {@code baseline}. The default vertical alignment is
     * {@code middle}. Setting the {@code valign} attribute to {@code baseline
     * }causes the first line of each cell's content to be aligned on the
     * text baseline, the invisible line on which text characters rest.
     * @param newValign valign
     */
    public void setValign(final String newValign) {
        this.valign = newValign;
    }

    /**
     * Use the visible attribute to indicate whether the component should be
     * viewable by the user in the rendered HTML page.
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
     * viewable by the user in the rendered HTML page.
     * @param newVisible visible
     */
    public void setVisible(final boolean newVisible) {
        this.visible = newVisible;
        this.visibleSet = true;
    }

    /**
     * Use the {@code width} attribute to specify the width of the cells of
     * the column. The width can be specified as the number of pixels or the
     * percentage of the table width, and is especially useful for spacer
     * columns. This attribute is deprecated in HTML 4.0 in favor of style
     * sheets.
     * @return String
     */
    public String getWidth() {
        if (this.width != null) {
            return this.width;
        }
        ValueExpression vb = getValueExpression("width");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Use the {@code width} attribute to specify the width of the cells of
     * the column. The width can be specified as the number of pixels or the
     * percentage of the table width, and is especially useful for spacer
     * columns. This attribute is deprecated in HTML 4.0 in favor of style
     * sheets.
     * @param newWidth width
     */
    public void setWidth(final String newWidth) {
        this.width = newWidth;
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.abbr = (String) values[1];
        this.align = (String) values[2];
        this.axis = (String) values[3];
        this.bgColor = (String) values[4];
        this.charFor = (String) values[5];
        this.charOff = (String) values[6];
        this.colSpan = ((Integer) values[7]);
        this.colSpanSet = ((Boolean) values[8]);
        this.extraHtml = (String) values[9];
        this.filterPanel = ((Boolean) values[10]);
        this.filterPanelSet = ((Boolean) values[11]);
        this.headers = (String) values[12];
        this.height = (String) values[13];
        this.noWrap = ((Boolean) values[14]);
        this.noWrapSet = ((Boolean) values[15]);
        this.onClick = (String) values[16];
        this.onDblClick = (String) values[17];
        this.onKeyDown = (String) values[18];
        this.onKeyPress = (String) values[19];
        this.onKeyUp = (String) values[20];
        this.onMouseDown = (String) values[21];
        this.onMouseMove = (String) values[22];
        this.onMouseOut = (String) values[23];
        this.onMouseOver = (String) values[24];
        this.onMouseUp = (String) values[25];
        this.preferencesPanel = ((Boolean) values[26]);
        this.preferencesPanelSet = ((Boolean) values[27]);
        this.rowSpan = ((Integer) values[28]);
        this.rowSpanSet = ((Boolean) values[29]);
        this.scope = (String) values[30];
        this.style = (String) values[31];
        this.styleClass = (String) values[32];
        this.toolTip = (String) values[33];
        this.valign = (String) values[34];
        this.visible = ((Boolean) values[35]);
        this.visibleSet = ((Boolean) values[36]);
        this.width = (String) values[37];
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[38];
        values[0] = super.saveState(context);
        values[1] = this.abbr;
        values[2] = this.align;
        values[3] = this.axis;
        values[4] = this.bgColor;
        values[5] = this.charFor;
        values[6] = this.charOff;
        values[7] = this.colSpan;
        if (colSpanSet) {
            values[8] = Boolean.TRUE;
        } else {
            values[8] = Boolean.FALSE;
        }
        values[9] = this.extraHtml;
        if (filterPanel) {
            values[10] = Boolean.TRUE;
        } else {
            values[10] = Boolean.FALSE;
        }
        if (filterPanelSet) {
            values[11] = Boolean.TRUE;
        } else {
            values[11] = Boolean.FALSE;
        }
        values[12] = this.headers;
        values[13] = this.height;
        if (noWrap) {
            values[14] = Boolean.TRUE;
        } else {
            values[14] = Boolean.FALSE;
        }
        if (noWrapSet) {
            values[15] = Boolean.TRUE;
        } else {
            values[15] = Boolean.FALSE;
        }
        values[16] = this.onClick;
        values[17] = this.onDblClick;
        values[18] = this.onKeyDown;
        values[19] = this.onKeyPress;
        values[20] = this.onKeyUp;
        values[21] = this.onMouseDown;
        values[22] = this.onMouseMove;
        values[23] = this.onMouseOut;
        values[24] = this.onMouseOver;
        values[25] = this.onMouseUp;
        if (preferencesPanel) {
            values[26] = Boolean.TRUE;
        } else {
            values[26] = Boolean.FALSE;
        }
        if (preferencesPanelSet) {
            values[27] = Boolean.TRUE;
        } else {
            values[27] = Boolean.FALSE;
        }
        values[28] = this.rowSpan;
        if (rowSpanSet) {
            values[29] = Boolean.TRUE;
        } else {
            values[29] = Boolean.FALSE;
        }
        values[30] = this.scope;
        values[31] = this.style;
        values[32] = this.styleClass;
        values[33] = this.toolTip;
        values[34] = this.valign;
        if (visible) {
            values[35] = Boolean.TRUE;
        } else {
            values[35] = Boolean.FALSE;
        }
        if (visibleSet) {
            values[36] = Boolean.TRUE;
        } else {
            values[36] = Boolean.FALSE;
        }
        values[37] = this.width;
        return values;
    }

    /**
     * Helper method to get selected option value used by the sort column menu
     * of the table sort panel.
     *
     * @param level The sort level.
     * @return The selected menu option value.
     */
    private String getSelectedSortColumnMenuOption(final int level) {
        String result = null;
        Table tableAncestor = getTableAncestor();
        TableRowGroup group;
        if (tableAncestor != null) {
            group = tableAncestor.getTableRowGroupChild();
        } else {
            group = null;
        }

        // Find the column that matches the given sort level and return the
        // SortCriteria key value.
        if (group != null) {
            Iterator kids = group.getTableColumnChildren();
            while (kids.hasNext()) {
                TableColumn col = (TableColumn) kids.next();
                if (!col.isRendered()) {
                    continue;
                }
                result = getSelectedSortColumnMenuOption(col, level);
                if (result != null) {
                    break;
                }
            }
        } else {
            log("getSelectedSortColumnMenuOption",
                    "Cannot obtain select sort column menu option,"
                            + " TableRowGroup is null");
        }
        return result;
    }

    /**
     * Helper method to get selected option value for nested TableColumn
     * components, used by the sort column menu of the table sort panel.
     *
     * @param level The sort level.
     * @param component The TableColumn component to render.
     * @return The selected menu option value.
     */
    private String getSelectedSortColumnMenuOption(final TableColumn component,
            final int level) {

        String result = null;
        if (component == null) {
            log("getSelectedSortColumnMenuOption",
                    "Cannot obtain select sort column menu option,"
                            + " TableColumn is null");
            return result;
        }

        Iterator kids = component.getTableColumnChildren();
        if (kids.hasNext()) {
            while (kids.hasNext()) {
                TableColumn kid = (TableColumn) kids.next();
                result = getSelectedSortColumnMenuOption(kid, level);
                if (result != null) {
                    return result;
                }
            }
        }

        Table tableAncestor = getTableAncestor();
        TableRowGroup group;
        if (tableAncestor != null) {
            group = tableAncestor.getTableRowGroupChild();
        } else {
            group = null;
        }

        // Get SortCriteria.
        if (group != null) {
            SortCriteria criteria = component.getSortCriteria();
            if (criteria != null) {
                // Get initial selected option value.
                int sortLevel = group.getSortLevel(criteria);
                if (sortLevel == level) {
                    result = criteria.getCriteriaKey();
                }
            }
        } else {
            log("getSelectedSortColumnMenuOption",
                    "Cannot obtain select sort column menu option,"
                            + " TableRowGroup is null");
        }
        return result;
    }

    /**
     * Helper method to get selected option value used by the sort order menu of
     * the table sort panel.
     *
     * @param level The sort level.
     * @return The selected menu option value.
     */
    private String getSelectedSortOrderMenuOption(final int level) {
        String result = null;
        Table tableAncestor = getTableAncestor();
        TableRowGroup group;
        if (tableAncestor != null) {
            group = tableAncestor.getTableRowGroupChild();
        } else {
            group = null;
        }

        // Find the column that matches the given sort level and return the
        // sort order.
        if (group != null) {
            Iterator kids = group.getTableColumnChildren();
            while (kids.hasNext()) {
                TableColumn col = (TableColumn) kids.next();
                if (!col.isRendered()) {
                    continue;
                }
                result = getSelectedSortOrderMenuOption(col, level);
                if (result != null) {
                    break;
                }
            }
        } else {
            log("getSelectedSortOrderMenuOption",
                    "Cannot obtain select sort order menu option,"
                            + " TableRowGroup is null");
        }
        return result;
    }

    /**
     * Helper method to get selected option value for nested TableColumn
     * components, used by the sort order menu of the table sort panel.
     *
     * @param level The sort level.
     * @param component The TableColumn component to render.
     * @return The selected menu option value.
     */
    private String getSelectedSortOrderMenuOption(final TableColumn component,
            final int level) {

        String result = null;
        if (component == null) {
            log("getSelectedSortOrderMenuOption",
                    "Cannot obtain select sort column order option,"
                            + " TableColumn is null");
            return result;
        }

        Iterator kids = component.getTableColumnChildren();
        if (kids.hasNext()) {
            while (kids.hasNext()) {
                TableColumn kid = (TableColumn) kids.next();
                result = getSelectedSortColumnMenuOption(kid, level);
                if (result != null) {
                    return result;
                }
            }
        }

        Table tableAncestor = getTableAncestor();
        TableRowGroup group;
        if (tableAncestor != null) {
            group = tableAncestor.getTableRowGroupChild();
        } else {
            group = null;
        }

        // Get SortCriteria.
        if (group != null) {
            SortCriteria criteria = component.getSortCriteria();
            if (criteria != null) {
                // Get initial selected option value.
                int sortLevel = group.getSortLevel(criteria);
                if (sortLevel == level) {
                    result = Boolean.toString(group.isDescendingSort(criteria));
                }
            }
        } else {
            log("getSelectedSortOrderMenuOption",
                    "Cannot obtain select sort order menu option,"
                            + " TableRowGroup is null");
        }
        return result;
    }

    /**
     * Helper method to get options used by the sort column menu of the table
     * sort panel.
     *
     * @return An array of menu options.
     */
    private Option[] getSortColumnMenuOptions() {
        ArrayList<Option> list = new ArrayList<Option>();
        Table tableAncestor = getTableAncestor();
        TableRowGroup group;
        if (tableAncestor != null) {
            group = tableAncestor.getTableRowGroupChild();
        } else {
            group = null;
        }

        // Add default "None" option -- an empty string represents no sort.
        list.add(new Option("", getTheme().getMessage("table.panel.none")));

        // For each sortable TableColumn, use the header text for each label and
        // the SortCriteria key as the value.
        if (group != null) {
            Iterator kids = group.getTableColumnChildren();
            while (kids.hasNext()) {
                TableColumn col = (TableColumn) kids.next();
                if (!col.isRendered()) {
                    continue;
                }
                // Get header text and sort value binding expression string.
                initSortColumnMenuOptions(col, list);
            }
        } else {
            log("getSortColumnMenuOptions",
                    "Cannot obtain sort column menu options,"
                            + " TableRowGroup is null");
        }
        // Set options.
        Option[] options = new Option[list.size()];
        return (Option[]) list.toArray(options);
    }

    /**
     * Helper method to get options for the sort order menu used in the table
     * sort panel.
     *
     * @return An array of menu options.
     */
    private Option[] getSortOrderMenuOptions() {
        ArrayList<Option> results = new ArrayList<Option>();

        // Add default option.
        results.add(new Option("false", getTheme().getMessage(
                "table.sort.augment.undeterminedAscending")));
        results.add(new Option("true", getTheme().getMessage(
                "table.sort.augment.undeterminedDescending")));

        // Set default options. Other options will be added client-side when
        // menu is initialized.
        Option[] options = new Option[results.size()];
        return (Option[]) results.toArray(options);
    }

    /**
     * Helper method to get Theme objects.
     *
     * @return The current theme.
     */
    private Theme getTheme() {
        return ThemeUtilities.getTheme(getFacesContext());
    }

    /**
     * Helper method to get options for nested TableColumn components, used by
     * the sort column menu of the table sort panel.
     *
     * @param component The TableColumn component to render.
     * @param list The array used to store menu options.
     */
    private void initSortColumnMenuOptions(final TableColumn component,
            final List<Option> list) {

        if (component == null) {
            return;
        }

        Iterator kids = component.getTableColumnChildren();
        if (kids.hasNext()) {
            while (kids.hasNext()) {
                TableColumn kid = (TableColumn) kids.next();
                initSortColumnMenuOptions(kid, list);
            }
        }

        // Get header text and sort value binding expression string.
        SortCriteria criteria = component.getSortCriteria();
        if (criteria == null) {
            log("initSortColumnMenuOptions",
                    "Cannot initialize sort column menu options,"
                            + " SortCriteria is null");
            return;
        }

        // Get label.
        String label;
        if (component.getSelectId() != null) {
            label = getTheme().getMessage("table.select.selectedItems");
        } else {
            label = component.getHeaderText();
        }

        // Add option.
        Option option;
        if (label != null) {
            option = new Option(criteria.getCriteriaKey(), label);
        } else {
            option = new Option(criteria.getCriteriaKey(), label);
        }
        list.add(option);
    }

    /**
     * Log fine messages.
     * @param method method to log
     * @param msg message to log
     */
    private void log(final String method, final String msg) {
        // Get class.
        Class clazz = this.getClass();
        if (LogUtil.fineEnabled(clazz)) {
            // Log method name and message.
            LogUtil.fine(clazz, clazz.getName() + "." + method + ": " + msg);
        }
    }
}
