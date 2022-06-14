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

import java.util.List;
import java.util.Set;

public record OverTrackData(Set<OverwatchMatch> games,
                            @JsonProperty("last_evaluated_key") String lastEvaluatedKey,
                            List<String> seasons) {

}
