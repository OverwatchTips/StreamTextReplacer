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

package com.overwatchtips.streamtextreplacer.api;

import com.overwatchtips.streamtextreplacer.api.commands.ConsoleCommand;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Collections;
import java.util.Map;

public abstract class ReplacerPlugin {

    private File dataFolder;
    private boolean dataFolderSet = false;

    private final Logger logger;
    public ReplacerPlugin(Logger logger) {
        this.logger = logger;
    }

    // Plugin metadata
    public abstract String getName();
    public abstract String getAuthor();
    public abstract String getIdentifier();
    public abstract String getVersion();

    // Commands to register. Can be overriden by plugins.
    public Map<String, ConsoleCommand> getCommandsToRegister() {
        return Collections.emptyMap();
    }

    // When the plugin gets enabled.
    // If it returns false, an error occurred, and the plugin won't be registered.
    public abstract boolean onEnable();

    // When the plugin gets disabled.
    public abstract void onDisable();

    // how often should its placeholders refresh? (seconds)
    public abstract long getRefreshTime();

    // When a placeholder is requested
    public abstract String onRequest(String params);

    public File getDataFolder() {
        return dataFolder;
    }

    public void setDataFolder(File dataFolder) {
        if (dataFolderSet) {
            throw new UnsupportedOperationException("Cannot set data folder from a plugin.");
        }

        this.dataFolder = dataFolder;
        this.dataFolderSet = true;
    }

    public Logger getLogger() {
        return logger;
    }
}
