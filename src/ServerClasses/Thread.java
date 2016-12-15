package ServerClasses;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Timestamp;

public class Thread {
	
	private Socket client;
	private BufferedReader input;
	private PrintWriter output;
	private boolean running = true;
	
	public Thread (Socket client){
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
				int zeilenAnzahl = input.read();
				Message message = new Message(zeilenAnzahl);
				String timestampAndTheme = input.readLine();
				message.setTimestamp(new Timestamp(
						Long.parseLong(
								timestampAndTheme
										.substring(0, timestampAndTheme.indexOf(" ")-1))));
				String[] text = new String[zeilenAnzahl-2];
				for(int counter=0; counter<zeilenAnzahl-2;counter++){
					text[counter] = input.readLine();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
			
		}
		
	}
	
	public void kill(){
		
	}

}
