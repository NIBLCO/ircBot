package com.nibl.bot.util;


import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;

import org.pircbotx.User;
import org.pircbotx.dcc.SendChat;


public interface Manageable {

	/**
	 * @return a name for the Service/Command
	 */
	public String getName();
	
	/**
	 * @return short description of what it does.
	 */
	public String getDescription();
	
	/**
	 * A welcome / help message for the module give a list of valid commands.
	 */
	public LinkedList<String> adminHelp();

	/**
	 * write code to manage your module
	 * 
	 * @param session
	 *          - chat session
	 * @param user
	 *          - user requested
	 * @param login
	 *          -
	 * @param hostname
	 * @param message
	 *          - message sent to you
	 * @throws SQLException 
	 */
	public void admin(SendChat session, User user, String message) throws IOException, SQLException;
	
	/**
	 * this is called before it is passed to the module admin
	 * 
	 * @param session
	 * @param user
	 * @param login
	 * @param hostname
	 * @throws IOException
	 */
	public void adminWrapper(SendChat session, User user) throws IOException;
	
}
