/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.config.util;

import java.util.Properties;
import org.glassfish.api.Param;

/**
 * Parameters for the remote instance register instance command, which includes params from _create-node and
 * _register-instance
 *
 * @author Jennifer Chou
 */
public class InstanceRegisterInstanceCommandParameters extends RegisterInstanceCommandParameters {

    @Param(name = ParameterNames.PARAM_NODEDIR, optional = true)
    public String nodedir = null;
    @Param(name = ParameterNames.PARAM_NODEHOST, optional = true)
    public String nodehost = null;
    @Param(name = ParameterNames.PARAM_INSTALLDIR, optional = true)
    public String installdir = null;
    @Param(name = ParameterNames.PARAM_TYPE, optional = true, defaultValue = "CONFIG")
    public String type = "CONFIG";
    @Param(name = ParameterNames.PARAM_SYSTEMPROPERTIES, optional = true, separator = ':')
    public Properties systemProperties;
    /*@Param(name = ParameterNames.PARAM_SSHPORT, optional = true)
    public String sshPort = "-1";
    @Param(name = ParameterNames.PARAM_SSHHOST, optional = true)
    public String sshHost = null;
    @Param(name = ParameterNames.PARAM_SSHUSER, optional = true)
    public String sshuser = null;
    @Param(name = ParameterNames.PARAM_SSHKEYFILE, optional = true)
    public String sshkeyfile;
    @Param(name = ParameterNames.PARAM_SSHPASSWORD, optional = true)
    public String sshpassword;
    @Param(name = ParameterNames.PARAM_SSHKEYPASSPHRASE, optional = true)
    public String sshkeypassphrase;*/

    /* instance params */
    //@Param(name = "resourceref", optional = true)
    //public  List<String> resourceRefs;
    //@Param(name = "applicationref", optional = true)
    //public  List<String> appRefs;

    public static class ParameterNames {

        //public static final String PARAM_RESOURCEREF = "resourceref";
        //public static final String PARAM_APPLICATIONREF = "applicationref";
        public static final String PARAM_NODEDIR = "nodedir";
        public static final String PARAM_NODEHOST = "nodehost";
        public static final String PARAM_INSTALLDIR = "installdir";
        public static final String PARAM_TYPE = "type";
        public static final String PARAM_SYSTEMPROPERTIES = "systemproperties";
        /*public static final String PARAM_SSHPORT = "sshport";
        public static final String PARAM_SSHHOST = "sshhost";
        public static final String PARAM_SSHUSER = "sshuser";
        public static final String PARAM_SSHKEYFILE = "sshkeyfile";
        public static final String PARAM_SSHPASSWORD = "sshpassword";
        public static final String PARAM_SSHKEYPASSPHRASE = "sshkeypassphrase";*/
    }

}
