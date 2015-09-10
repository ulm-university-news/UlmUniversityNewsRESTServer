package ulm.university.news.data;

import ulm.university.news.util.Platform;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

/**
 * This class represents an user of the application. The class contains information about the user which is relevant
 * for certain functionalities within the application.
 *
 * @author Matthias Mak
 * @author Philipp Speidel
 */
public class User {

    /** The id of the user. */
    private int id;
    /** The username of the user. The username is used to make users identifiable in a Group.*/
    private String name;
    /** The access token for the user. The token of a user is unique in the whole system and unambiguously identifies
     * the user. */
    private String serverAccessToken;
    /** The push token is used to identify the user, or rather his device, in the notification service of the
     * corresponding platform. It is required for sending push notifications to the user's device.*/
    private String pushAccessToken;
    /** The platform indicates which operating system runs on the user's device. This information is required to use
     * the correct push notification service for sending the notifications to the device. */
    private Platform platform;

    /**
     * Create an instance of User.
     */
    public User(){

    }

    /**
     * Create an instance of User.
     * @param name The username of the user.
     * @param serverAccessToken The access token which is assigned to this user.
     * @param pushAccessToken The push access token which identifies the user in the push notification service.
     * @param platform The platform of the user's device.
     */
    public User(String name, String serverAccessToken, String pushAccessToken, Platform platform){
        this.name = name;
        this.serverAccessToken = serverAccessToken;
        this.pushAccessToken = pushAccessToken;
        this.platform = platform;
    }

    /**
     * Create an instance of User.
     * @param id The id of the user.
     * @param name The username of the user.
     * @param serverAccessToken The access token which is assigned to this user.
     * @param pushAccessToken The push access token which identifies the user in the push notification service.
     * @param platform The platform of the user's device.
     */
    public User(int id, String name, String serverAccessToken, String pushAccessToken, Platform platform){
        this.id = id;
        this.name = name;
        this.serverAccessToken = serverAccessToken;
        this.pushAccessToken = pushAccessToken;
        this.platform = platform;
    }

    /**
     * Get the id of the user.
     * @return The id of the user.
     */
    public int getId() {
        return id;
    }

    /**
     * Set the id of the user.
     * @param id The id of the user.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Get the username of the user.
     * @return The username of the user.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the username of the user.
     * @param name The username of the user.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the access token which identifies the user on the server.
     * @return The access token.
     */
    public String getServerAccessToken() {
        return serverAccessToken;
    }

    /**
     * Set the access token which identifies the user on the server.
     * @param serverAccessToken The access token.
     */
    public void setServerAccessToken(String serverAccessToken) {
        this.serverAccessToken = serverAccessToken;
    }

    /**
     * Get the push access token which identifies the user in the push notification service.
     * @return The push access token.
     */
    public String getPushAccessToken() {
        return pushAccessToken;
    }

    /**
     * Set the push access token which identifies the user in the push notification service.
     * @param pushAccessToken The push access token.
     */
    public void setPushAccessToken(String pushAccessToken) {
        this.pushAccessToken = pushAccessToken;
    }

    /**
     * Get the platform of the user's device.
     * @return The platform of the user's device.
     */
    public Platform getPlatform() {
        return platform;
    }

    /**
     * Set the platform of the user's device.
     * @param platform The platform of the user's device.
     */
    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    /**
     * Creates an random access token for this user and stores it in the user instance. The access token is then an
     * unique identifier for the user in the system.
     */
    public void createUserToken(){
        try {
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "SUN");
            byte[] startBytes = new byte[128];
            sr.nextBytes(startBytes);
            byte[] paddingBytes = new byte[128];
            sr.nextBytes(paddingBytes);

            MessageDigest sha224 = MessageDigest.getInstance("SHA-224");
            byte[] randomBytes = new byte[256];
            System.arraycopy(startBytes, 0, randomBytes, 0, startBytes.length);
            System.arraycopy(paddingBytes, 0, randomBytes, startBytes.length, paddingBytes.length);

            byte[] token = sha224.digest(randomBytes);

        } catch (NoSuchAlgorithmException e) {
            //TODO
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            //TODO
            e.printStackTrace();
        }
    }
}
