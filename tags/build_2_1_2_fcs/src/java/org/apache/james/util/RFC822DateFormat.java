/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE file.
 */
package org.apache.james.util;

import java.util.Date;
import javax.mail.internet.MailDateFormat;

/**
 * A thread safe wrapper for the <code>javax.mail.internet.MailDateFormat</code> class.
 *
 * @author Serge Knystautas <sergek@lokitech.com>
 * @author Peter M. Goldstein <farsight@alum.mit.edu>
 */
public class RFC822DateFormat extends SynchronizedDateFormat {
    /**
     * A static instance of the RFC822DateFormat, used by toString
     */
    private static RFC822DateFormat instance; 

    static {
        instance = new RFC822DateFormat();
    }

    /**
     * This static method allows us to format RFC822 dates without
     * explicitly instantiating an RFC822DateFormat object.
     *
     * @return java.lang.String
     * @param d Date
     *
     * @deprecated This method is not necessary and is preserved for API
     *             backwards compatibility.  Users of this class should
     *             instantiate an instance and use it as they would any
     *             other DateFormat object.
     */
    public static String toString(Date d) {
        return instance.format(d);
    }

    /**
     * Constructor for RFC822DateFormat
     */
    public RFC822DateFormat() {
        super(new MailDateFormat());
    }
}
