package uk.ac.nott.mrl.arch.server;

import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

class Item
{
	private static final Logger logger = Logger.getLogger("Data");

	private static final Gson gson = new GsonBuilder().create();
	private static final Charset charset = Charset.forName("UTF-8");
	static Item current = new Item();
	static String currentJson = "";
	static String currentTag = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
	static Item next;

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
	private String height;
	private List<String> data = new ArrayList<>();

	public Item()
	{
	}

	public Item(final List<String> data)
	{
		this.data = data;
	}

	public State getState()
	{
		return state;
	}

	public void setState(State state)
	{
		this.state = state;
	}

	public void setData(List<String> data)
	{
		this.data = data;
	}

	public void setApproach(String approach)
	{
		this.approach = approach;
	}

	public void setLeave(String leave)
	{
		this.leave = leave;
	}

	public void setDirection(Direction direction)
	{
		this.direction = direction;
	}

	public String getHeight()
	{
		return height;
	}

	public void setHeight(String height)
	{
		this.height = height;
	}

	public void setAuthor(String author)
	{
		this.author = author;
	}

	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder();
		builder.append(approach);
		builder.append("\n");
		for(String dataItem: data)
		{
			builder.append(dataItem);
			builder.append("\n");
		}
		builder.append(leave);
		if(author != null)
		{
			builder.append("\n");
			builder.append(author);
		}
		return builder.toString();
	}

	public static void updateCurrent()
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

	public String getApproach()
	{
		return approach;
	}

	public String getLeave()
	{
		return leave;
	}

	public Direction getDirection()
	{
		return direction;
	}
}
