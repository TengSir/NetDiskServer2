package edu.hbuas.netdisk.server;
/**
 * 这是服务器类，用来给网盘客户端提供连接服务，连接之后可以给客户端提供上传和下载服务
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
					case UPLOAD://这个分支是判断当用户提交过来的消息是上传消息时处理上传业务
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
						out.close();
						in.close();
						break;
					}
					case  LISTALLFILES:{//这个消息类型意思是读取当前用户的文件列表
						System.out.println("用户当前是要执行读取文件列表的操作");
						
						File  userDir=new File(SocketConfig.serverStoreFileBasePath+message.getFrom().getUsername());
						if(!userDir.exists())userDir.mkdirs();//文件夹不存在说明是新用户，给用户创建一个用户目录
						File[]  files=userDir.listFiles();//通过文件夹对象的listFiles方法，列出当前用户目录下的所有文件
						
						Set<File> fs=new HashSet<File>();//创建一个集合用来存储所有的文件
						for(File  f:files) {
							System.out.println(f.getAbsolutePath());
							fs.add(f);
						}
						
						//文件列表读取完毕之后，服务器端需要封装一个消息对象回复给客户，通知客户你的网盘列表有哪些文件
						Message  allFilesMessage=new Message();
						allFilesMessage.setFiles(fs);
						
						out.writeObject(allFilesMessage);
						out.flush();
						System.out.println("服务器返回客户端消息完毕（消息中包含了用户的所有文件）");
						break;
					}
					case DOWNLOAD:{//用户发过来一个下载文件的消息
						System.out.println("用户当前是要执行下载文件的操作");
						String username=message.getFrom().getUsername();//获取用户名
						String fileName=message.getFilename();//获取用户要下载的文件名
						File  file=new File(SocketConfig.serverStoreFileBasePath+username+"/"+fileName);//创建一个文件对象指向要下载的文件
						FileInputStream  fileIn=new FileInputStream(file);//准备一个文件输入流指向这个要下载的文件对象
						//如下代码就是真正的下载业务传输数据的代码
						//使用文件输入流从文件里读取数据，然后使用socket的输出流将文件的数据写向网络通道的客户端那一方
						byte[] bs=new byte[1024];
						int length=-1;
						while((length=fileIn.read(bs))!=-1) {
							out.write(bs,0,length);
							out.flush();
						}
						fileIn.close();
						out.close();
						in.close();
						System.out.println("下载文件操作完毕，服务器已将所有文件数据写入网络通道的用户那端的socket");
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
