/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2020 Payara Services Ltd.
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
package com.sun.webui.jsf.util;

import com.sun.webui.jsf.component.Upload;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.fileupload2.core.DiskFileItem;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.fileupload2.core.FileUploadException;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletDiskFileUpload;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletFileUpload;

/**
 * <p>
 * Use the UploadFilter if your application contains an Upload component
 * (&lt;ui:upload&gt; tag).</p>
 * <p>
 * Configure the filter by declaring the filter element in the web application's
 * deployment descriptor.</p>
 * <pre>
 * &lt;filter&gt;
 * &lt;filter-name&gt;UploadFilter&lt;/filter-name&gt;
 * &lt;filter-class&gt;com.sun.webui.jsf.util.UploadFilter&lt;/filter-class&gt;
 * &lt;/filter&gt;
 * </pre>
 * <p>
 * Map the filter to the FacesServlet, for example</p>
 * <pre>
 * &lt;filter-mapping&gt;
 * &lt;filter-name&gt;UploadFilter&lt;/filter-name&gt;
 * &lt;servlet-name&gt;FacesServlet&lt;/servlet-name&gt;
 * &lt;/filter-mapping&gt;
 * </pre>
 * <p>
 * The UploadFilter uses the Apache commons fileupload package. You can
 * configure the parameters of the DiskFileUpload class by specifying init
 * parameters to the Filter. The following parameters are available:
 * <ul>
 * <li>{@code maxSize} The maximum allowed upload size in bytes. If
 * negative, there is no maximum. The default value is 1,000,000.</li>
 *
 * <li>{@code sizeThreshold}The implementation of the uploading
 * functionality uses temporary storage of the file contents before the Upload
 * component stores them per its configuration. In the temporary storage,
 * smaller files are stored in memory while larger files are written directly to
 * disk . Use this parameter to specify an integer value of the cut-off where
 * files should be written to disk. The default value is 4096 bytes.</li>
 * <li>{@code tmpDir} Use this directory to specify the directory to be
 * used for temporary storage of files. The default behaviour is to use the
 * directory specified in the system property "java.io.tmpdir". </li>
 * </ul>  *
 */
public final class UploadFilter implements Filter {

    /**
     * The name of the filter init parameter used to specify the maximum
     * allowable file upload size.
     */
    public static final String MAX_SIZE = "maxSize";

    /**
     * The name of the filter init parameter used to specify the byte size above
     * which temporary storage of files is on disk.
     */
    public static final String SIZE_THRESHOLD = "sizeThreshold";

    /**
     * The name of the filter init parameter used to specify the directory to be
     * used for temporary storage of uploaded files.
     */
    public static final String TMP_DIR = "tmpDir";

    /**
     * Max upload size in byte.
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private long maxSize = 1000000;

    /**
     * Size threshold before caching to disk.
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private int sizeThreshold = 4096;

    /**
     * Temp directory.
     */
    private String tmpDir = System.getProperty("java.io.tmpdir");

    /**
     * Messages resource bundle id.
     */
    private static final String MESSAGES =
            "com.sun.webui.jsf.resources.LogMessages";

    /**
     * Debug flag.
     */
    private static final boolean DEBUG = false;

    /**
     * The upload filter checks if the incoming request has multipart content.
     * If it doesn't, the request is passed on as is to the next filter in the
     * chain. If it does, the filter processes the request for form components.
     * If it finds input from an Upload component, the file contents are stored
     * for access by the Upload component's decode method.
     *
     * For other form components, the input is processed and used to create a
     * request parameter map. The original incoming request is wrapped, and the
     * wrapped request is configured to use the created map. This means that
     * subsequent filters in the chain (and Servlets, and JSPs) see the input
     * from the other components as request parameters.
     *
     * For advanced users: the UploadFilter uses the Apache commons FileUpload
     * package to process the file upload. When it detects input from an Upload
     * component, a {@code org.apache.commons.fileupload2.core.DiskFileItem} is placed in
     * a request attribute whose name is the ID of the HTML input element
     * written by the Upload component.
     *
     * @param response The servlet response
     * @param request The servlet request we are processing
     * @param chain The filter chain we are processing
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    @SuppressWarnings("unchecked")
    @Override
    public void doFilter(final ServletRequest request,
            final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;

        if (JakartaServletFileUpload.isMultipartContent(req)) {

            JakartaServletDiskFileUpload fu = new JakartaServletDiskFileUpload(
                    DiskFileItemFactory.builder()
                            .setThreshold(sizeThreshold)
                            .setPath(tmpDir)
                            .get());
            // maximum size before a FileUploadException will be thrown
            // Store this in a context parameter perhaps?

            // Enforce the maxSize. File larger than the maxSize will not be
            // uploaded (FileUploadExcetpion will be thrown instead)
            // Note: do not set the maxSize to -1, which means no size
            // limitation is enforced.
            // It is a big security hole to allow any file to be uploaded
            fu.setMaxSize(maxSize);

            // files with names in other languages (like Japanese) are not
            // being uploaded with the proper names. Proper encoding has to
            // be set for this to happen.
            if (request.getCharacterEncoding() != null) {
                fu.setHeaderCharset(Charset.forName(request.getCharacterEncoding()));
            } else {
                fu.setHeaderCharset(StandardCharsets.UTF_8);
            }
            List<DiskFileItem> fileItems = null;
            HashMap<String, String[]> parameters;
            try {
                fileItems = fu.parseRequest(req);
            } catch (FileUploadException fue) {
                request.setAttribute(Upload.UPLOAD_ERROR_KEY, fue);
                request.setAttribute(Upload.FILE_SIZE_KEY,
                        String.valueOf(maxSize));
            }

            if (fileItems != null) {
                parameters = parseRequest(fileItems, req);
            } else {
                parameters = new HashMap<String, String[]>();
            }

            // Need to add the parameters from the original request
            // into parameters
//
            Enumeration<String> names = request.getParameterNames();
            while (names.hasMoreElements()) {
                String param = names.nextElement();
                String paramValue = request.getParameter(param);
                if (!parameters.containsKey(param)) {
                    parameters.put(param, new String[]{paramValue});
                } else {
                    String[] origParamValue = parameters.get(param);
                    List<String> paramList = Arrays.asList(origParamValue);
                    paramList.add(paramValue);
                    String[] newParamValue = (String[]) paramList.toArray(
                            new String[0]);
                    parameters.put(param, newParamValue);
                }
            }

            UploadRequest wrappedRequest = new UploadRequest(req, parameters);
            chain.doFilter(wrappedRequest, response);

            Enumeration e = request.getAttributeNames();
            while (e.hasMoreElements()) {
                Object o = request.getAttribute(e.nextElement().toString());
                if (o instanceof DiskFileItem) {
                    ((DiskFileItem) o).delete();
                }
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    /**
     * Parse a request.
     * @param fileItems file items
     * @param request incoming request
     * @return Map
     * @throws IOException if a form field value cannot be read
     */
    private HashMap<String, String[]> parseRequest(
            final List<DiskFileItem> fileItems, final HttpServletRequest request)
            throws IOException {

        if (DEBUG) {
            log("parseRequest()");
        }

        // Iterate over the FileItems to see if any of them correspond
        // to the ID of an input type="file" that is rendered inside a
        // a span. This happens if the component has a label attribute or
        // a label facet, and in this case, the DOM id of the input
        // element is created by the component in such a way that we
        // can match it directly. If there is a match, a request attribute
        // is added for the corresponding FileItem . If there is no match,
        // add the fileItem to a map, with the fieldID as the key.
        Iterator<DiskFileItem> fileItemsIt = fileItems.iterator();
        String fieldID;
        DiskFileItem fileItem;
        Map<String, List<DiskFileItem>> fileItemsMap =
                new TreeMap<String, List<DiskFileItem>>();

        if (DEBUG) {
            log("\tFirst pass through the parameters which are");
        }
        while (fileItemsIt.hasNext()) {
            fileItem = fileItemsIt.next();
            fieldID = fileItem.getFieldName();
            if (DEBUG) {
                log("\t\t" + fieldID);
            }
            if (fieldID.endsWith(Upload.INPUT_ID)) {
                request.setAttribute(fieldID, fileItem);
                if (DEBUG) {
                    log(fieldID + " added to the request parameters");
                }
            } else {
                // Need to store the value as an ArrayList because some
                // fieldID has multiple values, for exampe, the selected
                // values for the add remove component (see CR6504432)
                List<DiskFileItem> valueList;
                if (!fileItemsMap.containsKey(fieldID)) {
                    valueList = new ArrayList<DiskFileItem>();
                } else {
                    valueList = fileItemsMap.get(fieldID);
                }
                valueList.add(fileItem);
                fileItemsMap.put(fieldID, valueList);
            }
        }

        // Iterate over the FileItems to see if any of them correspond
        // to a hidden field used to identify a FileUpload which does
        // not have an associated label. (In that case, the ID of the
        // input element is the same of the component ID, so we can't
        // tell by looking at the ID directly.) If so, we take the value
        // of the hidden ID, which is the ID of the input component and
        // get the corresponding file item. Both the ID of the hidden
        // component and that of the input itself are added to the
        // parameters to be ignored.
        if (DEBUG) {
            log("\tSecond pass through the parameters");
        }
        ArrayList<String> removes = new ArrayList<String>();
        ArrayList<String> unlabeledUploads = new ArrayList<String>();
        Iterator<String> fileItemsIditerator = fileItemsMap.keySet().iterator();
        String param;
        while (fileItemsIditerator.hasNext()) {
            fieldID = fileItemsIditerator.next();
            if (fieldID.endsWith(Upload.INPUT_PARAM_ID)) {
                fileItem = fileItemsMap.get(fieldID).get(0);
                param = fileItem.getString();
                unlabeledUploads.add(param);
                removes.add(fieldID);
                if (DEBUG) {
                    log("\tFound other fileUpload for parameter " + param);
                }
            }
        }

        // If we found IDs of any unlabeled Uploads, we create request
        // attributes for them too, and add their IDs to the list of IDs
        // to remove.
        if (!unlabeledUploads.isEmpty()) {
            if (DEBUG) {
                log("\tFound unlabeledUploads ");
            }
            Iterator<String> paramsIterator = unlabeledUploads.iterator();
            while (paramsIterator.hasNext()) {
                fieldID = paramsIterator.next();
                if (fileItemsMap.get(fieldID) != null) {
                    fileItem = fileItemsMap.get(fieldID).get(0);
                    request.setAttribute(fieldID.concat(Upload.INPUT_ID),
                            fileItem);
                    removes.add(fieldID);
                    if (DEBUG) {
                        log("\tAdd FileItem for "
                                + fieldID.concat(Upload.INPUT_ID));
                    }
                }
            }
        }

        // If we have any fields to be removed from the parameter map,
        // we do so
        if (!removes.isEmpty()) {
            Iterator<String> removesIterator = removes.iterator();
            while (removesIterator.hasNext()) {
                fileItemsMap.remove(removesIterator.next());
            }
        }

        // Create a hashtable to use for the parameters, and add the remaining
        // fileItems
        HashMap<String, String[]> parameters = new HashMap<String, String[]>();
        fileItemsIditerator = fileItemsMap.keySet().iterator();
        String id;
        if (DEBUG) {
            log("\tFinally, create regular parameters for ");
        }
        while (fileItemsIditerator.hasNext()) {
            id = fileItemsIditerator.next();

            List<DiskFileItem> valueList = fileItemsMap.get(id);
            if (valueList.size() == 1) {
                parameters.put(id, new String[]{
                    valueList.get(0).getString()
                });
                if (DEBUG) {
                    log("\t\t " + id + ":"
                            + valueList.get(0).getString());
                }
            } else {
                String[] params = new String[valueList.size()];
                for (int i = 0; i < valueList.size(); i++) {
                    params[i] = valueList.get(i).getString();
                }

                parameters.put(id, params);
                if (DEBUG) {
                    log("\t\t " + id + ":" + Arrays.toString(params));
                }
            }
        }
        return parameters;
    }

    @Override
    public void init(final FilterConfig filterConfig) {
        StringBuilder errorMessageBuffer = new StringBuilder();
        String param = filterConfig.getInitParameter(MAX_SIZE);
        if (param != null) {
            try {
                maxSize = Long.parseLong(param);
            } catch (NumberFormatException nfe) {
                Object[] params = {MAX_SIZE, param};
                String msg = MessageUtil.getMessage(MESSAGES,
                        "Upload.invalidLong", params);
                errorMessageBuffer.append(msg);
            }
        }
        param = filterConfig.getInitParameter(SIZE_THRESHOLD);
        if (param != null) {
            try {
                sizeThreshold = Integer.parseInt(param);
            } catch (NumberFormatException nfe) {
                Object[] params = {SIZE_THRESHOLD, param};
                errorMessageBuffer.append(" ");
                String msg = MessageUtil.getMessage(MESSAGES,
                        "Upload.invalidInt", params);
                errorMessageBuffer.append(msg);
            }
        }
        param = filterConfig.getInitParameter(TMP_DIR);
        if (param != null) {
            tmpDir = param;
            File dir = new File(tmpDir);
            if (!dir.canWrite()) {
                Object[] params = {TMP_DIR, param};
                errorMessageBuffer.append(" ");
                String msg = MessageUtil.getMessage(MESSAGES,
                        "Upload.invalidDir",
                        params);
                errorMessageBuffer.append(msg);
            }
        }
        String error = errorMessageBuffer.toString();
        if (error.length() > 0) {
            throw new RuntimeException(error);
        }
    }

    @Override
    public String toString() {
        return (this.getClass().getName());
    }

    @Override
    public void destroy() {
        // do nothing
    }

    /**
     * Log a message to the standard output.
     * @param msg message to log
     */
    private static void log(final String msg) {
        LogUtil.finest(UploadFilter.class.getName() + "::" + msg);
    }

    /**
     * This request wrapper class extends the support class
     * HttpServletRequestWrapper, which implements all the methods in the
     * HttpServletRequest interface, as delegations to the wrapped request. You
     * only need to override the methods that you need to change. You can get
     * access to the wrapped request using the method getRequest()
     */
    private static final class UploadRequest extends HttpServletRequestWrapper {

        /**
         * Request parameters.
         */
        private final Map<String, String[]> parameters;

        /**
         * Create a new instance.
         * @param request servlet request
         * @param params request parameters
         */
        UploadRequest(final HttpServletRequest request,
                final Map<String, String[]> params) {

            super(request);
            this.parameters = params;
        }

        @Override
        public String getParameter(final String name) {
            Object param = parameters.get(name);
            if (param instanceof String) {
                return (String) param;
            }
            if (param instanceof String[]) {
                String[] params = (String[]) param;
                return params[0];
            }
            if (param == null) {
                return null;
            }
            return param.toString();
        }

        @Override
        public String[] getParameterValues(final String name) {

            Object value = parameters.get(name);

            // If name does not exist return null.
            if (value == null) {
                return null;
            }
            // If a String array return it
            if (value instanceof String[]) {
                return (String[]) value;
            } else { // Must be one big String
                return new String[]{value.toString()};
            }
        }

        @Override
        public Enumeration<String> getParameterNames() {
            return Collections.enumeration(parameters.keySet());
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return parameters;
        }
    }
}
