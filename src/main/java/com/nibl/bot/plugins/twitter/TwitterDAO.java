package com.nibl.bot.plugins.twitter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import com.nibl.bot.Bot;
import com.nibl.bot.dao.DataAccessObject;

import java.sql.PreparedStatement;

public class TwitterDAO extends DataAccessObject {
	
	public TwitterDAO(Bot bot) {
		super(bot);
	}
	
	public void storeTweet(int channelID, String tweetid, String tweetString){
		try {
			PreparedStatement statement = getConnection().prepareStatement("INSERT INTO tweets (channelid, tweetid, tweet_text) VALUES (?,?,?)");
			statement.setInt(1, channelID);
			statement.setString(2, tweetid);
			statement.setString(3, tweetString);
			statement.executeUpdate();
			statement.close();
		}catch (SQLException e){
			_myBot.sendMessageFair("Jenga", "Failed to store tweet for channelID " + channelID + ". Tweet Text: " + tweetString);
		}
	}
	
	public long getMaxTweetID(int channelID){
		long tweetid = 0;
		Statement statement = null;
		try {
			String query = "SELECT MAX(tweetid) AS tweetid FROM tweets WHERE channelid = '" + channelID + "'";
			statement = getConnection().createStatement();
			ResultSet rs = statement.executeQuery(query);
			if( rs.next() ){
				tweetid = rs.getLong("tweetid");
			}
			
			statement.close();
			
		}catch (SQLException e){
			_myBot.sendMessageFair("Jenga", "Failed to get max tweet id for channelID " + channelID);
		}
		
		return tweetid;
	}
	
	public HashMap<String,TwitterChannel> getTweetsToProcess(){
		HashMap<String,TwitterChannel> twitterChannels = new HashMap<String,TwitterChannel>();
		Statement statement = null;
		
		try {
			String query = "SELECT * FROM tweet_channels";
			statement = getConnection().createStatement();
			ResultSet rs = statement.executeQuery(query);
			
			while(rs.next()){
				TwitterChannel twitterChannel = new TwitterChannel(rs.getInt("id"), rs.getString("channel"), rs.getString("twitter_user"), rs.getString("twitter_id"));
				twitterChannel.setLatestTweetID( getMaxTweetID( twitterChannel.getChannelID() ) );
				twitterChannels.put(twitterChannel.getTwitterID(), twitterChannel);
			}
			
			statement.close();
			
		}catch (SQLException e){
			return twitterChannels;
		}
		
		return twitterChannels;
	}

	@Override
	public void createTables() {}

	@Override
	public void dropTables() {}
	
}
