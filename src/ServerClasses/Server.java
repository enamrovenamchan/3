package ServerClasses;

import java.net.Socket;
import java.util.ArrayList;

public class Server {
	
	boolean running = true;
	int portNumber = 85858;
	ArrayList<Thread> openThreads = new ArrayList<Thread>();
	
	public void start(){
		
		while (running) {
			ServerThread waitForConnections = new ServerThread();
			Socket socket = waitForConnections.waitForConnections(portNumber);
			Thread thread = new Thread(socket);
			thread.start();
			addThread(thread);
		}
		
	}
	
	public void kill(){
		running=false;
	}
	
	public void addThread(Thread thread){
		openThreads.add(thread);
	}

}
