package net.fhirfactory.pegacorn.petasos.datasets.loader;

import org.jboss.arquillian.container.test.api.Deployment;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.fhirfactory.pegacorn.common.model.FDN;
import net.fhirfactory.pegacorn.petasos.datasets.manager.TopicIM;
import net.fhirfactory.pegacorn.petasos.model.topics.Topic;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;

@RunWith(Arquillian.class)
public class TopicManagerServicesTest {
    private static final Logger LOG = LoggerFactory.getLogger(TopicManagerServicesTest.class);

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive testWAR;

        File[] fileSet = Maven.resolver().loadPomFromFile("pom.xml").importRuntimeDependencies().resolve().withTransitivity().asFile();
        LOG.debug(".createDeployment(): ShrinkWrap Library Set for run-time equivalent, length --> {}", fileSet.length);
        for(int counter = 0; counter < fileSet.length; counter++ ){
            File currentFile = fileSet[counter];
            LOG.trace(".createDeployment(): Shrinkwrap Entry --> {}", currentFile.getName());
        }
        
        File topicFile = new File("/TopicsFile.json");
        testWAR = ShrinkWrap.create(WebArchive.class, "topic-test.war")
                .addAsLibraries(fileSet)
                .addPackages(true, "net.fhirfactory.pegacorn.petasos.datasets")
                .addAsManifestResource(topicFile, "/TopicsFile.json")
                .addAsManifestResource("META-INF/beans.xml", "beans.xml");

        Map<ArchivePath, Node> content = testWAR.getContent();
        Set<ArchivePath> contentPathSet = content.keySet();
        Iterator<ArchivePath> contentPathSetIterator = contentPathSet.iterator();
        while (contentPathSetIterator.hasNext()) {
            ArchivePath currentPath = contentPathSetIterator.next();
            LOG.trace(".createDeployment(): testWare Entry Path --> {}", currentPath.get());
        }
        return (testWAR);
    }

    @javax.inject.Inject
    TopicIM topicServer;

    @Before
    public void servicesSetup(){
        topicServer.initialiseServices();
    }

    @Test
    public void testLoadingOfFile() {
        LOG.info(".testLoadingOfFile(): Info Test");
        LOG.info(".testLoadingOfFile(): Now showing content of the TopologyCache");
        Set<Topic> nodeSet = topicServer.getTopicSet();
        LOG.info(".testLoadingOfFile(): nodeSet Size --> {}", nodeSet.size());
    }

    @Test
    public void testTopicServer() {
        LOG.info(".testTopicServer(): Info Test");
        LOG.info(".testTopicServer(): Now showing content of the TopologyCache");
        Set<Topic> nodeSet = topicServer.getTopicSet();
        LOG.info(".testTopicServer(): nodeSet --> {}", nodeSet);
        Iterator<Topic> nodeSetIterator = nodeSet.iterator();
        while (nodeSetIterator.hasNext()) {
            Topic currentNode = nodeSetIterator.next();
            FDN currentNodeFDN = new FDN(currentNode.getIdentifier());
            LOG.info(".testTopicServer(): Topic Instance ID--> {}", currentNodeFDN.getUnqualifiedToken());
        }
    }
}

