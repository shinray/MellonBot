import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;

public class Chat {
	
	public class Message {
		
		public String name, message;
		public boolean mod;
		
		public Message(String line) {
			if (line.charAt(line.indexOf("mod=")+4) == '1') mod = true;
			else mod = false;
			name = line.substring(line.indexOf(':')+1, line.indexOf('!'));
			int ind = line.indexOf(':');
			message = line.substring(line.substring(ind+1).indexOf(':')+1+ind+1);
		}
		
		public String getName() { return this.name; }
		public String getMessage() { return this.message; }
		public boolean isMod() { return mod; }
	}
	
	
	static String server = "irc.twitch.tv";
	static int port = 6667;
	HashSet<String> raffle;
	int KappaCount;
	
	String name, channel, oauth;
	BufferedWriter writer;
	BufferedReader reader;
	
	public Chat(String n, String c) {
		this.name = n;
		this.channel = "#"+c;
		raffle = new HashSet<String>();
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
				if (m.getMessage().contains("Kappa")) {
					KappaCount++;
				} else if (m.getMessage().equals("!kappa")) {
					writer.write("PRIVMSG "+channel+" :Kappa count: "+KappaCount+"\r\n");
				} else if (m.getMessage().contains("test")) {
					writer.write("JOIN "+channel+"\r\n");
				} else if (m.getMessage().startsWith(("!followers"))) {
					try {
						int index = Integer.parseInt(m.getMessage().substring(11));
						String fi = Test.followers.get(index-1);
						writer.write("PRIVMSG "+channel+" :Follower "+(index)+" is "+fi+" with "+Test.fc.get(fi)+" followers.\r\n");
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if (m.getMessage().equals("!raffle")) {
					raffle.add(m.getName());
				} else if (m.getMessage().equals("!roll") && raffle.size() > 0) {
					int index = (int) (Math.random()*(raffle.size()));
					System.out.println(index +" "+ raffle.size());
					String winner = (String) raffle.toArray()[index];
					writer.write("PRIVMSG "+channel+" :Winner is "+winner+"!\r\n");
				} else if (m.getMessage().equals("!mod")) {
					if (m.isMod()) writer.write("PRIVMSG "+channel+" :"+m.getName()+" is a mod\r\n");
					else writer.write("PRIVMSG "+channel+" :"+m.getName()+" is not a mod\r\n");
				}
				writer.flush();
			} 
		}
	}
}
