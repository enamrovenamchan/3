package ServerClasses;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

public class Server {
	
	boolean running = true;
	int portNumber = 85858;
	ArrayList<Thready> openThreads = new ArrayList<Thready>();
	ArrayList<Message> messages = new ArrayList<Message>();
	

	
	public static void main (String[] args){
		System.out.println("Starte Server");
		
		Server server = new Server();
		server.start();
	}

	
	
	public void start(){
		
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
	
	public class Thready extends Thread{
		
		private Socket client;
		private BufferedReader input;
		private PrintWriter output;
		private boolean running = true;
		
		public Thready (Socket client){
			this.client=client;
			try {
				this.output = new PrintWriter(client.getOutputStream());
				this.input = new BufferedReader(
						new InputStreamReader(client.getInputStream()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		}
		
		public void start(){
			while(running){
				try {
					String firstInput = input.readLine();
					
					if(firstInput.startsWith("W ")){
						String time = firstInput.substring(2);
						Timestamp timestamp = new Timestamp(Long.parseLong(time));
						sendMessages(timestamp);
					}
					if(firstInput.startsWith("P ")){
						
					}
					if(firstInput.startsWith("T ")){
	
					}
					if(firstInput.startsWith("L ")){
	
					}
					if(firstInput.startsWith("X ")){
						kill();
					}
					if(firstInput.startsWith("N ")){
	
					}
					
					
					
					
					int anzahlNachrichten = Integer.parseInt(firstInput);
					for(;anzahlNachrichten>0; anzahlNachrichten--){
						int zeilenAnzahl = input.read();
						Message message = new Message(zeilenAnzahl);
						String theme = input.readLine();
						message.setTheme(theme.substring(theme.indexOf(" ")));
						Date datum = new Date();
						message.setTimestamp(new Timestamp(datum.getTime()));
						String[] text = new String[zeilenAnzahl-1];
						for(int counter=0; counter<zeilenAnzahl-1;counter++){
							text[counter] = input.readLine();
						}
						message.setMessages(text);
						addMessage(message);
					}
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.err.println(e.getMessage());
					e.printStackTrace();
				}
				
			}
		}
		
		public void sendMessages(Timestamp timestamp){
			ArrayList<Message> searchedMessages = new ArrayList<Message>();
			synchronized(messages){
				for(Message message: messages){
					if(message.getTimestamp().after(timestamp)){
						searchedMessages.add(message);
					}
				}
				output.println(searchedMessages.size());
				for(Message message: searchedMessages){
					output.print(message.getSize());
					output.print(message.getTimestamp()+" "+message.getTheme());
					String[] lines = message.getMessages();
					for(int counter = lines.length; counter>0;counter--){
						output.print(lines[counter]);
					}
				}
			}
		}
		
		public void kill(){
			this.running=false;
		}
	}
	
	public synchronized void kill(){
		running=false;
		for(Thready thread : openThreads){
			thread.kill();
			thread.interrupt();
		}
	}

	public void addMessage(Message message){
		messages.add(message);
	}
	
	public void addThready(Thready thready){
		openThreads.add(thready);
	}

}
