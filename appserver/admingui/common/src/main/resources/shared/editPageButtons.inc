<!--

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

-->

<!-- common/shared/editPageButtons.inc -->

    <!-- To use this general include files for page buttons,  the following page Session attributes should be set:
        setPageSessionAttribute(key="edit" value="#{false}" );
        setPageSessionAttribute(key="showDefaultButton" value="#{false}" );
        setPageSessionAttribute(key="showCancelButton" value="#{false}" );
        setPageSessionAttribute(key="hasPropertySheet" value="#{true}" );
        setPageSessionAttribute(key="hasPropertyTable" value="#{false}" );
    -->


#include "/common/shared/editPageLoadDefaultButton.inc"
#include "/common/shared/editPageButtons_2.inc"
