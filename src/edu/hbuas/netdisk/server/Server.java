package edu.hbuas.netdisk.server;
/**
 * ���Ƿ������࣬���������̿ͻ����ṩ���ӷ�������֮����Ը��ͻ����ṩ�ϴ������ط���
 * @author Lenovo
 *
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import edu.hbuas.netdisk.config.SocketConfig;
import edu.hbuas.netdisk.model.Message;

public class Server {
	private ServerSocket  server;
	
	public Server() {
		try {
			server=new ServerSocket(SocketConfig.netDiskServerPort);
			System.out.println("�������Ѿ�����");
			
			while(true) {
				Socket client=server.accept();
				System.out.println(client.getInetAddress().getHostAddress()+"���ӽ����ˣ�");
				ClientThread  c=new ClientThread(client);
				c.start();
			
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		new Server();
	}

	
	
	/**
	 * �ڷ��������з�װһ���ڲ��࣬������д���̿ͻ������ӽ������̷߳���
	 */
	class  ClientThread extends Thread{
		private Socket  client;
		private ObjectInputStream in;
		private ObjectOutputStream  out;
		public  ClientThread( Socket  client) {
			this.client=client;
			try {
				in=new ObjectInputStream(client.getInputStream());
				out=new ObjectOutputStream(client.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		@Override
		public void run() {
			try {
				Message message=(Message)in.readObject();
				System.out.println(message);
				switch (message.getType()) {
					case UPLOAD:
					{
						System.out.println("�û���ǰ��Ҫִ���ϴ��ļ�����");
						File dir=new File(SocketConfig.serverStoreFileBasePath+message.getFrom().getUsername());
						if(!dir.exists())dir.mkdirs();
						
						FileOutputStream  fileOut=new FileOutputStream(dir+"/"+message.getFilename());
						byte[] bs=new byte[1024];
						int length=-1;
						while((length=in.read(bs))!=-1) {
							fileOut.write(bs,0,length);
							fileOut.flush();
						}
						fileOut.close();
						System.out.println("�û�["+message.getFrom().getUsername()+"]��["+message.getFilename()+"]�ļ��ϴ��ɹ�!");
						break;
					}

				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
}
