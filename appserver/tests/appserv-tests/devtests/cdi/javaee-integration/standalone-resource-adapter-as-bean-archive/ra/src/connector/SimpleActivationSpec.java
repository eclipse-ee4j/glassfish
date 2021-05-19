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

package connector;

import jakarta.resource.spi.ActivationSpec;
import jakarta.resource.spi.ResourceAdapter;

/**
 * This class is supposed to implemented as a JavaBean
 * so that the app server can instantiate and configure
 * it at the runtime.
 *
 * @author        Qingqing Ouyang
 */
public class SimpleActivationSpec
    implements ActivationSpec, java.io.Serializable {

    private String destinationName;
    private String destinationType;
    private String testProp;
    private Integer testIntegerProp;
    private ResourceAdapter resourceadapter;

    /**
     * Default constructor.
     */
    public SimpleActivationSpec () {}

    public String getDestinationName () {
        return this.destinationName;
    }

    public void setDestinationName (String name) {
        debug("setDestinationName() called... name = " + name);
        this.destinationName = name;
    }

    public String getDestinationType() {
        return this.destinationType;
    }

    public void setDestinationType (String type) {
        debug("setDestinationType () called... type = " + type);
        this.destinationType= type;
    }

    public String getTestProp() {
        return this.testProp;
    }

    public void setTestProp (String testProp) {
        debug("setTestProp () called... testProp = " + testProp);
        this.testProp = testProp;
    }

    public Integer getTestIntegerProp() {
        return this.testIntegerProp;
    }

    public void setTestIntegerProp (Integer testProp1) {
        debug("setTestIntegerProp () called... testIntegerProp = " + testProp1);
        this.testIntegerProp = testProp1;
    }

    public ResourceAdapter getResourceAdapter () {
        debug("getResourceAdapter() called... ");
        return this.resourceadapter;
    }

    public void setResourceAdapter (ResourceAdapter ra) {
        debug("setResourceAdapter() called... ra = " + ra);
        this.resourceadapter = ra;
    }

    public void validate() {
      if (this.testIntegerProp.intValue() == 2 && this.testProp.equals("WrongValue")) {
        //valid
      } else {
        throw new RuntimeException("Invalid Configuration testIntegerProp: " + this.testIntegerProp.intValue() + " testProp " + this.testProp);

      }

   }

    private void debug (String message)
    {
        System.out.println("[SimpleActivationSpec] ==> " + message);
    }
}
