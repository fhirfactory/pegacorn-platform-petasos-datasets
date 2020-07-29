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

import java.util.Map;
import net.fhirfactory.pegacorn.common.model.FDN;
import net.fhirfactory.pegacorn.common.model.FDNToken;
import net.fhirfactory.pegacorn.petasos.model.resilience.mode.ConcurrencyModeEnum;
import net.fhirfactory.pegacorn.petasos.model.resilience.mode.ResilienceModeEnum;
import net.fhirfactory.pegacorn.petasos.model.topology.*;
import net.fhirfactory.pegacorn.petasos.datasets.cache.DataSetsDM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import net.fhirfactory.pegacorn.petasos.model.dataset.DataSetElement;

/**
 * This class WILL do more in the future, but it is for now just a proxy to the
 DataSetsDM.
 */

@ApplicationScoped
public class DataSetsIM {

    private static final Logger LOG = LoggerFactory.getLogger(DataSetsIM.class);

    @Inject
    DataSetsDM dataSetCache;

//    @Inject
//    ElementNameExtensions nameExtensions;

    @Transactional
    public void registerTopic(DataSetElement newElement) {
        LOG.debug(".registerTopic(): Entry, newElement --> {}", newElement);
        dataSetCache.addTopic(newElement);
        if (newElement.hasContainingTopic()) {
            addContainedTopicToTopic(newElement.getContainingTopic(), newElement);
        }
    }

    @Transactional
    public void addContainedTopicToTopic(FDNToken topicID, DataSetElement containedTopic) {
        LOG.debug(".addContainedTopicToTopic(), nodeID --> {}, containedNode --> {}", topicID, containedTopic);
        DataSetElement containingElement = getTopic(topicID);
        if (containingElement != null) {
            LOG.trace(".addContainedTopicToTopic(): Containing Topic exists, so add contained node!");
            containingElement.addContainedTopic(containedTopic.getTopicID());
        } else {
            LOG.trace(".addContainedTopicToTopic(): Containing Topic doesn't exist, so the containedNode is actually the Top node!");
        }
    }

    @Transactional
    public void unregisterTopic(FDNToken elementID) {
        LOG.debug(".unregisterTopic(): Entry, elementID --> {}", elementID);
        dataSetCache.removeTopic(elementID);
    }

    public Set<DataSetElement> getTopicSet() {
        LOG.debug(".getTopicSet(): Entry");
        return (dataSetCache.getTopicSet());
    }

    public DataSetElement getTopic(FDNToken nodeID) {
        LOG.debug(".getTopic(): Entry, nodeID --> {}", nodeID);
        DataSetElement retrievedTopic = dataSetCache.getTopic(nodeID);
        LOG.debug(".getTopic(): Exit, retrievedNode --> {}", retrievedTopic);
        return (retrievedTopic);
    }
}
