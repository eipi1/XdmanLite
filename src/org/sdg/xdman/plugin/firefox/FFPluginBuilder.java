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


package org.sdg.xdman.plugin.firefox;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.sdg.xdman.gui.Main;

public class FFPluginBuilder {
	String files[] = { "install.rdf", "chrome.manifest", "modules/module.jsm",
			"chrome/content/overlay.js", "chrome/content/overlay.xul" };

	public void buildXPI(OutputStream out, String ext[]) throws Exception {
		StringBuffer extBuf = new StringBuffer();
		for (int i = 0; i < ext.length; i++) {
			extBuf.append("\"" + ext[i] + "\"");
			if (i == ext.length - 1) {
				continue;
			}
			extBuf.append(',');
		}
		String xdmanJar = new File(Main.getJarPath(), "xdman.jar")
				.getAbsolutePath();
		File javaPathBin = new File(System.getProperty("java.home"), "bin");
		File javaPath;
		if (File.separatorChar == '\\') {
			javaPath = new File(javaPathBin, "javaw.exe");
		} else {
			javaPath = new File(javaPathBin, "java");
		}
		ZipOutputStream zout = new ZipOutputStream(out);
		zout.setLevel(0);
		byte buf[] = new byte[512];
		for (int i = 0; i < files.length; i++) {
			zout.putNextEntry(new ZipEntry(files[i]));
			InputStream in = getClass().getResourceAsStream(
					"/plugin/firefox/" + files[i]);
			if (files[i].equals("modules/module.jsm")) {
				BufferedReader r = new BufferedReader(new InputStreamReader(in));
				while (true) {
					String line = r.readLine();
					if (line == null)
						break;
					String xdmPath = xdmanJar.replace("\\", "\\\\");
					String java = javaPath.getAbsolutePath().replace("\\",
							"\\\\");
					line = line.replace("<JAVA_PATH>", java);
					line = line.replace("<XDM_PATH>", xdmPath);
					line = line.replace("<FILE_EXT>", extBuf.toString());
					zout.write(line.getBytes());
				}
			} else {
				while (true) {
					int x = in.read(buf);
					if (x == -1)
						break;
					zout.write(buf, 0, x);
				}
			}
			in.close();
		}
		zout.close();
	}

	public static void main(String[] args) {
		File f = new File("g:\\songs");
		System.out.println(f.getAbsolutePath());
	}
}
