/*
 * Copyright (c) 2020 MAHun
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

import ca.uhn.fhir.rest.annotation.Transaction;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.common.model.FDNToken;
import net.fhirfactory.pegacorn.common.model.FDNTokenSet;

import javax.inject.Singleton;
import net.fhirfactory.pegacorn.petasos.model.topics.TopicToken;

@Singleton
public class TopicSubscriptionMapDM {
	private static final Logger LOG = LoggerFactory.getLogger(TopicSubscriptionMapDM.class);
	
	ConcurrentHashMap<TopicToken, FDNTokenSet> distributionList;
	
    public TopicSubscriptionMapDM(){
        distributionList = new ConcurrentHashMap<TopicToken, FDNTokenSet>();
    }

    /**
     * This function retrieves the list (FDNTokenSet) of WUPs that are interested in 
     * receiving the identified uowPayloadTopicID (FDNToken).
     * 
     * @param topicID The FDNToken representing the UoW (Ingres) Payload Topic that we want to know which WUPs are interested in
     * @return The set of WUPs wanting to receive this payload type.
     */
     public FDNTokenSet getSubscriberSet(TopicToken topicID){
    	LOG.debug(".getSubscriberSet(): Entry, topicID --> {}", topicID);
    	if(distributionList.isEmpty()) {
    		LOG.debug("getSubscriberSet(): Exit, empty list so can't match");
    		return(null);
    	}
    	if(this.distributionList.containsKey(topicID)) {
    		FDNTokenSet interestedWUPSet = this.distributionList.get(topicID);
    		LOG.debug(".getSubscriberSet(): Exit, returning associated FDNSet of the WUPs interested --> {}", interestedWUPSet);
    		return(interestedWUPSet);
    	}
    	LOG.debug(".getSubscriberSet(): Couldn't find any associated FDNTokenSet elements (i.e. couldn't find any interested WUPs, returning null");
    	return(null);
    }
    
    /**
     * This function establishes a link between a Payload Type and a WUP that is interested in
     * processing/using it.
     * 
     * @param topic The contentTopicID (FDNToken) of the payload we have received from a WUP
     * @param subscriberInstanceID The ID of the WUP that is interested in the payload type.
     */
    @Transaction
    public void addSubscriber(TopicToken topic, FDNToken subscriberInstanceID) {
    	LOG.debug(".addSubscriber(): Entry, topic --> {}, subscriberInstanceID --> {}", topic, subscriberInstanceID);
    	if((topic==null) || (subscriberInstanceID==null)) {
    		throw(new IllegalArgumentException(".addSubscriber(): topic or subscriberInstanceID is null"));
    	}
    	if(this.distributionList.containsKey(topic)) {
    		LOG.trace(".addSubscriber(): Removing existing map for topic --> {}", topic);
    		FDNTokenSet payloadDistributionList = this.distributionList.get(topic);
    		payloadDistributionList.addElement(subscriberInstanceID);
    	} else {
    		FDNTokenSet newPayloadDistributionList = new FDNTokenSet();
    		newPayloadDistributionList.addElement(subscriberInstanceID);
    		this.distributionList.put(topic, newPayloadDistributionList);
    	}
    	LOG.debug(".addSubscriber(): Exit, assigned the interestedWUP to the contentTopicID");
    }
    
    /**
     * Remove a Subscriber from the Topic Subscription list
     * 
     * @param topic The TopicToken of the Topic we want to unsubscribe from.
     * @param subscriberInstanceID  The subscriber we are removing from the subscription list.
     */
    @Transaction
    public void removeSubscriber(TopicToken topic, FDNToken subscriberInstanceID) {
    	LOG.debug(".removeSubscriber(): Entry, topic --> {}, subscriberInstanceID --> {}", topic, subscriberInstanceID);
    	if((topic==null) || (subscriberInstanceID==null)) {
    		throw(new IllegalArgumentException(".removeSubscriber(): topic or subscriberInstanceID is null"));
    	}
    	if(this.distributionList.containsKey(topic)) {
    		LOG.trace(".removeSubscriber(): Removing existing map for topic --> {}", topic);
    		FDNTokenSet payloadDistributionList = this.distributionList.get(topic);
    		payloadDistributionList.removeElement(subscriberInstanceID);
    	} else {
    		LOG.trace(".removeSubscriber(): Not such Topic in the Cache for topic --> {}", topic);
    	}
    	LOG.debug(".removeSubscriber(): Exit, removed the subscriberInstanceID from the topic");
    }

}
