package ServerClasses;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

public class Server {

	boolean running = true;
	int portNumber = 12345;
	ArrayList<Thready> openThreads = new ArrayList<Thready>();
	ArrayList<Message> messages = new ArrayList<Message>();
	

	public static void main(String[] args) {
		System.out.println("Starte Server");

		Server server = new Server();
		server.start();
	}

	public void start() {

		while (running) {
			try {
				ServerSocket serverSocket = new ServerSocket();
				serverSocket.bind(new InetSocketAddress(portNumber));
				Socket socket = serverSocket.accept();
				Thready Thready = new Thready(socket);
				Thready.start();
				addThready(Thready);
				serverSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public class Thready extends Thread {

		private BufferedReader input;
		private PrintWriter output;
		private boolean running = true;

		public Thready(Socket client) {
			try {
				this.output = new PrintWriter(client.getOutputStream());
				this.input = new BufferedReader(new InputStreamReader(client.getInputStream()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		}

		public void start() {
			while (running) {
				try {
					String firstInput = input.readLine();

					String beginningOfString = firstInput.substring(0, 1);
					switch (beginningOfString) {

					case "W ":
						String time = firstInput.substring(2);
						Timestamp timestamp = new Timestamp(Long.parseLong(time));
						sendMessages(timestamp);
						break;

					case "P ":
						ArrayList<Message> newMessages = new ArrayList<Message>();
						for (int anzahlNachrichten = input.read(); anzahlNachrichten > 0; anzahlNachrichten--) {
							int zeilenAnzahl = input.read();
							Message message = new Message(zeilenAnzahl);
							String timeStampAndtheme = input.readLine();
							message.setTheme(timeStampAndtheme.substring(timeStampAndtheme.indexOf(" ")));
							Date datum = new Date();
							message.setTimestamp(new Timestamp(datum.getTime()));
							String[] text = new String[zeilenAnzahl - 1];
							for (int counter = 0; counter < zeilenAnzahl - 1; counter++) {
								text[counter] = input.readLine();
							}
							message.setMessages(text);
							addMessage(message);
							newMessages.add(message);
						}
						sendToAll(newMessages);
						break;

					case "T ":
						String theme = firstInput.substring(2);
						sendMessages(theme);
						break;
					case "L ":
						ArrayList<Message> sortedMessages = sortByTimestamp(messages);
						if(firstInput.length()==1){
							sendMessages(sortedMessages);
						}
						int anzahlNachrichten = Integer.parseInt(firstInput.substring(2));
						if(anzahlNachrichten>=sortedMessages.size()){
							sendMessages(sortedMessages);
						}
						else{
							ArrayList<Message> messages = new ArrayList<Message>();
							for(int count=0;count<=anzahlNachrichten;count++){
								messages.add(sortedMessages.get(count));
							}
							sendMessages(messages);
						}
						break;
					case "X ": kill(); break;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.err.println(e.getMessage());
					e.printStackTrace();
				}

			}
		}
		
		public void sendMessages(Timestamp timestamp) {
			ArrayList<Message> searchedMessages = new ArrayList<Message>();
			synchronized (messages) {
				for (Message message : messages) {
					if (message.getTimestamp().after(timestamp)) {
						searchedMessages.add(message);
					}
				}
				output.println(searchedMessages.size());
				for (Message message : searchedMessages) {
					output.println(message.getSize());
					output.println(message.getTimestamp() + " " + message.getTheme());
					String[] lines = message.getMessages();
					for (int counter = lines.length; counter > 0; counter--) {
						output.println(lines[counter]);
					}
				}
			}
		}
		
		public void sendMessages(String theme){
			ArrayList<Message> searchedMessages = new ArrayList<Message>();
			synchronized (messages) {
				for (Message message : messages) {
					if (message.getTheme().equals(theme)) {
						searchedMessages.add(message);
					}
				}
				searchedMessages = sortByTimestamp(searchedMessages);
				sendMessages(searchedMessages);
			}
		}
		
		public ArrayList<Message> sortByTimestamp(ArrayList<Message> messages){
			Message[] messagesArray = new Message[messages.size()];
			for(int i = messages.size(); i>0 ; i--){
				messagesArray[i]=messages.get(i);
			}
			for(int i=0;i<messagesArray.length;i++){
				Message bubble = messagesArray[i];
				for(int j = i+1; j<messagesArray.length;j++){
					if(messagesArray[j].getTimestamp().before(bubble.getTimestamp())){
						Message store = messagesArray[j];
						messagesArray[j]=messagesArray[i];
						messagesArray[i]=store;
					}
				}
			}
			ArrayList<Message> sortedMessages = new ArrayList<Message>();
			for(Message message : messagesArray){
				sortedMessages.add(message);
			}
			return sortedMessages;
		}

		public void kill() {
			this.running = false;
		}
		
		public void sendMessages(ArrayList<Message> messages){
			output.println(messages.size());
			for (Message message : messages) {
				output.println(message.getSize());
				output.println(message.getTimestamp() + " " + message.getTheme());
				String[] lines = message.getMessages();
				for (int counter = lines.length; counter > 0; counter--) {
					output.println(lines[counter]);
				}
			}
		}
	}

	public synchronized void sendToAll(ArrayList<Message> messages){
		//TODO
		for(Thready thread : openThreads){
			PrintWriter out = thread.output;
			out.println(messages.size());
			for(Message message : messages){
				out.println(message.getSize());
				out.println(message.getTimestamp()+" "+message.getTheme());
				String[] texts = message.getMessages();
				for(int i = texts.length;i<texts.length;i++){
					out.println(texts[i]);
				}
			}
		}
	}
	
	public synchronized void kill() {
		running = false;
		for (Thready thread : openThreads) {
			thread.kill();
			thread.interrupt();
		}
	}

	public synchronized void addMessage(Message message) {
		messages.add(message);
	}

	public void addThready(Thready thready) {
		openThreads.add(thready);
	}

}
