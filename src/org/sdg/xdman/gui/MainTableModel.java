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

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import javax.swing.Icon;

import javax.swing.table.AbstractTableModel;

import org.sdg.xdman.util.XDMUtil;

public class MainTableModel extends AbstractTableModel implements Observer {
	DownloadList list = null;// new DownloadList();
	Icon q;

	public void setList(DownloadList list) {
		this.list = list;
		fireTableDataChanged();
	}

	void setType(String type) {
		list.setType(type);
	}

	@Override
	public Class<?> getColumnClass(int col) {
		// TODO Auto-generated method stub
		if (col == 0 || col == 2) {
			return Icon.class;
		} else {
			return String.class;
		}
	}

	private static final long serialVersionUID = -8936395745120671317L;
	final String cols[] = { "", "File Name", "Q", "Size", "Status",
			"Time left", "Transfer rate", "Last Try", "Description",
			"Date Added", "Save To", "URL", "Referer" };

	@Override
	public String getColumnName(int col) {
		// TODO Auto-generated method stub
		return cols[col];
	}

	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return cols.length;
	}

	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		// System.out.println("LIST SIZE: "+list.size());
		return list.size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		// System.out.println("CAlled");
		// TODO Auto-generated method stub
		DownloadListItem item = list.get(row);
		if (q == null) {
			q = Main.getIcon("q.png");
		}
		switch (col) {
		case 0:
			return item.icon;
		case 1:
			return XDMUtil.nvl(item.filename);
		case 2:
			return item.q ? this.q : null;
		case 3:
			return XDMUtil.nvl(item.size);
		case 4:
			return XDMUtil.nvl(item.status);
		case 5:
			return XDMUtil.nvl(item.timeleft);
		case 6:
			return XDMUtil.nvl(item.transferrate);
		case 7:
			return XDMUtil.nvl(item.lasttry);
		case 8:
			return XDMUtil.nvl(item.description);
		case 9:
			return XDMUtil.nvl(item.dateadded);
		case 10:
			return XDMUtil.nvl(item.saveto);
		case 11:
			return XDMUtil.nvl(item.url);
		case 12:
			HashMap<String, String> hm = item.extra;
			if (hm == null)
				return "";
			else {
				String ref = hm.get("referer");
				return (ref == null ? "" : ref);
			}
		default:
			return "";
		}
	}

	@Override
	public void update(Observable o, Object obj) {
		DownloadListItem item = (DownloadListItem) o;
		int index = list.getIndex(item);
		if (index < 0)
			return;
		// System.out.println("Index: " + index + " " + list.state + " "
		// + list.type);
		fireTableRowsUpdated(index, index);
	}
}
