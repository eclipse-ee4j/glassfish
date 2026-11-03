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

import com.sun.data.provider.FieldKey;
import com.sun.data.provider.FilterCriteria;
import com.sun.data.provider.RowKey;
import com.sun.data.provider.SortCriteria;
import com.sun.data.provider.TableDataFilter;
import com.sun.data.provider.TableDataProvider;
import com.sun.data.provider.TableDataSorter;
import com.sun.data.provider.impl.BasicTableDataFilter;
import com.sun.data.provider.impl.BasicTableDataSorter;
import com.sun.data.provider.impl.ObjectArrayDataProvider;
import com.sun.data.provider.impl.ObjectListDataProvider;
import com.sun.data.provider.impl.TableRowDataProvider;
import com.sun.faces.annotation.Component;
import com.sun.faces.annotation.Property;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.ConversionUtilities;
import com.sun.webui.jsf.util.LogUtil;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.el.ValueExpression;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.EditableValueHolder;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.FacesEvent;
import jakarta.faces.event.FacesListener;
import jakarta.faces.event.PhaseId;

import java.beans.Beans;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Component that represents a group of table rows.
 * <p>
 * The TableRowGroup component provides a layout mechanism for displaying rows
 * of data. UI guidelines describe specific behavior that can applied to the
 * rows and columns of data such as sorting, filtering, pagination, selection,
 * and custom user actions. In addition, UI guidelines also define sections of
 * the table that can be used for titles, row group headers, and placement of
 * pre-defined and user defined actions.
 * </p><p>
 * The TableRowGroup component supports a data binding to a collection of data
 * objects represented by a TableDataProvider instance, which is the current
 * value of this component itself. During iterative processing over the rows of
 * data in the data provider, the TableDataProvider for the current row is
 * exposed as a request attribute under the key specified by the var property.
 * </p><p>
 * Only children of type TableColumn should be processed by renderers associated
 * with this component.
 * </p><p>
 * Note: Column headers and footers are rendered by TableRowGroupRenderer. Table
 * column footers are rendered by TableRenderer.
 * </p>
 * <p>
 * <pre>
 * java.util.logging.ConsoleHandler.level = FINE
 * com.sun.web.ui.component.TableRowGroup.level = FINE
 * </pre>
 * </p>
 * <p>
 * See TLD docs for more information.
 * </p>
 */
@Component(type = "com.sun.webui.jsf.TableRowGroup",
        family = "com.sun.webui.jsf.TableRowGroup",
        displayName = "Row Group",
        tagName = "tableRowGroup",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_row_group")
public class TableRowGroup extends UIComponentBase implements NamingContainer {

    /**
     * The id for the column footer bar.
     */
    public static final String COLUMN_FOOTER_BAR_ID = "_columnFooterBar";

    /**
     * The id for the column header bar.
     */
    public static final String COLUMN_HEADER_BAR_ID = "_columnHeaderBar";

    /**
     * The component id for the empty data column.
     */
    public static final String EMPTY_DATA_COLUMN_ID = "_emptyDataColumn";

    /**
     * The facet name for the empty data column.
     */
    public static final String EMPTY_DATA_COLUMN_FACET = "emptyDataColumn";

    /**
     * The component id for the empty data text.
     */
    public static final String EMPTY_DATA_TEXT_ID = "_emptyDataText";

    /**
     * The facet name for the empty data text.
     */
    public static final String EMPTY_DATA_TEXT_FACET = "emptyDataText";

    /**
     * The facet name for the group footer area.
     */
    public static final String FOOTER_FACET = "footer";

    /**
     * The id for the group footer bar.
     */
    public static final String GROUP_FOOTER_BAR_ID = "_groupFooterBar";

    /**
     * The component id for the group footer.
     */
    public static final String GROUP_FOOTER_ID = "_groupFooter";

    /**
     * The facet name for the group footer.
     */
    public static final String GROUP_FOOTER_FACET = "groupFooter";

    /**
     * The id for the table row group header bar.
     */
    public static final String GROUP_HEADER_BAR_ID = "_groupHeaderBar";

    /**
     * The component id for the table row group header.
     */
    public static final String GROUP_HEADER_ID = "_groupHeader";

    /**
     * The facet name for the table row group header.
     */
    public static final String GROUP_HEADER_FACET = "groupHeader";

    /**
     * The facet name for the group header area.
     */
    public static final String HEADER_FACET = "header";

    /**
     * The id for the table column footers bar.
     */
    public static final String TABLE_COLUMN_FOOTER_BAR_ID =
            "_tableColumnFooterBar";

    /**
     * This map contains SavedState instances for each descendant component,
     * keyed by the client identifier of the descendant. Because descendant
     * client identifiers will contain the RowKey value of the parent, per-row
     * state information is actually preserved.
     */
    private Map<String, SavedState> saved = new HashMap<String, SavedState>();

    /**
     * TableDataFilter object used to apply filter. This object is not part of
     * the saved and restored state of the component.
     */
    private transient TableDataFilter filter = null;

    /**
     * TableDataSorter object used to apply sort. This object is not part of the
     * saved and restored state of the component.
     */
    private transient TableDataSorter sorter = null;

    /**
     * Flag indicating paginated state.
     */
    private boolean paginated = false;

    /**
     * paginated set flag.
     */
    private boolean paginatedSet = false;

    /**
     * The TableRowDataProvider associated with this component, lazily
     * instantiated if requested. This object is not part of the saved and
     * restored state of the component.
     */
    private TableRowDataProvider provider = null;

    /**
     * The Table ancestor enclosing this component.
     */
    private Table tableAncestor = null;

    /**
     * A List of TableColumn children found for this component.
     */
    private List<TableColumn> tableColumnChildren = null;

    /**
     * Array containing currently filtered RowKey objects.
     */
    private RowKey[] filteredRowKeys = null;

    /**
     * Array containing currently sorted RowKey objects. This sort will be
     * cached and used to iterate over children during the decode, validate, and
     * update phases.
     */
    private RowKey[] sortedRowKeys = null;

    /**
     * The number of columns to be rendered.
     */
    private int columnCount = -1;

    /**
     * Set the {@code aboveColumnFooter} attribute to true to display the
     * group footer bar above the column footers bar. The default is to display
     * the group footer below the column footers.
     */
    @Property(name = "aboveColumnFooter",
            displayName = "Is Above Column Footer",
            category = "Appearance")
    private boolean aboveColumnFooter = false;

    /**
     * aboveColumnFooter set flag.
     */
    private boolean aboveColumnFooterSet = false;

    /**
     * Set the {@code aboveColumnHeader} attribute to true to display the
     * group header bar above the column headers bar. The default is to display
     * the group header below the column headers.
     */
    @Property(name = "aboveColumnHeader",
            displayName = "Is Above Column Header",
            category = "Appearance")
    private boolean aboveColumnHeader = false;

    /**
     * aboveColumnHeader set flag.
     */
    private boolean aboveColumnHeaderSet = false;

    /**
     * Use the {@code align} attribute to specify the horizontal alignment
     * for the content of each cell in the row. Valid values are
     * {@code left}, {@code center}, {@code right},
     * {@code justify}, and {@code char}. The default alignment is
     * {@code left}. Setting the align attribute to {@code char}
     * causes the cell's contents to be aligned on the character that you
     * specify with the {@code char} attribute. For example, to align cell
     * contents on colons, set {@code align="char"} and
     * {@code char=":"}
     */
    @Property(name = "align",
            displayName = "Horizontal Alignment Cells In Group",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.TableAlignEditor")
            //CHECKSTYLE:ON
    private String align = null;

    /**
     * The deprecated BGCOLOR attribute suggests a background color for the row.
     * The combination of this attribute with &lt;FONT COLOR=...&gt; can leave
     * invisible or unreadable text on Netscape Navigator 2.x, which does not
     * support BGCOLOR on table elements. BGCOLOR is dangerous even on
     * supporting browsers, since most fail to override it when overriding other
     * author-specified colors. Style sheets provide a safer, more flexible
     * method of specifying a row's background color.
     */
    @Property(name = "bgColor",
            displayName = "Row Background Color",
            isHidden = true,
            isAttribute = false)
    private String bgColor = null;

    /**
     * Use the {@code char} attribute to specify a character to use for
     * horizontal alignment in each cell in the row. You must also set the
     * {@code align} attribute to {@code char} to enable character
     * alignment to be used. The default value for the {@code char}
     * attribute is the decimal point of the current language, such as a period
     * in English. The {@code char} HTML property is not supported by all
     * browsers.
     */
    @Property(name = "char",
            displayName = "Alignment Character For Cells",
            isHidden = true,
            isAttribute = false)
    private String charAttr = null;

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
            displayName = "Alignment Character Offset",
            isHidden = true,
            isAttribute = false)
    private String charOff = null;

    /**
     * Use the collapsed attribute to initially render the group as collapsed,
     * so that the data rows are hidden and only the header row is visible. The
     * default is to show the group expanded.
     */
    @Property(name = "collapsed",
            displayName = "Is Collapsed",
            category = "Appearance")
    private boolean collapsed = false;

    /**
     * collapsed set flag.
     */
    private boolean collapsedSet = false;

    /**
     * The text to be displayed when the table does not contain data. The text
     * is displayed left-aligned in a single row that contains one cell that
     * spans all columns. The {@code emptyDataMsg} text might be something
     * similar to "No items found." If users can add items to the table, the
     * message might include instructions, such as "This table contains no
     * files. To add a file to monitor, click the New button."
     */
    @Property(name = "emptyDataMsg",
            displayName = "Empty Data Message",
            category = "Advanced",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String emptyDataMsg = null;

    /**
     * Extra HTML code to be appended to the {@code &lt;tr&gt;} HTML
     * element that is rendered for the group footer. Use only code that is
     * valid in an HTML {@code &lt;td&gt;} element. The code you specify is
     * inserted in the HTML element, and is not checked for validity. For
     * example, you might set this attribute to {@code "nowrap=`nowrap'"}.
     */
    @Property(name = "extraFooterHtml",
            displayName = "Extra Footer HTML",
            category = "Advanced",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String extraFooterHtml = null;

    /**
     * Extra HTML code to be appended to the {@code &lt;tr&gt;} HTML
     * element that is rendered for the group header. Use only code that is
     * valid in an HTML {@code &lt;td&gt;} element. The code you specify is
     * inserted in the HTML element, and is not checked for validity. For
     * example, you might set this attribute to {@code "nowrap=`nowrap'"}.
     */
    @Property(name = "extraHeaderHtml",
            displayName = "Extra Header HTML",
            category = "Advanced",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String extraHeaderHtml = null;

    /**
     * Use the {@code first} attribute to specify which row should be the
     * first to be displayed. This value is used only when the table is
     * paginated. By default, the first row (0) is displayed first. The value of
     * this property is maintained as part of the table's state, and the value
     * is updated when the user clicks on buttons to page through the table.
     */
    @Property(name = "first",
            displayName = "First Row",
            category = "Behavior",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    private int first = Integer.MIN_VALUE;

    /**
     * first set flag.
     */
    private boolean firstSet = false;

    /**
     * The text to be displayed in the group footer.
     */
    @Property(name = "footerText",
            displayName = "Footer Text",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String footerText = null;

    /**
     * Use the {@code groupToggleButton} attribute to display a button in
     * the group header to allow users to collapse and expand the group of rows.
     */
    @Property(name = "groupToggleButton",
            displayName = "Show Group Toggle Button",
            category = "Appearance")
    private boolean groupToggleButton = false;

    /**
     * groupToggleButton set flag.
     */
    private boolean groupToggleButtonSet = false;

    /**
     * The text to be displayed in the group header.
     */
    @Property(name = "headerText",
            displayName = "header Text",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String headerText = null;

    /**
     * Use the {@code multipleColumnFooters} attribute when the
     * {@code webuijsf:tableRowGroup} contains nested
     * {@code webuijsf:tableColumn} tags, and you want the footers of all
     * the {@code webuijsf:tableColumn} tags to be shown. The default is to
     * show the footers of only the innermost level of nested
     * {@code webuijsf:tableColumn} tags.
     */
    @Property(name = "multipleColumnFooters",
            displayName = "Show Multiple Column Footers",
            category = "Layout")
    private boolean multipleColumnFooters = false;

    /**
     * multiple column footers set flag.
     */
    private boolean multipleColumnFootersSet = false;

    /**
     * Use the {@code multipleTableColumnFooters} attribute when the
     * {@code webuijsf:tableRowGroup} contains nested
     * {@code webuijsf:tableColumn} tags, and you want the table footers of
     * all the {@code webuijsf:tableColumn} tags to be shown. The default
     * is to show the table footers of only the innermost level of nested
     * {@code webuijsf:tableColumn} tags.
     */
    @Property(name = "multipleTableColumnFooters",
            displayName = "Show Nested Table Column Footers",
            category = "Layout")
    private boolean multipleTableColumnFooters = false;

    /**
     * multipleTableColumnFooters set flag.
     */
    private boolean multipleTableColumnFootersSet = false;

    /**
     * Scripting code executed when a mouse click occurs over this component.
     */
    @Property(name = "onClick",
            displayName = "Click Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onClick = null;

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
    private String onDblClick = null;

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
    private String onKeyDown = null;

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
    private String onKeyPress = null;

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
    private String onKeyUp = null;

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
    private String onMouseDown = null;

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
    private String onMouseMove = null;

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
    private String onMouseOut = null;

    /**
     *
     * Scripting code executed when the user moves the mouse pointer into the
     * boundary of this component.
     */
    @Property(name = "onMouseOver",
            displayName = "Mouse In Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onMouseOver = null;

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
    private String onMouseUp = null;

    /**
     * The number of rows per page to be displayed for a paginated table.
     * The default value is 25 per page.
     */
    @Property(name = "rows",
            displayName = "Paginated rows",
            category = "Data",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    private int rows = Integer.MIN_VALUE;

    /**
     * rows set flag.
     */
    private boolean rowsSet = false;

    /**
     * Use the {@code selectMultipleToggleButton} attribute to display a
     * button in the group header to allow users to select all rows of the group
     * at once. The button toggles a column of check boxes using the id that is
     * given to the {@code selectId} attribute of the
     * {@code webuijsf:tableColumn} tag.
     */
    @Property(name = "selectMultipleToggleButton",
            displayName = "Show Select Multiple Button",
            category = "Appearance")
    private boolean selectMultipleToggleButton = false;

    /**
     * selectMultipleToggleButton set flag.
     */
    private boolean selectMultipleToggleButtonSet = false;

    /**
     * Flag indicating that the current row is selected. If the value is set to
     * true, the row will appear highlighted.
     */
    @Property(name = "selected",
            displayName = "Is Selected",
            category = "Appearance")
    private boolean selected = false;

    /**
     * selected set flag.
     */
    private boolean selectedSet = false;

    /**
     * The {@code sourceData} attribute is used to specify the data source
     * to populate the table. The value of the {@code sourceData} attribute
     * may be a JavaServer Faces EL expression that resolves to a backing bean
     * of type {@code com.sun.data.provider.TableDataProvider}.
     * <br><br>
     * The sourceData property is referenced during multiple phases of the
     * JavaServer Faces life cycle while iterating over the rows. The object
     * that is bound to this attribute should be cached so that the object is
     * not created more often than needed.
     */
    @Property(name = "sourceData",
            displayName = "Source Data",
            category = "Data")
    private Object sourceData = null;
    /**
     * Use the {@code sourceVar} attribute to specify the name of the
     * request-scope attribute under which model data for the current row will
     * be exposed when iterating. During iterative processing over the rows of
     * data in the data provider, the TableDataProvider for the current row is
     * exposed as a request attribute under the key specified by this property.
     * Note: This value must be unique for each table in the JSP page.
     */
    @Property(name = "sourceVar",
            displayName = "Source Variable",
            category = "Data")
    private String sourceVar = null;

    /**
     * Use the {@code styleClasses} attribute to specify a list of CSS
     * style classes to apply to the rows of the group. You can apply all the
     * styles in the list to each row by separating the class names with commas.
     * Each row looks the same when commas are used to delimit the styles. You
     * can apply alternating styles to individual rows by separating the style
     * class names with spaces. You can create a pattern of shading alternate
     * rows, for example, to improve readability of the table. For example, if
     * the list has two elements, the first style class in the list is applied
     * to the first row, the second class to the second row, the first class to
     * the third row, the second class to the fourth row, etc. The tableRowGroup
     * component iterates through the list of styles and repeats from the
     * beginning until a style is applied to each row.
     */
    @Property(name = "styleClasses",
            displayName = "CSS Style Class(es)",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StyleClassPropertyEditor")
            //CHECKSTYLE:ON
    private String styleClasses = null;

    /**
     * The {@code tableDataFilter} attribute is used to define filter
     * criteria and mechanism for filtering the contents of a TableDataProvider.
     * The value of the {@code tableDataFilter} attribute must be a
     * JavaServer Faces EL expression that resolves to a backing bean of type
     * {@code com.sun.data.provider.TableDataFilter}.
     */
    @Property(name = "tableDataFilter",
            displayName = "Table Data Filter",
            category = "Data", isHidden = true,
            isAttribute = true,
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.FieldKeyEditor")
            //CHECKSTYLE:ON
    private TableDataFilter tableDataFilter = null;

    /**
     * The {@code tableDataSorter} attribute is used to define sort criteria
     * and the mechanism for sorting the contents of a TableDataProvider. The
     * value of the {@code tableDataSorter} attribute must be a JavaServer
     * Faces EL expression that resolves to a backing bean of type
     * {@code com.sun.data.provider.TableDataSorter}.
     */
    @Property(name = "tableDataSorter",
            displayName = "Table Data Sorter",
            category = "Data",
            isHidden = true,
            isAttribute = true,
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.FieldKeyEditor")
            //CHECKSTYLE:ON
    private TableDataSorter tableDataSorter = null;

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
            displayName = "Vertical Alignment Of Cells In Group",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.HtmlVerticalAlignEditor")
            //CHECKSTYLE:ON
    private String valign = null;

    /**
     * Use the visible attribute to indicate whether the component should be
     * viewable by the user in the rendered HTML page. If set to false, the HTML
     * code for the component is present in the page, but the component is
     * hidden with style attributes. By default, visible is set to true, so HTML
     * for the component HTML is included and visible to the user. If the
     * component is not visible, it can still be processed on subsequent form
     * submissions because the HTML is present.
     */
    @Property(name = "visible", displayName = "Visible", category = "Behavior")
    private boolean visible = false;

    /**
     * visible set flag.
     */
    private boolean visibleSet = false;

    /**
     * Default constructor.
     */
    public TableRowGroup() {
        super();
        setRendererType("com.sun.webui.jsf.TableRowGroup");
    }

    /**
     * This implementation returns {@code "com.sun.webui.jsf.TableRowGroup"}.
     * @return String
     */
    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.TableRowGroup";
    }

    /**
     * Clear cached properties.
     * <p>
     * Note: Properties may have been cached via the apply request values,
     * validate, and update phases and must be re-evaluated during the render
     * response phase. For example, the underlying DataProvider may have changed
     * and TableRenderer may need new calculations for the title and action bar.
     * </p><p>
     * Note: Properties of TableColumn children shall be cleared as well.
     * However, properties such as the current sort and filter criteria are not
     * automatically cleared (e.g., there may be situations where one or both
     * should be left as specified by the user).
     * </p>
     */
    public void clear() {
        provider = null;
        tableAncestor = null;
        tableColumnChildren = null;
        filteredRowKeys = null;
        sortedRowKeys = null;
        columnCount = -1;

        // Clear properties of TableColumn children.
        Iterator kids = getTableColumnChildren();
        while (kids.hasNext()) {
            TableColumn kid = (TableColumn) kids.next();
            kid.clear(); // Clear cached properties.
        }
    }

    /**
     * Get the closest Table ancestor that encloses this component.
     *
     * @return The Table ancestor.
     */
    public Table getTableAncestor() {
        if (tableAncestor == null) {
            UIComponent component = this;
            while (component != null) {
                component = component.getParent();
                if (component instanceof Table) {
                    tableAncestor = (Table) component;
                    break;
                }
            }
        }
        return tableAncestor;
    }

    /**
     * Get an Iterator over the TableColumn children found for this component.
     *
     * @return An Iterator over the TableColumn children.
     */
    public Iterator<TableColumn> getTableColumnChildren() {
        // Get TableColumn children.
        if (tableColumnChildren == null) {
            tableColumnChildren = new ArrayList<TableColumn>();
            Iterator kids = getChildren().iterator();
            while (kids.hasNext()) {
                UIComponent kid = (UIComponent) kids.next();
                if ((kid instanceof TableColumn)) {
                    tableColumnChildren.add((TableColumn) kid);
                }
            }
        }
        return tableColumnChildren.iterator();
    }

    /**
     * Get the number of columns found for this component that have a rendered
     * property of true.
     *
     * @return The number of rendered columns.
     */
    public int getColumnCount() {
        // Get column count.
        if (columnCount == -1) {
            columnCount = 0; // Initialize min value.
            Iterator kids = getTableColumnChildren();
            while (kids.hasNext()) {
                TableColumn col = (TableColumn) kids.next();
                columnCount += col.getColumnCount();
            }
        }
        return columnCount;
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Component methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    /**
     * Get empty data column.
     *
     * @return The empty data column.
     */
    public UIComponent getEmptyDataColumn() {
        UIComponent facet = getFacet(EMPTY_DATA_COLUMN_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        TableColumn child = new TableColumn();
        child.setId(EMPTY_DATA_COLUMN_ID);
        child.setColSpan(getColumnCount());
        child.getChildren().add(getEmptyDataText());

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get empty data text.
     *
     * @return The empty data text.
     */
    public UIComponent getEmptyDataText() {
        UIComponent facet = getFacet(EMPTY_DATA_TEXT_FACET);
        if (facet != null) {
            return facet;
        }

        Theme theme = getTheme();

        // Get message.
        String msg = null;
        if (getEmptyDataMsg() != null) {
            msg = getEmptyDataMsg();
        } else {
            // Get unfiltered row keys.
            RowKey[] rowKeys = getRowKeys();
            if (rowKeys != null && rowKeys.length > 0) {
                msg = theme.getMessage("table.filteredData");
            } else {
                msg = theme.getMessage("table.emptyData");
            }
        }

        // Get child.
        StaticText child = new StaticText();
        child.setId(EMPTY_DATA_TEXT_ID);
        child.setStyleClass(theme
                .getStyleClass(ThemeStyles.TABLE_MESSAGE_TEXT));
        child.setText(msg);

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get group footer.
     *
     * @return The group footer.
     */
    public UIComponent getGroupFooter() {
        UIComponent facet = getFacet(GROUP_FOOTER_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        TableFooter child = new TableFooter();
        child.setId(GROUP_FOOTER_ID);
        child.setColSpan(getColumnCount());
        child.setExtraHtml(getExtraFooterHtml());
        child.setGroupFooter(true);

        // Set rendered.
        facet = getFacet(FOOTER_FACET);
        if (!(facet != null && facet.isRendered() || getFooterText() != null)) {
            child.setRendered(false);
        } else {
            log("getGroupFooter",
                    "Group footer not rendered, nothing to display");
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get group header.
     *
     * @return The group header.
     */
    public UIComponent getGroupHeader() {
        UIComponent facet = getFacet(GROUP_HEADER_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        TableHeader child = new TableHeader();
        child.setId(GROUP_HEADER_ID);
        child.setScope("colgroup");
        child.setColSpan(getColumnCount());
        child.setExtraHtml(getExtraHeaderHtml());
        child.setGroupHeader(true);

        // Don't render for an empty table.
        boolean emptyTable = getRowCount() == 0;
        boolean renderControls = !emptyTable
                && (isSelectMultipleToggleButton() || isGroupToggleButton());

        // Set rendered.
        facet = getFacet(HEADER_FACET);
        if (!(facet != null
                && facet.isRendered()
                || getHeaderText() != null
                || renderControls)) {
            child.setRendered(false);
        } else {
            log("getGroupHeader",
                    "Group header not rendered, nothing to display");
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Clear FilterCriteria objects from the TableDataFilter instance used by
     * this component.
     * <p>
     * Note: This method clears the cached filter and sort, then resets
     * pagination to the first page per UI guidelines.
     * </p>
     */
    public void clearFilter() {
        getTableDataFilter().setFilterCriteria(null);
        // Clear all FilterCriteria.
        // Reset to first page.
        setPage(1);
        filteredRowKeys = null; // Clear filtered row keys.
        sortedRowKeys = null; // Clear sorted row keys.
    }

    /**
     * Get an array containing filtered RowKey objects.
     * <p>
     * Note: This filter depends on the FilterCriteria objects provided to the
     * TableDataFilter instance used by this component. Due to filtering, the
     * size of the returned array may be less than the total number of RowKey
     * objects for the underlying TableDataProvider.
     * </p><p>
     * Note: The returned RowKey objects are cached. If the TableDataFilter
     * instance used by this component is modified directly, invoke the
     * clearFilter method to clear the previous filter.
     * </p>
     *
     * @return An array containing filtered RowKey objects.
     */
    public RowKey[] getFilteredRowKeys() {
        // Initialize RowKey objects, if not cached already.
        if (filteredRowKeys != null) {
            return filteredRowKeys;
        } else {
            filteredRowKeys = getRowKeys();
        }

        // Do not attempt to filter with a null provider.
        TableDataProvider tableProvider = getTableRowDataProvider().
                getTableDataProvider();
        if (tableProvider == null) {
            log("getFilteredRowKeys",
                    "Cannot obtain filtered row keys,"
                    + " TableDataProvider is null");
            return filteredRowKeys;
        }

        // If TableDataFilter and TableDataProvider are the same instance, the
        // filter method is never called. The filter order is assumed to be
        // intrinsic in the row data of the TableDataProvider.
        TableDataFilter tableFilter = getTableDataFilter();
        if (tableProvider != tableFilter) {
            filteredRowKeys = tableFilter
                    .filter(tableProvider, filteredRowKeys);
        } else {
            log("getFilteredRowKeys",
                    "Row keys already filtered, TableDataFilter and"
                    + " TableDataProvider are the same instance");
        }
        return filteredRowKeys;
    }

    /**
     * Get the TableDataFilter object used to filter rows.
     *
     * @return The TableDataFilter object used to filter rows.
     */
    public TableDataFilter getTableDataFilter() {
        // Method is overriden because TableDataFilter is not serializable.
        TableDataFilter tdf;
        if (this.tableDataFilter != null) {
            tdf = this.tableDataFilter;
        } else {
            ValueExpression vb = getValueExpression("tableDataFilter");
            if (vb != null) {
                tdf = (TableDataFilter) vb.getValue(getFacesContext()
                        .getELContext());
            } else {
                tdf = null;
            }
        }
        if (tdf != null) {
            return tdf;
        }

        // Get default filter.
        if (filter == null) {
            filter = new BasicTableDataFilter();
        }
        return filter;
    }

    /**
     * Set FilterCriteria objects for the TableDataFilter instance used by this
     * component.
     * <p>
     * Note: This method clears the cached filter and sort, then resets
     * pagination to the first page per UI guidelines.
     * </p>
     *
     * @param newFilterCriteria An array of FilterCriteria objects defining the
     * filter order on this TableDataFilter.
     */
    public void setFilterCriteria(final FilterCriteria[] newFilterCriteria) {
        clearFilter();
        getTableDataFilter().setFilterCriteria(newFilterCriteria);
    }

    /**
     * Set the TableDataFilter object used to filter rows.
     *
     * @param newFilter The TableDataFilter object used to filter rows.
     */
    public void setTableDataFilter(final TableDataFilter newFilter) {
        // Method is overriden because TableDataFilter is not serializable.
        this.filter = newFilter;
    }

    /**
     * Get the zero-relative row number of the first row to be displayed for a
     * paginated table.
     * <p>
     * Note: If rows have been removed from the underlying DataProvider, there
     * is a chance that the first row could be greater than the total number of
     * rows. In this case, the zero-relative row number of the last page to be
     * displayed is returned.
     * </p>
     *
     * @return The zero-relative row number of the first row to be displayed.
     */
    public int getFirst() {
        // Ensure the first row is less than the row number of the last page.
        int last = getLast();
        int firstRow;
        if (isPaginated()) {
            int zFirst;
            if (this.firstSet) {
                zFirst = this.first;
            } else {
                ValueExpression vb = getValueExpression("first");
                if (vb != null) {
                    Object result = vb.getValue(getFacesContext()
                            .getELContext());
                    if (result == null) {
                        zFirst = Integer.MIN_VALUE;
                    } else {
                        zFirst = ((Integer) result);
                    }
                } else {
                    zFirst = 0;
                }
            }
            firstRow = Math.max(0, zFirst);
        } else {
            firstRow = 0;
        }
        if (firstRow < last) {
            return firstRow;
        }
        return last;
    }

    /**
     * Set the zero-relative row number of the first row to be displayed for a
     * paginated table.
     * <p>
     * Note: This method is used to set the current, first, next, prev, and last
     * pages. For example, you could use setFirst(0) to display the first page
     * and setFirst(getLast()) to display the last page. This method is
     * particularly useful when a subset of data is displayed in scroll mode or
     * when overriding pagination. As a convenience, the setPage(int) method is
     * provided.
     * </p><p>
     * Note: When ever a new DataProvider is used, UI Guidelines recommend that
     * pagination should be reset (e.g., remaining on the 4th page of a new set
     * of data makes no sense).
     * </p>
     *
     * @param newFirst The first row number.
     * @exception IllegalArgumentException for negative values.
     */
    public void setFirst(final int newFirst) {
        if (newFirst < 0) {
            log("setFirst", "First row cannot be < 0");
            throw new IllegalArgumentException(Integer.toString(newFirst));
        }
        this.first = newFirst;
        this.firstSet = true;
    }

    /**
     * Get the zero-relative row number of the last page to be displayed.
     *
     * @return The zero-relative row number of the last page to be displayed.
     */
    public int getLast() {
        return Math.max(0, getPages() - 1) * getRows();
    }

    /**
     * Get current page number to be displayed.
     * <p>
     * Note: The default is 1 when the table is not paginated.
     * </p>
     *
     * @return The current page number to be displayed.
     */
    public int getPage() {
        if (!isPaginated()) { // Rows is zero when paginated.
            return 1;
        }
        return (getFirst() / getRows()) + 1;
    }

    /**
     * Get total number of pages to be displayed. The default is 1 when the
     * table is not paginated.
     * <p>
     * Note: The page count depends on the FilterCriteria objects provided to
     * the TableDataFilter instance used by this component. Further, the filter
     * used to obtain the page count is cached. If the TableDataFilter instance
     * used by this component is to be modified directly, invoke the clearFilter
     * method to clear the previous filter.
     * </p>
     *
     * @return The total number of pages to be displayed.
     */
    public int getPages() {
        if (!isPaginated()) {
            return 1;
        }

        // Get row count.
        int rowCount = getRowCount();
        // Get rows per page.
        int tableRows = getRows();

        // Note: Rows should be > 0 when paginated.
        int modulus;
        if (tableRows > 0) {
            modulus = rowCount % tableRows;
        } else {
            modulus = 0;
        }
        int result;
        if (tableRows > 0) {
            result = rowCount / tableRows;
        } else {
            result = 1;
        }

        // Increment result for extra rows.
        if (modulus > 0) {
            return ++result;
        }
        return result;
    }

    /**
     * Test the paginated state of this component.
     * <p>
     * Note: If the paginationControls property of the Table component is true,
     * this property will be initialized as true.
     * </p>
     *
     * @return true for paginate mode, false for scroll mode.
     */
    public boolean isPaginated() {
        if (!paginatedSet) {
            Table table = getTableAncestor();
            if (table != null) {
                setPaginated(table.isPaginationControls());
            } else {
                log("isPaginated",
                        "Cannot initialize paginated state, Table is null");
            }
        }
        return paginated;
    }

    /**
     * A convenience method to set the current page to be displayed.
     * <p>
     * Note: You can also set the current, first, next, prev, and last pages by
     * invoking the setFirst(int) method directly. For example, you could use
     * setFirst(0) to display the first page and setFirst(getLast()) to display
     * the last page. The setFirst(int) method is particularly useful when a
     * subset of data is displayed in scroll mode or when overriding pagination.
     * </p><p>
     * Note: When ever a new DataProvider is used, UI Guidelines recommend that
     * pagination should be reset (e.g., remaining on the 4th page of a new set
     * of data makes no sense).
     * </p>
     *
     * @param page The current page.
     */
    public void setPage(final int page) {
        // Set the starting row for the new page.
        int row = (page - 1) * getRows();

        // Result cannot be greater than the row index for the last page.
        int result = Math.min(row, getLast());

        // Result cannot be greater than total number of rows or less than zero.
        setFirst(Math.min(Math.max(result, 0), getRowCount()));
    }

    /**
     * Set the paginated state of this component.
     * <p>
     * Note: When pagination controls are used, a value of true allows both
     * pagination controls and paginate buttons to be displayed. A value of
     * false allows only paginate buttons to be displayed. However, when all
     * data fits on one page, neither pagination controls or paginate buttons
     * are displayed.
     * </p><p>
     * Note: To properly maintain the paginated state of the table per UI
     * guidelines, the paginated property is cached. If the paginationControls
     * property of the table component changes (e.g., in an application builder
     * environment), use this method to set the paginated property accordingly.
     * </p>
     *
     * @param newPaginated The paginated state of this component.
     */
    public void setPaginated(final boolean newPaginated) {
        this.paginated = newPaginated;
        paginatedSet = true;
    }

    /**
     * Get the number of rows to be displayed for a paginated table.
     * <p>
     * Note: UI guidelines recommend a default value of 25 rows per page.
     * </p>
     *
     * @return The number of rows to be displayed for a paginated table.
     */
    @SuppressWarnings("checkstyle:magicnumber")
    public int getRows() {
        if (isPaginated()) {
            int tableRows;
            if (this.rowsSet) {
                tableRows = this.rows;
            } else {
                ValueExpression vb = getValueExpression("rows");
                if (vb != null) {
                    Object result = vb.getValue(getFacesContext()
                            .getELContext());
                    if (result == null) {
                        tableRows = Integer.MIN_VALUE;
                    } else {
                        tableRows = ((Integer) result);
                    }
                } else {
                    tableRows = 25;
                }
            }
            return Math.max(1, tableRows);
        }
        return 0;
    }

    /**
     * Set the number of rows to be displayed for a paginated table.
     *
     * @param newRows The number of rows to be displayed for a paginated table.
     * @exception IllegalArgumentException for negative values.
     */
    public void setRows(final int newRows) {
        if (newRows < 0) {
            log("setRows", "Paginated rows cannot be < 0");
            throw new IllegalArgumentException(Integer.toString(newRows));
        }
        this.rows = newRows;
        this.rowsSet = true;
    }

    /**
     * Get the flag indicating whether there is row data available for the
     * current RowKey. If no row data is available, false is returned.
     *
     * @return The flag indicating whether there is row data available.
     */
    public boolean isRowAvailable() {
        boolean result = false;
        TableDataProvider tableProvider = getTableRowDataProvider().
                getTableDataProvider();
        if (tableProvider != null) {
            result = tableProvider.isRowAvailable(getRowKey());
        } else {
            log("isRowAvailable",
                    "Cannot determine if row is available,"
                    + " TableDataProvider is null");
        }
        return result;
    }

    /**
     * Get an array of hidden RowKey objects from the underlying
     * TableDataProvider taking filtering, sorting, and pagination into account.
     * <p>
     * Note: The returned RowKey objects depend on the FilterCriteria and
     * SortCriteria objects provided to the TableDataFilter and TableDataSorter
     * instances used by this component. If TableDataFilter and TableDataSorter
     * are modified directly, invoke the clearSort and clearFilter method to
     * clear the previous sort and filter.
     * </p>
     *
     * @return An array of RowKey objects.
     */
    public RowKey[] getHiddenRowKeys() {
        if (!isPaginated()) {
            return null; // No rows are hidden during scroll mode.
        }

        // Get sorted RowKey objects.
        RowKey[] rowKeys = getSortedRowKeys();
        if (rowKeys == null) {
            return rowKeys;
        }

        // Find the number of selected rows hidden from view.
        ArrayList<RowKey> list = new ArrayList<RowKey>();
        int firstRow = getFirst();
        int tableRows = getRows();
        for (int i = 0; i < rowKeys.length; i++) {
            // Have we displayed the paginated number of rows?
            if (i >= firstRow && i < firstRow + tableRows) {
                continue;
            }
            list.add(rowKeys[i]);
        }
        rowKeys = new RowKey[list.size()];
        return (RowKey[]) list.toArray(rowKeys);
    }

    /**
     * Get the FieldKey from the underlying TableDataProvider.
     * <p>
     * WARNING for ObjectArrayDataProvider: Until you call setArray() or
     * setObjectType with a non-null parameter, or use a constructor variant
     * that accepts an non-null array, no information about field keys will be
     * available. Therefore, any attempt to reference a FieldKey or field
     * identifier in a method call will throw IllegalArgumentException}.
     * </p><p>
     * WARNING for ObjectListDataProvider: Until you call setList() or
     * setObjectType with a non-null parameter, or use a constructor variant
     * that accepts an non-null non-empty list, no information about field keys
     * will be available. Therefore, any attempt to reference a FieldKey or
     * field identifier in a method call will throw IllegalArgumentException.
     * </p>
     *
     * @param fieldId The id of the requested FieldKey.
     * @return The RowKey from the underlying TableDataProvider.
     */
    public FieldKey getFieldKey(final String fieldId) {
        return getTableRowDataProvider().getFieldKey(fieldId);
    }

    /**
     * Get the number of rows in the underlying TableDataProvider. If the number
     * of available rows is unknown, -1 is returned.
     * <p>
     * Note: This row count depends on the FilterCriteria objects provided to
     * the TableDataFilter instance used by this component. Further, the filter
     * used to obtain the row count is cached. If the TableDataFilter instance
     * used by this component is modified directly, invoke the clearFilter
     * method to clear the previous filter.
     * </p>
     *
     * @return The number of rows in the underlying TableDataProvider.
     */
    public int getRowCount() {
        RowKey[] rowKeys = getFilteredRowKeys();
        if (rowKeys != null) {
            return rowKeys.length;
        }
        return 0;
    }

    /**
     * Get the RowKey associated with the current row.
     *
     * @return The RowKey associated with the current row.
     */
    public RowKey getRowKey() {
        return getTableRowDataProvider().getTableRow();
    }

    /**
     * Get all RowKey objects for the underlying TableDataProvider.
     *
     * @return All RowKey objects for the underlying TableDataProvider.
     */
    @SuppressWarnings("checkstyle:magicnumber")
    public RowKey[] getRowKeys() {
        RowKey[] rowKeys = null;
        TableDataProvider tableProvider = getTableRowDataProvider().
                getTableDataProvider();
        if (tableProvider == null) {
            log("getRowKeys",
                    "Cannot obtain row keys, TableDataProvider is null");
            return rowKeys;
        }

        // Create fake data for design-time behavior. The ResultSetDataProvider
        // returns 3 rows of dummy data; however, this is not enough to display
        // pagination controls properly. When all rows fit on a single page, or
        // when we have an empty table, certain controls are hidden from view.
        // Thus, if a user specifies 20 rows per page, we want to create 20 + 1
        // rows of data forcing controls to be displayed.
        if (Beans.isDesignTime()) {
            log("getRowKeys", "Creating dummy data for design-time behavior");
            rowKeys = tableProvider.getRowKeys(tableProvider.getRowCount(),
                    null);
            // If pagination is not enabled, dummy data is not required.
            if (getRows() == 0 || rowKeys == null || rowKeys.length == 0) {
                log("getRowKeys",
                        "Cannot create dummy data, DataProvider has no rows");
                return rowKeys;
            } else {
                ArrayList<RowKey> list = new ArrayList<RowKey>();
                for (int i = 0; i < getRows() + 1; i++) {
                    list.add(rowKeys[i % rowKeys.length]);
                }
                rowKeys = new RowKey[list.size()];
                return ((RowKey[]) list.toArray(rowKeys));
            }
        }

        // It's possible that the provider returned -1 because it does not
        // actually have all the rows, so it's up to the consumer of the
        // interface to fetch them. Typically, 99% of the data providers will
        // return a valid row count (at least our providers will), but we still
        // need to handle the scenario where -1 is returned.
        int rowCount = tableProvider.getRowCount();
        if (rowCount == -1) {
            log("getRowKeys",
                    "Manually calculating row count,"
                    + " DataProvider.getRowCount() is -1");
            int index = 0;
            do {
                // Keep trying until all rows are obtained.
                rowCount = 1000000 * ++index;
                rowKeys = tableProvider.getRowKeys(rowCount, null);
            } while (rowKeys != null && rowKeys.length - 1 == rowCount);
        } else {
            rowKeys = tableProvider.getRowKeys(rowCount, null);
        }
        return rowKeys;
    }

    /**
     * Get the TableRowDataProvider object representing the data objects that we
     * will iterate over in this component's rendering.
     *
     * @return The TableRowDataProvider object.
     */
    protected TableRowDataProvider getTableRowDataProvider() {
        // Get provider.
        if (provider == null) {
            log("getTableRowDataProvider",
                    "Re-evaluating sourceData, TableRowDataProvider is null");

            // Synthesize a TableDataProvider around source data, if possible.
            TableDataProvider tdp;
            Object obj = getSourceData();
            if (obj == null) {
                tdp = null;
            } else if (obj instanceof TableDataProvider) {
                tdp = (TableDataProvider) obj;
            } else if (obj instanceof List) {
                tdp = new ObjectListDataProvider((List) obj);
            } else if (Object[].class.isAssignableFrom(obj.getClass())) {
                tdp = new ObjectArrayDataProvider((Object[]) obj);
            } else {
                // Default "single variable" case.
                ArrayList<Object> list = new ArrayList<Object>(1);
                list.add(obj);
                tdp = new ObjectListDataProvider(list);
            }
            provider = new TableRowDataProvider(tdp);
        }
        return provider;
    }

    /**
     * Get the data type of the data element referenced by the given FieldKey.
     *
     * @param fieldKey The FieldKey identifying the data element whose type is
     * to be returned.
     * @return The data type of the data element referenced by the given
     * FieldKey.
     */
    public Class getType(final FieldKey fieldKey) {
        return getTableRowDataProvider().getType(fieldKey);
    }

    /**
     * Get an array of rendered RowKey objects from the underlying
     * TableDataProvider taking filtering, sorting, and pagination into account.
     * <p>
     * Note: The returned RowKey objects depend on the FilterCriteria and
     * SortCriteria objects provided to the TableDataFilter and TableDataSorter
     * instances used by this component. If TableDataFilter and TableDataSorter
     * are modified directly, invoke the clearSort and clearFilter method to
     * clear the previous sort and filter.
     * </p>
     *
     * @return An array of RowKey objects.
     */
    public RowKey[] getRenderedRowKeys() {
        // Get sorted RowKey objects.
        RowKey[] rowKeys = getSortedRowKeys();
        if (rowKeys == null) {
            return rowKeys;
        }

        // Find the number of selected rows hidden from view.
        ArrayList<RowKey> list = new ArrayList<RowKey>();
        int firstRow = getFirst();
        int tableRows = getRows();
        for (int i = firstRow; i < rowKeys.length; i++) {
            // Have we displayed the paginated number of rows?
            if (isPaginated() && i >= firstRow + tableRows) {
                break;
            }
            list.add(rowKeys[i]);
        }
        rowKeys = new RowKey[list.size()];
        return (RowKey[]) list.toArray(rowKeys);
    }

    /**
     * Set the RowKey associated with the current row or null for no current row
     * association.
     * <p>
     * Note: It is possible to set the RowKey at a value for which the
     * underlying TableDataProvider does not contain any row data. Therefore,
     * callers may use the isRowAvailable() method to detect whether row data
     * will be available.
     * <ul>
     * <li>Save current state information for all descendant components (as
     * described below).
     * <li>Store the new RowKey, and pass it on to the TableDataProvider
     * associated with this TableRowGroup instance.</li>
     * <li>If the new RowKey value is null:
     * <ul>
     * <li>If the var property is not null, remove the corresponding request
     * scope attribute (if any).</li>
     * <li>Reset the state information for all descendant components (as
     * described below).</li>
     * </ul></li>
     * <li>If the new RowKey value is not null:
     * <ul>
     * <li>If the var property is not null, expose the data provider as a
     * request scope attribute whose key is the var property value.</li>
     * <li>Reset the state information for all descendant components (as
     * described below).
     * </ul></li>
     * </ul></p><p>
     * To save current state information for all descendant components,
     * TableRowGroup must maintain per-row information for each descendant as
     * follows:
     * <ul>
     * <li>If the descendant is an instance of EditableValueHolder, save the
     * state of its localValue property.</li>
     * <li>If the descendant is an instance of EditableValueHolder, save the
     * state of the localValueSet property.</li>
     * <li>If the descendant is an instance of EditableValueHolder, save the
     * state of the valid property.</li>
     * <li>If the descendant is an instance of EditableValueHolder, save the
     * state of the submittedValue property.</li>
     * </ul></p><p>
     * To restore current state information for all descendant components,
     * TableRowGroup must reference its previously stored information for the
     * current RowKey and call setters for each descendant as follows:
     * <ul>
     * <li>If the descendant is an instance of EditableValueHolder, restore the
     * value property.</li>
     * <li>If the descendant is an instance of EditableValueHolder, restore the
     * state of the localValueSet property.</li>
     * <li>If the descendant is an instance of EditableValueHolder, restore the
     * state of the valid property.</li>
     * <li>If the descendant is an instance of EditableValueHolder, restore the
     * state of the submittedValue property.</li>
     * </ul></p>
     *
     * @param rowKey The RowKey associated with the current row or null for no
     * association.
     */
    public void setRowKey(final RowKey rowKey) {
        // Save current state for the previous row.
        saveDescendantState();

        // Update to the new row.
        getTableRowDataProvider().setTableRow(rowKey);

        // Clear or expose the current row data as a request scope attribute
        String tableSourceVar = getSourceVar();
        if (tableSourceVar != null) {
            Map<String, Object> requestMap
                    = getFacesContext().getExternalContext().getRequestMap();
            if (rowKey == null) {
                requestMap.remove(tableSourceVar);
            } else if (isRowAvailable()) {
                requestMap.put(tableSourceVar, getTableRowDataProvider());
            } else {
                requestMap.remove(tableSourceVar);
            }
        } else {
            log("setRowKey", "Cannot set row key, sourceVar property is null");
        }

        // Reset current state information for the new row.
        restoreDescendantState();
    }

    /**
     * Set the source data of the TableRowGroup.
     * <p>
     * Note: When ever the underlying DataProvider has changed, UI Guidelines
     * recommend that pagination should be reset (e.g., remaining on the 4th
     * page of a new set of data makes no sense). However, properties such as
     * the sort and filter criteria should not automatically be cleared (e.g.,
     * there may be situations where one or both should be left as specified by
     * the user). In this scenario, pagination is set to the first page.
     * </p>
     *
     * @param newSourceData The source data of the TableRowGroup.
     */
    public void setSourceData(final Object newSourceData) {
        this.sourceData = newSourceData;
        // Reset to first page.
        setPage(1);
        // Clear cached properties.
        clear();
    }

    /**
     * Get the number of objects from the underlying data provider where the
     * selected property of this component is set to true and the row is
     * currently hidden from view.
     * <p>
     * Note: UI guidelines recommend that rows should be unselected when no
     * longer in view. For example, when a user selects rows of the table and
     * navigates to another page. Or, when a user applies a filter or sort that
     * may hide previously selected rows from view. If a user invokes an action
     * to delete the currently selected rows, they may inadvertently remove rows
     * not displayed on the current page. That said, there are cases when
     * maintaining state across table pages is necessary. When maintaining state
     * and there are currently no hidden selections, UI guidelines recommend
     * that the number zero should be shown.
     * </p><p>
     * Note: This count depends on the FilterCriteria and SortCriteria objects
     * provided to the TableDataFilter and TableDataSorter instances used by
     * this component. If TableDataFilter and TableFilterSorter are modified
     * directly, invoke the clearFilter method to clear the previous filter and
     * sort.
     * </p>
     *
     * @return The number of selected rows currently hidden from view.
     */
    public int getHiddenSelectedRowsCount() {
        RowKey[] rowKeys = getHiddenSelectedRowKeys();
        if (rowKeys != null) {
            return rowKeys.length;
        }
        return 0;
    }

    /**
     * Get an array of RowKey objects from the underlying data provider where
     * the selected property of this component is set to true and the row is
     * currently hidden from view.
     * <p>
     * Note: UI guidelines recommend that rows should be unselected when no
     * longer in view. For example, when a user selects rows of the table and
     * navigates to another page. Or, when a user applies a filter or sort that
     * may hide previously selected rows from view. If a user invokes an action
     * to delete the currently selected rows, they may inadvertently remove rows
     * not displayed on the current page.
     * </p><p>
     * Note: The returned RowKey objects depend on the FilterCriteria and
     * SortCriteria objects provided to the TableDataFilter and TableDataSorter
     * instances used by this component. If TableDataFilter and TableDataSorter
     * are modified directly, invoke the clearSort and clearFilter method to
     * clear the previous sort and filter.
     * </p>
     *
     * @return An array of RowKey objects.
     */
    public RowKey[] getHiddenSelectedRowKeys() {
        // Get hidden RowKey objects.
        RowKey[] rowKeys = getHiddenRowKeys();
        if (rowKeys == null) {
            return rowKeys;
        }

        // Save the current RowKey.
        RowKey rowKey = getRowKey();

        // Find the number of selected rows hidden from view.
        ArrayList<RowKey> list = new ArrayList<RowKey>();
        for (RowKey rowKey1 : rowKeys) {
            setRowKey(rowKey1);
            if (isRowAvailable() && isSelected()) {
                list.add(rowKey1);
            }
        }
        setRowKey(rowKey);
        // Restore the current RowKey.
        rowKeys = new RowKey[list.size()];
        return (RowKey[]) list.toArray(rowKeys);
    }

    /**
     * Get the number of selected rows from the underlying data provider where
     * the selected property of this component is set to true.
     * <p>
     * Note: This count depends on the FilterCriteria objects provided to the
     * TableDataFilter instance used by this component. If TableDataFilter is
     * modified directly, invoke the clearFilter method to clear the previous
     * filter.
     * </p>
     *
     * @return The number of selected rows.
     */
    public int getSelectedRowsCount() {
        RowKey[] rowKeys = getSelectedRowKeys();
        if (rowKeys != null) {
            return rowKeys.length;
        }
        return 0;
    }

    /**
     * Get an array of RowKey objects from the underlying data provider where
     * the selected property of this component is set to true.
     * <p>
     * Note: The returned RowKey objects depend on the FilterCriteria objects
     * provided to the TableDataFilter instance used by this component. If
     * TableDataFilter is modified directly, invoke the clearFilter method to
     * clear the previous filter.
     * </p>
     *
     * @return An array of RowKey objects.
     */
    public RowKey[] getSelectedRowKeys() {
        // Get filtered RowKey objects.
        RowKey[] rowKeys = getFilteredRowKeys();
        if (rowKeys == null) {
            return rowKeys;
        }

        // Save the current RowKey.
        RowKey rowKey = getRowKey();

        // Find the number of selected rows.
        ArrayList<RowKey> list = new ArrayList<RowKey>();
        for (RowKey rowKey1 : rowKeys) {
            setRowKey(rowKey1);
            if (isRowAvailable() && isSelected()) {
                list.add(rowKey1);
            }
        }
        setRowKey(rowKey); // Restore the current RowKey.
        rowKeys = new RowKey[list.size()];
        return (RowKey[]) list.toArray(rowKeys);
    }

    /**
     * Get the number of objects from the underlying data provider where the
     * selected property of this component is set to true and the row is
     * rendered.
     * <p>
     * Note: UI guidelines recommend that rows should be unselected when no
     * longer in view. For example, when a user selects rows of the table and
     * navigates to another page. Or, when a user applies a filter or sort that
     * may hide previously selected rows from view. If a user invokes an action
     * to delete the currently selected rows, they may inadvertently remove rows
     * not displayed on the current page.
     * </p><p>
     * Note: This count depends on the FilterCriteria and SortCriteria objects
     * provided to the TableDataFilter and TableDataSorter instances used by
     * this component. If TableDataFilter and TableFilterSorter are modified
     * directly, invoke the clearFilter method to clear the previous filter and
     * sort.
     * </p>
     *
     * @return The number of selected rows currently hidden from view.
     */
    public int getRenderedSelectedRowsCount() {
        RowKey[] rowKeys = getRenderedSelectedRowKeys();
        if (rowKeys != null) {
            return rowKeys.length;
        }
        return 0;
    }

    /**
     * Get an array of RowKey objects from the underlying data provider where
     * the selected property of this component is set to true and the row is
     * rendered.
     * <p>
     * Note: UI guidelines recommend that rows should be unselected when no
     * longer in view. For example, when a user selects rows of the table and
     * navigates to another page. Or, when a user applies a filter or sort that
     * may hide previously selected rows from view. If a user invokes an action
     * to delete the currently selected rows, they may inadvertently remove rows
     * not displayed on the current page.
     * </p><p>
     * Note: The returned RowKey objects depend on the FilterCriteria and
     * SortCriteria objects provided to the TableDataFilter and TableDataSorter
     * instances used by this component. If TableDataFilter and TableDataSorter
     * are modified directly, invoke the clearSort and clearFilter method to
     * clear the previous sort and filter.
     * </p>
     *
     * @return An array of RowKey objects.
     */
    public RowKey[] getRenderedSelectedRowKeys() {
        // Get rendered RowKey objects.
        RowKey[] rowKeys = getRenderedRowKeys();
        if (rowKeys == null) {
            return rowKeys;
        }

        // Save the current RowKey.
        RowKey rowKey = getRowKey();

        // Find the number of selected rows in view.
        ArrayList<RowKey> list = new ArrayList<RowKey>();
        for (RowKey rowKey1 : rowKeys) {
            setRowKey(rowKey1);
            if (isRowAvailable() && isSelected()) {
                list.add(rowKey1);
            }
        }
        setRowKey(rowKey); // Restore the current RowKey.
        rowKeys = new RowKey[list.size()];
        return (RowKey[]) list.toArray(rowKeys);
    }

    /**
     * Add a SortCriteria object to sort.
     * <p>
     * Note: Objects are sorted in the reverse order they were added. For
     * example, the first object added, will be the last sort applied as the
     * primary sort. The second object added, will be the second to last sort
     * applied as the secondary sort. The third object added, will be the third
     * to last sort applied as the tertiary sort and so on. If an existing
     * SortCriteria object is found with the same FieldKey, the sort order is
     * replaced with the new value. Note that sorts are not actually applied
     * until the getSortedRowKeys() method is invoked, which happens
     * automatically by the renderer.
     * </p><p>
     * Note: This method also resets pagination to the first page per UI
     * guidelines.
     * </p>
     *
     * @param criteria The SortCriteria object to sort.
     */
    public void addSort(final SortCriteria criteria) {
        if (criteria == null) {
            return;
        }

        TableDataSorter tableSorter = getTableDataSorter();
        SortCriteria[] oldCriteria = tableSorter.getSortCriteria();

        // Iterate over each SortCriteria object and check for a match.
        if (oldCriteria != null) {
            for (int i = 0; i < oldCriteria.length; i++) {
                if (oldCriteria[i] == null) {
                    continue;
                }
                String key = oldCriteria[i].getCriteriaKey();
                if (key != null && key.equals(criteria.getCriteriaKey())) {
                    oldCriteria[i] = criteria;
                    // No further processing is required.
                    return;
                }
            }
        }

        // Create array to hold new criteria.
        int oldLength;
        if (oldCriteria != null) {
            oldLength = oldCriteria.length;
        } else {
            oldLength = 0;
        }
        SortCriteria[] newCriteria = new SortCriteria[oldLength + 1];
        if (oldLength > 0) {
            System.arraycopy(oldCriteria, 0, newCriteria, 0, oldLength);
        }
        // Add new SortCriteria object.
        newCriteria[oldLength] = criteria;
        // Set new SortCriteria.
        tableSorter.setSortCriteria(newCriteria);
        // Clear sorted row keys.
        sortedRowKeys = null;
        // Reset to first page.
        setPage(1);
    }

    /**
     * Clear SortCriteria objects from the TableDataSorter instance used by this
     * component.
     * <p>
     * Note: This method clears the cached sort, then resets pagination to the
     * first page per UI guidelines.
     * </p>
     */
    public void clearSort() {
        // Clear all SortCriteria.
        getTableDataSorter().setSortCriteria(null);
        // Clear sorted row keys.
        sortedRowKeys = null;
        // Reset to first page.
        setPage(1);
    }

    /**
     * Get the number of SortCriteria objects to sort.
     *
     * @return The number of SortCriteria objects to sort.
     */
    public int getSortCount() {
        int result = 0;
        SortCriteria[] sortCriteria = getTableDataSorter().getSortCriteria();
        if (sortCriteria != null) {
            result = sortCriteria.length;
        }
        return result;
    }

    /**
     * Get the level of the given SortCriteria object to sort.
     * <p>
     * Note: The primary sort is level 1, the secondary sort is level 2, the
     * tertiary sort is level 3, and so on. If the SortCriteria object was not
     * previously added using the addSort method, the level will be returned as
     * -1.
     * </p>
     *
     * @param criteria The SortCriteria object to sort.
     * @return The sort level or -1 if the SortCriteria object was not
     * previously added.
     */
    public int getSortLevel(final SortCriteria criteria) {
        int result = -1;
        if (criteria == null) {
            return result;
        }

        // Iterate over each SortCriteria object and check for a match.
        SortCriteria[] sortCriteria = getTableDataSorter().getSortCriteria();
        if (sortCriteria != null) {
            for (int i = 0; i < sortCriteria.length; i++) {
                if (sortCriteria[i] == null) {
                    continue;
                }
                String key = sortCriteria[i].getCriteriaKey();
                if (key != null && key.equals(criteria.getCriteriaKey())) {
                    result = i + 1;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Test if given SortCriteria object is a descending sort.
     *
     * @param criteria The SortCriteria object to sort.
     * @return true if descending, else false.
     */
    public boolean isDescendingSort(final SortCriteria criteria) {
        boolean result = false;
        if (criteria == null) {
            return result;
        }

        // Iterate over each SortCriteria object and check for a match.
        SortCriteria[] sortCriteria = getTableDataSorter().getSortCriteria();
        if (sortCriteria != null) {
            for (SortCriteria sortCriteria1 : sortCriteria) {
                if (sortCriteria1 == null) {
                    continue;
                }
                String key = sortCriteria1.getCriteriaKey();
                if (key != null && key.equals(criteria.getCriteriaKey())) {
                    // Note: SortCriteria tests ascending instead of descending.
                    result = !sortCriteria1.isAscending();
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Get an array containing sorted RowKey objects.
     * <p>
     * Note: This sort depends on the SortCriteria objects provided to the
     * TableDataSorter instance used by this component. For better performance,
     * this sort also depends on the FilterCriteria objects provided to the
     * TableDataFilter instance used by this component. Due to filtering, the
     * size of the returned array may be less than the total number of RowKey
     * objects for the underlying TableDataProvider.
     * </p><p>
     * Note: The returned RowKey objects are cached. If the TableDataSorter and
     * TableDataFilter instances used by this component are modified directly,
     * invoke the clearSort and clearFilter methods to clear the previous sort
     * and filter.
     * </p>
     *
     * @return An array containing sorted RowKey objects.
     */
    public RowKey[] getSortedRowKeys() {
        // Initialize RowKey objects, if not cached already.
        if (sortedRowKeys != null) {
            return sortedRowKeys;
        } else {
            sortedRowKeys = getFilteredRowKeys();
        }

        // Do not attempt to sort with a null provider. BasicTableDataSorter
        // throws NullPointerException -- CR #6268451.
        TableDataProvider tableProvider = getTableRowDataProvider().
                getTableDataProvider();
        if (tableProvider == null) {
            log("getSortedRowKeys",
                    "Cannot obtain sorted row keys, TableDataProvider is null");
            return sortedRowKeys;
        }

        // If TableDataSorter and TableDataProvider are the same instance, the
        // sort method is never called. The sort order is assumed to be
        // intrinsic in the row order of the TableDataProvider.
        TableDataSorter tableSorter = getTableDataSorter();
        if (tableProvider != tableSorter) {
            sortedRowKeys = tableSorter.sort(tableProvider, sortedRowKeys);
        }
        return sortedRowKeys;
    }

    /**
     * Get the TableDataSorter object used to sort rows.
     *
     * @return The TableDataSorter object used to sort rows.
     */
    public TableDataSorter getTableDataSorter() {
        // Method is overriden because TableDataSorter is not serializable.
        TableDataSorter tds;
        if (this.tableDataSorter != null) {
            tds = this.tableDataSorter;
        } else {
            ValueExpression vb = getValueExpression("tableDataSorter");
            if (vb != null) {
                tds = (TableDataSorter) vb.getValue(getFacesContext()
                        .getELContext());
            } else {
                tds = null;
            }
        }
        if (tds != null) {
            return tds;
        }

        // Get default sorter.
        if (sorter == null) {
            sorter = new BasicTableDataSorter();
        }
        return sorter;
    }

    /**
     * Set the TableDataSorter object used to sort rows.
     *
     * @param newSorter The TableDataSorter object used to sort rows.
     */
    public void setTableDataSorter(final TableDataSorter newSorter) {
        // Method is overriden because TableDataSorter is not serializable.
        this.sorter = newSorter;
    }

    /**
     * Set SortCriteria objects for the TableDataSorter instance used by this
     * component.
     * <p>
     * Note: This method clears the cached sort, then resets pagination to the
     * first page per UI guidelines.
     * </p>
     *
     * @param newSortCriteria An array of SortCriteria objects defining the sort
     * order on this TableDataSorter.
     */
    public void setSortCriteria(final SortCriteria[] newSortCriteria) {
        clearSort();
        getTableDataSorter().setSortCriteria(newSortCriteria);
    }

    /**
     * This implementation restores the state of all properties.
     * @param context faces context
     * @param state state object
     */
    @Override
    @SuppressWarnings({"unchecked", "checkstyle:magicnumber"})
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) ((Object[]) state)[0];
        super.restoreState(context, values[0]);
        this.aboveColumnFooter = ((Boolean) values[1]);
        this.aboveColumnFooterSet = ((Boolean) values[2]);
        this.aboveColumnHeader = ((Boolean) values[3]);
        this.aboveColumnHeaderSet = ((Boolean) values[4]);
        this.align = (String) values[5];
        this.bgColor = (String) values[6];
        this.charAttr = (String) values[7];
        this.charOff = (String) values[8];
        this.collapsed = ((Boolean) values[9]);
        this.collapsedSet = ((Boolean) values[10]);
        this.emptyDataMsg = (String) values[11];
        this.extraFooterHtml = (String) values[12];
        this.extraHeaderHtml = (String) values[13];
        this.first = ((Integer) values[14]);
        this.firstSet = ((Boolean) values[15]);
        this.footerText = (String) values[16];
        this.groupToggleButton = ((Boolean) values[17]);
        this.groupToggleButtonSet = ((Boolean) values[18]);
        this.headerText = (String) values[19];
        this.multipleColumnFooters = ((Boolean) values[20]);
        this.multipleColumnFootersSet = ((Boolean) values[21]);
        this.multipleTableColumnFooters = ((Boolean) values[22]);
        this.multipleTableColumnFootersSet = ((Boolean) values[23]);
        this.onClick = (String) values[24];
        this.onDblClick = (String) values[25];
        this.onKeyDown = (String) values[26];
        this.onKeyPress = (String) values[27];
        this.onKeyUp = (String) values[28];
        this.onMouseDown = (String) values[29];
        this.onMouseMove = (String) values[30];
        this.onMouseOut = (String) values[31];
        this.onMouseOver = (String) values[32];
        this.onMouseUp = (String) values[33];
        this.rows = ((Integer) values[34]);
        this.rowsSet = ((Boolean) values[35]);
        this.selectMultipleToggleButton = ((Boolean) values[36]);
        this.selectMultipleToggleButtonSet = ((Boolean) values[37]);
        this.selected = ((Boolean) values[38]);
        this.selectedSet = ((Boolean) values[39]);
        this.sourceData = (TableDataProvider) values[40];
        this.sourceVar = (String) values[41];
        this.styleClasses = (String) values[42];
        this.tableDataFilter = (TableDataFilter) values[43];
        this.tableDataSorter = (TableDataSorter) values[44];
        this.toolTip = (String) values[45];
        this.valign = (String) values[46];
        this.visible = ((Boolean) values[47]);
        this.visibleSet = ((Boolean) values[48]);

        Object[] values2 = (Object[]) state;
        values2[0] = values;
        saved = (Map) values2[1];
        setPaginated(((Boolean) values2[2]));

        // Note: When the iterate method is called (during the decode, validate,
        // update phases), the previously displayed sort must be used to iterate
        // over the previously displayed children. If child values have changed
        // (e.g., TableSelectPhaseListener has cleared checkbox state after the
        // rendering phase), a new sort would not represent the same rows and
        // state may be lost. Thus, we must restore the previously sorted RowKey
        // objects.
        // Restore SortCriteria.
        TableDataSorter tableSorter = getTableDataSorter();
        tableSorter.setSortCriteria((SortCriteria[]) values2[3]);

        // Restore FilterCriteria.
        TableDataFilter tableFilter = getTableDataFilter();
        tableFilter.setFilterCriteria((FilterCriteria[]) values2[4]);

        // Restore previously filtered and sorted RowKey objects.
        // Clear filtered row keys.
        filteredRowKeys = (RowKey[]) values2[5];
        // Clear sorted row keys.
        sortedRowKeys = (RowKey[]) values2[6];
    }

    /**
     * This implementation saves the state of all properties.
     * @param context faces context
     * @return Object
     */
    @Override
    @SuppressWarnings({"unchecked", "checkstyle:magicnumber"})
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[49];
        values[0] = super.saveState(context);
        if (this.aboveColumnFooter) {
            values[1] = Boolean.TRUE;
        } else {
            values[1] = Boolean.FALSE;
        }
        if (this.aboveColumnFooterSet) {
            values[2] = Boolean.TRUE;
        } else {
            values[2] = Boolean.FALSE;
        }
        if (this.aboveColumnHeader) {
            values[3] = Boolean.TRUE;
        } else {
            values[3] = Boolean.FALSE;
        }
        if (this.aboveColumnHeaderSet) {
            values[4] = Boolean.TRUE;
        } else {
            values[4] = Boolean.FALSE;
        }
        values[5] = this.align;
        values[6] = this.bgColor;
        values[7] = this.charAttr;
        values[8] = this.charOff;
        if (this.collapsed) {
            values[9] = Boolean.TRUE;
        } else {
            values[9] = Boolean.FALSE;
        }
        if (this.collapsedSet) {
            values[10] = Boolean.TRUE;
        } else {
            values[10] = Boolean.FALSE;
        }
        values[11] = this.emptyDataMsg;
        values[12] = this.extraFooterHtml;
        values[13] = this.extraHeaderHtml;
        values[14] = this.first;
        if (this.firstSet) {
            values[15] = Boolean.TRUE;
        } else {
            values[15] = Boolean.FALSE;
        }
        values[16] = this.footerText;
        if (this.groupToggleButton) {
            values[17] = Boolean.TRUE;
        } else {
            values[17] = Boolean.FALSE;
        }
        if (this.groupToggleButtonSet) {
            values[18] = Boolean.TRUE;
        } else {
            values[18] = Boolean.FALSE;
        }
        values[19] = this.headerText;
        if (this.multipleColumnFooters) {
            values[20] = Boolean.TRUE;
        } else {
            values[20] = Boolean.FALSE;
        }
        if (this.multipleColumnFootersSet) {
            values[21] = Boolean.TRUE;
        } else {
            values[21] = Boolean.FALSE;
        }
        if (this.multipleTableColumnFooters) {
            values[22] = Boolean.TRUE;
        } else {
            values[22] = Boolean.FALSE;
        }
        if (this.multipleTableColumnFootersSet) {
            values[23] = Boolean.TRUE;
        } else {
            values[23] = Boolean.FALSE;
        }
        values[24] = this.onClick;
        values[25] = this.onDblClick;
        values[26] = this.onKeyDown;
        values[27] = this.onKeyPress;
        values[28] = this.onKeyUp;
        values[29] = this.onMouseDown;
        values[30] = this.onMouseMove;
        values[31] = this.onMouseOut;
        values[32] = this.onMouseOver;
        values[33] = this.onMouseUp;
        values[34] = this.rows;
        if (this.rowsSet) {
            values[35] = Boolean.TRUE;
        } else {
            values[35] = Boolean.FALSE;
        }
        if (this.selectMultipleToggleButton) {
            values[36] = Boolean.TRUE;
        } else {
            values[36] = Boolean.FALSE;
        }
        if (this.selectMultipleToggleButtonSet) {
            values[37] = Boolean.TRUE;
        } else {
            values[37] = Boolean.FALSE;
        }
        if (this.selected) {
            values[38] = Boolean.TRUE;
        } else {
            values[38] = Boolean.FALSE;
        }
        if (this.selectedSet) {
            values[39] = Boolean.TRUE;
        } else {
            values[39] = Boolean.FALSE;
        }
        values[40] = this.sourceData;
        values[41] = this.sourceVar;
        values[42] = this.styleClasses;
        values[43] = this.tableDataFilter;
        values[44] = this.tableDataSorter;
        values[45] = this.toolTip;
        values[46] = this.valign;
        if (this.visible) {
            values[47] = Boolean.TRUE;
        } else {
            values[47] = Boolean.FALSE;
        }
        if (this.visibleSet) {
            values[48] = Boolean.TRUE;
        } else {
            values[48] = Boolean.FALSE;
        }
        Object[] values2 = new Object[8];
        values2[0] = values;
        values2[1] = saved;
        if (isPaginated()) {
            values2[2] = Boolean.TRUE;
        } else {
            values2[2] = Boolean.FALSE;
        }
        // Save SortCriteria.
        values2[3] = getTableDataSorter().getSortCriteria();
        // Save FilterCriteria.
        values2[4] = getTableDataFilter().getFilterCriteria();
        // Save filtered RowKey objects.
        values2[5] = getFilteredRowKeys();
        // Save sorted RowKey objects.
        values2[6] = getSortedRowKeys();
        return values2;
    }

    /**
     * Set the ValueExpression used to calculate the value for the specified
     * attribute or property name, if any. In addition, if a ValueExpression is
     * set for the value property, remove any synthesized TableDataProvider for
     * the data previously bound to this component.
     * <p>
     * Note: When ever the underlying DataProvider has changed, UI Guidelines
     * recommend that pagination should be reset (e.g., remaining on the 4th
     * page of a new set of data makes no sense). However, properties such as
     * the sort and filter criteria should not automatically be cleared (e.g.,
     * there may be situations where one or both should be left as specified by
     * the user). In this scenario, pagination is set to the first page.
     * <p>
     * </p> @param name Name of the attribute or property for which to set a
     * ValueExpression.
     * @param valueExpression The ValueExpression to set, or null to remove any
     * currently set ValueExpression.
     *
     * @exception IllegalArgumentException If name is one of sourceVar.
     * @exception NullPointerException If name is null.
     */
    @Override
    public void setValueExpression(final String name,
            final ValueExpression valueExpression) {

        if ("sourceData".equals(name)) {
            // Reset to first page.
            setPage(1);
            // Clear cached properties.
            clear();
        } else if ("sourceVar".equals(name)
                && !valueExpression.isLiteralText()) {
            log("setValueExpression", "sourceVar cannot equal given name");
            throw new IllegalArgumentException();
        }
        super.setValueExpression(name, valueExpression);
    }

    /**
     * Return a client identifier for this component that includes the current
     * value of the RowKey property, if it is not set to null. This implies that
     * multiple calls to getClientId() may return different results, but ensures
     * that child components can themselves generate row-specific client
     * identifiers (since TableRowGroup is a NamingContainer).
     *
     * @exception NullPointerException if FacesContext is null.
     * @return The client id.
     */
    @Override
    public String getClientId(final FacesContext context) {
        if (context == null) {
            log("getClientId", "Cannot obtain client Id, FacesContext is null");
            throw new NullPointerException();
        }

        String baseClientId = super.getClientId(context);
        if (getRowKey() != null) {
            return (baseClientId
                    + UINamingContainer.getSeparatorChar(context)
                    + getRowKey().getRowId());
        } else {
            return (baseClientId);
        }
    }

    /**
     * Override the default UIComponentBase.queueEvent() processing to wrap any
     * queued events in a wrapper so that we can reset the current RowKey in
     * broadcast().
     *
     * @param event FacesEvent to be queued.
     *
     * @exception IllegalStateException If this component is not a descendant of
     * a UIViewRoot.
     * @exception NullPointerException If FacesEvent is null.
     */
    @Override
    public void queueEvent(final FacesEvent event) {
        super.queueEvent(new WrapperEvent(this, event, getRowKey()));
    }

    /**
     * Override the default UIComponentBase.broadcast() processing to unwrap any
     * wrapped FacesEvent and reset the current RowKey, before the event is
     * actually broadcast. For events that we did not wrap (in queueEvent()),
     * default processing will occur.
     *
     * @param event The FacesEvent to be broadcast.
     *
     * @exception AbortProcessingException Signal the JavaServer Faces
     * implementation that no further processing on the current event should be
     * performed.
     * @exception IllegalArgumentException if the implementation class of this
     * FacesEvent is not supported by this component.
     * @exception NullPointerException if FacesEvent is null.
     */
    @Override
    public void broadcast(final FacesEvent event)
            throws AbortProcessingException {

        if (!(event instanceof WrapperEvent)) {
            super.broadcast(event);
            return;
        }

        // Set up the correct context and fire our wrapped event
        WrapperEvent revent = (WrapperEvent) event;
        RowKey oldRowKey = getRowKey();
        setRowKey(revent.getRowKey());
        FacesEvent rowEvent = revent.getFacesEvent();
        rowEvent.getComponent().broadcast(rowEvent);
        setRowKey(oldRowKey);
    }

    /**
     * In addition to the default behavior, ensure that any saved per-row state
     * for our child input components is discarded unless it is needed to
     * re-render the current page with errors.
     *
     * @param context FacesContext for the current request.
     *
     * @exception IOException if an input/output error occurs while rendering.
     * @exception NullPointerException if FacesContext is null.
     */
    @Override
    public void encodeBegin(final FacesContext context) throws IOException {

        // Clear objects cached during the decode, validate, and update phases
        // so nested tables can render new TableDataProvider objects.
        if (isNestedWithinTableRowGroup()) {
            clear(); // Clear cached properties.
        }
        if (!keepSaved(context)) {
            saved = new HashMap<String, SavedState>();
        }
        super.encodeBegin(context);
    }

    /**
     * Override the default UIComponentBase.processDecodes() processing to
     * perform the following steps.
     *
     * <ul>
     * <li>If the rendered property of this UIComponent is false, skip further
     * processing.</li>
     * <li>Set the current RowKey to null.</li>
     * <li>Call the processDecodes() method of all facets of this TableRowGroup,
     * in the order determined by a call to
     * getFacets().keySet().iterator().</li>
     * <li>Call the processDecodes() method of all facets of the TableColumn
     * children of this TableRowGroup.</li>
     * <li>Iterate over the set of rows that were included when this component
     * was rendered (i.e. those defined by the first and rows properties),
     * performing the following processing for each row:</li>
     * <li>Set the current RowKey to the appropriate value for this row.</li>
     * <li>If isRowAvailable() returns true, iterate over the children
     * components of each TableColumn child of this TableRowGroup component,
     * calling the processDecodes() method for each such child.</li>
     * <li>Set the current RowKey to null.</li>
     * <li>Call the decode() method of this component.</li>
     * <li>If a RuntimeException is thrown during decode processing, call
     * FacesContext.renderResponse() and re-throw the exception.</li>
     * </ul>
     *
     * @param context FacesContext for the current request.
     *
     * @exception NullPointerException if FacesContext is null.
     */
    @Override
    public void processDecodes(final FacesContext context) {
        if (context == null) {
            log("processDecodes", "Cannot decode, FacesContext is null");
            throw new NullPointerException();
        }
        if (!isRendered()) {
            log("processDecodes", "Component not rendered, nothing to decode");
            return;
        }
        if (saved == null || !keepSaved(context)) {
            // We don't need saved state here
            saved = new HashMap<String, SavedState>();
        }
        iterate(context, PhaseId.APPLY_REQUEST_VALUES);
        decode(context);
    }

    /**
     * Override the default UIComponentBase.processValidators() processing to
     * perform the following steps.
     *
     * <ul>
     * <li>If the rendered property of this UIComponent is false, skip further
     * processing.</li>
     * <li>Set the current RowKey to null.</li>
     * <li>Call the processValidators() method of all facets of this
     * TableRowGroup, in the order determined by a call to
     * getFacets().keySet().iterator().</li>
     * <li>Call the processValidators() method of all facets of the TableColumn
     * children of this TableRowGroup.</li>
     * <li>Iterate over the set of rows that were included when this component
     * was rendered (i.e. those defined by the first and rows properties),
     * performing the following processing for each row:</li>
     * <li>Set the current RowKey to the appropriate value for this row.</li>
     * <li>If isRowAvailable() returns true, iterate over the children
     * components of each TableColumn child of this TableRowGroup component,
     * calling the processValidators() method for each such child.</li>
     * <li>Set the current RowKey to null.</li>
     * </ul>
     *
     * @param context FacesContext for the current request.
     *
     * @exception NullPointerException if FacesContext is null.
     */
    @Override
    public void processValidators(final FacesContext context) {
        if (context == null) {
            log("processValidators", "Cannot validate, FacesContext is null");
            throw new NullPointerException();
        }
        if (!isRendered()) {
            log("processValidators",
                    "Component not rendered, nothing to validate");
            return;
        }
        iterate(context, PhaseId.PROCESS_VALIDATIONS);
        // This is not a EditableValueHolder, so no further processing is
        // required
    }

    /**
     * Override the default UIComponentBase.processUpdates() processing to
     * perform the following steps.
     *
     * <ul>
     * <li>If the rendered property of this UIComponent is false, skip further
     * processing.</li>
     * <li>Set the current RowKey to null.</li>
     * <li>Call the processUpdates() method of all facets of this TableRowGroup,
     * in the order determined by a call to
     * getFacets().keySet().iterator().</li>
     * <li>Call the processUpdates() method of all facets of the TableColumn
     * children of this TableRowGroup.</li>
     * <li>Iterate over the set of rows that were included when this component
     * was rendered (i.e. those defined by the first and rows properties),
     * performing the following processing for each row:</li>
     * <li>Set the current RowKey to the appropriate value for this row.</li>
     * <li>If isRowAvailable() returns true, iterate over the children
     * components of each TableColumn child of this TableRowGroup component,
     * calling the processUpdates() method for each such child.</li>
     * <li>Set the current RowKey to null.</li>
     * </ul>
     *
     * @param context FacesContext for the current request.
     *
     * @exception NullPointerException if FacesContext is null.
     */
    @Override
    public void processUpdates(final FacesContext context) {
        if (context == null) {
            log("processUpdates", "Cannot update, FacesContext is null");
            throw new NullPointerException();
        }
        if (!isRendered()) {
            log("processUpdates", "Component not rendered, nothing to update");
            return;
        }
        iterate(context, PhaseId.UPDATE_MODEL_VALUES);

        // Set collapsed property applied client-side.
        UIComponent header = getFacet(GROUP_HEADER_ID);
        UIComponent field;
        if (header != null) {
            field =  header.getFacets()
                    .get(TableHeader.COLLAPSED_HIDDEN_FIELD_ID);
        } else {
            field = null;
        }
        if (field instanceof HiddenField) {
            Boolean value = (Boolean) ((HiddenField) field).getText();
            setCollapsed(value);
        } else {
            log("processUpdates", "Cannot obtain collapsed hidden field value");
        }
        // This is not a EditableValueHolder, so no further processing is
        // required
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
     * Set the {@code aboveColumnFooter} attribute to true to display the group
     * footer bar above the column footers bar.The default is to display the
     * group footer below the column footers.
     *
     * @return {@code boolean}
     */
    public boolean isAboveColumnFooter() {
        if (this.aboveColumnFooterSet) {
            return this.aboveColumnFooter;
        }
        ValueExpression vb = getValueExpression("aboveColumnFooter");
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
     * Set the {@code aboveColumnFooter} attribute to true to display the
     * group footer bar above the column footers bar. The default is to display
     * the group footer below the column footers.
     * @param newAboveColumnFooter aboveColumnFooter
     */
    public void setAboveColumnFooter(final boolean newAboveColumnFooter) {
        this.aboveColumnFooter = newAboveColumnFooter;
        this.aboveColumnFooterSet = true;
    }

    /**
     * Set the {@code aboveColumnHeader} attribute to true to display the
     * group header bar above the column headers bar. The default is to display
     * the group header below the column headers.
     * @return {@code boolean}
     */
    public boolean isAboveColumnHeader() {
        if (this.aboveColumnHeaderSet) {
            return this.aboveColumnHeader;
        }
        ValueExpression vb = getValueExpression("aboveColumnHeader");
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
     * Set the {@code aboveColumnHeader} attribute to true to display the
     * group header bar above the column headers bar. The default is to display
     * the group header below the column headers.
     * @param newAboveColumnHeader aboveColumnHeader
     */
    public void setAboveColumnHeader(final boolean newAboveColumnHeader) {
        this.aboveColumnHeader = newAboveColumnHeader;
        this.aboveColumnHeaderSet = true;
    }

    /**
     * Use the {@code align} attribute to specify the horizontal alignment
     * for the content of each cell in the row. Valid values are
     * {@code left}, {@code center}, {@code right},
     * {@code justify}, and {@code char}. The default alignment is
     * {@code left}. Setting the align attribute to {@code char}
     * causes the cell's contents to be aligned on the character that you
     * specify with the {@code char} attribute. For example, to align cell
     * contents on colons, set {@code align="char"} and
     * {@code char=":"}
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
     * for the content of each cell in the row. Valid values are
     * {@code left}, {@code center}, {@code right},
     * {@code justify}, and {@code char}. The default alignment is
     * {@code left}. Setting the align attribute to {@code char}
     * causes the cell's contents to be aligned on the character that you
     * specify with the {@code char} attribute. For example, to align cell
     * contents on colons, set {@code align="char"} and
     * {@code char=":"}
     * @param newAlign align
     */
    public void setAlign(final String newAlign) {
        this.align = newAlign;
    }

    /**
     * The deprecated BGCOLOR attribute suggests a background color for the row.
     * The combination of this attribute with &lt;FONT COLOR=...&gt; can leave
     * invisible or unreadable text on Netscape Navigator 2.x, which does not
     * support BGCOLOR on table elements. BGCOLOR is dangerous even on
     * supporting browsers, since most fail to override it when overriding other
     * author-specified colors. Style sheets provide a safer, more flexible
     * method of specifying a row's background color.
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
     * The deprecated BGCOLOR attribute suggests a background color for the row.
     * The combination of this attribute with &lt;FONT COLOR=...&gt; can leave
     * invisible or unreadable text on Netscape Navigator 2.x, which does not
     * support BGCOLOR on table elements. BGCOLOR is dangerous even on
     * supporting browsers, since most fail to override it when overriding other
     * author-specified colors. Style sheets provide a safer, more flexible
     * method of specifying a row's background color.
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
        if (this.charAttr != null) {
            return this.charAttr;
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
     * @param newCharAttr charAttr
     */
    public void setChar(final String newCharAttr) {
        this.charAttr = newCharAttr;
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
     * Use the collapsed attribute to initially render the group as collapsed,
     * so that the data rows are hidden and only the header row is visible. The
     * default is to show the group expanded.
     * @return {@code boolean}
     */
    public boolean isCollapsed() {
        if (this.collapsedSet) {
            return this.collapsed;
        }
        ValueExpression vb = getValueExpression("collapsed");
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
     * Use the collapsed attribute to initially render the group as collapsed,
     * so that the data rows are hidden and only the header row is visible. The
     * default is to show the group expanded.
     * @param newCollapsed collapsed
     */
    public void setCollapsed(final boolean newCollapsed) {
        this.collapsed = newCollapsed;
        this.collapsedSet = true;
    }

    /**
     * The text to be displayed when the table does not contain data. The text
     * is displayed left-aligned in a single row that contains one cell that
     * spans all columns. The {@code emptyDataMsg} text might be something
     * similar to "No items found." If users can add items to the table, the
     * message might include instructions, such as "This table contains no
     * files. To add a file to monitor, click the New button."
     * @return String
     */
    public String getEmptyDataMsg() {
        if (this.emptyDataMsg != null) {
            return this.emptyDataMsg;
        }
        ValueExpression vb = getValueExpression("emptyDataMsg");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The text to be displayed when the table does not contain data. The text
     * is displayed left-aligned in a single row that contains one cell that
     * spans all columns. The {@code emptyDataMsg} text might be something
     * similar to "No items found." If users can add items to the table, the
     * message might include instructions, such as "This table contains no
     * files. To add a file to monitor, click the New button."
     * @param newEmptyDataMsg emptyDataMsg
     */
    public void setEmptyDataMsg(final String newEmptyDataMsg) {
        this.emptyDataMsg = newEmptyDataMsg;
    }

    /**
     * Extra HTML code to be appended to the {@code &lt;tr&gt;} HTML
     * element that is rendered for the group footer. Use only code that is
     * valid in an HTML {@code &lt;td&gt;} element. The code you specify is
     * inserted in the HTML element, and is not checked for validity. For
     * example, you might set this attribute to {@code "nowrap=`nowrap'"}.
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
     * Extra HTML code to be appended to the {@code &lt;tr&gt;} HTML
     * element that is rendered for the group footer. Use only code that is
     * valid in an HTML {@code &lt;td&gt;} element. The code you specify is
     * inserted in the HTML element, and is not checked for validity. For
     * example, you might set this attribute to {@code "nowrap=`nowrap'"}.
     * @param newExtraFooterHtml extraFooterHtml
     */
    public void setExtraFooterHtml(final String newExtraFooterHtml) {
        this.extraFooterHtml = newExtraFooterHtml;
    }

    /**
     * Extra HTML code to be appended to the {@code &lt;tr&gt;} HTML
     * element that is rendered for the group header. Use only code that is
     * valid in an HTML {@code &lt;td&gt;} element. The code you specify is
     * inserted in the HTML element, and is not checked for validity. For
     * example, you might set this attribute to {@code "nowrap=`nowrap'"}.
     * @return String
     */
    public String getExtraHeaderHtml() {
        if (this.extraHeaderHtml != null) {
            return this.extraHeaderHtml;
        }
        ValueExpression vb = getValueExpression("extraHeaderHtml");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Extra HTML code to be appended to the {@code &lt;tr&gt;} HTML
     * element that is rendered for the group header. Use only code that is
     * valid in an HTML {@code &lt;td&gt;} element. The code you specify is
     * inserted in the HTML element, and is not checked for validity. For
     * example, you might set this attribute to {@code "nowrap=`nowrap'"}.
     * @param newExtraHeaderHtml extraHeaderHtml
     */
    public void setExtraHeaderHtml(final String newExtraHeaderHtml) {
        this.extraHeaderHtml = newExtraHeaderHtml;
    }

    /**
     * The text to be displayed in the group footer.
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
     * The text to be displayed in the group footer.
     * @param newFooterText footerText
     */
    public void setFooterText(final String newFooterText) {
        this.footerText = newFooterText;
    }

    /**
     * Use the {@code groupToggleButton} attribute to display a button in
     * the group header to allow users to collapse and expand the group of rows.
     * @return {@code boolean}
     */
    public boolean isGroupToggleButton() {
        if (this.groupToggleButtonSet) {
            return this.groupToggleButton;
        }
        ValueExpression vb = getValueExpression("groupToggleButton");
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
     * Use the {@code groupToggleButton} attribute to display a button in
     * the group header to allow users to collapse and expand the group of rows.
     * @param newGroupToggleButton groupToggleButton
     */
    public void setGroupToggleButton(final boolean newGroupToggleButton) {
        this.groupToggleButton = newGroupToggleButton;
        this.groupToggleButtonSet = true;
    }

    /**
     * The text to be displayed in the group header.
     * @return String
     */
    public String getHeaderText() {
        if (this.headerText != null) {
            return this.headerText;
        }
        ValueExpression vb = getValueExpression("headerText");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The text to be displayed in the group header.
     * @param newHeaderText headerText
     */
    public void setHeaderText(final String newHeaderText) {
        this.headerText = newHeaderText;
    }

    /**
     * Use the {@code multipleColumnFooters} attribute when the
     * {@code webuijsf:tableRowGroup} contains nested
     * {@code webuijsf:tableColumn} tags, and you want the footers of all
     * the {@code webuijsf:tableColumn} tags to be shown. The default is to
     * show the footers of only the innermost level of nested
     * {@code webuijsf:tableColumn} tags.
     * @return {@code boolean}
     */
    public boolean isMultipleColumnFooters() {
        if (this.multipleColumnFootersSet) {
            return this.multipleColumnFooters;
        }
        ValueExpression vb = getValueExpression("multipleColumnFooters");
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
     * Use the {@code multipleColumnFooters} attribute when the
     * {@code webuijsf:tableRowGroup} contains nested
     * {@code webuijsf:tableColumn} tags, and you want the footers of all
     * the {@code webuijsf:tableColumn} tags to be shown. The default is to
     * show the footers of only the innermost level of nested
     * {@code webuijsf:tableColumn} tags.
     * @param newMultipleColumnFooters multipleColumnFooters
     */
    public void setMultipleColumnFooters(
            final boolean newMultipleColumnFooters) {

        this.multipleColumnFooters = newMultipleColumnFooters;
        this.multipleColumnFootersSet = true;
    }

    /**
     * Use the {@code multipleTableColumnFooters} attribute when the
     * {@code webuijsf:tableRowGroup} contains nested
     * {@code webuijsf:tableColumn} tags, and you want the table footers of
     * all the {@code webuijsf:tableColumn} tags to be shown. The default
     * is to show the table footers of only the innermost level of nested
     * {@code webuijsf:tableColumn} tags.
     * @return {@code boolean}
     */
    public boolean isMultipleTableColumnFooters() {
        if (this.multipleTableColumnFootersSet) {
            return this.multipleTableColumnFooters;
        }
        ValueExpression vb = getValueExpression("multipleTableColumnFooters");
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
     * Use the {@code multipleTableColumnFooters} attribute when the
     * {@code webuijsf:tableRowGroup} contains nested
     * {@code webuijsf:tableColumn} tags, and you want the table footers of
     * all the {@code webuijsf:tableColumn} tags to be shown. The default
     * is to show the table footers of only the innermost level of nested
     * {@code webuijsf:tableColumn} tags.
     * @param newMultipleTableColumnFooters multipleTableColumnFooters
     */
    public void setMultipleTableColumnFooters(
            final boolean newMultipleTableColumnFooters) {

        this.multipleTableColumnFooters = newMultipleTableColumnFooters;
        this.multipleTableColumnFootersSet = true;
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
     * Use the {@code selectMultipleToggleButton} attribute to display a
     * button in the group header to allow users to select all rows of the group
     * at once. The button toggles a column of check boxes using the id that is
     * given to the {@code selectId} attribute of the
     * {@code webuijsf:tableColumn} tag.
     * @return {@code boolean}
     */
    public boolean isSelectMultipleToggleButton() {
        if (this.selectMultipleToggleButtonSet) {
            return this.selectMultipleToggleButton;
        }
        ValueExpression vb = getValueExpression("selectMultipleToggleButton");
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
     * Use the {@code selectMultipleToggleButton} attribute to display a
     * button in the group header to allow users to select all rows of the group
     * at once. The button toggles a column of check boxes using the id that is
     * given to the {@code selectId} attribute of the
     * {@code webuijsf:tableColumn} tag.
     * @param newSelectMultipleToggleButton selectMultipleToggleButton
     */
    public void setSelectMultipleToggleButton(
            final boolean newSelectMultipleToggleButton) {

        this.selectMultipleToggleButton = newSelectMultipleToggleButton;
        this.selectMultipleToggleButtonSet = true;
    }

    /**
     * Flag indicating that the current row is selected. If the value is set to
     * true, the row will appear highlighted.
     * @return {@code boolean}
     */
    public boolean isSelected() {
        if (this.selectedSet) {
            return this.selected;
        }
        ValueExpression vb = getValueExpression("selected");
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
     * Flag indicating that the current row is selected. If the value is set to
     * true, the row will appear highlighted.
     * @param newSelected selected
     */
    public void setSelected(final boolean newSelected) {
        this.selected = newSelected;
        this.selectedSet = true;
    }

    /**
     * The {@code sourceData} attribute is used to specify the data source
     * to populate the table. The value of the {@code sourceData} attribute
     * may be a JavaServer Faces EL expression that resolves to a backing bean
     * of type {@code com.sun.data.provider.TableDataProvider}.
     * <br><br>
     * The sourceData property is referenced during multiple phases of the
     * JavaServer Faces life cycle while iterating over the rows. The object
     * that is bound to this attribute should be cached so that the object is
     * not created more often than needed.
     * @return Object
     */
    public Object getSourceData() {
        if (this.sourceData != null) {
            return this.sourceData;
        }
        ValueExpression vb = getValueExpression("sourceData");
        if (vb != null) {
            return vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Use the {@code sourceVar} attribute to specify the name of the
     * request-scope attribute under which model data for the current row will
     * be exposed when iterating. During iterative processing over the rows of
     * data in the data provider, the TableDataProvider for the current row is
     * exposed as a request attribute under the key specified by this property.
     * Note: This value must be unique for each table in the JSP page.
     * @return String
     */
    public String getSourceVar() {
        if (this.sourceVar != null) {
            return this.sourceVar;
        }
        ValueExpression vb = getValueExpression("sourceVar");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Use the {@code sourceVar} attribute to specify the name of the
     * request-scope attribute under which model data for the current row will
     * be exposed when iterating. During iterative processing over the rows of
     * data in the data provider, the TableDataProvider for the current row is
     * exposed as a request attribute under the key specified by this property.
     * Note: This value must be unique for each table in the JSP page.
     * @param newSourceVar sourceVar
     */
    public void setSourceVar(final String newSourceVar) {
        this.sourceVar = newSourceVar;
    }

    /**
     * Use the {@code styleClasses} attribute to specify a list of CSS
     * style classes to apply to the rows of the group. You can apply all the
     * styles in the list to each row by separating the class names with commas.
     * Each row looks the same when commas are used to delimit the styles. You
     * can apply alternating styles to individual rows by separating the style
     * class names with spaces. You can create a pattern of shading alternate
     * rows, for example, to improve readability of the table. For example, if
     * the list has two elements, the first style class in the list is applied
     * to the first row, the second class to the second row, the first class to
     * the third row, the second class to the fourth row, etc. The tableRowGroup
     * component iterates through the list of styles and repeats from the
     * beginning until a style is applied to each row.
     * @return String
     */
    public String getStyleClasses() {
        if (this.styleClasses != null) {
            return this.styleClasses;
        }
        ValueExpression vb = getValueExpression("styleClasses");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Use the {@code styleClasses} attribute to specify a list of CSS
     * style classes to apply to the rows of the group. You can apply all the
     * styles in the list to each row by separating the class names with commas.
     * Each row looks the same when commas are used to delimit the styles. You
     * can apply alternating styles to individual rows by separating the style
     * class names with spaces. You can create a pattern of shading alternate
     * rows, for example, to improve readability of the table. For example, if
     * the list has two elements, the first style class in the list is applied
     * to the first row, the second class to the second row, the first class to
     * the third row, the second class to the fourth row, etc. The tableRowGroup
     * component iterates through the list of styles and repeats from the
     * beginning until a style is applied to each row.
     * @param newStyleClasses styleClasses
     */
    public void setStyleClasses(final String newStyleClasses) {
        this.styleClasses = newStyleClasses;
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
     * @param newToolTip toolTip
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
     * } causes the first line of each cell's content to be aligned on the
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
     * Helper method to get Theme objects.
     *
     * @return The current theme.
     */
    private Theme getTheme() {
        return ThemeUtilities.getTheme(getFacesContext());
    }

    /**
     * Helper method to determine if this component is nested within another
     * TableRowGroup component.
     *
     * @return true if this component is nested, else false.
     */
    private boolean isNestedWithinTableRowGroup() {
        UIComponent parent = this.getParent();
        while (parent != null) {
            if (parent instanceof TableRowGroup) {
                return true;
            }
            parent = parent.getParent();
        }
        return (false);
    }

    /**
     * Helper method to perform the appropriate phase-specific processing and
     * per-row iteration for the specified phase, as follows:
     *
     * <ul>
     * <li>Set the RowKey property to null, and process the facets of this
     * TableRowGroup component exactly once.</li>
     * <li>Set the RowKey property to null, and process the facets of the
     * TableColumn children of this TableRowGroup component exactly once.</li>
     * <li>Iterate over the relevant rows, based on the first and row
     * properties, and process the children of the TableColumn children of this
     * TableRowGroup component once per row.</li>
     * </ul>
     *
     * @param context FacesContext for the current request.
     * @param phaseId PhaseId of the phase we are currently running.
     */
    private void iterate(final FacesContext context, final PhaseId phaseId) {

        // Note: When the iterate method is called via the processDecode,
        // processValidate, and processUpdate methods), the previously displayed
        // sort must be used to iterate over the previously displayed children.
        // (The previously displayed sort is cached/restored via the
        // save/restoreState methods.) If child values have changed (e.g.,
        // TableSelectPhaseListener has cleared checkbox state after the
        // rendering phase), obtaining a new sort here may not represent the
        // same rows and state may be lost. Thus, don't clear cached properties
        // unless nested.
        if (isNestedWithinTableRowGroup()) {
            // Re-evaluate even with server-side state saving.
            // Clear cached properties.
            clear();
        }

        // Process each facet of this component exactly once.
        setRowKey(null);
        // Get facet keys.
        Iterator facets = getFacets().keySet().iterator();
        while (facets.hasNext()) {
            // Get facet.
            UIComponent facet = (UIComponent) getFacets().get(facets.next());
            if (phaseId == PhaseId.APPLY_REQUEST_VALUES) {
                facet.processDecodes(context);
            } else if (phaseId == PhaseId.PROCESS_VALIDATIONS) {
                facet.processValidators(context);
            } else if (phaseId == PhaseId.UPDATE_MODEL_VALUES) {
                facet.processUpdates(context);
            } else {
                log("iterate",
                        "Cannot process component facets, Invalid phase ID");
                throw new IllegalArgumentException();
            }
        }

        // Process the facet of each TableColumn child exactly once.
        setRowKey(null);
        Iterator kids = getTableColumnChildren();
        while (kids.hasNext()) {
            TableColumn kid = (TableColumn) kids.next();
            if (!kid.isRendered()) {
                log("iterate",
                        "Cannot process TableColumn facets,"
                        + " TableColumn not rendered");
                continue;
            }
            iterateTableColumnFacets(context, kid, phaseId);
        }

        // Get rendered row keys.
        RowKey[] rowKeys = getRenderedRowKeys();
        if (rowKeys == null) {
            log("iterate",
                    "Cannot iterate over TableColumn children,"
                    + " RowKey array is null");
            return;
        }

        // Iterate over the sorted, rendered RowKey objects.
        for (RowKey rowKey : rowKeys) {
            setRowKey(rowKey);
            if (!isRowAvailable()) {
                log("iterate",
                        "Cannot iterate over TableColumn children,"
                        + " row not available");
                break;
            }
            // Perform phase-specific processing as required on the children
            // of the TableColumn (facets have been done a single time with
            // setRowKey(null) already)
            kids = getTableColumnChildren();
            while (kids.hasNext()) {
                TableColumn kid = (TableColumn) kids.next();
                if (!kid.isRendered()) {
                    log("iterate", "Cannot process TableColumn, not rendered");
                    continue;
                }
                for (UIComponent grandkid : kid.getChildren()) {
                    if (!grandkid.isRendered()) {
                        log("iterate",
                                "Cannot process TableColumn child,"
                                + " not rendered");
                        continue;
                    }
                    iterateTableColumnChildren(context, grandkid, phaseId);
                }
            }
        }
        // Clean up after ourselves.
        setRowKey(null);
    }

    /**
     * Helper method to iterate over nested TableColumn facets.
     *
     * @param context FacesContext for the current request.
     * @param component The TableColumn component to be rendered.
     * @param phaseId PhaseId of the phase we are currently running.
     */
    private void iterateTableColumnFacets(final FacesContext context,
            final TableColumn component, final PhaseId phaseId) {

        if (component == null) {
            log("iterateTableColumnFacets",
                    "Cannot iterate over TableColumn facets,"
                    + "TableColumn is null");
            return;
        }

        Iterator kids = component.getTableColumnChildren();
        if (kids.hasNext()) {
            while (kids.hasNext()) {
                TableColumn col = (TableColumn) kids.next();
                iterateTableColumnFacets(context, col, phaseId);
            }
        } else {
            // Get facet keys.
            Iterator facets = component.getFacets().keySet().iterator();
            while (facets.hasNext()) {
                // Get facet.
                UIComponent facet = (UIComponent) component.getFacets()
                        .get(facets.next());
                if (phaseId == PhaseId.APPLY_REQUEST_VALUES) {
                    facet.processDecodes(context);
                } else if (phaseId == PhaseId.PROCESS_VALIDATIONS) {
                    facet.processValidators(context);
                } else if (phaseId == PhaseId.UPDATE_MODEL_VALUES) {
                    facet.processUpdates(context);
                } else {
                    log("iterateTableColumnFacets",
                            "Cannot iterate over TableColumn facets,"
                            + " Invalid phase ID");
                    throw new IllegalArgumentException();
                }
            }
        }
    }

    /**
     * Helper method to iterate over nested TableColumn children.
     *
     * @param context FacesContext for the current request.
     * @param component The TableColumn component to be rendered.
     * @param phaseId PhaseId of the phase we are currently running.
     */
    private void iterateTableColumnChildren(final FacesContext context,
            final UIComponent component, final PhaseId phaseId) {

        if (component == null) {
            log("iterateTableColumnChildren",
                    "Cannot iterate over TableColumn children, UIComponent"
                    + " is null");
            return;
        }

        // Do not process nested TableColumn components so facets will not be
        // decoded for each row of the table.
        if (component instanceof TableColumn) {
            Iterator kids = component.getChildren().iterator();
            if (kids.hasNext()) {
                while (kids.hasNext()) {
                    UIComponent kid = (UIComponent) kids.next();
                    iterateTableColumnChildren(context, kid, phaseId);
                }
            }
        } else {
            if (phaseId == PhaseId.APPLY_REQUEST_VALUES) {
                component.processDecodes(context);
            } else if (phaseId == PhaseId.PROCESS_VALIDATIONS) {
                component.processValidators(context);
            } else if (phaseId == PhaseId.UPDATE_MODEL_VALUES) {
                component.processUpdates(context);
            } else {
                log("iterateTableColumnChildren",
                        "Cannot iterate over TableColumn children,"
                        + " Invalid phase ID");
                throw new IllegalArgumentException();
            }
        }
    }

    /**
     * Helper method to get flag indicating that we need to keep the saved
     * per-child state information. This will be the case if any of the
     * following are true:
     *
     * <ul>
     * <li>Any of the saved state corresponds to components that have messages
     * that must be displayed.</li>
     * <li>This TableRowGroup instance is nested inside of another TableRowGroup
     * instance.</li>
     * </ul>
     *
     * @param context FacesContext for the current request.
     * @return true if state should be saved, else false.
     */
    private boolean keepSaved(final FacesContext context) {
        for (String clientId : saved.keySet()) {
            // Fix for immediate property -- see CR #6269737.
            SavedState state = (SavedState) saved.get(clientId);
            if (state != null && state.getSubmittedValue() != null) {
                return (true);
            }
        }

        // Bug 6377769
        // Check all components within the TableRowGroup not just
        // cached Editable value holders.
        Iterator messages = context.getMessages();
        while (messages.hasNext()) {
            FacesMessage message = (FacesMessage) messages.next();
            if (message.getSeverity().
                    compareTo(FacesMessage.SEVERITY_ERROR) >= 0) {
                return (true);
            }
        }
        return (isNestedWithinTableRowGroup());
    }

    /**
     * Log fine messages.
     * @param method method to log
     * @param message message to log
     */
    private void log(final String method, final String message) {
        // Get class.
        Class clazz = this.getClass();
        if (LogUtil.fineEnabled(clazz)) {
            // Log method name and message.
            LogUtil.fine(clazz, clazz.getName() + "." + method + ": "
                    + message);
        }
    }

    /**
     * Helper method to restore state information for all descendant components,
     * as described for setRowKey().
     */
    private void restoreDescendantState() {
        FacesContext context = getFacesContext();
        Iterator kids = getTableColumnChildren();
        while (kids.hasNext()) {
            TableColumn kid = (TableColumn) kids.next();
            if (!kid.isRendered()) {
                continue;
            }
            restoreDescendantState(kid, context);
        }
    }

    /**
     * Helper method to restore state information for the specified component
     * and its descendants.
     *
     * @param component Component for which to restore state information.
     * @param context FacesContext for the current request.
     */
    private void restoreDescendantState(final UIComponent component,
            final FacesContext context) {

        // Reset the client identifier for this component
        String id = component.getId();
        component.setId(id); // Forces client id to be reset

        // Restore state for this component (if it is a EditableValueHolder)
        if (component instanceof EditableValueHolder) {
            EditableValueHolder input = (EditableValueHolder) component;
            String clientId = component.getClientId(context);
            SavedState state = (SavedState) saved.get(clientId);
            if (state == null) {
                state = new SavedState();
            }
            input.setValue(state.getValue());
            input.setValid(state.isValid());
            input.setSubmittedValue(state.getSubmittedValue());
            // This *must* be set after the call to setValue(), since
            // calling setValue() always resets "localValueSet" to true.
            input.setLocalValueSet(state.isLocalValueSet());

            ConversionUtilities.restoreRenderedValueState(context, component);
        }

        // Restore state for children of this component
        Iterator kids = component.getChildren().iterator();
        while (kids.hasNext()) {
            restoreDescendantState((UIComponent) kids.next(), context);
        }
    }

    /**
     * Helper method to save state information for all descendant components, as
     * described for setRowKey().
     */
    private void saveDescendantState() {
        FacesContext context = getFacesContext();
        Iterator kids = getTableColumnChildren();
        while (kids.hasNext()) {
            TableColumn kid = (TableColumn) kids.next();
            if (!kid.isRendered()) {
                log("saveDescendantState",
                        "Cannot save descendant state,"
                        + " TableColumn not rendered");
                continue;
            }
            saveDescendantState(kid, context);
        }
    }

    /**
     * Helper method to save state information for the specified component and
     * its descendants.
     *
     * @param component Component for which to save state information.
     * @param context FacesContext for the current request.
     */
    private void saveDescendantState(final UIComponent component,
            final FacesContext context) {

        // Save state for this component (if it is a EditableValueHolder)
        if (component instanceof EditableValueHolder) {
            EditableValueHolder input = (EditableValueHolder) component;
            String clientId = component.getClientId(context);
            SavedState state = (SavedState) saved.get(clientId);
            if (state == null) {
                state = new SavedState();
                saved.put(clientId, state);
            }
            state.setValue(input.getLocalValue());
            state.setValid(input.isValid());
            state.setSubmittedValue(input.getSubmittedValue());
            state.setLocalValueSet(input.isLocalValueSet());

            ConversionUtilities.saveRenderedValueState(context, component);
        }

        // Note: Don't bother logging messages here -- too many messages.
        // For example, staticText is not an EditableValueHolder.
        // Save state for children of this component
        Iterator kids = component.getChildren().iterator();
        while (kids.hasNext()) {
            saveDescendantState((UIComponent) kids.next(), context);
        }
    }

    /**
     * Represent saved state information.
     */
    private static final class SavedState implements Serializable {

        /**
         * Serialization UID.
         */
        private static final long serialVersionUID = -5042211238247402956L;

        /**
         * Submitted value.
         */
        private Object submittedValue;

        /**
         * Valid flag.
         */
        private boolean valid = true;

        /**
         * Local value.
         */
        private Object value;

        /**
         * Local value set flag.
         */
        private boolean localValueSet;

        /**
         * Get the submitted value.
         *
         * @return Object
         */
        public Object getSubmittedValue() {
            return (this.submittedValue);
        }

        /**
         * Set the submitted value.
         *
         * @param newSubmittedValue submittedValue
         */
        public void setSubmittedValue(final Object newSubmittedValue) {
            this.submittedValue = newSubmittedValue;
        }

        /**
         * Get the valid flag value.
         *
         * @return {@code boolean}
         */
        public boolean isValid() {
            return (this.valid);
        }

        /**
         * Set the valid flag value.
         *
         * @param newValid valid
         */
        public void setValid(final boolean newValid) {
            this.valid = newValid;
        }

        /**
         * Get the local value.
         *
         * @return Object
         */
        public Object getValue() {
            return (this.value);
        }

        /**
         * Set the local value.
         *
         * @param newValue value
         */
        public void setValue(final Object newValue) {
            this.value = newValue;
        }

        /**
         * Get the local value set flag.
         *
         * @return {@code boolean}
         */
        public boolean isLocalValueSet() {
            return (this.localValueSet);
        }

        /**
         * Set the local value set flag.
         *
         * @param newLocalValueSet localValueSet
         */
        public void setLocalValueSet(final boolean newLocalValueSet) {
            this.localValueSet = newLocalValueSet;
        }

        @Override
        public String toString() {
            return ("submittedValue: " + submittedValue + " value: " + value
                    + " localValueSet: " + localValueSet);
        }
    }

    /**
     * Wrap an event with a RowKey.
     */
    static final class WrapperEvent extends FacesEvent {

        /**
         * Serialization UID.
         */
        private static final long serialVersionUID = 6625734597395780327L;

        /**
         * Wrapped event.
         */
        private final FacesEvent event;

        /**
         * Row key.
         */
        private final RowKey rowKey;

        /**
         * Create a new instance.
         *
         * @param component UI component
         * @param newEvent wrapped event
         * @param newRowKey row key
         */
        WrapperEvent(final UIComponent component, final FacesEvent newEvent,
                final RowKey newRowKey) {

            super(component);
            this.event = newEvent;
            this.rowKey = newRowKey;
        }

        /**
         * Get the wrapped event.
         *
         * @return FacesEvent
         */
        public FacesEvent getFacesEvent() {
            return (this.event);
        }

        /**
         * Get the row key.
         *
         * @return RowKey
         */
        public RowKey getRowKey() {
            return (this.rowKey);
        }

        @Override
        public PhaseId getPhaseId() {
            return (this.event.getPhaseId());
        }

        @Override
        public void setPhaseId(final PhaseId phaseId) {
            this.event.setPhaseId(phaseId);
        }

        @Override
        public boolean isAppropriateListener(final FacesListener listener) {
            return (false);
        }

        @Override
        public void processListener(final FacesListener listener) {
            throw new IllegalStateException();
        }
    }
}
