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

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

public class FFListRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = 7962470587884627634L;

	public FFListRenderer() {
		super.setIcon(Main.icon);
		setHorizontalTextPosition(CENTER);
		setVerticalTextPosition(BOTTOM);
		setHorizontalAlignment(CENTER);
		setVerticalAlignment(CENTER);
		setOpaque(false);
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean b1, boolean b2) {
		setText(value.toString());
		return this;
	}

}
