package singleServer;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import java.net.*;
import java.io.*;
import org.eclipse.swt.widgets.Label;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;


import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import java.util.Vector;

public class ServerApp {

	protected Shell shell;
	private Text textPort;
	List list;
	ServerSocket server = null;

	private char connFlag='F';
	private Text textManager;
	private Text textArea;
	static Vector userList = new Vector();
	private char flag = 'T';
	private String[] fFile;
	/**
	 * Launch the application.
	 * @param args
	 */
	// 内部类 client
	class Client extends Thread{
		BufferedReader cin = null;
		DataInputStream fin = null;
		PrintStream cout = null;
		PrintStream fout = null;
		Socket s;
		Socket f;
		String username;
		public void appendTA(String str)
		{
			Display.getDefault().syncExec(new Runnable(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					textArea.append(str);
				}
				
			});			
		}
	
		public Client(Socket s,Socket f)
		{
			this.s = s;
			this.f = f;
			try{
				cin = new BufferedReader(new InputStreamReader(this.s.getInputStream()));
				fin = new DataInputStream(new BufferedInputStream(this.f.getInputStream()));
				cout = new PrintStream(this.s.getOutputStream());
				fout = new PrintStream(this.f.getOutputStream());
				String str =cin.readLine();
				username = str;
				textArea.append(username+"已连接\n");
				
			}catch(IOException e)
			{
				textArea.append("用户连接出错");
			}
			
		}

		//method
		public void send(String msg){
			cout.println(msg);
			cout.flush(); // 清空缓冲区数据
			
		}
		
		public void run() {
			String fd = null;
			while(true)
			{
				if(flag!='F')
				{
					String str =null;
					try{
						str = cin.readLine();
					}catch(IOException e)
					{
//						appendTA("读取客户信息错误\n");
//						disconnect(this);
						return;
					}
					if(str.equalsIgnoreCase("EXIT"))
					{
						disconnect(this);
						this.stop();
					}
					else if(str.contains("SendFileByte"))
					{
						fFile = str.substring(12).split("#");
						fd = str;
						flag ='F';
					}
					else{
						if(flag!='G')
						{
							this.appendTA(str+"\n");
							Client conns;
							for(int i=0;i<userList.size();i++)
							{
								conns = (Client) userList.elementAt(i);
								conns.send(str);
							}
						}
						else
							flag='T';
					}
				}else{
					String savePath = SaveFile(fin,fFile[1],Integer.parseInt(fFile[2]));
					SendFile(savePath,fFile[1],Integer.parseInt(fFile[2]),fd);
					Client conn = GetClient(fFile[3]);
					
					conn.send(fFile[1]+"已发送至用用户"+fFile[0]);
					flag='G';
				}
				
			}
		}




		
	}
	// 内部类 connect
	class ConnectSocket extends Thread{
		public void appendformation()
		{
			Display.getDefault().syncExec(new Runnable(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					Client conn = new Client(socket,filesocket);
					userList.addElement(conn);
					String username = conn.username.substring(conn.username.indexOf(":")+1);
					if(list.indexOf(username)<0)
					{
						conn.start();
						list.add(username);
						Client conns;
						for(int i=0;i<userList.size();i++)
						{
							conns = (Client) userList.elementAt(i);
							conns.send("listusernames"+getlistname());
						}
					}
				}
				
			});
		}

		Socket socket;
		Socket filesocket;
		public void run(){
			while(true){
				try{
					socket = server.accept();
					filesocket = server.accept();
				}
				catch(IOException e2)
				{
					textArea.append("客户连接失败\n");	
				}this.appendformation();
			}
		}
	}
	public static void main(String[] args) {
		try {
			ServerApp window = new ServerApp();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
	/**
	 * Create method
	 */
	
	// 断开连接
	public void disconnect(Client conn){
		String username =  conn.username.substring(conn.username.indexOf(":")+1);
		Display.getDefault().syncExec(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				textArea.append(username+"断开连接\n");
				if(list.indexOf(username)>=0)
				{
					list.remove(list.indexOf(username));
				}
			}
		});
		conn.send("已断开");
		userList.removeElement(conn);
		try{
			conn.s.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		Client conns;
		for(int i=0;i<userList.size();i++)
		{
			conns = (Client) userList.elementAt(i);
			conns.send("listusernames"+getlistname());
		}
	}
	// GET 列表用户名单
	public String getlistname()
	{
		Client conns;
		String str="";
		String[] strs;
		for(int i=0;i<userList.size();i++)
		{
			conns = (Client) userList.elementAt(i);
			strs = conns.username.split(":");
			str=str+strs[1]+":";
		}
		return str;
	}
	public void SendFile(String savePath,String filename,int filelen,String line)
	{ 
		int len=1; 
		byte[] buf = new byte[filelen]; 
		try{ 
			DataInputStream fis = new DataInputStream(new BufferedInputStream(new FileInputStream(savePath))); 
			Client c=GetClient(fFile[0]);
			System.out.println(line);
			c.send(line);
			while ((len = fis.read(buf,0,buf.length)) > 0) 
			{ 
				c.fout.write(buf,0,len);
				c.fout.flush(); 
			}
			fis.close();
			deleteFile(savePath); 
		}
		catch(Exception e){
				e.printStackTrace();
			} 
	}
	// 保存文件
	public String SaveFile(DataInputStream gsm,String filename,int filelen)
	{
		int len = 1;
//		System.out.println(System.getProperty("user.dir"));
		
		String savePath = ".\\server\\"+filename;
		File testFile = new File(".\\server");
		if(!testFile.exists()) // 判断文件夹如果不存在 则重新创建
			testFile.mkdirs();
		File f = new File(savePath);
		byte[] buf = new byte[filelen];
		try{
			FileOutputStream in = new FileOutputStream(f);
			while((len=gsm.read(buf,0,buf.length))>0)
			{
				in.write(buf,0,len);
				in.flush();
				if(f.length()>=filelen)
				{
					break;
				}
			}
			in.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return savePath;
		
	}
	
	// 删除文件
	public boolean deleteFile(String path)
	{
		File f = new File(path);
		if(f.isFile() && f.exists()){
			f.delete();
			return true;
		}
		return false;
	}
	// getClient
	
	private Client GetClient(String username) {
		// TODO Auto-generated method stub

		for(int i=0;i<userList.size();i++)
		{
			Client conn = (Client) userList.elementAt(i);
			// 判断接收的username是否存在userList列表中
			if(username.equals(conn.username.substring(conn.username.indexOf(":")+1)))
				return conn;
		}
		
		return null;
	}
	
	/**
	 * Create contents of the window.
	 */
	
	
	protected void createContents() {
		shell = new Shell();
		shell.setModified(true);
		shell.setSize(450, 300);
		shell.setText("Server");
		shell.setLayout(null);
		
		Label label = new Label(shell, SWT.NONE);
		label.setFont(SWTResourceManager.getFont("微软雅黑", 12, SWT.NORMAL));
		label.setBounds(10, 10, 64, 23);
		label.setText("监听端口");
		
		textPort = new Text(shell, SWT.BORDER);
		textPort.setFont(SWTResourceManager.getFont("微软雅黑", 12, SWT.NORMAL));
		textPort.setBounds(80, 11, 258, 23);
		
		Button btnStart = new Button(shell, SWT.NONE);
		btnStart.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(textPort.getText()=="")
				{
					textArea.append("端口参数不能为空\n");
					return;
				}
				if(connFlag=='F')
				{					
					try{
						server = new ServerSocket(Integer.parseInt(textPort.getText()));
						textArea.append("服务器端口打开成功\n");
						btnStart.setText("关闭端口");
						textPort.setEnabled(false);
						connFlag='T';
					}catch(IOException e1)
					{
						textArea.append("服务器端口打开错误\n");
					}
					if(server == null)
					{
						textArea.append("端口为空\n");
						return;
					}
					ConnectSocket conn = new ConnectSocket();
					conn.start();
				}else{
					try {
						server.close();
						textArea.append("服务器端口关闭成功\n");
						btnStart.setText("开始监听");
						textPort.setEnabled(true);
						connFlag='F';
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						textArea.append("服务器端口关闭错误\n");
					}
					
				}
			}
		});

		btnStart.setBounds(344, 9, 80, 27);
		btnStart.setText("开始监听");
		
		textManager = new Text(shell, SWT.BORDER);
		textManager.setFont(SWTResourceManager.getFont("微软雅黑", 12, SWT.NORMAL));
		textManager.setBounds(10, 45, 328, 23);
		
		Button btnSend = new Button(shell, SWT.NONE);
		btnSend.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(connFlag=='F')
				{
					textArea.append("服务器未开启，无法发送信息\n");
					return;
				}
				if(list.getSelectionIndex()!=0){
					Client c = GetClient(list.getSelection()[0]);
//					System.out.println(list.getSelection()[0]);
					if(c!=null)
						c.send("服务端->"+list.getSelection()[0]+"："+textManager.getText());
						textArea.append("服务端->"+list.getSelection()[0]+"："+textManager.getText()+"\n");
				}else{
					for(int i=1;i<list.getItemCount();i++)
					{
						Client c = GetClient(list.getItem(i));
						if(c!=null)
							c.send("服务端->All："+textManager.getText());
					}
					textArea.append("服务端->All："+textManager.getText()+"\n");
				}
				textManager.setText("");
				
			}
		});
		btnSend.setText("发送信息");
		btnSend.setBounds(344, 41, 80, 27);
		
		list = new List(shell, SWT.BORDER);
		list.setItems(new String[] {"All"});
		list.setSelection(0);
		list.setBounds(10, 97, 80, 122);
		Button btnKick = new Button(shell, SWT.NONE);
		btnKick.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(list.getSelectionCount()>=0)
				{
					Client c;
					if(list.getSelectionIndex()==0)
					{
						for(int i=1;i<list.getItemCount();i++)
						{
							System.out.print(i);
							c = GetClient(list.getItem(i));
							if(c!=null)
								disconnect(c);	
						}
						// 不知为何删不了第一个用户 毫无办法再运行一次删除第一个
						c = GetClient(list.getItem(1));
						if(c!=null)
							disconnect(c);	
					}else{
						c = GetClient(list.getSelection()[0]);
						if(c!=null)
							disconnect(c);	
					}
					
				}
			}
		});
		btnKick.setText("断开用户");
		btnKick.setBounds(10, 225, 80, 27);
		
		Label label_1 = new Label(shell, SWT.NONE);
		label_1.setBounds(20, 74, 61, 17);
		label_1.setText("用户列表");
		
		textArea = new Text(shell, SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL);
		textArea.setBounds(96, 97, 328, 155);

	}
}
