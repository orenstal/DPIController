package OdlAddon;



import java.io.IOException;
import org.json.JSONObject;

public class DPIInstance {
	private String _name;
	private int _index;

	public DPIInstance(String name, int index) {
		_name = name;
		_index = index;
		System.out.println("new DPI instance: name: '" + name + "', index: '" + index);
	}


	public String getName() {
		return _name;
	}

	public int getIndex() {
		return _index;
	}


	public synchronized boolean sendMessage(JSONObject jsonMessage) throws IOException {
//		String message = jsonMessage.toString();
//		Socket socket = null;
//
//	    try {
//	        socket = new Socket(_ip, _port);
//
//	        OutputStreamWriter osw = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
//	        osw.write(message, 0, message.length());
//	    } catch (Exception e) {
//	        System.err.print(e);
//	    } finally {
//	        socket.close();
//	    }
//
        return true;
	}


	@Override
	public String toString() {
		return "DPIInstance [_name=" + _name + ", _index=" + _index + "]";
	}


//	public void run() {
//		try {
//			BufferedReader in = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
//	        PrintWriter out = new PrintWriter(_socket.getOutputStream(), true);
//
//	        while (true) {
//	        	String input = in.readLine();
//	        	if (input == null || input.equals("close")) {
//	        		break;
//	        	}
//
//	        	// TODO - remove: send the received message in upper case !!
//	        	out.println(input.toUpperCase());
//
//	        	/*
//	        	 * here i need to parse the received message and then handle it.
//	        	 */
//            }
//
//			System.out.println("dpi start listening: " + this.toString());
//			// TODO: read this: http://cs.lmu.edu/~ray/notes/javanetexamples/ !!
//		} catch (IOException e) {
//			System.out.println("an error occuerd on dpi instance: " + this.toString());
//        } finally {
//        	try {
//        		_socket.close();
//            } catch (IOException e) {
//                System.out.println("an error occuerd during closing the socket on dpi instance: " + this.toString());
//            }
//
//        	System.out.println("dpi instance is closed: " + this.toString());
//        }
//
//	}



}
