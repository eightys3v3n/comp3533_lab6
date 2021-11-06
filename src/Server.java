import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author eightys3v3n
 *
 * Tick the option in Run/Debug < Launching < Terminate and Relaunch while launching.
 * Otherwise Eclipse just keeps rerunning the program and continues to eat up more ports.
 * 
 * This program can't handle keys that are subsets of other keys. Should be changed to use a
 * TreeMap or order all codes by length, longest to shortest.
 */
public class Server {
	static String CODE_BOOK_PATH = "codebook.txt";
	static HashMap<String, String> codeBook;
	static String WORD_REGEX = "([\\w/\\d]+)";
	
	public static void main(String argv[]) throws Exception {
		String clientSentence;
		String capitalizedSentence;
		boolean running = true;
		
		codeBook = loadCodeBook(Paths.get(CODE_BOOK_PATH));

		try (ServerSocket welcomeSocket = new ServerSocket(6789)){
			System.out.println("Listening for connections...");
			
			while (running) {
				Socket connectionSocket = welcomeSocket.accept();
				BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
				DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
				List<String> words;
				List<String> decodedMessage = new ArrayList<String>();
				System.out.println("Accepted TCP connection from" 
						+ connectionSocket.getInetAddress() 
						+ ":" + connectionSocket.getPort());
				
				try {
					while (true) {
						clientSentence = inFromClient.readLine();
						if (clientSentence == null) {
							running = false;
							break;
						}
						
						outToClient.writeBytes(decodeMessage(clientSentence));
					}
				} catch (Exception e) {
					// TODO: handle exception, if client closed connection, print:
					System.out.println("Client closed connection.");
					System.err.println(e);
					welcomeSocket.close();
				}
			}
		}
	}
	
	
	/**Reads the specified filename, splitting it by tabs into the first half and second half.
	 * Only allows a single tab on each line, ignores everything else.
	 * 
	 * @param filename Filename to read
	 * @return Left of first tab : Right of first tab
	 * @throws IOException
	 */
	public static HashMap<String,String> loadCodeBook(Path filename)
			throws IOException {
		BufferedReader reader = Files.newBufferedReader(filename);
		String line = "";
		List<String> splitLine;
		HashMap<String, String> acronyms = new HashMap<String, String>();
		
		for (int l = 0; true; l++) {
			line = reader.readLine();
			if (line == null)
				break;
			
			splitLine = Arrays.asList(line.split("\t"));
			
			if (splitLine.size() == 2) {
				acronyms.put(splitLine.get(0).toUpperCase(),
							 splitLine.get(1).toUpperCase());
				
				System.out.printf("Loaded normal line %s:%s\n", splitLine.get(0), splitLine.get(1));
			} else {
				System.err.printf("Too few tabs in data line %d with %d sections: \"%s\"",
						l,
						splitLine.size(),
						line);
			}
		}
		
		return acronyms;
	}
	
	/**
	 * Returns all keys in codeBook that are contained in the message.
	 * Case sensitive.
	 * @param message Message to look through.
	 * @return List of keys from codeBook that are contained in message.
	 */
	public static List<String> findCodeWords(String message) {
		List<String> foundCodes = new ArrayList<String>();
		Pattern wordPattern = Pattern.compile(WORD_REGEX);
		Matcher match = wordPattern.matcher(message);
		
		while(match.find()) {	
			if (codeBook.containsKey(match.group(1))) {
				foundCodes.add(match.group(1));
				System.out.printf("Found %s\n", match.group(1));
			} else {
				System.out.printf("Not replacing %s\n", match.group(1));
			}
		}
		
		return foundCodes;
	}
	
	/**
	 * Decodes a message using the key:values in codeBook
	 * assuming all keys match the WORD_REGEX.
	 * 
	 * @param message Message to decode
	 * @return The decoded message..
	 */
	public static String decodeMessage(String message) {
		message = message.toUpperCase();
		List<String> foundCodes = findCodeWords(message);
		
		for (String code : foundCodes) {
			System.out.printf("Replacing '%s' with '%s'\n", code, codeBook.get(code));
			while (message.contains(code)) {
				message = message.replaceAll(code, codeBook.get(code));
			}
			System.out.println(message);
		}
		
		return message;
	}
}