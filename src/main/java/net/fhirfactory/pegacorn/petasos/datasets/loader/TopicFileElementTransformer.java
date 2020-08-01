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

import java.util.Iterator;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.fhirfactory.pegacorn.common.model.FDN;
import net.fhirfactory.pegacorn.common.model.FDNToken;
import net.fhirfactory.pegacorn.common.model.RDN;
import net.fhirfactory.pegacorn.petasos.datasets.loader.model.TopicMapElement;
import net.fhirfactory.pegacorn.petasos.datasets.manager.TopicIM;
import net.fhirfactory.pegacorn.petasos.model.topics.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Mark A. Hunter
 */
@Singleton
public class TopicFileElementTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(TopicFileElementTransformer.class);

    @Inject
    TopicIM topicServer;

    public Topic convertToTopicElement(TopicMapElement incomingTopicDetail, FDNToken parentTopic) {
        LOG.debug(".convertToTopicElement(): Entry, incomingTopicDetail --> {}, parentTopic --> {}", incomingTopicDetail, parentTopic);
        Topic newTopic = new Topic();
        LOG.trace(".convertToTopicElement(): Adding the ContainingTopic to the new TopicElement, containing topic instance id --> {}", parentTopic);
        newTopic.setContainingDataset(parentTopic);
        LOG.trace(".convertToTopicElement(): Adding the TopicID to the new TopicElement, instance name --> {}", incomingTopicDetail.getTopicName());
        FDN newTopicInstanceFDN;
        if (parentTopic == null) {
            newTopicInstanceFDN = new FDN();
        } else {
            newTopicInstanceFDN = new FDN(parentTopic);
        }
        newTopicInstanceFDN.appendRDN(new RDN(incomingTopicDetail.getTopicType().getTopicType(), incomingTopicDetail.getTopicName()));
        newTopic.setIdentifier(newTopicInstanceFDN.getToken());
        LOG.trace(".convertToTopicElement(): Calling on Topics Manager to add Topic to Topic Cache, parentTopicInstanceID --> {}, newTopic --> {}", parentTopic, newTopic);
        topicServer.registerTopic(newTopic);
        LOG.trace(".convertToTopicElement(): Adding the contained Topic IDs to the Topic Element");
        if (!incomingTopicDetail.getContainedElements().isEmpty()) {
            LOG.trace(".convertToTopicElement(): Adding the contained Topic IDs, number to be addded --> {}", incomingTopicDetail.getContainedElements().size());
            Iterator<TopicMapElement> topicElementIterator = incomingTopicDetail.getContainedElements().iterator();
            while (topicElementIterator.hasNext()) {
                TopicMapElement containedNode = topicElementIterator.next();
                LOG.trace("convertToTopicElement(): Adding the contained Node ID --> {}", containedNode.getTopicName());
                FDN containedNodeFDN = new FDN(newTopicInstanceFDN);
                containedNodeFDN.appendRDN(new RDN(containedNode.getTopicType().getTopicType(), containedNode.getTopicName()));
                newTopic.addContainedTopic(containedNodeFDN.getToken());
                convertToTopicElement(containedNode, newTopicInstanceFDN.getToken());
            }
        }
        return (newTopic);
    }
}
