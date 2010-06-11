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




package org.apache.james.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.avalon.framework.logger.Logger;
import org.apache.james.services.DNSServer;

/**
 * 
 * Util class for the DomainList interface
 */
public class DomainListUtil {
    
    /**
     * Return a List which holds all ipAddress of the domains in the given List
     * 
     * @param domains List of domains
     * @return domainIP List of ipaddress for domains
     */
    public static List getDomainsIP(List domains,DNSServer dns,Logger log) {
        List domainIP = new ArrayList();
        if (domains.size() > 0 ) {
            for (int i = 0; i < domains.size(); i++) {
                domainIP.addAll(getDomainIP(domains.get(i).toString(),dns,log));
            }
        }
        return domainIP;    
    }
    
    /**
     * @see #getDomainsIP(List)
     */
    public static List getDomainIP(String domain, DNSServer dns, Logger log) {
        List domainIP = new ArrayList();
        try {
            InetAddress[]  addrs = dns.getAllByName(domain);
            for (int j = 0; j < addrs.length ; j++) {
                domainIP.add(addrs[j].getHostAddress());
            }
        } catch (UnknownHostException e) {
            log.error("Cannot get IP address(es) for " + domain);
        }
        return domainIP;
    }
}
