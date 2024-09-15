/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.admin.cli.remote;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * This is a derivation of the CookieStore which provides load and store
 * methods which allows the cookies to be stored to and retreived from
 * a file.
 * This CookieStore is specialized for maintaining session cookies for
 * session routing and therefor has side effects that a generalized
 * cookie store would not have.   For example when cookies are stored
 * the URI associated with the cookie is not maintained.
 */
public class ClientCookieStore implements CookieStore {

    // 60 minutes
    private static final int CACHE_WRITE_DELTA = 60 * 60 * 1000;
    private static final URI COOKIE_URI = URI.create("http://CLI_Session/");
    private static final String CACHE_COMMENT = "# These cookies are used strictly for command routing.\n"
        + "# These cookies are not used for authentication and they are\n" + "# not assoicated with server state.";

    private final CookieStore cookieStore;
    private final File cookieStoreFile;

    public ClientCookieStore(CookieStore cookieStore, File file) {
        this.cookieStore = cookieStore;
        this.cookieStoreFile = file;
    }

    @Override
    public void add(URI uri, HttpCookie cookie) {
        cookieStore.add(uri, cookie);
    }

    @Override
    public List<HttpCookie> get(URI uri) {
        return cookieStore.get(uri);
    }

    @Override
    public List<HttpCookie> getCookies() {
        return cookieStore.getCookies();
    }

    @Override
    public List<URI> getURIs() {
        return cookieStore.getURIs();
    }

    @Override
    public boolean remove(URI uri, HttpCookie cookie) {
        return cookieStore.remove(uri, cookie);
    }

    @Override
    public boolean removeAll() {
        return cookieStore.removeAll();
    }

    public URI getStaticURI() {
        return COOKIE_URI;
    }

    /**
     * Load the persisted cookies into the CookieStore.
     *
     * The store has this schema:
     *
     * COOKIE1=xxx; ... COOKIE2=yyy; ...
     **/
    public void load() throws IOException {

        this.removeAll();

        if (!cookieStoreFile.exists()) {
            throw new IOException("File does not exist: " + cookieStoreFile);
        }

        try {
            in = new BufferedReader(new FileReader(cookieStoreFile));
            String str;
            while ((str = in.readLine()) != null) {

                // Ignore comment lines
                if (str.startsWith("#"))
                    continue;

                List<HttpCookie> cookies = HttpCookie.parse(str);
                for (HttpCookie cookie : cookies) {
                    this.add(getStaticURI(), cookie);
                }
            }
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {

            }
        }
    }


    /**
     * Store the cookies in the CookieStore to the provided location.
     * This method will overwrite the contents of the target file.
     */
    public void store() throws IOException {
        PrintWriter out = null;

        // Create the directory if it doesn't exist.
        if (!cookieStoreFile.getParentFile().exists() && !cookieStoreFile.getParentFile().mkdirs()) {
            throw new IOException("Unable to create directory: " + cookieStoreFile);
        }

        out = new PrintWriter(new BufferedWriter(new FileWriter(cookieStoreFile)));

        // Write comment at top of cache file.
        out.println(CACHE_COMMENT);

        for (URI uri : this.getURIs()) {
            for (HttpCookie cookie : this.get(uri)) {

                // Expire the cookie immediately if Max-Age = 0 or -1
                // Expire the cookie immediately if Discard is true
                if (cookie.getMaxAge() < 1 || cookie.getDiscard()) {
                    continue;
                }

                StringBuilder sb = new StringBuilder();

                sb.append(cookie.getName()).append("=").append(cookie.getValue());

                if (cookie.getPath() != null) {
                    sb.append("; Path=").append(cookie.getPath());
                }
                if (cookie.getDomain() != null) {
                    sb.append("; Domain=").append(cookie.getDomain());
                }
                if (cookie.getComment() != null) {
                    sb.append("; Comment=").append(cookie.getComment());
                }
                if (cookie.getCommentURL() != null) {
                    sb.append("; CommentURL=\"").append(cookie.getCommentURL()).append("\"");
                }
                sb.append("; Max-Age=").append(cookie.getMaxAge());
                if (cookie.getPortlist() != null) {
                    sb.append("; Port=\"").append(cookie.getPortlist()).append("\"");
                }

                //XXX: Can we safely ignore Secure attr?
                sb.append("; Version=").append(cookie.getVersion());

                out.println(sb);
            }
        }
        out.close();
    }

    /**
     * Updates the last modification time of the cache file if it is more
     * than CACHE_WRITE_DELTA old.  The file's modification time is use
     * as the creation time for any cookies stored in the cache.   We use
     * this time to determine when to expire cookies.   As an optimization
     * we don't update the file if a reponse has the *same* set-cookies as
     * what's in the file and the file is less than CACHE_WRITE_DELTA old.
     */
    public boolean touchStore() {
        if (cookieStoreFile.lastModified() + CACHE_WRITE_DELTA > System.currentTimeMillis()) {
            // The cache is less than CACHE_WRITE_DELTA so we
            // won't update the mod time on the file.
            return false;
        }

        return (cookieStoreFile.setLastModified(System.currentTimeMillis()));
    }
}
