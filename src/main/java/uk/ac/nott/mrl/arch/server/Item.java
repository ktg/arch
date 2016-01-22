package uk.ac.nott.mrl.arch.server;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
class Item
{
	public static final String RECENT_ID = "-default-";

	@Id
	private String id;

	private String in;
	private String out;
	private List<String> shake = new ArrayList<>();

	private Date timestamp;

	public Item()
	{

	}

	public Item(final String id, final List<String> value)
	{
		this.id = id;
		timestamp = new Date();
		this.shake = value;
	}

	public String getIn()
	{
		return in;
	}

	public String getOut()
	{
		return out;
	}

	public List<String> getShake()
	{
		return shake;
	}

	public Date getTimestamp()
	{
		return timestamp;
	}

	public void setId(final String id)
	{
		this.id = id;
	}

	public void setTimestamp(final Date timestamp)
	{
		this.timestamp = timestamp;
	}
}
