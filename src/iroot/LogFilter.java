package iroot;

import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class LogFilter implements Filter 
{

    @Override
    public boolean isLoggable(LogRecord lr) 
    {
    	if ((lr.getLevel() == Level.SEVERE)||(lr.getLevel() == Level.WARNING)) return false;
        String msg = lr.getMessage();
        if (msg.contains("{0} has connected")||msg.contains("UpstreamBridge")||msg.contains("InitialHandler")||msg.contains("{0} has disconnected")||msg.contains("disconnected")||msg.contains("Exception")||msg.contains("exception")||msg.contains("ServerConnector")) {
            return false;
        }
        return true;
    }
}