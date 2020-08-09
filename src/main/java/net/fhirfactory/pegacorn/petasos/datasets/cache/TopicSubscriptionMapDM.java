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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.enterprise.context.ApplicationScoped;

import net.fhirfactory.pegacorn.petasos.model.topology.NodeElement;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.common.model.FDNToken;

import net.fhirfactory.pegacorn.petasos.model.topics.TopicToken;

@ApplicationScoped
public class TopicSubscriptionMapDM {
	private static final Logger LOG = LoggerFactory.getLogger(TopicSubscriptionMapDM.class);
	
	ConcurrentHashMap<TopicToken, Set<NodeElementIdentifier>> distributionList;
	
    public TopicSubscriptionMapDM(){
        distributionList = new ConcurrentHashMap<TopicToken, Set<NodeElementIdentifier>>();
    }

    /**
     * This function retrieves the list (FDNTokenSet) of WUPs that are interested in 
 receiving the identified uowPayloadTopicID (FDNToken).
     * 
     * @param topicID The FDNToken representing the UoW (Ingres) Payload Topic that we want to know which WUPs are interested in
     * @return The set of WUPs wanting to receive this payload type.
     */
     public Set<NodeElementIdentifier> getSubscriberSet(TopicToken topicID){
    	LOG.debug(".getSubscriberSet(): Entry");
    	if(LOG.isDebugEnabled()){
    		if(topicID != null){
				LOG.debug(".getSubscriberSet(): topicID (TopicToken).identifier --> {}", topicID.getIdentifier());
				LOG.debug(".getSubscriberSet(): topicID (TopicToken).version --> {}", topicID.getVersion());
			}
		}
    	if(distributionList.isEmpty()) {
    		LOG.debug("getSubscriberSet(): Exit, empty list so can't match");
    		return(new HashSet<NodeElementIdentifier>());
    	}
   		Set<NodeElementIdentifier> interestedWUPSet = this.distributionList.get(topicID);
    	if(interestedWUPSet == null ){
			LOG.debug(".getSubscriberSet(): Couldn't find any associated FDNTokenSet elements (i.e. couldn't find any interested WUPs), returning an empty set");
			return(new HashSet<NodeElementIdentifier>());
    	} else {
    		if(LOG.isDebugEnabled()) {
				LOG.debug(".getSubscriberSet(): Exit, returning associated FDNSet of the WUPs interested:");
				int count = 0;
				Iterator<NodeElementIdentifier> nodeIterator = interestedWUPSet.iterator();
				while(nodeIterator.hasNext()){
					LOG.debug(".getSubscriberSet(): Interested Node [{}] Identifier --> {}", count, nodeIterator.next());
					count++;
				}
			}
			return (interestedWUPSet);
		}
    }
    
    /**
     * This function establishes a link between a Payload Type and a WUP that is interested in
     * processing/using it.
     * 
     * @param topic The contentTopicID (FDNToken) of the payload we have received from a WUP
     * @param subscriberNode The NodeElement of the WUP that is interested in the payload type.
     */
    @Transaction
    public void addSubscriber(TopicToken topic, NodeElementIdentifier subscriberNode) {
    	if(LOG.isDebugEnabled()){
    		LOG.debug(".addSubscriber(): Entry");
			if(topic != null){
				LOG.debug(".getSubscriberSet(): topicID (TopicToken).identifier --> {}", topic.getIdentifier());
				LOG.debug(".getSubscriberSet(): topicID (TopicToken).version --> {}", topic.getVersion());
			} else {
				LOG.debug(".getSubscriberSet(): topicID (TopicToken).xxx is null");
			}
			LOG.debug(".getSubscriberSet(): subscriberNode (NodeElementIdentifier) --> {}", subscriberNode);
		}
    	if((topic==null) || (subscriberNode==null)) {
    		throw(new IllegalArgumentException(".addSubscriber(): topic or subscriberInstanceID is null"));
    	}
		Set<NodeElementIdentifier> interestedWUPSet = this.distributionList.get(topic);
		if(interestedWUPSet != null) {
    		LOG.trace(".addSubscriber(): Adding subscriber to existing map for topic --> {}", topic);
			interestedWUPSet.add(subscriberNode);
    	} else {
			LOG.trace(".addSubscriber(): Topic Subscription Map: Created new Distribution List and Added Subscriber");
			interestedWUPSet = new LinkedHashSet<NodeElementIdentifier> ();
			interestedWUPSet.add(subscriberNode);
    		this.distributionList.put(topic, interestedWUPSet);
    	}
		if(LOG.isDebugEnabled()) {
			LOG.debug(".addSubscriber(): Exit, here is the Subscription list for the Topic:");
			int count = 0;
			Iterator<NodeElementIdentifier> nodeIterator = interestedWUPSet.iterator();
			while(nodeIterator.hasNext()){
				LOG.debug(".addSubscriber(): Interested Node [{}] Identifier --> {}", count, nodeIterator.next());
				count++;
			}
		}
    }
    
    /**
     * Remove a Subscriber from the Topic Subscription list
     * 
     * @param topic The TopicToken of the Topic we want to unsubscribe from.
     * @param subscriberInstanceID  The subscriber we are removing from the subscription list.
     */
    @Transaction
    public void removeSubscriber(TopicToken topic, NodeElementIdentifier subscriberInstanceID) {
    	LOG.debug(".removeSubscriber(): Entry, topic --> {}, subscriberInstanceID --> {}", topic, subscriberInstanceID);
    	if((topic==null) || (subscriberInstanceID==null)) {
    		throw(new IllegalArgumentException(".removeSubscriber(): topic or subscriberInstanceID is null"));
    	}
		boolean found = false;
		TopicToken currentToken = null;
		Enumeration<TopicToken> topicEnumerator = distributionList.keys();
		while(topicEnumerator.hasMoreElements()){
			currentToken = topicEnumerator.nextElement();
			if(currentToken.equals(topic)){
				LOG.trace(".removeSubscriber(): Found Topic in Subscription Cache");
				found = true;
				break;
			}
		}
		if(found) {
    		LOG.trace(".removeSubscriber(): Removing Subscriber from topic --> {}", topic);
			Set<NodeElementIdentifier> payloadDistributionList = this.distributionList.get(currentToken);
			Iterator<NodeElementIdentifier> nodeIterator = payloadDistributionList.iterator();
			while(nodeIterator.hasNext()){
				NodeElementIdentifier currentNode = nodeIterator.next();
				if(currentNode.equals(subscriberInstanceID)){
					LOG.trace(".removeSubscriber(): Found Subscriber in Subscription List, removing");
					payloadDistributionList.remove(currentNode);
					LOG.debug(".removeSubscriber(): Exit, removed the subscriberInstanceID from the topic");
					LOG.trace("Topic Subscription Map: (Remove Subscriber) Topic [{}] <-- Subscriber [{}]", currentToken, subscriberInstanceID);
					break;
				}
			}
    	} else {
    		LOG.debug(".removeSubscriber(): Exit, Could not find Subscriber in Subscriber Cache for Topic");
    		return;
    	}
		LOG.debug(".removeSubscriber(): Exit, Could not find Topic in Subscriber Cache");
    }

    public void printAllSubscriptionSets(){
    	if(!LOG.isDebugEnabled()){
    		return;
		}
    	Enumeration<TopicToken> topicEnumerator = distributionList.keys();
    	LOG.debug(".printAllSubscriptionSets(): Printing ALL Subscription Lists");
    	while(topicEnumerator.hasMoreElements()){
    		TopicToken currentToken = topicEnumerator.nextElement();
    		LOG.debug(".printAllSubscriptionSets(): Topic (TopicToken) --> {}", currentToken);
			Set<NodeElementIdentifier> subscribers = getSubscriberSet(currentToken);
			if(subscribers != null){
				Iterator<NodeElementIdentifier> currentNodeIdentifierIterator = subscribers.iterator();
				while(currentNodeIdentifierIterator.hasNext()){
					LOG.debug(".printAllSubscriptionSets(): Subscriber --> {}", currentNodeIdentifierIterator.next());
				}
			}

		}
	}

}
