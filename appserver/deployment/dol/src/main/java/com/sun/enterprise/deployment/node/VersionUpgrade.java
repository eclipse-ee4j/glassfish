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

package com.sun.enterprise.deployment.node;

import java.util.Map;

/**
 * This interface defines the processing used to upgrade an older
 * configuration file to the latest version.
 *
 * The class should be initialized by the init method, which should
 * initialize the map returned by getMatchXPath with the keys to
 * look for in the input configuration file and with values set to null.
 * For REMOVE_ELEMENT, the map would usually contain one item.
 * For REPLACE_ELEMENT, the map could contain more that one item.
 * For REPLACE_ELEMENT, when all elements in the map have been matched, the
 * isValid method is called.  If it returns true, the
 * getReplacementElementName and getReplacementElementValue methods
 * are called and should return the replacement element name and value.
 * if the input matched items are valid.  Otherwise, these methods should
 * return null.
 * @author  Gerald Ingalls
 * @version
 */
public interface VersionUpgrade {
  public enum UpgradeType {
    REMOVE_ELEMENT,
    REPLACE_ELEMENT
  }

  /**
   * Return the kind of processing to do
   * @return the kind of processing to do
   */
  public UpgradeType getUpgradeType();

  /**
   * Initialize
   */
  public void init();

  /**
   * Return the map of xml element to match
   * @return the map of xml element to match
   */
  public Map<String,String> getMatchXPath();

  /**
   * Return the replacement element name
   * @return the replacement element name
   */
  public String getReplacementElementName();

  /**
   * Return the replacement element value
   * @return the replacement element value
   */
  public String getReplacementElementValue();

  /**
   * Return whether the matched items are valid.
   * @return whether the matched items are valid.
   */
  public boolean isValid();
}
