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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Observer;

import org.sdg.xdman.core.common.XDMConfig;
import org.sdg.xdman.gui.MediaTableModel;

public class XDMProxyServer {
	public static HashSet<String> browsers = new HashSet<String>();
	public Observer observer;
	ServerSocket server;
	XDMConfig config;
	public MediaTableModel model;
	boolean stop = false;
	public RequestIntercepter intercepter;
	HashMap<String, String> arg;
	public IConnectionListener cl;
	public ArrayList<String> blockedHosts, skippedHosts;

	public XDMProxyServer(Observer o, XDMConfig config, MediaTableModel m,
			RequestIntercepter intercepter, HashMap<String, String> arg) {
		this.observer = o;
		this.config = config;
		this.model = m;
		this.intercepter = intercepter;
		this.arg = arg;
	}

	public boolean init() {
		System.out.println("Config port: " + config.port);
		int port = config.port;
		try {
			server = new ServerSocket(port);
			System.out.println("XDM Module running on: " + port);
			return true;
		} catch (Exception e) {
			try {
				System.out.println("Sending param " + arg);
				Socket sock = new Socket("localhost", port);
				OutputStream out = sock.getOutputStream();
				out.write("PARAM\r\n".getBytes());
				if (arg.size() > 0) {
					Iterator<String> keys = arg.keySet().iterator();
					while (keys.hasNext()) {
						String key = keys.next();
						String value = arg.get(key);
						out.write((key + ": " + value + "\r\n").getBytes());
					}
				}
				out.write("\r\n".getBytes());
				out.flush();
				InputStream in = sock.getInputStream();
				byte buf[] = new byte[2];
				in.read(buf);
			} catch (Exception err) {
			}
			return false;
		}
	}

	public void start() {
		while (!stop) {
			try {
				handleRequest();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	void handleRequest() {
		try {
			Socket sock = server.accept();
			RequestHandler r = new RequestHandler();
			r.blockedHosts = blockedHosts;
			r.skippedHosts = skippedHosts;
			r.cl = this.cl;
			r.intercepter = intercepter;
			r.model = model;
			r.config = config;
			if (observer != null)
				r.addObserver(observer);
			r.socket = sock;
			r.start();
		} catch (IOException e) {
			// se.printStackTrace();
		}
	}

	public void stop() {
		try {
			stop = true;
			server.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public static void main(String[] args) throws IOException {
		new XDMProxyServer(null, null, null, null, null);
	}
}
