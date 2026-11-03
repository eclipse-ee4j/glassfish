/*
 * Copyright (c) 2007, 2019 Oracle and/or its affiliates. All rights reserved.
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

/**
 * Resource model.
 */
public interface ResourceModel {

    /**
     * Returns the root value of the file system in question. For example, in
     * the default implementation of this interface for local file systems the
     * root value would be "/" in Unix and "C:\" on Windows.
     *
     * @return returns the absolute root (directory for files and folders).
     */
    String getAbsoluteRoot();

    /**
     * Sets the root value of the resource system in question. For example, in
     * the default implementation of this interface for local file systems the
     * root value could be set to "/" in Unix and "C:\" on Windows.
     *
     * @param absRoot - the value to be used as the root of this resource system
     */
    void setAbsoluteRoot(String absRoot);

    /**
     * Return the separator String for this resource system. For a file system
     * chooser this would be File.separator.
     *
     * @return returns the separator String.
     */
    String getSeparatorString();

    /**
     * Get the Server name from where the resources are being loaded.
     *
     * @return the server name
     */
    String getServerName();

    /**
     * Set the server name from where the resources are being loaded.
     *
     * @param serverName - the server name to be set
     */
    void setServerName(String serverName);

    /**
     * Return the filter String currently in use.
     *
     * @return returns the filter String.
     */
    String getFilterValue();

    /**
     * Set the filter String entered by the user in the Filter text field.
     *
     * @param filterString - the filter string to be used subsequently.
     */
    void setFilterValue(String filterString);

    /**
     * Return the sort field that is currently active.
     *
     * @return returns the sort field in use.
     */
    String getSortValue();

    /**
     * Set the sort field chosen by the user from the drop down menu.
     *
     * @param sortField - string representing sortField selected by the user.
     *
     */
    void setSortValue(String sortField);

    /**
     * This method is called to get the current directory of the resource list
     * being displayed in the file chooser list-box.
     *
     * @return returns the current root (directory for files and folders).
     */
    String getCurrentDir();

    /**
     * This method is called to set the current directory of the resource list
     * that would be displayed in the next display cycle.
     *
     * @param dir - the value to be set the new current root node.
     */
    void setCurrentDir(String dir);

    /**
     * Returns the list of objects in the container represented by the current
     * directory.This method returns an Array of ResourecItem objects.
     *
     * @param folder folder
     * @param disableFiles flag
     * @param disableFolders flag
     * @return ResourceItem[]
     */
    ResourceItem[] getFolderContent(String folder,
            boolean disableFiles, boolean disableFolders);

    /**
     * Given a ResourceItem key return the ResourceItem.
     *
     * @param itemKey the resource item key which is the same as the value of
     * the Option element in the list-box.
     * @return the ResourceItem object
     */
    ResourceItem getResourceItem(String itemKey);

    /**
     * Returns true if the supplied absolute path is a folder type.
     *
     * @param path the absolute path to the resource
     * @return returns the current root (directory for files and folders).
     */
    boolean isFolderType(String path);

    /**
     * This methods checks if the resource path in question can be accessed by
     * the user trying to select or view it.
     *
     * @param resourceName - the resource name to check for read access
     * @return true if the user can select the resource specified by the
     * resource name.
     */
    boolean canRead(String resourceName);

    /**
     * This methods checks if the resource path in question can be accessed for
     * writes by the user.
     *
     * @param resourceName - the resource name to check for write access
     * @return true if the user can select the resource specified by the
     * resource name for write.
     */
    boolean canWrite(String resourceName);

    /**
     * Get the parent folder.
     *
     * @return String
     */
    String getParentFolder();

    /**
     * Get the escape character.
     *
     * @return String
     */
    String getEscapeChar();

    /**
     * Get the delimiter character.
     *
     * @return String
     */
    String getDelimiterChar();

    /**
     * Get the selected content.
     *
     * @param content content
     * @param selectFolders flag
     * @return Object[]
     * @throws ResourceModelException if an error occurs
     */
    Object[] getSelectedContent(String[] content, boolean selectFolders)
            throws ResourceModelException;

    /**
     * Get the file system roots.
     *
     * @return String[]
     */
    String[] getRoots();
}
