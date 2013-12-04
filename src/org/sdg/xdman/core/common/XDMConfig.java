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


package org.sdg.xdman.core.common;

import java.io.*;
import java.util.Date;
import java.util.Observable;

public class XDMConfig extends Observable implements Serializable {

	private static final long serialVersionUID = 3162043915747863361L;
	File file;
	public String jarPath;
	public boolean useHttpProxy, useFtpProxy, useHttpsProxy;
	public String httpProxyHost, httpsProxyHost, ftpProxyHost;
	public int httpProxyPort, httpsProxyPort, ftpProxyPort;
	public String httpUser, httpPass, httpsUser, httpsPass, ftpUser, ftpPass;

	// private static final long serialVersionUID = -4029431942579013439L;
	public boolean showDownloadPrgDlg = true;
	public boolean showDownloadCompleteDlg = true;
	public boolean showDownloadBox = false;

	public final int PROMPT = 2, AUTO_RENAME = 0, RESUME = 1;

	public int duplicateLinkAction = PROMPT;

	public int maxConn = 8, timeout = 60;

	public String destdir, tempdir;

	public boolean executeCmd = false, hungUp = false, halt = false,
			antivir = false;

	public String cmdTxt, hungUpTxt, haltTxt, antivirTxt;

	public int antidrop = 0, hungdrop = 0, haltdrop = 0;

	public final String defaultFileTypes[] = { "3GP", "7Z", "AAC", "ACE",
			"AIF", "ARJ", "ASF", "AVI", "BIN", "BZ2", "EXE", "GZ", "GZIP",
			"IMG", "ISO", "LZH", "M4A", "M4V", "MOV", "MP3", "MP4", "MPA",
			"MPE", "MPEG", "MPG", "MSI", "MSU", "OGG", "PDF", "PLJ", "PPS",
			"PPT", "QT", "RA", "RAR", "RM", "SEA", "SIT", "SITX", "TAR", "TIF",
			"TIFF", "WAV", "WMA", "WMV", "Z", "ZIP", "JAR" };

	public String fileTypes[] = defaultFileTypes;

	public boolean schedule;
	public Date startDate, endDate;
	public boolean allowbrowser = false;
	public transient int port = 9614;
	public int tcpBuf = 8 * 1024;
	public boolean compress = false;
	public boolean attachProxy = true;

	public String getDefaultShutdownCommand() {
		if (File.separatorChar == '\\') {
			return "shutdown -s";
		} else {
			return "";
		}
	}

	public String getDefaultDisconnectCommand() {
		if (File.separatorChar == '\\') {
			return "rasdial /disconnect";
		} else {
			return "";
		}
	}

	public XDMConfig(File f) {
		file = f;
		haltTxt = getDefaultShutdownCommand();
		hungUpTxt = getDefaultDisconnectCommand();
	}

	public void save() {
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(new FileOutputStream(file));
			out.writeObject(this);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		setChanged();
		notifyObservers();
	}

	public static XDMConfig load(File file) {
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(new FileInputStream(file));
			XDMConfig c = (XDMConfig) in.readObject();
			c.port = 9614;
			return c;
		} catch (Exception e) {
			e.printStackTrace();
			try {
				in.close();
			} catch (Exception e2) {
			}
		}
		return new XDMConfig(file);
	}
}
