package org.github.stevemorse.disnlpbot.bot;

import java.io.Serializable;
import java.time.Instant;
/**
 * A Post object contains the contents of as well as the author(usernane), user id, channel id and the
 * timestamp of a discord message.  It is Serializable and Comparable on the java.time.Instant timestamp.
 * @author Steve Morse
 * @version 1.0
 */
public class Post implements Serializable, Comparable<Object> {
	
	/**
	 * serialization Long
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * String used for the author of a post
	 */
	private String userName = "";
	/**
	 * The Discord userId of a post author as a Long
	 */
	private Long userId = null;
	/**
	 * Long of the Discord channelId
	 */
	private Long channelId = null;
	/**
	 * Instant of the posts time of posting 
	 */
	private Instant timeStamp = null;
	/**
	 * String of the message content of a post
	 */
	private	String content = "";
	/**
	 * default constructor
	 */
	public Post() {	}
	/**
	 * Paramerterized constructor
	 * @param userName Sting
	 * @param userId Long
	 * @param channelId Long
	 * @param timeStamp Instant
	 * @param content String
	 */
	public Post(String userName, Long userId, Long channelId, Instant timeStamp, String content) {
		super();
		this.setUserName(userName);
		this.setUserId(userId);
		this.setChannelId(channelId); 
		this.setTimeStamp(timeStamp);
		this.setContent(content);
	}
	/**
	 * The method to implement comparable on the timestamp Instant
	 */
	@Override
	public int compareTo(Object o) {
		return this.getTimeStamp().compareTo(((Post) o).getTimeStamp());
	}
	/**
	 * The toString method for the post object
	 */
    @Override
	public String toString() {
		return "POST: \nby: " + this.getUserName() + "\nid: " + this.getUserId() + "\nchannel id: " + this.getChannelId()
				+ "\nat: " + this.getTimeStamp() + "\ncontents: " + this.getContent() + "\n\n";
	}
    /**
     * getter
     * @return userName String
     */
	public String getUserName() {
		return this.userName;
	}
	/**
	 * setter
	 * @param userName String
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}
	/**
	 * getter
	 * @return userId Long
	 */
	public Long getUserId() {
		return this.userId;
	}
	/**
	 * setter
	 * @param userId Long
	 */
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	/**
	 * getter
	 * @return channelId Long
	 */
	public Long getChannelId() {
		return this.channelId;
	}
	/**
	 * setter
	 * @param channelId Long
	 */
	public void setChannelId(Long channelId) {
		this.channelId = channelId;
	}
	/**
	 * getter
	 * @return timeStamp Instant
	 */
	public Instant getTimeStamp() {
		return this.timeStamp;
	}
	/**
	 * setter
	 * @param timeStamp Instant
	 */
	public void setTimeStamp(Instant timeStamp) {
		this.timeStamp = timeStamp;
	}
	/**
	 * getter
	 * @return content String
	 */
	public String getContent() {
		return this.content;
	}
	/**
	 * setter
	 * @param content String
	 */
	public void setContent(String content) {
		this.content = content;
	}
}//class
