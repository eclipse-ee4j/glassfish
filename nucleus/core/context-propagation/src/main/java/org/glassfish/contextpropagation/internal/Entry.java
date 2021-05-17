/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.contextpropagation.internal;

import java.util.EnumSet;

import org.glassfish.contextpropagation.PropagationMode;
import org.glassfish.contextpropagation.View;

/**
 * Entries hold work contexts in the ContextMap as well as related metadata:
 * propagation mode, whether this thread originated the work context, and
 * whether all are allowed to read its work context.
 */
public class Entry {
  Object value;
  EnumSet<PropagationMode> propagationModes;
  transient Boolean isOriginator;
  transient Boolean allowAllToRead;
  ContextType contextType;

  /**
   * Dedicated constructor, set the work context, it propagation  mode and type.
   * @param context
   * @param propModes
   * @param contextType
   */
  public Entry(Object context, EnumSet<PropagationMode> propModes, ContextType contextType) {
    this.value = context;
    propagationModes = propModes;
    this.contextType = contextType;
  }

  public View getView() {
    throw new UnsupportedOperationException("This Entry does not have a View associated to it");
  }

  public static Entry createViewEntryInstance(Object context,
      EnumSet<PropagationMode> propModes, final ViewImpl view) {
    return new Entry(context, propModes, ContextType.VIEW_CAPABLE) {
      @Override public View getView() { return view; }
    };
  }


  public String getClassName() {
    throw new UnsupportedOperationException("This Entry does not have a class name associated to it.");
  }

  public static Entry createOpaqueEntryInstance(Object context,
      EnumSet<PropagationMode> propModes, final String className) {
    return new Entry(context, propModes, ContextType.OPAQUE) {
      @Override public String getClassName() { return className; }
    };
  }

  /**
   * Utility method to set additional metadata.
   * @param isOriginator
   * @param allowAllToRead
   * @return
   */
  public Entry init(Boolean isOriginator, Boolean allowAllToRead) {
    this.isOriginator = isOriginator;
    this.allowAllToRead = allowAllToRead;
    return this;
  }

  private static final String LINE_SEP = System.getProperty("line.separator");
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(super.toString());
    sb.append(" {").append(LINE_SEP);
    sb.append("   Value: " + value).append(LINE_SEP);
    sb.append("   Propagation Modes: " + propagationModes).append(LINE_SEP);
    sb.append("   ContextType: " + contextType.name()).append(LINE_SEP);
    sb.append(isOriginator ? "   originator" : "   remote origin").append(LINE_SEP);
    sb.append(allowAllToRead ? "   readable by all" : "   restricted read access").append(LINE_SEP);
    sb.append("}");
    return sb.toString();
  }

  public ContextType getContextType() {
    return contextType;
  }

  @SuppressWarnings("unchecked")
  public <U> U getValue() {
    return (U) value;
  }

  public EnumSet<PropagationMode> getPropagationModes() {
    return propagationModes;
  }

  void validate() {
    if (value == null) throw new IllegalStateException("Entry must contain a non-null value.");
    if (contextType == null) throw new IllegalStateException("Entry must have a ContextType.");
    if (propagationModes == null) throw new IllegalStateException("Entry must have a EnumSet<PropagationMode.");
    if (isOriginator == null) throw new IllegalStateException("Entry must know if it originated in this scope.");
    if (allowAllToRead == null) throw new IllegalStateException("Entry's allowAllToRead was not specified.");
  }

  /**
   * Identifies the type of a work context
   * OPAQUE is a special type that identifies a context that came over the wire
   * that could not be instantiated. Therefore it is opaque to the context
   * propagation feature. However, opaque elements will be propagated even if this
   * process cannot make sense of them since they may end up in a process that
   * can understand them.
   */
  public enum ContextType {
    ATOMICINTEGER, ATOMICLONG, ASCII_STRING, BIGDECIMAL, BIGINTEGER, BOOLEAN,
    BYTE, CATALOG, CHAR, DOUBLE, FLOAT, INT, LONG, OPAQUE, SERIALIZABLE,
    SHORT, STRING, VIEW_CAPABLE;
    private static ContextType[] byOrdinal = createByOrdinal();

    private static ContextType[] createByOrdinal() {
      ContextType[] values = values();
      ContextType[] byOrdinal = new ContextType[values.length];
      for (ContextType value : values) {
        byOrdinal[value.ordinal()] = value;
      }
      return byOrdinal;
    }

    /**
     * Get the ContextType corresponding to the specified ordinal value.
     * Mostly used by the WireAdapters
     * @param ordinal
     * @return
     */
    public static ContextType fromOrdinal(int ordinal) {
      return byOrdinal[ordinal];
    }

    private static interface NumberConstants {
      static final int BYTE_LONG_SHORT = 4;
      static final char BYTE = 'B';
      static final char LONG = 'L';
      static final int SHORT_FLOAT = 5;
      static final char SHORT = 'S';
      static final char FLOAT = 'F';
      static final char DOUBLE = 6;
      static final int INTEGER = 7;
      static final int ATOMICLONG_BIGDECIMAL_BIGINTEGER = 10;
      static final char ATOMICLONG = 'm';
      static final char BIGDECIMAL = 'D';
      static final char BIGINTEGER = 'I';
      static final int ATOMICINTEGER = 13;
    };

    /**
     * Utility method designed to quickly determine the context type corresponding
     * to a given class, clz.
     * @param clz
     * @return
     */
    public static <T extends Number> ContextType fromNumberClass(Class<T> clz) {
      String simpleName = clz.getSimpleName();
      switch (simpleName.length()) {
      case NumberConstants.BYTE_LONG_SHORT:
        switch (simpleName.charAt(0)) {
        case NumberConstants.BYTE: return BYTE;
        case NumberConstants.LONG: return LONG;
        }
      case NumberConstants.SHORT_FLOAT:
        switch (simpleName.charAt(0)) {
        case NumberConstants.SHORT: return SHORT;
        case NumberConstants.FLOAT: return FLOAT;
        }
      case NumberConstants.DOUBLE: return DOUBLE;
      case NumberConstants.INTEGER: return INT;
      case NumberConstants.ATOMICLONG_BIGDECIMAL_BIGINTEGER:
        switch (simpleName.charAt(3)) {
        case NumberConstants.ATOMICLONG: return ATOMICLONG;
        case NumberConstants.BIGDECIMAL: return BIGDECIMAL;
        case NumberConstants.BIGINTEGER: return BIGINTEGER;
        }
      case NumberConstants.ATOMICINTEGER: return ATOMICINTEGER;
      }
      throw new AssertionError("Unexepected Number Type: " + clz.getName());
    }
  }

}
