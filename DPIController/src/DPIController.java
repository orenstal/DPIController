

/**
 * Step number 5.1 in SequenceDiagramV2.0 is removed. If I need to update the policy chain (policy chain) of a DPI instance, i
 * create a new instance and remove the previous one. so no need in this step (each DPI instance represents only one
 * policy chain).
 */

import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import org.json.*;


/*
 * good tutorial: http://tutorials.jenkov.com/java-concurrency/index.html
 * 
 * my general design
 * while(dpiController is active){
    listen for request
    hand request to worker thread
    }
 */

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////

// NEW DESIGN PROPOSAL:

/*
 * 
 * good example: http://cs.lmu.edu/~ray/notes/javanetexamples/
 * 
 * 
 * lets define the possible commands:
 * - "create instance [='addPolicyChain'], name=X". 
 * - "setPatternsSet=X".
 * - "addPatternsSet=X" (same as "updatePatternsSet" command).
 * - "remove policyChain=X".
 * - "register middlebox=X".
 */

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////



public class DPIController {
	
	// a mapping between a Middlebox name to the Middlebox id
	private ConcurrentMap <String, Middlebox> _registeredMiddleboxes;
	
	// a mapping between a dpi instance name to the dpi instance
	private ConcurrentMap <String, DPIInstance> _activeDpiInstances;
	
	// a mapping between a dpi index to dpi object
	private ConcurrentMap<Integer, DPIInstance> _dpiIndexToDPIObject;
	
	// create a matrix that looks like:
	// 			inst1 |	inst2 |	inst3
	// ----------------------------------
	// load  |
	// mb1Id |
	// mb2Id |
	// mb3Id |
	// mb4Id |
		
	// 
	// while all the middleboxes in the matrix were already registered. in addition, the value of each cell
	// is a number that represents the number of policy chains that using this middlebox for this dpi instance [0..]
	// the first column is for the load (index = 0)
	private int[][] _MBsDpiInstanceMatrix;
	
	// this linked list holds available indexes for the dpi instance 
	private ConcurrentLinkedQueue<Integer> _availableIndices;
		
	private String _ipAddrr;
	private int _port;
	private int _mbId;
	private int _instanceId;
	private int _nextSequentialDPIInstanceId;	// TODO maybe should be immutable primitive for thread-safe.
	
	
	
	private static final int WAIT_TIME_BEFORE_UPDATES_TAKE_PLACE = 2;
	private static final int WAIT_TIME_BEFORE_KILL_DPI_INSTANCE = 3;
	private static final String SEPERATOR = ",";
	private static final int numOfInstancesInTheBegining = 20;
	private static final int maxInst = 10;
	private static final int maxMB = 10;
	
	
	
	public DPIController(int port) {
		_ipAddrr = "127.0.0.1";
		_port = port;
		_mbId = 1;
		_instanceId = 0;
		_nextSequentialDPIInstanceId = 0;
		_registeredMiddleboxes  = new ConcurrentHashMap<String, Middlebox>(maxMB);
		_activeDpiInstances = new ConcurrentHashMap<String, DPIInstance>(maxInst);
		_dpiIndexToDPIObject = new ConcurrentHashMap<Integer, DPIInstance>(maxInst);

		_MBsDpiInstanceMatrix = new int[maxMB+1][maxInst];

		_availableIndices = new ConcurrentLinkedQueue<Integer>();
	}
	
//	public synchronized void init() {
//		// TODO: implement commandline and:
//		/*
//		 * while(dpiController is active){
//    	   listen for request
//    	   hand request to worker thread
//		 */
//	}
			
	// TODO: add threads, 'synchronization' and lock common data structures to the implementations !!!!
	// TODO: should i use callbacks??
	
	
	
	/*
	 * For the second note:
	 * could be a situation where the middlebox has been listed in the existing policy chains, but the
	 * lack of information regarding its required patterns set meant that the DFA representing
	 * this policy chain didn't check its patterns set. THUS:
	 * 1. run over all the policy chains and check if mb is part of the policy chain. If yes:
	 * 	1.1. RETRUN_VALUE ret = addPatternsSet(policyChain, mb.patternSet);
	 * 	1.2. if (ret == SUCCESS), continue.
	 * 	1.3. else: try again. if failed in the second time or time expired for this call, return FAILED.
	 */
	/**
	 * Let middleboxes to register for the DPI service. if the middlebox is alredy registered, nothing will happened.
	 * In case that SUCCESS is returned, the pattern set checking will take place after DPIController.WAIT_TIME_TO_START
	 * seconds.
	 * NOTE: All the packets that were sent and will be sent the next DPIController.WAIT_TIME_TO_START seconds after 
	 * RETURN_VALUE.SUCCESS is received by the middlebox, will not be checked by the DPI service.
	 * Note: could be a situtaion where the middlebox has been listed in some existing policy chain, but the middlebox
	 * didn't register until now (so it's pattern set are not scanned by the DPI instance). Therefore, we need to update
	 * the pattern set for this policy chain (i.e. update the DFA representing this policy chain).
	 * @param mb this object represents the middlebox object. mainly, it's id and required patterns set.
	 * @return RETURN_VALUE.ALREADY_EXIST if this middlebox is already registered. Otherwise, it returns RETURN_VALUE.SUCCESS 
	 */
	public RETURN_VALUE register(String mbName, String patternSet, boolean isFlow, boolean isStealth) {
				
		RETURN_VALUE ret = RETURN_VALUE.SUCCESS;
		
		int mbId;
		
		synchronized(this) {
			mbId = _mbId++;
		}

		_registeredMiddleboxes.put(mbName, new Middlebox(mbName, mbId, patternSet, isFlow, isStealth));
		
		return ret;
	}
	
	
	
	
	// NOTE: the TSA is the only one that calls to this function, so the return value informs the TSA about these values.
	// NOTE: addpolicyChain = create DPI instance
	/*
	 * 1. if (_activeDpiInstances.contains(policy chain)), return SUCCESS. else:
	 * 2. int port = generatePortNum()
	 * 3. _activeDpiInstances.put(policy chain, new DPIInstance(policy chain, this._ipAddrr, port))
	 * 4. return the ip address and the port number of the new DPI instance (for the received policy chain).
	 */
	/**
	 * 
	 * @param policyChain
	 * @return '' if all the middleboxes in the received policyChain didn't register yet
	 */
	public void addPolicyChain(String policyChain) {
		
		// returns the dpi instance id that will treat the received policy chain
		String dpiInstanceName = aggregate(policyChain);
		
		if (dpiInstanceName == null) {
			dpiInstanceName = createNewDPIInstance(policyChain); 
		}
		else {
			updateDPIInstanceCol(policyChain, dpiInstanceName, "add");
		}
		
		String newPolicyChain = addDPIInstanceToPolicyChain(policyChain, dpiInstanceName);
		
		sendMessageToTSA("replace policy chain: ;" + policyChain + "; with: ;" + newPolicyChain);
	}
	
	
	private String aggregate(String policyChain) {
		// TODO not implemented yet
		return null;
	}
	
	private String createNewDPIInstance(String policyChain) {
		
		int dpiIndex = generateDPIInstanceIndex();
		String dpiName = "inst" + dpiIndex;
		
		DPIInstance newInst = new DPIInstance(dpiName, dpiIndex);
		
		String patternSet = "";
		
		String[] mbNames = getMBNames(policyChain);
		
		for (int i=0; i<mbNames.length; i++) {
			patternSet += _registeredMiddleboxes.get(mbNames[i]).getPatternSet() + "#";
		}
		
		_activeDpiInstances.put(dpiName, newInst);
		_dpiIndexToDPIObject.put(dpiIndex, newInst);
		
		updateDPIInstanceCol(policyChain, dpiName, "add");
		
		// TODO must be with timeout or something..
		sendMessageToDPIInstanceServer("create new dpi instance: instanceName='" + dpiName +"', patternSet='" + patternSet + "'");
		
		return dpiName;
	}
	
	private String[] getMBNames (String policyChain) {
		return policyChain.split(",");		
	}
	
	
	private int[] getMBIds (String policyChain) {
		String[] splittedMBNames = policyChain.split(",");
		
		int[] mbIds = new int[splittedMBNames.length];
		
		for (int i=0; i< splittedMBNames.length; i++)
			if (_registeredMiddleboxes.get(splittedMBNames[i]) != null)
				mbIds[i] = _registeredMiddleboxes.get(splittedMBNames[i]).getIndex();

		return mbIds;
	}
	
	
	// returns true if the dpi instance should stay active (not all the values are 0).
	private boolean updateDPIInstanceCol (String policyChain, String dpiInstanceName, String command) {
		// not all the values is 0
		boolean shouldInstStay = false;

		int dpiInstanceIndex = _activeDpiInstances.get(dpiInstanceName).getIndex();
		
		int[] mbIds = getMBIds(policyChain);
		
		// add/remove one to all the middleboxes that take part in the received policy chain
		if (command.equals("add")) {
			// we start from 1 since i=0 represents the load
			for (int i=0; i<mbIds.length; i++) {
				_MBsDpiInstanceMatrix[mbIds[i]][dpiInstanceIndex]++;
			}
			shouldInstStay = true;
		}
		else if (command.equals("remove")) {
			// we start from 1 since i=0 represents the load
			for (int i=0; i<mbIds.length; i++) {
				int val = _MBsDpiInstanceMatrix[mbIds[i]][dpiInstanceIndex];
				
				if (val > 0)
					_MBsDpiInstanceMatrix[mbIds[i]][dpiInstanceIndex] = val-1;
				
				if (_MBsDpiInstanceMatrix[mbIds[i]][dpiInstanceIndex] > 0) {
					shouldInstStay = true;
				}
			}
		}
		
		return shouldInstStay;
	}
	
	
	// the function receives the policy chain and the selected dpi instance, and return a new policy chain in
	// which the dpi instance is part of the policy chain 
	private String addDPIInstanceToPolicyChain (String policyChain, String dpiInstanceName) {
		// TODO maybe i should get better dicision on *where* to add it		
		return dpiInstanceName + "," + policyChain;
	}
	
	
	private void sendMessageToTSA (String message) {
		// TODO not implemented yet
		System.out.println("send the following message to TSA: '" + message + "'");
	}
	
	private void sendMessageToDPIInstanceServer (String message) {
		// TODO not implemented yet
		System.out.println("send the following message to DPI Instance server: '" + message + "'");
	}
	
	private void sendMessageToOpenFlowController (String message) {
		// TODO not implemented yet
		System.out.println("send the following message to Open Flow controller: '" + message + "'");
	}
	
	private void sendMessageToMiddlebox (String message, Socket socket, String mbName) {
		// TODO not implemented yet
		System.out.println("send the following message to mb '" + mbName + "': '" + message + "'");
	}
	
	
	// this method extracts the dpi instance name from the policy chain (note that if the TSA asked to remove this
	// policy chain, it probably means that the policy chain includes one dpi instance).
	private String instanceNameExtraction (String policyChain) {

		String[] splitted = policyChain.split(",");
		
		for (int i=0; i< splitted.length; i++)
			if (splitted[i].startsWith("inst"))
				return splitted[i];
		
		return null;
	}
	


	// NOTE: the TSA is the only one that may cause to the calls of this function, so no need to notify the TSA about the removal.
	// NOTE: NOTE: removepolicy chain = remove DPI instance
	// NOTE: I decided not to update _unregisteredMiddleboxesInExistingPolicyChains hashmap since it may cost too much (and
	// by this, commit the purpose for which it was created). Alternatively, in the "register" method i checked if the
	// policy chain is still exists.
	/*
	 * 1. if (!_activeDpiInstances.contains(policy chain)), return SUCCESS. else:
	 * 2. call to killDPIInstance(policyChain)
	 * 3. remove policy chain from _activeDpiInstances
	 */
	public RETURN_VALUE removePolicyChain(String policyChain) {
		
		String dpiInstanceName = instanceNameExtraction(policyChain);
		boolean toRemoveInstance = !updateDPIInstanceCol(policyChain, dpiInstanceName, "remove");
		
		System.out.println("remove? " + toRemoveInstance);
		
		if (toRemoveInstance) {
			int dpiIndex = _activeDpiInstances.get(dpiInstanceName).getIndex();
			_availableIndices.add(dpiIndex);
			_dpiIndexToDPIObject.remove(dpiIndex);
			_activeDpiInstances.remove(dpiInstanceName);
			_MBsDpiInstanceMatrix[0][dpiIndex] = 0;
		}
		
		sendMessageToDPIInstanceServer("remove policy chain: " + policyChain);
		return RETURN_VALUE.SUCCESS;
	}
	
	
	
	// returns the next available port number.
	private synchronized int generateDPIInstanceIndex() {		
		if (_availableIndices.isEmpty())
			return _nextSequentialDPIInstanceId++;
		
		return _availableIndices.remove();
	}
	
	
	public RETURN_VALUE setPatternsSet(String mbName, String newPatternsSet) {
		/*
		 *  1. if (newPatternsSet.equals(mb.oldPatternsSet)), return SUCCESS.
		 * 	2. else:
		 * 		2.1. _registeredMiddleboxes[mb.getID()].setPatternSet(newPatternsSet);
		 * 		2.2. find the DPI instance (thread) from the thread pool, and return the returned value received by
		 * 			 calling to "setPatternSet(String newPatternsSet)" of the middlebox (this means to rebuild the DFA
		 * 			 for AC algorithm).
		 */
		
		Middlebox mb = _registeredMiddleboxes.get(mbName);
		
		System.out.println("set pattern set of '" + mbName +"' from: '" + mb.getPatternSet() + "' to: '" + newPatternsSet + "'");
		if (mb.getPatternSet().equals(newPatternsSet)) {
			sendMessageToMiddlebox("the new pattern set is equals to the old one",  null,  mb.getName());
			return RETURN_VALUE.ALREADY_EXIST;
		}
		
		mb.setPatternSet(newPatternsSet);
		
		int mbIndex = mb.getIndex();
		
		for (int i=0; i<maxInst; i++) {
			if (_MBsDpiInstanceMatrix[mbIndex][i] > 0) {
				sendMessageToDPIInstanceServer("in dpiInstance: '" + _dpiIndexToDPIObject.get(i).getName() + "' replace pattern set of mb '" + mbName + "' to be: '" + newPatternsSet + "'");
			}
		}
		
		
		return RETURN_VALUE.SUCCESS;
	}
	
	
		
	// command line for the DPI Controller
	public void commandLine(String[] splittedCommand) {
		String command = splittedCommand[1];
		if (command.equalsIgnoreCase("addPolicyChain")) {
			// bla bla
		}
		else if (command.equalsIgnoreCase("removePolicyChain")) {
			// bla bla
		}
		// .....
	}
	
	
	private void handleRequest(String msgAsString) {
		try {
			
			JSONObject msg = new JSONObject(msgAsString);
			String className = msg.getString("className");
			
			String mbName = msg.getString("name");
						
			switch (className.toLowerCase()) {
				case "middleboxregister":
					
					String id = msg.getString("id");
					boolean isFlow = msg.getBoolean("flow flag");
					boolean isStealth = msg.getBoolean("stealth flag");
										
					register(mbName, "", isFlow, isStealth);
					break;
				
				case "middleboxrulesetadd":
					String newRules = msg.getString("rules");
					
					setPatternsSet(mbName, newRules);
					break;
				
				case "middleboxrulesetremove":	// ????
					
					break;
					
				case "middleboxderegister":	// TODO implement
					
					break;
					
				case "rulestats":	// TODO implement
					
					break;
					
				case "rulestatsresponse":	// TODO implement
					
					break;
					
			}
			
			
		} catch (JSONException e) {
			System.out.println("invalid message (json) format");
			e.printStackTrace();
		}
	}
	
	
	public void printStatus() {
		System.out.println("----------------------------------------------------------------------------------------");
		System.out.println("the registered middleboxes are:");
		
		for (String key : _registeredMiddleboxes.keySet()) {
		    System.out.println(_registeredMiddleboxes.get(key).toString());
		}
		
		System.out.println("----------------------------------------------------------------------------------------");
		
		System.out.println("the active dpi instances are:");
		for (String name : _activeDpiInstances.keySet()) {
			System.out.println(_activeDpiInstances.get(name).toString());
		}
		
		System.out.println("----------------------------------------------------------------------------------------");
		
		System.out.println("the mapping between middleboxes ids and policy chains are:");
		
		for (int i=0; i<maxMB + 1; i++) {
			for (int j=0; j<maxInst; j++)
				System.out.print(_MBsDpiInstanceMatrix[i][j] + " ");
			System.out.println();
		}
	}
	
			
	////---------------- Needless --------------------
	
	// kill all the DPI instances related to the received policy chain
//	private void killAllDPIInstancesForPolicyChain(PolicyChain policyChain) {
//
//		ConcurrentLinkedQueue<DPIInstance> dpiInstances = _activeDpiInstances.get(policyChain);
//		ConcurrentLinkedQueue<Integer> releasedPortNums = new ConcurrentLinkedQueue<Integer>();
//
//		Iterator<DPIInstance> iter = dpiInstances.iterator();
//		while (iter.hasNext()) {
//			DPIInstance dpiInst = iter.next();
//			System.out.println("remove dpi instance: " + dpiInst.toString());
//			// TODO: Threads: kill all the threads related to the received policy chain (all the threads of dpiInstances linked list)
//			releasedPortNums.add(dpiInst.getPort());
//		}
//			
//
//			// TODO: addAll function behavior is undefined while changing the collection during this operation. Thus, I must
//			// lock this parameter (may collide with "generatePortNum" function).
//			_availablePortNums.addAll(releasedPortNums);
//			
//			System.out.println("waiting " + WAIT_TIME_BEFORE_KILL_DPI_INSTANCE + " seconds before kill the DPI instances related to this policyChain...");
//
//			try {
//				Thread.sleep(WAIT_TIME_BEFORE_KILL_DPI_INSTANCE*1000);	//1000 milliseconds is one second.
//			} catch(InterruptedException ex) {
//				Thread.currentThread().interrupt();
//			}
//
//			System.out.println("time passed.");
//
//		}
		
						
		// this method MUST work with timeout (first answer in: http://stackoverflow.com/questions/14069336/waiting-for-the-methods-return-value) !!
		// TODO: this method MUST be in new thread !! 
		// NOTE: This method does NOT create new instance, but update the existing ones!!
//		private RETURN_VALUE addPatternSet(PolicyChain policyChain, String newPatternSet) {
//			Iterator<DPIInstance> iter = _activeDpiInstances.get(policyChain).iterator();
//			
//			while (iter.hasNext()) {
//				DPIInstance dpiInst = iter.next();
//				if (!dpiInst.getPatternSets().contains(newPatternSet))
//					dpiInst.addToPatternsSet(newPatternSet);
//			}
//			
//			return RETURN_VALUE.SUCCESS;
//		}
//		
//		
//		public RETURN_VALUE setPatternsSet(String mbId, String newMiddleboxPatternSet) {
//			if (!_registeredMiddleboxes.containsKey(mbId))
//				return RETURN_VALUE.FAILED;
//			
//			String oldMiddleboxPatternSet = _registeredMiddleboxes.get(mbId).getPatternSet();
//			_registeredMiddleboxes.get(mbId).setPatternSet(newMiddleboxPatternSet);
//			
//			Iterator<PolicyChain> policyIter = _mappingMBIDsToPolicyChains.get(mbId).iterator();
//			
//			// run over all the policy chains that should be update according to the received pattern set
//			while (policyIter.hasNext()) {
//				// run over all the dpi instances implementing the policy chain.
//				Iterator<DPIInstance> dpiInstIter = _activeDpiInstances.get(policyIter.next()).iterator();
//				
//				while (dpiInstIter.hasNext()) {
//					DPIInstance dpiInst = dpiInstIter.next();
//					if (dpiInst.updatePatternsSet(oldMiddleboxPatternSet, newMiddleboxPatternSet) == RETURN_VALUE.FAILED)
//						return RETURN_VALUE.FAILED;
//				}
//			}
//			
//			// wait the time until the updates will take place in the dpi instances.
//			
//			// TODO: maybe i should run each "updatePatternsSet" in different thread, which will invoke callback that said that
//			// the changes take place.
//			
//			System.out.println("waiting " + WAIT_TIME_BEFORE_UPDATES_TAKE_PLACE + " seconds before the changes regarding the new patterns set will take place...");
//
//			try {
//				Thread.sleep(WAIT_TIME_BEFORE_UPDATES_TAKE_PLACE*1000);	//1000 milliseconds is one second.
//			} catch(InterruptedException ex) {
//				Thread.currentThread().interrupt();
//			}
//
//			System.out.println("time passed.");
//			
//			return RETURN_VALUE.SUCCESS;
//		}
		
	// Needless for now...
	/*
	 * after DPIController.WAIT_TIME_BEFORE_KILL_DPI_INSTANCE seconds pass, kill the thread of the received DPI instance.
	 */
//	private void killDPIInstance(PolicyChain policyChain, DPIInstance dpiInstanceToKill) {
//
//
//		Iterator<DPIInstance> iter = _activeDpiInstances.get(policyChain).iterator();
//
//		while (iter.hasNext()) {
//			DPIInstance dpiInstance = iter.next();
//
//			if (dpiInstance.equals(dpiInstanceToKill)) {
//				iter.remove();
//				return;
//			}
//		}		
//	}
	
			
	// Needless for now...			
	// run over the collected statistics of the instances, and check if need to duplicate instances for some policy chains
	private void checkInstancesLoad() {
		/*
		 * 1. run over all the dpi instances (threads), and check if the statistics is ok. else:
		 * 	1.1. if it is too busy:
		 * 		1.1.1. if the number of dpi instances (for this policy chain) == 1:
		 * 			1.1.1.1. set oldDpiInstance = _activeDpiInstances.get(policyChain).
		 * 			1.1.1.2. create new thread for fictitious DPI instance, and one more DPI instance for the busy instance (which will
		 * 				     create the same DFA).
		 * 			1.1.1.3. add them to _activeDpiInstances
		 * 			1.1.1.4. replace the value of "policy chain" (the old dpi instance) with the new fictitious dpi instance (which will call
		 * 				     to each dpi instance for this policy chain with uniform probability).
		 * 			1.1.1.5. notify the TSA to update the ip address and port number of the DPI Instance for this policy chain.
		 * 			1.1.1.6. call to killDPIInstance(oldDpiInstance) method, which kill the thread of the dpi instance after some seconds.
		 * 		1.1.2. if the number of dpi instances (for this policy chain) > 1:
		 * 			1.1.2.1. create new dpi instance for this policy chain.
		 * 			1.1.2.2. add it to _activeDpiInstances.
		 * 			1.1.2.3. update fictitious dpi instance about the new instance.
		 * 1.2. if the load is low:
		 * 		1.2.1. if the number of dpi instances == 1 - return.
		 * 		1.2.2. if the number of dpi instances == 2:
		 * 			1.2.2.1. replace the value of "policy chain" (the fictitious dpi instance) with the real existing dpi instance.
		 * 			1.2.2.2. notify the TSA to update the ip address and port number of the DPI Instance for this policy chain.
		 * 			1.2.2.3. call to killDPIInstance(fictitiousDpiInstance) method, which kill the thread of the dpi instance after some seconds.
		 * 			1.2.2.4. remove it from _activeDpiInstances.
		 * 		1.2.3. if the number of dpi instances > 2:
		 * 			1.2.3.1. set instanceToRemove to be one of the instances of this policy chain from _activeDpiInstances
		 * 			1.2.3.2. call to killDPIInstance(instanceToRemove)
		 * 			1.2.3.3. remove instanceToRemove from _activeDpiInstances
		 */ 
		 
	}
	
	
	// Needless for now...
	public void addDPIInstanceStatistics(DPIInstance dpiInst, String stats) {

	}
	
	
	
	//// ---------------- DEPRECATED --------------------
	/** 
	 * Let middleboxes to register for the DPI service. if the middlebox is alredy registered, nothing will happened.
	 * In case that SUCCESS is returned, the pattern set checking will take place after DPIController.WAIT_TIME_TO_START
	 * seconds.
	 * NOTE: All the packets that were sent and will be sent the next DPIController.WAIT_TIME_TO_START seconds after 
	 * RETURN_VALUE.SUCCESS is received by the middlebox, will not be checked by the DPI service.
	 * @param mb this object represents the middlebox object. mainly, it's id and required patterns set.
	 * @return RETURN_VALUE.ALREADY_EXIST if this middlebox is alredy registered. Otherwise, it returns RETURN_VALUE.SUCCESS 
	 */
//	public RETURN_VALUE registerANOTHER_OPTION(Middlebox mb) {
//		if (_registeredMiddleboxes.containsKey(mb.getId())) {
//			return RETURN_VALUE.ALREADY_EXIST;
//		}
//		
//		_registeredMiddleboxes.put(mb.getId(), mb);
		
		/*
		 * could be a situation where the middlebox has been listed in the existing policy chains, but the
		 * lack of information regarding its required patterns set meant that the DFA representing
		 * this policy chain didn't check its patterns set. THUS:
		 * 1. run over all the policy chains and check if mb is part of the policy chain. If yes:
		 * 	1.1. set oldDpiInstance = _activeDpiInstances.get(policy chain).
		 * 	1.2. create new thread for the new DPI instance, that will create DFA according to all the registered middleboxes
		 * 		 in the policy chain.
		 * 	1.3. replace the value of "policy chain" (the old dpi instance) with the new dpi instance.
		 * 	1.4. notify the TSA to update the ip address and port number of the DPI Instance for this policy chain.
		 * 	1.5. call to killDPIInstance(oldDpiInstance) method, which kill the thread of the dpi instance after some seconds.
		 * 	1.6. remove oldDpiInstance from _activeDpiInstances.
		 */
		
				
//		return RETURN_VALUE.SUCCESS;		
//	}
	
	
	// deprecated
	public RETURN_VALUE setPatternsSet(PolicyChain policyChain, String newPatternSet) {
		/*
		 *  1. if (policyChain.getPatternsSet().contains(newPatternSet)), return SUCCESS.
		 * 	2. else:
		 * 		2.1. _registeredMiddleboxes[mb.getID()].setPatternSet(newPatternsSet);
		 * 		2.2. policyChain.setPatternsSet(policyChain.getPatternsSet() + newPatternSet);
		 * 		2.3. find the DPI instance (thread) for this policy chain from the thread pool, and return the value that is returned 
		 * 			 by calling to "updatePatternsSet()" of the DPI Instance (this means to rebuild the DFA for AC algorithm).
		 */
		
		return RETURN_VALUE.SUCCESS;
	}

}
