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

import com.sun.data.provider.RowKey;
import com.sun.faces.annotation.Component;
import com.sun.faces.annotation.Property;
import com.sun.webui.jsf.event.TableSortActionListener;
import com.sun.webui.jsf.theme.ThemeImages;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.LogUtil;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.el.ValueExpression;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.context.FacesContext;

import java.io.IOException;

/**
 * Component that represents various table headers, including sortable,
 * selection, and group headers.
 * <p>
 * Note: Column headers and footers are rendered by TableRowGroupRenderer. Table
 * column footers are rendered by TableRenderer.
 * </p>
 *
 * <pre>
 * java.util.logging.ConsoleHandler.level = FINE
 * com.sun.webui.jsf.component.TableHeader.level = FINE
 * </pre>
 *
 */
@Component(type = "com.sun.webui.jsf.TableHeader",
        family = "com.sun.webui.jsf.TableHeader",
        displayName = "Header",
        isTag = false)
public final class TableHeader extends UIComponentBase
        implements NamingContainer {

    /**
     * The component id for the add sort button.
     */
    public static final String ADD_SORT_BUTTON_ID = "_addSortButton";

    /**
     * The facet name for the add sort button.
     */
    public static final String ADD_SORT_BUTTON_FACET = "addSortButton";

    /**
     * The component id for the collapsed hidden field.
     */
    public static final String COLLAPSED_HIDDEN_FIELD_ID =
            "_collapsedHiddenField";

    /**
     * The facet name for the collapsed hidden field.
     */
    public static final String COLLAPSED_HIDDEN_FIELD_FACET =
            "collapsedHiddenField";

    /**
     * The component id for the table row group toggle button.
     */
    public static final String GROUP_PANEL_TOGGLE_BUTTON_ID =
            "_groupPanelToggleButton";

    /**
     * The facet name for the table row group toggle button.
     */
    public static final String GROUP_PANEL_TOGGLE_BUTTON_FACET =
            "groupPanelToggleButton";

    /**
     * The component id for the primary sort button.
     */
    public static final String PRIMARY_SORT_BUTTON_ID = "_primarySortButton";

    /**
     * The facet name for the primary sort button.
     */
    public static final String PRIMARY_SORT_BUTTON_FACET = "primarySortButton";

    /**
     * The component id for the primary sort link.
     */
    public static final String PRIMARY_SORT_LINK_ID = "_primarySortLink";

    /**
     * The facet name for the primary sort link.
     */
    public static final String PRIMARY_SORT_LINK_FACET = "primarySortLink";

    /**
     * The component id for the select multiple toggle button.
     */
    public static final String SELECT_MULTIPLE_TOGGLE_BUTTON_ID =
            "_selectMultipleToggleButton";

    /**
     * The facet name for the select multiple toggle button.
     */
    public static final String SELECT_MULTIPLE_TOGGLE_BUTTON_FACET =
            "selectMultipleToggleButton";

    /**
     * The component id for the selection column sort button.
     */
    public static final String SELECT_SORT_BUTTON_ID = "_selectSortButton";

    /**
     * The facet name for the selection column sort button.
     */
    public static final String SELECT_SORT_BUTTON_FACET = "selectSortButton";

    /**
     * The component id for the sort level text.
     */
    public static final String SORT_LEVEL_TEXT_ID = "_sortLevelText";

    /**
     * The facet name for the sort level text.
     */
    public static final String SORT_LEVEL_TEXT_FACET = "sortLevelText";

    /**
     * The component id for the toggle sort button.
     */
    public static final String TOGGLE_SORT_BUTTON_ID = "_toggleSortButton";

    /**
     * The facet name for the toggle sort button.
     */
    public static final String TOGGLE_SORT_BUTTON_FACET = "toggleSortButton";

    /**
     * The component id for the warning icon.
     */
    public static final String WARNING_ICON_ID = "_warningIcon";

    /**
     * The facet name for the warning icon.
     */
    public static final String WARNING_ICON_FACET = "warningIcon";

    /**
     * The Table ancestor enclosing this component.
     */
    private Table table;

    /**
     * The TableColumn ancestor enclosing this component.
     */
    private TableColumn tableColumn;

    /**
     * The TableRowGroup ancestor enclosing this component.
     */
    private TableRowGroup tableRowGroup;

    /**
     * Flag indicating that the next sort order is descending.
     */
    private boolean descending;

    /**
     * descending set flag.
     */
    private boolean descendingSet;

    /**
     * The total number of selected rows.
     */
    private int selectedRowsCount = -1;

    /**
     * The total number of sorts applied.
     */
    private int sortCount = -1;

    /**
     * Sort level for this component.
     */
    private int sortLevel = -1;

    /**
     * ABBR gives an abbreviated version of the cell's content. This allows
     * visual browsers to use the short form if space is limited, and non-visual
     * browsers can give a cell's header information in an abbreviated form
     * before rendering each cell.
     */
    @Property(name = "abbr", displayName = "Abbreviation for Header Cell")
    private String abbr;

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
    @Property(name = "align", displayName = "Horizontal Alignment")
    private String align;

    /**
     * The AXIS attribute provides a method of categorizing cells. The
     * attribute's value is a comma-separated list of category names. See the
     * HTML 4.0 Recommendation's section on categorizing cells for an
     * application of AXIS.
     */
    @Property(name = "axis", displayName = "Category of Header Cell")
    private String axis;

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
    @Property(name = "bgColor", displayName = "Cell Background Color")
    private String bgColor;

    /**
     * Use the {@code char }attribute to specify a character to use for
     * horizontal alignment in each cell in the row. You must also set the
     * {@code align} attribute to {@code char} to enable character
     * alignment to be used. The default value for the {@code char}
     * attribute is the decimal point of the current language, such as a period
     * in English. The {@code char} HTML property is not supported by all
     * browsers.
     */
    @Property(name = "char", displayName = "Alignment Character")
    private String charFor;

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
    @Property(name = "charOff", displayName = "Alignment Character Offset")
    private String charOff;

    /**
     * The COLSPAN attribute of TH specifies the number of columns that are
     * spanned by the cell. The default value is 1. The special value 0
     * indicates that the cell spans all columns to the end of the table. The
     * value 0 is ignored by most browsers, so authors may wish to calculate the
     * exact number of rows or columns spanned and use that value.
     */
    @Property(name = "colSpan", displayName = "Columns Spanned By the Cell")
    private int colSpan = Integer.MIN_VALUE;

    /**
     * colSpan set flag.
     */
    private boolean colSpanSet;

    /**
     * Extra HTML to be appended to the tag output by this renderer.
     */
    @Property(name = "extraHtml", displayName = "Extra HTML")
    private String extraHtml;

    /**
     * Flag indicating this component should render a group header. The default
     * renders a column header. This should not be used if selectHeader or
     * sortHeader are used.
     */
    @Property(name = "groupHeader",
            displayName = "Is Group Header",
            isAttribute = false)
    private boolean groupHeader;

    /**
     * groupHeader set flag.
     */
    private boolean groupHeaderSet;

    /**
     * The HEADERS attribute specifies the header cells that apply to the TH.
     * The value is a space-separated list of the header cells' ID attribute
     * values. The HEADERS attribute allows non-visual browsers to render the
     * header information for a given cell.
     */
    @Property(name = "headers",
            displayName = "List of Header Cells for Current Cell")
    private String headers;

    /**
     * The number of pixels for the cell's height. Styles should be used to
     * specify cell height when possible because the height attribute is
     * deprecated in HTML 4.0.
     */
    @Property(name = "height",
            displayName = "Height")
    private String height;

    /**
     * Use the {@code noWrap} attribute to disable word wrapping of this
     * column's cells in visual browsers. Word wrap can cause unnecessary
     * horizontal scrolling when the browser window is small in relation to the
     * font size. Styles should be used to disable word wrap when possible
     * because the nowrap attribute is deprecated in HTML 4.0.
     */
    @Property(name = "noWrap",
            displayName = "Suppress Word Wrap")
    private boolean noWrap;

    /**
     * noWrap set flag.
     */
    private boolean noWrapSet;

    /**
     * Scripting code executed when a mouse click occurs over this component.
     */
    @Property(name = "onClick",
            displayName = "Click Script")
    private String onClick;

    /**
     * Scripting code executed when a mouse double click occurs over this
     * component.
     */
    @Property(name = "onDblClick",
            displayName = "Double Click Script")
    private String onDblClick;

    /**
     * Scripting code executed when the user presses down on a key while the
     * component has focus.
     */
    @Property(name = "onKeyDown",
            displayName = "Key Down Script")
    private String onKeyDown;

    /**
     * Scripting code executed when the user presses and releases a key while
     * the component has focus.
     */
    @Property(name = "onKeyPress",
            displayName = "Key Press Script")
    private String onKeyPress;

    /**
     * Scripting code executed when the user releases a key while the component
     * has focus.
     */
    @Property(name = "onKeyUp",
            displayName = "Key Up Script")
    private String onKeyUp;

    /**
     * Scripting code executed when the user presses a mouse button while the
     * mouse pointer is on the component.
     */
    @Property(name = "onMouseDown",
            displayName = "Mouse Down Script")
    private String onMouseDown;

    /**
     * Scripting code executed when the user moves the mouse pointer while over
     * the component.
     */
    @Property(name = "onMouseMove",
            displayName = "Mouse Move Script")
    private String onMouseMove;

    /**
     * Scripting code executed when a mouse out movement occurs over this
     * component.
     */
    @Property(name = "onMouseOut",
            displayName = "Mouse Out Script")
    private String onMouseOut;

    /**
     * Scripting code executed when the user moves the mouse pointer into the
     * boundary of this component.
     */
    @Property(name = "onMouseOver",
            displayName = "Mouse In Script")
    private String onMouseOver;

    /**
     * Scripting code executed when the user releases a mouse button while the
     * mouse pointer is on the component.
     */
    @Property(name = "onMouseUp",
            displayName = "Mouse Up Script")
    private String onMouseUp;

    /**
     * The ROWSPAN attribute of TH specifies the number of rows that are spanned
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
    private boolean rowSpanSet;

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
    private String scope;

    /**
     * Flag indicating this component should render a selection column header.
     * The default renders a column header. This should not be used if
     * groupHeader or sortHeader are used.
     */
    @Property(name = "selectHeader",
            displayName = "Is Select Header",
            isAttribute = false)
    private boolean selectHeader;

    /**
     * selectHeader set flag.
     */
    private boolean selectHeaderSet;

    /**
     * Flag indicating this component should render a sortable column header.
     * The default renders a column header. This should not be used if
     * groupHeader or selectHeader are used.
     */
    @Property(name = "sortHeader",
            displayName = "Is Sort Header",
            isAttribute = false)
    private boolean sortHeader;

    /**
     * sortHeader set flag.
     */
    private boolean sortHeaderSet;

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
     * Sets the value of the title attribute for the HTML element. The specified
     * text will display as a tool tip if the mouse cursor hovers over the HTML
     * element.
     */
    @Property(name = "toolTip",
            displayName = "Tool Tip", category = "Behavior")
    private String toolTip;

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
    private String valign;

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
     * Use the {@code width} attribute to specify the width of the cells of
     * the column. The width can be specified as the number of pixels or the
     * percentage of the table width, and is especially useful for spacer
     * columns. This attribute is deprecated in HTML 4.0 in favor of style
     * sheets.
     */
    @Property(name = "width", displayName = "Width")
    private String width;

    /**
     * Default constructor.
     */
    public TableHeader() {
        super();
        setRendererType("com.sun.webui.jsf.TableHeader");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.TableHeader";
    }

    /**
     * Helper method to get the total number of sorts applied.
     *
     * @return The sort count.
     */
    public int getSortCount() {
        if (sortCount == -1) {
            TableRowGroup group = getTableRowGroupAncestor();
            if (group != null) {
                sortCount = group.getSortCount();
            } else {
                sortCount = 0;
            }
        }
        return sortCount;
    }

    /**
     * Helper method to get sort level for this component.
     *
     * @return The sort level or 0 if sort does not apply.
     */
    public int getSortLevel() {
        if (sortLevel == -1) {
            TableColumn col = getTableColumnAncestor();
            TableRowGroup group = getTableRowGroupAncestor();
            if (col != null && group != null) {
                sortLevel = group.getSortLevel(col.getSortCriteria());
            } else {
                log("getSortLevel",
                        "Cannot obtain sort level, TableColumn or"
                                + " TableRowGroup is null");
            }
        }
        return sortLevel;
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
     * Get the closest TableColumn ancestor that encloses this component.
     *
     * @return The TableColumn ancestor.
     */
    public TableColumn getTableColumnAncestor() {
        if (tableColumn == null) {
            UIComponent component = this;
            while (component != null) {
                component = component.getParent();
                if (component instanceof TableColumn) {
                    tableColumn = (TableColumn) component;
                    break;
                }
            }
        }
        return tableColumn;
    }

    /**
     * Get the closest TableRowGroup ancestor that encloses this component.
     *
     * @return The TableRowGroup ancestor.
     */
    public TableRowGroup getTableRowGroupAncestor() {
        if (tableRowGroup == null) {
            UIComponent component = this;
            while (component != null) {
                component = component.getParent();
                if (component instanceof TableRowGroup) {
                    tableRowGroup = (TableRowGroup) component;
                    break;
                }
            }
        }
        return tableRowGroup;
    }

    /**
     * Get select multiple toggle button.
     *
     * @return The select multiple toggle button.
     */
    public UIComponent getCollapsedHiddenField() {
        UIComponent facet = getFacet(COLLAPSED_HIDDEN_FIELD_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        HiddenField child = new HiddenField();
        child.setId(COLLAPSED_HIDDEN_FIELD_ID);

        // Set value.
        TableRowGroup group = getTableRowGroupAncestor();
        if (group != null) {
            child.setText(group.isCollapsed());
        } else {
            log("getCollapsedHiddenField",
                    "Cannot set collapsed hidden field value,"
                            + " TableRowGroup is null");
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get group panel toggle button.
     *
     * @return The group panel toggle button.
     */
    public UIComponent getGroupPanelToggleButton() {
        UIComponent facet = getFacet(GROUP_PANEL_TOGGLE_BUTTON_FACET);
        if (facet != null) {
            return facet;
        }

        Theme theme = getTheme();
        Table tableAncestor = getTableAncestor();
        TableRowGroup group = getTableRowGroupAncestor();

        // Get child.
        IconHyperlink child = new IconHyperlink();
        child.setId(GROUP_PANEL_TOGGLE_BUTTON_ID);
        if (group != null && group.isCollapsed()) {
            child.setIcon(ThemeImages.TABLE_GROUP_PANEL);
        } else {
            child.setIcon(ThemeImages.TABLE_GROUP_PANEL);
        }
        child.setBorder(0);

        // Set JS to display table preferences panel.
        StringBuilder buff = new StringBuilder();
        if (tableAncestor != null && group != null) {
            buff.append("document.getElementById('")
                    .append(tableAncestor.getClientId(getFacesContext()))
                    .append("').toggleGroupPanel('")
                    .append(group.getClientId(getFacesContext()))
                    .append("'); return false");
            child.setOnClick(buff.toString());
        } else {
            log("getGroupPanelToggleButton",
                    "onClick not set, Table or TableRowGroup is null");
        }

        // Set tool tip.
        String zToolTip;
        if (group != null && group.isCollapsed()) {
            zToolTip = theme.getMessage("table.group.expand");
        } else {
            zToolTip = theme.getMessage("table.group.collapse");
        }
        child.setAlt(zToolTip);
        child.setToolTip(zToolTip);

        // Set tab index.
        if (tableAncestor != null) {
            child.setTabIndex(tableAncestor.getTabIndex());
        } else {
            log("getGroupPanelToggleButton",
                    "Tab index not set, Table is null");
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get select multiple toggle button.
     *
     * @return The select multiple toggle button.
     */
    public UIComponent getSelectMultipleToggleButton() {
        UIComponent facet = getFacet(SELECT_MULTIPLE_TOGGLE_BUTTON_FACET);
        if (facet != null) {
            return facet;
        }

        Table tableAncestor = getTableAncestor();
        TableRowGroup group = getTableRowGroupAncestor();

        // Get child.
        Checkbox child = new Checkbox();
        child.setId(SELECT_MULTIPLE_TOGGLE_BUTTON_ID);
        child.setSelectedValue(Boolean.TRUE);

        // Set JS to display table preferences panel.
        StringBuilder buff = new StringBuilder();
        if (tableAncestor != null && group != null) {
            buff.append("document.getElementById('")
                    .append(tableAncestor.getClientId(getFacesContext()))
                    .append("').selectGroupRows('")
                    .append(group.getClientId(getFacesContext()))
                    .append("', this.checked)");
            child.setOnClick(buff.toString());
        } else {
            log("getSelectMultipleToggleButton",
                    "onClick not set, Table or TableRowgroup is null");
        }

        // Set selected property.
        if (group != null) {
            // Checkbox is checked only if all rendered rows are selected.
            RowKey[] rowKeys = group.getRenderedRowKeys();
            if (rowKeys != null && rowKeys.length > 0
                    && rowKeys.length == getSelectedRowsCount()) {
                child.setSelected(Boolean.TRUE);
                child.setToolTip(getTheme().getMessage(
                        "table.group.deselectMultiple"));
            } else {
                child.setToolTip(getTheme().getMessage(
                        "table.group.selectMultiple"));
            }
        } else {
            log("getSelectMultipleToggleButton",
                    "Tool tip & selected not set, TableRowGroup is null");
        }

        // Set tab index.
        if (tableAncestor != null) {
            child.setTabIndex(tableAncestor.getTabIndex());
        } else {
            log("getSelectMultipleToggleButton",
                    "Tab index not set, Table is null");
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get warning icon.
     *
     * @return The warning icon.
     */
    public UIComponent getWarningIcon() {
        UIComponent facet = getFacet(WARNING_ICON_FACET);
        if (facet != null) {
            return facet;
        }

        Theme theme = getTheme();
        TableRowGroup group = getTableRowGroupAncestor();

        // Get child.
        Icon child = ThemeUtilities.getIcon(theme,
                ThemeImages.ALERT_WARNING_SMALL);

        // Warning icon is only rendered if at least one row is selected and the
        // select multiple toggle is not checked.
        RowKey[] rowKeys;
        if (group != null) {
            rowKeys = group.getRenderedRowKeys();
        } else {
            rowKeys = null;
        }
        int rows = getSelectedRowsCount();
        if (group != null
                && !group.isCollapsed()
                || rows == 0
                || rowKeys != null
                && rowKeys.length > 0
                && rowKeys.length == rows) {
            // Replace default icon with place holder.
            Icon placeHolder = ThemeUtilities.getIcon(theme, ThemeImages.DOT);
            placeHolder.setHeight(child.getHeight());
            placeHolder.setWidth(child.getWidth());
            child = placeHolder;
        } else {
            log("getWarningIcon",
                    "Height & width not set, TableRowGroup is null");
        }
        child.setId(WARNING_ICON_ID);
        child.setBorder(0);

        // Set tool tip.
        String zToolTip;
        if (group != null && group.isCollapsed()) {
            zToolTip = theme.getMessage("table.group.warning");
        } else {
            zToolTip = null;
        }
        child.setAlt(zToolTip);
        child.setToolTip(zToolTip);

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get add sort button.
     *
     * @return The add sort button.
     */
    public UIComponent getAddSortButton() {
        UIComponent facet = getFacet(ADD_SORT_BUTTON_FACET);
        if (facet != null) {
            return facet;
        }

        Theme theme = getTheme();
        Table tableAncestor = getTableAncestor();
        TableColumn col = getTableColumnAncestor();

        // Get child.
        IconHyperlink child = new IconHyperlink();
        child.setId(ADD_SORT_BUTTON_ID);
        child.setIcon(ThemeImages.TABLE_SORT_ADD);
        child.setBorder(0);
        child.setAlign("top");
        child.setStyleClass(
                theme.getStyleClass(ThemeStyles.TABLE_HEADER_LINK_IMG));
        child.addActionListener(new TableSortActionListener());

        // Set tool tip.
        String zToolTip = getTheme().getMessage("table.sort.button.add",
                new String[]{getNextSortToolTipAugment()});
        child.setToolTip(zToolTip);

        // Set alt.
        if (isSelectHeader()) {
            child.setAlt(theme.getMessage("table.sort.alt.add",
                    new String[]{
                        theme.getMessage("table.select.selectionColumn"),
                    }));
        } else {
            String header;
            if (col != null && col.getHeaderText() != null) {
                header = col.getHeaderText();
            } else {
                header = "";
            }
            // Select column does not have header text.
            child.setAlt(theme.getMessage("table.sort.alt.add",
                    new String[]{header}));
        }

        // Set tab index.
        if (tableAncestor != null) {
            child.setTabIndex(tableAncestor.getTabIndex());
        } else {
            log("getAddSortButton", "Tab index not set, Table is null");
        }

        // Add sort level text child.
        if (getSortCount() > 0 && getSortLevel() > 0) {
            // Span must appear within hyperlink for style to render properly.
            child.getChildren().add(getSortLevelText());
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child);

        return child;
    }

    /**
     * Get primary sort button.
     *
     * @return The primary sort button.
     */
    public UIComponent getPrimarySortButton() {
        UIComponent facet = getFacet(PRIMARY_SORT_BUTTON_FACET);
        if (facet != null) {
            return facet;
        }

        Theme theme = getTheme();
        Table tableAncestor = getTableAncestor();
        TableColumn col = getTableColumnAncestor();

        // Get child.
        IconHyperlink child = new IconHyperlink();
        child.setId(PRIMARY_SORT_BUTTON_ID);
        child.setIcon(ThemeImages.TABLE_SORT_PRIMARY);
        child.setBorder(0);
        child.setAlign("top");
        child.setStyleClass(
                theme.getStyleClass(ThemeStyles.TABLE_HEADER_LINK_IMG));
        child.addActionListener(new TableSortActionListener());

        // Set tool tip.
        String zToolTip = theme.getMessage("table.sort.button.primary",
                new String[]{getNextSortToolTipAugment()});
        child.setToolTip(zToolTip);

        // Set alt.
        if (col != null) {
            String header;
            if (col.getHeaderText() != null) {
                header = col.getHeaderText();
            } else {
                header = "";
            }
            child.setAlt(theme.getMessage("table.sort.alt.primary",
                    new String[]{header}));
        } else {
            log("getPrimarySortButton", "Alt text not set,"
                    + " TableColumn is null");
        }

        // Set tab index.
        if (tableAncestor != null) {
            child.setTabIndex(tableAncestor.getTabIndex());
        } else {
            log("getPrimarySortButton", "Tab index not set, Table is null");
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get primary sort link.
     *
     * @return The primary sort link.
     */
    public UIComponent getPrimarySortLink() {
        UIComponent facet = getFacet(PRIMARY_SORT_LINK_FACET);
        if (facet != null) {
            return facet;
        }

        Theme theme = getTheme();
        Table tableAncestor = getTableAncestor();
        TableColumn col = getTableColumnAncestor();

        // Get child.
        IconHyperlink child = new IconHyperlink();
        child.setId(PRIMARY_SORT_LINK_ID);
        child.setStyleClass(theme
                .getStyleClass(ThemeStyles.TABLE_HEADER_LINK));
        child.addActionListener(new TableSortActionListener());

        // Get tool tip.
        String zToolTip = "table.sort.link.other";
        if (getSortLevel() == 1 && getSortCount() == 1) {
            // Primary sort column, only sort applied.
            zToolTip = "table.sort.link.primary";
        } else if (getSortCount() == 0) {
            // No sorts applied.
            zToolTip = "table.sort.link.none";
        }

        // Set column properties.
        if (col != null) {
            child.setIcon(col.getSortIcon());
            child.setText(col.getHeaderText());
            child.setImageURL(col.getSortImageURL());
            child.setToolTip(getTheme().getMessage(zToolTip,
                    new String[]{
                        col.getSortToolTipAugment(col.isDescending()),
                    }));
        } else {
            log("getPrimarySortLink",
                    "Tool tip, icon, text, & image URL not set,"
                            + " TableColumn is null");
        }

        // Set tab index.
        if (tableAncestor != null) {
            child.setTabIndex(tableAncestor.getTabIndex());
        } else {
            log("getPrimarySortLink", "Tab index not set, Table is null");
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get select sort button.
     *
     * @return The title sort button.
     */
    public UIComponent getSelectSortButton() {
        UIComponent facet = getFacet(SELECT_SORT_BUTTON_FACET);
        if (facet != null) {
            return facet;
        }

        Theme theme = getTheme();
        Table tableAncestor = getTableAncestor();

        // Get child.
        IconHyperlink child = new IconHyperlink();
        child.setId(SELECT_SORT_BUTTON_ID);
        child.setIcon(ThemeImages.TABLE_SORT_SELECT);
        child.setBorder(0);
        child.setAlign("top");
        child.setStyleClass(
                theme.getStyleClass(ThemeStyles.TABLE_HEADER_LINK));
        child.addActionListener(new TableSortActionListener());

        // Get tool tip. Note: Use same tooltip as link -- bugtraq #6339188.
        String zToolTip = "table.sort.link.other";
        if (getSortLevel() == 1 && getSortCount() == 1) {
            // Primary sort column, only sort applied.
            zToolTip = "table.sort.link.primary";
        } else if (getSortCount() == 0) {
            // No sorts applied.
            zToolTip = "table.sort.link.none";
        }

        // Set tool tip.
        child.setToolTip(theme.getMessage(zToolTip,
                new String[]{getNextSortToolTipAugment()}));

        // Set alt.
        String alt = theme.getMessage("table.sort.alt.primary",
                new String[]{
                    theme.getMessage("table.select.selectionColumn"),
                });
        child.setAlt(alt);

        // Set tab index.
        if (tableAncestor != null) {
            child.setTabIndex(tableAncestor.getTabIndex());
        } else {
            log("getSelectSortButton", "Tab index not set, Table is null");
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get sort level static text.
     *
     * @return The sort level static text.
     */
    public UIComponent getSortLevelText() {
        UIComponent facet = getFacet(SORT_LEVEL_TEXT_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        StaticText child = new StaticText();
        child.setId(SORT_LEVEL_TEXT_ID);
        child.setText(Integer.toString(getSortLevel()));
        child.setStyleClass(getTheme().getStyleClass(
                ThemeStyles.TABLE_HEADER_SORTNUM));

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get toggle sort button.
     *
     * @return The toggle sort button.
     */
    public UIComponent getToggleSortButton() {
        UIComponent facet = getFacet(TOGGLE_SORT_BUTTON_FACET);
        if (facet != null) {
            return facet;
        }

        Theme theme = getTheme();
        Table tableAncestor = getTableAncestor();
        TableColumn col = getTableColumnAncestor();
        TableRowGroup group = getTableRowGroupAncestor();

        // Get child.
        IconHyperlink child = new IconHyperlink();
        child.setId(TOGGLE_SORT_BUTTON_ID);
        child.setBorder(0);
        child.setAlign("top");
        child.addActionListener(new TableSortActionListener());

        // Disable descending sort so selections don't move off page.
        if (tableAncestor != null && col != null) {
            if (!isDescending() && group.isPaginated()
                    && col.getSelectId() != null
                    && !tableAncestor.isHiddenSelectedRows()) {
                child.setDisabled(true);
            }
        } else {
            log("getToggleSortButton",
                    "Disabled state not set, Table or TableColumn is null");
        }

        // Set alt and tool tip for the next sort applied.
        if (col != null) {
            // Get tool tip.
            child.setToolTip(theme.getMessage("table.sort.button.toggle",
                    new String[]{
                        col.getSortToolTipAugment(!isDescending()),
                    }));

            // Get alt.
            if (isSelectHeader()) {
                // Select column does not have header text.
                child.setAlt(theme.getMessage("table.sort.alt.primary",
                        new String[]{
                            theme.getMessage("table.select.selectionColumn"),
                            col.getSortToolTipAugment(isDescending()),
                            Integer.toString(getSortLevel()),
                        }));
            } else {
                String header;
                if (col.getHeaderText() != null) {
                    header = col.getHeaderText();
                } else {
                    header = "";
                }
                child.setAlt(theme.getMessage("table.sort.alt.toggle",
                        new String[]{header,
                            col.getSortToolTipAugment(isDescending()),
                            Integer.toString(getSortLevel())}));
            }
        } else {
            log("getToggleSortButton", "Alt text not set, TableColumn is null");
        }

        // Set icon for the next sort applied.
        if (child.isDisabled()) {
            child.setIcon(ThemeImages.TABLE_SORT_DESCENDING_DISABLED);
        } else if (!isDescending()) {
            child.setIcon(ThemeImages.TABLE_SORT_DESCENDING);
        } else {
            child.setIcon(ThemeImages.TABLE_SORT_ASCENDING);
        }

        // Set styleClass.
        if (child.isDisabled()) {
            if (getSortLevel() == 1) {
                child.setStyleClass(
                        theme.getStyleClass(
                                ThemeStyles.TABLE_HEADER_SORT_DISABLED));
            } else {
                child.setStyleClass(
                        theme.getStyleClass(
                                ThemeStyles.TABLE_HEADER_SELECTCOL_DISABLED));
            }
        } else {
            child.setStyleClass(
                    theme.getStyleClass(ThemeStyles.TABLE_HEADER_LINK_IMG));
        }

        // Add sort level text.
        if (getSortLevel() > 0 && getSortCount() > 0) {
            // Span must appear within hyperlink for style to render properly.
            child.getChildren().add(getSortLevelText());
        }

        // Set tab index.
        if (tableAncestor != null) {
            child.setTabIndex(tableAncestor.getTabIndex());
        } else {
            log("getToggleSortButton", "Tab index not set, Table is null");
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child);

        return child;
    }

    /**
     * If the rendered property is true, render the begining of the current
     * state of this UIComponent to the response contained in the specified
     * FacesContext.
     *
     * If a Renderer is associated with this UIComponent, the actual encoding
     * will be delegated to Renderer.encodeBegin(FacesContext, UIComponent).
     *
     * @param context FacesContext for the current request.
     *
     * @exception IOException if an input/output error occurs while rendering.
     * @exception NullPointerException if FacesContext is null.
     */
    @Override
    public void encodeBegin(final FacesContext context) throws IOException {
        // Clear cached variables -- bugtraq #6300020.
        table = null;
        tableColumn = null;
        tableRowGroup = null;
        descending = false;
        descendingSet = false;
        selectedRowsCount = -1;
        sortCount = -1;
        sortLevel = -1;
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
     *
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
     *
     * @param newBgColor bgColor
     */
    public void setBgColor(final String newBgColor) {
        this.bgColor = newBgColor;
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
     * The COLSPAN attribute of TH specifies the number of columns that are
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
     * The COLSPAN attribute of TH specifies the number of columns that are
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
     * Flag indicating this component should render a group header. The default
     * renders a column header. This should not be used if selectHeader or
     * sortHeader are used.
     * @return {@code boolean}
     */
    public boolean isGroupHeader() {
        if (this.groupHeaderSet) {
            return this.groupHeader;
        }
        ValueExpression vb = getValueExpression("groupHeader");
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
     * Flag indicating this component should render a group header. The default
     * renders a column header. This should not be used if selectHeader or
     * sortHeader are used.
     * @param newGroupHeader groupHeader
     */
    public void setGroupHeader(final boolean newGroupHeader) {
        this.groupHeader = newGroupHeader;
        this.groupHeaderSet = true;
    }

    /**
     * The HEADERS attribute specifies the header cells that apply to the TH.
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
     * The HEADERS attribute specifies the header cells that apply to the TH.
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
     * The ROWSPAN attribute of TH specifies the number of rows that are spanned
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
     * The ROWSPAN attribute of TH specifies the number of rows that are spanned
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
     * Flag indicating this component should render a selection column header.
     * The default renders a column header. This should not be used if
     * groupHeader or sortHeader are used.
     * @return {@code boolean}
     */
    public boolean isSelectHeader() {
        if (this.selectHeaderSet) {
            return this.selectHeader;
        }
        ValueExpression vb = getValueExpression("selectHeader");
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
     * Flag indicating this component should render a selection column header.
     * The default renders a column header. This should not be used if
     * groupHeader or sortHeader are used.
     * @param newSelectHeader selectHeader
     */
    public void setSelectHeader(final boolean newSelectHeader) {
        this.selectHeader = newSelectHeader;
        this.selectHeaderSet = true;
    }

    /**
     * Flag indicating this component should render a sortable column header.
     * The default renders a column header. This should not be used if
     * groupHeader or selectHeader are used.
     * @return {@code boolean}
     */
    public boolean isSortHeader() {
        if (this.sortHeaderSet) {
            return this.sortHeader;
        }
        ValueExpression vb = getValueExpression("sortHeader");
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
     * Flag indicating this component should render a sortable column header.
     * The default renders a column header. This should not be used if
     * groupHeader or selectHeader are used.
     * @param newSortHeader sortHeader
     */
    public void setSortHeader(final boolean newSortHeader) {
        this.sortHeader = newSortHeader;
        this.sortHeaderSet = true;
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
        this.groupHeader = ((Boolean) values[10]);
        this.groupHeaderSet = ((Boolean) values[11]);
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
        this.rowSpan = ((Integer) values[26]);
        this.rowSpanSet = ((Boolean) values[27]);
        this.scope = (String) values[28];
        this.selectHeader = ((Boolean) values[29]);
        this.selectHeaderSet = ((Boolean) values[30]);
        this.sortHeader = ((Boolean) values[31]);
        this.sortHeaderSet = ((Boolean) values[32]);
        this.style = (String) values[33];
        this.styleClass = (String) values[34];
        this.toolTip = (String) values[35];
        this.valign = (String) values[36];
        this.visible = ((Boolean) values[37]);
        this.visibleSet = ((Boolean) values[38]);
        this.width = (String) values[39];
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[40];
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
        if (groupHeader) {
            values[10] = Boolean.TRUE;
        } else {
            values[10] = Boolean.FALSE;
        }
        if (groupHeaderSet) {
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
        values[26] = this.rowSpan;
        if (rowSpanSet) {
            values[27] = Boolean.TRUE;
        } else {
            values[27] = Boolean.FALSE;
        }
        values[28] = this.scope;
        if (selectHeader) {
            values[29] = Boolean.TRUE;
        } else {
            values[29] = Boolean.FALSE;
        }
        if (selectHeaderSet) {
            values[30] = Boolean.TRUE;
        } else {
            values[30] = Boolean.FALSE;
        }
        if (sortHeader) {
            values[31] = Boolean.TRUE;
        } else {
            values[31] = Boolean.FALSE;
        }
        if (sortHeaderSet) {
            values[32] = Boolean.TRUE;
        } else {
            values[32] = Boolean.FALSE;
        }
        values[33] = this.style;
        values[34] = this.styleClass;
        values[35] = this.toolTip;
        values[36] = this.valign;
        if (visible) {
            values[37] = Boolean.TRUE;
        } else {
            values[37] = Boolean.FALSE;
        }
        if (visibleSet) {
            values[38] = Boolean.TRUE;
        } else {
            values[38] = Boolean.FALSE;
        }
        values[39] = this.width;
        return values;
    }

    /**
     * Helper method to get next sort tool tip augment based on the value for
     * the align property of TableColumn.
     *
     * @return The sort tool tip augment.
     */
    private String getNextSortToolTipAugment() {
        TableColumn col = getTableColumnAncestor();
        if (col != null) {
            return col.getSortToolTipAugment(isDescending());
        }
        return "";
    }

    /**
     * Helper method to get the total number of selected rows.
     *
     * @return The number of selected rows.
     */
    private int getSelectedRowsCount() {
        if (selectedRowsCount == -1) {
            TableRowGroup group = getTableRowGroupAncestor();
            if (group != null) {
                selectedRowsCount = group.getRenderedSelectedRowsCount();
            }
        }
        return selectedRowsCount;
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
     * Helper method to test if the next sort order is descending.
     *
     * @return true if descending, else false.
     */
    private boolean isDescending() {
        if (!descendingSet) {
            TableColumn col = getTableColumnAncestor();
            TableRowGroup group = getTableRowGroupAncestor();

            // Get next sort order.
            if (col != null && group != null) {
                if (getSortLevel() > 0) {
                    descending = group.isDescendingSort(col.getSortCriteria());
                } else {
                    descending = col.isDescending();
                }
                descendingSet = true;
            }
        }
        return descending;
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
