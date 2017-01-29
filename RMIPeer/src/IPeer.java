import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IPeer extends Remote {
	public byte[] getFile(String fileName)throws RemoteException;
}
