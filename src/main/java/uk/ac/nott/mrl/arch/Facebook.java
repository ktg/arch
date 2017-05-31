package uk.ac.nott.mrl.arch;


import okhttp3.*;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Facebook
{
	private static final Logger logger = Logger.getLogger("");
	private OkHttpClient client = new OkHttpClient();
	private String accessToken;
	private final URL facebookURL;

	public Facebook(URL facebookURL)
	{
		this.facebookURL = facebookURL;
	}

	public void setAccessToken(String token)
	{
		this.accessToken = token;
	}

	public void post(Item item)
	{
		try
		{
			if (facebookURL != null)
			{
				final String message = "Someone has passed through the arch.\n" + item.toString();
				MultipartBody body = new MultipartBody.Builder()
						.addFormDataPart("message", message)
						.addFormDataPart("access_token", accessToken)
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
