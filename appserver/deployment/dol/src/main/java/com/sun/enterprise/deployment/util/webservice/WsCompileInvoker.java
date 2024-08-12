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

package com.sun.enterprise.deployment.util.webservice;

import java.io.IOException;
import java.util.HashMap;

import org.jvnet.hk2.annotations.Contract;

/**
 * This interface is used by the deploytool to generate webservice artifacts.
 * A client is expected to set the options and features using the add* methods before calling the generate* method
 *
 * Here is a code sample
 * <pre>
 *   SEIConfig cfg = new SEIConfig("WeatherWebService", "WeatherWebService", "endpoint",
 *       "endpoint.WeatherService", "endpoint.WeatherServiceImpl");
 *   WsCompileInvoker inv = WsCompileInvoker.getWsCompileInvoker(System.out);
 *   inv.addWsCompileOption(inv.TARGET_DIR, "/home/user/codesamples/weatherinfo/test");
 *   inv.addWsCompileOption(inv.MAP_FILE, "/home/user/codesamples/weatherinfo/test/map109.xml");
 *   inv.addWsCompileOption(inv.CLASS_PATH, "/home/user/codesamples/weatherinfo/service/build/classes");
 *   inv.addWsCompileFeature("wsi");
 *   inv.addWsCompileFeature("strict");
 *   inv.generateWSDL(cfg);
 * </pre>
 * If the client uses the same instance of WsCompileInvoker for multiple invocations of wscompile, then it is
 * the client's responsibility to empty the options (using the clearWsCompileOptionsAndFeatures() method) that
 * are present in this Map before using this Map for the next wscompile invocation
 */
@Contract
public abstract class WsCompileInvoker {

    /**
     * This Map holds all the options to be used while invoking the wscompile tool; the options are set by the client
     * of this interface using the setter methods available in this interface.
     **/
    protected HashMap wsCompileOptions = null;

    /**
     * This specifies the classpath to be used by the wscompile tool - ideally this should at least be set to
     * directory where the SEI package is present.
     */
    public static final String CLASS_PATH = "-classpath";

    /**
     * This specifies the target directory to be used by the wscompile tool to create the service artifacts - if
     * this is not specified, then the current directory will be used by the wscompile tool
     */
    public static final String TARGET_DIR = "-d";

    /**
     * This specifies the file name to be used by the wscompile tool for creating the 109 mapping file.
     */
    public static final String MAP_FILE = "-mapping";

    /**
     * This is used to generate WSDL and mapping files given information on SEI config; the caller sends in all the
     * required info in SEIConfig (info like name of webservice, interface name, package name etc) and this method
     * creates the equivalent jaxrpc-config.xml, invokes wscompile with -define option which will generate the WSDL
     * and the mapping file.
     *
     * @param SEIConfig containing webservice name, package name, namespace, SEI and its implementation
     */
    public abstract void generateWSDL(SEIConfig config) throws WsCompileInvokerException, IOException;

    /**
     * This is used to generate SEI and mapping files given information on WSDL file location, require package name.
     * The caller sends the required info on WSDL location, package name etc in WSDLConfig argument and this method
     * creates the equivalent jaxrpc-config,xml, invokes wscompile with -import option which will generate the SEI
     * and a template implementation of the SEI.
     * @param WSDLConfig containing webservice name, package name, and WSDL location
     */

    public abstract void generateSEI(WSDLConfig config) throws WsCompileInvokerException, IOException;

    /**
     * This is used to generate the non-portable client side artifacts given information on WSDL file location and the
     * package name; The caller sends the required info on WSDL location, package name etc in WSDLConfig argument and
     * this method creates the equivalent jaxrpc-config.xml, invokes wscompile with -gen:client option which will
     * generate all required non-portable client-side artifacts like JAXRPC stubs etc.
     * @param WSDLConfig containing webservice name, package name, and WSDL location
     */
    public abstract void generateClientStubs(WSDLConfig config) throws WsCompileInvokerException, IOException;

    /**
     * This is used to set an option to be used while invoking the wscompile tool; for example to use the -classpath
     * option for wscompile, the call will be setWsCompileOption(WsCompileInvoker.CLASS_PATH, "the_path");
     * For using wscompile options that are not defined in WsCompileInvoker, the 'option' argument of this call will be
     * the actual argument to be used ; for example, for using the '-s' option of wscompile, the call
     * to set the option will be setWsCompileOption("-s", "the_path_to_be_used");
     * For options that dont have an operand, the second argument can be null; for example, to invoke the wscompile tool
     * with verbose option, the -verbose option will be set with this call setWsCompileOption("-verbose", null);
     * @param String option  the wscompile option to be used
     * @param String operand the operand for the option; null if none;
     */
    public void addWsCompileOption(String option, String operand) {
        if(wsCompileOptions == null) {
            wsCompileOptions = new HashMap();
        }
        wsCompileOptions.put(option, operand);
    }

    /**
     * This is used to remove an option that was already set
     * @param String The option that has to be removed
     * @returns true If the option was found and removed
     * @returns false If the option was not found
     */
    public boolean removeWsCompileOption(String option) {
        if( (wsCompileOptions != null) && wsCompileOptions.containsKey(option) ) {
            wsCompileOptions.remove(option);
            return true;
        }
        return false;
    }

    /**
     * This is used to set a feature to be used while invoking the wscompile tool; for example to use the -f:wsi feature,
     * the call will be addWsCompileFeature("wsi")
     * This will prefix -f: to the feature and set it as part of the options Map with operand being null
     * @param String the feature to be set
     */
    public void addWsCompileFeature(String feature) {
        addWsCompileOption("-f:"+feature, null);
    }

    /**
     * This is used to remove a feature that was set; for example to remove "-f:strict" feature that was set before the
     * call will be removeWsCompileFeature("strict")
     * @param String the feature to be removed
     * @returns true if the feature was found and removed
     * @false false if the feature was not found
     */

    public boolean removeWsCompileFeature(String feature) {
        return(removeWsCompileOption("-f:"+feature));
    }

    /**
     * This is used to clear all options that have been set
     */
    public void clearWsCompileOptionsAndFeatures() {
        if(wsCompileOptions != null) {
            wsCompileOptions.clear();
        }
    }
}
