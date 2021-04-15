package app.db;

import java.util.Map;

/**
 * A class to filter the database before generating a graph of it
 */

public class GraphFilters {

    /**
     * This method generates a part of the SQL statement (where clause) that retrieves the data for the graph and adds (multiple) filter to it
     * @param filter Map of different filters (keys) and their corresponding value
     * @return returns a valid where clause for sql to filter the data of the database
     */
    public static String filterString(Map<String, String> filter) {
        StringBuilder whereClause = new StringBuilder();
        for (Map.Entry<String, String> entry : filter.entrySet()) {
            switch (entry.getKey()) {
                case "NAME":
                    whereClause.append(" AND (p1.name = ? or p2.name = ?)");
                    break;
                case "NOT_NAME":
                    whereClause.append(" AND (NOT p1.name = ? AND NOT p2.name = ?)");
                    break;
                case "MIN_DATETIME":
                    whereClause.append(" AND (m.datetime > ?)");
                    break;
                case "MAX_DATETIME":
                    whereClause.append(" AND (m.datetime < ?)");
                    break;
                default:
            }
        }
        return whereClause.toString();
    }
}
