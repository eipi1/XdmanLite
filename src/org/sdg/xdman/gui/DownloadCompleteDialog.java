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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.sdg.xdman.util.XDMUtil;

public class DownloadCompleteDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = -6952846084893748315L;
	JTextField file, url;
	JButton open, close, open_folder;
	String file_path, folder_path;

	public DownloadCompleteDialog() {
		super();
		setSize(300, 300);
		setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.insets = new Insets(5, 5, 5, 5);
		gc.gridwidth = 3;
		gc.fill = GridBagConstraints.HORIZONTAL;
		add(new JLabel("Download Complete"), gc);
		gc.gridx = 0;
		gc.gridy = 1;
		add(new JLabel("File"), gc);
		gc.gridx = 0;
		gc.gridy = 2;
		file = new JTextField();
		add(file, gc);
		file.setEditable(false);
		gc.gridx = 0;
		gc.gridy = 3;
		add(new JLabel("URL"), gc);
		url = new JTextField();
		url.setEditable(false);
		gc.gridy = 4;
		add(url, gc);
		open = new JButton("Open");
		open.addActionListener(this);
		open_folder = new JButton("Open Folder");
		open_folder.addActionListener(this);
		close = new JButton("Close");
		close.addActionListener(this);
		gc.gridx = 0;
		gc.gridy = 5;
		gc.gridwidth = 1;
		add(open, gc);
		gc.gridx = 1;
		add(open_folder, gc);
		gc.gridx = 2;
		add(close, gc);
		try {
			setIconImage(Main.icon.getImage());
		} catch (Exception e) {
		}
	}

	void setData(String file, String url) {
		this.file.setText(file);
		this.url.setText(url);
		setTitle(file);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String str = e.getActionCommand();
		if (str.equals("Open")) {
			XDMUtil.open(new File(folder_path, file_path));
			setVisible(false);
		}
		if (str.equals("Open Folder")) {
			XDMUtil.open(new File(folder_path));
			setVisible(false);
		}
		if (str.equals("Close")) {
			setVisible(false);
		}
	}
}
