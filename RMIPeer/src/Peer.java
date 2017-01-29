import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.ls.LSInput;

public class Peer {
	static Map<String, String> listShareFile = new HashMap<>();
	static Set<String> shareFileList = new HashSet<>();

	public static void main(String[] args) throws IOException {
		final String IndexServerName = "IndexServer";
		final int portNumber = Integer.parseInt(args[1]);
		final String peerHost = args[0];
		final int listnPeerPort = Integer.parseInt(args[2]);
		final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Enter the Peer name");
		final String peerName = br.readLine();

		final String FILE_LOC_URL = "" + InetAddress.getLocalHost().getHostAddress() + ":" + listnPeerPort + "/"
				+ peerName + "";

		Thread peerThread = new Thread(new Runnable() {
			@Override
			public void run() {
				// String IserverName = "rmi://localhost:1099/IndexServer";

				try {
					Registry registry = LocateRegistry.getRegistry(peerHost, portNumber);
					IIndexServer serverObject = (IIndexServer) registry.lookup(IndexServerName);

					if (serverObject != null) {
						System.out.println("Lookup success. Can talk with server now");
					}

					this.initPeer(peerName, serverObject);

					int choice = 0;
					boolean flgContinue = true;
					System.out.println("Select from following option");
					do {
						System.out.println("1 : Share file on network ");
						System.out.println("2 : remove file from network ");
						System.out.println("3 : download file from network");
						System.out.println("4 : Refresh share file info on the Index Server");
						System.out.println("5 : Shutdown Peer");
						try {
							choice = Integer.parseInt(br.readLine());
						} catch (Exception e) {
							e.printStackTrace();
							continue;
						}
						switch (choice) {
						case 1:
							this.registerFileOnNetwork(serverObject);
							break;
						case 2:
							this.deregisterFileFromNetwork(serverObject);
							break;
						case 3:
							this.downloadFileFromNetWork(serverObject);
							break;
						case 4:
							this.autoRegIndexServer(serverObject);
							break;
						case 5:
							System.out.println("Shutting down peer........");
							this.autoDeRegIndexServer(serverObject);
							System.exit(0);
							break;
						default:
							break;
						}
						System.out.println("Do you wnat to continue 1: yes 0:no");
						int ans = Integer.parseInt(br.readLine());
						flgContinue = (ans == 0) ? false : true;
					} while (flgContinue);

				} catch (Exception e) {
					e.printStackTrace();
				}

			}

			public void registerFileOnNetwork(IIndexServer serverObject) {
				String filePath = null;
				try {
					System.out.println(
							"Enter the name of file to be shared. Make sure you put file in peername directory");
					String shareFileName = br.readLine();
					serverObject.register(shareFileName, FILE_LOC_URL);
					listShareFile.put(shareFileName, filePath);
					System.out.println("register Success");
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

			public void deregisterFileFromNetwork(IIndexServer serverObject) {
				System.out.println("Select the file you want to remove from share network");
				try {
					String fileName = br.readLine();
					if (!serverObject.deRegister(fileName, FILE_LOC_URL))
						System.out.println("file not present on Index server. check name is correct");
					else
						System.out.println("File Removed from newwork success");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			public List<String> searchFileOnNetWork(IIndexServer serverObject, String fileName) throws IOException {

				return serverObject.lookUpFile(fileName);
			}

			public void downloadFileFromNetWork(IIndexServer serverObject) throws IOException {
				int failCount = 0;
				System.out.println("Enter the file to download");
				String fileName = br.readLine();
				List<String> peerList = this.searchFileOnNetWork(serverObject, fileName);
				if (peerList == null) {
					System.out.println("File not found on network");
					return;
				}
				System.out.println("Peer list " + peerList);
				Iterator<String> peerIt = peerList.iterator();

				while (peerIt.hasNext()) {
					String peerLocStr = peerIt.next();
					// 104.194.114.136:50002{peer1}
					System.out.println("Connecting Peer Location " + peerLocStr);
					String peerLookupName = peerLocStr.substring(peerLocStr.indexOf("/") + 1);
					String peerLooupPort = peerLocStr.substring(peerLocStr.indexOf(":") + 1, peerLocStr.indexOf("/"));
					String peerLookupHost = peerLocStr.substring(0, peerLocStr.indexOf(":"));
					try {
						Registry registry = LocateRegistry.getRegistry(peerLookupHost, Integer.parseInt(peerLooupPort));
						IPeer peer;
						peer = (IPeer) registry.lookup(peerLookupName);
						byte[] bufferData = peer.getFile(peerLookupName + "/" + fileName);
						if (!this.copyDataToLocalPeerFile(fileName, bufferData)) {
							System.out.println("Error While copying file in local peer. Check file permission");
						}
						System.out.println("File Copied successful: ");
						break;
					} catch (NotBoundException e) {

						System.out.println("Error While downloading file from peer " + peerLocStr);
						e.printStackTrace();
						if (failCount == peerList.size()) {
							System.out.println("No more peer available. File can not be downloaded from network");
							break;
						} else {
							failCount++;
							System.out.println("Trying to download file from available peer ");
						}

					}
				}
			}

			public boolean copyDataToLocalPeerFile(String fileName, byte[] bufferData) {
				File file = new File(System.getProperty("user.dir") + "/" + peerName + "/" + fileName);
				System.out.println("Writing data to file" + file.getAbsolutePath());
				try {
					BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
					bufferedOutputStream.write(bufferData, 0, bufferData.length);
					bufferedOutputStream.flush();
					bufferedOutputStream.close();
					return true;
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
			}

			public void initPeer(String peerName, IIndexServer serverObject) {
				File file = new File(peerName);
				if (!file.exists()) {
					if (file.mkdir()) {
						System.out.println("Directory is created!");
					} else {
						System.out.println("Failed to create directory!");
					}
				}
				this.autoRegIndexServer(serverObject);
				System.out.println();
			}

			public boolean autoRegIndexServer(IIndexServer serverObject) {
				File[] listFiles = null;
				try {
					listFiles = new File(peerName).listFiles();
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Error while reading file....");
				}
				for (int i = 0; i < listFiles.length; i++) {
					if (listFiles[i].isFile()) {
						try {
							if (!shareFileList.contains(listFiles[i].getName())) {
								serverObject.register(listFiles[i].getName(), FILE_LOC_URL);
								shareFileList.add(listFiles[i].getName());
							}
						} catch (RemoteException e) {
							e.printStackTrace();
							return false;
						}
					}
				}
				this.autoRmvFlFrmIndexServer(serverObject);
				System.out.println("Auto update complete ");
				return true;
			}

			public boolean autoRmvFlFrmIndexServer(IIndexServer serverObject) {
				Set<String> curFileSet = new HashSet<>();
				File[] listFiles = null;
				try {
					listFiles = new File(peerName).listFiles();
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Error while reading file....");
				}

				if (!shareFileList.isEmpty()) {
					for (int i = 0; i < listFiles.length; i++)
						curFileSet.add(listFiles[i].getName());

					Iterator<String> itList = shareFileList.iterator();
					while (itList.hasNext()) {
						String oldFileName = itList.next();
						if (!curFileSet.contains(oldFileName)) {
							itList.remove();
							try {
								serverObject.deRegister(oldFileName, FILE_LOC_URL);
							} catch (Exception e) {
								System.out.println("Error while deregister");
							}
						}
					}

				}
				return true;
			}
			
			public boolean autoDeRegIndexServer(IIndexServer serverObject) {
				File[] listFiles = new File(peerName).listFiles();
				for (int i = 0; i < listFiles.length; i++) {
					if (listFiles[i].isFile()) {
						try {
							serverObject.deRegister(listFiles[i].getName(), FILE_LOC_URL);
						} catch (RemoteException e) {
							e.printStackTrace();
							return false;
						}
					}
				}

				return true;
			}

		});

		Thread listingPeerThread = new Thread(new Runnable() {
			@Override
			public void run() {
				if (System.getSecurityManager() == null) {
					System.setSecurityManager(new SecurityManager());
				}
				IPeer listnPeer;
				try {
					listnPeer = new PeerImpl();
					Naming.rebind("rmi://localhost:" + listnPeerPort + "/" + peerName, listnPeer);
					System.out.println("Peer Bound..Now Listining");
				} catch (RemoteException | MalformedURLException e) {
					e.printStackTrace();
				}
			}
		});
		
		Thread updateDirThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Registry registry = LocateRegistry.getRegistry(peerHost, portNumber);
					IIndexServer serverObject = (IIndexServer) registry.lookup(IndexServerName);

					if (serverObject != null) {
						System.out.println("Lookup success. Can talk with server now");
					}

					while (true) {
						File[] listFiles = new File(peerName).listFiles();
						for (int i = 0; i < listFiles.length; i++) {
							if (listFiles[i].isFile()) {
								try {
									serverObject.register(listFiles[i].getName(), FILE_LOC_URL);
								} catch (RemoteException e) {
									e.printStackTrace();

								}
							}
						}
						Thread.sleep(10000);
					}

				} catch (Exception e) {
					System.out.println("Exception in autoupdate directory thread ");
					e.printStackTrace();
				}
			}
		});

		listingPeerThread.start();
		peerThread.start();
		updateDirThread.start();

	}
}
