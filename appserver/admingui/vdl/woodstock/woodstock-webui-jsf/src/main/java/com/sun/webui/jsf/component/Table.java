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
import com.sun.webui.jsf.model.Option;
import com.sun.webui.jsf.model.Separator;
import com.sun.webui.jsf.util.ComponentUtilities;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Component that represents a table.
 *
 * The table component provides a layout mechanism for displaying table actions.
 * UI guidelines describe specific behavior that can applied to the rows and
 * columns of data such as sorting, filtering, pagination, selection, and custom
 * user actions. In addition, UI guidelines also define sections of the table
 * that can be used for titles, row group headers, and placement of pre-defined
 * and user defined actions.
 * <p>
 * Note: Column headers and footers are rendered by TableRowGroupRenderer. Table
 * column footers are rendered by TableRenderer.
 * </p>
 * <pre>
 * java.util.logging.ConsoleHandler.level = FINE
 * com.sun.web.ui.component.Table.level = FINE
 * </pre>
 * <p>
 * See TLD docs for more information.
 * </p>
 */
@Component(type = "com.sun.webui.jsf.Table",
        family = "com.sun.webui.jsf.Table",
        displayName = "Table",
        tagName = "table",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_table")
public class Table extends UIComponentBase implements NamingContainer {

    /**
     * The facet name for the bottom actions area.
     */
    public static final String ACTIONS_BOTTOM_FACET = "actionsBottom";

    /**
     * The facet name for top actions area.
     */
    public static final String ACTIONS_TOP_FACET = "actionsTop";

    /**
     * The value for the custom filter option.
     */
    public static final String CUSTOM_FILTER = "_customFilter";

    /**
     * The value for the custom filter applied option.
     */
    public static final String CUSTOM_FILTER_APPLIED = "_customFilterApplied";

    /**
     * The id for the embedded panels bar.
     */
    public static final String EMBEDDED_PANELS_BAR_ID = "_embeddedPanelsBar";

    /**
     * The component id for embedded panels.
     */
    public static final String EMBEDDED_PANELS_ID = "_embeddedPanels";

    /**
     * The facet name for embedded panels.
     */
    public static final String EMBEDDED_PANELS_FACET = "embeddedPanels";

    /**
     * The facet name for the filter area.
     */
    public static final String FILTER_FACET = "filter";

    /**
     * The facet name for the filter panel.
     */
    public static final String FILTER_PANEL_FACET = "filterPanel";

    /**
     * The facet name for the footer area.
     */
    public static final String FOOTER_FACET = "footer";

    /**
     * The facet name for the preferences panel.
     */
    public static final String PREFERENCES_PANEL_FACET = "preferencesPanel";

    /**
     * The facet name for the sort panel.
     */
    public static final String SORT_PANEL_FACET = "sortPanel";

    /**
     * The id for the table.
     */
    public static final String TABLE_ID = "_table";

    /**
     * The id for the bottom actions bar.
     */
    public static final String TABLE_ACTIONS_BOTTOM_BAR_ID =
            "_tableActionsBottomBar";

    /**
     * The component id for bottom actions.
     */
    public static final String TABLE_ACTIONS_BOTTOM_ID = "_tableActionsBottom";

    /**
     * The facet name for bottom actions.
     */
    public static final String TABLE_ACTIONS_BOTTOM_FACET =
            "tableActionsBottom";

    /**
     * The id for the top actions bar.
     */
    public static final String TABLE_ACTIONS_TOP_BAR_ID = "_tableActionsTopBar";

    /**
     * The component id for top actions.
     */
    public static final String TABLE_ACTIONS_TOP_ID = "_tableActionsTop";

    /**
     * The facet name for top actions.
     */
    public static final String TABLE_ACTIONS_TOP_FACET = "tableActionsTop";

    /**
     * The id for the table footer.
     */
    public static final String TABLE_FOOTER_BAR_ID = "_tableFooterBar";

    /**
     * The component id for the table footer.
     */
    public static final String TABLE_FOOTER_ID = "_tableFooter";

    /**
     * The facet name for the table footer.
     */
    public static final String TABLE_FOOTER_FACET = "tableFooter";

    /**
     * The id for the title bar.
     */
    public static final String TITLE_BAR_ID = "_titleBar";

    /**
     * The facet name for the title area.
     */
    public static final String TITLE_FACET = "title";

    /**
     * The number of rows to be displayed per page for a paginated table.
     */
    private int rows = -1;

    /**
     * The max number of pages.
     */
    private int first = -1;

    /**
     * The number of rows.
     */
    private int rowCount = -1;

    /**
     * The max number of pages.
     */
    private int pageCount = -1;

    /**
     * The max number of columns.
     */
    private int columnCount = -1;

    /**
     * The number of column headers.
     */
    private int columnHeadersCount = -1;

    /**
     * The number of hidden selected rows.
     */
    private int hiddenSelectedRowsCount = -1;

    /**
     * The number of column footers.
     */
    private int tableColumnFootersCount = -1;

    /**
     * A List containing TableRowGroup children.
     */
    private List<TableRowGroup> tableRowGroupChildren;

    /**
     * The number of TableRowGroup children.
     */
    private int tableRowGroupCount = -1;

    /**
     * The deprecated ALIGN attribute suggests the horizontal alignment of the
     * table on visual browsers. Possible values are left, right, and center.
     * Browsers generally present left- or right-aligned tables as floating
     * tables, with the content following the TABLE flowing around it. To
     * prevent content from flowing around the table, use &lt;BR
     * CLEAR=all&gt; after the end of the TABLE.
     * <p>
     * Since many browsers do not support ALIGN=center with TABLE, authors may
     * wish to place the TABLE within a CENTER element.
     * </p><p>
     * Style sheets provide more flexibility in suggesting table alignment but
     * with less browser support than the ALIGN attribute.</p>
     */
    @Property(name = "align",
            displayName = "Table Alignment",
            category = "Appearance",
            isHidden = true,
            isAttribute = false,
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String align;

    /**
     * Flag indicating that the table title should be augmented with the range
     * of items currently displayed and the total number of items in the table.
     * For example, "(1 - 25 of 200)". If the table is not currently paginated,
     * the title is augmented with the number of displayed items. For example,
     * "(18)". When set to false, any values set for {@code itemsText} and
     * {@code filterText} are overridden.
     */
    @Property(name = "augmentTitle",
            displayName = "Show Augmented Title",
            category = "Appearance")
    private boolean augmentTitle;

    /**
     * augmentTitle set flag.
     */
    private boolean augmentTitleSet;

    /**
     * The deprecated BGCOLOR attribute suggests a background color for the
     * table. The combination of this attribute with &lt;FONT COLOR=...&gt; can
     * leave invisible or unreadable text on Netscape Navigator 2.x, which does
     * not support BGCOLOR on table elements. BGCOLOR is dangerous even on
     * supporting browsers, since most fail to override it when overriding other
     * author-specified colors. Style sheets provide a safer, more flexible
     * method of specifying a table's background color.
     */
    @Property(name = "bgColor",
            displayName = "Table Background Color",
            category = "Appearance",
            isHidden = true,
            isAttribute = false,
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String bgColor;

    /**
     * The BORDER attribute specifies the width in pixels of the border around a
     * table.
     */
    @Property(name = "border",
            displayName = "Border Width",
            category = "Appearance",
            isHidden = true,
            isAttribute = false,
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.LengthPropertyEditor")
            //CHECKSTYLE:ON
    private int border = Integer.MIN_VALUE;

    /**
     * borderSet flag.
     */
    private boolean borderSet;

    /**
     * The amount of white space that should be placed between the cell contents
     * and the cell borders, on all four sides of the cell. The default value is
     * 0, which causes a default amount of space to be used.
     * <p>
     * All browsers support specifying the number of pixels to use for cell
     * padding, so you should specify a number of pixels to achieve consistency
     * across platforms. Some browsers also support specifying the cell padding
     * as a percentage of the space available for padding, and the calculated
     * space is split evenly between the sides. Most browsers that do not
     * support percentages treat {@code cellpadding="20%"} as if it were
     * {@code cellpadding="20"}.</p>
     */
    @Property(name = "cellPadding",
            displayName = "Spacing Within Cells",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String cellPadding;

    /**
     * The amount of white space that should be placed between cells, and
     * between the edges of the table content area and the sides of the table.
     * The default value is 0, which causes a default amount of space to be
     * used.
     * <p>
     * All browsers support specifying the number of pixels to use for cell
     * spacing, so you should specify a number of pixels to achieve consistency
     * across platforms. Some browsers also support specifying the cell spacing
     * as a percentage of the space available for spacing, and the calculated
     * space is split evenly between the sides. Most browsers that do not
     * support percentages treat {@code cellspacing="20%"} as if it were
     * {@code cellspacing="20"}.</p>
     */
    @Property(name = "cellSpacing",
            displayName = "Spacing Between Cells",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String cellSpacing;

    /**
     * In the View-Changing Controls area of the Action Bar, display a button
     * that clears any sorting of the table. When the button is clicked, the
     * table items return to the order they were in when the page was initially
     * rendered.
     */
    @Property(name = "clearSortButton",
            displayName = "Show Clear Sort Button",
            category = "Appearance")
    private boolean clearSortButton;

    /**
     * clearSortButton set flag.
     */
    private boolean clearSortButtonSet;

    /**
     * In the Action Bar, display a deselect button for tables in which multiple
     * rows can be selected, to allow users to deselect all table rows that are
     * currently displayed. This button is used to deselect a column of
     * check boxes using the id that was given to the selectId attribute of the
     * {@code webuijsf:tableColumn} tag.
     */
    @Property(name = "deselectMultipleButton",
            displayName = "Show Deselect Multiple Button",
            category = "Appearance")
    private boolean deselectMultipleButton;

    /**
     * deselectMultipleButton set flag.
     */
    private boolean deselectMultipleButtonSet;

    /**
     * Scripting code that is executed when the user clicks the deselect
     * multiple button. You should use the JavaScript {@code setTimeout()}
     * function to invoke the script to ensure that check boxes are deselected
     * immediately, instead of waiting for the script to complete.
     */
    @Property(name = "deselectMultipleButtonOnClick",
            displayName = "Deselect Multiple Click Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String deselectMultipleButtonOnClick;

    /**
     * In the Action Bar, display a deselect button for tables in which only a
     * single table row can be selected at a time. This button is used to
     * deselect a column of radio buttons using the id that was given to the
     * selectId attribute of the {@code webuijsf:tableColumn} tag.
     */
    @Property(name = "deselectSingleButton",
            displayName = "Show Deselect Single Button",
            category = "Appearance")
    private boolean deselectSingleButton;

    /**
     * deselectSingleButton set flag.
     */
    private boolean deselectSingleButtonSet;

    /**
     * Scripting code that is executed when the user clicks the deselect single
     * button. You should use the JavaScript {@code setTimeout()} function to
     * invoke the script to ensure that the radio button is deselected
     * immediately, instead of waiting for the script to complete.
     */
    @Property(name = "deselectSingleButtonOnClick",
            displayName = "Deselect Single Click Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String deselectSingleButtonOnClick;

    /**
     * Extra HTML code to be appended to the {@code &lt;td&gt; }HTML element
     * that is rendered for the Action Bar (bottom). Use only code that is valid
     * in an HTML {@code &lt;td&gt;} element. The code you specify is inserted
     * in the HTML element, and is not checked for validity. For example, you
     * might set this attribute to {@code "style=`myActionBarStyle'"}.
     */
    @Property(name = "extraActionBottomHtml",
            displayName = "Extra Action (bottom) HTML",
            category = "Advanced",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String extraActionBottomHtml;

    /**
     * Extra HTML code to be appended to the {@code &lt;td&gt; }HTML element
     * that is rendered for the Action Bar (top). Use only code that is valid in
     * an HTML {@code &lt;td&gt;} element. The code you specify is inserted in
     * the HTML element, and is not checked for validity. For example, you might
     * set this attribute to {@code "style=`myActionBarStyle'"}.
     */
    @Property(name = "extraActionTopHtml",
            displayName = "Extra Action (top) HTML",
            category = "Advanced",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String extraActionTopHtml;

    /**
     * Extra HTML code to be appended to the {@code &lt;td&gt; }HTML element
     * that is rendered for the table footer. Use only code that is valid in an
     * HTML {@code &lt;td&gt;} element. The code you specify is inserted in the
     * HTML element, and is not checked for validity. For example, you might set
     * this attribute to {@code "nowrap=`nowrap'"}.
     */
    @Property(name = "extraFooterHtml",
            displayName = "Extra Footer HTML",
            category = "Advanced",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String extraFooterHtml;

    /**
     * Extra HTML code to be appended to the {@code &lt;td&gt; }HTML element
     * that is rendered for an embedded panel. Use only code that is valid in an
     * HTML {@code &lt;td&gt;} element. The code you specify is inserted in the
     * HTML element, and is not checked for validity.
     */
    @Property(name = "extraPanelHtml",
            displayName = "Extra Panel HTML",
            category = "Advanced",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String extraPanelHtml;

    /**
     * Extra HTML code to be appended to the {@code &lt;caption&gt;} HTML
     * element that is rendered for the table title. Use only code that is valid
     * in an HTML {@code &lt;caption&gt;} element. The code you specify is
     * inserted in the HTML element, and is not checked for validity. For
     * example, you might set this attribute to {@code "style=`myTitleStyle'"}.
     */
    @Property(name = "extraTitleHtml",
            displayName = "Extra Title HTML",
            category = "Advanced",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String extraTitleHtml;

    /**
     * The element id to be applied to the outermost HTML element that is
     * rendered for the dropDown component used to display filter options. The
     * id must be fully qualified. This id is required for JavaScript functions
     * to set the dropDown styles when the embedded filter panel is opened, and
     * to reset the default selected value when the panel is closed. Note that
     * if you use the {@code webuijsf:dropDown} tag as the only component in the
     * {@code filter} facet, the {@code filterId} is optional. If you use a
     * custom component, or use the {@code webuijsf:dropDown} as a child
     * component, you must specify a filterID.
     */
    @Property(name = "filterId",
            displayName = "Filter Component Id",
            category = "Appearance",
            isHidden = true,
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String filterId;

    /**
     * The element id used to set focus when the filter panel is open.
     */
    @Property(name = "filterPanelFocusId",
            displayName = "Filter Panel Focus ID",
            category = "Advanced",
            isHidden = true)
    private String filterPanelFocusId;

    /**
     * Text to be inserted into the table title bar when a filter is applied.
     * This text is expected to be the name of the filter that the user has
     * selected. The attribute value should be a JavaServer Faces EL expression
     * that resolves to a backing bean property whose value is set in your
     * filter code. The value of the filterText attribute is inserted into the
     * table title, as follows: Your Table's Title
     * <span style="font-style: italic;">filterText</span> Filter Applied.
     */
    @Property(name = "filterText",
            displayName = "Filter Text",
            category = "Appearance",
            isHidden = true,
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String filterText;

    /**
     * The text to be displayed in the table footer, which expands across the
     * width of the table.
     */
    @Property(name = "footerText",
            displayName = "Footer Text",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String footerText;

    /**
     * The BORDER attribute specifies the width in pixels of the border around a
     * table.
     */
    @Property(name = "frame",
            displayName = "Outer Border",
            category = "Appearance",
            isHidden = true,
            isAttribute = false,
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    private String frame;

    /**
     * Flag indicating that selected rows might be currently hidden from view.
     * UI guidelines recommend that rows that are not in view are deselected.
     * For example, when users select rows of the table and navigate to another
     * page, the selected rows should be deselected automatically. Or, when a
     * user applies a filter or sort that hides previously selected rows from
     * view, those selected rows should be deselected. By deselecting hidden
     * rows, you prevent the user from inadvertently invoking an action on rows
     * that are not displayed.
     * <p>
     * However, sometimes state must be maintained across table pages. If your
     * table must maintain state, you must set the hiddenSelectedRows attribute
     * to true. The attribute causes text to be displayed in the table title and
     * footer to indicate the number of selected rows that are currently hidden
     * from view. This title and footer text is also displayed with a count of 0
     * when there are no hidden selections, to make the user aware of the
     * possibility of hidden selections.
     * </p><p>
     * Note: When hiddenSelectedRows is false, the descending sort button for
     * the select column is disabled when the table is paginated. Disabling this
     * button prevents a sort from placing selected rows on a page other than
     * the current page.</p>
     */
    @Property(name = "hiddenSelectedRows",
            displayName = "Is Hidden Selected Rows",
            category = "Advanced")
    private boolean hiddenSelectedRows;

    /**
     * hiddenSelectedRows set flag.
     */
    private boolean hiddenSelectedRowsSet;

    /**
     * Flag indicating that this component should use a virtual form. A virtual
     * form is equivalent to enclosing the table component in its own HTML form
     * element, separate from other HTML elements on the same page. As an
     * example, consider the case where a required text field and table appear
     * on the same page. If the user clicks on a table sort button, while the
     * required text field has no value, the sort action is never invoked
     * because a value was required and validation failed. Placing the table in
     * a virtual form allows the table sort action to complete because
     * validation for the required text field is not processed. This is similar
     * to using the immediate property of a button, but allows table children to
     * be submitted so that selected checkbox values may be sorted, for example.
     */
    @Property(name = "internalVirtualForm",
            displayName = "Is Internal Virtual Form",
            category = "Advanced",
            isAttribute = false)
    private boolean internalVirtualForm;

    /**
     * internalVirtualForm set flag.
     */
    private boolean internalVirtualFormSet;

    /**
     * Text to add to the title of an unpaginated table. For example, if your
     * table title is "Critical" and there are 20 items in the table, the
     * default unpaginated table title would be Critical (20). If you specify
     * itemsText="alerts", the title would be Critical (20 alerts).
     */
    @Property(name = "itemsText",
            displayName = "Items Text",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String itemsText;

    /**
     * Renders the table in a style that makes the table look lighter weight,
     * generally by omitting the shading around the table and in the title bar.
     */
    @Property(name = "lite",
            displayName = "Light Weight Table",
            category = "Appearance")
    private boolean lite;

    /**
     * lite set flag.
     */
    private boolean liteSet;

    /**
     * Scripting code executed when a mouse click occurs over this component.
     */
    @Property(name = "onClick",
            displayName = "Click Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onClick;

    /**
     * Scripting code executed when a mouse double click occurs over this
     * component.
     */
    @Property(name = "onDblClick",
            displayName = "Double Click Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onDblClick;

    /**
     * Scripting code executed when the user presses down on a key while the
     * component has focus.
     */
    @Property(name = "onKeyDown",
            displayName = "Key Down Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onKeyDown;

    /**
     * Scripting code executed when the user presses and releases a key while
     * the component has focus.
     */
    @Property(name = "onKeyPress",
            displayName = "Key Press Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onKeyPress;

    /**
     * Scripting code executed when the user releases a key while the component
     * has focus.
     */
    @Property(name = "onKeyUp",
            displayName = "Key Up Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onKeyUp;

    /**
     * Scripting code executed when the user presses a mouse button while the
     * mouse pointer is on the component.
     */
    @Property(name = "onMouseDown",
            displayName = "Mouse Down Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onMouseDown;

    /**
     * Scripting code executed when the user moves the mouse pointer while over
     * the component.
     */
    @Property(name = "onMouseMove",
            displayName = "Mouse Move Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onMouseMove;

    /**
     * Scripting code executed when a mouse out movement occurs over this
     * component.
     */
    @Property(name = "onMouseOut",
            displayName = "Mouse Out Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onMouseOut;

    /**
     * Scripting code executed when the user moves the mouse pointer into the
     * boundary of this component.
     */
    @Property(name = "onMouseOver",
            displayName = "Mouse In Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onMouseOver;

    /**
     * Scripting code executed when the user releases a mouse button while the
     * mouse pointer is on the component.
     */
    @Property(name = "onMouseUp",
            displayName = "Mouse Up Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onMouseUp;

    /**
     * Show table paginate button to allow users to switch between viewing all
     * data on a single page (unpaginated) or to see data in multiple pages
     * (paginated).
     */
    @Property(name = "paginateButton",
            displayName = "Show Paginate Button",
            category = "Appearance")
    private boolean paginateButton;

    /**
     * paginateButton set flag.
     */
    private boolean paginateButtonSet;

    /**
     * Show the table pagination controls, which allow users to change which
     * page is displayed. The controls include an input field for specifying the
     * page number, a Go button to go to the specified page, and buttons for
     * going to the first, last, previous, and next page.
     */
    @Property(name = "paginationControls",
            displayName = "Show Pagination Controls",
            category = "Appearance")
    private boolean paginationControls;

    /**
     * paginationControls set flag.
     */
    private boolean paginationControlsSet;

    /**
     * The element id used to set focus when the preferences panel is open.
     */
    @Property(name = "preferencesPanelFocusId",
            displayName = "Preferences Panel Focus ID",
            category = "Advanced",
            isHidden = true)
    private String preferencesPanelFocusId;

    /**
     * The RULES attribute, poorly supported by browsers, specifies the borders
     * between table cells. Possible values are none for no inner borders,
     * groups for borders between row groups and column groups only, rows for
     * borders between rows only, cols for borders between columns only, and all
     * for borders between all cells. None is the default value if BORDER=0 is
     * used or if no BORDER attribute is given. All is the default value for any
     * other use of BORDER.
     */
    @Property(name = "rules",
            displayName = "Inner Borders",
            category = "Appearance",
            isHidden = true,
            isAttribute = false,
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String rules;

    /**
     * Show the button that is used for selecting multiple rows. The button is
     * displayed in the Action Bar (top), and allows users to select all rows
     * currently displayed. The button selects a column of check boxes using the
     * id specified in the selectId attribute of the
     * {@code webuijsf:tableColumn} tag.
     */
    @Property(name = "selectMultipleButton",
            displayName = "Show Select Multiple Button",
            category = "Appearance")
    private boolean selectMultipleButton;

    /**
     * selectMultipleButton set flag.
     */
    private boolean selectMultipleButtonSet;

    /**
     * Scripting code executed when the user clicks the mouse on the select
     * multiple button.
     */
    @Property(name = "selectMultipleButtonOnClick",
            displayName = "Select Multiple Click Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String selectMultipleButtonOnClick;

    /**
     * The element id used to set focus when the sort panel is open.
     */
    @Property(name = "sortPanelFocusId",
            displayName = "Sort Panel Focus ID",
            category = "Advanced",
            isHidden = true)
    private String sortPanelFocusId;

    /**
     * Show the button that is used to open and close the sort panel.
     */
    @Property(name = "sortPanelToggleButton",
            displayName = "Show Sort Panel Toggle Button",
            category = "Appearance")
    private boolean sortPanelToggleButton;

    /**
     * sortPanelToggleButton set flag.
     */
    private boolean sortPanelToggleButtonSet;

    /**
     * CSS style(s) to be applied to the outermost HTML element when this
     * component is rendered.
     */
    @Property(name = "style",
            displayName = "CSS Style(s)",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.jsfcl.std.css.CssStylePropertyEditor")
            //CHECKSTYLE:ON
    private String style;

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
    private String styleClass;

    /**
     * Text that describes this table's purpose and structure, for user agents
     * rendering to non-visual media such as speech and Braille.
     */
    @Property(name = "summary",
            displayName = "Purpose of Table",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String summary;

    /**
     * Position of this element in the tabbing order of the current document.
     * Tabbing order determines the sequence in which elements receive focus
     * when the tab key is pressed. The value must be an integer between 0 and
     * 32767.
     */
    @Property(name = "tabIndex",
            displayName = "Tab Index",
            category = "Accessibility",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    private int tabIndex = Integer.MIN_VALUE;

    /**
     * tabIndex set flag.
     */
    private boolean tabIndexSet;

    /**
     * The text displayed for the table title.
     */
    @Property(name = "title",
            displayName = "Table Title",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String title;

    /**
     * Sets the value of the title attribute for the HTML element. The specified
     * text will display as a tool tip if the mouse cursor hovers over the HTML
     * element.
     */
    @Property(name = "toolTip",
            displayName = "Tool Tip",
            category = "Behavior",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String toolTip;

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
     * Use the {@code width} attribute to specify the width of the table. The
     * width can be specified as the number of pixels or the percentage of the
     * page width, and is especially useful for spacer columns. This attribute
     * is deprecated in HTML 4.0 in favor of style sheets.
     */
    @Property(name = "width",
            displayName = "Table Width",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String width;

    /**
     * Default constructor.
     */
    public Table() {
        super();
        setRendererType("com.sun.webui.jsf.Table");
    }

    /**
     * This implementation returns {@code "com.sun.webui.jsf.Table"}.
     * @return String
     */
    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.Table";
    }

    /**
     * Clear cached properties.
     * Note: Properties may have been cached via the apply request values,
     * validate, and update phases and must be re-evaluated during the render
     * response phase. For example, the underlying DataProvider may have changed
     * and TableRenderer may need new calculations for the title and action bar.
     * <p>
     * Note: Properties of TableRowGroup and TableColumn children shall be
     * cleared as well.
     * </p>
     */
    public void clear() {
        rows = -1;
        first = -1;
        rowCount = -1;
        pageCount = -1;
        columnCount = -1;
        columnHeadersCount = -1;
        hiddenSelectedRowsCount = -1;
        tableColumnFootersCount = -1;
        tableRowGroupChildren = null;
        tableRowGroupCount = -1;

        // Clear properties of TableRowGroup children.
        Iterator kids = getTableRowGroupChildren();
        while (kids.hasNext()) {
            TableRowGroup kid = (TableRowGroup) kids.next();
            // Clear cached properties.
            kid.clear();
        }
    }

    /**
     * Get the number of column header bars for all TableRowGroup children.
     *
     * @return The number of column headers.
     */
    public int getColumnHeadersCount() {
        // Get column header count.
        if (columnHeadersCount == -1) {
            // Initialize min value.
            columnHeadersCount = 0;

            // Iterate over each TableRowGroup child to determine if each group
            // displays its own column header or if one column header is
            // dispalyed for all row groups.
            Iterator kids = getTableRowGroupChildren();
            while (kids.hasNext()) {
                TableRowGroup group = (TableRowGroup) kids.next();
                Iterator grandkids = group.getTableColumnChildren();
                while (grandkids.hasNext()) {
                    TableColumn col = (TableColumn) grandkids.next();
                    if (col.getHeaderText() != null) {
                        columnHeadersCount++;
                        // Break if at least one column header is found.
                        break;
                    }
                }
            }
        }
        return columnHeadersCount;
    }

    /**
     * Get the number of hidden selected rows for all TableRowGroup children.
     *
     * @return The number of hidden selected rows.
     */
    public int getHiddenSelectedRowsCount() {
        // Get hidden selected rows count.
        if (hiddenSelectedRowsCount == -1) {
            // Initialize min value.
            hiddenSelectedRowsCount = 0;
            Iterator kids = getTableRowGroupChildren();
            while (kids.hasNext()) {
                TableRowGroup group = (TableRowGroup) kids.next();
                hiddenSelectedRowsCount += group.getHiddenSelectedRowsCount();
            }
        }
        return hiddenSelectedRowsCount;
    }

    /**
     * Get the zero-relative row number of the first row to be displayed for a
     * paginated table for all TableRowGroup children.
     *
     * @return The first row to be displayed.
     */
    public int getFirst() {
        // Get first row.
        if (first == -1) {
            first = 0; // Initialize min value.
            Iterator kids = getTableRowGroupChildren();
            while (kids.hasNext()) {
                TableRowGroup group = (TableRowGroup) kids.next();
                first += group.getFirst();
            }
        }
        return first;
    }

    /**
     * Get the max number of pages for all TableRowGroup children.
     *
     * @return The max number of pages.
     */
    public int getPageCount() {
        // Get page count.
        if (pageCount == -1) {
            // Initialize min value.
            pageCount = 1;
            Iterator kids = getTableRowGroupChildren();
            while (kids.hasNext()) {
                TableRowGroup group = (TableRowGroup) kids.next();
                int pages = group.getPages();
                if (pageCount < pages) {
                    pageCount = pages;
                }
            }
        }
        return pageCount;
    }

    /**
     * Get the number of rows to be displayed per page for a paginated table for
     * all TableRowGroup children.
     *
     * @return The number of rows to be displayed per page for a paginated
     * table.
     */
    public int getRows() {
        // Get rows per page.
        if (rows == -1) {
            // Initialize min value.
            rows = 0;
            Iterator kids = getTableRowGroupChildren();
            while (kids.hasNext()) {
                TableRowGroup group = (TableRowGroup) kids.next();
                rows += group.getRows();
            }
        }
        return rows;
    }

    /**
     * Get the number of rows in the underlying TableDataProvider for all
     * TableRowGroup children.
     *
     * @return The number of rows.
     */
    public int getRowCount() {
        // Get row count.
        if (rowCount == -1) {
            // Initialize min value.
            rowCount = 0;
            Iterator kids = getTableRowGroupChildren();
            while (kids.hasNext()) {
                TableRowGroup group = (TableRowGroup) kids.next();
                rowCount += group.getRowCount();
            }
        }
        return rowCount;
    }

    /**
     * Get the max number of columns found for all TableRowGroup children.
     *
     * @return The max number of columns.
     */
    public int getColumnCount() {
        // Get column count.
        if (columnCount == -1) {
            // Initialize min value.
            columnCount = 1;
            Iterator kids = getTableRowGroupChildren();
            while (kids.hasNext()) {
                TableRowGroup group = (TableRowGroup) kids.next();
                int count = group.getColumnCount();
                if (columnCount < count) {
                    columnCount = count;
                }
            }
        }
        return columnCount;
    }

    /**
     * Get the number of table column footer bars for all TableRowGroup
     * children.
     *
     * @return The number of table column footers.
     */
    public int getTableColumnFootersCount() {
        // Get table column footer count.
        if (tableColumnFootersCount == -1) {
            // Initialize min value.
            tableColumnFootersCount = 0;

            // Iterate over each TableRowGroup child to determine if each group
            // displays its own table column footer or if one table column
            // footer is dispalyed for all row groups.
            Iterator kids = getTableRowGroupChildren();
            while (kids.hasNext()) {
                TableRowGroup group = (TableRowGroup) kids.next();
                Iterator grandkids = group.getTableColumnChildren();
                while (grandkids.hasNext()) {
                    TableColumn col = (TableColumn) grandkids.next();
                    if (col.isRendered() && col.getTableFooterText() != null) {
                        tableColumnFootersCount++;
                        // Break if at least one table column footer is shown.
                        break;
                    }
                }
            }
        }
        return tableColumnFootersCount;
    }

    /**
     * Get the first TableRowGroup child found for the specified component that
     * have a rendered property of true.
     *
     * @return The first TableRowGroup child found.
     */
    public TableRowGroup getTableRowGroupChild() {
        TableRowGroup group = null;
        Iterator kids = getTableRowGroupChildren();
        if (kids.hasNext()) {
            group = (TableRowGroup) kids.next();
        }
        return group;
    }

    /**
     * Get an Iterator over the TableRowGroup children found for this component.
     *
     * @return An Iterator over the TableRowGroup children.
     */
    public Iterator getTableRowGroupChildren() {
        // Get TableRowGroup children.
        if (tableRowGroupChildren == null) {
            tableRowGroupChildren = new ArrayList<TableRowGroup>();
            for (UIComponent kid : getChildren()) {
                if ((kid instanceof TableRowGroup)) {
                    tableRowGroupChildren.add((TableRowGroup) kid);
                }
            }
        }
        return tableRowGroupChildren.iterator();
    }

    /**
     * Get the number of child TableRowGroup components found for this component
     * that have a rendered property of true.
     *
     * @return The number of TableRowGroup children.
     */
    public int getTableRowGroupCount() {
        // Get TableRowGroup children count.
        if (tableRowGroupCount == -1) {
            // Initialize min value.
            tableRowGroupCount = 0;
            Iterator kids = getTableRowGroupChildren();
            while (kids.hasNext()) {
                UIComponent kid = (UIComponent) kids.next();
                if (kid.isRendered()) {
                    tableRowGroupCount++;
                }
            }
        }
        return tableRowGroupCount;
    }

    /**
     * Get bottom actions.
     *
     * @return The bottom actions.
     */
    public UIComponent getTableActionsBottom() {
        UIComponent facet = getFacet(TABLE_ACTIONS_BOTTOM_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        TableActions child = new TableActions();
        child.setId(TABLE_ACTIONS_BOTTOM_ID);
        child.setColSpan(getColumnCount());
        child.setExtraHtml(getExtraActionBottomHtml());
        child.setNoWrap(true);
        child.setActionsBottom(true);

        // We must determine if all TableRowGroup components are empty. Controls
        // are only hidden when all row groups are empty. Likewise, pagination
        // controls are only hidden when all groups fit on a single page.
        int totalRows = getRowCount();
        boolean emptyTable = (totalRows == 0);
        boolean singleRow = (totalRows == 1);
        boolean singlePage = (totalRows < getRows());

        // Get facets.
        UIComponent actions = getFacet(ACTIONS_BOTTOM_FACET);

        // Get flag indicating which facets to render.
        boolean renderActions = !emptyTable
                && !singleRow && actions != null
                && actions.isRendered();

        // Hide pagination controls when all rows fit on a page.
        boolean renderPaginationControls = !emptyTable
                && !singlePage && isPaginationControls();

        // Hide paginate button for a single row.
        boolean renderPaginateButton = !emptyTable
                && !singlePage && isPaginateButton();

        // Set rendered.
        if (!(renderActions
                || renderPaginationControls
                || renderPaginateButton)) {
            log("getTableActionsBottom",
                    "Action bar not rendered, nothing to display");
            child.setRendered(false);
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get top actions.
     *
     * @return The top actions.
     */
    public UIComponent getTableActionsTop() {
        UIComponent facet = getFacet(TABLE_ACTIONS_TOP_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        TableActions child = new TableActions();
        child.setId(TABLE_ACTIONS_TOP_ID);
        child.setColSpan(getColumnCount());
        child.setExtraHtml(getExtraActionTopHtml());
        child.setNoWrap(true);

        // We must determine if all TableRowGroup components are empty. Controls
        // are only hidden when all row groups are empty. Likewise, pagination
        // controls are only hidden when all groups fit on a single page.
        int totalRows = getRowCount();
        boolean emptyTable = (totalRows == 0);
        boolean singleRow = (totalRows == 1);
        boolean singlePage = (totalRows < getRows());

        // Get facets.
        UIComponent actions = getFacet(ACTIONS_TOP_FACET);
        UIComponent filter = getFacet(FILTER_FACET);
        UIComponent sort = getFacet(SORT_PANEL_FACET);
        UIComponent prefs = getFacet(PREFERENCES_PANEL_FACET);

        // Flags indicating which facets to render.
        boolean renderActions = actions != null && actions.isRendered();
        boolean renderFilter = filter != null && filter.isRendered();
        boolean renderSort = sort != null && sort.isRendered();
        boolean renderPrefs = prefs != null && prefs.isRendered();

        // Hide sorting and pagination controls for an empty table or when there
        // is only a single row.
        boolean renderSelectMultipleButton = !emptyTable
                && isSelectMultipleButton();
        boolean renderDeselectMultipleButton = !emptyTable
                && isDeselectMultipleButton();
        boolean renderDeselectSingleButton = !emptyTable
                && isDeselectSingleButton();
        boolean renderClearTableSortButton = !emptyTable
                && !singleRow && isClearSortButton();
        boolean renderTableSortPanelToggleButton = !emptyTable
                && !singleRow && (isSortPanelToggleButton() || renderSort);
        boolean renderPaginateButton = !emptyTable && !singlePage
                && isPaginateButton();

        // Return if nothing is rendered.
        if (!(renderActions || renderFilter || renderPrefs
                || renderSelectMultipleButton || renderDeselectMultipleButton
                || renderDeselectSingleButton || renderClearTableSortButton
                || renderTableSortPanelToggleButton || renderPaginateButton)) {
            log("getTableActionsTop",
                    "Action bar not rendered, nothing to display");
            child.setRendered(false);
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get the HTML element ID of the dropDown component used to display table
     * filter options.
     * <p>
     * Note: This is the fully qualified ID rendered in the outer tag enclosing
     * the HTML element. Required for JS functions to set the dropDown styles
     * when the embedded filter panel is opened and to reset the default
     * selected value when the panel is closed.
     * </p>
     *
     * @return The HTML element ID of the filter menu.
     */
    public String getFilterId() {
        String tableFilterId;
        if (this.filterId != null) {
            tableFilterId = this.filterId;
        } else {
            ValueExpression vb = getValueExpression("filterId");
            if (vb != null) {
                tableFilterId = (String) vb.getValue(getFacesContext()
                        .getELContext());
            } else {
                tableFilterId = null;
            }
        }
        if (tableFilterId == null) {
            log("getFilterId", "filterId is null, using facet client ID");
            UIComponent filter = getFacet(FILTER_FACET);
            if (filter != null) {
                tableFilterId = filter.getClientId(getFacesContext());
            } else {
                tableFilterId = null;
            }
        }
        return tableFilterId;
    }

    /**
     * Get the "custom filter" options used for a table filter menu.
     * <p>
     * Note: UI guidelines state that a "Custom Filter" option should be added
     * to the filter menu, used to open the table filter panel. Thus, if the
     * CUSTOM_FILTER option is selected, JS invoked via the onChange
     * event will open the table filter panel.
     * </p><p>
     * UI guidelines also state that a "Custom Filter Applied" option should be
     * added to the filter menu, indicating that a custom filter has been
     * applied. In this scenario, set the selected property of the filter menu
     * as CUSTOM_FILTER_APPLIED. This selection should persist until another
     * menu option has been selected.
     * </p>
     *
     * @param options An array of options to append to -- may be null.
     * @param customFilterApplied Flag indicating custom filter is applied.
     * @return A new array containing appended "custom filter" options.
     */
    public static Option[] getFilterOptions(final Option[] options,
            final boolean customFilterApplied) {

        FacesContext context = FacesContext.getCurrentInstance();
        Theme theme = ThemeUtilities.getTheme(context);
        ArrayList<Option> newOptions = new ArrayList<Option>();

        // Get old options.
        if (options != null) {
            newOptions.addAll(Arrays.asList(options));
        }

        // Add options separator.
        newOptions.add(new Separator());

        // Add custom filter applied option.
        if (customFilterApplied) {
            Option option = new Option(CUSTOM_FILTER_APPLIED,
                    theme.getMessage("table.viewActions.customFilterApplied"));
            option.setDisabled(true);
            newOptions.add(option);
        }

        // Add custom filter option.
        newOptions.add(new Option(CUSTOM_FILTER,
                theme.getMessage("table.viewActions.customFilter")));

        // Return options.
        Option[] result = new Option[newOptions.size()];
        return (Option[]) newOptions.toArray(result);
    }

    /**
     * Get table footer.
     *
     * @return The table footer.
     */
    public UIComponent getTableFooter() {
        UIComponent facet = getFacet(TABLE_FOOTER_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        TableFooter child = new TableFooter();
        child.setId(TABLE_FOOTER_ID);
        child.setColSpan(getColumnCount());
        child.setExtraHtml(getExtraFooterHtml());
        child.setTableFooter(true);

        // Set rendered.
        if (!(facet != null && facet.isRendered()
                || getFooterText() != null
                || isHiddenSelectedRows())) {
            // Note: Footer may be initialized to force rendering. This allows
            // developers to omit the footer text property for select columns.
            log("getTableFooter",
                    "Table footer not rendered, nothing to display");
            child.setRendered(false);
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get embedded panels.
     *
     * @return The embedded panels.
     */
    public UIComponent getEmbeddedPanels() {
        UIComponent facet = getFacet(EMBEDDED_PANELS_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        TablePanels child = new TablePanels();
        child.setId(EMBEDDED_PANELS_ID);
        child.setColSpan(getColumnCount());
        child.setExtraHtml(getExtraPanelHtml());
        child.setNoWrap(true);

        // Get facets.
        UIComponent sort = getFacet(SORT_PANEL_FACET);
        UIComponent filter = getFacet(FILTER_PANEL_FACET);
        UIComponent prefs = getFacet(PREFERENCES_PANEL_FACET);

        // Set flags indicating which facets to render.
        boolean renderFilter = filter != null && filter.isRendered();
        boolean renderPrefs = prefs != null && prefs.isRendered();
        boolean renderSort = sort != null && sort.isRendered();

        // Set type of panel to render.
        child.setFilterPanel(renderFilter);
        child.setPreferencesPanel(renderPrefs);

        // Set rendered.
        if (!(renderFilter
                || renderSort
                || renderPrefs
                || isSortPanelToggleButton())) {
            log("getEmbeddedPanels",
                    "Embedded panels not rendered, nothing to display");
            child.setRendered(false);
        }

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
        // Clear cached properties.
        clear();

        // Initialize the internal virtual form used by this component.
        if (isInternalVirtualForm()) {
            // Get Form component.
            Form form = (Form) ComponentUtilities.getForm(getFacesContext(),
                    this);
            if (form != null) {
                // Create VirtualFormDescriptor object.
                String id = getClientId(context) + "_virtualForm";
                Form.VirtualFormDescriptor descriptor
                        = new Form.VirtualFormDescriptor(id);
                String wildSuffix = String.valueOf(UINamingContainer
                        .getSeparatorChar(context))
                        + String.valueOf(Form.ID_WILD_CHAR);
                descriptor.setParticipatingIds(new String[]{getId()
                        + wildSuffix});
                descriptor.setSubmittingIds(new String[]{getId()
                        + wildSuffix});

                // Add virtual form.
                form.addInternalVirtualForm(descriptor);
            } else {
                log("encodeBegin",
                        "Internal virtual form not set, form ancestor is null");
            }
        }
        super.encodeBegin(context);
    }

    /**
     * The component identifier for this component. This value must be unique
     * within the closest parent component that is a naming container.
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

    /**
     * The deprecated ALIGN attribute suggests the horizontal alignment of the
     * table on visual browsers. Possible values are left, right, and center.
     * Browsers generally present left- or right-aligned tables as floating
     * tables, with the content following the TABLE flowing around it. To
     * prevent content from flowing around the table, use &lt;BR
     * CLEAR=all&gt; after the end of the TABLE.
     * <p>
     * Since many browsers do not support ALIGN=center with TABLE, authors may
     * wish to place the TABLE within a CENTER element.
     * </p><p>
     * Style sheets provide more flexibility in suggesting table alignment but
     * with less browser support than the ALIGN attribute.</p>
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
     * The deprecated ALIGN attribute suggests the horizontal alignment of the
     * table on visual browsers. Possible values are left, right, and center.
     * Browsers generally present left- or right-aligned tables as floating
     * tables, with the content following the TABLE flowing around it. To
     * prevent content from flowing around the table, use &lt;BR
     * CLEAR=all&gt; after the end of the TABLE.
     * <p>
     * Since many browsers do not support ALIGN=center with TABLE, authors may
     * wish to place the TABLE within a CENTER element.
     * </p><p>
     * Style sheets provide more flexibility in suggesting table alignment but
     * with less browser support than the ALIGN attribute.</p>
     * @param newAlign align
     */
    public void setAlign(final String newAlign) {
        this.align = newAlign;
    }

    /**
     * Flag indicating that the table title should be augmented with the range
     * of items currently displayed and the total number of items in the table.
     * For example, "(1 - 25 of 200)". If the table is not currently paginated,
     * the title is augmented with the number of displayed items. For example,
     * "(18)". When set to false, any values set for {@code itemsText} and
     * {@code filterText} are overridden.
     * @return {@code boolean}
     */
    public boolean isAugmentTitle() {
        if (this.augmentTitleSet) {
            return this.augmentTitle;
        }
        ValueExpression vb = getValueExpression("augmentTitle");
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
     * Flag indicating that the table title should be augmented with the range
     * of items currently displayed and the total number of items in the table.
     * For example, "(1 - 25 of 200)". If the table is not currently paginated,
     * the title is augmented with the number of displayed items. For example,
     * "(18)". When set to false, any values set for {@code itemsText} and
     * {@code filterText} are overridden.
     * @param newAugmentTitle augmentTitle
     */
    public void setAugmentTitle(final boolean newAugmentTitle) {
        this.augmentTitle = newAugmentTitle;
        this.augmentTitleSet = true;
    }

    /**
     * The deprecated BGCOLOR attribute suggests a background color for the
     * table. The combination of this attribute with &lt;FONT COLOR=...&gt; can
     * leave invisible or unreadable text on Netscape Navigator 2.x, which does
     * not support BGCOLOR on table elements. BGCOLOR is dangerous even on
     * supporting browsers, since most fail to override it when overriding other
     * author-specified colors. Style sheets provide a safer, more flexible
     * method of specifying a table's background color.
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
     * The deprecated BGCOLOR attribute suggests a background color for the
     * table. The combination of this attribute with &lt;FONT COLOR=...&gt; can
     * leave invisible or unreadable text on Netscape Navigator 2.x, which does
     * not support BGCOLOR on table elements. BGCOLOR is dangerous even on
     * supporting browsers, since most fail to override it when overriding other
     * author-specified colors. Style sheets provide a safer, more flexible
     * method of specifying a table's background color.
     * @param newBgColor bgColor
     */
    public void setBgColor(final String newBgColor) {
        this.bgColor = newBgColor;
    }

    /**
     * The BORDER attribute specifies the width in pixels of the border around a
     * table.
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
     * The BORDER attribute specifies the width in pixels of the border around a
     * table.
     * @param newBorder border
     */
    public void setBorder(final int newBorder) {
        this.border = newBorder;
        this.borderSet = true;
    }

    /**
     * The amount of white space that should be placed between the cell contents
     * and the cell borders, on all four sides of the cell. The default value is
     * 0, which causes a default amount of space to be used.
     * <p>
     * All browsers support specifying the number of pixels to use for cell
     * padding, so you should specify a number of pixels to achieve consistency
     * across platforms. Some browsers also support specifying the cell padding
     * as a percentage of the space available for padding, and the calculated
     * space is split evenly between the sides. Most browsers that do not
     * support percentages treat {@code cellpadding="20%"} as if it were
     * {@code cellpadding="20"}.</p>
     * @return String
     */
    public String getCellPadding() {
        if (this.cellPadding != null) {
            return this.cellPadding;
        }
        ValueExpression vb = getValueExpression("cellPadding");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The amount of white space that should be placed between the cell contents
     * and the cell borders, on all four sides of the cell. The default value is
     * 0, which causes a default amount of space to be used.
     * <p>
     * All browsers support specifying the number of pixels to use for cell
     * padding, so you should specify a number of pixels to achieve consistency
     * across platforms. Some browsers also support specifying the cell padding
     * as a percentage of the space available for padding, and the calculated
     * space is split evenly between the sides. Most browsers that do not
     * support percentages treat {@code cellpadding="20%"} as if it were
     * {@code cellpadding="20"}.</p>
     * @param newCellPadding cellPadding
     */
    public void setCellPadding(final String newCellPadding) {
        this.cellPadding = newCellPadding;
    }

    /**
     * The amount of white space that should be placed between cells, and
     * between the edges of the table content area and the sides of the table.
     * The default value is 0, which causes a default amount of space to be
     * used.
     * <p>
     * All browsers support specifying the number of pixels to use for cell
     * spacing, so you should specify a number of pixels to achieve consistency
     * across platforms. Some browsers also support specifying the cell spacing
     * as a percentage of the space available for spacing, and the calculated
     * space is split evenly between the sides. Most browsers that do not
     * support percentages treat {@code cellspacing="20%"} as if it were
     * {@code cellspacing="20"}.</p>
     *
     * @return String
     */
    public String getCellSpacing() {
        if (this.cellSpacing != null) {
            return this.cellSpacing;
        }
        ValueExpression vb = getValueExpression("cellSpacing");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The amount of white space that should be placed between cells, and
     * between the edges of the table content area and the sides of the table.
     * The default value is 0, which causes a default amount of space to be
     * used.
     * <p>
     * All browsers support specifying the number of pixels to use for cell
     * spacing, so you should specify a number of pixels to achieve consistency
     * across platforms. Some browsers also support specifying the cell spacing
     * as a percentage of the space available for spacing, and the calculated
     * space is split evenly between the sides. Most browsers that do not
     * support percentages treat {@code cellspacing="20%"} as if it were
     * {@code cellspacing="20"}.</p>
     *
     * @param newCellSpacing cellSpacing
     */
    public void setCellSpacing(final String newCellSpacing) {
        this.cellSpacing = newCellSpacing;
    }

    /**
     * In the View-Changing Controls area of the Action Bar, display a button
     * that clears any sorting of the table. When the button is clicked, the
     * table items return to the order they were in when the page was initially
     * rendered.
     * @return {@code boolean}
     */
    public boolean isClearSortButton() {
        if (this.clearSortButtonSet) {
            return this.clearSortButton;
        }
        ValueExpression vb = getValueExpression("clearSortButton");
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
     * In the View-Changing Controls area of the Action Bar, display a button
     * that clears any sorting of the table. When the button is clicked, the
     * table items return to the order they were in when the page was initially
     * rendered.
     * @param newClearSortButton clearSortButton
     */
    public void setClearSortButton(final boolean newClearSortButton) {
        this.clearSortButton = newClearSortButton;
        this.clearSortButtonSet = true;
    }

    /**
     * In the Action Bar, display a deselect button for tables in which multiple
     * rows can be selected, to allow users to deselect all table rows that are
     * currently displayed. This button is used to deselect a column of
     * check boxes using the id that was given to the selectId attribute of the
     * {@code webuijsf:tableColumn} tag.
     * @return {@code boolean}
     */
    public boolean isDeselectMultipleButton() {
        if (this.deselectMultipleButtonSet) {
            return this.deselectMultipleButton;
        }
        ValueExpression vb = getValueExpression("deselectMultipleButton");
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
     * In the Action Bar, display a deselect button for tables in which multiple
     * rows can be selected, to allow users to deselect all table rows that are
     * currently displayed. This button is used to deselect a column of
     * check boxes using the id that was given to the selectId attribute of the
     * {@code webuijsf:tableColumn} tag.
     * @param newDeselectMultipleButton deselectMultipleButton
     */
    public void setDeselectMultipleButton(
            final boolean newDeselectMultipleButton) {

        this.deselectMultipleButton = newDeselectMultipleButton;
        this.deselectMultipleButtonSet = true;
    }

    /**
     * Scripting code that is executed when the user clicks the deselect
     * multiple button. You should use the JavaScript {@code setTimeout()}
     * function to invoke the script to ensure that check boxes are deselected
     * immediately, instead of waiting for the script to complete.
     * @return String
     */
    public String getDeselectMultipleButtonOnClick() {
        if (this.deselectMultipleButtonOnClick != null) {
            return this.deselectMultipleButtonOnClick;
        }
        ValueExpression vb =
                getValueExpression("deselectMultipleButtonOnClick");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code that is executed when the user clicks the deselect
     * multiple button. You should use the JavaScript {@code setTimeout()}
     * function to invoke the script to ensure that check boxes are deselected
     * immediately, instead of waiting for the script to complete.
     * @param newDeselectMultipleButtonOnClick deselectMultipleButtonOnClick
     */
    public void setDeselectMultipleButtonOnClick(
            final String newDeselectMultipleButtonOnClick) {

        this.deselectMultipleButtonOnClick = newDeselectMultipleButtonOnClick;
    }

    /**
     * In the Action Bar, display a deselect button for tables in which only a
     * single table row can be selected at a time. This button is used to
     * deselect a column of radio buttons using the id that was given to the
     * selectId attribute of the {@code webuijsf:tableColumn} tag.
     * @return {@code boolean}
     */
    public boolean isDeselectSingleButton() {
        if (this.deselectSingleButtonSet) {
            return this.deselectSingleButton;
        }
        ValueExpression vb = getValueExpression("deselectSingleButton");
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
     * In the Action Bar, display a deselect button for tables in which only a
     * single table row can be selected at a time. This button is used to
     * deselect a column of radio buttons using the id that was given to the
     * selectId attribute of the {@code webuijsf:tableColumn} tag.
     * @param newDeselectSingleButton deselectSingleButton
     */
    public void setDeselectSingleButton(final boolean newDeselectSingleButton) {
        this.deselectSingleButton = newDeselectSingleButton;
        this.deselectSingleButtonSet = true;
    }

    /**
     * Scripting code that is executed when the user clicks the deselect single
     * button. You should use the JavaScript {@code setTimeout()} function to
     * invoke the script to ensure that the radio button is deselected
     * immediately, instead of waiting for the script to complete.
     * @return String
     */
    public String getDeselectSingleButtonOnClick() {
        if (this.deselectSingleButtonOnClick != null) {
            return this.deselectSingleButtonOnClick;
        }
        ValueExpression vb = getValueExpression("deselectSingleButtonOnClick");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code that is executed when the user clicks the deselect single
     * button. You should use the JavaScript {@code setTimeout()} function to
     * invoke the script to ensure that the radio button is deselected
     * immediately, instead of waiting for the script to complete.
     * @param newDeselectSingleButtonOnClick deselectSingleButtonOnClick
     */
    public void setDeselectSingleButtonOnClick(
            final String newDeselectSingleButtonOnClick) {

        this.deselectSingleButtonOnClick = newDeselectSingleButtonOnClick;
    }

    /**
     * Extra HTML code to be appended to the {@code &lt;td&gt; }HTML element
     * that is rendered for the Action Bar (bottom). Use only code that is valid
     * in an HTML {@code &lt;td&gt;} element. The code you specify is inserted
     * in the HTML element, and is not checked for validity. For example, you
     * might set this attribute to {@code "style=`myActionBarStyle'"}.
     * @return String
     */
    public String getExtraActionBottomHtml() {
        if (this.extraActionBottomHtml != null) {
            return this.extraActionBottomHtml;
        }
        ValueExpression vb = getValueExpression("extraActionBottomHtml");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Extra HTML code to be appended to the {@code &lt;td&gt; }HTML element
     * that is rendered for the Action Bar (bottom). Use only code that is valid
     * in an HTML {@code &lt;td&gt;} element. The code you specify is inserted
     * in the HTML element, and is not checked for validity. For example, you
     * might set this attribute to {@code "style=`myActionBarStyle'"}.
     * @param newExtraActionBottomHtml extraActionBottomHtml
     */
    public void setExtraActionBottomHtml(
            final String newExtraActionBottomHtml) {

        this.extraActionBottomHtml = newExtraActionBottomHtml;
    }

    /**
     * Extra HTML code to be appended to the {@code &lt;td&gt; }HTML element
     * that is rendered for the Action Bar (top). Use only code that is valid in
     * an HTML {@code &lt;td&gt;} element. The code you specify is inserted in
     * the HTML element, and is not checked for validity. For example, you might
     * set this attribute to {@code "style=`myActionBarStyle'"}.
     * @return String
     */
    public String getExtraActionTopHtml() {
        if (this.extraActionTopHtml != null) {
            return this.extraActionTopHtml;
        }
        ValueExpression vb = getValueExpression("extraActionTopHtml");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Extra HTML code to be appended to the {@code &lt;td&gt; }HTML element
     * that is rendered for the Action Bar (top). Use only code that is valid in
     * an HTML {@code &lt;td&gt;} element. The code you specify is inserted in
     * the HTML element, and is not checked for validity. For example, you might
     * set this attribute to {@code "style=`myActionBarStyle'"}.
     * @param newExtraActionTopHtml extraActionTopHtml
     */
    public void setExtraActionTopHtml(final String newExtraActionTopHtml) {
        this.extraActionTopHtml = newExtraActionTopHtml;
    }

    /**
     * Extra HTML code to be appended to the {@code &lt;td&gt; }HTML element
     * that is rendered for the table footer. Use only code that is valid in an
     * HTML {@code &lt;td&gt;} element. The code you specify is inserted in the
     * HTML element, and is not checked for validity. For example, you might set
     * this attribute to {@code "nowrap=`nowrap'"}.
     * @return String
     */
    public String getExtraFooterHtml() {
        if (this.extraFooterHtml != null) {
            return this.extraFooterHtml;
        }
        ValueExpression vb = getValueExpression("extraFooterHtml");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Extra HTML code to be appended to the {@code &lt;td&gt; }HTML element
     * that is rendered for the table footer. Use only code that is valid in an
     * HTML {@code &lt;td&gt;} element. The code you specify is inserted in the
     * HTML element, and is not checked for validity. For example, you might set
     * this attribute to {@code "nowrap=`nowrap'"}.
     * @param newExtraFooterHtml extraFooterHtml
     */
    public void setExtraFooterHtml(final String newExtraFooterHtml) {
        this.extraFooterHtml = newExtraFooterHtml;
    }

    /**
     * Extra HTML code to be appended to the {@code &lt;td&gt; }HTML element
     * that is rendered for an embedded panel. Use only code that is valid in an
     * HTML {@code &lt;td&gt;} element. The code you specify is inserted in the
     * HTML element, and is not checked for validity.
     * @return String
     */
    public String getExtraPanelHtml() {
        if (this.extraPanelHtml != null) {
            return this.extraPanelHtml;
        }
        ValueExpression vb = getValueExpression("extraPanelHtml");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Extra HTML code to be appended to the {@code &lt;td&gt; }HTML element
     * that is rendered for an embedded panel. Use only code that is valid in an
     * HTML {@code &lt;td&gt;} element. The code you specify is inserted in the
     * HTML element, and is not checked for validity.
     * @param newExtraPanelHtml extraPanelHtml
     */
    public void setExtraPanelHtml(final String newExtraPanelHtml) {
        this.extraPanelHtml = newExtraPanelHtml;
    }

    /**
     * Extra HTML code to be appended to the {@code &lt;caption&gt;} HTML
     * element that is rendered for the table title. Use only code that is valid
     * in an HTML {@code &lt;caption&gt;} element. The code you specify is
     * inserted in the HTML element, and is not checked for validity. For
     * example, you might set this attribute to {@code "style=`myTitleStyle'"}.
     * @return String
     */
    public String getExtraTitleHtml() {
        if (this.extraTitleHtml != null) {
            return this.extraTitleHtml;
        }
        ValueExpression vb = getValueExpression("extraTitleHtml");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Extra HTML code to be appended to the {@code &lt;caption&gt;} HTML
     * element that is rendered for the table title. Use only code that is valid
     * in an HTML {@code &lt;caption&gt;} element. The code you specify is
     * inserted in the HTML element, and is not checked for validity. For
     * example, you might set this attribute to {@code "style=`myTitleStyle'"}.
     * @param newExtraTitleHtml extraTitleHtml
     */
    public void setExtraTitleHtml(final String newExtraTitleHtml) {
        this.extraTitleHtml = newExtraTitleHtml;
    }

    /**
     * The element id to be applied to the outermost HTML element that is
     * rendered for the dropDown component used to display filter options. The
     * id must be fully qualified. This id is required for JavaScript functions
     * to set the dropDown styles when the embedded filter panel is opened, and
     * to reset the default selected value when the panel is closed. Note that
     * if you use the {@code webuijsf:dropDown} tag as the only component in the
     * {@code filter} facet, the {@code filterId} is optional. If you use a
     * custom component, or use the {@code webuijsf:dropDown} as a child
     * component, you must specify a filterID.
     * @param newFilterId filterId
     */
    public void setFilterId(final String newFilterId) {
        this.filterId = newFilterId;
    }

    /**
     * The element id used to set focus when the filter panel is open.
     * @return String
     */
    public String getFilterPanelFocusId() {
        if (this.filterPanelFocusId != null) {
            return this.filterPanelFocusId;
        }
        ValueExpression vb = getValueExpression("filterPanelFocusId");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The element id used to set focus when the filter panel is open.
     * @param newFilterPanelFocusId filterPanelFocusId
     */
    public void setFilterPanelFocusId(final String newFilterPanelFocusId) {
        this.filterPanelFocusId = newFilterPanelFocusId;
    }

    /**
     * Text to be inserted into the table title bar when a filter is applied.
     * This text is expected to be the name of the filter that the user has
     * selected. The attribute value should be a JavaServer Faces EL expression
     * that resolves to a backing bean property whose value is set in your
     * filter code. The value of the filterText attribute is inserted into the
     * table title, as follows: Your Table's Title
     * <i>filterText</i> Filter Applied.
     * @return String
     */
    public String getFilterText() {
        if (this.filterText != null) {
            return this.filterText;
        }
        ValueExpression vb = getValueExpression("filterText");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Text to be inserted into the table title bar when a filter is applied.
     * This text is expected to be the name of the filter that the user has
     * selected. The attribute value should be a JavaServer Faces EL expression
     * that resolves to a backing bean property whose value is set in your
     * filter code. The value of the filterText attribute is inserted into the
     * table title, as follows: Your Table's Title
     * <i>filterText</i> Filter Applied.
     * @param newFilterText filterText
     */
    public void setFilterText(final String newFilterText) {
        this.filterText = newFilterText;
    }

    /**
     * The text to be displayed in the table footer, which expands across the
     * width of the table.
     * @return String
     */
    public String getFooterText() {
        if (this.footerText != null) {
            return this.footerText;
        }
        ValueExpression vb = getValueExpression("footerText");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The text to be displayed in the table footer, which expands across the
     * width of the table.
     * @param newFooterText footerText
     */
    public void setFooterText(final String newFooterText) {
        this.footerText = newFooterText;
    }

    /**
     * The BORDER attribute specifies the width in pixels of the border around a
     * table.
     * @return String
     */
    public String getFrame() {
        if (this.frame != null) {
            return this.frame;
        }
        ValueExpression vb = getValueExpression("frame");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The BORDER attribute specifies the width in pixels of the border around a
     * table.
     * @param newFrame frame
     */
    public void setFrame(final String newFrame) {
        this.frame = newFrame;
    }

    /**
     * Flag indicating that selected rows might be currently hidden from view.
     * UI guidelines recommend that rows that are not in view are deselected.
     * For example, when users select rows of the table and navigate to another
     * page, the selected rows should be deselected automatically. Or, when a
     * user applies a filter or sort that hides previously selected rows from
     * view, those selected rows should be deselected. By deselecting hidden
     * rows, you prevent the user from inadvertently invoking an action on rows
     * that are not displayed.
     * <p>
     * However, sometimes state must be maintained across table pages. If your
     * table must maintain state, you must set the hiddenSelectedRows attribute
     * to true. The attribute causes text to be displayed in the table title and
     * footer to indicate the number of selected rows that are currently hidden
     * from view. This title and footer text is also displayed with a count of 0
     * when there are no hidden selections, to make the user aware of the
     * possibility of hidden selections.
     * </p><p>
     * Note: When hiddenSelectedRows is false, the descending sort button for
     * the select column is disabled when the table is paginated. Disabling this
     * button prevents a sort from placing selected rows on a page other than
     * the current page.</p>
     * @return {@code boolean}
     */
    public boolean isHiddenSelectedRows() {
        if (this.hiddenSelectedRowsSet) {
            return this.hiddenSelectedRows;
        }
        ValueExpression vb = getValueExpression("hiddenSelectedRows");
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
     * Flag indicating that selected rows might be currently hidden from view.
     * UI guidelines recommend that rows that are not in view are deselected.
     * For example, when users select rows of the table and navigate to another
     * page, the selected rows should be deselected automatically. Or, when a
     * user applies a filter or sort that hides previously selected rows from
     * view, those selected rows should be deselected. By deselecting hidden
     * rows, you prevent the user from inadvertently invoking an action on rows
     * that are not displayed.
     * <p>
     * However, sometimes state must be maintained across table pages. If your
     * table must maintain state, you must set the hiddenSelectedRows attribute
     * to true. The attribute causes text to be displayed in the table title and
     * footer to indicate the number of selected rows that are currently hidden
     * from view. This title and footer text is also displayed with a count of 0
     * when there are no hidden selections, to make the user aware of the
     * possibility of hidden selections.
     * </p><p>
     * Note: When hiddenSelectedRows is false, the descending sort button for
     * the select column is disabled when the table is paginated. Disabling this
     * button prevents a sort from placing selected rows on a page other than
     * the current page.</p>
     * @param newHiddenSelectedRows hiddenSelectedRows
     */
    public void setHiddenSelectedRows(final boolean newHiddenSelectedRows) {
        this.hiddenSelectedRows = newHiddenSelectedRows;
        this.hiddenSelectedRowsSet = true;
    }

    /**
     * Flag indicating that this component should use a virtual form. A virtual
     * form is equivalent to enclosing the table component in its own HTML form
     * element, separate from other HTML elements on the same page. As an
     * example, consider the case where a required text field and table appear
     * on the same page. If the user clicks on a table sort button, while the
     * required text field has no value, the sort action is never invoked
     * because a value was required and validation failed. Placing the table in
     * a virtual form allows the table sort action to complete because
     * validation for the required text field is not processed. This is similar
     * to using the immediate property of a button, but allows table children to
     * be submitted so that selected checkbox values may be sorted, for example.
     * @return {@code boolean}
     */
    public boolean isInternalVirtualForm() {
        if (this.internalVirtualFormSet) {
            return this.internalVirtualForm;
        }
        ValueExpression vb = getValueExpression("internalVirtualForm");
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
     * Flag indicating that this component should use a virtual form. A virtual
     * form is equivalent to enclosing the table component in its own HTML form
     * element, separate from other HTML elements on the same page. As an
     * example, consider the case where a required text field and table appear
     * on the same page. If the user clicks on a table sort button, while the
     * required text field has no value, the sort action is never invoked
     * because a value was required and validation failed. Placing the table in
     * a virtual form allows the table sort action to complete because
     * validation for the required text field is not processed. This is similar
     * to using the immediate property of a button, but allows table children to
     * be submitted so that selected checkbox values may be sorted, for example.
     * @param newInternalVirtualForm internalVirtualForm
     */
    public void setInternalVirtualForm(final boolean newInternalVirtualForm) {
        this.internalVirtualForm = newInternalVirtualForm;
        this.internalVirtualFormSet = true;
    }

    /**
     * Text to add to the title of an unpaginated table. For example, if your
     * table title is "Critical" and there are 20 items in the table, the
     * default unpaginated table title would be Critical (20). If you specify
     * itemsText="alerts", the title would be Critical (20 alerts).
     * @return String
     */
    public String getItemsText() {
        if (this.itemsText != null) {
            return this.itemsText;
        }
        ValueExpression vb = getValueExpression("itemsText");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Text to add to the title of an unpaginated table. For example, if your
     * table title is "Critical" and there are 20 items in the table, the
     * default unpaginated table title would be Critical (20). If you specify
     * itemsText="alerts", the title would be Critical (20 alerts).
     * @param newItemsText itemsText
     */
    public void setItemsText(final String newItemsText) {
        this.itemsText = newItemsText;
    }

    /**
     * Renders the table in a style that makes the table look lighter weight,
     * generally by omitting the shading around the table and in the title bar.
     * @return {@code boolean}
     */
    public boolean isLite() {
        if (this.liteSet) {
            return this.lite;
        }
        ValueExpression vb = getValueExpression("lite");
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
     * Renders the table in a style that makes the table look lighter weight,
     * generally by omitting the shading around the table and in the title bar.
     * @param newLite lite
     */
    public void setLite(final boolean newLite) {
        this.lite = newLite;
        this.liteSet = true;
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
     * Show table paginate button to allow users to switch between viewing all
     * data on a single page (unpaginated) or to see data in multiple pages
     * (paginated).
     * @return {@code boolean}
     */
    public boolean isPaginateButton() {
        if (this.paginateButtonSet) {
            return this.paginateButton;
        }
        ValueExpression vb = getValueExpression("paginateButton");
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
     * Show table paginate button to allow users to switch between viewing all
     * data on a single page (unpaginated) or to see data in multiple pages
     * (paginated).
     * @param newPaginateButton paginateButton
     */
    public void setPaginateButton(final boolean newPaginateButton) {
        this.paginateButton = newPaginateButton;
        this.paginateButtonSet = true;
    }

    /**
     * Show the table pagination controls, which allow users to change which
     * page is displayed. The controls include an input field for specifying the
     * page number, a Go button to go to the specified page, and buttons for
     * going to the first, last, previous, and next page.
     * @return {@code boolean}
     */
    public boolean isPaginationControls() {
        if (this.paginationControlsSet) {
            return this.paginationControls;
        }
        ValueExpression vb = getValueExpression("paginationControls");
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
     * Show the table pagination controls, which allow users to change which
     * page is displayed. The controls include an input field for specifying the
     * page number, a Go button to go to the specified page, and buttons for
     * going to the first, last, previous, and next page.
     * @param newPaginationControls paginationControls
     */
    public void setPaginationControls(final boolean newPaginationControls) {
        this.paginationControls = newPaginationControls;
        this.paginationControlsSet = true;
    }

    /**
     * The element id used to set focus when the preferences panel is open.
     * @return String
     */
    public String getPreferencesPanelFocusId() {
        if (this.preferencesPanelFocusId != null) {
            return this.preferencesPanelFocusId;
        }
        ValueExpression vb = getValueExpression("preferencesPanelFocusId");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The element id used to set focus when the preferences panel is open.
     * @param newPreferencesPanelFocusId preferencesPanelFocusId
     */
    public void setPreferencesPanelFocusId(
            final String newPreferencesPanelFocusId) {

        this.preferencesPanelFocusId = newPreferencesPanelFocusId;
    }

    /**
     * The RULES attribute, poorly supported by browsers, specifies the borders
     * between table cells. Possible values are none for no inner borders,
     * groups for borders between row groups and column groups only, rows for
     * borders between rows only, cols for borders between columns only, and all
     * for borders between all cells. None is the default value if BORDER=0 is
     * used or if no BORDER attribute is given. All is the default value for any
     * other use of BORDER.
     * @return String
     */
    public String getRules() {
        if (this.rules != null) {
            return this.rules;
        }
        ValueExpression vb = getValueExpression("rules");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The RULES attribute, poorly supported by browsers, specifies the borders
     * between table cells. Possible values are none for no inner borders,
     * groups for borders between row groups and column groups only, rows for
     * borders between rows only, cols for borders between columns only, and all
     * for borders between all cells. None is the default value if BORDER=0 is
     * used or if no BORDER attribute is given. All is the default value for any
     * other use of BORDER.
     * @param newRules rules
     */
    public void setRules(final String newRules) {
        this.rules = newRules;
    }

    /**
     * Show the button that is used for selecting multiple rows. The button is
     * displayed in the Action Bar (top), and allows users to select all rows
     * currently displayed. The button selects a column of check boxes using the
     * id specified in the selectId attribute of the
     * {@code webuijsf:tableColumn} tag.
     * @return {@code boolean}
     */
    public boolean isSelectMultipleButton() {
        if (this.selectMultipleButtonSet) {
            return this.selectMultipleButton;
        }
        ValueExpression vb = getValueExpression("selectMultipleButton");
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
     * Show the button that is used for selecting multiple rows. The button is
     * displayed in the Action Bar (top), and allows users to select all rows
     * currently displayed. The button selects a column of check boxes using the
     * id specified in the selectId attribute of the
     * {@code webuijsf:tableColumn} tag.
     * @param newSelectMultipleButton selectMultipleButton
     */
    public void setSelectMultipleButton(
            final boolean newSelectMultipleButton) {

        this.selectMultipleButton = newSelectMultipleButton;
        this.selectMultipleButtonSet = true;
    }

    /**
     * Scripting code executed when the user clicks the mouse on the select
     * multiple button.
     * @return String
     */
    public String getSelectMultipleButtonOnClick() {
        if (this.selectMultipleButtonOnClick != null) {
            return this.selectMultipleButtonOnClick;
        }
        ValueExpression vb = getValueExpression("selectMultipleButtonOnClick");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when the user clicks the mouse on the select
     * multiple button.
     * @param newSelectMultipleButtonOnClick selectMultipleButtonOnClick
     */
    public void setSelectMultipleButtonOnClick(
            final String newSelectMultipleButtonOnClick) {

        this.selectMultipleButtonOnClick = newSelectMultipleButtonOnClick;
    }

    /**
     * The element id used to set focus when the sort panel is open.
     * @return String
     */
    public String getSortPanelFocusId() {
        if (this.sortPanelFocusId != null) {
            return this.sortPanelFocusId;
        }
        ValueExpression vb = getValueExpression("sortPanelFocusId");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The element id used to set focus when the sort panel is open.
     * @param newSortPanelFocusId sortPanelFocusId
     */
    public void setSortPanelFocusId(final String newSortPanelFocusId) {
        this.sortPanelFocusId = newSortPanelFocusId;
    }

    /**
     * Show the button that is used to open and close the sort panel.
     * @return {@code boolean}
     */
    public boolean isSortPanelToggleButton() {
        if (this.sortPanelToggleButtonSet) {
            return this.sortPanelToggleButton;
        }
        ValueExpression vb = getValueExpression("sortPanelToggleButton");
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
     * Show the button that is used to open and close the sort panel.
     * @param newSortPanelToggleButton sortPanelToggleButton
     */
    public void setSortPanelToggleButton(
            final boolean newSortPanelToggleButton) {

        this.sortPanelToggleButton = newSortPanelToggleButton;
        this.sortPanelToggleButtonSet = true;
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
     * Text that describes this table's purpose and structure, for user agents
     * rendering to non-visual media such as speech and Braille.
     * @return String
     */
    public String getSummary() {
        if (this.summary != null) {
            return this.summary;
        }
        ValueExpression vb = getValueExpression("summary");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Text that describes this table's purpose and structure, for user agents
     * rendering to non-visual media such as speech and Braille.
     * @param newSummary summary
     */
    public void setSummary(final String newSummary) {
        this.summary = newSummary;
    }

    /**
     * Position of this element in the tabbing order of the current document.
     * Tabbing order determines the sequence in which elements receive focus
     * when the tab key is pressed. The value must be an integer between 0 and
     * 32767.
     * @return int
     */
    public int getTabIndex() {
        if (this.tabIndexSet) {
            return this.tabIndex;
        }
        ValueExpression vb = getValueExpression("tabIndex");
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
     * Position of this element in the tabbing order of the current document.
     * Tabbing order determines the sequence in which elements receive focus
     * when the tab key is pressed. The value must be an integer between 0 and
     * 32767.
     * @param newTabIndex tabIndex
     */
    public void setTabIndex(final int newTabIndex) {
        this.tabIndex = newTabIndex;
        this.tabIndexSet = true;
    }

    /**
     * The text displayed for the table title.
     * @return String
     */
    public String getTitle() {
        if (this.title != null) {
            return this.title;
        }
        ValueExpression vb = getValueExpression("title");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The text displayed for the table title.
     * @param newTitle title
     */
    public void setTitle(final String newTitle) {
        this.title = newTitle;
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
     * Use the {@code width} attribute to specify the width of the table. The
     * width can be specified as the number of pixels or the percentage of the
     * page width, and is especially useful for spacer columns. This attribute
     * is deprecated in HTML 4.0 in favor of style sheets.
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
     * Use the {@code width} attribute to specify the width of the table. The
     * width can be specified as the number of pixels or the percentage of the
     * page width, and is especially useful for spacer columns. This attribute
     * is deprecated in HTML 4.0 in favor of style sheets.
     * @param newWidth width
     */
    public void setWidth(final String newWidth) {
        this.width = newWidth;
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
        this.augmentTitle = ((Boolean) values[2]);
        this.augmentTitleSet = ((Boolean) values[3]);
        this.bgColor = (String) values[4];
        this.border = ((Integer) values[5]);
        this.borderSet = ((Boolean) values[6]);
        this.cellPadding = (String) values[7];
        this.cellSpacing = (String) values[8];
        this.clearSortButton = ((Boolean) values[9]);
        this.clearSortButtonSet = ((Boolean) values[10]);
        this.deselectMultipleButton = ((Boolean) values[11]);
        this.deselectMultipleButtonSet = ((Boolean) values[12]);
        this.deselectMultipleButtonOnClick = (String) values[13];
        this.deselectSingleButton = ((Boolean) values[14]);
        this.deselectSingleButtonSet = ((Boolean) values[15]);
        this.deselectSingleButtonOnClick = (String) values[16];
        this.extraActionBottomHtml = (String) values[17];
        this.extraActionTopHtml = (String) values[18];
        this.extraFooterHtml = (String) values[19];
        this.extraPanelHtml = (String) values[20];
        this.extraTitleHtml = (String) values[21];
        this.filterId = (String) values[22];
        this.filterPanelFocusId = (String) values[23];
        this.filterText = (String) values[24];
        this.footerText = (String) values[25];
        this.frame = (String) values[26];
        this.hiddenSelectedRows = ((Boolean) values[27]);
        this.hiddenSelectedRowsSet = ((Boolean) values[28]);
        this.internalVirtualForm = ((Boolean) values[29]);
        this.internalVirtualFormSet = ((Boolean) values[30]);
        this.itemsText = (String) values[31];
        this.lite = ((Boolean) values[32]);
        this.liteSet = ((Boolean) values[33]);
        this.onClick = (String) values[34];
        this.onDblClick = (String) values[35];
        this.onKeyDown = (String) values[36];
        this.onKeyPress = (String) values[37];
        this.onKeyUp = (String) values[38];
        this.onMouseDown = (String) values[39];
        this.onMouseMove = (String) values[40];
        this.onMouseOut = (String) values[41];
        this.onMouseOver = (String) values[42];
        this.onMouseUp = (String) values[43];
        this.paginateButton = ((Boolean) values[44]);
        this.paginateButtonSet = ((Boolean) values[45]);
        this.paginationControls = ((Boolean) values[46]);
        this.paginationControlsSet = ((Boolean) values[47]);
        this.preferencesPanelFocusId = (String) values[48];
        this.rules = (String) values[49];
        this.selectMultipleButton = ((Boolean) values[50]);
        this.selectMultipleButtonSet = ((Boolean) values[51]);
        this.selectMultipleButtonOnClick = (String) values[52];
        this.sortPanelFocusId = (String) values[53];
        this.sortPanelToggleButton = ((Boolean) values[54]);
        this.sortPanelToggleButtonSet = ((Boolean) values[55]);
        this.style = (String) values[56];
        this.styleClass = (String) values[57];
        this.summary = (String) values[58];
        this.tabIndex = ((Integer) values[59]);
        this.tabIndexSet = ((Boolean) values[60]);
        this.title = (String) values[61];
        this.toolTip = (String) values[62];
        this.visible = ((Boolean) values[63]);
        this.visibleSet = ((Boolean) values[64]);
        this.width = (String) values[65];
    }

    /**
     * This implementation saves the state of all properties.
     * @param context faces context
     * @return Object
     */
    @Override
    @SuppressWarnings({"checkstyle:magicnumber", "checkstyle:methodlength"})
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[66];
        values[0] = super.saveState(context);
        values[1] = this.align;
        if (this.augmentTitle) {
            values[2] = Boolean.TRUE;
        } else {
            values[2] = Boolean.FALSE;
        }
        if (this.augmentTitleSet) {
            values[3] = Boolean.TRUE;
        } else {
            values[3] = Boolean.FALSE;
        }
        values[4] = this.bgColor;
        values[5] = this.border;
        if (this.borderSet) {
            values[6] = Boolean.TRUE;
        } else {
            values[6] = Boolean.FALSE;
        }
        values[7] = this.cellPadding;
        values[8] = this.cellSpacing;
        if (this.clearSortButton) {
            values[9] = Boolean.TRUE;
        } else {
            values[9] = Boolean.FALSE;
        }
        if (this.clearSortButtonSet) {
            values[10] = Boolean.TRUE;
        } else {
            values[10] = Boolean.FALSE;
        }
        if (this.deselectMultipleButton) {
            values[11] = Boolean.TRUE;
        } else {
            values[11] = Boolean.FALSE;
        }
        if (this.deselectMultipleButtonSet) {
            values[12] = Boolean.TRUE;
        } else {
            values[12] = Boolean.FALSE;
        }
        values[13] = this.deselectMultipleButtonOnClick;
        if (this.deselectSingleButton) {
            values[14] = Boolean.TRUE;
        } else {
            values[14] = Boolean.FALSE;
        }
        if (this.deselectSingleButtonSet) {
            values[15] = Boolean.TRUE;
        } else {
            values[15] = Boolean.FALSE;
        }
        values[16] = this.deselectSingleButtonOnClick;
        values[17] = this.extraActionBottomHtml;
        values[18] = this.extraActionTopHtml;
        values[19] = this.extraFooterHtml;
        values[20] = this.extraPanelHtml;
        values[21] = this.extraTitleHtml;
        values[22] = this.filterId;
        values[23] = this.filterPanelFocusId;
        values[24] = this.filterText;
        values[25] = this.footerText;
        values[26] = this.frame;
        if (this.hiddenSelectedRows) {
            values[27] = Boolean.TRUE;
        } else {
            values[27] = Boolean.FALSE;
        }
        if (this.hiddenSelectedRowsSet) {
            values[28] = Boolean.TRUE;
        } else {
            values[28] = Boolean.FALSE;
        }
        if (this.internalVirtualForm) {
            values[29] = Boolean.TRUE;
        } else {
            values[29] = Boolean.FALSE;
        }
        if (this.internalVirtualFormSet) {
            values[30] = Boolean.TRUE;
        } else {
            values[30] = Boolean.FALSE;
        }
        values[31] = this.itemsText;
        if (this.lite) {
            values[32] = Boolean.TRUE;
        } else {
            values[32] = Boolean.FALSE;
        }
        if (this.liteSet) {
            values[33] = Boolean.TRUE;
        } else {
            values[33] = Boolean.FALSE;
        }
        values[34] = this.onClick;
        values[35] = this.onDblClick;
        values[36] = this.onKeyDown;
        values[37] = this.onKeyPress;
        values[38] = this.onKeyUp;
        values[39] = this.onMouseDown;
        values[40] = this.onMouseMove;
        values[41] = this.onMouseOut;
        values[42] = this.onMouseOver;
        values[43] = this.onMouseUp;
        if (this.paginateButton) {
            values[44] = Boolean.TRUE;
        } else {
            values[44] = Boolean.FALSE;
        }
        if (this.paginateButtonSet) {
            values[45] = Boolean.TRUE;
        } else {
            values[45] = Boolean.FALSE;
        }
        if (this.paginationControls) {
            values[46] = Boolean.TRUE;
        } else {
            values[46] = Boolean.FALSE;
        }
        if (this.paginationControlsSet) {
            values[47] = Boolean.TRUE;
        } else {
            values[47] = Boolean.FALSE;
        }
        values[48] = this.preferencesPanelFocusId;
        values[49] = this.rules;
        if (this.selectMultipleButton) {
            values[50] = Boolean.TRUE;
        } else {
            values[50] = Boolean.FALSE;
        }
        if (this.selectMultipleButtonSet) {
            values[51] = Boolean.TRUE;
        } else {
            values[51] = Boolean.FALSE;
        }
        values[52] = this.selectMultipleButtonOnClick;
        values[53] = this.sortPanelFocusId;
        if (this.sortPanelToggleButton) {
            values[54] = Boolean.TRUE;
        } else {
            values[54] = Boolean.FALSE;
        }
        if (this.sortPanelToggleButtonSet) {
            values[55] = Boolean.TRUE;
        } else {
            values[55] = Boolean.FALSE;
        }
        values[56] = this.style;
        values[57] = this.styleClass;
        values[58] = this.summary;
        values[59] = this.tabIndex;
        if (this.tabIndexSet) {
            values[60] = Boolean.TRUE;
        } else {
            values[60] = Boolean.FALSE;
        }
        values[61] = this.title;
        values[62] = this.toolTip;
        if (this.visible) {
            values[63] = Boolean.TRUE;
        } else {
            values[63] = Boolean.FALSE;
        }
        if (this.visibleSet) {
            values[64] = Boolean.TRUE;
        } else {
            values[64] = Boolean.FALSE;
        }
        values[65] = this.width;
        return values;
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
