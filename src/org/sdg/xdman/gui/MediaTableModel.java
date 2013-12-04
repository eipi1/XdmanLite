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

import javax.swing.table.AbstractTableModel;

public class MediaTableModel extends AbstractTableModel {

	Main mw;

	private static final long serialVersionUID = 3687589857430853297L;

	ArrayList<MediaInfo> list = new ArrayList<MediaInfo>();

	String cols[] = { "Name", "Type", "Size", "URL" };

	public MediaTableModel(Main mw) {
		this.mw = mw;
	}

	@Override
	public Class<?> getColumnClass(int arg0) {
		// TODO Auto-generated method stub
		return String.class;
	}

	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return cols.length;
	}

	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getValueAt(int arg0, int arg1) {
		// TODO Auto-generated method stub
		MediaInfo info = list.get(arg0);
		switch (arg1) {
		case 0:
			return info.name;
		case 1:
			return info.type;
		case 2:
			return info.size;
		case 3:
			return info.url;
		}
		return "";
	}

	public void add(MediaInfo info) {
		for (int i = 0; i < list.size(); i++) {
			MediaInfo mi = list.get(i);
			if (mi.url.equals(info.url)) {
				return;
			}
		}
		list.add(info);
		fireTableDataChanged();
		mw.showTrayMessage();
	}

	public void remove(int index) {
		list.remove(index);
		fireTableDataChanged();
	}

	@Override
	public String getColumnName(int arg0) {
		// TODO Auto-generated method stub
		return cols[arg0];
	}

}
