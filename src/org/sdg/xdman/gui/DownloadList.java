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
import java.util.LinkedList;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.File;
import org.sdg.xdman.util.XDMUtil;

import org.sdg.xdman.core.common.IXDMConstants;

public class DownloadList implements IXDMConstants, Serializable,
		IDownloadListener {
	private static final long serialVersionUID = -3009294778243929872L;
	LinkedList<DownloadListItem> list = new LinkedList<DownloadListItem>();
	String type;
	int state;
	String appdir;

	public DownloadList(String appdir) {
		this.appdir = appdir;
		loadDownloadList();
	}

	DownloadListItem get(int index) {
		if (type == null && state == 0)
			return list.get(index);
		int k = 0;
		for (int i = 0; i < list.size(); i++) {
			DownloadListItem item = list.get(i);
			if (sameType(item.type) && sameState(item.state)) {
				if (k == index)
					return item;
				k++;
			}
		}
		return null;
	}

	void remove(DownloadListItem item) {
		list.remove(item);
	}

	void remove(int index) {
		if (type == null && state == 0) {
			list.remove(index);
		}
		int k = 0;
		for (int i = 0; i < list.size(); i++) {
			DownloadListItem item = list.get(i);
			if (sameType(item.type) && sameState(item.state)) {
				if (k == index)
					list.remove(i);
				k++;
			}
		}

	}

	void add(DownloadListItem item) {
		list.add(item);
	}

	int size() {
		if (type == null && state == 0)
			return list.size();
		int k = 0;
		for (int i = 0; i < list.size(); i++) {
			DownloadListItem item = list.get(i);
			if (sameType(item.type) && sameState(item.state)) {
				k++;
			}
		}
		return k;
	}

	void setType(String type) {
		this.type = type;
	}

	void setState(int state) {
		this.state = state;
	}

	int getIndex(DownloadListItem item) {
		// System.out.println("LIST: " + type + " " + state);
		if (type == null && state == 0)
			return list.indexOf(item);
		// System.out.println("LISTz: " + type + " " + state);
		int k = 0;
		for (int i = 0; i < list.size(); i++) {
			DownloadListItem itm = list.get(i);
			if (sameType(itm.type) && sameState(itm.state)) {
				if (item.equals(itm)) {
					return k;
				}
				k++;
			}
		}
		return -1;
	}

	boolean sameType(String type) {
		if (this.type == null)
			return true;
		return this.type.equalsIgnoreCase(type);
	}

	boolean sameState(int state) {
		if (this.state == 0)
			return true;
		if (this.state == COMPLETE) {
			return this.state == state;
		} else {
			return state != COMPLETE;
		}
	}

	public void downloadStateChanged() {
		saveDownloadList();
	}

	private synchronized void saveDownloadList() {
		System.out.println("Saving config...");
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(new FileOutputStream(new File(appdir,
					".xdmlist")));
			out.writeObject(list);
			System.out.println("Done");
			// System.out.println("State saved");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (Exception e) {
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void loadDownloadList() {
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(new FileInputStream(new File(appdir,
					".xdmlist")));
			list = (LinkedList<DownloadListItem>) in.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (Exception e) {
			}
		}
		if (list == null) {
			list = new LinkedList<DownloadListItem>();
		}
		for (int i = 0; i < list.size(); i++) {
			DownloadListItem item = list.get(i);
			item.icon = IconUtil.getIcon(XDMUtil.findCategory(item.filename));
			if (!(item.state == IXDMConstants.COMPLETE || item.state == IXDMConstants.FAILED)) {
				item.state = IXDMConstants.STOPPED;
				item.status = "Stopped";
			}
		}
	}
}
