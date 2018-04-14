package com.nibl.bot.plugins.topicmanager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;

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

public class TopicManager extends Service {
	
	TopicManagerDAO _topicManagerDAO;
	String _adminHelp = null;
	
	public TopicManager(Bot myBot) {
		super(myBot);
		_topicManagerDAO = (TopicManagerDAO) myBot.getDAOFactory().getDAO("TopicManagerDAO");
	}

	@Override
	public void execute() {
		waitUntilStopped();
	}

	@Override
	public void onNotice(NoticeEvent notice) {
	}
	
	@Override
	public LinkedList<String> adminHelp() {
		LinkedList<String> temp = new LinkedList<String>();
		temp.add("This parses and stores the channel topic\r\n");
		return temp;
		/*sb.insert(0,getPrefix() + "This parses and stores the channel topic\r\n");
		_adminHelp = sb.toString();
		return _adminHelp;*/

	}

	@Override
	public String getDescription() {
		return "The topic manager service";
	}

	@Override
	public String getName() {
		return "topic_manager";
	}

	@Override
	public boolean needsMessages() {
		return false;
	}

	@Override
	public boolean needsNotices() {
		return true;
	}

	@Override
	public boolean needsIncomingFileTransfers() {
		return false;
	}

	@Override
	public void onMessage(MessageEvent message) {}

	@Override
	public boolean needsOnPart() {
		return false;
	}

	@Override
	public void onPart(String nick) {}

	@Override
	public boolean needsOnMode() {
		return false;
	}

	@Override
	public int getAccessLevel() {
		return 9;
	}

	@Override
	public int delay() {
		return 0;		
	}

	@Override
	public boolean needsOnHop() {
		return false;
	}

	@Override
	public void onHalfOp(HalfOpEvent event) {}

	@Override
	public void onMode(ModeEvent mode) {}

	@Override
	public boolean needsOnTopic() {
		return true;
	}

	@Override
	public void onTopic(TopicEvent topic) {
		_topicManagerDAO.updateTopicDatabase(topic);
	}

	@Override
	public boolean needsOnJoin() {
		return false;
	}

	@Override
	public void onJoin(String nick) {}

	@Override
	public void admin(SendChat session, User user, String message) throws IOException, SQLException {}

	@Override
	public void onIncomingFileTransfer(IncomingFileTransferEvent transfer) {}
}
