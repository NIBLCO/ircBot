package com.nibl.bot.plugins.hentaiverse;

/*import java.io.BufferedReader;
import java.io.FileReader;*/
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;

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

public class MonsterCheck extends Service {

	HentaiVerseDAO _hentaiverseDAO;
	String _adminHelp = null;
	
	final long _timeInSeconds = Long.parseLong(_myBot.getProperty("hv_monster_time")); // 15 minutes
	
	public MonsterCheck(Bot myBot) {
		super(myBot);
		_hentaiverseDAO = (HentaiVerseDAO) myBot.getDAOFactory().getDAO("HentaiVerseDAO");
	}

	@Override
	public void execute() {
		long start = System.currentTimeMillis();
		try {
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
			_myBot.getLogger().error("Interrupted in Scheduler sleep");
		} catch (Exception e){
			_myBot.getLogger().error("Unknown Exception: " + e.toString());
		}
		
		_myBot.getLogger().info("Service " + getName() + " stopped.");
	}
	
	public void update(){
		try{
			ArrayList<MonsterGift> gifts = processMonsterGifts();
			//this.sendToMaster(gifts);
			_hentaiverseDAO.storeAllHVGifts(gifts);
			/*if( gifts.size() > 0 )
				_myBot.sendMessageFair("Jenga", "Gifts Stored");*/
		}catch(Exception e){
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			_myBot.sendMessageFair("Jenga", "Error getting monster gifts. " + sw.toString());
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
		temp.add("Jenga's personal touch me script.\r\n");
		temp.add("run - pull hv monster gifts");
		return temp;
	}

	@Override
	public String getDescription() {
		return "The Monster Parser";
	}

	@Override
	public String getName() {
		return "hv_monster";
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
	
	private ArrayList<MonsterGift> processMonsterGifts() throws Exception{		
		ArrayList<MonsterGift> monsterGifts = new ArrayList<MonsterGift>();
		String urlTxt = "https://hentaiverse.org/?s=Bazaar&ss=ml";
		_myBot.getLogger().trace("Get monster gifts");
		URL myUrl = new URL(urlTxt);
		String page = HentaiVerse.getHVURL(myUrl);
        /*
		StringBuffer fileData = new StringBuffer();
        BufferedReader reader = new BufferedReader(new FileReader("/home/rob/Desktop/hvtest.txt"));
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        String page = fileData.toString();
		*/
		/*
        MonsterGift monsterGift = null;
		Source source = new Source(page);
		Element messageBox = source.getElementById("messagebox");
		if ( null != messageBox ) {
			String textBlob = messageBox.getTextExtractor().toString();
			if( !textBlob.equals("") ){ // Have some stuff
				for(String data : textBlob.split("!|\\.") ){
					if( data.contains( " brought you a gift" ) ){
						if( null != monsterGift ){
							monsterGifts.add(monsterGift);
						}
						monsterGift = new MonsterGift();
						monsterGift.setMonsterName( data.replace(" brought you a gift", "").replace("System Message", "").trim());
						continue;
					}
					monsterGift.addGift(data.replace("Received a ", "").replace("Received some ", "").trim());
				}
				if( null != monsterGift ){ // add last monster
					monsterGifts.add(monsterGift);
				}
			}
		}
		*/
		return monsterGifts;
	}
	
	@SuppressWarnings("unused")
	private void sendToMaster(ArrayList<MonsterGift> monsterGifts){ // Send summation of items
 		HashMap<String,Integer> summary = new HashMap<String,Integer>();
		for( MonsterGift gift : monsterGifts ){
			Iterator<Entry<String, Integer>> it = gift.getGifts().entrySet().iterator();
			while(it.hasNext()){
                Map.Entry<String, Integer> pair = (Map.Entry<String, Integer>)it.next();
                if( summary.containsKey(pair.getKey()) ){
                	summary.put(pair.getKey(), pair.getValue()+summary.get(pair.getKey()));
                } else {
                	summary.put(pair.getKey(), pair.getValue());
                }
			}
		}
		Iterator<Entry<String, Integer>> it = summary.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, Integer> pair = (Map.Entry<String, Integer>)it.next();
			_myBot.sendMessageFair("Jenga", pair.getValue() + "x " + pair.getKey());
		}
	}

	@Override
	public void onIncomingFileTransfer(IncomingFileTransferEvent transfer) {}
}
