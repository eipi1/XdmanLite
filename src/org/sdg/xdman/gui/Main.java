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

import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.sdg.xdman.core.common.Authenticator;
import org.sdg.xdman.core.common.ConnectionManager;
import org.sdg.xdman.core.common.IXDMConstants;
import org.sdg.xdman.core.common.UnsupportedProtocolException;
import org.sdg.xdman.core.common.XDMConfig;
import org.sdg.xdman.core.common.http.XDMHttpClient;
import org.sdg.xdman.proxy.RequestHandler;
import org.sdg.xdman.proxy.RequestIntercepter;
import org.sdg.xdman.proxy.XDMProxyServer;
import org.sdg.xdman.util.XDMUtil;
import org.sdg.xdman.util.win.RegUtil;

public class Main  implements Observer, IXDMQueue, Runnable, RequestIntercepter,
		BatchDownloadListener,YoutubeMediaListener {
	
	XDMProxyServer server;
	static DownloadList list = null;// new DownloadList();
	static String tempdir = System.getProperty("user.home");
	static String destdir = tempdir;
	static String appdir = tempdir;
	IDownloadListener dlistener;
	boolean stop = false;
	static ConfigWindow cwin;
	public static XDMConfig config;
	boolean queue;
	Thread scheduler;
	DownloadCompleteDialog completeDlg;
	ShutdownDlg sdlg;
	public static ImageIcon icon;
	
	Clipboard clipboard;
	RefreshLinkDlg rdlg;
	boolean haltPending = false;
	HelpDialog view;
	BrowserIntDlg bint;
	HttpTableModel httpModel;
	HttpMonitorDlg httpDlg;
	BatchDownloadDlg batchDlg;
	BatchDlg bdlg;
	public static boolean proxyAttached = false;
	JLabel queueLabel;
	Icon qIcon;
	YoutubeGrabberDlg ytgdlg;
	static JFrame frame = new JFrame();

	public static String version = "Version: 3.01 Build 15 (June 09, 2013)";
	String updateURL = "http://xdm.sourceforge.net/update.php";
	String homeURL = "http://xdm.sourceforge.net/";

	public Main(DownloadList list) {
		icon = getIcon("icon.png");
		Main.list = list;
		this.dlistener = list;
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				shutdownHook();
			}
		});
		// t = new Thread(this);
		// t.start();
	}

	public static ImageIcon getIcon(String name) {
		try {
			return new ImageIcon(Main.class.getResource("/res/" + name));
		} catch (Exception e) {
			return new ImageIcon("res/" + name);
		}
	}
	
	public static String getJarPath() {
		try {
			String path = Main.class.getResource("/").toURI().getPath();
			System.out.println(path);
			return new File(path).getAbsolutePath();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Error");
		return "Error";
	}

	void shutdownHook() {
		System.out.println("ShutdownHook...");
		dlistener.downloadStateChanged();
		config.save();
		Authenticator.getInstance().save();
		System.out.println("Stopping server");
		server.stop();
		System.out.println("Stopping server...done");
		if (proxyAttached) {
			File bak = new File(System.getProperty("user.home"), "reg-bak.reg");
			RegUtil.restore(bak);
			attachProxy(true);// runWPAC(false);
		}
	}

	void addDownload(String url, String fileName, String destdir,
			String tempdir, DownloadList lst, XDMHttpClient client,
			HashMap<String, String> extra, String cookies) {
		DownloadListItem item = new DownloadListItem();
		item.dateadded = new Date().toString();
		item.lasttry = new Date().toString();
		item.url = url;
		item.extra = extra;
		item.filename = fileName;// XDMUtil.getFileName(url);
		item.icon = IconUtil.getIcon(XDMUtil.findCategory(item.filename));
		item.saveto = destdir;
		item.cookies = cookies;
		list.add(item);
		startDownload(url, fileName, destdir, tempdir, item, client,
				item.extra, cookies);
	}

	void startDownload(String url, String fileName, String destdir,
			String tempdir, DownloadListItem item, XDMHttpClient client,
			HashMap<String, String> extra, String cookies) {
		startDownload(url, fileName, destdir, tempdir, item, client, extra,
				cookies, true);
	}

	void startDownload(String url, String fileName, String destdir,
			String tempdir, DownloadListItem item, XDMHttpClient client,
			HashMap<String, String> extra, String cookies, boolean fg) {
		ConnectionManager c = new ConnectionManager(url, fileName, destdir,
				tempdir, extra, config);
		c.extra = extra;
		c.setTimeOut(config.timeout);
		c.setMaxConn(config.maxConn);
		if (config.showDownloadPrgDlg && fg) {
			DownloadWindow w = new DownloadWindow(c);
			c.addObserver(w);
			item.window = w.window;
			w.showWindow();
		}
		//item.setCallback(c, model, dlistener);
		item.addObserver(this);
		//model.fireTableDataChanged();
		dlistener.downloadStateChanged();
		if (client == null) {
			try {
				c.start();
			} catch (UnsupportedProtocolException e) {
				//JOptionPane.showMessageDialog(frame, "Unsupported protocol");
			}
		} else {
			try {
				c.start(client);
			} catch (UnsupportedProtocolException e) {
				//JOptionPane.showMessageDialog(frame, "Unsupported protocol");
			}
		}
	}


	@SuppressWarnings("unchecked")
	@Override
	public void update(Observable o, Object obj) {
		if (o == config) {
			System.out.println("Config updated...");
			if (config.tempdir != null)
				if (config.tempdir.length() > 0) {
					if (new File(config.tempdir).exists()) {
						Main.tempdir = config.tempdir;
					}
				}
			System.out.println("M_DESTDIR: " + destdir + "\nTEMpDIR: "
					+ tempdir);
			System.out.println("C_DESTDIR: " + config.destdir + "\nTEMpDIR: "
					+ config.tempdir);
			if (config.destdir != null)
				if (config.destdir.length() > 0) {
					if (new File(config.destdir).exists()) {
						Main.destdir = config.destdir;
					}
				}

			System.out.println("DESTDIR: " + destdir + "\nTEMpDIR: " + tempdir);
			if (config.schedule) {
				if (scheduler == null) {
					scheduler = new Thread(this);
					scheduler.start();
					return;
				}
				if (!scheduler.isAlive()) {
					scheduler = new Thread(this);
					scheduler.start();
				}
			}
		}
		if (obj instanceof HashMap) {
			HashMap<String, Object> ht = (HashMap<String, Object>) obj;
			//addURL((String) ht.get("URL"), null, (HashMap<String, String>) ht
			//		.get("HT"), (String) ht.get("COOKIES"));
		}
		if (obj instanceof HashSet) {
			HashSet<String> hs = (HashSet<String>) obj;
			Iterator<String> it = hs.iterator();
			System.out.println(hs + " " + hs.size());
			String txt = "Browser: ";
			if (hs.size() < 3) {
				if (hs.size() == 1)
					txt += it.next();
				if (hs.size() == 2) {
					txt = it.next() + " , " + it.next();
				}
			} else {
				txt += it.next() + " , " + it.next() + " & " + (hs.size() - 2)
						+ " More";
			}
		}
		if (o instanceof DownloadListItem) {
			DownloadListItem item = (DownloadListItem) o;
			if (item.state == IXDMConstants.COMPLETE) {
				System.out.println("COMPLETE CALLBACK");
				if (queue) {
					next();
				} else if (config.showDownloadCompleteDlg) {
					if (completeDlg == null) {
						completeDlg = new DownloadCompleteDialog();
						completeDlg.setLocationRelativeTo(null);
						completeDlg.setAlwaysOnTop(true);
					}
					completeDlg.setData(item.filename, item.url);
					completeDlg.file_path = item.filename;
					completeDlg.folder_path = item.saveto;
					completeDlg.setVisible(true);
				}
				if (config.halt) {
					if (config.haltTxt == null || config.haltTxt.length() < 1)
						return;
					else
						haltPending = true;
				}
				File file = new File(item.saveto, item.filename);
				if (config.executeCmd) {
					//executeCommand(config.cmdTxt + " " + file);
				}
				if (config.hungUp) {
					//hungUp(config.hungUpTxt);
				}
				if (config.antivir) {
					//virusScan(config.antivirTxt + " " + file);
				}
			}
			if (item.state == IXDMConstants.FAILED) {
				System.out.println("FAILED CALLBACK");
				if (queue) {
					item.q = false;
					next();
				}
			}
		}
	}

	public void setConfig(XDMConfig c) {
		System.out.println("Setting config");
		config = c;
		if (config == null) {
			config = new XDMConfig(new File(appdir, ".xdmconf"));
		}
		if (config.tempdir == null || config.tempdir.length() < 1) {
			config.tempdir = tempdir;
		}
		if (config.destdir == null || config.destdir.length() < 1) {
			config.destdir = destdir;
		}
		System.out.println(config);
		if (config.tempdir != null)
			if (config.tempdir.length() > 0) {
				if (new File(config.tempdir).exists()) {
					Main.tempdir = config.tempdir;
				}
			}
		if (config.destdir != null)
			if (config.destdir.length() > 0) {
				if (new File(config.destdir).exists()) {
					Main.destdir = config.destdir;
				}
			}
		config.addObserver(this);
		if (config.schedule) {
			if (scheduler == null) {
				scheduler = new Thread(this);
				scheduler.start();
				return;
			}
			if (!scheduler.isAlive()) {
				scheduler = new Thread(this);
				scheduler.start();
			}
		}
	}

	@Override
	public void startQ() {
		if (!queue) {
			queue = true;
			next();
		}
	}

	@Override
	public void stopQ() {
		queue = false;
		/*if (queuedItem != null) {
			if (queuedItem.mgr != null) {
				queuedItem.mgr.stop();
				queuedItem = null;
			}
		}*/
	}

	@Override
	public void next() {
		// TODO Auto-generated method stub
		for (int i = 0; i < list.list.size(); i++) {
			DownloadListItem item = list.list.get(i);
			if (item.q) {
				if (item.mgr == null) {
					if (item.state != IXDMConstants.COMPLETE) {
						if (item.tempdir.equals("")) {
							startDownload(item.url, item.filename, item.saveto,
									tempdir, item, null, item.extra,
									item.cookies, false);
						} else {
							ConnectionManager c = new ConnectionManager(
									item.url, item.filename, item.saveto,
									item.tempdir, item.extra, config);
							c.setTimeOut(config.timeout);
							c.setMaxConn(config.maxConn);
							//item.setCallback(c, model, dlistener);
							item.addObserver(this);
							c.resume();
						}
						//queuedItem = item;
						if (qIcon == null) {
							qIcon = getIcon("icon16.png");
						}
						queueLabel.setIcon(qIcon);
						return;
					}
				}
			}
		}
		queue = false;
		//queuedItem = null;
		queueLabel.setIcon(null);
	}

	@Override
	public void run() {
		while (config.schedule) {
			System.out.println("Scheduler running...");
			long now = System.currentTimeMillis();
			if (config.startDate != null && config.endDate != null) {
				if (now > config.startDate.getTime()) {
					if (now < config.endDate.getTime())
						if (!queue)
							startQ();
				} else {
					System.out.println("Date error " + "Now: " + now
							+ " START: " + config.startDate.getTime()
							+ " END: " + config.endDate.getTime()
							+ (now > config.startDate.getTime()) + " "
							+ (now < config.endDate.getTime()));
				}
				if (config.endDate.getTime() < now) {
					stopQ();
					break;
				}
			} else {
				System.out.println("Dates are null");
			}
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
			}
		}
		System.out.println("Scheduler finished...");
	}

	public void intercept(Object obj, Object invoker) {
		if (obj instanceof XDMHttpClient) {
			XDMHttpClient client = (XDMHttpClient) obj;
			//addURL(client.url.toString(), client, client.requestHeader, client.cook, invoker);
			System.out.println("Returnd from addurl");
		}
	}

	void copyURL(String url) {
		if (clipboard == null) {
			clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		}
		clipboard.setContents(new StringSelection(url), null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void intercept(Object obj) {
		if (obj instanceof HashMap) {
			HashMap<String, String> arg = (HashMap<String, String>) obj;
			//addURL(arg.get("url"), null, null, arg.get("cookie"));
			return;
		}
		if (obj instanceof ArrayList) {
			ArrayList<String> flvList = (ArrayList<String>) obj;
			for (int i = 0; i < flvList.size(); i++) {
				try {
					URL url = new URL(flvList.get(i));
					MediaInfo minfo = new MediaInfo();
					minfo.name = XDMUtil.getFileName(url.getPath());
					minfo.url = url + "";
					minfo.size = "Unknown";
					minfo.type = "Unknown";
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			//if (mw == null) {
			//	mw = new MediaGrabberWindow(mmodel, this);
			//}
			//mw.setLocationRelativeTo(this);
			//mw.setVisible(true);
			return;
		}
		if (obj != null) {
			if (obj.toString().length() > 0);
				//addURL(obj.toString(), null, null, null);
		}
	}

	void showTrayMessage() {
		try {
			if (tray != null) {
				trayIcon.displayMessage("Video Captured",
						"A FLV Video has been captured",
						TrayIcon.MessageType.INFO);
			}
		} catch (Throwable e) {
			// TODO: handle exception
		}
	}


	void showBrowserStatus() {
		if (XDMProxyServer.browsers.size() > 0) {
			String bstr = "Monitoring browser: ";
			Iterator<String> it = XDMProxyServer.browsers.iterator();
			while (it.hasNext()) {
				bstr += " " + it.next();
			}
			JOptionPane.showMessageDialog(frame, bstr);
		} else {
			JOptionPane
					.showMessageDialog(
							frame,
							"XDM could not detect any network activity performed by your browser.\n"
									+ "To capture downloads you must configure your browser.\n"
									+ "[Goto help->Advanced Browser Integration]\n"
									+ "If you already configured it then try restarting the browser again");
		}
	}
	
	static boolean mod_xdm = false;
	static boolean first_run = false;

	public static boolean attachProxy(boolean refresh) {
		File exe = new File(tempdir, "xdm_net_helper.exe");
		try {
			File tmp = new File(tempdir, "xdm_win_proxy_attach");
			InputStream in = Main.class.getResourceAsStream("/resource/xdm");
			OutputStream out = new FileOutputStream(tmp);
			byte buf[] = new byte[8192];
			while (true) {
				int x = in.read(buf);
				if (x == -1)
					break;
				out.write(buf, 0, x);
			}
			out.close();
			tmp.renameTo(exe);
			List<String> cmds = new ArrayList<String>();
			cmds.add(exe.getAbsolutePath());
			if (!refresh) {
				cmds.add("http=http://localhost:" + config.port);
			}
			ProcessBuilder pb = new ProcessBuilder(cmds);
			pb.directory(new File(tempdir));
			Process proc = pb.start();
			proc.waitFor();
			if (proc.exitValue() != 0) {
				throw new Exception("Return code!=0" + " : " + proc.exitValue());
			}
		} catch (Exception e) {
			e.printStackTrace();
			exe.delete();
			return false;
		}
		System.out.println("File Deleted: " + exe.delete());
		return true;
	}

	

	
	
	

	HelpDialog getHTMLViwer() {
		HashMap<String, URL> map = new HashMap<String, URL>();
		map.put("Browser Integration", getClass().getResource(
				"/help/browser_integration.html"));
		map.put("Capturing Videos", getClass().getResource(
				"/help/video_download.html"));
		map.put("Refresh Broken Downloads", getClass().getResource(
				"/help/refresh_link.html"));
		HelpDialog hlp = new HelpDialog();
		hlp.addPages(map);
		return hlp;
	}

	@Override
	public void download(List<BatchItem> list, boolean startQ) {
		for (int i = 0; i < list.size(); i++) {
			BatchItem item = list.get(i);
			DownloadListItem ditem = new DownloadListItem();
			ditem.url = item.url;
			ditem.saveto = item.dir;
			ditem.filename = item.fileName;
			ditem.dateadded = new Date().toString();
			ditem.lasttry = new Date().toString();
			ditem.q = true;
			String user = item.user;
			String pass = item.pass;
			HashMap<String, String> extra = null;
			if (!(user == null || pass == null)) {
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
			}
			ditem.extra = extra;
			ditem.icon = IconUtil.getIcon(XDMUtil.findCategory(ditem.filename));
			Main.list.add(ditem);
		}
		if (startQ)
			startQ();
	}

	public void initBatchDownload(List<String> list, String user, String pass) {
		System.out.println("Batch");
		if (list == null || list.size() < 1) {
			return;
		}
		if (batchDlg == null) {
			batchDlg = new BatchDownloadDlg();
		}
		batchDlg.setLocationRelativeTo(null);
		List<BatchItem> blist = new ArrayList<BatchItem>();
		for (int i = 0; i < list.size(); i++) {
			BatchItem item = new BatchItem();
			item.url = list.get(i);
			item.fileName = XDMUtil.getFileName(item.url);
			item.user = user;
			item.pass = pass;
			blist.add(item);
			System.out.println(item.url);
		}
		batchDlg.showDialog(blist, config.destdir, this);
	}

	public void mediaCaptured(ArrayList<String> list) {
		if (list == null || list.size() < 1) {
			return;
		}
		for (int i = 0; i < list.size(); i++) {
			MediaInfo info = new MediaInfo();
			String yt = list.get(i);
			info.url = yt;
			info.name = XDMUtil.getFileName(info.url);
		}
		//if (mw == null) {
		//	mw = new MediaGrabberWindow(mmodel, this);
		//}
		//mw.setLocationRelativeTo(this);
		//mw.setVisible(true);
	}

	public static void main(String[] args) {
		// System.out.println(System.getProperty("java.home"));
		// System.out.println(System.getProperty("java.class.path"));
		File configFile = new File(appdir, ".xdmconf");
		first_run = !configFile.exists();
		config = XDMConfig.load(configFile);
		if (config.tcpBuf <= 8192) {
			config.tcpBuf = 8192;
		}
		showInfo = (!configFile.exists());
		XDMProxyServer server = new XDMProxyServer(null, config, null, null, null);
		if (!server.init()) {
			JOptionPane.showMessageDialog(frame,"Advanced Browser Integration Module could not be started.");
			return;
		} else {
			mod_xdm = true;
		}
		/*
		 * if (config.port != port) { showInfo = true; JOptionPane
		 * .showMessageDialog( null,
		 * "XDM Module is running on an alternate port\n" +
		 * "Advanced Browser Integration and Firefox Integration may not work");
		 * }
		 */
		DownloadList list = new DownloadList(appdir);
		IDownloadListener l = list;
		/*
		setConfig(config);
		w.mmodel = new MediaTableModel(w);
		w.setList(list, l);
		if (!min)
			w.setVisible(true);
		w.showInfo = showInfo;*/
		Authenticator a = Authenticator.getInstance();
		a.load(new File(appdir, "sites.conf"));
		startServer();
		System.out.println();
		System.out.println(args.length);
		new Thread() {
			@Override
			public void run() {
				String path = getJarPath();
				// first_run = true;
				if (first_run) {
					// boolean win = File.separatorChar == '\\';
					// if (win) {
					// w.eanableAutoStartWin();
					// }
					config.jarPath = path;
					//w.showBrowserIntegrationDlg();
					//w.runScriptWin();
					if (File.separatorChar == '\\') {
						System.out.println("Taking Main Network backup...");
						File bak = new File(System.getProperty("user.home"),
								"xdm-main-reg-bak.reg");
						if (RegUtil.takeBackup(bak)) {
							System.out.println("Main Backup successfull");
						} else {
							System.out.println("Main Backup Failed");
						}
					}
				} else {
					String jarPath = path;
					System.out.println("Old path: " + config.jarPath
							+ " Current Path: " + jarPath);
					config.jarPath = jarPath;
				}
				if (File.separatorChar == '\\') {
					//w.createWinLink(path);
					System.out.println("Taking Network backup...");
					File bak = new File(System.getProperty("user.home"), "reg-bak.reg");
					if (RegUtil.takeBackup(bak)) {
						System.out.println("Backup successfull");
						if (config.attachProxy) {
							System.out.println("Attaching Proxy...");
							//w.proxyAttached = w.attachProxy(false);
							//config.attachProxy = w.proxyAttached;
						}// w.runWPAC(true);
					} else {
						System.out.println("Backup failed");
					}
				} else {
					//w.createLinuxLink(path, false);
				}
			}
		}.start();
		icon = getIcon("icon.png");
		createTrayIcon();
		//w.server.start();
	}
	
	static void startServer(){
		/*server.observer = this;
		server.intercepter = this;
		//server.model = this.model;
		this.server = server;
		this.httpModel = new HttpTableModel();
		this.server.cl = this.httpModel;*/
	}
	
	static SystemTrayMenu trayPop;
	static boolean showInfo;
	static SystemTray tray;
	static TrayIcon trayIcon;
	
	static void createTrayIcon() {
		if (SystemTray.isSupported()) {
			tray = SystemTray.getSystemTray();
			trayIcon = new TrayIcon(icon.getImage(), "Xtreme Download Manager");
			trayIcon.setImageAutoSize(true);
			trayPop = new SystemTrayMenu();
			trayIcon.setPopupMenu(trayPop);
			try {
				tray.add(trayIcon);
			} catch (AWTException e) {
				e.printStackTrace();
			}
		}
	}
	
	JMenuBar bar;
	void createMenu() {
		
	}
}

class SystemTrayMenu extends PopupMenu implements ActionListener{
	private static final long serialVersionUID = 1L;
	boolean scheduleOn = false;
	final HashMap<String, MenuItem> menuMap = new HashMap<String, MenuItem>();
	final HashMap<String, MenuItem> downloadMap = new HashMap<String, MenuItem>();

	SystemTrayMenu(){
		addSeparator();
		menuItem("Add URL");
		menuItem("YouTube Downloader");
		
		addSeparator();
		// Options
		Menu optionsMenu = new Menu("Options");
        subMenuItem("Start Queue", optionsMenu);
        subMenuItem("Stop Queue", optionsMenu); menuMap.get("Stop Queue").setEnabled(false);
        subMenuItem("Make Shortcut", optionsMenu);
        subMenuItem("Auto Configure", optionsMenu);
        subMenuItem("Manual Configure", optionsMenu);
        CheckboxMenuItem cb1 = new CheckboxMenuItem("Auto Start on Startup");
        CheckboxMenuItem cb2 = new CheckboxMenuItem("Auto Start Scheduler");
        optionsMenu.add(cb1);
        optionsMenu.add(cb2);
		add(optionsMenu);
		// Integration
		Menu IntegrationMenu = new Menu("Integration");
		subMenuItem("Browser Integration", IntegrationMenu);
		subMenuItem("Capturing videos", IntegrationMenu);
		subMenuItem("Capturing downloads", IntegrationMenu);
		subMenuItem("Test Browser", IntegrationMenu);
		add(IntegrationMenu);
		// Final
		addSeparator();
		menuItem("About");
		menuItem("Exit");
	}
	
	void menuItem(String text){
		MenuItem item = new MenuItem(text);
		menuMap.put(text, item);
		item.addActionListener(this);
		add(item);
	}
	
	void subMenuItem(String text, Menu menu){
		MenuItem item = new MenuItem(text);
		menuMap.put(text, item);
		item.addActionListener(this);
		menu.add(item);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String str = e.getActionCommand();
		if (str.equals("Add URL")) {
			//addURL(null, null, null, null);
		}
		if (str.equals("Download Later")) {
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
			item.icon = IconUtil.getIcon(XDMUtil.findCategory(item.filename));
			item.saveto = dfi.getDir();// destdir;
			item.cookies = dfi.cookies;
		}
		if (str.equals("Download Now")) {
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
		if (str.equals("Manual Configure")) {
			ConfigWindow cwin = new ConfigWindow(Main.config, Main.frame);
			cwin.setVisible(true);
		}
		if (str.equals("Start Queue")) {
			menuMap.get("Start Queue").setEnabled(false);
			menuMap.get("Stop Queue").setEnabled(true);
			//startQ();
		}
		if (str.equals("Stop Queue")) {
			menuMap.get("Start Queue").setEnabled(true);
			menuMap.get("Stop Queue").setEnabled(false);
			//stopQ();
		}
		
		if(str.equals("Auto Start on Startup"))
			setAutoStart(((CheckboxMenuItem)menuMap.get("Auto Start on Startup")).getState());
		
		if (str.equals("Make Shortcut"))
			if (File.separatorChar == '\\') 
				winCreateLink();
			 else 
				linuxCreateLink();

		if (str.equals("About")) {
			AboutDlg abtDlg = new AboutDlg();
			abtDlg.setVisible(true);
		}
		if (str.equals("Exit"))
			System.exit(0);
		
		if (str.equals("Test Browser")) {
			JTextField url = new JTextField("http://localhost:" + Main.config.port + "/test");
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
		
		if (str.equals("Advanced Browser Integration")) {
			showInfo();
		}
		if (str.equals("Firefox Integration")) {
			DefaultListModel ffmodel = new DefaultListModel();
			ffmodel.add(0, "http://localhost:" + Main.config.port + "/xdmff.xpi");
			FFIntDlg ffdlg = new FFIntDlg(ffmodel, Main.frame);
			ffdlg.setVisible(true);
		}
		if (str.equals("Browser Integration")) {
			//showBrowserIntegrationDlg();
		}
		if (str.equals("Capturing downloads")) {
		}
		if (str.equals("Capturing videos")) {
		}
		if (str.equals("Auto Configure")) {
			showInfo();
		}
		if (str.equals("Run XDM on startup")) {
			JCheckBox chk = (JCheckBox) e.getSource();
			//setAutoStart(chk.isSelected());
			System.out.println(chk.isSelected());
		}
	}
	
	void addURL(String url, XDMHttpClient client, HashMap<String, String> map, String cookies) {
		addURL(url, client, map, cookies, null);
	}
	
	void addURL(String url, XDMHttpClient client, HashMap<String, String> map,
			String cookies, Object invoker) {
		System.out.println("Called");
		DownloadFileInfoDialog dlg = new DownloadFileInfoDialog(this, this, Main.config);
		dlg.setURL(url);
		dlg.setDir(Main.config.destdir);
		dlg.client = client;
		dlg.extra = map;
		dlg.cookies = cookies;
		dlg.setAlwaysOnTop(true);
		dlg.showDlg();
		if (Main.config.allowbrowser) {
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
	
	
	void showInfo() {
		if (File.separatorChar == '\\') {
			JCheckBox enableCapture = new JCheckBox("Capture Downloads", Main.proxyAttached);
			File bak = new File(System.getProperty("user.home"), "reg-bak.reg");
			File main_bak = new File(System.getProperty("user.home"), "xdm-main-reg-bak.reg");
			// restore.setEnabled(bak.exists());
			if (JOptionPane.showOptionDialog(null,
					new Object[] { enableCapture }, "Capture Downloads...",
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.INFORMATION_MESSAGE, null, null, null) == JOptionPane.OK_OPTION) {
				if (enableCapture.isSelected()) {
					Main.config.attachProxy = true;
					if (Main.proxyAttached) {
						return;
					} else {
						if (!RegUtil.takeBackup(bak)) {
							Main.config.attachProxy = false;
							Main.proxyAttached = false;
							JOptionPane
									.showMessageDialog(null,
											"Auto configuration Failed because Network backup failed.\nPlease try manual configuration");
							return;
						}
						if (!Main.attachProxy(false)) {
							Main.config.attachProxy = false;
							Main.proxyAttached = false;
							JOptionPane
									.showMessageDialog(null,
											"Auto configuration Failed.\nPlease try manual configuration");
							return;
						} else {
							Main.config.attachProxy = true;
							Main.proxyAttached = true;
						}
					}
				} else {
					boolean restore_main_bak = false;
					if (main_bak.exists()) {
						restore_main_bak = JOptionPane.showConfirmDialog(null,
								"Restore previous network settings?\nSettings will be restored to date: "
										+ new Date(main_bak.lastModified())) == JOptionPane.YES_OPTION;
					}
					if (!restore_main_bak) {
						RegUtil.restore(bak);
						Main.proxyAttached = false;
						Main.config.attachProxy = false;
						Main.attachProxy(true);
					} else {
						RegUtil.restore(main_bak);
						Main.proxyAttached = false;
						Main.config.attachProxy = false;
						Main.attachProxy(true);
					}

				}
			}
		} else {
			JOptionPane.showMessageDialog(Main.frame,
					"You have to manually configure your network for this OS: "
							+ System.getProperty("os.name"));
			try {
				if (Desktop.isDesktopSupported()) {
					Desktop.getDesktop().browse(
							new URI("http://localhost:" + Main.config.port
									+ "/help/index.html"));
				} else {
					JOptionPane.showMessageDialog(Main.frame,
							"Type this address in your browser:\n"
									+ "http://localhost:" + Main.config.port
									+ "/help/index.html");
				}
			} catch (Exception err) {
				err.printStackTrace();
			}
		}
	}
	
	

	static private void createProcess(String cmd) {
		try {
			Runtime.getRuntime().exec(cmd);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void virusScan(String antivirTxt) {
		createProcess(antivirTxt);
	}

	private void hungUp(String hungUpTxt) {
		createProcess(hungUpTxt);
	}

	private void executeCommand(String cmdTxt) {
		createProcess(cmdTxt);
	}
	
	/*
	 *  Shutdown on Download Complete
	 */
	void shutdownComputer(String cmd) {
		ShutdownDlg sdlg = new ShutdownDlg();
		sdlg.start(cmd);
	}
	
	/*
	 *  Auto Start Features
	 */
	void setAutoStart(boolean on) {
		boolean win = File.separatorChar == '\\';
		if (on) {
			if (win)
				eanableAutoStartWin();
			else {
				if (!enableAutoStartLinux()) {
					JOptionPane.showMessageDialog(Main.frame,
							"Please Manually Add XDM at startup");
				}
			}
		} else {
			if (win)
				disableAutoStartWin();
			else {
				disableAutoStartLinux();
			}
		}
	}

	boolean disableAutoStartLinux() {
		String autoStartDirs[] = { ".config/autostart", ".kde/Autostart",
				".kde/autostart", ".config/Autostart", ".kde4/Autostart" };
		File home = new File(System.getProperty("user.home"));
		File autoStartDir = null;
		for (int i = 0; i < autoStartDirs.length; i++) {
			autoStartDir = new File(home, autoStartDirs[i]);
			if (!autoStartDir.exists()) {
				autoStartDir = null;
			} else {
				// createLinuxLink(autoStartDir.getAbsolutePath());
				File file = new File(autoStartDir, "xdman.desktop");
				if (file.exists()) {
					if (file.delete()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	boolean enableAutoStartLinux() {
		String autoStartDirs[] = { ".config/autostart", ".kde/Autostart",
				".kde/autostart", ".config/Autostart", ".kde4/Autostart" };
		File home = new File(System.getProperty("user.home"));
		File autoStartDir = null;
		for (int i = 0; i < autoStartDirs.length; i++) {
			autoStartDir = new File(home, autoStartDirs[i]);
			if (!autoStartDir.exists()) {
				autoStartDir = null;
			} else {
				createLinuxLink(autoStartDir.getAbsolutePath(), true);
				return true;
			}
		}
		return false;
	}

	boolean eanableAutoStartWin() {
		try {
			System.out.println("Adding startup entry");
			String jarFolder = Main.getJarPath();
			String jarfile = new File(jarFolder, "xdman.jar").getAbsolutePath();
			File file = new File(System.getProperty("user.home"), "startup.vbs");
			OutputStream out = new FileOutputStream(file);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					Main.class.getResourceAsStream("/script/startup_add.txt")));
			while (true) {
				String ln = in.readLine();
				if (ln == null)
					break;
				String l2 = ln.replace("<JAR_PATH>", jarfile);
				String l3 = l2.replace("<ICON_LOCATION>", new File(jarFolder,
						"icon.ico").getAbsolutePath())
						+ "\r\n";
				out.write(l3.getBytes());
			}
			out.close();
			in.close();
			createProcess("WScript.exe \"" + file.getAbsolutePath() + "\"");
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
		return true;
	}
	
	boolean disableAutoStartWin() {
		try {
			InputStream in = getClass().getResourceAsStream("/script/startup_del.txt");
			File remScript = new File(System.getProperty("user.home"), "rem.vbs");
			OutputStream out = new FileOutputStream(remScript);
			byte b[] = new byte[1024];
			int x = in.read(b);
			out.write(b, 0, x);
			out.close();
			Runtime.getRuntime().exec("wscript \"" + remScript.getAbsolutePath() + "\"");
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/*
	 *  Making Shortcut
	 */
	
	JFileChooser fc;
	void winCreateLink() {
		try {
			if (fc == null) {
				fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			}
			if (fc.showSaveDialog(Main.frame) == JFileChooser.APPROVE_OPTION) {
				String desktopFile = fc.getSelectedFile().getAbsolutePath();
				createWinLink(desktopFile);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	void createWinLink(String targetFile) {
		try {
			System.out.println("Creating shortcut at: " + targetFile);
			String jarFolder = Main.getJarPath();
			String jarfile = new File(jarFolder, "xdman.jar").getAbsolutePath();
			File file = new File(System.getProperty("user.home"), "link.vbs");
			OutputStream out = new FileOutputStream(file);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					Main.class.getResourceAsStream("/script/link.txt")));
			while (true) {
				String ln = in.readLine();
				if (ln == null)
					break;
				String l1 = ln.replace("<TARGET_LOCATION>", targetFile);
				String l2 = l1.replace("<JAR_PATH>", jarfile);
				String l3 = l2.replace("<ICON_LOCATION>", new File(jarFolder,
						"icon.ico").getAbsolutePath())
						+ "\r\n";
				out.write(l3.getBytes());
			}
			out.close();
			in.close();
			createProcess("WScript.exe \"" + file.getAbsolutePath() + "\"");
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	void linuxCreateLink() {
		try {
			if (fc == null) {
				fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			}
			if (fc.showSaveDialog(Main.frame) == JFileChooser.APPROVE_OPTION) {
				String desktopFile = fc.getSelectedFile().getAbsolutePath();
				createLinuxLink(desktopFile, false);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	void createLinuxLink(String target, boolean min) {
		try {
			StringBuffer buf = new StringBuffer();
			buf.append("[Desktop Entry]\n");
			buf.append("Encoding=UTF-8\n");
			buf.append("Version=1.0\n");
			buf.append("Type=Application\n");
			buf.append("Terminal=false\n");
			String jarPath = Main.getJarPath();
			buf.append("Exec=java -jar '"
					+ new File(jarPath, "xdman.jar").getAbsolutePath() + "'"
					+ (min ? " -m" : "") + "\n");
			buf.append("Name=Xtreme Download Manager\n");
			buf.append("Icon="
					+ new File(jarPath, "icon.png").getAbsolutePath() + "\n");
			File desktop = new File(target, "xdman.desktop");
			OutputStream out = new FileOutputStream(desktop);
			out.write(buf.toString().getBytes());
			out.close();
			desktop.setExecutable(true);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(Main.frame, "Error creating shortcut");
		}
	}
	
	void runScriptWin() {
		try {
			System.out.println("Adding Desktop entry");
			String jarFolder = Main.getJarPath();
			String jarfile = new File(jarFolder, "xdman.jar").getAbsolutePath();
			File file = new File(System.getProperty("user.home"),
					"desktop_shortcut.vbs");
			OutputStream out = new FileOutputStream(file);
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
							Main.class
									.getResourceAsStream("/script/desktop_shortcut.txt")));
			while (true) {
				String ln = in.readLine();
				if (ln == null)
					break;
				String l2 = ln.replace("<JAR_PATH>", jarfile);
				String l3 = l2.replace("<ICON_LOCATION>", new File(jarFolder,
						"icon.ico").getAbsolutePath())
						+ "\r\n";
				out.write(l3.getBytes());
			}
			out.close();
			in.close();
			createProcess("WScript.exe \"" + file.getAbsolutePath() + "\"");
		} catch (Exception e) {
			System.out.println(e);
		}
		try {
			System.out.println("Adding Prgrams entry");
			String jarFolder = Main.getJarPath();
			String jarfile = new File(jarFolder, "xdman.jar").getAbsolutePath();
			File file = new File(System.getProperty("user.home"), "programs_shortcut.vbs");
			OutputStream out = new FileOutputStream(file);
			BufferedReader in = new BufferedReader(
				new InputStreamReader(Main.class.getResourceAsStream("/script/programs_shortcut.txt")));
			while (true) {
				String ln = in.readLine();
				if (ln == null)
					break;
				String l2 = ln.replace("<JAR_PATH>", jarfile);
				String l3 = l2.replace("<ICON_LOCATION>", new File(jarFolder,
						"icon.ico").getAbsolutePath())
						+ "\r\n";
				out.write(l3.getBytes());
			}
			out.close();
			in.close();
			createProcess("WScript.exe \"" + file.getAbsolutePath() + "\"");
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
