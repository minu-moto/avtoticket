/*
 * Copyright Avtoticket (c) 2015.
 */
package com.avtoticket.client.login;

import java.util.Date;
import java.util.logging.Logger;

import com.avtoticket.client.profile.ProfilePlace;
import com.avtoticket.client.ui.ImageBtn;
import com.avtoticket.client.utils.RPC;
import com.avtoticket.client.utils.SessionUtil;
import com.avtoticket.shared.models.core.Locales;
import com.avtoticket.shared.models.core.User;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.i18n.client.Constants;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 13 дек. 2015 г. 22:23:56
 */
public class LoginPanel extends FlexTable {

	private static final Logger logger = Logger.getLogger(LoginPanel.class.getName());

	public interface Style extends CssResource {
		String CSS_PATH = "login.css";

		String atLoginPanel();

		String atLoginHeader();

		String atLoginLogo();

		String atLoginPhone();

		String atLoginBtnLogin();

		String atLoginBtnRegister();

		String atLoginSocialBtns();

		String atLoginLoginFields();

		String atLoginBtnRemind();

		String atLoginRemember();

		String atLoginTextBox();

		String atLoginPasswordTextBox();

		String atLoginBtnLogout();

		String atLoginRegisterDialogLayout();

		String atLoginRemindDialogLayout();

		String atLoginRegisterDialogTitle();

		String atLoginRegisterDialogHint();

		String atLoginRegisterDialogFields();

		String atLoginRegisterDialogBtn();

		String atLoginProfile();
	}

	public interface Resources extends ClientBundle {

		ImageResource vk();

		ImageResource fb();

		ImageResource ok();

		ImageResource ru();

		ImageResource de();

		@Source(Style.CSS_PATH)
	    Style loginStyle();
	}

	public interface Strings extends Constants {
		@DefaultStringValue("AVTOTICKET.COM")
		String avtoticket();

		@DefaultStringValue("забыли пароль?")
		String remindPassword();

		@DefaultStringValue("запомнить меня")
		String remember();

		@DefaultStringValue("Восстановление")
		String remind();

		@DefaultStringValue("Восстановить")
		String doRemind();

		@DefaultStringValue("В ближайшее время на указанный адрес будет отправлено письмо с вашим паролем.")
		String remindEmail();

		@DefaultStringValue("Не удалось восстановить пароль на указанный E-mail")
		String remindFail();

		@DefaultStringValue("Вход")
		String enter();

		@DefaultStringValue("Регистрация")
		String register();

		@DefaultStringValue("Зарегистрироваться")
		String doRegister();

		@DefaultStringValue("Выйти")
		String exit();

		@DefaultStringValue("E-mail")
		String email();

		@DefaultStringValue("Неправильно указан E-mail")
		String invalidEmail();

		@DefaultStringValue("Пароль")
		String password();

		@DefaultStringValue("По вопросам приобретения электронных билетов обращайтесь по тел. +7/989/280-00-34, +7/989/280-00-35")
		String feedback();

		@DefaultStringValue("Вход через ВКонтакте")
		String vk();

		@DefaultStringValue("Вход через Facebook")
		String fb();

		@DefaultStringValue("Вход через Одноклассники")
		String ok();

		@DefaultStringValue("Для перевозчиков")
		String carrier();

		@DefaultStringValue("На указанный Вами e-mail будет отправлена<br>ссылка для подтверждения регистрации")
		String confirmMessage();

		@DefaultStringValue("В ближайшее время на указанный адрес будет отправлено письмо с дальнейшими инструкциями.")
		String confirmEmail();

		@DefaultStringValue(" не может быть пустым")
		String notEmpty();

		@DefaultStringValue("Указанный e-mail занят")
		String emailAlreadyTaken();
	}

	private static final Resources RESOURCES = GWT.create(Resources.class);
	private static final Style CSS = RESOURCES.loginStyle();
	private static final Strings STRINGS = GWT.create(Strings.class);

	private static final String VK_CLIENT_ID = "5066507";
	private static final String VK_REDIRECT_URI = "http://" + Window.Location.getHost() + "/vkauth";

	private static final String FB_CLIENT_ID = "954289294630389";
	private static final String FB_REDIRECT_URI = "http://" + Window.Location.getHost() + "/fbauth";

	private static final String OK_CLIENT_ID = "1155043328";
	private static final String OK_REDIRECT_URI = "http://" + Window.Location.getHost() + "/okauth";

	private static final int HIDE_TIMEOUT = 5000_000;

	private HorizontalPanel loginFields = new HorizontalPanel();
	private Anchor btnRemind;
	private TextBox edLogin = new TextBox();
	private PasswordTextBox edPassword = new PasswordTextBox();
	private CheckBox cbRemember;
	private Timer tmr = new Timer() {
		@Override
		public void run() {
			hideLoginFields();
		}
	};

	private Anchor btnLogin;
	private Anchor btnRegister;
	private Anchor btnLogout;

	private Image btnVK = new Image(RESOURCES.vk());
	private Image btnFB = new Image(RESOURCES.fb());
	private Image btnOK = new Image(RESOURCES.ok());

	private native void popupCenter(String url, String title, int w, int h) /*-{
	    // Fixes dual-screen position                         Most browsers      Firefox
	    var dualScreenLeft = $wnd.screenLeft != undefined ? $wnd.screenLeft : screen.left;
	    var dualScreenTop = $wnd.screenTop != undefined ? $wnd.screenTop : screen.top;

	    width = $wnd.innerWidth ? $wnd.innerWidth : $doc.documentElement.clientWidth ? $doc.documentElement.clientWidth : screen.width;
	    height = $wnd.innerHeight ? $wnd.innerHeight : $doc.documentElement.clientHeight ? $doc.documentElement.clientHeight : screen.height;

	    var left = ((width / 2) - (w / 2)) + dualScreenLeft;
	    var top = ((height / 2) - (h / 2)) + dualScreenTop;
	    var newWindow = $wnd.open(url, title, 'scrollbars=yes, width=' + w + ', height=' + h + ', top=' + top + ', left=' + left);

	    // Puts focus on the newWindow
	    if ($wnd.focus) {
	        newWindow.focus();
	    }
	}-*/;

	@SuppressWarnings("deprecation")
	private void setLocaleCookie(String locale) {
	    String cookieName = LocaleInfo.getLocaleCookieName();
	    if (cookieName != null) {
	        Date expires = new Date();
	        expires.setYear(expires.getYear() + 1);
	        Cookies.setCookie(cookieName, locale, expires);
	    }
	    Window.Location.reload();
	}

	public LoginPanel() {
		CSS.ensureInjected();

		addStyleName(CSS.atLoginPanel());

		FocusHandler focusHandler = new FocusHandler() {
			@Override
			public void onFocus(FocusEvent event) {
				tmr.cancel();
			}
		};
		BlurHandler blurHandler = new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				tmr.schedule(HIDE_TIMEOUT);
			}
		};
		edLogin.addStyleName(CSS.atLoginTextBox());
		edLogin.setName("login");
		edLogin.getElement().setAttribute("placeholder", STRINGS.email());
		edLogin.addFocusHandler(focusHandler);
		edLogin.addBlurHandler(blurHandler);
		edPassword.addStyleName(CSS.atLoginPasswordTextBox());
		edPassword.setName("password");
		edPassword.getElement().setAttribute("placeholder", STRINGS.password());
		edPassword.addFocusHandler(focusHandler);
		edPassword.addBlurHandler(blurHandler);
		btnRemind = new Anchor(STRINGS.remindPassword());
		btnRemind.addStyleName(CSS.atLoginBtnRemind());
		cbRemember = new CheckBox(STRINGS.remember());
		cbRemember.setName("remember");
		cbRemember.addStyleName(CSS.atLoginRemember());

		loginFields.addDomHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeKeyCode() == 13)
					SessionUtil.doLogin(edLogin.getValue(), edPassword.getValue(), cbRemember.getValue());
			}
		}, KeyDownEvent.getType());
		loginFields.addStyleName(CSS.atLoginLoginFields());
		loginFields.add(btnRemind);
		loginFields.add(edLogin);
		loginFields.add(edPassword);
		loginFields.add(cbRemember);

		Anchor btnLogo = new Anchor(STRINGS.avtoticket(), "#");
		btnLogo.addStyleName(CSS.atLoginLogo());
		ImageBtn btnRu = new ImageBtn(RESOURCES.ru(), Locales.RU.toString(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				setLocaleCookie(Locales.RU.name().toLowerCase());
			}
		});
		ImageBtn btnDe = new ImageBtn(RESOURCES.de(), Locales.DE.toString(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				setLocaleCookie(Locales.DE.name().toLowerCase());
			}
		});

		HorizontalPanel hp = new HorizontalPanel();
		hp.add(btnLogo);
		hp.add(btnRu);
		hp.add(btnDe);
		btnRu.setVisible(!SessionUtil.isProduction());
		btnDe.setVisible(!SessionUtil.isProduction());

		User usr = SessionUtil.getUser();
		setText(0, 0, "");
		setWidget(0, 1, hp);
		setText(0, 2, STRINGS.feedback());
		btnLogin = new Anchor(STRINGS.enter());
		btnRegister = new Anchor(STRINGS.register());
		btnLogout = new Anchor(STRINGS.exit());
		if (usr == null) {
			hp = new HorizontalPanel();
			hp.add(btnLogin);
			hp.add(btnRegister);
			setWidget(0, 3, hp);
//			setWidget(0, 4, btnRegister);
		} else {
			setWidget(0, 3, btnLogout);
			setHTML(1, 2, "<a href='#" + ProfilePlace.NAME + "'>" + usr.getLogin() + "</a>");
			getCellFormatter().addStyleName(1, 2, CSS.atLoginProfile());
		}
		setText(0, 4, "");

		btnLogin.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (loginFields.isAttached())
					SessionUtil.doLogin(edLogin.getValue(), edPassword.getValue(), cbRemember.getValue());
				else
					showLoginFields();
			}
		});
		btnRegister.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				new RegisterDialog(CSS, STRINGS).center();
			}
		});
		btnLogout.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				RPC.getTS().logout(new AsyncCallback<Void>() {
					@Override
					public void onSuccess(Void result) {
						Window.Location.replace("#");
						Window.Location.reload();
					}

					@Override
					public void onFailure(Throwable caught) {
						Window.alert(caught.getMessage());
					}
				});
			}
		});
		btnRemind.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				new RemindDialog(CSS, STRINGS).center();
			}
		});

		getCellFormatter().addStyleName(0, 2, CSS.atLoginPhone());
		btnLogin.addStyleName(CSS.atLoginBtnLogin());
		btnRegister.addStyleName(CSS.atLoginBtnRegister());
		btnLogout.addStyleName(CSS.atLoginBtnLogout());

		getColumnFormatter().setWidth(0, "50%");
		getColumnFormatter().setWidth(1, "250px");
		getColumnFormatter().setWidth(2, "1230px");
		getColumnFormatter().setWidth(3, "220px");
		getColumnFormatter().setWidth(4, "50%");

		if (usr == null) {
			hp = new HorizontalPanel();

			btnVK.addStyleName(CSS.atLoginSocialBtns());
			btnVK.setAltText(STRINGS.vk());
			btnVK.setTitle(STRINGS.vk());
			btnVK.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					popupCenter("https://oauth.vk.com/authorize?client_id=" + VK_CLIENT_ID
									+ "&redirect_uri=" + VK_REDIRECT_URI + "&display=popup&scope=email&response_type=code&v=5.37",
									"vk_auth", 600, 400);
				}
			});
			hp.add(btnVK);

			btnFB.setVisible(false);	// TODO убрать после перехода на HTTPS
			btnFB.addStyleName(CSS.atLoginSocialBtns());
			btnFB.setAltText(STRINGS.fb());
			btnFB.setTitle(STRINGS.fb());
			btnFB.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					popupCenter("https://www.facebook.com/dialog/oauth?client_id=" + FB_CLIENT_ID
									+ "&redirect_uri=" + FB_REDIRECT_URI + "&display=popup&scope=email,user_birthday&response_type=code", "fb_auth", 600, 400);
				}
			});
			hp.add(btnFB);

			btnOK.setVisible(false);	// TODO убрать после перехода на HTTPS
			btnOK.addStyleName(CSS.atLoginSocialBtns());
			btnOK.setAltText(STRINGS.ok());
			btnOK.setTitle(STRINGS.ok());
			btnOK.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					popupCenter("https://connect.ok.ru/oauth/authorize?client_id=" + OK_CLIENT_ID
									+ "&redirect_uri=" + OK_REDIRECT_URI + "&display=popup&layout=w&scope=GET_EMAIL&response_type=code", "ok_auth", 600, 400);
				}
			});
			hp.add(btnOK);
			setWidget(1, 3, hp);
//			getFlexCellFormatter().setColSpan(1, 3, 2);
		} else
			setText(1, 0, "\u00a0");

		getRowFormatter().setStyleName(0, CSS.atLoginHeader());
	}

	private void showLoginFields() {
		setWidget(0, 2, loginFields);
		edLogin.setFocus(true);
	}

	private void hideLoginFields() {
		setText(0, 2, STRINGS.feedback());
	}

}