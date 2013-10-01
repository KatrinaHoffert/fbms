package cmpt370.fbms;

import java.util.concurrent.locks.ReentrantLock;
import java.nio.file.*;
import java.util.*;

public class DbManager
{
	public static ReentrantLock lock = new ReentrantLock();
	
	public static void init()
	{
		
	}
	
	public static List<RevisionInfo> getRevisionData(Path file)
	{
		return null;
	}
	
	public static RevisionInfo getRevisionInfo(Path file, int timestamp)
	{
		return null;
	}
	
	public static void insertRevision(Path file, String diff, int timestamp, int delta)
	{
		
	}
	
	public static void renameFile(Path file, Path newName)
	{
		
	}
	
	public static String getConfig(String settingName)
	{
		return null;
	}
	
	public static void setConfig(String settingName, String settingValue)
	{
		
	}
}
