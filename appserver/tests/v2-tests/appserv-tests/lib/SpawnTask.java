/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.ExecTask;

/**
 * Extends the Apache Exec task to execute a given command in a separate thread and return control.
 * Supports the same attributes and elements as the exec task. This task is useful for running
 * server programs where you wish the server to be spawned off in a separate thread and execution
 * to proceed without blocking for the server.
 *
 * @author nandkumar.kesavan@sun.com
 * @see <a href="http://ant.apache.org/manual/CoreTasks/exec.html">Exec</a>
 */
public class SpawnTask extends ExecTask implements Runnable{

    /**
     * Run the command in a new thread
     */
    public void execute() throws BuildException {

        //Instantiate a new thread and run the command in this thread.
        Thread taskRunner = new Thread(this);
        taskRunner.start();

    }

    public void run() {

        //Run the parent ExecTask in a separate thread
        super.execute();

    }

}




