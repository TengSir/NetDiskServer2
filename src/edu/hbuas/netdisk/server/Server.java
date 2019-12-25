package edu.hbuas.netdisk.server;
/**
 * 这是服务器类，用来给网盘客户端提供连接服务，连接之后可以给客户端提供上传和下载服务
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
			System.out.println("服务器已经启动");
			
			while(true) {
				Socket client=server.accept();
				System.out.println(client.getInetAddress().getHostAddress()+"连接进来了！");
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
	 * 在服务器类中封装一个内部类，里面书写网盘客户端连接进来的线程方法
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
						System.out.println("用户当前是要执行上传文件操作");
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
						System.out.println("用户["+message.getFrom().getUsername()+"]的["+message.getFilename()+"]文件上传成功!");
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
