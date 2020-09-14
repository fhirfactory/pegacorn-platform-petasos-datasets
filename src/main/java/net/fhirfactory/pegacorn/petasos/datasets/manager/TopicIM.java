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

import net.fhirfactory.pegacorn.common.model.FDNToken;
import net.fhirfactory.pegacorn.petasos.datasets.cache.TopicCacheDM;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElement;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import net.fhirfactory.pegacorn.common.model.FDNTokenSet;
import net.fhirfactory.pegacorn.petasos.datasets.cache.TopicSubscriptionMapDM;
import net.fhirfactory.pegacorn.petasos.model.topics.Topic;
import net.fhirfactory.pegacorn.petasos.model.topics.TopicToken;

/**
 * This class WILL do more in the future, but it is for now just a proxy to the
 TopicCacheDM.
 */
@ApplicationScoped
public class TopicIM {

    private static final Logger LOG = LoggerFactory.getLogger(TopicIM.class);

    @Inject
    TopicCacheDM topicSetCache;

    @Inject
    TopicSubscriptionMapDM subscriptionCache;

    @Transactional
    public void registerTopic(Topic newElement) {
        LOG.debug(".registerTopic(): Entry, newElement --> {}", newElement);
        topicSetCache.addTopic(newElement);
        if (newElement.hasContainingDataset()) {
            addContainedTopicToTopic(newElement.getContainingDataset(), newElement);
        }
    }

    @Transactional
    public void addContainedTopicToTopic(FDNToken topicID, Topic containedTopic) {
        LOG.debug(".addContainedTopicToTopic(), nodeID --> {}, containedNode --> {}", topicID, containedTopic);
        Topic containingElement = getTopic(topicID);
        if (containingElement != null) {
            LOG.trace(".addContainedTopicToTopic(): Containing Topic exists, so add contained node!");
            containingElement.addContainedTopic(containedTopic.getIdentifier());
        } else {
            LOG.trace(".addContainedTopicToTopic(): Containing Topic doesn't exist, so the containedNode is actually the Top node!");
        }
    }

    @Transactional
    public void unregisterTopic(TopicToken elementID) {
        LOG.debug(".unregisterTopic(): Entry, elementID --> {}", elementID);
        topicSetCache.removeTopic(elementID);
    }

    public Set<Topic> getTopicSet() {
        LOG.debug(".getTopicSet(): Entry");
        return (topicSetCache.getTopicSet());
    }

    public Topic getTopic(FDNToken nodeID) {
        LOG.debug(".getTopic(): Entry, nodeID --> {}", nodeID);
        Topic retrievedTopic = topicSetCache.getTopic(nodeID);
        LOG.debug(".getTopic(): Exit, retrievedNode --> {}", retrievedTopic);
        return (retrievedTopic);
    }

    /**
     * This function retrieves the list (FDNTokenSet) of WUPs that are
 interested in receiving the identified uowPayloadTopicID (FDNToken).
     *
     * @param topicID The FDNToken representing the UoW (Ingres) Payload Topic
 that we want to know which WUPs are interested in
     * @return The set of WUPs wanting to receive this payload type.
     */
    public Set<NodeElementIdentifier> getSubscriberSet(TopicToken topicID) {
        LOG.debug(".getSubscriptionSetForUOWContentTopic(): Entry, topicID --> {}", topicID);
        Set<NodeElementIdentifier> subscribedTopicSet = subscriptionCache.getSubscriberSet(topicID);
        LOG.debug(".getSubscriptionSetForUOWContentTopic(): Exit");
        return (subscribedTopicSet);
    }

    /**
     * This function establishes a link between a Payload Type and a WUP that is interested in
     * processing/using it.
     * 
     * @param contentTopicID The contentTopicID (FDNToken) of the payload we have received from a WUP
     * @param interestedNode The ID of the (Topology) Node that is interested in the payload type.
     */
    @Transactional
    public void addTopicSubscriber(TopicToken contentTopicID, NodeElementIdentifier interestedNode) {
        LOG.debug(".addSubscriberToUoWContentTopic(): Entry, contentTopicID --> {}, interestedNode --> {}", contentTopicID, interestedNode);
        subscriptionCache.addSubscriber(contentTopicID, interestedNode);
        LOG.debug(".addSubscriberToUoWContentTopic(): Exit");
    }

    @Transactional
    public void removeSubscriber(TopicToken contentTopicID, NodeElementIdentifier interestedNode) {
        LOG.debug(".removeSubscriber(): Entry, contentTopicID --> {}, interestedNode --> {}", contentTopicID, interestedNode);
        subscriptionCache.removeSubscriber(contentTopicID, interestedNode);
        LOG.debug(".removeSubscriber(): Exit");
    }
}
