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
 * Enumeration that specifies an option for rendering a view item.
 * <p>
 * 
 * @see WAbstractItemDelegate#update(WWidget widget, WModelIndex index, EnumSet
 *      flags)
 */
public enum ViewItemRenderFlag {
	/**
	 * Render as selected
	 */
	RenderSelected,
	/**
	 * Render in editing mode
	 */
	RenderEditing,
	/**
	 * Render (the editor) focused
	 */
	RenderFocused,
	/**
	 * Render as invalid
	 */
	RenderInvalid;

	/**
	 * Returns the numerical representation of this enum.
	 */
	public int getValue() {
		return ordinal();
	}
}
