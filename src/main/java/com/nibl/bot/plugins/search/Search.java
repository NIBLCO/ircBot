package com.nibl.bot.plugins.search;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.TreeMap;

import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.dcc.SendChat;

import com.nibl.bot.Bot;
import com.nibl.bot.command.Command;


public class Search extends Command {
/*
 * The purpose of this module is to connect to the database, search for
 * some results, then output results.
 */
	
	SearchDAO _searchDAO;
	String _adminHelp = null;

	public Search(Bot myBot, Channel channel, User user, String actualcmd, String args) {
		super(myBot, channel, user, actualcmd, args);
		_searchDAO = (SearchDAO)myBot.getDAOFactory().getDAO("SearchDAO");
	}

	@Override
	public String getCommand() {
		return "!search @search !s @s !find";
	}

	@Override
	public String getDescription() {
		return "Search all % bots in this channel. Private message the bot to get results privately.";
	}

	@Override
	public String getSyntax() {
		return "[search_term]";
	}

	@Override
	public LinkedList<String> adminHelp() {
		return new LinkedList<String>();
		/*_adminHelp = sb.toString();
		return _adminHelp;*/
	}
	
	@Override
	public void execute() {
		_args = _args.trim();
		if(_args.length()==0)
		{
			if( this.isOriginatePrivateMessage() ){
				_myBot.sendMessageFair(_user.getNick(), getDescription());
			} else {
				_myBot.sendMessageFair(_channel, getDescription());
			}
			
			return;
		}

		for(String temp[] :_searchDAO.search(_user.getNick(),_args)){
			
			if( this.isOriginatePrivateMessage() ){
				_myBot.sendMessageFair(_user.getNick(), temp[1]);
			} else {
				_myBot.sendNoticeFair(temp[0], temp[1]);
			}
			
		}
	}

	@Override
	public String getName() {
		return "search";
	}

	@Override
	public int getAccessLevel() {
		return 7;
	}

	@Override
	public void executeAfterDisabled() {}

	@Override
	public void executeAfterEnabled() {}

	@Override
	public void admin(SendChat session, User user, String message) throws IOException, SQLException {}
	
	@Override
	public TreeMap<String, Integer> getCommandAccessLevels() {
		// Default command access levels
		TreeMap<String, Integer> commandAccessLevels = new TreeMap<String, Integer>();
		for(String command : this.getCommand().split(" ")) {
			commandAccessLevels.put(command.toLowerCase(), -1);
		}
		return commandAccessLevels;
	}
	
	@Override
	public Boolean acceptPrivateMessage() {
		return true;
	}
	
}
