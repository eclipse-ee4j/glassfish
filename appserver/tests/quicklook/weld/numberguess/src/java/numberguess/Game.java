/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package numberguess;


import java.io.Serializable;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.enterprise.context.ApplicationScoped;

@Named
@SessionScoped
public class Game implements Serializable
{
   private static final long serialVersionUID = 1L;

   private int number;

   private int guess;
   private int smallest;

   @Inject
   private StatefulBean sb;

   @MaxNumber @Inject
   private int maxNumber;

   private int biggest;
   private int remainingGuesses;

   @Random @Inject Instance<Integer> randomNumber;

   public Game()
   {
   }

   public int getNumber()
   {
      return number;
   }

   public int getGuess()
   {
      return guess;
   }

   public void setGuess(int guess)
   {
      this.guess = guess;
   }

   public int getSmallest()
   {
      return smallest;
   }

   public int getBiggest()
   {
      return biggest;
   }

   public int getRemainingGuesses()
   {
      return remainingGuesses;
   }

   public String check() throws InterruptedException
   {
       System.out.println("In Game::check");
       sb.hello();

      if (guess>number)
      {
         biggest = guess - 1;
      }
      if (guess<number)
      {
         smallest = guess + 1;
      }
      if (guess == number)
      {
         FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Correct!"));
      }
      remainingGuesses--;
      return null;
   }

   @PostConstruct
   public void reset()
   {
       System.out.println("In Game::reset()");
       sb.hello();

      this.smallest = 0;
      this.guess = 0;
      this.remainingGuesses = 10;
      this.biggest = maxNumber;
      this.number = randomNumber.get();
   }

   public void validateNumberRange(FacesContext context,  UIComponent toValidate, Object value)
   {
      if (remainingGuesses <= 0)
      {
         FacesMessage message = new FacesMessage("No guesses left!");
         context.addMessage(toValidate.getClientId(context), message);
         ((UIInput)toValidate).setValid(false);
         return;
      }
      int input = (Integer) value;

      if (input < smallest || input > biggest)
       {
         ((UIInput)toValidate).setValid(false);

         FacesMessage message = new FacesMessage("Invalid guess");
         context.addMessage(toValidate.getClientId(context), message);
      }
   }
}
