/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.render;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.lang.ref.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import javax.servlet.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CellState extends PageState {
	private static Logger logger = LoggerFactory.getLogger(CellState.class);

	public int lastRow;
	public double x;
	public Block cell;
	private static final double MARGINX = -1;
	private static final double EPSILON = 1e-4;

	static boolean isEpsilonMore(double x, double limit) {
		return x - EPSILON > limit;
	}

	static boolean isEpsilonLess(double x, double limit) {
		return x + EPSILON < limit;
	}
}
