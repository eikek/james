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



package org.apache.james.transport.mailets;

import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.mailet.GenericMailet;
import org.apache.mailet.Mail;

/**
 * Convert attributes to headers
 * 
 * Sample configuration:
 * 
 * &lt;mailet match="All" class="MailAttributesToMimeHeaders"&gt;
 * &lt;simplemapping&gt;org.apache.james.attribute1;
 * headerName1&lt;/simplemapping&gt;
 * &lt;simplemapping&gt;org.apache.james.attribute2;
 * headerName2&lt;/simplemapping&gt; &lt;/mailet&gt;
 * 
 */
public class MailAttributesToMimeHeaders extends GenericMailet {

    /**
     * HashMap which holds the attributeName and headerName
     */
    private HashMap map = new HashMap();

    /**
     * @see org.apache.mailet.GenericMailet#init()
     */
    public void init() throws MessagingException {
        String simplemappings = getInitParameter("simplemapping");
        if (simplemappings != null) {

            StringTokenizer st = new StringTokenizer(simplemappings, ",");
            while (st.hasMoreTokens()) {

                String parameters[] = st.nextToken().split(";");

                // Check if we have a valid config
                if (parameters.length > 2 || parameters.length < 2) {
                    throw new MessagingException(
                            "Invalid config. Please use \"attributeName; headerName\"");
                } else {
                    // Add it to the map
                    map.put(parameters[0].trim(), parameters[1].trim());
                }
            }
        } else {
            throw new MessagingException(
                    "Invalid config. Please use \"attributeName; headerName\"");
        }
    }

    /**
     * @see org.apache.mailet.GenericMailet#service(Mail)
     */
    public void service(Mail mail) {
        MimeMessage message;
        try {
            message = mail.getMessage();

            Iterator keys = map.keySet().iterator();

            while (keys.hasNext()) {
                String key = keys.next().toString();
                String value = (String) mail.getAttribute(key);
                String headerName = map.get(key).toString();

                // Check if we have all needed values
                if (headerName != null && value != null) {
                    // Add the header
                    message.setHeader(headerName, value);
                }
            }
            message.saveChanges();
        } catch (MessagingException e) {
            log(e.getMessage());
        }
    }

}
