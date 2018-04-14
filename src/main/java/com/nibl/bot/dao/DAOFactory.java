package com.nibl.bot.dao;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;

import com.nibl.bot.Bot;
import com.nibl.bot.BotExtend;

public class DAOFactory extends BotExtend {
	
	Map<String, DataAccessObject> _registeredDAO;

	public DAOFactory(Bot myBot) {
		super(myBot);
		_registeredDAO = register();
	}

	public Map<String, DataAccessObject> getRegisteredDOA() {
		return _registeredDAO;
	}

	private Map<String, DataAccessObject> register() {
		Map<String, DataAccessObject> result = new HashMap<String, DataAccessObject>();

		Reflections reflections = new Reflections("com.nibl.bot.plugins");
		
		 Set<Class<? extends DataAccessObject>> allClasses = 
		         reflections.getSubTypesOf(DataAccessObject.class);
		try {
		    
		    for(Class<? extends DataAccessObject> dao : allClasses ) {
		        String simpleClassName = StringUtils.substringAfterLast(dao.getName(), ".");
                _myBot.getLogger().info("Class " + simpleClassName + " Loading.");
                @SuppressWarnings("unchecked")
                Constructor<DataAccessObject> constructor = (Constructor<DataAccessObject>) dao.getConstructor(new Class[] { Bot.class });
                DataAccessObject dataAccessObject = constructor.newInstance(_myBot);
                result.put(simpleClassName, dataAccessObject);
		    }
		    
		} catch (Exception e) {
			_myBot.getLogger().error("You goofed loading the damn classes.",e);
		}

		return result;
	}

	public DataAccessObject getDAO(String className) {
		return _registeredDAO.get(className);
	}

	public static void main(String... strings) {
		System.out.println("done");
	}

}
