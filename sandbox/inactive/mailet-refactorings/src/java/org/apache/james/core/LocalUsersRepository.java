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

package org.apache.james.core;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.james.JamesMBean;
import org.apache.james.services.UsersStore;
import org.apache.mailet.User;
import org.apache.mailet.UsersRepository;

import java.util.Iterator;

/**
 * Provide access to the default "LocalUsers" UsersRepository.
 * Temporarily implements JamesMBean (formerly implemented by James) to keep backward compatibility. 
 */
public class LocalUsersRepository implements UsersRepository, Serviceable, Initializable, JamesMBean {

    private UsersStore usersStore;
    protected UsersRepository users;

    public void setUsersStore(UsersStore usersStore) {
        this.usersStore = usersStore;
    }

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager serviceManager) throws ServiceException {
        UsersStore usersStore =
           (UsersStore) serviceManager.lookup(UsersStore.ROLE);
        setUsersStore(usersStore);
    }

    /**
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        users = usersStore.getRepository("LocalUsers");
        if (users == null) {
            throw new ServiceException("","The user repository could not be found.");
        }
    }

    /**
     * @see org.apache.mailet.UsersRepository#addUser(org.apache.mailet.User)
     */
    public boolean addUser(User user) {
        return users.addUser(user);
    }
   

    /**
     * @see org.apache.mailet.UsersRepository#addUser(java.lang.String, java.lang.String)
     */
    public boolean addUser(String username, String password) {
        return users.addUser(username, password);
    }

    /**
     * @see org.apache.mailet.UsersRepository#getUserByName(java.lang.String)
     */
    public User getUserByName(String name) {
        return users.getUserByName(name);
    }

    /**
     * @see org.apache.mailet.UsersRepository#getUserByNameCaseInsensitive(java.lang.String)
     */
    public User getUserByNameCaseInsensitive(String name) {
        return users.getUserByNameCaseInsensitive(name);
    }

    /**
     * @see org.apache.mailet.UsersRepository#getRealName(java.lang.String)
     */
    public String getRealName(String name) {
        return users.getRealName(name);
    }

    /**
     * @see org.apache.mailet.UsersRepository#updateUser(org.apache.mailet.User)
     */
    public boolean updateUser(User user) {
        return users.updateUser(user);
    }

    /**
     * @see org.apache.mailet.UsersRepository#removeUser(java.lang.String)
     */
    public void removeUser(String name) {
        users.removeUser(name);
    }

    /**
     * @see org.apache.mailet.UsersRepository#contains(java.lang.String)
     */
    public boolean contains(String name) {
        return users.contains(name);
    }

    

    /**
     * @see org.apache.mailet.UsersRepository#test(java.lang.String, java.lang.String)
     */
    public boolean test(String name, String password) {
        return users.test(name,password);
    }

    /**
     * @see org.apache.mailet.UsersRepository#countUsers()
     */
    public int countUsers() {
        return users.countUsers();
    }

    /**
     * @see org.apache.mailet.UsersRepository#list()
     */
    public Iterator list() {
        return users.list();
    }

}
