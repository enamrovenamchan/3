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

public class Server implements Runnable {

	boolean running = true;
	static int portNumber = 12345;
	ArrayList<Thready> openThreads = new ArrayList<Thready>();
	ArrayList<Message> messages = new ArrayList<Message>();
	ServerSocket serverSocket = null;

	public static void main(String[] args) {
		System.out.println("Starte Server");
		System.out.println("Port ist: " + portNumber);

		Server server = new Server();
		new Thread(server).start();
	}

	public void run() { // run-Methode für den Server

		while (running) {
			try {
				serverSocket = new ServerSocket();
				serverSocket.bind(new InetSocketAddress(portNumber));
				Socket socket = serverSocket.accept();
				Thready Thready = new Thready(socket);
				addThready(Thready); // Threads merken damit wir später an alle senden können
				new Thread(Thready).start();
				serverSocket.close();
			} catch (IOException e) {
				System.out.println("Fehler beim erstellen einer neuen Verbindung");
				System.out.println(e.getMessage());
				e.printStackTrace();
				running=false;
			}
		}
	}

	public class Thready implements Runnable {

		private BufferedReader input;
		private PrintWriter output;
		private boolean running = true;
		private Socket client;

		public Thready(Socket client) {
			try {
				this.output = new PrintWriter(client.getOutputStream(), true);
				this.input = new BufferedReader(new InputStreamReader(client.getInputStream()));
				this.client = client;
			} catch (IOException e) {
				System.out.println("Fehler beim holen des Writers und oder Readers");
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		}

		public void run() {
			while (running) {
				try {
					String firstInput = input.readLine();

					String beginningOfString = firstInput.substring(0, 1);
					int anzahlNachrichten;
					String vilEineZahl;
					switch (beginningOfString) {

					case "W":
						vilEineZahl = firstInput.substring(2);
						try {
							Timestamp timestamp = new Timestamp(Long.parseLong(vilEineZahl));
							sendMessages(timestamp);
						} catch (NumberFormatException e1) {
							output.println("Nach dem >>W<< hat der Server einen ''TimeStamp'' erwartet. Du hast aber >>"+vilEineZahl+"<< eingegeben");
							output.println("Damit kann der jetzt nichts anfangen. Versuchs nochmal!");
							System.out.println("Fehler beim einlesen der Zahl nach dem >>L<<. War wohl keine Zahl");
							e1.printStackTrace();
						}
						break;

					case "P":
						ArrayList<Message> newMessages = new ArrayList<Message>();
						anzahlNachrichten=Integer.parseInt(input.readLine());	//einlesen wieviele Nachrichten kommen werden
						for (; anzahlNachrichten > 0; anzahlNachrichten--) { // jede Nachricht einlesen
							int zeilenAnzahl = Integer.parseInt(input.readLine());
							Message message = new Message(zeilenAnzahl);
							String timeStampAndtheme = input.readLine();
							message.setTheme(timeStampAndtheme.substring(timeStampAndtheme.indexOf(" ") + 1)); //Timestamp ist unwichtig, schneiden wir ab
							Date datum = new Date();
							message.setTimestamp(new Timestamp(datum.getTime())); // Timestamp setzten
							String[] text = new String[zeilenAnzahl - 1];// Für jede Zeile der Nachricht einen String anlegen
							for (int counter = 0; counter < zeilenAnzahl - 1; counter++) {
								text[counter] = input.readLine();
							}
							message.setMessages(text);
							addMessage(message); // Server soll sich Message merken
							newMessages.add(message); // Temporäre Liste die sich die neuen Nachrichten merkt über welche die anderen Clients noch Informiert werden müssen
						}
						sendToAll(newMessages); // Alle informieren
						break;

					case "T":
						String theme = firstInput.substring(2);
						sendMessages(theme);
						break;
					case "L":
						ArrayList<Message> sortedMessages = sortByTimestamp(messages);
						if (firstInput.trim().length() == 1) {	//Wenn keine zahl angegeben wurde schicke alle Nachrichten
							sendMessages(sortedMessages);
						} else {
							vilEineZahl = firstInput.substring(2);
							try {
								anzahlNachrichten = Integer.parseInt(vilEineZahl);
								if (anzahlNachrichten >= sortedMessages.size()) {	// Ist die Zahl größer als die anzahl der NAchrichten, schicke auch alle
									sendMessages(sortedMessages);
								} else {
									ArrayList<Message> messages = new ArrayList<Message>();// Geforderte anzahl an nachrichten schicken
									for (int count = 0; count < anzahlNachrichten; count++) {
										messages.add(sortedMessages.get(count));
									}
									sendMessages(messages);
								}
							} catch (NumberFormatException e) {
								output.println("Nach dem >>L<< hat der Server eine Zahl erwartet. Du hast aber >>"+vilEineZahl+"<< eingegeben");
								output.println("Damit kann der jetzt nichts anfangen. Versuchs nochmal!");
								System.out.println("Fehler beim einlesen der Zahl nach dem >>L<<. War wohl keine Zahl");
								e.printStackTrace();
							}
						}
						break;
					case "X":
						kill();
						break;
					default:
						output.println("Der gesendete Befehl ist dem Server nicht bekannt");
						output.println("Entweder war der Befehl falsch oder bei der übertragung ist etwas verloren gegangen");
						output.println("Versuchen sie es doch einfach noch mal ;)");
					}
				} catch (IOException e) {
					System.out.println("Fehler beim einlesen einer Zeile");
					System.out.println("Eventuell ist der Socket nicht verfügbar?");
					System.out.println("Verbindung wird abgebaut");
					System.err.println(e.getMessage());
					e.printStackTrace();
					running = false;
					try {
						client.close();
					} catch (IOException e1) {
						System.out.println("Socket konnte nicht geschlossen werden");
						System.err.println(e.getMessage());
						e1.printStackTrace();
					}
				}

			}
		}

		public void sendMessages(Timestamp timestamp) {// Sendet alle Nachrichten dessen Zeitstämpel älter ist
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
					output.println(message.getTimestamp().getTime() + " " + message.getTheme());
					String[] lines = message.getMessages();
					for (int counter = lines.length; counter > 0; counter--) {
						output.println(lines[counter - 1]);
					}
				}
			}
		}

		public void sendMessages(String theme) {// Sendet alle Nachrichten-Köpfe (Zeilenanzahl,Timestamp und Thema) die das Thema "theme" haben
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

		public ArrayList<Message> sortByTimestamp(ArrayList<Message> messages) {// Sortiert die gegebene ArrayList nach Timestamp. Neueste zuerst
			Message[] messagesArray = new Message[messages.size()];
			for (int i = messages.size(); i > 0; i--) {
				messagesArray[i - 1] = messages.get(i - 1);
			}
			for (int i = 0; i < messagesArray.length; i++) {
				Message bubble = messagesArray[i];
				for (int j = i + 1; j < messagesArray.length; j++) {
					if (messagesArray[j].getTimestamp().after(bubble.getTimestamp())) {
						Message store = messagesArray[j];
						messagesArray[j] = messagesArray[i];
						messagesArray[i] = store;
					}
				}
			}
			ArrayList<Message> sortedMessages = new ArrayList<Message>();
			for (Message message : messagesArray) {
				sortedMessages.add(message);
			}
			return sortedMessages;
		}

		public synchronized void kill() {//Schließt die verbindung und den dazugehörigen Thread
			this.running = false;
			try {
				this.client.close();
			} catch (IOException e) {
				System.out.println("Client-Socket konnte nicht geschlossen werden");
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
			Thread.currentThread().interrupt();
			openThreads.remove(this);
		}

		public void sendMessages(ArrayList<Message> messages) {// Sendet die gegebenen Nachrichten an den Client
			output.println(messages.size());
			for (Message message : messages) {
				output.println(message.getSize());
				output.println(message.getTimestamp().getTime() + " " + message.getTheme());
				String[] lines = message.getMessages();
				for (int counter = lines.length; counter > 0; counter--) {
					output.println(lines[counter - 1]);
				}
			}
		}
	}

	public synchronized void sendToAll(ArrayList<Message> messages) {// Sendet die gegebenen Nachrichten an alle Clients
		for (Thready thread : openThreads) {
			PrintWriter out = thread.output;
			out.println("N " + messages.size());
			for (Message message : messages) {
				out.println(message.getTimestamp().getTime() + " " + message.getTheme());
			}
		}
	}

	public synchronized void kill() {
		this.running = false;
		for (Thready thread : openThreads) {
			thread.kill();
		}
	}

	public synchronized void addMessage(Message message) {
		messages.add(message);
	}

	public void addThready(Thready thready) {
		openThreads.add(thready);
	}

}
