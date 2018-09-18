<%--

    Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.

    This program and the accompanying materials are made available under the
    terms of the Eclipse Public License v. 2.0, which is available at
    http://www.eclipse.org/legal/epl-2.0.

    This Source Code may also be made available under the following Secondary
    Licenses when the conditions for such availability set forth in the
    Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
    version 2 with the GNU Classpath Exception, which is available at
    https://www.gnu.org/software/classpath/license.html.

    SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0

--%>

<html>
<body>
<h2>JPA Validation Tests</h2>
<br>
The entity Employee has a validation constraint of size 5.
The tests check ConstraintViolationException and expected resuls to persist, update, remove an employee with the name longer than 5.
<br>
<ul>
<li>
<a href="test?tc=initialize">Step 1. Persist a project with a few employees with short name</a> </li>
<ul><li>Expected Result: The project and employees are in database.</li></ul>
<li><a href="test?tc=validatePersist">Step 2. Persist an employee with a long name</a> </li>
<ul><li>Expected Result: That employee will not be in databse.</li></ul>
<li><a href="test?tc=validateUpdate">Step 3. Update an employee with a long name</a> </li>
<ul><li>Expected Result: The name change will not be in database.</li></ul>
<li><a href="test?tc=validateRemove">Step 4. Remove an employee with a long name</a></li>
<ul><li>Expected Result: That employee will be removed from database.</li></ul>
<li><a href="test?tc=verify">Step 5. Verify the validation</a> </li>
<ul><li>Expected Result: No employee in database has long name.</li></ul>
</ul>
</body>
</html>
