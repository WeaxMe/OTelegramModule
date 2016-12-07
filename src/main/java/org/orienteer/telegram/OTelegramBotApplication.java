package org.orienteer.telegram;

import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.telegram.module.OTelegramModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OTelegramBotApplication extends OrienteerWebApplication {
	private static final Logger LOG = LoggerFactory.getLogger(OTelegramBotApplication.class);

	@Override
	public void init() {
		super.init();
		registerWidgets("org.orienteer.telegram.component.widget");
		registerModule(OTelegramModule.class);
	}
}
