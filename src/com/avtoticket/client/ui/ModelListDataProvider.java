/*
 * Copyright Бездна (c) 2012.
 */
package com.avtoticket.client.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.avtoticket.client.utils.RPC;
import com.avtoticket.shared.models.BaseModel;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.ListDataProvider;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 04.09.2012 16:04:51
 */
public class ModelListDataProvider<T> extends ListDataProvider<T> {

	private Class<T> modelsClass;
	private boolean loading = false;
	private boolean loaded = false;
	private T waiter;

	private static BaseModel createWaiter(String className) {
		BaseModel ret = BaseModel.getRefl().instantiate(className);
		if (ret != null) {
			ret.setValueField(0L);
			ret.setDisplayField("Загрузка...");
		}
		return ret;
	}

	public ModelListDataProvider(List<T> models) {
		super(models);
		loaded = true;
	}

	@SuppressWarnings("unchecked")
	public ModelListDataProvider(Class<T> clazz) {
		this(clazz, (clazz.getSuperclass() != Enum.class) ? (T) createWaiter(clazz.getName()) : null);
	}

	public ModelListDataProvider(Class<T> clazz, T waiter) {
		this.modelsClass = clazz;
		this.waiter = waiter;
		load();
	}

	public void reload() {
		loaded = false;
		load();
	}

	@SuppressWarnings("unchecked")
	private void load() {
		if (!loading && !loaded) {
			if (modelsClass.getSuperclass() == Enum.class) {
				loaded = true;
				setList((List<T>) Arrays.asList(((Class<Enum<?>>) modelsClass).getEnumConstants()));
			} else {
				loading = true;
				setList((List<T>) Arrays.asList(waiter));
				query(modelsClass.getName(), new AsyncCallback<List<T>>() {
					@Override
					public void onFailure(Throwable caught) {
						loading = false;
						Window.alert(caught.getMessage());
					}
	
					@Override
					public void onSuccess(List<T> result) {
						loading = false;
						loaded = true;
						if (result != null)
							setList(result);
						else
							setList(new ArrayList<T>());
					}
				});
			}
		}
	}

	protected void query(String className, final AsyncCallback<List<T>> callback) {
		RPC.getTS().getKeyValueObjects(className, new AsyncCallback<List<BaseModel>>() {
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}

			@SuppressWarnings("unchecked")
			@Override
			public void onSuccess(List<BaseModel> result) {
				callback.onSuccess((List<T>) result);
			}
		});
	}

	public boolean isLoaded() {
		return loaded;
	}

}