package net.pixeldepth.twitch;

import javax.swing.*;
import java.awt.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.Timer;

public class Twitch_Notifications {

	/**
	 * Polling time.  Default is to check every 30 seconds
	 *
	 * @property {Integer} interval
	 */

	public int interval = 30000;

	/**
	 * A system tray icon is created so that we can exit and refresh the list.
	 * Here we set it to true so that we can continue on if the tray icon was
	 * succesfully created.
	 *
	 * @property {Boolean} system_tray_created
	 */

	private boolean system_tray_created = false;

	/**
	 * The file name that we are storing the stream usernames in.
	 * The path to this is the same path as the application.
	 * Each streamer username should be on a new line.
	 *
	 * @property {String} file_name
	 */

	private String file_name = "streams.txt";

	/**
	 * The stream usernames from the file are stored here so that the Task can
	 * access them to loop over.
	 *
	 * @property {ArrayList} stream_names
	 */

	public ArrayList<String> stream_names = new ArrayList<String>();

	/**
	 * We want to be able to display a notification when a stream is live, otherwise
	 * what is the point in using this app?
	 *
	 * Things to note:  I had to enable Windows 10 notifications in the control panel.
	 * It seems as if "balloons" are now "toasters" that slide out from the right.
	 *
	 * @property {TrayIcon} tray_icon
	 */

	public TrayIcon tray_icon;

	/**
	 * Allows are to poll using a timer.
	 *
	 * @property {Timer} timer
	 */

	private Timer timer;

	/**
	 * Creates the system tray icon and calls init to load the stream
	 * names from the file, and then run the task to poll them.
	 */

	public Twitch_Notifications(){
		this.create_system_tray_icon();

		if(this.system_tray_created){
			this.init();
		}
	}

	/**
	 * Loads the stream names from the file, and runs the task.
	 */

	private void init(){
		this.load_stream_names();
		this.run_task();
	}

	/**
	 * Runs the task so we can poll the streams.  Because we can refresh the
	 * list by right clicking on the icon, we need to cancel the existing timer.
	 */

	private void run_task(){
		if(this.timer != null){
			this.timer.cancel();
			this.timer.purge();
		}

		Task task = new Task(this);
		this.timer = new Timer();
		this.timer.schedule(task, this.interval);
	}

	/**
	 * Loads the stream names (each one on a new line) from the file.
	 */

	public void load_stream_names(){
		this.stream_names.clear();

		BufferedReader reader = null;

		try {
			File file = new File(this.file_name);
			reader = new BufferedReader(new FileReader(file));
			String line;

			while((line = reader.readLine()) != null){
				if(line.length() > 0){
					this.stream_names.add(line);
				}
			}
		} catch(IOException e){
			e.printStackTrace();
		} finally {
			try {

				reader.close();

			} catch(IOException e){
				e.printStackTrace();
			}
		}
	}

	/**
	 * Creates a little twitch icon in the system tray so we can refresh the list and exit.
	 */

	private void create_system_tray_icon(){
		if(SystemTray.isSupported()){
			PopupMenu popup = new PopupMenu();
			Image image = this.create_image("/resources/images/twitch.png", "Twitch Stream Notifications");

			if(image != null){
				this.tray_icon = new TrayIcon(image);
				SystemTray tray = SystemTray.getSystemTray();

				MenuItem refresh = new MenuItem("Refresh List");
				MenuItem exit = new MenuItem("Exit");

				popup.add(refresh);
				popup.add(exit);

				this.tray_icon.setPopupMenu(popup);
				this.tray_icon.setToolTip("Twitch Stream Notifications");

				try {
					tray.add(this.tray_icon);
				} catch(AWTException e){
					e.printStackTrace();
					return;
				}

				Twitch_Notifications tn = this;

				exit.addActionListener(new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent e){
						tray.remove(tn.tray_icon);
						System.exit(0);
					}

				});

				refresh.addActionListener(new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent e){
						tn.init();
					}

				});

				this.system_tray_created = true;
			}
		}
	}

	/**
	 * Creates an image for the system tray.
	 *
	 * @param {String} path The path to the icon.
	 * @param {String} desc Description for the icon.
	 * @return {ImageIcon}
	 */

	private Image create_image(String path, String desc){
		URL image_url = this.getClass().getResource(path);

		if(image_url != null){
			return (new ImageIcon(image_url, desc)).getImage();
		}

		return null;
	}

}
