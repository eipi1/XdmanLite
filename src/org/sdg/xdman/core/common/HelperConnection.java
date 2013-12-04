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


package org.sdg.xdman.core.common;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.sdg.xdman.core.common.http.XDMHttpClient;
import org.sdg.xdman.util.Base64;

public class HelperConnection implements Runnable {
	long start, length;
	XDMConfig config;
	String url;
	XDMHttpClient client;
	InputStream in;
	ByteArrayOutputStream out;
	HelpListener listerner;
	Connection c;
	boolean stop = false;
	String fileName;
	Credential credential;
	int timeout;
	HashMap<String, String> extra;
	String cookies;

	public HelperConnection(XDMConfig config, long start, long length,
			String url, HelpListener l, Connection c, String fileName,
			int timeout, HashMap<String, String> extra, String cookies,
			Credential credential) {
		this.config = config;
		this.start = start;
		this.length = length;
		this.url = url;
		this.listerner = l;
		this.c = c;
		this.fileName = fileName;
		this.extra = extra;
		this.credential = credential;
		this.cookies = cookies;
		this.timeout = timeout;

	}

	void start() {
		Thread t = new Thread(this);
		t.start();
	}

	void stop() {
		stop = true;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			client = new XDMHttpClient(config);
			client.connect(url);
			if (stop) {
				close();
				return;
			}
			client.setTimeOut(timeout);
			if (extra != null) {
				Set<String> keys = extra.keySet();
				Iterator<String> it = keys.iterator();
				while (it.hasNext()) {
					String key = it.next();
					String value = extra.get(key);
					if (!(key.equalsIgnoreCase("cookie")
							|| key.equalsIgnoreCase("range")
							|| key.equalsIgnoreCase("connection") || key
							.equalsIgnoreCase("host"))) {
						client.addHeader(key, value);
					}
				}
			}
			client.addHeader("range", "bytes=" + start + "-");
			if (extra == null || extra.get("user-agent") == null) {
				client.addHeader("User-Agent",
						"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0)");
				// "Mozilla/5.0 (Windows NT 6.1; rv:15.0) Gecko/20100101 Firefox/15.0");//
				// "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/6.0)");
			}
			if (cookies != null) {
				if (cookies.length() > 0) {
					System.out.println("Setting cookies: " + cookies);
					client.addCookieString(cookies);
				}
			} else {
				System.out.println("COOKIE IS NULL");
			}

			if (credential == null) {
				credential = Authenticator.getInstance().getCredential(
						client.host);
			}
			if (credential != null) {
				System.out.println("Adding auth");
				client
						.addHeader(
								"Authorization",
								"Basic "
										+ Base64
												.encode((credential.user + ":" + credential.pass)
														.getBytes()));
			}
			client.sendGET();
			if (stop) {
				close();
				return;
			}
			int rc = client.getResponseCode();
			System.out.println("Helper RESPONSE " + rc);
			if (rc != 206) {
				throw new Exception("Invalid RESPONSE CODE");
			}
			in = client.getInputStream();
			out = new ByteArrayOutputStream();
			byte buf[] = new byte[config.tcpBuf];
			long dwn = 0;
			while (true) {
				if (stop) {
					close();
					return;
				}
				int x;
				int rem = (int) (length - dwn);
				if (buf.length > rem) {
					x = in.read(buf, 0, rem);
				} else {
					x = in.read(buf);
				}
				if (stop) {
					close();
					return;
				}
				if (x == -1)
					throw new Exception("UNEXPECTED EOF");
				out.write(buf, 0, x);
				dwn += x;
				if (dwn >= length)
					break;
			}
			if (listerner != null) {
				if (stop) {
					close();
					return;
				}
				listerner.helpComplete(this, this);
			}
			close();
		} catch (Exception e) {
			System.out.println("Error IN HELPER: " + e);
			e.printStackTrace();
			close();
		}
	}

	void close() {
		System.out.println("closing helper conn. " + stop);
		try {
			client.close();
		} catch (Exception e) {
		}
		try {
			in.close();
		} catch (Exception e) {
		}
		try {
			out.close();
		} catch (Exception e) {
		}
	}
}
