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

/*
 * $Id$
 */
package com.sun.ts.tests.common.vehicle.ejbembed;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

public final class EmbeddableEJBProcess {
  private static final Logger logger = Logger
      .getLogger("com.sun.ts.tests.common.vehicle.ejbembed");

  private String javaHome = System.getProperty("java.home");

  private String javaCmd = javaHome + File.separator + "bin" + File.separator
      + "java";

  private List<String> cmdList = new ArrayList<String>();

  private Process ps;

  private String tsHome;

  private String tsRunClasspath;

  private String classpathSuffix;

  private String testClassName;

  private String[] args;

  private Properties props;

  public EmbeddableEJBProcess(String[] args, Properties props) {
    this.args = args;
    this.props = props;

    this.tsHome = props.getProperty("ts.home");
    this.tsRunClasspath = props.getProperty("ts.run.classpath");
    this.classpathSuffix = getOpts("-classpathSuffix");
    this.testClassName = props.getProperty("test_classname");

    cmdList.add(javaCmd);
    constructClassPathOption();
    constructSystemProps();
    cmdList.add(testClassName);

    ProcessBuilder pb = new ProcessBuilder(cmdList).redirectErrorStream(true);

    logger.info(String.format("Starting process: %s%n", pb.command()));
    logger.fine(String.format("%s%nEnvironment:", pb.environment()));

    try {
      ps = pb.start();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void constructSystemProps() {
    cmdList.add("-Dts.ejb.embed=true");
    cmdList.add("-DtestName=" + props.getProperty("testName"));
  }

  private String getOpts(String tag) {
    String val = null;
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals(tag)) {
        val = args[i + 1];
        break;
      }
    }
    logger.fine(String.format("Got arg: %s %s", tag, val));
    return val;
  }

  /**
   * Gets the pkg dir (relative to ts.home/dist) from the testClassName, which
   * is in the form of
   * com.sun.ts.tests.ejb30.lite.packaging.embed.provider.Client, for example.
   */
  private String getPkgDirFromTestClass() {
    String pkgDir = testClassName.substring(0, testClassName.lastIndexOf('.'));
    pkgDir = testClassName.replace('.', File.separatorChar);
    return pkgDir;
  }

  /**
   * Constructs a classpath option for launching the new java process. The value
   * is in the form of: "-classpath /a/b/c.jar:/1/2/3.jar"
   */
  private void constructClassPathOption() {
    StringBuilder sb = new StringBuilder();
    sb.append(tsRunClasspath);
    sb.append(File.pathSeparator);

    sb.append(tsHome).append(File.separator).append("dist")
        .append(File.separator);
    sb.append(getPkgDirFromTestClass()).append(File.separator);
    sb.append("*.jar");

    if (classpathSuffix != null) {
      sb.append(File.pathSeparator);
      sb.append(classpathSuffix);
    }

    cmdList.add("-classpath");
    cmdList.add(sb.toString());
  }

  public String getTestResult() {
    StringBuilder sb = new StringBuilder();
    InputStream is = null;
    try {
      is = ps.getInputStream();
      InputStreamReader isr = new InputStreamReader(is);
      BufferedReader br = new BufferedReader(isr);
      String line;
      while ((line = br.readLine()) != null) {
        sb.append(line).append(System.getProperty("line.separator"));
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException ignore) {
        }
      }
    }

    return sb.toString();
  }

}
