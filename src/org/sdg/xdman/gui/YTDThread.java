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


package org.sdg.xdman.gui;

import java.util.ArrayList;

import org.sdg.xdman.core.common.XDMConfig;
import org.sdg.xdman.plugin.youtube.ParserProgressListner;
import org.sdg.xdman.plugin.youtube.YoutubeGrabber;

public class YTDThread implements Runnable {
	String url;
	YoutubeGrabber ytg;
	XDMConfig config;
	boolean stop;
	YoutubeListener listener;
	ParserProgressListner plistner;

	public YTDThread(String url, XDMConfig config, YoutubeListener listener) {
		this.url = url;
		this.config = config;
		this.listener = listener;
	}

	@Override
	public void run() {
		ytg = new YoutubeGrabber();
		ArrayList<String> list = new ArrayList<String>();
		try {
			ytg.setParserListener(plistner);
			list = ytg.getVideoURLs(url, config);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (!stop) {
			int count = 0;
			if (list == null) {
				count = 0;
			} else {
				count = list.size();
			}
			System.out.println("Total " + count + " Links found");
			if (listener != null) {
				if (count == 0) {
					listener.parsingFailed();
				} else {
					listener.parsingComplete(list);
				}
			}
		}
	}

	public void stop() {
		System.out.println("Stopping...");
		stop = true;
		ytg.stop();
	}

	public void start() {
		new Thread(this).start();
	}

}
