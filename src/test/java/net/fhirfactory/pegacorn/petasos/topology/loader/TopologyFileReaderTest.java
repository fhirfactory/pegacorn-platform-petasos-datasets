package net.fhirfactory.pegacorn.petasos.topology.loader;

import net.fhirfactory.pegacorn.petasos.datasets.loader.TopicFileReader;
import org.jboss.arquillian.container.test.api.Deployment;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class TopologyFileReaderTest {
    private static final Logger LOG = LoggerFactory.getLogger(TopologyFileReaderTest.class);

    @Deployment
    public static JavaArchive createDeployment() {
        WebArchive testWAR;

        File[] fileSet = Maven.resolver().loadPomFromFile("pom.xml").resolve("net.fhirfactory.pegacorn:pegacorn-platform-commoncode").withTransitivity().asFile();
        LOG.debug(".createDeployment(): ShrinkWrap Library Set, length --> {}", fileSet.length);
        for(int counter = 0; counter < fileSet.length; counter++ ){
            File currentFile = fileSet[counter];
            LOG.trace(".createDeployment(): Shrinkwrap Entry --> {}", currentFile.getName());
        }

        return ShrinkWrap.create(JavaArchive.class)
                .addClass(TopicFileReader.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void readFile() {
        LOG.info(".readFile(): Info Test");
        LOG.debug(".readFile(): Info Test");
    }
}
