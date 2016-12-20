package ServerClasses;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class Server {

	boolean running = true;
	int portNumber = 85858;
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

		private Socket client;
		private BufferedReader input;
		private PrintWriter output;
		private boolean running = true;

		public Thready(Socket client) {
			this.client = client;
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

						int anzahlNachrichten = Integer.parseInt(firstInput);
						for (; anzahlNachrichten > 0; anzahlNachrichten--) {
							int zeilenAnzahl = input.read();
							Message message = new Message(zeilenAnzahl);
							String theme = input.readLine();
							message.setTheme(theme.substring(theme.indexOf(" ")));
							Date datum = new Date();
							message.setTimestamp(new Timestamp(datum.getTime()));
							String[] text = new String[zeilenAnzahl - 1];
							for (int counter = 0; counter < zeilenAnzahl - 1; counter++) {
								text[counter] = input.readLine();
							}
							message.setMessages(text);
							addMessage(message);
						}
						sendToAll();

					case "T ":
						String theme = firstInput.substring(2);
						sendMessages(theme);
					case "L ":
					case "X ": kill();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.err.println(e.getMessage());
					e.printStackTrace();
				}

			}
		}
		
		public void sendToAll(){
			//TODO
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
					output.print(message.getSize());
					output.print(message.getTimestamp() + " " + message.getTheme());
					String[] lines = message.getMessages();
					for (int counter = lines.length; counter > 0; counter--) {
						output.print(lines[counter]);
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
				output.println(searchedMessages.size());
				sortByTimestamp(searchedMessages);
				for (Message message : searchedMessages) {
					output.print(message.getSize());
					output.print(message.getTimestamp() + " " + message.getTheme());
					String[] lines = message.getMessages();
					for (int counter = lines.length; counter > 0; counter--) {
						output.print(lines[counter]);
					}
				}
			}
		}
		
		public void sortByTimestamp(ArrayList<Message> messages){
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
		}

		public void kill() {
			this.running = false;
		}
	}

	public synchronized void kill() {
		running = false;
		for (Thready thread : openThreads) {
			thread.kill();
			thread.interrupt();
		}
	}

	public void addMessage(Message message) {
		messages.add(message);
	}

	public void addThready(Thready thready) {
		openThreads.add(thready);
	}

}
