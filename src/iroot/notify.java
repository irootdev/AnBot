package iroot;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class notify extends Command {

	public notify() 
	{
		super("ancore");
	}

	@SuppressWarnings("deprecation")
	@Override
	public void execute(CommandSender sender, String[] arg1) 
	{
		if (sender.hasPermission("ancore"))
		{
			if (arg1.length > 0)
			{
				if (arg1[0].contains("banlist"))
				{
					sender.sendMessage("§c"+BungeePlugin.blackList);
				}
				if (arg1[0].contains("unban"))
				{
					if (arg1.length < 2)
					{
						sender.sendMessage("§c/ancore unban <IP>");
					}
					else
					{
						BungeePlugin.blackList.remove(arg1[1]);
						sender.sendMessage("§8[§fAn§bBot§8]: §fУказаный IP успешно разблокирован!");
					}
				}
				if (arg1[0].contains("guard"))
				{
					if (arg1.length < 2)
					{
						sender.sendMessage("§c/ancore guard <on/off/auto>");
					}
					else
					{
						if (arg1[1].equalsIgnoreCase("on"))
						{
							BungeePlugin.guard = true;
							BungeePlugin.autoGuard = false;
							sender.sendMessage("§8[§fAn§bBot§8]: §fЗащита принудительно включена!");
							
						}
						if (arg1[1].equalsIgnoreCase("off"))
						{
							BungeePlugin.guard = false;
							BungeePlugin.autoGuard = false;
							sender.sendMessage("§8[§fAn§bBot§8]: §fЗащита принудительно отключена!");
						}
						if (arg1[1].equalsIgnoreCase("auto"))
						{
							BungeePlugin.guard = false;
							BungeePlugin.autoGuard = true;
							sender.sendMessage("§8[§fAn§bBot§8]: §fЗащита работает в автоматическом режиме!");
						}
					}
				}
			}
			else
			{
				Runtime runtime = Runtime.getRuntime();
				long maxMemory = runtime.maxMemory();
				long allocatedMemory = runtime.totalMemory();
				long freeMemory = runtime.freeMemory();
				
				sender.sendMessage("§f.......:::§8[ §bAnBot Information §8]§f:::.......");
				sender.sendMessage("§fUsed memory: §b" + (((allocatedMemory / 1024)/1024)-((freeMemory / 1024)/1024)) + "§f MB");
				sender.sendMessage("§fFree memory: §b" + (freeMemory / 1024)/1024 + "§f MB");
				sender.sendMessage("§fAllocated memory: §b" + (allocatedMemory / 1024)/1024 + "§f MB");
				sender.sendMessage("§fMax memory: §b" + (maxMemory / 1024)/1024 + "§f MB");
				sender.sendMessage("§fTotal free memory: §b" + ((freeMemory + (maxMemory - allocatedMemory)) / 1024)/1024 + "§f MB");
				sender.sendMessage(" ");
				sender.sendMessage("§f.......:::§8[ §bAnCore Information §8]§f:::.......");
				sender.sendMessage("§fJoin/Sec: §b" + BungeePlugin.connections);
				sender.sendMessage("§fGuard Status: §b" + BungeePlugin.guard);
				sender.sendMessage("§fAuto Guard: §b" + BungeePlugin.autoGuard);
				sender.sendMessage("§fSensitive: §b" + BungeePlugin.sensitive);
				sender.sendMessage("§fWhiteList: §b" + BungeePlugin.whiteList.size());
				sender.sendMessage("§fBanned IP's: §b" + BungeePlugin.blackList.size());
				sender.sendMessage(" ");
				sender.sendMessage("§bCommands: §f/ancore guard, /ancore unban, /ancore banlist");
			}
		}
		else
		{
			sender.sendMessage("§8[§fAn§bBot§8]: §fУ вас недостаточно прав.");
		}
	}

}
