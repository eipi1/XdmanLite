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

import java.util.HashMap;

public class MIMEUtil {
	HashMap<String, String> mime;

	public String getFileExt(String mime) {
		if (this.mime == null)
			init();
		return this.mime.get(mime);
	}

	void init() {
		mime = new HashMap<String, String>();
		mime.put("audio/x-aiff", "aiff");
		mime.put("audio/basic", "au");
		mime.put("video/x-msvideo", "avi");
		mime.put("application/x-bcpio", "bcpio");
		mime.put("image/bmp", "bmp");
		mime.put("application/x-cpio", "cpio");
		mime.put("text/css", "css");
		mime.put("application/x-msdownload", "dll");
		mime.put("application/msword", "doc");
		mime.put("image/gif", "gif");
		mime.put("application/x-gtar", "gtar");
		mime.put("application/x-gzip", "gz");
		mime.put("text/html", "html");
		mime.put("image/x-icon", "ico");
		mime.put("image/jpeg", "jpeg");
		mime.put("application/x-javascript", "js");
		mime.put("audio/mid", "mid");
		mime.put("video/quicktime", "mov");
		mime.put("audio/mpeg", "mp3");
		mime.put("video/mpeg", "mpeg");
		mime.put("application/pdf", "pdf");
		mime.put("application/vnd.ms-powerpoint", "ppt");
		mime.put("application/postscript", "ps");
		mime.put("video/quicktime", "qt");
		mime.put("application/rtf", "rtf");
		mime.put("application/x-stuffit", "sit");
		mime.put("image/svg+xml", "svg");
		mime.put("application/x-shockwave-flash", "swf");
		mime.put("application/x-tar", "tar");
		mime.put("application/x-compressed", "tgz");
		mime.put("image/tiff", "tiff");
		mime.put("text/plain", "txt");
		mime.put("audio/x-wav", "wav");
		mime.put("application/vnd.ms-excel", "xls");
		mime.put("application/x-compress", "z");
		mime.put("application/zip", "zip");
		mime.put("video/x-flv", "flv");
		mime.put("video/flv", "flv");
		mime.put("video/webm", "webm");
		mime.put("video/3gpp", "3gp");
		mime.put("video/mp4", "mp4");
		mime.put("video/x-ms-wmv","wmv");
	}
}
