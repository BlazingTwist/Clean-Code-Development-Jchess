package logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.LevelFilter;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.spi.ContextAwareBase;

import java.util.Arrays;
import java.util.List;

public class LogbackLoggerConfigurator extends ContextAwareBase implements Configurator {

    private static final String LOG_FILE = "logs/jChess.log";

    @Override
    public ExecutionStatus configure(LoggerContext context) {
        ConsoleAppender<ILoggingEvent> consoleAppender = createConsoleAppender();
        FileAppender<ILoggingEvent> fileAppender = createFileAppender();

        Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(consoleAppender);
        rootLogger.addAppender(fileAppender);

        // if a specific package logger is too vocal, you can silence it like this:
        List<OutputStreamAppender<ILoggingEvent>> appenders = Arrays.asList(consoleAppender, fileAppender);
        configureLogger(context, Level.INFO, appenders, "io.undertow");
        configureLogger(context, Level.INFO, appenders, "org.xnio");

        return ExecutionStatus.DO_NOT_INVOKE_NEXT_IF_ANY;
    }

    private static void configureLogger(LoggerContext ctx, Level minLevel, List<OutputStreamAppender<ILoggingEvent>> appenders, String loggerName) {
        Logger logger = ctx.getLogger(loggerName);
        logger.setLevel(minLevel);
        logger.setAdditive(false);
        for (OutputStreamAppender<ILoggingEvent> appender : appenders) {
            logger.addAppender(appender);
        }
    }

    private ConsoleAppender<ILoggingEvent> createConsoleAppender() {
        ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<>();
        appender.setContext(this.context);
        appender.setName("console");
        appender.setTarget("System.out");

        PatternLayout.DEFAULT_CONVERTER_MAP.put("highlightConsole", LogLevelCompositeConverter.class.getName());
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setPattern("%white(%date{HH:mm:ss.SSS})"
                + " %cyan([%-16.16thread])"
                + " %highlightConsole(%-5level) :"
                + " %magenta(%-24logger{24})"
                + " [%C]"
                + " - %msg"
                + "%n%nopex%highlightConsole(%ex)");
        encoder.setContext(this.context);
        encoder.start();
        appender.setEncoder(encoder);

        appender.start();
        return appender;
    }

    private FileAppender<ILoggingEvent> createFileAppender() {
        RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<>();
        appender.setContext(this.context);
        appender.setName("file");
        appender.setFile(LOG_FILE);

        FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
        rollingPolicy.setContext(this.context);
        rollingPolicy.setParent(appender);
        rollingPolicy.setFileNamePattern(LOG_FILE + ".%i.zip");
        rollingPolicy.setMinIndex(1);
        rollingPolicy.setMaxIndex(5);
        rollingPolicy.start();
        appender.setRollingPolicy(rollingPolicy);

        StartupTriggeringPolicy<ILoggingEvent> triggerPolicy = new StartupTriggeringPolicy<>();
        triggerPolicy.start();
        appender.setTriggeringPolicy(triggerPolicy);

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setPattern("%date{HH:mm:ss.SSS} [%-16.16thread] %-5level : %-24logger{24} - %msg%n");
        encoder.setContext(this.context);
        encoder.start();
        appender.setEncoder(encoder);

        LevelFilter levelFilter = new LevelFilter();
        levelFilter.setLevel(Level.INFO);
        levelFilter.start();
        appender.addFilter(levelFilter);

        appender.start();
        return appender;
    }
}
