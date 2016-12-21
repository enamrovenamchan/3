package ClientClasses;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client {
	
	public static void main(String[] args){
		System.out.println("Starte Client");
		boolean running = true;
		Socket mySocket = new Socket();
		try {
			mySocket.connect(new InetSocketAddress("192.168.178.33", 12345));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PrintWriter out = null;
		BufferedReader input;
		try {
			out=new PrintWriter(mySocket.getOutputStream(),true);
			input=new BufferedReader(new InputStreamReader(	mySocket.getInputStream()));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		while(running){
			String inbut = null;
			try {
				inbut = in.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			out.println(inbut);
			
		}
	}
	
}
