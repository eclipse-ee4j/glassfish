/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.remote.reader;

import com.sun.enterprise.admin.remote.ParamsWithPayload;
import com.sun.enterprise.admin.remote.RestPayloadImpl;
import com.sun.enterprise.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Properties;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.ParameterMap;
import org.jvnet.mimepull.MIMEConfig;
import org.jvnet.mimepull.MIMEMessage;
import org.jvnet.mimepull.MIMEPart;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 *
 * @author martinmares
 */
public final class MultipartProprietaryReader implements ProprietaryReader<ParamsWithPayload> {

    private final ActionReportJsonProprietaryReader actionReportReader;

    public MultipartProprietaryReader() {
        this(new ActionReportJsonProprietaryReader());
    }

    public MultipartProprietaryReader(ActionReportJsonProprietaryReader actionReportReader) {
        this.actionReportReader = actionReportReader;
    }

    @Override
    public boolean isReadable(final Class<?> type, final String mimetype) {
        if (mimetype == null || mimetype.startsWith("*/") || mimetype.startsWith("multipart/")) {
            return ParamsWithPayload.class.isAssignableFrom(type);
        }
        return false;
    }

    public ParamsWithPayload readFrom(final HttpURLConnection urlConnection) throws IOException {
        return readFrom(urlConnection.getInputStream(), urlConnection.getContentType());
    }

    @Override
    public ParamsWithPayload readFrom(final InputStream is, final String contentType) throws IOException {
        Properties mtProps = parseHeaderParams(contentType);
        final String boundary = mtProps.getProperty("boundary");
        if (!StringUtils.ok(boundary)) {
            throw new IOException("ContentType does not define boundary");
        }

        // FIXME: Big refactoring required because of possible resource leaks:
        // MIMEMessage produces MIMEPart objects based on the is parameter.
        // All these objects are Closeable.
        // The final result of this method is the payload, based on mimePart object.
        // That is used later, and at least in one branch none of these objecs can be closed,
        // because the payload's input stream would be closed too.
        final MIMEMessage mimeMessage = new MIMEMessage(is, boundary, new MIMEConfig());
        RestPayloadImpl.Inbound payload = null;
        ActionReport actionReport = null;
        ParameterMap parameters = null;
        // Parse
        for (MIMEPart mimePart : mimeMessage.getAttachments()) {
            String cd = getFirst(mimePart.getHeader("Content-Disposition"));
            cd = StringUtils.ok(cd) ? cd.trim() : "file";
            Properties cdParams = parseHeaderParams(cd);
            // 3 types of content disposition
            if (cd.startsWith("form-data")) {
                // COMMAND PARAMETER
                if (!StringUtils.ok(cdParams.getProperty("name"))) {
                    throw new IOException("Form-data Content-Disposition does not contains name parameter.");
                }
                if (parameters == null) {
                    parameters = new ParameterMap();
                }
                parameters.add(cdParams.getProperty("name"), stream2String(mimePart.readOnce()));
            } else if (mimePart.getContentType() != null && mimePart.getContentType().startsWith("application/json")) {
                // ACTION REPORT
                actionReport = actionReportReader.readFrom(mimePart.readOnce(), "application/json");
            } else {
                // PAYLOAD
                final String name;
                if (cdParams.containsKey("name")) {
                    name = new String(cdParams.getProperty("name").getBytes(ISO_8859_1), UTF_8);
                } else if (cdParams.containsKey("filename")) {
                    name = cdParams.getProperty("filename");
                } else {
                    name = "noname";
                }
                if (payload == null) {
                    payload = new RestPayloadImpl.Inbound();
                }
                final String ct = mimePart.getContentType();
                if (StringUtils.ok(ct) && !ct.trim().startsWith("text/plain")) {
                    payload.add(name, mimePart.read(), ct, mimePart.getAllHeaders());
                } else {
                    payload.add(name, stream2String(mimePart.readOnce()), mimePart.getAllHeaders());
                }
            }
        }
        // Result
        return new ParamsWithPayload(payload, parameters, actionReport);
    }

    /**
     * It is very simple implementation. Use it just for cli client
     */
    private static Properties parseHeaderParams(String contentType) {
        Properties result = new Properties();
        if (contentType == null) {
            return result;
        }
        int ind = contentType.indexOf(';');
        if (ind < 0) {
            return result;
        }
        contentType = contentType.substring(ind + 1);
        boolean parsingKey = true;
        boolean quoted = false;
        String key = "";
        StringBuilder tmp = new StringBuilder();
        for (char ch : contentType.toCharArray()) {
            switch (ch) {
            case '"':
                quoted = !quoted;
                break;
            case '=':
                if (parsingKey && !quoted) {
                    key = tmp.toString();
                    tmp.setLength(0);
                    parsingKey = false;
                } else {
                    tmp.append(ch);
                }
                break;
            case ';':
                if (quoted) {
                    tmp.append(ch);
                } else {
                    if (!parsingKey) {
                        parsingKey = true;
                        result.setProperty(key.trim(), tmp.toString().trim());
                        key = "";
                        tmp.setLength(0);
                    }
                }
                break;
            default:
                tmp.append(ch);
            }
        }
        if (key.length() > 0) {
            result.setProperty(key.trim(), tmp.toString().trim());
        }
        return result;
    }

    private static String getFirst(List<String> lst) {
        if (lst == null || lst.isEmpty()) {
            return null;
        }
        return lst.get(0);
    }

    private static String stream2String(InputStream is) throws IOException {
        if (is == null) {
            return null;
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buff = new byte[256];
            int count;
            while ((count = is.read(buff)) > 0) {
                baos.write(buff, 0, count);
            }
            return baos.toString("UTF-8");
        } finally {
            try {
                is.close();
            } catch (Exception ex) {
            }
        }
    }

}
