import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Test {
	
	public static String name = "allenmelon";
	public static String baseURL = "https://api.twitch.tv/kraken/";
	
	public static void main(String[] args) {
		Chat chat = new Chat("mellonbot", name);
		try {
			chat.connect();
			chat.run();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("finishing");
	}
	
	public static void getMostPopularFollower(String name) {
		try {
			ArrayList<String> followers = buildFollowerList(name);
			String most = followers.get(0);
			long m = 0;
			long i = 0;
			for (String s : followers) {
				System.out.println(i);
				i++;
				try {
					long c = getFollowerCount(s);
					if (c > m) {
						m = c;
						most = s;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			System.out.println("Most popular follower is "+most+" with "+m+" followers.");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static JSONObject getData(String link) throws IOException, ParseException {
		JSONParser parser = new JSONParser();
		JSONObject obj;
		URL url = new URL(link);
		URLConnection con = url.openConnection();
		Scanner scan = new Scanner(con.getInputStream());
		String line = "";
		while (scan.hasNextLine()) line += scan.nextLine();
		obj = (JSONObject)parser.parse(line);
		scan.close();
		return obj;
	}
	
	public static long getFollowerCount(String n) throws MalformedURLException, IOException {
		Scanner scan = new Scanner(new URL(baseURL+"channels/"+n+"/").openStream());
		long c = Long.parseLong(scan.findInLine("\"followers\":(\\d+)").substring(12));
		scan.close();
		return c;
	}
	
	public static ArrayList<String> buildFollowerList(String u) throws IOException, ParseException {
		ArrayList<String> followers = new ArrayList<String>();
		JSONObject user = getData(baseURL+"channels/"+u+"/follows?limit=100");
		long total = (long)user.get("_total");
		while (followers.size() < total) {
			JSONArray list = (JSONArray)user.get("follows");
			for (Object f : list) {
				followers.add((String)((JSONObject)((JSONObject)f).get("user")).get("name"));
				System.out.println(followers.size());
			}
			user = getData((String)((JSONObject)(JSONObject)user.get("_links")).get("next"));
		}
		return followers;
	}

}
