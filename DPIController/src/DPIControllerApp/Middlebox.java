package DPIControllerApp;

/**
 * This class represents a middlebox in the network.
 * NOTE: I think that it is represented by "Node" class in the OpenDayLight source code !!
 */


import org.json.JSONObject;

public class Middlebox {
	private String _name;
	private int _index;
	private String _patternSet;
	private boolean _isFlow;
	private boolean _isStealth;

	public Middlebox(String name, int index, String patternSet, boolean isFlow, boolean isStealth) {
		_name = name;
		_index = index;
		_patternSet = patternSet;
		_isFlow = isFlow;
		_isStealth = isStealth;
	}


	public int getIndex() {
		return _index;
	}

	public String getName() {
		return _name;
	}

	public String getPatternSet() {
		return _patternSet;
	}

	public synchronized void setPatternSet (String newPatternsSet) {
		_patternSet = newPatternsSet;
	}


	@Override
	public String toString() {
		return "Middlebox [_name=" + _name + ", _index=" + _index + ", _patternSet="
				+ _patternSet + "]";
	}


//	public synchronized boolean sendMessage(JSONObject jsonMessage) throws IOException {
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
//        return true;
//	}



}
