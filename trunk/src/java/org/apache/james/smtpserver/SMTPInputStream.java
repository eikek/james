/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE file.
 */
package org.apache.james.smtpserver;

import java.io.*;

/**
 * Removes the dot-stuffing happing during the SMTP DATA transport
 *
 * @author Serge Knystautas <sergek@lokitech.com>
 */
public class SMTPInputStream extends FilterInputStream {
    protected int last[] = new int[2];

    public SMTPInputStream(InputStream in) {
        super(in);
        last[0] = -1;
        last[1] = -1;
    }

    /**
     * Read through the stream, checking for '\r\n.'
     */
    public int read() throws IOException {
        int b = in.read();
        if (b == '.' && last[0] == '\r' && last[1] == '\n') {
            //skip this '.' because it should have been stuffed
            b = in.read();
        }
        last[0] = last[1];
        last[1] = b;
        return b;
    }

    /**
     * Read through the stream, checking for '\r\n.'
     */
    public int read(byte[] b, int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if ((off < 0) || (off > b.length) || (len < 0) ||
               ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        int c = read();
        if (c == -1) {
            return -1;
        }
        b[off] = (byte)c;

        int i = 1;

        for (; i < len ; i++) {
            c = read();
            if (c == -1) {
                break;
            }
            if (b != null) {
                b[off + i] = (byte)c;
            }
        }

        return i;
    }
}
