package com.nibl.bot.plugins.rules;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.dcc.SendChat;

import com.nibl.bot.Bot;
import com.nibl.bot.command.Command;

public class Rules extends Command {
	
	String _adminHelp = null;
	RulesDAO _rulesDAO;
	private static TreeMap<String, GregorianCalendar> restrictRuns = new TreeMap<String, GregorianCalendar>();
	
	public Rules(Bot myBot, Channel channel, User user, String actualcmd, String args) {
		super(myBot, channel, user, actualcmd, args);
		_rulesDAO = new RulesDAO(_myBot);
	}

	@Override
	public String getCommand() {
		return "!rules";
	}

	@Override
	public String getDescription() {
		return "Displays channel rules";
	}

	@Override
	public String getSyntax() {
		return "";
	}

	@Override
	public LinkedList<String> adminHelp() {
		return new LinkedList<String>();
	}

	@Override
	public void admin(SendChat session, User user, String message) throws IOException {}

	@Override
	public void execute() {
		GregorianCalendar currentDate = new GregorianCalendar();
		
		if( restrictRuns.containsKey( _channel.getName().toLowerCase() ) && currentDate.before( restrictRuns.get(_channel.getName().toLowerCase()) ) ){
			// Ran within the past hour
			return;
		} else {
			currentDate.add(Calendar.MINUTE, 10);
			restrictRuns.put(_channel.getName().toLowerCase(), currentDate);
		}
		
		LinkedHashMap<Integer, String> rules = _rulesDAO.getChannelRules(_channel.getName());
		if( rules.size() == 0 ) {
			_myBot.sendMessageFair(_channel, "No rules found. Go Wild!");
		} else {
			for( Map.Entry<Integer, String> rule : rules.entrySet() ){
				_myBot.sendMessageFair(_channel, rule.getKey() + ") " + rule.getValue());
			}
		}
	}

	@Override
	public String getName() {
		return "rules";
	}

	@Override
	public int getAccessLevel() {
		return 9;
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
