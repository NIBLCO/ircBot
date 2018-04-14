package com.nibl.bot.plugins.updatepacklist;

import java.sql.Timestamp;
import java.util.List;

import com.nibl.bot.Bot;

public class NewDistroBot extends HttpDistroBot {

	public NewDistroBot(Bot bot, int id, String name, String url, String type, int statusId, Timestamp lastSeen, Timestamp lastProcessed, List<Pack> listing, int informative, int external, String owner, int parserId) {
		super(bot, id, name, url, type, statusId, lastSeen, lastProcessed, listing, informative, external, owner, parserId);
	}

	@Override
	public void getPackListFromBot() {
		_myBot.sendMessageFair(getName(), "xdcc list");
		_myBot.getLogger().info(_updatePackListDAO.getUpdatePackList().getPrefix() + " Get list from " + getName());
	}
	
}
