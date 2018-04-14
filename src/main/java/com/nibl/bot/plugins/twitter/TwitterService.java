package com.nibl.bot.plugins.twitter;

import java.util.LinkedList;

import org.pircbotx.Colors;
import org.pircbotx.User;
import org.pircbotx.dcc.SendChat;
import org.pircbotx.hooks.events.HalfOpEvent;
import org.pircbotx.hooks.events.IncomingFileTransferEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.ModeEvent;
import org.pircbotx.hooks.events.NoticeEvent;
import org.pircbotx.hooks.events.TopicEvent;

import com.nibl.bot.Bot;
import com.nibl.bot.service.Service;

import twitter4j.StallWarning;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterService extends Service {
	final long _timeInSeconds;
	private static Twitter twitter = null;
	public static ConfigurationBuilder twitterStreamConfiguration = null;
	public static ConfigurationBuilder twitterConfiguration = null;
	public static TwitterDAO _twitterDAO = null;
	public static TwitterStream _twitterStream = null;
	
	public TwitterService(Bot bot) {
		super(bot);
		_timeInSeconds = Long.parseLong(_myBot.getProperty("twitter_update_time"));
		if( _myBot.getProperty("twitter").equals("start") )
			twitterObjects(); // TODO add an abstract method to service to 'execute after enabled'
	}
	
	private void twitterObjects(){
		_twitterDAO = new TwitterDAO(_myBot);
		
		// Use a read-write account @NIBLCO
		twitterConfiguration = new ConfigurationBuilder();
		twitterConfiguration
		  .setOAuthConsumerKey("0SWCeITlzOBZBNEHy2QfQ47Hz")
		  .setOAuthConsumerSecret("RF5CYBwOjqxJI8Cckm2dWkwbls7VpiRzKpHrv6SmZDubdCtkJY")
		  .setOAuthAccessToken("2329775923-9mpmu3M36QmykpQ61jMhnCOHMHV5YTdGBAMl9g7")
		  .setOAuthAccessTokenSecret("8SwwC0PnTTJUTjJATGsMx8ohGZCSaeP3kV6zmmD7yTUri");
		
		twitter = new TwitterFactory(twitterConfiguration.build()).getInstance();
		
		// Using a read-only account.  This allows reading from any twitter stream
		twitterStreamConfiguration = new ConfigurationBuilder();
		twitterStreamConfiguration
		  .setOAuthConsumerKey("iKOelGfpsuuTf5e08oe7nzwsg")
		  .setOAuthConsumerSecret("bMe9rgaMZc89WSUCWhifGWZGXb8xshDypK6BoXqtMeHlO4JR92")
		  .setOAuthAccessToken("1325410170-uyh3GH5yYC8hCz401s1MFjiPEpzVmiw4FZ3DL2j")
		  .setOAuthAccessTokenSecret("gMpKCNeOzuj8N1nnSJt9Gfq9r75g5hfcwQ2W3B3ytwqNJ");
		
		//twitterStream = new TwitterStream(_myBot);
		_twitterStream = new TwitterStream(_myBot, this);
	}
	
	/*
	private Twitter getTwitterObj(){
		try {
			
		} catch (TwitterException e) {
			error(e.getErrorMessage());
			error("Some twitter exception, rebuilding Twatter objects");
			twitterObjects();
		}
	
		return twitter;
	}*/
	
	public void remakeTwitterStream(){
		_twitterStream.shutdown();
		_twitterStream = new TwitterStream(_myBot, this);
	}
	
	public void update(){
		/*
		ArrayList<TwitterChannel> twitterChannels = _twitterDAO.getTweetsToProcess();
		
		for(TwitterChannel twitterChannel : twitterChannels){
			
			ResponseList<twitter4j.Status> statuses = twitter.getUserTimeline(twitterChannel.getTwitterUser());
		    for (twitter4j.Status status : statuses) {
		    	if( status.getId() > twitterChannel.getLatestTweetID() ){
		    		_twitterDAO.storeTweet(twitterChannel.getChannelID(), status.getId(), status.getText());
			    	_myBot.sendMessageFair(twitterChannel.getChannel(),  Colors.DARK_GREEN + "@" + twitterChannel.getTwitterUser() + Colors.DARK_BLUE + ": " + Colors.NORMAL + status.getText());
		    	}
		    }
		    
		}*/
		
	}
	
	@Override
	public void execute() {
		
		long start = 0;
		try {
			while(_status == Status.RUNNING){
				Thread.sleep(1000*10);
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

	public void postTweet(String tweet){
		try {
			twitter4j.Status status = twitter.updateStatus(tweet);
			_myBot.getLogger().info("Updated status successfully: " + status.getText());
		} catch (TwitterException e) {
			_myBot.sendMessageFairToAdmin("Failed Tweet: " + e.getMessage());
		}
	}
	
	public void handleStreamException(Exception ex){
		_myBot.getLogger().error("TwitterService Exception: " + ex.getMessage());
	}
	
	public void handleStreamStallWarning(StallWarning stallWarning){
		_myBot.getLogger().error("TwitterService StallWarning: " + stallWarning.getMessage());
	}
	
	public void handleStreamTwitterUpdate(TwitterChannel twitterChannel, String message){
		_myBot.sendMessageFair(twitterChannel.getChannel(),  Colors.DARK_GREEN + "@" + twitterChannel.getTwitterUser() + Colors.DARK_BLUE + ": " + Colors.NORMAL + message);
		_twitterDAO.storeTweet(twitterChannel.getChannelID(), twitterChannel.getTwitterID(), message);
	}
	
	@Override
	public String getName() {
		return "twitter";
	}

	@Override
	public String getDescription() {
		return "Get twitter feeds";
	}

	@Override
	public LinkedList<String> adminHelp() {
		return null;
	}

	@Override
	public void admin(SendChat session, User user, String message){}

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
