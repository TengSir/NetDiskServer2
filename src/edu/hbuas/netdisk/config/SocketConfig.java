package edu.hbuas.netdisk.config;
/**
 * ��������һ�����ýӿڣ���������socket���ӵ�һЩ����
 * @author Lenovo
 *
 */
public interface SocketConfig {
	public String netDiskServerIP="localhost";
	public String databaseServerIP=netDiskServerIP;
	public String jdbcDriverClass="com.mysql.jdbc.Driver";
	public String jdbcURL="jdbc:mysql://"+databaseServerIP+":3306/netdisk2?useSSL=false";
	public String jdbcUsername="root";
	public String jdbcPassword="root";
	public int netDiskServerPort=9999;
	public String serverStoreFileBasePath="D:/newdiskserver2/";

}
