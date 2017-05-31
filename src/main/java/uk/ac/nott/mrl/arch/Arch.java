package uk.ac.nott.mrl.arch;/*
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import spark.Spark;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Arch
{
	private static final Gson gson = new Gson();
	private static final Logger logger = Logger.getLogger("");
	private static final Random random = new Random();
	private static final List<String> approach = new ArrayList<>();
	private static final List<String> leave = new ArrayList<>();
	private static final List<String> qualities = new ArrayList<>();

	private static String OS = System.getProperty("os.name").toLowerCase();

	private static Facebook facebook;

	public static void main(String[] args)
	{
		try
		{
			LogManager.getLogManager().readConfiguration(Arch.class.getResourceAsStream("/logging.properties"));

			approach.addAll(loadStrings("/approach.json"));
			leave.addAll(loadStrings("/leave.json"));
			qualities.addAll(loadStrings("/qualities.json"));

			try
			{
				Properties properties = new Properties();
				properties.load(load("/facebook.properties"));
				if(properties.getProperty("upload.enabled").equals("true"))
				{
					facebook = new Facebook(new URL("https://graph.facebook.com/v2.5/" + properties.getProperty("page.id") + "/feed"));
					facebook.setAccessToken(properties.getProperty("page.accessToken"));
				}
			}
			catch (Exception e)
			{
				logger.log(Level.WARNING, e.getMessage(), e);
			}


			int port;
			if (isUnix())
			{
				port = 80;
			}
			else
			{
				port = 8080;
			}

			Spark.port(port);
			Spark.externalStaticFileLocation(new File(System.getProperty("user.dir"), "/src/main/web/").getAbsolutePath());
			Spark.get("/item", (request, response) ->
			{
				String matches = request.headers("If-None-Match");
				if (matches != null && matches.equals(Item.currentTag))
				{
					response.status(304);
					return "";
				}
				else
				{
					response.type("application/json");
					response.header("ETag", Item.currentTag);
					//resp.("UTF-8");
					return Item.currentJson;
				}
			});

			Spark.post("/item", (request, response) ->
			{
				String token = request.queryParams("token");
				if (token != null)
				{
					if(facebook != null)
					{
						facebook.setAccessToken(token);
					}
				}
				else
				{
					final List<String> values = new ArrayList<>();
					final List<String> names = new ArrayList<>(request.queryParams());
					Collections.sort(names);
					String data = "";
					for (String parameter : names)
					{
						String value = request.queryParams(parameter);
						if (value != null)
						{
							if (data.equals(""))
							{
								data = value;
							}
							else
							{
								data = data + ", " + value;
							}
							values.add(value);
						}
					}
					logger.info("Data: " + data);

					Item.current.setData(values);
					Item.updateCurrent();
				}
				response.type("application/json");
				response.header("ETag", Item.currentTag);
				//re.setCharacterEncoding("UTF-8");
				return Item.currentJson;
			});

			Spark.post("/state", (request, response) ->
			{
				final String stateString = request.queryParams("state");
				logger.info(stateString);
				if (stateString != null)
				{
					final Item.State state = Item.State.valueOf(stateString);
					if (state != Item.current.getState())
					{
						updateState(state);
					}
				}

				final String directionString = request.queryParams("direction");
				if (directionString != null)
				{
					final Item.Direction direction = Item.Direction.valueOf(directionString);
					if (Item.current.getDirection() != direction)
					{
						Item.current.setDirection(direction);
						logger.info("Direction: " + direction.toString());
					}
				}

				final String height = request.queryParams("height");
				if (height != null)
				{
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

				response.header("ETag", Item.currentTag);
				return Item.currentJson;
			});

			new Thread(() ->
			{
				while (true)
				{
					SerialPorts.updateSerialPorts(message ->
					{
						String string = message.trim().toLowerCase();
						if (string.startsWith("state"))
						{
							String[] parts = string.split(" ");

							logger.fine(string);
							if (parts.length >= 2)
							{
								updateState(Item.State.valueOf(parts[1]));

								if (parts[0].equals("stateleft"))
								{
									if (Item.current.getDirection() != Item.Direction.left)
									{
										Item.current.setDirection(Item.Direction.left);
										logger.info("Direction: " + Item.Direction.left.toString());
									}
								}
								else if (parts[0].equals("stateright"))
								{
									if (Item.current.getDirection() != Item.Direction.right)
									{
										Item.current.setDirection(Item.Direction.right);
										logger.info("Direction: " + Item.Direction.right.toString());
									}
								}

								if (parts.length >= 3)
								{
									try
									{
										int heightCM = Integer.parseInt(parts[2]);
										Item.current.setHeight(heightCM);
									}
									catch (Exception e)
									{
										logger.log(Level.WARNING, e.getMessage(), e);
									}
								}

								Item.updateCurrent();
							}
						}
					});
					try
					{
						synchronized (random)
						{
							random.wait(1000);
						}
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}).start();

			if (isWindows())
			{
				try
				{
					new ProcessBuilder("C:/Program Files (x86)/Google/Chrome/Application/chrome.exe", "--user-data-dir=D:/chrome-users/left", "--kiosk", " --incognito", "--disable-infobars", "--start-fullscreen", "http://localhost:8080/left.html").start();
					new ProcessBuilder("C:/Program Files (x86)/Google/Chrome/Application/chrome.exe", "--user-data-dir=D:/chrome-users/right", "--kiosk", " --incognito", "--disable-infobars", "--window-position=1980,0", "--start-fullscreen", "http://localhost:8080/right.html").start();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		catch (Exception e)
		{
			logger.warning(e.getMessage());
		}
	}

	private static boolean isUnix()
	{
		return (OS.contains("nix") || OS.contains("nux") || OS.indexOf("aix") > 0);
	}

	private static boolean isWindows()
	{
		return OS.contains("win");
	}

	private static List<String> loadStrings(String file) throws IOException
	{
		return gson.fromJson(load(file), new TypeToken<List<String>>()
		{
		}.getType());
	}

	private static Reader load(String file) throws IOException
	{
		return new InputStreamReader(Arch.class.getResource(file).openStream(), "UTF-8");
	}

	private static void updateState(Item.State state)
	{
		logger.info("State1: " + state);
		if (state != Item.current.getState())
		{
			logger.info("State: " + state);
			if (Item.current.getState() == Item.State.leaving || (Item.current.getState() == Item.State.under && state != Item.State.leaving))
			{
				if(facebook != null)
				{
					facebook.post(Item.current);
				}
				Item.current = new Item();
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

				if (Item.current.getData().isEmpty())
				{
					index = random.nextInt(qualities.size());
					Item.current.getData().add(qualities.get(index));
				}

				logger.info("Leave Message: " + leaveString);
			}

			Item.current.setState(state);
		}
	}
}