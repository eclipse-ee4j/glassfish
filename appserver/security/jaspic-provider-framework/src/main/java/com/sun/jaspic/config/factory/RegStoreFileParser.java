/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jaspic.config.factory;

import com.sun.jaspic.config.helper.JASPICLogManager;

import jakarta.security.auth.message.config.AuthConfigFactory.RegistrationContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Used by GFServerConfigProvider to parse the configuration file. If
 * a file does not exist originally, the default providers are not used.
 * A file is only created if needed, which happens if providers are
 * registered or unregistered through the store() or delete() methods.
 *
 * @author Bobby Bissett
 */
public final class RegStoreFileParser {

    private static final Logger LOG = Logger.getLogger(JASPICLogManager.LOGGER, JASPICLogManager.BUNDLE);

    private static final String SEP = ":";
    private static final String CON_ENTRY = "con-entry";
    private static final String REG_ENTRY = "reg-entry";
    private static final String REG_CTX = "reg-ctx";
    private static final String LAYER = "layer";
    private static final String APP_CTX = "app-ctx";
    private static final String DESCRIPTION = "description";
    private static final String [] INDENT = { "", "  ", "    " };

    private final File confFile;
    private List<EntryInfo> entries;
    private final List<EntryInfo> defaultEntries;

    /*
     * Loads the configuration file from the given filename.
     * If a file is not found, then the default entries
     * are used. Otherwise the file is parsed to load the entries.
     *
     */
    public RegStoreFileParser(String pathParent, String pathChild,List<EntryInfo> defaultEntries) {
        confFile = new File(pathParent, pathChild);
        this.defaultEntries = defaultEntries == null ? new ArrayList<>() : defaultEntries;
        try {
            loadEntries();
        } catch (IOException | IllegalArgumentException e) {
            LOG.log(Level.WARNING, JASPICLogManager.MSG_COULD_NOT_READ_AUTH_CFG, e);
        }
    }

    /**
     * Returns the in-memory list of entries.
     * MUST Hold exclusive lock on calling factory while processing entries
     */
    List<EntryInfo> getPersistedEntries() {
        return entries;
    }

    /**
     * Adds the provider to the entry list if it is not already
     * present, creates the configuration file if necessary, and
     * writes the entries to the file.
     */
    void store(String className, RegistrationContext ctx, Map properties) {
        synchronized (confFile) {
            if (checkAndAddToList(className, ctx, properties)) {
                try {
                    writeEntries();
                } catch (IOException e) {
                    LOG.log(Level.WARNING, JASPICLogManager.MSG_CANNOT_PERSIST_PROVIDERS, e);
                }
            }
        }
    }

    /**
     * Removes the provider from the entry list if it is already
     * present, creates the configuration file if necessary, and
     * writes the entries to the file.
     */
    void delete(RegistrationContext ctx) {
        synchronized (confFile) {
            if (checkAndRemoveFromList(ctx)) {
                try {
                    writeEntries();
                } catch (IOException e) {
                    LOG.log(Level.WARNING, JASPICLogManager.MSG_CANNOT_PERSIST_PROVIDERS, e);
                }
            }
        }
    }

    /**
     * If this entry does not exist, this method stores it in
     * the entries list and returns true to indicate that the
     * configuration file should be written.
     */
    private boolean checkAndAddToList(String className,
        RegistrationContext ctx, Map props) {

        // convention is to use null for empty properties
        if (props != null && props.isEmpty()) {
            props = null;
        }
        EntryInfo newEntry = new EntryInfo(className, props, ctx);
        EntryInfo entry = getMatchingRegEntry(newEntry);

        // there is no matching entry, so add to list
        if (entry == null) {
            entries.add(newEntry);
            return true;
        }

        // otherwise, check reg contexts to see if there is a match
        if (entry.getRegContexts().contains(ctx)) {
            return false;
        }

        // no matching context in existing entry, so add to existing entry
        entry.getRegContexts().add(new RegistrationContextImpl(ctx));
        return true;
    }

    /**
     * If this registration context does not exist, this method
     * returns false. Otherwise it removes the entry and returns
     * true to indicate that the configuration file should be written.
     *
     * This only makes sense for registry entries.
     */
    private boolean checkAndRemoveFromList(RegistrationContext target) {
        boolean retValue = false;
        try {
            ListIterator<EntryInfo> lit = entries.listIterator();
            while (lit.hasNext()) {

                EntryInfo info = lit.next();
                if (info.isConstructorEntry()) {
                    continue;
                }

                Iterator<RegistrationContext> iter =
                        info.getRegContexts().iterator();
                while (iter.hasNext()) {
                    RegistrationContext ctx = iter.next();
                    if (ctx.equals(target)) {
                        iter.remove();
                        if (info.getRegContexts().isEmpty()) {
                            lit.remove();
                        }
                        retValue = true;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return retValue;
    }

    /**
     * Used to find a matching registration entry in the 'entries'
     * list without including registration contexts. If there is not
     * a matching entry, return null.
     */
    private EntryInfo getMatchingRegEntry(EntryInfo target) {
        for (EntryInfo info : entries) {
            if (!info.isConstructorEntry() && info.matchConstructors(target)) {
                return info;
            }
        }
        return null;
    }

    /**
     * This method overwrites the existing file with the
     * current entries.
     */
    private void writeEntries() throws IOException {
        if (confFile.exists() && !confFile.canWrite()) {
            LOG.log(Level.WARNING, JASPICLogManager.MSG_CANNOT_WRITE_PROVIDERS_TO_FILE, confFile);
        }
        clearExistingFile();
        PrintWriter out = new PrintWriter(confFile);
        int indent = 0;
        for (EntryInfo info : entries) {
            if (info.isConstructorEntry()) {
                writeConEntry(info, out, indent);
            } else {
                writeRegEntry(info, out, indent);
            }
        }
        out.close();
    }

    /**
     * Writes constructor entry output of the form:
     * <pre>
     * con-entry {
     *   className
     *   key:value
     *   key:value
     * }
     * </pre>
     * The first appearance of a colon ":" separates
     * the key and value of the property (so a value may
     * contain a colon as part of the string). For instance:
     * "mydir:c:foo" would have key "mydir" and value "c:foo".
     */
    private void writeConEntry(EntryInfo info, PrintWriter out, int i) {
        out.println(INDENT[i++] + CON_ENTRY + " {");
        out.println(INDENT[i] + info.getClassName());
        Map<String, String> props = info.getProperties();
        if (props != null) {
            for (Map.Entry<String,String> val : props.entrySet()) {
                out.println(INDENT[i] + val.getKey() + SEP + val.getValue());
            }
        }
        out.println(INDENT[--i] + "}");
    }

    /**
     * Write registration entry output of the form:
     * <pre>
     * reg-entry {
     *   con-entry { see writeConEntry() for detail }
     *   reg-ctx {
     *     layer:HttpServlet
     *     app-ctx:security-jmac-https
     *     description:My provider
     *   }
     * }
     * </pre>
     */
    private void writeRegEntry(EntryInfo info, PrintWriter out, int i) {
        out.println(INDENT[i++] + REG_ENTRY + " {");
        if (info.getClassName() != null) {
            writeConEntry(info, out, i);
        }
        for (RegistrationContext ctx : info.getRegContexts()) {
            out.println(INDENT[i++] + REG_CTX + " {");
            if (ctx.getMessageLayer() != null) {
                out.println(INDENT[i] + LAYER + SEP + ctx.getMessageLayer());
            }
            if (ctx.getAppContext() != null) {
                out.println(INDENT[i] + APP_CTX + SEP + ctx.getAppContext());
            }
            if (ctx.getDescription() != null) {
                out.println(INDENT[i] + DESCRIPTION +
                    SEP + ctx.getDescription());
            }
            out.println(INDENT[--i] + "}");
        }
        out.println(INDENT[--i] + "}");
    }

    private void clearExistingFile() throws IOException {
        boolean newCreation = !confFile.exists();
        if (!newCreation) {
            if(!confFile.delete()) {
                throw new IOException();
            }
        }
        if (newCreation) {
            LOG.log(Level.INFO, JASPICLogManager.MSG_CREATING_JMAC_FILE, confFile);
        }
        if (!confFile.createNewFile()) {
            throw new IOException();
        }
    }

    /**
     * Called from the constructor. This is the only time
     * the file is read, though it is written when new
     * entries are stored or deleted.
     */
    private void loadEntries() throws IOException {
        synchronized (confFile) {
            entries = new ArrayList<>();
            if (confFile.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(confFile))) {
                    String line = reader.readLine();
                    while (line != null) {
                        String trimLine = line.trim(); // can't trim readLine() result
                        if (trimLine.startsWith(CON_ENTRY)) {
                            entries.add(readConEntry(reader));
                        } else if (trimLine.startsWith(REG_ENTRY)) {
                            entries.add(readRegEntry(reader));
                        }
                        line = reader.readLine();
                    }
                }
            } else {
                LOG.log(Level.FINER, JASPICLogManager.MSG_FILE_NOT_EXIST, confFile);
                for (EntryInfo e : defaultEntries) {
                    entries.add(new EntryInfo(e));
                }
            }
        }
    }

    private EntryInfo readConEntry(BufferedReader reader) throws IOException {
        // entry must contain class name as next line
        String className = reader.readLine();
        if(className != null) {
            className = className.trim();
        }
        Map<String, String> properties = readProperties(reader);
        return new EntryInfo(className, properties);
    }

    /**
     * Properties must be of the form "key:value." While the key
     * String cannot contain a ":" character, the value can. The
     * line will be broken into key and value based on the first
     * appearance of the ":" character.
     */
    private Map<String, String> readProperties(BufferedReader reader)
        throws IOException {

        String line = reader.readLine();
        if(line != null) {
            line = line.trim();
        }

        if ("}".equals(line)) {
            return null;
        }
        Map<String, String> properties = new HashMap<>();
        while (!"}".equals(line)) {
            properties.put(line.substring(0, line.indexOf(SEP)),
                line.substring(line.indexOf(SEP) + 1, line.length()));
            line = reader.readLine();
            if (line != null) {
                line = line.trim();
            }
        }
        return properties;
    }

    private EntryInfo readRegEntry(BufferedReader reader) throws IOException {
        String className = null;
        Map<String, String> properties = null;
        List<RegistrationContext> ctxs =
            new ArrayList<>();
        String line = reader.readLine();
        if(line != null) {
            line = line.trim();
        }
        while (!"}".equals(line)) {
            if (line.startsWith(CON_ENTRY)) {
                EntryInfo conEntry = readConEntry(reader);
                className = conEntry.getClassName();
                properties = conEntry.getProperties();
            } else if (line.startsWith(REG_CTX)) {
                ctxs.add(readRegContext(reader));
            }
            line = reader.readLine();
            if(line != null) {
                line = line.trim();
            }

        }
        return new EntryInfo(className, properties, ctxs);
    }

    private RegistrationContext readRegContext(BufferedReader reader)
        throws IOException {

        String layer = null;
        String appCtx = null;
        String description = null;
        String line = reader.readLine();
        if(line != null) {
            line = line.trim();
        }
        while (!"}".equals(line)) {
            String value = line.substring(line.indexOf(SEP) + 1,
                line.length());
            if (line.startsWith(LAYER)) {
                layer = value;
            } else if (line.startsWith(APP_CTX)) {
                appCtx = value;
            } else if (line.startsWith(DESCRIPTION)) {
                description = value;
            }
           line = reader.readLine();
            if(line != null) {
                line = line.trim();
            }
        }
        return new RegistrationContextImpl(layer, appCtx, description, true);
    }

}
