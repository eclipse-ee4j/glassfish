/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation.
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.cli.embeddable;

import com.sun.enterprise.util.io.FileUtils;

import jakarta.inject.Inject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.admin.payload.PayloadFilesManager;
import org.glassfish.admin.payload.PayloadImpl;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.CommandInvocation;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.Payload;
import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.InternalSystemAdministrator;
import org.jvnet.hk2.annotations.ContractsProvided;
import org.jvnet.hk2.annotations.Service;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * This is an implementation of {@link Deployer}. Unlike the other EmbeddedDeployer, this deployer uses admin command
 * execution framework to execute the underlying command, as a result we don't by-pass things like command replication
 * code.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */

@Service()
@PerLookup
@ContractsProvided({ DeployerImpl.class, Deployer.class }) // bcos Deployer interface can't depend on HK2, we need ContractProvided here.
public class DeployerImpl implements Deployer {

    private static final Logger logger = Logger.getLogger(DeployerImpl.class.getPackage().getName());

    /*
     * This class currently copies generic URIs to a file before processing. Once deployment backend
     * supports URI, we should be able to use URIs directly.
     */

    @Inject
    private ServiceLocator habitat;

    @Inject
    private InternalSystemAdministrator kernelIdentity;

    @Override
    public String deploy(URI archive, String... params) throws GlassFishException {
        File file;
        try {
            file = convertToFile(archive);
        } catch (IOException e) {
            throw new GlassFishException("Unable to make a file out of " + archive, e);
        }
        return deploy(file, params);
    }

    @Override
    public String deploy(File file, String... params) throws GlassFishException {
        String[] newParams = new String[params.length + 1];
        System.arraycopy(params, 0, newParams, 0, params.length);
        newParams[params.length] = file.getAbsolutePath();
        CommandExecutorImpl executer = habitat.getService(CommandExecutorImpl.class);
        try {
            String command = "deploy";
            ActionReport actionReport = executer.createActionReport();
            ParameterMap commandParams = executer.getParameters(command, newParams);
            CommandInvocation<?> inv = executer.getCommandRunner().getCommandInvocation(command, actionReport,
                kernelIdentity.getSubject());
            inv.parameters(commandParams);
            // set outputbound payload if --retrieve option is specified.
            Payload.Outbound outboundPayload = null;
            String retrieveOpt = commandParams.getOne("retrieve");
            File retrieve = retrieveOpt != null ? new File(retrieveOpt) : null;
            if (retrieve != null && retrieve.exists()) {
                outboundPayload = PayloadImpl.Outbound.newInstance();
                inv.outbound(outboundPayload);
            }
            inv.execute();
            // extract the outbound payload.
            if (outboundPayload != null) {
                extractPayload(outboundPayload, actionReport, retrieve);
            }

            return actionReport.getResultType(String.class);
        } catch (CommandException e) {
            throw new GlassFishException(e);
        }
    }

    @Override
    public String deploy(InputStream is, String... params) throws GlassFishException {
        try {
            return deploy(createFile(is), params);
        } catch (IOException e) {
            throw new GlassFishException(e);
        } finally {
            try {
                // Declared in the javadoc
                is.close();
            } catch (IOException e) {
                throw new IllegalStateException("Could not close the input stream!", e);
            }
        }
    }

    @Override
    public void undeploy(String appName, String... params) throws GlassFishException {
        String[] newParams = new String[params.length + 1];
        System.arraycopy(params, 0, newParams, 0, params.length);
        newParams[params.length] = appName;
        CommandExecutorImpl executer = habitat.getService(CommandExecutorImpl.class);
        try {
            ActionReport actionReport = executer.executeCommand("undeploy", newParams);
            if (UTF_8.equals(Charset.defaultCharset())) {
                actionReport.writeReport(System.out);
                return;
            }
            // We have to reencode.
            ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
            actionReport.writeReport(baos);
            System.out.print(baos.toString(UTF_8));
        } catch (CommandException e) {
            throw new GlassFishException(e);
        } catch (IOException e) {
            throw new GlassFishException(e);
        }
    }

    @Override
    public Collection<String> getDeployedApplications() throws GlassFishException {
        try {
            CommandExecutorImpl executer = habitat.getService(CommandExecutorImpl.class);
            ActionReport report = executer.executeCommand("list-components");
            Properties props = report.getTopMessagePart().getProps();
            return new ArrayList<>(props.stringPropertyNames());
        } catch (Exception e) {
            throw new GlassFishException(e);
        }
    }

    private File convertToFile(URI archive) throws IOException {
        if ("file".equalsIgnoreCase(archive.getScheme())) {
            return new File(archive);
        }
        try (InputStream openStream = archive.toURL().openStream()) {
            return createFile(openStream);
        }
    }

    private File createFile(InputStream in) throws IOException {
        File file = File.createTempFile("app", "tmp");
        file.deleteOnExit();
        FileUtils.copy(in, file);
        return file;
    }

    /**
     * Extract the payload (client side stub jar files) to the directory specified via --retrieve option.
     *
     * @param outboundPayload Payload to be extracted
     * @param actionReport ActionReport of the deploy command.
     * @param retrieveDir Directory where the payload should be extracted to.
     */
    private void extractPayload(Payload.Outbound outboundPayload, ActionReport actionReport, File retrieveDir) {
        File payloadZip = null;
        try {
            // Add the report to the payload to mimic what the normal non-embedded server does.
            final ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
            actionReport.writeReport(baos);
            final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            final Properties reportProps = new Properties();
            reportProps.setProperty("data-request-type", "report");
            outboundPayload.addPart(0, actionReport.getContentType(), "report", reportProps, bais);

            // Now process the payload as an *inbound* payload as the non-embedded
            // admin client does, by writing the *outbound* payload to a temporary file
            // then reading from that file.
            payloadZip = File.createTempFile("appclient", ".zip");
            try (FileOutputStream payloadOutputStream = new FileOutputStream(payloadZip)) {
                outboundPayload.writeTo(payloadOutputStream);
            }

            // Use the temp file's contents as the inbound payload to
            // correctly process the downloaded files.
            final PayloadFilesManager pfm = new PayloadFilesManager.Perm(retrieveDir, null);
            try (FileInputStream payloadInputStream = new FileInputStream(payloadZip)) {
                PayloadImpl.Inbound inboundPayload = PayloadImpl.Inbound.newInstance("application/zip", payloadInputStream);
                pfm.processParts(inboundPayload); // explodes the payloadZip.
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, ex.getMessage(), ex);
        } finally {
            if (payloadZip != null) {
                if (!payloadZip.delete()) {
                    logger.log(Level.WARNING, "Cannot delete payload: {0}", payloadZip);
                }
            }
        }
    }

}
