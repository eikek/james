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
package org.apache.james.smtpserver.core;

import java.util.Locale;

import javax.annotation.Resource;

import org.apache.james.dsn.DSNStatus;
import org.apache.james.services.MailServer;
import org.apache.james.smtpserver.SMTPRetCode;
import org.apache.james.smtpserver.SMTPSession;
import org.apache.james.smtpserver.hook.HookResult;
import org.apache.james.smtpserver.hook.HookReturnCode;
import org.apache.james.smtpserver.hook.RcptHook;
import org.apache.mailet.MailAddress;

/**
 * Handler which check if the authenticated user is incorrect
 */
public class SenderAuthIdentifyVerificationRcptHook implements RcptHook {

    private MailServer mailServer;
    
    /**
     * Gets the mail server.
     * @return the mailServer
     */
    public final MailServer getMailServer() {
        return mailServer;
    }

    /**
     * Sets the mail server.
     * @param mailServer the mailServer to set
     */
    @Resource(name="James")
    public final void setMailServer(MailServer mailServer) {
        this.mailServer = mailServer;
    }
    
    /**
     * @see org.apache.james.smtpserver.hook.RcptHook#doRcpt(org.apache.james.smtpserver.SMTPSession,
     *      org.apache.mailet.MailAddress, org.apache.mailet.MailAddress)
     */
    public HookResult doRcpt(SMTPSession session, MailAddress sender,
            MailAddress rcpt) {
        if (session.getUser() != null) {
            String authUser = (session.getUser()).toLowerCase(Locale.US);
            MailAddress senderAddress = (MailAddress) session.getState().get(
                    SMTPSession.SENDER);

            if ((senderAddress == null)
                    || (!authUser.equals(senderAddress.getLocalPart()))
                    || (!mailServer
                            .isLocalServer(senderAddress.getDomain()))) {
                return new HookResult(HookReturnCode.DENY, 
                        SMTPRetCode.BAD_SEQUENCE,
                        DSNStatus.getStatus(DSNStatus.PERMANENT,
                                DSNStatus.SECURITY_AUTH)
                                + " Incorrect Authentication for Specified Email Address");
            }
        }
        return new HookResult(HookReturnCode.DECLINED);
    }

}
