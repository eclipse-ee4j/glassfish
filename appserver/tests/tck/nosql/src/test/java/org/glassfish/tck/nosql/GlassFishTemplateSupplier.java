package org.glassfish.tck.nosql;

import jakarta.enterprise.inject.spi.CDI;

import org.eclipse.jnosql.mapping.DatabaseQualifier;
import org.eclipse.jnosql.mapping.document.DocumentTemplate;

import ee.jakarta.tck.nosql.TemplateSupplier;

public class GlassFishTemplateSupplier implements TemplateSupplier {

    @Override
    public jakarta.nosql.Template get() {
        return CDI.current()
                .select(DocumentTemplate.class, DatabaseQualifier.ofDocument())
                .get();
    }
}
