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

package admin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/*
 * Base class for SSH Dev tests
 * @author Yamini K B
 * @author Joe Di Pol
 */
public abstract class SshBaseDevTest extends AdminBaseDevTest {
    static final String SSH_DIRECTORY = System.getProperty("user.home")
                                + File.separator + ".ssh" + File.separator;

    // Properties that may be passed into test

    // Host to create remote instances on
    static final String SSH_HOST_PROP = "ssh.host";

    // SSH user to use when connecting to ssh.host. Defaults to user running test
    static final String SSH_USER_PROP = "ssh.user";

    // GlassFish installdir on ssh.host.
    static final String SSH_INSTALLDIR_PROP = "ssh.installdir";

    // Alternative to SSH_INSTALLDIR_PROP. If you provide this prefix
    // then SSHNodeTest will generate a unique install directory using this
    // prefix. Typically used with ssh.doinstall=true.
    static final String SSH_INSTALLPREFIX_PROP = "ssh.installprefix";

    // Location of nodedir to use. If not set use default location.
    static final String SSH_NODEDIR_PROP = "ssh.nodedir";

    // SSH password to use when connecting to ssh.host. Not needed if
    // public key authentication is already set up.
    static final String SSH_PASSWORD_PROP = "ssh.password";

    // Controlls whether or not the SSHNodeTest does an install for you
    // onto ssh.host.
    static final String SSH_DOINSTALL_PROP = "ssh.doinstall";

    // Used by SetupSshTest
    static final String SSH_CONFIGURE_PROP = "ssh.configure";

    /**
     * Modify asadmin common options to include --interactive=false
     */
    void disableInteractiveMode() {
        String s = antProp("as.props");

        String newProps = s + " --interactive=false";
        System.setProperty("as.props", newProps);
        System.out.println("Updated common options = " + antProp("as.props"));
    }
}
