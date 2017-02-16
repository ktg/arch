/*
 *     Light Night Arch Control software
 *     Copyright (C) 2016 University of Nottingham
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class StateServlet extends HttpServlet
{
	private static final OkHttpClient client = new OkHttpClient();
	private static final Logger logger = Logger.getLogger("Updates");
	private static final Gson gson = new GsonBuilder().create();

	static String access_token;
	private final Random random = new Random();
	private final List<String> approach;
	private final List<String> leave;
	private final List<String> qualities;
	private final Properties properties = new Properties();
	private URL facebookURL;

	public StateServlet()
	{
		approach = loadStrings("/approach.json");
		leave = loadStrings("/leave.json");
		qualities = StateServlet.loadStrings("/qualities.json");
		try
		{

			properties.load(load("/facebook.properties"));
			access_token = properties.getProperty("page.accessToken");

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
			if (state != Item.current.getState())
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

					if(Item.current.getData().isEmpty())
					{
						index = random.nextInt(qualities.size());
						Item.current.getData().add(qualities.get(index));
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
			if (Item.current.getDirection() != direction)
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
				final String message = "Someone has passed through the arch.\n" + item.toString();
				MultipartBody body = new MultipartBody.Builder()
						.addFormDataPart("message", message)
						.addFormDataPart("access_token", access_token)
						.build();

				Request request = new Request.Builder()
						.url(facebookURL)
						.post(body)
						.build();

				client.newCall(request).enqueue(new Callback()
				{
					@Override
					public void onFailure(Call call, IOException e)
					{
						logger.log(Level.WARNING, e.getMessage(), e);
					}

					@Override
					public void onResponse(Call call, Response response) throws IOException
					{
						logger.info("Sent to Facebook");
					}
				});
			}
		}
		catch (Exception e)
		{
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}
}