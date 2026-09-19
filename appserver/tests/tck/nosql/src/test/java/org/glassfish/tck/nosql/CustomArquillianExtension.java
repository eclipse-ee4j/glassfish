package org.glassfish.tck.nosql;

import org.jboss.arquillian.junit5.ArquillianExtension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;

public class CustomArquillianExtension extends ArquillianExtension {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        // TCK test methods do not use Arquillian parameter injection (like @Inject on test method arguments)
        // By always returning false, we avoid conflicting with JUnit's @ParameterizedTest
        // and we avoid NullPointerExceptions during reflection on CDI proxies inside GlassFish.
        return false;
    }
}
