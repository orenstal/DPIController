package DPIControllerApp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.json.*;

public class Test {

	private static final String COMMAND_STRING = "command";
	// the commands must be in lower case !!
	private static final String REGISTER_MIDDLEBOX_COMMAND = "registermiddleboxcommand";
	private static final String ADD_POLICY_CHAIN_COMMAND = "addpolicychaincommand";
	private static final String REMOVE_POLICY_CHAIN__COMMAND = "removepolicychaincommand";
	private static final String SET_PATTERNS_SET_COMMAND = "setpatternssetcommand";
	private static final String PRINT_DPI_CONTROLLER_STATUS_COMMAND = "printdpicontrollertatuscommand";

	private static final String MIDDLEBOX_NAME_STRING = "middlebox name";
	private static final String PATTERNS_SET_STRING = "patterns set";
	private static final String FLOW_FLAG_BOOLEAN_STRING = "flow flag";
	private static final String STEALTH_FLAG_BOOLEAN_STRING = "stealth flag";
	private static final String POLICY_CHAIN_STRING = "policy chain";

	public static void main(String[] args) {


		System.out.println("start init - by desktop");
		System.out.println("desktop test");
		int numOfMiddleBoxes = 4;
		// create the middleboxes
		Middlebox[] middleboxes = new Middlebox[numOfMiddleBoxes];
		String[] middleboxesInPolicyChains = new String[numOfMiddleBoxes];
		middleboxesInPolicyChains[0] = "mb0,mb1";
		middleboxesInPolicyChains[1] = "mb1,mb0";
		middleboxesInPolicyChains[2] = "mb0,mb1,mb2,mb3";
		middleboxesInPolicyChains[3] = "mb2,mb3,mb1";

		for (int i=0; i< numOfMiddleBoxes; i++) {
			middleboxes[i] = new Middlebox("mb" + Integer.toString(i), i, ("Ps" + Integer.toString(i)), false, false);
			System.out.println(middleboxes[i].toString() + " is created");
		}

//		for (int i=0; i< numOfMiddleBoxes; i++) {
//			policyChains[i] = new PolicyChain("127.0.0.1", (i + 6000), middleboxesInPolicyChains[i]);
//			System.out.println(policyChains[i].toString() + " is created");
//		}

		System.out.println("done init");
		System.out.println("----------------------------------------------------------------------------------------");

//		ArrayList<ArrayList<Integer>> a = new ArrayList<ArrayList<Integer>>();
//
//		a.add(new ArrayList<Integer>());
//		a.add(new ArrayList<Integer>());
//
//		System.out.println(a.size());
//
//		a.get(0).add(0);
//		a.get(0).add(1);
//		a.get(1).add(2);
//
//
//
//		for (int i=0; i< a.get(0).size(); i++) {
//			for (int j=0; j<a.size(); j++) {
//				System.out.print(a.get(j).get(i) + " ");
//			}
//			System.out.println();
//		}


//		test1(middleboxes, middleboxesInPolicyChains);
//		test2(middleboxes, middleboxesInPolicyChains, policyChains);
		test3(middleboxes, middleboxesInPolicyChains);

//		JSONObject obj = new JSONObject();
//		try {
//			obj.put("fname",  "tal");
//			obj.put("lname",  "orenstein");
//
//
//			System.out.println("obj is: " + obj.toString());
//
//			String obj1 = "{\"fname\":\"liraz\",\"lname\":\"orenstein\"}";
//			JSONObject obj2;
//
//			obj2 = new JSONObject(obj1);
//			System.out.println(obj2.get("fname"));
//		} catch (JSONException e) {
//			System.out.println("some error... !!");
//		}



	}

	private static void printReturnValue (String methodName, RETURN_VALUE ret) {
		if (ret == RETURN_VALUE.ALREADY_EXIST)
			System.out.println(methodName + " returned: already exist");
		else if (ret == RETURN_VALUE.SUCCESS)
			System.out.println(methodName + " returned: success");
		else
			System.out.println(methodName + " returned: failed");
	}

	private static void test1(Middlebox[] middleboxes, String[] middleboxesInPolicyChains) {
		System.out.println("start TEST 1");

		DPIController dpiController = new DPIController(10000);

		dpiController.register(middleboxes[0].getName(), middleboxes[0].getPatternSet(), false, false);

		dpiController.register(middleboxes[1].getName(), middleboxes[1].getPatternSet(), false, false);

		dpiController.printStatus();

		dpiController.addPolicyChain(middleboxesInPolicyChains[0]);

		dpiController.addPolicyChain(middleboxesInPolicyChains[1]);

		dpiController.printStatus();

		dpiController.register(middleboxes[2].getName(), middleboxes[2].getPatternSet(), false, false);
		dpiController.register(middleboxes[3].getName(), middleboxes[3].getPatternSet(), false, false);

		dpiController.addPolicyChain(middleboxesInPolicyChains[2]);
		dpiController.addPolicyChain(middleboxesInPolicyChains[3]);

		dpiController.printStatus();
		System.out.println("start removing...");
		dpiController.removePolicyChain("inst2," + middleboxesInPolicyChains[2]);

		dpiController.printStatus();

		dpiController.setPatternsSet(middleboxes[0].getName(), "2,3");

		dpiController.printStatus();


//
//		ret = dpiController.register(middleboxes[0]);
//		printReturnValue("register[0]", ret);
//		ret = dpiController.register(middleboxes[3]);
//		printReturnValue("register[3]", ret);
//
//		dpiController.addPolicyChain(policyChains[2]);
//
//		ret = dpiController.register(middleboxes[1]);
//		printReturnValue("register[1]", ret);
//
//		ret = dpiController.register(middleboxes[2]);
//		printReturnValue("register[2]", ret);
//
//		dpiController.addPolicyChain(policyChains[3]);
//
//		dpiController.printStatus();
//
//		dpiController.removePolicyChain(policyChains[2]);
//		dpiController.removePolicyChain(policyChains[1]);
//		dpiController.removePolicyChain(policyChains[3]);
//		dpiController.removePolicyChain(policyChains[0]);
//
//		ret = dpiController.register(middleboxes[0]);
//		printReturnValue("register[0]", ret);
//		dpiController.printStatus();
//
//		dpiController.addPolicyChain(policyChains[2]);
//		dpiController.addPolicyChain(policyChains[3]);
//
//		dpiController.printStatus();
		System.out.println("done TEST 1");
		System.out.println("----------------------------------------------------------------------------------------");
	}

//	private static void test2(Middlebox[] middleboxes, String[] middleboxesInPolicyChains, PolicyChain[] policyChains) {
//		System.out.println("start TEST 2");
//		RETURN_VALUE ret;
//		DPIController dpiController = new DPIController(10000);
//
//		dpiController.addPolicyChain(policyChains[1]);
//		dpiController.addPolicyChain(policyChains[2]);
//
//		ret = dpiController.register(middleboxes[0]);
//		printReturnValue("register[0]", ret);
//		ret = dpiController.register(middleboxes[1]);
//		printReturnValue("register[1]", ret);
//		ret = dpiController.register(middleboxes[2]);
//		printReturnValue("register[2]", ret);
//
//		dpiController.printStatus();
//
//		dpiController.setPatternsSet("1", "NEW_Ps1");
//		dpiController.setPatternsSet("2", "NEW_Ps2");
//
//		dpiController.printStatus();
//		System.out.println("done TEST 2");
//		System.out.println("----------------------------------------------------------------------------------------");
//	}

	private static void sendAndPrint(int dpiPort, JSONObject jsonObj, String msgToPrint) {
		Socket socket;
		BufferedReader in = null;
		PrintWriter out = null;
		String retString = "";

		try {
			socket = new Socket("127.0.0.1", dpiPort);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}


		try {

			out.println(jsonObj);
			String line = in.readLine();

			if (line != null)
				retString = line;

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println(msgToPrint + retString);
	}



	private static void test3(Middlebox[] middleboxes, String[] middleboxesInPolicyChains) {
		System.out.println("start TEST 3");

		int dpiPort = 9091;
		new DPIController(dpiPort);

		JSONObject jsonObj;


		// 1. register first middlebox
		jsonObj = new JSONObject();
		try {
			jsonObj.put(COMMAND_STRING, REGISTER_MIDDLEBOX_COMMAND);
			jsonObj.put(MIDDLEBOX_NAME_STRING, middleboxes[0].getName());
			jsonObj.put(PATTERNS_SET_STRING, middleboxes[0].getPatternSet());
			jsonObj.put(FLOW_FLAG_BOOLEAN_STRING, false);
			jsonObj.put(STEALTH_FLAG_BOOLEAN_STRING, false);

			sendAndPrint(dpiPort, jsonObj, "1. first register ret val: ");
		} catch (Exception e) {
			e.printStackTrace();
		}



		// 2. register second middlebox
		jsonObj = new JSONObject();
		try {
			jsonObj.put(COMMAND_STRING, REGISTER_MIDDLEBOX_COMMAND);
			jsonObj.put(MIDDLEBOX_NAME_STRING, middleboxes[1].getName());
			jsonObj.put(PATTERNS_SET_STRING, middleboxes[1].getPatternSet());
			jsonObj.put(FLOW_FLAG_BOOLEAN_STRING, false);
			jsonObj.put(STEALTH_FLAG_BOOLEAN_STRING, false);

			sendAndPrint(dpiPort, jsonObj, "2. second register ret val: ");
		} catch (Exception e) {
			e.printStackTrace();
		}



		// 3. first print status
		jsonObj = new JSONObject();
		try {
			jsonObj.put(COMMAND_STRING, PRINT_DPI_CONTROLLER_STATUS_COMMAND);

			sendAndPrint(dpiPort, jsonObj, "3. print status ret val: ");
		} catch (Exception e) {
			e.printStackTrace();
		}


		// 4. add first policy chain
		jsonObj = new JSONObject();
		try {
			jsonObj.put(COMMAND_STRING, ADD_POLICY_CHAIN_COMMAND);
			jsonObj.put(POLICY_CHAIN_STRING, middleboxesInPolicyChains[0]);

			sendAndPrint(dpiPort, jsonObj, "4. first adding policy chain ret val: ");
		} catch (Exception e) {
			e.printStackTrace();
		}



		// 5. add second policy chain
		jsonObj = new JSONObject();
		try {
			jsonObj.put(COMMAND_STRING, ADD_POLICY_CHAIN_COMMAND);
			jsonObj.put(POLICY_CHAIN_STRING, middleboxesInPolicyChains[1]);

			sendAndPrint(dpiPort, jsonObj, "5. second adding policy chain ret val: ");
		} catch (Exception e) {
			e.printStackTrace();
		}



		// 6. second print status
		jsonObj = new JSONObject();
		try {
			jsonObj.put(COMMAND_STRING, PRINT_DPI_CONTROLLER_STATUS_COMMAND);

			sendAndPrint(dpiPort, jsonObj, "6. print status ret val: ");
		} catch (Exception e) {
			e.printStackTrace();
		}



		// 7. register third middlebox
		jsonObj = new JSONObject();
		try {
			jsonObj.put(COMMAND_STRING, REGISTER_MIDDLEBOX_COMMAND);
			jsonObj.put(MIDDLEBOX_NAME_STRING, middleboxes[2].getName());
			jsonObj.put(PATTERNS_SET_STRING, middleboxes[2].getPatternSet());
			jsonObj.put(FLOW_FLAG_BOOLEAN_STRING, false);
			jsonObj.put(STEALTH_FLAG_BOOLEAN_STRING, false);

			sendAndPrint(dpiPort, jsonObj, "7. third register ret val: ");
		} catch (Exception e) {
			e.printStackTrace();
		}



		// 8. register fourth middlebox
		jsonObj = new JSONObject();
		try {
			jsonObj.put(COMMAND_STRING, REGISTER_MIDDLEBOX_COMMAND);
			jsonObj.put(MIDDLEBOX_NAME_STRING, middleboxes[3].getName());
			jsonObj.put(PATTERNS_SET_STRING, middleboxes[3].getPatternSet());
			jsonObj.put(FLOW_FLAG_BOOLEAN_STRING, false);
			jsonObj.put(STEALTH_FLAG_BOOLEAN_STRING, false);

			sendAndPrint(dpiPort, jsonObj, "8. fourth register ret val: ");
		} catch (Exception e) {
			e.printStackTrace();
		}



		// 9. add third policy chain
		jsonObj = new JSONObject();
		try {
			jsonObj.put(COMMAND_STRING, ADD_POLICY_CHAIN_COMMAND);
			jsonObj.put(POLICY_CHAIN_STRING, middleboxesInPolicyChains[2]);

			sendAndPrint(dpiPort, jsonObj, "9. third adding policy chain ret val: ");
		} catch (Exception e) {
			e.printStackTrace();
		}



		// 10. add fourth policy chain
		jsonObj = new JSONObject();
		try {
			jsonObj.put(COMMAND_STRING, ADD_POLICY_CHAIN_COMMAND);
			jsonObj.put(POLICY_CHAIN_STRING, middleboxesInPolicyChains[3]);

			sendAndPrint(dpiPort, jsonObj, "10. fourth adding policy chain ret val: ");
		} catch (Exception e) {
			e.printStackTrace();
		}



		// 11. third print status
		jsonObj = new JSONObject();
		try {
			jsonObj.put(COMMAND_STRING, PRINT_DPI_CONTROLLER_STATUS_COMMAND);

			sendAndPrint(dpiPort, jsonObj, "11. print third ret val: ");
		} catch (Exception e) {
			e.printStackTrace();
		}



		System.out.println("start removing...");

		// 12. remove first policy chain
		jsonObj = new JSONObject();
		try {
			jsonObj.put(COMMAND_STRING, REMOVE_POLICY_CHAIN__COMMAND);
			jsonObj.put(POLICY_CHAIN_STRING, "inst2," + middleboxesInPolicyChains[2]);

			sendAndPrint(dpiPort, jsonObj, "12. first removing policy chain ret val: ");
		} catch (Exception e) {
			e.printStackTrace();
		}



		// 13. third print status
		jsonObj = new JSONObject();
		try {
			jsonObj.put(COMMAND_STRING, PRINT_DPI_CONTROLLER_STATUS_COMMAND);

			sendAndPrint(dpiPort, jsonObj, "13. print fourth ret val: ");
		} catch (Exception e) {
			e.printStackTrace();
		}



		// 14. first set patterns set
		jsonObj = new JSONObject();
		try {
			jsonObj.put(COMMAND_STRING, SET_PATTERNS_SET_COMMAND);
			jsonObj.put(MIDDLEBOX_NAME_STRING, middleboxes[0].getName());
			jsonObj.put(PATTERNS_SET_STRING, "2,3");

			sendAndPrint(dpiPort, jsonObj, "14. first setting patterns set ret val: ");
			System.out.println("");
		} catch (Exception e) {
			e.printStackTrace();
		}



		// 15. fifth print status
		jsonObj = new JSONObject();
		try {
			jsonObj.put(COMMAND_STRING, PRINT_DPI_CONTROLLER_STATUS_COMMAND);

			sendAndPrint(dpiPort, jsonObj, "15. print fifth ret val: ");
		} catch (Exception e) {
			e.printStackTrace();
		}


		System.out.println("done TEST 3");
		System.out.println("----------------------------------------------------------------------------------------");

	}

}
