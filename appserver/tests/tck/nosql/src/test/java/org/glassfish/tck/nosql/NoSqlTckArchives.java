package org.glassfish.tck.nosql;
import java.io.File;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.api.asset.StringAsset;

public class NoSqlTckArchives {
    public static WebArchive create(String warName) {
        String mpConfigContent = System.getProperty(MongoDbBeforeAllExtension.MP_CONFIG_CONTENT_KEY);
        if (mpConfigContent == null) {
            mpConfigContent = "jnosql.document.database=tck\njnosql.mapping.packages=ee.jakarta.tck.nosql.entities\n";
            System.err.println("WARNING: " + MongoDbBeforeAllExtension.MP_CONFIG_CONTENT_KEY + " is not set! MongoDB might not be running.");
        }

        StringBuilder beansXml = new StringBuilder();
        beansXml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        beansXml.append("<beans xmlns=\"https://jakarta.ee/xml/ns/jakartaee\"\n");
        beansXml.append("       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
        beansXml.append("       xsi:schemaLocation=\"https://jakarta.ee/xml/ns/jakartaee https://jakarta.ee/xml/ns/jakartaee/beans_4_0.xsd\"\n");
        beansXml.append("       version=\"4.0\" bean-discovery-mode=\"all\">\n");
        beansXml.append("    <scan>\n");
        beansXml.append("        <exclude name=\"ee.jakarta.tck.nosql.basic.BasicConverterFieldTemplateTckRunner\"/>\n");
        beansXml.append("        <exclude name=\"ee.jakarta.tck.nosql.basic.BasicEnumFieldTemplateTckRunner\"/>\n");
        beansXml.append("        <exclude name=\"ee.jakarta.tck.nosql.basic.BasicIterableEntityTemplateTckRunner\"/>\n");
        beansXml.append("        <exclude name=\"ee.jakarta.tck.nosql.basic.BasicTemplateInheritanceTckRunner\"/>\n");
        beansXml.append("        <exclude name=\"ee.jakarta.tck.nosql.basic.BasicTemplateMappedSuperclassTckRunner\"/>\n");
        beansXml.append("        <exclude name=\"ee.jakarta.tck.nosql.basic.BasicTemplateRecordTckRunner\"/>\n");
        beansXml.append("        <exclude name=\"ee.jakarta.tck.nosql.basic.BasicTemplateTckRunner\"/>\n");
        beansXml.append("        <exclude name=\"ee.jakarta.tck.nosql.delete.DeleteFieldConvertTckRunner\"/>\n");
        beansXml.append("        <exclude name=\"ee.jakarta.tck.nosql.delete.DeleteRecordTckRunner\"/>\n");
        beansXml.append("        <exclude name=\"ee.jakarta.tck.nosql.delete.DeleteTemplateInheritanceTckRunner\"/>\n");
        beansXml.append("        <exclude name=\"ee.jakarta.tck.nosql.delete.DeleteTemplateMappedSuperclassTckRunner\"/>\n");
        beansXml.append("        <exclude name=\"ee.jakarta.tck.nosql.delete.DeleteTemplateTckRunner\"/>\n");
        beansXml.append("        <exclude name=\"ee.jakarta.tck.nosql.select.SelectEnumTckRunner\"/>\n");
        beansXml.append("        <exclude name=\"ee.jakarta.tck.nosql.select.SelectFieldConvertTckRunner\"/>\n");
        beansXml.append("        <exclude name=\"ee.jakarta.tck.nosql.select.SelectMappedSuperclassTckRunner\"/>\n");
        beansXml.append("        <exclude name=\"ee.jakarta.tck.nosql.select.SelectNegateTemplateTckRunner\"/>\n");
        beansXml.append("        <exclude name=\"ee.jakarta.tck.nosql.select.SelectRecordTckRunner\"/>\n");
        beansXml.append("        <exclude name=\"ee.jakarta.tck.nosql.select.SelectTemplateInheritanceTckRunner\"/>\n");
        beansXml.append("        <exclude name=\"ee.jakarta.tck.nosql.select.SelectTemplateTckRunner\"/>\n");
        beansXml.append("    </scan>\n");
        beansXml.append("</beans>\n");

        return ShrinkWrap.create(WebArchive.class, warName)
                .addAsLibraries(
                        org.jboss.shrinkwrap.api.ShrinkWrap.create(org.jboss.shrinkwrap.api.spec.JavaArchive.class, "tck-classes.jar")
                                .addPackages(true, "ee.jakarta.tck.nosql")
                                .addPackages(true, "org.glassfish.tck.nosql")
                )
                .addAsServiceProvider("ee.jakarta.tck.nosql.TemplateSupplier", "org.glassfish.tck.nosql.GlassFishTemplateSupplier")
                .addAsResource(new StringAsset(mpConfigContent), "META-INF/microprofile-config.properties")
                .addAsWebInfResource(new StringAsset(beansXml.toString()), "beans.xml");
    }
}
