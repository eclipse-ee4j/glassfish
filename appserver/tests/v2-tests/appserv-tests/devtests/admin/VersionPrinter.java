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

import com.sun.appserv.server.util.Version;

public class VersionPrinter {

  public static void main(final String[] arg) {
    try {
       System.out.println("Full Version: " + Version.getFullVersion());
       System.out.println("Abbreviated Version: " + Version.getAbbreviatedVersion());
       System.out.println("Build Version: " + Version.getBuildVersion());
       System.out.println("Major Version: " + Version.getMajorVersion());
       System.out.println("Minor Version: " + Version.getMinorVersion());
       System.out.println("Product Name: " + Version.getProductName());
     } catch (final NoClassDefFoundError e) {
          System.out.println("Please run this class as: java -cp .:{install-dir}/lib/appserv-ext.jar:${install-dir}/lib/appserv-se.jar:${install-dir}/lib/appserv-rt.jar\nwhere ${install-dir} is the installation location");
    }
  }
}
