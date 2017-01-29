import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class PeerImpl extends UnicastRemoteObject implements IPeer {
	final static String FILE_NOT_FOUND="File Not found";
	protected PeerImpl() throws RemoteException {
		super();
	}

	protected PeerImpl(int portNumber) throws RemoteException {
		super(portNumber);
	}

	private static final long serialVersionUID = 1L;

	@Override
	public byte[] getFile(String fileName) throws RemoteException {
		
		try {
			File file = new File(fileName);
			byte[] buffer = new byte[(int) file.length()];
			BufferedInputStream input;
			input = new BufferedInputStream(new FileInputStream(file));
			input.read(buffer, 0, buffer.length);
			input.close();
			return buffer;
		} catch (IOException e) {
			e.printStackTrace();
			return FILE_NOT_FOUND.getBytes();
		}
	}

}
