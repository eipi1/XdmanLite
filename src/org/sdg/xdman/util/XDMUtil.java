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


package org.sdg.xdman.util;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.sdg.xdman.core.common.IXDMConstants;

public class XDMUtil implements IXDMConstants {
	static final int MB = 1024 * 1024, KB = 1024;

	public static String getFormattedLength(double length) {
		if (length < 0)
			return "Unknown";
		if (length > MB) {
			return String.format("%.3f MB", (float) length / MB);
		} else if (length > KB) {
			return String.format("%.3f KB", (float) length / KB);
		} else {
			return String.format("%d Bytes", (int) length);
		}
	}

	public static String getETA(double length, float rate) {
		if (length == 0)
			return "0 second.";
		if (length < 1 || rate <= 0)
			return "Unknown";
		int sec = (int) (length / rate);
		return hms(sec);
	}

	static String hms(int sec) {
		int hrs = 0, min = 0;
		hrs = sec / 3600;
		min = (sec % 3600) / 60;
		sec = sec % 60;
		String str = "";
		if (hrs > 0)
			str += (hrs + " hour ");
		str += (min + " min ");
		str += (sec + " seconds ");
		return str;
	}

	public static boolean validateURL(String url) {
		try {
			new URL(url);
			if (url.startsWith("http") || url.startsWith("ftp://"))
				return true;
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	static String doc[] = { ".doc", ".docx", ".txt", ".pdf", ".rtf", ".xml",
			".c", ".cpp", ".java", ".cs", ".vb", ".html", ".htm", ".chm",
			".xls", ".xlsx", ".ppt", ".pptx" };
	static String cmp[] = { ".7z", ".zip", ".rar", ".gz", ".tgz", ".tbz2",
			".bz2", ".lzh", ".sit", ".z" };
	static String music[] = { ".mp3", ".wma", ".ogg", ".aiff", ".au", ".mid",
			".midi", ".mp2", ".mpa", ".wav", ".aac" };
	static String vid[] = { ".mpg", ".mpeg", ".avi", ".flv", ".asf", ".mov",
			".mpe", ".wmv", ".mkv", ".mp4", ".3gp", ".divx", ".vob", ".webm" };
	static String prog[] = { ".exe", ".msi", ".bin", ".sh", ".deb", ".cab",
			".cpio", ".dll", ".jar" };

	public static String findCategory(String filename) {
		String file = filename.toLowerCase();
		for (int i = 0; i < doc.length; i++) {
			if (file.endsWith(doc[i])) {
				return DOCUMENTS;
			}
		}
		for (int i = 0; i < cmp.length; i++) {
			if (file.endsWith(cmp[i])) {
				return COMPRESSED;
			}
		}
		for (int i = 0; i < music.length; i++) {
			if (file.endsWith(music[i])) {
				return MUSIC;
			}
		}
		for (int i = 0; i < prog.length; i++) {
			if (file.endsWith(prog[i])) {
				return PROGRAMS;
			}
		}
		for (int i = 0; i < vid.length; i++) {
			if (file.endsWith(vid[i])) {
				return VIDEO;
			}
		}
		return OTHER;
	}

	public static String getFileName2(String url) {
		String file = null;
		try {
			file = new File(new URI(url).getPath()).getName();
			System.out.println("File name: " + file);
		} catch (Exception e) {
		}
		if (file == null || file.length() < 1)
			file = "FILE";
		return file;
	}

	public static void open(File f) {
		char ch = File.separatorChar;
		if (ch == '\\') {
			openWindows(f);
		} else {
			try {
				Desktop.getDesktop().open(f);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static void openWindows(File f) {
		try {
			ProcessBuilder builder = new ProcessBuilder();
			List<String> lst = new ArrayList<String>();
			lst.add("rundll32");
			lst.add("url.dll,FileProtocolHandler");
			lst.add(f.getAbsolutePath());
			builder.command(lst);
			builder.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String getFileName(String uri) {
		try {
			if (uri == null)
				return "FILE";
			if (uri.equals("/") || uri.length() < 1) {
				return "FILE";
			}
			int x = uri.lastIndexOf("/");
			String path = uri;
			if (x > -1) {
				path = uri.substring(x);
			}
			int qindex = path.indexOf("?");
			if (qindex > -1) {
				path = path.substring(0, qindex);
			}
			path = decode(path);
			if (path.length() < 1)
				return "FILE";
			if (path.equals("/"))
				return "FILE";
			return path;
		} catch (Exception e) {
			return "FILE";
		}
	}

	public static String decode(String str) {
		char ch[] = str.toCharArray();
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < ch.length; i++) {
			if (ch[i] == '/' || ch[i] == '\\' || ch[i] == '"' || ch[i] == '?'
					|| ch[i] == '*' || ch[i] == '<' || ch[i] == '>'
					|| ch[i] == ':')
				continue;
			if (ch[i] == '%') {
				if (i + 2 < ch.length) {
					int c = Integer.parseInt(ch[i + 1] + "" + ch[i + 2], 16);
					buf.append((char) c);
					i += 2;
					continue;
				}
			}
			buf.append(ch[i]);
		}
		return buf.toString();
	}

	public static String nvl(Object o) {
		if (o == null)
			return "";
		return o.toString();
	}

	public static String createURL(String str) {
		try {
			new URL(str);
		} catch (MalformedURLException e) {
			return "http://" + str;
		}
		return null;
	}

	public static void main(String[] args) throws InterruptedException {
		System.out.println(hms(13547));
		String str = "http://sound27.mp3pk.com/indian/race2/[Songs.PK]%20Race%202%20-%2003%20-%20Lat%20Lag%20Gayee.mp3";// "http://url2.bollywoodmp3.se/[Songs.PK]%20Murder%203%20-%20Teri%20Jhuki%20Nazar%20-%20128Kbps.mp3";//
		// "http://sound27.mp3pk.com/indian/khiladi786/%5BSongs.PK%5D%20Khiladi%20786%20-%2008%20- %20Lonely%20%28Remix%29.mp3";//
		// "http://www.facebook.com/search/results.php?q=pinky+barua&init=quick&tas=search_preload&search_first_focus=1358401213399";//
		// "G:\\songs\\english\\x.mp3";
		System.out.println(str);
		System.out.println(decode(str) + "\n" + getFileName(str));
		System.out.println(getFileName(str));
		// open(null);
		// open(new File(str));
		// Thread.sleep(5000);
	}
}
