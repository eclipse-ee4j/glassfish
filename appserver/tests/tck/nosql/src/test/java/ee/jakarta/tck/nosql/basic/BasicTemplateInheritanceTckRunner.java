package ee.jakarta.tck.nosql.basic;

import org.jboss.arquillian.container.test.api.Deployment;
import org.glassfish.tck.nosql.CustomArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.extension.ExtendWith;
import org.glassfish.tck.nosql.NoSqlTckArchives;

@ExtendWith(CustomArquillianExtension.class)
public class BasicTemplateInheritanceTckRunner extends BasicTemplateInheritanceTest {

    @Deployment
    public static WebArchive deploy() {
        return NoSqlTckArchives.create("BasicTemplateInheritanceTckRunner.war");
    }
}
