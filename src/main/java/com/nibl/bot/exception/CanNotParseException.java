package com.nibl.bot.exception;

public class CanNotParseException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public CanNotParseException(String message) 
	{
		super(message);
	}

	public CanNotParseException(Throwable throwable) 
	{
		super(throwable);
	}

}
