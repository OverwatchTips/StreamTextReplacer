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

package com.overwatchtips.overtrackplugin;

import java.io.*;

public class PeakRatingFile {

    private final OverTrackPlugin plugin;
    private final File file;
    private int peakRating;

    public PeakRatingFile(OverTrackPlugin plugin) throws IOException {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "peak.sr");

        if (!file.exists()) {
            createFile();
        }

        loadFile();
    }

    public int getPeakRating() {
        return peakRating;
    }

    public void setPeakRating(int peakRating) {
        this.peakRating = peakRating;

        try {
            writeToFile(peakRating + "");
        } catch (IOException e) {
            plugin.getLogger().warn("Unable to save peak.sr file: {}", e);
        }
    }

    private void writeToFile(String rating) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(rating);
        writer.close();
    }

    private void createFile() throws IOException {
        plugin.getLogger().info("peak.sr file does not exist, creating.");
        file.createNewFile();
        writeToFile("0");
    }

    private void loadFile() throws IOException {
        plugin.getLogger().info("Loading peak.sr file.");
        this.peakRating = Integer.parseInt(new BufferedReader(new FileReader(file)).readLine());
    }
}
