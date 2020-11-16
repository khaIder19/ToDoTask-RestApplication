package com.todotask.rest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.hibernate.type.descriptor.java.JavaTypeDescriptorRegistry;

import com.todotask.AdjusterTypeDesc;
import com.todotask.ConstraintTypeDesc;

@WebListener
public class ServletBootListener implements ServletContextListener {

	private static Properties p;
	
	public static final String ENV_START_UP_PROPS ="env.var.prop.startup";
	public static final String LOGGER_CONFIG_PATH ="log.config.path";
	
	static {
		p = new Properties();
		try {
			p.load(new FileReader(System.getenv(ENV_START_UP_PROPS)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContextListener.super.contextInitialized(sce);
				
		try {
			System.out.println("logger config :"+p.getProperty(LOGGER_CONFIG_PATH));
			Properties logConfigProp = new Properties();
			logConfigProp.load(new FileInputStream(p.getProperty(LOGGER_CONFIG_PATH)));
			PropertyConfigurator.configure(logConfigProp);
		}  catch (IOException e) {
			e.printStackTrace();
		}
		
		Logger log = Logger.getLogger(ServletBootListener.class);
		
		log.info("Servlet initialization");
		
		JavaTypeDescriptorRegistry.INSTANCE.addDescriptor(new AdjusterTypeDesc());
        JavaTypeDescriptorRegistry.INSTANCE.addDescriptor(new ConstraintTypeDesc());
        
        
		
        log.info("Servlet initialization completed");
	}
}
