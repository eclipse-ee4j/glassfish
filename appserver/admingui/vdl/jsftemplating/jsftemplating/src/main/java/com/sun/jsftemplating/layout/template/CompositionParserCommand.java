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

package com.sun.jsftemplating.layout.template;

import com.sun.jsftemplating.layout.ProcessingCompleteException;
import com.sun.jsftemplating.layout.descriptors.LayoutComposition;
import com.sun.jsftemplating.layout.descriptors.LayoutDefinition;
import com.sun.jsftemplating.layout.descriptors.LayoutElement;
import com.sun.jsftemplating.util.LayoutElementUtil;

import java.io.IOException;
import java.util.List;

/**
 * <p>
 * This {@link CustomParserCommand} handles "composition" statements. TBD...
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class CompositionParserCommand implements CustomParserCommand {

    /**
     * <p>
     * Constructor. This constructor requires a flag to be passed in indicating wether content outside this component should
     * be ignored (trimmed) or left alone.
     * </p>
     *
     * @param trim <code>true</code> if content outside this component should be thrown away.
     *
     * @param templateAttName The name of the attribute for the template name.
     */
    public CompositionParserCommand(boolean trim, String templateAttName) {
        this.trimming = trim;
        this.templateAttName = templateAttName;
    }

    /**
     * <p>
     * This method processes a "custom" command. These are commands that start with a !. When this method receives control,
     * the <code>name</code> (i.e. the token after the '!' character) has already been read. It is passed via the
     * <code>name</code> parameter.
     * </p>
     *
     * <p>
     * The {@link ProcessingContext} and {@link ProcessingContextEnvironment} are both available.
     * </p>
     */
    @Override
    public void process(ProcessingContext ctx, ProcessingContextEnvironment env, String name) throws IOException {
        // Get the reader
        TemplateReader reader = env.getReader();

        LayoutElement parent = env.getParent();
        if (trimming) {
            // First remove the current children on the LD (trimming == true)
            parent = parent.getLayoutDefinition();
            parent.getChildLayoutElements().clear();
        }

        // Next get the attributes
        List<NameValuePair> nvps = reader.readNameValuePairs(name, templateAttName, true);

        // Create new LayoutComposition
        LayoutComposition compElt = new LayoutComposition(parent, LayoutElementUtil.getGeneratedId(name, reader.getNextIdNumber()), trimming);

        // Look for required attribute
        // Find the template name
        for (NameValuePair nvp : nvps) {
            if (nvp.getName().equals(templateAttName)) {
                compElt.setTemplate((String) nvp.getValue());
            } else if (nvp.getName().equals(REQUIRED_ATTRIBUTE)) {
                compElt.setRequired(nvp.getValue().toString());
            } else {
                // We are going to treat extra attributes on compositions to be
                // ui:param values
                compElt.setParameter(nvp.getName(), nvp.getValue());
            }
        }

        parent.addChildLayoutElement(compElt);

        // See if this is a single tag or not...
        TemplateParser parser = reader.getTemplateParser();
        int ch = parser.nextChar();
        if (ch == '/') {
            reader.popTag(); // Don't look for end tag
        } else {
            // Unread the ch we just read
            parser.unread(ch);

            // Process child LayoutElements (recurse)
            reader.process(LAYOUT_COMPOSITION_CONTEXT, compElt, LayoutElementUtil.isLayoutComponentChild(compElt));
        }

        if (trimming) {
            // End processing... (trimming == true)
            throw new ProcessingCompleteException((LayoutDefinition) parent);
        }
    }

    /**
     * <p>
     * This is the {@link ProcessingContext} for {@link LayoutComposition}s.
     * </p>
     */
    protected static class LayoutCompositionContext extends BaseProcessingContext {
        /**
         * <p>
         * This is called when a special tag is found (&lt;!tagname ...).
         * </p>
         *
         * <p>
         * This implementation looks for "define" tags and handles them specially. These tags are only valid in this context.
         * </p>
         */
        @Override
        public void beginSpecial(ProcessingContextEnvironment env, String content) throws IOException {
            if (content.equals("define")) {
                DEFINE_PARSER_COMMAND.process(this, env, content);
            } else {
                super.beginSpecial(env, content);
            }
        }
    }

    /**
     * <p>
     * This indicates whether content outside of this tag should be left alone or used.
     * </p>
     */
    private boolean trimming = true;
    private String templateAttName = null;

    public static final String REQUIRED_ATTRIBUTE = "required";
    /**
     * <p>
     * The {@link ProcessingContext} to be used when processing children of a {@link LayoutComposition}. This
     * {@link ProcessingContext} may have special meaning for {@link com.sun.jsftemplating.layout.descriptors.LayoutDefine}s
     * and other tags.
     * </p>
     */
    public static final ProcessingContext LAYOUT_COMPOSITION_CONTEXT = new LayoutCompositionContext();

    public static final CustomParserCommand DEFINE_PARSER_COMMAND = new DefineParserCommand();
}
