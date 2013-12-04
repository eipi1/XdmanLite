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

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class MediaGrabberWindow extends JFrame implements ActionListener,
		ItemListener {
	private static final long serialVersionUID = 8673545666964719266L;
	JButton download, remove, copy;
	MediaTableModel model;
	JTable table;
	Main mw;
	JCheckBox top;

	public MediaGrabberWindow(MediaTableModel model, Main mw) {
		setIconImage(Main.icon.getImage());
		this.model = model;
		this.mw = mw;
		init();
	}

	public void init() {
		setSize(500, 300);
		setTitle("XDM Media Grabber");
		table = new JTable(model);
		table.setAutoCreateRowSorter(true);
		add(new JScrollPane(table));
		Box box = Box.createHorizontalBox();
		top = new JCheckBox("Always on top");
		top.addItemListener(this);
		box.add(top);
		box.add(Box.createHorizontalGlue());
		copy = new JButton("Copy");
		copy.addActionListener(this);
		download = new JButton("Download");
		download.addActionListener(this);
		remove = new JButton("Remove");
		remove.addActionListener(this);
		box.add(copy);
		box.add(Box.createHorizontalStrut(10));
		box.add(download);
		box.add(Box.createHorizontalStrut(10));
		box.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		box.add(remove);
		add(box, BorderLayout.SOUTH);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if (e.getSource() == download) {
			System.out.println("Download video");
			int index = table.getSelectedRow();
			if (index < 0) {
				JOptionPane.showMessageDialog(this, "No item selected");
				return;
			}
			index = table.convertRowIndexToModel(index);
			MediaInfo info = model.list.get(index);
			if (mw != null) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("referer", info.referer);
				//mw.addURL(info.url, null, map, null);
			}
		} else if (e.getSource() == remove) {
			System.out.println("Remove video");
			int index[] = table.getSelectedRows();
			if (index.length < 1) {
				JOptionPane.showMessageDialog(this, "No item selected");
				return;
			}
			MediaInfo info[] = new MediaInfo[index.length];
			for (int i = 0; i < index.length; i++) {
				int row = table.convertRowIndexToModel(index[i]);
				info[i] = model.list.get(row);
			}
			for (int i = 0; i < index.length; i++) {
				model.list.remove(info[i]);
			}
			model.fireTableDataChanged();
		} else if (e.getSource() == copy) {
			int index = table.getSelectedRow();
			if (index < 0) {
				JOptionPane.showMessageDialog(this, "No item selected");
				return;
			}
			index = table.convertRowIndexToModel(index);
			MediaInfo info = model.list.get(index);
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
					new StringSelection(info.url), null);

		}
	}

	@Override
	public void itemStateChanged(ItemEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("On top");
		setAlwaysOnTop(top.isSelected());
	}
}
