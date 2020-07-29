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

import java.util.Enumeration;
import java.util.HashMap;

import net.fhirfactory.pegacorn.common.model.FDNToken;
import net.fhirfactory.pegacorn.petasos.model.topology.EndpointElement;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElement;
import net.fhirfactory.pegacorn.petasos.model.topology.LinkElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;

import net.fhirfactory.pegacorn.common.model.FDN;
import net.fhirfactory.pegacorn.common.model.RDN;
import net.fhirfactory.pegacorn.petasos.model.dataset.DataSetElement;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementInstanceTypeEnum;


/**
 * 
 * @author Mark A. Hunter
 * @since 2020-07-01
 * 
 */
@ApplicationScoped
public class DataSetsDM {

    private static final Logger LOG = LoggerFactory.getLogger(DataSetsDM.class);

    private ConcurrentHashMap<FDNToken, DataSetElement> dataSetCache;

    public DataSetsDM() {
        this.dataSetCache = new ConcurrentHashMap<FDNToken, DataSetElement>();
    }

    /**
     * This function adds an entry to the Element Set.
     * <p>
     * Note that the default behaviour is to UPDATE the values with the set if
     * there already exists an instance for the specified FDNToken (identifier).
     *
     * @param newElement The NodeElement to be added to the Set
     */
    @Transactional
    public void addTopic(DataSetElement newElement) {
        LOG.debug(".addTopic(): Entry, newElement --> {}", newElement);
        if (newElement == null) {
            throw (new IllegalArgumentException(".addTopic(): newElement is null"));
        }
        if (!newElement.hasTopicID()) {
            throw (new IllegalArgumentException(".addTopic(): bad elementID within newElement"));
        }
        if (this.dataSetCache.containsKey(newElement.getTopicID())) {
            this.dataSetCache.replace(newElement.getTopicID(), newElement);
        } else {
            this.dataSetCache.put(newElement.getTopicID(), newElement);
        }
    }

    /**
     * 
     * @param elementID the Topic to be removed
     * 
     * TODO Robustness issue - Need to address the scenario where the topic has contained topics
     */
    @Transactional
    public void removeTopic(FDNToken elementID) {
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

    public Set<DataSetElement> getTopicSet() {
        LOG.debug(".getTopicSet(): Entry");
        LinkedHashSet<DataSetElement> elementSet = new LinkedHashSet<DataSetElement>();
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

    public DataSetElement getTopic(FDNToken topicID) {
        LOG.debug(".getTopic(): Entry, nodeID --> {}", topicID);
        if (topicID == null) {
            LOG.debug(".getTopic(): Exit, provided a null nodeID , so returning null");
            return (null);
        }
        if (this.dataSetCache.containsKey(topicID)) {
            LOG.trace(".getTopic(): Element found!!! WooHoo!");
            DataSetElement retrievedElement = this.dataSetCache.get(topicID);
            LOG.debug(".getTopic(): Exit, returning element --> {}", retrievedElement);
            return (retrievedElement);
        } else {
            LOG.trace(".getTopic(): Couldn't find element!");
            LOG.debug(".getTopic(): Exit, returning null as an element with the specified ID was not in the map");
            return (null);
        }
    }

    public Map<Integer, DataSetElement> getTopicContainmentHierarchy(FDNToken topicID) {
        LOG.debug(".getTopicContainmentHierarchy(): Entry, nodeID --> {}", topicID);
        HashMap<Integer, DataSetElement> topicHierarchy = new HashMap<Integer, DataSetElement>();
        if (topicID == null) {
            return (topicHierarchy);
        }
        boolean hasContainer = true;
        int counter = 0;
        FDNToken currentTopic = topicID;
        while (hasContainer) {
            DataSetElement currentElement = dataSetCache.get(currentTopic);
            if (currentElement == null) {
                hasContainer = false;
            } else {
                topicHierarchy.put(counter, currentElement);
                counter++;
                if (!currentElement.hasContainingTopic()) {
                    hasContainer = false;
                } else {
                    currentTopic = currentElement.getContainingTopic();
                }
            }
        }
        if(LOG.isDebugEnabled()) {
            LOG.debug(".getTopicContainmentHierarchy(): Exit, retrieved Heirarchy, depth --> {}", topicHierarchy.size());
        }
        return(topicHierarchy);
    }
}
