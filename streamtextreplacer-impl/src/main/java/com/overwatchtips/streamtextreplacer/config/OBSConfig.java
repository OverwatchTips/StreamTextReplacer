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

package com.overwatchtips.streamtextreplacer.config;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.overwatchtips.streamtextreplacer.records.OBSSettings;
import com.overwatchtips.streamtextreplacer.records.OBSSource;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OBSConfig {

    private final Logger logger;
    private final File file;
    private final OBSSettings obsSettings;

    public OBSConfig(Logger logger) throws IOException {
        this.logger = logger;
        this.file = new File("./obs.json");
        if (!file.exists()) {
            logger.info("obs.json file does not exist, creating.");
            createFile();
        }

        ObjectMapper mapper = new ObjectMapper();
        obsSettings = mapper.readValue(file, OBSSettings.class);
    }

    private void writeToFile(OBSSettings obsSettings) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        writer.writeValue(file, obsSettings);
    }

    private void createFile() throws IOException {
        file.createNewFile();

        OBSSource customSourceDefault = new OBSSource("test", "Test: %overtrack_rating%");
        OBSSettings obsSettingsDefault = new OBSSettings("ws://localhost:4444","password", false,
                Stream.of(customSourceDefault).collect(Collectors.toSet()));

        writeToFile(obsSettingsDefault);
    }

    public OBSSettings getObsSettings() {
        return obsSettings;
    }
}
