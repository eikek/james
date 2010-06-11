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



import org.apache.mailet.MailAddress;
import org.apache.mailet.MailetException;
import org.apache.mailet.UsersRepository;

/**
 * Adds or removes an email address to a listserv.
 *
 * <p>Sample configuration:
 * <br>&lt;mailet match="CommandForListserv=james@list.working-dogs.com" class="AvalonListservManager"&gt;
 * <br>&lt;repositoryName&gt;name of user repository configured in UsersStore block &lt;/repositoryName&gt;
 * <br>&lt;/mailet&gt;
 *
 * @version This is $Revision$
 */
public class AvalonListservManager extends GenericListservManager {

    private UsersRepository members;

    /**
     * Initialize the mailet
     * @throws MailetException 
     */
    public void init() throws MailetException {
       
            String repName = getInitParameter("repositoryName");

            members = getMailetContext().getUsersRepository(repName);
      
    }

    /**
     * Add an address to the list.
     *
     * @param address the address to add
     *
     * @return true if successful, false otherwise
     */
    public boolean addAddress(MailAddress address) {
        members.addUser(address.toString(), "");
        return true;
    }

    /**
     * Remove an address from the list.
     *
     * @param address the address to remove
     *
     * @return true if successful, false otherwise
     */
    public boolean removeAddress(MailAddress address) {
        members.removeUser(address.toString());
        return true;
    }

    public boolean existsAddress(MailAddress address) {
        return members.contains(address.toString());
    }

    /**
     * Return a string describing this mailet.
     *
     * @return a string describing this mailet
     */
    public String getMailetInfo() {
        return "AvalonListservManager Mailet";
    }
}

