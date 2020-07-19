/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package firstcup.ejb;

import firstcup.entity.FirstcupUser;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * DukesBirthdayBean is a stateless session bean that calculates the age
 * difference between a user and Duke, who was born on May 23, 1995.
 */
@Stateless
public class DukesBirthdayBean {

    private static final Logger logger =
            Logger.getLogger("firstcup.ejb.DukesBirthdayBean");
    @PersistenceContext
    private EntityManager em;

    public Double getAverageAgeDifference() {
        Double avgAgeDiff =
                (Double) em.createNamedQuery("findAverageAgeDifferenceOfAllFirstcupUsers").getSingleResult();
        logger.log(Level.INFO, "Average age difference is: {0}", avgAgeDiff);
        return avgAgeDiff;

    }

    public int getAgeDifference(Date date) {
        int ageDifference;

        Calendar theirBirthday = new GregorianCalendar();
        Calendar dukesBirthday = new GregorianCalendar(1995, Calendar.MAY, 23);

        // Set the Calendar object to the passed-in Date
        theirBirthday.setTime(date);

        // Subtract the user's age from Duke's age
        ageDifference = dukesBirthday.get(Calendar.YEAR)
                - theirBirthday.get(Calendar.YEAR);
        logger.log(Level.INFO, "Raw ageDifference is: {0}", ageDifference);

        // Check to see if Duke's birthday occurs before the user's. If so,
        // subtract one from the age difference
        if (dukesBirthday.before(theirBirthday) && (ageDifference > 0)) {
            ageDifference--;
        }

        // Check to see if Duke's birthday occurs after the user's when the user 
        // is younger. If so, subtract one from the age difference
        if (dukesBirthday.after(theirBirthday) && (ageDifference < 0)) {
            ageDifference++;
        }

        // Create and store the user's birthday in the database
        FirstcupUser user = new FirstcupUser(date, ageDifference);
        em.persist(user);

        logger.log(Level.INFO, "Final ageDifference is: {0}", ageDifference);

        return ageDifference;
    }
}
