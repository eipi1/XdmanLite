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

import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class AuthDialog {
	static JTextField user = null;
	static JTextField pass = null;
	static Object obj[];

	public static String[] getAuth() {
		if (user == null)
			user = new JTextField();
		if (pass == null)
			pass = new JTextField();
		if (obj == null) {
			obj = new Object[4];
			obj[0] = "user";
			obj[1] = user;
			obj[2] = "pass";
			obj[3] = pass;
		}
		user.setText("");
		pass.setText("");
		while (JOptionPane.showOptionDialog(null, obj, "Enter Creditential",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
				null, null, null) == JOptionPane.OK_OPTION) {
			if (user.getText() == null || user.getText().length() < 1) {
				JOptionPane.showMessageDialog(null, "Enter username");
				continue;
			}
			if (pass.getText() == null || pass.getText().length() < 1) {
				JOptionPane.showMessageDialog(null, "Enter password");
				continue;
			}
			return new String[] { user.getText(), pass.getText() };
		}
		return null;
	}
}
