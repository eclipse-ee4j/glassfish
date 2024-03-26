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
 * CaptureSchema.java
 *
 * Created on March 15, 2002, 4:02 PM
 */

package com.sun.jdo.api.persistence.mapping.ejb;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.netbeans.modules.dbschema.DBIdentifier;
import org.netbeans.modules.dbschema.SchemaElement;
import org.netbeans.modules.dbschema.jdbcimpl.ConnectionProvider;
import org.netbeans.modules.dbschema.jdbcimpl.SchemaElementImpl;

/**
 *
 * @author  vkraemer
 * @version 1.0
 */
public class CaptureSchema {
    /** Required filename extension. */
    private static final String OUTPUTFILE_EXTENSION = ".dbschema"; // NOI18N
    private static final String PASSWORD_MASK_STRING = "Protected value, not to be logged"; // NOI18N

    /** Creates new CaptureSchema */
    public CaptureSchema() {
    }

    public static void main(String args[]) {
        int help = 0;
        ResourceBundle bundle =
            ResourceBundle.getBundle("com.sun.jdo.api.persistence.mapping.ejb.Bundle"); // NOI18N
        String driver=bundle.getString("STRING_ORACLE_DRIVER_NOI18N"); //NOI18N
        String username=bundle.getString("STRING_IASCTS_NOI18N"); //NOI18N
        String password=bundle.getString("STRING_IASCTS_NOI18N"); //NOI18N
        String dburl=null;
        String dbschemaname = null;
        String outfile = null;
        LinkedList tableList = new LinkedList();
        LinkedList vList = new LinkedList();
        try {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals(bundle.getString("CMD_FLAG_DRIVER"))) { // NOI18N
                    driver = args[++i];
                    help++;
                } else if (args[i].equals(bundle.getString("CMD_FLAG_SCHEMA_NAME"))) { // NOI18N
                    dbschemaname = args[++i];
                    help++;

                } else if (args[i].equals(bundle.getString("CMD_FLAG_USERNAME"))) { // NOI18N
                    username = args[++i];
                    help++;

                } else if (args[i].equals(bundle.getString("CMD_FLAG_PASSWORD"))) { // NOI18N
                    password = args[++i];
                    help++;
                } else if (args[i].equals(bundle.getString("CMD_FLAG_DBURL"))) { // NOI18N
                    dburl = args[++i];
                    help++;
                } else if (args[i].equals(bundle.getString("CMD_FLAG_TABLE"))) { // NOI18N
                    tableList.add(args[++i]);
                    // vList.add(args[i]);
                    help++;
                } else if (args[i].equals(bundle.getString("CMD_FLAG_OUTPUT"))) { // NOI18N
                    outfile = args[++i];
                    help++;
                }
            }
        }
        catch(Exception e) {
            help = 0;
        }

        if (help < 1 || null == outfile) {
            System.err.println(bundle.getString("HELP_USAGE")); //NOI18N
            System.exit(1);
        }

        // Ensure that outfile ends with OUTPUTFILE_EXTENSION, warning user if
        // we change their given name.
        if (!outfile.endsWith(OUTPUTFILE_EXTENSION)) {
            System.err.println(bundle.getString("MESSAGE_CHANGING_OUTFILENAME") // NOI18N
                               + OUTPUTFILE_EXTENSION);
            outfile += OUTPUTFILE_EXTENSION;
        }

        ConnectionProvider cp = null;
        boolean err_flag=false;
        try {
            System.err.println(bundle.getString("MESSAGE_USING_URL")+ dburl); //NOI18N
            System.err.println(bundle.getString("MESSAGE_USING_USERNAME")+ username); //NOI18N
            System.err.println(bundle.getString("MESSAGE_USING_PASSWORD")+ PASSWORD_MASK_STRING); //NOI18N
            System.err.println(bundle.getString("MESSAGE_USING_DRIVER")+ driver); //NOI18N
            System.err.println(bundle.getString("MESSAGE_USING_SCHEMANAME")+ dbschemaname); //NOI18N
            System.err.println(bundle.getString("MESSAGE_USING_OUTFILENAME")+ outfile); //NOI18N

            // prepare the connection
            cp = new ConnectionProvider(driver, dburl,username, password);
            if (null != dbschemaname)
                cp.setSchema(dbschemaname); //schema in the database you want to capture; needs to be improved and set in constructor probably

            System.out.println(bundle.getString("MESSAGE_CAPTURING_SCHEMA") + dbschemaname);//NOI18N

            SchemaElementImpl outSchemaImpl = new SchemaElementImpl(cp);
            SchemaElement se = new SchemaElement(outSchemaImpl);

            if (null != dbschemaname)
                se.setName(DBIdentifier.create(bundle.getString("STRING_SCHEMAS_SLASH_NOI18N") + dbschemaname));//NOI18N
            else
                se.setName(DBIdentifier.create("")); //NOI18N

            if (dburl.indexOf(bundle.getString("STRING_ORACLE_JDBC_URL_PREFIX_NOI18N")) > -1 && //NOI18N
            (null == dbschemaname || dbschemaname.length() == 0)) {
                // this argument combo has problems. print an error message and exit
                System.err.println(bundle.getString("ERR_ORACLE_ARGUMENTS")); //NOI18N
                err_flag=true;
                return;
            }

            if (tableList.size() == 0) {
                outSchemaImpl.initTables(cp);
            }
            else {
                pruneTableList(tableList, cp,bundle);
                if (tableList.size() > 0)
                    outSchemaImpl.initTables(cp, tableList,vList,false);
                else {
                    System.err.println(bundle.getString("MESSAGE_NO_VALID_TABLES")); //NOI18N
                    err_flag=true;
                    return;
                }
            }
            System.out.println(bundle.getString("MESSAGE_SCHEMA_CAPTURED")); //NOI18N

            System.out.println(bundle.getString("MESSAGE_SAVING_SCHEMA")); //NOI18N
            OutputStream outstream = new FileOutputStream(outfile);
            se.save(outstream);
        }
        catch (java.lang.ClassNotFoundException cnfe) {
            System.err.println(bundle.getString("ERR_CHECK_CLASSPATH")); //NOI18N
            cnfe.printStackTrace(System.err); //NOI18N
            err_flag=true;
        }
        catch (Throwable exc) {
            exc.printStackTrace(System.err); //NOI18N
            err_flag=true;
        }
        finally {
            if (cp != null) {
                cp.closeConnection();
            }
        }
        System.exit(err_flag ? 1 : 0);
    }

    private static void pruneTableList(List tableList, ConnectionProvider cp,
            ResourceBundle bundle) throws java.sql.SQLException {
        ResultSet rs;
        DatabaseMetaData dmd = cp.getDatabaseMetaData();
        Map tables = new HashMap();
        String catalog = cp.getConnection().getCatalog();
        String user = cp.getSchema();

        rs = dmd.getTables(catalog, user, "%", new String[] {"TABLE"}); //NOI18N
        if (rs != null) {
            while (rs.next()) {
                String tn = rs.getString("TABLE_NAME").trim(); //NOI18N
                tables.put(tn,tn);
            }
            rs.close();
        }
        Iterator iter = tableList.iterator();
        String [] args = new String[1];
        while (iter.hasNext()) {
            String s = (String) iter.next();
            if (null == tables.get(s.trim())) {
                iter.remove();
                args[0] = s;
                System.err.println(MessageFormat.format(bundle.getString("ERR_INVALID_TABLE_GIVEN"), (Object []) args)); //NOI18N
            }
        }
    }

    static class PropChangeReport implements java.beans.PropertyChangeListener {
        public void propertyChange(java.beans.PropertyChangeEvent pce) {
            //System.err.println(pce); //NOI18N
        }
    }
}
