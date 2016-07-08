package iroot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Filter;
import java.util.logging.Logger;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class BungeePlugin extends Plugin implements Listener
{
	static Configuration config = new Configuration();
	static int connections = 1;
	static int sensitive = 4;
	static boolean guard = false;
	static boolean autoGuard;
	static List<String> whiteList = new ArrayList<String>();
	static List<String> blackList = new ArrayList<String>();
	static int captcha_length = 3;
	static int font_min = 20;
	static int font_max = 40;
	static boolean strong_captcha = false;
	static String serverName = "game";
	static String banMessage = "§cÂàø IP çàáëîêèðîâàí. Íàïèøèòå §fvk.com/ÂÀØ_ÂÊ §cäëÿ ðàçáëîêèðîâêè..";
	HashMap<String, Integer> attempts = new HashMap<String, Integer>();
	
	@Override
	public void onEnable()
	{
		Message("èíèöèàëèçàöèÿ...");
		//Console filter
		Filter filter = new LogFilter();
        BungeeCord.getInstance().getLogger().setFilter(filter);
        Logger.getLogger("BungeeCord").setFilter(filter);
        this.getLogger().setFilter(filter);
        ProxyServer.getInstance().getLogger().setFilter(filter);
        //Console filter
		if (!getDataFolder().exists())
		{
			try 
			{
				getDataFolder().mkdir();
				config.set("serverName", "game");
				config.set("sensitive", 4);
				config.set("autoGuard", true);
				config.set("guard", true);
				config.set("strong_captcha", false);
				config.set("captcha_length", 3);
				config.set("font_min", 20);
				config.set("font_max", 40);
				config.set("banMessage", "§cÂàø IP çàáëîêèðîâàí. Íàïèøèòå §fvk.com/ÂÀØ_ÂÊ §cäëÿ ðàçáëîêèðîâêè.");
				ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, new File(getDataFolder(), "config.yml"));
				Message("§aêîíôèã óñïåøíî ñîçäàí!");
			} 
			catch (IOException e) 
			{
				Message("§4îøèáêà ñîçäàíèÿ êîíôèãà!");
			}
		}
		else
		{
			try 
			{
				config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
				sensitive = config.getInt("sensitive");
				autoGuard = config.getBoolean("autoGuard");
				if (!autoGuard) guard = config.getBoolean("guard");
				strong_captcha = config.getBoolean("strong_captcha");
				captcha_length = config.getInt("captcha_length");
				font_min = config.getInt("font_min");
				font_max = config.getInt("font_max");
				serverName = config.getString("serverName");
				banMessage = config.getString("banMessage");
				Message("êîíôèã óñïåøíî çàãðóæåí!");
			} 
			catch (IOException e) 
			{
				Message("§4îøèáêà çàãðóçêè êîíôèãà!");
			}
		}
		Message("èíèöèàëèçàöèÿ ýâåíòîâ...");
		ProxyServer.getInstance().getPluginManager().registerListener(this, this);
		getProxy().getPluginManager().registerCommand(this, new notify());
		
		Message("çàïóñê êàï÷à ñåðâåðà...");
		String[] a = null; //Äà äà, ãîâíîêîä.
		Main.main(a);

		Message("èíèöèàëèçàöèÿ òàéìåðîâ..");
		ProxyServer.getInstance().getScheduler().schedule(this, new Runnable() 
		{
            @Override
            public void run() 
            {
            	if (connections > 0) connections--;
            	if (connections > 50) connections = 50;
            }
        }, 1, 1, TimeUnit.SECONDS);
		ProxyServer.getInstance().getScheduler().schedule(this, new Runnable() 
		{
            @SuppressWarnings("deprecation")
			@Override
            public void run() 
            {
            	if (guard && autoGuard)
            	{
	            	if (connections < sensitive)
	            	{
	            		getProxy().broadcast("§8[§fAn§bBot§8]: §fáîò àòàêà îòðàæåíà! Îòêëþ÷àåì çàùèòó!");
	            		guard = false;
	            	}
            	}
            }
        }, 15, 15, TimeUnit.SECONDS);
		ProxyServer.getInstance().getScheduler().schedule(this, new Runnable() 
		{
            @Override
            public void run() 
            {
            	if (connections < sensitive)
            	{
            		whiteList.clear();
            		blackList.clear();
            		Message("áåëûé ñïèñîê è áàíëèñò î÷èùåíû!");
            	}
            }
        }, 12, 12, TimeUnit.HOURS);
		ProxyServer.getInstance().getScheduler().schedule(this, new Runnable() 
		{
            @Override
            public void run() 
            {
            	if (connections < sensitive)
            	{
            		attempts.clear();
            		Message("âðåìåííûé ñïèñîê èãðîêîâ î÷èùåí!");
            		System.gc();
            	}
            }
        }, 20, 20, TimeUnit.MINUTES);
		Message("AnBot óñïåøíî èíèöèàëèçèðîâàí!");
	}
	
	@Override
	public void onDisable()
	{
		Message("stopping...");
		try 
		{
			ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, new File(getDataFolder(), "config.yml"));
		} 
		catch (IOException e) 
		{
			Message("§4îøèáêà ñîõðàíåíèÿ êîíôèãà!");
		}
		Main.stopServer();
		Message("server successfully stopped!");
	}
	
	@SuppressWarnings("deprecation")
	public void Message(String s)
	{
		getProxy().getConsole().sendMessage("§8[§fAn§bBot§8]: §f" + s);
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerChat(ChatEvent e)
	{
		ProxiedPlayer player = (ProxiedPlayer)e.getSender();
		if (player.getServer().getInfo().getName().equals("ancore"))
		{
			String playerName = player.getDisplayName();
			String message = e.getMessage();
			String captcha = Main.getNameCaptcha(playerName);
			if ((message.contains(captcha) && captcha.length() > 1))//if ((message.equals(captcha) && captcha.length() > 1))
			{
				player.connect(ProxyServer.getInstance().getServerInfo(serverName));
				whiteList.add(player.getDisplayName());
				Message("§b"+player.getDisplayName() + "§f ïðîø¸ë êàï÷ó. Âõîä ðàçðåø¸í!");
			}
			else
			{
				String playerAddress = player.getAddress().getHostString();
				if (!attempts.containsKey(playerAddress))
				{
					attempts.put(playerAddress, 5);
				}
				else
				{
					attempts.put(playerAddress, attempts.get(playerAddress)-1);
				}
				player.sendMessage("§cÊàï÷à ââåäåíà íå âåðíî! Ââîäèòü íóæíî ïðÿìî â ÷àò!");
				player.sendMessage("§6Ó âàñ îñòàëîñü §c" + attempts.get(playerAddress) + "§6 ïîïûòîê!");
				if (attempts.get(playerAddress) < 2)
				{
					player.sendMessage("§cÂíèìàíèå! Âû áóäåòå çàáåíåíû åñëè íå ââåä¸òå êîððåêòíóþ êàï÷ó! Îñòàëàñü 1 ïîïûòêà!");
					player.sendMessage("§61. Âíèìàòåëüíî ïîñìîòðèòå íà êàðòó, ïðî÷èòàéòå êîä êîòîðûé âû âèäèòå íà íåé.");
					player.sendMessage("§62. Îòêðîéòå êëàâèøåé T ÷àò è ââåäèòå äàííûé êîä. Âíèìàíèå! Áåç çíàêà / è ïðî÷åãî, ïðîñòî êîä!");
					player.sendMessage("§cÂíèìàíèå! Âû áóäåòå çàáåíåíû åñëè íå ââåä¸òå êîððåêòíóþ êàï÷ó! Îñòàëàñü 1 ïîïûòêà!");
					if (attempts.get(playerAddress) < 1)
					{
						blackList.add(playerAddress);
						attempts.remove(playerAddress);
						Message("§cIP àäðåñ §f"+ playerAddress + "§c çàáëîêèðîâàí!");
						player.disconnect("§cÏîçäðàâëÿþ! Âû çàáàíåíû!");
					}
				}
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerLeave(PlayerDisconnectEvent e)
	{
		if(guard)
		{
			if (!whiteList.contains(e.getPlayer().getDisplayName()))
			{
				ProxiedPlayer player = e.getPlayer();
				String playerAddress = player.getAddress().getHostString();
				if (!attempts.containsKey(playerAddress))
				{
					attempts.put(playerAddress, 5);
				}
				else
				{
					attempts.put(playerAddress, attempts.get(playerAddress)-1);
				}
				if (attempts.get(playerAddress) < 1 && !blackList.contains(playerAddress))
				{
					blackList.add(playerAddress);
					attempts.remove(playerAddress);
					Message("§cIP àäðåñ §f"+ playerAddress + "§c çàáëîêèðîâàí!");
					player.disconnect("§cÏîçäðàâëÿþ! Âû çàáàíåíû!");
				}
				
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerInitial(PreLoginEvent e)
	{
		//Ñ÷¸ò÷èê ïîäêëþ÷åíèé.
		connections++;
		if (autoGuard)
		{
			if (connections >= sensitive)
			{
				if (!guard)
				{
					guard = true;
					getProxy().broadcast("§8[§fAn§bBot§8]: §cïîäîçðåíèå íà áîò àòàêó. Âêëþ÷àåì çàùèòó!");
				}
			}
		}
				
		//Ïðîâåðêà íà áàí IP àäðåññà. 
		if (blackList.contains(e.getConnection().getAddress().getHostString())) 
		{
			e.setCancelReason(banMessage);
			e.setCancelled(true);
		}
		if (guard)
		{
			for (int i = 0; i < getProxy().getPlayers().size(); i++)
			{
				ProxiedPlayer p = (ProxiedPlayer) getProxy().getPlayers().toArray()[i];
				if (p.getAddress().getHostString().equals(e.getConnection().getAddress().getHostString()) && p.getServer().getInfo().getName().equals("ancore"))
				{
					e.setCancelReason("§cÍåëüçÿ çàõîäèòü îäíîâðåìåííî ñ îäíîãî IP äâóì èãðîêàì!");
					e.setCancelled(true);
				}
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	public static void sendPlayer(String playerName)
	{
		ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerName);
		if (!guard || whiteList.contains(player.getDisplayName()))
		{
			player.connect(ProxyServer.getInstance().getServerInfo(serverName));
			ProxyServer.getInstance().getConsole().sendMessage("§8[§fAn§bBot§8]: §fÂõîä äëÿ §b" + player.getDisplayName() + "§f ðàçðåø¸í!");
		}
	}
}
