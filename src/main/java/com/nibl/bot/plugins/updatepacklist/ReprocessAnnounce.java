package com.nibl.bot.plugins.updatepacklist;

import com.nibl.bot.Bot;

public class ReprocessAnnounce implements Runnable {
	
	AbstractDistroBot _packlistBot;
	Bot _myBot;
	UpdatePackList _updatePackList;
	
	public ReprocessAnnounce(Bot myBot, UpdatePackList updatePackList, AbstractDistroBot packlistBot){
		_myBot = myBot;
		_packlistBot = packlistBot;
		_updatePackList = updatePackList;
	}
	
	@Override
	public void run() {
		if(!_updatePackList.isIterating()){
			_myBot.getLogger().trace(_updatePackList.getPrefix() + "Started reprocess for: " + _packlistBot.getName());
			_packlistBot.getPackListFromBot();
			_myBot.getLogger().trace(_updatePackList.getPrefix() + "Finished reprocess for: " + _packlistBot.getName());
		}
		_packlistBot.resetNumberOfAnnounces();
	}

	
}
