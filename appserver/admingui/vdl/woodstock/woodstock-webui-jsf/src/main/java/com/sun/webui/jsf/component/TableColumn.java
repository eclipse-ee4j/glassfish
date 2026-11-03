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
import com.sun.data.provider.SortCriteria;
import com.sun.data.provider.impl.FieldIdSortCriteria;
import com.sun.faces.annotation.Component;
import com.sun.faces.annotation.Property;
import com.sun.webui.jsf.faces.ValueBindingSortCriteria;
import com.sun.webui.jsf.theme.ThemeImages;
import com.sun.webui.jsf.util.LogUtil;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.el.ValueExpression;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.context.FacesContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Component that represents a table column.
 * <p>
 * The tableColumn component provides a layout mechanism for displaying columns
 * of data. UI guidelines describe specific behavior that can applied to the
 * rows and columns of data such as sorting, filtering, pagination, selection,
 * and custom user actions. In addition, UI guidelines also define sections of
 * the table that can be used for titles, row group headers, and placement of
 * pre-defined and user defined actions.
 * </p><p>
 * Note: Column headers and footers are rendered by TableRowGroupRenderer. Table
 * column footers are rendered by TableRenderer.
 * </p>
 * <p>
 * <pre>
 * java.util.logging.ConsoleHandler.level = FINE
 * com.sun.web.ui.component.TableColumn.level = FINE
 * </pre></p><p>
 * See TLD docs for more information.
 * </p>
 */
@Component(type = "com.sun.webui.jsf.TableColumn",
        family = "com.sun.webui.jsf.TableColumn",
        displayName = "Column",
        tagName = "tableColumn",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_column")
public class TableColumn extends UIComponentBase implements NamingContainer {

    /**
     * The component id for the column footer.
     */
    public static final String COLUMN_FOOTER_ID = "_columnFooter";

    /**
     * The facet name for the column footer.
     */
    public static final String COLUMN_FOOTER_FACET = "columnFooter";

    /**
     * The component id for the column header.
     */
    public static final String COLUMN_HEADER_ID = "_columnHeader";

    /**
     * The facet name for the column header.
     */
    public static final String COLUMN_HEADER_FACET = "columnHeader";

    /**
     * The facet name for the header area.
     */
    public static final String HEADER_FACET = "header";

    /**
     * The component id for the embedded action separator icon.
     */
    public static final String EMBEDDED_ACTION_SEPARATOR_ICON_ID
            = "_embeddedActionSeparatorIcon";

    /**
     * The facet name for the embedded action separator icon.
     */
    public static final String EMBEDDED_ACTION_SEPARATOR_ICON_FACET
            = "embeddedActionSeparatorIcon";

    /**
     * The component id for the empty cell icon.
     */
    public static final String EMPTY_CELL_ICON_ID = "_emptyCellIcon";

    /**
     * The facet name for the empty cell icon.
     */
    public static final String EMPTY_CELL_ICON_FACET = "emptyCellIcon";

    /**
     * The facet name for the footer area.
     */
    public static final String FOOTER_FACET = "footer";

    /**
     * The component id for the table column footer.
     */
    public static final String TABLE_COLUMN_FOOTER_ID = "_tableColumnFooter";

    /**
     * The facet name for the table column footer.
     */
    public static final String TABLE_COLUMN_FOOTER_FACET = "tableColumnFooter";

    /**
     * The facet name for the table footer area.
     */
    public static final String TABLE_FOOTER_FACET = "tableFooter";

    /**
     * The Table ancestor enclosing this component.
     */
    private Table tableAncestor = null;

    /**
     * The TableColumn ancestor enclosing this component.
     */
    private TableColumn tableColumnAncestor = null;

    /**
     * A List of TableColumn children found for this component.
     */
    private List<TableColumn> tableColumnChildren = null;

    /**
     * The TableRowGroup ancestor enclosing this component.
     */
    private TableRowGroup tableRowGroupAncestor = null;

    /**
     * The number of columns to be rendered.
     */
    private int columnCount = -1;

    /**
     * The number of rows to be rendered for headers and footers.
     */
    private int rowCount = -1;

    /**
     * ABBR gives an abbreviated version of the cell's content. This allows
     * visual browsers to use the short form if space is limited, and non-visual
     * browsers can give a cell's header information in an abbreviated form
     * before rendering each cell.
     */
    @Property(name = "abbr",
            displayName = "Abbreviation for Header Cell",
            isHidden = true,
            isAttribute = false)
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
            displayName = "Horizontal Alignment",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.TableAlignEditor")
            //CHECKSTYLE:ON
    private String align = null;

    /**
     * Use the {@code alignKey} attribute to specify the FieldKey id or
     * FieldKey to be used as an identifier for a specific data element on which
     * to align the table cell data in the column. If {@code alignKey}
     * specifies a FieldKey, the FieldKey is used as is; otherwise, a FieldKey
     * is created using the {@code alignKey} value that you specify.
     * Alignment is based on the object type of the data element. For example,
     * Date and Number objects are aligned "right", Character and String objects
     * are aligned "left", and Boolean objects are aligned "center". All
     * columns, including select columns, are aligned "left" by default. Note
     * that the align property overrides this value.
     */
    @Property(name = "alignKey",
            displayName = "Horizontal Alignment Key",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private Object alignKey = null;

    /**
     * The AXIS attribute provides a method of categorizing cells. The
     * attribute's value is a comma-separated list of category names. See the
     * HTML 4.0 Recommendation's section on categorizing cells for an
     * application of AXIS.
     */
    @Property(name = "axis",
            displayName = "Category of Header Cell",
            category = "Advanced",
            isHidden = true,
            isAttribute = false,
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
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
            displayName = "Cell Background Color",
            isHidden = true,
            isAttribute = false)
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
            displayName = "Alignment Character",
            isHidden = true,
            isAttribute = false)
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
            displayName = "Alignment Character Offset",
            isHidden = true,
            isAttribute = false)
    private String charOff = null;

    /**
     * The COLSPAN attribute of TD specifies the number of columns that are
     * spanned by the cell. The default value is 1. The special value 0
     * indicates that the cell spans all columns to the end of the table. The
     * value 0 is ignored by most browsers, so authors may wish to calculate the
     * exact number of rows or columns spanned and use that value.
     */
    @Property(name = "colSpan",
            displayName = "Columns Spanned By the Cell",
            category = "Layout",
            isAttribute = false,
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    private int colSpan = Integer.MIN_VALUE;

    /**
     * colSpan set flag.
     */
    private boolean colSpanSet = false;

    /**
     * Use the {@code descending} attribute to specify that the first
     * user-applied sort is descending. By default, the first time a user clicks
     * a column's sort button or column header, the sort is ascending. Note that
     * this not an initial sort. The data is initially displayed unsorted.
     */
    @Property(name = "descending",
            displayName = "Is Descending",
            category = "Data")
    private boolean descending = false;

    /**
     * descending set flag.
     */
    private boolean descendingSet = false;

    /**
     * Set the {@code embeddedActions} attribute to true when the column
     * includes more than one embedded action. This attribute causes a separator
     * image to be displayed between the action links. This attribute is
     * overridden by the {@code emptyCell} attribute.
     */
    @Property(name = "embeddedActions",
            displayName = "Is Embedded Actions",
            category = "Advanced")
    private boolean embeddedActions = false;

    /**
     * embeddedActions set flag.
     */
    private boolean embeddedActionsSet = false;

    /**
     * Use the {@code emptyCell} attribute to cause a theme-specific image
     * to be displayed when the content of a table cell is not applicable or is
     * unexpectedly empty. You should not use this attribute for a value that is
     * truly null, such as an empty alarm cell or a comment field that is blank.
     * In addition, the image should not be used for cells that contain user
     * interface elements such as checkboxes or drop-down lists when these
     * elements are not applicable. Instead, the elements should simply not be
     * displayed so the cell is left empty.
     */
    @Property(name = "emptyCell",
            displayName = "Empty Cell",
            category = "Appearance")
    private boolean emptyCell = false;

    /**
     * emptyCell set flag.
     */
    private boolean emptyCellSet = false;

    /**
     * Extra HTML code to be appended to the {@code &lt;td&gt;} HTML
     * element that is rendered for the column footer. Use only code that is
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
     * Extra HTML code to be appended to the {@code &lt;th&gt;} HTML
     * element that is rendered for the column header. Use only code that is
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
     * Extra HTML code to be appended to the {@code &lt;td&gt;} HTML
     * element that is rendered for the table column footer. Use only code that
     * is valid in an HTML {@code &lt;td&gt;} element. The code you specify
     * is inserted in the HTML element, and is not checked for validity. For
     * example, you might set this attribute to {@code "nowrap=`nowrap'"}.
     */
    @Property(name = "extraTableFooterHtml",
            displayName = "Extra Table Footer HTML",
            category = "Advanced",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String extraTableFooterHtml = null;

    /**
     * The text to be displayed in the column footer.
     */
    @Property(name = "footerText",
            displayName = "Footer Text",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String footerText = null;

    /**
     * The text to be displayed in the column header.
     */
    @Property(name = "headerText",
            displayName = "header Text",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String headerText = null;

    /**
     * The HEADERS attribute specifies the header cells that apply to the TD.
     * The value is a space-separated list of the header cells' ID attribute
     * values. The HEADERS attribute allows non-visual browsers to render the
     * header information for a given cell.
     */
    @Property(name = "headers",
            displayName = "List of Header Cells for Current Cell",
            category = "Advanced",
            isHidden = true,
            isAttribute = false,
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String headers = null;

    /**
     * The number of pixels for the cell's height. Styles should be used to
     * specify cell height when possible because the height attribute is
     * deprecated in HTML 4.0.
     */
    @Property(name = "height",
            displayName = "Height",
            category = "Layout",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String height = null;

    /**
     * Use the {@code noWrap} attribute to disable word wrapping of this
     * column's cells in visual browsers. Word wrap can cause unnecessary
     * horizontal scrolling when the browser window is small in relation to the
     * font size. Styles should be used to disable word wrap when possible
     * because the nowrap attribute is deprecated in HTML 4.0.
     */
    @Property(name = "noWrap",
            displayName = "Suppress Word Wrap",
            category = "Appearance")
    private boolean noWrap = false;

    /**
     * noWrap set flag.
     */
    private boolean noWrapSet = false;

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
     * Use the {@code rowHeader} attribute to specify that the cells of the
     * column are acting as row headers. Row headers are cells that "label" the
     * row. For example, consider a table where the first column contains
     * checkboxes, and the second column contains user names. The third and
     * subsequent columns contain attributes of those users. The content of the
     * cells in the user name column are acting as row headers. The
     * {@code webuijsf:tableColumn} tag for the user name column should set
     * the {@code rowHeader} attribute to true. If a table contains, for
     * example, a system log with time stamp and log entry columns, neither
     * column is acting as a row header, so the {@code rowHeader} attribute
     * should not be set.
     * <p>
     * By default, most column cells are rendered by the table component with
     * HTML {@code &lt;td scope="col"&gt;} elements. The exceptions are
     * columns that contain checkboxes or radio buttons and spacer columns, all
     * of which are rendered as {@code &lt;td&gt;} elements without a scope
     * property.
     * </p><p>
     * When you set the {@code rowHeader} attribute, the column cells are
     * rendered as {@code &lt;th scope="row"&gt;} elements, which enables
     * adaptive technologies such as screen readers to properly read the table
     * to indicate that the contents of these cells are headers for the
     * rows.</p>
     */
    @Property(name = "rowHeader",
            displayName = "Row Header",
            category = "Advanced")
    private boolean rowHeader = false;

    /**
     * rowHeader set flag.
     */
    private boolean rowHeaderSet = false;

    /**
     * The ROWSPAN attribute of TD specifies the number of rows that are spanned
     * by the cell. The default value is 1. The special value 0 indicates that
     * the cell spans all rows to the end of the table. The value 0 is ignored
     * by most browsers, so authors may wish to calculate the exact number of
     * rows or columns spanned and use that value.
     */
    @Property(name = "rowSpan",
            displayName = "Rows Spanned By the Cell",
            category = "Layout",
            isAttribute = false,
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
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
            displayName = "Cells Covered By Header Cell",
            category = "Advanced",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String scope = null;

    /**
     * Use the {@code selectId} attribute in select columns, which contain
     * checkboxes or radio buttons for selecting table rows. The value of
     * {@code selectId} must match the {@code id} attribute of the
     * checkbox or radioButton component that is a child of the tableColumn
     * component. A fully qualified ID based on the tableColumn component ID and
     * the {@code selectId} for the current row will be dynamically created
     * for the {@code &lt;input&gt;} element that is rendered for the
     * checkbox or radio button. The {@code selectId} is required for
     * functionality that supports the toggle buttons for selecting rows. The
     * {@code selectId} also identifies the column as a select column, for
     * which the table component uses different CSS styles.
     */
    @Property(name = "selectId",
            displayName = "Select Component Id",
            category = "Data",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String selectId = null;

    /**
     * Use the {@code severity} attribute when including the
     * {@code webuijsf:alarm} component in a column, to match the severity
     * of the alarm. Valid values are described in the
     * {@code webuijsf:alarm} documentation. When the {@code severity}
     * attribute is set in the tableColumn, the table component renders sort
     * tool tips to indicate that the column will be sorted least/most severe
     * first, and the table cell appears hightlighted according to the level of
     * severity. This functionality is overridden by the {@code emptyCell}
     * attribute.
     */
    @Property(name = "severity",
            displayName = "Severity",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String severity = null;

    /**
     * Use the {@code sort} attribute to specify a FieldKey id or
     * SortCriteria that defines the criteria to use for sorting the contents of
     * a TableDataProvider. If SortCriteria is provided, the object is used for
     * sorting as is. If an id is provided, a FieldIdSortCriteria is created for
     * sorting. In addition, a value binding can also be used to sort on an
     * object that is external to TableDataProvider, such as the selected state
     * of a checkbox or radio button. When a value binding is used, a
     * ValueBindingSortCriteria object is created for sorting. All sorting is
     * based on the object type associated with the data element (for example,
     * Boolean, Character, Comparator, Date, Number, and String). If the object
     * type cannot be determined, the object is compared as a String. The
     * {@code sort} attribute is required for a column to be shown as
     * sortable.
     */
    @Property(name = "sort",
            displayName = "Sort Key",
            category = "Data",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private Object sort = null;

    /**
     * The theme identifier to use for the sort button that is displayed in the
     * column header. Use this attribute to override the default image.
     */
    @Property(name = "sortIcon",
            displayName = "Sort Icon",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.ThemeIconsEditor")
            //CHECKSTYLE:ON
    private String sortIcon = null;

    /**
     * Absolute or relative URL to the image used for the sort button that is
     * displayed in the column header.
     */
    @Property(name = "sortImageURL",
            displayName = "Sort Image URL",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.ImageUrlPropertyEditor")
            //CHECKSTYLE:ON
    private String sortImageURL = null;

    /**
     * Use the {@code spacerColumn} attribute to use the column as a blank
     * column to enhance spacing in two or three column tables. When the
     * {@code spacerColumn} attribute is true, the CSS styles applied to
     * the column make it appear as if the columns are justified. If a column
     * header and footer are required, provide an empty string for the
     * {@code headerText} and {@code footerText} attributes. Set the
     * {@code width} attribute to justify columns accordingly.
     */
    @Property(name = "spacerColumn",
            displayName = "Spacer Column",
            category = "Layout")
    private boolean spacerColumn = false;

    /**
     * spacerColumn set flag.
     */
    private boolean spacerColumnSet = false;

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
     * The text to be displayed in the table column footer. The table column
     * footer is displayed once per table, and is especially useful in tables
     * with multiple groups of rows.
     */
    @Property(name = "tableFooterText",
            displayName = "Table Footer Text",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String tableFooterText = null;

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
            displayName = "Vertical Position",
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
     * Use the {@code width} attribute to specify the width of the cells of
     * the column. The width can be specified as the number of pixels or the
     * percentage of the table width, and is especially useful for spacer
     * columns. This attribute is deprecated in HTML 4.0 in favor of style
     * sheets.
     */
    @Property(name = "width",
            displayName = "Width",
            category = "Layout",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String width = null;

    /**
     * Default constructor.
     */
    public TableColumn() {
        super();
        setRendererType("com.sun.webui.jsf.TableColumn");
    }

    /**
     * This implementation returns {@code "com.sun.webui.jsf.TableColumn"}.
     * @return String
     */
    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.TableColumn";
    }

    /**
     * Clear cached properties.
     * <p>
     * Note: Properties may have been cached via the apply request values,
     * validate, and update phases and must be re-evaluated during the render
     * response phase. For example, the underlying DataProvider may have changed
     * and TableRenderer may need new calculations for the title and action bar.
     * </p><p>
     * Note: Properties of nested TableColumn children shall be cleared as well.
     * </p>
     */
    public void clear() {
        tableAncestor = null;
        tableColumnAncestor = null;
        tableColumnChildren = null;
        tableRowGroupAncestor = null;
        columnCount = -1;
        rowCount = -1;

        // Clear properties of nested TableColumn children.
        Iterator kids = getTableColumnChildren();
        while (kids.hasNext()) {
            TableColumn kid = (TableColumn) kids.next();
            // Clear cached properties.
            kid.clear();
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
     * Get the closest TableColumn ancestor that encloses this component.
     *
     * @return The TableColumn ancestor.
     */
    public TableColumn getTableColumnAncestor() {
        // Get TableColumn ancestor.
        if (tableColumnAncestor == null) {
            UIComponent component = this;
            while (component != null) {
                component = component.getParent();
                if (component instanceof TableColumn) {
                    tableColumnAncestor = (TableColumn) component;
                    break;
                }
            }
        }
        return tableColumnAncestor;
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
            for (UIComponent kid : getChildren()) {
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
            columnCount = getColumnCount(this);
        }
        return columnCount;
    }

    /**
     * Get the closest TableRowGroup ancestor that encloses this component.
     *
     * @return The TableRowGroup ancestor.
     */
    public TableRowGroup getTableRowGroupAncestor() {
        // Get TableRowGroup ancestor.
        if (tableRowGroupAncestor == null) {
            UIComponent component = this;
            while (component != null) {
                component = component.getParent();
                if (component instanceof TableRowGroup) {
                    tableRowGroupAncestor = (TableRowGroup) component;
                    break;
                }
            }
        }
        return tableRowGroupAncestor;
    }

    /**
     * Get the horizontal alignment for the cell.
     * <p>
     * Note: If the align property is specified, it is returned as is. However,
     * if the alignKey property is provided, alignment is based on the object
     * type of the data element. For example, Date and Number objects are
     * aligned using "right", Character and String objects are aligned using
     * "left", and Boolean objects are aligned using "center". Note that select
     * columns are aligned using "center" by default.
     * </p>
     *
     * @return The horizontal alignment for the cell. If the align property is
     * null or the object type cannot be determined, "left" is returned by
     * default.
     */
    public String getAlign() {
        // Note: The align property overrides alignKey.
         // FIX: Merge gen code with method of same name
        if (this.align != null) {
            return this.align;
        }
        String result;
        ValueExpression vb = getValueExpression("align");
        if (vb != null) {
            result = (String) vb.getValue(getFacesContext().getELContext());
            if (result != null) {
                return result;
            }
        }

        // Get alignment.
        Class type = getType();

        if (type != null
                && (Character.class.isAssignableFrom(type)
                || String.class.isAssignableFrom(type))) {
            result = "left";
        } else if (type != null
                && (Date.class.isAssignableFrom(type)
                || Number.class.isAssignableFrom(type))) {
            result = "right";
        } else if (type != null && Boolean.class.isAssignableFrom(type)) {
            result = "center";
        } else {
            // Note: Select columns also default to "left".
            result = "left";
        }
        return result;
    }

    /**
     * Get column footer.
     *
     * @return The column footer.
     */
    public UIComponent getColumnFooter() {
        UIComponent facet = getFacet(COLUMN_FOOTER_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        TableFooter child = new TableFooter();
        child.setId(COLUMN_FOOTER_ID);
        child.setAlign(getAlign());
        child.setExtraHtml(getExtraFooterHtml());
        child.setVisible(isVisible());

        // Set rendered.
        if (!(facet != null
                && facet.isRendered() || isColumnFooterRendered())) {
            // Note: Footer may be initialized to force rendering. This allows
            // developers to omit the footerText property for select columns.
            child.setRendered(false);
        } else {
            log("getColumnFooter",
                    "Column footer not rendered, nothing to display");
        }

        // If only showing one level, don't set colspan or rowspan.
        TableRowGroup group = getTableRowGroupAncestor();
        if (group != null && group.isMultipleColumnFooters()) {
            // Set colspan for nested TableColumn children, else rowspan.
            Iterator kids = getTableColumnChildren();
            if (kids.hasNext()) {
                int colspan = getColumnCount();
                if (colspan > 1) {
                    child.setColSpan(colspan);
                }
            } else {
                int rowspan = getRowCount();
                if (rowspan > 1) {
                    child.setRowSpan(rowspan);
                }
            }
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get column header.
     *
     * @return The column header.
     */
    public UIComponent getColumnHeader() {
        UIComponent facet = getFacet(COLUMN_HEADER_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        TableHeader child = new TableHeader();
        child.setId(COLUMN_HEADER_ID);
        child.setScope("col");
        child.setAlign(getAlign());
        if (getSelectId() != null) {
            child.setWidth("3%");
        } else {
            child.setWidth(null);
        }
        if (getSelectId() != null) {
            child.setNoWrap(true);
        } else {
            child.setNoWrap(false);
        }
        child.setExtraHtml(getExtraHeaderHtml());
        child.setVisible(isVisible());

        // Set type of header to render.
        boolean emptyTable = isEmptyTable();
        SortCriteria criteria = getSortCriteria();
        if (criteria != null && getSelectId() != null && !emptyTable) {
            child.setSelectHeader(true);
        } else if (criteria != null && getHeaderText() != null && !emptyTable) {
            child.setSortHeader(true);
        } else {
            log("getColumnHeader",
                    "Render default column header,"
                            + " no SortCriteria or selectId");
        }

        // Set rendered.
        if (!(facet != null
                && facet.isRendered() || isColumnHeaderRendered())) {
            // Note: Footer may be initialized to force rendering. This allows
            // developers to omit the headerText property for select columns.
            log("getColumnHeader",
                    "Column header not rendered, nothing to display");
            child.setRendered(false);
        }

        // Set colspan for nested TableColumn children, else rowspan.
        Iterator kids = getTableColumnChildren();
        if (kids.hasNext()) {
            int colspan = getColumnCount();
            if (colspan > 1) {
                child.setColSpan(colspan);
            }
        } else {
            int rowspan = getRowCount();
            if (rowspan > 1) {
                child.setRowSpan(rowspan);
            }
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get table column footer.
     *
     * @return The table column footer.
     */
    public UIComponent getTableColumnFooter() {
        UIComponent facet = getFacet(TABLE_COLUMN_FOOTER_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        TableFooter child = new TableFooter();
        child.setId(TABLE_COLUMN_FOOTER_ID);
        child.setAlign(getAlign());
        child.setExtraHtml(getExtraTableFooterHtml());
        child.setTableColumnFooter(true);
        child.setVisible(isVisible());

        // Set rendered.
        if (!(facet != null
                && facet.isRendered() || isTableColumnFooterRendered())) {
            // Note: Footer may be initialized to force rendering. This allows
            // developers to omit the tableFooterText property for select
            // columns.
            child.setRendered(false);
        } else {
            log("getTableColumnFooter",
                    "Table column footer not rendered, nothing to display");
        }

        // If only showing one level, don't set colspan or rowspan.
        TableRowGroup group = getTableRowGroupAncestor();
        if (group != null && group.isMultipleTableColumnFooters()) {
            // Set colspan for nested TableColumn children, else rowspan.
            Iterator kids = getTableColumnChildren();
            if (kids.hasNext()) {
                int colspan = getColumnCount();
                if (colspan > 1) {
                    child.setColSpan(colspan);
                }
            } else {
                int rowspan = getRowCount();
                if (rowspan > 1) {
                    child.setRowSpan(rowspan);
                }
            }
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get the icon used to display inapplicable or unexpectedly empty cells.
     * <p>
     * Note: UI guidelines suggest not to use this for a value that is truly
     * null, such as an empty alarm cell or a comment field which is blank,
     * neither of which should have the dash image. Further, it is recommended
     * not to use the dash image for cells that contain user interface elements
     * such as check boxes or drop-down lists when these elements are not
     * applicable. Instead, simply do not display the user interface element.
     * </p>
     *
     * @return The icon used to display empty cells.
     */
    public UIComponent getEmptyCellIcon() {
        UIComponent facet = getFacet(EMPTY_CELL_ICON_FACET);
        if (facet != null) {
            return facet;
        }

        Theme theme = getTheme();

        // Get child.
        Icon child = ThemeUtilities.getIcon(theme,
                ThemeImages.TABLE_EMPTY_CELL);
        child.setId(EMPTY_CELL_ICON_ID);
        child.setBorder(0);

        // Set tool tip.
        String zToolTip = theme.getMessage("table.emptyTableCell");
        child.setToolTip(zToolTip);
        child.setAlt(zToolTip);

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get separator icon for embedded actions.
     *
     * @return The separator icon for embedded actions.
     */
    public UIComponent getEmbeddedActionSeparatorIcon() {
        UIComponent facet = getFacet(EMBEDDED_ACTION_SEPARATOR_ICON_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        Icon child = ThemeUtilities.getIcon(getTheme(),
                ThemeImages.TABLE_EMBEDDED_ACTIONS_SEPARATOR);
        child.setId(EMBEDDED_ACTION_SEPARATOR_ICON_ID);
        child.setBorder(0);
        child.setAlign("top");

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get SortCriteria used for sorting the contents of a TableDataProvider.
     * <p>
     * Note: If the sortKey attribute resolves to a SortCriteria object, it is
     * returned as is. However, if there is a value binding, and it's not null,
     * a ValueBindingSortCriteria object is created. If there is no value
     * binding, a FieldIdSortCriteria object is created.
     * </p>
     *
     * @return The SortCriteria used for sorting.
     */
    public SortCriteria getSortCriteria() {
        // Return if value binding resolves to a SortCriteria object.
        Object key = getSort();
        if (key instanceof SortCriteria) {
            return (SortCriteria) key;
        }

        SortCriteria result = null;
        ValueExpression vb = getValueExpression("sort");
        if (vb != null) {
            // Note: Constructor accepts ascending param.
            ValueBindingSortCriteria vbsc =
                    new ValueBindingSortCriteria(vb, !isDescending());
            TableRowGroup group = getTableRowGroupAncestor();
            if (group != null) {
                vbsc.setRequestMapKey(group.getSourceVar());
            }
            result = vbsc;
        } else if (key != null) {
            result = new FieldIdSortCriteria(key.toString(), !isDescending());
        }
        return result;
    }

    /**
     * Get sort tool tip augment based on the value given to the align property
     * of the tableColumn component.
     *
     * @param desc Flag indicating descending sort.
     * @return The sort tool tip augment.
     */
    public String getSortToolTipAugment(final boolean desc) {
        String result;

        // To do: Test for toolTip property? The alarm or other custom
        // components may need to set the tooltip. If so, do we need both
        // ascending and descending tooltips?
        // Get object type.
        Class type = getType();

        // Get tooltip.
        ValueExpression vb = getValueExpression("severity");
        if (getSeverity() != null || vb != null) {
            if (desc) {
                result = "table.sort.augment.alarmDescending";
            } else {
                result = "table.sort.augment.alarmAscending";
            }
        } else if (getSelectId() != null
                || (type != null && type.equals(Boolean.class))) {
            if (desc) {
                result = "table.sort.augment.booleanDescending";
            } else {
                result = "table.sort.augment.booleanAscending";
            }
        } else if (type != null && type.equals(String.class)) {
            if (desc) {
                result = "table.sort.augment.stringDescending";
            } else {
                result = "table.sort.augment.stringAscending";
            }
        } else if (type != null && type.equals(Character.class)) {
            if (desc) {
                result = "table.sort.augment.charDescending";
            } else {
                result = "table.sort.augment.charAscending";
            }
        } else if (type != null && type.equals(Date.class)) {
            if (desc) {
                result = "table.sort.augment.dateDescending";
            } else {
                result = "table.sort.augment.dateAscending";
            }
        } else if (type != null && type.equals(Number.class)) {
            if (desc) {
                result = "table.sort.augment.numericDescending";
            } else {
                result = "table.sort.augment.numericAscending";
            }
        } else {
            if (desc) {
                result = "table.sort.augment.undeterminedDescending";
            } else {
                result = "table.sort.augment.undeterminedAscending";
            }
        }
        return getTheme().getMessage(result);
    }

    /**
     * The component identifier for this component. This value must be unique
     * within the closest parent component that is a naming container.
     * This implementation invokes {@code super.setId}.
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
     * This implementation invokes {@code super.Rendered}.
     */
    @Property(name = "rendered")
    @Override
    public void setRendered(final boolean rendered) {
        super.setRendered(rendered);
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
     * @param newAlign align
     */
    public void setAlign(final String newAlign) {
        this.align = newAlign;
    }

    /**
     * Use the {@code alignKey} attribute to specify the FieldKey id or
     * FieldKey to be used as an identifier for a specific data element on which
     * to align the table cell data in the column. If {@code alignKey}
     * specifies a FieldKey, the FieldKey is used as is; otherwise, a FieldKey
     * is created using the {@code alignKey} value that you specify.
     * Alignment is based on the object type of the data element. For example,
     * Date and Number objects are aligned "right", Character and String objects
     * are aligned "left", and Boolean objects are aligned "center". All
     * columns, including select columns, are aligned "left" by default. Note
     * that the align property overrides this value.
     * @return Object
     */
    public Object getAlignKey() {
        if (this.alignKey != null) {
            return this.alignKey;
        }
        ValueExpression vb = getValueExpression("alignKey");
        if (vb != null) {
            return (Object) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Use the {@code alignKey} attribute to specify the FieldKey id or
     * FieldKey to be used as an identifier for a specific data element on which
     * to align the table cell data in the column. If {@code alignKey}
     * specifies a FieldKey, the FieldKey is used as is; otherwise, a FieldKey
     * is created using the {@code alignKey} value that you specify.
     * Alignment is based on the object type of the data element. For example,
     * Date and Number objects are aligned "right", Character and String objects
     * are aligned "left", and Boolean objects are aligned "center". All
     * columns, including select columns, are aligned "left" by default. Note
     * that the align property overrides this value.
     * @param newAlignKey alignKey
     */
    public void setAlignKey(final Object newAlignKey) {
        this.alignKey = newAlignKey;
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
     * The COLSPAN attribute of TD specifies the number of columns that are
     * spanned by the cell. The default value is 1. The special value 0
     * indicates that the cell spans all columns to the end of the table. The
     * value 0 is ignored by most browsers, so authors may wish to calculate the
     * exact number of rows or columns spanned and use that value.
     * @return String
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
     * Use the {@code descending} attribute to specify that the first
     * user-applied sort is descending. By default, the first time a user clicks
     * a column's sort button or column header, the sort is ascending. Note that
     * this not an initial sort. The data is initially displayed unsorted.
     * @return {@code boolean}
     */
    public boolean isDescending() {
        if (this.descendingSet) {
            return this.descending;
        }
        ValueExpression vb = getValueExpression("descending");
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
     * Use the {@code descending} attribute to specify that the first
     * user-applied sort is descending. By default, the first time a user clicks
     * a column's sort button or column header, the sort is ascending. Note that
     * this not an initial sort. The data is initially displayed unsorted.
     * @param newDescending descending
     */
    public void setDescending(final boolean newDescending) {
        this.descending = newDescending;
        this.descendingSet = true;
    }

    /**
     * Set the {@code embeddedActions} attribute to true when the column
     * includes more than one embedded action. This attribute causes a separator
     * image to be displayed between the action links. This attribute is
     * overridden by the {@code emptyCell} attribute.
     * @return {@code boolean}
     */
    public boolean isEmbeddedActions() {
        if (this.embeddedActionsSet) {
            return this.embeddedActions;
        }
        ValueExpression vb = getValueExpression("embeddedActions");
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
     * Set the {@code embeddedActions} attribute to true when the column
     * includes more than one embedded action. This attribute causes a separator
     * image to be displayed between the action links. This attribute is
     * overridden by the {@code emptyCell} attribute.
     * @param newEmbeddedActions embeddedActions
     */
    public void setEmbeddedActions(final boolean newEmbeddedActions) {
        this.embeddedActions = newEmbeddedActions;
        this.embeddedActionsSet = true;
    }

    /**
     * Use the {@code emptyCell} attribute to cause a theme-specific image
     * to be displayed when the content of a table cell is not applicable or is
     * unexpectedly empty. You should not use this attribute for a value that is
     * truly null, such as an empty alarm cell or a comment field that is blank.
     * In addition, the image should not be used for cells that contain user
     * interface elements such as check boxes or drop-down lists when these
     * elements are not applicable. Instead, the elements should simply not be
     * displayed so the cell is left empty.
     * @return {@code boolean}
     */
    public boolean isEmptyCell() {
        if (this.emptyCellSet) {
            return this.emptyCell;
        }
        ValueExpression vb = getValueExpression("emptyCell");
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
     * Use the {@code emptyCell} attribute to cause a theme-specific image
     * to be displayed when the content of a table cell is not applicable or is
     * unexpectedly empty. You should not use this attribute for a value that is
     * truly null, such as an empty alarm cell or a comment field that is blank.
     * In addition, the image should not be used for cells that contain user
     * interface elements such as check boxes or drop-down lists when these
     * elements are not applicable. Instead, the elements should simply not be
     * displayed so the cell is left empty.
     * @param newEmptyCell emptyCell
     */
    public void setEmptyCell(final boolean newEmptyCell) {
        this.emptyCell = newEmptyCell;
        this.emptyCellSet = true;
    }

    /**
     * Extra HTML code to be appended to the {@code &lt;td&gt;} HTML
     * element that is rendered for the column footer. Use only code that is
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
     * Extra HTML code to be appended to the {@code &lt;td&gt;} HTML
     * element that is rendered for the column footer. Use only code that is
     * valid in an HTML {@code &lt;td&gt;} element. The code you specify is
     * inserted in the HTML element, and is not checked for validity. For
     * example, you might set this attribute to {@code "nowrap=`nowrap'"}.
     * @param newExtraFooterHtml extraFooterHtml
     */
    public void setExtraFooterHtml(final String newExtraFooterHtml) {
        this.extraFooterHtml = newExtraFooterHtml;
    }

    /**
     * Extra HTML code to be appended to the {@code &lt;th&gt;} HTML
     * element that is rendered for the column header. Use only code that is
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
     * Extra HTML code to be appended to the {@code &lt;th&gt;} HTML
     * element that is rendered for the column header. Use only code that is
     * valid in an HTML {@code &lt;td&gt;} element. The code you specify is
     * inserted in the HTML element, and is not checked for validity. For
     * example, you might set this attribute to {@code "nowrap=`nowrap'"}.
     * @param newExtraHeaderHtml extraHeaderHtml
     */
    public void setExtraHeaderHtml(final String newExtraHeaderHtml) {
        this.extraHeaderHtml = newExtraHeaderHtml;
    }

    /**
     * Extra HTML code to be appended to the {@code &lt;td&gt;} HTML
     * element that is rendered for the table column footer. Use only code that
     * is valid in an HTML {@code &lt;td&gt;} element. The code you specify
     * is inserted in the HTML element, and is not checked for validity. For
     * example, you might set this attribute to {@code "nowrap=`nowrap'"}.
     * @return String
     */
    public String getExtraTableFooterHtml() {
        if (this.extraTableFooterHtml != null) {
            return this.extraTableFooterHtml;
        }
        ValueExpression vb = getValueExpression("extraTableFooterHtml");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Extra HTML code to be appended to the {@code &lt;td&gt;} HTML
     * element that is rendered for the table column footer. Use only code that
     * is valid in an HTML {@code &lt;td&gt;} element. The code you specify
     * is inserted in the HTML element, and is not checked for validity. For
     * example, you might set this attribute to {@code "nowrap=`nowrap'"}.
     * @param newExtraTableFooterHtml extraTableFooterHtml
     */
    public void setExtraTableFooterHtml(final String newExtraTableFooterHtml) {
        this.extraTableFooterHtml = newExtraTableFooterHtml;
    }

    /**
     * The text to be displayed in the column footer.
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
     * The text to be displayed in the column footer.
     * @param newFooterText footerText
     */
    public void setFooterText(final String newFooterText) {
        this.footerText = newFooterText;
    }

    /**
     * The text to be displayed in the column header.
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
     * The text to be displayed in the column header.
     * @param newHeaderText headerText
     */
    public void setHeaderText(final String newHeaderText) {
        this.headerText = newHeaderText;
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
     * Use the {@code rowHeader} attribute to specify that the cells of the
     * column are acting as row headers. Row headers are cells that "label" the
     * row. For example, consider a table where the first column contains
     * check boxes, and the second column contains user names. The third and
     * subsequent columns contain attributes of those users. The content of the
     * cells in the user name column are acting as row headers. The
     * {@code webuijsf:tableColumn} tag for the user name column should set
     * the {@code rowHeader} attribute to true. If a table contains, for
     * example, a system log with time stamp and log entry columns, neither
     * column is acting as a row header, so the {@code rowHeader} attribute
     * should not be set.
     * <p>
     * By default, most column cells are rendered by the table component with
     * HTML {@code &lt;td scope="col"&gt;} elements. The exceptions are
     * columns that contain check boxes or radio buttons and spacer columns, all
     * of which are rendered as {@code &lt;td&gt;} elements without a scope
     * property.
     * </p><p>
     * When you set the {@code rowHeader} attribute, the column cells are
     * rendered as {@code &lt;th scope="row"&gt;} elements, which enables
     * adaptive technologies such as screen readers to properly read the table
     * to indicate that the contents of these cells are headers for the
     * rows.</p>
     * @return {@code boolean}
     */
    public boolean isRowHeader() {
        if (this.rowHeaderSet) {
            return this.rowHeader;
        }
        ValueExpression vb = getValueExpression("rowHeader");
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
     * Use the {@code rowHeader} attribute to specify that the cells of the
     * column are acting as row headers. Row headers are cells that "label" the
     * row. For example, consider a table where the first column contains
     * check boxes, and the second column contains user names. The third and
     * subsequent columns contain attributes of those users. The content of the
     * cells in the user name column are acting as row headers. The
     * {@code webuijsf:tableColumn} tag for the user name column should set
     * the {@code rowHeader} attribute to true. If a table contains, for
     * example, a system log with time stamp and log entry columns, neither
     * column is acting as a row header, so the {@code rowHeader} attribute
     * should not be set.
     * <p>
     * By default, most column cells are rendered by the table component with
     * HTML {@code &lt;td scope="col"&gt;} elements. The exceptions are
     * columns that contain check boxes or radio buttons and spacer columns, all
     * of which are rendered as {@code &lt;td&gt;} elements without a scope
     * property.
     * </p><p>
     * When you set the {@code rowHeader} attribute, the column cells are
     * rendered as {@code &lt;th scope="row"&gt;} elements, which enables
     * adaptive technologies such as screen readers to properly read the table
     * to indicate that the contents of these cells are headers for the
     * rows.</p>
     * @param newRowHeader rowHeader
     */
    public void setRowHeader(final boolean newRowHeader) {
        this.rowHeader = newRowHeader;
        this.rowHeaderSet = true;
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
     * Use the {@code selectId} attribute in select columns, which contain
     * check boxes or radio buttons for selecting table rows. The value of
     * {@code selectId} must match the {@code id} attribute of the
     * checkbox or radioButton component that is a child of the tableColumn
     * component. A fully qualified ID based on the tableColumn component ID and
     * the {@code selectId} for the current row will be dynamically created
     * for the {@code &lt;input&gt;} element that is rendered for the
     * checkbox or radio button. The {@code selectId} is required for
     * functionality that supports the toggle buttons for selecting rows. The
     * {@code selectId} also identifies the column as a select column, for
     * which the table component uses different CSS styles.
     * @return String
     */
    public String getSelectId() {
        if (this.selectId != null) {
            return this.selectId;
        }
        ValueExpression vb = getValueExpression("selectId");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Use the {@code selectId} attribute in select columns, which contain
     * check boxes or radio buttons for selecting table rows. The value of
     * {@code selectId} must match the {@code id} attribute of the
     * checkbox or radioButton component that is a child of the tableColumn
     * component. A fully qualified ID based on the tableColumn component ID and
     * the {@code selectId} for the current row will be dynamically created
     * for the {@code &lt;input&gt;} element that is rendered for the
     * checkbox or radio button. The {@code selectId} is required for
     * functionality that supports the toggle buttons for selecting rows. The
     * {@code selectId} also identifies the column as a select column, for
     * which the table component uses different CSS styles.
     * @param newSelectId selectId
     */
    public void setSelectId(final String newSelectId) {
        this.selectId = newSelectId;
    }

    /**
     * Use the {@code severity} attribute when including the
     * {@code webuijsf:alarm} component in a column, to match the severity
     * of the alarm. Valid values are described in the
     * {@code webuijsf:alarm} documentation. When the {@code severity}
     * attribute is set in the tableColumn, the table component renders sort
     * tool tips to indicate that the column will be sorted least/most severe
     * first, and the table cell appears hightlighted according to the level of
     * severity. This functionality is overridden by the {@code emptyCell}
     * attribute.
     * @return String
     */
    public String getSeverity() {
        if (this.severity != null) {
            return this.severity;
        }
        ValueExpression vb = getValueExpression("severity");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Use the {@code severity} attribute when including the
     * {@code webuijsf:alarm} component in a column, to match the severity
     * of the alarm. Valid values are described in the
     * {@code webuijsf:alarm} documentation. When the {@code severity}
     * attribute is set in the tableColumn, the table component renders sort
     * tool tips to indicate that the column will be sorted least/most severe
     * first, and the table cell appears hightlighted according to the level of
     * severity. This functionality is overridden by the {@code emptyCell}
     * attribute.
     * @param newSeverity severity
     */
    public void setSeverity(final String newSeverity) {
        this.severity = newSeverity;
    }

    /**
     * Use the {@code sort} attribute to specify a FieldKey id or
     * SortCriteria that defines the criteria to use for sorting the contents of
     * a TableDataProvider. If SortCriteria is provided, the object is used for
     * sorting as is. If an id is provided, a FieldIdSortCriteria is created for
     * sorting. In addition, a value binding can also be used to sort on an
     * object that is external to TableDataProvider, such as the selected state
     * of a checkbox or radio button. When a value binding is used, a
     * ValueBindingSortCriteria object is created for sorting. All sorting is
     * based on the object type associated with the data element (for example,
     * Boolean, Character, Comparator, Date, Number, and String). If the object
     * type cannot be determined, the object is compared as a String. The
     * {@code sort} attribute is required for a column to be shown as
     * sortable.
     * @return Object
     */
    public Object getSort() {
        if (this.sort != null) {
            return this.sort;
        }
        ValueExpression vb = getValueExpression("sort");
        if (vb != null) {
            return (Object) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Use the {@code sort} attribute to specify a FieldKey id or
     * SortCriteria that defines the criteria to use for sorting the contents of
     * a TableDataProvider. If SortCriteria is provided, the object is used for
     * sorting as is. If an id is provided, a FieldIdSortCriteria is created for
     * sorting. In addition, a value binding can also be used to sort on an
     * object that is external to TableDataProvider, such as the selected state
     * of a checkbox or radio button. When a value binding is used, a
     * ValueBindingSortCriteria object is created for sorting. All sorting is
     * based on the object type associated with the data element (for example,
     * Boolean, Character, Comparator, Date, Number, and String). If the object
     * type cannot be determined, the object is compared as a String. The
     * {@code sort} attribute is required for a column to be shown as
     * sortable.
     * @param newSort sort
     */
    public void setSort(final Object newSort) {
        this.sort = newSort;
    }

    /**
     * The theme identifier to use for the sort button that is displayed in the
     * column header. Use this attribute to override the default image.
     * @return String
     */
    public String getSortIcon() {
        if (this.sortIcon != null) {
            return this.sortIcon;
        }
        ValueExpression vb = getValueExpression("sortIcon");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The theme identifier to use for the sort button that is displayed in the
     * column header. Use this attribute to override the default image.
     * @param newSortIcon sortIcon
     */
    public void setSortIcon(final String newSortIcon) {
        this.sortIcon = newSortIcon;
    }

    /**
     * Absolute or relative URL to the image used for the sort button that is
     * displayed in the column header.
     * @return String
     */
    public String getSortImageURL() {
        if (this.sortImageURL != null) {
            return this.sortImageURL;
        }
        ValueExpression vb = getValueExpression("sortImageURL");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Absolute or relative URL to the image used for the sort button that is
     * displayed in the column header.
     * @param newSortImageURL sortImageURL
     */
    public void setSortImageURL(final String newSortImageURL) {
        this.sortImageURL = newSortImageURL;
    }

    /**
     * Use the {@code spacerColumn} attribute to use the column as a blank
     * column to enhance spacing in two or three column tables. When the
     * {@code spacerColumn} attribute is true, the CSS styles applied to
     * the column make it appear as if the columns are justified. If a column
     * header and footer are required, provide an empty string for the
     * {@code headerText} and {@code footerText} attributes. Set the
     * {@code width} attribute to justify columns accordingly.
     * @return {@code boolean}
     */
    public boolean isSpacerColumn() {
        if (this.spacerColumnSet) {
            return this.spacerColumn;
        }
        ValueExpression vb = getValueExpression("spacerColumn");
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
     * Use the {@code spacerColumn} attribute to use the column as a blank
     * column to enhance spacing in two or three column tables. When the
     * {@code spacerColumn} attribute is true, the CSS styles applied to
     * the column make it appear as if the columns are justified. If a column
     * header and footer are required, provide an empty string for the
     * {@code headerText} and {@code footerText} attributes. Set the
     * {@code width} attribute to justify columns accordingly.
     * @param newSpacerColumn spacerColumn
     */
    public void setSpacerColumn(final boolean newSpacerColumn) {
        this.spacerColumn = newSpacerColumn;
        this.spacerColumnSet = true;
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
     * The text to be displayed in the table column footer. The table column
     * footer is displayed once per table, and is especially useful in tables
     * with multiple groups of rows.
     * @return String
     */
    public String getTableFooterText() {
        if (this.tableFooterText != null) {
            return this.tableFooterText;
        }
        ValueExpression vb = getValueExpression("tableFooterText");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The text to be displayed in the table column footer. The table column
     * footer is displayed once per table, and is especially useful in tables
     * with multiple groups of rows.
     * @param newTableFooterText tableFooterText
     */
    public void setTableFooterText(final String newTableFooterText) {
        this.tableFooterText = newTableFooterText;
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
        this.abbr = (String) values[1];
        this.align = (String) values[2];
        this.alignKey = (Object) values[3];
        this.axis = (String) values[4];
        this.bgColor = (String) values[5];
        this.charFor = (String) values[6];
        this.charOff = (String) values[7];
        this.colSpan = ((Integer) values[8]);
        this.colSpanSet = ((Boolean) values[9]);
        this.descending = ((Boolean) values[10]);
        this.descendingSet = ((Boolean) values[11]);
        this.embeddedActions = ((Boolean) values[12]);
        this.embeddedActionsSet = ((Boolean) values[13]);
        this.emptyCell = ((Boolean) values[14]);
        this.emptyCellSet = ((Boolean) values[15]);
        this.extraFooterHtml = (String) values[16];
        this.extraHeaderHtml = (String) values[17];
        this.extraTableFooterHtml = (String) values[18];
        this.footerText = (String) values[19];
        this.headerText = (String) values[20];
        this.headers = (String) values[21];
        this.height = (String) values[22];
        this.noWrap = ((Boolean) values[23]);
        this.noWrapSet = ((Boolean) values[24]);
        this.onClick = (String) values[25];
        this.onDblClick = (String) values[26];
        this.onKeyDown = (String) values[27];
        this.onKeyPress = (String) values[28];
        this.onKeyUp = (String) values[29];
        this.onMouseDown = (String) values[30];
        this.onMouseMove = (String) values[31];
        this.onMouseOut = (String) values[32];
        this.onMouseOver = (String) values[33];
        this.onMouseUp = (String) values[34];
        this.rowHeader = ((Boolean) values[35]);
        this.rowHeaderSet = ((Boolean) values[36]);
        this.rowSpan = ((Integer) values[37]);
        this.rowSpanSet = ((Boolean) values[38]);
        this.scope = (String) values[39];
        this.selectId = (String) values[40];
        this.severity = (String) values[41];
        this.sort = (Object) values[42];
        this.sortIcon = (String) values[43];
        this.sortImageURL = (String) values[44];
        this.spacerColumn = ((Boolean) values[45]);
        this.spacerColumnSet = ((Boolean) values[46]);
        this.style = (String) values[47];
        this.styleClass = (String) values[48];
        this.tableFooterText = (String) values[49];
        this.toolTip = (String) values[50];
        this.valign = (String) values[51];
        this.visible = ((Boolean) values[52]);
        this.visibleSet = ((Boolean) values[53]);
        this.width = (String) values[54];
    }

    /**
     * This implementation saves the state of all properties.
     * @param context faces context
     * @return Object
     */
    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[55];
        values[0] = super.saveState(context);
        values[1] = this.abbr;
        values[2] = this.align;
        values[3] = this.alignKey;
        values[4] = this.axis;
        values[5] = this.bgColor;
        values[6] = this.charFor;
        values[7] = this.charOff;
        values[8] = this.colSpan;
        if (this.colSpanSet) {
            values[9] = Boolean.TRUE;
        } else {
            values[9] = Boolean.FALSE;
        }
        if (this.descending) {
            values[10] = Boolean.TRUE;
        } else {
            values[10] = Boolean.FALSE;
        }
        if (this.descendingSet) {
            values[11] = Boolean.TRUE;
        } else {
            values[11] = Boolean.FALSE;
        }
        if (this.embeddedActions) {
            values[12] = Boolean.TRUE;
        } else {
            values[12] = Boolean.FALSE;
        }
        if (this.embeddedActionsSet) {
            values[13] = Boolean.TRUE;
        } else {
            values[13] = Boolean.FALSE;
        }
        if (this.emptyCell) {
            values[14] = Boolean.TRUE;
        } else {
            values[14] = Boolean.FALSE;
        }
        if (this.emptyCellSet) {
            values[15] = Boolean.TRUE;
        } else {
            values[15] = Boolean.FALSE;
        }
        values[16] = this.extraFooterHtml;
        values[17] = this.extraHeaderHtml;
        values[18] = this.extraTableFooterHtml;
        values[19] = this.footerText;
        values[20] = this.headerText;
        values[21] = this.headers;
        values[22] = this.height;
        if (this.noWrap) {
            values[23] = Boolean.TRUE;
        } else {
            values[23] = Boolean.FALSE;
        }
        if (this.noWrapSet) {
            values[24] = Boolean.TRUE;
        } else {
            values[24] = Boolean.FALSE;
        }
        values[25] = this.onClick;
        values[26] = this.onDblClick;
        values[27] = this.onKeyDown;
        values[28] = this.onKeyPress;
        values[29] = this.onKeyUp;
        values[30] = this.onMouseDown;
        values[31] = this.onMouseMove;
        values[32] = this.onMouseOut;
        values[33] = this.onMouseOver;
        values[34] = this.onMouseUp;
        if (this.rowHeader) {
            values[35] = Boolean.TRUE;
        } else {
            values[35] = Boolean.FALSE;
        }
        if (this.rowHeaderSet) {
            values[36] = Boolean.TRUE;
        } else {
            values[36] = Boolean.FALSE;
        }
        values[37] = this.rowSpan;
        if (this.rowSpanSet) {
            values[38] = Boolean.TRUE;
        } else {
            values[38] = Boolean.FALSE;
        }
        values[39] = this.scope;
        values[40] = this.selectId;
        values[41] = this.severity;
        values[42] = this.sort;
        values[43] = this.sortIcon;
        values[44] = this.sortImageURL;
        if (this.spacerColumn) {
            values[45] = Boolean.TRUE;
        } else {
            values[45] = Boolean.FALSE;
        }
        if (this.spacerColumnSet) {
            values[46] = Boolean.TRUE;
        } else {
            values[46] = Boolean.FALSE;
        }
        values[47] = this.style;
        values[48] = this.styleClass;
        values[49] = this.tableFooterText;
        values[50] = this.toolTip;
        values[51] = this.valign;
        if (this.visible) {
            values[52] = Boolean.TRUE;
        } else {
            values[52] = Boolean.FALSE;
        }
        if (this.visibleSet) {
            values[53] = Boolean.TRUE;
        } else {
            values[53] = Boolean.FALSE;
        }
        values[54] = this.width;
        return values;
    }

    /**
     * Helper method to get the number of columns found for this component that
     * have a rendered property of true.
     *
     * @param component TableColumn to be rendered.
     * @return The first selectId property found.
     */
    private int getColumnCount(final TableColumn component) {
        int count = 0;
        if (component == null) {
            log("getColumnCount",
                    "Cannot obtain column count, TableColumn is null");
            return count;
        }

        // Get column count for nested TableColumn children.
        Iterator kids = component.getTableColumnChildren();
        if (kids.hasNext()) {
            while (kids.hasNext()) {
                TableColumn col = (TableColumn) kids.next();
                if (!col.isRendered()) {
                    continue;
                }
                count += getColumnCount(col);
            }
        } else {
            // Do not include root TableColumn nodes in count.
            if (component.isRendered()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Helper method to get the number of rows found for this component that
     * have a rendered property of true.
     *
     * @return The number of rendered rows.
     */
    private int getRowCount() {
        if (rowCount == -1) {
            rowCount = 0; // Initialize min value.

            // Get all TableColumn children at the same level of the tree.
            Iterator kids;
            TableColumn col = getTableColumnAncestor();
            if (col != null) {
                kids = col.getTableColumnChildren();
            } else {
                TableRowGroup group = getTableRowGroupAncestor();
                if (group != null) {
                    kids = group.getTableColumnChildren();
                } else {
                    kids = null;
                }
            }

            // Get max row count for this level of the tree.
            if (kids != null) {
                while (kids.hasNext()) {
                    int result = getRowCount((TableColumn) kids.next());
                    if (rowCount < result) {
                        rowCount = result;
                    }
                }
            }
        }
        return rowCount;
    }

    /**
     * Helper method to get the number of rows found for this component that
     * have a rendered property of true.
     *
     * @param component TableColumn to be rendered.
     * @return The first selectId property found.
     */
    private int getRowCount(final TableColumn component) {
        int count = 0;
        if (component == null) {
            log("getRowCount", "Cannot obtain row count, TableColumn is null");
            return count;
        }

        // Get max row count for nested TableColumn children.
        Iterator kids = component.getTableColumnChildren();
        if (kids.hasNext()) {
            while (kids.hasNext()) {
                TableColumn col = (TableColumn) kids.next();
                if (!col.isRendered()) {
                    continue;
                }
                int result = getRowCount(col);
                if (count < result) {
                    count = result;
                }
            }
        }
        // Include root TableColumn component in count.
        return ++count;
    }

    /**
     * Helper method to get the data type of the data element referenced by the
     * alignKey property.
     *
     * @return The data type of the data element.
     */
    private Class getType() {
        // Note: Avoid using getSourceData when possible. If developers do not
        // cache their TableDataProvider objects, this may cause providers to be
        // recreated, for each reference, which affects performance. Instead,
        // get the type cached in TableRowGroup.
        TableRowGroup group = getTableRowGroupAncestor();
        if (group == null) {
            log("getType", "Cannot obtain data type, TableRowGroup is null");
            return null;
        }

        // Get FieldKey.
        FieldKey key = null;
        if (getAlignKey() instanceof FieldKey) {
            // If value binding resolves to FieldKey, use as is.
            key = (FieldKey) getAlignKey();
        } else if (getAlignKey() != null) {
            try {
                key = group.getFieldKey(getAlignKey().toString());
            } catch (IllegalArgumentException e) {
                log("getType", "Cannot obtain data type,"
                        + " object type may not be set");
            }
        }
        if (key != null) {
            group.getType(key);
        }
        return String.class;
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
     * Helper method to test if column footers should be rendered.
     * <p>
     * Note: Since headers and footers are optional, we do not render them by
     * default. However, if any of the properties above are set, they must be
     * set for all columns, including nested columns. Otherwise, we may end up
     * with no header or footer and columns shift left. Alternatively,
     * developers could add an empty string for each property.
     * </p>
     * @return {@code boolean}
     */
    private boolean isColumnFooterRendered() {
        boolean result = false; // Assume no headers or footers are used.
        TableRowGroup group = getTableRowGroupAncestor();
        if (group == null) {
            log("isColumnFooterRendered",
                    "Cannot determine if column footer is rendered,"
                            + " TableRowGroup is null");
            return result;
        }

        // Test the footerText property for all TableColumn components.
        Iterator kids = group.getTableColumnChildren();
        while (kids.hasNext()) {
            TableColumn col = (TableColumn) kids.next();
            if (isColumnFooterRendered(col)) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * Helper method to test the footerText property for nested TableColumn
     * components.
     *
     * @param component TableColumn component to render.
     * @return {@code boolean}
     */
    private boolean isColumnFooterRendered(final TableColumn component) {
        boolean rendered = false;
        if (component == null) {
            log("isColumnFooterRendered",
                    "Cannot determine if column footer is rendered,"
                            + " TableColumn is null");
            return rendered;
        }

        // Test the footerText property for all TableColumn components.
        Iterator kids = component.getTableColumnChildren();
        if (kids.hasNext()) {
            while (kids.hasNext()) {
                TableColumn col = (TableColumn) kids.next();
                if (isColumnFooterRendered(col)) {
                    // When footer text is found, don't go any further.
                    return true;
                }
            }
        }

        // If either a facet or text are defined, set rendered property.
        UIComponent facet = component.getFacet(COLUMN_FOOTER_FACET);
        if (facet != null && facet.isRendered()
                || component.getFooterText() != null) {
            rendered = true;
        }
        return rendered;
    }

    /**
     * Helper method to test if column headers should be rendered.
     * <p>
     * Note: Since headers and footers are optional, we do not render them by
     * default. However, if any of the properties above are set, they must be
     * set for all columns, including nested columns. Otherwise, we may end up
     * with no header or footer and columns shift left. Alternatively,
     * developers could add an empty string for each property.
     * </p>
     * @return {@code boolean}
     */
    private boolean isColumnHeaderRendered() {
        boolean result = false; // Assume no headers or footers are used.
        TableRowGroup group = getTableRowGroupAncestor();
        if (group == null) {
            log("isColumnHeaderRendered",
                    "Cannot determine if column header is rendered,"
                            + " TableRowGroup is null");
            return result;
        }

        // Test the headerText property for all TableColumn components.
        Iterator kids = group.getTableColumnChildren();
        while (kids.hasNext()) {
            TableColumn col = (TableColumn) kids.next();
            if (isColumnHeaderRendered(col)) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * Helper method to test the headerText property for nested TableColumn
     * components.
     *
     * @param component TableColumn component to render.
     * @return {@code boolean}
     */
    private boolean isColumnHeaderRendered(final TableColumn component) {
        boolean rendered = false;
        if (component == null) {
            log("isColumnHeaderRendered",
                    "Cannot determine if column header is rendered,"
                            + " TableColumn is null");
            return rendered;
        }

        // Test the headerText property for all TableColumn components.
        Iterator kids = component.getTableColumnChildren();
        if (kids.hasNext()) {
            while (kids.hasNext()) {
                TableColumn col = (TableColumn) kids.next();
                if (isColumnHeaderRendered(col)) {
                    // When header text is found, don't go any further.
                    return true;
                }
            }
        }

        // If either a facet or text are defined, set rendered property.
        UIComponent facet = component.getFacet(COLUMN_HEADER_FACET);
        if (facet != null && facet.isRendered()
                || component.getHeaderText() != null) {
            rendered = true;
        }
        return rendered;
    }

    /**
     * Helper method to determine if table is empty.
     * <p>
     * Note: We must determine if column headers are available for all or
     * individual TableRowGroup components. That is, there could be a single
     * column header for all row groups or one for each group. If there is more
     * than one column header, we must test the row count of all groups. If
     * there is only one column header and other groups have more than one row,
     * we want to make sorting available. Thus, sorting is available only there
     * is more than on row for all row groups.
     * </p>
     *
     * @return true if sorting should be available, else false.
     */
    private boolean isEmptyTable() {
        boolean result = false;
        Table table = getTableAncestor();
        TableRowGroup group = getTableRowGroupAncestor();
        if (table != null && group != null) {
            // Get total rows and headers for all TableRowGroup components.
            int rows = table.getRowCount();
            int tableHeaders = table.getColumnHeadersCount();
            if (tableHeaders > 1) {
                // Test individual groups.
                result = !(group.getRowCount() > 1);
            } else {
                // No sorting for single row.
                result = rows == 0 || rows == 1;
            }
        }
        return result;
    }

    /**
     * Helper method to test if table column footers should be rendered.
     * <p>
     * Note: Since headers and footers are optional, we do not render them by
     * default. However, if any of the properties above are set, they must be
     * set for all columns, including nested columns. Otherwise, we may end up
     * with no header or footer and columns shift left. Alternatively,
     * developers could add an empty string for each property.
     * </p>
     * @return {@code boolean}
     */
    private boolean isTableColumnFooterRendered() {
        boolean result = false; // Assume no headers or footers are used.
        TableRowGroup group = getTableRowGroupAncestor();
        if (group == null) {
            log("isTableColumnFooterRendered",
                    "Cannot determine if table column footer is rendered,"
                            + " TableRowGroup is null");
            return result;
        }

        // Test the tableFooterText property for all TableColumn components.
        Iterator kids = group.getTableColumnChildren();
        while (kids.hasNext()) {
            TableColumn col = (TableColumn) kids.next();
            if (isTableColumnFooterRendered(col)) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * Helper method to test the tableFooterText property for nested TableColumn
     * components.
     *
     * @param component TableColumn component to render.
     * @return {@code boolean}
     */
    private boolean isTableColumnFooterRendered(final TableColumn component) {
        boolean rendered = false;
        if (component == null) {
            log("isTableColumnFooterRendered",
                    "Cannot determine if table column footer is rendered,"
                            + " TableColumn is null");
            return rendered;
        }

        // Test the tableFooterText property for all TableColumn components.
        Iterator kids = component.getTableColumnChildren();
        if (kids.hasNext()) {
            while (kids.hasNext()) {
                TableColumn col = (TableColumn) kids.next();
                if (isTableColumnFooterRendered(col)) {
                    // When footer text is found, don't go any further.
                    return true;
                }
            }
        }

        // If either a facet or text are defined, set rendered property.
        UIComponent facet = component.getFacet(TABLE_FOOTER_FACET);
        if (facet != null && facet.isRendered()
                || component.getTableFooterText() != null) {
            rendered = true;
        }
        return rendered;
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
