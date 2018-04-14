package com.nibl.bot.plugins.help;

import java.io.IOException;
import java.util.LinkedList;
import java.util.TreeMap;

import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.dcc.SendChat;

import com.nibl.bot.Bot;
import com.nibl.bot.command.Command;
import com.nibl.bot.plugins.checknameregistered.CheckNameRegisteredDAO;

public class Help extends Command {

	String _adminHelp = null;
	Boolean _firstRun = false;
	CheckNameRegisteredDAO _checkNameRegisteredDAO;
	
	public Help(Bot myBot, Channel channel, User user, String actualcmd, String args) {
		super(myBot, channel, user, actualcmd, args);
		_checkNameRegisteredDAO = (CheckNameRegisteredDAO)_myBot.getDAOFactory().getDAO("CheckNameRegisteredDAO");
	}

	@Override
	public String getCommand() {
		return "!help";
	}

	@Override
	public String getDescription() {
		return "Displays this help message";
	}

	@Override
	public String getSyntax() {
		return "[command]";
	}

	@Override
	public LinkedList<String> adminHelp() {
		return new LinkedList<String>();
		/*_adminHelp = sb.toString();
		return _adminHelp;*/
	}

	@Override
	public void admin(SendChat session, User user, String message) throws IOException {}

	@Override
	public void execute() {
		int accessLevel = _checkNameRegisteredDAO.getUserAccessLevel(_user);
		if (_args.length() == 0) {
			StringBuilder sb = new StringBuilder();
			for (Command command : _myBot.getCommandFactory().getRegisteredCommand()) {
				try{
					for( String singleCommand : command.getCommandAccessLevels().keySet() ){
						if(command.isEnabled() && command.getCommandAccessLevel(singleCommand.toLowerCase()) <= accessLevel ){
							sb.append(singleCommand + " ");
						}
					}
				}catch(Exception e){
					//command doesn't exist in config
				}
			}
			_myBot.sendMessageFair(_channel, "These are the valid commands.  Use !help [command] for more info.");
			_myBot.sendMessageFair(_channel, sb.toString());
		}

		_args = _args.substring(1);
		Command command = null;
		if((command = _myBot.getCommandFactory().findRegisteredCommand(_args))!= null){
			StringBuilder commands = new StringBuilder();
			for( String singleCommand : command.getCommandAccessLevels().keySet() ){
				if(command.isEnabled() && command.getCommandAccessLevel(singleCommand.toLowerCase()) <= accessLevel ){
					commands.append(singleCommand + " ");
				}
			}
			
			_myBot.sendMessageFair(_channel, "Syntax: " + commands.toString() + command.getSyntax());
			_myBot.sendMessageFair(_channel, "Description: " + command.getDescription());
		}else
			_myBot.sendMessageFair(_channel, "Unknown command: !" + _args);
		/*for (Command command : _myBot.getCommandFactory().getRegisteredCommand()) {
			if (_args.equals(command.getCommand())) {
				_myBot.sendMessage(_channel, command.getSyntax());
				_myBot.sendMessage(_channel, command.getDescription());
				return;
			}
		}
		_myBot.sendMessage(_channel, "Unknown command.");*/
	}

	@Override
	public String getName() {
		return "help";
	}

	@Override
	public int getAccessLevel() {
		return 3;
	}

	@Override
	public void executeAfterDisabled() {}

	@Override
	public void executeAfterEnabled() {}

	public void mySchedule() {}

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
		return false;
	}
}
