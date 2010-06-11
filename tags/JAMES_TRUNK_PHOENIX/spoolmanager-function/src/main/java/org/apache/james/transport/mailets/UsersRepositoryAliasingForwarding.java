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

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.james.api.user.UsersRepository;
import org.apache.james.api.user.UsersStore;
import org.apache.james.api.vut.ErrorMappingException;
import org.apache.james.api.vut.VirtualUserTable;
import org.apache.mailet.MailAddress;

/**
 * Receives a Mail from JamesSpoolManager and takes care of delivery of the
 * message to local inboxes.
 * 
 * Available configurations are:
 * 
 * <enableAliases>true</enableAliases>: specify wether the user aliases should
 * be looked up or not. Default is false.
 * 
 * <enableForwarding>true</enableForwarding>: enable the forwarding. Default to
 * false.
 * 
 * <usersRepository>LocalAdmins</usersRepository>: specific users repository
 * name. Default to empty. If empty does lookup the default userRepository.
 */
public class UsersRepositoryAliasingForwarding extends AbstractVirtualUserTableMailet {

    /**
     * The user repository for this mail server. Contains all the users with
     * inboxes on this server.
     */
    private UsersRepository usersRepository;
    private UsersStore usersStore;

    /**
     * Return a string describing this mailet.
     * 
     * @return a string describing this mailet
     */
    public String getMailetInfo() {
        return "Local User Aliasing and Forwarding Mailet";
    }

    @Resource(name="localusersrepository")
    public void setUsersRepository(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }
    
    @Resource(name="users-store")
    public void setUsersStore(UsersStore usersStore) {
        this.usersStore = usersStore;
    }
    
    /**
     * Return null when the mail should be GHOSTed, the username string when it
     * should be changed due to the ignoreUser configuration.
     * 
     * @param sender
     * @param recipient
     * @param message
     * @throws MessagingException
     */
    public Collection<MailAddress> processMail(MailAddress sender, MailAddress recipient,
            MimeMessage message) throws MessagingException {
        if (recipient == null) {
            throw new IllegalArgumentException(
                    "Recipient for mail to be spooled cannot be null.");
        }
        if (message == null) {
            throw new IllegalArgumentException(
                    "Mail message to be spooled cannot be null.");
        }

        if (usersRepository instanceof VirtualUserTable) {
            Collection<String> mappings;
            try {
                mappings = ((VirtualUserTable) usersRepository).getMappings(recipient.getLocalPart(), recipient.getDomain());
            } catch (ErrorMappingException e) {
                StringBuilder errorBuffer = new StringBuilder(128)
                    .append("A problem as occoured trying to alias and forward user ")
                    .append(recipient)
                    .append(": ")
                    .append(e.getMessage());
                    throw new MessagingException(errorBuffer.toString());
            }
            
            if (mappings != null) {
                return handleMappings(mappings, sender, recipient, message);
            }
        } else {
            StringBuilder errorBuffer = new StringBuilder(128)
                .append("Warning: the repository ")
                .append(usersRepository.getClass().getName())
                .append(" does not implement VirtualUserTable interface).");
            getMailetContext().log(errorBuffer.toString());
        }
        String realName = usersRepository.getRealName(recipient.getLocalPart());
      
        ArrayList<MailAddress> ret = new ArrayList<MailAddress>();
        if (realName != null) {
            ret.add(new MailAddress(realName, recipient.getDomain()));
            return ret;
        } else {
            ret.add(recipient);
            return ret;
        }
    }

    
    /**
     * @see org.apache.mailet.GenericMailet#init()
     */
    public void init() throws MessagingException {
        super.init();
        String userRep = getInitParameter("usersRepository");
        if (userRep != null && userRep.length() > 0) {
            usersRepository = usersStore.getRepository(userRep);
        }

    }

}
