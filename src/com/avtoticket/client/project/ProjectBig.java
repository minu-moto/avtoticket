/*
 * Copyright Avtoticket (c) 2015.
 */
package com.avtoticket.client.project;

import com.avtoticket.client.project.ProjectPlace.Strings;
import com.avtoticket.client.project.ProjectPlace.Style;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 20 дек. 2015 г. 21:37:27
 */
public class ProjectBig extends FlowPanel {

	public ProjectBig(Style CSS, Strings STRINGS) {
		HTML content = new HTML(STRINGS.content());
		content.addStyleName(CSS.atProjectBig());
		add(content);

		Label label = new Label(STRINGS.caption());
		label.addStyleName(CSS.atProjectLabel());
		add(label);
	}

}