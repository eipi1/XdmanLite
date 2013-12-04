/*
 * Copyright (c)  Subhra Das Gupta
 *
 * This file is part of Xtream Download Manager.
 *
 * Xtream Download Manager is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Xtream Download Manager is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with Xtream Download Manager; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

package org.sdg.xdman.core.common.http.io;

/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/java/org/apache/commons/httpclient/ChunkedInputStream.java,v 1.24 2004/10/10 15:18:55 olegk Exp $
 * $Revision: 291181 $
 * $Date: 2005-09-23 20:13:25 +0200 (Fri, 23 Sep 2005) $
 *
 * ====================================================================
 *
 *  Copyright 2002-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * <p>
 * Transparently coalesces chunks of a HTTP stream that uses Transfer-Encoding
 * chunked.
 * </p>
 * 
 * <p>
 * Note that this class NEVER closes the underlying stream, even when close gets
 * called. Instead, it will read until the "end" of its chunking on close, which
 * allows for the seamless invocation of subsequent HTTP 1.1 calls, while not
 * requiring the client to remember to read the entire contents of the response.
 * </p>
 * 
 * @author Ortwin Glueck
 * @author Sean C. Sullivan
 * @author Martin Elwin
 * @author Eric Johnson
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author Michael Becke
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * 
 * @since 2.0
 * 
 */
public class ChunkedInputStream extends InputStream {
	/** The inputstream that we're wrapping */
	private InputStream in;

	/** The chunk size */
	private int chunkSize;

	/** The current position within the current chunk */
	private int pos;

	/** True if we'are at the beginning of stream */
	private boolean bof = true;

	/** True if we've reached the end of stream */
	private boolean eof = false;

	/** True if this stream is closed */
	private boolean closed = false;

	/**
	 * ChunkedInputStream constructor that associates the chunked input stream
	 * with a {@link HttpMethod HTTP method}. Usually it should be the same
	 * {@link HttpMethod HTTP method} the chunked input stream originates from.
	 * If chunked input stream contains any footers (trailing headers), they
	 * will be added to the associated {@link HttpMethod HTTP method}.
	 * 
	 * @param in
	 *            the raw input stream
	 * @param method
	 *            the HTTP method to associate this input stream with. Can be
	 *            <tt>null</tt>.
	 * 
	 * @throws IOException
	 *             If an IO error occurs
	 */
	public ChunkedInputStream(final InputStream in) throws IOException {

		if (in == null) {
			throw new IllegalArgumentException(
					"InputStream parameter may not be null");
		}
		this.in = in;
		this.pos = 0;
	}

	/**
	 * <p>
	 * Returns all the data in a chunked stream in coalesced form. A chunk is
	 * followed by a CRLF. The method returns -1 as soon as a chunksize of 0 is
	 * detected.
	 * </p>
	 * 
	 * <p>
	 * Trailer headers are read automcatically at the end of the stream and can
	 * be obtained with the getResponseFooters() method.
	 * </p>
	 * 
	 * @return -1 of the end of the stream has been reached or the next data
	 *         byte
	 * @throws IOException
	 *             If an IO problem occurs
	 * 
	 * @see HttpMethod#getResponseFooters()
	 */
	public int read() throws IOException {

		if (closed) {
			throw new IOException("Attempted read from closed stream.");
		}
		if (eof) {
			return -1;
		}
		if (pos >= chunkSize) {
			nextChunk();
			if (eof) {
				return -1;
			}
		}
		pos++;
		return in.read();
	}

	/**
	 * Read some bytes from the stream.
	 * 
	 * @param b
	 *            The byte array that will hold the contents from the stream.
	 * @param off
	 *            The offset into the byte array at which bytes will start to be
	 *            placed.
	 * @param len
	 *            the maximum number of bytes that can be returned.
	 * @return The number of bytes returned or -1 if the end of stream has been
	 *         reached.
	 * @see java.io.InputStream#read(byte[], int, int)
	 * @throws IOException
	 *             if an IO problem occurs.
	 */
	public int read(byte[] b, int off, int len) throws IOException {

		if (closed) {
			throw new IOException("Attempted read from closed stream.");
		}

		if (eof) {
			return -1;
		}
		if (pos >= chunkSize) {
			nextChunk();
			if (eof) {
				return -1;
			}
		}
		len = Math.min(len, chunkSize - pos);
		int count = in.read(b, off, len);
		pos += count;
		return count;
	}

	/**
	 * Read some bytes from the stream.
	 * 
	 * @param b
	 *            The byte array that will hold the contents from the stream.
	 * @return The number of bytes returned or -1 if the end of stream has been
	 *         reached.
	 * @see java.io.InputStream#read(byte[])
	 * @throws IOException
	 *             if an IO problem occurs.
	 */
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	/**
	 * Read the CRLF terminator.
	 * 
	 * @throws IOException
	 *             If an IO error occurs.
	 */
	private void readCRLF() throws IOException {
		int cr = in.read();
		int lf = in.read();
		if ((cr != '\r') || (lf != '\n')) {
			throw new IOException("CRLF expected at end of chunk: " + cr + "/"
					+ lf);
		}
	}

	/**
	 * Read the next chunk.
	 * 
	 * @throws IOException
	 *             If an IO error occurs.
	 */
	private void nextChunk() throws IOException {
		if (!bof) {
			readCRLF();
		}
		chunkSize = getChunkSizeFromInputStream(in);
		bof = false;
		pos = 0;
		if (chunkSize == 0) {
			eof = true;
			parseTrailerHeaders();
		}
	}

	/**
	 * Expects the stream to start with a chunksize in hex with optional
	 * comments after a semicolon. The line must end with a CRLF: "a3; some
	 * comment\r\n" Positions the stream at the start of the next line.
	 * 
	 * @param in
	 *            The new input stream.
	 * @param required
	 *            <tt>true<tt/> if a valid chunk must be present,
     *                 <tt>false<tt/> otherwise.
	 * 
	 * @return the chunk size as integer
	 * 
	 * @throws IOException
	 *             when the chunk size could not be parsed
	 */
	private static int getChunkSizeFromInputStream(final InputStream in)
			throws IOException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		// States: 0=normal, 1=\r was scanned, 2=inside quoted string, -1=end
		int state = 0;
		while (state != -1) {
			int b = in.read();
			if (b == -1) {
				throw new IOException("chunked stream ended unexpectedly");
			}
			switch (state) {
			case 0:
				switch (b) {
				case '\r':
					state = 1;
					break;
				case '\"':
					state = 2;
					/* fall through */
				default:
					baos.write(b);
				}
				break;

			case 1:
				if (b == '\n') {
					state = -1;
				} else {
					// this was not CRLF
					throw new IOException("Protocol violation: Unexpected"
							+ " single newline character in chunk size");
				}
				break;

			case 2:
				switch (b) {
				case '\\':
					b = in.read();
					baos.write(b);
					break;
				case '\"':
					state = 0;
					/* fall through */
				default:
					baos.write(b);
				}
				break;
			default:
				throw new RuntimeException("assertion failed");
			}
		}

		// parse data
		String dataString = new String(baos.toByteArray(), "US-ASCII");// EncodingUtil.getAsciiString(baos.toByteArray());
		int separator = dataString.indexOf(';');
		dataString = (separator > 0) ? dataString.substring(0, separator)
				.trim() : dataString.trim();

		int result;
		try {
			result = Integer.parseInt(dataString.trim(), 16);
		} catch (NumberFormatException e) {
			throw new IOException("Bad chunk size: " + dataString);
		}
		return result;
	}

	private void parseTrailerHeaders() throws IOException {
		while (true) {
			String header = readLine(in);
			if (header == null || header.length() < 1)
				break;
		}
	}

	String readLine(InputStream in) throws IOException {
		StringBuffer buf = new StringBuffer();
		while (true) {
			int x = in.read();
			if (x == -1)
				break;
			if (x == '\n')
				break;
			if (x != '\r')
				buf.append((char) x);
		}
		return buf.toString();
	}
}
