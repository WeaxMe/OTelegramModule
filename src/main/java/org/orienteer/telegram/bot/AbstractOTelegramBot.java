package org.orienteer.telegram.bot;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.wicket.Application;
import org.apache.wicket.Localizer;
import org.apache.wicket.ThreadContext;
import org.orienteer.telegram.bot.handler.LongPollingHandlerConfig;
import org.orienteer.telegram.bot.handler.OTelegramLongPollingHandler;
import org.orienteer.telegram.bot.handler.OTelegramWebHookHandler;
import org.orienteer.telegram.bot.handler.WebHookHandlerConfig;
import org.orienteer.telegram.bot.util.BotState;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Abstract class for manage telegram bot
 */
public abstract class AbstractOTelegramBot {

    private static Application application;
    private static UserSession currentSession;
    private static boolean groupChat;

    /**
     * Create new {@link OTelegramLongPollingHandler}
     * @param botConfig {@link LongPollingHandlerConfig} - bot config
     * @return {@link OTelegramLongPollingHandler}
     */
    public static synchronized OTelegramLongPollingHandler getLongPollingBot(LongPollingHandlerConfig botConfig) {
        LoadingCache<Integer, UserSession> sessions = setUpDefaultConfig(botConfig.userSession);
        return new OTelegramLongPollingHandler(botConfig, sessions);
    }

    public static synchronized OTelegramWebHookHandler getWebHookBot(WebHookHandlerConfig botConfig) {
        LoadingCache<Integer, UserSession> sessions = setUpDefaultConfig(botConfig.userSession);
        return new OTelegramWebHookHandler(botConfig, sessions);
    }

    private static synchronized LoadingCache<Integer, UserSession> setUpDefaultConfig(long userSession) {
        LoadingCache<Integer, UserSession> sessions = CacheBuilder.newBuilder()
                .expireAfterWrite(userSession, TimeUnit.MINUTES)
                .build(
                        new CacheLoader<Integer, UserSession>() {
                            @Override
                            public UserSession load(Integer key) {
                                return new UserSession();
                            }
                        });
        return sessions;
    }

    public static synchronized void setApplication(Application app) {
        application = app;
        ThreadContext.setApplication(application);
    }

    public static boolean isGroupChat() {
        return groupChat;
    }

    public static void setGroupChat(boolean isGroupChat) {
        groupChat = isGroupChat;
    }

    public static synchronized Localizer getLocalizer() {
        return application.getResourceSettings().getLocalizer();
    }

    public static synchronized Locale getCurrentLocale() {
        return currentSession.getLocale();
    }

    public static synchronized void setCurrentLocale(Locale locale) {
        currentSession.setLocale(locale);
    }

    public static synchronized BotState getCurrentBotState() {
        return currentSession.getBotState();
    }

    public static synchronized void setCurrentBotState(BotState botState) {
        currentSession.setBotState(botState);
    }

    public static synchronized UserSession getCurrentSession() {
        return currentSession;
    }

    public static synchronized void setCurrentSession(UserSession userSession) {
        currentSession = userSession;
    }
}