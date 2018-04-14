package com.nibl.bot.plugins.latestpacks;

import java.io.IOException;
import java.util.LinkedList;
import java.util.TreeMap;

import org.pircbotx.Channel;
import org.pircbotx.Colors;
import org.pircbotx.User;
import org.pircbotx.dcc.SendChat;

import com.nibl.bot.Bot;
import com.nibl.bot.command.Command;
import com.nibl.bot.plugins.updatepacklist.Pack;

public class LatestPacks extends Command{

	private LatestPacksDAO _latestPacksDAO;
	
	public LatestPacks(Bot myBot, Channel channel, User user, String actualcmd, String args) {
		super(myBot, channel, user,actualcmd, args);
		_latestPacksDAO = new LatestPacksDAO(myBot);
	}
	
	private void showLatest(int number){
		LinkedList<Pack> latestPacks = _latestPacksDAO.getLatestPacks(number);
		if(latestPacks.size()>=1){
			for(Pack pack : latestPacks){
				_myBot.sendMessageFair(_user.getNick(), Colors.BROWN + "[" + Colors.TEAL + pack.getSize() + Colors.BROWN + "] " + Colors.DARK_GRAY + pack.getName() + Colors.NORMAL + "  /MSG " + pack.getBotName() + " XDCC SEND " + pack.getNumber());
			}
		}else{
			_myBot.sendMessageFair(_channel, "No Packs Recorded Yet");
		}
	}
	
	@Override
	public String getName() {
		return "latest";
	}

	@Override
	public LinkedList<String> adminHelp() {
		return new LinkedList<String>();
		//return sb.toString();
	}

	@Override
	public void admin(SendChat session, User user, String message) throws IOException {}

	@Override
	public void execute() {
		_args = _args.trim();
		if(_args.length()==0)
		{
			showLatest(5);
		}else{
			try{
				int number = Integer.parseInt(_args);
				if(number>15)
					number = 15;
				
				showLatest(number);
				
			}catch(Exception e){
				
			}
		}
	}

	@Override
	public int getAccessLevel() {
		return 4;
	}

	@Override
	public void executeAfterEnabled() {}

	@Override
	public void executeAfterDisabled() {}

	@Override
	public String getCommand() {
		return "!latest";
	}

	@Override
	public String getSyntax() {
		return "pack#";
	}

	@Override
	public String getDescription() {
		return "Returns the latest # of packs";
	}

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
