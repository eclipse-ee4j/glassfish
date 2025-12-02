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

import com.sun.enterprise.admin.remote.RestPayloadImpl.Outbound;
import com.sun.enterprise.admin.remote.reader.CliActionReport;
import com.sun.enterprise.admin.remote.reader.ProprietaryReader;
import com.sun.enterprise.admin.remote.reader.ProprietaryReaderFactory;
import com.sun.enterprise.admin.remote.sse.GfSseEventReceiver;
import com.sun.enterprise.admin.remote.sse.GfSseEventReceiverProprietaryReader;
import com.sun.enterprise.admin.remote.sse.GfSseInboundEvent;
import com.sun.enterprise.admin.remote.writer.ProprietaryWriter;
import com.sun.enterprise.admin.remote.writer.ProprietaryWriterFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.net.HttpURLConnection;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.glassfish.admin.payload.PayloadFilesManager;
import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.admin.AdminCommandState;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.Payload;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.TRACE;


/**
 *
 */
public class ExecHttpCommand implements HttpCommand<ActionReport> {

    private static final String MEDIATYPE_JSON = "application/json";
    private static final String MEDIATYPE_MULTIPART = "multipart/*";
    private static final String MEDIATYPE_SSE = "text/event-stream";

    private static final Logger LOG = System.getLogger(ExecHttpCommand.class.getName());

    private final ParameterMap params;
    private final boolean detached;
    private final boolean useSSE;
    private final AtomicBoolean closeSSE;
    private final boolean doUpload;
    private final Outbound payload;
    private final File fileOutputDir;
    private final Consumer<GfSseInboundEvent> eventConsumer;
    private final Consumer<String> jobPayloadDownloader;

    public ExecHttpCommand(final ParameterMap params, boolean detached, boolean useSSE, AtomicBoolean closeSSE, boolean doUpload,
        RestPayloadImpl.Outbound payload, File fileOutputDir, Consumer<GfSseInboundEvent> eventConsumer,
        Consumer<String> jobPayloadDownloader) {
        this.params = params;
        this.detached = detached;
        this.useSSE = useSSE;
        this.closeSSE = closeSSE;
        this.doUpload = doUpload;
        this.payload = payload;
        this.fileOutputDir = fileOutputDir;
        this.eventConsumer = eventConsumer;
        this.jobPayloadDownloader = jobPayloadDownloader;
    }

    @Override
    public void prepareConnection(HttpURLConnection urlConnection) throws IOException {
        if (useSSE) {
            urlConnection.addRequestProperty("Accept", MEDIATYPE_SSE);
        } else {
            urlConnection.addRequestProperty("Accept", MEDIATYPE_JSON + "; q=0.8, " + MEDIATYPE_MULTIPART + "; q=0.9");
        }
        // add any user-specified headers
//        for (Header h : requestHeaders) {
//            urlConnection.addRequestProperty(h.getName(), h.getValue());
//        }
        //Write data
        ParamsWithPayload pwp;
        if (doUpload) {
            urlConnection.setChunkedStreamingMode(0);
            pwp = new ParamsWithPayload(payload, params);
        } else {
            pwp = new ParamsWithPayload(null, params);
        }
        ProprietaryWriter writer = ProprietaryWriterFactory.getWriter(pwp);
        LOG.log(DEBUG, () -> "Writer to use " + writer.getClass().getName());
        writer.writeTo(pwp, urlConnection);
    }


    @Override
    public ActionReport useConnection(HttpURLConnection urlConnection) throws CommandException, IOException {
        final String resultMediaType = urlConnection.getContentType();
        LOG.log(DEBUG, "Result type is {0}", resultMediaType);
        LOG.log(DEBUG, "URL connection is {0}", urlConnection.getClass().getName());
        if (resultMediaType != null && resultMediaType.startsWith(MEDIATYPE_SSE)) {
            return processSSE(urlConnection, resultMediaType);
        }

        final ProprietaryReader<ParamsWithPayload> reader = ProprietaryReaderFactory.getReader(ParamsWithPayload.class, resultMediaType);
        if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR) {
            return reportInternalError(urlConnection, resultMediaType, reader);
        }

        final ParamsWithPayload pwp = reader.readFrom(urlConnection.getInputStream(), resultMediaType);
        if (pwp.getPayloadInbound() == null) {
            return pwp.getActionReport();
        }

        if (resultMediaType != null && resultMediaType.startsWith("multipart/")) {
            RestPayloadImpl.Inbound inbound = pwp.getPayloadInbound();
            ActionReport report = pwp.getActionReport();
            if (LOG.isLoggable(TRACE)) {
                LOG.log(TRACE, "------ PAYLOAD ------");
                Iterator<Payload.Part> parts = inbound.parts();
                while (parts.hasNext()) {
                    Payload.Part part = parts.next();
                    LOG.log(TRACE, " - {0} [{1}]", part.getName(), part.getContentType());
                }
                LOG.log(TRACE, "---- END PAYLOAD ----");
            }
            PayloadFilesManager downloadedFilesMgr = new PayloadFilesManager.Perm(fileOutputDir, null, null);
            try {
                downloadedFilesMgr.processParts(inbound);
            } catch (CommandException cex) {
                throw cex;
            } catch (Exception ex) {
                throw new CommandException(ex.getMessage(), ex);
            }
            return report;
        }
        throw new IllegalStateException("Unknown media type: " + resultMediaType);
    }

    private ActionReport processSSE(HttpURLConnection urlConnection, String resultMediaType) throws CommandException {
        String instanceId = null;
        boolean retryableCommand = false;
        try (GfSseEventReceiver eventReceiver = openEventReceiver(urlConnection, resultMediaType)) {
            LOG.log(DEBUG, "Response is SSE - about to read events");
            GfSseInboundEvent event;
            do {
                event = eventReceiver.readEvent();
                if (event != null) {
                    LOG.log(DEBUG, "Event name: {0}", event.getName());
                    eventConsumer.accept(event);
                    if (AdminCommandState.EVENT_STATE_CHANGED.equals(event.getName())) {
                        final AdminCommandState state = event.getData(AdminCommandState.class, MEDIATYPE_JSON);
                        if (state.getId() == null) {
                            if (detached && !closeSSE.get()) {
                                // If detached command, ignore everything else, we want just the ID.
                                LOG.log(TRACE, "Command instance job ID missing, waiting for another message.");
                                continue;
                            }
                        } else {
                            instanceId = state.getId();
                            LOG.log(DEBUG, "Command instance job ID: {0}", instanceId);
                        }
                        if (closeSSE.get()) {
                            LOG.log(TRACE, "DetachListener already did its job.");
                            // See DetachListener, the eventConsumer leads there
                            return null;
                        }
                        if (state.getState() == AdminCommandState.State.COMPLETED
                            || state.getState() == AdminCommandState.State.RECORDED
                            || state.getState() == AdminCommandState.State.REVERTED) {
                            if (!state.isOutboundPayloadEmpty()) {
                                LOG.log(DEBUG, "Remote command holds data. Must load it");
                                jobPayloadDownloader.accept(instanceId);
                            }
                            closeSSE.set(true);
                            return state.getActionReport();
                        } else if (state.getState() == AdminCommandState.State.FAILED_RETRYABLE) {
                            LOG.log(INFO, "Command is parked. Continue in it using command \"continue " + state.getId()
                                + "\" or revert using \"revert " + state.getId() + "\".");
                            closeSSE.set(true);
                            return state.getActionReport();
                        } else if (state.getState() == AdminCommandState.State.RUNNING_RETRYABLE) {
                            LOG.log(DEBUG, "Command stores checkpoint and is retryable");
                            retryableCommand = true;
                        }
                    }
                }
            } while (event != null && !eventReceiver.isClosed() && !closeSSE.get());
            return null;
        } catch (IOException e) {
            if (instanceId == null || !"Premature EOF".equals(e.getMessage())) {
                throw new CommandException(e.getMessage(), e);
            }
            if (retryableCommand) {
                throw new CommandException("Connection to the server lost."
                    + " \nIf the server is not running, the command will be resumed automatically"
                    + " at next server startup. Result can be seen in server.log or by reconnecting"
                    + " using subcommand attach " + instanceId, e);
            }
            throw new CommandException(
                "Connection to the server lost."
                    + "\nIf server is still running, it is possible to reconnect using subcommand attach " + instanceId,
                e);
        } catch (Exception e) {
            throw new CommandException(e.getMessage(), e);
        }
    }

    private GfSseEventReceiver openEventReceiver(HttpURLConnection urlConnection, String resultMediaType) {
        final ProprietaryReader<GfSseEventReceiver> reader = new GfSseEventReceiverProprietaryReader();
        try {
            InputStream inputStream = urlConnection.getInputStream();
            return reader.readFrom(inputStream, resultMediaType);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to open the even receiver.", e);
        }
    }


    private ActionReport reportInternalError(HttpURLConnection urlConnection, String resultMediaType,
        ProprietaryReader<ParamsWithPayload> reader) throws IOException {
        ActionReport report;
        if (reader != null) {
            return reader.readFrom(urlConnection.getErrorStream(), resultMediaType).getActionReport();
        }
        report = new CliActionReport();
        report.setActionExitCode(ExitCode.FAILURE);
        report.setMessage(urlConnection.getResponseMessage());
        return report;
    }
}
