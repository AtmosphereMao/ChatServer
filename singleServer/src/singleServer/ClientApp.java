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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

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
						this.appendTextArea("��������쳣\n");
					}
					if(line.equalsIgnoreCase("exit"))	{
						try{
							socket.close();
							this.appendTextArea("���յ��������Ͽ�������Ϣ\n");
						}catch(IOException e)
						{
							this.appendTextArea("�׽��ֹر��쳣\n");
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
					}else{
						if(flag!='G')this.appendTextArea(line+"\n");
						else flag='T';
					}
				}else{
					String savePath = SaveFile(fFile[1],Integer.parseInt(fFile[2]));
					this.appendTextArea(fFile[3]+"�����ļ�"+fFile[1]+"\n����·��"+savePath+"\n"+"�ļ���С:"+fFile[2]+"B,��ע�����\n");
					flag='G';
				}
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
		dlg.setText("ѡ����Ҫ���͵��ļ�");
		String filePath = dlg.open();
		if(filePath == null)
			return;
		String fileManager = "SendFileByte:"+fileClient;
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
			
		}
		return savePath;
	}
	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(450, 338);
		shell.setText("SWT Application");
		
		Label label = new Label(shell, SWT.NONE);
		label.setBounds(10, 24, 61, 17);
		label.setText("�û�����");
		
		Label lblNewLabel = new Label(shell, SWT.NONE);
		lblNewLabel.setBounds(10, 68, 61, 17);
		lblNewLabel.setText("������IP:");
		
		textIP = new Text(shell, SWT.BORDER);
		textIP.setBounds(77, 65, 73, 23);
		
		Label label_1 = new Label(shell, SWT.NONE);
		label_1.setBounds(171, 68, 36, 17);
		label_1.setText("�˿ڣ�");
		
		textPort = new Text(shell, SWT.BORDER);
		textPort.setBounds(213, 65, 73, 23);
		
		btnQuit = new Button(shell, SWT.NONE);
		btnQuit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shell.close();
			}
		});
		btnQuit.setBounds(321, 19, 80, 27);
		btnQuit.setText("�˳���¼");
		
		btnConnect = new Button(shell, SWT.NONE);
		btnConnect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					InetAddress ip = InetAddress.getByName(textIP.getText());
					int port = Integer.parseInt(textPort.getText());
					socket= new Socket(ip,port);
//					MessageDialog.openInformation(new Shell(), "1", socket.toString());
					filesocket = new Socket(ip,port);
					textArea.append("�������ӷ�����\n");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					textArea.append("���������Ӵ���\n");
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
					textArea.append("��������쳣\n");
				}
			}
		});
		btnConnect.setText("���ӷ�����");
		btnConnect.setBounds(294, 62, 63, 27);
		
		btnDeconnect = new Button(shell, SWT.NONE);
		btnDeconnect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String str ="EXIT";
//				MessageDialog.openInformation(new Shell(), "1", socket.toString());
				
				if(socket!=null){
					MessageDialog.openInformation(new Shell(), "1", socket.toString());
					
					cout.println(str); 
					textArea.append("�ͻ�����Ͽ�����\n");
				} else
					textArea.append("���ѶϿ�����\n");
			}
		});
		btnDeconnect.setText("�Ͽ�����");
		btnDeconnect.setBounds(363, 62, 61, 27);
		
		textManager = new Text(shell, SWT.BORDER);
		textManager.setBounds(10, 104, 278, 23);
		
		btnSendM = new Button(shell, SWT.NONE);
		btnSendM.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String str = textManager.getText();
				str=username+":"+str;
				cout.println(str);
			}
		});
		btnSendM.setText("������Ϣ");
		btnSendM.setBounds(321, 100, 80, 27);
		
		lblNewLabel_1 = new Label(shell, SWT.NONE);
		lblNewLabel_1.setAlignment(SWT.CENTER);
		lblNewLabel_1.setBounds(10, 139, 71, 17);
		lblNewLabel_1.setText("�û��б�");
		
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
		btnSendF.setText("�����ļ�");
		
		textArea = new Text(shell, SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL);
		textArea.setBounds(193, 163, 211, 128);
		
		Label label_2 = new Label(shell, SWT.NONE);
		label_2.setText("��Ϣ����");
		label_2.setAlignment(SWT.CENTER);
		label_2.setBounds(234, 139, 71, 17);
		
		labelUsername = new Label(shell, SWT.NONE);
		labelUsername.setBounds(77, 24, 211, 17);
		labelUsername.setText(username);

	}
}