import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	String CODE_BOOK_PATH = "codebook.txt";
	
	public static void main(String argv[]) throws Exception {
		String clientSentence;
		String capitalizedSentence;

		ServerSocket welcomeSocket = new ServerSocket(6789);
		
		codeBook = loadCodeBook(CODE_BOOK_PATH);

		try {
			while (true) {
	
				Socket connectionSocket = welcomeSocket.accept();
				BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
				DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
				System.out.println("Accepted TCP connection from" 
						+ connectionSocket.getInetAddress() 
						+ ":" + connectionSocket.getPort());
				try {
					while (true) {
						clientSentence = inFromClient.readLine();
	
						capitalizedSentence = clientSentence.toUpperCase() + '\n';
	
						outToClient.writeBytes(capitalizedSentence);
					}
				} catch (Exception e) {
					// TODO: handle exception, if client closed connection, print:
					System.out.println("Client closed connection.");
					welcomeSocket.close();
				}
			}
		} catch (Exception e) {
			welcomeSocket.close();
		}
	}
	
	public static void loadCodeBook(String filename) {
		
	}
	
	public static void saveCodeBook(String filename) {
		
	}
}