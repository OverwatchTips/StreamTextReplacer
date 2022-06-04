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

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.overwatchtips.streamtextreplacer.api.ReplacerPlugin;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class OverTrackPlugin extends ReplacerPlugin {

    private PluginConfig pluginConfig;
    private PeakRatingFile peakRatingFile;

    public OverTrackPlugin(Logger logger) {
        super(logger);
    }

    @Override
    public String getName() {
        return "OverTrack";
    }

    @Override
    public String getAuthor() {
        return "OverwatchTips.com (l3st4t)";
    }

    @Override
    public String getIdentifier() {
        return "overtrack";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean onEnable() {
        if (!getDataFolder().exists() && !getDataFolder().isDirectory()) {
            getDataFolder().mkdir();
        }

        File configFile = new File(getDataFolder(), "config.json");

        try {
            if (!configFile.exists()) {
                createFile(configFile);
            }

            ObjectMapper mapper = new ObjectMapper();
            this.pluginConfig = mapper.readValue(configFile, PluginConfig.class);
            this.peakRatingFile = new PeakRatingFile(this);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public void onDisable() {}

    private void createFile(File file) throws IOException {
        file.createNewFile();

        PluginConfig pluginConfig = new PluginConfig("null", 30);
        writeToFile(file, pluginConfig);
    }

    private void writeToFile(File file, PluginConfig pluginConfig) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        writer.writeValue(file, pluginConfig);
    }

    @Override
    public long getRefreshTime() {
        return pluginConfig.refreshInterval();
    }

    @Override
    public String onRequest(String params) {
        String rating = getUrlAsString("https://api2.overtrack.gg/overwatch/query/" + pluginConfig.shareToken() + "/sr");
        if (rating != null && !rating.isEmpty() && isInteger(rating)) {
            int ratingInt = Integer.parseInt(rating);
            if (ratingInt > peakRatingFile.getPeakRating()) {
                peakRatingFile.setPeakRating(ratingInt);
            }
        }

        String record = getUrlAsString("https://api2.overtrack.gg/overwatch/session/" + pluginConfig.shareToken());
        String lastMatch = getUrlAsString("https://api2.overtrack.gg/overwatch/query/" + pluginConfig.shareToken() + "/last_match");
        String peakRating = String.valueOf(peakRatingFile.getPeakRating());

        return switch (params.toLowerCase()) {
            case "rating" -> rating;
            case "record" -> record;
            case "last_match" -> lastMatch;
            case "peak_rating" -> peakRating;
            default -> null;
        };
    }

    private String getUrlAsString(String url) {
        try {
            URL urlObj = new URL(url);
            URLConnection con = urlObj.openConnection();

            con.setDoOutput(true);
            con.connect();

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

            StringBuilder response = new StringBuilder();
            String inputLine;

            String newLine = System.getProperty("line.separator");
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine).append(newLine);
            }

            in.close();

            return response.toString().split("\\|")[0].trim();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Extracted from: https://stackoverflow.com/questions/5439529/determine-if-a-string-is-an-integer-in-java
    public static boolean isInteger(String s) {
        return isInteger(s,10);
    }

    public static boolean isInteger(String s, int radix) {
        if(s.isEmpty()) return false;
        for(int i = 0; i < s.length(); i++) {
            if(i == 0 && s.charAt(i) == '-') {
                if(s.length() == 1) return false;
                else continue;
            }
            if(Character.digit(s.charAt(i),radix) < 0) return false;
        }
        return true;
    }
}
