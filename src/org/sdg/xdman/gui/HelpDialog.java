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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class HelpDialog extends JFrame implements ListSelectionListener,
		ActionListener {

	private static final long serialVersionUID = 2861769092407816472L;
	JButton back, next;
	JEditorPane htmlPane;
	HelpListModel model;
	JList helpList;

	public HelpDialog() {
		setTitle("Quick Help");
		setSize(640, 480);
		setLocationRelativeTo(null);
		htmlPane = new JEditorPane();
		JPanel panel = new JPanel(new BorderLayout(5, 5));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.add(new JScrollPane(htmlPane));
		add(panel);
		model = new HelpListModel();
		helpList = new JList(model);
		JPanel p = new JPanel(new BorderLayout());
		p.add(createToolBar(), BorderLayout.NORTH);
		p.add(new JScrollPane(helpList));
		panel.add(p, BorderLayout.WEST);
		try {
			setIconImage(Main.icon.getImage());
		} catch (Exception e) {
		}
		helpList.addListSelectionListener(this);
		htmlPane.setEditable(false);
	}

	JToolBar createToolBar() {
		JToolBar toolbar = new JToolBar();
		next = new JButton(Main.getIcon("next.png"));
		next.addActionListener(this);
		next.setRolloverIcon(Main.getIcon("next_r.png"));
		back = new JButton(Main.getIcon("back.png"));
		back.addActionListener(this);
		back.setRolloverIcon(Main.getIcon("back_r.png"));
		toolbar.add(back);
		toolbar.add(next);
		return toolbar;// add(toolbar, BorderLayout.NORTH);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		int index = helpList.getSelectedIndex();
		if (index < 0)
			return;
		String key = helpList.getSelectedValue() + "";
		URL url = model.map.get(key);
		if (url != null) {
			setDocument(url);
		}
	}

	public void setDocument(URL url) {
		try {
			setPage(url);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Page could not be displayed");
		}
	}

	void setPage(URL url) throws IOException {
		htmlPane.setPage(url);
	}

	public void addPages(HashMap<String, URL> map) {
		model.map = map;
		helpList.setModel(model);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == back) {
			int index = helpList.getSelectedIndex();
			if (index < 0) {
				index = 0;
				helpList.setSelectedIndex(index);
				return;
			}
			index--;
			if (index < 0) {
				return;
			} else {
				if (index < model.map.size()) {
					helpList.setSelectedIndex(index);
				}
			}
		}
		if (e.getSource() == next) {
			int index = helpList.getSelectedIndex();
			if (index < 0) {
				index = 0;
				helpList.setSelectedIndex(index);
				return;
			}
			index++;
			if (index > model.map.size()) {
				return;
			} else {
				if (index >= 0) {
					helpList.setSelectedIndex(index);
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		HashMap<String, URL> map = new HashMap<String, URL>();
		String dir = "F:\\Users\\subhra\\Desktop\\xdman\\help";
		map.put("Browser Integration",
				new File(dir, "Browser_Integration.html").toURI().toURL());
		map.put("Capturing Videos", new File(dir, "Video_download.html")
				.toURI().toURL());
		map.put("Refresh Broken Downloads", new File(dir, "Refresh_Link.html")
				.toURI().toURL());
		HelpDialog d = new HelpDialog();
		d.addPages(map);
		d.setVisible(true);
	}

}
