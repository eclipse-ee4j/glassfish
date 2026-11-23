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
package com.sun.webui.jsf.component;

import com.sun.faces.annotation.Component;
import com.sun.faces.annotation.Property;
import com.sun.webui.jsf.model.UploadedFile;
import com.sun.webui.jsf.util.LogUtil;
import com.sun.webui.jsf.util.ThemeUtilities;

import jakarta.el.ValueExpression;
import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;

import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;

import org.apache.commons.fileupload2.core.FileItem;

/**
 * The Upload component is used to create an input tag with its
 * {@code type} field set to "file".
 */
@Component(type = "com.sun.webui.jsf.Upload",
        family = "com.sun.webui.jsf.Upload",
        displayName = "File Upload",
        instanceName = "fileUpload",
        tagName = "upload",
        isContainer = false,
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_file_upload",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_upload_props")
        //CHECKSTYLE:ON
public final class Upload extends Field implements Serializable {

    /**
     * Serialization UID.
     */
    private static final long serialVersionUID = -8352221221756513893L;

    /**
     * A string concatenated with the component ID to form the ID and name of
     * the HTML input element.
     */
    public static final String INPUT_ID = "_com.sun.webui.jsf.upload";

    /**
     * Input parameter id.
     */
    public static final String INPUT_PARAM_ID =
            "_com.sun.webui.jsf.uploadParam";

    /**
     * Script id.
     */
    public static final String SCRIPT_ID = "_script";

    /**
     * Facet script.
     */
    public static final String SCRIPT_FACET = "script";

    /**
     * Text id.
     */
    public static final String TEXT_ID = "_text";

    /**
     * Length exceeded.
     */
    public static final String LENGTH_EXCEEDED = "length_exceeded";

    /**
     * Upload error key.
     */
    public static final String UPLOAD_ERROR_KEY = "upload_error_key";

    /**
     * File size key.
     */
    public static final String FILE_SIZE_KEY = "file_size_key";

    /**
     * Debug flag.
     */
    private static final boolean DEBUG = false;

    /**
     * Number of character character columns used to render this field. The
     * default is 40.
     */
    @Property(name = "columns",
            displayName = "Columns",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    private int columns = Integer.MIN_VALUE;

    /**
     * columns set flag.
     */
    private boolean columnsSet;

    /**
     * Default constructor.
     */
    public Upload() {
        super();
        setRendererType("com.sun.webui.jsf.Upload");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.Upload";
    }

    @Override
    protected void log(final String s) {
        LogUtil.finest(this.getClass().getName() + "::" + s);
    }

    /**
     * Converts the submitted value. Returns an object of type UploadedFile.
     *
     * @param context The FacesContext
     * @param value An object representing the submitted value
     * @return An Object representation of the value (a java.lang.String or a
     * {@code java.io.File}, depending on how the component is configured
     */
    @Override
    public Object getConvertedValue(final FacesContext context,
            final Object value) {

        if (DEBUG) {
            log("getConvertedValue");
        }
        UploadedFileImpl uf = new UploadedFileImpl(value, context);
        if (DEBUG) {
            log("\tSize is " + String.valueOf(uf.getSize()));
            log("\tName is " + uf.getOriginalName());
            log("\tValue is required " + String.valueOf(isRequired()));
        }
        if (isRequired() && uf.getSize() == 0) {
            String name = uf.getOriginalName();
            if (name == null || name.trim().length() == 0) {
                if (DEBUG) {
                    log("No file specified");
                }
                setValue("");
                if (DEBUG) {
                    log("Set value to empty string");
                }
                return "";
            }
        }
        return uf;
    }

    /**
     * Return the value to be rendered when the component is rendered as a
     * String. For the FileUpload, we never render the file name in the
     * text field, so we return null.
     *
     * @param context FacesContext for the current request
     * @return A String value of the component
     */
    @Override
    public String getValueAsString(final FacesContext context) {
        return null;
    }

    /**
     * Return the value to be rendered as a string when the component is
     * readOnly. This method overrides the default behavior by returning a
     * String "No file uploaded" if getValueAsString() returns null.
     *
     * @param context FacesContext for the current request
     * @return A String value of the component
     */
    @Override
    public String getReadOnlyValueString(final FacesContext context) {

        String valueString = null;
        Object value = getValue();
        if (value != null & value instanceof UploadedFile) {
            try {
                valueString = ((UploadedFile) value).getOriginalName();
            } catch (Exception ex) {
                // We have somehow lost the underlying file representation.
                // We do nothing in this case.
            }
        }
        if (valueString == null) {
            valueString = ThemeUtilities.getTheme(context)
                    .getMessage("FileUpload.noFile");
        }
        return valueString;
    }

    /**
     * Overrides getType in the FileInput class, to always return "file".
     *
     * @return "file"
     */
    public String getType() {
        return "file";
    }

    @Override
    public void setText(final Object text) {
        // do nothing
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public int getColumns() {
        int cols;
        if (this.columnsSet) {
            cols = this.columns;
        } else {
            ValueExpression vb = getValueExpression("columns");
            if (vb != null) {
                Object result = vb.getValue(getFacesContext().getELContext());
                if (result == null) {
                    cols = Integer.MIN_VALUE;
                } else {
                    cols = ((Integer) result);
                }
            } else {
                cols = 40;
            }
        }
        if (cols < 1) {
            cols = 40;
            setColumns(cols);
        }
        return cols;
    }

    // Overrides the method in Field.java as a workaround for an
    // apparent compiler problem (?). The renderer (for Upload as well
    // as TextField etc) casts the component to Field. It invokes
    // Field.getPrimaryElementID, and even though this.getClass()
    // returns Upload, this.INPUT_ID returns Field.INPUT_ID and
    // not Upload.INPUT_ID.
    /**
     * Retrieves the DOM ID for the HTML input element. To be used by Label
     * component as a value for the "for" attribute.
     *
     * @deprecated
     * @see #getLabeledElementId
     * @param context faces context
     * @return String
     */
    @Override
    public String getPrimaryElementID(final FacesContext context) {
        String clntId = this.getClientId(context);
        UIComponent labelComp = getLabelComponent(context, null);
        if (labelComp == null) {
            return clntId;
        } else {
            return clntId.concat(INPUT_ID);
        }
    }

    // Overrides the method in Field.java as a workaround for an
    // apparent compiler problem (?). The renderer (for Upload as well
    // as TextField etc) casts the component to Field. It invokes
    // Field.getPrimaryElementID, and even though this.getClass()
    // returns Upload, this.INPUT_ID returns Field.INPUT_ID and
    // not Upload.INPUT_ID.
    /**
     * Returns the ID of an HTML element suitable to use as the value of an HTML
     * LABEL element's {@code for} attribute.
     *
     * @param context The FacesContext used for the request
     * @return The id of the HTML element
     */
    @Override
    public String getLabeledElementId(final FacesContext context) {

        // If this component has a label either as a facet or
        // an attribute, return the id of the input element
        // that will have the "INPUT_ID" suffix. IF there is no
        // label, then the input element id will be the component's
        // client id.
        //
        // If it is read only then return null
        if (isReadOnly()) {
            return null;
        }

        // To ensure we get the right answer call getLabelComponent.
        // This checks for a developer facet or the private label facet.
        // It also checks the label attribute. This is better than
        // relying on "getLabeledComponent" having been called
        // like this method used to do.
        String clntId = this.getClientId(context);
        UIComponent labelComp = getLabelComponent(context, null);
        if (labelComp == null) {
            return clntId;
        } else {
            return clntId.concat(INPUT_ID);
        }
    }

    /**
     * Returns the id of an HTML element suitable to receive the focus.
     *
     * @param context The FacesContext used for the request
     * @return String
     */
    @Override
    public String getFocusElementId(final FacesContext context) {
        return getLabeledElementId(context);
    }

    /**
     * Flag indicating that an input value for this field is mandatory, and
     * failure to provide one will trigger a validation error.
     */
    @Property(name = "required")
    @Override
    public void setRequired(final boolean required) {
        super.setRequired(required);
    }

    @Override
    public ValueExpression getValueExpression(final String name) {
        if (name.equals("uploadedFile")) {
            return super.getValueExpression("value");
        }
        return super.getValueExpression(name);
    }

    @Override
    public void setValueExpression(final String name,
            final ValueExpression binding) {

        if (name.equals("uploadedFile")) {
            super.setValueExpression("value", binding);
            return;
        }
        super.setValueExpression(name, binding);
    }

    /**
     * The converter attribute is used to specify a method to translate native
     * property values to String and back for this component. The converter
     * attribute value must be one of the following:
     * <ul>
     * <li>a JavaServer Faces EL expression that resolves to a backing bean or
     * bean property that implements the
     * {@code jakarta.faces.converter.Converter} interface; or</li>
     * <li>the ID of a registered converter (a String).</li>
     * </ul>
     * @return Converter
     */
    @Property(name = "converter", isHidden = true, isAttribute = true)
    @Override
    public Converter getConverter() {
        return super.getConverter();
    }

    /**
     * The maximum number of characters that can be entered for this field.
     * @return int
     */
    @Property(name = "maxLength", isHidden = true, isAttribute = true)
    @Override
    public int getMaxLength() {
        return super.getMaxLength();
    }

    // Hide trim
    @Property(name = "trim", isHidden = true, isAttribute = false)
    @Override
    public boolean isTrim() {
        return super.isTrim();
    }

    // Hide text
    @Property(name = "text", isHidden = true, isAttribute = false)
    @Override
    public Object getText() {
        return null;
    }

    /**
     * Number of character character columns used to render this field. The
     * default is 40.
     *
     * @see #getColumns()
     */
    @Override
    public void setColumns(final int newColumns) {
        this.columns = newColumns;
        this.columnsSet = true;
    }

    /**
     * The value of this attribute must be a JSF EL expression, and it must
     * resolve to an object of type
     * {@code com.sun.webui.jsf.model.UploadedFile}. See the JavaDoc for
     * this class for details.
     * @return {@code UploadedFile}
     */
    @Property(name = "uploadedFile",
            displayName = "Uploaded File",
            category = "Data",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.binding.ValueBindingPropertyEditor")
            //CHECKSTYLE:ON
    public UploadedFile getUploadedFile() {
        return (com.sun.webui.jsf.model.UploadedFile) getValue();
    }

    /**
     * The value of this attribute must be a JSF EL expression, and it must
     * resolve to an object of type
     * {@code com.sun.webui.jsf.model.UploadedFile}. See the JavaDoc for
     * this class for details.
     *
     * @see #getUploadedFile()
     * @param uploadedFile uploadedFile
     */
    public void setUploadedFile(final UploadedFile uploadedFile) {
        setValue((Object) uploadedFile);
    }

    @Override
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.columns = ((Integer) values[1]);
        this.columnsSet = ((Boolean) values[2]);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[3];
        values[0] = super.saveState(context);
        values[1] = this.columns;
        if (this.columnsSet) {
            values[2] = Boolean.TRUE;
        } else {
            values[2] = Boolean.FALSE;
        }
        return values;
    }

    /**
     * Obtain the FileItem in the constructor, based on the arguments. The
     * original code attempted to get the object repeatedly from the request
     * parameters. But the instance may live longer that this current request
     * thereby potentially returning a different FileItem. Also there was
     * originally a transient attribute member. If the instance was serialized
     * and restored the attribute member would be null causing the exception to
     * be thrown. Originally the assumption was that the attribute member would
     * never be null, otherwise getConvertedValue would not have been called and
     * an instance of this class would not have been created.
     *
     * In addition, creator made changes that allows the FileItem to not exist
     * in the request map. This means that the instance returned does not
     * represent a valid FileItem. Originally this would have thrown an
     * exception. But with the creator changes an exception is only thrown if
     * the object found in the request map is not a FileItem object, and null is
     * acceptable.
     *
     * This behavior may be different since the application will receive an
     * instance of this class when there is no file when previously an exception
     * would have been thrown.
     */
    private static final class UploadedFileImpl implements UploadedFile {

        /**
         * Serialization UID.
         */
        private static final long serialVersionUID = -8806211528277303445L;

        /**
         * File item object.
         */
        private transient FileItem<?> fileItemObject;

        /**
         * Creates a new instance of UploadedFileImpl.
         * @param attribute request attributes
         * @param context faces context
         */
        UploadedFileImpl(final Object attribute, final FacesContext context) {
            // Allow null
            try {
                this.fileItemObject = (FileItem<?>) context
                        .getExternalContext()
                        .getRequestMap()
                        .get(attribute);
            } catch (Exception e) {
                throw new FacesException("File not uploaded."
                        + " Is the upload filter installed ?", e);
            }
        }

        @Override
        public void write(final java.io.File file) throws Exception {
            if (fileItemObject != null) {
                fileItemObject.write(file.toPath());
            }
        }

        @Override
        public long getSize() {
            if (fileItemObject != null) {
                return fileItemObject.getSize();
            } else {
                return 0;
            }
        }

        @Override
        public String getOriginalName() {
            if (fileItemObject != null) {
                return fileItemObject.getName();
            } else {
                return null;
            }
        }

        @Override
        public java.io.InputStream getInputStream() throws java.io.IOException {
            if (fileItemObject != null) {
                return fileItemObject.getInputStream();
            } else {
                return null;
            }
        }

        @Override
        public String getContentType() {
            if (fileItemObject != null) {
                return fileItemObject.getContentType();
            } else {
                return null;
            }
        }

        @Override
        public byte[] getBytes() {
            if (fileItemObject != null) {
                try {
                    return fileItemObject.get();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            } else {
                return null;
            }
        }

        @Override
        public String getAsString() {
            if (fileItemObject != null) {
                try {
                    return fileItemObject.getString();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            } else {
                return null;
            }
        }

        @Override
        public void dispose() {
            if (fileItemObject != null) {
                try {
                    fileItemObject.delete();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }
    }
}
