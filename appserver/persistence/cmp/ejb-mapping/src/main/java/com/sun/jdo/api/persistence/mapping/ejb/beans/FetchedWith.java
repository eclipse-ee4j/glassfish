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

package com.sun.jdo.api.persistence.mapping.ejb.beans;

import java.util.Vector;

import org.netbeans.modules.schema2beans.Common;

// BEGIN_NOI18N

public class FetchedWith extends org.netbeans.modules.schema2beans.BaseBean
{

    static Vector comparators = new Vector();
    private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);

    static public final String DEFAULT = "Default";    // NOI18N
    static public final String LEVEL = "Level";    // NOI18N
    static public final String NAMED_GROUP = "NamedGroup";    // NOI18N
    static public final String NONE = "None";    // NOI18N

    public FetchedWith() {
        this(Common.USE_DEFAULT_VALUES);
    }

    public FetchedWith(int options)
    {
        super(comparators, runtimeVersion);
        // Properties (see root bean comments for the bean graph)
        initPropertyTables(4);
        this.createProperty("default",     // NOI18N
            DEFAULT, Common.SEQUENCE_OR |
            Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_KEY,
            Boolean.class);
        this.createProperty("level",     // NOI18N
            LEVEL, Common.SEQUENCE_OR |
            Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY,
            Integer.class);
        this.createProperty("named-group",     // NOI18N
            NAMED_GROUP, Common.SEQUENCE_OR |
            Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY,
            String.class);
        this.createProperty("none",     // NOI18N
            NONE, Common.SEQUENCE_OR |
            Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_KEY,
            Boolean.class);
        this.initialize(options);
    }

    // Setting the default values of the properties
    void initialize(int options) {

    }

    // This attribute is mandatory
    public void setDefault(boolean value) {
        this.setValue(DEFAULT, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
        if (value != false) {
            // It's a mutually exclusive property.
            setLevel(0);
            setNamedGroup(null);
            setNone(false);
        }
    }

    //
    public boolean isDefault() {
        Boolean ret = (Boolean)this.getValue(DEFAULT);
        if (ret == null)
            ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
        return ((java.lang.Boolean)ret).booleanValue();
    }

    // This attribute is mandatory
    public void setLevel(int value) {
        this.setValue(LEVEL, new java.lang.Integer(value));
        if (value != 0) {
            // It's a mutually exclusive property.
            setDefault(false);
            setNamedGroup(null);
            setNone(false);
        }
    }

    //
    public int getLevel() {
        Integer ret = (Integer)this.getValue(LEVEL);
        if (ret == null)
            throw new RuntimeException(Common.getMessage(
                "NoValueForElt_msg",
                new Object[] {"LEVEL", "int"}));
        return ((java.lang.Integer)ret).intValue();
    }

    // This attribute is mandatory
    public void setNamedGroup(String value) {
        this.setValue(NAMED_GROUP, value);
        if (value != null) {
            // It's a mutually exclusive property.
            setDefault(false);
            setLevel(0);
            setNone(false);
        }
    }

    //
    public String getNamedGroup() {
        return (String)this.getValue(NAMED_GROUP);
    }

    // This attribute is mandatory
    public void setNone(boolean value) {
        this.setValue(NONE, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
        if (value != false) {
            // It's a mutually exclusive property.
            setDefault(false);
            setLevel(0);
            setNamedGroup(null);
        }
    }

    //
    public boolean isNone() {
        Boolean ret = (Boolean)this.getValue(NONE);
        if (ret == null)
            ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
        return ((java.lang.Boolean)ret).booleanValue();
    }

    //
    public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
        comparators.add(c);
    }

    //
    public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
        comparators.remove(c);
    }
    public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
        // Validating property default
        if (isDefault() != false) {
            if (getValue(LEVEL) != null) {
                throw new org.netbeans.modules.schema2beans.ValidateException("mutually exclusive properties: Default and Level", org.netbeans.modules.schema2beans.ValidateException.FailureType.MUTUALLY_EXCLUSIVE, "Level", this);    // NOI18N
            }
            if (getNamedGroup() != null) {
                throw new org.netbeans.modules.schema2beans.ValidateException("mutually exclusive properties: Default and NamedGroup", org.netbeans.modules.schema2beans.ValidateException.FailureType.MUTUALLY_EXCLUSIVE, "NamedGroup", this);    // NOI18N
            }
            if (isNone() != false) {
                throw new org.netbeans.modules.schema2beans.ValidateException("mutually exclusive properties: Default and None", org.netbeans.modules.schema2beans.ValidateException.FailureType.MUTUALLY_EXCLUSIVE, "None", this);    // NOI18N
            }
        }
        // Validating property level
        if (getValue(LEVEL) != null) {
            if (isDefault() != false) {
                throw new org.netbeans.modules.schema2beans.ValidateException("mutually exclusive properties: Level and Default", org.netbeans.modules.schema2beans.ValidateException.FailureType.MUTUALLY_EXCLUSIVE, "Default", this);    // NOI18N
            }
            if (getNamedGroup() != null) {
                throw new org.netbeans.modules.schema2beans.ValidateException("mutually exclusive properties: Level and NamedGroup", org.netbeans.modules.schema2beans.ValidateException.FailureType.MUTUALLY_EXCLUSIVE, "NamedGroup", this);    // NOI18N
            }
            if (isNone() != false) {
                throw new org.netbeans.modules.schema2beans.ValidateException("mutually exclusive properties: Level and None", org.netbeans.modules.schema2beans.ValidateException.FailureType.MUTUALLY_EXCLUSIVE, "None", this);    // NOI18N
            }
        }
        // Validating property namedGroup
        if (getNamedGroup() != null) {
            if (isDefault() != false) {
                throw new org.netbeans.modules.schema2beans.ValidateException("mutually exclusive properties: NamedGroup and Default", org.netbeans.modules.schema2beans.ValidateException.FailureType.MUTUALLY_EXCLUSIVE, "Default", this);    // NOI18N
            }
            if (getValue(LEVEL) != null) {
                throw new org.netbeans.modules.schema2beans.ValidateException("mutually exclusive properties: NamedGroup and Level", org.netbeans.modules.schema2beans.ValidateException.FailureType.MUTUALLY_EXCLUSIVE, "Level", this);    // NOI18N
            }
            if (isNone() != false) {
                throw new org.netbeans.modules.schema2beans.ValidateException("mutually exclusive properties: NamedGroup and None", org.netbeans.modules.schema2beans.ValidateException.FailureType.MUTUALLY_EXCLUSIVE, "None", this);    // NOI18N
            }
        }
        // Validating property none
        if (isNone() != false) {
            if (isDefault() != false) {
                throw new org.netbeans.modules.schema2beans.ValidateException("mutually exclusive properties: None and Default", org.netbeans.modules.schema2beans.ValidateException.FailureType.MUTUALLY_EXCLUSIVE, "Default", this);    // NOI18N
            }
            if (getValue(LEVEL) != null) {
                throw new org.netbeans.modules.schema2beans.ValidateException("mutually exclusive properties: None and Level", org.netbeans.modules.schema2beans.ValidateException.FailureType.MUTUALLY_EXCLUSIVE, "Level", this);    // NOI18N
            }
            if (getNamedGroup() != null) {
                throw new org.netbeans.modules.schema2beans.ValidateException("mutually exclusive properties: None and NamedGroup", org.netbeans.modules.schema2beans.ValidateException.FailureType.MUTUALLY_EXCLUSIVE, "NamedGroup", this);    // NOI18N
            }
        }
        if (getNamedGroup() == null && getValue(LEVEL) == null && isDefault() == false && isNone() == false) {
            throw new org.netbeans.modules.schema2beans.ValidateException("required properties: getNamedGroup() == null && getValue(LEVEL) == null && isDefault() == false && isNone() == false", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "None", this);    // NOI18N
        }
    }

    // Dump the content of this bean returning it as a String
    public void dump(StringBuffer str, String indent){
        String s;
        Object o;
        str.append(indent);
        str.append("Default");    // NOI18N
        str.append(indent+"\t");    // NOI18N
        str.append((this.isDefault()?"true":"false"));
        this.dumpAttributes(DEFAULT, 0, str, indent);

        if (this.getValue(LEVEL) != null) {
            str.append(indent);
            str.append("Level");    // NOI18N
            str.append(indent+"\t");    // NOI18N
            str.append("<");    // NOI18N
            s = String.valueOf(this.getLevel());
            str.append((s==null?"null":s.trim()));    // NOI18N
            str.append(">\n");    // NOI18N
            this.dumpAttributes(LEVEL, 0, str, indent);
        }

        str.append(indent);
        str.append("NamedGroup");    // NOI18N
        str.append(indent+"\t");    // NOI18N
        str.append("<");    // NOI18N
        o = this.getNamedGroup();
        str.append((o==null?"null":o.toString().trim()));    // NOI18N
        str.append(">\n");    // NOI18N
        this.dumpAttributes(NAMED_GROUP, 0, str, indent);

        str.append(indent);
        str.append("None");    // NOI18N
        str.append(indent+"\t");    // NOI18N
        str.append((this.isNone()?"true":"false"));
        this.dumpAttributes(NONE, 0, str, indent);

    }
    public String dumpBeanNode(){
        StringBuffer str = new StringBuffer();
        str.append("FetchedWith\n");    // NOI18N
        this.dump(str, "\n  ");    // NOI18N
        return str.toString();
    }}

// END_NOI18N


/*
        The following schema file has been used for generation:

<!--
  XML DTD for Sun ONE Application Server specific Object Relational Mapping
  with Container Managed Persistence.
-->

<!--

This sun-cmp-mapping_1_2.dtd has a workaround for an unfiled schema2beans bug
which prevents us from having the DTD specify the sub-elements in column-pair as
it really should be.  This issue is fixed in schema2beans shipped with NB > 3.5,
but we are currently using schema2beans from NB 3.5, and so must use this
workaround.

Because of the workaround, the file here differs from the official one in
appserv-commons/lib/dtds (which also has previous versions of sun-cmp-mapping
dtds) in the definition of the column pair element.  This difference is so
that schema2beans can produce usable beans.  The official dtd has:

    <!ELEMENT column-pair (column-name, column-name) >

and the one in here has:

    <!ELEMENT column-pair (column-name+) >

-->

<!-- This file maps at least one set of beans to tables and columns in a
     specific db schema
-->
<!ELEMENT sun-cmp-mappings ( sun-cmp-mapping+ ) >

<!-- At least one bean is mapped to database columns in the named schema -->
<!ELEMENT sun-cmp-mapping ( schema, entity-mapping+) >

<!-- A cmp bean has a name, a primary table, one or more fields, zero or
     more relationships, and zero or more secondary tables, plus flags for
     consistency checking.

     If the consistency checking flag element is not present, then none
     is assumed
-->
<!ELEMENT entity-mapping (ejb-name, table-name, cmp-field-mapping+,
        cmr-field-mapping*, secondary-table*, consistency?)>

<!ELEMENT consistency (none | check-modified-at-commit | lock-when-loaded |
        check-all-at-commit | (lock-when-modified, check-all-at-commit?) |
        check-version-of-accessed-instances) >

<!ELEMENT read-only EMPTY>

<!-- A cmp-field-mapping has a field, one or more columns that it maps to.
     The column can be from a bean's primary table or any defined secondary
     table.  If a field is mapped to multiple columns, the column listed first
     is used as the SOURCE for getting the value from the database.  The
     columns are updated in their order.  A field may also be marked as
     read-only.  It may also participate in a hierarchial or independent
     fetch group. If the fetched-with element is not present, the value,
          <fetched-with><none/></fetched-with>
     is assumed.
-->
<!ELEMENT cmp-field-mapping (field-name, column-name+, read-only?,
        fetched-with?) >

<!-- The java identifier of a field. Must match the value of the field-name
     sub-element of the cmp-field that is being mapped.
-->
<!ELEMENT field-name (#PCDATA) >

<!-- The java identifier of a field.  Must match the value of the
     cmr-field-name sub-element of the cmr-field tat is being mapped.
-->
<!ELEMENT cmr-field-name (#PCDATA) >

<!-- The ejb-name from the standard EJB-jar DTD-->
<!ELEMENT ejb-name (#PCDATA) >

<!-- The COLUMN name of a column from the primary table, or the table
     qualified name (TABLE.COLUMN) of a column from a secondary or related
     table
-->
<!ELEMENT column-name (#PCDATA) >

<!-- Holds the fetch group configuration for fields and relationships -->
<!ELEMENT fetched-with (default | level | named-group | none) >

<!-- Sub element of fetched-with. Implies that a field belongs to the default
     hierarchical fetch group. -->
<!ELEMENT default EMPTY>

<!-- A hierarchial fetch group.  The value of this element must be an integer.
     Fields and relationships that belong to a hierachial fetch group of equal
     (or lesser) value are fetched at the same time. The value of level must
     be greater than zero.
-->
<!ELEMENT level (#PCDATA) >

<!-- The name of an independent fetch group.  All the fields and relationships
  that are part of a named-group are fetched at the same time-->
<!ELEMENT named-group (#PCDATA) >

<!-- The name of a database table -->
<!ELEMENT table-name (#PCDATA) >

<!-- a bean's secondary tables -->
<!ELEMENT secondary-table (table-name, column-pair+) >

<!-- the pair of columns -->
<!ELEMENT column-pair (column-name+) >

<!-- cmr-field mapping.  A cmr field has a name and one or more column
     pairs that define the relationship. The relationship can also
     participate in a fetch group.

     If the fetched-with element is not present, the value,
          <fetched-with><none/></fetched-with>
     is assumed.
-->
<!ELEMENT cmr-field-mapping (cmr-field-name, column-pair+, fetched-with? ) >

<!-- The path name to the schema file-->
<!ELEMENT schema (#PCDATA) >

<!-- flag elements for consistency levels -->

<!-- note: none is also a sub-element of the fetched-with tag -->
<!ELEMENT none EMPTY >
<!ELEMENT check-modified-at-commit EMPTY >
<!ELEMENT check-all-at-commit EMPTY>
<!ELEMENT lock-when-modified EMPTY>
<!ELEMENT lock-when-loaded EMPTY >
<!ELEMENT check-version-of-accessed-instances (column-name+) >

*/
