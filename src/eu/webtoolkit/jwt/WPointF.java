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
 * A value class that defines a 2D point.
 */
public class WPointF extends WJavaScriptExposableObject {
	private static Logger logger = LoggerFactory.getLogger(WPointF.class);

	/**
	 * Creates point (0, 0).
	 */
	public WPointF() {
		super();
		this.x_ = 0;
		this.y_ = 0;
	}

	/**
	 * Creates a point (x, y).
	 */
	public WPointF(double x, double y) {
		super();
		this.x_ = x;
		this.y_ = y;
	}

	/**
	 * Copy constructor.
	 */
	public WPointF(final WPointF other) {
		super(other);
		this.x_ = other.getX();
		this.y_ = other.getY();
	}

	/**
	 * Creates a point from mouse coordinates.
	 */
	public WPointF(final Coordinates other) {
		super();
		this.x_ = other.x;
		this.y_ = other.y;
	}

	public WPointF clone() {
		return new WPointF(this);
	}

	/**
	 * Sets the X coordinate.
	 */
	public void setX(double x) {
		this.x_ = x;
	}

	/**
	 * Sets the Y coordinate.
	 */
	public void setY(double y) {
		this.y_ = y;
	}

	/**
	 * Returns the X coordinate.
	 */
	public double getX() {
		return this.x_;
	}

	/**
	 * Returns the Y coordinate.
	 */
	public double getY() {
		return this.y_;
	}

	/**
	 * Indicates whether some other object is "equal to" this one.
	 */
	public boolean equals(final WPointF other) {
		if (!this.sameBindingAs(other)) {
			return false;
		}
		return this.x_ == other.x_ && this.y_ == other.y_;
	}

	WPointF add(final WPointF other) {
		this.x_ += other.x_;
		this.y_ += other.y_;
		return this;
	}

	public String getJsValue() {
		char[] buf = new char[30];
		StringBuilder ss = new StringBuilder();
		ss.append('[');
		ss.append(MathUtils.roundJs(this.x_, 3)).append(',');
		ss.append(MathUtils.roundJs(this.y_, 3)).append(']');
		return ss.toString();
	}

	public WPointF swapHV(double width) {
		WPointF result = new WPointF(width - this.getY(), this.getX());
		if (this.isJavaScriptBound()) {
			StringBuilder ss = new StringBuilder();
			char[] buf = new char[30];
			ss.append("((function(p){return [");
			ss.append(MathUtils.roundJs(width, 3)).append(" - p[1],p[0]];})(")
					.append(this.getJsRef() + "))");
			result.assignBinding(this, ss.toString());
		}
		return result;
	}

	private double x_;
	private double y_;
}
