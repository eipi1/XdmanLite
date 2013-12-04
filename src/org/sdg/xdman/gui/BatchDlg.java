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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

public class BatchDlg extends JFrame implements ActionListener,
		DocumentListener, ChangeListener {

	private static final long serialVersionUID = 7844880366020855193L;

	JTextField url, user, pass, first, second, last;
	ArrayList<String> urls;
	JRadioButton num, letter;
	JSpinner low, high, range;
	JButton ok, cancel;
	SpinnerModel lowModelN, highModelN, lowModelA, highModelA;
	BatchDownloadListener listener;

	public BatchDlg() {
		String label_txt = "It's possible to add a group of sequential file names like img001.jpg, img002.jpg, etc., img100.jpg to XDM download queue. Use the asterisk wildcard for the file name pattern. \nFor example:   http://xdman.sourceforge.net/*.jpg";
		setSize(500, 400);
		setIconImage(Main.icon.getImage());
		setTitle("Batch download");
		JTextArea label = new JTextArea(label_txt);
		label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		label.setWrapStyleWord(true);
		label.setLineWrap(true);
		label.setEditable(false);
		label.setOpaque(false);
		add(label, BorderLayout.NORTH);
		url = new JTextField();
		url.getDocument().addDocumentListener(this);
		user = new JTextField(8);
		pass = new JTextField(8);
		first = new JTextField();
		first.setEditable(false);
		second = new JTextField();
		second.setEditable(false);
		last = new JTextField();
		last.setEditable(false);
		num = new JRadioButton("Numbers", true);
		num.addActionListener(this);
		letter = new JRadioButton("Letters");
		letter.addActionListener(this);
		lowModelN = new SpinnerNumberModel(0, 0, 9999, 1);
		low = new JSpinner(lowModelN);
		low.addChangeListener(this);
		highModelN = new SpinnerNumberModel(50, 0, 9999, 1);
		high = new JSpinner(highModelN);
		high.addChangeListener(this);
		String alphas[] = getAlpha();
		lowModelA = new SpinnerListModel(Arrays.asList(alphas));
		highModelA = new SpinnerListModel(Arrays.asList(alphas));
		range = new JSpinner(new SpinnerNumberModel(2, 0, 10, 1));
		range.addChangeListener(this);
		ok = new JButton("OK");
		ok.addActionListener(this);
		cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		ButtonGroup bg = new ButtonGroup();
		bg.add(letter);
		bg.add(num);
		JPanel p = new JPanel(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.insets = new Insets(5, 5, 5, 5);
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.gridwidth = 1;
		p.add(new JLabel("Addrss"), gc);
		gc.gridwidth = 5;
		gc.weightx = 1;
		gc.gridx = 1;
		p.add(url, gc);

		gc.gridy = 1;
		gc.gridx = 0;
		gc.gridwidth = 4;
		p.add(new JLabel("Replace asterik to:", JLabel.RIGHT), gc);
		gc.gridx = 4;
		gc.gridwidth = 1;
		p.add(num, gc);
		gc.gridx = 5;
		p.add(letter, gc);
		gc.gridy = 2;
		gc.gridx = 0;
		gc.gridwidth = 6;

		gc.gridx = 0;
		gc.gridwidth = 1;
		p.add(new JLabel("From: ", JLabel.RIGHT), gc);
		gc.gridx = 1;
		p.add(low, gc);
		gc.gridx = 2;
		p.add(new JLabel("To: ", JLabel.RIGHT), gc);
		gc.gridx = 3;
		p.add(high, gc);
		gc.gridx = 4;
		p.add(new JLabel("Wildcard size: ", JLabel.RIGHT), gc);
		gc.gridx = 5;
		p.add(range, gc);
		gc.gridy = 3;
		gc.gridwidth = 1;
		gc.weightx = 1;
		gc.gridx = 0;
		p.add(new JLabel("User", JLabel.RIGHT), gc);
		gc.gridx = 1;
		p.add(user, gc);
		gc.gridx = 2;
		p.add(new JLabel("Pass", JLabel.RIGHT), gc);
		gc.gridx = 3;
		p.add(pass, gc);
		gc.weightx = 0;
		gc.gridx = 0;
		gc.gridwidth = 1;
		gc.gridy = 4;
		p.add(new JLabel("First URL", JLabel.CENTER), gc);
		gc.gridx = 1;
		gc.gridwidth = 5;
		p.add(first, gc);

		gc.gridwidth = 1;
		gc.gridx = 0;
		gc.gridy = 5;
		p.add(new JLabel("Second URL", JLabel.CENTER), gc);
		gc.gridx = 1;
		gc.gridwidth = 5;
		p.add(second, gc);

		gc.gridwidth = 1;
		gc.gridx = 0;
		gc.gridy = 6;
		p.add(new JLabel("Last URL", JLabel.CENTER), gc);
		gc.gridx = 1;
		gc.gridwidth = 5;
		p.add(last, gc);

		gc.gridwidth = 1;
		gc.gridx = 4;
		gc.gridy = 7;
		p.add(ok, gc);
		gc.gridx = 5;
		p.add(cancel, gc);
		add(p);
	}

	static ArrayList<String> generateBatchURL(String base, boolean isNum,
			int width, int low, int high) {
		ArrayList<String> list = new ArrayList<String>();
		if (base == null || base.length() < 1) {
			return list;
		}
		int index = base.indexOf('*');
		if (index < 0) {
			return list;
		}
		if (!isNum) {
			width = 1;
		}
		int l, h;
		if (low > high) {
			l = high;
			h = low;
		} else {
			l = low;
			h = high;
		}
		int lw;
		if (isNum) {
			lw = (low + "").length();
		} else {
			lw = 1;
		}
		if (lw > width) {
			width = lw;
		}
		for (int i = l; i <= h; i++) {
			StringBuffer buf = new StringBuffer();
			if (isNum) {
				buf.append(i + "");
			} else {
				buf.append((char) i);
			}
			if (buf.length() < width) {
				for (int j = 0; j < width - buf.length(); j++) {
					buf.insert(0, '0');
				}
			}
			if (buf.length() > width) {
				break;
			}
			String string = base.replace("*", buf.toString());
			try {
				new URL(string);
				list.add(string);
			} catch (Exception e) {
			}
			System.out.println(string);
		}
		return list;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == letter) {
			range.setEnabled(false);
			low.setModel(lowModelA);
			high.setModel(highModelA);
			high.setValue("Z");
			update(url.getText());
		}
		if (e.getSource() == num) {
			range.setEnabled(true);
			low.setModel(lowModelN);
			high.setModel(highModelN);
			update(url.getText());
		}
		if (e.getSource() == ok) {
			if (urls == null || urls.size() < 1) {
				JOptionPane.showMessageDialog(this, "Please enter valid url");
				return;
			}
			setVisible(false);
			if (listener != null) {
				listener
						.initBatchDownload(urls, user.getText(), pass.getText());
			}
		}
		if (e.getSource() == cancel) {
			setVisible(false);
		}
	}

	void update(DocumentEvent e) {
		try {
			System.out.println("Called");
			Document doc = e.getDocument();
			int len = doc.getLength();
			String text = doc.getText(0, len);
			update(text);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	void update(String text) {
		try {
			int w, l, h;
			if (num.isSelected()) {
				w = Integer.parseInt(range.getValue().toString());
				l = Integer.parseInt(low.getValue().toString());
				h = Integer.parseInt(high.getValue().toString());
			} else {
				w = range.getValue().toString().charAt(0);
				l = low.getValue().toString().charAt(0);
				h = high.getValue().toString().charAt(0);
			}
			String addr = text;
			if (addr.startsWith("http://") || addr.startsWith("https://")
					|| addr.startsWith("ftp://")) {
				urls = generateBatchURL(addr, num.isSelected(), w, l, h);
				first.setText("");
				second.setText("");
				last.setText("");
				if (urls.size() == 1) {
					first.setText(urls.get(0));
				} else if (urls.size() >= 2) {
					first.setText(urls.get(0));
					second.setText(urls.get(1));
				}
				int lindex = urls.size() - 1;
				if (lindex >= 0) {
					last.setText(urls.get(lindex));
				}
			}
			// file.setText(getFileName(text));
		} catch (Exception err) {
			err.printStackTrace();
		}
	}

	String[] getAlpha() {
		String arr[] = new String[52];
		int count = 0;
		for (int i = 'A'; i <= 'Z'; i++) {
			arr[count++] = ((char) i) + "";
		}
		for (int i = 'a'; i <= 'z'; i++) {
			arr[count++] = ((char) i) + "";
		}
		return arr;
	}

	public static void main(String[] args) {
		generateBatchURL("http://localhost/pic*.jpg", false, 1, 'a', 'x');
		new BatchDlg().setVisible(true);
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		update(e);
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		update(e);
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		update(e);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		update(url.getText());
	}

	public void showDialog(BatchDownloadListener listener) {
		this.listener = listener;
		setVisible(true);
	}
}
