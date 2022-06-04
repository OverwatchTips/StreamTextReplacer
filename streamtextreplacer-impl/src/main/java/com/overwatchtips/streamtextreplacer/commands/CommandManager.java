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

package com.overwatchtips.streamtextreplacer.commands;

import com.overwatchtips.streamtextreplacer.StreamTextReplacer;
import com.overwatchtips.streamtextreplacer.api.commands.ConsoleCommand;
import com.overwatchtips.streamtextreplacer.commands.impl.ForceRefreshCommand;
import com.overwatchtips.streamtextreplacer.commands.impl.PluginsCommand;
import com.overwatchtips.streamtextreplacer.commands.impl.StopCommand;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CommandManager {

    private final StreamTextReplacer main;
    private final Map<String, ConsoleCommand> commandMap = new HashMap<>();
    private final Scanner scanner = new Scanner(System.in);
    public CommandManager(StreamTextReplacer main) {
        this.main = main;
        loadDefaultCommands();
    }

    public boolean isCommandRegistered(String command) {
        return commandMap.containsKey(command);
    }

    public void registerCommand(String commandName, ConsoleCommand command) {
        commandMap.put(commandName, command);
    }

    private void loadDefaultCommands() {
        commandMap.put("stop", new StopCommand(main));
        commandMap.put("plugins", new PluginsCommand(main));
        commandMap.put("forcerefresh", new ForceRefreshCommand(main));
    }

    public void scanConsole() {
        if (!scanner.hasNextLine()) {
            return;
        }

        String userInput = scanner.nextLine();
        if (userInput.isEmpty()) {
            return;
        }

        String[] split = userInput.split("[ ]+");
        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].trim();
        }

        String commandName = split[0].trim();
        String[] args = Arrays.copyOfRange(split, 1, split.length);

        ConsoleCommand consoleCommand = commandMap.get(commandName);
        if (consoleCommand != null) {
            consoleCommand.execute(args);
        }else{
            StreamTextReplacer.getLogger().info("Unknown command.");
        }
    }
}
