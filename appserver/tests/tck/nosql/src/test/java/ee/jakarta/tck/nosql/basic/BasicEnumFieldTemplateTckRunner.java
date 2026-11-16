/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */
package ee.jakarta.tck.nosql.basic;

import org.glassfish.tck.nosql.CustomArquillianExtension;
import org.glassfish.tck.nosql.NoSqlTckArchives;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(CustomArquillianExtension.class)
public class BasicEnumFieldTemplateTckRunner extends BasicEnumFieldTemplateTest {

    @Deployment
    public static WebArchive deploy() {
        return NoSqlTckArchives.create("BasicEnumFieldTemplateTckRunner.war");
    }
}
