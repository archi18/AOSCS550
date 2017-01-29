import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class IndexServer{
	public static void main(String[] args) {
		final int portNumber = Integer.parseInt(args[0]);
		Thread serverThead=new Thread(new Runnable() {
			@Override
			public void run() {
				String bindName="IndexServer";
				IIndexServer indexServer;
				if(System.getSecurityManager()==null){
					System.setSecurityManager(new SecurityManager());
				}
				try{
					 indexServer=new IndexServerImple();
					// LocateRegistry.getRegistry(portNumber).rebind(bindName, indexServer);
					// Naming.bind(bindName, indexServer);
					 Naming.rebind("rmi://localhost:"+portNumber+"/"+bindName,indexServer);  
					 System.out.println("Server Bound");
				} catch (Exception  e){
					e.printStackTrace();
				}
			}
		});
		serverThead.start();
		
		
	}
}
