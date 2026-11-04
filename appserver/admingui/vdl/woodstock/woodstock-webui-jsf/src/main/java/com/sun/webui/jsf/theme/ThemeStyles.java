/*
 * Copyright (c) 2007, 2019 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.webui.jsf.theme;

/**
 * This class contains constants for style class names.
 * FIXME: Eventually these need to move to a theme-based resource file.
 */
@SuppressWarnings("checkstyle:JavadocVariable")
public final class ThemeStyles {

    /**
     * Cannot be instanciated.
     */
    private ThemeStyles() {
    }

    /**
     * A properties file key whose value is a space separated list
     * of style sheet keys to include on every page.
     */
    public static final String GLOBAL =
            "global";

    // A master style sheet to be included on every page
    public static final String MASTER =
            "master";

    // Body
    public static final String CONTENT_MARGIN =
            "CONTENT_MARGIN";
    public static final String DEFAULT_BODY =
            "DEFAULT_BODY";

    // Hidden
    public static final String HIDDEN =
            "HIDDEN";

    // Link
    public static final String LINK_DISABLED =
            "LINK_DISABLED";

    // Clear
    public static final String CLEAR = "CLEAR";

    // Block Type
    public static final String FLOAT =
            "FLOAT";

    // Table
    public static final String TABLE =
            "TABLE";
    public static final String TABLE_LITE =
            "TABLE_LITE";
    public static final String TABLE_ACTION_LINK =
            "TABLE_ACTION_LINK";
    public static final String TABLE_ACTION_TD =
            "TABLE_ACTION_TD";
    public static final String TABLE_ACTION_TD_LASTROW =
            "TABLE_ACTION_TD_LASTROW";
    public static final String TABLE_HEADER =
            "TABLE_HEADER";
    public static final String TABLE_HEADER_LINK =
            "TABLE_HEADER_LINK";
    public static final String TABLE_HEADER_LINK_IMG =
            "TABLE_HEADER_LINK_IMG";
    public static final String TABLE_HEADER_SORTNUM =
            "TABLE_HEADER_SORTNUM";
    public static final String TABLE_HEADER_SORT =
            "TABLE_HEADER_SORT";
    public static final String TABLE_HEADER_SORT_DISABLED =
            "TABLE_HEADER_SORT_DISABLED";
    public static final String TABLE_HEADER_SELECTCOL =
            "TABLE_HEADER_SELECTCOL";
    public static final String TABLE_HEADER_SELECTCOL_DISABLED =
            "TABLE_HEADER_SELECTCOL_DISABLED";
    public static final String TABLE_HEADER_SELECTCOL_SORT =
            "TABLE_HEADER_SELECTCOL_SORT";
    public static final String TABLE_HEADER_TABLE =
            "TABLE_HEADER_TABLE";
    public static final String TABLE_HEADER_TEXT =
            "TABLE_HEADER_TEXT";
    public static final String TABLE_MARGIN =
            "TABLE_MARGIN";
    public static final String TABLE_MESSAGE_TEXT =
            "TABLE_MESSAGE_TEXT";
    public static final String TABLE_MULTIPLE_HEADER_ROOT =
            "TABLE_MULTIPLE_HEADER_ROOT";
    public static final String TABLE_MULTIPLE_HEADER =
            "TABLE_MULTIPLE_HEADER";
    public static final String TABLE_MULTIPLE_HEADER_SORT =
            "TABLE_MULTIPLE_HEADER_SORT";
    public static final String TABLE_MULTIPLE_HEADER_TEXT =
            "TABLE_MULTIPLE_HEADER_TEXT";
    public static final String TABLE_NAVIGATION_LINK =
            "TABLE_NAVIGATION_LINK";
    public static final String TABLE_PAGINATION_TEXT =
            "TABLE_PAGINATION_TEXT";
    public static final String TABLE_PAGINATION_TEXT_BOLD =
            "TABLE_PAGINATION_TEXT_BOLD";
    public static final String TABLE_PAGINATION_LEFT_BUTTON =
            "TABLE_PAGINATION_LEFT_BUTTON";
    public static final String TABLE_PAGINATION_RIGHT_BUTTON =
            "TABLE_PAGINATION_RIGHT_BUTTON";
    public static final String TABLE_PAGINATION_SUBMIT_BUTTON =
            "TABLE_PAGINATION_SUBMIT_BUTTON";
    public static final String TABLE_TD_ALARM =
            "TABLE_TD_ALARM";
    public static final String TABLE_TD_LAYOUT =
            "TABLE_TD_LAYOUT";
    public static final String TABLE_TD_SELECTCOL =
            "TABLE_TD_SELECTCOL";
    public static final String TABLE_TD_SELECTCOL_SORT =
            "TABLE_TD_SELECTCOL_SORT";
    public static final String TABLE_TD_SORT =
            "TABLE_TD_SORT";
    public static final String TABLE_TD_SPACER =
            "TABLE_TD_SPACER";
    public static final String TABLE_TITLE_TEXT =
            "TABLE_TITLE_TEXT";
    public static final String TABLE_TITLE_TEXT_SPAN =
            "TABLE_TITLE_TEXT_SPAN";
    public static final String TABLE_TITLE_MESSAGE_SPAN =
            "TABLE_TITLE_MESSAGE_SPAN";
    public static final String TABLE_SELECT_ROW =
            "TABLE_SELECT_ROW";
    public static final String TABLE_HOVER_ROW =
            "TABLE_HOVER_ROW";
    public static final String TABLE_CUSTOM_FILTER_MENU =
            "TABLE_CUSTOM_FILTER_MENU";

    // Table footers
    public static final String TABLE_FOOTER =
            "TABLE_FOOTER";
    public static final String TABLE_FOOTER_TEXT =
            "TABLE_FOOTER_TEXT";
    public static final String TABLE_FOOTER_LEFT =
            "TABLE_FOOTER_LEFT";
    public static final String TABLE_FOOTER_MESSAGE_SPAN =
            "TABLE_FOOTER_MESSAGE_SPAN";

    // Overall row group headers/footers.
    public static final String TABLE_GROUP_HEADER =
            "TABLE_GROUP_HEADER";
    public static final String TABLE_GROUP_HEADER_IMAGE =
            "TABLE_GROUP_HEADER_IMAGE";
    public static final String TABLE_GROUP_HEADER_LEFT =
            "TABLE_GROUP_HEADER_LEFT";
    public static final String TABLE_GROUP_HEADER_RIGHT =
            "TABLE_GROUP_HEADER_RIGHT";
    public static final String TABLE_GROUP_HEADER_TEXT =
            "TABLE_GROUP_HEADER_TEXT";
    public static final String TABLE_GROUP_FOOTER =
            "TABLE_GROUP_FOOTER";
    public static final String TABLE_GROUP_FOOTER_TEXT =
            "TABLE_GROUP_FOOTER_TEXT";
    public static final String TABLE_GROUP_MESSAGE_TEXT =
            "TABLE_GROUP_MESSAGE_TEXT";

    // Calendar
    public static final String CALENDAR_DIV =
            "CALENDAR_DIV";
    public static final String CALENDAR_DIV_SHOW =
            "CALENDAR_DIV_SHOW";
    public static final String CALENDAR_DIV_SHOW2 =
            "CALENDAR_DIV_SHOW2";
    public static final String CALENDAR_FIELD_LABEL =
            "CALENDAR_FIELD_LABEL";
    public static final String CALENDAR_FIELD_IMAGE =
            "CALENDAR_FIELD_IMAGE";
    public static final String CALENDAR_FOOTER =
            "CALENDAR_FOOTER";
    public static final String CALENDAR_FOOTER_DIV =
            "CALENDAR_FOOTER_DIV";
    public static final String CALENDAR_DAY_TEXT =
            "CALENDAR_DAY_TEXT";
    public static final String CALENDAR_CLOSE_LINK =
            "CALENDAR_CLOSE_LINK";
    public static final String CALENDAR_ROOT_TABLE =
            "CALENDAR_ROOT_TABLE";
    public static final String CALENDAR_CLOSE_BUTTON =
            "CALENDAR_CLOSE_BUTTON";

    // Column footers.
    public static final String TABLE_GROUP_COL_FOOTER =
            "TABLE_GROUP_COL_FOOTER";
    public static final String TABLE_GROUP_COL_FOOTER_TEXT =
            "TABLE_GROUP_COL_FOOTER_TEXT";
    public static final String TABLE_GROUP_COL_FOOTER_SORT =
            "TABLE_GROUP_COL_FOOTER_SORT";

    // Table column footers.
    public static final String TABLE_COL_FOOTER =
            "TABLE_COL_FOOTER";
    public static final String TABLE_COL_FOOTER_TEXT =
            "TABLE_COL_FOOTER_TEXT";
    public static final String TABLE_COL_FOOTER_SORT =
            "TABLE_COL_FOOTER_SORT";
    public static final String TABLE_COL_FOOTER_SPACER =
            "TABLE_COL_FOOTER_SPACER";

    // Embedded panels.
    public static final String TABLE_PANEL_TD =
            "TABLE_PANEL_TD";
    public static final String TABLE_PANEL_LAYOUT_DIV =
            "TABLE_PANEL_LAYOUT_DIV";
    public static final String TABLE_PANEL_SHADOW3_DIV =
            "TABLE_PANEL_SHADOW3_DIV";
    public static final String TABLE_PANEL_SHADOW2_DIV =
            "TABLE_PANEL_SHADOW2_DIV";
    public static final String TABLE_PANEL_SHADOW1_DIV =
            "TABLE_PANEL_SHADOW1_DIV";
    public static final String TABLE_PANEL_DIV =
            "TABLE_PANEL_DIV";
    public static final String TABLE_PANEL_TITLE =
            "TABLE_PANEL_TITLE";
    public static final String TABLE_PANEL_CONTENT =
            "TABLE_PANEL_CONTENT";
    public static final String TABLE_PANEL_BUTTON_DIV =
            "TABLE_PANEL_BUTTON_DIV";
    public static final String TABLE_PANEL_HELP_TEXT =
            "TABLE_PANEL_HELP_TEXT";
    public static final String TABLE_PANEL_TABLE =
            "TABLE_PANEL_TABLE";

    // Drop Down Menu
    public static final String MENU_JUMP =
            "MENU_JUMP";
    public static final String MENU_JUMP_OPTION =
            "MENU_JUMP_OPTION";
    public static final String MENU_JUMP_OPTION_DISABLED =
            "MENU_JUMP_OPTION_DISABLED";
    public static final String MENU_JUMP_OPTION_GROUP =
            "MENU_JUMP_OPTION_GROUP";
    public static final String MENU_JUMP_OPTION_SELECTED =
            "MENU_JUMP_OPTION_SELECTED";
    public static final String MENU_JUMP_OPTION_SEPARATOR =
            "MENU_JUMP_OPTION_SEPARATOR";
    public static final String MENU_STANDARD =
            "MENU_STANDARD";
    public static final String MENU_STANDARD_DISABLED =
            "MENU_STANDARD_DISABLED";
    public static final String MENU_STANDARD_OPTION =
            "MENU_STANDARD_OPTION";
    public static final String MENU_STANDARD_OPTION_DISABLED =
            "MENU_STANDARD_OPTION_DISABLED";
    public static final String MENU_STANDARD_OPTION_GROUP =
            "MENU_STANDARD_OPTION_GROUP";
    public static final String MENU_STANDARD_OPTION_SELECTED =
            "MENU_STANDARD_OPTION_SELECTED";
    public static final String MENU_STANDARD_OPTION_SEPARATOR =
            "MENU_STANDARD_OPTION_SEPARATOR";

    // Selectable List
    public static final String LIST =
            "LIST";
    public static final String LIST_DISABLED =
            "LIST_DISABLED";
    public static final String LIST_MONOSPACE =
            "LIST_MONOSPACE";
    public static final String LIST_MONOSPACE_DISABLED =
            "LIST_MONOSPACE_DISABLED";
    public static final String LIST_OPTION =
            "LIST_OPTION";
    public static final String LIST_OPTION_DISABLED =
            "LIST_OPTION_DISABLED";
    public static final String LIST_OPTION_GROUP =
            "LIST_OPTION_GROUP";
    public static final String LIST_OPTION_SELECTED =
            "LIST_OPTION_SELECTED";
    public static final String LIST_OPTION_SEPARATOR =
            "LIST_OPTION_SEPARATOR";
    public static final String LIST_ALIGN =
            "LIST_ALIGN";

    // Primary Button
    public static final String BUTTON1 =
            "BUTTON1";
    public static final String BUTTON1_HOVER =
            "BUTTON1_HOVER";
    public static final String BUTTON1_DISABLED =
            "BUTTON1_DISABLED";
    public static final String BUTTON1_DEFAULT =
            "BUTTON1_DEFAULT";
    public static final String BUTTON1_DEFAULT_HOVER =
            "BUTTON1_DEFAULT_HOVER";
    public static final String BUTTON1_MINI =
            "BUTTON1_MINI";
    public static final String BUTTON1_MINI_HOVER =
            "BUTTON1_MINI_HOVER";
    public static final String BUTTON1_MINI_DISABLED =
            "BUTTON1_MINI_DISABLED";

    // Secondary Button
    public static final String BUTTON2 =
            "BUTTON2";
    public static final String BUTTON2_HOVER =
            "BUTTON2_HOVER";
    public static final String BUTTON2_DISABLED =
            "BUTTON2_DISABLED";
    public static final String BUTTON2_MINI =
            "BUTTON2_MINI";
    public static final String BUTTON2_MINI_HOVER =
            "BUTTON2_MINI_HOVER";
    public static final String BUTTON2_MINI_DISABLED =
            "BUTTON2_MINI_DISABLED";

    // Icon Button
    public static final String BUTTON3 =
            "BUTTON3";
    public static final String BUTTON3_HOVER =
            "BUTTON3_HOVER";
    public static final String BUTTON3_DISABLED =
            "BUTTON3_DISABLED";

    // Breadcrumb
    public static final String BREADCRUMB_TEXT =
            "BREADCRUMB_TEXT";
    public static final String BREADCRUMB_WHITE_DIV =
            "BREADCRUMB_WHITE_DIV";
    public static final String BREADCRUMB_GRAY_DIV =
            "BREADCRUMB_GRAY_DIV";
    public static final String BREADCRUMB_LINK =
            "BREADCRUMB_LINK";
    public static final String BREADCRUMB_SEPARATOR =
            "BREADCRUMB_SEPARATOR";

    // Page Title
    public static final String TITLE_TEXT_DIV =
            "TITLE_TEXT_DIV";
    public static final String TITLE_LINE =
            "TITLE_LINE";
    public static final String TITLE_TEXT =
            "TITLE_TEXT";
    public static final String TITLE_HELP_DIV =
            "TITLE_HELP_DIV";
    public static final String TITLE_ACTION_DIV =
            "TITLE_ACTION_DIV";
    public static final String TITLE_VIEW_DIV =
            "TITLE_VIEW_DIV";
    public static final String TITLE_BUTTON_DIV =
            "TITLE_BUTTON_DIV";
    public static final String TITLE_BUTTON_BOTTOM_DIV =
            "TITLE_BUTTON_BOTTOM_DIV";

    // Add and Remove
    public static final String ADDREMOVE_LABEL =
            "ADDREMOVE_LABEL";
    public static final String ADDREMOVE_LABEL2 =
            "ADDREMOVE_LABEL2";
    public static final String ADDREMOVE_LABEL2_READONLY =
            "ADDREMOVE_LABEL2_READONLY";
    public static final String ADDREMOVE_BUTTON_TABLE =
            "ADDREMOVE_BUTTON_TABLE";
    public static final String ADDREMOVE_VERTICAL_FIRST =
            "ADDREMOVE_VERTICAL_FIRST";
    public static final String ADDREMOVE_VERTICAL_WITHIN =
            "ADDREMOVE_VERTICAL_WITHIN";
    public static final String ADDREMOVE_VERTICAL_BETWEEN =
            "ADDREMOVE_VERTICAL_BETWEEN";
    public static final String ADDREMOVE_VERTICAL_BUTTON =
            "ADDREMOVE_VERTICAL_BUTTON";
    public static final String ADDREMOVE_VERTICAL_DIV =
            "ADDREMOVE_VERTICAL_DIV";
    public static final String ADDREMOVE_VERTICAL_CLEAR =
            "ADDREMOVE_VERTICAL_CLEAR";
    public static final String ADDREMOVE_HORIZONTAL_WITHIN =
            "ADDREMOVE_HORIZONTAL_WITHIN";
    public static final String ADDREMOVE_HORIZONTAL_BETWEEN =
            "ADDREMOVE_HORIZONTAL_BETWEEN";
    public static final String ADDREMOVE_HORIZONTAL_ALIGN =
            "ADDREMOVE_HORIZONTAL_ALIGN";
    public static final String ADDREMOVE_HORIZONTAL_LAST =
            "ADDREMOVE_HORIZONTAL_LAST";

    // Checkbox
    public static final String CHECKBOX =
            "CHECKBOX";
    public static final String CHECKBOX_DISABLED =
            "CHECKBOX_DISABLED";
    public static final String CHECKBOX_LABEL =
            "CHECKBOX_LABEL";
    public static final String CHECKBOX_LABEL_DISABLED =
            "CHECKBOX_LABEL_DISABLED";
    public static final String CHECKBOX_IMAGE =
            "CHECKBOX_IMAGE";
    public static final String CHECKBOX_IMAGE_DISABLED =
            "CHECKBOX_IMAGE_DISABLED";
    public static final String CHECKBOX_SPAN =
            "CHECKBOX_SPAN";
    public static final String CHECKBOX_SPAN_DISABLED =
            "CHECKBOX_SPAN_DISABLED";

    // Checkbox Group
    public static final String CHECKBOX_GROUP =
            "CHECKBOX_GROUP";
    public static final String CHECKBOX_GROUP_CAPTION =
            "CHECKBOX_GROUP_CAPTION";
    public static final String CHECKBOX_GROUP_LABEL =
            "CHECKBOX_GROUP_LABEL";
    public static final String CHECKBOX_GROUP_LABEL_DISABLED =
            "CHECKBOX_GROUP_LABEL_DISABLED";
    public static final String CHECKBOX_GROUP_ROW_EVEN =
            "CHECKBOX_GROUP_ROW_EVEN";
    public static final String CHECKBOX_GROUP_ROW_ODD =
            "CHECKBOX_GROUP_ROW_ODD";
    public static final String CHECKBOX_GROUP_CELL_EVEN =
            "CHECKBOX_GROUP_CELL_EVEN";
    public static final String CHECKBOX_GROUP_CELL_ODD =
            "CHECKBOX_GROUP_CELL_ODD";

    // Radio Button
    public static final String RADIOBUTTON =
            "RADIOBUTTON";
    public static final String RADIOBUTTON_DISABLED =
            "RADIOBUTTON_DISABLED";
    public static final String RADIOBUTTON_LABEL =
            "RADIOBUTTON_LABEL";
    public static final String RADIOBUTTON_LABEL_DISABLED =
            "RADIOBUTTON_LABEL_DISABLED";
    public static final String RADIOBUTTON_IMAGE =
            "RADIOBUTTON_IMAGE";
    public static final String RADIOBUTTON_IMAGE_DISABLED =
            "RADIOBUTTON_IMAGE_DISABLED";
    public static final String RADIOBUTTON_SPAN =
            "RADIOBUTTON_SPAN";
    public static final String RADIOBUTTON_SPAN_DISABLED =
            "RADIOBUTTON_SPAN_DISABLED";

    // Radio Button Group
    public static final String RADIOBUTTON_GROUP =
            "RADIOBUTTON_GROUP";
    public static final String RADIOBUTTON_GROUP_CAPTION =
            "RADIOBUTTON_GROUP_CAPTION";
    public static final String RADIOBUTTON_GROUP_LABEL =
            "RADIOBUTTON_GROUP_LABEL";
    public static final String RADIOBUTTON_GROUP_LABEL_DISABLED =
            "RADIOBUTTON_GROUP_LABEL_DISABLED";
    public static final String RADIOBUTTON_GROUP_ROW_EVEN =
            "RADIOBUTTON_GROUP_ROW_EVEN";
    public static final String RADIOBUTTON_GROUP_ROW_ODD =
            "RADIOBUTTON_GROUP_ROW_ODD";
    public static final String RADIOBUTTON_GROUP_CELL_EVEN =
            "RADIOBUTTON_GROUP_CELL_EVEN";
    public static final String RADIOBUTTON_GROUP_CELL_ODD =
            "RADIOBUTTON_GROUP_CELL_ODD";

    // Inline Alert
    public static final String ALERT_LINK_DIV =
            "ALERT_LINK_DIV";
    public static final String ALERT_LINK =
            "ALERT_LINK";
    public static final String ALERT_DIV =
            "ALERT_DIV";
    public static final String ALERT_TOP_LEFT_CORNER =
            "ALERT_TOP_LEFT_CORNER";
    public static final String ALERT_TOP_MIDDLE =
            "ALERT_TOP_MIDDLE";
    public static final String ALERT_TOP_RIGHT_CORNER =
            "ALERT_TOP_RIGHT_CORNER";
    public static final String ALERT_MIDDLE_ROW =
            "ALERT_MIDDLE_ROW";
    public static final String ALERT_LEFT_MIDDLE =
            "ALERT_LEFT_MIDDLE";
    public static final String ALERT_MIDDLE =
            "ALERT_MIDDLE";
    public static final String ALERT_HEADER =
            "ALERT_HEADER";
    public static final String ALERT_TEXT =
            "ALERT_TEXT";
    public static final String ALERT_DETAILS =
            "ALERT_DETAILS";
    public static final String ALERT_RIGHT_MIDDLE =
            "ALERT_RIGHT_MIDDLE";
    public static final String ALERT_BOTTOM_LEFT_CORNER =
            "ALERT_BOTTOM_LEFT_CORNER";
    public static final String ALERT_BOTTOM_MIDDLE =
            "ALERT_BOTTOM_MIDDLE";
    public static final String ALERT_BOTTOM_RIGHT_CORNER =
            "ALERT_BOTTOM_RIGHT_CORNER";

    // Full page Alert
    public static final String ALERT_MESSAGE_DIV =
            "ALERT_MESSAGE_DIV";
    public static final String ALERT_MESSAGE_TEXT =
            "ALERT_MESSAGE_TEXT";
    public static final String ALERT_HEADER_DIV =
            "ALERT_HEADER_DIV";
    public static final String ALERT_HEADER_TXT =
            "ALERT_HEADER_TXT";
    public static final String ALERT_FORM_DIV =
            "ALERT_FORM_DIV";

    // Label
    public static final String LABEL_LEVEL_ONE_TEXT =
            "LABEL_LEVEL_ONE_TEXT";
    public static final String LABEL_LEVEL_TWO_TEXT =
            "LABEL_LEVEL_TWO_TEXT";
    public static final String LABEL_LEVEL_TWO_SMALL_TEXT =
            "LABEL_LEVEL_TWO_SMALL_TEXT";
    public static final String LABEL_LEVEL_THREE_TEXT =
            "LABEL_LEVEL_THREE_TEXT";
    public static final String LABEL_REQUIRED_DIV =
            "LABEL_REQUIRED_DIV";

    // Text Field
    public static final String TEXT_FIELD =
            "TEXT_FIELD";
    public static final String TEXT_FIELD_DISABLED =
            "TEXT_FIELD_DISABLED";

    // Text Area
    public static final String TEXT_AREA =
            "TEXT_AREA";
    public static final String TEXT_AREA_DISABLED =
            "TEXT_AREA_DISABLED";

    // Help
    public static final String HELP_BODY =
            "HELP_BODY";
    public static final String HELP_BUTTON_DIV =
            "HELP_BUTTON_DIV";
    public static final String HELP_BUTTON_NAV_BODY =
            "HELP_BUTTON_NAV_BODY";
    public static final String HELP_FIELD_LINK =
            "HELP_FIELD_LINK";
    public static final String HELP_FIELD_TEXT =
            "HELP_FIELD_TEXT";
    public static final String HELP_MASTHEAD_TITLE_BODY =
            "HELP_MASTHEAD_TITLE_BODY";
    public static final String HELP_PAGE_LINK =
            "HELP_PAGE_LINK";
    public static final String HELP_PAGE_TEXT =
            "HELP_PAGE_TEXT";
    public static final String HELP_RESULT_DIV =
            "HELP_RESULT_DIV";
    public static final String HELP_SEARCH_DIV =
            "HELP_SEARCH_DIV";
    public static final String HELP_STEP_TAB =
            "HELP_STEP_TAB";

    // Masthead
    public static final String MASTHEAD_BDY =
            "MASTHEAD_BDY";
    public static final String MASTHEAD_DIV =
            "MASTHEAD_DIV";
    public static final String MASTHEAD_LABEL =
            "MASTHEAD_LABEL";
    public static final String MASTHEAD_TEXT =
            "MASTHEAD_TEXT";
    public static final String MASTHEAD_ALARM_DOWN_TEXT =
            "MASTHEAD_ALARM_DOWN_TEXT";
    public static final String MASTHEAD_ALARM_CRITICAL_TEXT =
            "MASTHEAD_ALARM_CRITICAL_TEXT";
    public static final String MASTHEAD_ALARM_MAJOR_TEXT =
            "MASTHEAD_ALARM_MAJOR_TEXT";
    public static final String MASTHEAD_ALARM_MINOR_TEXT =
            "MASTHEAD_ALARM_MINOR_TEXT";
    public static final String MASTHEAD_TABLE_TOP =
            "MASTHEAD_TABLE_TOP";
    public static final String MASTHEAD_TABLE_BOTTOM =
            "MASTHEAD_TABLE_BOTTOM";
    public static final String MASTHEAD_TABLE_END =
            "MASTHEAD_TABLE_END";
    public static final String MASTHEAD_SECONDARY_TABLE =
            "MASTHEAD_SECONDARY_TABLE";
    public static final String MASTHEAD_TD_TITLE =
            "MASTHEAD_TD_TITLE";
    public static final String MASTHEAD_TD_ALARM =
            "MASTHEAD_TD_ALARM";
    public static final String MASTHEAD_TD_LOGO =
            "MASTHEAD_TD_LOGO";
    public static final String MASTHEAD_DIV_TITLE =
            "MASTHEAD_DIV_TITLE";
    public static final String MASTHEAD_DIV_SECONDARY_TITLE =
            "MASTHEAD_DIV_SECONDARY_TITLE";
    public static final String MASTHEAD_DIV_USER =
            "MASTHEAD_DIV_USER";
    public static final String MASTHEAD_LINK =
            "MASTHEAD_LINK";
    public static final String MASTHEAD_LINK_LEFT =
            "MASTHEAD_LINK_LEFT";
    public static final String MASTHEAD_LINK_RIGHT =
            "MASTHEAD_LINK_RIGHT";
    public static final String MASTHEAD_LINK_CENTER =
            "MASTHEAD_LINK_CENTER";
    public static final String MASTHEAD_USER_LINK =
            "MASTHEAD_USER_LINK";
    public static final String MASTHEAD_ALARM_LINK =
            "MASTHEAD_ALARM_LINK";
    public static final String MASTHEAD_PROGRESS_LINK =
            "MASTHEAD_PROGRESS_LINK";
    public static final String MASTHEAD_STATUS_DIV =
            "MASTHEAD_STATUS_DIV";
    public static final String MASTHEAD_TIME_DIV =
            "MASTHEAD_TIME_DIV";
    public static final String MASTHEAD_ALARM_DIV =
            "MASTHEAD_ALARM_DIV";
    public static final String MASTHEAD_SPACER_IMAGE =
            "MASTHEAD_SPACER_IMAGE";
    public static final String MASTHEAD_HRULE =
            "MASTHEAD_HRULE";
    public static final String MASTHEAD_SECONDARY_STYLE =
            "MASTHEAD_SECONDARY_STYLE";
    public static final String MASTHEAD_FOOTER =
            "MASTHEAD_FOOTER";
    public static final String MASTHEAD_BUTTON =
            "MASTHEAD_BUTTON";

    // Tab Navigation
    public static final String TAB1_DIV =
            "TAB1_DIV";
    public static final String TAB1_TABLE_NEW =
            "TAB1_TABLE_NEW";
    public static final String TAB1_TABLE2_NEW =
            "TAB1_TABLE2_NEW";
    public static final String TAB1_TABLE3_NEW =
            "TAB1_TABLE3_NEW";
    public static final String TAB1_LINK =
            "TAB1_LINK";
    public static final String TAB1_TABLE_SPACER_TD =
            "TAB1_TABLE_SPACER_TD";
    public static final String TAB1_TABLE_SELECTED_TD =
            "TAB1_TABLE_SELECTED_TD";
    public static final String TAB1_SELECTED_TEXT_NEW =
            "TAB1_SELECTED_TEXT_NEW";
    public static final String TAB1_SELECTED_TEXT_LEFT =
            "TAB1_SELECTED_TEXT_LEFT";
    public static final String TAB2_DIV =
            "TAB2_DIV";
    public static final String TAB2_TABLE_NEW =
            "TAB2_TABLE_NEW";
    public static final String TAB2_TABLE3_NEW =
            "TAB2_TABLE3_NEW";
    public static final String TAB2_LINK =
            "TAB2_LINK";
    public static final String TAB2_SELECTED_TEXT =
            "TAB2_SELECTED_TEXT";
    public static final String TAB2_SELECTED_TEXT_LEFT =
            "TAB2_SELECTED_TEXT_LEFT";
    public static final String TAB2_TABLE_SELECTED_TD =
            "TAB2_TABLE_SELECTED_TD";
    public static final String TAB3_DIV =
            "TAB3_DIV";
    public static final String TAB3_TABLE_NEW =
            "TAB3_TABLE_NEW";
    public static final String TAB3_LINK =
            "TAB3_LINK";
    public static final String TAB3_SELECTED_TEXT =
            "TAB3_SELECTED_TEXT";
    public static final String TAB3_TABLE_SELECTED_TD =
            "TAB3_TABLE_SELECTED_TD";
    public static final String TABGROUP =
            "TABGROUP";
    public static final String TABGROUPBOX =
            "TABGROUPBOX";
    public static final String TAB_PADDING =
            "TAB_PADDING";

    // Mini Tabs
    public static final String MINI_TAB_DIV =
            "MINI_TAB_DIV";
    public static final String MINI_TAB_TABLE =
            "MINI_TAB_TABLE";
    public static final String MINI_TAB_LINK =
            "MINI_TAB_LINK";
    public static final String MINI_TAB_SELECTED_TEXT =
            "MINI_TAB_SELECTED_TEXT";
    public static final String MINI_TAB_TABLE_SELECTED_TD =
            "MINI_TAB_TABLE_SELECTED_TD";

    // Properties Page Jump Links
    public static final String JUMP_LINK =
            "JUMP_LINK";
    public static final String JUMP_TOP_LINK =
            "JUMP_TOP_LINK";

    // Property sheet
    public static final String PROPERTY_SHEET =
            "PROPERTY_SHEET";

    // Content Pages
    public static final String CONTENT_DEFAULT_TEXT =
            "CONTENT_DEFAULT_TEXT";
    public static final String CONTENT_LIN =
            "CONTENT_LIN";
    public static final String CONTENT_FIELDSET_DIV =
            "CONTENT_FIELDSET_DIV";
    public static final String CONTENT_FIELDSET =
            "CONTENT_FIELDSET";
    public static final String CONTENT_FIELDSET_LEGEND =
            "CONTENT_FIELDSET_LEGEND";
    public static final String CONTENT_FIELDSET_LEGEND_DIV =
            "CONTENT_FIELDSET_LEGEND_DIV";
    public static final String CONTENT_SUBSECTION_DIV =
            "CONTENT_SUBSECTION_DIV";
    public static final String CONTENT_SUBSECTION_TITLE_TEXT =
            "CONTENT_SUBSECTION_TITLE_TEXT";
    public static final String CONTENT_TABLE_COL1_DIV =
            "CONTENT_TABLE_COL1_DIV";
    public static final String CONTENT_TABLE_COL2_DIV =
            "CONTENT_TABLE_COL2_DIV";
    public static final String CONTENT_JUMP_SECTION_DIV =
            "CONTENT_JUMP_SECTION_DIV";
    public static final String CONTENT_JUMP_LINK_DIV =
            "CONTENT_JUMP_LINK_DIV";
    public static final String CONTENT_REQUIRED_DIV =
            "CONTENT_REQUIRED_DIV";
    public static final String CONTENT_REQUIRED_TEXT =
            "CONTENT_REQUIRED_TEXT";
    public static final String CONTENT_JUMP_TOP_DIV =
            "CONTENT_JUMP_TOP_DIV";
    public static final String CONTENT_ERROR_LABEL_TEXT =
            "CONTENT_ERROR_LABEL_TEXT";
    public static final String CONTENT_EMBEDDED_TABLE_COL1_DIV =
            "CONTENT_EMBEDDED_TABLE_COL1_DIV";
    public static final String CONTENT_EMBEDDED_TABLE_COL2_DIV =
            "CONTENT_EMBEDDED_TABLE_COL2_DIV";

    // File Chooser
    public static final String FILECHOOSER_CONMGN =
            "FILECHOOSER_CONMGN";
    public static final String FILECHOOSER_LABEL_TXT =
            "FILECHOOSER_LABEL_TXT";
    public static final String FILECHOOSER_NAME_TXT =
            "FILECHOOSER_NAME_TXT";
    public static final String FILECHOOSER_WIDTH_TXT =
            "FILECHOOSER_WIDTH_TXT";
    public static final String FILECHOOSER_FOLD_STYLE =
            "FILECHOOSER_FOLD_STYLE";
    public static final String FILECHOOSER_FILE_STYLE =
            "FILECHOOSER_FILE_STYLE";
    public static final String FILECHOOSER_LOOK_IN_DIV =
            "FILECHOOSER_LOOK_IN_DIV";
    public static final String FILECHOOSER_BTN_GRP_DIV =
            "FILECHOOSER_BTN_GRP_DIV";
    public static final String FILECHOOSER_IMG_BTN =
            "FILECHOOSER_IMG_BTN";
    public static final String FILECHOOSER_SORT_BY_DIV =
            "FILECHOOSER_SORT_BY_DIV";
    public static final String FILECHOOSER_FLT_HLP_DIV =
            "FILECHOOSER_FLT_HLP_DIV";
    public static final String FILECHOOSER_MULT_HLP_DIV =
            "FILECHOOSER_MULT_HLP_DIV";
    public static final String FILECHOOSER_LEV2_DIV =
            "FILECHOOSER_LEV2_DIV";
    public static final String FILECHOOSER_SEL_FILE_LEV2_DIV =
            "FILECHOOSER_SEL_FILE_LEV2_DIV";
    public static final String FILECHOOSER_FLT_DIV =
            "FILECHOOSER_FLT_DIV";
    public static final String FILECHOOSER_SEL_FILE_DIV =
            "FILECHOOSER_SEL_FILE_DIV";
    public static final String FILECHOOSER_SRV_DIV =
            "FILECHOOSER_SRV_DIV";
    public static final String FILECHOOSER_LST_HDR =
            "FILECHOOSER_LST_HDR";
    public static final String FILECHOOSER_NAME_HDR =
            "FILECHOOSER_NAME_HDR";
    public static final String FILECHOOSER_SIZE_HDR =
            "FILECHOOSER_SIZE_HDR";
    public static final String FILECHOOSER_DATE_TIME_HDR =
            "FILECHOOSER_DATE_TIME_HDR";
    public static final String FILECHOOSER_LST_DIV =
            "FILECHOOSER_LST_DIV";
    public static final String FILECHOOSER_CONTROL_BTN =
            "FILECHOOSER_CONTROL_BTN";

    // Tree
    public static final String TREE =
            "TREE";
    public static final String TREE_CONTENT =
            "TREE_CONTENT";
    public static final String TREE_HAS_SELECTED_CHILD_LINK =
            "TREE_HAS_SELECTED_CHILD_LINK";
    public static final String TREE_LINK =
            "TREE_LINK";
    public static final String TREE_LINK_SPACE =
            "TREE_LINK_SPACE";
    public static final String TREE_NODE_IMAGE =
            "TREE_NODE_IMAGE";
    public static final String TREE_NODE_IMAGE_HEIGHT =
            "TREE_NODE_IMAGE_HEIGHT";
    public static final String TREE_ROOT_ROW =
            "TREE_ROOT_ROW";
    public static final String TREE_ROOT_ROW_HEADER =
            "TREE_ROOT_ROW_HEADER";
    public static final String TREE_ROW =
            "TREE_ROW";
    public static final String TREE_SELECTED_LINK =
            "TREE_SELECTED_LINK";
    public static final String TREE_SELECTED_ROW =
            "TREE_SELECTED_ROW";
    public static final String TREE_SELECTED_TEXT =
            "TREE_SELECTED_TEXT";
    public static final String TREE_TITLE =
            "TREE_TITLE";

    // Version
    public static final String VERSION_BODY =
            "VERSION_BODY";
    public static final String VERSION_TEXT =
            "VERSION_TEXT";
    public static final String VERSION_MASTHEAD_BODY =
            "VERSION_MASTHEAD_BODY";
    public static final String VERSION_BUTTON_BODY =
            "VERSION_BUTTON_BODY";
    public static final String VERSION_PRODUCT_DIV =
            "VERSION_PRODUCT_DIV";
    public static final String VERSION_PRODUCT_TD =
            "VERSION_PRODUCT_TD";
    public static final String VERSION_LOGO_TD =
            "VERSION_LOGO_TD";
    public static final String VERSION_LOGO_DIV =
            "VERSION_LOGO_DIV";
    public static final String VERSION_BUTTON_MARGIN_DIV =
            "VERSION_BUTTON_MARGIN_DIV";
    public static final String VERSION_MARGIN =
            "VERSION_MARGIN";
    public static final String VERSION_HEADER_TEXT =
            "VERSION_HEADER_TEXT";

    // Skip navigation links
    public static final String SKIP_WHITE =
            "SKIP_WHITE";
    public static final String SKIP_MEDIUM_GREY1 =
            "SKIP_MEDIUM_GREY1";

    // Wizard
    public static final String WIZARD =
            "WIZARD";
    public static final String WIZARD_STEP_LINK =
            "WIZARD_STEP_LINK";
    public static final String WIZARD_BODY =
            "WIZARD_BODY";
    public static final String WIZARD_BUTTON =
            "WIZARD_BUTTON";
    public static final String WIZARD_BUTTON_DIV =
            "WIZARD_BUTTON_DIV";
    public static final String WIZARD_CONTENT_HELP_TEXT =
            "WIZARD_CONTENT_HELP_TEXT";
    public static final String WIZARD_HELP_DIV =
            "WIZARD_HELP_DIV";
    public static final String WIZARD_HELP_TEXT =
            "WIZARD_HELP_TEXT";
    public static final String WIZARD_STEP =
            "WIZARD_STEP";
    public static final String WIZARD_STEP_ARROW_DIV =
            "WIZARD_STEP_ARROW_DIV";
    public static final String WIZARD_STEP_CURRENT_TEXT =
            "WIZARD_STEP_CURRENT_TEXT";
    public static final String WIZARD_STEP_NUMBER_DIV =
            "WIZARD_STEP_NUMBER_DIV";
    public static final String WIZARD_STEP_TABLE =
            "WIZARD_STEP_TABLE";
    public static final String WIZARD_STEP_TAB =
            "WIZARD_STEP_TAB";
    public static final String WIZARD_STEP_TEXT =
            "WIZARD_STEP_TEXT";
    public static final String WIZARD_STEP_TEXT_DIV =
            "WIZARD_STEP_TEXT_DIV";
    public static final String WIZARD_STEP_TITLE =
            "WIZARD_STEP_TITLE";
    public static final String WIZARD_SUB_TITLE_DIV =
            "WIZARD_SUB_TITLE_DIV";
    public static final String WIZARD_SUB_TITLE_TEXT =
            "WIZARD_SUB_TITLE_TEXT";
    public static final String WIZARD_SUBSTEP_TITLE_DIV =
            "WIZARD_SUBSTEP_TITLE_DIV";
    public static final String WIZARD_SUBSTEP_TITLE_TEXT =
            "WIZARD_SUBSTEP_TITLE_TEXT";
    public static final String WIZARD_TASK =
            "WIZARD_TASK";
    public static final String WIZARD_TITLE =
            "WIZARD_TITLE";
    public static final String WIZARD_STEPS_PANE_TITLE_DIV =
            "WIZARD_STEPS_PANE_TITLE_DIV";
    public static final String WIZARD_BOTTOM =
            "WIZARD_BOTTOM";
    public static final String WIZARD_BOTTOM_SPACER =
            "WIZARD_BOTTOM_SPACER";
    public static final String WIZARD_BUTTON_BOTTOM =
            "WIZARD_BUTTON_BOTTOM";
    public static final String WIZARD_LEFT =
            "WIZARD_LEFT";
    public static final String WIZARD_RIGHT =
            "WIZARD_RIGHT";
    public static final String WIZARD_BAR =
            "WIZARD_BAR";
    public static final String WIZARD_TITLE_BAR =
            "WIZARD_TITLE_BAR";

    // Date/Time
    public static final String DATE_TIME_DAY_HEADER =
            "DATE_TIME_DAY_HEADER";
    public static final String DATE_TIME_LABEL_TEXT =
            "DATE_TIME_LABEL_TEXT";
    public static final String DATE_TIME_LINK =
            "DATE_TIME_LINK";
    public static final String DATE_TIME_OTHER_BOLD_LINK =
            "DATE_TIME_OTHER_BOLD_LINK";
    public static final String DATE_TIME_OTHER_LINK =
            "DATE_TIME_OTHER_LINK";
    public static final String DATE_TIME_BOLD_LINK =
            "DATE_TIME_BOLD_LINK";
    public static final String DATE_TIME_TODAY_LINK =
            "DATE_TIME_TODAY_LINK";
    public static final String DATE_TIME_ZONE_TEXT =
            "DATE_TIME_ZONE_TEXT";
    public static final String DATE_TIME_SELECT_DIV =
            "DATE_TIME_SELECT_DIV";
    public static final String DATE_TIME_CALENDAR_DIV =
            "DATE_TIME_CALENDAR_DIV";
    public static final String DATE_TIME_CALENDAR_TABLE =
            "DATE_TIME_CALENDAR_TABLE";
    public static final String DATE_TIME_CALENDAR_LEFT =
            "DATE_TIME_CALENDAR_LEFT";
    public static final String DATE_TIME_CALENDAR_RIGHT =
            "DATE_TIME_CALENDAR_RIGHT";
    public static final String DATE_TIME_FIELD_TABLE =
            "DATE_TIME_FIELD_TABLE";
    public static final String DATE_TIME_SELECT_CONTENT =
            "DATE_TIME_SELECT_CONTENT";
    public static final String DATE_TIME_SELECT_TOP_MIDDLE =
            "DATE_TIME_SELECT_TOP_MIDDLE";
    public static final String DATE_TIME_SELECT_DATE =
            "DATE_TIME_SELECT_DATE";

    // EditableList
    public static final String EDITABLELIST_TABLE =
            "EDITABLELIST_TABLE";
    public static final String EDITABLELIST_FIELD_LABEL =
            "EDITABLELIST_FIELD_LABEL";
    public static final String EDITABLELIST_FIELD =
            "EDITABLELIST_FIELD";
    public static final String EDITABLELIST_ADD_BUTTON =
            "EDITABLELIST_ADD_BUTTON";
    public static final String EDITABLELIST_LIST_LABEL =
            "EDITABLELIST_LIST_LABEL";
    public static final String EDITABLELIST_LIST =
            "EDITABLELIST_LIST";
    public static final String EDITABLELIST_REMOVE_BUTTON =
            "EDITABLELIST_REMOVE_BUTTON";

    // Message and Message Group
    public static final String MESSAGE_FIELD_SUMMARY_TEXT =
            "MESSAGE_FIELD_SUMMARY_TEXT";
    public static final String MESSAGE_FIELD_TEXT =
            "MESSAGE_FIELD_TEXT";
    public static final String MESSAGE_GROUP_TABLE =
            "MESSAGE_GROUP_TABLE";
    public static final String MESSAGE_GROUP_TABLE_TITLE =
            "MESSAGE_GROUP_TABLE_TITLE";
    public static final String MESSAGE_GROUP_DIV =
            "MESSAGE_GROUP_DIV";
    public static final String MESSAGE_GROUP_SUMMARY_TEXT =
            "MESSAGE_GROUP_SUMMARY_TEXT";
    public static final String MESSAGE_GROUP_TEXT =
            "MESSAGE_GROUP_TEXT";
    public static final String MESSAGE_INFO =
            "MESSAGE_INFO";
    public static final String MESSAGE_ERROR =
            "MESSAGE_ERROR";
    public static final String MESSAGE_FATAL =
            "MESSAGE_FATAL";
    public static final String MESSAGE_WARN =
            "MESSAGE_WARN";
    public static final String MESSAGE_GROUP_INFO =
            "MESSAGE_GROUP_INFO";
    public static final String MESSAGE_GROUP_ERROR =
            "MESSAGE_GROUP_ERROR";
    public static final String MESSAGE_GROUP_FATAL =
            "MESSAGE_GROUP_FATAL";
    public static final String MESSAGE_GROUP_WARN =
            "MESSAGE_GROUP_WARN";

    // Currently this selector key is mapped to MASTHEAD_TEXT
    // to preserve compatibility for now. Ideally want every component
    // to reference their own defined selectors even if the definition
    // is the same.
    public static final String TIMESTAMP_TEXT =
            "TIMESTAMP_TEXT";

    // Common Tasks Section
    public static final String CTS_TASK =
            "CTS_TASK";
    public static final String CTS_GROUP =
            "CTS_GROUP";
    public static final String CTS_SECTION =
            "CTS_SECTION";
    public static final String CTS_HEADER =
            "CTS_HEADER";
    public static final String CTS_TASK_MORE =
            "CTS_TASK_MORE";
    public static final String CTS_SECTION_HELP =
            "CTS_SECTION_HELP";
    public static final String CTS_TASK_LEFT =
            "CTS_TASK_LEFT";
    public static final String CTS_TASK_CENTER =
            "CTS_TASK_CENTER";
    public static final String CTS_TASK_RIGHT =
            "CTS_TASK_RIGHT";
    public static final String CTS_TASK_BACKGROUND =
            "CTS_TASK_BACKGROUND";
    public static final String CTS_TASK_INFOPANEL =
            "CTS_TASK_INFOPANEL";
    public static final String CTS_TASK_CONTENT =
            "CTS_TASK_CONTENT";
    public static final String CTS_INFOPANEL_CLOSE =
            "CTS_INFOPANEL_CLOSE";
    public static final String CTS_TASK_INFO =
            "CTS_TASK_INFO";
    public static final String CTS_TASK_BULLET =
            "CTS_TASK_BULLET";
    public static final String CTS_TOP_BOX =
            "CTS_TOP_BOX";
    public static final String CTS_TEXT_BGROUND =
            "CTS_TEXT_BGROUND";
    public static final String CTS_LEFT_BOTTOM =
            "CTS_LEFT_BOTTOM";
    public static final String CTS_LEFT_TOP =
            "CTS_LEFT_TOP";
    public static final String CTS_RIGHT_BOTTOM =
            "CTS_RIGHT_BOTTOM";
    public static final String CTS_RIGHT_TOP =
            "CTS_RIGHT_TOP";
    public static final String CTS_RIGHT_BORDER =
            "CTS_RIGHT_BORDER";
    public static final String CTS_PADDING =
            "CTS_PADDING";
    public static final String CTS_BACKGROUND =
            "CTS_BACKGROUND";

    // ProgressBar
    public static final String PROGRESSBAR_DIV =
            "PROGRESSBAR_DIV";
    public static final String PROGRESSBAR_CONTAINER =
            "PROGRESSBAR_CONTAINER";
    public static final String PROGRESSBAR_DETERMINATE =
            "PROGRESSBAR_DETERMINATE";
    public static final String PROGRESSBAR_INDETERMINATE =
            "PROGRESSBAR_INDETERMINATE";
    public static final String PROGRESSBAR_STATUS =
            "PROGRESSBAR_STATUS";
    public static final String PROGRESSBAR_OPERATION =
            "PROGRESSBAR_OPERATION";
    public static final String PROGRESSBAR_RIGHTBUTTON =
            "PROGRESSBAR_RIGHTBUTTON";
    public static final String PROGRESSBAR_BOTTOMBUTTON =
            "PROGRESSBAR_BOTTOMBUTTON";
    public static final String PROGRESSBAR_BARLABEL =
            "PROGRESSBAR_BARLABEL";
    public static final String PROGRESSBAR_LOG =
            "PROGRESSBAR_LOG";
    public static final String PROGRESSBAR_BUSY =
            "PROGRESSBAR_BUSY";
    public static final String PROGRESSBAR_FAILED =
            "PROGRESSBAR_FAILED";
    public static final String PROGRESSBAR_FAILEDLABEL =
            "PROGRESSBAR_FAILEDLABEL";
    public static final String PROGRESSBAR_INDETERMINATE_PAUSED =
            "PROGRESSBAR_INDETERMINATE_PAUSED";
}

