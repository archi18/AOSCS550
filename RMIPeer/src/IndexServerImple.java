import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class IndexServerImple extends UnicastRemoteObject implements IIndexServer {
	public static Map<String, List<String>> peerFileMap;
	private static final long serialVersionUID = 1L;
	
	public IndexServerImple(int portnumber) throws RemoteException {
		super(portnumber);
	}
	
	public IndexServerImple() throws RemoteException {
		super();
	}
	
	static{
		peerFileMap=new HashMap<>();
	}
	
	@Override
	public List<String> lookUpFile(String fileName) {
		return peerFileMap.get(fileName);
	}
	@Override
	public boolean register(String fileName, String address) {
		if(!peerFileMap.containsKey(fileName)){
			List<String> peerList= new ArrayList<>();
			peerList.add(address);
			peerFileMap.put(fileName, peerList);
		}else{
			List<String> peerList= peerFileMap.get(fileName);
			peerList.add(address);
			peerFileMap.put(fileName, peerList);
		}
		return true;
	}
	@Override
	public boolean deRegister(String fileName, String address) {
		List<String> peerList = peerFileMap.get(fileName);
		Iterator<String> it = peerList.iterator();
		while(it.hasNext()){
			String peerAdd = it.next();
			if(peerAdd==address){
				peerList.remove(peerAdd);
				System.out.println("Remove from peer listn");
				break;
			}
		}
		if(peerList.size()==0)
			peerFileMap.remove(fileName);
		return true;
	}
	
	
}
