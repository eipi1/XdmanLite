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
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import org.sdg.xdman.core.common.XDMConfig;
import org.sdg.xdman.core.common.http.XDMHttpClient;
import org.sdg.xdman.proxy.RequestHandler;
import org.sdg.xdman.util.XDMUtil;

public class DownloadFileInfoDialog extends JDialog implements ActionListener,
		DocumentListener {

	private static final long serialVersionUID = 5445253177209103274L;
	JTextField url = new JTextField();
	JTextField file = new JTextField();
	JTextField dir = new JTextField();
	JTextField user = new JTextField();
	JPasswordField pass = new JPasswordField();
	ActionListener dl_action, dn_action;
	JFileChooser fc;
	XDMHttpClient client;
	HashMap<String, String> extra;
	Object interceptor;
	XDMConfig config;
	boolean cancelled = true;

	public DownloadFileInfoDialog(ActionListener dl, ActionListener dn,
			XDMConfig config) {
		this.dl_action = dl;
		this.dn_action = dn;
		this.config = config;
		init();
	}

	void setURL(String uri) {
		url.setText(uri);
	}

	String getURL() {
		return url.getText();
	}

	String getFile() {
		return file.getText();
	}

	String getDir() {
		return dir.getText();
	}

	String getUser() {
		return user.getText();
	}

	String getPass() {
		return new String(pass.getPassword());
	}

	void setDir(String f) {
		dir.setText(f);
	}

	DownloadList list;
	JButton dl, dn, cn, br;
	public String cookies;

	public DownloadFileInfoDialog() {
		init();
	}

	void showDlg() {
		if (url.getText().length() < 1) {
			try {
				Object obj = Toolkit.getDefaultToolkit().getSystemClipboard()
						.getData(DataFlavor.stringFlavor);
				String txt = "";
				if (obj != null) {
					txt = obj.toString();
				}
				if (txt.length() > 0) {
					int index = txt.indexOf('\n');
					if (index != -1) {
						txt = txt.substring(0, index);
					}
					url.setText(new URL(txt).toString());
				}
			} catch (Exception e) {
			}
		}
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(d.width / 2 - getWidth() / 2, d.height / 2 - getHeight()
				/ 2);
		setVisible(true);
	}

	void init() {
		// setSize(400, 250);
		setTitle("New Download");
		try {
			setIconImage(Main.icon.getImage());
		} catch (Exception e) {
		}
		// setIconImage(MainWindow.icon.getImage());
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.out.println("CALLED");
				synchronized (DownloadFileInfoDialog.this) {
					DownloadFileInfoDialog.this.notifyAll();
				}
				setVisible(false);
			}
		});
		// Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		// setLocation(d.width / 2 - 200, d.height / 2 - 250 / 2);

		url.getDocument().addDocumentListener(this);

		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.insets = new Insets(5, 5, 5, 5);
		gc.gridx = 0;
		gc.gridy = 0;
		panel.add(new JLabel("Address"), gc);
		gc.gridx = 1;
		gc.gridy = 0;
		gc.gridwidth = 3;
		gc.weightx = 1.0;
		panel.add(url, gc);
		gc.weightx = 0;
		gc.gridx = 0;
		gc.gridy = 1;
		gc.gridwidth = 1;
		panel.add(new JLabel("Save As"), gc);
		gc.weightx = 1.0;
		gc.gridx = 1;
		gc.gridy = 1;
		gc.gridwidth = 3;
		panel.add(file, gc);
		gc.weightx = 0;
		gc.gridx = 0;
		gc.gridy = 2;
		gc.gridwidth = 1;
		panel.add(new JLabel("Save In"), gc);
		gc.weightx = 1.0;
		gc.gridx = 1;
		gc.gridy = 2;
		gc.gridwidth = 2;
		panel.add(dir, gc);
		dir.setEditable(false);
		gc.weightx = 0.0;
		gc.gridx = 3;
		gc.gridy = 2;
		gc.gridwidth = 1;
		br = new JButton("Browse...");
		br.addActionListener(this);
		panel.add(br, gc);
		gc.weightx = 0;
		gc.gridx = 0;
		gc.gridy = 3;
		gc.gridwidth = 1;
		panel.add(new JLabel("UserName"), gc);
		gc.weightx = 0.5;
		gc.gridx = 1;
		gc.gridy = 3;
		gc.gridwidth = 1;
		panel.add(user, gc);
		gc.weightx = 0;
		gc.gridx = 2;
		gc.gridy = 3;
		gc.gridwidth = 1;
		panel.add(new JLabel("Password"), gc);
		gc.weightx = 0.5;
		gc.gridx = 3;
		gc.gridy = 3;
		gc.gridwidth = 1;
		panel.add(pass, gc);
		dl = new JButton("Download Later");
		dl.addActionListener(this);
		dn = new JButton("Download Now");
		dn.addActionListener(this);
		cn = new JButton("Cancel");
		cn.addActionListener(this);
		gc.weightx = 0.0;
		gc.gridx = 1;
		gc.gridy = 4;
		gc.gridwidth = 1;
		panel.add(dl, gc);
		gc.gridx = 2;
		gc.gridy = 4;
		panel.add(dn, gc);
		gc.gridx = 3;
		gc.gridy = 4;
		panel.add(cn, gc);
		add(panel);
		pack();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Cancel")) {
			synchronized (this) {
				if (interceptor != null) {
					((RequestHandler) interceptor).intercept = false;
					this.notifyAll();
				}
			}
			setVisible(false);
		} else if (e.getActionCommand().equals("Download Later")) {
			if (dl_action != null) {
				if (getURL().length() < 1) {
					JOptionPane.showMessageDialog(this, "URL is Empty");
					return;
				}
				if (!XDMUtil.validateURL(getURL())) {
					String cu = XDMUtil.createURL(getURL());
					if (cu != null) {
						setURL(cu);
					} else {
						JOptionPane.showMessageDialog(this, "Invalid URL");
					}
					return;
				}
				cancelled = false;
				dl_action.actionPerformed(new ActionEvent(this, 0, e
						.getActionCommand()));
				setVisible(false);
			}
		} else if (e.getActionCommand().equals("Download Now")) {
			if (dn_action != null) {
				if (!XDMUtil.validateURL(getURL())) {
					if (getURL().length() < 1) {
						JOptionPane.showMessageDialog(this, "URL is Empty");
						return;
					}
					String cu = XDMUtil.createURL(getURL());
					if (cu != null) {
						setURL(cu);
					} else {
						JOptionPane.showMessageDialog(this, "Invalid URL");
					}
					return;
				}
				cancelled = false;
				dl_action.actionPerformed(new ActionEvent(this, 0, e
						.getActionCommand()));
				setVisible(false);
			}
		} else if (e.getActionCommand().equals("Browse...")) {
			if (fc == null) {
				fc = new JFileChooser();
			}
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				dir.setText(fc.getSelectedFile().getAbsolutePath());
				config.destdir = dir.getText();
			}
		}
	}

	public static void main(String[] args) {
		new DownloadFileInfoDialog().setVisible(true);
	}

	@Override
	protected void finalize() throws Throwable {
		System.out.println("###########################FINALIZING...");
		super.finalize();
	}

	String getFileName(String url) {
		String file = null;
		try {
			file = XDMUtil.getFileName(url);
		} catch (Exception e) {
		}
		if (file == null || file.length() < 1)
			file = "FILE";
		return file;
	}

	void update(DocumentEvent e) {
		try {
			Document doc = e.getDocument();
			int len = doc.getLength();
			String text = doc.getText(0, len);
			file.setText(getFileName(text));
		} catch (Exception err) {
			err.printStackTrace();
		}
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
}
