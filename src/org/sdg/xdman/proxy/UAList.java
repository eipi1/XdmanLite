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


package org.sdg.xdman.proxy;

class UAList {
	static String ualist[] = { "AOL", "Avant Browser",
			"Arora",
			"Fireweb Navigator",
			"Flock",
			"Navigator",// Netscape
			"Navscape",// Netscape
			"PaleMoon", "Firebird", "Firefox", "Konqueror", "Maxthon", "Opera",
			"rekonq", "RockMelt", "SeaMonkey", "ChromePlus", "Chrome",
			"Galeon", "Safari", "MSIE", "Konqueror" };

	public static String getBrowser(String ua) {
		ua = ua.toLowerCase();
		for (int i = 0; i < ualist.length; i++)
			if (ua.indexOf(ualist[i].toLowerCase()) >= 0)
				return ualist[i];
		return "Unknown Browser";
	}

	public static void main(String a[]) {
		System.out.println(getBrowser(a[0]));
	}
}
