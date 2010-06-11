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



package org.apache.mailet;


/**
 * The Util class provide some static methods which can be usefull in mailets
 */
public class MailetUtil {
    
    /**
     * <p>This takes the subject string and reduces (normailzes) it.
     * Multiple "Re:" entries are reduced to one, and capitalized.  The
     * prefix is always moved/placed at the beginning of the line, and
     * extra blanks are reduced, so that the output is always of the
     * form:</p>
     * <code>
     * &lt;prefix&gt; + &lt;one-optional-"Re:"*gt; + &lt;remaining subject&gt;
     * </code>
     * <p>I have done extensive testing of this routine with a standalone
     * driver, and am leaving the commented out debug messages so that
     * when someone decides to enhance this method, it can be yanked it
     * from this file, embedded it with a test driver, and the comments
     * enabled.</p>
     */
    public static String normalizeSubject(String subj, String prefix) {
        StringBuffer subject = new StringBuffer(subj);
        int prefixLength = prefix.length();

        // If the "prefix" is not at the beginning the subject line, remove it
        int index = subject.indexOf(prefix);
        if (index != 0) {

            if (index > 0) {
                subject.delete(index, index + prefixLength);
            }
            subject.insert(0, prefix); // insert prefix at the front
        }

        // Replace Re: with RE:
        String match = "Re:";
        index = subject.indexOf(match, prefixLength);

        while(index > -1) {
            subject.replace(index, index + match.length(), "RE:");
            index = subject.indexOf(match, prefixLength);
        }

        // Reduce them to one at the beginning
        match ="RE:";
        int indexRE = subject.indexOf(match, prefixLength) + match.length();
        index = subject.indexOf(match, indexRE);
        while(index > 0) {    
            subject.delete(index, index + match.length());
            index = subject.indexOf(match, indexRE);
        }

        // Reduce blanks
        match = "  ";
        index = subject.indexOf(match, prefixLength);
        while(index > -1) {
            subject.replace(index, index + match.length(), " ");
            index = subject.indexOf(match, prefixLength);
        }
        return subject.toString();
    }

}
