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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.sdg.xdman.core.common.Authenticator;
import org.sdg.xdman.core.common.Credential;
import org.sdg.xdman.core.common.XDMConfig;

public class ConfigDialog extends JDialog implements TreeSelectionListener,
		ActionListener {

	private static final long serialVersionUID = -8869224019635935507L;
	JTree tree;
	JPanel rp, up;
	Box dp;
	JButton ok, cancel;
	CardLayout card = new CardLayout();
	JPanel general, saveto, advanced, connection;
	String items[] = { "General", "Connection", "Save To", "Advanced", "Proxy",
			"File Types", "Sites Login", "Scheduler" };
	JPanel cardPanel;
	JTextField tempdir, destdir;
	JButton br1, br2;
	JComboBox c = new JComboBox(new String[] { "1", "2", "4", "8", "16", "32" });
	JComboBox buf = new JComboBox(new String[] { "8", "16", "32", "64" });
	JSpinner t = new JSpinner(new SpinnerNumberModel(60, 0, 3600, 1));
	JCheckBox chk1 = new JCheckBox("Show Download Progress Dialog");
	JCheckBox chk2 = new JCheckBox("Show Download Complete Dialog");
	JCheckBox chk3 = new JCheckBox("Show Download Box");
	JComboBox act = new JComboBox(new String[] { "Auto Rename", "Resume",
			"Prompt" });
	JCheckBox cmdChk = new JCheckBox("Execute a command");
	JCheckBox mdmChk = new JCheckBox("Hung up Modem");
	JCheckBox haltChk = new JCheckBox("Shutdown Computer");
	JCheckBox antiChk = new JCheckBox("Scan with antivirus");

	JTextField cmd1 = new JTextField(30);
	JTextField cmd2 = new JTextField(30);
	JTextField cmd3 = new JTextField(30);
	JTextField cmd4 = new JTextField(30);

	JTextArea fileTypes;

	JTextField httpProxy, ftpProxy, httpsProxy;
	JTextField httpPort, ftpPort, httpsPort;
	JTextField httpUser, httpPass, httpsUser, httpsPass, ftpUser, ftpPass;
	JTextField bypassProxy;
	JCheckBox useHttpProxy, useHttpsProxy, useFtpProxy;
	JPanel proxyPanel;
	JPanel fileTypesPanel, authPanel, schedulePanel;

	XDMConfig config;

	JFileChooser fc;

	JTable table;
	CreditentialTableModel model;
	JButton addAuth, removeAuth, editAuth;
	JButton defaults;

	JSpinner startDate, endDate;
	JCheckBox schedule;

	SpinnerDateModel start, end;
	JCheckBox allowBrowser;
	JButton antivir;

	JComboBox ctype;

	public ConfigDialog(XDMConfig c, JFrame f) {
		super(f);
		setTitle("Configure");
		setIconImage(Main.icon.getImage());
		setModal(true);
		this.config = c;
		init();
	}

	void init() {
		rp = new JPanel(new BorderLayout());
		rp.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		add(rp);
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		setSize(d.width / 2, d.height / 2);
		tree = new JTree(items);
		tree.addTreeSelectionListener(this);
		rp.add(new JScrollPane(tree), BorderLayout.WEST);
		ok = new JButton("Save");
		ok.addActionListener(this);
		cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		dp = Box.createHorizontalBox();
		dp.add(Box.createHorizontalGlue());
		dp.add(ok);
		dp.add(Box.createHorizontalStrut(10));
		dp.add(cancel);
		dp.add(Box.createHorizontalStrut(10));
		dp.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		add(dp, BorderLayout.SOUTH);

		general = new JPanel(new BorderLayout());
		general.add(new JLabel("dfsdff"));
		connection = new JPanel(new BorderLayout());
		saveto = new JPanel(new BorderLayout());
		advanced = new JPanel(new BorderLayout());
		proxyPanel = new JPanel(new BorderLayout());
		fileTypesPanel = new JPanel(new BorderLayout(10, 10));
		authPanel = new JPanel(new BorderLayout(10, 10));
		schedulePanel = new JPanel(new BorderLayout());

		cardPanel = new JPanel(card);

		cardPanel.add(general, items[0]);
		cardPanel.add(connection, items[1]);
		cardPanel.add(saveto, items[2]);
		cardPanel.add(advanced, items[3]);
		cardPanel.add(proxyPanel, items[4]);
		cardPanel.add(fileTypesPanel, items[5]);
		cardPanel.add(authPanel, items[6]);
		cardPanel.add(schedulePanel, items[7]);

		rp.add(cardPanel);

		createSaveToPanel();
		createConnectionPanel();
		createGeneralPanel();
		createAdvancedPanel();
		createProxyPanel();
		createFileTypesPanel();
		createAuthPanel();
		createSchedulePanel();
		card.show(cardPanel, items[0]);
	}

	void createSaveToPanel() {
		br1 = new JButton("...");
		br1.addActionListener(this);
		br2 = new JButton("...");
		br2.addActionListener(this);
		tempdir = new JTextField();
		tempdir.setEditable(false);
		destdir = new JTextField();
		destdir.setEditable(false);
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.insets = new Insets(5, 5, 5, 5);
		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridwidth = 2;
		panel.add(new JLabel("Temporary Directory"), gc);
		gc.gridx = 0;
		gc.gridy = 1;
		gc.gridwidth = 1;
		gc.weightx = 1.0;
		panel.add(tempdir, gc);
		gc.gridx = 1;
		gc.gridy = 1;
		gc.gridwidth = 1;
		gc.weightx = 0.0;
		panel.add(br1, gc);
		gc.gridx = 0;
		gc.gridy = 2;
		gc.gridwidth = 2;
		panel.add(new JLabel("Destination Directory"), gc);
		gc.gridx = 0;
		gc.gridy = 3;
		gc.gridwidth = 1;
		gc.weightx = 1.0;
		panel.add(destdir, gc);
		gc.weightx = 0.0;
		gc.gridx = 1;
		gc.gridy = 3;
		gc.gridwidth = 1;
		panel.add(br2, gc);

		saveto.add(panel);
		saveto.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "Save To"));

	}

	void createConnectionPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.insets = new Insets(5, 5, 5, 5);
		gc.gridx = 0;
		gc.gridy = 0;
		panel.add(new JLabel("Connection per download:"), gc);
		gc.gridx = 1;
		gc.gridy = 0;
		panel.add(c, gc);
		gc.gridx = 0;
		gc.gridy = 1;
		panel.add(new JLabel("Connection timeout:"), gc);
		gc.gridx = 1;
		gc.gridy = 1;
		panel.add(t, gc);
		gc.gridx = 0;
		gc.gridy = 2;
		panel.add(new JLabel("Tcp Window Size(KB):"), gc);
		gc.gridx = 1;
		gc.gridy = 2;
		panel.add(buf, gc);
		gc.gridx = 0;
		gc.gridy = 3;
		compress = new JCheckBox("Enable compression");
		panel.add(compress, gc);

		connection.add(panel);
		connection.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "Connection"));
	}

	JCheckBox compress;

	void createGeneralPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.gridx = 0;
		gc.gridy = 0;
		panel.add(chk1, gc);
		gc.gridx = 0;
		gc.gridy = 1;
		panel.add(chk2, gc);
		gc.gridx = 0;
		gc.gridy = 2;
		panel.add(chk3, gc);
		allowBrowser = new JCheckBox(
				"Allow the browser to download if you press cancel");
		gc.gridx = 0;
		gc.gridy = 3;
		panel.add(allowBrowser, gc);
		general.add(panel);
		general.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "General"));
	}

	void createAdvancedPanel() {
		antivir = new JButton("Browse");
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridwidth = 2;
		panel.add(new JLabel("When download completes:"), gc);
		gc.gridx = 0;
		gc.gridy = 1;
		gc.gridwidth = 2;
		panel.add(cmdChk, gc);
		gc.gridx = 0;
		gc.gridy = 2;
		gc.gridwidth = 2;
		gc.weightx = 1.0;
		panel.add(cmd1, gc);
		gc.gridx = 0;
		gc.gridy = 3;
		gc.gridwidth = 2;
		panel.add(mdmChk, gc);
		gc.gridx = 0;
		gc.gridy = 4;
		gc.gridwidth = 2;
		panel.add(cmd2, gc);
		gc.gridx = 0;
		gc.gridy = 5;
		gc.gridwidth = 2;
		panel.add(haltChk, gc);
		gc.gridx = 0;
		gc.gridy = 6;
		gc.gridwidth = 2;
		panel.add(cmd3, gc);
		gc.gridx = 0;
		gc.gridy = 7;
		gc.gridwidth = 2;
		panel.add(antiChk, gc);
		gc.gridx = 0;
		gc.gridy = 8;
		gc.gridwidth = 1;
		panel.add(cmd4, gc);
		gc.gridx = 1;
		gc.gridy = 8;
		gc.gridwidth = 1;
		gc.weightx = 0.0;
		panel.add(antivir, gc);
		cmdChk.addActionListener(this);
		mdmChk.addActionListener(this);
		haltChk.addActionListener(this);
		antiChk.addActionListener(this);
		antivir.addActionListener(this);
		advanced.add(new JScrollPane(panel));
		advanced.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "Advanced"));
	}

	void createProxyPanel() {
		JPanel p = new JPanel(new GridLayout(4, 1));
		JPanel http = new JPanel(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.insets = new Insets(5, 5, 5, 5);
		gc.gridx = 0;
		gc.gridy = 0;
		gc.fill = GridBagConstraints.HORIZONTAL;
		useHttpProxy = new JCheckBox("Use HTTP Proxy");
		useHttpProxy.addActionListener(this);
		gc.gridwidth = 7;
		http.add(useHttpProxy, gc);
		gc.gridy = 1;
		gc.gridwidth = 1;
		http.add(new JLabel("Host"), gc);
		httpProxy = new JTextField();
		gc.gridx = 1;
		gc.weightx = 1;
		http.add(httpProxy, gc);
		gc.gridx = 2;
		gc.weightx = 0;
		http.add(new JLabel("Port"), gc);
		httpPort = new JTextField();
		gc.gridx = 3;
		gc.weightx = 0.5;
		http.add(httpPort, gc);
		gc.gridx = 4;
		gc.weightx = 0;
		http.add(new JLabel("USER"), gc);
		httpUser = new JTextField();
		gc.gridx = 5;
		gc.weightx = 1;
		http.add(httpUser, gc);
		gc.weightx = 0;
		gc.gridx = 6;
		http.add(new JLabel("PASS"), gc);
		httpPass = new JTextField();
		gc.weightx = 1;
		gc.gridx = 7;
		http.add(httpPass, gc);
		http.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "HTTP"));
		p.add(http);

		JPanel https = new JPanel(new GridBagLayout());
		gc = new GridBagConstraints();
		gc.insets = new Insets(5, 5, 5, 5);
		gc.gridx = 0;
		gc.gridy = 0;
		gc.fill = GridBagConstraints.HORIZONTAL;
		useHttpsProxy = new JCheckBox("Use HTTPS Proxy");
		useHttpsProxy.addActionListener(this);
		gc.gridwidth = 7;
		https.add(useHttpsProxy, gc);
		gc.gridwidth = 1;
		gc.gridx = 0;
		gc.gridy = 1;
		gc.weightx = 0;
		https.add(new JLabel("Host"), gc);
		httpsProxy = new JTextField();
		gc.gridx = 1;
		gc.weightx = 1;
		https.add(httpsProxy, gc);
		gc.gridx = 2;
		gc.weightx = 0;
		https.add(new JLabel("Port"), gc);
		httpsPort = new JTextField();
		gc.gridx = 3;
		gc.weightx = 0.5;
		https.add(httpsPort, gc);
		gc.gridx = 4;
		gc.weightx = 0;
		https.add(new JLabel("USER"), gc);
		httpsUser = new JTextField();
		gc.gridx = 5;
		gc.weightx = 1;
		https.add(httpsUser, gc);
		gc.weightx = 0;
		gc.gridx = 6;
		https.add(new JLabel("PASS"), gc);
		httpsPass = new JTextField();
		gc.weightx = 1;
		gc.gridx = 7;
		https.add(httpsPass, gc);
		https.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "HTTPS"));
		p.add(https);

		JPanel ftp = new JPanel(new GridBagLayout());
		gc = new GridBagConstraints();
		gc.insets = new Insets(5, 5, 5, 5);
		gc.gridx = 0;
		gc.gridy = 0;
		gc.fill = GridBagConstraints.HORIZONTAL;
		useFtpProxy = new JCheckBox("Use FTP Proxy");
		useFtpProxy.addActionListener(this);
		gc.gridwidth = 7;
		ftp.add(useFtpProxy, gc);
		gc.gridwidth = 1;
		gc.gridx = 0;
		gc.gridy = 1;
		gc.weightx = 0;
		ftp.add(new JLabel("Host"), gc);
		ftpProxy = new JTextField();
		gc.gridx = 1;
		gc.weightx = 1;
		ftp.add(ftpProxy, gc);
		gc.gridx = 2;
		gc.weightx = 0;
		ftp.add(new JLabel("Port"), gc);
		ftpPort = new JTextField();
		gc.gridx = 3;
		gc.weightx = 0.5;
		ftp.add(ftpPort, gc);
		gc.gridx = 4;
		gc.weightx = 0;
		ftp.add(new JLabel("USER"), gc);
		ftpUser = new JTextField();
		gc.gridx = 5;
		gc.weightx = 1;
		ftp.add(ftpUser, gc);
		gc.weightx = 0;
		gc.gridx = 6;
		ftp.add(new JLabel("PASS"), gc);
		ftpPass = new JTextField();
		gc.weightx = 1;
		gc.gridx = 7;
		ftp.add(ftpPass, gc);
		ftp.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "FTP"));
		p.add(ftp);

		JPanel bypass = new JPanel(new GridBagLayout());
		gc = new GridBagConstraints();
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.insets = new Insets(5, 5, 5, 5);
		bypassProxy = new JTextField();
		gc.weightx = 1.0;
		gc.gridwidth = 4;
		bypass.add(bypassProxy, gc);
		gc.gridy = 1;
		bypass.add(new JLabel("Separate names by spaces"), gc);
		bypass.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "Do NOT use proxy for following hosts"));
		p.add(bypass);
		p.add(bypass);
		proxyPanel.add(new JScrollPane(p));
		proxyPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "Proxy"));
	}

	void createFileTypesPanel() {
		fileTypes = new JTextArea();
		fileTypes.setWrapStyleWord(true);
		fileTypes.setLineWrap(true);
		fileTypesPanel.add(new JLabel(
				"Capture download for following file types:"),
				BorderLayout.NORTH);
		fileTypesPanel.add(new JScrollPane(fileTypes));
		fileTypes.setBorder(BorderFactory.createBevelBorder(1));
		defaults = new JButton("Defaults");
		defaults.addActionListener(this);
		fileTypesPanel.add(defaults, BorderLayout.SOUTH);
	}

	void createAuthPanel() {
		JPanel p = new JPanel(new BorderLayout());
		model = new CreditentialTableModel();
		Authenticator.getInstance().addObserver(model);
		table = new JTable(model);
		p.add(new JScrollPane(table));
		Box b = Box.createHorizontalBox();
		b.add(Box.createHorizontalGlue());
		addAuth = new JButton("Add");
		b.add(addAuth);
		addAuth.addActionListener(this);
		b.add(Box.createHorizontalStrut(10));
		removeAuth = new JButton("Remove");
		removeAuth.addActionListener(this);
		b.add(removeAuth);
		b.add(Box.createHorizontalStrut(10));
		editAuth = new JButton("Edit");
		editAuth.addActionListener(this);
		b.add(editAuth);
		b.add(Box.createHorizontalStrut(10));
		authPanel.add(p);
		authPanel.add(b, BorderLayout.SOUTH);
		authPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "Sites Logins"));
	}

	private void createSchedulePanel() {
		schedulePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "Scheduler"));
		schedule = new JCheckBox("Enable Scheduler");
		schedule.addActionListener(this);
		schedulePanel.add(schedule, BorderLayout.NORTH);
		start = new SpinnerDateModel();
		end = new SpinnerDateModel();
		startDate = new JSpinner(start);
		startDate.setEditor(new JSpinner.DateEditor(startDate, "dd-MMM HH:mm"));
		endDate = new JSpinner(end);
		endDate.setEditor(new JSpinner.DateEditor(endDate, "dd-MMM HH:mm"));
		JPanel center = new JPanel(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = 0;
		gc.gridy = 0;
		center.add(new JLabel("Start Queue Processing at:"), gc);
		gc.gridx = 0;
		gc.gridy = 1;
		center.add(startDate, gc);
		gc.gridx = 0;
		gc.gridy = 2;
		center.add(new JLabel("Stop Queue Processing at:"), gc);
		gc.gridx = 0;
		gc.gridy = 3;
		center.add(endDate, gc);
		schedulePanel.add(center);
	}

	void chkTxt() {
		if (haltChk.isSelected()) {
			haltChk.setSelected(cmd2.getText().length() > 0);
		}
		if (cmdChk.isSelected()) {
			cmdChk.setSelected(cmd1.getText().length() > 0);
		}
		if (antiChk.isSelected()) {
			antiChk.setSelected(cmd4.getText().length() > 0);
		}
		if (mdmChk.isSelected()) {
			mdmChk.setSelected(cmd3.getText().length() > 0);
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == schedule) {
			startDate.setEnabled(schedule.isSelected());
			endDate.setEnabled(schedule.isSelected());
		}
		if (e.getSource() == ok) {
			if (!chkVal()) {
				System.out.println("ERROR");
				return;
			}
			chkTxt();
			applyConfig();
			config.save();
			Authenticator.getInstance().save();
			setVisible(false);
		}
		if (e.getSource() == cancel) {
			setVisible(false);
		}
		if (e.getSource() == br1) {
			if (fc == null) {
				fc = new JFileChooser();
			}
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				tempdir.setText(fc.getSelectedFile().getAbsolutePath());
			}
		}
		if (e.getSource() == br2) {
			if (fc == null) {
				fc = new JFileChooser();
			}
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				destdir.setText(fc.getSelectedFile().getAbsolutePath());
			}
		}
		if (e.getSource() == useHttpProxy) {
			applyHttpProxyConfig();
		}
		if (e.getSource() == useHttpsProxy) {
			applyHttpsProxyConfig();
		}
		if (e.getSource() == useFtpProxy) {
			applyFtpProxyConfig();
		}
		if (e.getSource() == addAuth) {
			Credential c = getCredential(true);
			if (c != null)
				Authenticator.getInstance().addCreditential(c);
		}
		if (e.getSource() == removeAuth) {
			int index = table.getSelectedRow();
			if (index < 0) {
				JOptionPane.showMessageDialog(this, "No item selected");
				return;
			}
			String host = model.getValueAt(index, 0) + "";
			Authenticator.getInstance().removeCreditential(host);
		}
		if (e.getSource() == editAuth) {
			int index = table.getSelectedRow();
			if (index < 0) {
				JOptionPane.showMessageDialog(this, "No item selected");
				return;
			}
			String host = model.getValueAt(index, 0) + "";
			System.out.println("HOST: " + host);
			Credential c = Authenticator.getInstance().getCredential(host);
			System.out.println("CR: " + c);
			if (this.host == null)
				this.host = new JTextField();
			this.host.setText(host);
			c = getCredential(false);
			if (c != null)
				Authenticator.getInstance().addCreditential(c);
		}
		if (e.getSource() == defaults) {
			String types = "";
			for (int i = 0; i < config.defaultFileTypes.length; i++)
				types += config.defaultFileTypes[i] + " ";
			fileTypes.setText(types);
		}
		if (e.getSource() == antivir) {
			if (fc == null) {
				fc = new JFileChooser();
			}
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				cmd4.setText(fc.getSelectedFile().getAbsolutePath());
			}
		}

	}

	JTextField host, user, pass;
	Object obj[];

	Credential getCredential(boolean clear) {
		if (host == null)
			host = new JTextField();
		if (user == null)
			user = new JTextField();
		if (pass == null)
			pass = new JTextField();
		if (obj == null) {
			obj = new Object[6];
			obj[0] = "Host";
			obj[1] = host;
			obj[2] = "user";
			obj[3] = user;
			obj[4] = "pass";
			obj[5] = pass;
		}
		if (clear) {
			host.setText("");
			user.setText("");
			pass.setText("");
		}
		host.setEditable(clear);
		while (JOptionPane.showOptionDialog(null, obj, "Enter Creditential",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
				null, null, null) == JOptionPane.OK_OPTION) {
			if (host.getText() == null || host.getText().length() < 1) {
				JOptionPane.showMessageDialog(null, "Enter Host");
				continue;
			}
			if (user.getText() == null || user.getText().length() < 1) {
				JOptionPane.showMessageDialog(null, "Enter username");
				continue;
			}
			if (pass.getText() == null || pass.getText().length() < 1) {
				JOptionPane.showMessageDialog(null, "Enter password");
				continue;
			}
			Credential c = new Credential();
			c.host = host.getText();
			c.user = user.getText();
			c.pass = pass.getText();
			return c;
		}
		return null;
	}

	void applyHttpProxyConfig() {
		boolean selected = useHttpProxy.isSelected();
		httpProxy.setEnabled(selected);
		httpPort.setEnabled(selected);
		httpUser.setEnabled(selected);
		httpPass.setEnabled(selected);
	}

	void applyHttpsProxyConfig() {
		boolean selected = useHttpsProxy.isSelected();
		httpsProxy.setEnabled(selected);
		httpsPort.setEnabled(selected);
		httpsUser.setEnabled(selected);
		httpsPass.setEnabled(selected);
	}

	void applyFtpProxyConfig() {
		boolean selected = useFtpProxy.isSelected();
		ftpProxy.setEnabled(selected);
		ftpPort.setEnabled(selected);
		ftpUser.setEnabled(selected);
		ftpPass.setEnabled(selected);
	}

	void showDialog() {
		showConfig();
		setVisible(true);
	}

	void showConfig() {
		chk1.setSelected(config.showDownloadPrgDlg);
		chk2.setSelected(config.showDownloadCompleteDlg);
		chk3.setSelected(config.showDownloadBox);
		act.setSelectedIndex(config.duplicateLinkAction);
		c.setSelectedItem(config.maxConn + "");
		t.setValue(config.timeout);
		destdir.setText(config.destdir);
		tempdir.setText(config.tempdir);
		cmdChk.setSelected(config.executeCmd);
		antiChk.setSelected(config.antivir);
		haltChk.setSelected(config.halt);
		mdmChk.setSelected(config.hungUp);
		useHttpProxy.setSelected(config.useHttpProxy);
		useHttpsProxy.setSelected(config.useHttpsProxy);
		useFtpProxy.setSelected(config.useFtpProxy);
		applyHttpProxyConfig();
		applyFtpProxyConfig();
		applyHttpsProxyConfig();
		httpProxy.setText(config.httpProxyHost);
		httpPort.setText(config.httpProxyPort + "");
		httpUser.setText(config.httpUser);
		httpPass.setText(config.httpPass);
		httpsProxy.setText(config.httpsProxyHost);
		httpsPort.setText(config.httpsProxyPort + "");
		httpsUser.setText(config.httpsUser);
		httpsPass.setText(config.httpsPass);
		ftpProxy.setText(config.ftpProxyHost);
		ftpPort.setText(config.ftpProxyPort + "");
		ftpUser.setText(config.ftpUser);
		ftpPass.setText(config.ftpPass);
		String arr[] = config.fileTypes;
		String types = "";
		for (int i = 0; i < arr.length; i++) {
			types += arr[i] + " ";
		}
		fileTypes.setText(types);
		model.load();
		schedule.setSelected(config.schedule);
		startDate.setEnabled(config.schedule);
		endDate.setEnabled(config.schedule);
		if (config.startDate != null)
			start.setValue(config.startDate);
		if (config.endDate != null)
			end.setValue(config.endDate);
		cmd1.setText(config.cmdTxt);
		cmd4.setText(config.antivirTxt);
		cmd3.setText(config.hungUpTxt);
		cmd2.setText(config.haltTxt);
		allowBrowser.setSelected(config.allowbrowser);
		int v = config.tcpBuf / 1024;
		buf.setSelectedItem(v + "");
		compress.setSelected(config.compress);
	}

	boolean chkVal() {
		try {
			if (useHttpProxy.isSelected()) {
				if (httpProxy.getText().length() < 1)
					throw new Exception("Host is empty");
				Integer.parseInt(httpPort.getText());
			}
			if (useHttpsProxy.isSelected()) {
				if (httpsProxy.getText().length() < 1)
					throw new Exception("Host is empty");
				Integer.parseInt(httpsPort.getText());
			}
			if (useFtpProxy.isSelected()) {
				if (ftpProxy.getText().length() < 1)
					throw new Exception("Host is empty");
				Integer.parseInt(ftpPort.getText());
			}
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(this, "Please enter numeric value");
			return false;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Field can not be left blank");
			return false;
		}
		return true;
	}

	void applyConfig() {
		config.antivir = antiChk.isSelected();
		config.antivirTxt = cmd4.getText();
		config.cmdTxt = cmd1.getText();
		config.destdir = destdir.getText();
		config.tempdir = tempdir.getText();
		config.duplicateLinkAction = act.getSelectedIndex();
		config.executeCmd = cmdChk.isSelected();
		config.halt = haltChk.isSelected();
		config.haltTxt = cmd3.getText();
		config.hungUp = mdmChk.isSelected();
		config.hungUpTxt = cmd2.getText();
		config.maxConn = Integer.parseInt(c.getSelectedItem().toString());
		config.showDownloadBox = chk3.isSelected();
		config.showDownloadPrgDlg = chk1.isSelected();
		config.showDownloadCompleteDlg = chk2.isSelected();
		config.useHttpProxy = useHttpProxy.isSelected();
		config.useHttpsProxy = useHttpsProxy.isSelected();
		config.useFtpProxy = useFtpProxy.isSelected();
		config.httpProxyHost = httpProxy.getText();
		config.httpsProxyHost = httpsProxy.getText();
		config.ftpProxyHost = ftpProxy.getText();
		config.httpUser = httpUser.getText();
		config.httpsUser = httpsUser.getText();
		config.ftpUser = ftpUser.getText();
		config.httpPass = httpPass.getText();
		config.httpsPass = httpsPass.getText();
		config.ftpPass = ftpPass.getText();
		config.httpProxyPort = Integer.parseInt(httpPort.getText());
		config.httpsProxyPort = Integer.parseInt(httpsPort.getText());
		config.ftpProxyPort = Integer.parseInt(ftpPort.getText());
		config.tcpBuf = Integer.parseInt(buf.getSelectedItem().toString()) * 1024;
		// config.fileTypes = fileTypes.getText().replaceAll("\n",
		// " ").split(" ");

		ArrayList<String> lst = new ArrayList<String>();
		String arr[] = fileTypes.getText().replaceAll("\n", " ").split(" ");
		for (int i = 0; i < arr.length; i++) {
			String t = arr[i].trim();
			if (t.length() > 0)
				lst.add(t);
			// System.out.println("TYPE: '" + t + "'");
		}
		config.fileTypes = new String[lst.size()];
		for (int i = 0; i < lst.size(); i++) {
			config.fileTypes[i] = lst.get(i);
		}
		config.schedule = schedule.isSelected();
		config.startDate = start.getDate();
		config.endDate = end.getDate();
		config.allowbrowser = allowBrowser.isSelected();
		config.compress = compress.isSelected();
	}

	public void valueChanged(TreeSelectionEvent e) {
		Object path[] = e.getPath().getPath();
		if (path.length > 0) {
			card.show(cardPanel, path[path.length - 1].toString());
		}
	}

	public static void main(String a[]) {
		new ConfigDialog(new XDMConfig(null), null).showDialog();
	}
}
