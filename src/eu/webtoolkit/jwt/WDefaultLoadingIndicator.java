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
 * A default loading indicator.
 * <p>
 * 
 * The default loading indicator displays the text message Loading... in the
 * right top corner of the window.
 * <p>
 * <h3>CSS</h3>
 * 
 * <p>
 * This widget does not provide styling, and can be styled using inline or
 * external CSS as appropriate.
 * <p>
 * <h3>i18n</h3>
 * 
 * <p>
 * The strings used in this class can be translated by overriding the default
 * values for the following localization keys:
 * <ul>
 * <li>Wt.WDefaultLoadingIndicator.Loading: Loading...</li>
 * </ul>
 * 
 * 
 * <p>
 * 
 * @see WApplication#setLoadingIndicator(WLoadingIndicator indicator)
 */
public class WDefaultLoadingIndicator extends WText implements
		WLoadingIndicator {
	private static Logger logger = LoggerFactory
			.getLogger(WDefaultLoadingIndicator.class);

	/**
	 * Constructor.
	 */
	public WDefaultLoadingIndicator() {
		super(tr("Wt.WDefaultLoadingIndicator.Loading"));
		this.setInline(false);
		this.setStyleClass("Wt-loading");
		WApplication app = WApplication.getInstance();
		app.getStyleSheet()
				.addRule(
						"div.Wt-loading",
						"background-color: red; color: white;font-family: Arial,Helvetica,sans-serif;font-size: small;position: absolute; right: 0px; top: 0px;");
		app.getStyleSheet().addRule("body div > div.Wt-loading",
				"position: fixed;");
		if (app.getEnvironment().getUserAgent().indexOf("MSIE 5.5") != -1
				|| app.getEnvironment().getUserAgent().indexOf("MSIE 6") != -1) {
			app.getStyleSheet()
					.addRule(
							"div.Wt-loading",
							"right: expression(((ignoreMe2 = document.documentElement.scrollLeft ? document.documentElement.scrollLeft : document.body.scrollLeft )) + 'px' );top: expression(((ignoreMe = document.documentElement.scrollTop ? document.documentElement.scrollTop : document.body.scrollTop)) + 'px' );");
		}
	}

	/**
	 * Returns the widget that visually represents the indicator.
	 * <p>
	 * 
	 * You should reimplement this method to return a widget that will be shown
	 * to indicate that a response is pending. The widget should be positioned
	 * using CSS.
	 * <p>
	 * The widget will be shown and hidden using {@link WWidget#show()} and
	 * {@link WWidget#hide()}. If you want to customize this behaviour, you
	 * should reimplement the
	 * {@link WWidget#setHidden(boolean hidden, WAnimation animation)
	 * WWidget#setHidden()} method. Note that {@link WWidget#show()} and
	 * {@link WWidget#hide()} are stateless slots, and thus you need to make
	 * sure that your implementation comforms to that contract, so that it may
	 * be optimized to JavaScript (the server-side implementation will only be
	 * called during stateless slot prelearning).
	 * <p>
	 * 
	 * <p>
	 * <i><b>Note: </b>The widget will not be added to the
	 * {@link WApplication#getRoot()} container. </i>
	 * </p>
	 */
	public WWidget getWidget() {
		return this;
	}

	public void setMessage(final CharSequence text) {
		this.setText(text);
	}
}
