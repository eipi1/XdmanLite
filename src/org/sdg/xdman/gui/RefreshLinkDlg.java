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
import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.sdg.xdman.util.XDMUtil;

public class RefreshLinkDlg extends JDialog implements ActionListener {

	private static final long serialVersionUID = 1359910126781110728L;
	String info1 = "The Old Download Link was contained in the following web page: ";
	String info2 = "\nPlease open the web page in your browser"
			+ "\nFind the download link again and paste it in the 'New Download Link' field.";

	JTextField link;
	String oldLink = "";
	JTextArea txt;
	DownloadListItem item;
	JButton openPage;

	public RefreshLinkDlg(JFrame f) {
		super(f);
		setTitle("Refresh Link");
		setModal(true);
		init();
	}

	void init() {
		setSize(300, 300);
		txt = new JTextArea();
		txt.setEditable(false);
		txt.setWrapStyleWord(true);
		txt.setLineWrap(true);
		JScrollPane jsp = new JScrollPane(txt);
		add(jsp);

		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.insets = new Insets(5, 5, 5, 5);
		gc.gridwidth = 2;
		gc.fill = GridBagConstraints.HORIZONTAL;
		openPage = new JButton("Open Web Page");
		openPage.addActionListener(this);
		gc.gridy = 1;
		panel.add(openPage, gc);
		gc.gridwidth = 1;
		gc.weightx = 1;
		gc.gridy = 2;
		gc.weightx = 0.5;
		panel.add(new JLabel("New Download Link"), gc);
		gc.weightx = 1;
		link = new JTextField();
		gc.gridx = 1;
		panel.add(link, gc);
		gc.weightx = 0;
		JButton ok = new JButton("OK");
		ok.addActionListener(this);
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		box.add(ok);
		box.add(Box.createHorizontalStrut(10));
		box.add(cancel);
		gc.gridx = 0;
		gc.gridy = 3;
		gc.gridwidth = 2;
		panel.add(box, gc);
		add(panel, BorderLayout.SOUTH);
	}

	void showDlg(DownloadListItem item, String ref) {
		this.item = item;
		if (ref == null || ref.length() < 1) {
			oldLink = "Unknown";
			openPage.setEnabled(false);
		} else {
			oldLink = ref;
			openPage.setEnabled(true);
		}
		link.setText("");
		txt.setText(info1 + oldLink + info2);
		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		System.out.println(cmd);
		if (cmd.equals("OK")) {
			System.out.println(cmd);
			if (item != null) {
				String url = link.getText().trim();
				if (url.length() < 1) {
					JOptionPane.showMessageDialog(this, "Field is empty");
					return;
				}
				if (!XDMUtil.validateURL(url)) {
					JOptionPane.showMessageDialog(this, "Invalid URL");
					return;
				}
				item.url = url;
				Main.list.downloadStateChanged();
				setVisible(false);
			}
		}
		if (cmd.equals("Cancel")) {
			setVisible(false);
		}
		if (cmd.equals("Open Web Page")) {
			try {
				Desktop.getDesktop().browse(new URI(oldLink));
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		new RefreshLinkDlg(null).showDlg(null, null);
	}
}
