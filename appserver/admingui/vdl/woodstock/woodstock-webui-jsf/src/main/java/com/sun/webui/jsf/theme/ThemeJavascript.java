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
 * This class contains JS related theme constants.
 * FIXME: Eventually these need to move to a theme-based resource file.
 */
public final class ThemeJavascript {

    /**
     * Cannot be instanciated.
     */
    private ThemeJavascript() {
    }

    /**
     * A JS file that contains DOJO functions.
     */
    public static final String DOJO = "dojo";

    /**
     * A properties file key whose value is a space separated list of
     * keys identifying JS files that are included in every page.
     */
    public static final String GLOBAL = "global";

    /**
     * A JS file that contains JSON functions.
     */
    public static final String JSON = "json";

    /**
     * A JS file that contains JSF Extensions functions.
     */
    public static final String JSFX = "jsfx";

    /**
     * The path to module resources.
     */
    public static final String MODULE_PATH = "modulePath";

    /**
     * The module to prefix to all resources.
     */
    public static final String MODULE_PREFIX = "modulePrefix";

    /**
     * A JS file that contains Prototype functions.
     */
    public static final String PROTOTYPE = "prototype";

    /**
     * A JS file that contains helper functions.
     */
    public static final String HELPER = "helper";

    /**
     * A JS file that contains functions for manipulating
     * the AddRemove component.
     *
     * @deprecated
     */
    public static final String ADD_REMOVE = "global";

    /**
     * A JS file that contains general functions used by
     * simple components.
     *
     * @deprecated
     */
    public static final String BASIC = "global";

    /**
     * A JS file that contains functions for manipulating
     * the Calendar component.
     *
     * @deprecated
     */
    public static final String CALENDAR = "global";

    /**
     * A JS file that contains functions for manipulating
     * cookies.
     *
     * @deprecated
     */
    public static final String COOKIE = "global";

    /**
     * A JS file that contains functions for manipulating
     * the common tasks section component.
     *
     * @deprecated
     */
    public static final String COMMONTASKSSECTION = "global";

    /**
     * A JS file that contains DynaFaces functions.
     *
     * @deprecated
     */
    public static final String DYNAFACES = "global";

    /**
     * A JS file that contains functions for manipulating
     * the EditableList component.
     *
     * @deprecated
     */
    public static final String EDITABLE_LIST = "global";

    /**
     * A JS file that contains functions for manipulating
     * the FileChooser component.
     *
     * @deprecated
     */
    public static final String FILE_CHOOSER = "global";

    /**
     * A JS file that contains functions for maintaining
     * the focus within the page.
     *
     * @deprecated
     */
    public static final String FOCUS_COOKIE = "global";

    /**
     * A JS prefix for locating function names.
     *
     * @deprecated Use MODULE_PREFIX.
     */
    public static final String JS_PREFIX = "modulePrefix";

    /**
     * A JS file that contains functions for manipulating
     * the OrderableList component.
     *
     * @deprecated
     */
    public static final String ORDERABLE_LIST = "global";

    /**
     * A JS file that contains functions for manipulating
     * the ProgressBar component.
     *
     * @deprecated
     */
    public static final String PROGRESSBAR = "global";

    /**
     * A JS file that contains functions for manipulating
     * the ProgressBar component based on JSF Extensions.
     *
     * @deprecated
     */
    public static final String PROGRESSBAR_DYNAFACES = "global";

    /**
     * A JS file that contains functions for maintaining
     * the scroll position within a page.
     *
     * @deprecated
     */
    public static final String SCROLL_COOKIE = "global";

    /**
     * A JS file that contains functions for manipulating
     * the Scheduler component.
     *
     * @deprecated
     */
    public static final String SCHEDULER = "global";

    /**
     * A JS file that contains functions for manipulating
     * component styles.
     *
     * @deprecated
     */
    public static final String STYLESHEET = "global";

    /**
     * A JS file that contains functions for manipulating
     * the Table component.
     *
     * @deprecated
     */
    public static final String TABLE = "global";

    /**
     * A JS file that contains functions for manipulating
     * the Tree component.
     *
     * @deprecated
     */
    public static final String TREE = "global";

    /**
     * A JS file that contains functions for manipulating
     * the Wizard component.
     *
     * @deprecated
     */
    public static final String WIZARD = "global";

    /**
     * A JS file that contains common functions for widgets.
     *
     * @deprecated
     */
    public static final String WIDGET = "global";

    /**
     * The location of the widget module assigned via Dojo.
     *
     * @deprecated Use MODULE_PATH.
     */
    public static final String WIDGET_MODULE = "modulePath";

}
