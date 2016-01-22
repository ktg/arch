/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Servlet Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloWorld
*/

package uk.ac.nott.mrl.arch.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ItemServlet extends HttpServlet
{
	private final Gson gson;

	public ItemServlet()
	{
		this.gson = new GsonBuilder().create();
	}

	@Override
	public void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException
	{
		final Item item = DataStore.load().type(Item.class).id(Item.RECENT_ID).now();
		resp.setContentType("application/json");
		if (item != null)
		{
			resp.getWriter().print(gson.toJson(item));
		}
		else
		{
			final Item responseItem = new Item();
			resp.getWriter().print(gson.toJson(responseItem));
		}
	}

	@Override
	public void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException
	{
		final List<String> values = new ArrayList<>();
		final Enumeration parameterNames = req.getParameterNames();
		while (parameterNames.hasMoreElements())
		{
			Object parameter = parameterNames.nextElement();
			if (parameter instanceof String)
			{
				String value = req.getParameter((String) parameter);
				if (value != null)
				{
					values.add(value);
				}
			}
		}
		final Item item = new Item(UUID.randomUUID().toString(), values);
		final Item recent = new Item(Item.RECENT_ID, values);
		DataStore.save().entities(recent, item).now();
		resp.setContentType("application/json");
		resp.getWriter().print(gson.toJson(item));
	}
}