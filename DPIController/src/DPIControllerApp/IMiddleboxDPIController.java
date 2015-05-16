package DPIControllerApp;



import java.rmi.Remote;

public interface IMiddleboxDPIController extends Remote {

	public RETURN_VALUE register(Middlebox mb);

	public RETURN_VALUE updatePatternSet(String patternSet);
}
