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

class JavaScriptEvent {
	private static Logger logger = LoggerFactory
			.getLogger(JavaScriptEvent.class);

	public int clientX;
	public int clientY;
	public int documentX;
	public int documentY;
	public int screenX;
	public int screenY;
	public int widgetX;
	public int widgetY;
	public int dragDX;
	public int dragDY;
	public int wheelDelta;
	public int button;
	public int keyCode;
	public int charCode;
	public EnumSet<KeyboardModifier> modifiers;
	public List<Touch> touches;
	public List<Touch> targetTouches;
	public List<Touch> changedTouches;
	public double scale;
	public double rotation;
	public int scrollX;
	public int scrollY;
	public int viewportWidth;
	public int viewportHeight;
	public String type;
	public String tid;
	public String response;
	public List<String> userEventArgs;

	public void get(final WebRequest request, final String se) {
		String s = se;
		int seLength = se.length();
		this.type = getStringParameter(request, concat(s, seLength, "type"));
		this.type = this.type.toLowerCase();
		this.clientX = parseIntParameter(request,
				concat(s, seLength, "clientX"), 0);
		this.clientY = parseIntParameter(request,
				concat(s, seLength, "clientY"), 0);
		this.documentX = parseIntParameter(request,
				concat(s, seLength, "documentX"), 0);
		this.documentY = parseIntParameter(request,
				concat(s, seLength, "documentY"), 0);
		this.screenX = parseIntParameter(request,
				concat(s, seLength, "screenX"), 0);
		this.screenY = parseIntParameter(request,
				concat(s, seLength, "screenY"), 0);
		this.widgetX = parseIntParameter(request,
				concat(s, seLength, "widgetX"), 0);
		this.widgetY = parseIntParameter(request,
				concat(s, seLength, "widgetY"), 0);
		this.dragDX = parseIntParameter(request, concat(s, seLength, "dragdX"),
				0);
		this.dragDY = parseIntParameter(request, concat(s, seLength, "dragdY"),
				0);
		this.wheelDelta = parseIntParameter(request,
				concat(s, seLength, "wheel"), 0);
		this.modifiers.clear();
		if (request.getParameter(concat(s, seLength, "altKey")) != null) {
			this.modifiers.add(KeyboardModifier.AltModifier);
		}
		if (request.getParameter(concat(s, seLength, "ctrlKey")) != null) {
			this.modifiers.add(KeyboardModifier.ControlModifier);
		}
		if (request.getParameter(concat(s, seLength, "shiftKey")) != null) {
			this.modifiers.add(KeyboardModifier.ShiftModifier);
		}
		if (request.getParameter(concat(s, seLength, "metaKey")) != null) {
			this.modifiers.add(KeyboardModifier.MetaModifier);
		}
		this.keyCode = parseIntParameter(request,
				concat(s, seLength, "keyCode"), 0);
		this.charCode = parseIntParameter(request,
				concat(s, seLength, "charCode"), 0);
		this.button = parseIntParameter(request, concat(s, seLength, "button"),
				0);
		this.scrollX = parseIntParameter(request,
				concat(s, seLength, "scrollX"), 0);
		this.scrollY = parseIntParameter(request,
				concat(s, seLength, "scrollY"), 0);
		this.viewportWidth = parseIntParameter(request,
				concat(s, seLength, "width"), 0);
		this.viewportHeight = parseIntParameter(request,
				concat(s, seLength, "height"), 0);
		this.response = getStringParameter(request,
				concat(s, seLength, "response"));
		int uean = parseIntParameter(request, concat(s, seLength, "an"), 0);
		this.userEventArgs.clear();
		for (int i = 0; i < uean; ++i) {
			this.userEventArgs.add(getStringParameter(request, se + "a"
					+ String.valueOf(i)));
		}
		decodeTouches(
				getStringParameter(request, concat(s, seLength, "touches")),
				this.touches);
		decodeTouches(
				getStringParameter(request, concat(s, seLength, "ttouches")),
				this.targetTouches);
		decodeTouches(
				getStringParameter(request, concat(s, seLength, "ctouches")),
				this.changedTouches);
	}

	public JavaScriptEvent() {
		this.modifiers = EnumSet.noneOf(KeyboardModifier.class);
		this.touches = new ArrayList<Touch>();
		this.targetTouches = new ArrayList<Touch>();
		this.changedTouches = new ArrayList<Touch>();
		this.type = "";
		this.tid = "";
		this.response = "";
		this.userEventArgs = new ArrayList<String>();
	}

	static String concat(final String prefix, int prefixLength, String s2) {
		return prefix + s2;
	}

	static int asInt(final String v) {
		return Integer.parseInt(v);
	}

	static long asLongLong(final String v) {
		return Long.parseLong(v);
	}

	static int parseIntParameter(final WebRequest request, final String name,
			int ifMissing) {
		String p;
		if ((p = request.getParameter(name)) != null) {
			try {
				return asInt(p);
			} catch (final NumberFormatException ee) {
				logger.error(new StringWriter()
						.append("Could not cast event property '").append(name)
						.append(": ").append(p).append("' to int").toString());
				return ifMissing;
			}
		} else {
			return ifMissing;
		}
	}

	static String getStringParameter(final WebRequest request, final String name) {
		String p;
		if ((p = request.getParameter(name)) != null) {
			return p;
		} else {
			return "";
		}
	}

	static void decodeTouches(String str, final List<Touch> result) {
		if (str.length() == 0) {
			return;
		}
		List<String> s = new ArrayList<String>();
		s = new ArrayList<String>(Arrays.asList(str.split(";")));
		if (s.size() % 9 != 0) {
			logger.error(new StringWriter()
					.append("Could not parse touches array '").append(str)
					.append("'").toString());
			return;
		}
		try {
			for (int i = 0; i < s.size(); i += 9) {
				result.add(new Touch(asLongLong(s.get(i + 0)), asInt(s
						.get(i + 1)), asInt(s.get(i + 2)), asInt(s.get(i + 3)),
						asInt(s.get(i + 4)), asInt(s.get(i + 5)), asInt(s
								.get(i + 6)), asInt(s.get(i + 7)), asInt(s
								.get(i + 8))));
			}
		} catch (final NumberFormatException ee) {
			logger.error(new StringWriter()
					.append("Could not parse touches array '").append(str)
					.append("'").toString());
			return;
		}
	}
}
