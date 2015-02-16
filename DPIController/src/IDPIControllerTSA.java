

import java.rmi.Remote;

public interface IDPIControllerTSA extends Remote {
	public RETURN_VALUE updatePolicyChain(String policyChain);
	
	
}
