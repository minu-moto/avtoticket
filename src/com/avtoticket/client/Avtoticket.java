package com.avtoticket.client;

import com.avtoticket.client.login.LoginPanel;
import com.avtoticket.client.menu.MainMenu;
import com.avtoticket.client.utils.SessionUtil;
import com.avtoticket.shared.models.BaseModel;
import com.avtoticket.shared.models.Reflection;
import com.avtoticket.shared.utils.DateUtil;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.i18n.client.Constants;
import com.google.gwt.i18n.client.TimeZone;
import com.google.gwt.i18n.client.TimeZoneInfo;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;

public class Avtoticket implements EntryPoint {

	public static interface Scaler {
		public void zoomBody(double width);
	}

	public static class DefaultScaler implements Scaler {
		@Override
		public void zoomBody(double width) {
			Double scale = Math.min(width, 1920) / 1920.0;
			RootPanel.get().getElement().getStyle().setProperty("zoom", String.valueOf(scale));
		}
	}

	public static class MozillaScaler implements Scaler {
		@Override
		public void zoomBody(double width) {
			Double scale = Math.min(width, 1920) / 1920.0;
			RootPanel.get().getElement().getStyle().setProperty("transform", "scale(" + scale + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			RootPanel.get().getElement().getStyle().setProperty("marginLeft", 50 - 50 / scale + "%"); //$NON-NLS-1$
			RootPanel.get().getElement().getStyle().setProperty("marginTop", Window.getClientHeight() * (scale - 1) / 2 + "px");
			RootPanel.get().getElement().getStyle().setProperty("width", 100 / scale + "%");
		}
	}

	public interface Style extends CssResource {
		String CSS_PATH = "avtoticket.css";

		String atHeadBackground();

		String atBody();

		String atBodyBackground();

		String atFooter();

		String atStavr();
	}

	public interface Resources extends ClientBundle {

		ImageResource background();

		ImageResource copyright();

		@Source(Style.CSS_PATH)
	    Style avtoticketStyle();
	}

	public interface Strings extends Constants {
		@DefaultStringValue("Билеты из Ставропольского края")
		String stavropol();

		@DefaultStringValue("Проект разработан компанией ApPoint\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A02015")
		String footer();
	}

	private static final Resources RESOURCES = GWT.create(Resources.class);
	private static final Style CSS = RESOURCES.avtoticketStyle();
	private static final Strings STRINGS = GWT.create(Strings.class);
	private static final Scaler scaler = GWT.create(Scaler.class);

	@Override
	public void onModuleLoad() {
		CSS.ensureInjected();
		DateUtil.setMSKTimeZone(TimeZone.createTimeZone(TimeZoneInfo.buildTimeZoneData("{\"id\": \"Europe/Moscow\", "
				+ "\"transitions\": [98589, 60, 102980, 0, 107349, 60, 111740, 0, 116109, 60, 120500, 0, 124893, 60, 129263, 0, 133631, 60, 137999, 0, 142367, 60, 146735, 0, 151103, 60, 155471, 0, 159839, 60, 164207, 0, 168575, 60, 172943, 0, 177311, 60, 181847, 0, 186215, 0, 190584, -60, 193272, 0, 194948, 60, 199315, 0, 203687, 60, 208055, 0, 212423, 60, 216791, 0, 221159, 60, 225527, 0, 230063, 60, 235103, 0, 238799, 60, 243839, 0, 247535, 60, 252575, 0, 256271, 60, 261479, 0, 265007, 60, 270215, 0, 273743, 60, 278951, 0, 282647, 60, 287687, 0, 291383, 60, 296423, 0, 300119, 60, 305327, 0, 308855, 60, 314063, 0, 317591, 60, 322799, 0, 326327, 60, 331535, 0, 335231, 60, 340271, 0, 343967, 60, 349007, 0, 352703, 60, 357911, 0, 361439, 60, 392854, 0], "
				+ "\"names\": [\"MSK\", \"Moscow Standard Time\", \"MSD\", \"Moscow Summer Time\"], \"std_offset\": 180}")));
		BaseModel.setRefl(GWT.<Reflection> create(Reflection.class));
		SessionUtil.init(ret -> {
			RootPanel rp = RootPanel.get();
			rp.add(new LoginPanel());

			SimplePanel hb = new SimplePanel();
			hb.addStyleName(CSS.atHeadBackground());
			hb.setWidget(new MainMenu());
			rp.add(hb);

			SimplePanel bb = new SimplePanel();
			bb.addStyleName(CSS.atBodyBackground());
			bb.setWidget(new BodyPanel(CSS, STRINGS));
			rp.add(bb);

			rp.add(new FooterPanel(CSS, STRINGS));
		});
		Window.addResizeHandler(new ResizeHandler() {
			@Override
			public void onResize(ResizeEvent event) {
				scaler.zoomBody(event.getWidth());
			}
		});
		scaler.zoomBody(Window.getClientWidth());
	}

}