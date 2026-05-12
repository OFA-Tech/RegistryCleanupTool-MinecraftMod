package net.ofatech.registrycleanuptool.logging;

import java.io.Serializable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.ofatech.registrycleanuptool.discovery.MissingBlockCaptureStore;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.LoggerContext;

public final class ChunkSerializerLogWatcher {
    private final MissingRegistryLogParser parser = new MissingRegistryLogParser();
    private final MissingBlockCaptureStore store;
    private volatile ResourceKey<Level> currentDimension = Level.OVERWORLD;
    private Appender appender;
    private boolean active;

    public ChunkSerializerLogWatcher(MissingBlockCaptureStore store) { this.store = store; }
    public void setContextLevel(ServerLevel level) { this.currentDimension = level.dimension(); }
    public boolean isActive() { return active; }

    public synchronized void start() {
        if (active) return;
        LoggerContext ctx = LoggerContext.getContext(false);
        Logger root = ctx.getRootLogger();
        appender = new AbstractAppender("rct_chunk_serializer_watcher", null, (Layout<? extends Serializable>) null, false, Property.EMPTY_ARRAY) {
            @Override public void append(LogEvent event) {
                String loggerName = event.getLoggerName();
                String msg = event.getMessage().getFormattedMessage();
                if ((loggerName != null && loggerName.contains("ChunkSerializer")) || msg.contains("Unknown registry key in ResourceKey[minecraft:root / minecraft:block]")) {
                    store.addAll(parser.parse(currentDimension, msg));
                }
            }
        };
        appender.start();
        root.addAppender(appender);
        active = true;
    }

    public synchronized void stop() {
        if (!active || appender == null) return;
        LoggerContext ctx = LoggerContext.getContext(false);
        ctx.getRootLogger().removeAppender(appender);
        appender.stop();
        appender = null;
        active = false;
    }
}
