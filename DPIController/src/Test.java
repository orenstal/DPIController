import java.util.ArrayList;

import org.json.*;

public class Test {

	public static void main(String[] args) {

		System.out.println("start init - by desktop");
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


		test1(middleboxes, middleboxesInPolicyChains);
//		test2(middleboxes, middleboxesInPolicyChains, policyChains);

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
}
