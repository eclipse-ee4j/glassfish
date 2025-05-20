/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

package com.sun.appserv.management.client.prefs;

import com.sun.enterprise.security.store.AsadminSecurityUtil;
import com.sun.enterprise.util.Utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A {@link LoginInfoStore} that reads the information from the default file ".gfclient/pass"
 * and stores it as a map in the memory. It is not guaranteed that the concurrent
 * modifications will yield consistent results. This class is <i> not </i> thread safe. The
 * serial access has to be ensured by the callers.
 * @since Appserver 9.0
 */
public class MemoryHashLoginInfoStore implements LoginInfoStore {

    private static final String DEFAULT_STORE_NAME = "pass";

    private Map<HostPortKey, LoginInfo> state;
    private final File store;

    /**
     * Creates a new instance of MemoryHashLoginInfoStore. A side effect of calling
     * this constructor is that if the default store does not exist, it will be created.
     * This does not pose any harm or surprises.
     */
    public MemoryHashLoginInfoStore() throws StoreException {
        BufferedReader br = null;
        BufferedWriter bw = null;
        try {
            store = new File(AsadminSecurityUtil.GF_CLIENT_DIR, DEFAULT_STORE_NAME);
            if (store.createNewFile()) {
                bw = new BufferedWriter(new FileWriter(store, UTF_8));
                FileMapTransform.writePreamble(bw);
                state = new HashMap<>();
            } else {
                br = new BufferedReader(new FileReader(store, UTF_8));
                state = FileMapTransform.readAll(br);
            }
        } catch (final Exception e) {
            throw new StoreException(e);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (final Exception ee) {
            } // ignore
            try {
                if (bw != null) {
                    bw.close();
                }
            } catch (final Exception ee) {
            } // ignore
        }
    }

    @Override
    public void store(final LoginInfo login) throws StoreException {
        this.store(login, false);
    }

    @Override
    public void store(final LoginInfo login, boolean overwrite) throws StoreException {
        Objects.requireNonNull(login, "login");
        final String host = login.getHost();
        final int port    = login.getPort();
        if (!overwrite && this.exists(host, port)) {
            throw new StoreException("Login exists for host: " + host + " port: " + port);
        }
        final HostPortKey key = new HostPortKey(host, port);
        final LoginInfo old   = state.get(key);
        state.put(key, login);
        //System.out.println("committing: " + login);
        commit(key, old);
        protect();
    }

    @Override
    public void remove(final String host, final int port) {
        final HostPortKey key = new HostPortKey(host, port);
        final LoginInfo gone  = state.remove(key);
        commit(key, gone);
    }

    @Override
    public LoginInfo read(String host, int port) {
        final HostPortKey key = new HostPortKey(host, port);
        final LoginInfo login = state.get(key); //no need to access disk
        return ( login );
    }

    @Override
    public boolean exists(String host, int port) {
        final HostPortKey key = new HostPortKey(host, port);
        final boolean exists  = state.containsKey(key); //no need to access disk
        return ( exists );
    }

    @Override
    public int size() {
        return ( state.size() ); // no need to access disk
    }

    @Override
    public Collection<LoginInfo> list() {
        final Collection<LoginInfo> logins = state.values(); // no need to access disk
        return (Collections.unmodifiableCollection(logins) );
    }

    @Override
    public String getName() {
        return ( store.getAbsoluteFile().getAbsolutePath() );
    }

    private void commit(final HostPortKey key, final LoginInfo old) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(store, UTF_8))) {
            FileMapTransform.writeAll(state.values(), writer);
        } catch (final Exception e) {
            // try to roll back, first memory
            state.put(key, old);
            // then disk, if the old value is not null
            try {
                if (old != null) {
                    try (BufferedWriter writer2 = new BufferedWriter(new FileWriter(store, UTF_8))) {
                        FileMapTransform.writeAll(state.values(), writer2);
                    }
                }
            } catch (final Exception ae) {
                throw new RuntimeException("Catastrophe, can't write it to file", ae);
            }
        }
    }


    private void protect() {
        /*
         * note: if this is Windows we still try 'chmod' -- they may have MKS or
         * some other UNIXy package for Windows.
         * cacls is too dangerous to use because it requires a "Y" to be written to
         * stdin of the cacls process. If cacls doesn't exist or if they are using
         * a non-NTFS file system we would hang here forever.
         */
        try {
            if (store == null || !store.exists()) {
                return;
            }

            ProcessBuilder pb = new ProcessBuilder("chmod", "0600", store.getAbsolutePath());
            pb.start();
        } catch (Exception e) {
            // we tried...
        }
    }

    private static class FileMapTransform {
        private FileMapTransform() {} //disallow
        static Map<HostPortKey, LoginInfo> readAll(final BufferedReader reader) throws IOException, URISyntaxException {
            String line;
            final Map<HostPortKey, LoginInfo> map = new HashMap<> ();
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue; //ignore comments
                }
                final int si = line.indexOf(' '); //index of space
                if (si == -1) {
                    throw new IOException("Error: invalid record: " + line);
                }
                final URI uri         = new URI(line.substring(0, si));
                final String encp     = line.substring(si+1, line.length());
                final HostPortKey key = uri2Key(uri);
                final LoginInfo value = line2LoginInfo(uri, encp);
                map.put(key, value);
            }
            return ( map );
        }
        static void writeAll(final Collection<LoginInfo> logins, final BufferedWriter writer) throws IOException, URISyntaxException {
            writePreamble(writer);
            //write out sorted, because not more than 100 logins are expected to be there
            final List<LoginInfo> list = new ArrayList<>(logins);
            Collections.sort(list);
            for (LoginInfo login : list) {
                //System.out.println("wrote: " + login);
                writeOne(login, writer);
            }
        }
        private static void writeOne(final LoginInfo login, final BufferedWriter writer) throws IOException, URISyntaxException {
            writer.write(login2Line(login));
            writer.newLine();
        }
        static HostPortKey uri2Key(final URI uri) {
            final String host     = uri.getHost();
            final int port        = uri.getPort();
            final HostPortKey key = new HostPortKey(host, port);
            return ( key );
        }
        static LoginInfo line2LoginInfo(final URI uri, final String encp) throws IOException {
            final String host     = uri.getHost();
            final int port        = uri.getPort();
            final String user     = uri.getUserInfo();
            final char[] password = Utility.convertByteArrayToCharArray(Base64.getDecoder().decode(encp), UTF_8);
            return new LoginInfo(host, port, user, password);
        }
        static String login2Line(final LoginInfo login) throws IOException, URISyntaxException {
            final String scheme   = "asadmin";
            final String host     = login.getHost();
            final int port        = login.getPort();
            final String user     = login.getUser();
            final URI uri         = new URI(scheme, user, host, port, null, null, null);
            final char[] password = login.getPassword();
            final String encp = Base64.getEncoder()
                .encodeToString(Utility.convertCharArrayToByteArray(password, UTF_8));
            final String line     = uri.toString() + ' ' + encp;

            return ( line );
        }
        static void writePreamble(final BufferedWriter bw) throws IOException {
            final String preamble = "# Do not edit this file by hand. Use login interface instead.";
            bw.write(preamble);
            bw.newLine();
        }
    }

    private static class HostPortKey {
        private final String host;
        private final int port;
        HostPortKey(final String host, final int port) {
            this.host = host;
            this.port = port;
        }
        @Override
        public boolean equals(final Object other) {
            boolean same = false;
            if (other instanceof HostPortKey) {
                final HostPortKey that = (HostPortKey)other;
                same = this.host.equals(that.host) && this.port == that.port;
            }
            return ( same );
        }
        @Override
        public int hashCode() {
            return ( 53 * host.hashCode() + 31 * port );
        }
    }
}
