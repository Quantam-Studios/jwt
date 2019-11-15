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
 * A container widget which provides layout of children in a table grid.
 * <p>
 * 
 * A WTable arranges its children in a table.
 * <p>
 * To insert or access contents, use
 * {@link WTable#getElementAt(int row, int column) getElementAt()} to access the
 * {@link WTableCell cell} at a particular location in the table. The WTable
 * expands automatically to create the indexed (row, column) as necessary.
 * <p>
 * It is possible to insert and delete entire rows or columns from the table
 * using the insertColumn(int column), insertRow(int row),
 * {@link WTable#deleteColumn(int column) deleteColumn()}, or
 * {@link WTable#deleteRow(int row) deleteRow()} methods.
 * <p>
 * You may indicate a number of rows and columns that act as headers using
 * {@link WTable#setHeaderCount(int count, Orientation orientation)
 * setHeaderCount()}. Header cells are rendered as <code>&lt;th&gt;</code>
 * instead of <code>&lt;td&gt;</code> elements. By default, no rows or columns
 * are configured as headers.
 * <p>
 * WTable is displayed as a {@link WWidget#setInline(boolean inlined) block}.
 * <p>
 * <h3>CSS</h3>
 * 
 * <p>
 * The widget corresponds to the HTML <code>&lt;table&gt;</code> tag and does
 * not provide styling. It can be styled using inline or external CSS as
 * appropriate.
 * <p>
 * 
 * @see WTableCell
 * @see WTableRow
 * @see WTableColumn
 */
public class WTable extends WInteractWidget {
	private static Logger logger = LoggerFactory.getLogger(WTable.class);

	/**
	 * Creates an empty table.
	 */
	public WTable(WContainerWidget parent) {
		super(parent);
		this.flags_ = new BitSet();
		this.rows_ = new ArrayList<WTableRow>();
		this.columns_ = new ArrayList<WTableColumn>();
		this.rowsChanged_ = null;
		this.rowsAdded_ = 0;
		this.headerRowCount_ = 0;
		this.headerColumnCount_ = 0;
		this.setInline(false);
		this.setIgnoreChildRemoves(true);
	}

	/**
	 * Creates an empty table.
	 * <p>
	 * Calls {@link #WTable(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WTable() {
		this((WContainerWidget) null);
	}

	/**
	 * Deletes the table and its entire contents.
	 */
	public void remove() {
		for (int i = 0; i < this.rows_.size(); ++i) {
			;
		}
		for (int i = 0; i < this.columns_.size(); ++i) {
			;
		}
		;
		this.rowsChanged_ = null;
		super.remove();
	}

	/**
	 * Accesses the table element at the given row and column.
	 * <p>
	 * 
	 * If the row/column is beyond the current table dimensions, then the table
	 * is expanded automatically.
	 */
	public WTableCell getElementAt(int row, int column) {
		this.expand(row, column, 1, 1);
		final WTableRow.TableData d = this.itemAt(row, column);
		return d.cell;
	}

	/**
	 * Returns the row object for the given row.
	 * <p>
	 * 
	 * Like with {@link WTable#getElementAt(int row, int column) getElementAt()}
	 * , the table expands automatically when the row is beyond the current
	 * table dimensions.
	 * <p>
	 * 
	 * @see WTable#getElementAt(int row, int column)
	 * @see WTable#getColumnAt(int column)
	 */
	public WTableRow getRowAt(int row) {
		this.expand(row, 0, 1, 0);
		return this.rows_.get(row);
	}

	/**
	 * Returns the column object for the given column.
	 * <p>
	 * 
	 * Like with {@link WTable#getElementAt(int row, int column) getElementAt()}
	 * , the table expands automatically when the column is beyond the current
	 * table dimensions.
	 * <p>
	 * 
	 * @see WTable#getElementAt(int row, int column)
	 * @see WTable#getRowAt(int row)
	 */
	public WTableColumn getColumnAt(int column) {
		this.expand(0, column, 0, 1);
		return this.columns_.get(column);
	}

	/**
	 * Deletes a table cell and its contents.
	 * <p>
	 * 
	 * The table cell at that position is recreated.
	 * <p>
	 * 
	 * @see WTable#removeCell(int row, int column)
	 */
	public void removeCell(WTableCell item) {
		this.removeCell(item.getRow(), item.getColumn());
	}

	/**
	 * Deletes the table cell at the given position.
	 * <p>
	 * 
	 * @see WTable#removeCell(WTableCell item)
	 */
	public void removeCell(int row, int column) {
		final WTableRow.TableData d = this.itemAt(row, column);
		if (d.cell != null)
			d.cell.remove();
		d.cell = this.rows_.get(row).createCell(column);
	}

	/**
	 * Inserts an empty row.
	 */
	public WTableRow insertRow(int row, WTableRow tableRow) {
		if (row == this.getRowCount()
				&& this.getRowCount() >= this.headerRowCount_) {
			++this.rowsAdded_;
		} else {
			this.flags_.set(BIT_GRID_CHANGED);
		}
		if (!(tableRow != null)) {
			tableRow = this.createRow(row);
		}
		tableRow.table_ = this;
		this.rows_.add(0 + row, tableRow);
		tableRow.expand(this.getColumnCount());
		this.repaint(EnumSet.of(RepaintFlag.RepaintSizeAffected));
		return tableRow;
	}

	/**
	 * Inserts an empty row.
	 * <p>
	 * Returns {@link #insertRow(int row, WTableRow tableRow) insertRow(row,
	 * (WTableRow)null)}
	 */
	public final WTableRow insertRow(int row) {
		return insertRow(row, (WTableRow) null);
	}

	/**
	 * Deletes a row and all its contents.
	 * <p>
	 * 
	 * Rows below the given row are shifted up.
	 */
	public void deleteRow(int row) {
		if (this.rowsChanged_ != null) {
			this.rowsChanged_.remove(this.rows_.get(row));
			if (this.rowsChanged_.isEmpty()) {
				;
				this.rowsChanged_ = null;
			}
		}
		for (int i = 0; i < this.getColumnCount(); ++i) {
			WTableCell cell = this.rows_.get(row).cells_.get(i).cell;
			if (cell != null)
				cell.remove();
		}
		if (row >= (int) (this.getRowCount() - this.rowsAdded_)) {
			--this.rowsAdded_;
		} else {
			this.flags_.set(BIT_GRID_CHANGED);
			this.repaint(EnumSet.of(RepaintFlag.RepaintSizeAffected));
		}
		;
		this.rows_.remove(0 + row);
	}

	/**
	 * Inserts an empty column.
	 */
	public WTableColumn insertColumn(int column, WTableColumn tableColumn) {
		for (int i = 0; i < this.rows_.size(); ++i) {
			this.rows_.get(i).insertColumn(column);
		}
		if ((int) column <= this.columns_.size()) {
			if (!(tableColumn != null)) {
				tableColumn = this.createColumn(column);
				tableColumn.table_ = this;
			}
			this.columns_.add(0 + column, tableColumn);
		}
		this.flags_.set(BIT_GRID_CHANGED);
		this.repaint(EnumSet.of(RepaintFlag.RepaintSizeAffected));
		return tableColumn;
	}

	/**
	 * Inserts an empty column.
	 * <p>
	 * Returns {@link #insertColumn(int column, WTableColumn tableColumn)
	 * insertColumn(column, (WTableColumn)null)}
	 */
	public final WTableColumn insertColumn(int column) {
		return insertColumn(column, (WTableColumn) null);
	}

	/**
	 * Delete a column and all its contents.
	 */
	public void deleteColumn(int column) {
		for (int i = 0; i < this.getRowCount(); ++i) {
			this.rows_.get(i).deleteColumn(column);
		}
		if ((int) column <= this.columns_.size()) {
			;
			this.columns_.remove(0 + column);
		}
		this.flags_.set(BIT_GRID_CHANGED);
		this.repaint(EnumSet.of(RepaintFlag.RepaintSizeAffected));
	}

	/**
	 * Clears the entire table.
	 * <p>
	 * 
	 * This method clears the entire table: all cells and their contents are
	 * deleted.
	 */
	public void clear() {
		while (this.getRowCount() > 0) {
			this.deleteRow(this.getRowCount() - 1);
		}
		while (this.getColumnCount() > 0) {
			this.deleteColumn(this.getColumnCount() - 1);
		}
	}

	/**
	 * Returns the number of rows in the table.
	 */
	public int getRowCount() {
		return this.rows_.size();
	}

	/**
	 * Returns the number of columns in the table.
	 */
	public int getColumnCount() {
		return this.columns_.size();
	}

	/**
	 * Sets the number of header rows or columns.
	 * <p>
	 * 
	 * The default values are 0.
	 * <p>
	 * 
	 * <p>
	 * <i><b>Note: </b>This must be set before the initial rendering and cannot
	 * be changed later. </i>
	 * </p>
	 */
	public void setHeaderCount(int count, Orientation orientation) {
		if (orientation == Orientation.Horizontal) {
			this.headerRowCount_ = count;
		} else {
			this.headerColumnCount_ = count;
		}
	}

	/**
	 * Sets the number of header rows or columns.
	 * <p>
	 * Calls {@link #setHeaderCount(int count, Orientation orientation)
	 * setHeaderCount(count, Orientation.Horizontal)}
	 */
	public final void setHeaderCount(int count) {
		setHeaderCount(count, Orientation.Horizontal);
	}

	/**
	 * Returns the number of header rows or columns.
	 * <p>
	 * 
	 * @see WTable#setHeaderCount(int count, Orientation orientation)
	 */
	public int getHeaderCount(Orientation orientation) {
		if (orientation == Orientation.Horizontal) {
			return this.headerRowCount_;
		} else {
			return this.headerColumnCount_;
		}
	}

	/**
	 * Returns the number of header rows or columns.
	 * <p>
	 * Returns {@link #getHeaderCount(Orientation orientation)
	 * getHeaderCount(Orientation.Horizontal)}
	 */
	public final int getHeaderCount() {
		return getHeaderCount(Orientation.Horizontal);
	}

	/**
	 * Move a table row from its original position to a new position.
	 * <p>
	 * 
	 * The table expands automatically when the <code>to</code> row is beyond
	 * the current table dimensions.
	 * <p>
	 * 
	 * @see WTable#moveColumn(int from, int to)
	 */
	public void moveRow(int from, int to) {
		if (from < 0 || from >= (int) this.rows_.size()) {
			logger.error(new StringWriter().append(
					"moveRow: the from index is not a valid row index.")
					.toString());
			return;
		}
		WTableRow from_tr = this.getRowAt(from);
		this.rows_.remove(from_tr);
		if (to > (int) this.rows_.size()) {
			this.getRowAt(to);
		}
		this.rows_.add(0 + to, from_tr);
		final List<WTableRow.TableData> cells = this.rows_.get(to).cells_;
		for (int i = 0; i < cells.size(); ++i) {
			if (cells.get(i).cell.getRowSpan() > 1) {
				this.getRowAt(to + cells.get(i).cell.getRowSpan() - 1);
			}
		}
		this.flags_.set(BIT_GRID_CHANGED);
		this.repaint(EnumSet.of(RepaintFlag.RepaintSizeAffected));
	}

	/**
	 * Move a table column from its original position to a new position.
	 * <p>
	 * 
	 * The table expands automatically when the <code>to</code> column is beyond
	 * the current table dimensions.
	 * <p>
	 * 
	 * @see WTable#moveRow(int from, int to)
	 */
	public void moveColumn(int from, int to) {
		if (from < 0 || from >= (int) this.columns_.size()) {
			logger.error(new StringWriter().append(
					"moveColumn: the from index is not a valid column index.")
					.toString());
			return;
		}
		WTableColumn from_tc = this.getColumnAt(from);
		this.columns_.remove(from_tc);
		if (to > (int) this.columns_.size()) {
			this.getColumnAt(to);
		}
		this.columns_.add(0 + to, from_tc);
		for (int i = 0; i < this.rows_.size(); i++) {
			final List<WTableRow.TableData> cells = this.rows_.get(i).cells_;
			WTableRow.TableData cell = cells.get(from);
			cells.remove(0 + from);
			cells.add(0 + to, cell);
			if (cell.cell.getColumnSpan() - 1 != 0) {
				this.getColumnAt(to + cell.cell.getColumnSpan() - 1);
			}
			for (int j = Math.min(from, to); j < cells.size(); ++j) {
				cells.get(j).cell.column_ = j;
			}
		}
		this.flags_.set(BIT_GRID_CHANGED);
		this.repaint(EnumSet.of(RepaintFlag.RepaintSizeAffected));
	}

	static final int BIT_GRID_CHANGED = 0;
	private static final int BIT_COLUMNS_CHANGED = 1;
	BitSet flags_;
	List<WTableRow> rows_;
	List<WTableColumn> columns_;
	private Set<WTableRow> rowsChanged_;
	private int rowsAdded_;
	private int headerRowCount_;
	private int headerColumnCount_;

	void expand(int row, int column, int rowSpan, int columnSpan) {
		int curNumRows = this.getRowCount();
		int curNumColumns = this.getColumnCount();
		int newNumRows = row + rowSpan;
		int newNumColumns = Math.max(curNumColumns, column + columnSpan);
		for (int r = curNumRows; r < newNumRows; ++r) {
			this.insertRow(r);
		}
		for (int c = curNumColumns; c < newNumColumns; ++c) {
			this.insertColumn(c);
		}
	}

	private WTableRow.TableData itemAt(int row, int column) {
		return this.rows_.get(row).cells_.get(column);
	}

	void repaintRow(WTableRow row) {
		if (row.getRowNum() >= (int) (this.getRowCount() - this.rowsAdded_)) {
			return;
		}
		if (!(this.rowsChanged_ != null)) {
			this.rowsChanged_ = new HashSet<WTableRow>();
		}
		this.rowsChanged_.add(row);
		this.repaint(EnumSet.of(RepaintFlag.RepaintSizeAffected));
	}

	void repaintColumn(WTableColumn column) {
		this.flags_.set(BIT_COLUMNS_CHANGED);
		this.repaint(EnumSet.of(RepaintFlag.RepaintSizeAffected));
	}

	/**
	 * Creates a table cell.
	 * <p>
	 * 
	 * You may want to override this method if you want your table to contain
	 * specialized cells.
	 */
	protected WTableCell createCell(int row, int column) {
		return new WTableCell();
	}

	/**
	 * Creates a table row.
	 * <p>
	 * 
	 * You may want to override this method if you want your table to contain
	 * specialized rows.
	 */
	protected WTableRow createRow(int row) {
		return new WTableRow();
	}

	/**
	 * Creates a table column.
	 * <p>
	 * 
	 * You may want to override this method if you want your table to contain
	 * specialized columns.
	 */
	protected WTableColumn createColumn(int column) {
		return new WTableColumn();
	}

	void updateDom(final DomElement element, boolean all) {
		super.updateDom(element, all);
	}

	DomElementType getDomElementType() {
		return DomElementType.DomElement_TABLE;
	}

	protected DomElement createDomElement(WApplication app) {
		boolean withIds = !app.getEnvironment().agentIsSpiderBot();
		DomElement table = DomElement.createNew(this.getDomElementType());
		this.setId(table, app);
		DomElement thead = null;
		if (this.headerRowCount_ != 0) {
			thead = DomElement.createNew(DomElementType.DomElement_THEAD);
			if (withIds) {
				thead.setId(this.getId() + "th");
			}
		}
		DomElement tbody = DomElement
				.createNew(DomElementType.DomElement_TBODY);
		if (withIds) {
			tbody.setId(this.getId() + "tb");
		}
		DomElement colgroup = DomElement
				.createNew(DomElementType.DomElement_COLGROUP);
		for (int col = 0; col < this.columns_.size(); ++col) {
			DomElement c = DomElement.createNew(DomElementType.DomElement_COL);
			if (withIds) {
				c.setId(this.columns_.get(col).getId());
			}
			this.columns_.get(col).updateDom(c, true);
			colgroup.addChild(c);
		}
		table.addChild(colgroup);
		this.flags_.clear(BIT_COLUMNS_CHANGED);
		for (int row = 0; row < (int) this.getRowCount(); ++row) {
			for (int col = 0; col < (int) this.getColumnCount(); ++col) {
				this.itemAt(row, col).overSpanned = false;
			}
		}
		for (int row = 0; row < (int) this.getRowCount(); ++row) {
			DomElement tr = this.createRowDomElement(row, withIds, app);
			if (row < (int) this.headerRowCount_) {
				thead.addChild(tr);
			} else {
				tbody.addChild(tr);
			}
		}
		this.rowsAdded_ = 0;
		if (thead != null) {
			table.addChild(thead);
		}
		table.addChild(tbody);
		this.updateDom(table, true);
		this.flags_.clear(BIT_GRID_CHANGED);
		;
		this.rowsChanged_ = null;
		return table;
	}

	protected void getDomChanges(final List<DomElement> result, WApplication app) {
		DomElement e = DomElement.getForUpdate(this, this.getDomElementType());
		if (!this.isStubbed() && this.flags_.get(BIT_GRID_CHANGED)) {
			DomElement newE = this.createDomElement(app);
			e.replaceWith(newE);
		} else {
			if (this.rowsChanged_ != null) {
				for (Iterator<WTableRow> i_it = this.rowsChanged_.iterator(); i_it
						.hasNext();) {
					WTableRow i = i_it.next();
					DomElement e2 = DomElement.getForUpdate(i,
							DomElementType.DomElement_TR);
					i.updateDom(e2, false);
					result.add(e2);
				}
				;
				this.rowsChanged_ = null;
			}
			if (this.rowsAdded_ != 0) {
				DomElement etb = DomElement.getForUpdate(this.getId() + "tb",
						DomElementType.DomElement_TBODY);
				for (int i = 0; i < (int) this.rowsAdded_; ++i) {
					DomElement tr = this.createRowDomElement(this.getRowCount()
							- this.rowsAdded_ + i, true, app);
					etb.addChild(tr);
				}
				result.add(etb);
				this.rowsAdded_ = 0;
			}
			if (this.flags_.get(BIT_COLUMNS_CHANGED)) {
				for (int i = 0; i < this.columns_.size(); ++i) {
					DomElement e2 = DomElement
							.getForUpdate(this.columns_.get(i),
									DomElementType.DomElement_COL);
					this.columns_.get(i).updateDom(e2, false);
					result.add(e2);
				}
				this.flags_.clear(BIT_COLUMNS_CHANGED);
			}
			this.updateDom(e, false);
		}
		result.add(e);
	}

	void propagateRenderOk(boolean deep) {
		this.flags_.clear();
		if (this.rowsChanged_ != null) {
			;
			this.rowsChanged_ = null;
		}
		this.rowsAdded_ = 0;
		super.propagateRenderOk(deep);
	}

	private DomElement createRowDomElement(int row, boolean withIds,
			WApplication app) {
		DomElement tr = DomElement.createNew(DomElementType.DomElement_TR);
		if (withIds) {
			tr.setId(this.rows_.get(row).getId());
		}
		this.rows_.get(row).updateDom(tr, true);
		tr.setWasEmpty(false);
		int spanCounter = 0;
		for (int col = 0; col < this.getColumnCount(); ++col) {
			final WTableRow.TableData d = this.itemAt(row, col);
			if (!d.overSpanned) {
				DomElement td = d.cell.createSDomElement(app);
				if (col < this.headerColumnCount_ || row < this.headerRowCount_) {
					tr.addChild(td);
				} else {
					tr.insertChildAt(td, col - spanCounter);
				}
				for (int i = 0; i < d.cell.getRowSpan(); ++i) {
					for (int j = 0; j < d.cell.getColumnSpan(); ++j) {
						if (i + j > 0) {
							this.itemAt(row + i, col + j).overSpanned = true;
							this.itemAt(row + i, col + j).cell
									.setRendered(false);
						}
					}
				}
			} else {
				spanCounter++;
			}
		}
		return tr;
	}
}
