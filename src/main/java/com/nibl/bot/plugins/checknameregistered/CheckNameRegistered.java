package com.nibl.bot.plugins.checknameregistered;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

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

//******************************************
// This class is depreciated. Use user.isVerified()
//******************************************


public class CheckNameRegistered extends Service {
	
	BlockingQueue<KeyName> _namesToCheck = new LinkedBlockingQueue<KeyName>();
	ConcurrentHashMap<KeyName,Boolean> _results = new ConcurrentHashMap<KeyName, Boolean>();
	String _adminHelp = null;
	CheckNameRegisteredDAO _checkNameRegisteredDAO;
	
	public CheckNameRegistered(Bot myBot) {
		super(myBot);
		_checkNameRegisteredDAO = (CheckNameRegisteredDAO) _myBot.getDAOFactory().getDAO("CheckNameRegisteredDAO");
	}

	@Override
	public void execute() {
		try {
			KeyName keyName = _namesToCheck.take();
			_results.put(keyName, false);
			_myBot.sendMessageFair("nickserv", " info " + keyName._name);

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
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
		return true;
	}
	
	@Override
	public void onNotice(NoticeEvent notice) {
		if(notice.getUser().getNick().toLowerCase().equals("nickserv"))
		{
			for(KeyName keyName : _results.keySet())
			{
				String name = keyName._name.toLowerCase();
				String message = notice.getMessage().toLowerCase();
				if (message.startsWith(name+" is"))
				{
					_results.put(keyName, true);
					keyName._set = true;
					synchronized(keyName)
					{
						keyName.notifyAll();
					}
				}
				else if (message.startsWith("nick "+name))
				{
					_results.put(keyName, false);
					keyName._set = true;
					synchronized(keyName)
					{
						keyName.notifyAll();
					}
				}
			}
		}
	}

	@Override
	public void admin(SendChat session, User user, String message) throws IOException {
		
	}

	@Override
	public LinkedList<String> adminHelp() {
		LinkedList<String> temp = new LinkedList<String>();
		temp.add("This just checks if names are registered\r\n");
		return temp;
		/*sb.insert(0,getPrefix() + "This just checks if names are registered\r\n");
		_adminHelp = sb.toString();
		return _adminHelp;*/
	}

	@Override
	public String getDescription() {
		return "checks if submited names are registered";
	}

	@Override
	public String getName() {
		return "check_name_registered";
	}

	public boolean CheckUser(User user)
	{
		String key = user.getNick()+Thread.currentThread().getId();
		KeyName keyName = new KeyName(key,user.getNick());

		_namesToCheck.offer(keyName);
		
		try {
			synchronized(keyName){
				while(!keyName._set)
				{
					keyName.wait();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		boolean result = _results.get(keyName);
		_results.remove(keyName);
		return result;
	}
	
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
	public boolean needsOnTopic() {
		return false;
	}

	@Override
	public boolean needsOnJoin() {
		return false;
	}

	@Override
	public void onJoin(String nick) {}
	
	public Integer getUserAccessLevel(User user){
		return _checkNameRegisteredDAO.getUserAccessLevel(user);
	}

	@Override
	public void onTopic(TopicEvent topic) {}

	@Override
	public void onMessage(MessageEvent message) {}

	@Override
	public void onMode(ModeEvent mode) {}

	@Override
	public void onHalfOp(HalfOpEvent event) {}

	@Override
	public void onIncomingFileTransfer(IncomingFileTransferEvent transfer) {}
	
}
