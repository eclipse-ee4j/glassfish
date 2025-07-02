package ee.jakarta.tck.data.example.extension;

import java.util.List;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

//tag::applicationProcessor[]
public class MyApplicationArchiveProcessor implements ApplicationArchiveProcessor {
    
    //List of test classes that deploy application that you need to customize
    List<String> testClasses;

    @Override
    public void process(Archive<?> archive, TestClass testClass) {
        //if(testClasses.contains(testClass.getClass().getCanonicalName())){
          //  ((WebArchive) archive).addAsWebInfResource("my-custom-sun-web.xml", "sun-web.xml");
        //}
    }
}
//end::applicationProcessor[]
