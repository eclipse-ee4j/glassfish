/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation.
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

import com.sun.enterprise.admin.util.AdminLoggerInfo;
import com.sun.enterprise.util.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommandState;
import org.glassfish.api.admin.AdminCommandState.State;

/**
 *
 * @author mmares
 */
public final class AdminCommandStateJsonProprietaryReader implements ProprietaryReader<AdminCommandState> {

    static class LoggerRef {
        private static final Logger logger = AdminLoggerInfo.getLogger();
    }

    @Override
    public boolean isReadable(Class<?> type, String mimetype) {
        return type.isAssignableFrom(AdminCommandState.class);
    }

    public AdminCommandState readFrom(HttpURLConnection urlConnection) throws IOException {
        return readFrom(urlConnection.getInputStream(), urlConnection.getContentType());
    }

    @Override
    public AdminCommandState readFrom(final InputStream is, final String contentType) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            FileUtils.copy(is, baos);
        } finally {
            is.close();
        }
        String str = baos.toString(StandardCharsets.UTF_8);
        try {
            JSONObject json = new JSONObject(str);
            return readAdminCommandState(json);
        } catch (JSONException ex) {
            LoggerRef.logger.log(Level.SEVERE, AdminLoggerInfo.mUnexpectedException, ex);
            throw new IOException(ex);
        }
    }

    public static AdminCommandState readAdminCommandState(JSONObject json) throws JSONException {
        final String strState = json.optString("state");
        final State state = strState == null ? null : State.valueOf(strState);
        final boolean emptyPayload = json.optBoolean("empty-payload", true);
        final CliActionReport report;
        final JSONObject jsonReport = json.optJSONObject("action-report");
        if (jsonReport == null) {
            report = null;
        } else {
            report = new CliActionReport();
            ActionReportJsonProprietaryReader.fillActionReport(report, jsonReport);
        }
        final String id = json.optString("id");
        return new AsadminCommandState(id, state, report, emptyPayload);
    }


    private static final class AsadminCommandState implements AdminCommandState {

        private final String id;
        private final State state;
        private final ActionReport actionReport;
        private final boolean emptyPayload;

        AsadminCommandState(String id, State state, ActionReport actionReport, boolean emptyPayload) {
            this.id = id;
            this.state = state;
            this.actionReport = actionReport;
            this.emptyPayload = emptyPayload;
        }

        @Override
        public State getState() {
            return state;
        }

        @Override
        public ActionReport getActionReport() {
            return actionReport;
        }

        @Override
        public boolean isOutboundPayloadEmpty() {
            return emptyPayload;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getName() {
            return null;
        }
    }
}
