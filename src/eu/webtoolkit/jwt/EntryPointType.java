/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
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
 * Enumeration that indicates a JWt entrypoint type.
 * <p>
 * 
 * An entry point binds a behavior to a public URL. Only the wthttpd connector
 * currently supports multiple entry points.
 */
public enum EntryPointType {
	/**
	 * Specifies a full-screen application.
	 * <p>
	 * 
	 * A full screen application manages the entire browser window and provides
	 * its own HTML page.
	 * <p>
	 * 
	 * @see WApplication#getRoot()
	 */
	Application,
	/**
	 * Specifies an application that manages one or more widgets.
	 * <p>
	 * 
	 * A widget set application is part of an existing HTML page. One or more
	 * HTML elements in that web page may be bound to widgets managed by the
	 * application.
	 * <p>
	 * The application presents itself as a JavaScript file, and therefore
	 * should be embedded in the web page using a &lt;script&gt; tag, from
	 * within the &lt;body&gt; (since it needs access to the &lt;body&gt;).
	 * <p>
	 * 
	 * <p>
	 * <i><b>Note: </b>A WidgetSet application requires JavaScript support</i>
	 * </p>
	 * 
	 * @see WApplication#bindWidget(WWidget widget, String domId)
	 */
	WidgetSet,
	/**
	 * Specifies a static resource.
	 * <p>
	 * 
	 * A static resource binds a {@link WResource} to a public URL, and is not
	 * bound to a specific session.
	 * <p>
	 */
	StaticResource;

	/**
	 * Returns the numerical representation of this enum.
	 */
	public int getValue() {
		return ordinal();
	}
}
