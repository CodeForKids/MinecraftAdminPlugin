package codeforkids.minecraft.codeforkidsminecraft;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class CodeForKidsPlugin extends JavaPlugin implements Listener  {
	
	File configFile;
	FileConfiguration config;
	String configFileName = "config.yml";
	
	@Override
	public void onEnable(){
		setupConfig();
		this.getServer().getPluginManager().registerEvents(this,this);
		getLogger().info("CodeForKidsPlugin has been enabled!");
	}
	
	@Override
	public void onDisable() {
		saveConfig();
		getLogger().info("CodeForKidsPlugin has been disabled!");
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent evt) {
	    Player player = evt.getPlayer(); // The player who joined
	    try {
	    	getLogger().info(player.getName() + " logged in... handling event");
	    	notifyLoginOut(player.getName(), "true", player.getAddress().getHostName());
			
		} catch (Exception e) {
			getLogger().info(e.getLocalizedMessage());
			getLogger().info(e.getStackTrace().toString());
		}
	}
	
	@EventHandler
    public void onPlayerLogout(PlayerQuitEvent event){
        Player player = event.getPlayer();
	    try {
	    	getLogger().info(player.getName() + " logged out... handling event");
	    	notifyLoginOut(player.getName(), "false", player.getAddress().getHostName());
		} catch (Exception e) {
			getLogger().info(e.getLocalizedMessage());
			getLogger().info(e.getStackTrace().toString());
		}
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("notify")) {
			Player player = (Player) sender;
			if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
				player.sendMessage("Type \"/notify MESSAGE\" to notify the admins of a help request (like protecting a house!)");
				return true;
			} else {
				return doNotify(player, args);
			}
		} 
		return false; 
	}
	
	/****** NOTIFY EMAIL ******/
	
	private Boolean doNotify(Player player, String[] args) {
		Location loc = player.getLocation();
		String userName = player.getName();			
		String msg = formatMessage(args);
		String query = "username=" + userName + "&notify_message=" + msg + "&x_pos=" + loc.getBlockX() + "&y_pos=" + loc.getBlockY() + "&z_pos=" + loc.getBlockZ();
		if (msg != null && sendPost(query)) {
			player.sendMessage("Message sucessfully sent!");
			return true;
		} else {
			player.sendMessage("Something went wrong. Please try again in a bit! Or email us at minecraft@codeforkids.ca");
			return false;
		}
	}
	
	private String combine(String[] args) {
		String result = "";
		for (int i = 0; i<args.length; i++) {
			result += args[i] + " ";
		}
		return result;
	}
	
	private String formatMessage(String[] args) {
		try {
			return URLEncoder.encode(combine(args), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			getLogger().info(e.getLocalizedMessage());
			getLogger().info(e.getStackTrace().toString());
			return null;
		}
	}
	
	/****** LOGIN/LOGOUT ******/
	
	private void notifyLoginOut(String username, String joined, String ipAddress) {
		String query = "username="+ username+ "&joined=" + joined + "&ip=" + ipAddress;
		sendPost(query);
	}
 
	private Boolean sendPost(String query) {
		try {
			String urlString = config.getString("url");
			URL url = new URL(urlString);
		    URLConnection conn = url.openConnection();
		    conn.setDoOutput(true);
		    OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
		    writer.write(query);
		    writer.flush();
		    String line;
		    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		    String message = "";
		    while ((line = reader.readLine()) != null) {
		      message += line;
		    }
		    getLogger().info(message);
		    writer.close();
		    reader.close();
		    return message.length() == 0;
		} catch (Exception e) {
			getLogger().info(e.getLocalizedMessage());
			getLogger().info(e.getStackTrace().toString());
			return false;
		}
	}
	
	/****** CONFIG ******/
	
	private void setupConfig() {
		configFile = new File(getDataFolder(), configFileName);
		firstRun();
		config = new YamlConfiguration();
		loadConfig();
	}
	
	private void firstRun() {
	    if(!configFile.exists()){
	    	getLogger().info("Config File did not exist, creating it now");
	    	getDataFolder().mkdirs();
	        copy(getResource(configFileName), configFile);
	    }
	}
	
	private void copy(InputStream in, File file) {
	    try {
	        OutputStream out = new FileOutputStream(file);
	        byte[] buf = new byte[1024];
	        int len;
	        while((len=in.read(buf))>0){
	            out.write(buf,0,len);
	        }
	        out.close();
	        in.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public void saveConfig() {
	    try {
	        config.save(configFile);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	public void loadConfig() {
	    try {
	        config.load(configFile);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
 
}
