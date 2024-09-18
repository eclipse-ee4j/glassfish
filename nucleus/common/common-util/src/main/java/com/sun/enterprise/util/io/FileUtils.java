/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.util.io;

import com.sun.enterprise.util.OS;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.glassfish.api.deployment.archive.WritableArchiveEntry;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.WARNING;


public final class FileUtils {

    /** Current user's home directory resolved from the system property user.home */
    public static final File USER_HOME = new File(System.getProperty("user.home"));

    private static final Logger LOG = System.getLogger(FileUtils.class.getName());

    private static final char[] ILLEGAL_FILENAME_CHARS = {'/', '\\', ':', '*', '?', '"', '<', '>', '|'};
    private static final char REPLACEMENT_CHAR = '_';
    private static final char BLANK = ' ';
    private static final char DOT = '.';

    private static final int FILE_OPERATION_MAX_RETRIES = Integer.getInteger("com.sun.appserv.winFileLockRetryLimit", 5).intValue();
    private static final int FILE_OPERATION_SLEEP_DELAY_MS = Integer.getInteger("com.sun.appserv.winFileLockRetryDelay", 1000).intValue();


    private FileUtils() {
        // hidden
    }


    /**
     * Result: existing writable directory given in parameter OR {@link IllegalStateException}.
     *
     * @param dir
     * @throws IllegalStateException The exception should not be catched, because it means that
     *             the client can have unpredictable issues.
     */
    public static void ensureWritableDir(File dir) {
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                throw new IllegalStateException("The configured temporary directory is not a directory: " + dir);
            }
            if (!dir.canWrite()) {
                throw new IllegalStateException("The configured temporary directory is not writeable: " + dir);
            }
            return;
        }
        if (dir.mkdirs()) {
            LOG.log(DEBUG, "The directory {0} has been created.", dir);
            return;
        }
        // just note: mkdirs maybe created part of the path, but we don't care here.
        throw new IllegalStateException("The configured directory does not exist and could not be created: " + dir);
    }


    /**
     * Wrapper for File.mkdirs
     * This version will return true if the directory exists when the method returns.
     * Unlike File.mkdirs which returns false if the directory already exists.
     *
     * @param f The file pointing to the directory to be created
     * @return true if the directory exists or was created by this method.
     */
    public static boolean mkdirsMaybe(File f) {
        return f != null && (f.isDirectory() || f.mkdirs());
    }

    /**
     * Wrapper for File.delete
     * This version will return true if the file does not exist when the method returns.
     * Unlike File.delete which returns false if the file does not exist.
     *
     * @param f The file to be deleted
     * @return true if the directory does not exist or was deleted by this method.
     */
    public static boolean deleteFileMaybe(File f) {
        return f != null && (!f.exists() || f.delete());
    }


    /**
     * Wrapper for File.listFiles
     * Guaranteed to return an array in all cases.
     * File.listFiles() returns either null or an empty array.
     * This is annoying and results in harder than neccessry to read code -- i.e. there are 3
     * results possible:
     * <ul>
     * <li>an array with files in it
     * <li>an empty array
     * <li>a null
     * </ul>
     */
    public static File[] listFiles(File f) {
        try {
            File[] files = f.listFiles();
            if (files != null) {
                return files;
            }
        } catch (Exception e) {
            // fall through
        }
        return new File[0];
    }


    public static File[] listFiles(File f, FileFilter ff) {
        try {
            File[] files = f.listFiles(ff);
            if (files != null) {
                return files;
            }
        } catch (Exception e) {
            // fall through
        }
        return new File[0];
    }

    public static File[] listFiles(File f, FilenameFilter fnf) {
        try {
            File[] files = f.listFiles(fnf);
            if (files != null) {
                return files;
            }
        } catch (Exception e) {
            // fall through
        }
        return new File[0];
    }


    public static boolean safeIsDirectory(File f) {
        return f != null && f.exists() && f.isDirectory();
    }


    public static boolean safeIsRealDirectory(File f) {
        if (!safeIsDirectory(f)) {
            return false;
        }

        // these 2 values while be different for symbolic links
        String canonical = safeGetCanonicalPath(f);
        String absolute = f.getAbsolutePath();

        if (canonical.equals(absolute)) {
            return true;
        }

        /* Bug 4715043 -- WHOA -- Bug Obscura!!
           * In Windows, if you create the File object with, say, "d:/foo", then the
           * absolute path will be "d:\foo" and the canonical path will be "D:\foo"
           * and they won't match!!!
           **/
        if (OS.isWindows() && canonical.equalsIgnoreCase(absolute)) {
            return true;
        }

        return false;
    }


    public static String safeGetCanonicalPath(File f) {
        if (f == null) {
            return null;
        }
        try {
            return f.getCanonicalPath();
        } catch (IOException e) {
            return f.getAbsolutePath();
        }
    }


    public static File safeGetCanonicalFile(File f) {
        if (f == null) {
            return null;
        }
        try {
            return f.getCanonicalFile();
        } catch (IOException e) {
            return f.getAbsoluteFile();
        }
    }


    /**
     * @param f
     * @param ext
     * @return true if the file exists and it's name ends with ext
     */
    public static boolean hasExtension(File f, String ext) {
        if (f == null || !f.exists()) {
            return false;
        }
        return f.getName().endsWith(ext);
    }


    /**
     * @param f
     * @param ext
     * @return true if the file exists and it's name ends with ext (ignoring case)
     */
    public static boolean hasExtensionIgnoreCase(File f, String ext) {
        if (f == null || !f.exists()) {
            return false;
        }
        return f.getName().toLowerCase(Locale.ENGLISH).endsWith(ext.toLowerCase(Locale.ENGLISH));
    }

    /**
     * Gets the extension of the {@code file}.
     * <p>
     * This method returns the extension of the {@code file} <strong>with</strong> the leading dot.
     *
     * @param file the file
     * @return the file extension
     */
    public static String getExtension(File file) {
        if (file == null) {
            return null;
        }
        String fileName = file.getName();
        int extensionIndex = fileName.lastIndexOf('.');
        if (extensionIndex == -1) {
            return "";
        }
        return fileName.substring(extensionIndex);
    }

    /**
     * Removes the extension from a file name for the {@code file}.
     * <p>
     * This method returns the textual part of the file name before last dot.
     *
     * @param file the file
     * @return the file name without extension or {@code null} if {@code file} is {@code null}
     */
    public static String removeExtension(File file) {
        if (file == null) {
            return null;
        }
        String fileName = file.getName();
        int extensionIndex = fileName.lastIndexOf('.');
        if (extensionIndex == -1) {
            return fileName;
        }
        return fileName.substring(0, extensionIndex);
    }

    public static boolean isLegalFilename(String filename) {
        if (!isValidString(filename)) {
            return false;
        }

        for (char element : ILLEGAL_FILENAME_CHARS) {
            if (filename.indexOf(element) >= 0) {
                return false;
            }
        }

        return true;
    }


    public static boolean isFriendlyFilename(String filename) {
        if (!isValidString(filename)) {
            return false;
        }

        if (filename.indexOf(BLANK) >= 0 || filename.indexOf(DOT) >= 0) {
            return false;
        }

        return isLegalFilename(filename);
    }


    public static String makeLegalFilename(String filename) {
        if (isLegalFilename(filename)) {
            return filename;
        }

        // let's use "__" to replace "/" and "\" (on Windows) so less chance
        // to collide with the actual name when reverting
        filename = filename.replaceAll("[/" + Pattern.quote("\\") + "]", "__");

        for (char element : ILLEGAL_FILENAME_CHARS) {
            filename = filename.replace(element, REPLACEMENT_CHAR);
        }

        return filename;
    }


    public static String makeLegalNoBlankFileName(String filename) {
        return makeLegalFilename(filename).replace(BLANK, REPLACEMENT_CHAR);
    }


    public static String makeFriendlyFilename(String filename) {
        if (isFriendlyFilename(filename)) {
            return filename;
        }

        String ret = makeLegalFilename(filename).replace(BLANK, REPLACEMENT_CHAR);
        ret = ret.replace(DOT, REPLACEMENT_CHAR);
        return ret;
    }


    public static String makeFriendlyFilenameExtension(String filename) {
        if (filename == null) {
            return null;
        }

        filename = makeLegalNoBlankFileName(filename);
        final String extension;
        if (filename.endsWith(".ear")) {
            filename = filename.substring(0, filename.indexOf(".ear"));
            extension = "_ear";
        } else if (filename.endsWith(".war")) {
            filename = filename.substring(0, filename.indexOf(".war"));
            extension = "_war";
        } else if (filename.endsWith(".jar")) {
            filename = filename.substring(0, filename.indexOf(".jar"));
            extension = "_jar";
        } else if (filename.endsWith(".rar")) {
            filename = filename.substring(0, filename.indexOf(".rar"));
            extension = "_rar";
        } else {
            extension = "";
        }
        return filename + extension;
    }


    public static String revertFriendlyFilenameExtension(String filename) {
        if (filename == null || (!filename.endsWith("_ear") && !filename.endsWith("_war") && !filename.endsWith("_jar")
            && !filename.endsWith("_rar"))) {
            return filename;
        }

        final String extension;
        if (filename.endsWith("_ear")) {
            filename = filename.substring(0, filename.indexOf("_ear"));
            extension = ".ear";
        } else if (filename.endsWith("_war")) {
            filename = filename.substring(0, filename.indexOf("_war"));
            extension = ".war";
        } else if (filename.endsWith("_jar")) {
            filename = filename.substring(0, filename.indexOf("_jar"));
            extension = ".jar";
        } else if (filename.endsWith("_rar")) {
            filename = filename.substring(0, filename.indexOf("_rar"));
            extension = ".rar";
        } else {
            extension = "";
        }
        return filename + extension;
    }


    public static String revertFriendlyFilename(String filename) {

        //first, revert the file extension
        String name = revertFriendlyFilenameExtension(filename);

        //then, revert the rest of the string
        return name.replaceAll("__", "/");
    }


    public static void liquidate(File parent) {
        whack(parent);
    }


    public static boolean isJar(File f) {
        return hasExtension(f, ".jar");
    }


    public static boolean isZip(File f) {
        return hasExtensionIgnoreCase(f, ".zip");
    }


    /**
     * Deletes a directory and its contents.
     * <p/>
     * If this method encounters a symbolic link in the subtree below "parent"
     * then it deletes the link but not any of the files pointed to by the link.
     * Note that whack will delete files if a symbolic link appears in the
     * path above the specified parent directory in the path.
     *
     * @param parent the File at the top of the subtree to delete
     * @return success or failure of deleting the directory
     */
    public static boolean whack(File parent) {
        return whack(parent, null);
    }

    /**
     * Deletes a directory and its contents.
     * <p/>
     * If this method encounters a symbolic link in the subtree below "parent"
     * then it deletes the link but not any of the files pointed to by the link.
     * Note that whack will delete files if a symbolic link appears in the
     * path above the specified parent directory in the path.
     *
     * @param parent the File at the top of the subtree to delete
     * @return success or failure of deleting the directory
     */
    public static boolean whack(File parent, Collection<File> undeletedFiles) {
        try {
            // Resolve any links up-stream from this parent directory and
            // then whack the resulting resolved directory.
            return whackResolvedDirectory(parent.getCanonicalFile(), undeletedFiles);
        } catch (IOException ioe) {
            LOG.log(Level.ERROR, "Could not recursively delete the directory " + parent, ioe);
            return false;
        }
    }

    /**
     * Deletes a directory and its contents.
     * <p/>
     * The whackResolvedDirectory method is invoked with a File argument
     * in which any upstream file system links have already been resolved.
     * This method will treate Any file passed in that does not have the same
     * absolute and canonical path - as evaluated in safeIsRealDirectory -
     * as a link and will delete the link without deleting any files in the
     * linked directory.
     *
     * @param parent the File at the top of the subtree to delete
     * @return success or failure of deleting the directory
     */
    private static boolean whackResolvedDirectory(File parent, Collection<File> undeletedFiles) {
        // Do not recursively delete the contents if the current parent is a symbolic link.
        if (safeIsRealDirectory(parent)) {
            File[] kids = listFiles(parent);

            for (File f : kids) {
                if (f.isDirectory()) {
                    whackResolvedDirectory(f, undeletedFiles);
                } else if (!deleteFile(f) && undeletedFiles != null) {
                    undeletedFiles.add(f);
                }

            }
        }
        // Delete the directory or symbolic link.
        return deleteFile(parent);
    }

    /**
     * Delete a file.  If impossible to delete then try to delete it when the JVM exits.
     * E.g. when Windows is using a jar in the current JVM -- you can not delete the jar until
     * the JVM dies.
     * @param f file to delete
     * @deprecated Usually points to an IO leak
     */
    @Deprecated
    public static void deleteFileNowOrLater(File f) {
        if (!deleteFile(f)) {
            f.deleteOnExit();
        }
    }

    /**
     * Delete a file.  Will retry every ten milliseconds for five seconds, doing a
     * gc after each second.
     *
     * @param f file to delete
     * @return boolean indicating success or failure of the deletion atttempt; returns true if file is absent
     */
    public static boolean deleteFileWithWaitLoop(File f) {
        return internalDeleteFile(f, true);

    }

    /**
     * Delete a file.  If on Windows and the delete fails, run the gc and retry the deletion.
     *
     * @param f file to delete
     * @return boolean indicating success or failure of the deletion atttempt; returns true if file is absent
     */
    public static boolean deleteFile(File f) {
        return internalDeleteFile(f, false);
    }

    /**
     * Delete a file.  If on Windows and the delete fails, run the gc and retry the deletion.
     *
     * @param f file to delete
     * @return boolean indicating success or failure of the deletion atttempt; returns true if file is absent
     */
    private static boolean internalDeleteFile(File f, boolean doWaitLoop) {
        // The operation succeeds immediately if the file is deleted successfully.
        // On systems that support symbolic links, the file will be reported as non-existent if
        // the file is a sym link to a non-existent directory.
        // In that case invoke delete to remove the link before checking for existence, since
        // File.exists on a symlink checks for the existence of the linked-to directory or
        // file rather than of the link itself.
        if (doWaitLoop) {
            DeleteFileWork work = new DeleteFileWork(f);
            doWithRetry(work);
            if (work.workComplete()) {
                return true;
            }
        } else {
            if (f.delete()) {
                return true;
            }
        }

        // The deletion failed.  This could be simply because the file does not exist.
        // In that case, log an appropriate message and return.
        if (f.exists()) {
            // The delete failed and the file exists.
            // Log a message if that level is enabled and return false to indicate the failure.
            LOG.log(WARNING, "Error attempting to delete {0}", f);
            return false;
        }
        LOG.log(Level.TRACE, "Attempt to delete {0} failed; the file is reported as non-existent", f);
        return true;
    }

    /**
     * Opens a stream to the specified output file, retrying if necessary.
     *
     * @param out the output File for which a stream is needed
     * @return the FileOutputStream
     * @throws IOException for any errors opening the stream
     */
    public static FileOutputStream openFileOutputStream(File out) throws IOException {
        FileOutputStreamWork work = new FileOutputStreamWork(out);
        int retries = doWithRetry(work);
        if (retries > 0) {
            LOG.log(DEBUG, "Retried {0} times the output to {1}", retries, out);
        }
        if (work.workComplete()) {
            return work.getStream();
        }
        throw new IOException("Failed opening file for output: " + out, work.getLastError());
    }


    public static Set<File> getAllFilesAndDirectoriesUnder(File directory) throws IOException {
        if (!directory.exists() || !directory.isDirectory()) {
            throw new IOException("Problem with: " + directory + ". You must supply a directory that exists");
        }
        Set<File> allFiles = new TreeSet<>();
        recursiveGetFilesUnder(directory, directory, null, allFiles, true);
        return allFiles;
    }

    // relativizingRoot can be null, in which case no relativizing is performed.
    private static void recursiveGetFilesUnder(File relativizingRoot, File directory, FilenameFilter filenameFilter, Set<File> set, boolean returnDirectories) {
        File[] files = listFiles(directory, filenameFilter);
        for (File file : files) {
            if (file.isDirectory()) {
                recursiveGetFilesUnder(relativizingRoot, file, filenameFilter, set, returnDirectories);
                if (returnDirectories) {
                    if (relativizingRoot != null) {
                        set.add(relativize(relativizingRoot, file));
                    } else {
                        set.add(file);
                    }
                }
            } else {
                if (relativizingRoot != null) {
                    set.add(relativize(relativizingRoot, file));
                } else {
                    set.add(file);
                }
            }
        }
    }

    /**
     * Given a directory and a fully-qualified file somewhere
     * under that directory, return the portion of the child
     * that is relative to the parent.
     */
    private static File relativize(File parent, File child) {
        String baseDir = parent.getAbsolutePath();
        String baseDirAndChild = child.getAbsolutePath();
        String relative = baseDirAndChild.substring(baseDir.length(), baseDirAndChild.length());

        // Strip off any extraneous file separator character.
        if (relative.startsWith(File.separator)) {
            relative = relative.substring(1);
        }
        return new File(relative);
    }


    /**
     * Executes the supplied work object until the work is done or the max.
     * retry count is reached.
     *
     * @param work the RetriableWork implementation to be run
     * @return the number of retries performed; 0 indicates the work succeeded without having to
     *         retry
     * @deprecated The situation usually means there's an IO leak. The only practical usage is
     *             on Windows OS when many threads/processes are trying to access the same file.
     */
    @Deprecated
    private static int doWithRetry(RetriableWork work) {
        int retries = 0;

        // Try the work the first time. Ideally this will work.
        work.run();

        // If the work failed and this is Windows - on which running gc may
        // unlock the locked file - then begin the retries.
        if (!work.workComplete() && OS.isWindows()) {
            while (!work.workComplete() && retries++ < FILE_OPERATION_MAX_RETRIES) {
                try {
                    Thread.sleep(FILE_OPERATION_SLEEP_DELAY_MS);
                } catch (InterruptedException ex) {
                }
                LOG.log(DEBUG, "Performing gc to try to force file closures");
                System.gc();
                work.run();
            }
        }
        return retries;
    }


    /**
     * Copies the entire tree to a new location.
     *
     * @param din  File pointing at root of tree to copy
     * @param dout File pointing at root of new tree
     * @throws IOException if an error while copying the content
     */
    public static void copyTree(File din, File dout) throws IOException {
        if (!safeIsDirectory(din)) {
            throw new IllegalArgumentException("Source isn't a directory");
        }

        if (!mkdirsMaybe(dout)) {
            throw new IllegalArgumentException("Can't create destination directory");
        }

        FileListerRelative flr = new FileListerRelative(din);
        String[] files = flr.getFiles();

        for (String file : files) {
            File fin = new File(din, file);
            File fout = new File(dout, file);
            copy(fin, fout);
        }
    }


    public static File copyResourceToDirectory(String resourcePath, File outputDirectory) throws IOException {
        int slashIndex = resourcePath.lastIndexOf('/');
        String fileName = slashIndex < 0 ? resourcePath : resourcePath.substring(slashIndex + 1);
        File output = new File(outputDirectory, fileName);
        if (output.exists()) {
            return output;
        }
        return copyResource(resourcePath, output);
    }


    /**
     * If the path dir/file does not exist, look for it in the classpath.
     * If found in classpath, create dir/file.
     * <p>
     * Existing file will not be overwritten.
     *
     * @param resourcePath - resource loadable by the thread context classloader.
     * @param outputFile - if the file exists, it will be overwritten
     * @return the File representing dir/file. If the resource does not exist, return null.
     * @throws IOException
     */
    public static File copyResource(String resourcePath, File outputFile) throws IOException {
        LOG.log(DEBUG, "copyResource(resourcePath={0}, outputFile={1})", resourcePath, outputFile);
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                return null;
            }
            if (!mkdirsMaybe(outputFile.getParentFile())) {
                throw new IOException("Can't create parent dir of output file: " + outputFile);
            }
            copy(is, outputFile);
            return outputFile;
        }
    }


    /**
     * Copies a file or directory.
     *
     * @param fin File to copy
     * @param fout New file
     * @throws IOException if an error while copying the content
     */
    public static void copy(File fin, File fout) throws IOException {
        if (safeIsDirectory(fin)) {
            copyTree(fin, fout);
            return;
        }
        if (!fin.exists()) {
            throw new IllegalArgumentException("File source doesn't exist");
        }
        if (!mkdirsMaybe(fout.getParentFile())) {
            throw new RuntimeException("Can't create parent dir of output file: " + fout);
        }
        Files.copy(fin.toPath(), fout.toPath(), StandardCopyOption.REPLACE_EXISTING);
        LOG.log(DEBUG, "Successfully copyied file {0} to {1}", fin, fout);
    }


    /**
     * Computes file or directory size. Follows symlinks just for the provided parameter, but not
     * for files under the tree.
     *
     * @param fileOrDirectory
     * @return summary of sizes of regular files.
     * @throws IOException if any file size cannot be read.
     */
    public static long getSize(File fileOrDirectory) throws IOException {
        try {
            return Files.walk(fileOrDirectory.getCanonicalFile().toPath())
                .filter(p -> Files.isRegularFile(p, LinkOption.NOFOLLOW_LINKS)).mapToLong(FileUtils::getRegularFileSize)
                .sum();
        } catch (IllegalStateException e) {
            throw new IOException("Could not read size of " + fileOrDirectory, e);
        }
    }


    private static long getRegularFileSize(Path path) {
        try {
            return Files.size(path);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read file size for " + path, e);
        }
    }


    /**
     * Returns a String with uniform slashes such that all the
     * occurances of '\\' are replaced with '/'.
     * In other words, the returned string will have all forward slashes.
     * Accepts non-null strings only.
     *
     * @param inputStr non null String
     * @return a String which <code> does not contain `\\` character </code>
     */
    public static String makeForwardSlashes(String inputStr) {
        if (inputStr == null) {
            throw new IllegalArgumentException("null String FileUtils.makeForwardSlashes");
        }
        return inputStr.replace('\\', '/');
    }


    /**
     * Given a string (typically a path), quote the string such that spaces
     * are protected from interpretation by a Unix or Windows command shell.
     * Note that this method does not handle quoting for all styles of special
     * characters. Just for the basic case of strings with spaces.
     *
     * @param s input string
     * @return a String which is quoted to protect spaces
     */
    public static String quoteString(String s) {
        if (s == null) {
            throw new IllegalArgumentException("null string");
        }

        if (!s.contains("\'")) {
            return ("\'" + s + "\'");
        } else if (!s.contains("\"")) {
            return ("\"" + s + "\"");
        } else {
            // Contains a single quote and a double quote. Use backslash
            // On Unix. Double quotes on Windows. This method does not claim
            // to support this case well if at all
            if (OS.isWindows()) {
                return("\"" + s + "\"");
            } else {
                return(s.replaceAll("\040", "\134 "));
            }
        }
    }


    static boolean isValidString(String s) {
        return s != null && !s.isEmpty();
    }

    /**
     * This method should be used instead of {@link #copy(InputStream, File, long)} if you don't
     * know the size of the input stream.
     *
     * @param in It will NOT be closed after processing. That is caller's responsibility.
     * @param out Target output file. If the file already exists, it will be overwritten!
     * @throws IOException
     */
    public static void copy(File in, OutputStream out) throws IOException {
        try (FileInputStream input  = new FileInputStream(in)) {
            input.transferTo(out);
        }
    }


    /**
     * Fast method using NIO to copy data from the input to the output file, when you already do
     * know the size of the input.
     * <p>
     * WARNING: Don't use it when you don't know the byteCount value.
     *
     * @param in It will be closed after processing.
     * @param out Target output file.
     * @param byteCount count of bytes to be transferred.
     * @throws IOException if the operation failed.
     * @throws IllegalArgumentException if the byte count is less then 0 or equal to
     *             {@link Long#MAX_VALUE} (obvious hacks)
     */
    public static void copy(InputStream in, File out, long byteCount) throws IOException, IllegalArgumentException {
        if (byteCount < 0 || byteCount >= Long.MAX_VALUE) {
            throw new IllegalArgumentException("If you don't know the byte count, don't use this method!");
        }
        try (
            ReadableByteChannel inputChannel = Channels.newChannel(in);
            FileOutputStream output = new FileOutputStream(out)) {
            output.getChannel().transferFrom(inputChannel, 0, byteCount);
        }
    }


    /**
     * This method should be used instead of {@link #copy(InputStream, File, long)} if you don't
     * know the size of the input stream.
     *
     * @param in It will NOT be closed after processing. That is caller's responsibility.
     * @param out Target output file. If the file already exists, it will be overwritten!
     * @throws IOException
     */
    public static void copy(InputStream in, File out) throws IOException {
        if (out.getParentFile().mkdirs()) {
            LOG.log(DEBUG, "Created directory {0}", out.getCanonicalPath());
        }
        long bytes = Files.copy(in, out.toPath(), StandardCopyOption.REPLACE_EXISTING);
        LOG.log(DEBUG, "Copyied {0} bytes to {1}", bytes, out);
    }


    /**
     * Copies stream with internal 8K buffer.
     *
     * @param in It is NOT closed after processing, caller is responsible for that.
     * @param os It is NOT closed after processing, caller is responsible for that.
     *
     * @throws IOException
     */
    public static void copy(InputStream in, OutputStream os) throws IOException {
        final ReadableByteChannel inputChannel = Channels.newChannel(in);
        final WritableByteChannel outputChannel = getChannel(os);
        if (outputChannel instanceof FileChannel) {
            // Can be optimized by the operating system
            FileChannel foch = (FileChannel) outputChannel;
            long transferred = foch.transferFrom(inputChannel, 0, Long.MAX_VALUE);
            LOG.log(Level.TRACE, "Copyied {0} B via {1}", transferred, foch);
            os.flush();
            return;
        }
        final ByteBuffer byteBuffer = ByteBuffer.allocate(8192);
        long transferred = 0;
        int read;
        do {
            read = inputChannel.read(byteBuffer);
            if (read >= 0) {
                byteBuffer.flip();
                outputChannel.write(byteBuffer);
                byteBuffer.clear();
                transferred += read;
            }
        } while (read != -1);
        LOG.log(Level.TRACE, "Copyied {0} B via {1}", transferred, outputChannel);
        os.flush();
    }


    private static WritableByteChannel getChannel(final OutputStream stream) {
        if (stream instanceof WritableArchiveEntry) {
            return ((WritableArchiveEntry) stream).getChannel();
        }
        return Channels.newChannel(stream);
    }


    /**
     * Rename, running gc on Windows if needed to try to force open streams to close.
     *
     * @param fromFile to be renamed
     * @param toFile name for the renamed file
     * @return boolean result of the rename attempt
     */
    public static boolean renameFile(File fromFile, File toFile) {
        RenameFileWork renameWork = new RenameFileWork(fromFile, toFile);
        int retries = doWithRetry(renameWork);
        boolean result = renameWork.workComplete();
        if (result) {
            LOG.log(DEBUG, "Attempt to rename {0} to {1} succeeded after {2} retries", fromFile, toFile, retries);
        } else {
            LOG.log(WARNING, "Attempt to rename {0} to {1} failed after {2} retries", fromFile, toFile, retries);
        }
        return result;
    }


    /**
     * A utility routine to read a <b> text file </b> efficiently and return
     * the contents as a String. Sometimes while reading log files of spawned
     * processes this kind of facility is handy. Instead of opening files, coding
     * FileReaders etc. this method could be employed. It is expected that the
     * file to be read is <code> small </code>.
     *
     * @param file Absolute path of the file
     * @param charset file charset
     * @return String representing the contents of the file. Lines are separated by
     *         {@link System#lineSeparator()}.
     * @throws java.io.IOException if there is an i/o error.
     * @throws java.io.FileNotFoundException if the file could not be found
     */
    public static String readSmallFile(final File file, final Charset charset) throws IOException {
        final StringBuilder sb = new StringBuilder();
        try (BufferedReader bf = new BufferedReader(new FileReader(file, charset))) {
            String line;
            while ((line = bf.readLine()) != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
            }
        }
        return sb.toString();
    }


    /**
     * Write the String to a file. Then make the file readable and writable.
     * If the file already exists it will be truncated and the contents replaced
     * with the String argument.
     *
     * @param s The String to write to the file
     * @param f The file to write the String to
     * @param charset file charset
     * @throws IOException if any errors
     */
    public static void writeStringToFile(String s, File f, Charset charset) throws IOException {
        try (Writer writer = new PrintWriter(f, charset)) {
            writer.write(s);
        } finally {
            f.setReadable(true);
            f.setWritable(true);
        }
    }


    /**
     * Find files matching the regular expression in the given directory
     *
     * @param dir the directory to search
     * @param regexp the regular expression pattern
     * @return either an array of matching File objects or an empty array. Guaranteed
     *         to never return null
     */
    public static File[] findFilesInDir(File dir, final String regexp) {
        File[] matches = dir.listFiles((dir1, name) -> name.matches(regexp));
        if (matches == null) {
            LOG.log(Level.WARNING, "Could not list files in {0}. Check permissions!", dir);
            return new File[0];
        }
        return matches;
    }

    /**
     * Represents a unit of work that should be retried, if needed, until it
     * succeeds or the configured retry limit is reached.
     * <p/>
     * The <code>run</code> method required by the Runnable interface is invoked
     * to perform the work.
     */
    private interface RetriableWork extends Runnable {

        /**
         * Returns whether the work to be done by this instance of RetriableWork
         * has been completed successfully.
         * <p/>
         * This method may be invoked multiple times and so should not have
         * side effects.
         *
         * @return whether the work has been successfully completed
         */
        boolean workComplete();
    }

    /**
     * Retriable work for renaming a file.
     */
    private static class RenameFileWork implements RetriableWork {

        private final File originalFile;
        private final File newFile;
        private boolean renameResult;

        public RenameFileWork(File originalFile, File newFile) {
            this.originalFile = originalFile;
            this.newFile = newFile;
        }

        @Override
        public boolean workComplete() {
            return renameResult;
        }

        @Override
        public void run() {
            renameResult = originalFile.renameTo(newFile);
        }
    }

    /**
     * Retriable work for opening a FileOutputStream.
     */
    private static class FileOutputStreamWork implements RetriableWork {

        private FileOutputStream fos;
        private Throwable lastError;
        private final File out;

        private FileOutputStreamWork(File out) {
            this.out = out;
        }

        @Override
        public boolean workComplete() {
            return fos != null;
        }

        @Override
        public void run() {
            try {
                fos = new FileOutputStream(out);
                lastError = null;
            } catch (IOException ioe) {
                lastError = ioe;
            }
        }

        public FileOutputStream getStream() {
            return fos;
        }

        public Throwable getLastError() {
            return lastError;
        }
    }

    /**
     * Retriable work for deleting a file
     */
    private static final class DeleteFileWork implements RetriableWork {

        private final File deleteMe;
        private boolean complete;

        private DeleteFileWork(File deleteMe) {
            this.deleteMe = deleteMe;
        }

        @Override
        public void run() {
            if (complete) {
                return;
            }
            if (deleteMe.delete()) {
                complete = true;
            }
        }

        @Override
        public boolean workComplete() {
            return complete;
        }
    }
}
