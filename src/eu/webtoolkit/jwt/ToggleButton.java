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

class ToggleButton extends WText {
	private static Logger logger = LoggerFactory.getLogger(ToggleButton.class);

	public ToggleButton(ToggleButtonConfig config, WContainerWidget parent) {
		super(parent);
		this.signals_ = new ArrayList<AbstractSignal>();
		this.config_ = config;
		this.setStyleClass(this.config_.getStyleClass());
		this.setInline(false);
		if (WApplication.getInstance().getEnvironment().hasAjax()) {
			this.clicked().addListener(this.config_.toggleJS_);
			this.clicked().preventPropagation();
			for (int i = 0; i < this.config_.getStates().size(); ++i) {
				this.signals_.add(new JSignal(this, "t-"
						+ this.config_.getStates().get(i)));
			}
		} else {
			this.clicked().addListener(this,
					new Signal1.Listener<WMouseEvent>() {
						public void trigger(WMouseEvent e1) {
							ToggleButton.this.handleClick();
						}
					});
			for (int i = 0; i < this.config_.getStates().size(); ++i) {
				this.signals_.add(new Signal(this));
			}
		}
	}

	public ToggleButton(ToggleButtonConfig config) {
		this(config, (WContainerWidget) null);
	}

	public void remove() {
		for (int i = 0; i < this.signals_.size(); ++i) {
			;
		}
		super.remove();
	}

	public AbstractSignal signal(int i) {
		return this.signals_.get(i);
	}

	public void setState(int i) {
		this.setStyleClass(this.config_.getStyleClass()
				+ this.config_.getStates().get(i));
	}

	private List<AbstractSignal> signals_;
	private ToggleButtonConfig config_;

	private void handleClick() {
		for (int i = 0; i < this.config_.getStates().size(); ++i) {
			if (this.getStyleClass().endsWith(this.config_.getStates().get(i))) {
				(((this.signals_.get(i)) instanceof Signal ? (Signal) (this.signals_
						.get(i)) : null)).trigger();
				break;
			}
		}
	}
}
