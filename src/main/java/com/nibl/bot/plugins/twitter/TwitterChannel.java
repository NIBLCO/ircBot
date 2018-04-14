package com.nibl.bot.plugins.twitter;

public class TwitterChannel {

	private int _channelid;
	private String _channel;
	private String _twitter_user;
	private long _latest_tweet_id;
	private String twitter_id;
	
	public TwitterChannel(int channelid, String channel, String twitter_user, String twitter_id){
		setChannelID(channelid);
		setChannel(channel);
		setTwitterUser(twitter_user);
		setTwitterID(twitter_id);
	}
	
	public int getChannelID() {
		return _channelid;
	}

	public void setChannelID(int channelid) {
		this._channelid = channelid;
	}
	public String getChannel() {
		return _channel;
	}

	public void setChannel(String channel) {
		this._channel = channel;
	}

	public String getTwitterUser() {
		return _twitter_user;
	}

	public void setTwitterUser(String twitter_user) {
		this._twitter_user = twitter_user;
	}
	
	public String getTwitterID() {
		return twitter_id;
	}

	public void setTwitterID(String twitter_id) {
		this.twitter_id = twitter_id;
	}

	public long getLatestTweetID() {
		return _latest_tweet_id;
	}

	public void setLatestTweetID(long latest_tweet_id) {
		this._latest_tweet_id = latest_tweet_id;
	}
	
}
