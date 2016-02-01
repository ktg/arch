package uk.ac.nott.mrl.arch.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class Item
{
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
	private String leave;
	private Direction direction = null;
	private State state = State.waiting;
	private String height;
	private List<String> data = new ArrayList<>();

	private Date timestamp = new Date();

	public Item()
	{
	}

	public Item(final List<String> data)
	{
		this.data = data;
	}

	public Date getTimestamp()
	{
		return timestamp;
	}

	public State getState()
	{
		return state;
	}

	public void setState(State state)
	{
		this.state = state;
		timestamp = new Date();
	}

	public void setData(List<String> data)
	{
		this.data = data;
		timestamp = new Date();
	}

	public void setHeight(String height)
	{
		this.height = height;
		timestamp = new Date();
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
		timestamp = new Date();
	}

	public String getHeight()
	{
		return height;
	}
}
