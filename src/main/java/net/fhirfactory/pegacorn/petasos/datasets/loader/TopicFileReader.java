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
package net.fhirfactory.pegacorn.petasos.datasets.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import net.fhirfactory.pegacorn.petasos.datasets.loader.model.TopicMapElement;
import net.fhirfactory.pegacorn.petasos.datasets.loader.model.TopicMapFileModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Mark A. Hunter
 */
@ApplicationScoped
public class TopicFileReader {

    private static final Logger LOG = LoggerFactory.getLogger(TopicFileReader.class);

    private static String fileName = "/META-INF/TopicsFile.json";
    
    @Inject
    TopicFileElementTransformer transformer;

    public void readFile() {
        LOG.debug(".readFile(): Entry");
        
        LOG.trace(".readFile(): Instantiate our ObjectMapper for JSON parsing of the Topics File");
        ObjectMapper objectMapper = new ObjectMapper();
        LOG.trace(".readFile(): Create the URL for the Topics File (it should be within the WAR), filename --> {}", fileName );
        URL fileURL = getClass().getResource(fileName);
        LOG.trace(".readFile(): URL created, content --> {}", fileURL);
        TopicMapFileModel topicFileContent = null;
        try {
            LOG.trace(".readFile(): Open the file as an InputStream");
            InputStream configFileIS = fileURL.openStream();
            LOG.trace(".readFile(): File Openned, now reading content into the ObjectMapper");
            topicFileContent = objectMapper.readValue(configFileIS, TopicMapFileModel.class);
            LOG.trace(".readFile(): Content Read, topicFileContent --> {}", topicFileContent);
        } catch (Exception Ex) {
            LOG.trace(".readFile(): Error!!! - ObjectMapper read failed - error --> " + Ex.toString());
        }
        if (topicFileContent == null) {
            LOG.debug(".readFile(): Unable to read file or file was empty, exiting");
            return;
        }
        LOG.trace(".readFile(): Iterate through topicFileContent and add to the Topics Cache");
        Iterator<TopicMapElement> sectorIterator = topicFileContent.getTopicSectors().iterator();
        while(sectorIterator.hasNext()){
            TopicMapElement currentTopicElement = sectorIterator.next();
            LOG.trace(".readFile(): Now processing the map file for Topic Sector --> {}", currentTopicElement.getTopicName());
            transformer.convertToTopicElement(currentTopicElement, null);
        }
        LOG.debug(".readFile(): Exit");
    }
}
