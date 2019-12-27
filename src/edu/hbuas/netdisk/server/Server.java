package edu.hbuas.netdisk.server;
/**
 * ���Ƿ������࣬���������̿ͻ����ṩ���ӷ�������֮����Ը��ͻ����ṩ�ϴ������ط���
 * @author Lenovo
 *
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

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
					case UPLOAD://�����֧���жϵ��û��ύ��������Ϣ���ϴ���Ϣʱ�����ϴ�ҵ��
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
						out.close();
						in.close();
						break;
					}
					case  LISTALLFILES:{//�����Ϣ������˼�Ƕ�ȡ��ǰ�û����ļ��б�
						System.out.println("�û���ǰ��Ҫִ�ж�ȡ�ļ��б�Ĳ���");
						
						File  userDir=new File(SocketConfig.serverStoreFileBasePath+message.getFrom().getUsername());
						if(!userDir.exists())userDir.mkdirs();//�ļ��в�����˵�������û������û�����һ���û�Ŀ¼
						File[]  files=userDir.listFiles();//ͨ���ļ��ж����listFiles�������г���ǰ�û�Ŀ¼�µ������ļ�
						
						Set<File> fs=new HashSet<File>();//����һ�����������洢���е��ļ�
						for(File  f:files) {
							System.out.println(f.getAbsolutePath());
							fs.add(f);
						}
						
						//�ļ��б��ȡ���֮�󣬷���������Ҫ��װһ����Ϣ����ظ����ͻ���֪ͨ�ͻ���������б�����Щ�ļ�
						Message  allFilesMessage=new Message();
						allFilesMessage.setFiles(fs);
						
						out.writeObject(allFilesMessage);
						out.flush();
						System.out.println("���������ؿͻ�����Ϣ��ϣ���Ϣ�а������û��������ļ���");
						break;
					}
					case DOWNLOAD:{//�û�������һ�������ļ�����Ϣ
						System.out.println("�û���ǰ��Ҫִ�������ļ��Ĳ���");
						String username=message.getFrom().getUsername();//��ȡ�û���
						String fileName=message.getFilename();//��ȡ�û�Ҫ���ص��ļ���
						File  file=new File(SocketConfig.serverStoreFileBasePath+username+"/"+fileName);//����һ���ļ�����ָ��Ҫ���ص��ļ�
						FileInputStream  fileIn=new FileInputStream(file);//׼��һ���ļ�������ָ�����Ҫ���ص��ļ�����
						//���´����������������ҵ�������ݵĴ���
						//ʹ���ļ����������ļ����ȡ���ݣ�Ȼ��ʹ��socket����������ļ�������д������ͨ���Ŀͻ�����һ��
						byte[] bs=new byte[1024];
						int length=-1;
						while((length=fileIn.read(bs))!=-1) {
							out.write(bs,0,length);
							out.flush();
						}
						fileIn.close();
						out.close();
						in.close();
						System.out.println("�����ļ�������ϣ��������ѽ������ļ�����д������ͨ�����û��Ƕ˵�socket");
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
