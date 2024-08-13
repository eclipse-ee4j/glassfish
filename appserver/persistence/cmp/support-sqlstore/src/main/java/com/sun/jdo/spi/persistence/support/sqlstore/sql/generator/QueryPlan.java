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

/*
 * QueryPlan.java
 *
 * Created on March 3, 2000
 *
 */

package com.sun.jdo.spi.persistence.support.sqlstore.sql.generator;

import com.sun.jdo.api.persistence.support.JDOFatalInternalException;
import com.sun.jdo.spi.persistence.support.sqlstore.ActionDesc;
import com.sun.jdo.spi.persistence.support.sqlstore.SQLStoreManager;
import com.sun.jdo.spi.persistence.support.sqlstore.model.ClassDesc;
import com.sun.jdo.spi.persistence.support.sqlstore.model.ReferenceKeyDesc;
import com.sun.jdo.spi.persistence.support.sqlstore.model.TableDesc;

import java.util.ArrayList;
import java.util.ResourceBundle;

import org.glassfish.persistence.common.I18NHelper;
import org.netbeans.modules.dbschema.TableElement;


/**
 * This class is used to generate SQL statements.
 */
public abstract class QueryPlan {
    public static final int ACT_UPDATE = 1;

    public static final int ACT_INSERT = 2;

    public static final int ACT_DELETE = 3;

    public static final int ACT_SELECT = 4;

    public static final int ACT_NOOP = 5;

    protected static final int ST_BUILT = 0x1;

    /** Array of Statement. */
    public ArrayList statements;

    protected ClassDesc config;

    /**
     * Bitmask containing one of {@link QueryPlan#ST_BUILT},
     * {@link SelectQueryPlan#ST_C_BUILT}, {@link SelectQueryPlan#ST_OC_BUILT},
     * {@link SelectQueryPlan#ST_JOINED}
     */
    protected int status;

    protected int action;

    protected SQLStoreManager store;

    /** Array of QueryTable. */
    protected ArrayList tables;

    /** I18N message handler. */
    protected final static ResourceBundle messages = I18NHelper.loadBundle(
            "com.sun.jdo.spi.persistence.support.sqlstore.Bundle", // NOI18N
            QueryPlan.class.getClassLoader());

    public QueryPlan(ActionDesc desc, SQLStoreManager store) {
        this.tables = new ArrayList();
        this.statements = new ArrayList();
        this.store = store;
        this.config = (ClassDesc) store.getPersistenceConfig(desc.getPersistenceCapableClass());
    }

    public QueryTable addQueryTable(TableDesc tableDesc) {
        QueryTable table = new QueryTable(tableDesc);
        tables.add(table);
        table.setTableIndex(new TableIndex(tables.size() - 1));
        return table;
    }

    /**
     * Identifies a database table which will become part of this
     * query plan.  We will build a QueryTable object describing its
     * use and return it.
     *
     * Note:  No join is constructed at this point for the table added.
     *
     * @param tableElement Identifies which table is being added.
     * @param persistenceConfig
     *     If we are adding a foreign table the persistenceConfig parameter
     *     holds the PersistenceConfig for the foreign Persistence Class.
     */
    public QueryTable addQueryTable(TableElement tableElement,
                                    ClassDesc persistenceConfig) {

        ClassDesc _config = (persistenceConfig == null) ? this.config : persistenceConfig;
        TableDesc tableDesc = _config.findTableDesc(tableElement);

        if (tableDesc == null) {

            if (tableElement != null) {
               throw new JDOFatalInternalException(I18NHelper.getMessage(messages,
                        "core.configuration.classnotmappedtotable", // NOI18N
                        _config.getPersistenceCapableClass().getName(),
                        tableElement.getName().getName()));
            } else {
                throw new JDOFatalInternalException(I18NHelper.getMessage(messages,
                         "core.configuration.classnotmapped", // NOI18N
                         _config.getPersistenceCapableClass().getName()));

            }
        }

        return addQueryTable(tableDesc);
    }

    /**
     * Add all the tables from <code>queryTables</code> to the current
     * query plan.  Useful when transfering tables when joining query plans
     * together.
     *
     * @param queryTables Query tables from the foreign plan to be added.
     */
    public void addQueryTables(ArrayList queryTables) {

        for (int i = 0; i < queryTables.size(); i++) {
            QueryTable t = (QueryTable) queryTables.get(i);

            if (tables.indexOf(t) == -1) {
                tables.add(t);
                t.getTableIndex().setValue(tables.size() - 1);
            }
        }
    }

    /**
     * Finds the QueryTable object that this query plan is using
     * to describe the TableElement indicated by the tableElement parameter.
     */
    public QueryTable findQueryTable(TableElement tableElement) {
        for (int i = 0; i < tables.size(); i++) {
            QueryTable t = (QueryTable) tables.get(i);

            if (t.getTableDesc().getTableElement() == tableElement) {
            //if (t.getTableDesc().getTableElement().equals(tableElement)) {
                return t;
            }
        }

        return null;
    }

    /**
     * Finds the QueryTable object that this query plan is using
     * to describe the TableDesc indicated by the tableDesc parameter.
     */
    public QueryTable findQueryTable(TableDesc tableDesc) {
        for (int i = 0; i < tables.size(); i++) {
            QueryTable t = (QueryTable) tables.get(i);

            if (t.getTableDesc() == tableDesc) return t;
        }

        return null;
    }

    public ArrayList getStatements() {
        for (int i = 0; i < statements.size(); i++) {
            Statement s = (Statement) statements.get(i);
            //Initialize sql text of the statement
            s.getText();
        }

        return statements;
    }

    protected Statement addStatement(QueryTable t) {
        Statement s = createStatement(t);
        statements.add(s);

        return s;
    }

    protected abstract Statement newStatement();

    protected Statement createStatement(QueryTable t) {
        Statement statement = newStatement();

        statement.action = action;
        statement.addQueryTable(t);

        return statement;
    }

    protected Statement getStatement(QueryTable t) {
        if (t == null) return null;

        for (int i = 0; i < statements.size(); i++) {
            Statement s = (Statement) statements.get(i);

            if (s.tableList.indexOf(t) != -1)
                return s;
        }

        return null;
    }

    public abstract void build();

    /**
     * This method goes through the statement list and tries to set up relationship
     * between statements based on secondary table keys.
     */
    protected void processStatements() {
        for (int i = 0; i < statements.size(); i++) {
            Statement s = (Statement) statements.get(i);

            QueryTable qt = (QueryTable) s.getQueryTables().get(0);

            ArrayList secondaryTableKeys = qt.getTableDesc().getSecondaryTableKeys();

            if (secondaryTableKeys != null) {
                for (int j = 0; j < secondaryTableKeys.size(); j++) {
                    ReferenceKeyDesc secondaryTableKey = (ReferenceKeyDesc) secondaryTableKeys.get(j);
                    s.addSecondaryTableStatement(getStatement(findQueryTable(secondaryTableKey.getTableDesc())));
                }
            }
        }
    }

    public int getAction() {
        return action;
    }

    public ClassDesc getConfig() {
        return config;
    }

}





