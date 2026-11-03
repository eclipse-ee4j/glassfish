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
package com.sun.webui.jsf.component;

import com.sun.faces.annotation.Component;
import com.sun.faces.annotation.Property;

import jakarta.el.ValueExpression;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.context.FacesContext;

/**
 * The Link component is used to insert information into the &lt;head&gt;
 * element, such as links to external style sheets.
 */
@Component(type = "com.sun.webui.jsf.Link",
        family = "com.sun.webui.jsf.Link",
        displayName = "Link", tagName = "link",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_link",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_link_props")
        //CHECKSTYLE:ON
public final class Link extends UIComponentBase {

    /**
     * Defines the character (charset) encoding of the target URL. Default value
     * is {@code "ISO-8859-1"}.
     */
    @Property(name = "charset",
            displayName = "Charset",
            category = "Advanced",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.CharacterSetsEditor")
            //CHECKSTYLE:ON
    private String charset = null;

    /**
     * Specifies the type of display device for which the referenced document is
     * designed. The media attribute is useful for specifying different style
     * sheets for print and viewing on a screen. The default value is "screen".
     */
    @Property(name = "media",
            displayName = "Media Type",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String media = null;

    /**
     * Defines the relationship between the current document and the targeted
     * document. Default is {@code "stylesheet"}. Other possible values are
     * described at w3.org.
     */
    @Property(name = "rel",
            displayName = "Rel",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.HtmlLinkTypesEditor")
            //CHECKSTYLE:ON
    private String rel = null;

    /**
     * Specifies the MIME type of the target URL. Default is: {@code "text/css"}
     */
    @Property(name = "type",
            displayName = "Mime type",
            category = "Appearance")
    private String type = null;

    /**
     * The absolute or relative target URL of the resource.
     */
    @Property(name = "url",
            displayName = "URL",
            category = "Appearance",
            isDefault = true,
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.SunWebUrlPropertyEditor")
            //CHECKSTYLE:OFF
    private String url = null;

    /**
     * Defines the ISO language code of the human language used in the target
     * URL file. For example, valid values might be en, fr, es.
     */
    @Property(name = "urlLang",
            displayName = "URL Language",
            category = "Advanced",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.LanguagesEditor")
            //CHECKSTYLE:ON
    private String urlLang = null;

    /**
     * Construct a new {@code Link}.
     */
    public Link() {
        super();
        setRendererType("com.sun.webui.jsf.Link");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.Link";
    }

    /**
     * The component identifier for this component. This value must be unique
     * within the closest parent component that is a naming container.
     */
    @Property(name = "id")
    @Override
    public void setId(final String id) {
        super.setId(id);
    }

    @Property(name = "rendered")
    @Override
    public void setRendered(final boolean rendered) {
        super.setRendered(rendered);
    }

    /**
     * Defines the character (charset) encoding of the target URL. Default value
     * is {@code "ISO-8859-1"}.
     * @return String
     */
    public String getCharset() {
        if (this.charset != null) {
            return this.charset;
        }
        ValueExpression vb = getValueExpression("charset");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Defines the character (charset) encoding of the target URL. Default value
     * is {@code "ISO-8859-1"}.
     *
     * @see #getCharset()
     * @param newCharset charset
     */
    public void setCharset(final String newCharset) {
        this.charset = newCharset;
    }

    /**
     * Specifies the type of display device for which the referenced document is
     * designed. The media attribute is useful for specifying different
     * style sheets for print and viewing on a screen. The default value is
     * {@code "screen"}.
     * @return String
     */
    public String getMedia() {
        if (this.media != null) {
            return this.media;
        }
        ValueExpression vb = getValueExpression("media");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Specifies the type of display device for which the referenced document is
     * designed. The media attribute is useful for specifying different
     * style sheets for print and viewing on a screen. The default value is
     * {@code "screen"}.
     *
     * @see #getMedia()
     * @param newMedia media
     */
    public void setMedia(final String newMedia) {
        this.media = newMedia;
    }

    /**
     * Defines the relationship between the current document and the targeted
     * document. Default is {@code "stylesheet"}. Other possible values are
     * described at w3.org.
     *
     * @return String
     */
    public String getRel() {
        if (this.rel != null) {
            return this.rel;
        }
        ValueExpression vb = getValueExpression("rel");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return "stylesheet";
    }

    /**
     * Defines the relationship between the current document and the targeted
     * document. Default is {@code "stylesheet"}. Other possible values are
     * described at w3.org.
     *
     * @see #getRel()
     * @param newRel rel
     */
    public void setRel(final String newRel) {
        this.rel = newRel;
    }

    /**
     * Specifies the MIME type of the target URL. Default is: {@code "text/css"}
     * @return String
     */
    public String getType() {
        if (this.type != null) {
            return this.type;
        }
        ValueExpression vb = getValueExpression("type");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return "text/css";
    }

    /**
     * Specifies the MIME type of the target URL. Default is: {@code "text/css"}
     *
     * @see #getType()
     * @param newType type
     */
    public void setType(final String newType) {
        this.type = newType;
    }

    /**
     * The absolute or relative target URL of the resource.
     * @return String
     */
    public String getUrl() {
        if (this.url != null) {
            return this.url;
        }
        ValueExpression vb = getValueExpression("url");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The absolute or relative target URL of the resource.
     *
     * @see #getUrl()
     * @param newUrl url
     */
    public void setUrl(final String newUrl) {
        this.url = newUrl;
    }

    /**
     * Defines the ISO language code of the human language used in the target
     * URL file. For example, valid values might be en, fr, es.
     * @return String
     */
    public String getUrlLang() {
        if (this.urlLang != null) {
            return this.urlLang;
        }
        ValueExpression vb = getValueExpression("urlLang");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Defines the ISO language code of the human language used in the target
     * URL file. For example, valid values might be en, fr, es.
     *
     * @see #getUrlLang()
     * @param newUrlLang urlLang
     */
    public void setUrlLang(final String newUrlLang) {
        this.urlLang = newUrlLang;
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.charset = (String) values[1];
        this.media = (String) values[2];
        this.rel = (String) values[3];
        this.type = (String) values[4];
        this.url = (String) values[5];
        this.urlLang = (String) values[6];
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[7];
        values[0] = super.saveState(context);
        values[1] = this.charset;
        values[2] = this.media;
        values[3] = this.rel;
        values[4] = this.type;
        values[5] = this.url;
        values[6] = this.urlLang;
        return values;
    }
}
