import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;

public class Chat {
	
	public class Message {
		
		public String name, message;
		public String[] words;
		public boolean mod;
		
		public Message(String line) {
			if (line.charAt(line.indexOf("mod=")+4) == '1') mod = true;
			else mod = false;
			name = line.substring(line.indexOf("display-name=")+13, line.indexOf(";emotes="));
			String tmp = line.substring(line.indexOf("PRIVMSG"));
			message = tmp.substring(tmp.indexOf(':')+1);
			words = message.split(" ");
		}
		
		public String getName() { return this.name; }
		public String getMessage() { return this.message; }
		public boolean isMod() { return mod; }
		public String getCommand() { return this.words[0]; }
		public String[] getWords() { return this.words; }
	}
	
	
	static String server = "irc.twitch.tv";
	static int port = 6667;
	HashMap<String, String> cmds;
	HashSet<String> raffle;
	boolean raffleOpen = false;
	String raffleWord = "!raffle";
	int KappaCount;
	
	String name, channel, oauth;
	BufferedWriter writer;
	BufferedReader reader;
	
	public Chat(String n, String c) {
		this.name = n;
		this.channel = "#"+c;
		raffle = new HashSet<String>();
		cmds = new HashMap<String, String>();
	}
	
	public void connect() throws UnknownHostException, IOException {
		BufferedReader bf = new BufferedReader(new FileReader(name+".txt"));
		oauth = bf.readLine();
		Socket s = new Socket(server, port);
		writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
		reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
	}
	
	public void run() throws IOException {
		writer.write("PASS "+oauth+"\r\n");
		writer.write("NICK "+name+"\r\n");
		writer.flush();
		String line = null;
		writer.write("CAP REQ :twitch.tv/membership\r\n");
		writer.write("CAP REQ :twitch.tv/tags\r\n");
		writer.write("JOIN "+channel+"\r\n");
		writer.write("PRIVMSG "+channel+" :MellonBot activated\r\n");
		writer.flush();
		while ((line = reader.readLine()) != null) {
			System.out.println(line);
			if (line.startsWith("PING ")) {
				writer.write("PONG "+line.substring(5)+"\r\n");
				writer.flush();
			} else if (line.contains("PRIVMSG")) {
				Message m = new Message(line);
				otherChatParse(m);
				runCommands(m);
				writer.flush();
			} 
		}
	}
	
	public void otherChatParse(Message msg) {
		if (msg.getMessage().contains("Kappa")) 
			KappaCount++;
	}
	
	public void runCommands(Message msg) throws IOException{
		String com = msg.getCommand();
		if (com.equals("!kappa")) {
			writer.write("PRIVMSG "+channel+" :Kappa count: "+KappaCount+"\r\n");
		} else if (com.equals(("!followers"))) {
			try {
				int index = Integer.parseInt(msg.getWords()[1]);
				String fi = Test.followers.get(index-1);
				writer.write("PRIVMSG "+channel+" :Follower "+(index)+" is "+fi+" with "+Test.fc.get(fi)+" followers.\r\n");
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (com.equals("!openRaffle")) {
			if (raffleOpen) {
				writer.write("PRIVMSG "+channel+" :Raffle is already open. Close current raffle first.\r\n");
				return;
			}
			raffle.clear();
			if (msg.getWords().length == 2) raffleWord = "!"+msg.getWords()[1];
			else raffleWord = "!raffle";
			writer.write("PRIVMSG "+channel+" :New raffle opened with keyword: "+raffleWord+"\r\n");
			raffleOpen = true;
		} else if (com.equals("!closeRaffle")) {
			raffleOpen = false;
		} else if (raffleOpen && msg.getMessage().equals(raffleWord)) {
			System.out.println(msg.getName()+" added to raffle");
			raffle.add(msg.getName());
		} else if (raffleOpen && com.equals("!roll") && raffle.size() > 0) {
			int index = (int) (Math.random()*(raffle.size()));
			System.out.println(index +" "+ raffle.size());
			String winner = (String) raffle.toArray()[index];
			writer.write("PRIVMSG "+channel+" :Winner is "+winner+"!\r\n");
		} else if (com.equals("!mod")) {
			if (msg.isMod()) writer.write("PRIVMSG "+channel+" :"+msg.getName()+" is a mod\r\n");
			else writer.write("PRIVMSG "+channel+" :"+msg.getName()+" is not a mod\r\n");
		} else if (com.equals("!addCommand")) {
			String[] words = msg.getWords();
			if (words.length < 3) return;
			StringBuilder s = new StringBuilder();
			for (int i = 2; i < msg.getWords().length; i++)	s.append(words[i]+" ");
			cmds.put("!"+words[1], s.toString());
		} else if (cmds.containsKey(com)) {
			writer.write("PRIVMSG "+channel+" :"+cmds.get(com)+"\r\n");
		}
	}
}
