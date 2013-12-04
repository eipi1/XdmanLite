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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

public class AboutDlg extends JDialog implements ActionListener {

	private static final long serialVersionUID = -2763244157779366358L;
	URI home, me;

	public AboutDlg() {
		setIconImage(Main.icon.getImage());
		setTitle("About XDM");
		setLocationRelativeTo(null);
		JPanel top = new JPanel(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.insets = new Insets(5, 5, 5, 5);
		gc.gridheight = 3;
		JLabel icon = new JLabel(Main.icon);
		icon.setMinimumSize(new Dimension(Main.icon.getIconWidth(),
				Main.icon.getIconHeight()));
		icon.setPreferredSize(new Dimension(Main.icon.getIconWidth(),
				Main.icon.getIconHeight()));
		top.add(icon, gc);
		JLabel title = new JLabel("Xtreme Download Manager");
		title.setFont(new Font(Font.DIALOG, Font.BOLD, 20));
		gc.gridwidth = 4;
		gc.gridheight = 2;
		gc.gridx = 1;
		top.add(title, gc);
		gc.gridheight = 3;
		gc.gridy = 1;
		gc.gridx = 1;
		JLabel ver = new JLabel(Main.version);
		top.add(ver, gc);
		add(top, BorderLayout.NORTH);
		JPanel bottom = new JPanel(new GridBagLayout());
		gc = new GridBagConstraints();
		gc.insets = new Insets(5, 5, 5, 5);
		Icon me = Main.getIcon("me.png");
		gc.gridheight = 4;
		bottom.add(new JLabel(me), gc);
		JTextArea info = new JTextArea("Copyright (C) Subhra Das Gupta\n"
				+ "http://xdman.sourceforge.net\n\n"
				+ "This program is licenced under\n"
				+ "GNU General Public License");
		info.setEditable(false);
		info.setOpaque(false);
		gc.gridx = 1;
		bottom.add(info, gc);
		JPanel center = new JPanel(new BorderLayout());
		center.setBorder(new EmptyBorder(5, 5, 5, 5));

		bottom.setBorder(new TitledBorder(new EtchedBorder()));
		center.add(bottom);
		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		JButton close = new JButton("OK");
		close.addActionListener(this);
		box.add(close);
		box.setBorder(new EmptyBorder(10, 10, 10, 10));
		add(box, BorderLayout.SOUTH);
		add(center);
		pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String str = e.getActionCommand();
		if (str.equals("OK")) {
			setVisible(false);
		}
	}
}
