/*
 * Copyright (C) 2014  Marcin Krupa
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.edu.agh.gvc;

import com.google.common.collect.Sets;

import java.util.Set;

public class EdgeLabels {

    public static final String CONTAINS = "CONTAINS";

    public static final String LATEST_VERSION = "LATEST_VERSION";

    public static final String PREVIOUS_REVISION = "PREVIOUS_REVISION";

    public static final String PREVIOUS_VERSION = "PREVIOUS_VERSION";

    public static final Set<String> GVC_LABELS = Sets.newHashSet(CONTAINS, LATEST_VERSION, PREVIOUS_REVISION, PREVIOUS_VERSION);
}
