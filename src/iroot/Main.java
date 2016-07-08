package iroot;

import org.spacehq.mc.auth.data.GameProfile;
import org.spacehq.mc.auth.exception.request.RequestException;
import org.spacehq.mc.protocol.MinecraftProtocol;
import org.spacehq.mc.protocol.MinecraftConstants;
import org.spacehq.mc.protocol.data.SubProtocol;
import org.spacehq.mc.protocol.ServerLoginHandler;
import org.spacehq.mc.protocol.data.game.ItemStack;
import org.spacehq.mc.protocol.data.game.Position;
import org.spacehq.mc.protocol.data.game.values.entity.player.GameMode;
import org.spacehq.mc.protocol.data.game.values.setting.Difficulty;
import org.spacehq.mc.protocol.data.game.values.window.WindowType;
import org.spacehq.mc.protocol.data.game.values.world.WorldType;
import org.spacehq.mc.protocol.data.game.values.world.map.MapData;
import org.spacehq.mc.protocol.data.game.values.world.map.MapPlayer;
import org.spacehq.mc.protocol.data.message.ChatColor;
import org.spacehq.mc.protocol.data.message.ChatFormat;
import org.spacehq.mc.protocol.data.message.Message;
import org.spacehq.mc.protocol.data.message.MessageStyle;
import org.spacehq.mc.protocol.data.message.TextMessage;
import org.spacehq.mc.protocol.data.message.TranslationMessage;
import org.spacehq.mc.protocol.data.status.PlayerInfo;
import org.spacehq.mc.protocol.data.status.ServerStatusInfo;
import org.spacehq.mc.protocol.data.status.VersionInfo;
import org.spacehq.mc.protocol.data.status.handler.ServerInfoBuilder;
import org.spacehq.mc.protocol.data.status.handler.ServerInfoHandler;
import org.spacehq.mc.protocol.data.status.handler.ServerPingTimeHandler;
import org.spacehq.mc.protocol.packet.ingame.client.ClientChatPacket;
import org.spacehq.mc.protocol.packet.ingame.server.ServerChatPacket;
import org.spacehq.mc.protocol.packet.ingame.server.ServerJoinGamePacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket;
import org.spacehq.mc.protocol.packet.ingame.server.window.ServerSetSlotPacket;
import org.spacehq.mc.protocol.packet.ingame.server.world.ServerMapDataPacket;
import org.spacehq.mc.protocol.packet.ingame.server.world.ServerSpawnPositionPacket;
import org.spacehq.packetlib.Client;
import org.spacehq.packetlib.Server;
import org.spacehq.packetlib.Session;
import org.spacehq.packetlib.event.server.ServerAdapter;
import org.spacehq.packetlib.event.server.SessionAddedEvent;
import org.spacehq.packetlib.event.server.SessionRemovedEvent;
import org.spacehq.packetlib.event.session.DisconnectedEvent;
import org.spacehq.packetlib.event.session.PacketReceivedEvent;
import org.spacehq.packetlib.event.session.SessionAdapter;
import org.spacehq.packetlib.tcp.TcpSessionFactory;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Proxy;
import java.util.Arrays;
import java.util.Random;

import com.octo.captcha.component.image.backgroundgenerator.BackgroundGenerator;
import com.octo.captcha.component.image.backgroundgenerator.UniColorBackgroundGenerator;
import com.octo.captcha.component.image.color.ColorGenerator;
import com.octo.captcha.component.image.color.RandomListColorGenerator;
import com.octo.captcha.component.image.fontgenerator.FontGenerator;
import com.octo.captcha.component.image.fontgenerator.RandomFontGenerator;
import com.octo.captcha.component.image.textpaster.SimpleTextPaster;
import com.octo.captcha.component.image.textpaster.TextPaster;
import com.octo.captcha.component.image.wordtoimage.ComposedWordToImage;

import iroot.BungeePlugin;

@SuppressWarnings({ "deprecation", "unused" })
public class Main
{
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 1512;
    private static Server server;

    public static void main(String[] args) 
    {
    	//Инициализация сервера
    	server = new Server(HOST, PORT, MinecraftProtocol.class, new TcpSessionFactory(Proxy.NO_PROXY));
    	
    	//Установка глобальных флагов (параметров)
        server.setGlobalFlag(MinecraftConstants.AUTH_PROXY_KEY, Proxy.NO_PROXY);
        server.setGlobalFlag(MinecraftConstants.VERIFY_USERS_KEY, false);
        server.setGlobalFlag(MinecraftConstants.SERVER_COMPRESSION_THRESHOLD, 100);
        server.setGlobalFlag(MinecraftConstants.ACCESS_TOKEN_KEY, false);
        server.setGlobalFlag(MinecraftConstants.SERVER_PING_TIME_HANDLER_KEY, 0);

        //Перехват события входа игрока на сервер
        server.setGlobalFlag(MinecraftConstants.SERVER_LOGIN_HANDLER_KEY, new ServerLoginHandler() 
        {
	        @Override
	        public void loggedIn(Session session) 
	        {
		    	session.send(new ServerJoinGamePacket(0, false, GameMode.SURVIVAL, 0, Difficulty.PEACEFUL, 0, WorldType.DEFAULT, false));
		        session.send(new ServerSpawnPositionPacket(new Position(0,0,0)));
		        session.send(new ServerPlayerPositionRotationPacket(0,0,0,0,0));
		        GameProfile profile = session.getFlag("profile");
		        BungeePlugin.sendPlayer(profile.getName());
		        
		        //Генерация капчи и выдача ёё игроку
		        session.send(new ServerChatPacket(new TextMessage("§8§l[§f§lAn§b§lBot§8§l]: §6§lПожалуйста, введите цифры которые вы видите в чат!")));
		        if (!BungeePlugin.strong_captcha)
		        {
		        	String code = "";
		        	for (int i = 1; i <= BungeePlugin.captcha_length; i++)
		        	{
		        		code = code + random(0, 9);
		        	}
		        	session.setFlag("cap", code);
		        	getCaptcha(code, session);
		        }
		        else
		        {
		        	String code = "";
		        	for (int i = 1; i <= BungeePlugin.captcha_length; i++)
		        	{
		        		code = code + rc();
		        	}
		        	session.setFlag("cap", code);
		        	getCaptcha(code, session);
		        }
		        Log("Total online: " + server.getSessions().size());
	        }
        });
            
        //Перехват события выхода игрока с сервера
        server.addListener(new ServerAdapter() 
        {
	        @Override
	        public void sessionRemoved(SessionRemovedEvent event) 
	        {
		         MinecraftProtocol protocol = (MinecraftProtocol) event.getSession().getPacketProtocol();
		         if(protocol.getSubProtocol() == SubProtocol.GAME) 
		         {
		        	 Log("Total online: " + server.getSessions().size());
		         }
	        }
        });

        //Запуск сервера
        server.bind();
        Log("Server enabled!");
    }
    
    public static void Log(String text)
    {
    	System.out.println("§8[§fAn§bCore§8]: §f" + text);
    }
    
	public static void getCaptcha(String cap, Session s)
    {
    	WindowType wt = WindowType.GENERIC_INVENTORY;
    	s.send(new ServerSetSlotPacket(wt.ordinal(), 36, new ItemStack(358)));
    	final Font font = new Font( "Arial", Font.BOLD, 12 );
    	final FontGenerator fontGen = new RandomFontGenerator( BungeePlugin.font_min, BungeePlugin.font_max, new Font[]{ font } );
    	final ColorGenerator colorGen = new RandomListColorGenerator( new Color[]{ Color.RED, Color.GREEN, Color.BLUE, Color.BLACK } );
    	final BackgroundGenerator bgGen = new UniColorBackgroundGenerator( 128, 128, Color.WHITE );
    	final TextPaster textPaster = new SimpleTextPaster( BungeePlugin.captcha_length, BungeePlugin.captcha_length, colorGen );
    	final ComposedWordToImage wordToImage = new ComposedWordToImage( fontGen, bgGen, textPaster );
    	
        BufferedImage img = wordToImage.getImage(cap);
        
        //Костыль
        String result = "";
	    for (int i=25; i < 128; i++)
	    {
	    	String t = "";
		    for (int j=0; j< 128; j++)
		    {
			    if (img.getRGB(j, i) == -1)
			    {
			    	t = t+"!";
			    }
			    else
			    {
			    	t = t+"G";
			    }
		    }
		    result = result+t;
	    }
	    //Костыль №2
	    for (int i=1; i <= 25; i++)
	    {
	    	String t = "";
	    	for (int j=0; j< 128; j++)
		    {
	    		t = t+"!";
		    }
	    	result = result+t;
	    }
	    MapPlayer[] mp = {};
	    MapData md = new MapData(127, 127, 1, 1, result.getBytes());
	    ServerMapDataPacket t = new ServerMapDataPacket(0, (byte)1, mp, md);
	    s.send(t);
	}
	
	//Генерация случайного числа в диапазоне
	public static int random(int min, int max)
	{
		return (min + (int)(Math.random() * ((max - min) + 1)));
	}
	
	//Генерация случайного символа из строки
    private static char rc()
    {
    	 Random r = new Random();

    	    String alphabet = "qwertyuiopasdfghjklzxcvbnm0123456789";
    	    return alphabet.charAt(r.nextInt(alphabet.length()));

    }
	
    //Получение капчи выданой игроку
    public static String getNameCaptcha(String playerName)
    {
    	for (Session s : server.getSessions()) 
        {
    		GameProfile profile = s.getFlag("profile");
            if (profile.getName().equals(playerName))
            {
            	return s.getFlag("cap");
            }
        }
    	return "0";
    }
    
    //Выключение сервера
    public static void stopServer()
    {
    	for (Session s : server.getSessions()) 
        {
    		s.disconnect("Капча сервер остановлен!");
        }
    	server.close();
    }
}
