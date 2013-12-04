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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;

import org.sdg.xdman.core.common.ftp.FTPConnection;
import org.sdg.xdman.core.common.http.HttpConnection;
import org.sdg.xdman.core.common.http.XDMHttpClient;
import org.sdg.xdman.gui.AuthDialog;
import org.sdg.xdman.gui.DownloadWindow;
import org.sdg.xdman.util.MIMEUtil;
import org.sdg.xdman.util.XDMUtil;

public class ConnectionManager extends Observable implements IXDMConstants,
		HelpListener {
	public String cookies;
	boolean init = false;
	long length = -1, downloaded = 0;
	String fileName = null, finalFileName;
	private String url;
	List<Connection> list = new ArrayList<Connection>();
	int MAX_CHUNK = 8;
	int timeout = 60 * 1000;
	List<ChunkFileInfo> fileList = new ArrayList<ChunkFileInfo>();
	private String tempdir;
	private String destdir;
	int fileCounter = 0;
	Object lock = new Object();
	String statefileName = ".state";
	File statefile;
	public boolean stop = false;
	long startTime, prevdownload;
	String resume_support = "Unknown", status = "connecting...";
	int cnc = 0;
	public int state = STOPPED;
	String category;
	File prevtempdir;
	boolean assembling = false;
	long assemble_len = 0;
	XDMConfig config;
	int MIN_CHUNK_SZ = 8192;
	HelperConnection helper;

	public void setTempdir(String tempdir) {
		this.tempdir = tempdir;
		this.prevtempdir = new File(tempdir);
	}

	public String getTempdir() {
		return tempdir;
	}

	public void setFileName(String file) {
		finalFileName = file;
	}

	public void setDestdir(String destdir) {
		this.destdir = destdir;
	}

	public String getDestdir() {
		return destdir;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public ConnectionManager(String url, String file, String destdir,
			String tempdir, HashMap<String, String> extra, XDMConfig config) {
		this.setUrl(url);
		this.setDestdir(destdir);
		this.setTempdir(tempdir);
		this.statefile = new File(tempdir, statefileName);
		this.fileName = getFileName(url);
		this.finalFileName = file;
		category = XDMUtil.findCategory(fileName);
		this.extra = extra;
		this.config = config;
		if (extra != null) {
			String user = extra.get("USER");
			String pass = extra.get("PASS");
			if (user != null) {
				if (pass == null)
					pass = "";
				creditential = new Credential();
				creditential.user = user;
				creditential.pass = pass;
				creditential.host = URI.create(url).getHost();
				extra.remove("USER");
				extra.remove("PASS");
			}
		}
	}

	private File getUniqueFolderName() {
		String f = getFileName(getUrl());
		File target = new File(tempdir, "tmp_" + f);
		int k = 0;
		while (target.exists()) {
			target = new File(tempdir, k + "_" + target.getName());
			k++;
		}
		return target;
	}

	private String getUniqueFileName(String dir, String f) {
		File target = new File(dir, f);
		while (target.exists()) {
			target = new File(dir, "copy of " + target.getName());
		}
		return target.getName();
	}

	public synchronized void connected(Connection c)
			throws UnsupportedProtocolException {
		if (++cnc > 1)
			resume_support = "yes";
		if (!init) {
			boolean redirected = !url.equals(c.url);
			setUrl(c.url);
			cookies = c.cookies;
			length = c.getLength();
			System.out.println("Final Length: " + length);
			if (c.content_disposition != null) {
				fileName = getFileName(c.content_disposition);
				finalFileName = fileName;
			} else {
				fileName = getFileName(getUrl());
				fileName = getUniqueFileName(destdir, fileName);
				checkExt(c.content_type);
				try {
					String mime = c.content_type;
					if (mime != null) {
						if (finalFileName.indexOf(".") < 0) {
							if (mimeutil == null)
								mimeutil = new MIMEUtil();
							String ext = mimeutil.getFileExt(mime);
							System.out.println("FINAL_EXTENSION: " + ext);
							if (ext != null) {
								finalFileName += ("." + ext);
							}
						}
					}
				} catch (Exception e) {
				}
				if (redirected) {
					finalFileName = fileName;
				}
			}
			finalFileName = getUniqueFileName(destdir, finalFileName);
			File f = getUniqueFolderName();
			f.mkdirs();
			tempdir = f.getAbsolutePath();
			statefile = new File(tempdir, ".state");
			System.out.println("Temporary directory: " + tempdir);
			c.fileName = new File(tempdir, 0 + fileName).toString();
			category = XDMUtil.findCategory(fileName);
			init = true;
			int state2 = state;
			state = REDIRECTING;
			updated();
			state = state2;
			updated();
		}
		saveState();
		createChunk(null);
		status = "downloading...";
	}

	public synchronized void failed(String error, int errorCode) {
		if (state == FAILED)
			return;
		if (cnc < 2)
			resume_support = "no";
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).status != FAILED)
				return;
		}
		System.out.println("CM: Download Failed: " + error);
		if (errorCode == RESP_ERR) {
			long dwn = 0;
			for (int i = 0; i < list.size(); i++) {
				dwn += list.get(i).downloaded;
			}
			if (dwn > 0) {
				errorCode = SESSION_ERR;
			}
			// status = error;
		}
		if (errorCode == UNKNOWN_ERR) {
			status = errMsg[errorCode] + "\n" + XDMUtil.nvl(error);
		} else {
			status = errMsg[errorCode];// error;
		}
		if (error != null)
			status += "\n" + error;
		state = FAILED;
		updated();
	}

	long pdwnld = 0, ptime = 0;
	float prate = 0;

	public void updated() {
		try {
			DownloadInfo info = new DownloadInfo();
			info.path = new File(tempdir);
			info.url = getUrl();
			info.file = finalFileName;
			int sz = list.size() + fileList.size();
			long len[] = new long[sz], start[] = new long[sz], dwn[] = new long[sz];
			String stat = this.status;
			int k = 0;
			long dwnld = 0;
			info.rlen = length;

			for (int i = 0; i < fileList.size(); i++) {
				ChunkFileInfo cinfo = fileList.get(i);
				len[k] = cinfo.len;
				start[k] = cinfo.start;
				dwn[k] = cinfo.len;
				dwnld += dwn[k];
				k++;
			}
			info.stat = new String[list.size()];
			info.dwnld = new String[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Connection c = list.get(i);
				len[k] = c.getLength();
				start[k] = c.getStartOff();
				dwn[k] = c.getDownloaded();
				dwnld += dwn[k];
				info.stat[i] = c.message;
				info.dwnld[i] = XDMUtil.getFormattedLength(c.getDownloaded());
				k++;
			}
			if (dwnld > this.length)
				dwnld = length;
			info.rdwn = dwnld;
			info.len = len;
			info.startoff = start;
			info.dwn = dwn;
			info.length = XDMUtil.getFormattedLength(this.length);
			info.downloaded = XDMUtil.getFormattedLength(dwnld);
			long diff = dwnld - prevdownload;
			long time = System.currentTimeMillis();
			long dt = time - startTime;
			if (dt != 0) {
				float rate = ((float) diff / dt) * 1000;// diff / ((float) dt /
				// 1000);
				// info.speed = XDMUtil.getFormattedLength(rate) + "/sec";
				info.eta = XDMUtil.getETA(length - dwnld/* diff */, rate);
			}
			float rte = 0.0f;
			for (int m = 0; m < list.size(); m++) {
				rte += list.get(m).rate;
			}
			info.speed = XDMUtil.getFormattedLength(rte) + "/sec";
			if (assembling) {
				info.eta = "";
				info.speed = "";
			}
			info.resume = this.resume_support;
			info.status = stat;
			float prg = 0;
			if (!assembling) {
				if (length <= 0)
					prg = 0;
				else
					prg = (((float) dwnld / this.length) * 100);
			} else {
				if (length > 0) {
					prg = (((float) assemble_len / this.length) * 100);
				}
			}
			info.prg = (int) prg;
			info.progress = String.format("%.3f", prg);
			info.state = this.state;
			info.msg = this.status;
			info.category = category;
			info.tempdir = tempdir;
			setChanged();
			notifyObservers(info);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void connect(String url, String fileName, long startOff, long length,
			long contentLength, int timeout, HashMap<String, String> extra)
			throws UnsupportedProtocolException {
		if (url.startsWith("ftp://")) {
			Connection c = new FTPConnection(url, fileName, startOff, length,
					contentLength, timeout, this, lock, extra, cookies,
					creditential, config);
			list.add(c);
			c.start();
		} else if (url.startsWith("http")) {
			HttpConnection c = new HttpConnection(url, fileName, startOff,
					length, contentLength, timeout, this, lock, extra, cookies,
					creditential, config);
			list.add(c);
			c.start();
		} else {
			throw new UnsupportedProtocolException();
		}
	}

	void connect(String url, String fileName, long startOff, long length,
			long contentLength, int timeout, XDMHttpClient client,
			HashMap<String, String> extra) throws UnsupportedProtocolException {
		if (url.startsWith("ftp://")) {
			Connection c = new FTPConnection(url, fileName, startOff, length,
					contentLength, timeout, this, lock, extra, cookies,
					creditential, config);
			list.add(c);
			c.start();
		} else if (url.startsWith("http")) {
			HttpConnection c = new HttpConnection(url, fileName, startOff,
					length, contentLength, timeout, this, lock, client, extra,
					cookies, creditential, config);
			list.add(c);
			c.start();
		} else {
			throw new UnsupportedProtocolException();
		}
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

	synchronized void createChunk(XDMHttpClient client)
			throws UnsupportedProtocolException {
		if (stop)
			return;
		try {
			if (list.size() >= MAX_CHUNK)
				return;
			for (int i = 0; i < list.size(); i++) {
				list.get(i).pause();
			}

			Connection lc = findChunk();
			if (lc == null)
				return;
			long lc_len = lc.getLength();
			long lc_dwn = lc.getDownloaded();
			long lc_off = lc.getStartOff();
			long rem = lc_len - lc_dwn;
			if (rem < MIN_CHUNK_SZ)
				return;
			long startOff = lc_off + lc_len - rem / 2;// lc_off + lc_dwn + rem /
			// 2;// lc_dwn +
			// (lc_len-rem /
			// 2);
			long len = rem / 2, clen = length - startOff;
			if (lc_len > lc_dwn) {
				lc.setLength(lc_len - rem / 2);
				lc.msg("Changing length to " + lc.getLength() + " from "
						+ lc_len);
				connect(getUrl(), new File(getTempdir(), startOff + fileName)
						.toString(), startOff, len, clen, timeout, client,
						extra);
			}
		} finally {
			for (int i = 0; i < list.size(); i++) {
				list.get(i).resume();
			}
		}
	}

	Connection findChunk() {
		long len = -9999;
		Connection c = null;
		for (int i = 0; i < list.size(); i++) {
			Connection cl = list.get(i);
			long diff = cl.getLength() - cl.getDownloaded();
			if (diff > len && diff > 0) {
				len = diff;
				c = cl;
			}
		}
		if (len < MIN_CHUNK_SZ)
			return null;
		return c;
	}

	Connection findNextChunk(long end) {
		for (int i = 0; i < list.size(); i++) {
			Connection cl = list.get(i);
			if (cl.getStartOff() == end && cl.status != DOWNLOADING) {
				if (cl.getDownloaded() == 0)
					return cl;
			}
		}
		return null;
	}

	public synchronized boolean donwloadComplete(Connection c)
			throws UnsupportedProtocolException {
		saveState();
		if (length < 0) {
			c.close();
			list.remove(c);
			fileList.add(new ChunkFileInfo(c.fileName, c.getStartOff(), c
					.getDownloaded()/* c.getLength() */));
			downloaded += c.getDownloaded();
			length = downloaded;
			checkFinished();
			return true;
		}
		Connection nc = findNextChunk(c.getStartOff() + c.getLength());
		if (nc == null) {
			// Finalize download
			try {
				c.out.close();
			} catch (Exception e) {
			}
			// c.close();
			fileList.add(new ChunkFileInfo(c.fileName, c.getStartOff(), c
					.getLength()));
			downloaded += c.getLength();
			list.remove(c);
			boolean finish = checkFinished();
			if (finish) {
				// System.out.println("FINISHED");
			} else {
				// System.out.println("NOT FINISHED");
				createChunk(null);
				// checkFinished();
				needsHelp();
			}
			return true;
		} else {
			// Continue download
			list.remove(nc);
			// System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@Calling stop");
			nc.stop();
			c.setLength(c.getLength() + nc.getLength());
			createChunk(null);
			return false;
		}
	}

	boolean checkFinished() {
		if (list.size() == 0) {
			if (length > -1)
				if (downloaded == length) {
					assemble();
					return true;
				}
		}
		return false;
	}

	void assemble() {
		// Sort according to offsets
		status = "Appending all parts...";
		if (helper != null) {
			// System.out.println("Stopping helper");
			helper.stop();
		}
		assembling = true;
		updated();
		Collections.sort(fileList, new ChunkFileInfo());
		if (stop)
			return;
		try {
			File outDir = new File(destdir);
			if (!outDir.exists()) {
				outDir.mkdirs();
			}
			finalFileName = getUniqueFileName(destdir, finalFileName);
			File outFile = new File(getDestdir(), finalFileName);
			OutputStream out = new BufferedOutputStream(new FileOutputStream(
					outFile), config.tcpBuf);
			int state2 = state;
			state = REDIRECTING;
			updated();
			state = state2;
			updated();
			InputStream in = null;
			int count = 0;
			long MB = 1024 * 1024;
			for (int i = 0; i < fileList.size(); i++) {
				ChunkFileInfo info = fileList.get(i);
				System.out.println("Reading..." + info.file);
				in = new BufferedInputStream(new FileInputStream(info.file),
						config.tcpBuf);
				for (int j = 0; j < info.len; j++) {
					if (stop) {
						in.close();
						out.close();
						outFile.delete();
						return;
					}
					int x = in.read();
					if (x == -1)
						throw new IllegalArgumentException("Assemble EOF");
					out.write(x);
					assemble_len++;
					count++;
					if (count > MB) {
						updated();
						count = 0;
					}
				}
				in.close();
			}
			out.close();
			for (int i = 0; i < fileList.size(); i++) {
				ChunkFileInfo info = fileList.get(i);
				System.out.println("Deleting: " + info.file + " "
						+ new File(info.file).delete());
			}
			File t = new File(tempdir);
			File f[] = t.listFiles();
			for (int i = 0; i < f.length; i++) {
				f[i].delete();
			}
			t.delete();
		} catch (Exception e) {
			e.printStackTrace();
			if (e instanceof IOException)
				status = "Output folder is write protected or full.\n"
						+ "Try another location using 'Save As' option";
			// state = FAILED;
			// updated();
			return;
		}
		status = "Download Complete.";
		state = COMPLETE;
		updated();
	}

	public void start() throws UnsupportedProtocolException {
		state = DOWNLOADING;
		startTime = System.currentTimeMillis();
		connect(getUrl(), "FILE", 0, -1, -1, timeout, extra);
	}

	public void start(XDMHttpClient client) throws UnsupportedProtocolException {
		state = DOWNLOADING;
		startTime = System.currentTimeMillis();
		connect(getUrl(), "FILE", 0, -1, -1, timeout, client, extra);
	}

	long endTime;

	public void saveDownload() {
		long time = System.currentTimeMillis();
		if (time - endTime > 5 * 1000) {
			saveState();
			endTime = time;
		}
	}

	public synchronized void saveState() {
		try {
			if (!init)
				return;
			ObjectOutputStream out = new ObjectOutputStream(
					new FileOutputStream(statefile));
			out.writeUTF(getUrl());
			out.writeObject(creditential);
			out.writeUTF(fileName);
			out.writeUTF(finalFileName);
			out.writeUTF(getDestdir());
			out.writeLong(length);
			out.writeLong(downloaded);
			out.writeInt(fileList.size());
			for (int i = 0; i < fileList.size(); i++) {
				out.writeObject(fileList.get(i));
			}
			out.writeInt(list.size());
			for (int i = 0; i < list.size(); i++) {
				Connection c = list.get(i);
				// System.out.println("Writing state object");
				out.writeObject(c.getState());
			}
			long dwn = 0;
			for (int i = 0; i < fileList.size(); i++) {
				ChunkFileInfo cinfo = fileList.get(i);
				dwn += cinfo.len;
			}
			for (int i = 0; i < list.size(); i++) {
				Connection c = list.get(i);
				long len = c.getLength();
				long dwnl = c.getDownloaded();
				if (len < dwnl)
					dwn += len;
				else
					dwn += dwnl;
			}
			out.writeLong(dwn);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized boolean restoreState() {
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(
					statefile));
			String u = in.readUTF();
			if (!(url == null || url.length() < 1)) {
				u = url;
			}
			setUrl(u);
			creditential = (Credential) in.readObject();
			fileName = in.readUTF();
			String finalFileName2 = in.readUTF();
			if (finalFileName == null) {
				finalFileName = finalFileName2;
			}
			String destdir2 = in.readUTF();
			if (destdir == null)
				setDestdir(destdir2);
			length = in.readLong();
			downloaded = in.readLong();
			int sz = in.readInt();
			fileList = new ArrayList<ChunkFileInfo>();
			for (int i = 0; i < sz; i++) {
				fileList.add((ChunkFileInfo) in.readObject());
			}
			sz = in.readInt();
			list = new ArrayList<Connection>();
			for (int i = 0; i < sz; i++) {
				Connection.State state = (Connection.State) in.readObject();
				state.url = url;
				if (url.startsWith("http"))
					list.add(new HttpConnection(state, timeout, this, lock,
							cookies, creditential, config));
				else
					list.add(new FTPConnection(state, timeout, this, lock,
							cookies, creditential, config));
			}
			prevdownload = in.readLong();
			in.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			return false;
		}
	}

	public void stop() {
		stop = true;
		status = "Stopped";
		for (int i = 0; i < list.size(); i++) {
			Connection c = list.get(i);
			c.stop();
			try {
				c.out.close();
			} catch (Exception e) {
			}
		}
		saveState();
		state = STOPPED;
		updated();
	}

	public void resume() {
		if (state == DOWNLOADING)
			return;
		state = DOWNLOADING;
		startTime = System.currentTimeMillis();
		// init = true;
		init = restoreState();
		if (init) {
			if (list.size() < 1) {
				long d = 0;
				for (int i = 0; i < fileList.size(); i++) {
					d += fileList.get(i).len;
				}
				if (d == this.length) {
					new Thread() {
						@Override
						public void run() {
							assemble();
						}
					}.start();

				}
			} else {
				for (int i = 0; i < list.size(); i++) {
					Connection c = list.get(i);
					// c.stop = false;
					// c.pause = false;
					c.start();
				}
			}
		} else {
			// tempdir = prevtempdir.getAbsolutePath();
			// start();
			state = FAILED;
			status = "Partially downloaded files have been modified.\n"
					+ "You have to 'Restart' the download.";
			updated();
		}
	}

	void checkExt(String mime) {
		if (mime == null) {
			return;
		}
		if (mime.equals("text/html")) {
			if (!(fileName.endsWith("html") || fileName.endsWith(".htm"))) {
				fileName += ".html";
				return;
			}
		}
		if (fileName.indexOf(".") < 0) {
			if (mimeutil == null)
				mimeutil = new MIMEUtil();
			String ext = mimeutil.getFileExt(mime);
			System.out.println("EXTENSION: " + ext);
			if (ext != null) {
				fileName += ("." + ext);
			}
		}
	}

	MIMEUtil mimeutil;
	public HashMap<String, String> extra;
	private Credential creditential;

	public void setTimeOut(int tout) {
		this.timeout = tout * 1000;
	}

	public void setMaxConn(int c) {
		MAX_CHUNK = c;
	}

	public synchronized Credential getCreditential() {

		String a[] = AuthDialog.getAuth();
		if (a == null) {
			creditential = null;
			return null;
		}
		creditential = new Credential();
		creditential.host = url;
		creditential.pass = a[1];
		creditential.user = a[0];
		return creditential;

	}

	@Override
	public void helpComplete(Object invoker, Object data) {
		// TODO Auto-generated method stub
		if (invoker instanceof HelperConnection) {
			System.out.println("Helper Connection Complete...");
			HelperConnection hc = (HelperConnection) invoker;
			if (state == COMPLETE) {
				System.out
						.println("Helper returing because MGR state is complete.");
				return;
			}
			if (list.size() != 1) {
				System.out.println("Helper returing because list size!=1: "
						+ list.size());
				return;
			}
			Connection c = list.get(0);
			if (c == hc.c) {
				if (hc.stop) {
					System.out.println("Helper return as stopped");
					return;
				}
				if (c.status == COMPLETE) {
					System.out
							.println("Helper returning because thread state COMPLETE");
					return;
				}
				c.stop();
				try {
					System.out.println("Replace stream");
					c.out.close();
					RandomAccessFile raf = new RandomAccessFile(hc.fileName,
							"rw");
					raf.write(hc.out.toByteArray());
					raf.close();
					fileList.add(new ChunkFileInfo(hc.fileName, hc.start,
							hc.length));
					System.out.println("Finalized download: HELPER");
					downloaded += hc.length;
					System.out.println("HELPER CALLING DWNCOMPLETE");
					System.out
							.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&HELPER USED AND DONE");
					c.length = hc.start - c.startOff;
					c.downloaded = c.length;
					c.status = COMPLETE;
					// list.remove(c);
					// fileList.add(new ChunkFileInfo(c.fileName, c.startOff,
					// c.length));
					// assemble();
					System.out
							.println("DWN: " + downloaded + " LEN: " + length);
					if (donwloadComplete(c)) {
						System.out.println("Helper Thread complete.");
					} else {
						System.out.println("Mgr return false ERROR!!!");
					}
				} catch (Exception e) {
					System.out.println(e);
					e.printStackTrace();
				}
			} else {
				System.out
						.println("Helper is returning because reference is not same");
			}
		}
	}

	boolean needsHelp() {
		if (list.size() == 1) {
			Connection c = list.get(0);
			if (c instanceof HttpConnection) {
				HttpConnection hc = (HttpConnection) c;
				if (hc.length + downloaded == length) {
					long start = hc.startOff + hc.downloaded;
					long dwn = hc.downloaded;
					long len = hc.length - dwn;
					if (len - dwn < MIN_CHUNK_SZ) {
						if (helper != null) {
							System.out.println("HELPER!=NULL");
							return false;
						}
						if (start == hc.startOff) {
							System.out
									.println("Helper return as startoff same");
							return false;
						}
						helper = new HelperConnection(config, start, len, url,
								this, hc, new File(getTempdir(), start
										+ fileName).toString(), hc.timeout,
								hc.extra, hc.cookies, hc.credential);
						helper.start();
						System.out.println("Helper Connection Started");
						return true;
					} else {
						System.out.println("HELPER Chunk SZ>32K");
						return false;
					}
				} else {
					System.out.println("HELPER SIZE DOES NOT MATCH");
					return false;
				}
			} else {
				System.out.println("Helper reference ERROR");
				return false;
			}
		} else {
			System.out.println("Helper return: List size!=1 : " + list.size());
			return false;
		}
	}

	public static void main(String a[]) throws Exception {
		// http://www.google.co.in/url?q=http://onyxneon.com/books/modern_perl/modern_perl_a4.pdf&sa=U&ei=MbfaUJaVJI2nrAf5zYDADA&ved=0CCUQFjAG&usg=AFQjCNFZ4wyOE0u83mwArGCvFIrfLrpYRA
		// String url =
		// "http://apache.techartifact.com/mirror//httpcomponents/httpcore/source/httpcomponents-core-4.3-alpha1-src.tar.gz";//
		// "http://apache.techartifact.com/mirror//httpcomponents/httpclient/source/httpcomponents-client-4.2.2-src.tar.gz";//"http://localhost/x.rar";//"http://localhost:8080/tesr/Test";//"http://www.stepheniemeyer.com/pdf/midnightsun_partial_draft4.pdf";//"http://localhost/x.rar";
		// //
		String url = "http://localhost:8080/s.rar";// "https://www.verisign.com";//"http://jd.benow.ca/jd-gui/downloads/jd-gui-0.3.5.windows.zip";//
		// URL u = new URL(url);
		// System.out.println("HOST: " + u.getHost() + " PATH: " +
		// u.getPath()+" "+u.getProtocol());
		// "http://www.google.com";//"http://localhost/x.rar";//
		// "http://mybeat.techmahindra.com/Attachments/vcard/User_Manua_for_Business_Cards_Distribution_System.pdf";//"http://mybeat.techmahindra.com";//"http://onyxneon.com/books/modern_perl/modern_perl_a4.pdf&sa=U&ei=MbfaUJaVJI2nrAf5zYDADA&ved=0CCUQFjAG&usg=AFQjCNFZ4wyOE0u83mwArGCvFIrfLrpYRA";
		// if (true)
		// return;
		URI uri = new URI("http://x.com/sfc/rfc/f?a=b");
		System.out.println(uri.getRawPath() + "?" + uri.getQuery() + " "
				+ uri.getScheme());
		System.out.println(uri.getRawSchemeSpecificPart());
		ConnectionManager mgr = new ConnectionManager(url, "gfgd", "g:/tst",
				"g:/tst", null, null);
		DownloadWindow d = new DownloadWindow(mgr);
		mgr.addObserver(d);
		d.showWindow();
		mgr.start();
		// Thread.sleep(2000);
		// System.out
		// .println("####################################PAUSED#####################");
		// Thread.sleep(1000);
		// mgr.stop();

		// Thread.sleep(500); System.out.println(
		// "####################################RESUMED#####################");
		// HttpConnectionManager mgr2 = new HttpConnectionManager(url,
		// "g:/tst",
		// "g:/tst");
		// mgr2.resume();
	}

}

class ChunkFileInfo implements Comparator<ChunkFileInfo>, Serializable {
	private static final long serialVersionUID = -4766221633164988384L;
	String file;
	long start, len;

	ChunkFileInfo() {
	}

	ChunkFileInfo(String f, long s, long l) {
		this.file = f;
		this.start = s;
		this.len = l;
	}

	public int compare(ChunkFileInfo o1, ChunkFileInfo o2) {
		if (o1.start == o2.start)
			return 0;
		else if (o1.start > o2.start)
			return 1;
		else
			return -1;
	}

}
