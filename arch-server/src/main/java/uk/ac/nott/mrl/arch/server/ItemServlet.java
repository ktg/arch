/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Servlet Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloWorld
*/

package uk.ac.nott.mrl.arch.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.googlecode.objectify.Work;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ItemServlet extends HttpServlet
{
	private static final Logger logger = Logger.getLogger("");
	private final Gson gson;

	public ItemServlet()
	{
		this.gson = new GsonBuilder().create();
	}

	@Override
	public void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException
	{
		Item item = DataStore.load().type(Item.class).id(Item.CURRENT_ITEM).now();
		final long modifiedSince = req.getDateHeader("If-Modified-Since");
		if(modifiedSince != -1)
		{
			if(item != null)
			{
				// Round to second accuracy
				long timestamp = item.getTimestamp().getTime() / 10000;
				timestamp = timestamp * 10000;
				if(timestamp <= modifiedSince)
				{
					resp.setStatus(304);
					return;
				}
			}
			else
			{
				resp.setStatus(304);
			}
		}

		resp.setContentType("application/json");
		if (item == null)
		{
			item = new Item();
		}

		resp.addDateHeader("Last-Modified", item.getTimestamp().getTime());
		resp.setCharacterEncoding("UTF-8");
		resp.getWriter().print(gson.toJson(item));
	}

	@Override
	public void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException
	{
		final List<String> values = new ArrayList<>();
		final Map<String, String[]> parameterNames = req.getParameterMap();
		final List<String> names = new ArrayList<>(parameterNames.keySet());
		Collections.sort(names);
		for(String parameter: names)
		{
			String value = req.getParameter(parameter);
			if (value != null)
			{
				values.add(value);
			}
		}

		final Item item = DataStore.get().transact(new Work<Item>()
		{
			@Override
			public Item run()
			{
				Item item = DataStore.load().type(Item.class).id(Item.CURRENT_ITEM).now();
				if(item == null)
				{
					item = new Item(Item.CURRENT_ITEM, values);
				}
				//else if(item.getState() == Item.State.leaving || item.getState() == Item.State.under)
				//{
				//	item = new Item(Item.NEXT_ITEM, values);
				//}
				else
				{
					item.setData(values);
				}

				DataStore.save().entities(item);

				return item;
			}
		});

		resp.setContentType("application/json");
		resp.getWriter().print(gson.toJson(item));
	}
}