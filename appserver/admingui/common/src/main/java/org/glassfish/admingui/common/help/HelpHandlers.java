/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admingui.common.help;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import java.util.Locale;

import org.glassfish.admingui.common.handlers.PluginHandlers;
import org.glassfish.admingui.connector.Index;
import org.glassfish.admingui.connector.IntegrationPoint;
import org.glassfish.admingui.connector.TOC;
import org.glassfish.admingui.plugin.ConsolePluginService;


/**
 *  <p>These handlers help make the help system work.</p>
 *
 *  @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class HelpHandlers {

    /**
     * This handler provides access to {@link IntegrationPoint}s for the
     * requested key.
     *
     * @param handlerCtx The <code>HandlerContext</code>.
     */
    @Handler(id="getHelpTOC",
        input={
            @HandlerInput(name="locale", type=Locale.class)},
        output={
            @HandlerOutput(name="toc", type=TOC.class)})
    public static void getHelpTOC(HandlerContext handlerCtx) {
        // Get the desired Locale and the ConsolePluginService...
        Locale locale = (Locale) handlerCtx.getInputValue("locale");
        ConsolePluginService cps = PluginHandlers.getPluginService(
                handlerCtx.getFacesContext());

        // Determine the correct locale for the path...
        String localePath = getHelpLocalePath(locale, cps);

        handlerCtx.setOutputValue("toc", cps.getHelpTOC(localePath));
    }


    /**
     * This handler provides access to {@link IntegrationPoint}s for the
     * requested key.
     *
     * @param handlerCtx The <code>HandlerContext</code>.
     */
    @Handler(id="getHelpIndex",
            input={
            @HandlerInput(name="locale", type=Locale.class)},
        output={
            @HandlerOutput(name="index", type=Index.class)})
    public static void getHelpIndex(HandlerContext handlerCtx) {
        // Get the desired Locale and the ConsolePluginService...
        Locale locale = (Locale) handlerCtx.getInputValue("locale");
        ConsolePluginService cps = PluginHandlers.getPluginService(
                handlerCtx.getFacesContext());

        // Determine the correct locale for the path...
        String localePath = getHelpLocalePath(locale, cps);

        handlerCtx.setOutputValue("index", cps.getHelpIndex(localePath));
    }

    /**
     * <p> This method calculates the correct locale portion of the help path
     *     for the requested locale.</p>
     */
    private static String getHelpLocalePath(Locale locale, ConsolePluginService cps) {
        // Use common toc.xml to validate locale path...
        ClassLoader loader = cps.getModuleClassLoader("common");
        String path = PluginHandlers.getHelpPathForResource(
                "toc.xml", locale, loader);
        if (path == null) {
            // Not found, try the system default...
            path = PluginHandlers.getHelpPathForResource(
                "toc.xml", Locale.getDefault(), loader);
            if (path == null) {
                // Default to "en"
                path = "/en/";
            }
        }

        // Return the locale-portion of the path
        return path.substring(1, path.indexOf('/', 1));
    }
}
