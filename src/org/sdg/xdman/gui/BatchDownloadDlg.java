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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.sdg.xdman.util.XDMUtil;

public class BatchDownloadDlg extends JFrame implements ActionListener {

	private static final long serialVersionUID = -4717157093775786226L;
	BatchTableModel model;
	JTable table;
	JTextField dir;
	JButton browse, ok, cancel, checkAll, uncheckAll;
	JCheckBox startQ;
	JFileChooser fc;
	BatchDownloadListener listener;

	public BatchDownloadDlg() {
		setSize(600, 300);
		setTitle("Batch download from clipboard");
		setIconImage(Main.icon.getImage());
		add(new JLabel("Seleceted Items will be added to XDM Download Queue"),
				BorderLayout.NORTH);
		dir = new JTextField();
		browse = new JButton("Browse...");
		browse.addActionListener(this);
		ok = new JButton("OK");
		ok.addActionListener(this);
		cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		checkAll = new JButton("Check All");
		checkAll.addActionListener(this);
		uncheckAll = new JButton("Uncheck All");
		uncheckAll.addActionListener(this);
		startQ = new JCheckBox("Start Queue Processing");
		startQ.addActionListener(this);
		model = new BatchTableModel();
		table = new JTable(model);
		table.setFillsViewportHeight(true);
		add(new JScrollPane(table));
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int index = table.getSelectedRow();
				if (index < 0)
					return;
				int c = table.getSelectedColumn();
				if (c != 0) {
					return;
				}
				BatchItem item = model.batchList.get(index);
				item.selected = !item.selected;
				model.fireTableCellUpdated(index, index);
			}
		});
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.insets = new Insets(5, 5, 5, 5);
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.gridwidth = 1;
		gc.gridy = 0;
		panel.add(new JLabel("Save in: "), gc);
		gc.gridx = 1;
		gc.weightx = 1;
		gc.gridwidth = 2;
		panel.add(dir, gc);
		gc.gridx = 3;
		gc.weightx = 0;
		gc.gridwidth = 2;
		panel.add(browse, gc);
		gc.gridwidth = 1;

		gc.gridx = 0;
		gc.gridy = 1;
		panel.add(checkAll, gc);
		gc.gridx = 1;
		panel.add(uncheckAll, gc);
		gc.gridx = 2;
		panel.add(startQ, gc);
		gc.gridx = 3;
		panel.add(ok, gc);
		gc.gridx = 4;
		panel.add(cancel, gc);
		add(panel, BorderLayout.SOUTH);
	}

	public void showDialog(List<BatchItem> list, String folder,
			BatchDownloadListener listener) {
		try {
			setTitle("Batch download");
			this.listener = listener;
			this.model.batchList.clear();
			dir.setText(folder);
			for (int i = 0; i < list.size(); i++) {
				this.model.batchList.add(list.get(i));
			}
			model.fireTableDataChanged();
			setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void showDialog(String folder, BatchDownloadListener listener) {
		setTitle("Batch download from clipboard");
		this.listener = listener;
		this.model.batchList.clear();
		dir.setText(folder);
		try {
			Object obj = Toolkit.getDefaultToolkit().getSystemClipboard()
					.getData(DataFlavor.stringFlavor);
			String txt = "";
			int count = 0;
			if (obj != null) {
				txt = obj.toString();
			} else {
				JOptionPane.showMessageDialog(this,
						"No web address found in clipboard");
				return;
			}
			if (txt.length() > 0) {
				String urls[] = txt.split("\n");
				for (int i = 0; i < urls.length; i++) {
					BatchItem item = new BatchItem();
					String url = urls[i];
					if (!XDMUtil.validateURL(url)) {
						continue;
					}
					count++;
					item.url = url;
					item.dir = dir.getText();
					item.fileName = XDMUtil.getFileName(url);
					System.out.println(urls[i]);
					model.batchList.add(item);
				}
			}
			if (count < 1) {
				JOptionPane.showMessageDialog(this,
						"No web address found in clipboard");
				return;
			}
			model.fireTableDataChanged();
			setVisible(true);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this,
					"No web address found in clipboard");
			return;
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == browse) {
			if (fc == null) {
				fc = new JFileChooser();
			}
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				dir.setText(fc.getSelectedFile().getAbsolutePath());
				for (int i = 0; i < model.batchList.size(); i++) {
					model.batchList.get(i).dir = dir.getText();
				}
				model.fireTableDataChanged();
			}
		}
		if (e.getSource() == checkAll) {
			for (int i = 0; i < model.batchList.size(); i++) {
				model.batchList.get(i).selected = true;
			}
			model.fireTableDataChanged();
		}
		if (e.getSource() == uncheckAll) {
			for (int i = 0; i < model.batchList.size(); i++) {
				model.batchList.get(i).selected = false;
			}
			model.fireTableDataChanged();
		}
		if (e.getSource() == cancel) {
			setVisible(false);
		}
		if (e.getSource() == ok) {
			listener.download(model.batchList, startQ.isSelected());
			setVisible(false);
		}
	}

	public static void main(String[] args) {
		new BatchDownloadDlg()
				.showDialog(System.getProperty("user.home"), null);
	}

}
