package pl.edu.agh.gvc;

import com.google.common.collect.Sets;

import java.util.Set;

public class EdgeLabels {

    public static final String CONTAINS = "CONTAINS";

    public static final String LATEST_VERSION = "LATEST_VERSION";

    public static final String PREVIOUS_REVISION = "PREVIOUS_REVISION";

    public static final String PREVIOUS_VERSION = "PREVIOUS_VERSION";

    public static final Set<String> GVC_LABELS = Sets.newHashSet(new String[]{CONTAINS, LATEST_VERSION, PREVIOUS_REVISION, PREVIOUS_VERSION});
}
