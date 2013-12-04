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



package org.sdg.xdman.proxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Observable;
import java.util.zip.DeflaterInputStream;
import java.util.zip.GZIPInputStream;

import org.sdg.xdman.core.common.XDMConfig;
import org.sdg.xdman.core.common.http.XDMHttpClient;
import org.sdg.xdman.core.common.http.io.ChunkedInputStream;
import org.sdg.xdman.gui.MediaInfo;
import org.sdg.xdman.gui.MediaTableModel;
import org.sdg.xdman.plugin.firefox.FFPluginBuilder;
import org.sdg.xdman.util.XDMUtil;

public class RequestHandler extends Observable implements Runnable, IConnection {
	String error = "HTTP/1.1 502 Bad Gateway\r\n\r\nERROR";
	MediaTableModel model;
	Socket socket;
	InputStream in, remoteIn;
	OutputStream out, remoteOut;
	String requestLine, url, responseLine;
	URL uri;
	Socket remote;
	HashMap<String, String> requestHeader;
	HashMap<String, String> responseHeader;
	Thread t;
	String host, path, query;
	int port;
	byte b[];
	XDMConfig config;
	StringBuffer cookies = new StringBuffer();
	StringBuffer serverCookie = new StringBuffer();
	RequestIntercepter intercepter;
	public Boolean intercept = false;
	long dwn;
	String type = "GET";
	IConnectionListener cl;
	boolean stop = false;
	public ArrayList<String> blockedHosts, skippedHosts;

	void start() {
		t = new Thread(this);
		t.start();
	}

	@Override
	public void run() {
		try {
			handleRequest();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				socket.close();
			} catch (Exception err) {
			}
		}
		if (cl != null) {
			cl.closed(this);
		}
	}

	private void handleRequest() throws IOException, URISyntaxException {
		in = socket.getInputStream();
		out = socket.getOutputStream();
		requestLine = readLine(in);
		System.out
				.println("***********************************HANDLING REQUEST*****************************");
		System.out.println(socket);
		if (requestLine.startsWith("PARAM")) {
			HashMap<String, String> arg = new HashMap<String, String>();
			while (true) {
				String ln = readLine(in);
				if (ln == null || ln.length() < 1) {
					break;
				}
				System.out.println("PARAM_LINE: " + ln + " " + ln.length());
				int pos = ln.indexOf(":");
				if (pos > 0) {
					String key = ln.substring(0, pos).trim();
					String value = ln.substring(pos + 1).trim();
					arg.put(key, value);
				}
			}
			if (intercepter != null) {
				if (arg.size() > 0)
					intercepter.intercept(arg);
			}
			try {
				socket.close();
			} catch (Exception e) {
			}
			return;
		}
		System.out.println("REQUEST LINE: " + requestLine);
		if (requestLine.length() < 1) {
			in.close();
			out.close();
			return;
		}
		String arr[] = requestLine.split(" ");
		if (arr.length < 1) {
			in.close();
			out.close();
			return;
		}
		url = arr[1];
		url = url.trim();
		System.out.println("**************************URL: " + url);
		if (url.equals("/proxy.pac")) {
			while (true) {
				String line = readLine(in);
				if (line == null || line.length() < 1)
					break;
				if (line.toLowerCase().startsWith("user-agent")) {
					String ua = line.substring(line.indexOf(':')).trim();
					if (XDMProxyServer.browsers.add(UAList.getBrowser(ua))) {
						setChanged();
						notifyObservers(XDMProxyServer.browsers);
					}
				}
			}
			out.write(("HTTP/1.0 200 OK\r\n"
					+ "Content-Type: application/x-ns-proxy-autoconfig"
					+ "\r\n\r\n").getBytes());
			out.write(proxy_pac.getBytes());
			out.close();
			in.close();
			socket.close();
			return;
		} else if (url.startsWith("/")) {
			System.out.println("SELF#########################################");
			handleLocalRequest(url);
			// out.write("SELF".getBytes());
			out.close();
			in.close();
			socket.close();
			return;
		} else if (url.startsWith("http://localhost:" + config.port)) {
			handleLocalRequest(new URL(url).getPath());
			// out.write("SELF".getBytes());
			out.close();
			in.close();
			socket.close();
			return;
		}
		resolveURI(url);
		try {
			connectToHost();
		} catch (Exception e) {
			try {
				OutputStream out = socket.getOutputStream();
				out.write(error.getBytes());
				out.flush();
			} catch (Exception err) {
				// TODO: handle exception
			}
			throw new IOException(e);
		}
		if (requestLine.startsWith("GET ") || requestLine.startsWith("HEAD")) {
			handleGET();
		} else if (requestLine.startsWith("POST")) {
			handlePOST();
		} else {
			throw new IOException("Invalid method: " + requestLine);
		}
	}

	private void handleLocalRequest(String url2) {
		try {
			System.out.println("Fetching resource: " + url2);
			System.out.println("Local PATH: " + url2);
			if (url2.equals("/flv")) {
				ArrayList<String> flvList = new ArrayList<String>();
				while (true) {
					String line = readLine(in);
					if (line == null || line.length() < 2)
						break;
					System.out.println("LINK: " + line);
					if (line.toLowerCase().startsWith("link")) {
						String link = line.substring(line.indexOf(":") + 1);
						flvList.add(link.trim());
					}
				}
				if (intercepter != null) {
					intercepter.intercept(flvList);
					out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
					out.flush();
				}
				return;
			}
			if (url2.equals("/link")) {
				while (true) {
					String line = readLine(in);
					if (line == null || line.length() < 2)
						break;
					System.out.println("LINK: " + line);
					if (line.toLowerCase().startsWith("link")) {
						String link = line.substring(line.indexOf(":") + 1);
						if (intercepter != null) {
							intercepter.intercept(link.trim());
							out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
							out.flush();
						}
					}
				}
				return;
			}
			if (url2.equals("/xdmff.xpi")) {
				ByteArrayOutputStream bufOut = new ByteArrayOutputStream();
				try {
					new FFPluginBuilder().buildXPI(bufOut, config.fileTypes);
				} catch (Exception e) {
					e.printStackTrace();
					out.write("HTTP/1.1 500 Internal Server Error\r\n\r\n"
							.getBytes());
					return;
				}
				System.out.println("Creating XPI on the fly...");
				out.write("HTTP/1.1 200 OK\r\n".getBytes());
				byte buffer[] = bufOut.toByteArray();
				out.write(("Content-Length: " + buffer.length + "\r\n")
						.getBytes());
				out.write("Content-Type: application/x-xpinstall\r\n\r\n"
						.getBytes());
				out.write(buffer);
				out.flush();
				System.out.println("XPI SENT...");
				out.close();
				return;
			}

			String line = null;
			String ua = null;
			if (url2.equals("/test")) {
				System.out.println("Checking for browser...");
				while (true) {
					line = readLine(in);
					if (line == null || line.length() < 2)
						break;
					if (line.toLowerCase().startsWith("user-agent")) {
						int index = line.indexOf(":");
						if (index > -1)
							ua = line.substring(index).trim();
						break;
					}
				}
				if (ua == null) {
					out
							.write("HTTP/1.1 200 OK\r\nContent-type: text/html\r\n\r\n<h1>XDM can not capture downloads from this browser.</h1>"
									.getBytes());
				} else {
					if (XDMProxyServer.browsers.contains(UAList.getBrowser(ua))) {
						out
								.write("HTTP/1.1 200 OK\r\nContent-type: text/html\r\n\r\n<h1>XDM can capture downloads from this browser.</h1>"
										.getBytes());

					} else {
						out
								.write("HTTP/1.1 200 OK\r\nContent-type: text/html\r\n\r\n<h1>XDM can not capture downloads from this browser.</h1>"
										.getBytes());
					}

				}
				return;
			}
			InputStream localIn = getClass().getResourceAsStream(url2);
			if (localIn == null) {
				throw new NullPointerException();
			}
			out.write("HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n"
					.getBytes());
			while (true) {
				int x = localIn.read();
				if (x == -1)
					break;
				out.write(x);
			}
		} catch (Exception e) {
			try {
				out.write("HTTP/1.1 200 OK\r\n\r\nNot Found\r\n".getBytes());
			} catch (Exception e2) {
			}
		}
	}

	private void handlePOST() throws IOException {
		try {
			type = "POST";
			System.out.println("\n\nHANDLING POST REQUEST:: " + path + "\n");
			remoteOut.write(("POST " + path + " HTTP/1.0\r\n").getBytes());
			String line = null;
			StringBuffer buf = new StringBuffer();
			while (true) {
				line = readLine(in);
				if (line == null || line.length() < 1)
					break;
				System.out.println(line);
				int index = line.indexOf(":");
				String key = line.substring(0, index).trim();
				String value = line.substring(index + 1).trim();
				// System.out.println("ADDING... " + key + ":" + value);
				requestHeader.put(key.toLowerCase(), value);
				if (!(key.equalsIgnoreCase("Accept-Encoding") || key
						.equalsIgnoreCase("Proxy-Connection")))
					buf.append(line + "\r\n");// remoteOut.write((line +
				// "\r\n").getBytes());
				if (key.toLowerCase().equals("user-agent")) {
					if (XDMProxyServer.browsers.add(UAList.getBrowser(value))) {
						setChanged();
						notifyObservers(XDMProxyServer.browsers);
					}
				}
			}
			if (cl != null) {
				cl.connected(this);
			}
			remoteOut.write((buf.toString() + "\r\n").getBytes());
			remoteOut.flush();
			int length = Integer.parseInt(requestHeader.get("content-length"));
			b = new byte[8192];
			int read = 0;
			while (true) {
				int x;
				if (length - read > b.length) {
					x = in.read(b);
				} else {
					x = in.read(b, 0, length - read);
				}
				if (x == -1)
					break;
				remoteOut.write(b, 0, x);
				read += x;
				dwn += x;
				if (length == read)
					break;
			}
			remoteOut.flush();
			responseLine = readLine(remoteIn);
			responseLine = responseLine.replace("HTTP/1.1", "HTTP/1.0");
			System.out.println(responseLine);
			out.write((responseLine + "\r\n").getBytes());
			while (true) {
				line = readLine(remoteIn);
				if (line == null || line.length() < 1)
					break;
				System.out.println(line);
				int index = line.indexOf(":");
				String key = line.substring(0, index).trim();
				String value = line.substring(index + 1).trim();
				// System.out.println("ADDING... " + key + ":" + value);
				responseHeader.put(key.toLowerCase(), value);
				out.write((line + "\r\n").getBytes());
			}
			if (cl != null) {
				cl.update(this);
			}
			out.write("connection: close\r\n\r\n".getBytes());
			out.flush();
			while (!stop) {
				int x = remoteIn.read(b);
				if (x == -1)
					break;
				out.write(b, 0, x);
			}
			out.close();
			in.close();
			remoteIn.close();
			remoteOut.close();
			remote.close();
			socket.close();
		} catch (Exception err) {
			try {
				remoteIn.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
			try {
				remoteOut.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
			try {
				remote.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
			try {
				out.write(error.getBytes());
				out.flush();
			} catch (Exception e) {
				// TODO: handle exception
			}
			try {
				in.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
			try {
				out.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
			try {
				socket.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
			throw new IOException("Error handling POST: " + err);
		}
	}

	private void handleGET() throws UnknownHostException, IOException,
			URISyntaxException {
		try {
			if (requestLine.toUpperCase().startsWith("HEAD")) {
				type = "HEAD";
				remoteOut.write(("HEAD " + path + " HTTP/1.1\r\n").getBytes());
			} else {
				type = "GET";
				remoteOut.write(("GET " + path + " HTTP/1.1\r\n").getBytes());
			}

			System.out.println("\n\nHANDLING GET REQUEST: " + path + "\n");
			String line = null;
			StringBuffer buf = new StringBuffer();
			while (true) {
				line = readLine(in);
				if (line == null || line.length() < 1)
					break;
				System.out.println(line);
				if (line.toLowerCase().startsWith("cookie")) {
					cookies.append(line + "\r\n");
				}
				int index = line.indexOf(":");
				String key = line.substring(0, index).trim();
				String value = line.substring(index + 1).trim();
				// System.out.println("ADDING... " + key + ":" + value);

				if (!(key.equalsIgnoreCase("Accept-Encoding")
						|| key.equalsIgnoreCase("Proxy-Connection") || key
						.equalsIgnoreCase("Connection"))) {
					requestHeader.put(key.toLowerCase(), value);
					buf.append(line + "\r\n");
				} else
					System.out.println("Skipping: " + key);
				if (key.equalsIgnoreCase("accept-encoding")) {
					String enc[] = value.split(",");
					for (int i = 0; i < enc.length; i++) {
						String e = enc[i].trim();
						if (e.equalsIgnoreCase("gzip")) {
							requestHeader.put("accept-encoding", "gzip");
						}
					}
					/*
					 * String enc[] = value.split(","); boolean gzip = false,
					 * deflate = false; for (int i = 0; i < enc.length; i++) {
					 * String e = enc[i].trim(); if (e.equalsIgnoreCase("gzip"))
					 * { gzip = true; } if (e.equalsIgnoreCase("deflate")) {
					 * deflate = true; } } if (gzip && deflate) {
					 * requestHeader.put("accept-encoding","gzip,deflate"); }
					 * else if (gzip) { requestHeader.put("accept-encoding",
					 * "gzip"); } else if (deflate) {
					 * requestHeader.put("accept-encoding", "deflate"); }
					 */// We use gzip as default
				}
				if (key.toLowerCase().equals("user-agent")) {
					if (XDMProxyServer.browsers.add(UAList.getBrowser(value))) {
						setChanged();
						notifyObservers(XDMProxyServer.browsers);
					}
				}

				// if (key.toLowerCase().equals("range")) {
				// try {
				// String r = value.split("=")[1];
				// String rs[] = r.split("-");
				// System.out.println("Range size: "
				// + ((Integer.parseInt(rs[1]) - Integer
				// .parseInt(rs[0])) / 1024));
				// } catch (Exception e) {
				// TODO: handle exception
				// }
				// }

				System.out.println("Key " + key);
				/*
				 * if (key.toLowerCase().equals("user-agent")) { String ua =
				 * UAList.getBrowser(value); System.out.println("Browser: " +
				 * ua); if (XDMProxyServer.browsers.add(ua)) { setChanged();
				 * notifyObservers(XDMProxyServer.browsers); } }
				 */
				// remoteOut.write((line +
				// "\r\n").getBytes());
			}
			if (config.compress) {
				String accept_encoding = requestHeader.get("accept-encoding");
				if (accept_encoding != null) {
					if (accept_encoding.toLowerCase().indexOf("gzip") != -1) {
						buf.append("Accept-Encoding: gzip\r\n");
						requestHeader.put("accept-encoding", "gzip");
					}
				}
			}

			buf.append("Connection: close\r\n");
			if (cl != null) {
				cl.connected(this);
			}
			System.out.println(buf.toString() + "\r\n");
			remoteOut.write((buf.toString() + "\r\n").getBytes());
			remoteOut.flush();
			responseLine = readLine(remoteIn);
			buf = new StringBuffer();
			// responseLine = responseLine.replace("HTTP/1.1", "HTTP/1.0");
			System.out.println(responseLine);
			buf.append(responseLine + "\r\n");
			while (true) {
				line = readLine(remoteIn);
				if (line == null || line.length() < 1)
					break;
				System.out.println(line);
				int index = line.indexOf(":");
				String key = line.substring(0, index).trim();
				String value = line.substring(index + 1).trim();
				// System.out.println("ADDING... " + key + ":" + value);
				responseHeader.put(key.toLowerCase(), value);
				// if (!(line.toLowerCase().startsWith("transfer-encoding") ||
				// line
				// .toLowerCase().startsWith("content-encoding")))
				// if (!(line.toLowerCase().startsWith("connection")))
				buf.append(line + "\r\n");
				// else
				// System.out.println("Skipping...");
				if (line.toLowerCase().startsWith("set-cookie")) {
					serverCookie.append("cookie: " + value + "\r\n");
				}
			}
			if (cl != null) {
				cl.update(this);
			}
			String type = responseHeader.get("content-type");
			System.out.println(type);
			String clen = responseHeader.get("content-length");
			// remoteIn = createInStream(remoteIn);
			// System.out.println(remoteIn + " " + remoteIn.getClass());
			int len = -1;
			if (clen != null) {
				try {
					len = Integer.parseInt(clen);
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
			System.out.println("XDM_SKIP: " + requestHeader.get("xdm-skip"));
			if ((type != null) && (requestHeader.get("xdm-skip") == null)) {
				if (!type.toLowerCase().startsWith("text/")) {
					if (config != null) {
						boolean skip = false;
						if (skippedHosts != null) {
							for (int i = 0; i < skippedHosts.size(); i++) {
								if (host.equals(skippedHosts.get(i))) {
									skip = true;
									break;
								}
							}
						}
						if (matchFileType() && (!skip)) {
							if (requestHeader.get("range") == null) {
								System.out
										.println("Rangle field is null so use this request");
								XDMHttpClient client = new XDMHttpClient(config);
								client.url = uri;
								client.ua = requestHeader.get("user-agent");
								client.in = createInStream(remoteIn);
								client.sock = remote;
								client.statusLine = responseLine;
								client.header = responseHeader;
								client.connected = client.sendGet = true;
								client.referer = requestHeader.get("referer");
								client.cook = cookies.toString();
								client.cookie = serverCookie;
								client.requestHeader = requestHeader;

								// setChanged();
								// notifyObservers(client);
								if (intercepter != null) {
									intercepter.intercept(client, this);
									if (intercept) {
										out
												.write("HTTP/1.0 204 No Content\r\n\r\n"
														.getBytes());
										in.close();
										out.close();
										socket.close();
										return;
									} else {
										System.out.println("Dont intercept");
									}
								}
							} else {
								/*
								 * System.out
								 * .println("Range is not null recreating request"
								 * ); System.out.println(); if
								 * (!UAList.getBrowser(
								 * requestHeader.get("user-agent"))
								 * .equalsIgnoreCase("chrome")) {
								 * HashMap<String, Object> ht = new
								 * HashMap<String, Object>(); ht.put("URL",
								 * uri.toString()); ht.put("HT", requestHeader);
								 * ht.put("COOKIES", cookies.toString());
								 * setChanged(); notifyObservers(ht);
								 * out.write("HTTP/1.0 204 No Content\r\n\r\n"
								 * .getBytes()); in.close(); out.close();
								 * socket.close(); return; }
								 */
							}
						} else {
							if (type.toLowerCase().startsWith("video/")
									|| type.toLowerCase().startsWith("audio/")) {
								if (model != null) {
									MediaInfo info = new MediaInfo();
									info.referer = requestHeader.get("referer");
									System.out.println("FLV REFERER: "
											+ requestHeader.get("referer"));
									info.name = XDMUtil.getFileName(uri
											.getPath());
									info.url = uri.toString();
									if (len < 0)
										info.size = "Unknown";
									else
										info.size = XDMUtil
												.getFormattedLength(len);
									info.type = type;
									model.add(info);
								}
							}
						}
					}
				}
			}
			System.out
					.println("Sending data back to browser==========================================================");
			buf.append("Proxy-Connection: close\r\n\r\n");
			out.write(buf.toString().getBytes());
			out.flush();
			b = new byte[8192];
			int read = 0;
			while (!stop) {
				int x = remoteIn.read(b);
				if (x == -1)
					break;
				out.write(b, 0, x);
				read += x;
				dwn += x;
				if (len > 0)
					if (read >= len)
						break;
			}
			out.close();
			in.close();
			remoteIn.close();
			remoteOut.close();
			remote.close();
			socket.close();
		} catch (Exception err) {
			err.printStackTrace();
			try {
				remoteIn.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
			try {
				remoteOut.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
			try {
				remote.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
			try {
				out.write(error.getBytes());
				out.flush();
			} catch (Exception e) {
				// TODO: handle exception
			}
			try {
				in.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
			try {
				out.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
			try {
				socket.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
			throw new IOException("Error handling GET: " + err);
		}
	}

	void connectToHost() throws UnknownHostException, IOException {
		System.out.println("REQUSET: " + requestLine);
		System.out.println("URI: " + uri);
		if (blockedHosts != null) {
			for (int i = 0; i < blockedHosts.size(); i++) {
				if (host.equals(blockedHosts.get(i))) {
					throw new IOException("Host is blocked");
				}
			}
		}
		remote = new Socket();
		remote.setTcpNoDelay(true);
		remote.setReceiveBufferSize(config.tcpBuf);
		remote.connect(new InetSocketAddress(host, port));
		remoteIn = remote.getInputStream();
		remoteOut = remote.getOutputStream();
		requestHeader = new HashMap<String, String>();
		responseHeader = new HashMap<String, String>();
	}

	void resolveURI(String u) throws MalformedURLException {
		u = u.trim();
		System.out.println("***************************Resolving uri: " + u);
		uri = new URL(u);
		System.out.println("URI IS:" + uri + " " + url);
		host = uri.getHost();
		port = uri.getPort();
		// protocol = uri.getScheme();
		path = uri.getPath();
		query = uri.getQuery();
		System.out.println("HOST: " + host);
		System.out.println("PORT: " + port);
		// System.out.println("PROT: " + protocol);
		if (!(path.startsWith("/"))) {
			path = "/" + path;
		}
		if (query != null) {
			path = path + "?" + query;
		}
		if (port < 0) {
			port = 80;
		}
	}

	String readLine(InputStream in) throws IOException {
		StringBuffer buf = new StringBuffer();
		while (true) {
			int x = in.read();
			if (x == -1)
				return buf.toString();
			if (x == '\n')
				return buf.toString();
			if (x != '\r')
				buf.append((char) x);
		}
	}

	String proxy_pac = "function FindProxyForURL(url, host)"
			+ "{var proxy_yes = \"PROXY 127.0.0.1:9614; DIRECT\";"
			+ "if(url.indexOf(\"http://\")==0)" + "return proxy_yes; "
			+ "else return \"DIRECT\";}";

	boolean matchFileType() {
		try {
			if (config == null)
				return false;
			String file = uri.getPath().toUpperCase();
			System.out.println("Matching: " + file);
			String type[] = config.fileTypes;
			for (int i = 0; i < type.length; i++) {
				if (file.endsWith("." + type[i])) {
					return true;
				}
			}
			System.out.println("Not Matched");
			return false;
		} catch (Exception e) {
			System.out.println("Not Matched: " + e.getMessage() + " " + e);
			e.printStackTrace();
			return false;
		}
	}

	InputStream createInStream(InputStream in) throws IOException {
		String tenc = responseHeader.get("transfer-encoding");
		InputStream in2 = in;
		if (tenc != null) {
			if (tenc.equalsIgnoreCase("chunked")) {
				in2 = new ChunkedInputStream(in);
			} else {
				throw new IOException("Transfer Encoding not supported: "
						+ tenc);
			}
		}
		String enc = responseHeader.get("content-encoding");
		InputStream in3 = in2;
		if (enc != null) {
			if (enc.equalsIgnoreCase("gzip")) {
				in3 = new GZIPInputStream(in2);
			} else if (enc.equalsIgnoreCase("deflate")) {
				in3 = new DeflaterInputStream(in);
			} else if (enc.equalsIgnoreCase("none")) {
				in3 = in2;
			} else {
				throw new IOException("Content Encoding not supported: " + enc);
			}
		}
		return in3;
	}

	@Override
	public String getApplication() {
		if (requestHeader == null) {
			return "";
		}
		String ua = requestHeader.get("user-agent");
		if (ua == null) {
			return "Unknown";
		}
		return UAList.getBrowser(ua);
	}

	@Override
	public String getContentLength() {
		if (responseHeader == null) {
			return "";
		}
		String len = responseHeader.get("content-length");
		if (len == null) {
			return "Unknown";
		}
		return len;

	}

	@Override
	public String getContentType() {
		if (responseHeader == null) {
			return "";
		}
		String ct = responseHeader.get("content-type");
		if (ct == null) {
			return "Unknown";
		}
		return ct;

	}

	@Override
	public String getDownloaded() {
		return dwn + " Bytes";
	}

	@Override
	public String getHost() {
		return host;
	}

	@Override
	public String getRequest() {
		if (requestHeader == null)
			return "";
		StringBuffer r = new StringBuffer();
		r.append(requestLine + "\r\n");
		Iterator<String> it = requestHeader.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			String val = requestHeader.get(key);
			r.append(key + ": " + val + "\r\n");
		}
		r.append(cookies + "\r\n");
		return r.toString();
	}

	@Override
	public String getResponse() {
		if (responseHeader == null)
			return "";
		StringBuffer r = new StringBuffer();
		r.append(responseLine + "\r\n");
		Iterator<String> it = responseHeader.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			String val = responseHeader.get(key);
			r.append(key + ": " + val + "\r\n");
		}
		r.append(cookies + "\r\n");
		return r.toString();
	}

	@Override
	public String getURL() {
		return url;
	}

	@Override
	public String getHTTPTYPE() {
		return type;
	}

	@Override
	public void stop() {
		stop = true;
	}

}
