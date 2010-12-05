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
package org.apache.james.socket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.james.lifecycle.Configurable;
import org.apache.james.protocols.api.ConnectHandler;
import org.apache.james.protocols.api.ConnectHandlerResultHandler;
import org.apache.james.protocols.api.ExtensibleHandler;
import org.apache.james.protocols.api.ProtocolSession;
import org.apache.james.protocols.api.WiringException;

public abstract class AbstractConnectHandlerResultJMXMonitor<S extends ProtocolSession> implements ConnectHandlerResultHandler<S>, ExtensibleHandler, Configurable{

    private Map<String, ConnectHandlerStats> cStats = new HashMap<String, ConnectHandlerStats>();
    private String jmxPath;

    /*
     * (non-Javadoc)
     * @see org.apache.james.protocols.api.ConnectHandlerResultHandler#onResponse(org.apache.james.protocols.api.ProtocolSession, boolean, long, org.apache.james.protocols.api.ConnectHandler)
     */
    public boolean onResponse(ProtocolSession session, boolean response, long executionTime, ConnectHandler<S> handler) {
        cStats.get(handler.getClass().getName()).increment(response);
        return response;     
    }

    /*
     * (non-Javadoc)
     * @see org.apache.james.protocols.api.ExtensibleHandler#getMarkerInterfaces()
     */
    public List<Class<?>> getMarkerInterfaces() {
        List<Class<?>> marker = new ArrayList<Class<?>>();
        marker.add(ConnectHandler.class);

        return marker;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.james.protocols.api.ExtensibleHandler#wireExtensions(java.lang.Class, java.util.List)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void wireExtensions(Class<?> interfaceName, List<?> extension) throws WiringException {
        if (interfaceName.equals(ConnectHandler.class)) {
            // add stats for all hooks
            for (int i = 0; i < extension.size(); i++ ) {
                ConnectHandler c =  (ConnectHandler) extension.get(i);
                if (equals(c) == false) {
                    String cName = c.getClass().getName();
                    try {
                        cStats.put(cName, new ConnectHandlerStats(jmxPath, cName));
                    } catch (Exception e) {
                        throw new WiringException("Unable to wire Hooks",  e);
                    }
                }
            }
        }
    }
    
    /**
     * Return the default JMXPath to use if none is configured
     * 
     * @return defaultJMXPath
     */
    protected abstract String getDefaultJMXPath();

    /*
     * (non-Javadoc)
     * @see org.apache.james.lifecycle.Configurable#configure(org.apache.commons.configuration.HierarchicalConfiguration)
     */
    public void configure(HierarchicalConfiguration config) throws ConfigurationException {
        this.jmxPath = config.getString("jmxPath", getDefaultJMXPath());
    }
    

}