/*
 * Copyright Бездна (c) 2013.
 */
package com.avtoticket.client.ui.grid;

import com.avtoticket.client.ui.Editor;
import com.avtoticket.client.ui.ModelEditor;
import com.avtoticket.client.utils.DefaultCallback;
import com.avtoticket.client.utils.RPC;
import com.avtoticket.client.utils.Waiter;
import com.avtoticket.shared.models.BaseModel;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 18.07.2013 18:38:36
 */
public class ModelEditorDialog<T extends BaseModel> extends DialogBox {

	private T model;
	private Button btnOk;
	private Button btnClose;
	private DefaultCallback<T> callback;
	private FlexTable layout;
	private Editor<T> editor;

	public ModelEditorDialog() {
		this(null);
	}

	public ModelEditorDialog(Editor<T> editor) {
		super();
		this.editor = editor;
		setGlassEnabled(true);

		btnOk = new Button();
		btnOk.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				onOkClick();
			}
		});
		btnClose = new Button("Отмена");
		btnClose.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});

		layout = new FlexTable();
		layout.setWidget(0, 0, editor);
		layout.setWidget(1, 0, btnOk);
		layout.setWidget(1, 1, btnClose);
		FlexCellFormatter cf = layout.getFlexCellFormatter();
		cf.setStyleName(1, 0, ModelEditor.CSS.atEditorCells());
		cf.setStyleName(1, 1, ModelEditor.CSS.atEditorCells());
		cf.setColSpan(0, 0, 2);
		cf.setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_LEFT);
		cf.setHorizontalAlignment(1, 1, HasHorizontalAlignment.ALIGN_RIGHT);
		setWidget(layout);
	}

	public void startProgress() {
		setEnabled(false);
		Waiter.start();
	}

	public void finishProgress() {
		setEnabled(true);
		Waiter.stop();
	}

	public void errorMessage(String message) {
		setEnabled(true);
		Waiter.stop();
		Window.alert(message);
	}

	public void clearState() {
		setEnabled(true);
	}

	public void edit(final boolean isNewModel, final T model) {
		edit(isNewModel, model, new DefaultCallback<T>() {
			@Override
			public void onSuccess(T ret) {
				startProgress();
				RPC.getTS().saveModel(ret, new AsyncCallback<Long>() {
					@Override
					public void onFailure(Throwable caught) {
						errorMessage(caught.getMessage());
					}

					@Override
					public void onSuccess(Long result) {
						if (isNewModel)
							model.setId(result);
						finishProgress();
						hide();
					}
				});
			}
		});
	}

	public void edit(boolean isNewModel, T model, DefaultCallback<T> callback) {
		this.model = model;
		this.callback = callback;
		setOkText(isNewModel ? "Создать" : "Сохранить");
		setText(isNewModel ? "Создать новую запись" : "Редактирование записи");
		center();
		if (editor != null)
			editor.edit(model);
	}

	public void onOkClick() {
		if (editor.flush() && (callback != null))
			callback.onSuccess(editor.getModel());
	}

	public void onValueChange(String fieldName) { }

	public void setEnabled(Boolean isEnabled) {
		btnOk.setEnabled(isEnabled);
		btnClose.setEnabled(isEnabled);
		editor.setEnabled(isEnabled);
	}

	public Editor<T> getEditor() {
		return editor;
	}

	public void setEditor(Editor<T> editor) {
		this.editor = editor;
		layout.setWidget(0, 0, editor);
		editor.edit(model);
		editor.setEnabled(btnOk.isEnabled());
	}

	public void setOkText(String text) {
		btnOk.setText(text);
	}

	public void setCloseText(String text) {
		btnClose.setText(text);
	}

}