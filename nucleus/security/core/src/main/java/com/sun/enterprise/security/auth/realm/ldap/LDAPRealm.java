/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.auth.realm.ldap;

import com.sun.enterprise.security.auth.realm.Realm;
import com.sun.enterprise.security.auth.realm.exceptions.BadRealmException;
import com.sun.enterprise.security.auth.realm.exceptions.InvalidOperationException;
import com.sun.enterprise.security.auth.realm.exceptions.NoSuchRealmException;
import com.sun.enterprise.security.auth.realm.exceptions.NoSuchUserException;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;

import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.security.auth.login.LoginException;

import org.glassfish.internal.api.RelativePathResolver;
import org.glassfish.main.jdke.props.SystemProperties;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.util.Utility.isAnyNull;
import static java.util.Collections.enumeration;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static javax.naming.Context.SECURITY_CREDENTIALS;
import static javax.naming.Context.SECURITY_PRINCIPAL;
import static javax.naming.directory.SearchControls.SUBTREE_SCOPE;

/**
 * Realm wrapper for supporting LDAP authentication.
 *
 * <P>
 * See LDAPLoginModule documentation for more details on the operation of the LDAP realm and login module.
 *
 * <P>
 * The ldap realm needs the following properties in its configuration:
 * <ul>
 *   <li>directory - URL of LDAP directory to use
 *   <li>base-dn - The base DN to use for user searches.
 *   <li>jaas-ctx - JAAS context name used to access LoginModule for authentication.
 * </ul>
 *
 * <P>
 * Besides JDK Context properties start with java.naming, javax.security, one can also set connection pool related
 * properties starting with com.sun.jndi.ldap.connect.pool. See
 * http://java.sun.com/products/jndi/tutorial/ldap/connect/config.html for details. Also, the following optional
 * attributes can also be specified:
 *
 * <ul>
 *   <li>search-filter - LDAP filter to use for searching for the user entry based on username given to iAS. The default
 * value is <code>uid=%s</code> where %s is expanded to the username.
 *   <li>group-base-dn - The base DN to use for group searches. By default its value is the same as base-dn.
 *   <li>group-search-filter - The LDAP filter to use for searching group membership of a given user. The default value is
 *       <code>uniquemember=%d</code> where %d is expanded to the DN of the user found by the user search.
 *   <li>group-target - The attribute which value(s) are interpreted as group membership names of the user. Default value
 *       is <code>cn</code>.
 *   <li>search-bind-dn - The dn of ldap user. optional and no default value.
 *   <li>search-bind-password - The password of search-bind-dn.optional and no default value.
 *   <li>pool-size - The JNDI ldap connection pool size.
 * </ul>
 *
 * @see com.sun.enterprise.security.auth.login.LDAPLoginModule
 *
 */
@Service
public final class LDAPRealm extends Realm {
    // Descriptive string of the authentication type of this realm.
    public static final String AUTH_TYPE = "ldap";

    // These are property names which should be in auth-realm in server.xml
    public static final String PARAM_DIRURL = "directory";
    public static final String PARAM_USERDN = "base-dn";

    // These are optional, defaults are provided
    // %s = subject name
    // %d = DN of user search result
    public static final String PARAM_SEARCH_FILTER = "search-filter";
    public static final String PARAM_GRPDN = "group-base-dn";
    public static final String PARAM_GRP_SEARCH_FILTER = "group-search-filter";
    public static final String PARAM_GRP_TARGET = "group-target";
    public static final String PARAM_DYNAMIC_GRP_FILTER = "dynamic-group-search-filter";
    public static final String PARAM_DYNAMIC_GRP_TARGET = "dynamic-group-target";
    public static final String PARAM_MODE = "mode";
    public static final String PARAM_JNDICF = "jndiCtxFactory";
    public static final String PARAM_POOLSIZE = "pool-size";

    // These are optional, no default values are provided
    public static final String PARAM_BINDDN = "search-bind-dn";
    public static final String PARAM_BINDPWD = "search-bind-password";

    // Only find-bind mode is supported so mode attribute is not exposed yet
    public static final String MODE_FIND_BIND = "find-bind";

    // Expansion strings
    public static final String SUBST_SUBJECT_NAME = "%s";
    public static final String SUBST_SUBJECT_DN = "%d";

    // Defaults
    private static final String SEARCH_FILTER_DEFAULT = "uid=" + SUBST_SUBJECT_NAME;
    private static final String GRP_SEARCH_FILTER_DEFAULT = "uniquemember=" + SUBST_SUBJECT_DN;
    private static final String GRP_TARGET_DEFAULT = "cn";
    private static final String DYNAMIC_GRP_TARGET_DEFAULT = "ismemberof";// "memberOf";
    private static final String MODE_DEFAULT = MODE_FIND_BIND;
    private static final String JNDICF_DEFAULT = "com.sun.jndi.ldap.LdapCtxFactory";
    private static final int POOLSIZE_DEFAULT = 5;

    private final String[] _dnOnly = { "dn" };

    private static final String SUN_JNDI_POOL = "com.sun.jndi.ldap.connect.pool";
    private static final String SUN_JNDI_POOL_ = "com.sun.jndi.ldap.connect.pool.";
    private static final String SUN_JNDI_POOL_PROTOCOL = "com.sun.jndi.ldap.connect.pool.protocol";
    private static final String SUN_JNDI_POOL_MAXSIZE = "com.sun.jndi.ldap.connect.pool.maxsize";

    // Dynamic group related properties.
    private static final String DYNAMIC_GROUP_OBJECT_FACTORY = "com.sun.jndi.ldap.obj.LdapGroupFactory";
    public static final String DYNAMIC_GROUP_FACTORY_OBJECT_PROPERTY = "java.naming.factory.object";
    private static final String DYNAMIC_GROUP_STATE_FACTORY = "com.sun.jndi.ldap.obj.LdapGroupFactory";
    public static final String DYNAMIC_GROUP_STATE_FACTORY_PROPERTY = "java.naming.factory.state";
    public static final String LDAP_SOCKET_FACTORY = "java.naming.ldap.factory.socket";
    public static final String DEFAULT_SSL_LDAP_SOCKET_FACTORY = "com.sun.enterprise.security.auth.realm.ldap.CustomSocketFactory";
    public static final String LDAPS_URL = "ldaps://";
    public static final String DEFAULT_POOL_PROTOCOL = "plain ssl";

    public static final String DYNAMIC_GROUP_FILTER = "(&(objectclass=groupofuniquenames)(objectclass=*groupofurls*))";

    public static final String SSL = "SSL";

    private HashMap<String, Vector<String>> groupCache;
    private Vector<String> emptyVector;
    private final Properties ldapBindProps = new Properties();

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
     * Initialize a realm with some properties. This can be used when instantiating realms from their descriptions. This
     * method may only be called a single time.
     *
     * @param props Initialization parameters used by this realm.
     * @exception BadRealmException If the configuration parameters identify a corrupt realm.
     * @exception NoSuchRealmException If the configuration parameters specify a realm which doesn't exist.
     *
     */
    @Override
    public synchronized void init(Properties props) throws BadRealmException, NoSuchRealmException {
        super.init(props);

        String url = props.getProperty(PARAM_DIRURL);
        String dn = props.getProperty(PARAM_USERDN);
        String jaasCtx = props.getProperty(JAAS_CONTEXT_PARAM);

        if (isAnyNull(url, dn, jaasCtx)) {
            throw new BadRealmException(MessageFormat.format(
                "Incomplete configuration of ldap realm: url: {0} baseDN: {1} login module: {2}", url, dn, jaasCtx));
        }

        setProperty(PARAM_DIRURL, url);
        ldapBindProps.setProperty(Context.PROVIDER_URL, url);
        setProperty(PARAM_USERDN, dn);
        setProperty(JAAS_CONTEXT_PARAM, jaasCtx);

        String mode = props.getProperty(PARAM_MODE, MODE_DEFAULT);
        if (!MODE_DEFAULT.equals(mode)) {
            throw new BadRealmException(MessageFormat.format("Unsupported mode {0}.", mode));
        }

        setProperty(PARAM_MODE, mode);

        String ctxF = props.getProperty(PARAM_JNDICF, JNDICF_DEFAULT);
        setProperty(PARAM_JNDICF, ctxF);
        ldapBindProps.setProperty(Context.INITIAL_CONTEXT_FACTORY, ctxF);

        String searchFilter = props.getProperty(PARAM_SEARCH_FILTER, SEARCH_FILTER_DEFAULT);
        setProperty(PARAM_SEARCH_FILTER, searchFilter);

        String grpDN = props.getProperty(PARAM_GRPDN, dn);
        setProperty(PARAM_GRPDN, grpDN);

        String grpSearchFilter = props.getProperty(PARAM_GRP_SEARCH_FILTER, GRP_SEARCH_FILTER_DEFAULT);
        setProperty(PARAM_GRP_SEARCH_FILTER, grpSearchFilter);

        String dynGrpSearchFilter = props.getProperty(PARAM_DYNAMIC_GRP_FILTER, SEARCH_FILTER_DEFAULT);
        setProperty(PARAM_DYNAMIC_GRP_FILTER, dynGrpSearchFilter);

        String grpTarget = props.getProperty(PARAM_GRP_TARGET, GRP_TARGET_DEFAULT);
        setProperty(PARAM_GRP_TARGET, grpTarget);

        String dynGrpTarget = props.getProperty(PARAM_DYNAMIC_GRP_TARGET, DYNAMIC_GRP_TARGET_DEFAULT);
        setProperty(PARAM_DYNAMIC_GRP_TARGET, dynGrpTarget);

        String objectFactory = props.getProperty(DYNAMIC_GROUP_FACTORY_OBJECT_PROPERTY, DYNAMIC_GROUP_OBJECT_FACTORY);
        setProperty(DYNAMIC_GROUP_FACTORY_OBJECT_PROPERTY, objectFactory);
        ldapBindProps.setProperty(DYNAMIC_GROUP_FACTORY_OBJECT_PROPERTY, objectFactory);

        String stateFactory = props.getProperty(DYNAMIC_GROUP_STATE_FACTORY_PROPERTY, DYNAMIC_GROUP_STATE_FACTORY);
        setProperty(DYNAMIC_GROUP_STATE_FACTORY_PROPERTY, stateFactory);
        ldapBindProps.setProperty(DYNAMIC_GROUP_STATE_FACTORY_PROPERTY, stateFactory);

        String bindDN = props.getProperty(PARAM_BINDDN);
        if (bindDN != null) {
            this.setProperty(PARAM_BINDDN, bindDN);
            ldapBindProps.setProperty(SECURITY_PRINCIPAL, bindDN);
        }

        String bindPWD = props.getProperty(PARAM_BINDPWD);
        if (bindPWD != null) {
            // If the password is aliased, de-alias it
            try {
                bindPWD = RelativePathResolver.getRealPasswordFromAlias(bindPWD);
            } catch (Exception ex) {
                _logger.log(WARNING, "ldaprealm.pwd.dealiasing.failed", ex);
            }

            this.setProperty(PARAM_BINDPWD, bindPWD);
            ldapBindProps.setProperty(SECURITY_CREDENTIALS, bindPWD);
        }

        Set<String> penum = props.stringPropertyNames();
        for (String propName : penum) {
            if (propName.startsWith("java.naming.") || propName.startsWith("javax.security.") || propName.startsWith("com.sun.jndi.ldap.")) {
                ldapBindProps.setProperty(propName, props.getProperty(propName));
            } else if (propName.startsWith(SUN_JNDI_POOL_) && !SUN_JNDI_POOL_MAXSIZE.equals(propName)) {
                SystemProperties.setProperty(propName, props.getProperty(propName), false);
            }
        }

        String poolSize = Integer.getInteger(PARAM_POOLSIZE, POOLSIZE_DEFAULT).toString();
        String sunPoolSizeStr = props.getProperty(SUN_JNDI_POOL_MAXSIZE, poolSize);

        // Precedence rule: SUN_JNDI_POOL_MAXSIZE > PARAM_POOLSIZE > POOLSIZE_DEFAULT
        try {
            sunPoolSizeStr = Integer.valueOf(sunPoolSizeStr).toString();
        } catch (Exception ex) {
            sunPoolSizeStr = poolSize;
        }
        SystemProperties.setProperty(SUN_JNDI_POOL_MAXSIZE, sunPoolSizeStr, false);
        setProperty(PARAM_POOLSIZE, sunPoolSizeStr);

        String usePool = props.getProperty(SUN_JNDI_POOL, "true");
        ldapBindProps.setProperty(SUN_JNDI_POOL, usePool);

        if (url.startsWith(LDAPS_URL)) {
            ldapBindProps.setProperty(LDAP_SOCKET_FACTORY, DEFAULT_SSL_LDAP_SOCKET_FACTORY);
            SystemProperties.setProperty(SUN_JNDI_POOL_PROTOCOL, DEFAULT_POOL_PROTOCOL, false);
            _logger.log(FINE, "LDAPRealm : Using custom socket factory for SSL with pooling");
        }

        if (_logger.isLoggable(FINE)) {
            Properties tempProps = (Properties) ldapBindProps.clone();
            tempProps.remove(SECURITY_CREDENTIALS);
            _logger.log(FINE, "LDAPRealm : " + tempProps);
        }

        groupCache = new HashMap<>();
        emptyVector = new Vector<>();
    }

    /**
     * Returns the name of all the groups that this user belongs to. Note that this information is only known after the user
     * has logged in. This is called from web path role verification, though it should not be.
     *
     * @param username Name of the user in this realm whose group listing is needed.
     * @return Enumeration of group names (strings).
     * @exception InvalidOperationException thrown if the realm does not support this operation - e.g. Certificate realm
     * does not support this operation.
     */
    @Override
    public Enumeration<String> getGroupNames(String username) throws NoSuchUserException {
        Vector<String> groupNames = groupCache.get(username);

        if (groupNames == null) {
            // Note : assuming the username is a userDN here
            List<String> searchedGroups = getGroups(username);
            if (searchedGroups != null) {
                return enumeration(searchedGroups);
            }

            _logger.log(FINE, () -> "No groups available for: " + username);

            // We don't load group here as we need to bind ctx to user with
            // password before doing that and password is not available here
            return emptyVector.elements();
        }

        if (groupMapper != null) {
            Vector<String> ret = new Vector<>();
            ret.addAll(groupNames);
            List<String> result = new ArrayList<>();
            for (String grp : groupNames) {
                List<String> tmp = getMappedGroupNames(grp);
                result.addAll(tmp);
            }
            ret.addAll(result);
            return ret.elements();
        }

        return groupNames.elements();
    }

    /**
     * Set group membership info for a user.
     *
     * <P>
     * See bugs 4646133,4646270 on why this is here.
     *
     */
    private void setGroupNames(String username, String[] groups) {
        Vector<String> groupNames = new Vector<>(groups.length);
        for (String group : groups) {
            groupNames.add(group);
        }

        groupCache.put(username, groupNames);
    }

    /**
     * Supports mode=find-bind. See class documentation.
     *
     */
    public String[] findAndBind(String _username, char[] _password) throws LoginException {
        // Do search for user, substituting %s for username
        _username = RFC2254Encode(_username);

        StringBuilder userIdBuilder = new StringBuilder(getProperty(PARAM_SEARCH_FILTER));
        substitute(userIdBuilder, SUBST_SUBJECT_NAME, _username);
        String userid = userIdBuilder.toString();

        // Attempt to bind as the user
        DirContext ctx = null;
        String srcFilter = null;
        String[] groups = null;

        String dynFilter = null;
        String dynMember = getProperty(PARAM_DYNAMIC_GRP_TARGET);
        try {
            ctx = new InitialDirContext(getLdapBindProps());
            String realUserDN = userSearch(ctx, getProperty(PARAM_USERDN), userid);
            if (realUserDN == null) {
                throw new LoginException(MessageFormat.format("User {0} not found.", _username));
            }

            boolean bindSuccessful = bindAsUser(realUserDN, _password);
            if (bindSuccessful == false) {
                throw new LoginException(MessageFormat.format("LDAP bind failed for {0}.", realUserDN));
            }

            // Search groups using above connection, substituting %d (and %s)
            StringBuilder srcFilterBuilder = new StringBuilder(getProperty(PARAM_GRP_SEARCH_FILTER));
            StringBuilder dynamicFilterBuilder = new StringBuilder(getProperty(PARAM_DYNAMIC_GRP_FILTER));

            substitute(srcFilterBuilder, SUBST_SUBJECT_NAME, _username);
            substitute(srcFilterBuilder, SUBST_SUBJECT_DN, realUserDN);
            substitute(dynamicFilterBuilder, SUBST_SUBJECT_NAME, _username);
            substitute(dynamicFilterBuilder, SUBST_SUBJECT_DN, realUserDN);

            srcFilter = srcFilterBuilder.toString();
            dynFilter = dynamicFilterBuilder.toString();
            ArrayList<String> groupsList = new ArrayList<>();
            groupsList.addAll(groupSearch(ctx, getProperty(PARAM_GRPDN), srcFilter, getProperty(PARAM_GRP_TARGET)));

            // Search filter is constructed internally as as a groupofURLS
            groupsList.addAll(dynamicGroupSearch(ctx, getProperty(PARAM_GRPDN), dynMember, dynFilter, getProperty(PARAM_GRP_TARGET)));
            groups = new String[groupsList.size()];
            groupsList.toArray(groups);
        } catch (Exception e) {
            LoginException le = new LoginException(e.toString());
            le.initCause(e);
            _logger.log(SEVERE, "ldaprealm.exception", le);
            throw le;
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException e) {
                }
            }
        }

        if (_logger.isLoggable(FINE)) {
            _logger.log(FINE, "LDAP:Group search filter: " + srcFilter);

            StringBuilder gb = new StringBuilder();
            gb.append("Group memberships found: ");
            if (groups.length > 0) {
                for (String element : groups) {
                    gb.append(" " + element);
                }
            } else {
                gb.append("(null)");
            }

            _logger.log(FINE, "LDAP: " + gb.toString());
        }

        groups = addAssignGroups(groups);
        groups = this.addMappedGroupNames(groups);
        setGroupNames(_username, groups);

        if (_logger.isLoggable(FINE)) {
            _logger.log(FINE, "LDAP: login succeeded for: " + _username);
        }

        return groups;
    }

    private String[] addMappedGroupNames(String[] groups) {
        if (groupMapper == null) {
            return groups;
        }

        List<String> finalresult = new ArrayList<>();
        for (String group : groups) {
            List<String> result = new ArrayList<>();
            groupMapper.getMappedGroups(group, result);
            finalresult.add(group);
            if (!result.isEmpty()) {
                finalresult.addAll(result);
            }
        }

        return finalresult.toArray(new String[finalresult.size()]);
    }

    /**
     * Get binding properties defined in server.xml for LDAP server.
     *
     */
    private Properties getLdapBindProps() {
        return (Properties) ldapBindProps.clone();
    }

    private List<String> getGroups(String userDN) {
        // No authentication has happened through the realm.
        DirContext ctx = null;
        String srcFilter = null;

        String dynFilter = null;
        String dynMember = getProperty(PARAM_DYNAMIC_GRP_TARGET);
        try {
            ctx = new InitialDirContext(getLdapBindProps());

            String _username = userDN;
            try {
                _username = new LdapName(userDN).getRdns().stream().filter(rdn -> rdn.getType().equalsIgnoreCase("cn"))
                        .map(rdn -> rdn.getValue().toString()).findFirst().orElseGet(null);

            } catch (InvalidNameException e) {
                // Ignoring the exception to suppot simple group names as userDN
                // Issue GLASSFISH-19595
            }

            if (_username == null && userDN != null && userDN.startsWith("uid")) {
                // Handle uid=XXX here where cn is not present
                // TODO :maybe there is a better way to handle this??
                int first = userDN.indexOf("uid=");
                int last = userDN.indexOf(",");
                if (first != -1 && last != -1) {
                    _username = userDN.substring(first + 4, last);
                }

            }
            StringBuilder searchFilterBuilder = new StringBuilder(getProperty(PARAM_GRP_SEARCH_FILTER));
            StringBuilder dynamicFilterBuilder = new StringBuilder(getProperty(PARAM_DYNAMIC_GRP_FILTER));

            substitute(searchFilterBuilder, SUBST_SUBJECT_NAME, _username);
            substitute(searchFilterBuilder, SUBST_SUBJECT_DN, userDN);
            substitute(dynamicFilterBuilder, SUBST_SUBJECT_NAME, _username);
            substitute(dynamicFilterBuilder, SUBST_SUBJECT_DN, userDN);

            srcFilter = searchFilterBuilder.toString();
            dynFilter = dynamicFilterBuilder.toString();
            List<String> groupsList = new ArrayList<>();
            groupsList.addAll(groupSearch(ctx, getProperty(PARAM_GRPDN), srcFilter, getProperty(PARAM_GRP_TARGET)));

            // Search filter is constructed internally as as a groupofURLS
            groupsList.addAll(dynamicGroupSearch(ctx, getProperty(PARAM_GRPDN), dynMember, dynFilter, getProperty(PARAM_GRP_TARGET)));

            return groupsList;
        } catch (Exception e) {
            _logger.log(WARNING, "ldaprealm.groupsearcherror", e);
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException e) {
                    _logger.log(WARNING, "ldaprealm.exception", e);
                }
            }
        }

        return null;
    }

    /**
     * Do anonymous search for the user. Should be unique if exists.
     *
     */
    private String userSearch(DirContext ctx, String baseDN, String filter) {
        if (_logger.isLoggable(FINEST)) {
            _logger.log(FINE, "search: baseDN: " + baseDN + "  filter: " + filter);
        }

        SearchControls searchControls = new SearchControls();
        searchControls.setReturningAttributes(_dnOnly);
        searchControls.setSearchScope(SUBTREE_SCOPE);
        searchControls.setCountLimit(1);

        NamingEnumeration<SearchResult> namingEnum = null;
        String foundDN;
        try {
            namingEnum = ctx.search(baseDN, filter, searchControls);
            if (namingEnum.hasMore()) {
                SearchResult res = namingEnum.next();

                StringBuffer sb = new StringBuffer();
                // For dn name with '/'
                CompositeName compDN = new CompositeName(res.getName());
                String ldapDN = compDN.get(0);
                sb.append(ldapDN);

                if (res.isRelative()) {
                    sb.append(",");
                    sb.append(baseDN);
                }
                foundDN = sb.toString();
                if (_logger.isLoggable(FINEST)) {
                    _logger.log(FINE, "Found user DN: " + foundDN);
                }

                return foundDN;
            }
            return null;
        } catch (Exception e) {
            _logger.log(WARNING, "ldaprealm.searcherror", filter);
            _logger.log(WARNING, "security.exception", e);
            return null;
        } finally {
            if (namingEnum != null) {
                try {
                    namingEnum.close();
                } catch (Exception ex) {
                }
            }
        }
    }

    /**
     * Attempt to bind as a specific DN.
     *
     */
    private boolean bindAsUser(String bindDN, char[] password) {
        boolean bindSuccessful = false;

        Properties ldapProperties = getLdapBindProps();

        ldapProperties.put(SECURITY_PRINCIPAL, bindDN);
        ldapProperties.put(SECURITY_CREDENTIALS, new String(password));

        DirContext ctx = null;
        try {
            ctx = new InitialDirContext(ldapProperties);
            bindSuccessful = true;
        } catch (Exception e) {
            if (_logger.isLoggable(FINEST)) {
                _logger.finest("Error binding to directory as: " + bindDN);
                _logger.finest("Exception from JNDI: " + e.toString());
            }
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException e) {
                }
            }
        }

        return bindSuccessful;
    }

    /**
     * Search for group membership using the given connection.
     *
     */
    private List<String> dynamicGroupSearch(DirContext ctx, String baseDN, String memberOfAttr, String filter, String target)
            throws NamingException {
        List<String> groupList = new ArrayList<>();

        String[] targets = new String[] { memberOfAttr };

        try {
            SearchControls ctls = new SearchControls();
            ctls.setReturningAttributes(targets);
            ctls.setSearchScope(SUBTREE_SCOPE);

            // Set this to false to avoid objects and hence exposing ldap object injection.
            ctls.setReturningObjFlag(false);

            NamingEnumeration<SearchResult> e = ctx.search(baseDN, filter, ctls);
            while (e.hasMore()) {
                SearchResult res = e.next();
                Attribute isMemberOf = res.getAttributes().get(memberOfAttr);
                if (isMemberOf != null) {
                    for (Enumeration<?> values = isMemberOf.getAll(); values.hasMoreElements();) {
                        String groupDN = (String) values.nextElement();
                        LdapName dn = new LdapName(groupDN);
                        for (Rdn rdn : dn.getRdns()) {
                            if (rdn.getType().equalsIgnoreCase(target)) {
                                groupList.add((String) rdn.getValue());
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            _logger.log(WARNING, "ldaprealm.searcherror", filter);
            _logger.log(WARNING, "security.exception", e);
        }
        return groupList;
    }

    /**
     * Search for group membership using the given connection.
     *
     */
    private List<String> groupSearch(DirContext ctx, String baseDN, String filter, String target) {
        List<String> groups = new ArrayList<>();

        try {
            String[] targets = new String[1];
            targets[0] = target;

            SearchControls searchControls = new SearchControls();
            searchControls.setReturningAttributes(targets);
            searchControls.setSearchScope(SUBTREE_SCOPE);

            NamingEnumeration<SearchResult> e = ctx.search(baseDN,
                    filter.replaceAll(Matcher.quoteReplacement("\\"), Matcher.quoteReplacement("\\\\")), searchControls);

            while (e.hasMore()) {
                SearchResult res = e.next();
                Attribute grpAttr = res.getAttributes().get(target);
                int sz = grpAttr.size();
                for (int i = 0; i < sz; i++) {
                    String s = (String) grpAttr.get(i);
                    groups.add(s);
                }
            }

        } catch (Exception e) {
            _logger.log(WARNING, "ldaprealm.searcherror", filter);
            _logger.log(WARNING, "security.exception", e);
        }

        return groups;
    }

    /**
     * Do string substitution. target is replaced by value for all occurences.
     *
     */
    private static void substitute(StringBuilder sb, String target, String value) {
        int i = sb.indexOf(target);
        while (i >= 0) {
            sb.replace(i, i + target.length(), value);
            i = sb.indexOf(target);
        }
    }

    /**
     * Escape special chars in search filter, according to RFC2254
     *
     * @param inName
     * @return
     */
    private String RFC2254Encode(String inName) {
        int len = inName.length();
        StringBuilder buf = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char ch = inName.charAt(i);
            switch (ch) {
            case '*':
                buf.append("\\2a");
                break;
            case '(':
                buf.append("\\28");
                break;
            case ')':
                buf.append("\\29");
                break;
            case '\\':
                buf.append("\\5c");
                break;
            case 0:
                buf.append("\\00");
                break;
            default:
                buf.append(ch);
            }
        }
        return buf.toString();
    }
}
