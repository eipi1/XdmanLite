package org.sdg.xdman.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Observer;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.sdg.xdman.core.common.XDMConfig;
import org.sdg.xdman.gui.Main;

public class Config {
	public static XDMConfig config;
	public static File configFile;
	public static String tempdir = System.getProperty("user.home");
	public static String destdir = Config.tempdir;
	public static String appdir = Config.tempdir;
	public static final String version = "Version: 3.01 Build 15 (June 09, 2013)";
	public static final String updateURL = "http://xdm.sourceforge.net/update.php";
	public static final String homeURL = "http://xdm.sourceforge.net/";
	public static Thread scheduler;
	
	public static boolean first_run = false;
	public static boolean showInfo;
	
	public static void init() {
		// System.out.println(System.getProperty("java.home"));
		// System.out.println(System.getProperty("java.class.path"));
		configFile = new File(Config.appdir, ".xdmconf");
		first_run = !configFile.exists();
		showInfo = (!configFile.exists());
		Config.config = XDMConfig.load(configFile);
		if (Config.config.tcpBuf <= 8192) Config.config.tcpBuf = 8192;
		System.out.println("Setting config");
		if (Config.config == null) {
			Config.config = new XDMConfig(new File(appdir, ".xdmconf"));
		}
		if (Config.config.tempdir == null || Config.config.tempdir.length() < 1) {
			Config.config.tempdir = Config.tempdir;
		}
		if (Config.config.destdir == null || Config.config.destdir.length() < 1) {
			Config.config.destdir = destdir;
		}
		System.out.println(Config.config);
		if (Config.config.tempdir != null)
			if (Config.config.tempdir.length() > 0) {
				if (new File(Config.config.tempdir).exists()) {
					Config.tempdir = Config.config.tempdir;
				}
			}
		if (Config.config.destdir != null)
			if (Config.config.destdir.length() > 0) {
				if (new File(Config.config.destdir).exists()) {
					destdir = Config.config.destdir;
				}
			}
	}
	
	/*
	 *  Auto Start Features
	 */
	public static void setAutoStart(boolean on) {
		boolean win = File.separatorChar == '\\';
		if (on) {
			if (win)
				eanableAutoStartWin();
			else {
				if (!enableAutoStartLinux()) {
					JOptionPane.showMessageDialog(Main.frame, "Please Manually Add XDM at startup");
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

	private static boolean disableAutoStartLinux() {
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

	private static boolean enableAutoStartLinux() {
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

	private static boolean eanableAutoStartWin() {
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
			Core.createProcess("WScript.exe \"" + file.getAbsolutePath() + "\"");
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
		return true;
	}
	
	private static boolean disableAutoStartWin() {
		try {
			InputStream in = Config.class.getResourceAsStream("/script/startup_del.txt");
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
	
	public static void makeLink(){
		if (File.separatorChar == '\\') 
			winCreateLink();
		 else 
			 linuxCreateLink();
	}
	
	private static void winCreateLink() {
		try {
			JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (fc.showSaveDialog(Main.frame) == JFileChooser.APPROVE_OPTION) {
				String desktopFile = fc.getSelectedFile().getAbsolutePath();
				createWinLink(desktopFile);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void createWinLink(String targetFile) {
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
			Core.createProcess("WScript.exe \"" + file.getAbsolutePath() + "\"");
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	private static void linuxCreateLink() {
		try {
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (fc.showSaveDialog(Main.frame) == JFileChooser.APPROVE_OPTION) {
					String desktopFile = fc.getSelectedFile().getAbsolutePath();
					createLinuxLink(desktopFile, false);
				}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void createLinuxLink(String target, boolean min) {
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
			Core.createProcess("WScript.exe \"" + file.getAbsolutePath() + "\"");
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
			Core.createProcess("WScript.exe \"" + file.getAbsolutePath() + "\"");
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	/*
	 *  Shutdown on Download Complete
	 */
	public static void shutdownComputer(String cmd) {
		ShutdownDlg sdlg = new ShutdownDlg();
		sdlg.start(cmd);
	}

}
