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

package com.sun.enterprise.admin.servermgmt;

import java.util.Properties;

/**
 */
public interface InstancesManager {
    /**
     * Creates a server instance.
     *
     * @throws InstanceException This exception is thrown if - the instance already exists. - an invalid or insufficient
     * config. is supplied. - an exception occurred during instance creation.
     */
    public void createInstance() throws InstanceException;

    /**
     * Deletes an instance identified by the given name. (Should we stop the instance before deleting the instance?)
     *
     * @throws InstanceException This exception is thrown if - the instance doesnot exist. - an exception occurred while
     * deleting the instance.
     */
    public void deleteInstance() throws InstanceException;

    /**
     * Starts the instance.
     *
     * @param startParams
     * @throws InstanceException
     */
    public Process startInstance() throws InstanceException;

    /**
     * Starts the instance.
     *
     * @param interativeOptions which may be used for security, these paramters are passed in on the standard input stream
     * of the executing process
     * @throws InstanceException
     */
    public Process startInstance(String[] interativeOptions) throws InstanceException;

    /**
     * Starts the instance.
     *
     * @param interativeOptions which may be used for security, these paramters are passed in on the standard input stream
     * of the executing process
     * @param commandLineArgs is additional commandline arguments that are to be appended to the processes commandline when
     * it starts
     * @throws InstanceException
     */
    public Process startInstance(String[] interativeOptions, String[] commandLineArgs) throws InstanceException;

    /**
     * Starts the instance.
     *
     * @param interativeOptions which may be used for security, these paramters are passed in on the standard input stream
     * of the executing process
     * @param commandLineArgs is additional commandline arguments that are to be appended to the processes commandline when
     * it starts
     * @param envProps properties to be added to System
     * @throws InstanceException
     */
    public Process startInstance(String[] interativeOptions, String[] commandLineArgs, Properties envProps) throws InstanceException;

    /**
     * Stops the instance.
     *
     * @throws InstanceException
     */
    public void stopInstance() throws InstanceException;

    /**
     * Lists all the instances.
     */
    public String[] listInstances() throws InstanceException;

    /**
     * Returns status of an instance.
     */
    public int getInstanceStatus() throws InstanceException;

    /**
     * @return true if the instance requires a restart for some config changes to take effect, false otherwise.
     */
    boolean isRestartNeeded() throws InstanceException;

    public String getNativeName();

    /**
     * Trys to stop the instance with the specified timeout. Returns true if success; false if failure
     *
     * @throws InstanceException
     */
    public boolean stopInstanceWithinTime(int timeout) throws InstanceException;

    public void killRelatedProcesses() throws InstanceException;
}
