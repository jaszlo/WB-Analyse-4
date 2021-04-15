package app.db;

import app.data.InteractionRequest;
import app.graph.Graph;
import app.http.logger.Logger;
import org.mariadb.jdbc.Driver;

import java.sql.*;
import java.util.*;

import static app.analysis.Centrality.flowDistance;

/**
 * A class to interact with the database and add/update/delete data or tables of the database.
 */

public class Database {
    private final String url;
    private final String user;
    private final String password;
    private final String productionTableMeetings;
    private final String productionTablePersons;
    // Above String won't change
    private String workingTableMeetings;
    private String workingTablePersons;
    //weights for flow distance
    private final double talkWeight;
    private final double meetingWeight;
    private final double documentWeight;

    static {
        try {
            DriverManager.registerDriver(new Driver());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes the given Database for the interaction data for a given URL.
     *
     * A password and username are required as well as the name of the table that will be used to store the interactions.
     * For calculating the flow distance talk-, meeting and documentWeight is needed.
     * Those information are stored in a Properties object.
     * @param prop The properties object that contains initialization information for the database
     */
    public Database(Properties prop) {
        if(!prop.getProperty("db_production_table_meetings").matches("a4_[a-zA-Z]+")
                || !prop.getProperty("db_production_table_persons").matches("a4_[a-zA-Z]+")){
            throw new IllegalArgumentException("Production tables may only contain letters");
        }

        this.url = prop.getProperty("db_url");
        this.user = prop.getProperty("db_user");
        this.password = prop.getProperty("db_pass");
        this.productionTableMeetings = prop.getProperty("db_production_table_meetings");
        this.productionTablePersons = prop.getProperty("db_production_table_persons");
        this.talkWeight = Double.parseDouble(prop.getProperty("talk_weight"));
        this.meetingWeight = Double.parseDouble(prop.getProperty("meeting_weight"));
        this.documentWeight = Double.parseDouble(prop.getProperty("document_weight"));

        if(!this.createTable(productionTableMeetings, productionTablePersons)) throw new IllegalArgumentException("Could not create production table");
        workingTableMeetings = productionTableMeetings;
        workingTablePersons = productionTablePersons;
    }

    /**
     * Initializes configuration for testing by switching to a test environment in the database.
     *
     * Clears all data from the test tables in the beginning.
     * @return Return true if the initialization of the test tables were successful and testing can begin
     */
    public boolean initTest() {
        if (!workingTableMeetings.equals(productionTableMeetings)) {
            this.deInitTest();
        }

        // Create name of a new table that does not exists
        String tableMeetings;
        do {
            tableMeetings = "a4_test_";
            tableMeetings += new Random().ints('A', 'Z' + 1)
                    .limit(10)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();
        } while(this.exists(tableMeetings));
        // Create name of a new table that does not exists
        String tablePersons;
        do {
            tablePersons = "a4_test_";
            tablePersons += new Random().ints('A', 'Z' + 1)
                    .limit(10)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();
        } while(this.exists(tablePersons));

        if (createTable(tableMeetings, tablePersons)) {
            workingTableMeetings = tableMeetings;
            workingTablePersons = tablePersons;
            return true;
        }
        return false;
    }

    /**
     * De-initializes the test environment and switches to production mode by switching the active tables to
     * the production tables. Deletes the old test tables
     * @return Return true of the database is now back to work on the production table and the test tables were deleted successfully.
     */
    public boolean deInitTest() {
        if (workingTableMeetings.equals(productionTableMeetings)) return true;

        // Delete newly created test table
        try (Connection conn = DriverManager.getConnection(url, user, password); Statement stmtOne = conn.createStatement(); Statement stmtTwo = conn.createStatement()) {
            String queryPersons = String.format("DROP TABLE IF EXISTS VirtuHoS_4.%s;", workingTablePersons);
            stmtTwo.executeUpdate(queryPersons);
            String queryMeetings = String.format("DROP TABLE IF EXISTS VirtuHoS_4.%s;", workingTableMeetings);
            stmtOne.executeUpdate(queryMeetings);
        } catch (SQLException e) {
            Logger.log(e);
            return false;
        }

        workingTableMeetings = productionTableMeetings;
        workingTablePersons = productionTablePersons;
        return true;
    }

    /**
     * Creates a new pair of tables (one meetinga and one person table) with their properties
     * @param tableMeetings table name for the meeting table
     * @param tablePersons table name for the person table
     * @return returns whether the method was successful or not
     */
    private boolean createTable(String tableMeetings, String tablePersons) {
        try (Connection conn = DriverManager.getConnection(url, user, password); Statement stmt = conn.createStatement()) {
            String queryMeetings = String.format(
                    """
                    CREATE TABLE IF NOT EXISTS VirtuHoS_4.%s (
                        meeting_ID bigint auto_increment,
                        datetime bigint null,
                        duration int null,
                        distinctPersons int not null,
                        constraint %s_pk
                            primary key (meeting_ID)
                    );
                    """, tableMeetings, tableMeetings);
            stmt.executeUpdate(queryMeetings);
            String queryPersons = String.format(
                    """
                    CREATE TABLE IF NOT EXISTS VirtuHoS_4.%s (
                        meeting_ID bigint not null,
                        name varchar(2048) not null,
                        constraint %s_%s_meeting_ID_fk
                            foreign key (meeting_ID) references %s (meeting_ID)
                                on update cascade on delete cascade  
                    );
                    """, tablePersons, tablePersons, tableMeetings, tableMeetings);
            stmt.executeUpdate(queryPersons);
        } catch (SQLException e) {
            Logger.log(e);
            return false;
        }
        return true;
    }

    /**
     * Check whether a given table already exists in the database
     * @param table Name of the table to check for existence
     * @return returns a boolean whether the table was found or not
     */
    private boolean exists(String table) {
        String query = String.format(
                """
                SELECT EXISTS
                (SELECT *
                FROM INFORMATION_SCHEMA.TABLES
                WHERE TABLE_SCHEMA = 'VirtuHoS_4' AND  TABLE_NAME = '%s'
                );
                """, table);

        try (Connection conn = DriverManager.getConnection(url, user, password); Statement stmt = conn.createStatement()) {
            ResultSet set = stmt.executeQuery(query);
            if (set.next()) return set.getInt(1) != 0;

        } catch (SQLException e) {
            Logger.log(e);
        }
        return false;
    }

    /**
     * Clears the current working tables.
     */
    public void clear() {
        try (Connection conn = DriverManager.getConnection(url, user, password); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM VirtuHoS_4." + workingTableMeetings);
            stmt.executeUpdate("DELETE FROM VirtuHoS_4." + workingTablePersons);
        } catch (SQLException e) {
            Logger.log(e);
        }
    }

    /**
     * Checks whether the given interaction exists in current working tables. Not dependent of the meeting ID.
     *
     * @param name1 the first interaction partner
     * @param name2 the second interaction partner
     * @param datetime the datetime of the interaction
     * @param duration the duration of the interaction
     * @param distinctPersons number of distinct persons who were part of the same meeting
     * @return returns a boolean whether the entry in the database with given parameters was found or not
     */

    public boolean contains(String name1, String name2, long datetime, int duration, int distinctPersons){
        String query =  String.format(
                "SELECT EXISTS " +
                "(SELECT * FROM %s m JOIN %s p1 USING(meeting_ID) JOIN %s p2 USING(meeting_ID) " +
                        "WHERE p1.name = ? and p2.name = ? and m.datetime = ? and m.duration = ? and m.distinctPersons = ?)"
        , workingTableMeetings, workingTablePersons, workingTablePersons);
        try (Connection conn = DriverManager.getConnection(url, user, password); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name1);
            stmt.setString(2, name2);
            stmt.setLong(3, datetime);
            stmt.setInt(4, duration);
            stmt.setInt(5, distinctPersons);
            ResultSet set = stmt.executeQuery();
            if (set.next()) return set.getInt(1) != 0;
        } catch (SQLException e) {
            Logger.log(e);
        }
        return false;
    }

    /**
     * Check if the current working tables are empty.
     *
     * @return True of no tuples are stored in the current working tables
     */
    public boolean isEmpty(){
        String queryOne = "SELECT NOT EXISTS (SELECT * FROM " + workingTableMeetings + ");";
        String queryTwo = "SELECT NOT EXISTS (SELECT * FROM " + workingTablePersons + ");";
        try (Connection conn = DriverManager.getConnection(url, user, password); PreparedStatement stmtOne = conn.prepareStatement(queryOne); PreparedStatement stmtTwo = conn.prepareStatement(queryTwo)) {
            ResultSet set = stmtOne.executeQuery();
            if (set.next()){
                if(set.getInt(1) == 0) return false;
            }
            set = stmtTwo.executeQuery();
            if (set.next()) return set.getInt(1) != 0;
        } catch (SQLException e) {
            Logger.log(e);
        }
        return false;
    }

    /**
     * Remove all tuples in the current working table for Persons with the given name.
     *
     * The distinctPerson attribute in the working meeting tale is updated and after that
     * non valid persons and meetings are deleted
     * (e.g if an url is deleted the corresponding meeting and person are deleted as well)
     * @param name The name of the person which interaction will be deleted
     * @return The number of meetings which were updated and/or deleted
     */
    public int removePerson(String name) {
        int deletedMeetingsForPerson = 0;
        String updateDistinctPersons = String.format(
                """
                UPDATE %s m 
                JOIN %s p USING(meeting_ID)
                SET m.distinctPersons = m.distinctPersons - 1
                WHERE p.name = ?
                """, workingTableMeetings, workingTablePersons);
        String deletePerson = "DELETE FROM " + workingTablePersons + " Where name = ?";
        String deleteZombiePersons = String.format(
                """
                DELETE p FROM %s p
                WHERE EXISTS 
                    (SELECT *
                    FROM %s m
                    WHERE m.meeting_ID = p.meeting_ID 
                    AND (m.distinctPersons = 1) OR (m.distinctPersons = -1) );
                """, workingTablePersons, workingTableMeetings);
        String deleteZombieMeetings = String.format(
                """
                DELETE FROM %s
                WHERE (distinctPersons = 1) OR (distinctPersons = -1)
                """, workingTableMeetings);
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmtOne = conn.prepareStatement(updateDistinctPersons);
             PreparedStatement stmtTwo = conn.prepareStatement(deletePerson);
             PreparedStatement stmtThree = conn.prepareStatement(deleteZombiePersons);
             PreparedStatement stmtFour = conn.prepareStatement(deleteZombieMeetings))
        {
            stmtOne.setString(1, name);
            stmtTwo.setString(1, name);
            stmtOne.executeUpdate();
            deletedMeetingsForPerson = stmtTwo.executeUpdate();
            stmtThree.executeUpdate();
            stmtFour.executeUpdate();
        } catch (SQLException e) {
            Logger.log(e);
        }
        return deletedMeetingsForPerson;
    }

    /**
     * This methods returns all unique use IDs from the database.
     *
     * On error returns an empty list. Note that this will also happen if no interactions have ever been added.
     * @return A list of all UIDs
     */
    public List<String> getAllIds() {
        List<String> result = new ArrayList<>();
        String query = String.format("SELECT distinct name FROM %s ORDER BY name ", workingTablePersons);
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet set = stmt.executeQuery()
        ) {
            while (set.next()) {
                result.add(set.getString(1));
            }
        } catch(SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
        return result;
    }

    /**
     * Connects to the database and saves meta data of the given interactionRequest along with a unique meeting_ID in the meetings table.
     *
     * Connects to the database and saves meta data of the given interactionRequest
     * along with a unique meeting_ID in the meetings table.
     * Then this method adds the persons of the interactionRequest in the persons table
     * with their corresponding meeting_ID set in the meetings table.
     *
     * @param data contains datetime, duration, weight of the interaction depending on type (only relevant for generateFlowGraph)
     * @return returns the number of different subjects who were added to one meeting in the database
     */
    public int addMeeting(InteractionRequest data) {
        String[] distinctNames = Arrays.stream(data.names).distinct().toArray(String[]::new);
        if (distinctNames.length < 2) return 0;
        int personCounter = 0;
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            //Add meta data of interaction Request to meetings table
            String queryTableMeetings = "INSERT INTO VirtuHoS_4." + workingTableMeetings + " (datetime, duration, distinctPersons) VALUES (?, ?, ?)";
            PreparedStatement updateTableMeetings = conn.prepareStatement(queryTableMeetings, PreparedStatement.RETURN_GENERATED_KEYS);
            updateTableMeetings.setLong(1, data.datetime); // may be null ?
            updateTableMeetings.setInt(2, data.duration);
            if (data.document)
                updateTableMeetings.setInt(3, 0);
            else
                updateTableMeetings.setInt(3, distinctNames.length);
            updateTableMeetings.executeQuery();

            //Get latest meeting ID,
            ResultSet generatedKeys = updateTableMeetings.getGeneratedKeys();
            long latestMeetingID = 0;
            if (generatedKeys.next())
                latestMeetingID = generatedKeys.getLong(1);

            //Add persons/document of interaction Request to persons table
            String queryTablePersons = "INSERT INTO VirtuHoS_4." + workingTablePersons + " (meeting_ID, name) VALUES (?, ?)";
            PreparedStatement updateTablePersons = conn.prepareStatement(queryTablePersons);
            for (String name : distinctNames) {
                updateTablePersons.setLong(1, latestMeetingID); // meeting_id corresponding tuple in a4_meetings
                updateTablePersons.setString(2, name);
                updateTablePersons.addBatch();
                updateTablePersons.clearParameters();
            }
            personCounter = Arrays.stream(updateTablePersons.executeBatch()).sum();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return personCounter;
    }

    /**
     * Adds interactionRequests from the hall to the current working tables in the database.
     *
     * In case the document is set true, addInteractions splits the names of the meeting up and adds for each name a meeting with the given document to the db.
     * @param data data contains datetime, duration, weight of the interaction depending on type (only relevant for generateFlowGraph)
     * @return returns the number of different subjects who were added to the database, in case document is true, it represents the number of meetings that were added
     */
    public int addInteractions(InteractionRequest data) {
        if (data.document) {
            String[] distinctNames = Arrays.stream(data.names).distinct().toArray(String[]::new);
            for (int i = 0; i < distinctNames.length - 1; i++) {
                InteractionRequest temp = new InteractionRequest(new String[]{distinctNames[i], distinctNames[distinctNames.length - 1]}, data.datetime, data.duration, true);
                this.addMeeting(temp);
            }
            return distinctNames.length - 1;
        } else {
            return this.addMeeting(data);
        }
    }

    /**
     * Generates a graph of the data contained in the working tables of the database (mainly for editor-group)
     *
     * Connects to database and pulls all valid (filtered) meetings and their corresponding persons and
     * adds the persons who were interacting to the Graph and sets the corresponding weight
     *
     * @param mode specifies how to calculate the edgeWeight for the graph
     * @param filter may contain one or more filter the data in database for values such as names, datetime, ...
     * @return the generated Graph of the database
     */
    public Graph generateGraph(GraphOptions mode, SortedMap<String, String> filter) {
        if (mode == GraphOptions.FLOW_DISTANCE) return this.generateFlowGraph(filter);
        Graph g = new Graph();
        String whereClause = GraphFilters.filterString(filter);
        int setCount = 1;
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            PreparedStatement stmt = conn.prepareStatement(mode.query(workingTableMeetings, workingTablePersons, whereClause));
            for (SortedMap.Entry<String, String> entry : filter.entrySet()) {
                if (entry.getKey().contains("NAME")) {
                    stmt.setString(setCount, entry.getValue());
                    setCount++;
                    stmt.setString(setCount, entry.getValue());
                } else if (entry.getKey().contains("DATETIME")) {
                    stmt.setLong(setCount, Long.parseLong(entry.getValue()));
                }
                setCount++;
            }
            ResultSet set = stmt.executeQuery();
            while (set.next()) {
                String name1 = set.getString(1);
                String name2 = set.getString(2);
                double edgeWeight = set.getDouble(3);
                edgeWeight = set.wasNull() ? Double.POSITIVE_INFINITY : edgeWeight;
                if (g.getVertex(name1) == null) {
                    g.addVertex(name1);
                }
                if (g.getVertex(name2) == null) {
                    g.addVertex(name2);
                }
                g.setWeight(name1, name2, edgeWeight);
            }
            set.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (!g.getVertices().isEmpty())
            g.normalizeEdgeWeights();
        return g;
    }

    /**
     * Overloaded generateGraph method without the need to specify filters.
     *
     * @param mode specifies how to calculate the edgeWeight for the graph
     * @return the generated graph of the database
     */
    public Graph generateGraph(GraphOptions mode) {
        SortedMap<String, String> sortedEmptyMap = Collections.emptySortedMap();
        return this.generateGraph(mode, sortedEmptyMap);
    }

    /**
     * Generates graph of the interaction data in database according to the flow distance, can contain documents.
     *
     * Uses the distinctPersons column of the meetings table to determine the distance between two subjects
     * @param filter may contain one or more filter the data in database for values such as names, datetime, ...
     * @return the generated flow distance graph of the database
     */
    public Graph generateFlowGraph(SortedMap<String, String> filter) {
        Graph g = new Graph();
        String whereClause = GraphFilters.filterString(filter);
        String query = String.format(
                """
                SELECT p1.name, p2.name, min(distinctPersons)
                FROM %s m JOIN %s p1 USING(meeting_ID) JOIN %s p2 USING(meeting_ID)
                Where p1.name < p2.name %s
                GROUP BY p1.name, p2.name;
                """, workingTableMeetings, workingTablePersons, workingTablePersons, whereClause);

        int setCount = 1;
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            PreparedStatement stmt = conn.prepareStatement(query);
            for (SortedMap.Entry<String, String> entry : filter.entrySet()) {
                if (entry.getKey().contains("NAME")) {
                    stmt.setString(setCount, entry.getValue());
                    setCount++;
                    stmt.setString(setCount, entry.getValue());
                } else if (entry.getKey().contains("DATETIME")) {
                    stmt.setLong(setCount, Long.parseLong(entry.getValue()));
                }
                setCount++;
            }
            ResultSet set = stmt.executeQuery();
            while (set.next()) {
                String name1 = set.getString(1);
                String name2 = set.getString(2);
                int distinctPersons = set.getInt(3);
                double edgeWeight;
                if (distinctPersons == 0)
                    edgeWeight = documentWeight;
                else if (distinctPersons == 2)
                    edgeWeight = talkWeight;
                else // If distinct Persons > 2 ( 1 and < 0 can't happen because of addInteractions)
                    edgeWeight = meetingWeight;

                if (g.getVertex(name1) == null)
                    g.addVertex(name1);
                if (g.getVertex(name2) == null)
                    g.addVertex(name2);

                g.setWeight(name1, name2, edgeWeight);
            }
            set.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flowDistance(g);
    }

    /**
     * Overloaded generateFlowGraph method without the need to specify filters.
     *
     * @return the generated flow distance graph of the database
     */
    public Graph generateFlowGraph() {
        SortedMap<String, String> sortedEmptyMap = Collections.emptySortedMap();
        return this.generateFlowGraph(sortedEmptyMap);
    }

}
