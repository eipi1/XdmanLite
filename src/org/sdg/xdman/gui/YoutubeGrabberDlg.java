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
import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.sdg.xdman.core.common.XDMConfig;
import org.sdg.xdman.plugin.youtube.ParserProgressListner;
import org.sdg.xdman.util.XDMUtil;

public class YoutubeGrabberDlg extends JFrame implements ActionListener,
		YoutubeListener, ParserProgressListner {
	private static final long serialVersionUID = -1072376334080340930L;
	CardLayout card;
	JPanel p1;
	Box p2;
	JTextField ytaddr;
	JButton get_video, cancel, stop;
	JLabel anim;
	JPanel p;
	JTextArea info;
	String info1 = "Enter the link of Youtube video you want to download\nExample: http://www.youtube.com/watch?v=F7k_U1ZXybo",
			info2 = "XDM is collecting information to get a direct download link\nThis may take a minute or two";
	XDMConfig config;
	YTDThread ytd;
	YoutubeMediaListener listener;

	public YoutubeGrabberDlg(YoutubeMediaListener l) {
		super("XDM Youtube Grabber 2013");
		this.listener = l;
		setSize(400, 300);
		createP1();
		createP2();
		info = new JTextArea();
		info.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		info.setEditable(false);
		info.setOpaque(false);
		info.setWrapStyleWord(true);
		info.setLineWrap(true);
		add(info, BorderLayout.NORTH);
		card = new CardLayout();
		p = new JPanel(card);
		p.add(p1, "1");
		p.add(p2, "2");
		add(p);
		card.show(p, "1");
		try {
			setIconImage(Main.icon.getImage());
		} catch (Exception e) {
		}
	}

	void showDialog(JFrame f, XDMConfig config) {
		setLocationRelativeTo(f);
		info.setText(info1);
		this.config = config;
		card.show(p, "1");
		ytaddr.setText("");
		setVisible(true);
	}

	void createP1() {
		p1 = new JPanel(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.insets = new Insets(10, 10, 10, 10);
		ytaddr = new JTextField();
		get_video = new JButton("Get Video!");
		get_video.addActionListener(this);
		cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.weightx = 1.0;
		gc.gridwidth = 2;
		p1.add(ytaddr, gc);
		gc.gridwidth = 1;
		gc.gridy = 1;
		p1.add(get_video, gc);
		gc.gridx = 1;
		p1.add(cancel, gc);
	}

	void createP2() {
		p2 = Box.createHorizontalBox();
		p2.add(Box.createHorizontalStrut(10));
		anim = new JLabel("Downloading information...");
		p2.add(anim);
		p2.add(Box.createHorizontalGlue());
		stop = new JButton("Stop");
		stop.addActionListener(this);
		p2.add(stop);
		p2.add(Box.createHorizontalStrut(10));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == get_video) {
			String addr = ytaddr.getText();
			if (addr.length() < 1) {
				JOptionPane.showMessageDialog(this,
						"Address may not be left blank");
				return;
			}
			if (!XDMUtil.validateURL(addr)) {
				JOptionPane.showMessageDialog(this, "Not a valid URL");
				return;
			}
			info.setText(info2);
			card.show(p, "2");
			ytd = new YTDThread(addr, config, this);
			ytd.plistner = this;
			ytd.start();
		}
		if (e.getSource() == stop) {
			if (ytd != null) {
				ytd.stop();
				setVisible(false);
			}
		}
		if (e.getSource() == cancel) {
			setVisible(false);
		}
	}

	@Override
	public void parsingComplete(ArrayList<String> list) {
		System.out.println("List of urls======");
		Iterator<String> it = list.iterator();
		while (it.hasNext()) {
			System.out.println(it.next());
		}
		setVisible(false);
		if (listener != null) {
			listener.mediaCaptured(list);
		}
	}

	@Override
	public void parsingFailed() {
		if (isVisible()) {
			JOptionPane.showMessageDialog(this, "Operation failed");
			info.setText(info1);
			card.show(p, "1");
		}
	}

	@Override
	public void update(long downloaded) {
		anim.setText("Downloading information "
				+ XDMUtil.getFormattedLength(downloaded));
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// new YoutubeGrabberDlg().showDialog(null,new XDMConfig(n));
	}

}
