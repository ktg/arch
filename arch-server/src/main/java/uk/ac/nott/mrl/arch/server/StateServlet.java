/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Servlet Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloWorld
*/

package uk.ac.nott.mrl.arch.server;

import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class StateServlet extends HttpServlet
{
	private static final Logger logger = Logger.getLogger("Updates");
	private static final Gson gson = new GsonBuilder().create();
	private static final URLFetchService fetcher = URLFetchServiceFactory.getURLFetchService();

	private final Random random = new Random();
	private final List<String> approach;
	private final List<String> leave;
	private final Properties properties = new Properties();
	private URL facebookURL;

	public StateServlet()
	{
		approach = loadStrings("/approach.json");
		leave = loadStrings("/leave.json");
		try
		{

			properties.load(load("/facebook.properties"));

			facebookURL = new URL("https://graph.facebook.com/v2.5/" + properties.getProperty("page.id") + "/feed");
		}
		catch (Exception e)
		{
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	private static List<String> loadStrings(String file)
	{
		try
		{
			return gson.fromJson(new InputStreamReader(load(file), "UTF-8"), new TypeToken<List<String>>()
			{
			}.getType());
		}
		catch (IOException e)
		{
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		return new ArrayList<>();
	}

	private static InputStream load(String file) throws IOException
	{
		return StateServlet.class.getResource(file).openStream();
	}

	@Override
	public void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException
	{
		if (Item.current == null)
		{
			Item.current = new Item(new ArrayList<String>());
		}

		final String stateString = req.getParameter("state");
		if (stateString != null)
		{
			final Item.State state = Item.State.valueOf(stateString);
			if (state != null && state != Item.current.getState())
			{
				logger.info("State: " + state);
				if (Item.current.getState() == Item.State.leaving || (Item.current.getState() == Item.State.under && state != Item.State.leaving))
				{
					facebookPost(Item.current);

					Item.current = Item.next;
					if (Item.current == null)
					{
						Item.current = new Item(new ArrayList<String>());
					}
				}

				if (state == Item.State.engagement)
				{
					int index = random.nextInt(approach.size());
					Item.current.setApproach(approach.get(index).trim());
					logger.info("Approach Message: " + Item.current.getApproach());

					index = random.nextInt(leave.size());
					String leaveString = leave.get(index).trim();
					String[] leaveArray = leaveString.split(" - ");
					if (leaveArray.length > 1)
					{
						Item.current.setLeave(leaveArray[0].trim());
						Item.current.setAuthor(leaveArray[1].trim());
					}
					else
					{
						Item.current.setLeave(leaveString);
					}
					logger.info("Leave Message: " + leaveString);
				}

				Item.current.setState(state);
			}
		}

		final String directionString = req.getParameter("direction");
		if (directionString != null)
		{
			final Item.Direction direction = Item.Direction.valueOf(directionString);
			if (direction != null && Item.current.getDirection() != direction)
			{
				Item.current.setDirection(direction);
				logger.info("Direction: " + direction.toString());
			}
		}

		final String height = req.getParameter("height");
		if (height != null)
		{
			//logger.info("Height: " + height);
			try
			{
				int heightCM = Integer.parseInt(height);
				Item.current.setHeight(heightCM);
			}
			catch (Exception e)
			{
				logger.log(Level.WARNING, e.getMessage(), e);
			}
		}

		Item.updateCurrent();

		resp.addHeader("ETag", Item.currentTag);
		resp.setCharacterEncoding("UTF-8");
		resp.getWriter().print(Item.currentJson);
	}

	private void facebookPost(Item item)
	{
		try
		{
			if (properties.getProperty("upload.enabled").equals("true"))
			{
				final HTTPRequest request = new HTTPRequest(facebookURL, HTTPMethod.POST);

				final String message = "Someone has passed through the arch.\n" + item.toString();
				final String body = "message=" + URLEncoder.encode(message, "UTF-8") + "&access_token=" + properties.getProperty("page.accessToken");
				request.setPayload(body.getBytes("UTF-8"));

				logger.info(facebookURL.toString());
				logger.info(body);

				fetcher.fetchAsync(request);
			}
		}
		catch (Exception e)
		{
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}
}