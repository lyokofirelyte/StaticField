package com.github.lyokofirelyte.StaticField;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class DecorativeToasterCozy extends JavaPlugin implements CommandExecutor, Listener {
	
	File configFile;
	FileConfiguration config;
	private int taskID;
	DecorativeToasterCozy plugin;
	String prefix = "§aStaticField §f// §a";
	
	
	@Override
	public void onEnable(){
	
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this, this);
		configFile = new File(getDataFolder(), "config.yml");
	
			try {
		
				firstRun();
			} catch (Exception e) {
				e.printStackTrace();
			}
	
		config = new YamlConfiguration();
		loadYamls();
		registerCommands();
		plugin = this;
		
	}
		
	public void onDisable() {
		
		saveYamls();
		
	}
	
	private void registerCommands() {
		
		getCommand("forcefield").setExecutor(this);
		getCommand("ff").setExecutor(this);
	
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
	
	public void saveYamls() {

	    try {
	        config.save(configFile);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	public void loadYamls() {
	    try {
	        config.load(configFile);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	private void firstRun() throws Exception {
	    if(!configFile.exists()){
	        configFile.getParentFile().mkdirs();
	        copy(getResource("config.yml"), configFile);
	    }


	}
	
	private int getTaskID(){
		
		return taskID;
		
	}
	



	@Override
	public boolean onCommand(final CommandSender sender, Command cmd, String label, String[] args) {
		
		
	switch (cmd.getName()){
	
	case "forcefield": case "ff":
		
		
		
		final List<String> forceUsers = config.getStringList("ForceField.Users");

	if (!forceUsers.contains(sender.getName())){
		
		forceUsers.add(sender.getName());
		config.set("ForceField.Users", forceUsers);
		
			if (config.getBoolean("GUI")){
				sender.sendMessage(prefix + "FORCEFIELD ON!");
			}
			


			taskID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new BukkitRunnable() {
				
				public void run() {
					
					World world = ((Player) sender).getWorld();
					Location loc = ((Player) sender).getLocation();
					Location l = new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());
					Location l2 = new Location(loc.getWorld(), loc.getX(), loc.getY()+1, loc.getZ());
					world.playEffect(l, Effect.ENDER_SIGNAL, 0); 
					world.playEffect(l2, Effect.ENDER_SIGNAL, 0);
					int thisTask = getTaskID();
						if (config.getBoolean("Users." + sender.getName() + ".HasTask") == false){
							config.set("Users." + sender.getName() + ".Task", thisTask);
							config.set("Users." + sender.getName() + ".HasTask", true);
						}
				    }
				}, 0L, 5L);
			
	
			
			


		} else {

			forceUsers.remove(sender.getName());
			Bukkit.getServer().getScheduler().cancelTask(config.getInt("Users." + sender.getName() + ".Task"));
			config.set("Users." + sender.getName() + ".HasTask", false);
			config.set("ForceField.Users", forceUsers);
			if (config.getBoolean("GUI")){
				sender.sendMessage(prefix + "FORCEFIELD OFF!");
			}
			return true;
		}
	

	break;
	
	}
	
	return true;
}
	
	
	@EventHandler(priority = EventPriority.NORMAL)

	public void onPlayerMove(PlayerMoveEvent event){
	
		List<String> forceUsers = config.getStringList("ForceField.Users");
		
		if (forceUsers.size() >= 1 && !forceUsers.contains(event.getPlayer().getName())){
		
			for (String forcePlayer : forceUsers){
			
				double xto = event.getTo().getBlockX();
				double yto = event.getTo().getBlockY();
				double zto = event.getTo().getBlockZ();
		    
				double xfrom2 = Bukkit.getPlayer(forcePlayer).getLocation().getBlockX();
				double yfrom2 = Bukkit.getPlayer(forcePlayer).getLocation().getBlockY();
				double zfrom2 = Bukkit.getPlayer(forcePlayer).getLocation().getBlockZ();
		    
					if (xto == xfrom2 && yto == yfrom2 && zto == zfrom2) {
						event.getPlayer().setVelocity(event.getPlayer().getLocation().getDirection().multiply(-5));
							if(config.getBoolean("Message")){
								event.getPlayer().sendMessage(prefix + Bukkit.getPlayer(forcePlayer).getDisplayName() + " is currently using a forcefield!");
							}
					}
			}
		
		}
		
	}
	

	@EventHandler(priority = EventPriority.NORMAL)

	public void onEntityDamange(EntityDamageEvent event){
		
		
		List<String> forceUsers = config.getStringList("ForceField.Users");
		
		if (forceUsers.size() >= 1){
		
			Entity e = event.getEntity();
		
				if (e instanceof Player) {
					Player p = (Player) e;
		
		
						if (forceUsers.contains(p.getName())){
			
			    
							for (Entity e1 : event.getEntity().getNearbyEntities(5, 5, 5)) {
		    	
								e1.setVelocity(e1.getLocation().getDirection().multiply(-5));
	 
							}
		    
						event.setCancelled(true);
		    
						}
				}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public boolean onPlayerQuit(PlayerQuitEvent event) {
		
		if (config.getBoolean("Users." + event.getPlayer().getName() + ".HasTask")){
			
			List<String> forceUsers = config.getStringList("ForceField.Users");
			
			Bukkit.getServer().getScheduler().cancelTask(config.getInt("Users." + event.getPlayer().getName() + ".Task"));
			forceUsers.remove(event.getPlayer().getName());
			config.set("ForceField.Users", forceUsers);
			config.set("Users." + event.getPlayer().getName() + ".HasTask", false);

		}
		
		
		
		return true;
	
	}
	
}
