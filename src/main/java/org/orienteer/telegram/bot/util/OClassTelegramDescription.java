package org.orienteer.telegram.bot.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.iterator.ORecordIteratorClass;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.telegram.bot.AbstractOTelegramBot;
import org.orienteer.telegram.bot.Cache;
import org.orienteer.telegram.module.OTelegramModule;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

import java.util.*;

/**
 * Represents {@link OClass} description in Telegram
 */
public class OClassTelegramDescription {
    private final String className;

    public OClassTelegramDescription(String classLink) {
        className = classLink.substring(BotState.GO_TO_CLASS.getCommand().length());
    }

    public Map<Integer, String> getDescription() {
        return new DBClosure<Map<Integer, String>>() {
            @Override
            protected Map<Integer, String> execute(ODatabaseDocument db) {
                Map<Integer, String> result = Maps.newHashMap();
                if (!Cache.getClassCache().containsKey(className)) {
                    result = new HashMap<>();
                    result.put(0, MessageKey.SEARCH_FAILED_CLASS_BY_NAME.getString());
                    return result;
                }
                OClass oClass = Cache.getClassCache().get(className);
                List<String> telegramDocs = getTelegramDocuments(oClass, db);
                String head = createOClassHeadDescription(oClass, !telegramDocs.isEmpty());
                if (!telegramDocs.isEmpty()) {
                    result.put(0, head + Markdown.BOLD.toString("1. ") + telegramDocs.get(0) + "\n");
                    for (int i = 1; i < telegramDocs.size(); i++) {
                        if (i % 10 == 0) {
                            result.put(i, head + Markdown.BOLD.toString((i + 1) + ". ") + telegramDocs.get(i) + "\n");
                        } else {
                            result.put(i, Markdown.BOLD.toString((i + 1) + ". ") + telegramDocs.get(i) + "\n");
                        }
                    }
                } else result.put(0, head);
                return result;
            }
        }.execute();
    }

    private String createOClassHeadDescription(OClass oClass, boolean docsPresent) {
        StringBuilder sb = new StringBuilder();
        sb.append(Markdown.BOLD.toString(MessageKey.CLASS_DESCRIPTION_MSG.getString()))
                .append("\n\n")
                .append(Markdown.BOLD.toString(MessageKey.NAME.getString()))
                .append(" ")
                .append(oClass.getName())
                .append("\n")
                .append(Markdown.BOLD.toString(MessageKey.SUPER_CLASSES.getString()))
                .append(" ");
        appendSuperClasses(sb, oClass, Cache.getClassCache());
        appendProperties(sb, oClass);
        if (docsPresent) sb.append(Markdown.BOLD.toString(MessageKey.CLASS_DOCUMENTS.getString())).append("\n");
        else sb.append(Markdown.BOLD.toString(MessageKey.WITHOUT_DOCUMENTS.getString()));
        return sb.toString();
    }

    private void appendSuperClasses(StringBuilder sb, OClass oClass, Map<String, OClass> classCache) {
        if (!oClass.getSuperClasses().isEmpty()) {
            for (OClass superClass : oClass.getSuperClasses()) {
                if (classCache.containsKey(superClass.getName())) {
                    sb.append("/").append(superClass.getName()).append(" ");
                }
            }
        } else sb.append(MessageKey.WITHOUT_SUPER_CLASSES.getString());
        sb.append("\n");
    }

    private void appendProperties(StringBuilder sb, OClass oClass) {
        List<String> properties = getTelegramProperties(oClass);
        if (!properties.isEmpty()) {
            for (String property : properties) {
                sb.append(property)
                        .append("\n");
            }
        } else sb.append(MessageKey.WITHOUT_PROPERTIES.getString());
        sb.append("\n");
    }

    private List<String> getTelegramProperties(OClass oClass) {
        List<String> result = Lists.newArrayList();
        if (OTelegramModule.TELEGRAM_CLASS_DESCRIPTION.getValue(oClass)) {
            Collection<OProperty> properties = oClass.properties();
            for (OProperty property : properties) {
                result.add(Markdown.BOLD.toString(property.getName() + ":") + " " + property.getDefaultValue() + " (" + MessageKey.DEFAULT_VALUE.getString() + ")");
            }
            Collections.sort(result);
        }
        return result;
    }

    private List<String> getTelegramDocuments(OClass oClass, ODatabaseDocument db) {
        List<String> result = Lists.newArrayList();
        if (OTelegramModule.TELEGRAM_DOCUMENTS_LIST.getValue(oClass)) {
            ORecordIteratorClass<ODocument> docs = db.browseClass(oClass.getName());
            for (ODocument oDocument : docs) {
                String docId = BotState.GO_TO_CLASS.getCommand() + oDocument.getClassName()
                        + "\\_" + oDocument.getIdentity().getClusterId()
                        + "\\_" + oDocument.getIdentity().getClusterPosition();
                String docName = AbstractOTelegramBot.getDocName(oDocument);
                result.add(docName + " " + docId);
            }
            Collections.sort(result);
        }
        return result;
    }
}
