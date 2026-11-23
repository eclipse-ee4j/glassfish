/*
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.webui.jsf.model;

import com.sun.webui.jsf.component.FileChooser;
import com.sun.webui.jsf.util.FilterUtil;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

import java.io.File;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.Collator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.MissingResourceException;

// FIXME: Should be logging errors that don't have
// a way of bubbling up, like messages that don't accept arguments.
//
/**
 * File chooser model.
 */
public final class FileChooserModel implements ResourceModel, Serializable {

    /**
     * Serialization UID.
     */
    private static final long serialVersionUID = 7437797148169351743L;

    /**
     * Windows os name constant.
     */
    private static final String WINDOWS_OS = "window";

    /**
     * HTML encoded space.
     */
    private static final String SPACE = "&nbsp;";

    /**
     * Windows root.
     */
    private static final String WINDOWS_ROOT = "c:\\";

    /**
     * Unix root.
     */
    private static final String UNIX_ROOT = "/";

    /**
     * Default server hsot name.
     */
    private static final String DEFAULT_SERVER = "localhost";

    /**
     * File system root.
     */
    private String root;

    /**
     * File separator character.
     */
    private String separatorString = File.separator;

    /**
     * Current directory.
     */
    private String currentDir;

    /**
     * Filter value.
     */
    private String filterValue = "*";

    /**
     * Sort value.
     */
    private String sortValue;

    /**
     * Folder chooser flag.
     */
    private boolean folderChooser;

    /**
     * Type set flag.
     */
    private boolean typeSet;

    /**
     * Server name.
     */
    private String serverName;

    /**
     * Collator.
     */
    private transient Collator collator;

    /**
     * Creates a new instance of FileChooserModel.
     */
    public FileChooserModel() {
        if (isWindows()) {
            root = WINDOWS_ROOT;
        } else {
            root = UNIX_ROOT;
        }
        try {
            serverName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ukhe) {
            serverName = DEFAULT_SERVER;
        }
    }

    /**
     * Test if the current operating system is windows.
     *
     * @return {@code boolean}
     */
    private boolean isWindows() {
        String osName = System.getProperty("os.name").toUpperCase();
        return osName.startsWith(WINDOWS_OS.toUpperCase());
    }

    /**
     * Returns the root value of the file system in question. For example, in
     * the default implementation of this interface for local filesystems the
     * root value would be "/" in Unix and "C:\" on Windows.
     *
     * @return returns the absolute root (directory for files and folders).
     */
    @Override
    public String getAbsoluteRoot() {
        return this.root;
    }

    @Override
    public String[] getRoots() {

        File[] roots = File.listRoots();

        // In case we support just "\" as an absolute path on Windows
        // without the drive letter, add the separator as a root.
        //
        int len = roots.length;
        boolean isWindows = isWindows();
        if (isWindows) {
            ++len;
        }
        String[] strRoots = new String[len];
        for (int i = 0; i < roots.length; ++i) {
            strRoots[i] = roots[i].getPath();
        }
        if (isWindows) {
            strRoots[len] = File.separator;
        }
        return strRoots;
    }

    /**
     * Sets the root value of the resource system in question. For example, in
     * the default implementation of this interface for local file systems the
     * root value could be set to "/" in Unix and "C:\" on Windows.
     *
     * @param absRoot - the value to be used as the root of this resource system
     */
    @Override
    public void setAbsoluteRoot(final String absRoot) {
        if (absRoot != null) {
            this.root = absRoot;
        }
    }

    // These types of methods are not useful.
    // If any editing is required it must be part of the
    // interface like "dirname" and "basename"
    // However they may be needed for modification of
    // values on the client in javascript.
    //
    /**
     * Return the separator String for this resource system. For a file system
     * chooser this would be File.separator.
     *
     * @return returns the separator String.
     */
    @Override
    public String getSeparatorString() {
        return this.separatorString;
    }

    /**
     * Get the Server name from where the resources are being loaded.
     *
     * @return the server name
     */
    @Override
    public String getServerName() {
        return serverName;
    }

    /**
     * Set the server name from where the resources are being loaded.
     *
     * @param newServerName - the server name to be set
     */
    @Override
    public void setServerName(final String newServerName) {
        if (newServerName != null) {
            this.serverName = newServerName;
        }
    }

    /**
     * Return the filter String currently in use.
     *
     * @return returns the filter String.
     */
    @Override
    public String getFilterValue() {
        return this.filterValue;
    }

    /**
     * Set the filter String entered by the user in the Filter text field.
     *
     * @param newFilterValue - the filter string to be used subsequently.
     */
    @Override
    public void setFilterValue(final String newFilterValue) {
        if (newFilterValue != null) {
            validateFilterValue(newFilterValue);
            this.filterValue = newFilterValue;
        }
    }

    /**
     * Return the sort field that is currently active.
     *
     * @return returns the sort field in use.
     */
    @Override
    public String getSortValue() {
        return this.sortValue;
    }

    /**
     * Set the sort field chosen by the user from the drop down menu.
     *
     * @param newSortValue - string representing sortValue selected by the user.
     */
    @Override
    public void setSortValue(final String newSortValue) {
        if (newSortValue != null) {
            validateSortValue(newSortValue);
            this.sortValue = newSortValue;
        }
    }

    /**
     * This method is called to get the current directory of the resource list
     * being displayed in the file chooser's list box If the current directory
     * has not been set, root directory is returned by calling
     * {@code getAbsoluteRoot()}.
     *
     * @return returns the current directory or the root directory.
     */
    @Override
    public String getCurrentDir() {
        if (this.currentDir != null) {
            return this.currentDir;
        } else {
            return getAbsoluteRoot();
        }
    }

    /**
     * This method is called to set the current directory of the resource list
     * that would be displayed in the next display cycle.
     *
     * @param dir the value to be set the new current root node.
     */
    @Override
    public void setCurrentDir(final String dir) throws ResourceModelException {
        // How about returning the previous value ?
        // And why silence for null dir ? Use default ?
        if (dir != null && dir.length() > 0) {
            validateFolder(dir);
            this.currentDir = dir;
        }
    }

    /**
     * Returns the list of files in the directory represented by the
     * {@code folder} parameter. If {@code folder} is null the contents of
     * folder returned by {@code getCurrentDir} are returned. This method
     * returns an Array of ResourecItem objects
     *
     * @return returns the contents of folder or the current directory.
     */
    @Override
    public ResourceItem[] getFolderContent(final String folder,
            final boolean disableFiles, final boolean disableFolders) {

        FacesContext context = FacesContext.getCurrentInstance();
        ArrayList<FileChooserItem> optList = new ArrayList<FileChooserItem>();
        FileChooserItem[] fileEntries;
        FilterUtil filter = new FilterUtil(getFilterValue());
        boolean filesExist = false;

        String zFolder;
        if (folder == null) {
            zFolder = getCurrentDir();
        } else {
            zFolder = folder;
        }
        if (zFolder == null) {
            return null;
        }

        File file = new File(zFolder);
        File[] fileList = file.listFiles();
        if ((fileList == null) || (fileList.length == 0)) {
            return null;
        }

        // As per SWAED guidelines the list of folders should appear before the
        // list of files. Hence, we need to sort the dirs followed by the files
        // and then append the two arrays.
        // read each entry from the array and store it in one
        // of the two arraylists based on whether its a file or dir
        ArrayList<File> justFiles = new ArrayList<File>();
        ArrayList<File> justDirs = new ArrayList<File>();
        for (File fileList1 : fileList) {
            if (fileList1.isDirectory()) {
                justDirs.add(fileList1);
            } else {
                justFiles.add(fileList1);
            }
        }

        File[] fileArray = new File[justFiles.size()];
        for (int i = 0; i < justFiles.size(); i++) {
            fileArray[i] = (File) justFiles.get(i);

        }

        File[] dirArray = new File[justDirs.size()];
        for (int i = 0; i < justDirs.size(); i++) {
            dirArray[i] = (File) justDirs.get(i);
        }

        // sort the two files arrays
        sort(fileArray, getSortValue());
        sort(dirArray, getSortValue());

        // merge the two files
        int count = 0;
        for (; count < justDirs.size(); count++) {
            fileList[count] = dirArray[count];
        }
        System.arraycopy(fileArray, 0, fileList, count, justFiles.size());

        // end of code change to list of sorted dirs before files.
        for (File fileList1 : fileList) {
            String name = fileList1.getName();
            boolean bSelectable = false;
            boolean bIsDirectory = fileList1.isDirectory();
            boolean disabled = false;
            if (!bIsDirectory) {
                if (filter.accept(fileList1)) {
                    bSelectable = true;
                    // if folderchooser then files should look as
                    // if they are disabled.
                    if (disableFiles) {
                        disabled = true;
                    }
                } else {
                    continue;
                }
            } else {
                // all folders are selectable
                bSelectable = true;
            }
            FileChooserItem item = getItem(fileList1, context, disabled);
            optList.add(item);
            filesExist = true;
        }
        if (filesExist) {
            fileEntries = new FileChooserItem[optList.size()];
            for (int i = 0; i < optList.size(); i++) {
                fileEntries[i] = optList.get(i);
            }
            return fileEntries;
        } else {
            return null;
        }
    }

    /**
     * Given a ResourceItem key return the ResourceItem.
     *
     * @param itemKey the resource item key which is the same as the value of
     * the Option element in the list box.
     *
     * @return the ResourceItem object
     */
    @Override
    public ResourceItem getResourceItem(final String itemKey) {
        FacesContext context = FacesContext.getCurrentInstance();
        String resource;
        String[] strArray = itemKey.split("=");
        if (strArray == null) {
            return null;
        }
        if (strArray.length == 2) {
            resource = strArray[1];
        } else {
            resource = strArray[0];
        }
        File f = new File(resource);
        return getItem(f, context, false);
    }

    // FIXME: Don't need "Type" should be just "isFolder".
    /**
     * Returns true if the supplied absolute path is a folder type.
     *
     * @param path - the absolute path to the resource
     * @return returns the current root (directory for files and folders).
     */
    @Override
    public boolean isFolderType(final String path) {
        File f = new File(path);
        return f.isDirectory();
    }

    /**
     * This methods checks if the resource path in question can be accessed by
     * the user trying to select or view it.
     *
     * @param resourceName - the resource name to check for read access
     * @return true if the user can select the resource specified by the
     * resource name.
     */
    @Override
    public boolean canRead(final String resourceName) {

        File f = new File(resourceName);
        return f.canRead();

    }

    /**
     * This methods checks if the resource path in question can be accessed for
     * writes by the user.
     *
     * @param resourceName - the resource name to check for write access
     * @return true if the user can select the resource specified by the
     * resource name for write.
     *
     */
    @Override
    public boolean canWrite(final String resourceName) {
        File f = new File(resourceName);
        return f.canWrite();
    }

    /**
     * Create and return an Option object representing en entry for the list
     * box.
     *
     * @param file file
     * @param context faces context
     * @param disabled disabled flag
     * @return FileChooserItem
     */
    private static FileChooserItem getItem(final File file,
            final FacesContext context, final boolean disabled) {

        Theme theme = ThemeUtilities.getTheme(context);

        int fileNameLen = Integer.parseInt(
                theme.getMessage("filechooser.fileNameLen"));
        int fileSizeLen = Integer.parseInt(
                theme.getMessage("filechooser.fileSizeLen"));
        int fileDateLen = Integer.parseInt(
                theme.getMessage("filechooser.fileDateLen"));

        Locale locale = context.getViewRoot().getLocale();

        DateFormat dateFormat
                = SimpleDateFormat.getDateInstance(DateFormat.SHORT, locale);
        String defaultPattern
                = ((SimpleDateFormat) dateFormat).toLocalizedPattern();
        if (!defaultPattern.contains("yyyy")) {
            defaultPattern = defaultPattern.replaceFirst("yy", "yyyy");
        }
        if (!defaultPattern.contains("MM")) {
            defaultPattern = defaultPattern.replaceFirst("M", "MM");
        }
        if (!defaultPattern.contains("dd")) {
            defaultPattern = defaultPattern.replaceFirst("d", "dd");
        }

        try {
            defaultPattern = ThemeUtilities.getTheme(context)
                    .getMessage("filechooser.".concat(defaultPattern));
        } catch (MissingResourceException mre) {
            defaultPattern = "MM/dd/yyyy";
        }

        ((SimpleDateFormat) dateFormat).applyPattern(defaultPattern);

        SimpleDateFormat timeFormat = new SimpleDateFormat(
                theme.getMessage("filechooser.timeFormat"), locale);

        String name = file.getName();
        String value;
        if (file.isDirectory()) {
            name += File.separator;
            value = "folder" + "=" + file.getAbsolutePath();
        } else {
            value = "file" + "=" + file.getAbsolutePath();
        }
        name = getDisplayString(name, fileNameLen);
        String size = Long.toString(file.length());
        size = getDisplayString(size, fileSizeLen);
        Date modifiedDate = new Date(file.lastModified());
        String date = dateFormat.format(modifiedDate);
        String time = timeFormat.format(modifiedDate);
        StringBuilder buffer = new StringBuilder();
        buffer.append(name)
                .append(SPACE)
                .append(SPACE)
                .append(SPACE)
                .append(size)
                .append(SPACE)
                .append(SPACE)
                .append(date)
                .append(SPACE)
                .append(time);
        FileChooserItem item = new FileChooserItem(file);
        item.setItemKey(value);
        item.setItemLabel(buffer.toString());
        item.setItemDisabled(disabled);
        return item;
    }

    /**
     * This method returns the string of size maxLen by padding the appropriate
     * amount of spaces next to str.
     * @param str input string
     * @param maxLen maximum length
     * @return String
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private static String getDisplayString(final String str, final int maxLen) {

        String res = str;
        int length = res.length();
        if (length < maxLen) {
            int spaceCount = maxLen - length;
            for (int j = 0; j < spaceCount; j++) {
                res += SPACE;
            }
        } else if (length > maxLen) {
            int shownLen = maxLen - 3;
            res = res.substring(0, shownLen);
            res += "...";
        }
        return res;
    }

    /**
     * Sort rule interface.
     */
    private interface SortRule {

        /**
         * Compare two files.
         * @param f1 first file
         * @param f2 second file
         * @return boolean
         */
        boolean compare(File f1, File f2);
    }

    /**
     * This module sorts the files and directories in a given directory
     * according to the sort field selected by the user. By default the files
     * will be sorted alphabetically with all folders first, followed by all
     * files in alphabetical order. If the sort field is TIME the file will be
     * sorted in ascending order with the earliest modified file first. if the
     * sort field is SIZE, the files will be sorted by size with the smallest
     * first. The bubble sort algorithm is being used here.
     *
     * @param fileList array of files to be sorted
     * @param newSortValue the field to be sorted in
     */
    private void sort(final File[] fileList, final String newSortValue) {

        if ((fileList == null) || (fileList.length == 0)) {
            return;
        }

        if (fileList.length == 1) {
            return;
        }

        FacesContext context = FacesContext.getCurrentInstance();
        Locale locale = context.getViewRoot().getLocale();
        collator = Collator.getInstance(locale);
        collator.setStrength(Collator.SECONDARY);

        String zSortValue = newSortValue;
        if (zSortValue == null) {
            zSortValue = FileChooser.ALPHABETIC_ASC;
        }

        // check if the two files being compared are already
        // in the desired order
        SortRule sr = null;
        if (zSortValue.equals(FileChooser.ALPHABETIC_ASC)) {
            sr = new SortRule() {

                @Override
                public boolean compare(final File file1, final File file2) {
                    return (collator.compare(file1.getName(),
                            file2.getName()) >= 0);
                }
            };
        } else if (zSortValue.equals(FileChooser.ALPHABETIC_DSC)) {
            sr = new SortRule() {

                @Override
                public boolean compare(final File file1, final File file2) {
                    return (collator.compare(file2.getName(),
                            file1.getName()) >= 0);
                }
            };
        } else if (zSortValue.equals(FileChooser.SIZE_ASC)) {
            sr = new SortRule() {

                @Override
                public boolean compare(final File file1, final File file2) {
                    if (file1.length() == file2.length()) {
                        return (collator.compare(file1.getName(),
                                file2.getName()) >= 0);
                    } else {
                        return (file1.length() > file2.length());
                    }
                }
            };
        } else if (zSortValue.equals(FileChooser.SIZE_DSC)) {
            sr = new SortRule() {

                @Override
                public boolean compare(final File file1, final File file2) {
                    if (file1.length() == file2.length()) {
                        return (collator.compare(file1.getName(),
                                file2.getName()) >= 0);
                    } else {
                        return (file1.length() < file2.length());
                    }
                }
            };
        } else if (zSortValue.equals(FileChooser.LASTMODIFIED_ASC)) {
            sr = new SortRule() {

                @Override
                public boolean compare(final File file1, final File file2) {
                    if (file1.lastModified() == file2.lastModified()) {
                        return (collator.compare(file1.getName(),
                                file2.getName()) >= 0);
                    } else {
                        return (file1.lastModified() > file2.lastModified());
                    }
                }
            };
        } else if (zSortValue.equals(FileChooser.LASTMODIFIED_DSC)) {
            sr = new SortRule() {

                @Override
                public boolean compare(final File file1, final File file2) {
                    if (file1.lastModified() == file2.lastModified()) {
                        return (collator.compare(file1.getName(),
                                file2.getName()) >= 0);
                    } else {
                        return (file1.lastModified() < file2.lastModified());
                    }
                }
            };
        }

        // sort the file using bubble sort
        for (int i = fileList.length - 1; i >= 0; i--) {
            for (int j = 0; j < i; j++) {

                // if not swap them
                if (sr.compare(fileList[j], fileList[j + 1])) {
                    File tmp = fileList[j];
                    fileList[j] = fileList[j + 1];
                    fileList[j + 1] = tmp;
                }
            }
        }
    }

    // This was returning "getCurrentDir" on exception and if there
    // was no parent. This can result in an infinite loop
    // if this method is naviget up the hierarchy.
    // return null if there is no parent.
    /**
     * Return the parent folder of the value of {@code getCurrentDir}. If the
     * current directory does not have a parent null is returned.
     */
    @Override
    public String getParentFolder() {
        File parent;
        String parentDir = null;
        try {
            // Create a file object from the lookIn folder.
            parent = new File(getCurrentDir());
            parentDir = parent.getParent();
        } catch (Exception e) {
            // Does not have a parent, returns null.
        }
        return parentDir;
    }

    // This is really a function of platform. And configurable.
    @Override
    public String getEscapeChar() {
        if (getSeparatorString().equals("/")) {
            return "\\";
        }
        return "/";
    }

    @Override
    public String getDelimiterChar() {
        return ",";
    }

    // Currently this is the only validation done for folders.
    // Actually this should be mode detailed, like doesn't
    // exist, no permission, etc.
    /**
     * Validate the specified folder.
     * @param folder folder to validate
     * @throws ResourceModelException if an error occurs
     */
    private void validateFolder(final String folder)
            throws ResourceModelException {

        if (!canRead(folder)) {
            throw new ResourceModelException(
                    createFacesMessage("filechooser.cannotCompleteErrSum",
                            "filechooser.cannot_read_folder",
                            null, new String[]{folder}));
        }
    }

    /**
     * Validate the sort value.
     * @param newSortValue sort value to validate
     * @throws ResourceModelException if an error occurs
     */
    private void validateSortValue(final String newSortValue)
            throws ResourceModelException {

        // Currently no validation for the sort value.
    }

    /**
     * Validate the filter value.
     * @param newFilterValue filter value to validate
     * @throws ResourceModelException if an error occurs
     */
    private void validateFilterValue(final String newFilterValue)
            throws ResourceModelException {

        // Currently no validation for the filter value.
    }

    @Override
    public Object[] getSelectedContent(final String[] content,
            final boolean selectFolders) throws ResourceModelException {

        // Contents of Selected File textfield could be a list of files
        // separated by commas. Parse the list, prepend the folder name
        // if required and add them to the list of selected files.
        // For a single entry check if it is a file or a folder.
        // If folder, open this folder and enter folder name in
        // Look In field. If file, prepend folder name if required
        // and it to the selected files list.
        // Create the parent File object and pass it to the
        // appropriate method.
        File parent = null;
        try {
            parent = new File(getCurrentDir());
        } catch (Exception e) {
            // Convert to a ResourceModelException with the
            // cause.
            String detail;
            if (selectFolders) {
                detail = "filechooser.folderSelectError";
            } else {
                detail = "filechooser.fileSelectError";
            }
            throwException(e, "filechooser.cannotCompleteErrSum",
                    detail, null, null);
        }
        if (selectFolders) {
            return getSelectedFolders(parent, content);
        } else {
            return getSelectedFiles(parent, content);
        }
    }

    // There is an odd policy from the guidelines implementd here.
    // If there is one selection and it is a folder, relative or
    // absolute path, it is set as the current directory.
    // If it is a folder chooser, it also becomes the value.
    // If it is a file chooser, it is not accepted as a value,
    // just as a directory change.
    //
    /**
     * Return a File array of the selected folders from the parent directory.If
     * there are no selections, an empty array is returned.Typically the parent
     * is the current directory or the look in folder value and folders is
     * comprised of relative paths, relative to parent, but they can be absolute
     * paths.This method validates the folder entries. This means the
     * "File.canRead" method must return true and the entry must be a folder and
     * not a file.
     *
     * If there is one selection and it is a folder, relative or absolute path,
     * it is set as the current directory.
     *
     * ResourceModelException are thrown on error.
     *
     * @param parent directory
     * @param folders file names
     * @return File[]
     * @throws ResourceModelException if an error occurs
     */
    private static File[] getSelectedFolders(final File parent,
            final String[] folders) throws ResourceModelException {

        // If a selected folder is not an absolute path
        // it must be a child of the lookIn folder.
        // If it does not exist throw FileChooserModelExeption.
        // If it exists it must be a folder if it's not throw
        // FileChooserModelExeption.
        //
        ArrayList<File> folderArray = new ArrayList<File>();
        for (int i = 0; i < folders.length; ++i) {

            // For general exceptions
            try {
                File folder = new File(folders[i]);
                // If its not an absolute path it must be a child entry
                // of the parent.
                if (!folder.isAbsolute()) {
                    folder = new File(parent, folders[i]);
                }
                // It must exist
                // Currently we only have an error message for "canRead".
                if (!folder.canRead()) {
                    throw new ResourceModelException(
                            createFacesMessage(
                                    "filechooser.cannotCompleteErrSum",
                                    "filechooser.cannot_read_selected_folder",
                                    null, new String[]{folders[i]}));
                }
                // It must be a folder
                // FIXME: This error should include an argument to let the
                // user know which choice was a file.
                if (!folder.isDirectory()) {
                    throw new ResourceModelException(
                            createFacesMessage(
                                    "filechooser.cannotCompleteErrSum",
                                    "filechooser.cannotSelectFile",
                                    null, null));
                }
                folderArray.add(folder);

            } catch (ResourceModelException fcme) {
                // Just pass it on
                throw fcme;
            } catch (Exception e) {
                // Convert to a ResourceModelException with the
                // cause.
                throwException(e, "filechooser.cannotCompleteErrSum",
                        "filechooser.fileSelectError",
                        null, null);
            }
        }
        return (File[]) folderArray.toArray(new File[0]);
    }

    /**
     * Return a File array of the selected files from the parent directory.If
     * there are no selections, an empty array is returned.Typically the parent
     * is the current directory or the look in folder value and files is
     * comprised of relative paths, relative to parent, but they can be absolute
     * paths.This method validates the file entries. This is currently that the
     * "File.canRead" method must return true and the entry must be a file and
     * not a folder, with one exception.
     *
     * If there is one selection and it is a folder, relative or absolute path,
     * it is set as the current directory and an empty File[] is returned.
     *
     * ResourceModelException are thrown on error.
     *
     * @param parent directory
     * @param files file names
     * @return File[]
     */
    private File[] getSelectedFiles(final File parent, final String[] files) {

        // If a selected file is not an absolute path
        // If it is an absolute path, capture the first
        // selection that is a full path and record the
        // parent directory. When complete set the current directory
        // to that folder.
        // it must be a child of the lookIn folder.
        // If it does not exist throw FileChooserModelExeption.
        // If it exists it must be a file, if it's not a file
        // throw FileChooserModelExeption
        String newCurrentDir = null;
        ArrayList<File> filesArray = new ArrayList<File>();
        for (int i = 0; i < files.length; ++i) {

            // For General Exception catching
            try {
                File file = new File(files[i]);
                // If it's not an absolute path it must be a child entry
                // of the parent.
                if (!file.isAbsolute()) {
                    file = new File(parent, files[i]);
                } else {
                    if (newCurrentDir == null) {
                        newCurrentDir = file.getParent();
                    }
                }
                // It must exist
                // Currently we only have an error message for "canRead".
                if (!file.canRead()) {
                    throw new ResourceModelException(
                            createFacesMessage(
                                    "filechooser.cannotCompleteErrSum",
                                    "filechooser.cannot_read_selected_file",
                                    null, new String[]{files[i]}));
                }
                // It must be a file
                // FIXME: This error should include an argument to let the
                // user know which choice was a folder.
                if (file.isDirectory()) {
                    // Special case.
                    // If there is only one selection and it is a
                    // directory, use it to set the current directory
                    // and do not return it as a value.
                    //
                    // This may have set newCurrentDir
                    newCurrentDir = null;
                    if (files.length == 1) {
                        setCurrentDir(file.getPath());
                        break;
                    }
                    throw new ResourceModelException(
                            createFacesMessage(
                                    "filechooser.cannotCompleteErrSum",
                                    "filechooser.cannotSelectFolder",
                                    null, null));
                }

                filesArray.add(file);

            } catch (ResourceModelException fcme) {
                // Just pass it on
                throw fcme;
            } catch (Exception e) {
                // Convert to a ResourceModelException with the
                // cause.
                throwException(e, "filechooser.cannotCompleteErrSum",
                        "filechooser.folderSelectError",
                        null, null);
            }
        }
        // If we have a new parent dir set it
        if (newCurrentDir != null) {
            setCurrentDir(newCurrentDir);
        }
        return filesArray.toArray(new File[0]);
    }

    /**
     * Throw an exception.
     * @param e exception to wrap
     * @param summary summary message key
     * @param detail detail message key
     * @param summaryArgs summary message arguments
     * @param detailArgs detail message arguments
     * @throws ResourceModelException if an error occurs
     */
    private static void throwException(final Exception e, final String summary,
            final String detail, final String[] summaryArgs,
            final String[] detailArgs) throws ResourceModelException {

        FacesMessage fmsg = createFacesMessage(summary, detail, summaryArgs,
                detailArgs);
        Throwable cause = e.getCause();
        if (cause != null) {
            throw new ResourceModelException(fmsg, cause);
        } else {
            throw new ResourceModelException(fmsg);
        }
    }

    /**
     * This method creates a FacesMessage.
     * @param summary summary message key
     * @param detail detail message key
     * @param summaryArgs summary message arguments
     * @param detailArgs detail message arguments
     * @return FacesMessage
     */
    private static FacesMessage createFacesMessage(final String summary,
            final String detail, final String[] summaryArgs,
            final String[] detailArgs) {

        FacesContext context = FacesContext.getCurrentInstance();
        Theme theme = ThemeUtilities.getTheme(context);
        String summaryMsg = theme.getMessage(summary, summaryArgs);
        String detailMsg = theme.getMessage(detail, detailArgs);
        FacesMessage fmsg = new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                summaryMsg, detailMsg);
        return fmsg;
    }
}
