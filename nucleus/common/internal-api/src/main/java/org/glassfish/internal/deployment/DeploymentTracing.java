/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.internal.deployment;

import com.sun.enterprise.module.HK2Module;
import com.sun.enterprise.module.ModuleState;
import com.sun.enterprise.module.ModulesRegistry;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tracing facility for all the deployment backend activities.
 *
 * @author Jerome Dochez
 */
public class DeploymentTracing {

    public static enum Mark {
        ARCHIVE_OPENED,
        ARCHIVE_HANDLER_OBTAINED,
        INITIAL_CONTEXT_CREATED,
        APPINFO_PROVIDED,
        DOL_LOADED,
        APPNAME_DETERMINED,
        TARGET_VALIDATED,
        CONTEXT_CREATED,
        DEPLOY,
        CLASS_LOADER_HIERARCHY,
        PARSING_DONE,
        CLASS_LOADER_CREATED,
        CONTAINERS_SETUP_DONE,
        PREPARE,
        PREPARED,
        LOAD,
        LOAD_EVENTS,
        LOADED,
        START,
        START_EVENTS,
        STARTED,
        REGISTRATION

    }

    public static enum ModuleMark {
        PREPARE,
        PREPARE_EVENTS,
        PREPARED,
        LOAD,
        LOADED,
        START,
        STARTED

    }

    public static enum ContainerMark {
        SNIFFER_DONE,
        BEFORE_CONTAINER_SETUP,
        AFTER_CONTAINER_SETUP,
        GOT_CONTAINER,
        GOT_DEPLOYER,
        PREPARE,
        PREPARED,
        LOAD,
        LOADED,
        START,
        STARTED
    }

    private abstract class Event {
        final long inception = System.currentTimeMillis();

        long elapsedInMs() {
            return inception - DeploymentTracing.this.inception;
        }
        abstract void print(PrintStream ps);
    }

    private final class GlobalEvent extends Event {
        final Mark mark;

        private GlobalEvent(Mark mark) {
            this.mark = mark;
        }

        void print(PrintStream ps) {
            ps.println("Mark " + mark.toString() + " at " + elapsedInMs());
        }
    }

    private class ContainerEvent extends Event{
        final ContainerMark mark;
        final String name;

        private ContainerEvent(ContainerMark mark, String name) {
            this.mark = mark;
            this.name = name;
        }
        void print(PrintStream ps) {
            ps.println("Container : " + name + " Mark " + mark.toString() + " at " + elapsedInMs());
        }
    }

    private class ModuleEvent extends Event {
        final ModuleMark mark;
        final String moduleName;

        private ModuleEvent(ModuleMark mark, String moduleName) {
            this.mark = mark;
            this.moduleName = moduleName;
        }
        void print(PrintStream ps) {
            ps.println("HK2Module " +  moduleName + " Mark " + mark.toString() + " at " + elapsedInMs());
        }
    }

    final long inception = System.currentTimeMillis();
    final List<Event> events = new ArrayList<Event>();

    public long elapsed() {
        return System.currentTimeMillis() - inception;
    }

    public void addMark(Mark mark) {
        events.add(new GlobalEvent(mark));
    }

    public void addContainerMark(ContainerMark mark, String name) {
        events.add(new ContainerEvent(mark, name));
    }

    public void addModuleMark(ModuleMark mark, String moduleName) {
        events.add(new ModuleEvent(mark, moduleName));
    }

    public void print(PrintStream ps) {
        for (int i=0;i<events.size(); i++) {
            events.get(i).print(ps);
        }

    }

    public static void printModuleStatus(ModulesRegistry registry, Level level, Logger logger)
    {
        if (!logger.isLoggable(level)) {

            return;
        }
        int counter=0;

        StringBuilder sb = new StringBuilder("HK2Module Status Report Begins\n");
        // first started :

        for (HK2Module m : registry.getModules()) {
            if (m.getState()== ModuleState.READY) {
                sb.append(m).append("\n");
                counter++;
            }
        }
        sb.append("there were " + counter + " modules in ACTIVE state");
        sb.append("\n");
        counter=0;
        // then resolved
        for (HK2Module m : registry.getModules()) {
            if (m.getState()== ModuleState.RESOLVED) {
                sb.append(m).append("\n");
                counter++;
            }
        }
        sb.append("there were " + counter + " modules in RESOLVED state");
        sb.append("\n");
        counter=0;
        // finally installed
        for (HK2Module m : registry.getModules()) {
            if (m.getState()!= ModuleState.READY && m.getState()!=ModuleState.RESOLVED) {
                sb.append(m).append("\n");
                counter++;
            }
        }
        sb.append("there were " + counter + " modules in INSTALLED state");
        sb.append("HK2Module Status Report Ends");
        logger.log(level, sb.toString());
    }
}
