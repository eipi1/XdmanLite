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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.DeflaterInputStream;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.sdg.xdman.core.common.AuthenticationException;
import org.sdg.xdman.core.common.XDMConfig;
import org.sdg.xdman.core.common.http.io.ChunkedInputStream;
import org.sdg.xdman.util.Base64;

public class XDMHttpClient {
	public boolean connected = false, sendGet = false;
	public Socket sock;
	public URL url;
	public String host, path, protocol, query, referer, ua;
	int port;
	List<String> head = new ArrayList<String>();
	public HashMap<String, String> header = new HashMap<String, String>();
	public OutputStream out;
	public InputStream in;
	public String statusLine;
	public String cook;
	public StringBuffer cookie = new StringBuffer();
	public HashMap<String, String> requestHeader;
	boolean usingProxy = false;
	final int HTTP_PROXY = 10, HTTPS_PROXY = 20, NONE = 30;
	int proxyType = NONE;
	XDMConfig config;
	// public static int BUF_SIZE =8 * 1024;
	long len;

	public XDMHttpClient(XDMConfig config) {
		this.config = config;
	}

	public String getHostString() {
		if (port != 80 || port != 443) {
			return host + ":" + port;
		}
		return host;
	}

	public void addCookieString(String cookie) {
		cook = cookie;
	}

	public String getCookieString() {
		return cookie.toString();
	}

	public void addHeader(String key, String value) {
		String hkey = key.toLowerCase();
		for (int i = 0; i < head.size(); i++) {
			String str = head.get(i);
			String mkey = str.substring(0, str.indexOf(":")).toLowerCase()
					.trim();
			if (mkey.equalsIgnoreCase(hkey)) {
				head.set(i, hkey + ": " + value);
				return;
			}
		}
		head.add(key + ": " + value);
	}

	public String getHeader(String key) {
		return header.get(key.toLowerCase());
	}

	public void connect(String uri) throws UnknownHostException, IOException,
			URISyntaxException, AuthenticationException {
		if (connected)
			return;
		uri = uri.trim();
		url = new URL(uri);
		System.out.println("URI IS:" + uri + " " + url);
		host = url.getHost();
		port = url.getPort();
		protocol = url.getProtocol();
		path = url.getPath();
		query = url.getQuery();
		System.out.println("HOST: " + host);
		System.out.println("PORT: " + port);
		System.out.println("PROT: " + protocol);
		if (path == null)
			path = "/";
		if (!(path.startsWith("/"))) {
			path = "/" + path;
		}
		if (query != null) {
			path = path + "?" + query;
		}
		if (port < 0) {
			if (protocol.equals("http")) {
				port = 80;
			}
			if (protocol.equals("https"))
				port = 443;
		}
		// System.out.println("Keep alive " + sock.getKeepAlive());
		if (protocol.equalsIgnoreCase("http")) {
			sock = new Socket();
			sock.setTcpNoDelay(true);
			sock.setReceiveBufferSize(config.tcpBuf);
			if (config.useHttpProxy) {
				proxyType = HTTP_PROXY;
				String proxyHost = config.httpProxyHost;
				int proxyPort = config.httpProxyPort;
				sock.connect(new InetSocketAddress(proxyHost, proxyPort));
			} else {
				proxyType = NONE;
				sock.connect(new InetSocketAddress(host, port));
			}
		} else if (protocol.equalsIgnoreCase("https")) {
			if (config.useHttpsProxy) {
				proxyType = HTTPS_PROXY;
				sock = new Socket();
				sock.setReceiveBufferSize(config.tcpBuf);
				sock.setTcpNoDelay(true);
				doTunneling(config.httpProxyHost, config.httpsProxyPort);
			} else {
				proxyType = NONE;
				sock = SSLSocketFactory.getDefault().createSocket();
				sock.setTcpNoDelay(true);
				sock.setReceiveBufferSize(config.tcpBuf);
				sock.connect(new InetSocketAddress(host, port));
			}
		} else {
			throw new IOException("Protocol " + protocol + " is not supported");
		}
		sock.setTcpNoDelay(true);
		in = sock.getInputStream();
		out = sock.getOutputStream();
		String h = host;
		if (!(port == 80 || port == 443))
			h += (":" + port);
		sock.setKeepAlive(true);
		addHeader("Host", h);
		// addHeader("Accept", "*/*");
		// addHeader("Accept-Encoding", "gzip, deflate");
		// addHeader("Connection", "Close");
		// addHeader("Accept-Encoding", "gzip,deflate");

		// System.out.println("DefaultBufferSize: " +
		// sock.getReceiveBufferSize()
		// + " keep-alive: " + sock.getKeepAlive() + " Reuse-address"
		// + sock.getReuseAddress() + " Tcp no delay"
		// + sock.getTcpNoDelay() + " Traffic-class"
		// + sock.getTrafficClass());
		System.out.println("Buffer Size: " + sock.getReceiveBufferSize() + " "
				+ sock.getTcpNoDelay());
		connected = true;
	}

	// GET / HTTP/1.1
	// User-Agent: Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0)
	// Host: localhost:9615
	// Accept: */*
	// Referer: http://localhost:9615/
	// Content-Disposition: attachment; filename=Stephenie Meyer-New Moon-Book
	// 2.pdf
	// Content-Disposition:attachment; filename=Stephenie Meyer-New Moon-Book
	// 2.pdf

	void doTunneling(String host, int port) throws IOException,
			AuthenticationException {
		sock.connect(new InetSocketAddress(host, port));
		StringBuffer buf = new StringBuffer();

		String proxyUser = config.httpsUser;
		String proxyPass = config.httpsPass;
		boolean proxyAuth = false;
		if (!(proxyUser == null || proxyUser.length() < 1)) {
			if (!(proxyPass == null || proxyPass.length() < 1)) {
				proxyAuth = true;
			}
		}
		String authString = "";
		if (proxyAuth) {
			authString = "Proxy-Authorization: "
					+ "Basic "
					+ Base64.encode((config.httpsUser + ":" + config.httpsPass)
							.getBytes()) + "\r\n";

		}
		buf.append("CONNECT " + this.host + ":" + this.port
				+ " HTTP/1.1\r\nProxy-Connection: Close\r\n"
				+ (authString.length() < 1 ? "" : authString) + "\r\n");
		out = sock.getOutputStream();
		in = sock.getInputStream();
		out.write(buf.toString().getBytes());
		out.flush();
		String statLine = readLine(in);
		System.out.println("Proxy Status LINE: " + statLine);
		String rc = statLine.split(" ")[1];
		if (rc.equals("407"))
			throw new AuthenticationException("Proxy Authentication required");
		if (!rc.equals("200")) {
			throw new IOException("Unable to establish connection with proxy");
		}
		SSLSocket sock2 = (SSLSocket) ((SSLSocketFactory) SSLSocketFactory
				.getDefault()).createSocket(sock, this.host, this.port, true);
		sock2.startHandshake();
		sock = sock2;
	}

	public void sendGET() throws IOException {
		if (sendGet)
			return;
		System.out.println(path);
		if (path.length() < 1) {
			path = "/";
		}
		if (proxyType == HTTP_PROXY) {
			path = url.toString();
		}
		StringBuffer buf = new StringBuffer("GET " + path + " HTTP/1.1\r\n");
		if (proxyType == HTTP_PROXY) {
			addHeader("Proxy-Connection", "Close");
			System.out.println("USING PROXY");
			String proxyUser = config.httpUser;
			String proxyPass = config.httpPass;
			boolean proxyAuth = false;
			if (!(proxyUser == null || proxyUser.length() < 1)) {
				if (!(proxyPass == null || proxyPass.length() < 1)) {
					proxyAuth = true;
				}
			}
			if (proxyAuth) {
				System.out.println("Proxy Auth");
				addHeader(
						"Proxy-Authorization",
						"Basic "
								+ Base64
										.encode((config.httpUser + ":" + config.httpPass)
												.getBytes()));
			}
		}
		for (int i = 0; i < this.head.size(); i++) {
			String h = head.get(i);
			if (!h.toLowerCase().startsWith("connection:"))
				buf.append(h + "\r\n");
		}
		if (cook != null) {
			System.out.println("SETTIMG COOOOOOOOOOOOOOOOOOOOOOOOOOOOKIE: "
					+ cook);
			buf.append(cook);
		}
		if (!buf.toString().endsWith("\r\n")) {
			buf.append("\r\n");
		}
		buf.append("Connection: close\r\n");
		buf.append("\r\n");
		System.out.println("HEADERS: " + buf);
		out.write(buf.toString().getBytes());
		out.flush();
		// in = sock.getInputStream();
		System.out.println("Waiting for server response " + sock);
		statusLine = readLine(in);
		System.out.println("STATUS LINE: " + statusLine);
		while (true) {
			String ln = readLine(in);
			System.out.println("RAW HEADER: " + ln);
			if (ln.length() < 1)
				break;
			int index = ln.indexOf(":");
			// String arr[] = ln.split(":");
			String key = ln.substring(0, index).trim();// arr[0].trim().toLowerCase();
			String value = ln.substring(index + 1).trim();// new StringBuffer()
			System.out.println(key + ":" + value);
			header.put(key.toLowerCase(), value);
			if (ln.toLowerCase().startsWith("set-cookie")) {
				cookie.append("cookie: " + value + "\r\n");
				System.out.println("Accepting cookie");
			}
		}
		String tenc = getHeader("transfer-encoding");
		InputStream in2 = in;
		if (tenc != null) {
			if (tenc.equalsIgnoreCase("chunked")) {
				in2 = new ChunkedInputStream(in);
			} else {
				throw new IOException("Transfer Encoding not supported: "
						+ tenc);
			}
		}
		String enc = getHeader("content-encoding");
		InputStream in3 = in2;
		if (enc != null) {
			if (enc.equalsIgnoreCase("gzip")) {
				in3 = new GZIPInputStream(in2);
			} else if (enc.equalsIgnoreCase("deflate")) {
				in3 = new DeflaterInputStream(in2);
			}else if (enc.equalsIgnoreCase("none")) {
				in3 = in2;
			} else {
				throw new IOException("Content Encoding not supported: " + enc);
			}
		}
		in = in3;
		sendGet = true;
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

	public InputStream getInputStream() {
		return in;
	}

	public void close() {
		try {
			sock.close();
		} catch (Exception e) {
		}
		connected = false;
		sendGet = false;
		sock = null;
	}

	public void setTimeOut(int timeout) {
		try {
			sock.setSoTimeout(timeout);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public int getResponseCode() {
		System.out.println("Parsing status line");
		String arr[] = statusLine.split(" ");
		if (arr.length < 2)
			return 400;
		return Integer.parseInt(arr[1]);
	}

	public long getContentLength() {
		try {
			String clen = getHeader("content-length");
			System.out.println("Cstr: " + clen);
			if (clen != null) {
				return Long.parseLong(clen);
			} else {
				clen = getHeader("content-range");
				if (clen != null) {
					// Content-Length: 8063277
					// Content-Range: bytes 7039344-15102620/15102621
					String str = clen.split(" ")[1];
					str = str.split("/")[0];
					String arr[] = str.split("-");
					return Long.parseLong(arr[1]) - Long.parseLong(arr[0]) + 1;
				} else {
					return -1;
				}
			}
		} catch (Exception e) {
			return -1;
		}
	}

	public String getContentName() {
		try {
			String cd = getHeader("content-disposition");
			if (cd == null)
				return null;
			cd = cd.toLowerCase();
			if (cd.startsWith("attachment")) {
				String fm = cd.split(";")[1].trim();
				int index = fm.indexOf("=");
				if (index < 0)
					return null;
				return fm.substring(index + 1).trim();
			}
		} catch (Exception e) {
		}
		return null;
	}

	public void setBufSize(int sz) throws SocketException {
		sock.setReceiveBufferSize(sz);
	}

	public int getBufSize() throws SocketException {
		return sock.getReceiveBufferSize();
	}

	public void prepareReuse() {
		sendGet = false;
		this.head = new ArrayList<String>();
		String h = host;
		if (!(port == 80 || port == 443))
			h += (":" + port);
		addHeader("Host", h);
		// addHeader("Accept", "*/*");
		// addHeader("Accept-Encoding", "gzip, deflate");
		addHeader("Connection", "Keep-Alive");

	}
	/*
	 * public static void main(String[] args) throws UnknownHostException,
	 * IOException { XDMHttpClient client = new XDMHttpClient();
	 * client.connect("https://www.verisign.com"); client.sendGET();
	 * BufferedReader r = new BufferedReader(new InputStreamReader(client
	 * .getInputStream())); System.out.println(r.readLine()); }
	 */
}
