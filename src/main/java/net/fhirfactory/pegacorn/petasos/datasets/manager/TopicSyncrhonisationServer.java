/*
 * Copyright (c) 2020 Mark A. Hunter (ACT Health)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.fhirfactory.pegacorn.petasos.datasets.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.fhirfactory.pegacorn.petasos.datasets.cache.TopicCacheDM;
import net.fhirfactory.pegacorn.petasos.datasets.cache.TopicSubscriptionMapDM;
import net.fhirfactory.pegacorn.petasos.datasets.loader.TopicFileElementTransformer;
import net.fhirfactory.pegacorn.petasos.datasets.loader.model.TopicMapElement;
import net.fhirfactory.pegacorn.petasos.datasets.loader.model.TopicMapFileModel;
import net.fhirfactory.pegacorn.petasos.model.topics.Topic;
import net.fhirfactory.pegacorn.petasos.model.topics.TopicToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Mark A. Hunter
 */
@ApplicationScoped
public class TopicSyncrhonisationServer {

    private static final Logger LOG = LoggerFactory.getLogger(TopicSyncrhonisationServer.class);
    boolean fileHasBeenLoaded;
    Object fileHasBeenLoadedLock;

    @Inject
    TopicFileElementTransformer topicFileTransformer;

    @Inject
    TopicSubscriptionMapDM subscriptionMapDM;

    @Inject
    TopicCacheDM topicCache;

    public TopicSyncrhonisationServer() {
        LOG.debug(".TopicSyncrhonisationServer(): Entry");
        this.fileHasBeenLoaded = false;
        this.fileHasBeenLoadedLock = new Object();
        LOG.debug(".TopicSyncrhonisationServer(): Exit");
    }

    @PostConstruct
    public void initialise() {
        LOG.debug(".initialise(): Entry");
        synchronized (fileHasBeenLoadedLock) {
            if (!fileHasBeenLoaded) {
                synchroniseFromFile();
                fileHasBeenLoaded = true;
            }
            printTopicList();
        }
        LOG.debug(".initialise(): Exit");
    }

    public void initialiseServices(){
        LOG.debug("initialiseServices(): Entry");
        synchronized(fileHasBeenLoadedLock) {
            if (fileHasBeenLoaded) {
                return;
            } else {
                synchroniseFromFile();
                fileHasBeenLoaded = true;
            }
        }
        this.printTopicList();
        this.printSubscriptionList();
        LOG.debug("initialiseServices(): Exit");
    }

    public void synchroniseFromFile() {
        if (fileHasBeenLoaded) {
            return;
        }
        LOG.debug("synchroniseFromFile(): Entry");
        TopicMapFileModel mapFileContent = null;
        String filePath2 = "/META-INF/TopicsFile.json";
        String filePath1 = "/TopicsFile.json";
        boolean worked = false;
        LOG.trace(".synchroniseFromFile(): Instantiate our ObjectMapper for JSON parsing of the Topic Configuration File");
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            LOG.trace(".synchroniseFromFile(): Create the URL for the Topic Configuration File (it should be within the WAR), filename --> {}", filePath1);
            URL fileURL = getClass().getResource(filePath1);
            LOG.trace(".synchroniseFromFile(): URL created, content --> {}", fileURL);
            LOG.trace(".synchroniseFromFile(): Open the file as an InputStream");
            InputStream configFileIS = fileURL.openStream();
            LOG.trace(".synchroniseFromFile(): File Openned, now reading content into the ObjectMapper");
            mapFileContent = objectMapper.readValue(configFileIS, TopicMapFileModel.class);
            worked = true;
            LOG.trace(".synchroniseFromFile(): Content Read, mapFileContent --> {}", mapFileContent);
        } catch (Exception Ex) {
            LOG.trace(".synchroniseFromFile(): Error!!! - ObjectMapper read failed - error --> {} " + Ex);
        }
        if (!worked) {
            try {
                LOG.trace(".synchroniseFromFile(): Create the URL for the Topic Configuration File (it should be within the WAR), filename --> {}", filePath2);
                URL fileURL = getClass().getResource(filePath2);
                LOG.trace(".synchroniseFromFile(): URL created, content --> {}", fileURL);
                LOG.trace(".synchroniseFromFile(): Open the file as an InputStream");
                InputStream configFileIS = fileURL.openStream();
                LOG.trace(".synchroniseFromFile(): File Openned, now reading content into the ObjectMapper");
                mapFileContent = objectMapper.readValue(configFileIS, TopicMapFileModel.class);
                worked = true;
                LOG.trace(".synchroniseFromFile(): Content Read, mapFileContent --> {}", mapFileContent);
            } catch (Exception Ex) {
                LOG.trace(".synchroniseFromFile(): Error!!! - ObjectMapper read failed - error --> {} " + Ex);
            }
        }
        if (mapFileContent == null) {
            LOG.debug(".synchroniseFromFile(): Unable to read file or file was empty, exiting");
            return;
        }
        LOG.trace(".synchroniseFromFile(): Now processing the map file");
        Iterator<TopicMapElement> sectorIterator = mapFileContent.getTopicSectors().iterator();
        while (sectorIterator.hasNext()) {
            TopicMapElement currentTopicElement = sectorIterator.next();
            LOG.trace(".synchroniseFromFile(): Now processing the map file for Topic Sector --> {}", currentTopicElement.getTopicName());
            topicFileTransformer.convertToTopicElement(currentTopicElement, null);
        }
        LOG.debug(".synchroniseFromFile(): Exit, file processed.");
    }

    public void printTopicList(){
        if(!LOG.isDebugEnabled()){
            return;
        }
        LOG.debug(".printTopicList(): List of Topics is as follows:");
        Set<Topic> topicSet = topicCache.getTopicSet();
        Iterator<Topic> topicIterator = topicSet.iterator();
        while(topicIterator.hasNext()){
            Topic currentTopic = topicIterator.next();
            LOG.debug(".printTopicList(): Topic Identifier --> {}, version --> {}", currentTopic.getIdentifier(), currentTopic.getTopicToken().getVersion());
        }
    }

    public void printSubscriptionList(){
        if(!LOG.isDebugEnabled()){
            return;
        }
        LOG.debug(".printSubscriptionList(): Entry");
        subscriptionMapDM.printAllSubscriptionSets();
        LOG.debug(".printSubscriptionList(): Exit");
    }
}
