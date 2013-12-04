package org.sdg.xdman.core;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableColumnModel;
import javax.swing.text.Document;

import org.sdg.xdman.core.common.ConnectionManager;
import org.sdg.xdman.core.common.DownloadInfo;
import org.sdg.xdman.core.common.IXDMConstants;
import org.sdg.xdman.core.common.XDMConfig;
import org.sdg.xdman.core.common.http.XDMHttpClient;
import org.sdg.xdman.gui.ConfigDialog;
import org.sdg.xdman.gui.ConnectionTableModel;
import org.sdg.xdman.gui.DownloadList;
import org.sdg.xdman.gui.DownloadListItem;
import org.sdg.xdman.gui.FFListRenderer;
import org.sdg.xdman.gui.Main;
import org.sdg.xdman.gui.SegmentPanel;
import org.sdg.xdman.gui.XDMProgressBar;
import org.sdg.xdman.gui.YTDThread;
import org.sdg.xdman.gui.YoutubeListener;
import org.sdg.xdman.gui.YoutubeMediaListener;
import org.sdg.xdman.plugin.youtube.ParserProgressListner;
import org.sdg.xdman.proxy.RequestHandler;
import org.sdg.xdman.util.XDMUtil;

public class Dialog {
	
	public static void about(){
		new AboutDlg().setVisible(true);
	}
	public static void config(){
		new ConfigDialog(Config.config, Main.frame).setVisible(true);
	}
	
	public static String[] auth(){
		return AuthDialog.getAuth();
	}
	
	public static void download(ConnectionManager c, DownloadListItem item){
		DownloadWindow w = new DownloadWindow(c);
		c.addObserver(w);
		item.window = w.window;
		w.showWindow();
	}
	
	public static void download(ConnectionManager c){
		DownloadWindow w = new DownloadWindow(c);
		c.addObserver(w);
		w.showWindow();
	}
	
	public static void downloadFileInfo(ActionListener a, ActionListener b, String url, XDMHttpClient client,
			HashMap<String, String> map, String cookies, Object invoker){
		DownloadFileInfoDialog dlg = new DownloadFileInfoDialog(a, b);
		dlg.setURL(url);
		dlg.setDir(Config.config.destdir);
		dlg.client = client;
		dlg.extra = map;
		dlg.cookies = cookies;
		dlg.setAlwaysOnTop(true);
		dlg.showDlg();
		if (Config.config.allowbrowser) {
			if (invoker != null) {
				dlg.interceptor = invoker;
				synchronized (dlg) {
					try {
						dlg.wait();
						System.out.println("Returned Intercept? "
								+ ((RequestHandler) invoker).intercept);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			if (invoker != null) {
				((RequestHandler) invoker).intercept = true;
			}
		}
	}
	
	public static void downloadNow(ActionEvent e){
		DownloadFileInfoDialog dfi = (DownloadFileInfoDialog) e.getSource();
		if (dfi.interceptor != null) {
			((RequestHandler) dfi.interceptor).intercept = true;
			synchronized (dfi) {
				dfi.notifyAll();
			}
		}
		String url = dfi.getURL();
		String file = dfi.getFile();
		String dir = dfi.getDir();
		String user = dfi.getUser();
		String pass = dfi.getPass();
		HashMap<String, String> extra = dfi.extra;
		if (user.length() > 0) {
			try {
				if (extra == null) {
					extra = new HashMap<String, String>();
				}
				extra.put("USER", user);
				extra.put("PASS", pass);
			} catch (Exception err) {
			}
		}
		//addDownload(url, file, dir, tempdir, list, dfi.client, extra, dfi.cookies);
	}
	
	public static void downloadLater(ActionEvent e){
		DownloadFileInfoDialog dfi = (DownloadFileInfoDialog) e.getSource();
		if (dfi.interceptor != null) {
			((RequestHandler) dfi.interceptor).intercept = true;
			synchronized (dfi) {
				dfi.notifyAll();
			}
		}
		String url = dfi.getURL();
		String user = dfi.getUser();
		String pass = dfi.getPass();
		HashMap<String, String> extra = dfi.extra;
		if (user.length() > 0) {
			try {
				if (extra == null) {
					extra = new HashMap<String, String>();
				}
				extra.put("USER", user);
				extra.put("PASS", pass);
			} catch (Exception err) {
			}
		}
		// addDownload(url, file, tempdir, list, dfi.client, extra,
		// dfi.cookies);
		DownloadListItem item = new DownloadListItem();
		item.dateadded = new Date().toString();
		item.lasttry = new Date().toString();
		item.q = true;
		item.url = url;
		item.extra = extra;
		item.filename = dfi.getFile();// XDMUtil.getFileName(url);
		item.saveto = dfi.getDir();// destdir;
		item.cookies = dfi.cookies;
	}
	
	public static void downloadComplete(DownloadListItem item){
		DownloadCompleteDialog completeDlg = new DownloadCompleteDialog();
		completeDlg.setLocationRelativeTo(null);
		completeDlg.setAlwaysOnTop(true);
		completeDlg.setData(item.filename, item.url);
		completeDlg.file_path = item.filename;
		completeDlg.folder_path = item.saveto;
		completeDlg.setVisible(true);
	}
	
	public static void fireFoxIntegration(){
		DefaultListModel ffmodel = new DefaultListModel();
		ffmodel.add(0, "http://localhost:" + Config.config.port + "/xdmff.xpi");
		new FFIntDlg(ffmodel).setVisible(true);
	}
	
	public static void browserIntegration(){
		new BrowserIntDlg().setVisible(true);
	}
	
	public static void testBrowser(){
		JTextField url = new JTextField("http://localhost:" + Config.config.port + "/test");
		url.setEditable(false);
		JButton copy = new JButton("Copy");
		Object arr[] = {
				"To check if XDM is synchronized with your browser\n"
						+ "Paste/type this URL in your browser.\n",
				url,
				copy,
				"If its not synchronized\n"
						+ "You have to configure the browser.\n"
						+ "If you have configured it already then try restarting it again." };
		JOptionPane.showOptionDialog(Main.frame, arr, "Test Browser",
				JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
				null, null, null);
	}

}



class FFIntDlg extends JDialog {

	private static final long serialVersionUID = 2611700042218657860L;

	public FFIntDlg(ListModel model) {
		super(Main.frame);
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		// setSize(size.width / 2, size.height / 2);
		setModal(true);
		setTitle("Integrate with Firefox");
		JList list = new JList(model);
		list.setCellRenderer(new FFListRenderer());
		list.setDragEnabled(true);
		add(list);
		Cursor c = new Cursor(Cursor.HAND_CURSOR);
		list.setCursor(c);
		JLabel info = new JLabel(
				"Drag the Icon or type the address into firefox",
				JLabel.CENTER);
		add(info, BorderLayout.NORTH);
		pack();
		setLocation(size.width / 2 - getWidth() / 2, size.height / 2
				- getHeight() / 2);

	}
}

class ShutdownDlg extends JDialog implements Runnable, ActionListener {

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

	public void start(String cmd) {
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

class AboutDlg extends JDialog implements ActionListener {

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
		JLabel ver = new JLabel(Config.version);
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

class AuthDialog {
	static JTextField user = null;
	static JTextField pass = null;
	static Object obj[];

	public static String[] getAuth() {
		if (user == null)
			user = new JTextField();
		if (pass == null)
			pass = new JTextField();
		if (obj == null) {
			obj = new Object[4];
			obj[0] = "user";
			obj[1] = user;
			obj[2] = "pass";
			obj[3] = pass;
		}
		user.setText("");
		pass.setText("");
		while (JOptionPane.showOptionDialog(null, obj, "Enter Creditential",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
				null, null, null) == JOptionPane.OK_OPTION) {
			if (user.getText() == null || user.getText().length() < 1) {
				JOptionPane.showMessageDialog(null, "Enter username");
				continue;
			}
			if (pass.getText() == null || pass.getText().length() < 1) {
				JOptionPane.showMessageDialog(null, "Enter password");
				continue;
			}
			return new String[] { user.getText(), pass.getText() };
		}
		return null;
	}
}

class BrowserIntDlg extends JDialog {

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

class DownloadWindow implements Observer, ActionListener {
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

class RefreshLinkDlg extends JDialog implements ActionListener {

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
				Main.downloadList.downloadStateChanged();
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


class YoutubeGrabberDlg extends JFrame implements ActionListener,
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

class DownloadCompleteDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = -6952846084893748315L;
	JTextField file, url;
	JButton open, close, open_folder;
	String file_path, folder_path;

	public DownloadCompleteDialog() {
		super();
		setSize(300, 300);
		setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.insets = new Insets(5, 5, 5, 5);
		gc.gridwidth = 3;
		gc.fill = GridBagConstraints.HORIZONTAL;
		add(new JLabel("Download Complete"), gc);
		gc.gridx = 0;
		gc.gridy = 1;
		add(new JLabel("File"), gc);
		gc.gridx = 0;
		gc.gridy = 2;
		file = new JTextField();
		add(file, gc);
		file.setEditable(false);
		gc.gridx = 0;
		gc.gridy = 3;
		add(new JLabel("URL"), gc);
		url = new JTextField();
		url.setEditable(false);
		gc.gridy = 4;
		add(url, gc);
		open = new JButton("Open");
		open.addActionListener(this);
		open_folder = new JButton("Open Folder");
		open_folder.addActionListener(this);
		close = new JButton("Close");
		close.addActionListener(this);
		gc.gridx = 0;
		gc.gridy = 5;
		gc.gridwidth = 1;
		add(open, gc);
		gc.gridx = 1;
		add(open_folder, gc);
		gc.gridx = 2;
		add(close, gc);
		try {
			setIconImage(Main.icon.getImage());
		} catch (Exception e) {
		}
	}

	void setData(String file, String url) {
		this.file.setText(file);
		this.url.setText(url);
		setTitle(file);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String str = e.getActionCommand();
		if (str.equals("Open")) {
			XDMUtil.open(new File(folder_path, file_path));
			setVisible(false);
		}
		if (str.equals("Open Folder")) {
			XDMUtil.open(new File(folder_path));
			setVisible(false);
		}
		if (str.equals("Close")) {
			setVisible(false);
		}
	}
}

class DownloadFileInfoDialog extends JDialog implements ActionListener,
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
	
	public DownloadFileInfoDialog(ActionListener dl, ActionListener dn) {
		this.dl_action = dl;
		this.dn_action = dn;
		this.config = Config.config;
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
