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
 * Class to represent a client-side DOM element (proxy).
 * <p>
 * 
 * The DOM element proxy object is used as an intermediate layer to render the
 * creation of new DOM elements or updates to existing DOM elements. A DOM
 * element can be serialized to HTML or to JavaScript manipulations, and
 * therefore is the main abstraction layer to avoid hard-coding JavaScript-based
 * rendering within the library while still allowing fine-grained Ajax updates
 * or large-scale HTML changes.
 * <p>
 * This is an internal API, subject to change.
 */
public class DomElement {
	private static Logger logger = LoggerFactory.getLogger(DomElement.class);

	/**
	 * Enumeration for the access mode (creation or update).
	 */
	public enum Mode {
		ModeCreate, ModeUpdate;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * Constructor.
	 * <p>
	 * This constructs a {@link DomElement} reference, with a given mode and
	 * element type. Note that even when updating an existing element, the type
	 * is taken into account for information on what kind of operations are
	 * allowed (workarounds for IE deficiencies for examples) or to infer some
	 * basic CSS defaults for it (whether it is inline or a block element).
	 * <p>
	 * Typically, elements are created using one of the &apos;named&apos;
	 * constructors: {@link DomElement#createNew(DomElementType type)
	 * createNew()},
	 * {@link DomElement#getForUpdate(String id, DomElementType type)
	 * getForUpdate()} or
	 * {@link DomElement#updateGiven(String var, DomElementType type)
	 * updateGiven()}.
	 */
	public DomElement(DomElement.Mode mode, DomElementType type) {
		this.mode_ = mode;
		this.wasEmpty_ = this.mode_ == DomElement.Mode.ModeCreate;
		this.removeAllChildren_ = -1;
		this.minMaxSizeProperties_ = false;
		this.unstubbed_ = false;
		this.unwrapped_ = false;
		this.replaced_ = null;
		this.insertBefore_ = null;
		this.type_ = type;
		this.id_ = "";
		this.numManipulations_ = 0;
		this.timeOut_ = -1;
		this.javaScript_ = new EscapeOStream();
		this.javaScriptEvenWhenDeleted_ = "";
		this.var_ = "";
		this.attributes_ = new HashMap<String, String>();
		this.removedAttributes_ = new HashSet<String>();
		this.properties_ = new TreeMap<Property, String>();
		this.eventHandlers_ = new HashMap<String, DomElement.EventHandler>();
		this.childrenToAdd_ = new ArrayList<DomElement.ChildInsertion>();
		this.childrenToSave_ = new ArrayList<String>();
		this.updatedChildren_ = new ArrayList<DomElement>();
		this.childrenHtml_ = new EscapeOStream();
		this.timeouts_ = new ArrayList<DomElement.TimeoutEvent>();
		this.elementTagName_ = "";
	}

	/**
	 * set dom element custom tag name
	 */
	public void setDomElementTagName(final String name) {
		this.elementTagName_ = name;
	}

	/**
	 * Low-level URL encoding function.
	 */
	public static String urlEncodeS(final String url) {
		return urlEncodeS(url, "");
	}

	/**
	 * Low-level URL encoding function.
	 * <p>
	 * This variant allows the exclusion of certain characters from URL
	 * encoding.
	 */
	public static String urlEncodeS(final String url, final String allowed) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < url.length(); ++i) {
			char c = url.charAt(i);
			if (c <= 31 || c >= 127 || unsafeChars_.indexOf(c) != -1) {
				if (allowed.indexOf(c) != -1) {
					result.append((char) c);
				} else {
					result.append('%');
					result.append(hexLookup(c >> 4));
					result.append(hexLookup(c));
				}
			} else {
				result.append((char) c);
			}
		}
		return result.toString();
	}

	/**
	 * Returns the mode.
	 */
	public DomElement.Mode getMode() {
		return this.mode_;
	}

	/**
	 * Sets the element type.
	 */
	public void setType(DomElementType type) {
		this.type_ = type;
	}

	/**
	 * Returns the element type.
	 */
	public DomElementType getType() {
		return this.type_;
	}

	/**
	 * Creates a reference to a new element.
	 */
	public static DomElement createNew(DomElementType type) {
		DomElement e = new DomElement(DomElement.Mode.ModeCreate, type);
		return e;
	}

	/**
	 * Creates a reference to an existing element, using its ID.
	 */
	public static DomElement getForUpdate(final String id, DomElementType type) {
		if (id.length() == 0) {
			throw new WException("Cannot update widget without id");
		}
		DomElement e = new DomElement(DomElement.Mode.ModeUpdate, type);
		e.id_ = id;
		return e;
	}

	/**
	 * Creates a reference to an existing element, deriving the ID from an
	 * object.
	 * <p>
	 * This uses object.{@link DomElement#getId() getId()} as the id.
	 */
	public static DomElement getForUpdate(WObject object, DomElementType type) {
		return getForUpdate(object.getId(), type);
	}

	/**
	 * Creates a reference to an existing element, using an expression to access
	 * the element.
	 */
	public static DomElement updateGiven(final String var, DomElementType type) {
		DomElement e = new DomElement(DomElement.Mode.ModeUpdate, type);
		e.var_ = var;
		return e;
	}

	/**
	 * Returns the JavaScript variable name.
	 * <p>
	 * This variable name is only defined when the element is being rendered
	 * using JavaScript, after {@link DomElement#declare(EscapeOStream out)
	 * declare()} has been called.
	 */
	public String getVar() {
		return this.var_;
	}

	/**
	 * Sets whether the element was initially empty.
	 * <p>
	 * Knowing that an element was empty allows optimization of
	 * {@link DomElement#addChild(DomElement child) addChild()}
	 */
	public void setWasEmpty(boolean how) {
		this.wasEmpty_ = how;
	}

	/**
	 * Adds a child.
	 * <p>
	 * Ownership of the child is transferred to this element, and the child
	 * should not be manipulated after the call, since it could be that it gets
	 * directly converted into HTML and deleted.
	 */
	public void addChild(DomElement child) {
		if (child.getMode() == DomElement.Mode.ModeCreate) {
			this.numManipulations_ += 2;
			if (this.wasEmpty_
					&& this.canWriteInnerHTML(WApplication.getInstance())) {
				child.asHTML(this.childrenHtml_, this.javaScript_,
						this.timeouts_);
				;
			} else {
				this.childrenToAdd_
						.add(new DomElement.ChildInsertion(-1, child));
			}
		} else {
			this.updatedChildren_.add(child);
		}
	}

	/**
	 * Inserts a child.
	 * <p>
	 * Ownership of the child is transferred to this element, and the child
	 * should not be manipulated after the call.
	 */
	public void insertChildAt(DomElement child, int pos) {
		++this.numManipulations_;
		this.childrenToAdd_.add(new DomElement.ChildInsertion(pos, child));
	}

	/**
	 * Saves an existing child.
	 * <p>
	 * This detaches the child from the parent, allowing the manipulation of the
	 * innerHTML without deleting the child. Stubs in the the new HTML that
	 * reference the same id will be replaced with the saved child.
	 */
	public void saveChild(final String id) {
		this.childrenToSave_.add(id);
	}

	/**
	 * Sets an attribute value.
	 */
	public void setAttribute(final String attribute, final String value) {
		++this.numManipulations_;
		this.attributes_.put(attribute, value);
		this.removedAttributes_.remove(attribute);
	}

	/**
	 * Returns an attribute value set.
	 * <p>
	 * 
	 * @see DomElement#setAttribute(String attribute, String value)
	 */
	public String getAttribute(final String attribute) {
		String i = this.attributes_.get(attribute);
		if (i != null) {
			return i;
		} else {
			return "";
		}
	}

	/**
	 * Removes an attribute.
	 */
	public void removeAttribute(final String attribute) {
		++this.numManipulations_;
		this.attributes_.remove(attribute);
		this.removedAttributes_.add(attribute);
	}

	/**
	 * Sets a property.
	 */
	public void setProperty(Property property, final String value) {
		++this.numManipulations_;
		this.properties_.put(property, value);
		if (property.getValue() >= Property.PropertyStyleMinWidth.getValue()
				&& property.getValue() <= Property.PropertyStyleMaxHeight
						.getValue()) {
			this.minMaxSizeProperties_ = true;
		}
	}

	/**
	 * Adds a &apos;word&apos; to a property.
	 * <p>
	 * This adds a word (delimited by a space) to an existing property value.
	 */
	public void addPropertyWord(Property property, final String value) {
		this.setProperty(property, StringUtils.addWord(this
				.getProperty(property), value));
	}

	/**
	 * Returns a property value set.
	 * <p>
	 * 
	 * @see DomElement#setProperty(Property property, String value)
	 */
	public String getProperty(Property property) {
		String i = this.properties_.get(property);
		if (i != null) {
			return i;
		} else {
			return "";
		}
	}

	/**
	 * Removes a property.
	 */
	public void removeProperty(Property property) {
		this.properties_.remove(property);
	}

	/**
	 * Sets a whole map of properties.
	 */
	public void setProperties(final SortedMap<Property, String> properties) {
		for (Iterator<Map.Entry<Property, String>> i_it = properties.entrySet()
				.iterator(); i_it.hasNext();) {
			Map.Entry<Property, String> i = i_it.next();
			this.setProperty(i.getKey(), i.getValue());
		}
	}

	/**
	 * Returns all properties currently set.
	 */
	public SortedMap<Property, String> getProperties() {
		return this.properties_;
	}

	/**
	 * Clears all properties.
	 */
	public void clearProperties() {
		this.numManipulations_ -= this.properties_.size();
		this.properties_.clear();
	}

	/**
	 * Sets an event handler based on a signal&apos;s connections.
	 */
	public void setEventSignal(String eventName,
			final AbstractEventSignal signal) {
		this.setEvent(eventName, signal.getJavaScript(), signal.encodeCmd(),
				signal.isExposedSignal());
	}

	/**
	 * Sets an event handler.
	 * <p>
	 * This sets an event handler by a combination of client-side JavaScript
	 * code and a server-side signal to emit.
	 */
	public void setEvent(String eventName, final String jsCode,
			final String signalName, boolean isExposed) {
		WApplication app = WApplication.getInstance();
		boolean anchorClick = this.getType() == DomElementType.DomElement_A
				&& eventName == WInteractWidget.CLICK_SIGNAL;
		StringBuilder js = new StringBuilder();
		if (isExposed || anchorClick || jsCode.length() != 0) {
			js.append("var e=event||window.event,");
			js.append("o=this;");
			if (anchorClick) {
				js
						.append("if(e.ctrlKey||e.metaKey||(Wt3_3_4.button(e) > 1))return true;else{");
			}
			js.append(jsCode);
			if (isExposed) {
				js.append(app.getJavaScriptClass()).append("._p_.update(o,'")
						.append(signalName).append("',e,true);");
			}
			if (anchorClick) {
				js.append("}");
			}
		}
		++this.numManipulations_;
		this.eventHandlers_.put(eventName, new DomElement.EventHandler(js
				.toString(), signalName));
	}

	/**
	 * Sets an event handler.
	 * <p>
	 * Calls
	 * {@link #setEvent(String eventName, String jsCode, String signalName, boolean isExposed)
	 * setEvent(eventName, jsCode, signalName, false)}
	 */
	public final void setEvent(String eventName, final String jsCode,
			final String signalName) {
		setEvent(eventName, jsCode, signalName, false);
	}

	/**
	 * Sets an event handler.
	 * <p>
	 * This sets a JavaScript event handler.
	 */
	public void setEvent(String eventName, final String jsCode) {
		this.eventHandlers_.put(eventName, new DomElement.EventHandler(jsCode,
				""));
	}

	/**
	 * This adds more JavaScript to an event handler.
	 */
	public void addEvent(String eventName, final String jsCode) {
		this.eventHandlers_.get(eventName).jsCode += jsCode;
	}

	/**
	 * A data-structure for an aggregated event handler.
	 */
	public static class EventAction {
		private static Logger logger = LoggerFactory
				.getLogger(EventAction.class);

		public String jsCondition;
		public String jsCode;
		public String updateCmd;
		public boolean exposed;

		public EventAction(final String aJsCondition, final String aJsCode,
				final String anUpdateCmd, boolean anExposed) {
			this.jsCondition = aJsCondition;
			this.jsCode = aJsCode;
			this.updateCmd = anUpdateCmd;
			this.exposed = anExposed;
		}
	}

	/**
	 * Sets an aggregated event handler.
	 */
	public void setEvent(String eventName,
			final List<DomElement.EventAction> actions) {
		StringBuilder code = new StringBuilder();
		for (int i = 0; i < actions.size(); ++i) {
			if (actions.get(i).jsCondition.length() != 0) {
				code.append("if(").append(actions.get(i).jsCondition).append(
						"){");
			}
			code.append(actions.get(i).jsCode);
			if (actions.get(i).exposed) {
				code.append(WApplication.getInstance().getJavaScriptClass())
						.append("._p_.update(o,'").append(
								actions.get(i).updateCmd).append("',e,true);");
			}
			if (actions.get(i).jsCondition.length() != 0) {
				code.append("}");
			}
		}
		this.setEvent(eventName, code.toString(), "");
	}

	/**
	 * Sets the DOM element id.
	 */
	public void setId(final String id) {
		++this.numManipulations_;
		this.id_ = id;
	}

	/**
	 * Sets a DOM element name.
	 */
	public void setName(final String name) {
		++this.numManipulations_;
		this.id_ = name;
		this.setAttribute("name", name);
	}

	/**
	 * Configures the DOM element as a source for timed events.
	 */
	public void setTimeout(int msec, boolean jsRepeat) {
		++this.numManipulations_;
		this.timeOut_ = msec;
		this.timeOutJSRepeat_ = jsRepeat;
	}

	/**
	 * Calls a JavaScript method on the DOM element.
	 */
	public void callMethod(final String method) {
		++this.numManipulations_;
		if (this.var_.length() == 0) {
			this.javaScript_.append("Wt3_3_4").append(".$('").append(this.id_)
					.append("').");
		} else {
			this.javaScript_.append(this.var_).append('.');
		}
		this.javaScript_.append(method).append(";\n");
	}

	/**
	 * Calls JavaScript (related to the DOM element).
	 */
	public void callJavaScript(final String jsCode, boolean evenWhenDeleted) {
		++this.numManipulations_;
		if (!evenWhenDeleted) {
			this.javaScript_.append(jsCode).append('\n');
		} else {
			this.javaScriptEvenWhenDeleted_ += jsCode;
		}
	}

	/**
	 * Calls JavaScript (related to the DOM element).
	 * <p>
	 * Calls {@link #callJavaScript(String jsCode, boolean evenWhenDeleted)
	 * callJavaScript(jsCode, false)}
	 */
	public final void callJavaScript(final String jsCode) {
		callJavaScript(jsCode, false);
	}

	/**
	 * Returns the id.
	 */
	public String getId() {
		return this.id_;
	}

	/**
	 * Removes all children.
	 * <p>
	 * If firstChild != 0, then only children starting from firstChild are
	 * removed.
	 */
	public void removeAllChildren(int firstChild) {
		++this.numManipulations_;
		this.removeAllChildren_ = firstChild;
		this.wasEmpty_ = firstChild == 0;
	}

	/**
	 * Removes all children.
	 * <p>
	 * Calls {@link #removeAllChildren(int firstChild) removeAllChildren(0)}
	 */
	public final void removeAllChildren() {
		removeAllChildren(0);
	}

	/**
	 * Removes the element.
	 */
	public void removeFromParent() {
		this.callJavaScript("Wt3_3_4.remove('" + this.getId() + "');", true);
	}

	/**
	 * Replaces the element by another element.
	 */
	public void replaceWith(DomElement newElement) {
		++this.numManipulations_;
		this.replaced_ = newElement;
	}

	/**
	 * Unstubs an element by another element.
	 * <p>
	 * Stubs are used to render hidden elements initially and update them in the
	 * background. This is almost the same as
	 * {@link DomElement#replaceWith(DomElement newElement) replaceWith()}
	 * except that some style properties are copied over (most importantly its
	 * visibility).
	 */
	public void unstubWith(DomElement newElement, boolean hideWithDisplay) {
		this.replaceWith(newElement);
		this.unstubbed_ = true;
		this.hideWithDisplay_ = hideWithDisplay;
	}

	/**
	 * Inserts the element in the DOM as a new sibling.
	 */
	public void insertBefore(DomElement sibling) {
		++this.numManipulations_;
		this.insertBefore_ = sibling;
	}

	/**
	 * Unwraps an element to progress to Ajax support.
	 * <p>
	 * In plain HTML mode, some elements are rendered wrapped in or as another
	 * element, to provide more interactivity in the absense of JavaScript.
	 */
	public void unwrap() {
		++this.numManipulations_;
		this.unwrapped_ = true;
	}

	/**
	 * Enumeration for an update rendering phase.
	 */
	public enum Priority {
		Delete, Create, Update;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * Structure for keeping track of timers attached to this element.
	 */
	public static class TimeoutEvent {
		private static Logger logger = LoggerFactory
				.getLogger(TimeoutEvent.class);

		public int msec;
		public String event;
		public boolean repeat;

		public TimeoutEvent() {
			this.event = "";
		}

		public TimeoutEvent(int m, final String e, boolean r) {
			this.msec = m;
			this.event = e;
			this.repeat = r;
		}
	}

	/**
	 * Renders the element as JavaScript.
	 */
	public void asJavaScript(final StringBuilder out) {
		this.mode_ = DomElement.Mode.ModeUpdate;
		EscapeOStream eout = new EscapeOStream(out);
		this.declare(eout);
		eout.append(this.var_).append(".setAttribute('id', '").append(this.id_)
				.append("');\n");
		this.mode_ = DomElement.Mode.ModeCreate;
		this.setJavaScriptProperties(eout, WApplication.getInstance());
		this.setJavaScriptAttributes(eout);
		this.asJavaScript(eout, DomElement.Priority.Update);
	}

	/**
	 * Renders the element as JavaScript, by phase.
	 * <p>
	 * To avoid temporarily having dupliate IDs as elements move around in the
	 * page, rendering is ordered in a number of phases : first deleting
	 * existing elements, then creating new elements, and finally updates to
	 * existing elements.
	 */
	public String asJavaScript(final EscapeOStream out,
			DomElement.Priority priority) {
		switch (priority) {
		case Delete:
			if (this.javaScriptEvenWhenDeleted_.length() != 0
					|| this.removeAllChildren_ >= 0) {
				out.append(this.javaScriptEvenWhenDeleted_);
				if (this.removeAllChildren_ >= 0) {
					this.declare(out);
					if (this.removeAllChildren_ == 0) {
						out.append("Wt3_3_4").append(".setHtml(").append(
								this.var_).append(", '');\n");
					} else {
						out.append("$(").append(this.var_).append(
								").children(':gt(").append(
								this.removeAllChildren_ - 1).append(
								")').remove();");
					}
				}
			}
			return this.var_;
		case Create:
			if (this.mode_ == DomElement.Mode.ModeCreate) {
				if (this.id_.length() != 0) {
					out.append(this.var_).append(".setAttribute('id', '")
							.append(this.id_).append("');\n");
				}
				this.setJavaScriptAttributes(out);
				this.setJavaScriptProperties(out, WApplication.getInstance());
			}
			return this.var_;
		case Update: {
			WApplication app = WApplication.getInstance();
			boolean childrenUpdated = false;
			if (this.mode_ == DomElement.Mode.ModeUpdate
					&& this.numManipulations_ == 1) {
				for (int i = 0; i < this.updatedChildren_.size(); ++i) {
					DomElement child = this.updatedChildren_.get(i);
					child.asJavaScript(out, DomElement.Priority.Update);
				}
				childrenUpdated = true;
				if (this.properties_.get(Property.PropertyStyleDisplay) != null) {
					String style = this.properties_
							.get(Property.PropertyStyleDisplay);
					if (style.equals("none")) {
						out.append("Wt3_3_4.hide('").append(this.id_).append(
								"');\n");
						return this.var_;
					} else {
						if (style.length() == 0) {
							out.append("Wt3_3_4.show('").append(this.id_)
									.append("');\n");
							return this.var_;
						} else {
							if (style.equals("inline")) {
								out.append("Wt3_3_4.inline('" + this.id_
										+ "');\n");
								return this.var_;
							} else {
								if (style.equals("block")) {
									out.append("Wt3_3_4.block('" + this.id_
											+ "');\n");
									return this.var_;
								}
							}
						}
					}
				} else {
					if (!this.javaScript_.isEmpty()) {
						out.append(this.javaScript_);
						return this.var_;
					}
				}
			}
			if (this.unwrapped_) {
				out.append("Wt3_3_4.unwrap('").append(this.id_).append("');\n");
			}
			this.processEvents(app);
			this.processProperties(app);
			if (this.replaced_ != null) {
				this.declare(out);
				String varr = this.replaced_.getCreateVar();
				StringBuilder insertJs = new StringBuilder();
				insertJs.append(this.var_).append(".parentNode.replaceChild(")
						.append(varr).append(',').append(this.var_).append(
								");\n");
				this.replaced_.createElement(out, app, insertJs.toString());
				if (this.unstubbed_) {
					out.append("Wt3_3_4.unstub(").append(this.var_).append(',')
							.append(varr).append(',').append(
									this.hideWithDisplay_ ? 1 : 0).append(
									");\n");
				}
				return this.var_;
			} else {
				if (this.insertBefore_ != null) {
					this.declare(out);
					String varr = this.insertBefore_.getCreateVar();
					StringBuilder insertJs = new StringBuilder();
					insertJs.append(this.var_).append(
							".parentNode.insertBefore(").append(varr).append(
							",").append(this.var_ + ");\n");
					this.insertBefore_.createElement(out, app, insertJs
							.toString());
					return this.var_;
				}
			}
			if (!this.childrenToSave_.isEmpty()) {
				this.declare(out);
				out.append("Wt3_3_4").append(".saveReparented(").append(
						this.var_).append(");");
			}
			for (int i = 0; i < this.childrenToSave_.size(); ++i) {
				out.append("var c").append(this.var_).append((int) i).append(
						'=').append("$('#").append(this.childrenToSave_.get(i))
						.append("')");
				if (app.getEnvironment().agentIsIE()) {
					out.append(".detach()");
				}
				out.append(";");
			}
			if (this.mode_ != DomElement.Mode.ModeCreate) {
				this.setJavaScriptProperties(out, app);
				this.setJavaScriptAttributes(out);
			}
			for (Iterator<Map.Entry<String, DomElement.EventHandler>> i_it = this.eventHandlers_
					.entrySet().iterator(); i_it.hasNext();) {
				Map.Entry<String, DomElement.EventHandler> i = i_it.next();
				if (this.mode_ == DomElement.Mode.ModeUpdate
						|| i.getValue().jsCode.length() != 0) {
					this.setJavaScriptEvent(out, i.getKey(), i.getValue(), app);
				}
			}
			this.renderInnerHtmlJS(out, app);
			for (int i = 0; i < this.childrenToSave_.size(); ++i) {
				out.append("$('#").append(this.childrenToSave_.get(i)).append(
						"').replaceWith(c").append(this.var_).append((int) i)
						.append(");");
			}
			this.renderDeferredJavaScript(out);
			if (!childrenUpdated) {
				for (int i = 0; i < this.updatedChildren_.size(); ++i) {
					DomElement child = this.updatedChildren_.get(i);
					child.asJavaScript(out, DomElement.Priority.Update);
				}
			}
			return this.var_;
		}
		}
		return this.var_;
	}

	/**
	 * Renders the element as HTML.
	 * <p>
	 * Anything that cannot be rendered as HTML is rendered as javaScript as a
	 * by-product.
	 */
	public void asHTML(final EscapeOStream out, final EscapeOStream javaScript,
			final List<DomElement.TimeoutEvent> timeouts, boolean openingTagOnly) {
		if (this.mode_ != DomElement.Mode.ModeCreate) {
			throw new WException("DomElement::asHTML() called with ModeUpdate");
		}
		WApplication app = WApplication.getInstance();
		this.processEvents(app);
		this.processProperties(app);
		DomElement.EventHandler clickEvent = this.eventHandlers_
				.get(WInteractWidget.CLICK_SIGNAL);
		boolean needButtonWrap = !app.getEnvironment().hasAjax()
				&& clickEvent != null && clickEvent.signalName.length() != 0
				&& !app.getEnvironment().agentIsSpiderBot();
		boolean isSubmit = needButtonWrap;
		DomElementType renderedType = this.type_;
		if (needButtonWrap) {
			if (this.type_ == DomElementType.DomElement_BUTTON) {
				DomElement self = this;
				self.setAttribute("type", "submit");
				self.setAttribute("name", "signal=" + clickEvent.signalName);
				needButtonWrap = false;
			} else {
				if (this.type_ == DomElementType.DomElement_IMG) {
					renderedType = DomElementType.DomElement_INPUT;
					DomElement self = this;
					self.setAttribute("type", "image");
					self
							.setAttribute("name", "signal="
									+ clickEvent.signalName);
					needButtonWrap = false;
				}
			}
		}
		if (needButtonWrap) {
			if (this.type_ == DomElementType.DomElement_AREA
					|| this.type_ == DomElementType.DomElement_INPUT
					|| this.type_ == DomElementType.DomElement_SELECT) {
				needButtonWrap = false;
			}
			if (this.type_ == DomElementType.DomElement_A) {
				String href = this.getAttribute("href");
				if (app.getEnvironment().getAgent() == WEnvironment.UserAgent.IE7
						|| app.getEnvironment().getAgent() == WEnvironment.UserAgent.IE8
						|| href.length() > 1) {
					needButtonWrap = false;
				} else {
					if (app.getTheme().isCanStyleAnchorAsButton()) {
						DomElement self = this;
						self.setAttribute("href", app
								.url(app.getInternalPath())
								+ "&signal=" + clickEvent.signalName);
						needButtonWrap = false;
					}
				}
			} else {
				if (this.type_ == DomElementType.DomElement_AREA) {
					DomElement self = this;
					self.setAttribute("href", app.url(app.getInternalPath())
							+ "&signal=" + clickEvent.signalName);
				}
			}
		}
		final boolean supportButton = true;
		boolean needAnchorWrap = false;
		if (!supportButton && this.type_ == DomElementType.DomElement_BUTTON) {
			renderedType = DomElementType.DomElement_INPUT;
			DomElement self = this;
			if (!isSubmit) {
				self.setAttribute("type", "button");
			}
			self.setAttribute("value", this.properties_
					.get(Property.PropertyInnerHTML));
			self.setProperty(Property.PropertyInnerHTML, "");
		}
		EscapeOStream attributeValues = out.push();
		attributeValues.pushEscape(EscapeOStream.RuleSet.HtmlAttribute);
		String style = "";
		if (needButtonWrap) {
			if (supportButton) {
				out.append("<button type=\"submit\" name=\"signal=");
				out.append(clickEvent.signalName, attributeValues);
				out.append("\" class=\"Wt-wrap ");
				String l = this.properties_.get(Property.PropertyClass);
				if (l != null) {
					out.append(l);
					final SortedMap<Property, String> map = this.properties_;
					map.remove(Property.PropertyClass);
				}
				out.append('"');
				String wrapStyle = this.getCssStyle();
				if (!this.isDefaultInline()) {
					wrapStyle += "display: block;";
				}
				if (wrapStyle.length() != 0) {
					out.append(" style=");
					fastHtmlAttributeValue(out, attributeValues, wrapStyle);
				}
				String i = this.properties_.get(Property.PropertyDisabled);
				if (i != null && i.equals("true")) {
					out.append(" disabled=\"disabled\"");
				}
				for (Iterator<Map.Entry<String, String>> j_it = this.attributes_
						.entrySet().iterator(); j_it.hasNext();) {
					Map.Entry<String, String> j = j_it.next();
					if (j.getKey().equals("title")) {
						out.append(' ').append(j.getKey()).append('=');
						fastHtmlAttributeValue(out, attributeValues, j
								.getValue());
					}
				}
				if (app.getEnvironment().getAgent() != WEnvironment.UserAgent.Konqueror
						&& !app.getEnvironment().agentIsWebKit()
						&& !app.getEnvironment().agentIsIE()) {
					style = "margin: 0px -3px -2px -3px;";
				}
				out.append("><").append(elementNames_[renderedType.getValue()]);
			} else {
				if (this.type_ == DomElementType.DomElement_IMG) {
					out.append("<input type=\"image\"");
				} else {
					out.append("<input type=\"submit\"");
				}
				out.append(" name=");
				fastHtmlAttributeValue(out, attributeValues, "signal="
						+ clickEvent.signalName);
				out.append(" value=");
				String i = this.properties_.get(Property.PropertyInnerHTML);
				if (i != null) {
					fastHtmlAttributeValue(out, attributeValues, i);
				} else {
					out.append("\"\"");
				}
			}
		} else {
			if (needAnchorWrap) {
				out.append("<a href=\"#\" class=\"Wt-wrap\" onclick=");
				fastHtmlAttributeValue(out, attributeValues, clickEvent.jsCode);
				out.append("><").append(elementNames_[renderedType.getValue()]);
			} else {
				if (renderedType == DomElementType.DomElement_OTHER) {
					out.append('<').append(this.elementTagName_);
				} else {
					out.append('<').append(
							elementNames_[renderedType.getValue()]);
				}
			}
		}
		if (this.id_.length() != 0) {
			out.append(" id=");
			fastHtmlAttributeValue(out, attributeValues, this.id_);
		}
		for (Iterator<Map.Entry<String, String>> i_it = this.attributes_
				.entrySet().iterator(); i_it.hasNext();) {
			Map.Entry<String, String> i = i_it.next();
			if (!app.getEnvironment().agentIsSpiderBot()
					|| !i.getKey().equals("name")) {
				out.append(' ').append(i.getKey()).append('=');
				fastHtmlAttributeValue(out, attributeValues, i.getValue());
			}
		}
		if (app.getEnvironment().hasAjax()) {
			for (Iterator<Map.Entry<String, DomElement.EventHandler>> i_it = this.eventHandlers_
					.entrySet().iterator(); i_it.hasNext();) {
				Map.Entry<String, DomElement.EventHandler> i = i_it.next();
				if (i.getValue().jsCode.length() != 0) {
					if (this.id_.equals(app.getDomRoot().getId())
							|| i.getKey() == WInteractWidget.WHEEL_SIGNAL
							&& app.getEnvironment().agentIsIE()
							&& app.getEnvironment().getAgent().getValue() >= WEnvironment.UserAgent.IE9
									.getValue()) {
						this.setJavaScriptEvent(javaScript, i.getKey(), i
								.getValue(), app);
					} else {
						out.append(" on").append(i.getKey()).append('=');
						fastHtmlAttributeValue(out, attributeValues, i
								.getValue().jsCode);
					}
				}
			}
		}
		String innerHTML = "";
		for (Iterator<Map.Entry<Property, String>> i_it = this.properties_
				.entrySet().iterator(); i_it.hasNext();) {
			Map.Entry<Property, String> i = i_it.next();
			switch (i.getKey()) {
			case PropertyInnerHTML:
				innerHTML += i.getValue();
				break;
			case PropertyDisabled:
				if (i.getValue().equals("true")) {
					out.append(" disabled=\"disabled\"");
				}
				break;
			case PropertyReadOnly:
				if (i.getValue().equals("true")) {
					out.append(" readonly=\"readonly\"");
				}
				break;
			case PropertyTabIndex:
				out.append(" tabindex=\"").append(i.getValue()).append('"');
				break;
			case PropertyChecked:
				if (i.getValue().equals("true")) {
					out.append(" checked=\"checked\"");
				}
				break;
			case PropertySelected:
				if (i.getValue().equals("true")) {
					out.append(" selected=\"selected\"");
				}
				break;
			case PropertySelectedIndex:
				if (i.getValue().equals("-1")) {
					DomElement self = this;
					self.callMethod("selectedIndex=-1");
				}
				break;
			case PropertyMultiple:
				if (i.getValue().equals("true")) {
					out.append(" multiple=\"multiple\"");
				}
				break;
			case PropertyTarget:
				out.append(" target=\"").append(i.getValue()).append("\"");
				break;
			case PropertyDownload:
				out.append(" download=\"").append(i.getValue()).append("\"");
				break;
			case PropertyIndeterminate:
				if (i.getValue().equals("true")) {
					DomElement self = this;
					self.callMethod("indeterminate=" + i.getValue());
				}
				break;
			case PropertyValue:
				if (this.type_ != DomElementType.DomElement_TEXTAREA) {
					out.append(" value=");
					fastHtmlAttributeValue(out, attributeValues, i.getValue());
				} else {
					innerHTML += i.getValue();
				}
				break;
			case PropertySrc:
				out.append(" src=");
				fastHtmlAttributeValue(out, attributeValues, i.getValue());
				break;
			case PropertyColSpan:
				out.append(" colspan=");
				fastHtmlAttributeValue(out, attributeValues, i.getValue());
				break;
			case PropertyRowSpan:
				out.append(" rowspan=");
				fastHtmlAttributeValue(out, attributeValues, i.getValue());
				break;
			case PropertyClass:
				out.append(" class=");
				fastHtmlAttributeValue(out, attributeValues, i.getValue());
				break;
			case PropertyLabel:
				out.append(" label=");
				fastHtmlAttributeValue(out, attributeValues, i.getValue());
				break;
			default:
				break;
			}
		}
		if (!needButtonWrap) {
			style += this.getCssStyle();
		}
		if (style.length() != 0) {
			out.append(" style=");
			fastHtmlAttributeValue(out, attributeValues, style);
		}
		if (needButtonWrap && !supportButton) {
			out.append(" />");
		} else {
			if (openingTagOnly) {
				out.append('>');
				return;
			}
			if (!isSelfClosingTag(renderedType)) {
				out.append('>');
				for (int i = 0; i < this.childrenToAdd_.size(); ++i) {
					this.childrenToAdd_.get(i).child.asHTML(out, javaScript,
							timeouts);
				}
				out.append(innerHTML);
				out.append(this.childrenHtml_.toString());
				if (renderedType == DomElementType.DomElement_DIV
						&& app.getEnvironment().getAgent() == WEnvironment.UserAgent.IE6
						&& innerHTML.length() == 0
						&& this.childrenToAdd_.isEmpty()
						&& this.childrenHtml_.isEmpty()) {
					out.append("&nbsp;");
				}
				if (renderedType == DomElementType.DomElement_OTHER) {
					out.append("</").append(this.elementTagName_).append(">");
				} else {
					out.append("</").append(
							elementNames_[renderedType.getValue()]).append(">");
				}
			} else {
				out.append(" />");
			}
			if (needButtonWrap && supportButton) {
				out.append("</button>");
			} else {
				if (needAnchorWrap) {
					out.append("</a>");
				}
			}
		}
		javaScript.append(this.javaScriptEvenWhenDeleted_).append(
				this.javaScript_);
		if (this.timeOut_ != -1) {
			timeouts.add(new DomElement.TimeoutEvent(this.timeOut_, this.id_,
					this.timeOutJSRepeat_));
		}
		timeouts.addAll(this.timeouts_);
	}

	/**
	 * Renders the element as HTML.
	 * <p>
	 * Calls
	 * {@link #asHTML(EscapeOStream out, EscapeOStream javaScript, List timeouts, boolean openingTagOnly)
	 * asHTML(out, javaScript, timeouts, false)}
	 */
	public final void asHTML(final EscapeOStream out,
			final EscapeOStream javaScript,
			final List<DomElement.TimeoutEvent> timeouts) {
		asHTML(out, javaScript, timeouts, false);
	}

	/**
	 * Creates the JavaScript statements for timer rendering.
	 */
	public static void createTimeoutJs(final StringBuilder out,
			final List<DomElement.TimeoutEvent> timeouts, WApplication app) {
		for (int i = 0; i < timeouts.size(); ++i) {
			out.append(app.getJavaScriptClass()).append("._p_.addTimerEvent('")
					.append(timeouts.get(i).event).append("', ").append(
							timeouts.get(i).msec).append(",").append(
							timeouts.get(i).repeat).append(");\n");
		}
	}

	/**
	 * Returns the default display property for this element.
	 * <p>
	 * This returns whether the element is by default an inline or block
	 * element.
	 */
	public boolean isDefaultInline() {
		return isDefaultInline(this.type_);
	}

	/**
	 * Declares the element.
	 * <p>
	 * Only after the element has been declared, {@link DomElement#getVar()
	 * getVar()} returns a useful JavaScript reference.
	 */
	public void declare(final EscapeOStream out) {
		if (this.var_.length() == 0) {
			out.append("var ").append(this.getCreateVar()).append(
					"=Wt3_3_4.$('").append(this.id_).append("');\n");
		}
	}

	/**
	 * Renders properties and attributes into CSS.
	 */
	public String getCssStyle() {
		if (this.properties_.isEmpty()) {
			return "";
		}
		EscapeOStream style = new EscapeOStream();
		String styleProperty = null;
		for (Iterator<Map.Entry<Property, String>> j_it = this.properties_
				.entrySet().iterator(); j_it.hasNext();) {
			Map.Entry<Property, String> j = j_it.next();
			if (j.getKey() == Property.PropertyStyle) {
				styleProperty = j.getValue();
			} else {
				if (j.getKey().getValue() >= Property.PropertyStylePosition
						.getValue()
						&& j.getKey().getValue() <= Property.PropertyStyleBoxSizing
								.getValue()) {
					if (j.getKey() == Property.PropertyStyleCursor
							&& j.getValue().equals("pointer")) {
						style.append("cursor:pointer;cursor:hand;");
					} else {
						if (j.getValue().length() != 0) {
							style.append(
									cssNames_[j.getKey().getValue()
											- Property.PropertyStylePosition
													.getValue()]).append(':')
									.append(j.getValue()).append(';');
							if (j.getKey().getValue() >= Property.PropertyStyleBoxSizing
									.getValue()) {
								WApplication app = WApplication.getInstance();
								if (app.getEnvironment().agentIsGecko()) {
									style.append("-moz-");
								} else {
									if (app.getEnvironment().agentIsWebKit()) {
										style.append("-webkit-");
									}
								}
								style
										.append(
												cssNames_[j.getKey().getValue()
														- Property.PropertyStylePosition
																.getValue()])
										.append(':').append(j.getValue())
										.append(';');
							}
						}
					}
				} else {
					if (j.getKey() == Property.PropertyStyleWidthExpression) {
						style.append("width:expression(").append(j.getValue())
								.append(");");
					}
				}
			}
		}
		if (styleProperty != null) {
			style.append(styleProperty);
		}
		return style.toString();
	}

	/**
	 * Utility for rapid rendering of JavaScript strings.
	 * <p>
	 * It uses pre-computed mixing rules for escaping of the string.
	 */
	public static void fastJsStringLiteral(final EscapeOStream outRaw,
			final EscapeOStream outEscaped, final String s) {
		outRaw.append('\'');
		outRaw.append(s, outEscaped);
		outRaw.append('\'');
	}

	/**
	 * Utility that renders a string as JavaScript literal.
	 */
	public static void jsStringLiteral(final EscapeOStream out, final String s,
			char delimiter) {
		out.append(delimiter);
		out
				.pushEscape(delimiter == '\'' ? EscapeOStream.RuleSet.JsStringLiteralSQuote
						: EscapeOStream.RuleSet.JsStringLiteralDQuote);
		out.append(s);
		out.popEscape();
		out.append(delimiter);
	}

	/**
	 * Utility that renders a string as JavaScript literal.
	 */
	public static void jsStringLiteral(final StringBuilder out, final String s,
			char delimiter) {
		EscapeOStream sout = new EscapeOStream(out);
		jsStringLiteral(sout, s, delimiter);
	}

	/**
	 * Utility for rapid rendering of HTML attribute values.
	 * <p>
	 * It uses pre-computed mixing rules for escaping of the attribute value.
	 */
	public static void fastHtmlAttributeValue(final EscapeOStream outRaw,
			final EscapeOStream outEscaped, final String s) {
		outRaw.append('"');
		outRaw.append(s, outEscaped);
		outRaw.append('"');
	}

	/**
	 * Utility that renders a string as HTML attribute.
	 */
	public static void htmlAttributeValue(final StringBuilder out,
			final String s) {
		EscapeOStream sout = new EscapeOStream(out);
		sout.pushEscape(EscapeOStream.RuleSet.HtmlAttribute);
		sout.append(s);
	}

	/**
	 * Returns whether a tag is self-closing in HTML.
	 */
	public static boolean isSelfClosingTag(final String tag) {
		return tag.equals("br") || tag.equals("hr") || tag.equals("img")
				|| tag.equals("area") || tag.equals("col")
				|| tag.equals("input");
	}

	/**
	 * Returns whether a tag is self-closing in HTML.
	 */
	public static boolean isSelfClosingTag(DomElementType element) {
		return element == DomElementType.DomElement_BR
				|| element == DomElementType.DomElement_IMG
				|| element == DomElementType.DomElement_AREA
				|| element == DomElementType.DomElement_COL
				|| element == DomElementType.DomElement_INPUT;
	}

	/**
	 * Parses a tag name to a DOMElement type.
	 */
	public static DomElementType parseTagName(final String tag) {
		for (int i = 0; i < DomElementType.DomElement_UNKNOWN.getValue(); ++i) {
			if (tag.equals(elementNames_[i])) {
				return DomElementType.values()[i];
			}
		}
		return DomElementType.DomElement_UNKNOWN;
	}

	/**
	 * Returns the tag name for a DOMElement type.
	 */
	public static String tagName(DomElementType type) {
		return elementNames_[type.getValue()];
	}

	/**
	 * Returns the name for a CSS property, as a string.
	 */
	public static String cssName(Property property) {
		return cssNames_[property.getValue()
				- Property.PropertyStylePosition.getValue()];
	}

	/**
	 * Returns whether a paritcular element is by default inline.
	 */
	public static boolean isDefaultInline(DomElementType type) {
		return defaultInline_[type.getValue()];
	}

	/**
	 * Returns all custom JavaScript collected in this element.
	 */
	public String getJavaScript() {
		return this.javaScript_.toString();
	}

	/**
	 * Something to do with broken IE Mobile 5 browsers...
	 */
	public void updateInnerHtmlOnly() {
		this.mode_ = DomElement.Mode.ModeUpdate;
		assert this.replaced_ == null;
		assert this.insertBefore_ == null;
		this.attributes_.clear();
		this.removedAttributes_.clear();
		this.eventHandlers_.clear();
		for (Iterator<Map.Entry<Property, String>> i_it = this.properties_
				.entrySet().iterator(); i_it.hasNext();) {
			Map.Entry<Property, String> i = i_it.next();
			if (i.getKey() == Property.PropertyInnerHTML
					|| i.getKey() == Property.PropertyTarget) {
			} else {
				i_it.remove();
			}
		}
	}

	/**
	 * Adds an element to a parent, using suitable methods.
	 * <p>
	 * Depending on the type, different DOM methods are needed. In particular
	 * for table cells, some browsers require dedicated API instead of generic
	 * insertAt() or appendChild() functions.
	 */
	public String addToParent(final StringBuilder out, final String parentVar,
			int pos, WApplication app) {
		EscapeOStream sout = new EscapeOStream(out);
		return this.addToParent(sout, parentVar, pos, app);
	}

	/**
	 * Renders the element as JavaScript, and inserts it in the DOM.
	 */
	public void createElement(final StringBuilder out, WApplication app,
			final String domInsertJS) {
		EscapeOStream sout = new EscapeOStream(out);
		this.createElement(sout, app, domInsertJS);
	}

	/**
	 * Allocates a JavaScript variable.
	 */
	public String getCreateVar() {
		this.var_ = "j" + String.valueOf(nextId_++);
		return this.var_;
	}

	static class EventHandler {
		private static Logger logger = LoggerFactory
				.getLogger(EventHandler.class);

		public String jsCode;
		public String signalName;

		public EventHandler() {
			this.jsCode = "";
			this.signalName = "";
		}

		public EventHandler(final String j, final String sn) {
			this.jsCode = j;
			this.signalName = sn;
		}
	}

	private boolean willRenderInnerHtmlJS(WApplication app) {
		return !this.childrenHtml_.isEmpty() || this.wasEmpty_
				&& this.canWriteInnerHTML(app);
	}

	private boolean canWriteInnerHTML(WApplication app) {
		if ((app.getEnvironment().agentIsIE() || app.getEnvironment()
				.getAgent() == WEnvironment.UserAgent.Konqueror)
				&& (this.type_ == DomElementType.DomElement_TBODY
						|| this.type_ == DomElementType.DomElement_THEAD
						|| this.type_ == DomElementType.DomElement_TABLE
						|| this.type_ == DomElementType.DomElement_COLGROUP
						|| this.type_ == DomElementType.DomElement_TR
						|| this.type_ == DomElementType.DomElement_SELECT
						|| this.type_ == DomElementType.DomElement_TD || this.type_ == DomElementType.DomElement_OPTGROUP)) {
			return false;
		}
		return true;
	}

	// private boolean containsElement(DomElementType type) ;
	private void processEvents(WApplication app) {
		DomElement self = this;
		String S_keypress = WInteractWidget.KEYPRESS_SIGNAL;
		DomElement.EventHandler keypress = this.eventHandlers_.get(S_keypress);
		if (keypress != null && keypress.jsCode.length() != 0) {
			MapUtils.access(self.eventHandlers_, S_keypress,
					DomElement.EventHandler.class).jsCode = "if (Wt3_3_4.isKeyPress(event)){"
					+ MapUtils.access(self.eventHandlers_, S_keypress,
							DomElement.EventHandler.class).jsCode + '}';
		}
	}

	private void processProperties(WApplication app) {
		if (this.minMaxSizeProperties_
				&& app.getEnvironment().getAgent() == WEnvironment.UserAgent.IE6) {
			DomElement self = this;
			String w = self.properties_.get(Property.PropertyStyleWidth);
			String minw = self.properties_.get(Property.PropertyStyleMinWidth);
			String maxw = self.properties_.get(Property.PropertyStyleMaxWidth);
			if (minw != null || maxw != null) {
				if (w == null) {
					StringBuilder expr = new StringBuilder();
					expr.append("Wt3_3_4.IEwidth(this,");
					if (minw != null) {
						expr.append('\'').append(minw).append('\'');
						self.properties_.remove(Property.PropertyStyleMinWidth);
					} else {
						expr.append("'0px'");
					}
					expr.append(',');
					if (maxw != null) {
						expr.append('\'').append(maxw).append('\'');
						self.properties_.remove(Property.PropertyStyleMaxWidth);
					} else {
						expr.append("'100000px'");
					}
					expr.append(")");
					self.properties_.remove(Property.PropertyStyleWidth);
					self.properties_.put(Property.PropertyStyleWidthExpression,
							expr.toString());
				}
			}
			String i = self.properties_.get(Property.PropertyStyleMinHeight);
			if (i != null) {
				self.properties_.put(Property.PropertyStyleHeight, i);
			}
		}
	}

	private void setJavaScriptProperties(final EscapeOStream out,
			WApplication app) {
		EscapeOStream escaped = out.push();
		boolean pushed = false;
		for (Iterator<Map.Entry<Property, String>> i_it = this.properties_
				.entrySet().iterator(); i_it.hasNext();) {
			Map.Entry<Property, String> i = i_it.next();
			this.declare(out);
			switch (i.getKey()) {
			case PropertyInnerHTML:
			case PropertyAddedInnerHTML:
				if (this.willRenderInnerHtmlJS(app)) {
					break;
				}
				out.append("Wt3_3_4.setHtml(").append(this.var_).append(',');
				if (!pushed) {
					escaped
							.pushEscape(EscapeOStream.RuleSet.JsStringLiteralSQuote);
					pushed = true;
				}
				fastJsStringLiteral(out, escaped, i.getValue());
				if (i.getKey() == Property.PropertyInnerHTML) {
					out.append(",false");
				} else {
					out.append(",true");
				}
				out.append(");");
				break;
			case PropertyValue:
				out.append(this.var_).append(".value=");
				if (!pushed) {
					escaped
							.pushEscape(EscapeOStream.RuleSet.JsStringLiteralSQuote);
					pushed = true;
				}
				fastJsStringLiteral(out, escaped, i.getValue());
				out.append(';');
				break;
			case PropertyTarget:
				out.append(this.var_).append(".target='").append(i.getValue())
						.append("';");
				break;
			case PropertyIndeterminate:
				out.append(this.var_).append(".indeterminate=").append(
						i.getValue()).append(";");
				break;
			case PropertyDisabled:
				if (this.type_ == DomElementType.DomElement_A) {
					if (i.getValue().equals("true")) {
						out.append(this.var_).append(
								".setAttribute('disabled', 'disabled');");
					} else {
						out.append(this.var_).append(
								".removeAttribute('disabled', 'disabled');");
					}
				} else {
					out.append(this.var_).append(".disabled=").append(
							i.getValue()).append(';');
				}
				break;
			case PropertyReadOnly:
				out.append(this.var_).append(".readOnly=").append(i.getValue())
						.append(';');
				break;
			case PropertyTabIndex:
				out.append(this.var_).append(".tabIndex=").append(i.getValue())
						.append(';');
				break;
			case PropertyChecked:
				out.append(this.var_).append(".checked=").append(i.getValue())
						.append(';');
				break;
			case PropertySelected:
				out.append(this.var_).append(".selected=").append(i.getValue())
						.append(';');
				break;
			case PropertySelectedIndex:
				out.append(this.var_).append(".selectedIndex=").append(
						i.getValue()).append(';');
				break;
			case PropertyMultiple:
				out.append(this.var_).append(".multiple=").append(i.getValue())
						.append(';');
				break;
			case PropertySrc:
				out.append(this.var_).append(".src='").append(i.getValue())
						.append("\';");
				break;
			case PropertyColSpan:
				out.append(this.var_).append(".colSpan=").append(i.getValue())
						.append(";");
				break;
			case PropertyRowSpan:
				out.append(this.var_).append(".rowSpan=").append(i.getValue())
						.append(";");
				break;
			case PropertyClass:
				out.append(this.var_).append(".className=");
				if (!pushed) {
					escaped
							.pushEscape(EscapeOStream.RuleSet.JsStringLiteralSQuote);
					pushed = true;
				}
				fastJsStringLiteral(out, escaped, i.getValue());
				out.append(';');
				break;
			case PropertyStyleFloat:
				out.append(this.var_).append(".style.");
				if (app.getEnvironment().agentIsIE()) {
					out.append("styleFloat");
				} else {
					out.append("cssFloat");
				}
				out.append("=\'").append(i.getValue()).append("\';");
				break;
			case PropertyStyleWidthExpression:
				out.append(this.var_).append(".style.setExpression('width',");
				if (!pushed) {
					escaped
							.pushEscape(EscapeOStream.RuleSet.JsStringLiteralSQuote);
					pushed = true;
				}
				fastJsStringLiteral(out, escaped, i.getValue());
				out.append(");");
				break;
			default:
				if (i.getKey().getValue() >= Property.PropertyStyle.getValue()
						&& i.getKey().getValue() <= Property.PropertyStyleBoxSizing
								.getValue()) {
					if (app.getEnvironment().getAgent() == WEnvironment.UserAgent.IE6) {
						out.append(this.var_).append(".style['").append(
								cssNames_[i.getKey().getValue()
										- Property.PropertyStylePosition
												.getValue()]).append("']='")
								.append(i.getValue()).append("';");
					} else {
						out.append(this.var_).append(".style.").append(
								cssCamelNames_[i.getKey().getValue()
										- Property.PropertyStyle.getValue()])
								.append("='").append(i.getValue()).append("';");
					}
				}
			}
			out.append('\n');
		}
	}

	private void setJavaScriptAttributes(final EscapeOStream out) {
		for (Iterator<Map.Entry<String, String>> i_it = this.attributes_
				.entrySet().iterator(); i_it.hasNext();) {
			Map.Entry<String, String> i = i_it.next();
			this.declare(out);
			if (i.getKey().equals("style")) {
				out.append(this.var_).append(".style.cssText = ");
				jsStringLiteral(out, i.getValue(), '\'');
				out.append(';').append('\n');
			} else {
				out.append(this.var_).append(".setAttribute('").append(
						i.getKey()).append("',");
				jsStringLiteral(out, i.getValue(), '\'');
				out.append(");\n");
			}
		}
		for (Iterator<String> i_it = this.removedAttributes_.iterator(); i_it
				.hasNext();) {
			String i = i_it.next();
			this.declare(out);
			out.append(this.var_).append(".removeAttribute('").append(i)
					.append("');\n");
		}
	}

	private void setJavaScriptEvent(final EscapeOStream out, String eventName,
			final DomElement.EventHandler handler, WApplication app) {
		boolean globalUnfocused = this.id_.equals(app.getDomRoot().getId());
		int fid = nextId_++;
		out.append("function f").append(fid).append("(event) { ");
		if (globalUnfocused) {
			out
					.append("var g=event||window.event; var t=g.target||g.srcElement;if ((!t||Wt3_3_4.hasTag(t,'DIV') ||Wt3_3_4.hasTag(t,'BODY') ||Wt3_3_4.hasTag(t,'HTML'))) {");
		}
		out.append(handler.jsCode);
		if (globalUnfocused) {
			out.append('}');
		}
		out.append("}\n");
		if (globalUnfocused) {
			out.append("document");
		} else {
			this.declare(out);
			out.append(this.var_);
		}
		if (eventName == WInteractWidget.WHEEL_SIGNAL
				&& app.getEnvironment().agentIsIE()
				&& app.getEnvironment().getAgent().getValue() >= WEnvironment.UserAgent.IE9
						.getValue()) {
			out.append(".addEventListener('wheel', f").append(fid).append(
					", false);\n");
		} else {
			out.append(".on").append(eventName).append("=f").append(fid)
					.append(";\n");
		}
	}

	private void createElement(final EscapeOStream out, WApplication app,
			final String domInsertJS) {
		if (this.var_.length() == 0) {
			this.getCreateVar();
		}
		out.append("var ").append(this.var_).append("=");
		if (app.getEnvironment().agentIsIE()
				&& app.getEnvironment().getAgent().getValue() <= WEnvironment.UserAgent.IE8
						.getValue()
				&& this.type_ != DomElementType.DomElement_TEXTAREA) {
			out.append("document.createElement('");
			out.pushEscape(EscapeOStream.RuleSet.JsStringLiteralSQuote);
			List<DomElement.TimeoutEvent> timeouts = new ArrayList<DomElement.TimeoutEvent>();
			EscapeOStream dummy = new EscapeOStream();
			this.asHTML(out, dummy, timeouts, true);
			out.popEscape();
			out.append("');");
			out.append(domInsertJS);
			this.renderInnerHtmlJS(out, app);
			this.renderDeferredJavaScript(out);
		} else {
			out.append("document.createElement('").append(
					elementNames_[this.type_.getValue()]).append("');");
			out.append(domInsertJS);
			this.asJavaScript(out, DomElement.Priority.Create);
			this.asJavaScript(out, DomElement.Priority.Update);
		}
	}

	private String addToParent(final EscapeOStream out, final String parentVar,
			int pos, WApplication app) {
		this.getCreateVar();
		if (this.type_ == DomElementType.DomElement_TD
				|| this.type_ == DomElementType.DomElement_TR) {
			out.append("var ").append(this.var_).append("=");
			if (this.type_ == DomElementType.DomElement_TD) {
				out.append(parentVar).append(".insertCell(").append(pos)
						.append(");\n");
			} else {
				out.append(parentVar).append(".insertRow(").append(pos).append(
						");\n");
			}
			this.asJavaScript(out, DomElement.Priority.Create);
			this.asJavaScript(out, DomElement.Priority.Update);
		} else {
			StringBuilder insertJS = new StringBuilder();
			if (pos != -1) {
				insertJS.append("Wt3_3_4.insertAt(").append(parentVar).append(
						",").append(this.var_).append(",").append(pos).append(
						");");
			} else {
				insertJS.append(parentVar).append(".appendChild(").append(
						this.var_).append(");\n");
			}
			this.createElement(out, app, insertJS.toString());
		}
		return this.var_;
	}

	// private String createAsJavaScript(final EscapeOStream out, final String
	// parentVar, int pos, WApplication app) ;
	private void renderInnerHtmlJS(final EscapeOStream out, WApplication app) {
		if (this.willRenderInnerHtmlJS(app)) {
			String innerHTML = "";
			if (!this.properties_.isEmpty()) {
				String i = this.properties_.get(Property.PropertyInnerHTML);
				if (i != null) {
					innerHTML += i;
				}
				i = this.properties_.get(Property.PropertyAddedInnerHTML);
				if (i != null) {
					innerHTML += i;
				}
			}
			if (this.type_ == DomElementType.DomElement_DIV
					&& app.getEnvironment().getAgent() == WEnvironment.UserAgent.IE6
					|| !this.childrenToAdd_.isEmpty()
					|| !this.childrenHtml_.isEmpty() || innerHTML.length() != 0) {
				this.declare(out);
				out.append("Wt3_3_4.setHtml(").append(this.var_).append(",'");
				out.pushEscape(EscapeOStream.RuleSet.JsStringLiteralSQuote);
				List<DomElement.TimeoutEvent> timeouts = new ArrayList<DomElement.TimeoutEvent>();
				EscapeOStream js = new EscapeOStream();
				for (int i = 0; i < this.childrenToAdd_.size(); ++i) {
					this.childrenToAdd_.get(i).child.asHTML(out, js, timeouts);
				}
				out.append(innerHTML);
				out.append(this.childrenHtml_.toString());
				if (this.type_ == DomElementType.DomElement_DIV
						&& app.getEnvironment().getAgent() == WEnvironment.UserAgent.IE6
						&& this.childrenToAdd_.isEmpty()
						&& innerHTML.length() == 0
						&& this.childrenHtml_.isEmpty()) {
					out.append("&nbsp;");
				}
				out.popEscape();
				out.append("');\n");
				timeouts.addAll(this.timeouts_);
				for (int i = 0; i < timeouts.size(); ++i) {
					out.append(app.getJavaScriptClass()).append(
							"._p_.addTimerEvent('").append(
							timeouts.get(i).event).append("', ").append(
							timeouts.get(i).msec).append(',').append(
							timeouts.get(i).repeat).append(");\n");
				}
				out.append(js);
			}
		} else {
			for (int i = 0; i < this.childrenToAdd_.size(); ++i) {
				this.declare(out);
				DomElement child = this.childrenToAdd_.get(i).child;
				child.addToParent(out, this.var_,
						this.childrenToAdd_.get(i).pos, app);
			}
		}
		if (this.timeOut_ != -1) {
			out.append(app.getJavaScriptClass()).append("._p_.addTimerEvent('")
					.append(this.id_).append("', ").append(this.timeOut_)
					.append(',').append(this.timeOutJSRepeat_).append(");\n");
		}
	}

	private void renderDeferredJavaScript(final EscapeOStream out) {
		if (!this.javaScript_.isEmpty()) {
			this.declare(out);
			out.append(this.javaScript_).append('\n');
		}
	}

	private DomElement.Mode mode_;
	private boolean wasEmpty_;
	private int removeAllChildren_;
	private boolean hideWithDisplay_;
	private boolean minMaxSizeProperties_;
	private boolean unstubbed_;
	private boolean unwrapped_;
	private DomElement replaced_;
	private DomElement insertBefore_;
	private DomElementType type_;
	private String id_;
	private int numManipulations_;
	private int timeOut_;
	private boolean timeOutJSRepeat_;
	private EscapeOStream javaScript_;
	private String javaScriptEvenWhenDeleted_;
	private String var_;
	private boolean declared_;
	private Map<String, String> attributes_;
	private Set<String> removedAttributes_;
	private SortedMap<Property, String> properties_;
	private Map<String, DomElement.EventHandler> eventHandlers_;

	static class ChildInsertion {
		private static Logger logger = LoggerFactory
				.getLogger(ChildInsertion.class);

		public int pos;
		public DomElement child;

		public ChildInsertion() {
			this.pos = 0;
			this.child = null;
		}

		public ChildInsertion(int p, DomElement c) {
			this.pos = p;
			this.child = c;
		}
	}

	private List<DomElement.ChildInsertion> childrenToAdd_;
	private List<String> childrenToSave_;
	private List<DomElement> updatedChildren_;
	private EscapeOStream childrenHtml_;
	private List<DomElement.TimeoutEvent> timeouts_;
	private String elementTagName_;
	private static int nextId_ = 0;
	private static String[] elementNames_ = { "a", "br", "button", "col",
			"colgroup", "div", "fieldset", "form", "h1", "h2", "h3", "h4",
			"h5", "h6", "iframe", "img", "input", "label", "legend", "li",
			"ol", "option", "ul", "script", "select", "span", "table", "tbody",
			"thead", "tfoot", "th", "td", "textarea", "optgroup", "tr", "p",
			"canvas", "map", "area", "style", "object", "param", "audio",
			"video", "source", "b", "strong", "em", "i", "hr" };
	private static boolean[] defaultInline_ = { true, false, true, false,
			false, false, false, false, false, false, false, false, false,
			false, true, true, true, true, true, false, false, true, false,
			false, true, true, false, false, false, false, false, false, true,
			true, false, false, true, false, true, true, false, false, false,
			false, false, true, true, true, true, false };
	private static String[] cssNames_ = { "position", "z-index", "float",
			"clear", "width", "height", "line-height", "min-width",
			"min-height", "max-width", "max-height", "left", "right", "top",
			"bottom", "vertical-align", "text-align", "padding", "padding-top",
			"padding-right", "padding-bottom", "padding-left", "margin-top",
			"margin-right", "margin-bottom", "margin-left", "cursor",
			"border-top", "border-right", "border-bottom", "border-left",
			"border-color-top", "border-color-right", "border-color-bottom",
			"border-color-left", "border-width-top", "border-width-right",
			"border-width-bottom", "border-width-left", "color", "overflow-x",
			"overflow-y", "opacity", "font-family", "font-style",
			"font-variant", "font-weight", "font-size", "background-color",
			"background-image", "background-repeat", "background-attachment",
			"background-position", "text-decoration", "white-space",
			"table-layout", "border-spacing", "border-collapse",
			"page-break-before", "page-break-after", "zoom", "visibility",
			"display", "box-sizing" };
	private static String[] cssCamelNames_ = { "cssText", "width", "position",
			"zIndex", "cssFloat", "clear", "width", "height", "lineHeight",
			"minWidth", "minHeight", "maxWidth", "maxHeight", "left", "right",
			"top", "bottom", "verticalAlign", "textAlign", "padding",
			"paddingTop", "paddingRight", "paddingBottom", "paddingLeft",
			"marginTop", "marginRight", "marginBottom", "marginLeft", "cursor",
			"borderTop", "borderRight", "borderBottom", "borderLeft",
			"borderColorTop", "borderColorRight", "borderColorBottom",
			"borderColorLeft", "borderWidthTop", "borderWidthRight",
			"borderWidthBottom", "borderWidthLeft", "color", "overflowX",
			"overflowY", "opacity", "fontFamily", "fontStyle", "fontVariant",
			"fontWeight", "fontSize", "backgroundColor", "backgroundImage",
			"backgroundRepeat", "backgroundAttachment", "backgroundPosition",
			"textDecoration", "whiteSpace", "tableLayout", "borderSpacing",
			"border-collapse", "pageBreakBefore", "pageBreakAfter", "zoom",
			"visibility", "display", "boxSizing" };
	private static final String unsafeChars_ = " $&+,:;=?@'\"<>#%{}|\\^~[]`/";

	static char hexLookup(int n) {
		return "0123456789abcdef".charAt(n & 0xF);
	}
}
