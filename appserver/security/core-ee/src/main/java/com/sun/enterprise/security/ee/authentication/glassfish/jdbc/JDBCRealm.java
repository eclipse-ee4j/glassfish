/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.security.ee.authentication.glassfish.jdbc;

import static com.sun.enterprise.security.common.Util.getDefaultHabitat;
import static java.util.logging.Level.FINEST;

import com.sun.appserv.connectors.internal.api.ConnectorRuntime;
import com.sun.enterprise.security.auth.digest.api.DigestAlgorithmParameter;
import com.sun.enterprise.security.auth.digest.api.Password;
import com.sun.enterprise.security.auth.realm.exceptions.BadRealmException;
import com.sun.enterprise.security.auth.realm.exceptions.InvalidOperationException;
import com.sun.enterprise.security.auth.realm.exceptions.NoSuchRealmException;
import com.sun.enterprise.security.auth.realm.exceptions.NoSuchUserException;
import com.sun.enterprise.security.ee.authentication.glassfish.digest.DigestRealmBase;
import com.sun.enterprise.universal.GFBase64Encoder;
import com.sun.enterprise.util.Utility;
import java.io.Reader;
import java.nio.charset.CharacterCodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import javax.security.auth.login.LoginException;
import javax.sql.DataSource;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.jvnet.hk2.annotations.Service;

/**
 * Realm for supporting JDBC authentication.
 *
 * <P>
 * The JDBC realm needs the following properties in its configuration:
 * <ul>
 * <li>jaas-context : JAAS context name used to access LoginModule for authentication (for example JDBCRealm).
 * <li>datasource-jndi : jndi name of datasource
 * <li>db-user : user name to access the datasource
 * <li>db-password : password to access the datasource
 * <li>digest: digest mechanism
 * <li>charset: charset encoding
 * <li>user-table: table containing user name and password
 * <li>group-table: table containing user name and group name
 * <li>user-name-column: column corresponding to user name in user-table and group-table
 * <li>password-column : column corresponding to password in user-table
 * <li>group-name-column : column corresponding to group in group-table
 * </ul>
 *
 * @see com.sun.enterprise.security.ee.auth.login.SolarisLoginModule
 *
 */
@Service
public final class JDBCRealm extends DigestRealmBase {
    // Descriptive string of the authentication type of this realm.
    public static final String AUTH_TYPE = "jdbc";
    public static final String PRE_HASHED = "HASHED";
    public static final String PARAM_DATASOURCE_JNDI = "datasource-jndi";
    public static final String PARAM_DB_USER = "db-user";
    public static final String PARAM_DB_PASSWORD = "db-password";

    public static final String PARAM_DIGEST_ALGORITHM = "digest-algorithm";
    public static final String NONE = "none";

    public static final String PARAM_ENCODING = "encoding";
    public static final String HEX = "hex";
    public static final String BASE64 = "base64";
    public static final String DEFAULT_ENCODING = HEX; // for digest only

    public static final String PARAM_CHARSET = "charset";
    public static final String PARAM_USER_TABLE = "user-table";
    public static final String PARAM_USER_NAME_COLUMN = "user-name-column";
    public static final String PARAM_PASSWORD_COLUMN = "password-column";
    public static final String PARAM_GROUP_TABLE = "group-table";
    public static final String PARAM_GROUP_NAME_COLUMN = "group-name-column";
    public static final String PARAM_GROUP_TABLE_USER_NAME_COLUMN = "group-table-user-name-column";

    private static final char[] HEXADECIMAL = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    private static final String MISSING_PROPERTY = "Missing required property {0} for {1}.";

    private Map<String, Vector> groupCache;
    private Vector<String> emptyVector;
    private String passwordQuery;
    private String groupQuery;
    private MessageDigest messageDigest;

    private ActiveDescriptor<ConnectorRuntime> connectorRuntimeDescriptor;

    /**
     * Initialize a realm with some properties. This can be used when instantiating realms from their descriptions. This
     * method may only be called a single time.
     *
     * @param props Initialization parameters used by this realm.
     * @exception BadRealmException If the configuration parameters identify a corrupt realm.
     * @exception NoSuchRealmException If the configuration parameters specify a realm which doesn't exist.
     */
    @Override
    @SuppressWarnings("unchecked")
    public synchronized void init(Properties props) throws BadRealmException, NoSuchRealmException {
        super.init(props);

        String jaasCtx = props.getProperty(JAAS_CONTEXT_PARAM);
        String dbUser = props.getProperty(PARAM_DB_USER);
        String dbPassword = props.getProperty(PARAM_DB_PASSWORD);
        String dsJndi = props.getProperty(PARAM_DATASOURCE_JNDI);
        String digestAlgorithm = props.getProperty(PARAM_DIGEST_ALGORITHM, getDefaultDigestAlgorithm());
        String encoding = props.getProperty(PARAM_ENCODING);
        String charset = props.getProperty(PARAM_CHARSET);
        String userTable = props.getProperty(PARAM_USER_TABLE);
        String userNameColumn = props.getProperty(PARAM_USER_NAME_COLUMN);
        String passwordColumn = props.getProperty(PARAM_PASSWORD_COLUMN);
        String groupTable = props.getProperty(PARAM_GROUP_TABLE);
        String groupNameColumn = props.getProperty(PARAM_GROUP_NAME_COLUMN);
        String groupTableUserNameColumn = props.getProperty(PARAM_GROUP_TABLE_USER_NAME_COLUMN, userNameColumn);

        connectorRuntimeDescriptor = (ActiveDescriptor<ConnectorRuntime>)
            getDefaultHabitat().getBestDescriptor(BuilderHelper.createContractFilter(ConnectorRuntime.class.getName()));

        if (jaasCtx == null) {
            throw new BadRealmException(MessageFormat.format(MISSING_PROPERTY, JAAS_CONTEXT_PARAM, "JDBCRealm"));
        }

        if (dsJndi == null) {
            throw new BadRealmException(MessageFormat.format(MISSING_PROPERTY, PARAM_DATASOURCE_JNDI, "JDBCRealm"));
        }
        if (userTable == null) {
            throw new BadRealmException(MessageFormat.format(MISSING_PROPERTY, PARAM_USER_TABLE, "JDBCRealm"));
        }
        if (groupTable == null) {
            throw new BadRealmException(MessageFormat.format(MISSING_PROPERTY, PARAM_GROUP_TABLE, "JDBCRealm"));
        }
        if (userNameColumn == null) {
            throw new BadRealmException(MessageFormat.format(MISSING_PROPERTY, PARAM_USER_NAME_COLUMN, "JDBCRealm"));
        }
        if (passwordColumn == null) {
            throw new BadRealmException(MessageFormat.format(MISSING_PROPERTY, PARAM_PASSWORD_COLUMN, "JDBCRealm"));
        }
        if (groupNameColumn == null) {
            throw new BadRealmException(MessageFormat.format(MISSING_PROPERTY, PARAM_GROUP_NAME_COLUMN, "JDBCRealm"));
        }

        passwordQuery = "SELECT " + passwordColumn + " FROM " + userTable + " WHERE " + userNameColumn + " = ?";

        groupQuery = "SELECT " + groupNameColumn + " FROM " + groupTable + " WHERE " + groupTableUserNameColumn + " = ? ";

        if (!NONE.equalsIgnoreCase(digestAlgorithm)) {
            try {
                messageDigest = MessageDigest.getInstance(digestAlgorithm);
            } catch (NoSuchAlgorithmException e) {
                throw new BadRealmException(MessageFormat.format("Digest algorithm {0} is not supported.", digestAlgorithm));
            }
        }
        if (messageDigest != null && encoding == null) {
            encoding = DEFAULT_ENCODING;
        }

        setProperty(JAAS_CONTEXT_PARAM, jaasCtx);
        if (dbUser != null && dbPassword != null) {
            setProperty(PARAM_DB_USER, dbUser);
            setProperty(PARAM_DB_PASSWORD, dbPassword);
        }
        setProperty(PARAM_DATASOURCE_JNDI, dsJndi);
        setProperty(PARAM_DIGEST_ALGORITHM, digestAlgorithm);
        if (encoding != null) {
            setProperty(PARAM_ENCODING, encoding);
        }
        if (charset != null) {
            setProperty(PARAM_CHARSET, charset);
        }

        if (_logger.isLoggable(FINEST)) {
            _logger.finest(
                "JDBCRealm : " +
                JAAS_CONTEXT_PARAM + "= " + jaasCtx + ", " +
                PARAM_DATASOURCE_JNDI + " = " + dsJndi + ", " +
                PARAM_DB_USER + " = " + dbUser + ", " +
                PARAM_DIGEST_ALGORITHM + " = " + digestAlgorithm + ", " +
                PARAM_ENCODING + " = " + encoding + ", " +
                PARAM_CHARSET + " = " + charset);
        }

        groupCache = new HashMap<>();
        emptyVector = new Vector<>();
    }

    /**
     * Returns a short (preferably less than fifteen characters) description of the kind of authentication which is
     * supported by this realm.
     *
     * @return Description of the kind of authentication that is directly supported by this realm.
     */
    @Override
    public String getAuthType() {
        return AUTH_TYPE;
    }

    /**
     * Returns the name of all the groups that this user belongs to. It loads the result from groupCache first. This is
     * called from web path group verification, though it should not be.
     *
     * @param username Name of the user in this realm whose group listing is needed.
     * @return Enumeration of group names (strings).
     * @exception InvalidOperationException thrown if the realm does not support this operation - e.g. Certificate realm
     * does not support this operation.
     */
    @Override
    public Enumeration getGroupNames(String username) throws InvalidOperationException, NoSuchUserException {
        Vector vector = groupCache.get(username);
        if (vector == null) {
            String[] grps = findGroups(username);
            setGroupNames(username, grps);
            vector = groupCache.get(username);
        }
        return vector.elements();
    }

    private void setGroupNames(String username, String[] groups) {
        Vector<String> v = null;

        if (groups == null) {
            v = emptyVector;

        } else {
            v = new Vector<>(groups.length + 1);
            Collections.addAll(v, groups);
        }

        synchronized (this) {
            groupCache.put(username, v);
        }
    }

    /**
     * Invoke the native authentication call.
     *
     * @param username User to authenticate.
     * @param password Given password.
     * @returns true of false, indicating authentication status.
     *
     */
    public String[] authenticate(String username, char[] password) {
        String[] groups = null;
        if (isUserValid(username, password)) {
            groups = findGroups(username);
            groups = addAssignGroups(groups);
            setGroupNames(username, groups);
        }
        return groups;
    }

    @Override
    public boolean validate(String username, DigestAlgorithmParameter[] params) {
        final Password pass = getPassword(username);
        if (pass == null) {
            return false;
        }
        return validate(pass, params);
    }

    private Password getPassword(String username) {

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        boolean valid = false;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(passwordQuery);
            statement.setString(1, username);
            rs = statement.executeQuery();

            if (rs.next()) {
                final String pwd = rs.getString(1);
                if (!PRE_HASHED.equalsIgnoreCase(getProperty(PARAM_ENCODING))) {
                    return new Password() {

                        @Override
                        public byte[] getValue() {
                            return pwd.getBytes();
                        }

                        @Override
                        public int getType() {
                            return Password.PLAIN_TEXT;
                        }
                    };
                }
                return new Password() {

                    @Override
                    public byte[] getValue() {
                        return pwd.getBytes();
                    }

                    @Override
                    public int getType() {
                        return Password.HASHED;
                    }
                };
            }
        } catch (Exception ex) {
            _logger.log(Level.SEVERE, "jdbcrealm.invaliduser", username);
            _logger.log(Level.SEVERE, null, ex);
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "Cannot validate user", ex);
            }
        } finally {
            close(connection, statement, rs);
        }
        return null;

    }

    /**
     * Test if a user is valid
     *
     * @param user user's identifier
     * @param password user's password
     * @return true if valid
     */
    private boolean isUserValid(String user, char[] password) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        boolean valid = false;

        try {
            char[] hpwd = hashPassword(password);
            connection = getConnection();
            statement = connection.prepareStatement(passwordQuery);
            statement.setString(1, user);
            rs = statement.executeQuery();
            if (rs.next()) {
                // Obtain the password as a char[] with a max size of 50
                try (Reader reader = rs.getCharacterStream(1)) {
                    char[] pwd = new char[1024];
                    int noOfChars = reader.read(pwd);

                    /*
                     * Since pwd contains 1024 elements arbitrarily initialized, construct a new char[] that has the right no of char
                     * elements to be used for equal comparison
                     */
                    if (noOfChars < 0) {
                        noOfChars = 0;
                    }
                    char[] passwd = new char[noOfChars];
                    System.arraycopy(pwd, 0, passwd, 0, noOfChars);
                    if (HEX.equalsIgnoreCase(getProperty(PARAM_ENCODING))) {
                        valid = true;
                        // Do a case-insensitive equals
                        for (int i = 0; i < noOfChars; i++) {
                            if (!(Character.toLowerCase(passwd[i]) == Character.toLowerCase(hpwd[i]))) {
                                valid = false;
                                break;
                            }
                        }
                    } else {
                        valid = Arrays.equals(passwd, hpwd);
                    }
                }
            }
        } catch (SQLException ex) {
            _logger.log(Level.SEVERE, "jdbcrealm.invaliduserreason", new String[] { user, ex.toString() });
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "Cannot validate user", ex);
            }
        } catch (Exception ex) {
            _logger.log(Level.SEVERE, "jdbcrealm.invaliduser", user);
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "Cannot validate user", ex);
            }
        } finally {
            close(connection, statement, rs);
        }
        return valid;
    }

    private char[] hashPassword(char[] password) throws CharacterCodingException {
        byte[] bytes = null;
        char[] result = null;
        String charSet = getProperty(PARAM_CHARSET);
        bytes = Utility.convertCharArrayToByteArray(password, charSet);

        if (messageDigest != null) {
            synchronized (messageDigest) {
                messageDigest.reset();
                bytes = messageDigest.digest(bytes);
            }
        }

        String encoding = getProperty(PARAM_ENCODING);
        if (HEX.equalsIgnoreCase(encoding)) {
            result = hexEncode(bytes);
        } else if (BASE64.equalsIgnoreCase(encoding)) {
            result = base64Encode(bytes).toCharArray();
        } else { // no encoding specified
            result = Utility.convertByteArrayToCharArray(bytes, charSet);
        }
        return result;
    }

    private char[] hexEncode(byte[] bytes) {
        StringBuilder sb = new StringBuilder(2 * bytes.length);
        for (byte element : bytes) {
            int low = element & 0x0f;
            int high = (element & 0xf0) >> 4;
            sb.append(HEXADECIMAL[high]);
            sb.append(HEXADECIMAL[low]);
        }
        char[] result = new char[sb.length()];
        sb.getChars(0, sb.length(), result, 0);
        return result;
    }

    private String base64Encode(byte[] bytes) {
        GFBase64Encoder encoder = new GFBase64Encoder();
        return encoder.encode(bytes);

    }

    /**
     * Delegate method for retreiving users groups
     *
     * @param user user's identifier
     * @return array of group key
     */
    private String[] findGroups(String user) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            connection = getConnection();
            statement = connection.prepareStatement(groupQuery);
            statement.setString(1, user);
            rs = statement.executeQuery();
            final List<String> groups = new ArrayList<>();
            while (rs.next()) {
                groups.add(rs.getString(1));
            }
            final String[] groupArray = new String[groups.size()];
            return groups.toArray(groupArray);
        } catch (Exception ex) {
            _logger.log(Level.SEVERE, "jdbcrealm.grouperror", user);
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "Cannot load group", ex);
            }
            return null;
        } finally {
            close(connection, statement, rs);
        }
    }

    private void close(Connection conn, PreparedStatement stmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (Exception ex) {
            }
        }

        if (stmt != null) {
            try {
                stmt.close();
            } catch (Exception ex) {
            }
        }

        if (conn != null) {
            try {
                conn.close();
            } catch (Exception ex) {
            }
        }
    }

    /**
     * Return a connection from the properties configured
     *
     * @return a connection
     */
    private Connection getConnection() throws LoginException {
        final SimpleJndiName dsJndi = SimpleJndiName.of(getProperty(PARAM_DATASOURCE_JNDI));
        final String dbUser = getProperty(PARAM_DB_USER);
        final String dbPassword = getProperty(PARAM_DB_PASSWORD);

        try {
            ConnectorRuntime connectorRuntime = getDefaultHabitat().getServiceHandle(connectorRuntimeDescriptor).getService();
            final DataSource dataSource = (DataSource) connectorRuntime.lookupNonTxResource(dsJndi, false);

            Connection connection = null;
            if (dbUser != null && dbPassword != null) {
                connection = dataSource.getConnection(dbUser, dbPassword);
            } else {
                connection = dataSource.getConnection();
            }

            return connection;
        } catch (Exception ex) {
            LoginException loginEx = new LoginException(MessageFormat.format("Unable to connect to datasource {0} for database user {1}.", dsJndi, dbUser));
            loginEx.initCause(ex);
            throw loginEx;
        }
    }
}
