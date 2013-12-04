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
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
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

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;

import org.sdg.xdman.core.Config;
import org.sdg.xdman.core.Dialog;
import org.sdg.xdman.core.common.Authenticator;
import org.sdg.xdman.core.common.ConnectionManager;
import org.sdg.xdman.core.common.IXDMConstants;
import org.sdg.xdman.core.common.UnsupportedProtocolException;
import org.sdg.xdman.core.common.XDMConfig;
import org.sdg.xdman.core.common.http.XDMHttpClient;
import org.sdg.xdman.proxy.RequestIntercepter;
import org.sdg.xdman.proxy.XDMProxyServer;
import org.sdg.xdman.util.XDMUtil;
import org.sdg.xdman.util.win.RegUtil;

interface IXDMQueue {
	public void startQ();
	public void stopQ();
	public void next();
}

public class Main  implements Observer, IXDMQueue, Runnable, RequestIntercepter, YoutubeMediaListener {
	public static JFrame frame = new JFrame();
	XDMProxyServer server;
	public static DownloadList downloadList = null;
	static IDownloadListener dlistener;
	
	boolean stop = false;
	boolean queue;
	public static ImageIcon icon;
	Clipboard clipboard;
	boolean haltPending = false;
	// Dialogs
	HelpDialog view;
	HttpTableModel httpModel;
	HttpMonitorDlg httpDlg;
	
	public static boolean proxyAttached = false;
	JLabel queueLabel;
	private DownloadListItem queuedItem;

	public Main(DownloadList list) {
		icon = getIcon("icon.png");
		downloadList = list;
		dlistener = list;
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				shutdownHook();
			}
		});
		// t = new Thread(this);
		// t.start();
		Config.config.addObserver(this);
		if (Config.config.schedule) {
			if (Config.scheduler == null) {
				Config.scheduler = new Thread(this);
				Config.scheduler.start();
				return;
			}
			if (!Config.scheduler.isAlive()) {
				Config.scheduler = new Thread(this);
				Config.scheduler.start();
			}
		}
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
		Config.config.save();
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

	@SuppressWarnings("unchecked")
	@Override
	public void update(Observable o, Object obj) {
		if (o == Config.config) {
			System.out.println("Config updated...");
			if (Config.config.tempdir != null)
				if (Config.config.tempdir.length() > 0) {
					if (new File(Config.config.tempdir).exists()) {
						Config.tempdir = Config.config.tempdir;
					}
				}
			System.out.println("M_DESTDIR: " + Config.destdir + "\nTEMpDIR: "
					+ Config.tempdir);
			System.out.println("C_DESTDIR: " + Config.config.destdir + "\nTEMpDIR: "
					+ Config.config.tempdir);
			if (Config.config.destdir != null)
				if (Config.config.destdir.length() > 0) {
					if (new File(Config.config.destdir).exists()) {
						Config.destdir = Config.config.destdir;
					}
				}

			System.out.println("DESTDIR: " + Config.destdir + "\nTEMpDIR: " + Config.tempdir);
			if (Config.config.schedule) {
				if (Config.scheduler == null) {
					Config.scheduler = new Thread(this);
					Config.scheduler.start();
					return;
				}
				if (!Config.scheduler.isAlive()) {
					Config.scheduler = new Thread(this);
					Config.scheduler.start();
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
				if (queue) next();
				else if (Config.config.showDownloadCompleteDlg) Dialog.downloadComplete(item);
				if (Config.config.halt) {
					if (Config.config.haltTxt == null || Config.config.haltTxt.length() < 1)
						return;
					else
						haltPending = true;
				}
				File file = new File(item.saveto, item.filename);
				if (Config.config.executeCmd) {
					//executeCommand(config.cmdTxt + " " + file);
				}
				if (Config.config.hungUp) {
					//hungUp(config.hungUpTxt);
				}
				if (Config.config.antivir) {
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
		for (int i = 0; i < downloadList.list.size(); i++) {
			DownloadListItem item = downloadList.list.get(i);
			if (item.q) {
				if (item.mgr == null) {
					if (item.state != IXDMConstants.COMPLETE) {
						if (item.tempdir.equals("")) {
							SystemTrayMenu.startDownload(item.url, item.filename, item.saveto,
									Config.tempdir, item, null, item.extra,
									item.cookies, false);
						} else {
							ConnectionManager c = new ConnectionManager(
									item.url, item.filename, item.saveto,
									item.tempdir, item.extra, Config.config);
							c.setTimeOut(Config.config.timeout);
							c.setMaxConn(Config.config.maxConn);
							//item.setCallback(c, model, dlistener);
							item.addObserver(this);
							c.resume();
						}
						queuedItem = item;
						return;
					}
				}
			}
		}
		queue = false;
		queuedItem = null;
		queueLabel.setIcon(null);
	}

	@Override
	public void run() {
		while (Config.config.schedule) {
			System.out.println("Scheduler running...");
			long now = System.currentTimeMillis();
			if (Config.config.startDate != null && Config.config.endDate != null) {
				if (now > Config.config.startDate.getTime()) {
					if (now < Config.config.endDate.getTime())
						if (!queue)
							startQ();
				} else {
					System.out.println("Date error " + "Now: " + now
							+ " START: " + Config.config.startDate.getTime()
							+ " END: " + Config.config.endDate.getTime()
							+ (now > Config.config.startDate.getTime()) + " "
							+ (now < Config.config.endDate.getTime()));
				}
				if (Config.config.endDate.getTime() < now) {
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
	

	public static boolean attachProxy(boolean refresh) {
		File exe = new File(Config.tempdir, "xdm_net_helper.exe");
		try {
			File tmp = new File(Config.tempdir, "xdm_win_proxy_attach");
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
				cmds.add("http=http://localhost:" + Config.config.port);
			}
			ProcessBuilder pb = new ProcessBuilder(cmds);
			pb.directory(new File(Config.tempdir));
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
		Config.init();
		Main main = new Main(null);
		XDMProxyServer server = new XDMProxyServer(null, Config.config, null, null, null);
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
		DownloadList list = new DownloadList(Config.appdir);
		IDownloadListener l = list;
		/*
		w.showInfo = showInfo;*/
		Authenticator a = Authenticator.getInstance();
		a.load(new File(Config.appdir, "sites.conf"));
		startServer();
		System.out.println();
		System.out.println(args.length);
		new Thread() {
			@Override
			public void run() {
				String path = getJarPath();
				// first_run = true;
				if (Config.first_run) {
					// boolean win = File.separatorChar == '\\';
					// if (win) {
					// w.eanableAutoStartWin();
					// }
					Config.config.jarPath = path;
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
					System.out.println("Old path: " + Config.config.jarPath
							+ " Current Path: " + jarPath);
					Config.config.jarPath = jarPath;
				}
				if (File.separatorChar == '\\') {
					//w.createWinLink(path);
					System.out.println("Taking Network backup...");
					File bak = new File(System.getProperty("user.home"), "reg-bak.reg");
					if (RegUtil.takeBackup(bak)) {
						System.out.println("Backup successfull");
						if (Config.config.attachProxy) {
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
		if (str.equals("Download Later")) Dialog.downloadLater(e);
		if (str.equals("Download Now")) Dialog.downloadNow(e);
		
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
		
		if (str.equals("Test Browser")) Dialog.testBrowser();
		//------------------------------------------------------------------------------
		if(str.equals("Auto Start on Startup")) Config.setAutoStart(((CheckboxMenuItem)
				menuMap.get("Auto Start on Startup")).getState());
		if (str.equals("Make Shortcut")) Config.makeLink();
		if (str.equals("Manual Configure")) Dialog.config();
		if (str.equals("Firefox Integration")) Dialog.fireFoxIntegration();
		if (str.equals("Browser Integration")) Dialog.browserIntegration();
		if (str.equals("Advanced Browser Integration")) showInfo();
		if (str.equals("Auto Configure")) showInfo();
		if (str.equals("Capturing downloads"));
		if (str.equals("Capturing videos"));
		//------------------------------------------------------------------------------
		if (str.equals("About")) Dialog.about();
		if (str.equals("Exit")) System.exit(0);
	}
	
	void addURL(String url, XDMHttpClient client, HashMap<String, String> map, String cookies) {
		addURL(url, client, map, cookies, null);
	}
	
	void addURL(String url, XDMHttpClient client, HashMap<String, String> map, String cookies, Object invoker) {
		System.out.println("Called");
		Dialog.downloadFileInfo(this, this, url, client, map, cookies, invoker);
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
		item.saveto = destdir;
		item.cookies = cookies;
		Main.downloadList.add(item);
		startDownload(url, fileName, destdir, tempdir, item, client,
				item.extra, cookies);
	}

	public static void startDownload(String url, String fileName, String destdir,
			String tempdir, DownloadListItem item, XDMHttpClient client,
			HashMap<String, String> extra, String cookies) {
		startDownload(url, fileName, destdir, tempdir, item, client, extra,
				cookies, true);
	}

	static void startDownload(String url, String fileName, String destdir,
			String tempdir, DownloadListItem item, XDMHttpClient client,
			HashMap<String, String> extra, String cookies, boolean fg) {
		ConnectionManager c = new ConnectionManager(url, fileName, destdir,
				tempdir, extra, Config.config);
		c.extra = extra;
		c.setTimeOut(Config.config.timeout);
		c.setMaxConn(Config.config.maxConn);
		if (Config.config.showDownloadPrgDlg && fg) Dialog.download(c, item);
		//item.setCallback(c, model, dlistener);
		//item.addObserver(this);
		Main.dlistener.downloadStateChanged();
		if (client == null) {
			try {
				c.start();
			} catch (UnsupportedProtocolException e) {
				JOptionPane.showMessageDialog(Main.frame, "Unsupported protocol");
			}
		} else {
			try {
				c.start(client);
			} catch (UnsupportedProtocolException e) {
				JOptionPane.showMessageDialog(Main.frame, "Unsupported protocol");
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
					Config.config.attachProxy = true;
					if (Main.proxyAttached) {
						return;
					} else {
						if (!RegUtil.takeBackup(bak)) {
							Config.config.attachProxy = false;
							Main.proxyAttached = false;
							JOptionPane
									.showMessageDialog(null,
											"Auto configuration Failed because Network backup failed.\nPlease try manual configuration");
							return;
						}
						if (!Main.attachProxy(false)) {
							Config.config.attachProxy = false;
							Main.proxyAttached = false;
							JOptionPane
									.showMessageDialog(null,
											"Auto configuration Failed.\nPlease try manual configuration");
							return;
						} else {
							Config.config.attachProxy = true;
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
						Config.config.attachProxy = false;
						Main.attachProxy(true);
					} else {
						RegUtil.restore(main_bak);
						Main.proxyAttached = false;
						Config.config.attachProxy = false;
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
							new URI("http://localhost:" + Config.config.port
									+ "/help/index.html"));
				} else {
					JOptionPane.showMessageDialog(Main.frame,
							"Type this address in your browser:\n"
									+ "http://localhost:" + Config.config.port
									+ "/help/index.html");
				}
			} catch (Exception err) {
				err.printStackTrace();
			}
		}
	}
}
