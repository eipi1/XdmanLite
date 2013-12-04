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


package org.sdg.xdman.util.win;

import java.io.File;
import java.io.OutputStream;

public class RegUtil {
	static private String regKey = "HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings";

	public static boolean takeBackup(File file) {
		try {
			ProcessBuilder pb = new ProcessBuilder();
			pb.command("reg", "EXPORT", regKey, file.getAbsolutePath());
			Process proc = pb.start();
			OutputStream out = proc.getOutputStream();
			out.write("yes\n".getBytes());
			out.flush();
			proc.waitFor();
			if (proc.exitValue() == 0) {
				return true;
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static void restore(File file) {
		try {
			ProcessBuilder pb = new ProcessBuilder();
			pb.command("reg", "IMPORT", file.getAbsolutePath());
			Process proc = pb.start();
			proc.waitFor();
			System.out.println("Exit code: " + proc.exitValue());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
