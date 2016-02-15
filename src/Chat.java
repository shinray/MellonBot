import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class Chat {
	
	static String server = "irc.twitch.tv";
	static int port = 6667;
	int KappaCount;
	String name, channel, oauth;
	BufferedWriter writer;
	BufferedReader reader;
	
	public Chat(String n, String c) {
		this.name = n;
		this.channel = "#"+c;
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
		writer.write("JOIN "+channel+"\r\n");
		writer.write("PRIVMSG "+channel+" :MellonBot activated\r\n");
		writer.flush();
		while ((line = reader.readLine()) != null) {
			System.out.println(line);
			if (line.startsWith("PING ")) {
				writer.write("PONG "+line.substring(5)+"\r\n");
				writer.flush();
			} else if (line.contains("PRIVMSG")) {
				line = line.substring(line.substring(1).indexOf(':')+2);
				if (line.contains("Kappa")) {
					KappaCount++;
				} else if (line.equals("!kappa")) {
					writer.write("PRIVMSG "+channel+" :Kappa count: "+KappaCount+"\r\n");
				} else if (line.contains("test")) {
					writer.write("JOIN "+channel+"\r\n");
				} else if (line.startsWith(("!followers"))) {
					try {
						int index = Integer.parseInt(line.substring(11));
						String fi = Test.followers.get(index-1);
						writer.write("PRIVMSG "+channel+" :Follower "+(index)+" is "+fi+" with "+Test.fc.get(fi)+" followers.\r\n");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				writer.flush();
			} 
		}
	}
}
