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



package org.apache.james.userrepository;

import org.apache.mailet.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * A Jdbc-backed UserRepository which handles User instances
 * of the <CODE>DefaultUser</CODE> class.
 * Although this repository can handle subclasses of DefaultUser,
 * like <CODE>DefaultJamesUser</CODE>, only properties from
 * the DefaultUser class are persisted.
 * 
 * TODO Please note that default configuration uses JamesUsersJdbcRepository
 * instead of this class. So we could also delete this implementation.
 * 
 */
public class DefaultUsersJdbcRepository extends AbstractJdbcUsersRepository
{
    /**
     * @see org.apache.james.userrepository.AbstractJdbcUsersRepository#readUserFromResultSet(java.sql.ResultSet)
     */
    protected User readUserFromResultSet(ResultSet rsUsers) throws SQLException 
    {
        // Get the username, and build a DefaultUser with it.
        String username = rsUsers.getString(1);
        String passwordHash = rsUsers.getString(2);
        String passwordAlg = rsUsers.getString(3);
        DefaultUser user = new DefaultUser(username, passwordHash, passwordAlg);
        return user;
    }

    /**
     * @see org.apache.james.userrepository.AbstractJdbcUsersRepository#setUserForInsertStatement(org.apache.mailet.User, java.sql.PreparedStatement)
     */
    protected void setUserForInsertStatement(User user, 
                                             PreparedStatement userInsert) 
        throws SQLException 
    {
        DefaultUser defUser = (DefaultUser)user;
        userInsert.setString(1, defUser.getUserName());
        userInsert.setString(2, defUser.getHashedPassword());
        userInsert.setString(3, defUser.getHashAlgorithm());
    }

    /**
     * @see org.apache.james.userrepository.AbstractJdbcUsersRepository#setUserForUpdateStatement(org.apache.mailet.User, java.sql.PreparedStatement)
     */
    protected void setUserForUpdateStatement(User user, 
                                             PreparedStatement userUpdate) 
        throws SQLException 
    {
        DefaultUser defUser = (DefaultUser)user;
        userUpdate.setString(1, defUser.getHashedPassword());
        userUpdate.setString(2, defUser.getHashAlgorithm());
        userUpdate.setString(3, defUser.getUserName());
    }
    
    /**
     * @see org.apache.mailet.UsersRepository#addUser(java.lang.String, java.lang.String)
     */
    public boolean addUser(String username, String password)  {
        User newbie = new DefaultUser(username, "SHA");
        newbie.setPassword(password);
        return addUser(newbie);
    }


}

