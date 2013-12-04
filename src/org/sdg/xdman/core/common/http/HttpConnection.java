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

package org.sdg.xdman.core.common.http;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.sdg.xdman.core.common.AuthenticationException;
import org.sdg.xdman.core.common.Authenticator;
import org.sdg.xdman.core.common.Connection;
import org.sdg.xdman.core.common.ConnectionManager;
import org.sdg.xdman.core.common.Credential;
import org.sdg.xdman.core.common.InvalidContentException;
import org.sdg.xdman.core.common.InvalidReplyException;
import org.sdg.xdman.core.common.ResumeNotSupportedException;
import org.sdg.xdman.core.common.XDMConfig;
import org.sdg.xdman.util.Base64;

public class HttpConnection extends Connection {
	// DefaultHttpClient client;
	public XDMHttpClient client;
	boolean clientSet = false;
	int count = 0;
	XDMConfig config;
	public int errorCode;

	public HttpConnection(String url, String fileName, long startOff,
			long length, long contentLength, int timeout,
			ConnectionManager mgr, Object lock, HashMap<String, String> extra,
			String cookie, Credential c, XDMConfig config) {
		super(url, fileName, startOff, length, contentLength, timeout, mgr,
				lock, extra, cookie);
		this.credential = c;
		this.config = config;
	}

	public HttpConnection(String url, String fileName, long startOff,
			long length, long contentLength, int timeout,
			ConnectionManager mgr, Object lock, XDMHttpClient client,
			HashMap<String, String> extra, String cookie, Credential c,
			XDMConfig config) {
		super(url, fileName, startOff, length, contentLength, timeout, mgr,
				lock, extra, cookie);
		this.client = client;
		this.config = config;
		if (client != null)
			clientSet = true;
		this.credential = c;
	}

	public HttpConnection(State state, int timeout, ConnectionManager mgr,
			Object lock, String cookie, Credential c, XDMConfig config) {
		super(state, timeout, mgr, lock, cookie);
		this.credential = c;
		this.config = config;
	}

	public boolean connect() {
		status = CONNECTING;
		while (true) {
			read = 0;
			clen = 0;
			if (stop) {
				close();
				break;
			}
			chkPause();
			try {
				message = "Connecting...";
				mgr.updated();
				msg("Connecting...");
				if (length > 0)
					if ((startOff + downloaded) - (startOff + length - 1) > 0) {
						mgr.donwloadComplete(this);
						return true;
					}
				if (!clientSet) {
					if (client == null)
						client = new XDMHttpClient(config);
				} else {
					System.out
							.println("***********************************REUSING CONNECTION******************************");
					clientSet = false;
				}
				// client = new DefaultHttpClient(
				// new BasicClientConnectionManager());
				// HttpParams params = new BasicHttpParams();
				// params.setParameter(ClientPNames.HANDLE_REDIRECTS, false);
				// params.setParameter(CoreConnectionPNames.SO_TIMEOUT,
				// timeout);
				// client.setParams(params);
				// HttpGet get = new HttpGet(url);
				msg("Connecting to..." + url);
				if (!client.connected) {
					client.connect(url);
					msg("Connecting client...");
				} else
					msg("Reusing client conn..");
				count = 0;
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
				// get.addHeader("range", "bytes= " + (startOff + downloaded)
				// + "-");
				if (length > 0) {
					client.addHeader("Range", "bytes="
							+ (startOff + downloaded) + "-");
					// + (startOff + length - 1));
				}
				if (extra == null || extra.get("user-agent") == null) {
					client
							.addHeader("User-Agent",
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
							.addHeader("Authorization",
									"Basic "
											+ Base64.encode((credential.user
													+ ":" + credential.pass)
													.getBytes()));
				}

				msg("SEND GET...");
				message = "Send GET...";
				client.sendGET();
				count = 0;
				// HttpResponse response = client.execute(get);
				msg("SEND GET...Done");
				if (stop) {
					close();
					break;
				}
				chkPause();
				message = "Parsing response...";
				mgr.updated();
				// int code = response.getStatusLine().getStatusCode();
				int code = client.getResponseCode();// response.getStatusLine().getStatusCode();
				msg("content-range: " + client.getHeader("content-range"));
				msg("Response code: " + code);
				cookies = client.getCookieString();
				System.out.println("COOKIE RECEIVED: " + cookies + " "
						+ cookies.length());
				if (code >= 300 && code < 400) {
					client.close();
					// client.getConnectionManager().shutdown();
					if (length < 0) {
						// url = response.getFirstHeader("location").getValue();
						url = client.getHeader("location");
						if (!url.startsWith("http")) {
							url = "http://" + client.getHostString() + "/"
									+ url;
						}
						url = url.replace(" ", "%20");
						throw new IllegalAccessException("Redirecting to: "
								+ url);
					} else {
						throw new InvalidReplyException(code,
								"Invalid redirect");
					}
				}
				// System.out
				// .println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ "
				// + response.getFirstHeader("content-range"));
				System.out
						.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ "
								+ client.getHeader("content-range"));

				if (code != 200 && code != 206 && code != 416 && code != 413
						&& code != 401 && code != 408 && code != 407
						&& code != 503)
					throw new InvalidReplyException(code,
							"Invalid response from server");
				if (code == 503) {
					throw new Exception();
				}
				if (code == 401) {
					credential = mgr.getCreditential();
					if (credential == null) {
						throw new AuthenticationException(client.statusLine);
					} else
						throw new IllegalArgumentException("Unauthorized");
				}
				if (code == 407) {
					throw new AuthenticationException(client.statusLine);
				}
				if (startOff + downloaded > 0) {
					if (code != 206)
						throw new ResumeNotSupportedException(
								"Server does not support partial content(Resume feature)");
				}
				long len = client.getContentLength();
				clen = len;
				if (length < 0) {
					try {
						length = len;
						contentLength = len;
					} catch (Exception e) {
					}
				}
				msg("Expected contentlength: " + contentLength + " found "
						+ len + " " + length);
				if (contentLength != -1 && length != -1) {
					if (contentLength != len)
						if (contentLength - downloaded != len)
							throw new InvalidContentException(
									"Invalid Content Length: Expected: "
											+ contentLength + " but got: "
											+ len);
				}
				// HttpEntity entity = response.getEntity();
				if (stop) {
					close();
					break;
				}
				chkPause();
				in = client.getInputStream();// entity.getContent();
				status = DOWNLOADING;
				message = "Downloading...";
				buf = new byte[config.tcpBuf];
				mgr.updated();
				msg("Notify...");
				msg("Going to call connected()...");
				content_type = client.getHeader("content-type");
				try {
					if (content_type.indexOf(";") >= 0) {
						content_type = content_type.split(";")[0].trim();
					}
				} catch (Exception e) {
				}
				System.out.println("Final content-type: " + content_type);
				content_disposition = client.getContentName();
				mgr.connected(this);
				msg("Returned from connected()");
				return true;
			} catch (UnknownHostException e) {
				message = "Disconnect.";
				mgr.updated();
				msg(e);
				e.printStackTrace();
				close();
				if (count > 30) {
					status = FAILED;
					lastError = "Host not found";
					errorCode = CONNECT_ERR;
					break;
				}
				if (stop) {
					close();
					break;
				}
				chkPause();
				msg("Sleeping 5 sec");
				message = "Disconnect.";
				try {
					Thread.sleep(5000);
					chkPause();
				} catch (Exception err) {
				}
				message = "Connecting...";
				mgr.updated();
				count++;
			} catch (IllegalAccessException e) {
				message = "Redirecting...";
				mgr.updated();
				msg(e);
				e.printStackTrace();
				close();
				if (stop) {
					close();
					break;
				}
				chkPause();
			} catch (IllegalArgumentException e) {
				message = "Authenticating...";
				mgr.updated();
				msg(e);
				e.printStackTrace();
				close();
				if (stop) {
					close();
					break;
				}
				chkPause();
			} catch (AuthenticationException e) {
				message = e.getMessage();
				errorCode = CONNECT_ERR;
				mgr.updated();
				msg(e);
				e.printStackTrace();
				close();
				status = FAILED;
				lastError = client.statusLine;
				if (lastError == null || lastError.length() < 1) {
					lastError = "Proxy Authentication Required";
				}
				break;
			} catch (InvalidContentException e) {
				message = e.getMessage();
				errorCode = CONTENT_ERR;
				mgr.updated();
				msg(e);
				e.printStackTrace();
				close();
				status = FAILED;
				lastError = "Content size invalid";
				break;
			} catch (InvalidReplyException e) {
				message = e.getMessage();
				errorCode = RESP_ERR;
				mgr.updated();
				msg(e);
				e.printStackTrace();
				close();
				status = FAILED;
				lastError = client.statusLine;
				break;
			} catch (ResumeNotSupportedException e) {
				message = e.getMessage();
				errorCode = RESUME_ERR;
				mgr.updated();
				msg(e);
				e.printStackTrace();
				close();
				status = FAILED;
				lastError = "Resume not supported";
				break;
			} catch (Exception e) {
				message = "ReConnecting...";
				mgr.updated();
				msg(e);
				e.printStackTrace();
				close();
				if (stop) {
					close();
					break;
				}
				chkPause();
				msg("Sleeping 2 sec");
				try {
					Thread.sleep(2000);
					chkPause();
				} catch (Exception err) {
				}
			} catch (Error e) {
				message = "Not a valid response";
				errorCode = UNKNOWN_ERR;
				mgr.updated();
				msg(e);
				e.printStackTrace();
				close();
				status = FAILED;
				lastError = "Not a valid response";
				break;
			}
			msg("Remaining " + (this.length - this.downloaded));
			if (stop) {
				close();
				break;
			}
			chkPause();
			client = null;
			clientSet = false;
			try {
				client.close();
			} catch (Exception e) {
			}
		}
		msg("Exiting connect");
		if (!stop) {
			status = FAILED;
			message = "disconnect.";
			mgr.updated();
			mgr.failed(lastError + " ", errorCode);
		}
		return false;
	}

	long getContentLengthFromRange(String r) {
		try {
			String len = r.split("/")[0].split("-")[1];
			return Long.parseLong(len) + 1;
		} catch (Exception e) {
			return -1;
		}
	}

	public void close() {
		msg(stop);
		msg(stop ? "STOP " : "" + "Releasing all resource...");
		try {
			// if (!Thread.currentThread().equals(t))
			// t.stop();
		} catch (Exception e) {
		}
		try {
			// in.close();
		} catch (Exception e) {
		}
		try {
			out.close();
		} catch (Exception e) {
		}
		try {
			// client.getConnectionManager().shutdown();
			client.close();
		} catch (Exception e) {
		}
		msg("Releasing all resource...done");
		message = "disconnect";
	}

	public boolean isEOF() {
		try {
			System.out.println("IS EOF: " + in.read());
			// in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ((read == clen) && (read > 0));
	}
}
