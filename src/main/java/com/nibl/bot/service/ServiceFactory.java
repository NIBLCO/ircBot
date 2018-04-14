package com.nibl.bot.service;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;

import com.nibl.bot.Bot;
import com.nibl.bot.BotExtend;

public class ServiceFactory extends BotExtend {
	
	Map<String, Service> _registeredService = new HashMap<String, Service>();

	public ServiceFactory(Bot myBot) {
		super(myBot);
		_registeredService = register();
	}
	
	public Service findRegisteredService(String sname) {
		return _registeredService.get(sname);
	}
	
	private Map<String, Service> register() {
		Map<String, Service> result = new HashMap<String, Service>();
		
        Reflections reflections = new Reflections("com.nibl.bot.plugins");
        
        Set<Class<? extends Service>> allClasses = 
                reflections.getSubTypesOf(Service.class);
        
		try {
		    for(Class<? extends Service> service : allClasses ) {
		        String simpleClassName = StringUtils.substringAfterLast(service.getName(), ".");
                @SuppressWarnings("unchecked")
                Constructor<Service> constructor = (Constructor<Service>) service.getConstructor(new Class[] { Bot.class });
                Service svc = constructor.newInstance(_myBot);
                result.put(simpleClassName, svc);
		    }
		} catch (Exception e) {
			_myBot.getLogger().error("You goofed loading the damn classes.", e);
		}

		return result;
	}

	public Map<String, Service> getRegisteredService() {
		return _registeredService;
	}
}
