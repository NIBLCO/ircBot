package com.nibl.bot.plugins.anidb;

import java.io.IOException;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.dcc.SendChat;
import org.w3c.dom.Document;

import com.nibl.bot.Bot;
import com.nibl.bot.command.Command;

public class Anidb extends Command {
	
	String _adminHelp = null;
	AnidbDAO _anidbDAO;
	AnidbKeepAlive _anidbKeepAlive;
	
	public Anidb(Bot myBot, Channel channel, User user, String actualcmd, String args) {
		super(myBot, channel, user, actualcmd, args);
		_anidbDAO = (AnidbDAO) _myBot.getDAOFactory().getDAO("AnidbDAO");
	}

	@Override
	public String getCommand() {
		return "!anidb";
	}

	@Override
	public String getSyntax() {
		return "[Anime Name]";
	}

	@Override
	public String getDescription() {
		return "Performs a lookup on http://anidb.net/.";
	}

	@Override
	public LinkedList<String> adminHelp() {
		return new LinkedList<String>();
	}

	@Override
	public void admin(SendChat session, User user, String message) throws IOException {
	}

	@Override
	public void execute() {
		if (_args.length() == 0) {
			_myBot.sendMessageFair(_channel.getName(), "Use !anidb search_term to search http://anidb.net/ ");
			return;
		}
		
		
		try {
			int moveForward = 0;
			String term = _args;
			Pattern searchPattern = Pattern.compile("(.*)(?:\\+|)(\\+\\d+)$");
			Matcher digitMatcher = searchPattern.matcher(_args);
			if(digitMatcher.find()){
				term = digitMatcher.group(1);
				term = term.trim();
				String test = digitMatcher.group(2);
				moveForward = Integer.parseInt(test.trim().replace("+", ""));
			}
			
			_anidbKeepAlive = (AnidbKeepAlive) _myBot.getServiceFactory().getRegisteredService().get("AnidbKeepAlive");
			
			//find aid, total#of series with this name, as well as the series name
			Object[] data = _anidbDAO.findAidTotalAndSeriesName(term, moveForward);
			int aid = (Integer) data[0];
			int total = (Integer) data[1];
			String seriesName = (String) data[2];
			
			//if the series has been searched in the last 24 hrs, output what we have in memory
			if(_anidbKeepAlive.anidbSeriesExists(aid)){
				_anidbKeepAlive.getAnidbSeries(aid).outputToChannel(_channel.getName(), _user.getNick());
			}else{//otherwise create a new AnidbSeries and store it
				Document series = _anidbDAO.findSeries(aid);
				if(series!=null){
					_anidbKeepAlive.addAnidbSeries(createSeries(series, moveForward, total, aid, seriesName));
					_anidbKeepAlive.getAnidbSeries(aid).outputToChannel(_channel.getName(), _user.getNick());
				}else{
					_myBot.sendMessageFair(_channel.getName(), "!anidb - No Results Found");
				}
			}
		} catch (Exception e) {
			_myBot.sendMessageFair(_channel.getName(), "!anidb - No Results Found");
		}

	}

	public AnidbSeries createSeries(Document series, Integer moveForward, Integer total, Integer aid, String seriesName){
		String type;
		String episodeCount;
		String startDate;
		String endDate;
		String rating;
		String tempRating;
		String description;
		
		moveForward++;//do this to use 1 as first instead of 0 as first series.
		
		try{
			type = series.getElementsByTagName("type").item(0).getFirstChild().getNodeValue();
		}catch(NullPointerException e){type = "";}
		try{
			episodeCount = series.getElementsByTagName("episodecount").item(0).getFirstChild().getNodeValue();
		}catch(NullPointerException e){episodeCount = "";}
		try{
			startDate = series.getElementsByTagName("startdate").item(0).getFirstChild().getNodeValue();
		}catch(NullPointerException e){startDate = "";}
		try{
			endDate = series.getElementsByTagName("enddate").item(0).getFirstChild().getNodeValue();
		}catch(NullPointerException e){endDate = "";}
		try{
			rating = series.getElementsByTagName("permanent").item(0).getFirstChild().getNodeValue();
			 tempRating = "";
		}catch(NullPointerException e){
			rating = "";
			try{
				tempRating = series.getElementsByTagName("temporary").item(0).getFirstChild().getNodeValue();
			}catch(NullPointerException f){tempRating = "";}
		}
		try{
			description = series.getElementsByTagName("description").item(0).getFirstChild().getNodeValue().replaceAll("\\n", "");
		}catch(NullPointerException e){description = "";}
		
		
		return new AnidbSeries(_myBot, aid, total, moveForward, seriesName, type, episodeCount, startDate, endDate, rating, tempRating, description);
	}

	@Override
	public String getName() {
		return "anidb";
	}

	@Override
	public int getAccessLevel() {
		return 5;
	}

	@Override
	public void executeAfterDisabled() {
		_anidbDAO.dropTables();
	}

	@Override
	public void executeAfterEnabled() {
		_anidbDAO.createTables();
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
