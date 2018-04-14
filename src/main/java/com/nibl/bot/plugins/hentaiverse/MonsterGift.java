package com.nibl.bot.plugins.hentaiverse;

import java.util.HashMap;

public class MonsterGift {

	String monsterName;
	HashMap<String,Integer> gifts;
	
	public MonsterGift(){
		gifts = new HashMap<String,Integer>();
	}

	public String getMonsterName() {
		return monsterName;
	}

	public void setMonsterName(String monsterName) {
		this.monsterName = monsterName;
	}

	public void addGift(String gift) {
		if( this.gifts.containsKey(gift) ){
			this.gifts.put(gift, this.gifts.get(gift)+1);
		} else {
			this.gifts.put(gift, 1);
		}
	}
	
	public HashMap<String,Integer> getGifts(){
		return this.gifts;
	}
}
