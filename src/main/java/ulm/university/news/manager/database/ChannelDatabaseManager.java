package ulm.university.news.manager.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ulm.university.news.data.Channel;
import ulm.university.news.data.Event;
import ulm.university.news.data.Lecture;
import ulm.university.news.data.Sports;
import ulm.university.news.util.Constants;
import ulm.university.news.util.exceptions.DatabaseException;

import java.sql.*;

import static ulm.university.news.util.Constants.LOG_SQL_EXCEPTION;

/**
 * TODO
 *
 * @author Matthias Mak
 * @author Philipp Speidel
 */
public class ChannelDatabaseManager extends DatabaseManager {

    /** The logger instance for ChannelDatabaseManager. */
    private static final Logger logger = LoggerFactory.getLogger(ChannelDatabaseManager.class);

    /**
     * Creates an instance of the ChannelDatabaseManager class.
     */
    public ChannelDatabaseManager() {
    }

    /**
     * Stores the new channel (and its subclass) in the database. Then the creator is added as responsible moderator
     * by storing the link between the channel id and the moderator id.
     *
     * @param channel The channel object which contains the channel data.
     * @param moderatorId The id of the moderator who wants to create the channel.
     * @throws DatabaseException If the data could not be stored in the database due to database failure.
     */
    public void storeChannel(Channel channel, int moderatorId) throws DatabaseException {
        logger.debug("Start with channel:{} and moderatorId:{}.", channel, moderatorId);
        Connection con = null;
        try {
            con = getDatabaseConnection();
            // Start transaction.
            con.setAutoCommit(false);

            String storeChannelQuery =
                    "INSERT INTO Channel (Name, Description, Type, Term, Locations, Contacts, CreationDate, " +
                            "ModificationDate, Dates) VALUES (?,?,?,?,?,?,?,?,?); ";

            PreparedStatement storeChannelStmt = con.prepareStatement(storeChannelQuery);
            storeChannelStmt.setString(1, channel.getName());
            storeChannelStmt.setString(2, channel.getDescription());
            storeChannelStmt.setInt(3, channel.getType().ordinal());
            storeChannelStmt.setString(4, channel.getTerm());
            storeChannelStmt.setString(5, channel.getLocations());
            storeChannelStmt.setString(6, channel.getContacts());
            storeChannelStmt.setTimestamp(7, Timestamp.from(channel.getCreationDate().toInstant()));
            storeChannelStmt.setTimestamp(8, Timestamp.from(channel.getModificationDate().toInstant()));
            storeChannelStmt.setString(9, channel.getDates());

            storeChannelStmt.execute();

            // Retrieve auto incremented id of the database record.
            String getIdQuery = "SELECT LAST_INSERT_ID();";

            Statement getIdStmt = con.createStatement();
            ResultSet getIdRs = getIdStmt.executeQuery(getIdQuery);
            if (getIdRs.next()) {
                channel.setId(getIdRs.getInt(1));
            }
            logger.info("Stored channel with id:{}.", channel.getId());

            // Store data of the channels subclass.
            switch (channel.getType()) {
                case LECTURE:
                    Lecture lecture = (Lecture) channel;
                    String storeLectureQuery =
                            "INSERT INTO Lecture (Faculty, StartDate, EndDate, Lecturer, Assistent, Channel_Id) " +
                                    "VALUES (?,?,?,?,?,?); ";

                    PreparedStatement storeLectureStmt = con.prepareStatement(storeLectureQuery);
                    storeLectureStmt.setInt(1, lecture.getFaculty().ordinal());
                    storeLectureStmt.setString(2, lecture.getStartDate());
                    storeLectureStmt.setString(3, lecture.getEndDate());
                    storeLectureStmt.setString(4, lecture.getLecturer());
                    storeLectureStmt.setString(5, lecture.getAssistant());
                    storeLectureStmt.setInt(6, channel.getId());

                    storeLectureStmt.execute();
                    // TODO What about the lecture id in the database? Completely redundant!?
                    logger.info("Stored lecture.");
                    break;
                case EVENT:
                    Event event = (Event) channel;
                    String storeEventQuery =
                            "INSERT INTO Event (Cost, Organizer, Channel_Id) " +
                                    "VALUES (?,?,?); ";

                    PreparedStatement storeEventStmt = con.prepareStatement(storeEventQuery);
                    storeEventStmt.setString(1, event.getCost());
                    storeEventStmt.setString(2, event.getOrganizer());
                    storeEventStmt.setInt(3, channel.getId());

                    storeEventStmt.execute();
                    // TODO What about the event id in the database? Completely redundant!?
                    logger.info("Stored event.");
                    break;
                case SPORTS:
                    Sports sports = (Sports) channel;
                    String storeSportQuery =
                            "INSERT INTO CollegeSports (Cost, NumberOfParticipants, Channel_Id) " +
                                    "VALUES (?,?,?); ";

                    PreparedStatement storeSportStmt = con.prepareStatement(storeSportQuery);
                    storeSportStmt.setString(1, sports.getCost());
                    storeSportStmt.setString(2, sports.getNumberOfParticipants());
                    storeSportStmt.setInt(3, channel.getId());

                    storeSportStmt.execute();
                    // TODO What about the event id in the database? Completely redundant!?
                    logger.info("Stored sports.");
                    break;
                default:
                    // There is no subclass for channel type OTHER and STUDENT_GROUP, so storing is already complete.
                    break;
            }

            // TODO Can the addModeratorToChannel() method be used here regarding the transaction and rollback?
            // Add moderator (creator of the channel) to responsible moderators.
            String addModeratorQuery =
                    "INSERT INTO ModeratorChannel (Moderator_Id, Channel_Id, Active) " +
                            "VALUES (?,?,?);";

            PreparedStatement addModeratorStmt = con.prepareStatement(addModeratorQuery);
            addModeratorStmt.setInt(1, moderatorId);
            addModeratorStmt.setInt(2, channel.getId());
            addModeratorStmt.setBoolean(3, true);  // It is an active moderator.

            addModeratorStmt.executeUpdate();
            logger.info("Added the moderator with id {} as responsible for the channel with id {}.", moderatorId,
                    channel.getId());

            // End transaction.
            con.commit();

            storeChannelStmt.close();
            getIdStmt.close();
            addModeratorStmt.close();
        } catch (SQLException e) {
            try {
                logger.warn("Need to rollback the transaction.");
                con.rollback();
            } catch (SQLException e1) {
                logger.warn("Rollback failed.");
                logger.error(Constants.LOG_SQL_EXCEPTION, e1.getSQLState(), e1.getErrorCode(), e1.getMessage());
            }
            // Throw back DatabaseException to the Controller.
            logger.error(LOG_SQL_EXCEPTION, e.getSQLState(), e.getErrorCode(), e.getMessage());
            throw new DatabaseException("Database failure.");
        } finally {
            returnConnection(con);
        }
        logger.debug("End.");
    }

    /**
     * Adds the moderator with the given id to the channel with the given id as responsible moderator.
     *
     * @param channelId The id of the channel to which the moderator should be added.
     * @param moderatorId The id of the moderator who should be added to the channel.
     * @throws DatabaseException If the data could not be stored in the database due to database failure.
     */
    public void addModeratorToChannel(int channelId, int moderatorId) throws DatabaseException {
        logger.debug("Start with channelId:{} and moderatorId:{}.", channelId, moderatorId);
        Connection con = null;
        try {
            con = getDatabaseConnection();

            // Add a moderator as responsible to a channel.
            String addModeratorQuery =
                    "INSERT INTO ModeratorChannel (Moderator_Id, Channel_Id, Active) " +
                            "VALUES (?,?,?);";

            PreparedStatement addModeratorStmt = con.prepareStatement(addModeratorQuery);
            addModeratorStmt.setInt(1, moderatorId);
            addModeratorStmt.setInt(2, channelId);
            addModeratorStmt.setBoolean(3, true);  // It is an active moderator.

            addModeratorStmt.executeUpdate();
            logger.info("Added the moderator with id {} as responsible for the channel with id {}.", moderatorId,
                    channelId);

            addModeratorStmt.close();
        } catch (SQLException e) {
            logger.error(Constants.LOG_SQL_EXCEPTION, e.getSQLState(), e.getErrorCode(), e.getMessage());
            // Throw back DatabaseException to the Controller.
            throw new DatabaseException("Database failure.");
        }
        finally {
            returnConnection(con);
        }
        logger.debug("End.");
    }

}