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

package com.overwatchtips.streamtextreplacer.commands.impl;

import com.overwatchtips.streamtextreplacer.StreamTextReplacer;
import com.overwatchtips.streamtextreplacer.api.commands.ConsoleCommand;

public class StopCommand implements ConsoleCommand {

    private final StreamTextReplacer main;
    public StopCommand(StreamTextReplacer main) {
        this.main = main;
    }

    @Override
    public void execute(String[] args) {
        main.shutdown();
    }
}
