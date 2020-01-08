/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

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

/**
 * Enumeration that specifies a layout mechanism for a widget.
 * <p>
 * 
 * The layout mechanism determines how the widget positions itself relative to
 * the parent or sibling widgets.
 * <p>
 * 
 * @see WWidget#setPositionScheme(PositionScheme scheme)
 */
public enum PositionScheme {
	/**
	 * Static position scheme.
	 * <p>
	 * 
	 * The widget is layed-out with other {@link PositionScheme#Static Static}
	 * and {@link PositionScheme#Relative Relative} sibling widgets, one after
	 * another.
	 * <p>
	 * Inline widgets are layed out in horizontal lines (like text), wrapping
	 * around at the end of the line to continue on the next line. Block widgets
	 * are stacked vertically.
	 * <p>
	 * Static widgets may also float to the left or right border, using
	 * setFloatSide().
	 */
	Static,
	/**
	 * Relative position scheme.
	 * <p>
	 * 
	 * The widget is first layed out according to Static layout rules, but after
	 * layout, the widget may be offset relative to where it would be in a
	 * static layout, using setOffsets().
	 * <p>
	 * Another common use of a Relative position scheme (even with no specified
	 * offsets) is to provide a new reference coordinate system for Absolutely
	 * positioned widgets.
	 */
	Relative,
	/**
	 * Absolute position scheme.
	 * <p>
	 * 
	 * The widget is positioned at an absolute position with respect to the
	 * nearest ancestor widget that is either:
	 * <ul>
	 * <li>
	 * a {@link WTableCell}</li>
	 * <li>
	 * or has a position scheme that is {@link PositionScheme#Relative Relative}
	 * or {@link PositionScheme#Absolute Absolute}.</li>
	 * </ul>
	 */
	Absolute,
	/**
	 * Fixed position scheme.
	 * <p>
	 * 
	 * The widget is positioned at fixed position with respect to the
	 * browser&apos;s view-pane.
	 */
	Fixed;

	/**
	 * Returns the numerical representation of this enum.
	 */
	public int getValue() {
		return ordinal();
	}
}
