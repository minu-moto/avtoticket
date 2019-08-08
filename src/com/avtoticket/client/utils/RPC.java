package com.avtoticket.client.utils;

import com.avtoticket.shared.rpc.TicketService;
import com.avtoticket.shared.rpc.TicketServiceAsync;
import com.google.gwt.core.shared.GWT;

public class RPC {

	private static TicketServiceAsync ts;

	public static TicketServiceAsync getTS() {
		if (ts == null)
			ts = GWT.create(TicketService.class);
		return ts;
	}

}