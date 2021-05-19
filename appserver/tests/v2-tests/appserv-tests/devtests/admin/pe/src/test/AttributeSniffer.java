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

/**
 *
 * @author bnevins
 */
package test;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import java.util.*;
import java.io.IOException;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import com.sun.enterprise.util.*;

public class AttributeSniffer extends LifeCycle
{
    public AttributeSniffer()
    {
    }

    String testInternal() throws JMException, IOException
    {
        title("AttributeSniffer");

        for(String classname : classnames)
        {
            System.out.println(classname);
            MBeanInfo info = getMBeanInfo(classname);
            MBeanAttributeInfo[] atts = info.getAttributes();

            for(MBeanAttributeInfo ainfo : atts)
            {
                System.out.println("ATTRIBUTE --> name=[" + ainfo.getName() + "], type=[" + ainfo.getType() + "], Is Writable: " + ainfo.isWritable());
            }
        }
        return ( SimpleReporterAdapter.PASS );
    }

    private static final String[] classnames = {
            "testmbeans.OneClassDynamicMBean",
            "testmbeans.MicrowaveOvenImpl",
            "testmbeans.SimpleStandard",
            "testmbeans.PrimitiveStandard",
    };

    //private int numIter = 500;
    //private String namePrefix;
    //private String alphabet = "abcdefghijklmnopqrstuvwxyz";
    //private Random rnd;
    //private boolean interactive = false;
}
