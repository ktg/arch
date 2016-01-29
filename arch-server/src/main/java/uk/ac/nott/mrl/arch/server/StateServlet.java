/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Servlet Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloWorld
*/

package uk.ac.nott.mrl.arch.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class StateServlet extends HttpServlet
{
	private static final Logger logger = Logger.getLogger("");

	private List<String> approach = new ArrayList<>();
	private List<String> leave = new ArrayList<>();

	private final Gson gson;
	private final Random random = new Random();

	public StateServlet()
	{
		gson = new GsonBuilder().create();
		try
		{
			approach = gson.fromJson(new InputStreamReader(StateServlet.class.getResource("/approach.json").openStream()), new TypeToken<List<String>>()
			{
			}.getType());
			leave = gson.fromJson(new InputStreamReader(StateServlet.class.getResource("/leave.json").openStream()), new TypeToken<List<String>>()
			{
			}.getType());
		}
		catch (Exception e)
		{
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	@Override
	public void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException
	{
		if (ItemServlet.current == null)
		{
			ItemServlet.current = new Item(Item.CURRENT_ITEM, new ArrayList<String>());
		}

		final String stateString = req.getParameter("state");
		if (stateString != null)
		{
			logger.info("State: " + stateString);
			final Item.State state = Item.State.valueOf(stateString);
			if (state != null)
			{
				if (ItemServlet.current.getState() == Item.State.leaving || (ItemServlet.current.getState() == Item.State.under && state != Item.State.leaving))
				{
					ItemServlet.current.setId(UUID.randomUUID().toString());
					DataStore.save().entity(ItemServlet.current).now();

					ItemServlet.current = ItemServlet.next;
					if (ItemServlet.current == null)
					{
						ItemServlet.current = new Item(Item.CURRENT_ITEM, new ArrayList<String>());
					}
					else
					{
						ItemServlet.current.setId(Item.CURRENT_ITEM);
					}

					DataStore.get().delete().type(Item.class).id(Item.NEXT_ITEM).now();

					// TODO Upload to Facebook here
				}

				if (state == Item.State.engagement)
				{
					int index = random.nextInt(approach.size());
					ItemServlet.current.setApproach(approach.get(index));

					index = random.nextInt(leave.size());
					ItemServlet.current.setLeave(leave.get(index));
				}

				ItemServlet.current.setState(state);
			}
		}

		final String directionString = req.getParameter("direction");
		if (directionString != null)
		{
			final Item.Direction direction = Item.Direction.valueOf(directionString);
			if (direction != null)
			{
				ItemServlet.current.setDirection(direction);
			}
		}

		final String height = req.getParameter("height");
		if (height != null)
		{
			logger.info("height: " + height);
			try
			{
				int heightCM = Integer.parseInt(height);

				int feetPart = (int) Math.floor((heightCM / 2.54) / 12);
				int inchesPart = (int) Math.floor((heightCM / 2.54) - (feetPart * 12));
				ItemServlet.current.setHeight(String.format("%d' %d\"", feetPart, inchesPart));
				logger.info("height: " + ItemServlet.current.getHeight());
			}
			catch (Exception e)
			{
				logger.log(Level.WARNING, e.getMessage(), e);
			}
		}

		//DataStore.save().entity(item).now();

		logger.info(gson.toJson(ItemServlet.current));


		resp.addDateHeader("Last-Modified", ItemServlet.current.getTimestamp().getTime());
		resp.setCharacterEncoding("UTF-8");
		resp.getWriter().print(gson.toJson(ItemServlet.current));
	}
}