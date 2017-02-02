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
 * Standard delegate class for rendering a view item.
 * <p>
 * 
 * This class provides the standard implementation for rendering an item (as in
 * a {@link WAbstractItemView}), and renders data provided by the standard data
 * roles (see ItemDataRole). It also provides default editing support using a
 * line edit.
 * <p>
 * You may provide special editing support for an item by specializing the
 * widget and reimplement
 * {@link WItemDelegate#createEditor(WModelIndex index, EnumSet flags)
 * createEditor()},
 * {@link WItemDelegate#setModelData(Object editState, WAbstractItemModel model, WModelIndex index)
 * setModelData()}, {@link WItemDelegate#getEditState(WWidget editor)
 * getEditState()}, and
 * {@link WItemDelegate#setEditState(WWidget editor, Object value)
 * setEditState()}.
 */
public class WItemDelegate extends WAbstractItemDelegate {
	private static Logger logger = LoggerFactory.getLogger(WItemDelegate.class);

	/**
	 * Create an item delegate.
	 */
	public WItemDelegate(WObject parent) {
		super(parent);
		this.textFormat_ = "";
	}

	/**
	 * Create an item delegate.
	 * <p>
	 * Calls {@link #WItemDelegate(WObject parent) this((WObject)null)}
	 */
	public WItemDelegate() {
		this((WObject) null);
	}

	/**
	 * Creates or updates a widget that renders an item.
	 * <p>
	 * The following properties of an item are rendered:
	 * <p>
	 * <ul>
	 * <li>text using the {@link ItemDataRole#DisplayRole} data, with the format
	 * specified by {@link WItemDelegate#setTextFormat(String format)
	 * setTextFormat()}</li>
	 * <li>a check box depending on the {@link ItemFlag#ItemIsUserCheckable}
	 * flag and {@link ItemDataRole#CheckStateRole} data</li>
	 * <li>an anchor depending on Wt::InternalPathRole or Wt::UrlRole values</li>
	 * <li>an icon depending on the value of {@link ItemDataRole#DecorationRole}
	 * </li>
	 * <li>a tooltip depending on the value of {@link ItemDataRole#ToolTipRole}</li>
	 * <li>a custom style class depending on the value of
	 * {@link ItemDataRole#StyleClassRole}</li>
	 * </ul>
	 * <p>
	 * When the flags indicates {@link ViewItemRenderFlag#RenderEditing}, then
	 * {@link WItemDelegate#createEditor(WModelIndex index, EnumSet flags)
	 * createEditor()} is called to create a suitable editor for editing the
	 * item.
	 */
	public WWidget update(WWidget widget, final WModelIndex index,
			EnumSet<ViewItemRenderFlag> flags) {
		boolean editing = widget != null && widget.find("t") == null;
		if (!EnumUtils.mask(flags, ViewItemRenderFlag.RenderEditing).isEmpty()) {
			if (!editing) {
				widget = this.createEditor(index, flags);
				WInteractWidget iw = ((widget) instanceof WInteractWidget ? (WInteractWidget) (widget)
						: null);
				if (iw != null) {
					iw.mouseWentDown().preventPropagation();
					iw.clicked().preventPropagation();
				}
			}
		} else {
			if (editing) {
				widget = null;
			}
		}
		WItemDelegate.WidgetRef widgetRef = new WItemDelegate.WidgetRef(widget);
		boolean isNew = false;
		boolean haveCheckBox = (index != null) ? !(index
				.getData(ItemDataRole.CheckStateRole) == null) : false;
		boolean haveLink = (index != null) ? !(index
				.getData(ItemDataRole.LinkRole) == null) : false;
		boolean haveIcon = (index != null) ? !(index
				.getData(ItemDataRole.DecorationRole) == null) : false;
		if (!!EnumUtils.mask(flags, ViewItemRenderFlag.RenderEditing).isEmpty()) {
			if (widgetRef.w != null) {
				if (haveCheckBox != (this.checkBox(widgetRef, index, false) != null)
						|| haveLink != (this.anchorWidget(widgetRef, index,
								false) != null)
						|| haveIcon != (this
								.iconWidget(widgetRef, index, false) != null)) {
					widgetRef.w = null;
				}
			}
			if (!(widgetRef.w != null)) {
				isNew = true;
				IndexText t = new IndexText(index);
				t.setObjectName("t");
				if ((index != null)
						&& !!EnumUtils.mask(index.getFlags(),
								ItemFlag.ItemIsXHTMLText).isEmpty()) {
					t.setTextFormat(TextFormat.PlainText);
				}
				t.setWordWrap(true);
				widgetRef.w = t;
			}
			if (!(index != null)) {
				return widgetRef.w;
			}
			Object checkedData = index.getData(ItemDataRole.CheckStateRole);
			if (!(checkedData == null)) {
				CheckState state = checkedData.getClass().equals(Boolean.class) ? ((Boolean) checkedData) ? CheckState.Checked
						: CheckState.Unchecked
						: checkedData.getClass().equals(CheckState.class) ? ((CheckState) checkedData)
								: CheckState.Unchecked;
				IndexCheckBox icb = this.checkBox(
						widgetRef,
						index,
						true,
						true,
						!EnumUtils.mask(index.getFlags(),
								ItemFlag.ItemIsTristate).isEmpty());
				icb.setCheckState(state);
				icb.setEnabled(!EnumUtils.mask(index.getFlags(),
						ItemFlag.ItemIsUserCheckable).isEmpty());
			} else {
				if (!isNew) {
					if (this.checkBox(widgetRef, index, false) != null)
						this.checkBox(widgetRef, index, false).remove();
				}
			}
			Object linkData = index.getData(ItemDataRole.LinkRole);
			if (!(linkData == null)) {
				WLink link = ((WLink) linkData);
				IndexAnchor a = this.anchorWidget(widgetRef, index, true);
				a.setLink(link);
				a.setTarget(link.getTarget());
			}
			IndexText t = this.textWidget(widgetRef, index);
			WString label = StringUtils.asString(index.getData(),
					this.textFormat_);
			if ((label.length() == 0) && haveCheckBox) {
				label = new WString(" ");
			}
			t.setText(label);
			String iconUrl = StringUtils.asString(
					index.getData(ItemDataRole.DecorationRole)).toString();
			if (iconUrl.length() != 0) {
				this.iconWidget(widgetRef, index, true).setImageLink(
						new WLink(iconUrl));
			} else {
				if (!isNew) {
					if (this.iconWidget(widgetRef, index, false) != null)
						this.iconWidget(widgetRef, index, false).remove();
				}
			}
		}
		if (!EnumUtils.mask(index.getFlags(), ItemFlag.ItemHasDeferredTooltip)
				.isEmpty()) {
			widgetRef.w.setDeferredToolTip(true,
					!EnumUtils.mask(index.getFlags(), ItemFlag.ItemIsXHTMLText)
							.isEmpty() ? TextFormat.XHTMLText
							: TextFormat.PlainText);
		} else {
			WString tooltip = StringUtils.asString(index
					.getData(ItemDataRole.ToolTipRole));
			if (!(tooltip.length() == 0) || !isNew) {
				widgetRef.w
						.setToolTip(
								tooltip,
								!EnumUtils.mask(index.getFlags(),
										ItemFlag.ItemIsXHTMLText).isEmpty() ? TextFormat.XHTMLText
										: TextFormat.PlainText);
			}
		}
		String sc = StringUtils.asString(
				index.getData(ItemDataRole.StyleClassRole)).toString();
		if (!EnumUtils.mask(flags, ViewItemRenderFlag.RenderSelected).isEmpty()) {
			sc += " " + WApplication.getInstance().getTheme().getActiveClass();
		}
		if (!EnumUtils.mask(flags, ViewItemRenderFlag.RenderEditing).isEmpty()) {
			sc += " Wt-delegate-edit";
		}
		widgetRef.w.setStyleClass(sc);
		if (!EnumUtils.mask(index.getFlags(), ItemFlag.ItemIsDropEnabled)
				.isEmpty()) {
			widgetRef.w.setAttributeValue("drop",
					new WString("true").toString());
		} else {
			if (widgetRef.w.getAttributeValue("drop").length() != 0) {
				widgetRef.w.setAttributeValue("drop",
						new WString("f").toString());
			}
		}
		return widgetRef.w;
	}

	public void updateModelIndex(WWidget widget, final WModelIndex index) {
		WItemDelegate.WidgetRef w = new WItemDelegate.WidgetRef(widget);
		if (!EnumUtils.mask(index.getFlags(), ItemFlag.ItemIsUserCheckable)
				.isEmpty()) {
			IndexCheckBox cb = this.checkBox(w, index, false);
			if (cb != null) {
				cb.setIndex(index);
			}
		}
		if (!EnumUtils.mask(index.getFlags(), ItemFlag.ItemHasDeferredTooltip)
				.isEmpty()) {
			IndexText text = ((widget) instanceof IndexText ? (IndexText) (widget)
					: null);
			if (text != null) {
				text.setIndex(index);
			}
			IndexAnchor anchor = ((widget) instanceof IndexAnchor ? (IndexAnchor) (widget)
					: null);
			if (anchor != null) {
				anchor.setIndex(index);
			}
			IndexContainerWidget c = ((widget) instanceof IndexContainerWidget ? (IndexContainerWidget) (widget)
					: null);
			if (c != null) {
				c.setIndex(index);
			}
		}
	}

	/**
	 * Sets the text format string.
	 * <p>
	 * The DisplayRole data is converted to a string using
	 * {@link StringUtils#asString(Object)}, passing the given format. If the
	 * format is an empty string, this corresponds to {@link Object#toString()}.
	 * <p>
	 * The default value is &quot;&quot;.
	 */
	public void setTextFormat(final String format) {
		this.textFormat_ = format;
	}

	/**
	 * Returns the text format string.
	 * <p>
	 * 
	 * @see WItemDelegate#setTextFormat(String format)
	 */
	public String getTextFormat() {
		return this.textFormat_;
	}

	/**
	 * Saves the edited data to the model.
	 * <p>
	 * The default implementation saves the current edit value to the model. You
	 * will need to reimplement this method for a custom editor.
	 * <p>
	 * As an example of how to deal with a specialized editor, consider the
	 * default implementation:
	 * <p>
	 * 
	 * <pre>
	 *   {@code
	 *    public void setModelData(Object editState, WAbstractItemModel model, WModelIndex index) {
	 *      model.setData(index, editState, ItemDataRole.EditRole);
	 *    }
	 *   }
	 * </pre>
	 * <p>
	 * 
	 * @see WItemDelegate#createEditor(WModelIndex index, EnumSet flags)
	 * @see WItemDelegate#getEditState(WWidget editor)
	 */
	public void setModelData(final Object editState, WAbstractItemModel model,
			final WModelIndex index) {
		model.setData(index, editState, ItemDataRole.EditRole);
	}

	/**
	 * Returns the current edit state.
	 * <p>
	 * The default implementation returns the current text in the line edit. You
	 * will need to reimplement this method for a custom editor.
	 * <p>
	 * As an example of how to deal with a specialized editor, consider the
	 * default implementation:
	 * <p>
	 * 
	 * <pre>
	 *   {@code
	 *    public Object getEditState(WWidget editor) {
	 *      WContainerWidget w = (WContainerWidget) editor;
	 *      WLineEdit lineEdit = (WLineEdit) w.getWidget(0);
	 *      return lineEdit.getText();
	 *    }
	 *   }
	 * </pre>
	 * <p>
	 * 
	 * @see WItemDelegate#createEditor(WModelIndex index, EnumSet flags)
	 * @see WItemDelegate#setEditState(WWidget editor, Object value)
	 * @see WItemDelegate#setModelData(Object editState, WAbstractItemModel
	 *      model, WModelIndex index)
	 */
	public Object getEditState(WWidget editor) {
		IndexContainerWidget w = ((editor) instanceof IndexContainerWidget ? (IndexContainerWidget) (editor)
				: null);
		WLineEdit lineEdit = ((w.getWidget(0)) instanceof WLineEdit ? (WLineEdit) (w
				.getWidget(0)) : null);
		return lineEdit.getText();
	}

	/**
	 * Sets the editor data from the editor state.
	 * <p>
	 * The default implementation resets the text in the line edit. You will
	 * need to reimplement this method if for a custom editor.
	 * <p>
	 * As an example of how to deal with a specialized editor, consider the
	 * default implementation:
	 * <p>
	 * 
	 * <pre>
	 *   {@code
	 *    public void setEditState(WWidget editor, Object value) {
	 *      WContainerWidget w = (WContainerWidget) editor;
	 *      WLineEdit lineEdit = (WLineEdit) w.getWidget(0);
	 *      lineEdit.setText((String) value);
	 *    }
	 *   }
	 * </pre>
	 * <p>
	 * 
	 * @see WItemDelegate#createEditor(WModelIndex index, EnumSet flags)
	 */
	public void setEditState(WWidget editor, final Object value) {
		IndexContainerWidget w = ((editor) instanceof IndexContainerWidget ? (IndexContainerWidget) (editor)
				: null);
		WLineEdit lineEdit = ((w.getWidget(0)) instanceof WLineEdit ? (WLineEdit) (w
				.getWidget(0)) : null);
		lineEdit.setText(((String) value));
	}

	/**
	 * Creates an editor for a data item.
	 * <p>
	 * The default implementation returns a {@link WLineEdit} which edits the
	 * item&apos;s {@link ItemDataRole#EditRole} value.
	 * <p>
	 * You may reimplement this method to provide a suitable editor, or to
	 * attach a custom validator. In that case, you will probably also want to
	 * reimplement {@link WItemDelegate#getEditState(WWidget editor)
	 * getEditState()},
	 * {@link WItemDelegate#setEditState(WWidget editor, Object value)
	 * setEditState()}, and
	 * {@link WItemDelegate#setModelData(Object editState, WAbstractItemModel model, WModelIndex index)
	 * setModelData()}.
	 * <p>
	 * The editor should not keep a reference to the model index (it does not
	 * need to since
	 * {@link WItemDelegate#setModelData(Object editState, WAbstractItemModel model, WModelIndex index)
	 * setModelData()} will provide the proper model index to save the data to
	 * the model). Otherwise, because model indexes may shift because of row or
	 * column insertions, you should reimplement
	 * {@link WItemDelegate#updateModelIndex(WWidget widget, WModelIndex index)
	 * updateModelIndex()}.
	 * <p>
	 * As an example of how to provide a specialized editor, consider the
	 * default implementation, which returns a {@link WLineEdit}:
	 * <p>
	 * 
	 * <pre>
	 *   {@code
	 *    protected WWidget createEditor(WModelIndex index, EnumSet&lt;ViewItemRenderFlag&gt; flags) {
	 *     final WContainerWidget result = new WContainerWidget();
	 *     result.setSelectable(true);
	 *     WLineEdit lineEdit = new WLineEdit();
	 *     lineEdit.setText(StringUtils.asString(index.getData(ItemDataRole.EditRole), this.textFormat_).toString());
	 *     lineEdit.enterPressed().addListener(this, new Signal.Listener() {
	 *       public void trigger() {
	 *         WItemDelegate.this.closeEditor().trigger(result, true);
	 *       }
	 *     });
	 *     lineEdit.escapePressed().addListener(this, new Signal.Listener() {
	 *       public void trigger() {
	 *         WItemDelegate.this.closeEditor().trigger(result, false);
	 *       }
	 *     });
	 *   
	 *     if (flags.contains(ViewItemRenderFlag.RenderFocused))
	 *       lineEdit.setFocus();
	 *   
	 *     result.setLayout(new WHBoxLayout());
	 *     result.getLayout().setContentsMargins(1, 1, 1, 1);
	 *     result.getLayout().addWidget(lineEdit);
	 *     return result;
	 *    }
	 *   }
	 * </pre>
	 */
	protected WWidget createEditor(final WModelIndex index,
			EnumSet<ViewItemRenderFlag> flags) {
		final IndexContainerWidget result = new IndexContainerWidget(index);
		result.setSelectable(true);
		WLineEdit lineEdit = new WLineEdit();
		lineEdit.setText(StringUtils.asString(
				index.getData(ItemDataRole.EditRole), this.textFormat_)
				.toString());
		lineEdit.enterPressed().addListener(this, new Signal.Listener() {
			public void trigger() {
				WItemDelegate.this.doCloseEditor(result, true);
			}
		});
		lineEdit.escapePressed().addListener(this, new Signal.Listener() {
			public void trigger() {
				WItemDelegate.this.doCloseEditor(result, false);
			}
		});
		lineEdit.escapePressed().preventPropagation();
		if (!EnumUtils.mask(flags, ViewItemRenderFlag.RenderFocused).isEmpty()) {
			lineEdit.setFocus(true);
		}
		WApplication app = WApplication.getInstance();
		if (app.getEnvironment().getAgent() != WEnvironment.UserAgent.Konqueror) {
			result.setLayout(new WHBoxLayout());
			result.getLayout().setContentsMargins(1, 1, 1, 1);
			result.getLayout().addWidget(lineEdit);
		} else {
			lineEdit.resize(new WLength(100, WLength.Unit.Percentage),
					new WLength(100, WLength.Unit.Percentage));
			result.addWidget(lineEdit);
		}
		return result;
	}

	/**
	 * Creates an editor for a data item.
	 * <p>
	 * Returns {@link #createEditor(WModelIndex index, EnumSet flags)
	 * createEditor(index, EnumSet.of(flag, flags))}
	 */
	protected final WWidget createEditor(final WModelIndex index,
			ViewItemRenderFlag flag, ViewItemRenderFlag... flags) {
		return createEditor(index, EnumSet.of(flag, flags));
	}

	private String textFormat_;

	static class WidgetRef {
		private static Logger logger = LoggerFactory.getLogger(WidgetRef.class);

		public WWidget w;

		public WidgetRef(WWidget widget) {
			this.w = widget;
		}
	}

	private IndexCheckBox checkBox(final WItemDelegate.WidgetRef w,
			final WModelIndex index, boolean autoCreate, boolean update,
			boolean triState) {
		IndexCheckBox checkBox = ((w.w.find("c")) instanceof IndexCheckBox ? (IndexCheckBox) (w.w
				.find("c")) : null);
		if (!(checkBox != null)) {
			if (autoCreate) {
				final IndexCheckBox result = checkBox = new IndexCheckBox(index);
				checkBox.setObjectName("c");
				checkBox.clicked().preventPropagation();
				IndexContainerWidget wc = ((w.w.find("o")) instanceof IndexContainerWidget ? (IndexContainerWidget) (w.w
						.find("o")) : null);
				if (!(wc != null)) {
					wc = new IndexContainerWidget(index);
					wc.setObjectName("o");
					w.w.setInline(true);
					w.w.setStyleClass(WString.Empty.toString());
					IndexContainerWidget p = ((w.w.getParent()) instanceof IndexContainerWidget ? (IndexContainerWidget) (w.w
							.getParent()) : null);
					if (p != null) {
						p.removeWidget(w.w);
					}
					wc.addWidget(w.w);
					w.w = wc;
				}
				wc.insertWidget(0, checkBox);
				checkBox.changed().addListener(this, new Signal.Listener() {
					public void trigger() {
						WItemDelegate.this.onCheckedChange(result);
					}
				});
			} else {
				return null;
			}
		}
		if (update) {
			checkBox.setTristate(triState);
		}
		return checkBox;
	}

	private final IndexCheckBox checkBox(final WItemDelegate.WidgetRef w,
			final WModelIndex index, boolean autoCreate) {
		return checkBox(w, index, autoCreate, false, false);
	}

	private final IndexCheckBox checkBox(final WItemDelegate.WidgetRef w,
			final WModelIndex index, boolean autoCreate, boolean update) {
		return checkBox(w, index, autoCreate, update, false);
	}

	private IndexText textWidget(final WItemDelegate.WidgetRef w,
			final WModelIndex index) {
		return ((w.w.find("t")) instanceof IndexText ? (IndexText) (w.w
				.find("t")) : null);
	}

	private WImage iconWidget(final WItemDelegate.WidgetRef w,
			final WModelIndex index, boolean autoCreate) {
		WImage image = ((w.w.find("i")) instanceof WImage ? (WImage) (w.w
				.find("i")) : null);
		if (image != null || !autoCreate) {
			return image;
		}
		WContainerWidget wc = ((w.w.find("a")) instanceof IndexAnchor ? (IndexAnchor) (w.w
				.find("a")) : null);
		if (!(wc != null)) {
			wc = ((w.w.find("o")) instanceof IndexContainerWidget ? (IndexContainerWidget) (w.w
					.find("o")) : null);
		}
		if (!(wc != null)) {
			wc = new IndexContainerWidget(index);
			wc.setObjectName("o");
			wc.addWidget(w.w);
			w.w = wc;
		}
		image = new WImage();
		image.setObjectName("i");
		image.setStyleClass("icon");
		wc.insertWidget(wc.getCount() - 1, image);
		if (WApplication.getInstance().getEnvironment().agentIsIE()) {
			WImage inv = new WImage(WApplication.getInstance()
					.getOnePixelGifUrl());
			inv.setStyleClass("rh w0 icon");
			inv.resize(new WLength(0), WLength.Auto);
			wc.insertWidget(wc.getCount() - 1, inv);
		}
		return image;
	}

	private final WImage iconWidget(final WItemDelegate.WidgetRef w,
			final WModelIndex index) {
		return iconWidget(w, index, false);
	}

	private IndexAnchor anchorWidget(final WItemDelegate.WidgetRef w,
			final WModelIndex index, boolean autoCreate) {
		IndexAnchor anchor = ((w.w.find("a")) instanceof IndexAnchor ? (IndexAnchor) (w.w
				.find("a")) : null);
		if (anchor != null || !autoCreate) {
			return anchor;
		}
		anchor = new IndexAnchor(index);
		anchor.setObjectName("a");
		IndexContainerWidget wc = ((w.w.find("o")) instanceof IndexContainerWidget ? (IndexContainerWidget) (w.w
				.find("o")) : null);
		if (wc != null) {
			int firstToMove = 0;
			WCheckBox cb = ((wc.getWidget(0)) instanceof WCheckBox ? (WCheckBox) (wc
					.getWidget(0)) : null);
			if (cb != null) {
				firstToMove = 1;
			}
			wc.insertWidget(firstToMove, anchor);
			while (wc.getCount() > firstToMove + 1) {
				WWidget c = wc.getWidget(firstToMove + 1);
				wc.removeWidget(c);
				anchor.addWidget(c);
			}
		} else {
			anchor.addWidget(w.w);
			w.w = anchor;
		}
		return anchor;
	}

	private final IndexAnchor anchorWidget(final WItemDelegate.WidgetRef w,
			final WModelIndex index) {
		return anchorWidget(w, index, false);
	}

	private void onCheckedChange(IndexCheckBox cb) {
		WAbstractItemModel model = cb.getIndex().getModel();
		if (cb.isTristate()) {
			model.setData(cb.getIndex(), cb.getCheckState(),
					ItemDataRole.CheckStateRole);
		} else {
			model.setData(cb.getIndex(), cb.isChecked(),
					ItemDataRole.CheckStateRole);
		}
	}

	private void doCloseEditor(WWidget editor, boolean save) {
		this.closeEditor().trigger(editor, save);
	}
}
