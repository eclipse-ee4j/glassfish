/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.server.logging;

import java.util.ArrayList;
import java.util.Arrays;
import com.sun.logging.LogDomains;


/**
 *  A Simple No Brainer Utility to map the Module Name to Logger Name..
 *
 *  @author Hemanth Puttaswamy
 */
public class ModuleToLoggerNameMapper {

    /*the sequence of each module entry in this table is important. always place
      module with longer loggername in the first.
      This table makes it possible to support more than 1 loggernames for each
      module. Each module log level's change through admin will result in level
      change in all its logger objects.

      The log module part is consistent with what module is defined in
      LogService.java (package com.sun.enterprise.config.serverbeans)
      refer also: ModuleLogLevels.java & ServerTags.java

      In sun-domain.dtd:
      <!ATTLIST module-log-levels
         root %log-level; "INFO"
         server %log-level; "INFO"
         ejb-container %log-level; "INFO"
         cmp-container %log-level; "INFO"
         mdb-container %log-level; "INFO"
         web-container %log-level; "INFO"
         classloader %log-level; "INFO"
         configuration %log-level; "INFO"
         naming %log-level; "INFO"
         security %log-level; "INFO"
         jts %log-level; "INFO"
         jta %log-level; "INFO"
         admin %log-level; "INFO"
         deployment %log-level; "INFO"
         verifier %log-level; "INFO"
         jaxr %log-level; "INFO"
         jaxrpc %log-level; "INFO"
         saaj %log-level; "INFO"
         corba %log-level; "INFO"
         javamail %log-level; "INFO"
         jms %log-level; "INFO"
         connector %log-level; "INFO"
         jdo %log-level; "INFO"
         cmp %log-level; "INFO"
         util %log-level; "INFO"
         resource-adapter %log-level; "INFO"
         synchronization %log-level; "INFO"
         node-agent %log-level; "INFO"
         self-management %log-level; "INFO"
         group-management-service %log-level; "INFO"
         management-event %log-level; "INFO">
    */
    private static final Object[][] ModuleAndLoggerTable = {
        {"admin",          new String[] { LogDomains.ADMIN_LOGGER } },    //admin
        {"classloader",    new String[] { LogDomains.LOADER_LOGGER} },    //classloader
        {"cmp",            new String[] { LogDomains.CMP_LOGGER} },
        {"cmp-container",  new String[] { LogDomains.CMP_LOGGER} }, //todo: verify with "cmp"
        {"configuration",  new String[] { LogDomains.CONFIG_LOGGER} },
        {"connector",      new String[] { LogDomains.RSR_LOGGER} },
        {"resource-adapter", new String[] { LogDomains.RSR_LOGGER} },//todo: verify with "connector"
        {"corba",          new String[] { LogDomains.CORBA_LOGGER} },
        {"deployment",     new String[] { LogDomains.DPL_LOGGER} },
        {"ejb-container",  new String[] { LogDomains.EJB_LOGGER} },
        {"javamail",       new String[] { LogDomains.JAVAMAIL_LOGGER} },
        {"jaxr",           new String[] { LogDomains.JAXR_LOGGER} },
        {"jaxrpc",         new String[] { LogDomains.JAXRPC_LOGGER} },
        {"jdo",            new String[] { LogDomains.JDO_LOGGER} },
        {"jms",            new String[] { LogDomains.JMS_LOGGER, "javax.resourceadapter.mqjmsra"} },
        {"jta",            new String[] { LogDomains.JTA_LOGGER} },
        {"jts",            new String[] { LogDomains.TRANSACTION_LOGGER} },
        {"mdb-container",  new String[] { LogDomains.MDB_LOGGER} },
        //{"management-event"  //todo: management-event module owner needs to impl this.
        {"naming",         new String[] { LogDomains.JNDI_LOGGER} },
        {"saaj",           new String[] { LogDomains.SAAJ_LOGGER} },
        {"security",       new String[] { LogDomains.SECURITY_LOGGER} },
        {"self-management",new String[] { LogDomains.SELF_MANAGEMENT_LOGGER} },
        {"synchronization",new String[] { "javax.ee.enterprise.system.tools.synchronization"} },
        {"web-container",  new String[] { LogDomains.WEB_LOGGER,
                                          "org.apache.catalina",
                                          "org.apache.coyote","org.apache.jasper"
                                        } },
        {"group-management-service", new String[] { LogDomains.GMS_LOGGER} },
        {"node-agent",     new String[] { "javax.ee.enterprise.system.nodeagent" } },
        {"util",           new String[] { LogDomains.UTIL_LOGGER } },
        {"core",           new String[] { LogDomains.CORE_LOGGER} },
        {"server",         new String[] { LogDomains.SERVER_LOGGER} },
    };


    /**
     * @loggername  the logname
     * @return the module name the logger is for.
     */
    public static String getModuleName(String loggerName) {
        for (int i=0; i<ModuleAndLoggerTable.length; i++) {
            Object[] dim = ModuleAndLoggerTable[i];
            String   modName = (String)dim[0];
            String[] loggerNames = (String[]) dim[1];
            for (int j=0; loggerNames!=null && j<loggerNames.length;j++) {
                String name=loggerNames[j];
                if (loggerName.equals(name))
                    return modName;
            }
        }
        return null;
    }


    /**
     * @moduleName   the log module name (eg. "admin");
                     if null, it means all modules.
     * @return       the logger names for this module; size of returned String[] >=1.
     */
    public static String[] getLoggerNames( String moduleName ) {
        ArrayList result = new ArrayList();
        for (int i=0; i<ModuleAndLoggerTable.length; i++) {
            Object[] dim = ModuleAndLoggerTable[i];
            String   modName = (String)dim[0];
            String[] loggerNames = (String[]) dim[1];
            if (loggerNames!=null) {
                if (moduleName == null) {  //we return all AS module loggers in this case
                    result.addAll(Arrays.asList(loggerNames) );
                } else if (moduleName.equals(modName)) {
                    result.addAll( Arrays.asList(loggerNames) );
                    break;
                }
            }
        }
        String[] lNames = new String[ result.size()];
        return (String[])result.toArray(lNames);
    }


}
