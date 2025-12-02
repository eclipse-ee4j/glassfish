/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.admin.remote;

import com.sun.enterprise.admin.remote.reader.ProprietaryReaderFactory;
import com.sun.enterprise.admin.util.AdminLoggerInfo;
import com.sun.enterprise.admin.util.CachedCommandModel;
import com.sun.enterprise.admin.util.CommandModelData.ParamModelData;
import com.sun.enterprise.admin.util.cache.AdminCacheUtils;

import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Properties;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.CommandModel;
import org.glassfish.api.admin.InvalidCommandException;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.TRACE;
import static java.lang.System.Logger.Level.WARNING;


/**
 *
 */
public class CommandModelHttpCommand implements HttpCommand<CachedCommandModel> {

    private static final String MEDIATYPE_JSON = "application/json";
    private static final Logger LOG = System.getLogger(CommandModelHttpCommand.class.getName());

    private final String commandName;
    private final String commandCacheKey;
    private final boolean detached;
    private final boolean notify;

    /**
     * @param commandName
     * @param commandCacheKey
     */
    public CommandModelHttpCommand(String commandName, String commandCacheKey, boolean detached, boolean notify) {
        this.commandName = commandName;
        this.commandCacheKey = commandCacheKey;
        this.detached = detached;
        this.notify = notify;
    }


    @Override
    public void prepareConnection(HttpURLConnection urlConnection) throws IOException {
        urlConnection.setRequestProperty("Accept", MEDIATYPE_JSON);
    }


    @Override
    public CachedCommandModel useConnection(HttpURLConnection urlConnection) throws CommandException, IOException {
        long startNanos = System.nanoTime();
        String eTag = urlConnection.getHeaderField("ETag");
        if (eTag != null) {
            eTag = eTag.trim();
            if (eTag.startsWith("W/")) {
                eTag = eTag.substring(2).trim();
            }
            if (eTag.startsWith("\"")) {
                eTag = eTag.substring(1);
            }
            if (eTag.endsWith("\"")) {
                eTag = eTag.substring(0, eTag.length() - 1);
            }
        }
        String json = ProprietaryReaderFactory.<String>getReader(String.class, urlConnection.getContentType())
                .readFrom(urlConnection.getInputStream(), urlConnection.getContentType());
        CachedCommandModel commandModel = parseMetadata(json, eTag, detached, notify);
        if (commandModel == null) {
            throw new InvalidCommandException(
                "Command model could not be parsed from JSON metadata received from server.");
        }
        LOG.log(DEBUG, "Command model for {0} command fetched from remote server. Duration: {1} nanos",
                    commandName, System.nanoTime() - startNanos);
        try {
            StringBuilder forCache = new StringBuilder(json.length() + 40);
            forCache.append("ETag: ").append(eTag);
            forCache.append("\n");
            forCache.append(json);
            AdminCacheUtils.getCache().put(commandCacheKey, forCache.toString());
        } catch (Exception ex) {
            LOG.log(WARNING, AdminLoggerInfo.mCantPutToCache, commandCacheKey);
        }
        return commandModel;
    }

    static CommandModel fromCache(String key, boolean detached, boolean notify) {
        final String cachedModel = getCachedModel(key);
        final int ind = cachedModel == null ? -1 : cachedModel.indexOf('\n');
        if (ind < 0) {
            return null;
        }
        final String eTag = toEtag(cachedModel, ind);
        LOG.log(DEBUG, () -> "Cached command model ETag is " + eTag);
        final String content = cachedModel.substring(ind + 1).trim();
        return parseMetadata(content, eTag, detached, notify);
    }

    private static String getCachedModel(String key) {
        String cachedModel = AdminCacheUtils.getCache().get(key, String.class);
        if (cachedModel == null) {
            return null;
        }
        return cachedModel.trim();
    }

    private static String toEtag(String cachedModel, int ind) {
        String eTag = cachedModel.substring(0, ind);
        if (!eTag.startsWith("ETag:")) {
            return null;
        }
        return eTag.substring(5).trim();
    }


    /**
     * Parse the JSon metadata for the command.
     *
     * @param str the string
     * @return the etag to compare the command cache model
     */
    private static CachedCommandModel parseMetadata(String str, String etag, boolean detached, boolean notify) {
        if (LOG.isLoggable(TRACE)) {
            LOG.log(TRACE, "------- RAW METADATA RESPONSE ---------");
            LOG.log(TRACE, "ETag: {0}", etag);
            LOG.log(TRACE, str);
            LOG.log(TRACE, "------- RAW METADATA RESPONSE ---------");
        }
        if (str == null) {
            return null;
        }
        try {
            boolean sawFile = false;
            JSONObject obj = new JSONObject(str);
            obj = obj.getJSONObject("command");
            CachedCommandModel cm = new CachedCommandModel(obj.getString("@name"), etag);
            cm.dashOk = obj.optBoolean("@unknown-options-are-operands", false);
            cm.managedJob = obj.optBoolean("@managed-job", false);
            cm.setUsage(obj.optString("usage", null));
            Object optns = obj.opt("option");
            if (!JSONObject.NULL.equals(optns)) {
                JSONArray jsonOptions;
                if (optns instanceof JSONArray) {
                    jsonOptions = (JSONArray) optns;
                } else {
                    jsonOptions = new JSONArray();
                    jsonOptions.put(optns);
                }
                for (int i = 0; i < jsonOptions.length(); i++) {
                    JSONObject jsOpt = jsonOptions.getJSONObject(i);
                    String type = jsOpt.getString("@type");
                    ParamModelData opt = new ParamModelData(jsOpt.getString("@name"), typeOf(type), jsOpt.optBoolean("@optional", false),
                            jsOpt.optString("@default"), jsOpt.optString("@short"), jsOpt.optBoolean("@obsolete", false),
                            jsOpt.optString("@alias"));
                    opt.param._acceptableValues = jsOpt.optString("@acceptable-values");
                    if ("PASSWORD".equals(type)) {
                        opt.param._password = true;
                        opt.prompt = jsOpt.optString("@prompt");
                        opt.promptAgain = jsOpt.optString("@prompt-again");
                    } else if ("FILE".equals(type)) {
                        sawFile = true;
                    }
                    if (jsOpt.optBoolean("@primary", false)) {
                        opt.param._primary = true;
                    }
                    if (jsOpt.optBoolean("@multiple", false)) {
                        if (opt.type == File.class) {
                            opt.type = File[].class;
                        } else {
                            opt.type = List.class;
                        }
                        opt.param._multiple = true;
                    }
                    cm.add(opt);
                }
            }
            if (sawFile) {
                cm.add(new ParamModelData("upload", Boolean.class, true, null));
                cm.setAddedUploadOption(true);
            }
            if (notify) {
                cm.add(new ParamModelData("notify", Boolean.class, false, "false"));
            }
            if (detached) {
                cm.add(new ParamModelData("detach", Boolean.class, false, "false"));
            }
            return cm;
        } catch (JSONException e) {
            LOG.log(DEBUG, "Can not parse command metadata", e);
            return null;
        }
    }

    private static Class<?> typeOf(String type) {
        if (type.equals("STRING")) {
            return String.class;
        } else if (type.equals("BOOLEAN")) {
            return Boolean.class;
        } else if (type.equals("FILE")) {
            return File.class;
        } else if (type.equals("PASSWORD")) {
            return String.class;
        } else if (type.equals("PROPERTIES")) {
            return Properties.class;
        } else {
            return String.class;
        }
    }
}
