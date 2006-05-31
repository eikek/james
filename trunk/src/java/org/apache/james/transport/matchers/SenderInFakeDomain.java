/***********************************************************************
 * Copyright (c) 2000-2006 The Apache Software Foundation.             *
 * All rights reserved.                                                *
 * ------------------------------------------------------------------- *
 * Licensed under the Apache License, Version 2.0 (the "License"); you *
 * may not use this file except in compliance with the License. You    *
 * may obtain a copy of the License at:                                *
 *                                                                     *
 *     http://www.apache.org/licenses/LICENSE-2.0                      *
 *                                                                     *
 * Unless required by applicable law or agreed to in writing, software *
 * distributed under the License is distributed on an "AS IS" BASIS,   *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or     *
 * implied.  See the License for the specific language governing       *
 * permissions and limitations under the License.                      *
 ***********************************************************************/

package org.apache.james.transport.matchers;

import org.apache.mailet.Mail;

import java.util.Collection;

/**
 * Does a DNS lookup (MX and A/CNAME records) on the sender's domain.  If
 * there are no entries, the domain is considered fake and the match is 
 * successful.
 *
 */
public class SenderInFakeDomain extends AbstractNetworkMatcher {

    public Collection match(Mail mail) {
        if (mail.getSender() == null) {
            return null;
        }
        String domain = mail.getSender().getHost();
        //DNS Lookup for this domain
        Collection servers = getMailetContext().getMailServers(domain);
        if (servers.size() == 0) {
            //No records...could not deliver to this domain, so matches criteria.
            log("No MX, A, or CNAME record found for domain: " + domain);
            return mail.getRecipients();
        } else if (matchNetwork(servers.iterator().next().toString())){
            /*
             * It could be a wildcard address like these:
             *
             * 64.55.105.9/32          # Allegiance Telecom Companies Worldwide (.nu)
             * 64.94.110.11/32         # VeriSign (.com .net)
             * 194.205.62.122/32       # Network Information Center - Ascension Island (.ac)
             * 194.205.62.62/32        # Internet Computer Bureau (.sh)
             * 195.7.77.20/32          # Fredrik Reutersward Data (.museum)
             * 206.253.214.102/32      # Internap Network Services (.cc)
             * 212.181.91.6/32         # .NU Domain Ltd. (.nu)
             * 219.88.106.80/32        # Telecom Online Solutions (.cx)
             * 194.205.62.42/32        # Internet Computer Bureau (.tm)
             * 216.35.187.246/32       # Cable & Wireless (.ws)
             * 203.119.4.6/32          # .PH TLD (.ph)
             *
             */
            log("Banned IP found for domain: " + domain);
            log(" --> :" + servers.iterator().next().toString());
            return mail.getRecipients();
        } else {
            // Some servers were found... the domain is not fake.

            return null;
        }
    }
}
