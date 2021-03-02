package org.github.stevemorse.disnlpbot.bot;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.github.stevemorse.disnlpbot.bot.Post;
/**
 * The DisNLPBot strips all messages from all channels of a Discord guild to which it attached and stores them
 * to a file as serialized objects
 *
 * @author Steve Morse
 * @version 1.0
 */
public final class DisNLPBot {
	/**
	 * A String of the Discord token for the Bot 
	 */
	private String token = "";
	/**
	 * A String of the filename to make and take output and input to and from
	 */
	private String filename = "";
	/**
	 * A String of the name of the resource file
	 */
	private String resourceFileName = "";
	
	/**
	 * default constructor
	 */
	public DisNLPBot() {
		this.setResourceFileName("res.json");
		this.setToken(this.getResources(resourceFileName, "token"));
		this.setFilename(this.getResources(resourceFileName, "filename"));
		this.setResourceFileName(resourceFileName);
	}
	/**
	 * constructor
	 * @param token String
	 * @param filename String
	 */
	public DisNLPBot(String token, String filename) {
		this.setResourceFileName("res.json");
		this.setToken(token);
		this.setFilename(filename);
	}
	/**
	 * getter
	 * @return token String
	 */
	public String getToken() {
		return token;
	}
	/**
	 * setter
	 * @param token String
	 */
	public void setToken(String token) {
		this.token = token;
	}
	/**
	 * getter
	 * @return filename String
	 */
	public String getFilename() {
		return filename;
	}
	/**
	 * setter
	 * @param filename String
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}
	/**
	 * getter
	 * @return resourceFileName String
	 */
	public String getResourceFileName() {
		return resourceFileName;
	}
	/**
	 * setter
	 * @param resourceFileName String
	 */
	public void setResourceFileName(String resourceFileName) {
		this.resourceFileName = resourceFileName;
	}
	/**
	 * The execute method picks up commands written in a discord channel and performs the actions dictated by the 
	 * commands posted.  Valid commands are:
	 *  !ping which returns a message !pong from the bot to the same channel in which !ping was posted, 
	 *  !get which gets all the messages from all channels of a guild and stores them to a new File, and 
	 *  !update which gets all new messages from all channels of a guild since last !get or !update and 
	 *  appends them to the serialized file of stored posts
	 * 
	 */
	public void execute() {
		final DiscordClient client = DiscordClient.create(this.getToken());
		final GatewayDiscordClient gateway = client.login().block();
	
		gateway.on(MessageCreateEvent.class).subscribe(event -> {
		final Message message = event.getMessage();
		if ("!ping".equals(message.getContent())) {
			final MessageChannel channel = message.getChannel().block();
			channel.createMessage("Pong!").block();
		}//end if !ping
		else
		{
			if ("!get".equals(message.getContent())) {
				scrub(filename);
				List<Post> posts = new ArrayList<Post>();
				final Snowflake now = Snowflake.of(Instant.now());
				final Mono<Guild> g = message.getGuild();
				final Guild guild = g.block();
				Flux<GuildChannel> guildChannelFlux = guild.getChannels();
				final List<GuildChannel> channels = (List<GuildChannel>) guildChannelFlux.collectList().block();
				Collections.reverse(channels);
				channels.stream().forEach(elem -> {
					System.out.println("name: " + elem.getName() + " id: " + elem.getId() + " type: " + elem.getType());
					if(elem.getType().toString().compareTo("GUILD_TEXT") == 0) {
						final Flux<Message> messageFlux = ((MessageChannel) elem).getMessagesBefore(now);
						final List<Message> messages = (List<Message>) messageFlux.collectList().block();
			    		Collections.reverse(messages);
			    		messages.stream().forEach(e -> posts.add(getMessageData(e)));
			    		System.out.println("total posts in channel: " + posts.size() + "\n");
					}//end if elem type
				});//end lamda elems
				writeToFile(posts);
			}//end if !get"
			else
			{
				if ("!update".equals(message.getContent())) {
					if(new File(filename).exists()) {
						List<Post> newPosts = new ArrayList<Post>();
						List<Post> posts = readFromFile();
						Post lastPost = getLastPost(posts);
						Instant lastPostTime = lastPost.getTimeStamp();
						final Snowflake last = Snowflake.of(lastPostTime);
						final Mono<Guild> g = message.getGuild();
						final Guild guild = g.block();
						Flux<GuildChannel> guildChannelFlux = guild.getChannels();
						final List<GuildChannel> channels = (List<GuildChannel>) guildChannelFlux.collectList().block();
						Collections.reverse(channels);
						channels.stream().forEach(elem -> {
							System.out.println("name: " + elem.getName() + " id: " + elem.getId() + " type: " + elem.getType());
							if(elem.getType().toString().compareTo("GUILD_TEXT") == 0) {
								final Flux<Message> messageFlux = ((MessageChannel) elem).getMessagesAfter(last);
								final List<Message> messages = (List<Message>) messageFlux.collectList().block();
					    		Collections.reverse(messages);
					    		messages.stream().forEach(e -> newPosts.add(getMessageData(e)));
							}//end if elem.getType()
						});//end lamda elems
						//MessageChannel getMessagesAfter() is inclusive so last post from read is included in new
						//posts...so strip it off from the arraylist (also reverse the array for proper chronological order)
						Collections.reverse(newPosts);
						newPosts.remove(0);
						writeToFile(newPosts);
						System.out.println("number of new posts in update: " + newPosts.size());
					}//if File(filename).exists()
					else {
						System.out.println("Not a valid update, you must have used !get before using !update. Nothing written to file.");
					}//else for if File(filename).exists()
				}//if !update
			}//else for if !get
		}//else for if !ping
		});//end lamda events
	gateway.onDisconnect().block();
	}//execute
	/**
	 * The scrub method ensures the File object from past gets is deleted
	 * @param filename String
	 */
	private void scrub(String filename) {
		File f= new File(filename);
		if (f.exists()) {
			f.delete();
			System.out.println("original file deleted");
		}//if
	}//scrub
	/**
	 * Processes one Message object into a Post object
	 * @param message a discord4j core Message object of a single message posted on a discord channel
	 * @return a Post object containing the relevant data extracted from the message
	 */
	private static Post getMessageData (Message message){
		System.out.println(message + "\n" + message.getContent() + "\n");
		Post post = new Post();
		Optional<User> user;
		try {
			user = message.getAuthor();
			post.setTimeStamp(message.getTimestamp());
			post.setUserId(user.get().getId().asLong());
			post.setChannelId(message.getChannelId().asLong());
			post.setUserName(user.get().getUsername());
			post.setContent(message.getContent());
		} catch(NoSuchElementException nsee) {
				System.out.println(nsee.getMessage());
				nsee.printStackTrace();
		}
		System.out.println(post.toString());
		return post;		  
	}//getMessageData
	/**
	 * Writes a List of Posts to file using a ObjectOutputStream if the file does not yet exist and a 
	 * AppendingObjectOutputStream if it does
	 * @param posts List of Posts
	 */
	public void writeToFile(List<Post> posts) {
		try {
			File f= new File(filename);
			FileOutputStream fos = null;
			ObjectOutputStream oos = null;
			if(f.exists()) {
				fos = new FileOutputStream(f);
				oos = new AppendingObjectOutputStream(fos);
			}//if
			else {
				fos = new FileOutputStream(f);
				oos = new ObjectOutputStream(fos);
			}//else
			System.out.println("File: " + f.getName() + " at : " + f.getCanonicalPath());
			oos.writeObject(posts);
			oos.flush();
			oos.close();
			fos.close();
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			ioe.printStackTrace();
		}//catch
	}//end writeToFile
	/**
	 * Reads a serialized file of Post objects and returns them as a List
	 * @return List of Posts
	 */
	public List<Post> readFromFile() {
		ArrayList<Post> posts = new ArrayList<Post>();
		try {
			FileInputStream fis = new FileInputStream(filename);
			BufferedInputStream bis = new BufferedInputStream(fis);
			ObjectInputStream ois = new ObjectInputStream(bis);
			Object obj = ois.readObject();
			ois.close();
			posts = (ArrayList<Post>) obj;
			if(posts.size() == 0) {
				System.err.println("failure to obtain any data from input object (<Posts>) file");
			}
		} catch (IOException | ClassNotFoundException ioe) {
			System.out.println(ioe.getMessage());
			ioe.printStackTrace();
		}//catch
		return posts;
	}//readFromFile
	/**
	 * Finds the latest post object in a List of Post objects
	 * @param posts List of Posts
	 * @return Post the chronologically last occurring Post object in a List of Post objects
	 */
	private static Post getLastPost(List<Post> posts) {
		Collections.sort(posts);
		return posts.get(posts.size() -1);
	}//getLastPost
	/**
	 * Gets the value of a resource from the resource file when requested by key
	 * @param resourcefileName A String of the name of the resource file
	 * @param key String that is the key (or resource type name) of a json key value pair
	 * @return String that is the value of the resource requested by its key (type name)
	 */
	public String getResources(String resourcefileName, String key) {
		StringBuilder value = new StringBuilder();
		List<String> lines = null;
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource(resourcefileName).getFile());		
		try {
			String content = new String(Files.readAllBytes(file.toPath()));
			String [] arrLines = content.split(",");
			lines = Arrays.asList(arrLines);
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			ioe.printStackTrace();
		}//catch
		lines.stream().forEach(line-> {
			if(line.contains(key)) {
				//extract value
				String [] pair = line.split(":");
				if(!value.isEmpty()) {
					value.delete(0, value.length() -1);
				}//if not empty
				value.append(pair[1].trim().replaceAll("\"", "").replaceAll("}", "").replaceAll("\r|\n", ""));
			}//if key in line
		});
		System.out.println("resource file: " + resourcefileName + " key: " + key + " value returned: " + value);
		return value.toString();
	}//getResources
}//class
