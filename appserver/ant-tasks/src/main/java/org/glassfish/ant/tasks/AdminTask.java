/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.ant.tasks;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

public class AdminTask extends Task {

    // default value for installdir?
    String installDir, command;

    public AdminTask() {
        setCommand("");
    }

    public void setTarget(String target) {
        optionIgnored("target");
    }

    public void setInstallDir(String installDir) {
        this.installDir = installDir;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public void addCommandParameter(String name, String value) {
        command += " --" + name + "=" + value;
    }

    public void addCommandOperand(String value) {
        command += " " + value;
    }

    public void setUser(String user) {
        addCommandParameter("user", user);
    }

    public void setPasswordFile(String passwordfile) {
        addCommandParameter("passwordfile", passwordfile);
    }

    public void setHost(String host) {
        addCommandParameter("host", host);
    }

    public void setPort(String port) {
        addCommandParameter("port", port);
    }

    public String getInstallDir() {
        if (installDir == null) {
            String home = getProject().getProperty("asinstall.dir");
            if (home != null) {
                return home;
            }
        }
        return installDir;
    }

    public void execute() throws BuildException {
        execute(this.command);
    }

    public void execute(String commandExec) throws BuildException {
        log ("Running command " + commandExec);
        String installDirectory = getInstallDir();
        if (installDirectory == null) {
            log("Install Directory of application server not known. Specify either the installDir attribute or the asinstall.dir property",
                Project.MSG_WARN);
            return;
        }

        File f = new File(installDirectory);
        if (!f.exists()) {
            log("Glassfish install directory : " + installDirectory + " not found. Specify the correct directory as installDir attribute or asinstall.dir property");
            return;
        }
        BufferedReader error = null;
        BufferedReader input = null;
        try {
            File asadmin = getAsAdmin(f);
            Process pr = Runtime.getRuntime().exec(asadmin.getAbsolutePath() + " " + commandExec);

            error = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
            String errorLine=null;
            while((errorLine=error.readLine()) != null) {
                log(errorLine);
            }

            input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String inputLine=null;
            while((inputLine=input.readLine()) != null) {
                log(inputLine);
            }

            int exitVal = pr.waitFor();
            if (exitVal != 0)
                log("asadmin command exited with error code "+exitVal);

        } catch (Exception ex) {
            log(ex.getMessage());
        }
        finally {
            if (error != null) {
                try {
                    error.close();
                }
                catch (Exception e) {
                    // nothing can be or should be done...
                }
            }
            if (input != null) {
                try {
                    input.close();
                }
                catch (Exception e) {
                    // nothing can be or should be done...
                }
            }
        }
    }

    void optionIgnored(String option) {
        log("Option Ignored : " + option);
    }

    private File getAsAdmin(File installDir) {
        String osName = System.getProperty("os.name");
        File binDir = new File(installDir, "bin");
        if (osName.indexOf("Windows") == -1) {
            return new File(binDir, "asadmin");
        } else {
            return new File(binDir, "asadmin.bat");
        }

    }
}
