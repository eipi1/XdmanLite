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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

public class ShutdownDlg extends JDialog implements Runnable, ActionListener {

	private static final long serialVersionUID = 192973133550711683L;
	Thread t;
	XDMProgressBar prg;
	JButton stop;
	int sec;
	JLabel label;
	String cmd;
	boolean abort;

	public ShutdownDlg() {
		setTitle("Warning...");
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				stop();
			}
		});
		setLayout(new GridBagLayout());
		prg = new XDMProgressBar();
		prg.setPreferredSize(new Dimension(20, 20));
		stop = new JButton("Stop");
		stop.addActionListener(this);
		label = new JLabel("Initiating Shutdown Process...");
		setAlwaysOnTop(true);
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridwidth = 3;
		gc.insets = new Insets(5, 5, 5, 5);
		gc.weightx = 1.0;
		gc.fill = GridBagConstraints.HORIZONTAL;
		add(label, gc);
		gc.gridx = 0;
		gc.gridy = 1;
		add(prg, gc);
		gc.gridy = 2;
		gc.weightx = 0;
		gc.gridwidth = 1;
		gc.gridx = 0;
		add(stop, gc);
		setLocationRelativeTo(null);
		pack();
		try {
			setIconImage(Main.icon.getImage());
		} catch (Exception e) {
		}
	}

	void start(String cmd) {
		if (t != null)
			return;
		this.cmd = cmd;
		setVisible(true);
		prg.setValue(0);
		t = new Thread(this);
		t.start();
	}

	@Override
	public void run() {
		for (int i = 0; i < 60; i++) {
			if (abort)
				return;
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
			}
			if (abort)
				return;
			prg.setValue((i * 100 / 59));
			label.setText("Shutting down in T-" + (59 - i) + " Sec.");
		}
		System.out.println("EXECUTEING: " + cmd);
		try {
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {

		}
		stop();
	}

	public static void main(String[] args) {
		ShutdownDlg dlg = new ShutdownDlg();
		dlg.start("shutdown -s");
	}

	public void stop() {
		abort = true;
		t = null;
		setVisible(false);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		stop();
	}
}
