package OdlAddon;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TSA {

	private static String NUM_OF_MIDDLEBOXES_HEADER = "number of middleboxes:";
	private static String MIDDLEBOXES_PATTERNS_SETS_HEADER = "middleboxes patterns sets:";
	private static String POLICIES_CHAINS_HEADER = "policies chains:";

	private DPIController dpiController;

	public static void main(String[] args) {
		System.out.println("TSA !!");
		TSA tsa = new TSA(10000);
		tsa.installFromConfigFile("C:\\Users\\orenstal\\git\\DPIController\\DPIController\\src\\OdlAddon\\configureFiles");
	}

	public TSA(int port) {
		this.dpiController = new DPIController(port);
	}

	// this method receives source and destination host ids, and returns all the paths between them.
	// remember that the policy chain includes only middleboxes (hosts) and not the switches and routers,
	// so i need to find all the paths between two hosts.
	public LinkedList<LinkedList<Link>> getAllPathsBetweenHosts(String srcHostId, String dstHostId) {
		return null;
	}


	public void installFromConfigFile(String path) {
		System.out.println("start installing from config file..");
		BufferedReader br = null;
		try {
	    	br = new BufferedReader(new FileReader(path));
	        String line = br.readLine();

	        while (line != null && !line.contains(NUM_OF_MIDDLEBOXES_HEADER)) {
	        	line = br.readLine();
	        }

	        int num = Integer.valueOf(line.substring(NUM_OF_MIDDLEBOXES_HEADER.length()).trim());
	        System.out.println("num of middleboxes: " + num);
	        line = br.readLine();

	        if (line != null && line.contains(MIDDLEBOXES_PATTERNS_SETS_HEADER)) {
	        	line = br.readLine();

	        	while (line != null && !line.contains(POLICIES_CHAINS_HEADER)) {

	        		// register the middleboxes
	        		Pattern p = Pattern.compile("^([^:]+)[\\s]*:[\\s]*(.+)$");
	        		Matcher m = p.matcher(line);

	        		if (m.matches()) {
	        			this.dpiController.register(m.group(1), m.group(2), false, false);
	        		}

		            line = br.readLine();
		        }
	        }

	        if (line != null)
	        	line = br.readLine();

	        // add policies chains
	        while (line != null) {
	            String newPolicyChain = dpiController.addPolicyChain(line);
	            installPolicyChain(newPolicyChain);

	            line = br.readLine();
	        }
	    } catch (Exception e) {
			e.printStackTrace();
		} finally {
	        try {
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
	    }

		System.out.println("done installing from config file..");
	}


	private void installPolicyChain(String policyChain) {
		// TODO: not implemented yet !!
		// supposed to install the policy chain in the ODL Controller.

		/*
		 * 1. the tsa should be singleton.
		 * 2. tsa data structures:
		 * 		a. the tsa should have a hashmap of all the installed rules of all the
		 * 		   switches.
		 * 		   the hashmap supposed to be of type <swtichId-string, List<flowObject>>,
		 * 		   where the flowObject contains all the flow matching fields.
		 *
		 * 		b. the tsa should have a hashmap of
		 * 		   type <vlanTag-string, policyChains - string[]>
		 *
		 * 		c. the tsa should have a hashmap of
		 * 		   type <hostId, Pair<hostMac, switchId-string>>
		 * 		   where the switchId is the one that the host id directly connected to
		 *
		 * 3. the tsa should install the suitable flows on each switch in order to support
		 * 	  the policy chains. thus, the tsa should have an access to the network
		 * 	  topology, including the port to reach from some node to its specific
		 * 	  neighbor. then, for each two neighbors in the policy chain, the tsa should
		 * 	  calculate a path (of switches) between them and install the flows for each
		 * 	  switch in the path. maybe i need to change the network graph to holds for
		 * 	  each vertex (switch) also its neighbors and the port to reach them.
		 * 	  OPEN ISSUE: how can i install an action in one of the middleboxes (hosts)
		 * 	  that forward the packet to the next middlebox ??
		 *
		 * 3. in the "onPacketReceive" of l2switch module, i need to check if the received
		 * 	  packet has vlan's value that exist in 2.b data structure. if not - do the
		 * 	  regular operations (before my changes). if yes - decide which flow to
		 * 	  install, i need to insert it also to the 2.a data structure.
		 *
		 *
		 * 4. i need to see where in the code the l2-switch removes a flow, and add the
		 * 	  code that removes it also from the tsa hashmap.
		 *
		 * 5.
		 * 6.
		 */
	}


//	private void openTSAListener(int port) {
//		ServerSocket echoServer = null;
//        String line;
//        BufferedReader is;
//        PrintStream os;
//        Socket clientSocket = null;
//
//        // Try to open a server socket on port 9999
//        try {
//        	echoServer = new ServerSocket(9999);
//        }
//        catch (IOException e) {
//        	System.out.println(e);
//        }
//
//
//		// Create a socket object from the ServerSocket to listen and accept
//		// connections.
//		// Open input and output streams
//        try {
//        	clientSocket = echoServer.accept();
//        	is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//
//        	os = new PrintStream(clientSocket.getOutputStream());
//
//        	// As long as we receive data, echo that data back to the client.
//        	while (true) {
//        		line = is.readLine();
//        		os.println(line);
//        	}
//        }
//
//        catch (IOException e) {
//        	System.out.println(e);
//        }
//    }

}
