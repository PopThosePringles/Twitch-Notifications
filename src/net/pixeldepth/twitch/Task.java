package net.pixeldepth.twitch;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.TimerTask;

public class Task extends TimerTask {

	/**
	 * Here we will store a map so that we can check later who is live and who is not.
	 *
	 * @property {HashMap} streams
	 */

	private HashMap<String, Boolean> streams = new HashMap<String, Boolean>();

	/**
	 * Need to access the Twitch_Notifications class so we can get the stream names and tray icon.
	 *
	 * @property {Twitch_Notifications} twitch_notifications
	 */

	private Twitch_Notifications twitch_notifications;

	/**
	 * Here we create the map for the streams.  By default they are not live, so we set them to false.
	 *
	 * @param {Twitch_Notifications} twitch_notifications
	 */

	public Task(Twitch_Notifications twitch_notifications){
		this.twitch_notifications = twitch_notifications;

		for(String streamer : this.twitch_notifications.stream_names){
			this.streams.put(streamer, false);
		}

		// We want to run instantly, then let the timer take over.

		this.run();
	}

	/**
	 * This is for the timer.
	 * Notice we sleep.  Am not sure if Twitch has a rate limit for accessing the JSON,
	 * but just in case, I added in a interval between each check.
	 */

	@Override
	public void run(){
		for(String streamer : this.streams.keySet()){
			if(!this.streams.get(streamer)){
				this.streams.put(streamer, this.stream_is_live(streamer));
			}
		}
	}

	/**
	 * Fetches the JSON for the stream so that we can check if they are live.
	 * If they are live, we show a notification using the name and game being streamed.
	 *
	 * @param {String} streamer
	 * @return {Boolean} Returns true if the streamer is live.
	 */

	private boolean stream_is_live(String streamer){
		try {
			URL url = new URL("https://api.twitch.tv/kraken/streams/" + streamer);

			URLConnection connection = url.openConnection();
			HttpURLConnection http_cnnection = (HttpURLConnection) connection;
			InputStream is = http_cnnection.getInputStream();

			BufferedReader reader = new BufferedReader(new InputStreamReader(is,"UTF8"),8);
			String line = null;
			StringBuilder sb = new StringBuilder();

			while((line = reader.readLine()) != null){
				sb.append(line);
			}

			is.close();
			http_cnnection.disconnect();

			if(sb.toString().length() > 0){
				try {
					JSONParser parser = new JSONParser();
					JSONObject json = (JSONObject) parser.parse(sb.toString());

					if(json.get("stream") != null){
						JSONObject stream = (JSONObject) json.get("stream");
						JSONObject channel = (JSONObject) stream.get("channel");

						String msg = channel.get("display_name") + " is live and playing \"" + channel.get("game") + "\"";

						this.twitch_notifications.tray_icon.displayMessage("Stream Live", msg, TrayIcon.MessageType.INFO);

						return true;
					}
				} catch(ParseException ex){
					ex.printStackTrace();
				}
			}
		} catch(IOException e){
			e.printStackTrace();
		}

		return false;
	}
}
