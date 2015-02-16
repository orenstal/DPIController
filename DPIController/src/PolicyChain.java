/**
 * This class represents a policy chain in the TSA
 */


public class PolicyChain {

	private String _nextHopIP;
	private int	   _nextHopPort;
	private String _policyChain;
	
		
	public PolicyChain(String _nextHopIP, int _nextHopPort, String _policyChain) {
		this._nextHopIP = _nextHopIP;
		this._nextHopPort = _nextHopPort;
		this._policyChain = _policyChain;
	}

	
	public String[] getMiddleboxesIdInPolicy() {
		return _policyChain.split(",");
	}
	
	
	public String getNextHopIP() {
		return _nextHopIP;
	}
	
	public int getNextHopPortNum() {
		return _nextHopPort;
	}
	
	public String getPolicyChain() {
		return _policyChain;
	}
	
	
	public synchronized void setNextHopIP(String newIpAddrs) {
		_nextHopIP = newIpAddrs;
	}
	
	public synchronized void setNextHopPortNum(int newPortNum) {
		_nextHopPort = newPortNum;
	}
	
	// TODO maybe this function should be "synchronized" ??
	// the function returns true if the received middlebox id is part of the current policy chain.
	public boolean contains(String mbId) {
		String[] splittedMbsInPolicy = _policyChain.split(",");
		for (int i=0; i< splittedMbsInPolicy.length; i++) {
			if (splittedMbsInPolicy[i].trim().toLowerCase().equals(mbId.toLowerCase()))
				return true;
		}
		
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((_nextHopIP == null) ? 0 : _nextHopIP.hashCode());
		result = prime * result + _nextHopPort;
		result = prime * result
				+ ((_policyChain == null) ? 0 : _policyChain.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		
		PolicyChain other = (PolicyChain) obj;
		if (_nextHopIP == null) {
			if (other._nextHopIP != null)
				return false;
		} else if (!_nextHopIP.equals(other._nextHopIP))
			return false;
		if (_nextHopPort != other._nextHopPort)
			return false;
		if (_policyChain == null) {
			if (other._policyChain != null)
				return false;
		} else if (!_policyChain.equals(other._policyChain))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return "PolicyChain [_nextHopIP=" + _nextHopIP + ", _nextHopPort="
				+ _nextHopPort + ", _policyChain=" + _policyChain + "]";
	}
	
	
}
