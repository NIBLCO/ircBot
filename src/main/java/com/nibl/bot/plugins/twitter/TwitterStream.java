package com.nibl.bot.plugins.twitter;

import java.util.HashMap;

import com.nibl.bot.Bot;
import com.nibl.bot.BotExtend;

import twitter4j.*;

public class TwitterStream extends BotExtend {
	
    twitter4j.TwitterStream twitterStream = null;
    public HashMap<String,TwitterChannel> twitterChannels = null;
    public TwitterDAO _twitterDAO = null;
    public TwitterService _twitterService = null;
    
    public TwitterStream(Bot bot, TwitterService twitterService){
    	super(bot);
    	_twitterService = twitterService;
    	_twitterDAO = new TwitterDAO(_myBot);
    	twitterStream = new TwitterStreamFactory(TwitterService.twitterStreamConfiguration.build()).getInstance();
    	
        twitterStream.addListener(listener);
        twitterChannels = _twitterDAO.getTweetsToProcess();
        
        if( twitterChannels.size() != 0 ) {
        	int counter = 0;
        	long[] userIDs = new long[twitterChannels.size()];
        	for(TwitterChannel channel : twitterChannels.values()){
        		userIDs[counter] = Long.parseLong(channel.getTwitterID());
        		counter++;
        	}
        	/*
        	twitterStream.user( userIDs.toArray(new String[userIDs.size()]) );
            */
        	FilterQuery query = new FilterQuery();
            query.follow( userIDs );
            twitterStream.filter(query);
            
        } else {
        	_myBot.getLogger().info("No Twitter channels to stream");
        }
    }
    
    public void shutdown(){
    	twitterStream.shutdown();
    }
    
    private final UserStreamListener listener = new UserStreamListener() {
        @Override
        public void onStatus(Status status) {
        	_myBot.getLogger().info("Logging tweet for " + status.getUser().getId() + ": " + status.getText());
        	String userID = String.valueOf(status.getUser().getId());
        	if( twitterChannels.containsKey(userID) ){
        		_myBot.getLogger().info("Twitter Status: " + twitterChannels.get(userID) + ": " + status.getText());
        		_twitterService.handleStreamTwitterUpdate(twitterChannels.get(userID), status.getText());
        	}
        }

        @Override
        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}

        @Override
        public void onDeletionNotice(long directMessageId, long userId) {}

        @Override
        public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}

        @Override
        public void onScrubGeo(long userId, long upToStatusId) {}
        
        @Override
        public void onFriendList(long[] friendIds) {}

        @Override
        public void onFavorite(User source, User target, Status favoritedStatus) {}

        @Override
        public void onUnfavorite(User source, User target, Status unfavoritedStatus) {}

        @Override
        public void onFollow(User source, User followedUser) {}

        @Override
        public void onUnfollow(User source, User followedUser) {}

        @Override
        public void onDirectMessage(DirectMessage directMessage) {}

        @Override
        public void onUserListMemberAddition(User addedMember, User listOwner, UserList list) {}

        @Override
        public void onUserListMemberDeletion(User deletedMember, User listOwner, UserList list) {}

        @Override
        public void onUserListSubscription(User subscriber, User listOwner, UserList list) {}

        @Override
        public void onUserListUnsubscription(User subscriber, User listOwner, UserList list) {}

        @Override
        public void onUserListCreation(User listOwner, UserList list) {}

        @Override
        public void onUserListUpdate(User listOwner, UserList list) {}

        @Override
        public void onUserListDeletion(User listOwner, UserList list) {}

        @Override
        public void onUserProfileUpdate(User updatedUser) {}

        @Override
        public void onBlock(User source, User blockedUser) {}

        @Override
        public void onUnblock(User source, User unblockedUser) {}

        @Override
        public void onException(Exception ex) {
        	_twitterService.handleStreamException(ex);
        }
        
        @Override
        public void onStallWarning(StallWarning stallWarning) {
        	_twitterService.handleStreamStallWarning(stallWarning);
        }
        
    };
    
}
