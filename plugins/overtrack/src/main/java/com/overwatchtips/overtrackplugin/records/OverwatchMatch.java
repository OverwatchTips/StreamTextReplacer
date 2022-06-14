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

package com.overwatchtips.overtrackplugin.records;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.overwatchtips.overtrackplugin.enums.GameResult;

import java.util.Comparator;
import java.util.Objects;
import java.util.Set;

public record OverwatchMatch(@JsonProperty("custom_game") boolean customGame,
                             long duration,
                             @JsonProperty("end_sr") int endSr,
                             @JsonProperty("game_type") String gameType,
                             @JsonProperty("game_version") String gameVersion,
                             @JsonProperty("heroes_played") Set<Set<String>> heroesPlayed,
                             String key,
                             String map,
                             @JsonProperty("player_name") String playerName,
                             String rank,
                             GameResult result,
                             String role,
                             Set<Integer> score,
                             String season, @JsonProperty("season_index") int seasonIndex,
                             @JsonProperty("start_sr") int startSr,
                             long time,
                             String url,
                             @JsonProperty("user_id") long userId,
                             boolean viewable) implements Comparable<OverwatchMatch> {

    @Override
    public int compareTo(OverwatchMatch that) {
        return Objects.compare(this, that, Comparator.comparingLong(OverwatchMatch::time));
    }
}
