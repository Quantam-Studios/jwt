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

class NavContainer extends WContainerWidget {
	private static Logger logger = LoggerFactory.getLogger(NavContainer.class);

	public NavContainer(WContainerWidget parent) {
		super(parent);
	}

	public NavContainer() {
		this((WContainerWidget) null);
	}

	public boolean isBootstrap2Responsive() {
		return this.getStyleClass().indexOf("nav-collapse") != -1;
	}

	public void setHidden(boolean hidden, final WAnimation animation) {
		if (this.isBootstrap2Responsive()) {
			if (animation.isEmpty()) {
				if (hidden) {
					this.setHeight(new WLength(0));
				} else {
					this.setHeight(WLength.Auto);
				}
			}
		}
		super.setHidden(hidden, animation);
	}
}
