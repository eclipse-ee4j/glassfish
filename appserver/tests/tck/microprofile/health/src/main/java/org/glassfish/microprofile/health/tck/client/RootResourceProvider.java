package org.glassfish.microprofile.health.tck.client;

import org.jboss.arquillian.container.test.impl.enricher.resource.URIResourceProvider;
import org.jboss.arquillian.test.api.ArquillianResource;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URISyntaxException;

public class RootResourceProvider extends URIResourceProvider {

    @Override
    public Object lookup(ArquillianResource arquillianResource, Annotation... annotations) {
        Object lookup = super.lookup(arquillianResource, annotations);
        // remove the context path from the URI
        if (lookup instanceof URI) {
            URI uri = (URI) lookup;
            try {
                return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), "", uri.getQuery(), uri.getFragment());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}
