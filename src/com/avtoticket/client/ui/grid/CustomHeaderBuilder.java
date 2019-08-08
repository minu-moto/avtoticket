/*
 * Copyright Бездна (c) 2013.
 */
package com.avtoticket.client.ui.grid;

import java.util.List;

import com.avtoticket.client.ui.grid.fields.Field;
import com.avtoticket.shared.models.BaseModel;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.dom.builder.shared.TableCellBuilder;
import com.google.gwt.dom.builder.shared.TableRowBuilder;
import com.google.gwt.dom.client.Style.OutlineStyle;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.AbstractHeaderOrFooterBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.AbstractCellTable.Style;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 23.10.2013 17:50:02
 */
public class CustomHeaderBuilder<T extends BaseModel> extends AbstractHeaderOrFooterBuilder<T> {

	private View<T> table;

	@SuppressWarnings("unchecked")
	public CustomHeaderBuilder(View<T> table) {
		super((AbstractCellTable<T>) table, false);
		this.table = table;
		MenuHeaderCell.CSS.ensureInjected();
	}

	@Override
	protected boolean buildHeaderOrFooterImpl() {
		List<Field<?, T>> columns = table.getFields();
		// Early exit if there aren't any columns to render.
		int columnCount = (columns != null) ? columns.size() : 0;
		if (columnCount == 0)
			return false;	// Nothing to render;

		// Early exit if there aren't any headers in the columns to render.
		boolean hasHeader = false;
		for (int i = 0; i < columnCount; i++)
			if (getHeader(i) != null) {
				hasHeader = true;
				break;
			}
		if (hasHeader == false)
			return false;

		// Get information about the sorted column.
		ColumnSortList sortList = table.getColumnSortList();
		ColumnSortInfo sortedInfo = (sortList.size() == 0) ? null : sortList.get(0);
		Column<?, ?> sortedColumn = (sortedInfo == null) ? null : sortedInfo.getColumn();
		boolean isSortAscending = (sortedInfo == null) ? false : sortedInfo.isAscending();

		// Get the common style names.
		Style style = getTable().getResources().style();
		String className = style.header();
		String sortableStyle = " " + style.sortableHeader();
		String sortedStyle = " " + (isSortAscending ? style.sortedHeaderAscending() : style.sortedHeaderDescending());

		// Setup the first column.
		Header<?> prevHeader = getHeader(0);
		Column<T, ?> column = getTable().getColumn(0);
		int prevColspan = 1;
		boolean isSortable = false;
		boolean isSorted = false;
		boolean isFiltred = (columns != null) && (columns.get(0) != null) && (columns.get(0).getFilter() != null);
		StringBuilder classesBuilder = new StringBuilder(className);
		classesBuilder.append(" ").append(style.firstColumnHeader());
		if (column.isSortable()) {
			isSortable = true;
			isSorted = (column == sortedColumn);
		}

		// Loop through all column headers.
		TableRowBuilder tr = startRow();
		int curColumn;
		for (curColumn = 1; curColumn < columnCount; curColumn++) {
			Header<?> header = getHeader(curColumn);

			if (header != prevHeader) {
				// The header has changed, so append the previous one.
				if (isSortable)
					classesBuilder.append(sortableStyle);
				if (isSorted)
					classesBuilder.append(sortedStyle);
				if (isFiltred)
					classesBuilder.append(" ").append(MenuHeaderCell.CSS.atFilteredHeader());
				appendExtraStyles(prevHeader, classesBuilder);

				// Render the header.
				TableCellBuilder th = tr.startTH().colSpan(prevColspan).className(classesBuilder.toString());
				enableColumnHandlers(th, column);
				if (prevHeader != null) {
					// Build the header.
					Context context = new Context(0, curColumn - prevColspan, prevHeader.getKey());
					// Add div element with aria button role
					if (isSortable) {
						// TODO: Figure out aria-label and translation of label text
						th.attribute("role", "button");
						th.style().outlineStyle(OutlineStyle.NONE);
						th.tabIndex(-1);
					}
					renderHeader(th, context, prevHeader);
				}
				th.endTH();

				// Reset the previous header.
				prevHeader = header;
				prevColspan = 1;
				classesBuilder = new StringBuilder(className);
				isSortable = false;
				isFiltred = false;
				isSorted = false;
			} else {
				// Increment the colspan if the headers == each other.
				prevColspan++;
			}

			// Update the sorted state.
			column = table.getField(curColumn).getColumn();
			if (column.isSortable()) {
				isSortable = true;
				isSorted = (column == sortedColumn);
			}
			isFiltred = (columns != null) && (columns.get(curColumn) != null) && (columns.get(curColumn).getFilter() != null);
		}

		// Append the last header.
		if (isSortable)
			classesBuilder.append(sortableStyle);
		if (isSorted)
			classesBuilder.append(sortedStyle);
		if (isFiltred)
			classesBuilder.append(" ").append(MenuHeaderCell.CSS.atFilteredHeader());

		// The first and last columns could be the same column.
		classesBuilder.append(" ").append(style.lastColumnHeader());
		appendExtraStyles(prevHeader, classesBuilder);

		// Render the last header.
		TableCellBuilder th = tr.startTH().colSpan(prevColspan).className(classesBuilder.toString());
		enableColumnHandlers(th, column);
		if (prevHeader != null) {
			Context context = new Context(0, curColumn - prevColspan, prevHeader.getKey());
			renderHeader(th, context, prevHeader);
		}
		th.endTH();

		// End the row.
		tr.endTR();

		return true;
	}

	/**
	 * Append the extra style names for the header.
	 * 
	 * @param header
	 *            the header that may contain extra styles, it can be null
	 * @param classesBuilder
	 *            the string builder for the TD classes
	 */
	private <H> void appendExtraStyles(Header<H> header, StringBuilder classesBuilder) {
		if (header == null)
			return;
		String headerStyleNames = header.getHeaderStyleNames();
		if (headerStyleNames != null)
			classesBuilder.append(" ").append(headerStyleNames);
	}

}