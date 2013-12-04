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

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;

public class SegmentPanel extends JComponent {
	private static final long serialVersionUID = -6537879808121349569L;
	long start[], length[], dwnld[], len;
	GradientPaint high, low, back;

	public void setValues(long start[], long length[], long dwnld[], long len) {
		this.length = length;
		this.start = start;
		this.dwnld = dwnld;
		this.len = len;
		repaint();
	}

	public void paintComponent(Graphics g) {
		if (g == null)
			return;
		if (len == 0)
			return;
		Graphics2D g2 = (Graphics2D) g;
		if (high == null)
			high = new GradientPaint(0, 0, new Color(255, 205, 205), 0,
					getHeight() / 2, new Color(255, 145, 145), false);
		if (low == null)
			low = new GradientPaint(0, 0, new Color(218, 0, 0), 0,
					getHeight() / 2, new Color(254, 0, 0), false);
		if (back == null)
			back = new GradientPaint(0, 0, Color.WHITE, 0, getHeight() / 2,
					Color.LIGHT_GRAY, false);
		g2.setPaint(back);
		g2.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
		float r = (float) getWidth() / len;
		g2.setPaint(low);// g.setColor(Color.BLACK);
		g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
		for (int i = 0; i < start.length; i++) {
			int _start = (int) (start[i] * r);
			int _length = (int) (length[i] * r);
			int _dwnld = (int) (dwnld[i] * r);
			if (_dwnld > _length)
				_dwnld = _length;
			// g2.drawRect(_start, 0, _length, getHeight() - 1);
			g2.setPaint(high);
			g2.fillRect(_start, 0, _dwnld + 1, getHeight() / 2);
			g2.setPaint(low);
			g2.fillRect(_start, getHeight() / 2, _dwnld + 1, getHeight() - 1);
			// g.setColor(Color.RED);
			// g.drawLine(_start, 0, _start, getHeight() - 1);
			// g.setColor(Color.BLACK);
		}
		g2.setColor(Color.GRAY);
		g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
	}
}
