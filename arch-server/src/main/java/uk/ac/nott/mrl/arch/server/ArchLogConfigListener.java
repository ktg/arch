package uk.ac.nott.mrl.arch.server;

import java.util.logging.Handler;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ArchLogConfigListener implements ServletContextListener
{
	@Override
	public void contextInitialized(ServletContextEvent sce)
	{
		for (Handler handler : Logger.getLogger("").getHandlers())
		{
			handler.setFormatter(new ArchLogFormatter());
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce)
	{

	}
}