package com.nibl.bot.plugins.money;

import java.io.IOException;
import java.util.LinkedList;
import java.util.TreeMap;

import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.dcc.SendChat;

import com.nibl.bot.Bot;
import com.nibl.bot.command.Command;

public class Money extends Command {
	
	MoneyDAO _moneyDAO;
	String _adminHelp = null;
	
	public Money(Bot myBot, Channel channel, User user, String actualcmd, String args) {
		super(myBot, channel, user, actualcmd, args);
		_moneyDAO = (MoneyDAO) myBot.getDAOFactory().getDAO("MoneyDAO");
	}

	@Override
	public String getName() {
		return "money";
	}

	@Override
	public String getCommand()	 {
		return "!money";
	}

	@Override
	public String getDescription() {
		return "Money trigger (not real money).";
	}

	@Override
	public String getSyntax() {
		return "[optional nick for value]";
	}

	@Override
	public LinkedList<String> adminHelp() {
		LinkedList<String> help = new LinkedList<String>();
		help.add("show user_name - shows the amount of money the user has\r\n");
		help.add("top10 - shows the top 10 users\r\n");
		help.add("give user_name amount - give the user some $ (it's logged... don't abuse)\r\n");
		help.add("take user_name amount - take some of the user's $ (it's logged... don't abuse)\r\n");
		help.add("cflags - clears today's flags so people can get money again\r\n");
		return help;
		/*
		StringBuilder commands = new StringBuilder();
		commands.append(getPrefix() +"show user_name - shows the amount of money the user has\r\n");
		commands.append(getPrefix() +"top10 - shows the top 10 users\r\n");
		commands.append(getPrefix() +"give user_name amount - give the user some $ (it's logged... don't abuse)\r\n");
		commands.append(getPrefix() +"take user_name amount - take some of the user's $ (it's logged... don't abuse)\r\n");
		commands.append(getPrefix() +"cflags - clears today's flags so people can get money again\r\n");
		_adminHelp = sb.toString() + commands.toString();
		return _adminHelp;*/
	}

	@Override
	public void admin(SendChat session, User user, String message) throws IOException {
		if (message.startsWith("show ")) {//show a single person's money
			session.sendLine(_moneyDAO.checkMoney(message.substring(5).trim()));
		} else if(message.startsWith("top10")){//show top 10
			session.sendLine("Top 10 Richest People:");
			session.sendLine(_moneyDAO.topTen());
		} else if(message.startsWith("cflags")){
			session.sendLine("Clearing Today's Flags");
			_moneyDAO.resetFlags();
		} else if(message.startsWith("give ")){
			session.sendLine(_moneyDAO.give(message.substring(5)));
		} else if(message.startsWith("take ")){
			session.sendLine(_moneyDAO.take(message.substring(5)));
		} else {
			session.sendLine("Unknown Command.");
		}
	}

	@Override
	public void execute() {
		if(_args.length()==0){
			if( _user.isVerified() )
				_moneyDAO.add(_user, _channel);
			else
				_myBot.sendNoticeFair(_user.getNick(),"Please register or identify your nick");
		} else if(_args.trim().toLowerCase().equals("top10")){
			_myBot.sendMessageFair(_channel, "Top 10 Richest People:");
			_myBot.sendMessageFair(_channel,_moneyDAO.topTen());
		}else{
			_myBot.sendMessageFair(_channel,_moneyDAO.checkMoney(_args.trim()));
		}
	}

	@Override
	public int getAccessLevel() {
		return 7;
	}

	@Override
	public void executeAfterDisabled() {
		this.delSchedule(1);
	}

	@Override
	public void executeAfterEnabled() {
		this.addSchedule(1, 10, 24*60*60);
	}
	
	public void mySchedule1() {
		_myBot.getLogger().info(getPrefix() + "Reset !money today_flag.");
		_moneyDAO.resetFlags();
	}
	
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
