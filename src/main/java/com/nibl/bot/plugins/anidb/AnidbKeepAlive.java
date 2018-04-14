package com.nibl.bot.plugins.anidb;

import java.io.IOException;
import java.util.HashMap;
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

public class AnidbKeepAlive extends Service {

	private HashMap<Integer,AnidbSeries> _anidbStore;
	private AnidbDAO _anidbDAO;
	public AnidbKeepAlive(Bot bot) {
		super(bot);
		_anidbStore = new HashMap<Integer,AnidbSeries>();
		_anidbDAO = (AnidbDAO) _myBot.getDAOFactory().getDAO("AnidbDAO");
	}

	public void addAnidbSeries(AnidbSeries series){
		_anidbStore.put(series.getAid(), series);	
	}
	
	public boolean anidbSeriesExists(Integer aid){
		return _anidbStore.containsKey(aid);
	}
	
	public AnidbSeries getAnidbSeries(Integer aid){
		return _anidbStore.get(aid);
	}
	
	@Override
	public void execute() {
		waitUntilStopped();
		_anidbStore.clear();
		_anidbDAO.dropTables();
		_anidbDAO.createTables();
	}

	@Override
	public int getAccessLevel() {
		return 0;
	}

	@Override
	public boolean needsIncomingFileTransfers() {
		return false;
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
	public boolean needsOnMode() {
		return false;
	}

	@Override
	public boolean needsOnPart() {
		return false;
	}
	
	@Override
	public void onIncomingFileTransfer(IncomingFileTransferEvent transfer) {}

	@Override
	public void onMessage(MessageEvent message) {}

	@Override
	public void onMode(ModeEvent mode) {}

	@Override
	public void onNotice(NoticeEvent notice) {}

	@Override
	public void onPart(String nick) {}

	@Override
	public void admin(SendChat session, User user, String message) throws IOException {}

	@Override
	public LinkedList<String> adminHelp() {
		return new LinkedList<String>();
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public String getName() {
		return "anidb_keep_alive";
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
	public boolean needsOnTopic() {
		return false;
	}

	@Override
	public void onTopic(TopicEvent topic) {}

	@Override
	public boolean needsOnJoin() {
		return false;
	}

	@Override
	public void onJoin(String nick) {}

}
