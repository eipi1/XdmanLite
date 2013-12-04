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


package org.sdg.xdman.plugin.youtube;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import org.sdg.xdman.core.common.XDMConfig;
import org.sdg.xdman.core.common.http.XDMHttpClient;

public class YoutubeGrabber {

	private XDMHttpClient client;
	boolean stop;
	ParserProgressListner plistener;

	public void setParserListener(ParserProgressListner p) {
		this.plistener = p;
	}

	public void stop() {
		try {
			client.close();
			System.out.println("Stopped YTD");
		} catch (Exception e) {
		}
	}

	public ArrayList<String> getVideoURLs(String yturl, XDMConfig config)
			throws Exception {
		ArrayList<String> list = new ArrayList<String>();
		try {
			list = getURLs(yturl, config);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			client.close();
		} catch (Exception e) {
		}
		return list;
	}

	ArrayList<String> getURLs(String url, XDMConfig config) throws Exception {
		client = new XDMHttpClient(config);
		client.connect(url);
		if (stop) {
			return null;
		}
		client.addHeader("accept-encoding", "gzip");
		client.sendGET();
		int rc = client.getResponseCode();
		if (!(rc == 200 || rc == 206)) {
			throw new Exception("Invalid response: " + rc);
		}
		InputStream in = new BufferedInputStream(client.getInputStream());
		JSONParser p = new JSONParser();
		p.plistener = plistener;
		return p.list(in);
	}
}