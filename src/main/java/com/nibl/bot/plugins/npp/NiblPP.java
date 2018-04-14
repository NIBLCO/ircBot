package com.nibl.bot.plugins.npp;

import java.io.IOException;
import java.util.LinkedList;
import java.util.TreeMap;

import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.dcc.SendChat;

import com.nibl.bot.Bot;
import com.nibl.bot.command.Command;

public class NiblPP extends Command {
	
	NppDAO _nppDAO;
	String _adminHelp = null;
	
	public NiblPP(Bot myBot, Channel channel, User user, String actualcmd, String args) {
		super(myBot, channel, user, actualcmd, args);
		_nppDAO = (NppDAO) myBot.getDAOFactory().getDAO("NppDAO");
	}

	@Override
	public String getName() {
		return "nibl++";
	}

	@Override
	public String getCommand()	 {
		return "!loli !moehawk !nibl !shota !trap !nibl++";
	}

	@Override
	public String getDescription() {
		return "Get random internet currency.";
	}

	@Override
	public String getSyntax() {
		return "[optional nick for value]";
	}

	@Override
	public LinkedList<String> adminHelp() {
		return new LinkedList<String>();
		/*StringBuilder commands = new StringBuilder();
		commands.append("commands:\r\n");
		commands.append("none");
		
		sb.insert(0, commands.toString());
		_adminHelp = sb.toString();
		return _adminHelp;*/
	}

	@Override
	public void admin(SendChat session, User user, String message) throws IOException {
		/*if (message.startsWith("show ")) {//show a single person's money
			session.sendLine(_nppDAO.checkType(message.substring(5).trim(),"all"));
		} else if(message.startsWith("top10")){//show top 10
			session.sendLine("Top 10 Overall Riches:");
			session.sendLine(_nppDAO.topTen("all"));
		/*} else if(message.startsWith("give ")){
			session.sendLine(_nppDAO.give(message.substring(5)));
		} else if(message.startsWith("take ")){
			session.sendLine(_moneyDAO.take(message.substring(5)));*/
		//} else {
			session.sendLine("Unknown Command.");
		//}
	}

	@Override
	public void execute() {
		if(_args.length()==0){
			if(_user.isVerified())
				_nppDAO.add(_user, _channel, "all");
			else
				_myBot.sendNoticeFair(_user.getNick(),"Please register or identify your nick");
		} else if(_args.trim().toLowerCase().equals("top10")){
			_myBot.sendMessageFair(_channel, "Top 10 Richest People:");
			//_myBot.sendMessageFair(_channel,_nppDAO.topTen("all"));
			_myBot.sendMessageFair(_channel, "Not implemented yet");
		}else{
			_myBot.sendMessageFair(_channel,_nppDAO.checkType(_args.trim(),"all"));
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
	
	@Override
	public void mySchedule1() {
		_myBot.getLogger().info(getPrefix() + "Reset NPP flags.");
		_nppDAO.resetFlags();
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
