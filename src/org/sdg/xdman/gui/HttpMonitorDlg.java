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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;

import org.sdg.xdman.proxy.IConnection;

public class HttpMonitorDlg extends JFrame implements ActionListener {

	private static final long serialVersionUID = -1599332187194114536L;

	HttpTableModel model;

	JTable table;

	JButton stop, copy;

	JTextArea headers;

	HttpMonitorDlg(HttpTableModel model) {
		setTitle("HTTP Monitor");
		try {
			setIconImage(Main.icon.getImage());
		} catch (Exception e) {
		}
		JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		this.model = model;
		table = new JTable(model);
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int index = table.getSelectedRow();
				if (index < 0) {
					return;
				}

				IConnection c = HttpMonitorDlg.this.model.arr.get(table
						.convertRowIndexToModel(index));
				headers.setText("----Request Headers----\n" + c.getRequest()
						+ "\n\n----Response Headers----\n" + c.getResponse());
			}
		});
		pane.add(new JScrollPane(table));
		JPanel tool = new JPanel();
		stop = new JButton("Stop Connection");
		tool.add(stop);
		stop.addActionListener(this);
		copy = new JButton("Copy Address");
		tool.add(copy);
		copy.addActionListener(this);
		add(tool, BorderLayout.NORTH);
		headers = new JTextArea();
		headers.setEditable(false);
		pane.add(new JScrollPane(headers));
		pane.setDividerLocation(200);
		add(pane);
		add(new JLabel("   HTTP traffic captured by XDM"), BorderLayout.SOUTH);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int index = table.getSelectedRow();
		if (index == -1) {
			JOptionPane.showMessageDialog(this, "No item selected");
			return;
		}
		IConnection c = model.arr.get(table.convertRowIndexToModel(index));
		if (e.getSource() == stop) {
			c.stop();
		} else {
			String url = c.getURL();
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
					new StringSelection(url), null);
		}
	}

}
