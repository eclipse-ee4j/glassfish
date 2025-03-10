package arquillian;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

public class GlassfishTckExtension implements LoadableExtension {
    @Override
    public void register(ExtensionBuilder builder) {
        builder.service(ResourceProvider.class, GlassfishXmlProcessor.class);
        builder.observer(GlassfishXmlProcessor.class);
    }
}