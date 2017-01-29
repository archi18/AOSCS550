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

	static {
		peerFileMap = new HashMap<>();
	}

	@Override
	public List<String> lookUpFile(String fileName) {
		return peerFileMap.containsKey(fileName) ? peerFileMap.get(fileName) : null;
	}

	@Override
	public boolean register(String fileName, String address) {
		synchronized (peerFileMap) {
			if (!peerFileMap.containsKey(fileName)) {
				List<String> peerList = new ArrayList<>();
				peerList.add(address);
				peerFileMap.put(fileName, peerList);
			} else {
				boolean flgInsert = true;
				List<String> peerList = peerFileMap.get(fileName);
				Iterator<String> it = peerList.iterator();
				while (it.hasNext()) {
					String peerAdd = it.next();
					if (peerAdd.equals(address)) {
						flgInsert = false;
						continue;
					}
				}
				if (flgInsert) {
					peerList.add(address);
					peerFileMap.put(fileName, peerList);
				}
			}
		}
		return true;
	}

	@Override
	public boolean deRegister(String fileName, String address) {
		synchronized (peerFileMap) {
			System.out.println("Remove file " + fileName + " address :: " + address);
			List<String> peerList = peerFileMap.get(fileName);
			Iterator<String> it = peerList.iterator();
			while (it.hasNext()) {
				String peerAdd = it.next();
				System.out.println(peerAdd + " peer entry remove " + address + " Size" + peerList.size());
				if (peerAdd.equals(address)) {
					// peerList.remove(peerAdd);
					it.remove();
					System.out.println(" inside if size " + peerList.size());
				}
			}
		}
		return true;
	}

}
