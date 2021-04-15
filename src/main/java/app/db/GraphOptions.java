package app.db;

/**
 * An enum class to select the different modes of how the edge weight of the generated graph should be
 */

public enum GraphOptions {
    INTERACTION_SUM,
    DURATION_SUM,
    INTERACTION_TIMES_DURATION,

    INVERTED_INTERACTION_SUM,
    INVERTED_DURATION_SUM,
    INVERTED_INTERACTION_TIMES_DURATION,

    FLOW_DISTANCE;

    /**
     * Generates the query for the retrieval of the data from the database
     *
     * @param tableMeetings the meeting table where meeting data should come from
     * @param tablePersons the person table where person data should come from
     * @param whereClause the where clause that might filter the data
     * @return the complete SQL query string which can be executed by the database
     */
    public String query(String tableMeetings, String tablePersons, String whereClause) {
        return switch (this) {
            case INTERACTION_SUM -> String.format(
                    """
                    SELECT p1.name, p2.name, count(*)
                    FROM %s m JOIN %s p1 USING(meeting_ID) JOIN %s p2 USING(meeting_ID)
                    Where p1.name < p2.name %s
                    GROUP BY p1.name, p2.name;
                    """, tableMeetings, tablePersons, tablePersons, whereClause);
            case DURATION_SUM -> String.format(
                    """
                    SELECT p1.name, p2.name, sum(duration)
                    FROM %s m JOIN %s p1 USING(meeting_ID) JOIN %s p2 USING(meeting_ID)
                    Where p1.name < p2.name %s
                    GROUP BY p1.name, p2.name;
                    """, tableMeetings, tablePersons, tablePersons, whereClause);
            case INTERACTION_TIMES_DURATION -> String.format(
                    """
                    SELECT p1.name, p2.name, count(*) * sum(duration)
                    FROM %s m JOIN %s p1 USING(meeting_ID) JOIN %s p2 USING(meeting_ID)
                    Where p1.name < p2.name %s
                    GROUP BY p1.name, p2.name;
                    """, tableMeetings, tablePersons, tablePersons, whereClause);
            case INVERTED_INTERACTION_SUM -> String.format(
                    """
                    SELECT p1.name, p2.name, CAST(1 AS DOUBLE) / count(*)
                    FROM %s m JOIN %s p1 USING(meeting_ID) JOIN %s p2 USING(meeting_ID)
                    Where p1.name < p2.name %s
                    GROUP BY p1.name, p2.name;
                    """, tableMeetings, tablePersons, tablePersons, whereClause);
            case INVERTED_DURATION_SUM -> String.format(
                    """
                    SELECT p1.name, p2.name, CAST(1 AS DOUBLE) / sum(duration)
                    FROM %s m JOIN %s p1 USING(meeting_ID) JOIN %s p2 USING(meeting_ID)
                    Where p1.name < p2.name %s
                    GROUP BY p1.name, p2.name;
                    """, tableMeetings, tablePersons, tablePersons, whereClause);
            case INVERTED_INTERACTION_TIMES_DURATION -> String.format(
                    """
                    SELECT p1.name, p2.name, CAST(1 AS DOUBLE) / count(*) * sum(duration)
                    FROM %s m JOIN %s p1 USING(meeting_ID) JOIN %s p2 USING(meeting_ID)
                    Where p1.name < p2.name %s
                    GROUP BY p1.name, p2.name;
                    """, tableMeetings, tablePersons, tablePersons, whereClause);
            default -> "";
        };
    }
}
