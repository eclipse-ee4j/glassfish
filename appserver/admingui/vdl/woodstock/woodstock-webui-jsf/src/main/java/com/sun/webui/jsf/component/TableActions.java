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
import com.sun.webui.jsf.event.TablePaginationActionListener;
import com.sun.webui.jsf.event.TableSortActionListener;
import com.sun.webui.jsf.theme.ThemeImages;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.ClientSniffer;
import com.sun.webui.jsf.util.LogUtil;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.el.ValueExpression;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.context.FacesContext;

import java.io.IOException;

import static com.sun.webui.jsf.theme.ThemeImages.TABLE_ACTIONS_SEPARATOR;

/**
 * Component that represents a table action bar.
 * <p>
 * Note: Column headers and footers are rendered by TableRowGroupRenderer. Table
 * column footers are rendered by TableRenderer.
 * </p>
 *
 * <pre>
 * java.util.logging.ConsoleHandler.level = FINE
 * com.sun.webui.jsf.component.TableActions.level = FINE
 * </pre>
 *
 */
@Component(type = "com.sun.webui.jsf.TableActions",
        family = "com.sun.webui.jsf.TableActions",
        displayName = "Actions",
        isTag = false)
public final class TableActions extends UIComponentBase
        implements NamingContainer {

    /**
     * The component id for the actions separator icon.
     */
    public static final String ACTIONS_SEPARATOR_ICON_ID
            = "_actionsSeparatorIcon";

    /**
     * The facet name for the actions separator icon.
     */
    public static final String ACTIONS_SEPARATOR_ICON_FACET
            = "actionsSeparatorIcon";

    /**
     * The component id for the clear sort button.
     */
    public static final String CLEAR_SORT_BUTTON_ID
            = "_clearSortButton";

    /**
     * The facet name for the clear sort button.
     */
    public static final String CLEAR_SORT_BUTTON_FACET = "clearSortButton";

    /**
     * The component id for the deselect multiple button.
     */
    public static final String DESELECT_MULTIPLE_BUTTON_ID
            = "_deselectMultipleButton";

    /**
     * The facet name for the deselect multiple button.
     */
    public static final String DESELECT_MULTIPLE_BUTTON_FACET
            = "deselectMultipleButton";

    /**
     * The component id for the deselect single button.
     */
    public static final String DESELECT_SINGLE_BUTTON_ID
            = "_deselectSingleButton";

    /**
     * The facet name for the deselect single button.
     */
    public static final String DESELECT_SINGLE_BUTTON_FACET
            = "deselectSingleButton";

    /**
     * The component id for the filter label.
     */
    public static final String FILTER_LABEL_ID = "_filterLabel";

    /**
     * The facet name for the filter label.
     */
    public static final String FILTER_LABEL_FACET = "filterLabel";

    /**
     * The component id for the filter separator icon.
     */
    public static final String FILTER_SEPARATOR_ICON_ID
            = "_filterSeparatorIcon";

    /**
     * The facet name for the filter separator icon.
     */
    public static final String FILTER_SEPARATOR_ICON_FACET
            = "filterSeparatorIcon";

    /**
     * The component id for the paginate button.
     */
    public static final String PAGINATE_BUTTON_ID = "_paginateButton";

    /**
     * The facet name for the paginate button.
     */
    public static final String PAGINATE_BUTTON_FACET = "paginateButton";

    /**
     * The component id for the paginate separator icon.
     */
    public static final String PAGINATE_SEPARATOR_ICON_ID
            = "_paginateSeparatorIcon";

    /**
     * The facet name for the paginate separator icon.
     */
    public static final String PAGINATE_SEPARATOR_ICON_FACET
            = "paginateSeparatorIcon";

    /**
     * The component id for the pagination first button.
     */
    public static final String PAGINATION_FIRST_BUTTON_ID
            = "_paginationFirstButton";

    /**
     * The facet name for the pagination first button.
     */
    public static final String PAGINATION_FIRST_BUTTON_FACET
            = "paginationFirstButton";

    /**
     * The component id for the pagination last button.
     */
    public static final String PAGINATION_LAST_BUTTON_ID
            = "_paginationLastButton";

    /**
     * The facet name for the pagination last button.
     */
    public static final String PAGINATION_LAST_BUTTON_FACET
            = "paginationLastButton";

    /**
     * The component id for the pagination next button.
     */
    public static final String PAGINATION_NEXT_BUTTON_ID
            = "_paginationNextButton";

    /**
     * The facet name for the pagination next button.
     */
    public static final String PAGINATION_NEXT_BUTTON_FACET
            = "paginationNextButton";

    /**
     * The component id for the pagination page field.
     */
    public static final String PAGINATION_PAGE_FIELD_ID
            = "_paginationPageField";

    /**
     * The facet name for the pagination page field.
     */
    public static final String PAGINATION_PAGE_FIELD_FACET
            = "paginationPageField";

    /**
     * The component id for the pagination pages text.
     */
    public static final String PAGINATION_PAGES_TEXT_ID
            = "_paginationPagesText";

    /**
     * The facet name for the pagination pages text.
     */
    public static final String PAGINATION_PAGES_TEXT_FACET
            = "paginationPagesText";

    /**
     * The component id for the pagination previous button.
     */
    public static final String PAGINATION_PREV_BUTTON_ID
            = "_paginationPrevButton";

    /**
     * The facet name for the pagination previous button.
     */
    public static final String PAGINATION_PREV_BUTTON_FACET
            = "paginationPrevButton";

    /**
     * The component id for the pagination submit button.
     */
    public static final String PAGINATION_SUBMIT_BUTTON_ID
            = "_paginationSubmitButton";

    /**
     * The facet name for the pagination submit button.
     */
    public static final String PAGINATION_SUBMIT_BUTTON_FACET
            = "paginationSubmitButton";

    /**
     * The component id for the preferences panel button.
     */
    public static final String PREFERENCES_PANEL_TOGGLE_BUTTON_ID
            = "_preferencesPanelToggleButton";

    /**
     * The facet name for the preferences panel button.
     */
    public static final String PREFERENCES_PANEL_TOGGLE_BUTTON_FACET
            = "preferencesPanelToggleButton";

    /**
     * The component id for the select multiple button.
     */
    public static final String SELECT_MULTIPLE_BUTTON_ID
            = "_selectMultipleButton";

    /**
     * The facet name for the select multiple button.
     */
    public static final String SELECT_MULTIPLE_BUTTON_FACET
            = "selectMultipleButton";

    /**
     * The component id for the sort panel toggle button.
     */
    public static final String SORT_PANEL_TOGGLE_BUTTON_ID
            = "_sortPanelToggleButton";

    /**
     * The facet name for the sort panel toggle button.
     */
    public static final String SORT_PANEL_TOGGLE_BUTTON_FACET
            = "sortPanelToggleButton";

    /**
     * The component id for the view actions separator icon.
     */
    public static final String VIEW_ACTIONS_SEPARATOR_ICON_ID
            = "_viewActionsSeparatorIcon";

    /**
     * The facet name for the view actions separator icon.
     */
    public static final String VIEW_ACTIONS_SEPARATOR_ICON_FACET
            = "viewActionsSeparatorIcon";

    /**
     * The Table ancestor enclosing this component.
     */
    private Table table;

    /**
     * ABBR gives an abbreviated version of the cell's content. This allows
     * visual browsers to use the short form if space is limited, and non-visual
     * browsers can give a cell's header information in an abbreviated form
     * before rendering each cell.
     */
    @Property(name = "abbr", displayName = "Abbreviation for Header Cell")
    private String abbr;

    /**
     * Flag indicating this component should render actions at the bottom of the
     * table. The default renders action for the top of the table.
     */
    @Property(name = "actionsBottom",
            displayName = "Is Actions Bottom",
            isAttribute = false)
    private boolean actionsBottom;

    /**
     * actionsBottom set flag.
     */
    private boolean actionsBottomSet;

    /**
     * Use the {@code align} attribute to specify the horizontal alignment for
     * the content of each cell in the column. Valid values are {@code left},
     * {@code center}, {@code right}, {@code justify}, and {@code char}. The
     * default alignment is {@code left}. Setting the {@code align} attribute to
     * {@code char} causes the cell's contents to be aligned on the character
     * that you specify with the {@code char} attribute. For example, to align
     * cell contents on colons, set {@code align="char"} and {@code char=":"
     * }Some browsers do not support aligning on the character.
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
     * {@code align} attribute to {@code char} to enable character alignment to
     * be used. The default value for the {@code char} attribute is the decimal
     * point of the current language, such as a period in English. The
     * {@code char} HTML property is not supported by all browsers.
     */
    @Property(name = "char", displayName = "Alignment Character")
    private String charFor;

    /**
     * Use the {@code charOff }attribute to specify the offset of the first
     * occurrence of the alignment character that is specified with the
     * {@code char} attribute. The offset is the distance from the left cell
     * border, in locales that read from left to right. The {@code charOff}
     * attribute's value can be a number of pixels or a percentage of the cell's
     * width. For example, {@code charOff="50%"} centers the alignment character
     * horizontally in a cell. If {@code charOff="25%"}, the first instance of
     * the alignment character is placed at one fourth of the width of the cell.
     */
    @Property(name = "charOff", displayName = "Alignment Character Offset")
    private String charOff;

    /**
     * The COLSPAN attribute of TD specifies the number of columns that are
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
     * The HEADERS attribute specifies the header cells that apply to the TD.
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
    @Property(name = "height", displayName = "Height")
    private String height;

    /**
     * Use the {@code noWrap} attribute to disable word wrapping of this
     * column's cells in visual browsers. Word wrap can cause unnecessary
     * horizontal scrolling when the browser window is small in relation to the
     * font size. Styles should be used to disable word wrap when possible
     * because the nowrap attribute is deprecated in HTML 4.0.
     */
    @Property(name = "noWrap", displayName = "Suppress Word Wrap")
    private boolean noWrap;

    /**
     * noWrap set flag.
     */
    private boolean noWrapSet;

    /**
     * Scripting code executed when a mouse click occurs over this component.
     */
    @Property(name = "onClick", displayName = "Click Script")
    private String onClick;

    /**
     * Scripting code executed when a mouse double click occurs over this
     * component.
     */
    @Property(name = "onDblClick", displayName = "Double Click Script")
    private String onDblClick;

    /**
     * Scripting code executed when the user presses down on a key while the
     * component has focus.
     */
    @Property(name = "onKeyDown", displayName = "Key Down Script")
    private String onKeyDown;

    /**
     * Scripting code executed when the user presses and releases a key while
     * the component has focus.
     */
    @Property(name = "onKeyPress", displayName = "Key Press Script")
    private String onKeyPress;

    /**
     * Scripting code executed when the user releases a key while the component
     * has focus.
     */
    @Property(name = "onKeyUp", displayName = "Key Up Script")
    private String onKeyUp;

    /**
     * Scripting code executed when the user presses a mouse button while the
     * mouse pointer is on the component.
     */
    @Property(name = "onMouseDown", displayName = "Mouse Down Script")
    private String onMouseDown;

    /**
     * Scripting code executed when the user moves the mouse pointer while over
     * the component.
     */
    @Property(name = "onMouseMove", displayName = "Mouse Move Script")
    private String onMouseMove;

    /**
     * Scripting code executed when a mouse out movement occurs over this
     * component.
     */
    @Property(name = "onMouseOut", displayName = "Mouse Out Script")
    private String onMouseOut;

    /**
     * Scripting code executed when the user moves the mouse pointer into the
     * boundary of this component.
     */
    @Property(name = "onMouseOver", displayName = "Mouse In Script")
    private String onMouseOver;

    /**
     * Scripting code executed when the user releases a mouse button while the
     * mouse pointer is on the component.
     */
    @Property(name = "onMouseUp", displayName = "Mouse Up Script")
    private String onMouseUp;

    /**
     * The ROWSPAN attribute of TD specifies the number of rows that are spanned
     * by the cell. The default value is 1. The special value 0 indicates that
     * the cell spans all rows to the end of the table. The value 0 is ignored
     * by most browsers, so authors may wish to calculate the exact number of
     * rows or columns spanned and use that value.
     */
    @Property(name = "rowSpan", displayName = "Rows Spanned By the Cell")
    private int rowSpan = Integer.MIN_VALUE;

    /**
     * rowSpan set flag.
     */
    private boolean rowSpanSet;

    /**
     * Use the {@code scope} attribute to specify that the data cells of the
     * column are also acting as headers for rows or other columns of the table.
     * This attribute supports assistive technologies by enabling them to
     * determine the order in which to read the cells. Valid values include:
     * <ul>
     * <li>{@code row}, when the cells provide header information for the
     * row</li>
     * <li>{@code col}, when the cells provide header information for the
     * column</li>
     * <li>{@code rowgroup}, when the cells provide header information for the
     * row group</li>
     * <li>{@code colgroup}, when the cells provide header information for the
     * column group</li>
     * </ul>
     */
    @Property(name = "scope", displayName = "Cells Covered By Header Cell")
    private String scope;

    /**
     * CSS style(s) to be applied to the outermost HTML element when this
     * component is rendered.
     */
    @Property(name = "style", displayName = "CSS Style(s)")
    private String style;

    /**
     * CSS style class(es) to be applied to the outermost HTML element when this
     * component is rendered.
     */
    @Property(name = "styleClass", displayName = "CSS Style Class(es)")
    private String styleClass;

    /**
     * Sets the value of the title attribute for the HTML element. The specified
     * text will display as a tooltip if the mouse cursor hovers over the HTML
     * element.
     */
    @Property(name = "toolTip", displayName = "Tool Tip")
    private String toolTip;

    /**
     * Use the {@code valign} attribute to specify the vertical alignment for
     * the content of each cell in the column. Valid values are {@code top},
     * {@code middle}, {@code bottom}, and {@code baseline}. The default
     * vertical alignment is {@code middle}. Setting the {@code valign}
     * attribute to {@code baseline }causes the first line of each cell's
     * content to be aligned on the text baseline, the invisible line on which
     * text characters rest.
     */
    @Property(name = "valign", displayName = "Vertical Position")
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
    @Property(name = "visible", displayName = "Visible")
    private boolean visible;

    /**
     * visible set flag.
     */
    private boolean visibleSet;

    /**
     * Use the {@code width} attribute to specify the width of the cells of the
     * column. The width can be specified as the number of pixels or the
     * percentage of the table width, and is especially useful for spacer
     * columns. This attribute is deprecated in HTML 4.0 in favor of style
     * sheets.
     */
    @Property(name = "width", displayName = "Width")
    private String width;

    /**
     * Default constructor.
     */
    public TableActions() {
        super();
        setRendererType("com.sun.webui.jsf.TableActions");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.TableActions";
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
     * Get first page button for pagination controls.
     *
     * @return The first page button.
     */
    public UIComponent getPaginationFirstButton() {
        UIComponent facet = getFacet(PAGINATION_FIRST_BUTTON_FACET);
        if (facet != null) {
            return facet;
        }

        // Get disabled state.
        Table tableAncestor = getTableAncestor();
        TableRowGroup group;
        if (tableAncestor != null) {
            group = tableAncestor.getTableRowGroupChild();
        } else {
            group = null;
        }
        boolean disabled;
        if (group != null) {
            disabled = group.getFirst() <= 0;
        } else {
            disabled = false;
        }

        // Get child.
        IconHyperlink child = new IconHyperlink();
        child.setId(PAGINATION_FIRST_BUTTON_ID);
        if (disabled) {
            child.setIcon(ThemeImages.TABLE_PAGINATION_FIRST_DISABLED);
        } else {
            child.setIcon(ThemeImages.TABLE_PAGINATION_FIRST);
        }
        child.setBorder(0);
        child.setAlign("top");
        child.setDisabled(disabled);
        child.addActionListener(new TablePaginationActionListener());

        // Set tool tip.
        String zToolTip = getTheme().getMessage("table.pagination.first");
        child.setAlt(zToolTip);
        child.setToolTip(zToolTip);

        // Set tab index.
        if (tableAncestor != null) {
            child.setTabIndex(tableAncestor.getTabIndex());
        } else {
            log("getPaginationFirstButton", "Tab index not set, Table is null");
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get pagination submit button for pagination controls.
     *
     * @return The pagination submit button.
     */
    public UIComponent getPaginationSubmitButton() {
        UIComponent facet = getFacet(PAGINATION_SUBMIT_BUTTON_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        Button child = new Button();
        child.setId(PAGINATION_SUBMIT_BUTTON_ID);
        child.setText(getTheme().getMessage("table.pagination.submit"));
        child.setToolTip(getTheme().getMessage("table.pagination.submitPage"));
        child.addActionListener(new TablePaginationActionListener());

        // Set tab index.
        Table tableAncestor = getTableAncestor();
        if (tableAncestor != null) {
            child.setTabIndex(tableAncestor.getTabIndex());
        } else {
            log("getPaginationSubmitButton",
                    "Tab index not set, Table is null");
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get last page button for pagination controls.
     *
     * @return The last page button.
     */
    public UIComponent getPaginationLastButton() {
        UIComponent facet = getFacet(PAGINATION_LAST_BUTTON_FACET);
        if (facet != null) {
            return facet;
        }

        // Get disabled state.
        Table tableAncestor = getTableAncestor();
        TableRowGroup group;
        if (tableAncestor != null) {
            group = tableAncestor.getTableRowGroupChild();
        } else {
            group = null;
        }
        boolean disabled;
        if (group != null) {
            disabled = group.getFirst() >= group.getLast();
        } else {
            disabled = false;
        }

        // Get child.
        IconHyperlink child = new IconHyperlink();
        child.setId(PAGINATION_LAST_BUTTON_ID);
        if (disabled) {
            child.setIcon(ThemeImages.TABLE_PAGINATION_LAST_DISABLED);
        } else {
            child.setIcon(ThemeImages.TABLE_PAGINATION_LAST);
        }
        child.setBorder(0);
        child.setAlign("top");
        child.setDisabled(disabled);
        child.addActionListener(new TablePaginationActionListener());

        // Set tool tip.
        String zToolTip = getTheme().getMessage("table.pagination.last");
        child.setAlt(zToolTip);
        child.setToolTip(zToolTip);

        // Set tab index.
        if (tableAncestor != null) {
            child.setTabIndex(tableAncestor.getTabIndex());
        } else {
            log("getPaginationLastButton", "Tab index not set, Table is null");
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get next page button for pagination controls.
     *
     * @return The next page button.
     */
    public UIComponent getPaginationNextButton() {
        UIComponent facet = getFacet(PAGINATION_NEXT_BUTTON_FACET);
        if (facet != null) {
            return facet;
        }

        // Get disabled state.
        Table tableAncestor = getTableAncestor();
        TableRowGroup group;
        if (tableAncestor != null) {
            group = tableAncestor.getTableRowGroupChild();
        } else {
            group = null;
        }
        boolean disabled;
        if (group != null) {
            disabled = group.getFirst() >= group.getLast();
        } else {
            disabled = false;
        }

        // Get child.
        IconHyperlink child = new IconHyperlink();
        child.setId(PAGINATION_NEXT_BUTTON_ID);
        if (disabled) {
            child.setIcon(ThemeImages.TABLE_PAGINATION_NEXT_DISABLED);
        } else {
            child.setIcon(ThemeImages.TABLE_PAGINATION_NEXT);
        }
        child.setBorder(0);
        child.setAlign("top");
        child.setDisabled(disabled);
        child.addActionListener(new TablePaginationActionListener());

        // Set tool tip.
        String zToolTip = getTheme().getMessage("table.pagination.next");
        child.setAlt(zToolTip);
        child.setToolTip(zToolTip);

        // Set tab index.
        if (tableAncestor != null) {
            child.setTabIndex(tableAncestor.getTabIndex());
        } else {
            log("getPaginationNextButton", "Tab index not set, Table is null");
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get page field for pagination controls.
     *
     * @return The page field.
     */
    @SuppressWarnings("checkstyle:magicnumber")
    public UIComponent getPaginationPageField() {
        UIComponent facet = getFacet(PAGINATION_PAGE_FIELD_FACET);
        if (facet != null) {
            return facet;
        }

        // Get current page.
        Table tableAncestor = getTableAncestor();
        TableRowGroup group;
        if (tableAncestor != null) {
            group =  tableAncestor.getTableRowGroupChild();
        } else {
            group = null;
        }
        int page;
        if (group != null) {
            page = group.getPage();
        } else {
            page = 1;
        }

        // Get child.
        TextField child = new TextField();
        child.setId(PAGINATION_PAGE_FIELD_ID);
        child.setText(Integer.toString(page));
        child.setOnKeyPress(getPaginationJavascript());
        child.setColumns(3);
        child.setLabelLevel(2);
        child.setLabel(getTheme().getMessage("table.pagination.page"));

        // Set tab index.
        if (tableAncestor != null) {
            child.setTabIndex(tableAncestor.getTabIndex());
        } else {
            log("getPaginationPageField", "Tab index not set, Table is null");
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get pages text for pagination controls.
     *
     * @return The pages text.
     */
    public UIComponent getPaginationPagesText() {
        UIComponent facet = getFacet(PAGINATION_PAGES_TEXT_FACET);
        if (facet != null) {
            return facet;
        }

        Theme theme = getTheme();

        // Get child.
        StaticText child = new StaticText();
        child.setId(PAGINATION_PAGES_TEXT_ID);
        child.setStyleClass(theme.getStyleClass(
                ThemeStyles.TABLE_PAGINATION_TEXT));

        // Set page text.
        Table tableAncestor = getTableAncestor();
        if (tableAncestor != null) {
            child.setText(theme.getMessage("table.pagination.pages",
                    new String[]{
                        Integer.toString(tableAncestor.getPageCount()),
                    }));
        } else {
            log("getPaginationPagesText", "Pages text not set, Table is null");
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get paginate button of pagination controls.
     *
     * @return The paginate button.
     */
    public UIComponent getPaginateButton() {
        UIComponent facet = getFacet(PAGINATE_BUTTON_FACET);
        if (facet != null) {
            return facet;
        }

        // Get paginated state.
        Table tableAncestor = getTableAncestor();
        TableRowGroup group;
        if (tableAncestor != null) {
            group = tableAncestor.getTableRowGroupChild();
        } else {
            group = null;
        }
        boolean paginated;
        if (group != null) {
            paginated = group.isPaginated();
        } else {
            paginated = false;
        }

        // Get child.
        IconHyperlink child = new IconHyperlink();
        child.setId(PAGINATE_BUTTON_ID);
        if (paginated) {
            child.setIcon(ThemeImages.TABLE_SCROLL_PAGE);
        } else {
            child.setIcon(ThemeImages.TABLE_PAGINATE);
        }
        child.setBorder(0);
        child.setAlign("top");
        child.addActionListener(new TablePaginationActionListener());

        // Set i18n tool tip.
        String zToolTip;
        if (paginated) {
            zToolTip = getTheme().getMessage("table.pagination.scroll");
        } else {
            zToolTip = getTheme().getMessage("table.pagination.paginated");
        }
        child.setAlt(zToolTip);
        child.setToolTip(zToolTip);

        // Set tab index.
        if (tableAncestor != null) {
            child.setTabIndex(tableAncestor.getTabIndex());
        } else {
            log("getPaginateButton", "Tab index not set, Table is null");
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get previous page button for pagination controls.
     *
     * @return The previous page button.
     */
    public UIComponent getPaginationPrevButton() {
        UIComponent facet = getFacet(PAGINATION_PREV_BUTTON_FACET);
        if (facet != null) {
            return facet;
        }

        // Get disabled state.
        Table tableAncestor = getTableAncestor();
        TableRowGroup group;
        if (tableAncestor != null) {
            group = tableAncestor.getTableRowGroupChild();
        } else {
            group = null;
        }

        boolean disabled;
        if (group != null) {
            disabled = group.getFirst() <= 0;
        } else {
            disabled = false;
        }

        // Get child.
        IconHyperlink child = new IconHyperlink();
        child.setId(PAGINATION_PREV_BUTTON_ID);
        if (disabled) {
            child.setIcon(ThemeImages.TABLE_PAGINATION_PREV_DISABLED);
        } else {
            child.setIcon(ThemeImages.TABLE_PAGINATION_PREV);
        }
        child.setBorder(0);
        child.setAlign("top");
        child.setDisabled(disabled);
        child.addActionListener(new TablePaginationActionListener());

        // Set tool tip.
        String zToolTip = getTheme().getMessage("table.pagination.previous");
        child.setAlt(zToolTip);
        child.setToolTip(zToolTip);

        // Set tab index.
        if (tableAncestor != null) {
            child.setTabIndex(tableAncestor.getTabIndex());
        } else {
            log("getPaginationPrevButton", "Tab index not set, Table is null");
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get deselect multiple button.
     *
     * @return The deselect multiple button.
     */
    public UIComponent getDeselectMultipleButton() {
        UIComponent facet = getFacet(DESELECT_MULTIPLE_BUTTON_FACET);
        if (facet != null) {
            return facet;
        }

        // Get paginated state.
        Table tableAncestor = getTableAncestor();
        TableRowGroup group;
        if (tableAncestor != null) {
            group = tableAncestor.getTableRowGroupChild();
        } else {
            group = null;
        }
        boolean paginated;
        if (group != null) {
            paginated = group.isPaginated();
        } else {
            paginated = false;
        }

        // Get child.
        IconHyperlink child = new IconHyperlink();
        child.setId(DESELECT_MULTIPLE_BUTTON_ID);
        child.setIcon(ThemeImages.TABLE_DESELECT_MULTIPLE);
        child.setBorder(0);
        child.setAlign("top");

        // Set onClick and tab index.
        if (tableAncestor != null) {
            child.setOnClick(getSelectJavascript(
                    tableAncestor.getDeselectMultipleButtonOnClick(), false));
            child.setTabIndex(tableAncestor.getTabIndex());
        } else {
            log("getDeselectMultipleButton",
                    "Tab index & onClick not set, Table is null");
        }

        // Get tool tip.
        String zToolTip;
        if (paginated) {
            zToolTip = getTheme()
                    .getMessage("table.select.deselectMultiplePaginated");
        } else {
            zToolTip = getTheme().getMessage("table.select.deselectMultiple");
        }
        child.setAlt(zToolTip);
        child.setToolTip(zToolTip);

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get deselect single button.
     *
     * @return The deselect single button.
     */
    public UIComponent getDeselectSingleButton() {
        UIComponent facet = getFacet(DESELECT_SINGLE_BUTTON_FACET);
        if (facet != null) {
            return facet;
        }

        // Get paginated state.
        Table tableAncestor = getTableAncestor();
        TableRowGroup group;
        if (tableAncestor != null) {
            group = tableAncestor.getTableRowGroupChild();
        } else {
            group = null;
        }
        boolean paginated;
        if (group != null) {
            paginated = group.isPaginated();
        } else {
            paginated = false;
        }

        // Get child.
        IconHyperlink child = new IconHyperlink();
        child.setId(DESELECT_SINGLE_BUTTON_ID);
        child.setIcon(ThemeImages.TABLE_DESELECT_SINGLE);
        child.setBorder(0);
        child.setAlign("top");

        // Set onClick and tab index.
        if (tableAncestor != null) {
            child.setOnClick(getSelectJavascript(
                    tableAncestor.getDeselectSingleButtonOnClick(), false));
            child.setTabIndex(tableAncestor.getTabIndex());
        } else {
            log("getDeselectSingleButton",
                    "Tab index & onClick not set, Table is null");
        }

        // Set tool tip.
        String zToolTip;
        if (paginated) {
            zToolTip = getTheme()
                    .getMessage("table.select.deselectSinglePaginated");
        } else {
            zToolTip = getTheme().getMessage("table.select.deselectSingle");
        }
        child.setAlt(zToolTip);
        child.setToolTip(zToolTip);

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get select multiple button.
     *
     * @return The select multiple button.
     */
    public UIComponent getSelectMultipleButton() {
        UIComponent facet = getFacet(SELECT_MULTIPLE_BUTTON_FACET);
        if (facet != null) {
            return facet;
        }

        // Get paginated state.
        Table tableAncestor = getTableAncestor();
        TableRowGroup group;
        if (tableAncestor != null) {
            group = tableAncestor.getTableRowGroupChild();
        } else {
            group = null;
        }
        boolean paginated;
        if (group != null) {
            paginated = group.isPaginated();
        } else {
            paginated = false;
        }

        // Get child.
        IconHyperlink child = new IconHyperlink();
        child.setId(SELECT_MULTIPLE_BUTTON_ID);
        child.setIcon(ThemeImages.TABLE_SELECT_MULTIPLE);
        child.setBorder(0);
        child.setAlign("top");

        // Set onClick and tab index.
        if (tableAncestor != null) {
            child.setOnClick(getSelectJavascript(
                    tableAncestor.getSelectMultipleButtonOnClick(), true));
            child.setTabIndex(tableAncestor.getTabIndex());
        } else {
            log("getSelectMultipleButton",
                    "Tab index & onClick not set, Table is null");
        }

        // Set tool tip.
        String zToolTip;
        if (paginated) {
            zToolTip = getTheme()
                    .getMessage("table.select.selectMultiplePaginated");
        } else {
             zToolTip = getTheme().getMessage("table.select.selectMultiple");
        }
        child.setAlt(zToolTip);
        child.setToolTip(zToolTip);

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get the actions separator icon.
     *
     * @return The top actions separator icon.
     */
    public UIComponent getActionsSeparatorIcon() {
        return getSeparatorIcon(ACTIONS_SEPARATOR_ICON_ID,
                ACTIONS_SEPARATOR_ICON_FACET);
    }

    /**
     * Get the filter separator icon.
     *
     * @return The filter separator icon.
     */
    public UIComponent getFilterSeparatorIcon() {
        return getSeparatorIcon(FILTER_SEPARATOR_ICON_ID,
                FILTER_SEPARATOR_ICON_FACET);
    }

    /**
     * Get the paginate separator icon.
     *
     * @return The paginate separator icon.
     */
    public UIComponent getPaginateSeparatorIcon() {
        return getSeparatorIcon(PAGINATE_SEPARATOR_ICON_ID,
                PAGINATE_SEPARATOR_ICON_FACET);
    }

    /**
     * Get the view actions separator icon.
     *
     * @return The view actions separator icon.
     */
    public UIComponent getViewActionsSeparatorIcon() {
        return getSeparatorIcon(VIEW_ACTIONS_SEPARATOR_ICON_ID,
                VIEW_ACTIONS_SEPARATOR_ICON_FACET);
    }

    /**
     * Get clear sort button.
     *
     * @return The clear sort button.
     */
    public UIComponent getClearSortButton() {
        UIComponent facet = getFacet(CLEAR_SORT_BUTTON_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        IconHyperlink child = new IconHyperlink();
        child.setId(CLEAR_SORT_BUTTON_ID);
        child.setIcon(ThemeImages.TABLE_SORT_CLEAR);
        child.setBorder(0);
        child.setAlign("top");
        child.addActionListener(new TableSortActionListener());

        // Set tool tip.
        String zToolTip = getTheme().getMessage(
                "table.viewActions.clearSort");
        child.setAlt(zToolTip);
        child.setToolTip(zToolTip);

        // Set tab index.
        if (table != null) {
            child.setTabIndex(table.getTabIndex());
        } else {
            log("getClearSortButton", "Tab index not set, Table is null");
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get filter label.
     *
     * @return The filter label.
     */
    public UIComponent getFilterLabel() {
        UIComponent facet = getFacet(FILTER_LABEL_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        Label child = new Label();
        child.setId(FILTER_LABEL_ID);
        child.setText(getTheme().getMessage("table.viewActions.filter"));
        child.setLabelLevel(2);

        Table tableAncestor = getTableAncestor();
        if (tableAncestor != null) {
            UIComponent tableFilterFacet = tableAncestor
                    .getFacet(Table.FILTER_FACET);
            if (tableFilterFacet != null) {
                child.setFor(tableFilterFacet.getClientId(getFacesContext()));
            }
        } else {
            log("getFilterLabel", "Labeled component not set, Table is null");
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get preferences panel toggle button.
     *
     * @return The preferences panel toggle button.
     */
    public UIComponent getPreferencesPanelToggleButton() {
        UIComponent facet = getFacet(PREFERENCES_PANEL_TOGGLE_BUTTON_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        IconHyperlink child = new IconHyperlink();
        child.setId(PREFERENCES_PANEL_TOGGLE_BUTTON_ID);
        child.setIcon(ThemeImages.TABLE_PREFERENCES_PANEL);
        child.setBorder(0);
        child.setAlign("top");

        // Set JS to display table preferences panel.
        Table tableAncestor = getTableAncestor();
        if (tableAncestor != null) {
            String buff = "document.getElementById('"
                    + tableAncestor.getClientId(getFacesContext())
                    + "').togglePreferencesPanel(); return false";
            child.setOnClick(buff);
            child.setTabIndex(tableAncestor.getTabIndex());
        } else {
            log("getPreferencesPanelToggleButton",
                    "Tab index & onClick not set, Table is null");
        }

        // Get tool tip.
        String zToolTip = getTheme()
                .getMessage("table.viewActions.preferences");
        child.setAlt(zToolTip);
        child.setToolTip(zToolTip);

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get sort panel toggle button.
     *
     * @return The sort panel toggle button.
     */
    public UIComponent getSortPanelToggleButton() {
        UIComponent facet = getFacet(SORT_PANEL_TOGGLE_BUTTON_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        IconHyperlink child = new IconHyperlink();
        child.setId(SORT_PANEL_TOGGLE_BUTTON_ID);
        child.setIcon(ThemeImages.TABLE_SORT_PANEL);
        child.setBorder(0);
        child.setAlign("top");

        // Set JS to display table preferences panel.
        Table tableAncestor = getTableAncestor();
        if (tableAncestor != null) {
            String buff = "document.getElementById('"
                    + tableAncestor.getClientId(getFacesContext())
                    + "').toggleSortPanel(); return false";
            child.setOnClick(buff);
            child.setTabIndex(tableAncestor.getTabIndex());
        } else {
            log("getSortPanelToggleButton",
                    "Tab index & onClick not set, Table is null");
        }

        // Set tool tip.
        Theme theme = getTheme();
        String zToolTip = theme.getMessage("table.viewActions.sort");
        child.setAlt(zToolTip);
        child.setToolTip(zToolTip);

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * If the rendered property is true, render the beginning of the current
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
        super.encodeBegin(context);
    }

    /**
     * ABBR gives an abbreviated version of the cell's content. This allows
     * visual browsers to use the short form if space is limited, and
     * non-visual browsers can give a cell's header information in an
     * abbreviated form before rendering each cell.
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
     * visual browsers to use the short form if space is limited, and
     * non-visual browsers can give a cell's header information in an
     * abbreviated form before rendering each cell.
     * @param newAbbr abbr
     */
    public void setAbbr(final String newAbbr) {
        this.abbr = newAbbr;
    }

    /**
     * Flag indicating this component should render actions at the bottom of
     * the table. The default renders action for the top of the table.
     * @return {@code boolean}
     */
    public boolean isActionsBottom() {
        if (this.actionsBottomSet) {
            return this.actionsBottom;
        }
        ValueExpression vb = getValueExpression("actionsBottom");
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
     * Flag indicating this component should render actions at the bottom of
     * the table. The default renders action for the top of the table.
     * @param newActionsBottom actionsBottom
     */
    public void setActionsBottom(final boolean newActionsBottom) {
        this.actionsBottom = newActionsBottom;
        this.actionsBottomSet = true;
    }

    /**
     * Use the {@code align} attribute to specify the horizontal alignment for
     * the content of each cell in the column. Valid values are {@code left},
     * {@code center}, {@code right}, {@code justify}, and {@code char}. The
     * default alignment is {@code left}. Setting the {@code align} attribute to
     * {@code char} causes the cell's contents to be aligned on the character
     * that you specify with the {@code char} attribute. For example, to align
     * cell contents on colons, set {@code align="char"} and {@code char=":"
     * }Some browsers do not support aligning on the character.
     *
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
     * Use the {@code align} attribute to specify the horizontal alignment for
     * the content of each cell in the column. Valid values are {@code left},
     * {@code center}, {@code right}, {@code justify}, and {@code char}. The
     * default alignment is {@code left}. Setting the {@code align} attribute to
     * {@code char} causes the cell's contents to be aligned on the character
     * that you specify with the {@code char} attribute. For example, to align
     * cell contents on colons, set {@code align="char"} and {@code char=":"
     * }Some browsers do not support aligning on the character.
     *
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
     * supporting browsers, since most fail to override it when overriding
     * other author-specified colors. Style sheets provide a safer, more
     * flexible method of specifying a table's background color. This
     * attribute is deprecated (in HTML 4.0) in favor of style sheets.
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
     * supporting browsers, since most fail to override it when overriding
     * other author-specified colors. Style sheets provide a safer, more
     * flexible method of specifying a table's background color. This
     * attribute is deprecated (in HTML 4.0) in favor of style sheets.
     * @param newBgColor bgColor
     */
    public void setBgColor(final String newBgColor) {
        this.bgColor = newBgColor;
    }

    /**
     * Use the {@code char }attribute to specify a character to use for
     * horizontal alignment in each cell in the row. You must also set the
     * {@code align} attribute to {@code char} to enable character alignment to
     * be used. The default value for the {@code char} attribute is the decimal
     * point of the current language, such as a period in English. The
     * {@code char} HTML property is not supported by all browsers.
     *
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
     * {@code align} attribute to {@code char} to enable character alignment to
     * be used. The default value for the {@code char} attribute is the decimal
     * point of the current language, such as a period in English. The
     * {@code char} HTML property is not supported by all browsers.
     * @param newCharFor charFor
     */
    public void setChar(final String newCharFor) {
        this.charFor = newCharFor;
    }

    /**
     * Use the {@code charOff }attribute to specify the offset of the first
     * occurrence of the alignment character that is specified with the
     * {@code char} attribute. The offset is the distance from the left cell
     * border, in locales that read from left to right. The {@code charOff}
     * attribute's value can be a number of pixels or a percentage of the cell's
     * width. For example, {@code charOff="50%"} centers the alignment character
     * horizontally in a cell. If {@code charOff="25%"}, the first instance of
     * the alignment character is placed at one fourth of the width of the cell.
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
     * {@code char} attribute. The offset is the distance from the left cell
     * border, in locales that read from left to right. The {@code charOff}
     * attribute's value can be a number of pixels or a percentage of the cell's
     * width. For example, {@code charOff="50%"} centers the alignment character
     * horizontally in a cell. If {@code charOff="25%"}, the first instance of
     * the alignment character is placed at one fourth of the width of the cell.
     *
     * @param newCharOff charOff
     */
    public void setCharOff(final String newCharOff) {
        this.charOff = newCharOff;
    }

    /**
     * The COLSPAN attribute of TD specifies the number of columns that are
     * spanned by the cell. The default value is 1. The special value 0
     * indicates that the cell spans all columns to the end of the table. The
     * value 0 is ignored by most browsers, so authors may wish to calculate
     * the exact number of rows or columns spanned and use that value.
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
     * value 0 is ignored by most browsers, so authors may wish to calculate
     * the exact number of rows or columns spanned and use that value.
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
     * The HEADERS attribute specifies the header cells that apply to the
     * TD. The value is a space-separated list of the header cells' ID
     * attribute values. The HEADERS attribute allows non-visual browsers to
     * render the header information for a given cell.
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
     *
     * @param newHeaders headers
     */
    public void setHeaders(final String newHeaders) {
        this.headers = newHeaders;
    }

    /**
     * The number of pixels for the cell's height. Styles should be used to
     * specify cell height when possible because the height attribute is
     * deprecated in HTML 4.0.
     *
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
     *
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
     *
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
     *
     * @param newNoWrap noWrap
     */
    public void setNoWrap(final boolean newNoWrap) {
        this.noWrap = newNoWrap;
        this.noWrapSet = true;
    }

    /**
     * Scripting code executed when a mouse click
     * occurs over this component.
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
     * Scripting code executed when a mouse click
     * occurs over this component.
     * @param newOnClick onClick
     */
    public void setOnClick(final String newOnClick) {
        this.onClick = newOnClick;
    }

    /**
     * Scripting code executed when a mouse double click
     * occurs over this component.
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
     * Scripting code executed when a mouse double click
     * occurs over this component.
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
     * Scripting code executed when the user releases a key while the
     * component has focus.
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
     * Scripting code executed when the user releases a key while the
     * component has focus.
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
     * Scripting code executed when the user moves the mouse pointer while
     * over the component.
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
     * Scripting code executed when the user moves the mouse pointer while
     * over the component.
     * @param newOnMouseMove onMouseMove
     */
    public void setOnMouseMove(final String newOnMouseMove) {
        this.onMouseMove = newOnMouseMove;
    }

    /**
     * Scripting code executed when a mouse out movement
     * occurs over this component.
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
     * Scripting code executed when a mouse out movement
     * occurs over this component.
     * @param newOnMouseOut onMouseOut
     */
    public void setOnMouseOut(final String newOnMouseOut) {
        this.onMouseOut = newOnMouseOut;
    }

    /**
     * Scripting code executed when the user moves the  mouse pointer into
     * the boundary of this component.
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
     * Scripting code executed when the user moves the  mouse pointer into
     * the boundary of this component.
     * @param newOnMouseOver onMouseOver
     */
    public void setOnMouseOver(final String newOnMouseOver) {
        this.onMouseOver = newOnMouseOver;
    }

    /**
     * Scripting code executed when the user releases a mouse button while
     * the mouse pointer is on the component.
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
     * Scripting code executed when the user releases a mouse button while
     * the mouse pointer is on the component.
     * @param newOnMouseUp onMouseUp
     */
    public void setOnMouseUp(final String newOnMouseUp) {
        this.onMouseUp = newOnMouseUp;
    }

    /**
     * The ROWSPAN attribute of TD specifies the number of rows that are
     * spanned by the cell. The default value is 1. The special value 0
     * indicates that the cell spans all rows to the end of the table. The
     * value 0 is ignored by most browsers, so authors may wish to calculate
     * the exact number of rows or columns spanned and use that value.
     * @return int
     */
    public int getRowSpan() {
        if (this.visible) {
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
     * The ROWSPAN attribute of TD specifies the number of rows that are
     * spanned by the cell. The default value is 1. The special value 0
     * indicates that the cell spans all rows to the end of the table. The
     * value 0 is ignored by most browsers, so authors may wish to calculate
     * the exact number of rows or columns spanned and use that value.
     * @param newRowSpan rowSpan
     */
    public void setRowSpan(final int newRowSpan) {
        this.rowSpan = newRowSpan;
        this.visible = true;
    }

    /**
     * Use the {@code scope} attribute to specify that the data cells of the
     * column are also acting as headers for rows or other columns of the table.
     * This attribute supports assistive technologies by enabling them to
     * determine the order in which to read the cells. Valid values include:
     * <ul>
     * <li>{@code row}, when the cells provide header information for the
     * row</li>
     * <li>{@code col}, when the cells provide header information for the
     * column</li>
     * <li>{@code rowgroup}, when the cells provide header information for the
     * row group</li>
     * <li>{@code colgroup}, when the cells provide header information for the
     * column group</li>
     * </ul>
     *
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
     * Use the {@code scope} attribute to specify that the data cells of the
     * column are also acting as headers for rows or other columns of the table.
     * This attribute supports assistive technologies by enabling them to
     * determine the order in which to read the cells. Valid values include:
     * <ul>
     * <li>{@code row}, when the cells provide header information for the
     * row</li>
     * <li>{@code col}, when the cells provide header information for the
     * column</li>
     * <li>{@code rowgroup}, when the cells provide header information for the
     * row group</li>
     * <li>{@code colgroup}, when the cells provide header information for the
     * column group</li>
     * </ul>
     *
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
     * Sets the value of the title attribute for the HTML element.
     * The specified text will display as a tool tip if the mouse cursor hovers
     * over the HTML element.
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
     * Sets the value of the title attribute for the HTML element.
     * The specified text will display as a tooltip if the mouse cursor hovers
     * over the HTML element.
     * @param newToolTip tool tip
     */
    public void setToolTip(final String newToolTip) {
        this.toolTip = newToolTip;
    }

    /**
     * Use the {@code valign} attribute to specify the vertical alignment for
     * the content of each cell in the column. Valid values are {@code top},
     * {@code middle}, {@code bottom}, and {@code baseline}. The default
     * vertical alignment is {@code middle}. Setting the {@code valign}
     * attribute to {@code baseline }causes the first line of each cell's
     * content to be aligned on the text baseline, the invisible line on which
     * text characters rest.
     *
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
     * Use the {@code valign} attribute to specify the vertical alignment for
     * the content of each cell in the column. Valid values are {@code top},
     * {@code middle}, {@code bottom}, and {@code baseline}. The default
     * vertical alignment is {@code middle}. Setting the {@code valign}
     * attribute to {@code baseline }causes the first line of each cell's
     * content to be aligned on the text baseline, the invisible line on which
     * text characters rest.
     *
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
     *
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
     * viewable by the user in the rendered HTML page. If set to false, the
     * HTML code for the component is present in the page, but the component
     * is hidden with style attributes. By default, visible is set to true, so
     * HTML for the component HTML is included and visible to the user. If the
     * component is not visible, it can still be processed on subsequent form
     * submissions because the HTML is present.
     * @param newVisible visible
     */
    public void setVisible(final boolean newVisible) {
        this.visible = newVisible;
        this.visibleSet = true;
    }

    /**
     * Use the {@code width} attribute to specify the width of the cells of the
     * column. The width can be specified as the number of pixels or the
     * percentage of the table width, and is especially useful for spacer
     * columns. This attribute is deprecated in HTML 4.0 in favor of style
     * sheets.
     *
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
     * Use the {@code width} attribute to specify the width of the cells of the
     * column. The width can be specified as the number of pixels or the
     * percentage of the table width, and is especially useful for spacer
     * columns. This attribute is deprecated in HTML 4.0 in favor of style
     * sheets.
     *
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
        this.actionsBottom = ((Boolean) values[2]);
        this.actionsBottomSet = ((Boolean) values[3]);
        this.align = (String) values[4];
        this.axis = (String) values[5];
        this.bgColor = (String) values[6];
        this.charFor = (String) values[7];
        this.charOff = (String) values[8];
        this.colSpan = ((Integer) values[9]);
        this.colSpanSet = ((Boolean) values[10]);
        this.extraHtml = (String) values[11];
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
        this.visible = ((Boolean) values[27]);
        this.scope = (String) values[28];
        this.style = (String) values[29];
        this.styleClass = (String) values[30];
        this.toolTip = (String) values[31];
        this.valign = (String) values[32];
        this.visible = ((Boolean) values[33]);
        this.visibleSet = ((Boolean) values[34]);
        this.width = (String) values[35];
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[36];
        values[0] = super.saveState(context);
        values[1] = this.abbr;
        if (this.actionsBottom) {
            values[2] = Boolean.TRUE;
        } else {
            values[2] = Boolean.FALSE;
        }
        if (this.actionsBottomSet) {
            values[3] = Boolean.TRUE;
        } else {
            values[3] = Boolean.FALSE;
        }
        values[4] = this.align;
        values[5] = this.axis;
        values[6] = this.bgColor;
        values[7] = this.charFor;
        values[8] = this.charOff;
        values[9] = this.colSpan;
        if (this.colSpanSet) {
            values[10] = Boolean.TRUE;
        } else {
            values[10] = Boolean.FALSE;
        }
        values[11] = this.extraHtml;
        values[12] = this.headers;
        values[13] = this.height;
        if (this.noWrap) {
            values[14] = Boolean.TRUE;
        } else {
            values[14] = Boolean.FALSE;
        }
        if (this.noWrapSet) {
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
        if (this.rowSpanSet) {
            values[27] = Boolean.TRUE;
        } else {
            values[27] = Boolean.FALSE;
        }
        values[28] = this.scope;
        values[29] = this.style;
        values[30] = this.styleClass;
        values[31] = this.toolTip;
        values[32] = this.valign;
        if (this.visible) {
            values[33] = Boolean.TRUE;
        } else {
            values[33] = Boolean.FALSE;
        }
        if (this.visibleSet) {
            values[34] = Boolean.TRUE;
        } else {
            values[34] = Boolean.FALSE;
        }
        values[35] = this.width;
        return values;
    }

    /**
     * Helper method to get JS for the de/select all buttons.
     *
     * @param script The JS to be prepended, if any.
     * @param checked true if components used for row selection should be
     * checked; otherwise, false.
     *
     * @return The JS for the de/select buttons.
     */
    private String getSelectJavascript(final String script,
            final boolean checked) {

        // Get JS to de/select all components in table.
        StringBuilder buff = new StringBuilder();

        // Developer may have added onClick Javascript for de/select all button.
        if (script != null) {
            buff.append(script).append(";");
        }

        // Append Javascript to de/select all select components.
        Table tableAncestor = getTableAncestor();
        if (tableAncestor != null) {
            buff.append("document.getElementById('")
                    .append(tableAncestor.getClientId(getFacesContext()))
                    .append("').selectAllRows(")
                    .append(checked).append("); return false");
        } else {
            log("getSelectJavascript",
                    "Cannot obtain select Javascript, Table is null");
        }
        return buff.toString();
    }

    /**
     * Helper method to get separator icons used for top and bottom actions,
     * filter, view actions, and paginate button.
     *
     * @param id The identifier for the component.
     * @param name The facet name used to override the component.
     *
     * @return The separator icon.
     */
    private UIComponent getSeparatorIcon(final String id, final String name) {
        UIComponent facet = getFacet(name);
        if (facet != null) {
            return facet;
        }

        // Get child.
        Icon child = ThemeUtilities.getIcon(getTheme(),
                TABLE_ACTIONS_SEPARATOR);
        child.setId(id);
        child.setBorder(0);
        child.setAlign("top");

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Helper method to get JS to submit the "go" button when the user
     * clicks enter in the page field.
     *
     * @return The JS used to submit the "go" button.
     */
    private String getPaginationJavascript() {
        ClientSniffer cs = ClientSniffer.getInstance(getFacesContext());

        // Get key code.
        String keyCode;
        if (cs.isNav()) {
            keyCode = "event.which";
        } else {
            keyCode = "event.keyCode";
        }

        // Append JS to capture the event.
        StringBuffer buff = new StringBuffer()
                .append("if (")
                .append(keyCode)
                .append("==13) {");

        // To prevent an auto-submit, Netscape 6.x and netscape 7.0 require
        // setting the cancelBubble property. However, Netscape 7.1,
        // Mozilla 1.x, IE 5.x for SunOS/Windows do not use this property.
        if (cs.isNav6() || cs.isNav70()) {
            buff.append("event.cancelBubble = true;");
        }

        // Append JS to submit the button.
        FacesContext context = getFacesContext();
        buff.append("var e=document.getElementById('")
                .append(getClientId(context))
                .append(UINamingContainer.getSeparatorChar(context))
                .append(TableActions.PAGINATION_SUBMIT_BUTTON_ID)
                .append("'); if (e != null) e.click(); return false}");
        return buff.toString();
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
     * Helper method to determine if table is empty.
     *
     * @return true if table contains no rows.
     */
    private boolean isEmptyTable() {
        int totalRows = table.getRowCount();
        return (totalRows == 0);
    }

    /**
     * Helper method to determine if all rows fit on a single page.
     * <p>
     * Note: Pagination controls are only hidden when all groups fit on a single
     * page.
     * </p>
     * @return true if all rows fit on a single page.
     */
    private boolean isSinglePage() {
        int totalRows = table.getRowCount();
        return (totalRows < table.getRows());
    }

    /**
     * Helper method to determine if table contains a single row.
     *
     * @return true if all rows fit on a single page.
     */
    private boolean isSingleRow() {
        int totalRows = table.getRowCount();
        return (totalRows == 1);
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
            LogUtil.fine(clazz, clazz.getName() + "." + method + ": "
                    + msg);
        }
    }
}
