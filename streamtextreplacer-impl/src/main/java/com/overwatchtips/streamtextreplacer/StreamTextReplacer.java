// This file is part of the StreamTextReplacer project.
// Copyright (C) 2022 Fernando Pettinelli

// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// at your option any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <https://www.gnu.org/licenses/>.

package com.overwatchtips.streamtextreplacer;

import com.overwatchtips.streamtextreplacer.api.ReplacerPlugin;
import com.overwatchtips.streamtextreplacer.commands.CommandManager;
import com.overwatchtips.streamtextreplacer.config.OBSConfig;
import com.overwatchtips.streamtextreplacer.plugins.PluginManager;
import com.overwatchtips.streamtextreplacer.records.OBSSettings;
import com.overwatchtips.streamtextreplacer.threads.ConsoleThread;
import com.overwatchtips.streamtextreplacer.threads.QueryThread;
import net.twasi.obsremotejava.OBSRemoteController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class StreamTextReplacer {

    private static final Logger logger = LogManager.getLogger();

    private final OBSConfig obsConfig;

    private OBSRemoteController obsRemoteController;
    private PluginManager pluginManager;
    private CommandManager commandManager;
    private Timer queryTimer;
    private Timer consoleTimer;
    public StreamTextReplacer() {
        this.obsConfig = loadConfig();

        connectToWebSocket(controller -> {
            this.obsRemoteController = controller;
            this.pluginManager = new PluginManager(this);
            this.commandManager = new CommandManager(this);

            this.queryTimer = new Timer();
            this.consoleTimer = new Timer();
            queryTimer.scheduleAtFixedRate(new QueryThread(this), 0, 1000);
            consoleTimer.scheduleAtFixedRate(new ConsoleThread(this), 0, 1000);
        });
    }

    private void connectToWebSocket(Consumer<OBSRemoteController> callback) {
        OBSSettings obsSettings = obsConfig.getObsSettings();
        OBSRemoteController controller = new OBSRemoteController(obsSettings.address(),
                false,
                obsSettings.passwordProtected() ? obsSettings.webSocketPassword() : null);

        controller.registerConnectionFailedCallback(message -> {
            logger.fatal("Error while connecting to WebSocket: {}", message);
            System.exit(2);
        });

        controller.registerOnError((message, throwable) -> {
            logger.fatal("An error has occurred: {}", message);
            System.exit(2);
        });

        controller.registerDisconnectCallback(() -> {
            logger.fatal("WebSocket disconnected.");
            System.exit(0);
        });

        if (controller.isFailed()) {
            logger.fatal("Error while connecting to WebSocket.");
            System.exit(2);
        }

        controller.registerConnectCallback(response -> {
            logger.info("Connected to OBS WebSocket successfully. Loading plugins.");
            callback.accept(controller);
        });
    }

    public void shutdown() {
        for (ReplacerPlugin plugin : pluginManager.getLoadedPlugins()) {
            StreamTextReplacer.getLogger().info("Disabling {}, version {} by {}", plugin.getName(), plugin.getVersion(), plugin.getAuthor());
            plugin.onDisable();
        }

        queryTimer.cancel();
        queryTimer.purge();
        consoleTimer.cancel();
        consoleTimer.purge();
        obsRemoteController.disconnect();
        System.exit(0);
    }

    private OBSConfig loadConfig() {
        OBSConfig obsFile = null;
        try {
            obsFile = new OBSConfig(logger);
        } catch (IOException e) {
            logger.fatal("Error while loading OBS file: {}", e);
            System.exit(1);
        }

        return obsFile;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public OBSRemoteController getController() {
        return obsRemoteController;
    }

    public OBSConfig getObsConfig() {
        return obsConfig;
    }

    public PluginManager getPluginManager() {
        return pluginManager;
    }

    public static Logger getLogger() {
        return logger;
    }
}
