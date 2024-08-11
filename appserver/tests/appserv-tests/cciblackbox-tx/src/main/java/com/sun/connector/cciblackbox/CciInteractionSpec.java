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

import jakarta.resource.cci.InteractionSpec;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * This implementation class holds properties for driving an Interaction
 * with an EIS instance. This class is a Java Bean, hence it supports
 * bound properties.
 * @author Sheetal Vartak
 */
public class CciInteractionSpec implements InteractionSpec, java.io.Serializable {

  private String functionName;

  private String schema;

  private String catalog;

  private PropertyChangeSupport changes = new PropertyChangeSupport(this);

  public CciInteractionSpec() {
  }

  public String getFunctionName() {
    return this.functionName;
  }

  public void setFunctionName(String functionName) {
    String oldName = this.functionName;
    this.functionName = functionName;
    changes.firePropertyChange("functionName", oldName, functionName);
  }

  public String getCatalog() {
    return this.catalog;
  }

  public void setCatalog(String catalog) {
    String oldCatalog = this.catalog;
    this.catalog = catalog;
    changes.firePropertyChange("catalog", oldCatalog, catalog);
  }

  public String getSchema() {
    return this.schema;
  }

  public void setSchema(String schema) {
    String oldSchema = this.schema;
    this.schema = schema;
    changes.firePropertyChange("schema", oldSchema, schema);
  }

  public void addPropertyChangeListener(PropertyChangeListener l) {
    changes.addPropertyChangeListener(l);
  }

  public void removePropertyChangeListener(PropertyChangeListener l) {
    changes.removePropertyChangeListener(l);
  }

}
