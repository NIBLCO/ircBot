package com.nibl.bot.plugins.hentaiverse;

import java.io.IOException;
import java.net.URL;
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

public class LotteryCheck extends Service {

	HentaiVerseDAO _hentaiverseDAO;
	String _adminHelp = null;
	
	private Integer exceptionCounter = 0;
	final long _timeInSeconds = Long.parseLong(_myBot.getProperty("hv_lottery_time")); // couple hours
	final String _channel;
	
	public LotteryCheck(Bot myBot) {
		super(myBot);
		_hentaiverseDAO = (HentaiVerseDAO) myBot.getDAOFactory().getDAO("HentaiVerseDAO");
		_channel = _myBot.getProperty("hv_lottery_channel");
	}
	
	

	@Override
	public void execute() {
		long start = System.currentTimeMillis();
		try {
			Thread.sleep(1000*20);
			update(); // Update on start
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
			_myBot.getLogger().error("Interrupted in Scheduler sleep", e);
		} catch (Exception e){
			_myBot.getLogger().error("Unknown Exception: " + e.toString(), e);
		}
		
		_myBot.getLogger().info("Service " + getName() + " stopped.");
	}
	
	public void update(){
		try {
			// Weapon Lottery
			URL myUrl = new URL("http://hentaiverse.org/?s=Bazaar&ss=lt");
			Equipment item = HentaiVerse.urlToEquipment(myUrl);
			if( item.getIsNewLotteryItem() ){
				_myBot.sendMessageFair("#"+_channel, "New Weapon Lottery");
				item.sendEquip(_myBot, _myBot.stringToChannel(_channel) );
			}
			
			// Armor Lottery
			myUrl = new URL("http://hentaiverse.org/?s=Bazaar&ss=la");
			item = HentaiVerse.urlToEquipment(myUrl);
			if( item.getIsNewLotteryItem() ){
				_myBot.sendMessageFair("#"+_channel, "New Armor Lottery");
				item.sendEquip(_myBot, _myBot.stringToChannel(_channel) );
			}
			exceptionCounter = 0;
		} catch (Exception e) {
			exceptionCounter++;
			if( exceptionCounter > 20 ){
				_myBot.sendMessageFair("Jenga", "Failed saving lottery " + exceptionCounter + " times.");
			}
		}
	}

	@Override
	public void onNotice(NoticeEvent notice) {
	}
	
	@Override
	public void admin(SendChat session, User user, String message) throws IOException {
		if (message.equals("run")) {
			update();
		}
	}

	@Override
	public LinkedList<String> adminHelp() {
		LinkedList<String> temp = new LinkedList<String>();
		temp.add("run - pull hv lotto");
		return temp;
	}

	@Override
	public String getDescription() {
		return "The Lotto Parser";
	}

	@Override
	public String getName() {
		return "hv_lotto";
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
		
	}

	@Override
	public boolean needsOnJoin() {
		return false;
	}

	@Override
	public void onJoin(String nick) {
		
	}

	@Override
	public void onIncomingFileTransfer(IncomingFileTransferEvent transfer) {}
}
