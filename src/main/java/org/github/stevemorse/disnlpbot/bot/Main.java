package org.github.stevemorse.disnlpbot.bot;

/**
 * The driver for the disNLPBot.  Contains java main method
 * @author Steve Morse
 * @version 1.0
 */
public class Main {
	/**
	 * main method
	 * @param args Array of String
	 */
	public static void main(final String[] args) {
		/**
		 * The Discord Bot token
		 */
		  //final String token =  "ODAyMTg4NTc4NzA4NTIwOTgw.YArmVQ.gD5GGSF8f66GIENK8BTocfC9PXA";
		  /**
		   * The filename to read and write from and to
		   */
		  //final String filename = "posts.txt";
		  
		  /**
		   * The instantiated Bot
		   */
		  final DisNLPBot bot = new DisNLPBot();
		  bot.execute();
	}//main
}//Main
