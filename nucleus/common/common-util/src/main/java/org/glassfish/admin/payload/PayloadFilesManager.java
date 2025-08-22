/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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

package org.glassfish.admin.payload;

import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.Payload;
import org.glassfish.api.admin.Payload.Part;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.WARNING;

/**
 * Manages transferred files delivered via the request or response {@link Payload}.
 * <p>
 * Callers can process the entire payload
 * at once, treating each Part as a file, using the {@link #processParts}
 * method.  Or, the caller can invoke the {@link #processPart}
 * method to work with a single Part as a file.
 * <p>
 * If the caller wants to extract the payload's content as temporary files it should
 * instantiate {@link Temp} which exposes a {@link PayLoadFilesManager.Temp#cleanup}
 * method.  The caller should invoke this method once it has finished with
 * the transferred files, although the finalizer will invoke cleanup just in case.
 * <p>
 * On the other hand, if the caller wants to keep the transferred files it
 * should instantiate {@link Perm}.
 * <p>
 * <code>Temp</code> uses a unique temporary directory, then creates one
 * temp file for each part it is asked to deal with, either from an entire payload
 * ({@link #processParts(org.glassfish.api.admin.Payload.Inbound)}) or a
 * single part ({@link #processPart(org.glassfish.api.admin.Payload.Part)}).  Recall that each part in the
 * payload has a name which is a relative or absolute URI.
 *
 * @author tjquinn
 */
public abstract class PayloadFilesManager {
    private static final Logger LOG = System.getLogger(PayloadFilesManager.class.getName());
    private static final String XFER_DIR_PREFIX = "xfer-";
    private static final LocalStringManagerImpl strings = new LocalStringManagerImpl(PayloadFilesManager.class);

    private final File targetDir;
    private final ActionReport report;
    private final ActionReportHandler reportHandler;

    protected final Map<File,Long> dirTimestamps = new HashMap<>();

    private PayloadFilesManager(
            final File targetDir,
            final ActionReport report,
            final ActionReportHandler reportHandler) {
        this.targetDir = targetDir;
        this.report = report;
        this.reportHandler = reportHandler;
    }

    private PayloadFilesManager(
            final File targetDir,
            final ActionReport report) {
        this(targetDir, report, null);
    }

    protected File getTargetDir() {
        return targetDir;
    }

    protected URI getParentURI(Part part) throws UnsupportedEncodingException {
            /*
             * parentURI and parentFile start as the target extraction directory for this
             * manager, but will change iff the part specifies a file
             * transfer root.
             */
            File parentFile = getTargetDir();
            URI parentFileURI = parentFile.toURI();

            final Properties partProps = part.getProperties();
            String parentPathFromPart = partProps.getProperty("file-xfer-root");
            if (parentPathFromPart != null) {
                if (! parentPathFromPart.endsWith(File.separator)) {
                    parentPathFromPart = parentPathFromPart + File.separator;
                }
                final File xferRootFile = new File(parentPathFromPart);
                if (xferRootFile.isAbsolute()) {
                    parentFile = xferRootFile;
                } else {
                    parentFile = new File(parentFile, parentPathFromPart);
                }
                /*
                 * If this parent directory does not exist, then the URI from
                 * the File object will lack the trailing slash.  So create
                 * the URI a little oddly to account for that case.
                 */
                parentFileURI = URI.create(
                        parentFile.toURI().toASCIIString() +
                        (parentFile.exists() ? "" : "/"));
            }
            return parentFileURI;
        }

    /**
     * Extracts files from a Payload and leaves them on disk.
     * <p>
     * The Perm manager constructs output file paths this way.  The URI from the
     * manager's targetDir (which the caller passes to the constructor) is the default
     * parent URI for the output file.
     * <p>
     * Next, the Part's properties are checked for a file-xfer-root property.
     * If found, it is used as a URI (either absolute or, if relative, resolved
     * against the targetDir).
     * <p>
     * Finally, the "output name" is either the
     * name from the Payload.Part for the {@link #extractFile(org.glassfish.api.admin.Payload.Part) }
     * method or the caller-provided argument in the {@link #extractFile(org.glassfish.api.admin.Payload.Part, java.lang.String) }
     * method.
     * <p>
     * In either case, the output name is used as a URI
     * string and is resolved against the targetDir combined with (if present) the
     * file-xfer-root property.
     * <p>
     * The net effect of this
     * is that if the output name is an absolute URI then it will override the
     * targetDir and the file-xfer-root setting.  If the output name is
     * relative then it will be resolved
     * against the targetDir plus file-xfer-root URI to derive the URI for the output file.
     */
    public static class Perm extends PayloadFilesManager {

        /**
         * Creates a new PayloadFilesManager for dealing with permanent files that
         * will be anchored at the specified target directory.
         * @param targetDir directory under which the payload's files should be stored
         * @param report result report to which extraction results will be appened
         */
        public Perm(final File targetDir, final ActionReport report) {
            this(targetDir, report, null);
        }

        /**
         * Creates a new PayloadFilesManager for permanent files anchored at
         * the specified target directory.
         * @param targetDir directory under which the payload's files should be stored
         * @param report result report to which extraction results will be appened
         * @param reportHandler handler to invoke for each ActionReport in the payload
         */
        public Perm(final File targetDir, final ActionReport report, final ActionReportHandler reportHandler) {
            super(targetDir != null ? targetDir : new File(System.getProperty("user.dir")), report, reportHandler);
        }

        /**
         * Creates a new PayloadFilesManager for permanent files anchored at
         * the caller's current directory.
         * @param report result report to which extraction results will be appended
         */
        public Perm(final ActionReport report) {
            this(report,  null);
        }


        /**
         * Creates a new PayloadFilesManager for permanent files anchored at
         * the caller's current directory.
         * @param report result report to which extraction results will be appened
         * @param reportHandler handler to invoke for each ActionReport in the payload
         */
        public Perm(final ActionReport report, final ActionReportHandler reportHandler) {
            super(new File(System.getProperty("user.dir")), report, reportHandler);
        }

        /**
         * Creates a new PayloadFilesManager for permanent files anchored at
         * the caller's current directory.
         */
        public Perm() {
            this((ActionReportHandler) null);
        }

        public Perm(final ActionReportHandler reportHandler) {
            this(null, reportHandler);
        }

        @Override
        protected void postExtract(File extractedFile) {
            // no-op for permanent files
        }

        @Override
        protected void postProcessParts() {
            for (Map.Entry<File,Long> entry : dirTimestamps.entrySet()) {
                final Date when = new Date(entry.getValue());
                LOG.log(DEBUG, "Setting lastModified for {0} explicitly to {1}", entry.getKey(), when);
                if (!entry.getKey().setLastModified(entry.getValue())) {
                    LOG.log(Level.WARNING, strings.getLocalString("payload.setLatModifiedFailed",
                        "Attempt to set lastModified for {0} failed; no further information is available. Continuing.",
                        entry.getKey().getAbsoluteFile()));
                }
            }
        }

    }

    /**
     * Extracts files from a payload, treating them as temporary files.
     * The caller should invoke {@link #cleanup} once it is finished with the
     * extracted files, although the finalizer will invoke cleanup if the
     * caller has not.
     */
    public static class Temp extends PayloadFilesManager {

        private boolean isCleanedUp = false;

        public Temp(final File parentDir, final ActionReport report) throws IOException {
            super(createTempFolder(parentDir), report);
        }


        /**
         * Creates a new PayloadFilesManager for temporary files.
         * @param report results report to which extraction results will be appended
         * @throws java.io.IOException
         */
        public Temp(final ActionReport report) throws IOException {
            this(new File(System.getProperty("java.io.tmpdir")), report);
        }

        /**
         * Creates a new PayloadFilesManager for temporary files.
         * @throws java.io.IOException
         */
        public Temp() throws IOException {
            this(null);
        }

        /**
         * Deletes the temporary files created by this temp PayloadFilesManager.
         */
        public void cleanup() {
            if ( ! isCleanedUp) {
                FileUtils.whack(super.targetDir);
                isCleanedUp = true;
            }
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            cleanup();
        }

        @Override
        protected void postExtract(File extractedFile) {
            extractedFile.deleteOnExit();
        }

        @Override
        protected void postProcessParts() {
            // no-op
        }
    }

    protected abstract void postExtract(final File extractedFile);

    protected URI getOutputFileURI(Part part, String name) throws IOException {
        /*
         * The part name might have path elements using / as the
         * separator, so figure out the full path for the resulting
         * file.
         */
        if (name.startsWith("/")) {
            name = name.substring(1);
        }
        URI targetURI = getParentURI(part).resolve(name);
        return targetURI;
    }

    private File removeFile(final Payload.Part part) throws IOException {
        final File result = removeFileWithoutConsumingPartBody(part);
        consumePartBody(part);
        return result;
    }

    private File removeFileWithoutConsumingPartBody(final Payload.Part part) throws IOException {
        File targetFile = new File(getOutputFileURI(part, part.getName()));
        if (targetFile.exists()) {
            final boolean isRemovalRecursive = targetFile.isDirectory() && part.isRecursive();
            if (isRemovalRecursive ? FileUtils.whack(targetFile) : targetFile.delete()) {
                LOG.log(DEBUG, "Deleted {0}{1} as requested", targetFile.getAbsolutePath(),
                    isRemovalRecursive ? " recursively" : "");
                reportDeletionSuccess();
            } else {
                LOG.log(DEBUG, "File {0} ({1}) requested for deletion exists but was not able to be deleted",
                    part.getName(), targetFile.getAbsolutePath());
                reportDeletionFailure(part.getName(), strings.getLocalString("payload.deleteFailedOnFile",
                    "Requested deletion of {0} failed; the file was found but the deletion attempt failed - no reason is available"));
            }
        } else {
            LOG.log(DEBUG, "File {0} ({1}) requested for deletion does not exist.", part.getName(),
                targetFile.getAbsolutePath());
            reportDeletionFailure(part.getName(), new FileNotFoundException(targetFile.getAbsolutePath()));
        }
        return targetFile;
    }


    private File replaceFile(final Payload.Part part) throws IOException {
        removeFileWithoutConsumingPartBody(part);
        return extractFile(part, part.getName());
    }

    private void consumePartBody(final Part part) throws FileNotFoundException, IOException {
        try (InputStream is = part.getInputStream()) {
            is.readAllBytes();
        }
    }

    private void processReport(final Payload.Part part) throws Exception {
        if (reportHandler == null) {
            consumePartBody(part);
        } else {
            reportHandler.handleReport(part.getInputStream());
        }
    }

    /**
     * Extracts the contents of the specified Part as a file, specifying
     * the relative or absolute URI to use for creating the extracted file.
     * If outputName is relative it is resolved against the manager's target
     * directory (which the caller passed to the constructor) and the
     * file-xfer-root Part property, if present.
     * @param part the Part containing the file's contents
     * @param outputName absolute or relative URI string to use for the extracted file
     * @return File for the extracted file
     * @throws java.io.IOException
     */
    private File extractFile(final Payload.Part part, final String outputName) throws IOException {
        // Look in the Part's properties first for the URI of the target
        // directory for the file.  If there is none there then use the
        // target directory for this manager.
        try {
            File extractedFile = new File(getOutputFileURI(part, outputName));

            // Create the required directory tree under the target directory.
            File immediateParent = extractedFile.getParentFile();
            if (!immediateParent.exists() && !immediateParent.mkdirs()) {
                LOG.log(Level.WARNING, strings.getLocalString(
                        "payload.mkdirsFailed",
                        "Attempt to create directories for {0} failed; no further information is available. Continuing.",
                        immediateParent));
            }
            if (extractedFile.exists()) {
                if (!extractedFile.delete() && !extractedFile.isDirectory()) {
                    // Don't warn if we cannot delete the directory - there
                    // are likely to be files in it preventing its removal.
                    LOG.log(WARNING, strings.getLocalString("payload.overwrite",
                        "Overwriting previously-uploaded file because the attempt to delete it failed: {0}",
                        extractedFile));
                } else {
                    LOG.log(DEBUG, "Deleted pre-existing file {0} before extracting transferred file", extractedFile);
                }
            }

            // If we are extracting a directory, then we need to consume the
            // Part's body but we won't write anything into the directory file.
            if (outputName.endsWith("/")) {
                if (!extractedFile.exists() && !extractedFile.mkdir()) {
                    LOG.log(Level.WARNING, strings.getLocalString("payload.mkdirsFailed",
                        "Attempt to create directories for {0} failed; no further information is available. Continuing.",
                        extractedFile));
                }
            }

            if (!extractedFile.isDirectory()) {
                try (InputStream is = part.getInputStream()) {
                    FileUtils.copy(is, extractedFile);
                }
            }

            // This is because some commands need to process also stream payload
            // parts more then ones. We have to tell that it was extracted to some file
            part.setExtracted(extractedFile);

            final String lastModifiedString = part.getProperties().getProperty("last-modified");
            final long lastModified = lastModifiedString == null ? System.currentTimeMillis() : Long.parseLong(lastModifiedString);

            if (!extractedFile.setLastModified(lastModified)) {
                LOG.log(Level.WARNING,
                    strings.getLocalString("payload.setLatModifiedFailed",
                        "Attempt to set lastModified for {0} failed; no further information is available.  Continuing.",
                        extractedFile));
            }
            if (extractedFile.isDirectory()) {
                dirTimestamps.put(extractedFile, lastModified);
            }
            postExtract(extractedFile);
            LOG.log(DEBUG, "Extracted transferred entry {0} of size {1} B to {2}",
                new Object[] {part.getName(), extractedFile.length(), extractedFile});
            reportExtractionSuccess();
            return extractedFile;
        } catch (IOException e) {
            reportExtractionFailure(part.getName(), e);
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * Returns all Files extracted from the Payload, treating each Part as a
     * separate file, via a Map from each File to its associated Properties.
     *
     * @param inboundPayload Payload containing file data to be extracted
     * @return map from each extracted File to its corresponding Properties
     * @throws java.io.IOException
     */
    public Map<File,Properties> processPartsExtended(
            final Payload.Inbound inboundPayload) throws Exception {

        if (inboundPayload == null) {
            return Collections.emptyMap();
        }

        final Map<File,Properties> result = new LinkedHashMap<>();

        boolean isReportProcessed = false;
        Part possibleUnrecognizedReportPart = null;

        StringBuilder uploadedEntryNames = new StringBuilder();
        for (Iterator<Payload.Part> partIt = inboundPayload.parts(); partIt.hasNext();) {
            Payload.Part part = partIt.next();
            DataRequestType drt = DataRequestType.getType(part);
            if (drt != null) {
                result.put(drt.processPart(this, part, part.getName()), part.getProperties());
                isReportProcessed |= (drt == DataRequestType.REPORT);
                uploadedEntryNames.append(part.getName()).append(" ");
            } else {
                if ( (! isReportProcessed) && possibleUnrecognizedReportPart == null) {
                    possibleUnrecognizedReportPart = part;
                }
            }
        }
        if ( (! isReportProcessed) && possibleUnrecognizedReportPart != null) {
            DataRequestType.REPORT.processPart(this, possibleUnrecognizedReportPart,
                    possibleUnrecognizedReportPart.getName());
            isReportProcessed = true;
        }
        postProcessParts();
        return result;
    }

    /**
     * Returns all Files extracted from the Payload, treating each Part as a
     * separate file.
     * @param inboundPayload Payload containing file data to be extracted
     * @parma reportHandler invoked for each ActionReport Part in the payload
     * @return the Files corresponding to the content of each extracted file
     * @throws java.io.IOException
     */
    public List<File> processParts(
            final Payload.Inbound inboundPayload) throws Exception {

        return new ArrayList<>(processPartsExtended(inboundPayload).keySet());
    }

    public interface ActionReportHandler {
        void handleReport(final InputStream reportStream) throws Exception;
    }

    protected abstract void postProcessParts();

    private void reportExtractionSuccess() {
        reportSuccess();
    }

    private void reportSuccess() {
        if (report != null) {
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        }
    }

    private void reportDeletionSuccess() {
        reportSuccess();
    }

    private void reportDeletionFailure(final String partName, final String msg) {
        reportFailure(partName, msg, null);
    }

    private void reportDeletionFailure(final String partName, final Exception e) {
        reportFailure(partName, strings.getLocalString(
                    "payload.errDeleting",
                    "Error deleting file {0}",
                    partName), e);
    }

    private void reportFailure(final String partName, final String formattedMessage, final Exception e) {
        if (report != null) {
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(formattedMessage);
            report.setFailureCause(e);
        }
    }

    private void reportExtractionFailure(final String partName, final Exception e) {
        reportFailure(partName,
                strings.getLocalString(
                    "payload.errExtracting",
                    "Error extracting transferred file {0}",
                    partName),
                    e);
    }

    /**
     * Creates a unique temporary directory within the specified parent.
     * @param parent directory within which to create the temp dir; will be created if absent
     * @return the temporary folder
     * @throws java.io.IOException
     */
    private static File createTempFolder(final File parent, final String prefix) throws IOException {
        File result = File.createTempFile(prefix, "", parent);
        try {
            if (!result.delete()) {
                throw new IOException(strings.getLocalString("payload.command.errorDeletingTempFile",
                    "Unknown error deleting temporary file {0}", result.getAbsolutePath()));
            }
            if (!result.mkdir()) {
                throw new IOException(strings.getLocalString("payload.command.errorCreatingDir",
                    "Unknown error creating directory {0}", result.getAbsolutePath()));
            }
            LOG.log(Level.DEBUG, "Created temporary upload folder {0}", result.getAbsolutePath());
            return result;
        } catch (Exception e) {
            throw new IOException(strings.getLocalString("payload.command.errorCreatingXferFolder",
                "Error creating temporary file transfer folder"), e);
        }
    }

    private static File createTempFolder(final File parent) throws IOException {
        return createTempFolder(parent, XFER_DIR_PREFIX);
    }

    /**
     * Types of data requests the PayloadFilesManager understands.
     * <p>
     * To add a new type, add a new enum value with the value of the data
     * request type as the constructor argument and implement the processPart
     * method.
     */
    private enum DataRequestType {
        FILE_TRANSFER("file-xfer") {

            @Override
            protected File processPart(
                    final PayloadFilesManager pfm,
                    final Part part,
                    final String partName) throws Exception {
                return pfm.extractFile(part, partName);
            }

        },
        FILE_REMOVAL("file-remove") {
            @Override
            protected File processPart(
                    final PayloadFilesManager pfm,
                    final Part part,
                    final String partName) throws Exception {
                return pfm.removeFile(part);
            }

        },
        FILE_REPLACEMENT("file-replace") {

            @Override
            protected File processPart(
                    final PayloadFilesManager pfm,
                    final Part part,
                    final String partName) throws Exception {
                return pfm.replaceFile(part);
            }
        },
        REPORT("report") {

            @Override
            protected File processPart(
                    final PayloadFilesManager pfm, final
                    Part part,
                    final String partName) throws Exception {
                pfm.processReport(part);
                return null;
            }

        };

        /** data-request-type value for this enum */
        private final String dataRequestType;

        /**
         * Creates a new instance of the enum
         * @param type
         */
        DataRequestType(final String type) {
            dataRequestType = type;
        }

        /**
         * Processes the specified part by delegating to the right method on
         * the PayloadFilesManager.
         * @param pfm
         * @param part
         * @param partName
         * @return
         * @throws IOException
         */
        protected abstract File processPart(
                final PayloadFilesManager pfm, final Part part, final String partName)
                throws Exception;

        /**
         * Finds the DataRequestType enum which matches the data-request-type
         * in the Part's properties.
         * @param part
         * @return DataRequestType matching the Part's data-request-type; null if no match exists
         */
        private static DataRequestType getType(final Part part) {
            final String targetDataRequestType = part.getProperties().getProperty("data-request-type");
            for (DataRequestType candidateType : values()) {
                if (candidateType.dataRequestType.equals(targetDataRequestType)) {
                    return candidateType;
                }
            }
            return null;
        }
    }
}

