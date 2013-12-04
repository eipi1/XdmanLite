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

import org.sdg.xdman.proxy.IConnection;
import org.sdg.xdman.proxy.IConnectionListener;

public class HttpTableModel extends AbstractTableModel implements
		IConnectionListener {

	private static final long serialVersionUID = -4973440686633702921L;

	String cols[] = { "URL", "Application", "Type", "Length", "HTTP", "Host" };
	ArrayList<IConnection> arr = new ArrayList<IConnection>();

	@Override
	public int getColumnCount() {
		return cols.length;
	}

	@Override
	public int getRowCount() {
		return arr.size();
	}

	@Override
	public String getColumnName(int c) {
		return cols[c];
	}

	@Override
	public Object getValueAt(int r, int c) {
		try {
			IConnection cn = arr.get(r);
			switch (c) {
			case 0:
				return cn.getURL();
			case 1:
				return cn.getApplication();
			case 2:
				return cn.getContentType();
			case 3:
				return cn.getContentLength();
			case 4:
				return cn.getHTTPTYPE();
			case 5:
				return cn.getHost();
			default:
				return "";
			}
		} catch (Exception e) {
			return "";
		}
	}

	@Override
	public void closed(IConnection c) {
		try{
		arr.remove(c);
		fireTableDataChanged();
		}catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Override
	public void connected(IConnection c) {
		try{
		arr.add(c);
		fireTableDataChanged();
		}catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Override
	public void update(IConnection c) {
		try {
			int index = arr.indexOf(c);
			if (index != -1) {
				fireTableRowsUpdated(index, index);
			}
		} catch (Exception e) {
		}
	}

}
