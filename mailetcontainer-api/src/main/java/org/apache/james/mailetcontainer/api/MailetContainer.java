/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/
package org.apache.james.mailetcontainer.api;

import java.util.List;

import org.apache.mailet.Mailet;
import org.apache.mailet.Matcher;


/**
 * Interface for mailet/matcher-containing processors. This Processors will use the configured Matchers and Mailets to call them
 * in the {@link #service(org.apache.mailet.Mail)} method
 * 
 */
public interface MailetContainer extends MailProcessor{

    /**
     * retrieve mailet configuration data for introspection
     * @return List<MailetConfig>
     */
    public List<Mailet> getMailets();

    /**
     * retrieve matcher configuration data for introspection
     * @return List<MatcherConfig>
     */
    public List<Matcher> getMatchers();
    
    /**
     * Add a {@link MailetContainerListener} which will get triggered after a matcher / mailet was called
     * 
     * @param listener
     */
    public void addListener(MailetContainerListener listener);
    
    /**
     * Remove a {@link MailetContainerListener}
     * 
     * @param listener
     */
    public void removeListener(MailetContainerListener listener);
    
    /**
     * Return a unmodifiable {@link List} of {@link MailetContainerListener} which are registered 
     * 
     * @return listeners
     */
    public List<MailetContainerListener> getListeners();

}
