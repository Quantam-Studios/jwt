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
 * An enumeration describing an event&apos;s type.
 * <p>
 * 
 * @see WEvent#getEventType()
 */
public enum EventType {
	/**
	 * An event which is not user- or timer-initiated.
	 */
	OtherEvent,
	/**
	 * A user-initiated event.
	 */
	UserEvent,
	/**
	 * A timer-initiated event.
	 */
	TimerEvent,
	/**
	 * An event which is a resource request.
	 */
	ResourceEvent;

	/**
	 * Returns the numerical representation of this enum.
	 */
	public int getValue() {
		return ordinal();
	}
}
