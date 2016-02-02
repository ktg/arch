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
	private static final Gson gson = new GsonBuilder().create();

	@Override
	public void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException
	{
		resp.setContentType("application/json");
		if (Item.current == null)
		{
			Item.current = new Item();
		}

		resp.addDateHeader("Last-Modified", Item.current.getTimestamp().getTime());
		resp.setCharacterEncoding("UTF-8");
		resp.getWriter().print(gson.toJson(Item.current));
	}

	@Override
	public void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException
	{
		final List<String> values = new ArrayList<>();
		final Map<String, String[]> parameterNames = req.getParameterMap();
		final List<String> names = new ArrayList<>(parameterNames.keySet());
		Collections.sort(names);
		for (String parameter : names)
		{
			String value = req.getParameter(parameter);
			if (value != null)
			{
				values.add(value);
			}
		}

		if (Item.current == null)
		{
			Item.current = new Item(values);
		}
		//else if(item.getState() == Item.State.leaving || item.getState() == Item.State.under)
		//{
		//	next = new Item(values);
		//}
		else
		{
			Item.current.setData(values);
		}

		resp.setContentType("application/json");
		resp.getWriter().print(gson.toJson(Item.current));
	}
}