/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.util.HashMap;
import java.util.Map;

/**
 * This class implements VersionUpgrade and can be used as a
 * convenience class to cause an element name to be removed.
 *
 * @author  Gerald Ingalls
 * @version
 */
public abstract class RemoveVersionUpgrade implements VersionUpgrade {
  protected Map<String,String> matches;
  protected String replacedElementName;
  public RemoveVersionUpgrade(String replacedName) {
    replacedElementName = replacedName;
    matches = new HashMap<String,String>();
    init();
  }

  /**
   * Return the kind of processing to do
   * @return the kind of processing to do
   */
  public UpgradeType getUpgradeType() {
    return UpgradeType.REMOVE_ELEMENT;
  }

  /**
   * Initialize
   */
  public void init() {
    matches.put(replacedElementName, null);
  }

  /**
   * Return the map of xml element to match
   * @return the map of xml element to match
   */
  public Map<String,String> getMatchXPath() {
    return matches;
  }

  /**
   * Return the replacement element name
   * @return the replacement element name
   */
  public String getReplacementElementName() {
    return null;
  }

  /**
   * Return the replacement element value
   * @return the replacement element value
   */
  public String getReplacementElementValue() {
    return null;
  }

  /**
   * Return whether the matched items are valid.
   * @return whether the matched items are valid.
   */
  public boolean isValid() {
    return false;
  }
}
