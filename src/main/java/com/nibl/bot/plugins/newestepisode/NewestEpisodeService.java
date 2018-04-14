package com.nibl.bot.plugins.newestepisode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.pircbotx.User;
import org.pircbotx.dcc.SendChat;
import org.pircbotx.hooks.events.HalfOpEvent;
import org.pircbotx.hooks.events.IncomingFileTransferEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.ModeEvent;
import org.pircbotx.hooks.events.NoticeEvent;
import org.pircbotx.hooks.events.TopicEvent;

import com.nibl.bot.Bot;
import com.nibl.bot.plugins.twitter.TwitterService;
import com.nibl.bot.service.Service;

public class NewestEpisodeService extends Service {
	final long _timeInSeconds = Long.parseLong(_myBot.getProperty("newestepisode_update_time")); // 15 minutes
	NewestEpisodeDAO _newestEpisodeDAO;
	
	public NewestEpisodeService(Bot bot) {
		super(bot);
		_newestEpisodeDAO = new NewestEpisodeDAO(bot);
	}
	
	public void update(){		
		for(Entry<Integer, HashMap<String, Object>> series : _newestEpisodeDAO.getSearchable().entrySet() ){
			
			Integer latestEpisode = _newestEpisodeDAO.searchNewestEpisode((String)series.getValue().get("seriessearch"), (Integer)series.getValue().get("nextepisode"));
			if( latestEpisode != -1 ){
				_myBot.getLogger().info("NewestEpisode - Found new episode: " + ((String)series.getValue().get("seriesname")) + " Episode " + latestEpisode);
				// Increment latest episode number
				_newestEpisodeDAO.incrementNewestEpisode(series.getKey(), latestEpisode);
				
				// Create url to redirect to
				StringBuilder url = new StringBuilder();
				url.append( "http://nibl.co.uk/bots.php?search=" );
				url.append( ((String)series.getValue().get("seriesname")).replace(" ", "+") );
				url.append( "+" );
				url.append( latestEpisode );
				
				try {
					String redirectHash = _newestEpisodeDAO.getRedirectHash(url.toString());
					String redirectURL = _newestEpisodeDAO.getRedirect(redirectHash);
					if( null == redirectURL ){
						// redirect url doesn't exist, so create a new one
						_newestEpisodeDAO.createRedirect(url.toString(), redirectHash);
						redirectURL = _newestEpisodeDAO.getRedirect(redirectHash);
					}
					String tweet = ((String)series.getValue().get("seriesname")) + " Episode " + latestEpisode + " - " + redirectURL;
					
					_myBot.getLogger().info("NewestEpisode - Post new tweet: " + tweet);
					
					// Post tweet using redirect url
					TwitterService twitterService = (TwitterService) _myBot.getServiceFactory().getRegisteredService().get("TwitterService");
					twitterService.postTweet( tweet );
					
				} catch (Exception e) {
					_myBot.getLogger().error("Failed creating redirect and posting to twitter " + url.toString() + ": " + e.getMessage());
				}
				
			}
		}
	}
	
	public ArrayList<String> refreshList(String url) throws IOException{
		try{
			BufferedReader in = getBufferedReader(url);
			ArrayList<String> seriesList = new ArrayList<String>();
    		String line = "";
    		while ( (line = in.readLine()) != null ) {
				seriesList.add(line.trim());
			}
			return seriesList;
		}catch(Exception e){
			_session.sendLine("I fucked it up: " + url);
		}
		return null;
	}
	
	public void showCurrent() throws IOException{
		_session.sendLine("Show current");
	}
	
	// copied from abstractdistrobot
	private BufferedReader getBufferedReader(String url) throws Exception {
		if ( url.contains(".") && ( url.toLowerCase().startsWith("http://") || url.toLowerCase().startsWith("https://") ) ) {
			URL urlopen = new URL(url);
			URLConnection urlConnection = urlopen.openConnection();
			urlConnection.setConnectTimeout(10000);
			urlConnection.setReadTimeout(10000);
			InputStream inputStream = urlConnection.getInputStream();
			return new BufferedReader(new InputStreamReader(inputStream,"UTF8"));
		} else {
			throw new Exception("Not a valid url");
		}
	}
	
	@Override
	public void execute() {
		
		long start = System.currentTimeMillis();
		try {
			while(_status == Status.RUNNING){
				Thread.sleep(1000*20);
				long elapsedTimeMillis = System.currentTimeMillis()-start;
				float elapsedTimeSec = elapsedTimeMillis/1000F;
				if(elapsedTimeSec>=_timeInSeconds || start == 0){
					update();
					start = System.currentTimeMillis();
				}
			}
		} catch (InterruptedException e) {
			_myBot.getLogger().error("Interrupted in Scheduler sleep");
		} catch (Exception e){
			_myBot.getLogger().error("Unknown Exception: " + e.toString());
		}
		
		_myBot.getLogger().info("Service " + getName() + " stopped.");
		
	}

	@Override
	public String getName() {
		return "newestepisode";
	}

	@Override
	public String getDescription() {
		return "Finds newest episodes and posts to Twitter";
	}

	@Override
	public LinkedList<String> adminHelp() {
		LinkedList<String> help = new LinkedList<String>();
		help.add("help - Shows a list commands\r\n");
		help.add("run - Run search for new series and post to twitter\r\n");
		help.add("current - Shows a list of current series\r\n");
		help.add("set [url] - Gets list of current series\r\n");
		return help;
	}

	@Override
	public void admin(SendChat session, User user, String message) throws IOException, SQLException {
		if (message.equals("run")) {
			update();
		} else if(message.startsWith("set ") ){
			if( _newestEpisodeDAO.setList( refreshList(message.replace("set ", "").trim()) ) ){
				_session.sendLine("List set successfully");
			} else {
				_session.sendLine("Failed to set list");
			}
		} else if(message.startsWith("current") ){
			_newestEpisodeDAO.showCurrent(session);
		}
	}

	@Override
	public int getAccessLevel() {
		return 0;
	}

	@Override
	public boolean needsMessages() {
		return false;
	}

	@Override
	public boolean needsNotices() {
		return false;
	}

	@Override
	public boolean needsIncomingFileTransfers() {
		return false;
	}

	@Override
	public boolean needsOnPart() {
		return false;
	}

	@Override
	public boolean needsOnJoin() {
		return false;
	}

	@Override
	public boolean needsOnMode() {
		return false;
	}

	@Override
	public boolean needsOnHop() {
		return false;
	}

	@Override
	public boolean needsOnTopic() {
		return false;
	}

	@Override
	public int delay() {
		return 0;
	}

	@Override
	public void onTopic(TopicEvent topic) {}

	@Override
	public void onMessage(MessageEvent message) {}

	@Override
	public void onNotice(NoticeEvent notice) {}

	@Override
	public void onPart(String nick) {}

	@Override
	public void onJoin(String nick) {}

	@Override
	public void onMode(ModeEvent mode) {}
	
	@Override
	public void onHalfOp(HalfOpEvent event) {}
	
	@Override
	public void onIncomingFileTransfer(IncomingFileTransferEvent transfer) {}

}