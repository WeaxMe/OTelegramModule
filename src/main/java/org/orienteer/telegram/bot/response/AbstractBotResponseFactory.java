package org.orienteer.telegram.bot.response;

import org.orienteer.telegram.bot.AbstractOTelegramBot;
import org.orienteer.telegram.bot.UserSession;
import org.orienteer.telegram.bot.util.*;
import org.orienteer.telegram.bot.search.Result;
import org.orienteer.telegram.bot.search.Search;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;

import java.util.Locale;
import java.util.Map;

/**
 * Abstract factory which creates {@link OTelegramBotResponse}
 */
public abstract class AbstractBotResponseFactory {

    public static OTelegramBotResponse createResponse(Message message) {
        UserSession userSession = AbstractOTelegramBot.getCurrentSession();
        BotState state = getBotState(message.getText());
        SendMessage sendMessage;
        state = state == BotState.BACK ? userSession.getPreviousBotState() : state;
        if (state == null) {
            userSession.setBotState(BotState.NEW_CLASS_SEARCH);
            userSession.setPreviousBotState(BotState.START);
            sendMessage = AbstractResponseMessageFactory.createStartMenu(message);
            return new OTelegramBotResponse(sendMessage);
        } else return new OTelegramBotResponse(createSendMessageFromState(state, message, userSession));
    }

    private static SendMessage createSendMessageFromState(BotState state, Message message, UserSession userSession) {
        SendMessage sendMessage;
        switch (state) {
            case START:
                userSession.setBotState(BotState.NEW_CLASS_SEARCH);
                userSession.setPreviousBotState(BotState.START);
                sendMessage = AbstractResponseMessageFactory.createStartMenu(message);
                break;
            case CLASS_SEARCH:
                userSession.setTargetClass(message.getText().substring(MessageKey.CLASS_BUT.getString().length()));
                userSession.setBotState(BotState.SEARCH_IN_CLASS_GLOBAL);
                userSession.setPreviousBotState(BotState.START);
                sendMessage = AbstractResponseMessageFactory.createBackMenu(message, String.format(MessageKey.CLASS_SEARCH_MSG.getString(), "/" + userSession.getTargetClass()));
                break;
            case GO_TO_DOCUMENT_SHORT_DESCRIPTION:
                sendMessage = AbstractResponseMessageFactory.createDocumentDescription(new ODocumentTelegramDescription(message.getText(), false), message, userSession, false);
                break;
            case GO_TO_CLASS:
                sendMessage = setResultOfSearch(new OClassTelegramDescription(message.getText()).getDescription(), message, userSession);
                break;
            case CHANGE_LANGUAGE:
                userSession.setLocale(changeLanguage(message));
                userSession.setPreviousBotState(BotState.START);
                userSession.setBotState(BotState.NEW_CLASS_SEARCH);
                sendMessage = AbstractResponseMessageFactory.createStartMenu(message);
                break;
            case LANGUAGE:
                sendMessage = AbstractResponseMessageFactory.createLanguageMenu(message);
                break;
            case ABOUT:
                sendMessage = AbstractResponseMessageFactory.createTextMessage(message, MessageKey.ABOUT_MSG.getString());
                break;
            default:
                sendMessage = handleSearchRequest(message, userSession);
        }
        return sendMessage;
    }

    private static SendMessage handleSearchRequest(Message message, UserSession userSession) {
        Result result = null;
        Search search;
        SendMessage sendMessage;
        switch (userSession.getBotState()) {
            case SEARCH_IN_CLASS_GLOBAL:
                search = Search.getSearch(message.getText(), userSession.getTargetClass(), userSession.getLocale());
                result = search.execute();
                break;
            case NEW_CLASS_SEARCH:
                search = Search.getSearch(message.getText(), null, userSession.getLocale());
                result = search.execute();
                break;
        }
        if (result != null) {
            sendMessage = setResultOfSearch(result.getResultOfSearch(), message, userSession);
        } else sendMessage = AbstractResponseMessageFactory.createTextMessage(message,
                Markdown.BOLD.toString(MessageKey.SEARCH_RESULT_FAILED_MSG.getString()));

        return sendMessage;
    }

    private static SendMessage setResultOfSearch(Map<Integer, String> resultOfSearch, Message message, UserSession userSession) {
        SendMessage sendMessage;
        userSession.setResultOfSearch(resultOfSearch);
        if (resultOfSearch.size() > 1) {
            sendMessage = AbstractResponseMessageFactory.createPagingMenu(message, userSession);
        } else {
            sendMessage = AbstractResponseMessageFactory.createTextMessage(message, userSession.getResultInPage());
        }
        return sendMessage;
    }

    private static Locale changeLanguage(Message message) {
        String lang = message.getText().substring(MessageKey.LANGUAGE_BUT.getString().length());
        if (lang.equals(MessageKey.ENGLISH.toString())) {
            return new Locale("en");
        } else if (lang.equals(MessageKey.RUSSIAN.toString())) {
            return new Locale("ru");
        } else {
            return new Locale("uk");
        }
    }

    private static BotState getBotState(String command) {
        BotState state = searchBotState(command);
        if (state == BotState.ERROR) {
            if (command.startsWith(BotState.GO_TO_CLASS.getCommand()) && command.endsWith(BotState.DETAILS.getCommand())) {
                state = BotState.GO_TO_DOCUMENT_ALL_DESCRIPTION;
            } else if (command.startsWith(MessageKey.LANGUAGE_BUT.getString())) {
                state = BotState.CHANGE_LANGUAGE;
            } else if (command.startsWith(BotState.GO_TO_CLASS.getCommand()) &&
                    command.contains(BotState.GO_TO_DOCUMENT_SHORT_DESCRIPTION.getCommand())) {
                state = BotState.GO_TO_DOCUMENT_SHORT_DESCRIPTION;
            } else if (command.startsWith(BotState.GO_TO_CLASS.getCommand())) {
                state = BotState.GO_TO_CLASS;
            } else if (command.startsWith(MessageKey.CLASS_BUT.getString())) {
                state = BotState.CLASS_SEARCH;
            } else if (command.equals(MessageKey.NEXT_RESULT_BUT.getString())) {
                state = BotState.NEXT_RESULT;
            } else if (command.endsWith(MessageKey.PREVIOUS_RESULT_BUT.getString())) {
                state = BotState.PREVIOUS_RESULT;
            } else if (command.equals(MessageKey.BACK.getString())) {
                state = BotState.BACK;
            }
        }
        return state;
    }

    private static BotState searchBotState(String command) {
        BotState result = BotState.ERROR;
        for (BotState search : BotState.values()) {
            if (search.getCommand().equals(command)) {
                result = search;
                break;
            }
        }
        return result;
    }
}
