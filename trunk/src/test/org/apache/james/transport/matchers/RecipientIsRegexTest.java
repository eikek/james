/***********************************************************************
 * Copyright (c) 2006 The Apache Software Foundation.                  *
 * All rights reserved.                                                *
 * ------------------------------------------------------------------- *
 * Licensed under the Apache License, Version 2.0 (the "License"); you *
 * may not use this file except in compliance with the License. You    *
 * may obtain a copy of the License at:                                *
 *                                                                     *
 *     http://www.apache.org/licenses/LICENSE-2.0                      *
 *                                                                     *
 * Unless required by applicable law or agreed to in writing, software *
 * distributed under the License is distributed on an "AS IS" BASIS,   *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or     *
 * implied.  See the License for the specific language governing       *
 * permissions and limitations under the License.                      *
 ***********************************************************************/

package org.apache.james.transport.matchers;

import java.io.UnsupportedEncodingException;
import java.util.Collection;

import javax.mail.MessagingException;

import org.apache.mailet.MailAddress;
import org.apache.mailet.Matcher;

public class RecipientIsRegexTest extends AbstractRecipientIsTest {

    private String regex = ".*";

    public RecipientIsRegexTest(String arg0)
            throws UnsupportedEncodingException {
        super(arg0);
    }

    private void setRegex(String regex) {
        this.regex = regex;
    }

    // test if the recipients get returned as matched
    public void testRegexIsMatchedAllRecipients() throws MessagingException {
        setRecipients(new MailAddress[] { new MailAddress(
                "test@james.apache.org") });
        setRegex(".*@.*");

        setupAll();

        Collection matchedRecipients = matcher.match(mockedMail);

        assertNotNull(matchedRecipients);
        assertEquals(matchedRecipients.size(), mockedMail.getRecipients()
                .size());
    }

    // test if one recipients get returned as matched
    public void testRegexIsMatchedOneRecipient() throws MessagingException {
        setRecipients(new MailAddress[] {
                new MailAddress("test@james.apache.org"),
                new MailAddress("test2@james.apache.org") });
        setRegex("^test@.*");

        setupAll();

        Collection matchedRecipients = matcher.match(mockedMail);

        assertNotNull(matchedRecipients);
        assertEquals(matchedRecipients.size(), 1);
    }

    // test if no recipient get returned cause it not match
    public void testRegexIsNotMatch() throws MessagingException {
        setRecipients(new MailAddress[] {
                new MailAddress("test@james2.apache.org"),
                new MailAddress("test2@james2.apache.org") });
        setRegex(".*\\+");

        setupAll();

        Collection matchedRecipients = matcher.match(mockedMail);

        assertEquals(matchedRecipients.size(), 0);
    }

    // test if an exception was thrown cause the regex was invalid
    public void testRegexIsNotMatchedCauseError() throws MessagingException {
        Collection matchedRecipients = null;
        String invalidRegex = "(!(";
        String regexException = null;
        String exception = "Malformed pattern: " + invalidRegex;

        setRecipients(new MailAddress[] {
                new MailAddress("test@james2.apache.org"),
                new MailAddress("test2@james2.apache.org") });
        setRegex(invalidRegex);

        try {
            setupAll();
            matchedRecipients = matcher.match(mockedMail);
        } catch (MessagingException m) {
            m.printStackTrace();
            regexException = m.getMessage();
        }

        assertNull(matchedRecipients);
        assertEquals(regexException, exception);

    }

    protected String getRecipientName() {
        return regex;
    }

    protected Matcher createMatcher() {
        return new RecipientIsRegex();
    }
}
