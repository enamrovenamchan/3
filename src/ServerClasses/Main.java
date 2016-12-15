package ServerClasses;

public class Main {
	
	public static void main (String[] args){
		System.out.println("Starte Server");
		
		Server server = new Server();
		server.start();
	}

}
