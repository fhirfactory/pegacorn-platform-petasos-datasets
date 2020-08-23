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
package net.fhirfactory.pegacorn.petasos.datasets.cache;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.common.model.FDNToken;
import net.fhirfactory.pegacorn.petasos.model.topics.Topic;
import net.fhirfactory.pegacorn.petasos.model.topics.TopicToken;

/**
 *
 * @author Mark A. Hunter
 * @since 2020-07-01
 *
 */
@ApplicationScoped
public class TopicCacheDM {

    private static final Logger LOG = LoggerFactory.getLogger(TopicCacheDM.class);

    private ConcurrentHashMap<TopicToken, Topic> dataSetCache;

    public TopicCacheDM() {
        this.dataSetCache = new ConcurrentHashMap<TopicToken, Topic>();
    }

    /**
     * This function adds an entry to the Element Set.
     * <p>
 Note that the default behaviour is to UPDATE the values with the set if
 there already exists an instance for the specified FDNToken (identifier).
     *
     * @param newTopic The NodeElement to be added to the Set
     */
    @Transactional
    public void addTopic(Topic newTopic) {
        LOG.debug(".addTopic(): Entry, newTopic (TopicToken) --> {}", newTopic);
        if (newTopic == null) {
            throw (new IllegalArgumentException(".addTopic(): newTopic is null"));
        }
        if (!newTopic.hasIdentifier()) {
            throw (new IllegalArgumentException(".addTopic(): bad Identifier within newTopic"));
        }
        TopicToken newToken = newTopic.getTopicToken();
        if (this.dataSetCache.containsKey(newToken)) {
            this.dataSetCache.replace(newToken, newTopic);
        } else {
            this.dataSetCache.put(newToken, newTopic);
        }
    }

    /**
     *
     * @param elementID the Topic to be removed
     *
     * TODO Robustness issue - Need to address the scenario where the topic has
     * contained topics
     */
    @Transactional
    public void removeTopic(TopicToken elementID) {
        LOG.debug(".removeTopic(): Entry, elementID --> {}", elementID);
        if (elementID == null) {
            throw (new IllegalArgumentException(".removeNode(): elementID is null"));
        }
        if (this.dataSetCache.containsKey(elementID)) {
            LOG.trace(".removeTopic(): Element found, now removing it...");
            this.dataSetCache.remove(elementID);
        } else {
            LOG.trace(".removeTopic(): No element with that elementID is in the map");
        }
        LOG.debug(".removeTopic(): Exit");
    }

    public Set<Topic> getTopicSet() {
        LOG.debug(".getTopicSet(): Entry");
        LinkedHashSet<Topic> elementSet = new LinkedHashSet<Topic>();
        if (this.dataSetCache.isEmpty()) {
            LOG.debug(".getTopicSet(): Exit, The topic map is empty, returning null");
            return (null);
        }
        elementSet.addAll(this.dataSetCache.values());
        if (LOG.isDebugEnabled()) {
            LOG.debug(".getTopicSet(): Exit, returning an element set, size --> {}", elementSet.size());
        }
        return (elementSet);
    }

    public Topic getTopic(FDNToken topicID) {
        LOG.debug(".getTopic(): Entry, nodeID --> {}", topicID);
        if (topicID == null) {
            LOG.debug(".getTopic(): Exit, provided a null nodeID , so returning null");
            return (null);
        }
        if (this.dataSetCache.containsKey(topicID)) {
            LOG.trace(".getTopic(): Element found!!! WooHoo!");
            Topic retrievedElement = this.dataSetCache.get(topicID);
            LOG.debug(".getTopic(): Exit, returning element --> {}", retrievedElement);
            return (retrievedElement);
        } else {
            LOG.trace(".getTopic(): Couldn't find element!");
            LOG.debug(".getTopic(): Exit, returning null as an element with the specified ID was not in the map");
            return (null);
        }
    }

    public Map<Integer, Topic> getTopicContainmentHierarchy(FDNToken topicID) {
        LOG.debug(".getTopicContainmentHierarchy(): Entry, nodeID --> {}", topicID);
        HashMap<Integer, Topic> topicHierarchy = new HashMap<Integer, Topic>();
        if (topicID == null) {
            return (topicHierarchy);
        }
        boolean hasContainer = true;
        int counter = 0;
        FDNToken currentTopic = topicID;
        while (hasContainer) {
            Topic currentElement = dataSetCache.get(currentTopic);
            if (currentElement == null) {
                hasContainer = false;
            } else {
                topicHierarchy.put(counter, currentElement);
                counter++;
                if (!currentElement.hasContainingDataset()) {
                    hasContainer = false;
                } else {
                    currentTopic = currentElement.getContainingDataset();
                }
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(".getTopicContainmentHierarchy(): Exit, retrieved Heirarchy, depth --> {}", topicHierarchy.size());
        }
        return (topicHierarchy);
    }
}
