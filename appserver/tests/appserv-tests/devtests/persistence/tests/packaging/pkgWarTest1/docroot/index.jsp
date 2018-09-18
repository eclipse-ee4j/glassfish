<%--

    Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.

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
<h2>pkgWebTest</h2>
<br> PU in lib dir accessed by ejb jar in steps 1 and 2
<br> PU in lib dir accessed by war in steps 3 and 4
<br>
<a href="test?case=testInsert">Step 1. testInsert</a> <br>
<a href="test?case=verifyInsert">Step 2. verifyInsert</a> <br>
<a href="jpa?case=testDelete">Step 3. testDelete</a> <br>
<a href="jpa?case=verifyDelete">Step 4. verifyDelete</a> <br>
</body>
</html>
