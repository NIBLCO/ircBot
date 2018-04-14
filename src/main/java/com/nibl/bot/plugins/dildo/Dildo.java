package com.nibl.bot.plugins.dildo;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;
import java.util.TreeMap;

import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.dcc.SendChat;

import com.nibl.bot.Bot;
import com.nibl.bot.command.Command;

public class Dildo extends Command {
	
	String _adminHelp = null;
	DildoDAO _dildoDAO;
	int _accessLevel;
	
	public static int NOUN = 1;
	public static int VERB = 2;
	public static int ADJECTIVE = 3;
	public static int ADVERB = 4;
	
	public Dildo(Bot myBot, Channel channel, User user, String actualcmd, String args) {
		super(myBot, channel, user, actualcmd, args);
		_dildoDAO = (DildoDAO) _myBot.getDAOFactory().getDAO("DildoDAO");
	}

	@Override
	public String getCommand() {
		return "!dildo";
	}

	@Override
	public String getDescription() {
		return "Nuuuuuuuuu";
	}

	@Override
	public String getSyntax() {
		return "[username]";
	}

	@Override
	public LinkedList<String> adminHelp() {
		return new LinkedList<String>();
	}

	@Override
	public void admin(SendChat session, User user, String message) throws IOException {}

	// ooinuza [verb]s [randUser] with [randNumber] [adjective:?] [noun] dildo of [adverb:?] [verb]
	// 1=>Noun, 2=>Verb, 3=>Adjective, 4=>Adverb
	@Override
	public void execute() {
		
		String reciever = "";
		if( !_args.trim().equals("") ) {
			reciever = _args;
		} else {
			Random rand = new Random();
			User randUser = (User) getChannel().getUsers().toArray()[ rand.nextInt( getChannel().getUsers().size() ) ];
			reciever = randUser.getNick();
		}
		
		_myBot.sendMessageFair(_channel.getName(), _dildoDAO.getRandomDildo(_user.getNick(), reciever) );
	}

	@Override
	public String getName() {
		return "dildo";
	}

	@Override
	public int getAccessLevel() {
		return 7;
	}

	@Override
	public void executeAfterDisabled() {

	}

	@Override
	public void executeAfterEnabled() {
		
	}

	public void mySchedule() {
		
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
