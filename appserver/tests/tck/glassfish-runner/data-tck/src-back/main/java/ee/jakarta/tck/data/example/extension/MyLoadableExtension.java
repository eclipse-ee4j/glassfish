package ee.jakarta.tck.data.example.extension;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.core.spi.LoadableExtension;

//tag::loadableExtension[]
public class MyLoadableExtension implements LoadableExtension {
    @Override
    public void register(ExtensionBuilder extensionBuilder) {
        extensionBuilder.service(ApplicationArchiveProcessor.class, MyApplicationArchiveProcessor.class);
    }
}
//end::loadableExtension[]
