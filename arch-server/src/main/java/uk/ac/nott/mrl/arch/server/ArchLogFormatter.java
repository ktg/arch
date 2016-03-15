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

package uk.ac.nott.mrl.arch.server;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class ArchLogFormatter extends Formatter
{
	private static final DateFormat df = new SimpleDateFormat("dd MMM yyyy hh:mm:ss.SSS");

	@Override
	public String format(LogRecord record)
	{
		final StringBuilder builder = new StringBuilder(1000);
		builder.append(df.format(new Date(record.getMillis()))).append(" ");
		//builder.append("[").append(record.getSourceClassName()).append(".");
		//builder.append(record.getSourceMethodName()).append("] - ");
		if(!record.getLoggerName().isEmpty())
		{
			builder.append(record.getLoggerName()).append(" ");
		}
		builder.append(record.getLevel()).append(": ");
		builder.append(formatMessage(record));
		builder.append("\n");
		if (record.getThrown() != null)
		{
			try
			{
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				record.getThrown().printStackTrace(pw);
				pw.close();
				builder.append(sw.toString());
			}
			catch (Exception ex)
			{
			}
		}
		return builder.toString();
	}

	public String getHead(Handler h)
	{
		return super.getHead(h);
	}

	public String getTail(Handler h)
	{
		return super.getTail(h);
	}
}