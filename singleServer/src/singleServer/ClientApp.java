package singleServer;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import java.net.*;
import java.io.*;
import org.eclipse.swt.widgets.Label;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.custom.CCombo;

public class ClientApp {

	protected Shell shell;
	private Text textIP;
	private Text textPort;
	private Button btnQuit;
	private Button btnConnect;
	private Button btnDeconnect;
	private Text textManager;
	private Button btnSendM;
	private Label lblNewLabel_1;
	private Text textArea;
	
	private Socket socket=null;
	private String username=null;
	private char flag='T';
	public String[] userList=null;
	public List list;
	private Socket filesocket = null;
	private BufferedReader cin = null;
	private PrintStream cout = null;
	public String[] fFile;
	private Label labelUsername;
	private Button button;
	Combo combo;
	/**
	 * Launch the application.
	 * @param args
	 */
	public ClientApp(){
		
	}
	public ClientApp(Shell father,String Loginname){
		username = Loginname;
		father.close();
		open();
		
	}
	
	public static void main(String[] args) {
		try {
			ClientApp window = new ClientApp();
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
	class ReadMessageThread extends Thread{
		public void appendTextArea(final String str)
		{
			Display.getDefault().syncExec(new Runnable(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					textArea.append(str);
				}
				
			});
		}
		public void run(){
			String line="";
			while(true)
			{
				if(flag!='F')
				{
					try{
						line=cin.readLine();
					}catch(IOException e)
					{
						this.appendTextArea("输入输出异常\n");
					}
					if(line.equalsIgnoreCase("exit")){
						try{
							socket.close();
							this.appendTextArea("接收到服务器断开连接信息\n");
						}catch(IOException e)
						{
							this.appendTextArea("套接字关闭异常\n");
						}
						this.stop();
					}else if(line.contains("listusernames")){
						if(line.length()>13){
//							MessageDialog.openInformation(new Shell(), "1", line);
							userList = line.substring(13).split(":");
							Display.getDefault().syncExec(new Runnable(){

								@Override
								public void run() {
									// TODO Auto-generated method stub
									list.setItems(userList);
								}
								
							});
						}
					}else if(line.contains("SendFileByte")){
						fFile = line.substring(12).split("#");
						flag='F';
					}
					else{
						if(flag!='G')this.appendTextArea(line+"\n");
						else flag='T';
					}
				}else{
					String savePath = SaveFile(fFile[1],Integer.parseInt(fFile[2]));
					this.appendTextArea(fFile[3]+"发送文件"+fFile[1]+"\n保存路径"+savePath+"\n"+"文件大小:"+fFile[2]+"B,请注意查收\n");
					flag='G';
				}
			}
		}
		
	}
	
	class SendFileThread extends Thread{
		private String filePath;
		private String fileClient;
		private String fileManager;
		private File f;
		private int count=0;
		public SendFileThread(String filePath,String fileClient) {
			this.filePath = filePath;
			this.fileClient = fileClient;
		}
		   
	   public void run() {
		   fileManager = "SendFileByte"+fileClient;
			f = new File(filePath);
			try{
				DataInputStream fis = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath)));
				DataOutputStream ps = new DataOutputStream(filesocket.getOutputStream());
				byte[] buf = new byte[fis.available()];
				fileManager=fileManager+"#"+f.getName()+"#"+f.length()+"#"+username;
				cout.println(fileManager); cout.flush(); 
				while ((count = fis.read(buf,0,buf.length)) > 0) {
					ps.write(buf,0,count); ps.flush(); 
				}
				fis.close();
			}catch(Exception e){
					e.printStackTrace();
	
			}
	   }

	}
	/**
	 * create method
	 */
	private void SendFile(String fileClient) {
		int count=0;
		// TODO Auto-generated method stub
		FileDialog dlg = new FileDialog(new Shell(),SWT.OPEN);
		dlg.setText("选择需要发送的文件");
		String filePath = dlg.open();
		if(filePath == null)
			return;
		SendFileThread sendF = new SendFileThread(filePath, fileClient);
		sendF.start();
	}
	private void SendFileALL() {
		// TODO Auto-generated method stub
		int count = 0;
		FileDialog dlg = new FileDialog(new Shell(),SWT.OPEN);
		dlg.setText("选择需要发送的文件");
		String filePath = dlg.open();
		if(filePath == null)
			return;
		
		String fileClient = null;
		String fileManager = null;
		for(int i=0;i<list.getItemCount();i++)
		{	
			count = 0;
			fileClient = list.getItem(i);
			if(fileClient.equals(username))
				continue;
//			SendFileThread sendF = new SendFileThread(filePath, fileClient);
//			sendF.start();
			fileManager = "SendFileByte"+fileClient;
			File f = new File(filePath);
			try{
				DataInputStream fis = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath)));
				DataOutputStream ps = new DataOutputStream(filesocket.getOutputStream());
				byte[] buf = new byte[fis.available()];
				fileManager=fileManager+"#"+f.getName()+"#"+f.length()+"#"+username;
				cout.println(fileManager); cout.flush(); 
				while ((count = fis.read(buf,0,buf.length)) > 0) {
					ps.write(buf,0,count); ps.flush(); 
				}
				fis.close();
			}catch(Exception e){
					e.printStackTrace();

			}
		}
	}

	public String SaveFile(String filename,int filelen)
	{
		int len =0;
		String savePath = ".\\client\\"+filename;
		File testFile = new File(".\\client");
		if(!testFile.exists())
			testFile.mkdirs();
		File f= new File(savePath);
		byte[] buf = new byte[filelen];
		try{
			FileOutputStream in = new FileOutputStream(f);
			DataInputStream getMessageStream = new DataInputStream(new BufferedInputStream(filesocket.getInputStream()));
			while((len = getMessageStream.read(buf,0,buf.length))>0){
				in.write(buf, 0, len);
				in.flush();
				if(f.length()>=filelen)
				{
					break;
				}
			}
			in.close();
		}catch(IOException e)
		{
			textArea.append("文件传输失败");
		}
		return savePath;
	}
	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(450, 338);
		shell.setText("ChatSystem - 用户："+username);
		
		Label label = new Label(shell, SWT.NONE);
		label.setBounds(10, 24, 61, 17);
		label.setText("用户名：");
		
		Label lblNewLabel = new Label(shell, SWT.NONE);
		lblNewLabel.setBounds(10, 68, 61, 17);
		lblNewLabel.setText("服务器IP:");
		
		textIP = new Text(shell, SWT.BORDER);
		textIP.setBounds(77, 65, 73, 23);
		
		Label label_1 = new Label(shell, SWT.NONE);
		label_1.setBounds(171, 68, 36, 17);
		label_1.setText("端口：");
		
		textPort = new Text(shell, SWT.BORDER);
		textPort.setBounds(213, 65, 73, 23);
		
		btnQuit = new Button(shell, SWT.NONE);
		btnQuit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String str ="EXIT";
				cout.println(str); 
				try {
					socket.close();
					filesocket.close();
				} catch (Exception e1) {
					// TODO Auto-generated catch block

				}
				shell.close();
			}
		});
		btnQuit.setBounds(321, 19, 80, 27);
		btnQuit.setText("退出登录");
		
		btnConnect = new Button(shell, SWT.NONE);
		btnConnect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(textPort.getText()=="")
				{
					MessageDialog.openInformation(new Shell(), "Warning", "端口号不能为空");
					return;
				}
				if(textIP.getText()=="")
				{
					textIP.setText("127.0.0.1");
				}
				try {
					InetAddress ip = InetAddress.getByName(textIP.getText());
					int port = Integer.parseInt(textPort.getText());
					socket= new Socket(ip,port);
//					MessageDialog.openInformation(new Shell(), "1", socket.toString());
					filesocket = new Socket(ip,port);
					textArea.append("正在连接服务器\n");
					textPort.setEnabled(false);
					textIP.setEnabled(false);
					btnDeconnect.setEnabled(true);
					btnConnect.setEnabled(false);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					textArea.append("服务器连接错误\n");
				}
//				if(socket==null)
//					return;
				try{
					cin = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					cout = new PrintStream(socket.getOutputStream());
					String str = "PEOPLE:"+username;
					cout.println(str);
					ReadMessageThread readThread = new ReadMessageThread();
					readThread.start();
					
				}catch(IOException e2){
					textArea.append("输入输出异常\n");
				}catch(Exception e3){
					textArea.append("此端口不存在\n");
				}
			}
		});
		btnConnect.setText("连接服务器");
		btnConnect.setBounds(294, 62, 63, 27);
		
		btnDeconnect = new Button(shell, SWT.NONE);
		btnDeconnect.setEnabled(false);
		btnDeconnect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String str ="EXIT";
//				MessageDialog.openInformation(new Shell(), "1", socket.toString());
				
				if(socket!=null){
//					MessageDialog.openInformation(new Shell(), "1", socket.toString());
					
					cout.println(str); 
					list.removeAll();
					textPort.setEnabled(true);
					textIP.setEnabled(true);
					btnDeconnect.setEnabled(false);
					btnConnect.setEnabled(true);
					textArea.append("客户请求断开连接\n");

				} else
					textArea.append("您不处于连接状态\n");
			}
		});
		btnDeconnect.setText("断开连接");
		btnDeconnect.setBounds(363, 62, 61, 27);
		
		textManager = new Text(shell, SWT.BORDER);
		textManager.setBounds(10, 104, 261, 23);
		
		btnSendM = new Button(shell, SWT.NONE);
		btnSendM.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(combo.getSelectionIndex()==0){
					String str = textManager.getText();
					str=username+":"+str;
					cout.println(str);
				}else if(combo.getSelectionIndex()==1)
				{
					String str = textManager.getText();
					str=username+"(private):"+str;
					cout.println(str);
				}
			}
		});
		btnSendM.setText("发送信息");
		btnSendM.setBounds(344, 102, 80, 27);
		
		lblNewLabel_1 = new Label(shell, SWT.NONE);
		lblNewLabel_1.setAlignment(SWT.CENTER);
		lblNewLabel_1.setBounds(10, 139, 71, 17);
		lblNewLabel_1.setText("用户列表");
		
		list = new List(shell, SWT.BORDER);
		list.setBounds(10, 162, 175, 95);
		
		Button btnSendF = new Button(shell, SWT.NONE);
		btnSendF.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(list.getSelectionIndex()>=0&& list.getSelection()[0]!=username ) 
	 					SendFile(list.getSelection()[0]); 

			}


		});
		btnSendF.setBounds(10, 263, 80, 27);
		btnSendF.setText("发送文件");
		
		textArea = new Text(shell, SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL);
		textArea.setBounds(193, 163, 211, 128);
		
		Label label_2 = new Label(shell, SWT.NONE);
		label_2.setText("信息窗口");
		label_2.setAlignment(SWT.CENTER);
		label_2.setBounds(234, 139, 71, 17);
		
		labelUsername = new Label(shell, SWT.NONE);
		labelUsername.setBounds(77, 24, 211, 17);
		labelUsername.setText(username);
		
		button = new Button(shell, SWT.NONE);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(list.getItemCount()>0) 
 					SendFileALL();
				else
					textArea.append("当前没有用户在线");
			}
		});
		button.setText("群发文件");
		button.setBounds(106, 263, 80, 27);
		
		combo = new Combo(shell, SWT.DROP_DOWN | SWT.READ_ONLY);
		combo.setItems(new String[] {"全体", "私人"});
		combo.setBounds(277, 104, 61, 25);
		combo.select(0);

	}
}
