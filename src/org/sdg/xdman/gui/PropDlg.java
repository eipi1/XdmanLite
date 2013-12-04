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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.sdg.xdman.core.common.IXDMConstants;
import org.sdg.xdman.util.XDMUtil;

public class PropDlg extends JDialog implements ActionListener {
	private static final long serialVersionUID = 7041991846285917185L;
	JTextField file, url, path, ref;
	JLabel type, size, date;
	JButton open, open_folder, save_in, close;
	JFileChooser fc;
	DownloadListItem item;

	public PropDlg(JFrame f) {
		super(f);
		setTitle("Properties");
		Box b = Box.createVerticalBox();
		url = new JTextField();
		url.setEditable(false);
		path = new JTextField();
		path.setEditable(false);
		ref = new JTextField();
		GridBagConstraints gc = new GridBagConstraints();
		Insets inset = new Insets(10, 10, 10, 10);
		JPanel m1 = new JPanel(new GridBagLayout());
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.gridx = 0;
		gc.gridy = 0;
		gc.insets = inset;
		m1.add(new JLabel("File"), gc);
		gc.gridx = 1;
		gc.gridy = 0;
		gc.gridwidth = 2;
		file = new JTextField();
		file.setEditable(false);
		m1.add(file, gc);
		gc.gridx = 0;
		gc.gridy = 1;
		gc.gridwidth = 1;
		m1.add(new JLabel("URL"), gc);
		gc.gridx = 1;
		gc.weightx = 0.5;
		gc.gridwidth = 2;
		m1.add(url, gc);
		b.add(m1);
		// JPanel m2 = new JPanel(new GridBagLayout());
		gc = new GridBagConstraints();
		gc.insets = inset;
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.gridx = 0;
		gc.gridy = 2;
		gc.weightx = 0;
		m1.add(new JLabel("PATH"), gc);
		gc.gridx = 1;
		gc.weightx = 0.5;
		gc.gridwidth = 2;
		m1.add(path, gc);
		// JPanel m3 = new JPanel(new GridBagLayout());
		gc = new GridBagConstraints();
		gc.insets = inset;
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.gridx = 0;
		gc.gridy = 3;
		m1.add(new JLabel("Type of file"), gc);
		gc.gridx = 1;
		gc.gridy = 3;
		gc.weightx = 1.0;
		type = new JLabel("<TYPE>");
		gc.gridwidth = 2;
		m1.add(type, gc);
		gc = new GridBagConstraints();
		gc.insets = inset;
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.gridx = 0;
		gc.gridy = 4;
		m1.add(new JLabel("Size"), gc);
		gc.gridx = 1;
		gc.weightx = 1.0;
		gc.gridwidth = 2;
		size = new JLabel("<SIZE>");
		m1.add(size, gc);
		date = new JLabel("<DATE>");
		gc = new GridBagConstraints();
		gc.insets = inset;
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.gridx = 0;
		gc.gridy = 5;
		m1.add(new JLabel("Date"), gc);
		gc.gridwidth = 2;
		gc.gridx = 1;
		gc.weightx = 1.0;
		m1.add(date, gc);
		gc = new GridBagConstraints();
		gc.insets = inset;
		gc.gridx = 0;
		gc.gridy = 6;
		save_in = new JButton("Save As");
		save_in.addActionListener(this);
		gc.gridx = 0;
		m1.add(save_in, gc);
		open = new JButton("Open");
		open.addActionListener(this);
		gc.gridx = 1;
		m1.add(open, gc);
		gc.gridx = 2;
		open_folder = new JButton("Open Folder");
		open_folder.addActionListener(this);
		m1.add(open_folder, gc);
		JLabel glue = new JLabel();
		gc.gridx = 0;
		gc.gridy = 7;
		gc.fill = GridBagConstraints.VERTICAL;
		gc.insets = inset;
		gc.weighty = 1.0;
		m1.add(glue, gc);
		close = new JButton("Close");
		close.addActionListener(this);
		Box l = Box.createHorizontalBox();
		l.add(Box.createHorizontalGlue());
		l.add(close);
		l.add(Box.createRigidArea(new Dimension(10, 40)));
		add(l, BorderLayout.SOUTH);
		b.add(Box.createVerticalGlue());
		add(b);
		try {
			setIconImage(Main.icon.getImage());
		} catch (Exception e) {
		}
	}

	void setDownloadProperty(DownloadListItem item) {
		file.setText(item.filename);
		url.setText(item.url);
		path.setText(item.saveto);
		size.setText(item.size);
		type.setText(item.type);
		date.setText(item.dateadded);
		this.item = item;
		if (item.state == IXDMConstants.COMPLETE) {
			save_in.setEnabled(false);
		} else {
			save_in.setEnabled(true);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		String str = e.getActionCommand();
		if (str.equals("Save As")) {
			if (item == null)
				return;
			if (item.state == IXDMConstants.COMPLETE) {
				JOptionPane.showMessageDialog(this,
						"Can't change completed downloads");
				return;
			}
			if (fc == null) {
				fc = new JFileChooser();
			}
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fc.setSelectedFile(new File(item.saveto, item.filename));
			if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				if (item.state == IXDMConstants.COMPLETE
						|| item.state == IXDMConstants.ASSEMBLING) {
					JOptionPane
							.showMessageDialog(this,
									"Can't change download property while it is almost complete");
					return;
				}
				item.saveto = fc.getSelectedFile().getParent();
				item.filename = fc.getSelectedFile().getName();
				if (item.mgr != null) {
					item.mgr.setDestdir(item.saveto);
					item.mgr.setFileName(item.filename);
				}
				Main.list.downloadStateChanged();
				setDownloadProperty(item);
			}
		}
		if (str.equals("Open")) {
			if (item == null)
				return;
			XDMUtil.open(new File(item.saveto, item.filename));
		}
		if (str.equals("Open Folder")) {
			if (item == null)
				return;
			XDMUtil.open(new File(item.saveto));
		}
		if (str.equals("Close")) {
			setVisible(false);
		}
	}
}
