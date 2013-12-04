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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

public class BrowserIntDlg extends JDialog {

	private static final long serialVersionUID = -6629147016436649030L;
	JTabbedPane pane;
	JPanel p1, p2;
	JTextArea text1, text2, text3, text4;
	JList ff;
	JButton helpff, auto, man;
	JCheckBox autoStart;

	public void setListeners(ActionListener info1, ActionListener info2,
			ActionListener autoConfig, ActionListener manConfig,
			ActionListener startup, String url) {
		helpff.addActionListener(info1);
		man.addActionListener(info2);
		auto.addActionListener(autoConfig);
		autoStart.addActionListener(startup);
		DefaultListModel model = new DefaultListModel();
		model.add(0, url);
		ff.setModel(model);
		ff.setCellRenderer(new FFListRenderer());
		ff.setDragEnabled(true);
	}

	public BrowserIntDlg() {
		setTitle("Browser Integration");
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		int w = 400, h = 500;
		if (d.width < w) {
			w = d.width;
		}
		if (d.height < h) {
			h = d.height;
		}
		setSize(w, h);
		createP1();
		createP2();
		pane = new JTabbedPane();
		pane.addTab("Monitor Firefox", p1);
		pane.addTab("Monitor All Browsers", p2);
		add(pane);
		// setDefaultCloseOperation(EXIT_ON_CLOSE);
		// setLocationRelativeTo(null);
	}

	void createP2() {
		p2 = new JPanel(new BorderLayout());
		p2.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		text3 = new JTextArea();
		text3.setOpaque(false);
		text3.setWrapStyleWord(true);
		text3.setEditable(false);
		text3.setLineWrap(true);
		text3
				.setText("XDM can capture downloads from any browser (and videos being played in the browser from Youtube, Metacafe etc.) using Advanced Browser Integration.\nTo enable this Network must be configured.\nIf you are using Windows then click on the 'Auto configure' button to Enable/Disable it. If you are using Linux, Mac etc or auto configure is not working then click on the 'Manual Configuration' button and follow the steps.");
		JScrollPane jsp1 = new JScrollPane(text3);
		jsp1.setOpaque(false);
		jsp1.setBorder(null);
		text3.setBorder(null);
		p2.add(jsp1, BorderLayout.NORTH);

		auto = new JButton("Auto Configure");
		man = new JButton("Manual Configuration");

		JPanel bp = new JPanel(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.insets = new Insets(5, 5, 5, 5);
		bp.add(auto, gc);
		gc.gridy = 1;
		bp.add(man, gc);
		gc.gridy = 2;

		p2.add(bp);

		text4 = new JTextArea();
		text4.setOpaque(false);
		text4.setWrapStyleWord(true);
		text4.setEditable(false);
		text4.setLineWrap(true);
		text4
				.setText("Manual Configuration only: To capture downloads properly XDM must run on startup");
		JScrollPane jsp2 = new JScrollPane(text4);
		jsp2.setOpaque(false);
		jsp2.setBorder(null);
		text4.setBorder(null);

		Box box = Box.createHorizontalBox();// new JPanel(new BorderLayout());
		box.add(jsp2);

		autoStart = new JCheckBox("Run XDM on startup", false);

		box.add(autoStart, BorderLayout.SOUTH);

		p2.add(box, BorderLayout.SOUTH);

	}

	void createP1() {
		text1 = new JTextArea();
		text1.setOpaque(false);
		text1.setWrapStyleWord(true);
		text1.setEditable(false);
		text1.setLineWrap(true);
		text1
				.setText("XDM can capture videos (videos being played in Firefox from Youtube, Metacafe etc) and take over downloads from Firefox using Firefox Integration.\n"
						+ "\nIf you use Google Chrome, MSIE, Oprera, Safari or any other browser please click on the 'Monitor All Browsers' tab");
		p1 = new JPanel(new BorderLayout());
		JScrollPane jsp1 = new JScrollPane(text1);
		jsp1.setOpaque(false);
		jsp1.setBorder(null);
		text1.setBorder(null);
		p1.add(jsp1, BorderLayout.NORTH);

		Box box = Box.createHorizontalBox();

		text2 = new JTextArea();
		Cursor c = text2.getCursor();
		text2.setOpaque(false);
		text2.setWrapStyleWord(true);
		text2.setEditable(false);
		text2.setLineWrap(true);
		text2
				.setText("Drag the above icon or paste http://localhost:9614/xdmff.xpi\nin Firefox's window.\n\nAfter this step is done play a video in Firefox, Right click and select 'Download FLV with XDM'");
		text2.setCursor(c);
		JScrollPane jsp2 = new JScrollPane(text2);
		jsp2.setOpaque(false);
		jsp2.setBorder(null);
		text2.setBorder(null);
		box.add(jsp2);
		helpff = new JButton("More Information");
		box.add(helpff);
		p1.add(box, BorderLayout.SOUTH);
		ff = new JList();
		p1.add(new JScrollPane(ff));
		p1.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	}

	public static void main(String[] args) {
		new BrowserIntDlg().setVisible(true);
	}

}
