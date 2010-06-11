/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.james.transport.match;

import java.util.*;
import org.apache.mail.Mail;

/**
 * @version 1.0.0, 24/04/1999
 * @author  Federico Barbieri <scoobie@pop.systemy.it>
 */
public class SenderIs extends AbstractMatch {
    
    public Vector match(Mail mail, String condition) {
        String sender = mail.getSender();
        if (condition.indexOf(sender) != -1) {
            return mail.getRecipients();
        } else {
            return new Vector();
        }
    }
}
    
