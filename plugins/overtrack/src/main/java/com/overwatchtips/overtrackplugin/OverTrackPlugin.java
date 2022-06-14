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
import com.overwatchtips.overtrackplugin.enums.GameResult;
import com.overwatchtips.overtrackplugin.records.OverTrackData;
import com.overwatchtips.overtrackplugin.records.OverwatchMatch;
import com.overwatchtips.streamtextreplacer.api.ReplacerPlugin;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class OverTrackPlugin extends ReplacerPlugin {

    private PluginConfig pluginConfig;
    private TreeSet<OverwatchMatch> cachedData;

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

        PluginConfig pluginConfig = new PluginConfig("null", 30, 172800, 5400);
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        writer.writeValue(file, pluginConfig);
    }

    @Override
    public long getRefreshTime() {
        return pluginConfig.refreshInterval();
    }

    @Override
    public String onRequest(String params, boolean sameCycle) {
        ObjectMapper mapper = new ObjectMapper();

        TreeSet<OverwatchMatch> validMatches = new TreeSet<>(Collections.reverseOrder());
        OverTrackData data;
        if (sameCycle) {
            validMatches = cachedData;
        }else {
            try {
                data = mapper.readValue(new URL("https://api2.overtrack.gg/overwatch/games/" + pluginConfig.shareToken()), OverTrackData.class);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            Set<OverwatchMatch> matches = data.games();
            getLogger().info(matches.size() + " matches before removeIf");
            matches.removeIf(match -> Instant.now().getEpochSecond() - match.time() > pluginConfig.maxLookupPeriod());
            getLogger().info(matches.size() + " matches after removeIf");

            long previousTimestamp = 0;
            Iterator<OverwatchMatch> iterator = matches.iterator();
            while (iterator.hasNext()) {
                OverwatchMatch match = iterator.next();
                if (previousTimestamp - match.time() < pluginConfig.timeBetweenSessions()) {
                    validMatches.add(match);
                }
            }
        }

        cachedData = validMatches;

        switch (params.toLowerCase()) {
            case "rating": {
                if (validMatches.isEmpty()) {
                    return "Unknown";
                }

                OverwatchMatch first = validMatches.first();
                long currentSr = first.endSr() == 0 ? first.startSr() : first.endSr();
                return String.valueOf(currentSr);
            }
            case "wins": {
                return getResults(validMatches, GameResult.WIN);
            }
            case "losses": {
                return getResults(validMatches, GameResult.LOSS);
            }
            case "draws": {
                return getResults(validMatches, GameResult.DRAW);
            }
            case "last_match": {
                if (validMatches.isEmpty()) {
                    return "Unknown";
                }

                OverwatchMatch first = validMatches.first();
                return first.result().name() + " on " + first.map();
            }
            default: {
                return null;
            }
        }
    }

    private String getResults(Set<OverwatchMatch> matches, GameResult result) {
        return String.valueOf(matches.stream().filter(match -> match.result() == result).count());
    }
}
