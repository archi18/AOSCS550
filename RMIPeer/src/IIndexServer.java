import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
public interface IIndexServer extends Remote{
	public List<String> lookUpFile(String fileName)throws RemoteException;
	public boolean register(String fileName, String address)throws RemoteException;
	public boolean deRegister(String fileName, String address)throws RemoteException;
}
