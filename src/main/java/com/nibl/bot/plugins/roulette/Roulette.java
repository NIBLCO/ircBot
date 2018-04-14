package com.nibl.bot.plugins.roulette;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;
import java.util.TreeMap;

import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.dcc.SendChat;

import com.nibl.bot.Bot;
import com.nibl.bot.command.Command;

public class Roulette extends Command {
	
	String _adminHelp = null;
	RouletteDAO _rouletteDAO;
	int _accessLevel;
	
	public Roulette(Bot myBot, Channel channel, User user, String actualcmd, String args) {
		super(myBot, channel, user, actualcmd, args);
		_rouletteDAO = (RouletteDAO) _myBot.getDAOFactory().getDAO("RouletteDAO");
	}

	@Override
	public String getCommand() {
		return "!roulette";
	}

	@Override
	public String getDescription() {
		return "Take out your Roulette rope =:3";
	}

	@Override
	public String getSyntax() {
		return "(Only useable by level 10 internet wizards and above)";
	}

	@Override
	public LinkedList<String> adminHelp() {
		return new LinkedList<String>();
	}

	@Override
	public void admin(SendChat session, User user, String message) throws IOException {}

	@Override
	public void execute() {
		_accessLevel = _rouletteDAO.getAccessLevel(_user);
		if(_accessLevel>=9){
			Random rand = new Random();
			int randUser = rand.nextInt(_channel.getNormalUsers().size());
			User possibleKick = (User) _channel.getNormalUsers().toArray()[randUser];
			
			if( !_channel.isOp( _myBot.getBot().getUserBot() ) ){
				_myBot.sendMessageFair(_channel, "I'm not OP in " + _channel.getName() + ", but I would have chosen " + possibleKick.getNick() + " for kickrape.");
				if( possibleKick.getNick().toLowerCase().equals( _myBot.getBot().getNick().toLowerCase()) ){
					_myBot.sendMessageFair(_channel, "Ohshi.. that's me");
				}
				return;
			}
			
			_myBot.sendMessageFair(_channel, _user.getNick() + " has chosen " + possibleKick.getNick() + " for kickrape!");
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				_myBot.getLogger().warn("Interrupted");
			}
			_channel.send().kick(possibleKick);
		}else{
			_myBot.sendMessageFair(_channel, "You dont have access to run this command.");
		}
	}

	@Override
	public String getName() {
		return "roulette";
	}

	@Override
	public int getAccessLevel() {
		return 9;
	}

	@Override
	public void executeAfterDisabled() {

	}

	@Override
	public void executeAfterEnabled() {
		
	}

	public void mySchedule() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public TreeMap<String, Integer> getCommandAccessLevels() {
		// Default command access levels
		TreeMap<String, Integer> commandAccessLevels = new TreeMap<String, Integer>();
		for(String command : this.getCommand().split(" ")) {
			commandAccessLevels.put(command.toLowerCase(), this.getAccessLevel());
		}
		return commandAccessLevels;
	}
	
	@Override
	public Boolean acceptPrivateMessage() {
		return false;
	}

}
