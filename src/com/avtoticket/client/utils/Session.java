package com.avtoticket.client.utils;

import java.util.Date;

import com.avtoticket.shared.models.BaseModel;
import com.avtoticket.shared.models.core.User;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class Session {
	
	private User user; 
	private Date timestamp;
	private boolean isServiceMode = false;
	private String birthdayTheme;
	private String birthdayMail;
	private Long salePeriod;
	private String adultPriceFormula;
	private String childPriceFormula;
	private String bagPriceFormula;
	private String adultStavPriceFormula;
	private String childStavPriceFormula;
	private String bagStavPriceFormula;
	private Long childhood;
	private boolean isProduction;

	private static Timer tmr;

	public Boolean isLocal() {
		return false;
	}

	public void init(DefaultCallback<User> cback) {
		fillInitData((DefaultCallback<Boolean>) ret -> {
			if (ret) {
				scheduleExpand();
				cback.onSuccess(user);
			}
		});
	}

	private void scheduleExpand() {
		if (tmr == null) {
			tmr = new Timer() {
				@Override
				public void run() {
					expand();
				}
			};
			tmr.scheduleRepeating(300000);
		}
	}

	private void expand() {
		RPC.getTS().touch(new AsyncCallback<Date>() {
			@Override
			public void onSuccess(Date result) {
				if ((result == null) && (getUser() != null))
					Window.Location.reload();
				else
					timestamp = result;
			}
			@Override
			public void onFailure(Throwable caught) {}
		});
	}

	private void fillInitData(DefaultCallback<Boolean> endCallback) {
		RPC.getTS().getInitData(new AsyncCallback<BaseModel>() {
			@Override
			public void onFailure(Throwable caught) {
				endCallback.onSuccess(false);
				Window.alert("Ошибка при обращении на сервер за данными конфигурации");
			}
			@Override
			public void onSuccess(BaseModel result) {
				if (result != null) {
					user = result.getModelProp("user");
					timestamp = result.getDateProp("server_timestamp");
					isServiceMode = result.getBooleanProp("service_mode");
					birthdayTheme = result.getStringProp("birthday_theme");
					birthdayMail = result.getStringProp("birthday_mail");
					salePeriod = result.getLongProp("sale_period");
					adultPriceFormula = result.getStringProp("adult_price_formula");
					childPriceFormula = result.getStringProp("child_price_formula");
					bagPriceFormula = result.getStringProp("bag_price_formula");
					adultStavPriceFormula = result.getStringProp("stav_adult_price_formula");
					childStavPriceFormula = result.getStringProp("stav_child_price_formula");
					bagStavPriceFormula = result.getStringProp("stav_bag_price_formula");
					childhood = result.getLongProp("childhood");
					isProduction = result.getBooleanProp("production");
					endCallback.onSuccess(true);
				} else {
					endCallback.onSuccess(false);
					Window.alert("Ошибка, получены пустые данные конфигурации");
				}
			}
		});
	}

	public void doLogin(String login, String password, boolean remember) {
		RPC.getTS().login(login, password, remember, new AsyncCallback<Boolean>() {
			@Override
			public void onFailure(Throwable caught) {
				Window.alert(caught.getMessage());
			}

			@Override
			public void onSuccess(Boolean result) {
				if (result)
					Window.Location.reload();
				else
					Window.alert("Неправильный логин или пароль");
			}
		});
	}

	public void reGetData(DefaultCallback<Boolean> callback) {
		fillInitData(callback);
	}

	public User getUser() {
		return user;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public Boolean isServiceMode() {
		return isServiceMode;
	}

	public String getBirthdayTheme() {
		return birthdayTheme;
	}

	public String getBirthdayMail() {
		return birthdayMail;
	}

	public Long getSalePeriod() {
		return salePeriod;
	}

	public String getAdultPriceFormula() {
		return adultPriceFormula;
	}

	public String getChildPriceFormula() {
		return childPriceFormula;
	}

	public String getBagPriceFormula() {
		return bagPriceFormula;
	}

	public String getStavAdultPriceFormula() {
		return adultStavPriceFormula;
	}

	public String getStavChildPriceFormula() {
		return childStavPriceFormula;
	}

	public String getStavBagPriceFormula() {
		return bagStavPriceFormula;
	}

	public Long getChildhood() {
		return childhood;
	}

	public boolean isProduction() {
		return isProduction;
	}

}