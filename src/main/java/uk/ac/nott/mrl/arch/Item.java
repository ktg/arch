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

package uk.ac.nott.mrl.arch;

import com.google.common.hash.Hashing;
import com.google.gson.Gson;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

class Item
{
	private static final Logger logger = Logger.getLogger("Data");

	private static final Gson gson = new Gson();
	private static final Charset charset = Charset.forName("UTF-8");
	static Item current = new Item();
	static String currentJson = "";
	static String currentTag = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

	enum State
	{
		waiting,
		engagement,
		approaching,
		under,
		leaving,
		left
	}

	enum Direction
	{
		left,
		right
	}

	private String approach;
	private String author;
	private String leave;
	private Direction direction = null;
	private State state = State.waiting;
	private int heightCM = 0;
	private String height;
	private List<String> data = new ArrayList<>();

	Item()
	{
	}

	State getState()
	{
		return state;
	}

	void setState(State state)
	{
		this.state = state;
	}

	public void setData(List<String> data)
	{
		this.data = data;
	}

	void setApproach(String approach)
	{
		this.approach = approach;
	}

	void setLeave(String leave)
	{
		this.leave = leave;
	}

	void setDirection(Direction direction)
	{
		this.direction = direction;
	}

	void setHeight(int heightCM)
	{
		if(heightCM > this.heightCM)
		{
			this.heightCM = heightCM;
			int feetPart = (int) Math.floor((heightCM / 2.54) / 12);
			int inchesPart = (int) Math.floor((heightCM / 2.54) - (feetPart * 12));
			this.height = String.format("%d' %d\"", feetPart, inchesPart);
			//logger.info("Height: " + Item.current.getHeight());
		}
	}

	void setAuthor(String author)
	{
		this.author = author;
	}

	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder();
		for(String dataItem: data)
		{
			builder.append(dataItem);
			builder.append("\n");
		}
		if(height != null)
		{
			builder.append(height);
			builder.append("\n");
		}
		if(approach != null)
		{
			builder.append("They asked themselves ");
			builder.append(approach.toLowerCase());
			builder.append("\n");
			builder.append("They gained this wisdom: ");
			builder.append(leave);
			if (author != null)
			{
				builder.append(author);
			}
		}
		return builder.toString();
	}

	static void updateCurrent()
	{
		final String json = gson.toJson(current);
		final String tag = Hashing.sha256().hashString(json, charset).toString();
		if(!tag.equals(currentTag))
		{
			currentTag = tag;
			currentJson = json;
			logger.info(currentJson);
		}
	}

	String getApproach()
	{
		return approach;
	}

	Direction getDirection()
	{
		return direction;
	}

	public List<String> getData()
	{
		return data;
	}
}
