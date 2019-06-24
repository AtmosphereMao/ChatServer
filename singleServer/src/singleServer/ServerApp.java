package singleServer;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import java.net.*;
import java.sql.ClientInfoStatus;
import java.io.*;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.TouchListener;
import org.eclipse.swt.events.TouchEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import java.util.Vector;

import javax.swing.text.html.CSS;

public class ServerApp {

	protected Shell shell;
	private Text textPort;
	List list;
	ServerSocket server = null;
	Socket socket = null;
	Socket filesocket = null;
	BufferedReader cin = null;
	DataInputStream fin = null;
	PrintStream cout = null;
	PrintStream fout = null;
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
				cin = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				fin = new DataInputStream(new BufferedInputStream(filesocket.getInputStream()));
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
						textArea.append("读取客户信息错误");
						disconnect(this);
						return;
					}
					if(str.equalsIgnoreCase("exit"))
					{
						disconnect(this);
						return;
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
					String username = conn.username.substring(conn.username.indexOf(":")+1)
					if(list.indexOf(username)<0)
					{
						conn.start();
						list.add(username);
						Client conns;
						for(int i=0;i<userList.size();i++)
						{
							conns = (Client) userList.elementAt(i);
						}
					}
				}
				
			});
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
		conn.send("退出");
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
			conns.send("当前用户:"+getlistname());
		}
	}
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
	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setModified(true);
		shell.setSize(450, 300);
		shell.setText("SWT Application");
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
				try{
					server = new ServerSocket(Integer.parseInt(textPort.getText()));
					textArea.append("服务器端口打开成功\n");
				}catch(IOException e1)
				{
					textArea.append("服务器端口打开错误\n");
				}
				try{
					socket = server.accept();
				}catch(IOException e2)
				{
					textArea.append("用户连接服务器出错\n");
				}
				if(server == null)
				{
					textArea.append("端口为空\n");
					return;
				}
				
					
//				try{
//					cin = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//					cout = new PrintStream(socket.getOutputStream());
//					String str = "来自"+InetAddress.getLocalHost().toString()+"的连接信息";
//					cout.println(str);
//					textArea.append("向服务器发送如下的信息:"+str+"\n");
//				}catch(IOException e3)
//				{
//					textArea.append("输入输出异常\n");
//				}
				
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
				String str = textManager.getText();
				textArea.append("向服务器发送信息:"+str+"\n");
				cout.println(str);
			}
		});
		btnSend.setText("发送信息");
		btnSend.setBounds(344, 41, 80, 27);
		
		list = new List(shell, SWT.BORDER);
		list.setBounds(10, 97, 80, 122);
		Button btnKick = new Button(shell, SWT.NONE);
		btnKick.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

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
