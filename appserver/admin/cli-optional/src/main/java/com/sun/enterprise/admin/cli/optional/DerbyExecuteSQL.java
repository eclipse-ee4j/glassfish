/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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
package com.sun.enterprise.admin.cli.optional;

import java.io.File;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;

import static com.sun.enterprise.util.Utility.isAllEmpty;

/**
 * This class executes SQL statements from a file against Derby. Its prime purpose is for database initialization
 * after starting it.
 *
 * <p>
 * The reason for creating this class instead of directly executing SQL from the
 * StartDatabaseCommand class is so that a separate JVM is launched when starting the database with all the right
 * drivers in place for the remote Derby.
 *
 * @author Arjan Tijms
 */
public class DerbyExecuteSQL {

    final private String derbyHost;
    final private String derbyPort;
    final private String derbyName;
    final private String sqlFileName;
    final private String derbyUser;
    final private String derbyPassword;

    public static void main(String[] args) {
        DerbyExecuteSQL sql = new DerbyExecuteSQL(
                args[0],
                args[1],
                args[2],
                args[3],
                args.length < 5? null : args[4],
                args.length < 6? null : args[5]);

        sql.executeCommand();
    }

    public DerbyExecuteSQL(String derbyHost, String derbyPort, String derbyName, String sqlFileName, String derbyUser, String derbyPassword) {
        this.derbyHost = derbyHost;
        this.derbyPort = derbyPort;
        this.derbyName = derbyName;
        this.sqlFileName = sqlFileName;
        this.derbyUser = derbyUser;
        this.derbyPassword = derbyPassword;
    }

    private void executeCommand() {
        try {
            Driver driver = (Driver)
                Class.forName("org.apache.derby.jdbc.ClientDriver")
                     .getDeclaredConstructor()
                     .newInstance();

            Properties properties = new Properties();
            if (!isAllEmpty(derbyUser, derbyPassword)) {
                properties.put("user", derbyUser);
                properties.put("password", derbyPassword);
            }

            try (Connection connection = driver.connect("jdbc:derby://" + derbyHost + ":" + derbyPort + "/" + derbyName, properties)) {
                try (Scanner scanner = new Scanner(new File(sqlFileName), "UTF-8")) {

                    scanner.useDelimiter(";");

                    while (scanner.hasNext()) {
                        String statement = null;
                        try {
                            statement = scanner.next().strip();
                            if (!statement.isEmpty()) {
                                connection.prepareStatement(statement).execute();
                            }
                        } catch (SQLException s) {
                            System.out.println("Exeception executing: " + statement);
                            s.printStackTrace();
                        }
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}


