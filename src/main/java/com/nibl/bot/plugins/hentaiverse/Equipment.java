package com.nibl.bot.plugins.hentaiverse;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.pircbotx.Channel;
import org.pircbotx.Colors;

import com.nibl.bot.Bot;

import net.htmlparser.jericho.Attributes;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTagType;

public class Equipment {

	private Source _source = null;
	private String _title = null;
	private TreeMap<String, EquipmentAttribute> _attributes = new TreeMap<String, EquipmentAttribute>();
	private ArrayList<EquipmentProc> _procs = new ArrayList<EquipmentProc>();
	private String _eid = null;
	private String _key = null;
	private String _type = null;
	private String _subType = null;
	private String _quality = null;
	private String _suffix = null;
	private String _prefix = null;
	private String _level = null;
	private Boolean _isWeapon = false;
	private EquipmentAttribute _weaponDamage = null;
	private String _weaponDamageString = null;
	private Boolean _isNewLotteryItem = false;
	
	public Equipment(){

	}
	
	public void setSource(Source source) {
        this._source = source;
        this.setTitle(); // set the title
        this.setLevel(); // set the level
        this.setAttributes(); // set the attributes	    
	}
	
	public void setIsNewLotteryItem(Boolean isNewLotteryItem){
		this._isNewLotteryItem = isNewLotteryItem;
	}
	
	public Boolean getIsNewLotteryItem() {
		return this._isNewLotteryItem;
	}
	
	public void setTitle(){
		String titleText = "";
		List<Element> titleElems = this._source.getElementById("showequip").getChildElements().get(0).getAllElementsByClass("fc4");
		for( Element title : titleElems ){
			titleText += " " + HentaiVerse.extractValue( title );
		}
		this._title = titleText;
	}
	
	public void setLotteryTitle(){
		List<Element> titleElems = this._source.getElementById("leftpane").getChildElements();
		this._title = HentaiVerse.extractValue( titleElems.get(1) );
	}
	
	public String getTitle(){
		return this._title;
	}
	
	public void setLevel(){
		String levelText = HentaiVerse.extractValue( this._source.getElementById("equip_extended").getChildElements().get(0).getChildElements().get(0).getContent() );
		
        if( levelText.toLowerCase().contains(HentaiVerse.SOULBOUND.toLowerCase()) ){
            levelText = HentaiVerse.SOULBOUND;
        } else {
            levelText = levelText.replaceAll("[^0-9]", "");
        }
        
		this._level = levelText;
	}
	
	public void setLevel(String level){
		this._level = level;
	}
	
	public String getLevel(){
		return this._level;
	}
	
	public String getSubType(){
		if( this._subType != null ){
			return this._subType;
		}
		
		if( HentaiVerse.equipTypes.get(this.getType()) ){
			String title = this.getTitle();
			for(String subTypeTemp : HentaiVerse.equipSubTypes) {
				if( title.contains(subTypeTemp) ){
					this._subType = subTypeTemp;
					break;
				}
			}
		}
		
		if( this._subType == null )
			return "";
		else
			return this._subType;
		
	}
	
	public String getType(){
		if( this._type != null ){
			return this._type;
		}
		
		String title = this.getTitle();
		
		for(Map.Entry<String,Boolean> entry : HentaiVerse.equipTypes.entrySet()) {
			if( title.contains(entry.getKey()) ){
				this._type = entry.getKey();
				break;
			}
		}
		
		return this._type;
	}
	
	public String getPrefix(){
		if( this._prefix != null ){
			return this._prefix;
		}
		
		this._prefix = "";
		
		String title = this.getTitle();
		
		for(String prefix : HentaiVerse.equipPrefix) {
			if( title.toLowerCase().contains(prefix.toLowerCase()) ){
				this._prefix = prefix;
				break;
			}
		}
		
		return this._prefix;
		
	}
	
	public String getSuffix(){
		if( this._suffix != null ){
			return this._suffix;
		}
		
		this._suffix = "";
		
		String title = this.getTitle();
		
		for(String suffix : HentaiVerse.equipSuffix) {
			if( title.toLowerCase().contains(suffix.toLowerCase()) ){
				this._suffix = suffix;
				break;
			}
		}
		
		return this._suffix;
		
	}
	
	public String getQuality(){
		if( this._quality != null ){
			return this._quality;
		}
		
		String title = this.getTitle();
		
		for(String qual : HentaiVerse.equipQuality) {
			if( title.toLowerCase().contains(qual.toLowerCase()) ){
				this._quality = qual;
				break;
			}
		}
		
		return this._quality;
		
	}
	
	// Determine if item can be stored.
	public Boolean canStore(){
		if( HentaiVerse._hentaiverseDAO.equipmentExists(this) ){
			return false;
		}
		
		if( null != this._source.getElementById("eu") || null != this._source.getElementById("ep") ) {
			return false;
		}
		
		return true;
	}
	
	/*
	 * Parse out PXP0
	 * This number is only valid if the canStore() function returns true 
	 */
	public Integer parsePXP0(){
		try {
			String[] temp = this.getConditionAndPotencyString().split(" ");
			return Integer.parseInt(temp[temp.length-1].replaceAll("[^\\d]", ""));
		} catch (Exception e){
			return -1;
		}
	}
	
	/*
	private String getActualAttribute(String attribString){
		return attribString.replaceAll(" ", "_").replaceAll("[\\W\\d]", "").replaceAll(":|-|turns|DOT|/|chance|turn|points", "").replaceAll("_", " ").trim();
	}*/
	
	private String getBaseStat(Element base){
		String elem = base.getAttributeValue("title");
		if( elem != null ){
			elem = elem.replace("Base:", "").trim();
		}
		return elem;
	}
	
	private void setAttributes(){
		// class e3 item attributes
		//  - Only use items with a title
		//  - Take base stat from title
		//  - Take name from class 'ek'
	    
		// List<Element> top = this._source.getElementById("equip_extended").getChildElements().get(0).getChildElements();
	    List<Element> top = this._source.getElementById("equip_extended").getAllElementsByClass("ex");
	    top.addAll(this._source.getElementById("equip_extended").getAllElementsByClass("ep"));
	    
		if ( top.size() != 0 ) {
			for( Element attributeList : top ){
				for( Element attribute : attributeList.getChildElements() ){
					if( null == attribute.getAttributeValue("title") ){ // only use elements with a title
						continue;
					}
					
					String appender = "";
					// Do not use appender if the first element is a stat/has a title
					if( null == attribute.getParentElement().getChildElements().get(0).getAttributeValue("title") ) {
						if( HentaiVerse.extractValue( attribute.getParentElement().getChildElements().get(0) ).toLowerCase().contains("mitigation") ) {
						    appender = " MIT";
						} else if ( HentaiVerse.extractValue( attribute.getParentElement().getChildElements().get(0) ).toLowerCase().contains("spell damage") ) {
						    appender = " EDB";
						} else if ( HentaiVerse.extractValue( attribute.getParentElement().getChildElements().get(0) ).toLowerCase().contains("proficiency") ) {
							appender = " PROF";
						}
					}
					
					String baseStat = getBaseStat( attribute ); // Take base stat from title
					// String attributeTitle = StringUtils.substringBeforeLast(StringUtils.substringBeforeLast(HentaiVerse.extractValue( attribute ), "+"), "-").trim() + appender; // Take attribute name
					String attributeTitle = StringUtils.substringBeforeLast(HentaiVerse.extractValue( attribute ), "+").trim() + appender; // Take attribute name
					
					String percentText = ( HentaiVerse.extractValue( attribute ).contains("%") ) ? "%" : "";
					
					this._attributes.put(attributeTitle.toLowerCase(), new EquipmentAttribute(attributeTitle, baseStat, percentText));
				}
			}
		}
		
		// If class 'es' or 'et' exists, this is a weapon
		if ( testIsWeapon() ) {
		    _isWeapon = true;
		    top = this._source.getElementById("equip_extended").getChildElements().get(0).getChildElements();
		    
		    Element procChance = top.get(2);
		    Element procChance2 = null;
		    Element weaponDamage = null;
		    
		    // Check for 2nd proc chance
		    if(  HentaiVerse.extractValue( top.get(3) ).toLowerCase().contains("chance") ) {
		    	procChance2 = top.get(3);
		    	weaponDamage = top.get(4);
		    } else {
		    	weaponDamage = top.get(3);
		    }
		    
		    
		    /*
		    Element strike = top.get(0);
		    if( null != strike.getStartTag().getAttributeValue("class") && strike.getStartTag().getAttributeValue("class").toLowerCase().equals("ex") ) {
		        strike = null;
		    }*/
		    
		    if( null != procChance ) {
		        String shortAttributeTitle = HentaiVerse.extractValue( procChance.getChildElements().get(0) );
		        String procChanceString = HentaiVerse.extractValue( procChance );
                String[] content = procChanceString.replace(shortAttributeTitle, "").replaceAll("[^\\d%\\.\\s]+", "").trim().replaceAll("\\s+", " ").split(" ");
                if( content != null && content.length >= 2 ){
                    
                    EquipmentProc proc = new EquipmentProc(shortAttributeTitle);
                    proc.setChance( content[0] );
                    if( procChanceString.toLowerCase().contains("points") ){
                        proc.setPoints( content[1] );
                    }else{
                        proc.setTurns( content[1] );
                    }
                    
                    if( content.length >= 3 ) {
                        proc.setDot( content[2] );
                    }
                    
                    _procs.add(proc);
                    
                }
		    }
		    
		    if( null != procChance2 ) {
		        String shortAttributeTitle = HentaiVerse.extractValue( procChance2.getChildElements().get(0) );
		        String procChanceString = HentaiVerse.extractValue( procChance2 );
                String[] content = procChanceString.replace(shortAttributeTitle, "").replaceAll("[^\\d%\\.\\s]+", "").trim().replaceAll("\\s+", " ").split(" ");
                if( content != null && content.length >= 2 ){
                    
                    EquipmentProc proc = new EquipmentProc(shortAttributeTitle);
                    proc.setChance( content[0] );
                    if( procChanceString.toLowerCase().contains("points") ){
                        proc.setPoints( content[1] );
                    }else{
                        proc.setTurns( content[1] );
                    }
                    
                    if( content.length >= 3 ) {
                        proc.setDot( content[2] );
                    }
                    
                    _procs.add(proc);
                    
                }
		    }
		    
		    
		    if( null != weaponDamage ) {
		        String weaponDamageString = HentaiVerse.extractValue( weaponDamage );
		        _weaponDamageString = weaponDamageString.split(" ")[1];
		        _weaponDamage = new EquipmentAttribute( weaponDamageString.split(" ")[1], getBaseStat( weaponDamage ), "");
		    }
		    /*
		    if( null != strike ) {
		        this.
		    }*/
		    
		}
	}
	
	public TreeMap<String, EquipmentAttribute> getAttributes(){
		if( this._attributes.size() != 0 ){
			return this._attributes;
		}
		return null;
	}
	
	public ArrayList<EquipmentProc> getProcs(){
		if( this._procs.size() != 0 ){
			return this._procs;
		}
		return null;
	}
	
	public EquipmentAttribute getAttribute(String attributeTitle){
		if( this._attributes.containsKey(attributeTitle.toLowerCase()) )
			return this._attributes.get(attributeTitle);
		return null;
	}
	
	public String getItemTypeAndLevel(){
	    return HentaiVerse.extractValue( this._source.getElementById("equip_extended").getChildElements().get(0).getChildElements().get(0) );
	}
	
	public String getConditionAndPotencyString(){
	    return HentaiVerse.extractValue( this._source.getElementById("equip_extended").getChildElements().get(0).getChildElements().get(1) );
	}
	
	public Boolean testIsWeapon(){
		String weaponType = HentaiVerse.extractValue( this._source.getElementById("equip_extended").getChildElements().get(0).getChildElements().get(0) ).toLowerCase();
		return (weaponType.contains("weapon") || weaponType.contains("staff"));
	}
	
	public String getOwner(){
		List<Element> elems = this._source.getElementById("showequip").getChildElements();
		return HentaiVerse.extractValue( elems.get(elems.size()-1) );
	}

	public void setEID(String eid){
		this._eid = eid;
	}
	
	public String getEID(){
		return this._eid;
	}
	
	public void setKEY(String key){
		this._key = key;
	}
	
	public String getKEY(){
		return this._key;
	}
	
	public void sendEquip(Bot myBot, Channel channel){
		
		StringBuilder equipOut = new StringBuilder();
		DecimalFormat df = new DecimalFormat("0.00");
		//DecimalFormat df2 = new DecimalFormat("0.#");
		LinkedHashMap<String, StatPriority> statPriorities = HentaiVerse._hentaiverseDAO.getStatPriority();
		Set<String> attributeKeys = this.getAttributes().keySet();
		
		myBot.sendMessageFair(channel.getName(), this.getTitle().trim() + "; " + this.getItemTypeAndLevel().trim().replaceAll(" +", " ") );
		
		if( isWeapon() ){
			StringBuilder damageString = new StringBuilder();
			damageString.append(_weaponDamage.getTitle() + ": " + Colors.NORMAL + df.format(_weaponDamage.getValue()) + " ");
			
			StatPriority statPriority = statPriorities.get("magic damage");
			EquipmentAttribute attribute = this.getAttribute( statPriority.getName() );
			if( attribute != null ){
				attributeKeys.remove(statPriority.getName());
				damageString.append(Colors.MAGENTA + attribute.getTitle() + ": " + Colors.NORMAL + attribute.getValue() + " ");
			}
			
			
			for(EquipmentProc proc : _procs ){
				damageString.append( proc.toString() );
			}
			
			myBot.sendMessageFair(channel.getName(), Colors.MAGENTA + damageString.toString().trim() );
		}
		
		for(StatPriority statPriority : statPriorities.values()){
			if( statPriority.isLinebreak() && equipOut.toString().length() > 0 ){
				myBot.sendMessageFair(channel.getName(), equipOut.toString());
				equipOut = new StringBuilder();
			}
			
			EquipmentAttribute attribute = this.getAttribute( statPriority.getName() );
			if( attribute == null )
				continue;
			attributeKeys.remove(statPriority.getName());
			equipOut.append(statPriority.getColor() + attribute.getTitle() + ": " + Colors.NORMAL + df.format(attribute.getValue()) + attribute.getPercent() + " ");
			
		}
		
		
		for(String attributeKey : attributeKeys){
			EquipmentAttribute attribute = this.getAttribute( attributeKey );
			if( attribute == null )
				continue;
			equipOut.append(Colors.BLUE + attribute.getTitle() + ": " + Colors.NORMAL + df.format(attribute.getValue()) + attribute.getPercent() + " ");
		}
		
		myBot.sendMessageFair(channel.getName(), equipOut.toString());
		
		myBot.sendMessageFair(channel.getName(), Colors.LIGHT_GRAY + this.getOwner());
		
	}
	
	public Boolean isWeapon(){
		return this._isWeapon;
	}
	
	public EquipmentAttribute getWeaponDamage(){
		return this._weaponDamage;
	}
	
	public String getWeaponDamageString(){
		return this._weaponDamageString;
	}
	
}