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
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;

import org.sdg.xdman.core.common.ConnectionManager;
import org.sdg.xdman.core.common.DownloadInfo;
import org.sdg.xdman.core.common.IXDMConstants;
import org.sdg.xdman.util.XDMUtil;

public class DownloadWindow implements Observer, ActionListener {
	JFrame window;
	JLabel url, status, filesize, downloaded, rate, time, resume;
	SegmentPanel p = new SegmentPanel();
	XDMProgressBar prg = new XDMProgressBar();
	JButton bg, view, cancel;
	ConnectionTableModel model = new ConnectionTableModel();
	JTable table = new JTable(model);
	ConnectionManager mgr;
	File file;
	boolean showed = false;

	public DownloadWindow(ConnectionManager mgr) {
		init();
		this.mgr = mgr;
	}

	public void showWindow() {
		window.setVisible(true);
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		DownloadInfo info = (DownloadInfo) arg1;
		window.setTitle(info.prg + " % " + info.file);
		url.setText(info.url);
		if (info.state == IXDMConstants.FAILED)
			status.setText("Error");
		else
			status.setText(info.status);
		filesize.setText(info.length);
		downloaded.setText(info.downloaded + " (" + info.progress + "%)");
		rate.setText(info.speed);
		time.setText(info.eta);
		resume.setText(info.resume);
		p.setValues(info.startoff, info.len, info.dwn, info.rlen);
		prg.setValue(info.prg);
		model.update(info.dwnld, info.stat);
		file = info.path;
		// if (info.resume.equalsIgnoreCase("yes")) {
		// pause.setEnabled(true);
		// } else {
		// pause.setEnabled(false);
		// }
		if (info.state == IXDMConstants.FAILED) {
			// pause.setEnabled(false);
			ConnectionManager c = (ConnectionManager) arg0;
			if (!c.stop) {
				if (!(c.state == ConnectionManager.COMPLETE)) {
					if (!showed) {
						JOptionPane.showMessageDialog(window, info.msg);
						window.setVisible(false);
						window.dispose();
						showed = true;
					}
				}
			}
		}
		if (info.state == IXDMConstants.COMPLETE) {
			System.out.println("%%%%%%%%%%%%%%%%%%DOWNLOAD COMPLETE");
			bg.setEnabled(false);
			// pause.setEnabled(false);
			window.setVisible(false);
			window.dispose();
		}
		if (info.state == IXDMConstants.STOPPED) {
			System.out.println("%%%%%%%%%%%%%%%%%%DOWNLOAD STOPPED");
			bg.setEnabled(false);
			// pause.setEnabled(false);
			window.setVisible(false);
			window.dispose();
		}
	}

	void init() {
		window = new JFrame();
		ImageIcon img = Main.icon;
		if (img != null)
			window.setIconImage(img.getImage());
		window.setSize(450, 400);
		Toolkit t = Toolkit.getDefaultToolkit();
		Dimension d = t.getScreenSize();
		window.setLocation(d.width / 2 - window.getWidth() / 2, d.height / 2
				- window.getHeight() / 2);
		Box vbox = Box.createVerticalBox();
		vbox.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		url = new JLabel("", JLabel.LEFT);
		status = new JLabel("", JLabel.LEFT);
		filesize = new JLabel("", JLabel.LEFT);
		downloaded = new JLabel("", JLabel.LEFT);
		rate = new JLabel("", JLabel.LEFT);
		time = new JLabel("", JLabel.LEFT);
		resume = new JLabel("", JLabel.LEFT);
		JPanel infoPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.insets = new Insets(0, 5, 0, 5);
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.gridwidth = 2;
		gc.gridx = 0;
		gc.gridy = 0;
		gc.weightx = 1.0;
		infoPanel.add(url, gc);
		gc.weightx = 0;
		gc.gridwidth = 1;
		gc.gridy = 1;
		infoPanel.add(new JLabel("Status", JLabel.LEFT), gc);
		gc.gridx = 1;
		gc.gridy = 1;
		infoPanel.add(status, gc);
		gc.gridy = 2;
		gc.gridx = 0;
		infoPanel.add(new JLabel("File size", JLabel.LEFT), gc);
		gc.gridx = 1;
		gc.gridy = 2;
		infoPanel.add(filesize, gc);
		gc.gridy = 3;
		gc.gridx = 0;
		infoPanel.add(new JLabel("Downloaded", JLabel.LEFT), gc);
		gc.gridx = 1;
		gc.gridy = 3;
		infoPanel.add(downloaded, gc);
		gc.gridx = 0;
		gc.gridy = 4;
		infoPanel.add(new JLabel("Transfer rate", JLabel.LEFT), gc);
		gc.gridx = 1;
		gc.gridy = 4;
		infoPanel.add(rate, gc);
		gc.gridx = 0;
		gc.gridy = 5;
		infoPanel.add(new JLabel("Time left", JLabel.LEFT), gc);
		gc.gridx = 1;
		gc.gridy = 5;
		infoPanel.add(time, gc);
		gc.gridx = 0;
		gc.gridy = 6;
		infoPanel.add(new JLabel("Resume support", JLabel.LEFT), gc);
		gc.gridx = 1;
		gc.gridy = 6;
		infoPanel.add(resume, gc);
		vbox.add(infoPanel);

		vbox.add(Box.createVerticalStrut(10));

		Box prgBox = Box.createHorizontalBox();
		prgBox.add(Box.createRigidArea(new Dimension(0, 15)));
		prgBox.add(prg);
		vbox.add(prgBox);

		vbox.add(Box.createVerticalStrut(10));

		bg = new JButton("Background");
		bg.addActionListener(this);
		view = new JButton("Preview");
		view.addActionListener(this);
		cancel = new JButton("Pause");
		cancel.addActionListener(this);
		Box hbox8 = Box.createHorizontalBox();
		hbox8.add(bg);
		hbox8.add(Box.createHorizontalGlue());
		hbox8.add(view);
		hbox8.add(Box.createHorizontalStrut(10));
		hbox8.add(cancel);
		vbox.add(hbox8);

		vbox.add(Box.createVerticalStrut(10));
		Box hbox9 = Box.createHorizontalBox();
		hbox9.add(Box.createRigidArea(new Dimension(0, 15)));
		hbox9.add(p);
		vbox.add(hbox9);

		vbox.add(Box.createVerticalStrut(10));
		Box hbox10 = Box.createHorizontalBox();
		JScrollPane jsp = new JScrollPane(table);
		table.setCellSelectionEnabled(false);
		table.setRowSelectionAllowed(false);
		table.setColumnSelectionAllowed(false);
		table.setFocusable(false);
		table.setFillsViewportHeight(true);
		table.setShowGrid(false);
		hbox10.add(jsp);

		TableColumnModel cm = table.getColumnModel();
		cm.getColumn(0).setPreferredWidth(10);
		cm.getColumn(1).setPreferredWidth(50);
		cm.getColumn(2).setPreferredWidth(200);
		vbox.add(hbox10);
		window.add(vbox);
		window.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (mgr != null)
					mgr.stop();
				window.setVisible(false);
				window.dispose();
			}
		});

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Pause")) {
			if (mgr != null)
				mgr.stop();
			window.setVisible(false);
			window.dispose();
		}
		if (e.getActionCommand().equals("Preview")) {
			if (file != null) {
				XDMUtil.open(file);
			}
		}
		if (e.getActionCommand().equals("Background")) {
			window.setVisible(false);
		}
	}

	public static void main(String[] args) {
		new DownloadWindow(null).showWindow();
	}

}
