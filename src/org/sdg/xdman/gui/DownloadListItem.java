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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
import javax.swing.Icon;

import org.sdg.xdman.core.common.ConnectionManager;
import org.sdg.xdman.core.common.DownloadInfo;
import org.sdg.xdman.core.common.IXDMConstants;
import org.sdg.xdman.util.XDMUtil;

public class DownloadListItem extends Observable implements Observer,
		Serializable {
	private static final long serialVersionUID = -4925098929484510725L;
	IDownloadListener listener;
	String cookies;

	void setCallback(ConnectionManager mgr, Observer o, IDownloadListener l) {
		this.mgr = mgr;
		mgr.addObserver(this);
		addObserver(o);
		this.listener = l;
	}

	public DownloadListItem() {
	}

	String filename;
	boolean q;
	int state;
	String status, timeleft, transferrate, lasttry, description, dateadded,
			saveto, type, url, size, tempdir = "";
	transient ConnectionManager mgr;
	transient JFrame window;
	transient Icon icon;
	public HashMap<String, String> extra;

	// this method will be called by connection manager
	@Override
	public void update(Observable o, Object obj) {
		if (this.mgr == null) {
			return;
		}
		DownloadInfo info = (DownloadInfo) obj;
		this.status = info.status;
		this.timeleft = info.eta;
		this.transferrate = info.speed;
		this.url = info.url;
		this.size = info.length;
		this.type = info.category;
		this.state = info.state;
		if (info.state == IXDMConstants.COMPLETE
				|| info.state == IXDMConstants.STOPPED
				|| info.state == IXDMConstants.FAILED) {
			this.mgr = null;
			this.window = null;
			System.out.println("removed connection mgr");
			if (info.state == IXDMConstants.COMPLETE) {
				q = false;
				this.status = "Download complete";
			} else
				this.status = "Stopped";
			if (listener != null)
				listener.downloadStateChanged();
			listener = null;
		}
		if (info.state == IXDMConstants.REDIRECTING) {
			tempdir = info.tempdir;
			filename = info.file;
			icon = IconUtil.getIcon(XDMUtil.findCategory(filename));
			if (listener != null)
				listener.downloadStateChanged();
		}
		setChanged();
		notifyObservers(this);
	}

	public void setDest(String dest) {
		if (mgr != null) {
			saveto = dest;
			mgr.setDestdir(dest);
		}
	}

}
