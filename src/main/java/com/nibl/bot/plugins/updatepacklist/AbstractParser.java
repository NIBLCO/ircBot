package com.nibl.bot.plugins.updatepacklist;

import java.io.RandomAccessFile;
import java.util.LinkedList;

public abstract class AbstractParser {
	public abstract LinkedList<Pack> parse(AbstractDistroBot bot, RandomAccessFile in) throws Exception; 
}
