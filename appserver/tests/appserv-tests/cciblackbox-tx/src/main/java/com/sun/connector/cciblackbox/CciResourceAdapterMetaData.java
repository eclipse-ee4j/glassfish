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

package com.sun.connector.cciblackbox;

import jakarta.resource.cci.ResourceAdapterMetaData;

/**
 * this implementation class provides info about the capabilities of a
 * resource adapter implementation.
 * @author Sheetal Vartak
 */
public class CciResourceAdapterMetaData implements ResourceAdapterMetaData {

  private String vendorName = "Oracle";

  private String adapterVersion = "1.0";

  private String specVersion = "1.0";

  private String adapterName = "CCI Resource Adapter";

  private String description = "Resource Adapter for CCI";

  public CciResourceAdapterMetaData() {
  }

  public String getAdapterVersion() {
    return adapterVersion;
  }

  public String getSpecVersion() {
    return specVersion;
  }

  public String getAdapterName() {
    return adapterName;
  }

  public String getAdapterVendorName() {
    return vendorName;
  }

  public String getAdapterShortDescription() {
    return description;
  }

  public void setAdapterVersion(String version) {
    this.adapterVersion = version;
  }

  public void setSpecVersion(String version) {
    this.specVersion = version;
  }

  public void setAdapterName(String name) {
    this.adapterName = name;
  }

  public void setAdapterVendorName(String name) {
    this.vendorName = name;
  }

  public void setAdapterShortDescription(String description) {
    this.description = description;
  }

  public String[] getInteractionSpecsSupported() {
    String[] str = new String[1];
    str[0] = new String("com.sun.connector.cciblackbox.CciInteractionSpec");
    return str;

  }

  public boolean supportsExecuteWithInputAndOutputRecord() {

    /* Method method;
      Class interactionClass =
    Class.forName("jakarta.resource.cci.Interaction");
    try {
          method =
      interactionClass.getDeclaredMethod(InteractionSpec,Record,Record);
    }catch(NoSuchMethodException e) {
    return false;
    }
    if(method != null) {
    return true;
    }*/

    return true;

  }

  public boolean supportsExecuteWithInputRecordOnly() {
    /*  Method method;
      Class interactionClass =
    Class.forName("jakarta.resource.cci.Interaction");
    try {
          method =
      interactionClass.getDeclaredMethod(InteractionSpec,Record);
    }catch(NoSuchMethodException e) {
    return false;
    }
    if(method != null) {
    return true;
    }*/

    return true;
  }

  public boolean supportsLocalTransactionDemarcation() {

    return true;

  }
}
