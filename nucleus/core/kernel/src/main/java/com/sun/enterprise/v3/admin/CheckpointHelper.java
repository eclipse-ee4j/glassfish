/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.v3.admin;

import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.io.FileUtils;

import jakarta.inject.Inject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import org.glassfish.admin.payload.PayloadImpl;
import org.glassfish.api.admin.Job;
import org.glassfish.api.admin.JobManager;
import org.glassfish.api.admin.Payload;
import org.glassfish.api.admin.Payload.Inbound;
import org.glassfish.api.admin.Payload.Outbound;
import org.glassfish.api.admin.Payload.Part;
import org.glassfish.common.util.ObjectInputOutputStreamFactory;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.security.services.api.authentication.AuthenticationService;
import org.jvnet.hk2.annotations.Service;

/**
 * This class is starting point for persistent CheckpointHelper, and currently only
 * persists and restores AdminCommandContext with payloads in separate files.
 *
 * @author Andriy Zhdanov
 *
 */
@Service
public class CheckpointHelper {

    private static final String CONTENT_TYPE_NAME = "Content-Type";

    public static class CheckpointFilename {

        enum ExtensionType {
            BASIC, ATTACHMENT, PAYLOAD_INBOUD, PAYLOAD_OUTBOUND;
        }

        private static final Map<ExtensionType, String> EXTENSIONS;
        static {
            Map<ExtensionType, String> extMap = new EnumMap<>(ExtensionType.class);
            extMap.put(ExtensionType.BASIC, ".checkpoint");
            extMap.put(ExtensionType.ATTACHMENT, ".checkpoint_attach");
            extMap.put(ExtensionType.PAYLOAD_INBOUD, ".checkpoint_inb");
            extMap.put(ExtensionType.PAYLOAD_OUTBOUND, ".checkpoint_outb");
            EXTENSIONS = Collections.unmodifiableMap(extMap);
        }

        private final ExtensionType ext;
        private final String jobId;
        private String attachmentId;
        private final File parentDir;

        private String cachedFileName;

        private CheckpointFilename(String jobId, File parentDir, ExtensionType ext) {
            this.ext = ext;
            this.jobId = jobId;
            this.parentDir = parentDir;
        }

        private CheckpointFilename(CheckpointFilename basic, ExtensionType ext) {
            this.ext = ext;
            this.jobId = basic.jobId;
            this.attachmentId = basic.attachmentId;
            this.parentDir = basic.parentDir;
        }

        private CheckpointFilename(Job job, String attachmentId) {
            this(job.getId(), job.getJobsFile().getParentFile(), ExtensionType.ATTACHMENT);
            this.attachmentId = attachmentId;
        }

        public CheckpointFilename(File file) throws IOException {
            this.parentDir = file.getParentFile();
            String name = file.getName();
            this.cachedFileName = name;
            //Find extension
            int ind = name.lastIndexOf('.');
            if (ind <= 0) {
                throw new IOException(strings.getLocalString("checkpointhelper.wrongfileextension", "Wrong checkpoint file extension {0}", file.getName()));
            }
            String extensionStr = name.substring(ind);
            ExtensionType extension = null;
            for (Map.Entry<ExtensionType, String> entry : EXTENSIONS.entrySet()) {
                if (extensionStr.equals(entry.getValue())) {
                    extension = entry.getKey();
                    break;
                }
            }
            if (extension == null) {
                throw new IOException(strings.getLocalString("checkpointhelper.wrongfileextension", "Wrong checkpoint file extension {0}", file.getName()));
            }
            this.ext = extension;
            //Parse id
            name = name.substring(0, ind);
            if (this.ext == ExtensionType.ATTACHMENT) {
                ind = name.indexOf('-');
                if (ind < 0) {
                    throw new IOException(strings.getLocalString("checkpointhelepr.wrongfilename", "Wrong checkpoint filename format: {0}.", file.getName()));
                }
                this.jobId = name.substring(0, ind);
                this.attachmentId = name.substring(ind + 1);
            } else {
                this.jobId = name;
            }
        }

        public ExtensionType getExt() {
            return ext;
        }

        public String getJobId() {
            return jobId;
        }

        public String getAttachmentId() {
            return attachmentId;
        }

        public File getParentDir() {
            return parentDir;
        }

        @Override
        public String toString() {
            if (cachedFileName == null) {
                StringBuilder result = new StringBuilder();
                result.append(jobId);
                if (ext == ExtensionType.ATTACHMENT) {
                    result.append("-");
                    if (attachmentId == null) {
                        result.append("null");
                    } else if (!attachmentId.isEmpty()) {
                        try {
                            MessageDigest md = MessageDigest.getInstance("MD5");
                            byte[] thedigest = md.digest(attachmentId.getBytes("UTF-8"));
                            for (byte element : thedigest) {
                                result.append(Integer.toString((element & 0xff) + 0x100, 16).substring(1));
                            }
                        } catch (Exception ex) {
                            result.append(attachmentId);
                        }
                    }
                }
                result.append(EXTENSIONS.get(ext));
                cachedFileName = result.toString();
            }
            return cachedFileName;
        }

        public File getFile() {
            return new File(parentDir, toString());
        }

        public CheckpointFilename getForPayload(boolean inbound) {
            return new CheckpointFilename(this, inbound ? ExtensionType.PAYLOAD_INBOUD : ExtensionType.PAYLOAD_OUTBOUND);
        }

        public static CheckpointFilename createBasic(Job job) {
            return createBasic(job.getId(), job.getJobsFile().getParentFile());
        }

        public static CheckpointFilename createBasic(String jobId, File dir) {
            return new CheckpointFilename(jobId, dir, ExtensionType.BASIC);
        }

        public static CheckpointFilename createAttachment(Job job, String attachmentId) {
            return new CheckpointFilename(job, attachmentId);
        }

    }

    private final static LocalStringManagerImpl strings = new LocalStringManagerImpl(CheckpointHelper.class);

    @Inject
    AuthenticationService authenticationService;

    @Inject
    ServiceLocator serviceLocator;

    @Inject
    ObjectInputOutputStreamFactory factory;

    public void save(JobManager.Checkpoint checkpoint) throws IOException {
        CheckpointFilename cf = CheckpointFilename.createBasic(checkpoint.getJob());
        ObjectOutputStream oos = null;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(cf.getFile());
            oos = factory.createObjectOutputStream(fos);
            oos.writeObject(checkpoint);
            oos.close();
            Outbound outboundPayload = checkpoint.getContext().getOutboundPayload();
            if (outboundPayload != null && outboundPayload.isDirty()) {
                saveOutbound(outboundPayload, cf.getForPayload(false).getFile());
            }
            Inbound inboundPayload = checkpoint.getContext().getInboundPayload();
            if (inboundPayload != null) {
                saveInbound(inboundPayload, cf.getForPayload(true).getFile());
            }
        } catch (IOException e) {
            try {oos.close();} catch (Exception ex) {
            }
            try {fos.close();} catch (Exception ex) {
            }
            File file = cf.getFile();
            if (file.exists()) {
                file.delete();
            }
            file = cf.getForPayload(true).getFile();
            if (file.exists()) {
                file.delete();
            }
            file = cf.getForPayload(false).getFile();
            if (file.exists()) {
                file.delete();
            }
            throw e;
        }
    }

    public void saveAttachment(Serializable data, Job job, String attachmentId) throws IOException {
        ObjectOutputStream oos = null;
        FileOutputStream fos = null;
        CheckpointFilename cf = CheckpointFilename.createAttachment(job, attachmentId);
        try {
            fos = new FileOutputStream(cf.getFile());
            oos = factory.createObjectOutputStream(fos);
            oos.writeObject(data);
        } finally {
            try {oos.close();} catch (Exception ex) {
            }
            try {fos.close();} catch (Exception ex) {
            }
        }
    }

    public JobManager.Checkpoint load(CheckpointFilename cf, Outbound outbound) throws IOException, ClassNotFoundException {
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        JobManager.Checkpoint checkpoint;
        try {
            fis = new FileInputStream(cf.getFile());
            ois = factory.createObjectInputStream(fis);
            checkpoint = (JobManager.Checkpoint) ois.readObject();
        } finally {
            try {ois.close();} catch (Exception ex) {
            }
            try {fis.close();} catch (Exception ex) {
            }
        }
        if (outbound != null) {
            loadOutbound(outbound, cf.getForPayload(false).getFile());
            checkpoint.getContext().setOutboundPayload(outbound);
        }
        Inbound inbound = loadInbound(cf.getForPayload(true).getFile());
        checkpoint.getContext().setInboundPayload(inbound);
        try {
            String username = checkpoint.getJob().getSubjectUsernames().get(0);
            Subject subject = authenticationService.impersonate(username, /* groups */ null, /* subject */ null, /* virtual */ false);
            checkpoint.getContext().setSubject(subject);
        } catch (LoginException e) {
            throw new RuntimeException(e);
        }
        return checkpoint;
    }

    public <T extends Serializable> T loadAttachment(Job job, String attachmentId) throws IOException, ClassNotFoundException {
        CheckpointFilename cf = CheckpointFilename.createAttachment(job, attachmentId);
        File file = cf.getFile();
        if (!file.exists()) {
            return null;
        }
        ObjectInputStream ois = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(cf.getFile());
            ois = factory.createObjectInputStream(fis);
            return (T) ois.readObject();
        } finally {
            try {ois.close();} catch (Exception ex) {
            }
            try {fis.close();} catch (Exception ex) {
            }
        }
    }

    public Collection<CheckpointFilename> listCheckpoints(File dir) {
        if (dir == null || !dir.exists()) {
            return Collections.emptyList();
        }
        final String extension = CheckpointFilename.EXTENSIONS.get(CheckpointFilename.ExtensionType.BASIC);
        File[] checkpointFiles = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(extension);
            }
        });
        if (checkpointFiles != null) {
            Collection<CheckpointFilename> result = new ArrayList<>(checkpointFiles.length);
            for (File checkpointFile : checkpointFiles) {
                try {
                    result.add(new CheckpointFilename(checkpointFile));
                } catch (IOException ex) {
                }
            }
            return result;
        } else {
            return Collections.emptyList();
        }
    }

    private void saveOutbound(Payload.Outbound outbound, File outboundFile) throws IOException {
        FileOutputStream os = new FileOutputStream(outboundFile);
        // Outbound saves text/plain with one part as text with no any details, force zip
        writePartsTo(outbound.parts(), os);
        outbound.resetDirty();
    }

    private void loadOutbound(Outbound outbound, File outboundFile) throws IOException {
        if (outbound == null || !outboundFile.exists()) {
            return;
        }

        Inbound outboundSource = loadInbound(outboundFile);
        Iterator<Part> parts = outboundSource.parts();
        File topDir = createTempDir("checkpoint", "");
        topDir.deleteOnExit();
        while (parts.hasNext()) {
            Part part = parts.next();
            File sourceFile = File.createTempFile("source", "", topDir);
            try (InputStream inputStream = part.getInputStream()) {
                FileUtils.copy(inputStream, sourceFile, Long.MAX_VALUE);
            }
            outbound.addPart(part.getContentType(), part.getName(), part.getProperties(), new FileInputStream(sourceFile));
        }

        outbound.resetDirty();
    }

    private void saveInbound(Payload.Inbound inbound, File inboundFile) throws IOException {
        if (!inboundFile.exists()) { // not saved yet
            FileOutputStream os = new FileOutputStream(inboundFile);
            writePartsTo(inbound.parts(), os);
        }
    }

    private Inbound loadInbound(File inboundFile) throws IOException {
        if (inboundFile == null || !inboundFile.exists()) {
            return null;
        }

        FileInputStream is = new FileInputStream(inboundFile);
        Inbound inboundSource = PayloadImpl.Inbound.newInstance("application/zip", is);
        return inboundSource;
    }

    // ZipPayloadImpl

    private void writePartsTo(Iterator<Part> parts, OutputStream os) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(os)) {
            while (parts.hasNext()) {
                Part part = parts.next();
                prepareEntry(part, zos);
                part.copy(zos);
                zos.closeEntry();
            }
        }
    }

    private void prepareEntry(final Payload.Part part, final ZipOutputStream zos) throws IOException {
        ZipEntry entry = new ZipEntry(part.getName());
        entry.setExtra(getExtraBytes(part));
        zos.putNextEntry(entry);
    }

    private byte[] getExtraBytes(Part part) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Properties props = part.getProperties();
        Properties fullProps = new Properties();
        if (props != null) {
            fullProps.putAll(props);
        }
        fullProps.setProperty(CONTENT_TYPE_NAME, part.getContentType());
        try {
            fullProps.store(baos, null);
            return baos.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private File createTempDir(final String prefix, final String suffix) throws IOException {
        File temp = File.createTempFile(prefix, suffix);
        if ( ! temp.delete()) {
            throw new IOException("Cannot delete temp file " + temp.getAbsolutePath());
        }
        if ( ! temp.mkdirs()) {
            throw new IOException("Cannot create temp directory" + temp.getAbsolutePath());
        }
        return temp;
    }

}
