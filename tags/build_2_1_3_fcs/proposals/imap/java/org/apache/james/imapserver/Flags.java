/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache", "Jakarta", "JAMES" and "Apache Software Foundation"
 *    must not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * Portions of this software are based upon public domain software
 * originally written at the National Center for Supercomputing Applications,
 * University of Illinois, Urbana-Champaign.
 */

package org.apache.james.imapserver;

import org.apache.avalon.framework.activity.Initializable;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * The set of flags associated with a message. The \Seen flag is maintained
 * on a per-user basis.
 *
 * <p>Reference: RFC 2060 - para 2.3
 * @author <a href="mailto:charles@benett1.demon.co.uk">Charles Benett</a>
 * @version 0.1 on 14 Dec 2000
 */
public class Flags 
    implements Serializable, Initializable {

    public static final int ANSWERED  = 0;
    public static final int DELETED   = 1;
    public static final int DRAFT     = 2;
    public static final int FLAGGED   = 3;
    public static final int RECENT    = 4;
    public static final int SEEN      = 5;

    // Array does not include seen flag
    private boolean[] flags = {false, false, false, false, true};

    //users who have seen this message
    private Set users; 

    public Flags() {
    }

    /**
     * Initialisation - only for object creation not on deserialisation.
     */
    public void initialize() {
        users = new HashSet();
    }

    /**
     * Returns IMAP formatted String of Flags for named user
     */
    public String getFlags(String user) {
        StringBuffer buf = new StringBuffer();
        buf.append("(");
        if (flags[ANSWERED]) { buf.append("\\ANSWERED ");}
        if (flags[DELETED]) { buf.append("\\DELETED ");}
        if (flags[DRAFT]) { buf.append("\\DRAFT ");}
        if (flags[FLAGGED]) { buf.append("\\FLAGGED ");}
        if (flags[RECENT]) { buf.append("\\RECENT ");}
        if (users.contains(user)) { buf.append("\\SEEN ");}
        buf.append(")");
        return buf.toString();
    }

    /**
     * Sets Flags for message from IMAP-forammted string parameter.
     * <BR> The FLAGS<list> form overwrites existing flags, ie sets all other
     * flags to false.
     * <BR> The +FLAGS<list> form adds the flags in list to the existing flags
     * <BR> The -FLAGS<list> form removes the flags in list from the existing
     * flags
     * <BR> Note that the Recent flag cannot be set by user and is ignored by
     * this method.
     *
     * @param flagString a string formatted according to
     * RFC2060 store_att_flags
     * @param user the String email address of the user
     * @return true if successful, false if not (including uninterpretable
     * argument)
     */
    public boolean setFlags(String flagString, String user) {
        flagString = flagString.toUpperCase();
        if (flagString.startsWith("FLAGS")) {
            boolean [] newflags = new boolean[5];
            newflags[ANSWERED]
                = (flagString.indexOf("\\ANSWERED") != -1) ? true : false;
            newflags[DELETED]
                = (flagString.indexOf("\\DELETED") != -1) ? true : false;
            newflags[DRAFT]
                = (flagString.indexOf("\\DRAFT") != -1) ? true : false;
            newflags[FLAGGED]
                = (flagString.indexOf("\\FLAGGED") != -1) ? true : false;
            newflags[RECENT] =  false;
            if (flagString.indexOf("\\SEEN") != -1) {
                users.add(user);
            }
            System.arraycopy(newflags, 0, flags, 0, newflags.length);
            return true;
        } else if (flagString.startsWith("+FLAGS") ||flagString.startsWith("-FLAGS") ) {
            boolean mod = (flagString.startsWith("+") ? true : false);
            if (flagString.indexOf("\\ANSWERED") != -1) {
                flags[ANSWERED] = mod;
            }
            if (flagString.indexOf("\\DELETED") != -1) {
                flags[DELETED] = mod;
            }
            if (flagString.indexOf("\\DRAFT") != -1) {
                flags[DRAFT] = mod;
            }
            if (flagString.indexOf("\\FLAGGED") != -1) {
                flags[FLAGGED] = mod;
            }
            if (flagString.indexOf("\\SEEN") != -1) {
                if( mod) {
                    users.add(user);
                } else {
                    users.remove(user);
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public void setAnswered(boolean newState) {
        flags[ANSWERED] = newState;
    }

    public boolean isAnswered() {
        return flags[ANSWERED];
    }

    public void setDeleted(boolean newState) {
        flags[DELETED] = newState;
    }

    public boolean isDeleted() {
        return flags[DELETED];
    }

    public void setDraft(boolean newState) {
        flags[DRAFT] = newState;
    }

    public boolean isDraft() {
        return flags[DRAFT];
    }

    public void setFlagged(boolean newState) {
        flags[FLAGGED] = newState;
    }

    public boolean isFlagged() {
        return flags[FLAGGED];
    }

    public void setRecent(boolean newState) {
        flags[RECENT] = newState;
    }

    public boolean isRecent() {
        return flags[RECENT];
    }

    public void setSeen(boolean newState, String user) {
        if( newState) {
            users.add(user);
        } else {
            users.remove(user);
        }
    }

    public boolean isSeen(String user) {
        return users.contains(user);
    }
}

