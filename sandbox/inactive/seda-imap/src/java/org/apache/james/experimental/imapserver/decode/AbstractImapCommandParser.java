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

package org.apache.james.experimental.imapserver.decode;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.mail.Flags;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.james.experimental.imapserver.ImapConstants;
import org.apache.james.experimental.imapserver.ImapRequestLineReader;
import org.apache.james.experimental.imapserver.ProtocolException;
import org.apache.james.experimental.imapserver.commands.ImapCommand;
import org.apache.james.experimental.imapserver.message.IdRange;
import org.apache.james.experimental.imapserver.message.ImapCommandMessage;
import org.apache.james.experimental.imapserver.message.ImapMessageFactory;
import org.apache.james.experimental.imapserver.store.MessageFlags;

/**
 * <p>
 * <strong>Note:</strong> 
 * </p>
 * @version $Revision: 109034 $
 */
public abstract class AbstractImapCommandParser extends AbstractLogEnabled implements ImapCommandParser, MessagingImapCommandParser
{
    private ImapCommand command;
    private ImapMessageFactory messageFactory;
    
    public AbstractImapCommandParser() {
        super();
    }
    
    public ImapCommand getCommand() {
        return command;
    }
    
    protected void setCommand(ImapCommand command) {
        this.command = command;
    }

    /**
     * @see org.apache.james.experimental.imapserver.decode.MessagingImapCommandParser#getMessageFactory()
     */
    public ImapMessageFactory getMessageFactory() {
        return messageFactory;
    }

    /**
     * @see org.apache.james.experimental.imapserver.decode.MessagingImapCommandParser#setMessageFactory(org.apache.james.experimental.imapserver.message.ImapMessageFactory)
     */
    public void setMessageFactory(ImapMessageFactory messageFactory) {
        this.messageFactory = messageFactory;
    }

    /**
     * Parses a request into a command message
     * for later processing.
     * @param request <code>ImapRequestLineReader</code>, not null
     * @return <code>ImapCommandMessage</code>, not null
     */
    public final ImapCommandMessage parse( ImapRequestLineReader request, String tag ) {
        ImapCommandMessage result;
        try {
            
            ImapCommandMessage message = decode(command, request, tag);
            final Logger logger = getLogger();
            setupLogger(message);
            result = message;
            
        } catch ( ProtocolException e ) {
            getLogger().debug("error processing command ", e);
            String msg = e.getMessage() + " Command should be '" +
                    command.getExpectedMessage() + "'";
            result = messageFactory.createErrorMessage( msg, tag );
        }
        return result;
    }
    
    /**
     * Parses a request into a command message
     * for later processing.
     * @param request <code>ImapRequestLineReader</code>, not null
     * @param tag TODO
     * @param command <code>ImapCommand</code> to be parsed, not null
     * @return <code>ImapCommandMessage</code>, not null
     * @throws ProtocolException if the request cannot be parsed
     */
    protected abstract ImapCommandMessage decode( ImapCommand command, ImapRequestLineReader request, String tag ) 
        throws ProtocolException;
    
    /**
     * Reads an argument of type "atom" from the request.
     */
    public static String atom( ImapRequestLineReader request ) throws ProtocolException
    {
        return consumeWord( request, new ATOM_CHARValidator() );
    }

    /**
     * Reads a command "tag" from the request.
     */
    public static String tag(ImapRequestLineReader request) throws ProtocolException
    {
        CharacterValidator validator = new TagCharValidator();
        return consumeWord( request, validator );
    }

    /**
     * Reads an argument of type "astring" from the request.
     */
    public String astring(ImapRequestLineReader request) throws ProtocolException
    {
        char next = request.nextWordChar();
        switch ( next ) {
            case '"':
                return consumeQuoted( request );
            case '{':
                return consumeLiteral( request );
            default:
                return atom( request );
        }
    }

    /**
     * Reads an argument of type "nstring" from the request.
     */
    public String nstring( ImapRequestLineReader request ) throws ProtocolException
    {
        char next = request.nextWordChar();
        switch ( next ) {
            case '"':
                return consumeQuoted( request );
            case '{':
                return consumeLiteral( request );
            default:
                String value = atom( request );
                if ( "NIL".equals( value ) ) {
                    return null;
                }
                else {
                    throw new ProtocolException( "Invalid nstring value: valid values are '\"...\"', '{12} CRLF *CHAR8', and 'NIL'." );
                }
        }
    }

    /**
     * Reads a "mailbox" argument from the request. Not implemented *exactly* as per spec,
     * since a quoted or literal "inbox" still yeilds "INBOX"
     * (ie still case-insensitive if quoted or literal). I think this makes sense.
     *
     * mailbox         ::= "INBOX" / astring
     *              ;; INBOX is case-insensitive.  All case variants of
     *              ;; INBOX (e.g. "iNbOx") MUST be interpreted as INBOX
     *              ;; not as an astring.
     */
    public String mailbox( ImapRequestLineReader request ) throws ProtocolException
    {
        String mailbox = astring( request );
        if ( mailbox.equalsIgnoreCase( ImapConstants.INBOX_NAME ) ) {
            return ImapConstants.INBOX_NAME;
        }
        else {
            return mailbox;
        }
    }

    /**
     * Reads a "date-time" argument from the request.
     * TODO handle timezones properly
     */
    public Date dateTime( ImapRequestLineReader request ) throws ProtocolException
    {
        char next = request.nextWordChar();
        String dateString;
        if ( next == '"' ) {
            dateString = consumeQuoted( request );
        }
        else {
            throw new ProtocolException( "DateTime values must be quoted." );
        }

        DateFormat dateFormat = new SimpleDateFormat( "dd-MMM-yyyy hh:mm:ss zzzz" );
        try {
            return dateFormat.parse( dateString );
        }
        catch ( ParseException e ) {
            throw new ProtocolException( "Invalid date format.", e);
        }
    }

    /**
     * Reads a "date" argument from the request.
     * TODO handle timezones properly
     */
    public Date date( ImapRequestLineReader request ) throws ProtocolException
    {
        char next = request.nextWordChar();
        String dateString;
        if ( next == '"' ) {
            dateString = consumeQuoted( request );
        }
        else {
            dateString = atom( request );
        }

        DateFormat dateFormat = new SimpleDateFormat( "dd-MMM-yyyy" );
        try {
            return dateFormat.parse( dateString );
        }
        catch ( ParseException e ) {
            throw new ProtocolException( "Invalid date format.", e);
        }
    }

    /**
     * Reads the next "word from the request, comprising all characters up to the next SPACE.
     * Characters are tested by the supplied CharacterValidator, and an exception is thrown
     * if invalid characters are encountered.
     */
    protected static String consumeWord( ImapRequestLineReader request,
                                  CharacterValidator validator )
            throws ProtocolException
    {
        StringBuffer atom = new StringBuffer();

        char next = request.nextWordChar();
        while( ! isWhitespace( next ) ) {
            if ( validator.isValid( next ) )
            {
                atom.append( next );
                request.consume();
            }
            else {
                throw new ProtocolException( "Invalid character: '" + next + "'" );
            }
            next = request.nextChar();
        }
        return atom.toString();
    }

    private static boolean isWhitespace( char next )
    {
        return ( next == ' ' || next == '\n' || next == '\r' || next == '\t' );
    }

    /**
     * Reads an argument of type "literal" from the request, in the format:
     *      "{" charCount "}" CRLF *CHAR8
     * Note before calling, the request should be positioned so that nextChar
     * is '{'. Leading whitespace is not skipped in this method.
     */
    protected String consumeLiteral( ImapRequestLineReader request )
            throws ProtocolException
    {
        // The 1st character must be '{'
        consumeChar( request, '{' );

        StringBuffer digits = new StringBuffer();
        char next = request.nextChar();
        while ( next != '}' && next != '+' )
        {
            digits.append( next );
            request.consume();
            next = request.nextChar();
        }

        // If the number is *not* suffixed with a '+', we *are* using a synchronized literal,
        // and we need to send command continuation request before reading data.
        boolean synchronizedLiteral = true;
        // '+' indicates a non-synchronized literal (no command continuation request)
        if ( next == '+' ) {
            synchronizedLiteral = false;
            consumeChar(request, '+' );
        }

        // Consume the '}' and the newline
        consumeChar( request, '}' );
        consumeCRLF( request );

        if ( synchronizedLiteral ) {
            request.commandContinuationRequest();
        }

        int size = Integer.parseInt( digits.toString() );
        byte[] buffer = new byte[size];
        request.read( buffer );

        return new String( buffer );
    }

    /**
     * Consumes a CRLF from the request.
     * TODO we're being liberal, the spec insists on \r\n for new lines.
     * @param request
     * @throws ProtocolException
     */
    private void consumeCRLF( ImapRequestLineReader request )
            throws ProtocolException
    {
        char next = request.nextChar();
        if ( next != '\n' ) {
            consumeChar( request, '\r' );
        }
        consumeChar( request, '\n' );
    }

    /**
     * Consumes the next character in the request, checking that it matches the
     * expected one. This method should be used when the
     */
    protected void consumeChar( ImapRequestLineReader request, char expected )
            throws ProtocolException
    {
        char consumed = request.consume();
        if ( consumed != expected ) {
            throw new ProtocolException( "Expected:'" + expected + "' found:'" + consumed + "'" );
        }
    }

    /**
     * Reads a quoted string value from the request.
     */
    protected String consumeQuoted( ImapRequestLineReader request )
            throws ProtocolException
    {
        // The 1st character must be '"'
        consumeChar(request, '"' );

        StringBuffer quoted = new StringBuffer();
        char next = request.nextChar();
        while( next != '"' ) {
            if ( next == '\\' ) {
                request.consume();
                next = request.nextChar();
                if ( ! isQuotedSpecial( next ) ) {
                    throw new ProtocolException( "Invalid escaped character in quote: '" +
                                                 next + "'" );
                }
            }
            quoted.append( next );
            request.consume();
            next = request.nextChar();
        }

        consumeChar( request, '"' );

        return quoted.toString();
    }

    /**
     * Reads a base64 argument from the request.
     */
    public byte[] base64( ImapRequestLineReader request ) throws ProtocolException
    {
        // TODO: throw unsupported exception?
        // TODO: log
        return null;
    }

    /**
     * Reads a "flags" argument from the request.
     */
    public Flags flagList( ImapRequestLineReader request ) throws ProtocolException
    {
        Flags flags = new Flags();
        request.nextWordChar();
        consumeChar( request, '(' );
        CharacterValidator validator = new NoopCharValidator();
        String nextWord = consumeWord( request, validator );
        while ( ! nextWord.endsWith(")" ) ) {
            setFlag( nextWord, flags );
            nextWord = consumeWord( request, validator );
        }
        // Got the closing ")", may be attached to a word.
        if ( nextWord.length() > 1 ) {
            setFlag( nextWord.substring(0, nextWord.length() - 1 ), flags );
        }

        return flags;
        }

    public void setFlag( String flagString, Flags flags ) throws ProtocolException
    {
        if ( flagString.equalsIgnoreCase( MessageFlags.ANSWERED ) ) {
            flags.add(Flags.Flag.ANSWERED);
        }
        else if ( flagString.equalsIgnoreCase( MessageFlags.DELETED ) ) {
            flags.add(Flags.Flag.DELETED);
        }
        else if ( flagString.equalsIgnoreCase( MessageFlags.DRAFT ) ) {
            flags.add(Flags.Flag.DRAFT);
        }
        else if ( flagString.equalsIgnoreCase( MessageFlags.FLAGGED ) ) {
            flags.add(Flags.Flag.FLAGGED);
        }
        else if ( flagString.equalsIgnoreCase( MessageFlags.SEEN ) ) {
            flags.add(Flags.Flag.SEEN);
        }
        else {
            throw new ProtocolException( "Invalid flag string." );
        }
    }

     /**
     * Reads an argument of type "number" from the request.
     */
    public long number( ImapRequestLineReader request ) throws ProtocolException
    {
        String digits = consumeWord( request, new DigitCharValidator() );
        return Long.parseLong( digits );
    }

    /**
     * Reads an argument of type "nznumber" (a non-zero number)
     * (NOTE this isn't strictly as per the spec, since the spec disallows
     * numbers such as "0123" as nzNumbers (although it's ok as a "number".
     * I think the spec is a bit shonky.)
     */
    public long nzNumber( ImapRequestLineReader request ) throws ProtocolException
    {
        long number = number( request );
        if ( number == 0 ) {
            throw new ProtocolException( "Zero value not permitted." );
        }
        return number;
    }

    private static boolean isCHAR( char chr )
    {
        return ( chr >= 0x01 && chr <= 0x7f );
    }

    private boolean isCHAR8( char chr )
    {
        return ( chr >= 0x01 && chr <= 0xff );
    }

    protected static boolean isListWildcard( char chr )
    {
        return ( chr == '*' || chr == '%' );
    }

    private static boolean isQuotedSpecial( char chr )
    {
        return ( chr == '"' || chr == '\\' );
    }

    /**
     * Consumes the request up to and including the eno-of-line.
     * @param request The request
     * @throws ProtocolException If characters are encountered before the endLine.
     */
    public void endLine( ImapRequestLineReader request ) throws ProtocolException
    {
        request.eol();
    }

    /**
     * Reads a "message set" argument, and parses into an IdSet.
     * Currently only supports a single range of values.
     */
    public IdRange[] parseIdRange( ImapRequestLineReader request )
            throws ProtocolException
    {
        CharacterValidator validator = new MessageSetCharValidator();
        String nextWord = consumeWord( request, validator );

        int commaPos = nextWord.indexOf( ',' );
        if ( commaPos == -1 ) {
            return new IdRange[]{ parseRange( nextWord ) };
        }

        ArrayList rangeList = new ArrayList();
        int pos = 0;
        while ( commaPos != -1 ) {
            String range = nextWord.substring( pos, commaPos );
            IdRange set = parseRange( range );
            rangeList.add( set );

            pos = commaPos + 1;
            commaPos = nextWord.indexOf( ',', pos );
        }
        String range = nextWord.substring( pos );
        rangeList.add( parseRange( range ) );
        return (IdRange[]) rangeList.toArray(new IdRange[rangeList.size()]);
    }

    private IdRange parseRange( String range ) throws ProtocolException
    {
        int pos = range.indexOf( ':' );
        try {
            if ( pos == -1 ) {
                long value = parseLong( range );
                return new IdRange( value );
            }
            else {
                long lowVal = parseLong( range.substring(0, pos ) );
                long highVal = parseLong( range.substring( pos + 1 ) );
                return new IdRange( lowVal, highVal );
            }
        }
        catch ( NumberFormatException e ) {
            throw new ProtocolException( "Invalid message set.", e);
        }
    }

    private long parseLong( String value ) {
        if ( value.length() == 1 && value.charAt(0) == '*' ) {
            return Long.MAX_VALUE;
        }
        return Long.parseLong( value );
    }
    /**
     * Provides the ability to ensure characters are part of a permitted set.
     */
    protected interface CharacterValidator
    {
        /**
         * Validates the supplied character.
         * @param chr The character to validate.
         * @return <code>true</code> if chr is valid, <code>false</code> if not.
         */
        boolean isValid( char chr );
    }

    protected static class NoopCharValidator implements CharacterValidator
    {
        public boolean isValid( char chr )
        {
            return true;
        }
    }

    protected static class ATOM_CHARValidator implements CharacterValidator
    {
        public boolean isValid( char chr )
        {
            return ( isCHAR( chr ) && !isAtomSpecial( chr ) &&
                     !isListWildcard( chr ) && !isQuotedSpecial( chr ) );
        }

        private boolean isAtomSpecial( char chr )
        {
            return ( chr == '(' ||
                    chr == ')' ||
                    chr == '{' ||
                    chr == ' ' ||
                    chr == Character.CONTROL );
        }
    }

    protected static class DigitCharValidator implements CharacterValidator
    {
        public boolean isValid( char chr )
        {
            return ( ( chr >= '0' && chr <= '9' ) ||
                     chr == '*' );
        }
    }

    private static class TagCharValidator extends ATOM_CHARValidator
    {
        public boolean isValid( char chr )
        {
            if ( chr == '+' ) return false;
            return super.isValid( chr );
        }
    }

    private static class MessageSetCharValidator implements CharacterValidator
    {
        public boolean isValid( char chr )
        {
            return ( isDigit( chr ) ||
                    chr == ':' ||
                    chr == '*' ||
                    chr == ',' );
        }

        private boolean isDigit( char chr )
        {
            return '0' <= chr && chr <= '9';
        }
    }

}
