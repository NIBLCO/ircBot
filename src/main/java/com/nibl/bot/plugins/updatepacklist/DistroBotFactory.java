package com.nibl.bot.plugins.updatepacklist;

import java.sql.Timestamp;
import java.util.List;

import com.nibl.bot.Bot;

public class DistroBotFactory {
	public static AbstractDistroBot create(Bot bot, int id, String name, String url, String type, int statusId, Timestamp lastSeen, Timestamp lastProcessed, List<Pack> listing, int informative, int external, String owner, int parserId) {
		if(type.equals("NEW")){
			return new NewDistroBot(bot, id, name, url, type, statusId, lastSeen, lastProcessed, listing, informative, external, owner, 0);
		} else if (type.equals("XDCC"))
		{
			return new XdccDistroBot(bot, id, name, url, type, statusId, lastSeen, lastProcessed, listing, informative, external, owner, parserId);
		} else if (type.equals("JS"))
		{
			return new JSDistroBot(bot, id, name, url, type, statusId, lastSeen, lastProcessed, listing, informative, external, owner, parserId);
		} else if (type.equals("HTTP"))
		{
			return new HttpDistroBot(bot, id, name, url, type, statusId, lastSeen, lastProcessed, listing, informative, external, owner, parserId);
		}
		else 
			return new NewDistroBot(bot, id, name, url, type, statusId, lastSeen, lastProcessed, listing, informative, external, owner, parserId);
	}

	public static AbstractDistroBot create(Bot bot, UpdatePackListDAO updatePackListDAO, String name, String url, String type, List<Pack> listing, int informative, int external, String owner, int parserId) {
		int statusId = 1;
		Timestamp lastSeen = new Timestamp(System.currentTimeMillis());
		Timestamp lastProcessed = new Timestamp(System.currentTimeMillis());
		if(type.equals("NEW")){
			return new NewDistroBot(bot, 0, name, url, type, statusId, lastSeen, lastProcessed, listing, informative, external, owner, parserId);
		} else if (type.equals("XDCC"))
		{
			return new XdccDistroBot(bot, 0, name, url, type, statusId, lastSeen, lastProcessed, listing, informative, external, owner, parserId);
		} else if (type.equals("JS"))
		{
			return new JSDistroBot(bot, 0, name, url, type, statusId, lastSeen, lastProcessed, listing, informative, external, owner, parserId);
		} else if (type.equals("HTTP"))
		{
			return new HttpDistroBot(bot, 0, name, url, type, statusId, lastSeen, lastProcessed, listing, informative, external, owner, parserId);
		}
		else 
			return new NewDistroBot(bot, 0, name, url, type, statusId, lastSeen, lastProcessed, listing, informative, external, owner, parserId);
	}
}
