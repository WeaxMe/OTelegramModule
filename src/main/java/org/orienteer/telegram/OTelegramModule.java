package org.orienteer.telegram;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.module.AbstractOrienteerModule;
import org.orienteer.core.util.OSchemaHelper;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.metadata.schema.OType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class OTelegramModule extends AbstractOrienteerModule{

	private static final Logger LOG = LoggerFactory.getLogger(OTelegramModule.class);

	public static final String OCLASS_NAME = "OTelegramBot";
	public static final String OPROPERTY_USERNAME = "username";
	public static final String OPROPERTY_TOKKEN = "tokken";

	protected OTelegramModule() {
		super("OTelegramModule", 1);
	}
	
	@Override
	public ODocument onInstall(OrienteerWebApplication app, ODatabaseDocument db) {
		OSchemaHelper helper = OSchemaHelper.bind(db);
		helper.oClass(OCLASS_NAME, "OModule")
				.oProperty(OPROPERTY_USERNAME, OType.STRING)
				.oProperty(OPROPERTY_TOKKEN, OType.STRING)
				.switchDisplayable(true, OPROPERTY_USERNAME, OPROPERTY_TOKKEN);
		LOG.info("Install");
		//Install data model
		//Return null of default OModule is enough
		return helper.getODocument();
	}

	@Override
	public void onInitialize(OrienteerWebApplication app, ODatabaseDocument db) {
		createClass(db);
		getOrientClasses(db);
	}



	protected void createClass(ODatabaseDocument db) {
		if (!db.getMetadata().getSchema().existsClass(OCLASS_NAME)) {
			OClass oTelegramClass = db.getMetadata().getSchema().createClass(OCLASS_NAME);
			OClass oModule = db.getMetadata().getSchema().getClass("OModule");
			oTelegramClass.addSuperClass(oModule);
			oTelegramClass.createProperty(OPROPERTY_USERNAME, OType.STRING);
			oTelegramClass.createProperty(OPROPERTY_TOKKEN, OType.STRING);
		}
	}

	protected void getOrientClasses(ODatabaseDocument db) {
		OClass omodule = db.getMetadata().getSchema().getClass("OModule");
		Collection<OClass> allSubclasses = omodule.getAllSubclasses();

		Collection<OClass> classes = db.getMetadata().getSchema().getClasses();
		int count = 0;
		LOG.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		LOG.info("!!!                                          !!!");
		LOG.info("!!!            Start testing                 !!!");
		LOG.info("!!!                                          !!!");
		LOG.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		LOG.info("allSubclasses: " + allSubclasses.size());
		LOG.info("all schema: " + db.getMetadata().getSchema());
		LOG.info("classes.size(): " + classes.size());
		for (OClass oClass : classes) {
			LOG.info(count + " class name: " + oClass.getName());
			count++;
		}
	}


}