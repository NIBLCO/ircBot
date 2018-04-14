package com.nibl.bot.plugins.topicmanager;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.pircbotx.Colors;
import org.pircbotx.hooks.events.TopicEvent;

import com.nibl.bot.Bot;
import com.nibl.bot.dao.DataAccessObject;


/**
 * 
CREATE TABLE `channel_topics` (
`channel` VARCHAR( 20 ) NOT NULL ,
`full_topic` TEXT NOT NULL ,
`website_trim` VARCHAR( 400 ) NOT NULL ,
PRIMARY KEY ( `channel` )
) ENGINE = InnoDB;
 * 
 */
public class TopicManagerDAO extends DataAccessObject{
	
	public TopicManagerDAO(Bot myBot) {
		super(myBot);
	}

	@Override
	public void createTables() {
		if (!tableExists("channel_topics")) {
			try {
				Statement statement = getConnection().createStatement();
				statement.execute("CREATE TABLE `channel_topics` (`channel` VARCHAR( 20 ) NOT NULL ,`setBy` VARCHAR( 20 ) NOT NULL, `website_trim` VARCHAR( 400 ) NOT NULL ,PRIMARY KEY ( `channel` )) ENGINE = InnoDB;");
			} catch (SQLException e) {
				_myBot.getLogger().error("createTables",e);
			}
		}
		
	}

	@Override
	public void dropTables() {
		try {
			Statement statement = getConnection().createStatement();
			statement.execute("DROP TABLE IF EXISTS channel_topics;");
		} catch (SQLException e) {
			_myBot.getLogger().error("dropTables",e);
		}
	}

	/**
	 * This method will update the channel_topics table when a topic has changed
	 * @param message
	 */
	public void updateTopicDatabase(TopicEvent topic){
		String fullTopic = topic.getTopic();
		String channel = topic.getChannel().getName();
		String setBy = "";
		if(topic.getUser().getNick().contains("!"))
			setBy = topic.getUser().getNick().substring(0, topic.getUser().getNick().indexOf("!"));
		else
			setBy = topic.getUser().getNick();
		
		String trimTopic = Colors.removeFormattingAndColors(fullTopic.substring(fullTopic.lastIndexOf("[")+1, fullTopic.lastIndexOf("]")));

	   try {
		   
		   PreparedStatement stmt = getConnection().prepareStatement("INSERT INTO channel_topics(channel,setBy,website_trim) values (?,?,?) ON DUPLICATE KEY UPDATE setBy=?, website_trim=?");
		   stmt.setString(1, channel);
		   stmt.setString(2, setBy);
		   stmt.setString(3, trimTopic);
		   stmt.setString(4, setBy);
		   stmt.setString(5, trimTopic);
		   
		   stmt.executeUpdate();

		
		} catch (SQLException e1) {
			_myBot.getLogger().error("TopicManager died inserting new topic",e1);
		}
	}
}
