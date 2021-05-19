/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.jboss.weld.examples.translator;

import jakarta.ejb.Remove;
import jakarta.ejb.Stateful;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Stateful
@RequestScoped
@Named("translator")
    public class TranslatorControllerBean implements TranslatorController , TranslatorController2
{

   @Inject
   private TextTranslator translator;

   private String inputText;

   private String translatedText;

   public String getText()
   {
      return inputText;
   }

   public void setText(String text)
   {
      this.inputText = text;
   }

   public void translate()
   {
      translatedText = translator.translate(inputText);
   }

   public String getTranslatedText()
   {
      return translatedText;
   }

    public void foo() {}

   @Remove
   public void remove()
   {

   }

}
