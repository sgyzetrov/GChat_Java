import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

public class Client{

	private JFrame clientFrame;
	private JList userList;
	private JTextArea textArea;
	private JTextField textField;
	private JButton sayButton;
	private JLabel sayLabel;


	private JButton connectButton;
	private JButton logoutbutton;
	//private JButton sayButton;
	//private JPanel northPanel;
	//private JPanel southPanel;
	private JScrollPane leftScroll;
	private JScrollPane rightScroll;
	private JSplitPane centerSplit;

	private DefaultListModel listModel;
	private boolean isConnected = false;

	private Socket socket;
	private PrintWriter writer;
	private BufferedReader reader;
	private MessageThread messageThread;// 负责接收消息的线程
	private Map<String, User> onLineUsers = new HashMap<String, User>();// 所有在线用户
	
	private String nickname;
	private JLabel nicknameLabel;
	private JTextField nicknameText;
	//private JButton nicknameButton;
	
	private JPanel jPanelNorth1; 
	private JPanel jPanelNorth0;
	private JPanel jPanelNorth2;
	
	private JLabel IPLabel; 
	private JLabel PortLabel;
	private JTextField Host_IP; 
	private JTextField PortText;
	
	private JScrollPane scroller;
	
	private JPanel jPanelSouth0; 
	private JPanel jPanelSouth1; 
	private JPanel jPanelSouth2;
	public Object[] red_options = {"<html><font color="+"RED"+">OK</font></html>"};
	public Object[] yellow_options = {"<html><font color="+"yellow"+">OK</font></html>"};
	// public Object[] orange_options = {"<html><font color="+"orange"+">chat</font></html>","<html><font color="+"orange"+">file</font></html>"};
	
	//双击用户名对应的事件
	// protected void whenDbClickLst() {
	// 	int m = JOptionPane.showOptionDialog(clientFrame,"<html><font color="+"orange"+" size="+"6"+">you choosed : " +userList.getSelectedValue()+"</font></html>","message",JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, orange_options, orange_options[0]);
	// 	if (m==0) {
	// 		int nport = Integer.parseInt(PortText.getText().trim());
	// 		String hostIp = Host_IP.getText().trim();
	// 		String name = nicknameText.getText().trim();
	// 		SingleClient aClient = new SingleClient(name,hostIp,nport); 
	// 		aClient.startUp();
	// 	}
	// }
	
	//主方法,程序入口-------------------------------------------------------------------------
	public static void main(String[] args) {
		new Client();
	}
	//-----------------------------------------------------------------------------------
	//执行发送------------------------------------------------------------------------------
	public void send() {
		if (!isConnected) {
			JOptionPane.showOptionDialog(clientFrame,"<html><font color="+"red"+" size="+"6"+">you have yet been connected to the server</font></html>","error",JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, red_options, red_options[0]);
			return;
		}
		String message = textField.getText().trim();
		if (message == null || message.equals("")) {
			JOptionPane.showOptionDialog(clientFrame,"<html><font color="+"red"+" size="+"6"+">blank messages are prohibited！</font></html>","error",JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, red_options, red_options[0]);
			return;
		}
		sendMessage(clientFrame.getTitle() + "@" + "ALL" + "@" + message);
		textField.setText(null);
	}
	//-----------------------------------------------------------------------------------
	//构造方法------------------------------------------------------------------------------
	public Client() {
		nickname = "user"+new java.util.Random().nextInt(200);
		
		//弹窗ui
		UIManager UI=new UIManager();
		UI.put("OptionPane.background", new Color(30, 30, 30));
		UI.put("Panel.background", new Color(30, 30, 30));
		UI.put("Button.background", new Color(30, 30, 30));
		
		textArea = new JTextArea();
		textField = new JTextField();
		IPLabel = new JLabel("server IP", JLabel.LEFT);
		Host_IP = new JTextField("127.0.0.1",10);
		PortLabel = new JLabel("server port", JLabel.LEFT);
		PortText = new JTextField("4444",10);
		nicknameText = new JTextField(nickname, 30);
		nicknameLabel = new JLabel("Username", JLabel.LEFT);  
		//nicknameButton = new JButton("one-time change"); 
		sayLabel = new JLabel("Message", JLabel.LEFT); 
		textField = new JTextField(30); 
		sayButton = new JButton("SEND");
		scroller = new JScrollPane(textField);
		connectButton = new JButton("connect");
		logoutbutton = new JButton("log out");
		
		listModel = new DefaultListModel();
		userList = new JList(listModel);
		
		jPanelNorth1 = new JPanel(); 
		jPanelNorth0 = new JPanel();
		jPanelNorth2 = new JPanel();
		jPanelSouth0 = new JPanel(); 
		jPanelSouth1 = new JPanel(); 
		jPanelSouth2 = new JPanel(); 
		
		clientFrame = new JFrame("client terminal");
		
		//IP部 
		jPanelNorth1.add(IPLabel);
		IPLabel.setForeground(new Color(32, 178, 170));
		IPLabel.setBackground(new Color(30, 30, 30));
		
		//IP框
		jPanelNorth1.add(Host_IP);
		Host_IP.setForeground(new Color(32, 178, 170));
		Host_IP.setBackground(new Color(30, 30, 30));
		Host_IP.setBorder(BorderFactory.createLineBorder(new Color(34, 139, 34)));//设置面板边框颜色
		
		//port部
		jPanelNorth1.add(PortLabel);
		PortLabel.setForeground(new Color(32, 178, 170));
		PortLabel.setBackground(new Color(30, 30, 30));
		//PortLabel.setBorder(BorderFactory.createLineBorder(new Color(34, 139, 34)));//设置面板边框颜色
		  
		//port输入框
		jPanelNorth1.add(PortText); 
		PortText.setForeground(new Color(32, 178, 170));
		PortText.setBackground(new Color(30, 30, 30));
		PortText.setBorder(BorderFactory.createLineBorder(new Color(34, 139, 34)));//设置面板边框颜色
		  
		//连接键
		jPanelNorth1.add(connectButton); 
		connectButton.setBackground(new Color(30, 30, 30));
		connectButton.setForeground(new Color(32, 178, 170));
		connectButton.setBorder(BorderFactory.createLineBorder(new Color(34, 139, 34)));//设置面板边框颜色

		//断开键
		jPanelNorth1.add(logoutbutton); 
		logoutbutton.setBackground(new Color(30, 30, 30));
		logoutbutton.setForeground(new Color(32, 178, 170));
		logoutbutton.setBorder(BorderFactory.createLineBorder(new Color(34, 139, 34)));//设置面板边框颜色
				
		//昵称部
		jPanelNorth2.add(nicknameLabel);
		nicknameLabel.setForeground(new Color(32, 178, 170));
		nicknameLabel.setBackground(new Color(30, 30, 30));
		  
		//昵称输入框
		jPanelNorth2.add(nicknameText); 
		nicknameText.setForeground(new Color(238,238,0));
		nicknameText.setBackground(new Color(165, 42, 42));
		nicknameText.setBorder(BorderFactory.createLineBorder(new Color(34, 139, 34)));//设置面板边框颜色
		
		/*
		//昵称设置键
		jPanelNorth2.add(nicknameButton);
		nicknameButton.setBackground(new Color(30, 30, 30));
		nicknameButton.setForeground(new Color(32, 178, 170));
		nicknameButton.setBorder(BorderFactory.createLineBorder(new Color(34, 139, 34)));//设置按钮边框颜色
		*/
		
		jPanelNorth0.setLayout(new BoxLayout(jPanelNorth0, BoxLayout.Y_AXIS));
		jPanelNorth0.add(jPanelNorth2);
		jPanelNorth0.add(jPanelNorth1);
		jPanelNorth1.setBackground(new Color(30, 30, 30));
		jPanelNorth2.setBackground(new Color(30, 30, 30));
		
		//中间的组件
		//textArea.setFocusable(false);
		textArea.setEditable(false);
		textArea.setBackground(new Color(30, 30, 30));
		textArea.setForeground(new Color(32, 178, 170));
		leftScroll = new JScrollPane(textArea);
		leftScroll.setBorder(new TitledBorder(null, "Messages",0, 0, null, new Color(32, 178, 170)));
		leftScroll.setBackground(new Color(30, 30, 30));
		leftScroll.setForeground(new Color(32, 178, 170));
		rightScroll = new JScrollPane(userList);
		userList.setBackground(new Color(30, 30, 30));
		userList.setForeground(new Color(32, 178, 170));
		rightScroll.setBorder(new TitledBorder(null, "Users",0, 0, null, new Color(32, 178, 170)));
		rightScroll.setBackground(new Color(30, 30, 30));
		rightScroll.setForeground(new Color(32, 178, 170));
		centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,leftScroll,rightScroll);
		centerSplit.setDividerLocation(400);

		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS); 
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		JScrollBar bar = scroller.getVerticalScrollBar();
		bar.setBackground(new Color(32, 178, 170));
		JLabel jb = new JLabel();
		jb.setBackground(new Color(32, 178, 170));
		jb.setOpaque(true);
		scroller.setCorner(JScrollPane.UPPER_RIGHT_CORNER,jb);
		  
		JScrollBar bar1 = scroller.getHorizontalScrollBar();
		bar1.setBackground(new Color(32, 178, 170));
		JLabel jb1 = new JLabel();
		jb1.setBackground(new Color(32, 178, 170));
		jb1.setOpaque(true);
		scroller.setCorner(JScrollPane.LOWER_LEFT_CORNER,jb1);
		
		// 更改JclientFrame的图标：
		Toolkit tool=clientFrame.getToolkit();
		Image myimage=tool.getImage("src\\c.jpg");
		clientFrame.setIconImage(myimage);
		
		// 南区的组件 
		//消息部
		jPanelSouth2.add(sayLabel);
		sayLabel.setForeground(new Color(32, 178, 170));
		  
		//消息输入框
		jPanelSouth2.add(textField);
		textField.setForeground(new Color(32, 178, 170));
		textField.setBackground(new Color(30, 30, 30));
		textField.setBorder(BorderFactory.createLineBorder(new Color(34, 139, 34)));//设置按钮边框颜色
		  
		  
		//消息发送建
		jPanelSouth2.add(sayButton);
		sayButton.setBackground(new Color(30, 30, 30));
		sayButton.setForeground(new Color(32, 178, 170));
		sayButton.setBorder(BorderFactory.createLineBorder(new Color(34, 139, 34)));//设置按钮边框颜色
		  
		jPanelSouth0.setLayout(new BoxLayout(jPanelSouth0, BoxLayout.Y_AXIS)); 
		jPanelSouth0.add(jPanelSouth2); 
		jPanelSouth2.setBackground(new Color(30, 30, 30));
		  
		clientFrame.getContentPane().add(BorderLayout.SOUTH, jPanelSouth0); 
		
		// 设置窗口可见 
		clientFrame.setVisible(true); 
		
		clientFrame.setLayout(new BorderLayout());
		clientFrame.add(jPanelNorth0, "North");
		clientFrame.add(centerSplit, "Center");
		clientFrame.add(jPanelSouth0, "South");
		clientFrame.setSize(550, 550);
		
		int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;
		int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;
		clientFrame.setLocation((screen_width - clientFrame.getWidth()) / 2,
				(screen_height - clientFrame.getHeight()) / 2);
		clientFrame.setVisible(true);

		// 写消息的文本框中按回车键时事件
		textField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				send();
			}
		});

		// 单击发送按钮时事件
		sayButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				send();
			}
		});

		// 单击连接按钮时事件
		connectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int port;
				if (isConnected) {
					JOptionPane.showOptionDialog(clientFrame,"<html><font color="+"red"+" size="+"6"+">already connected!</font></html>","error",JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, red_options, red_options[0]);
					return;
				}
				try {
					try {
						port = Integer.parseInt(PortText.getText().trim());
					} catch (NumberFormatException e2) {
						throw new Exception("<html><font color="+"red"+" size="+"6"+">port is not recognized, must be integer</font></html>");
					}
					String hostIp = Host_IP.getText().trim();
					String name = nicknameText.getText().trim();
					if (name.equals("") || hostIp.equals("")) {
						throw new Exception("<html><font color="+"red"+" size="+"6"+">please input IP or nickname!</font></html>");
					}
					boolean flag = connectServer(port, hostIp, name);
					if (flag == false) {
						throw new Exception("<html><font color="+"red"+" size="+"6"+">failed to connect, try again later!</font></html>");
					}
					clientFrame.setTitle(name);
					nicknameText.setEnabled(false);
					//JOptionPane.showOptionDialog(clientFrame, "<html><font color="+"yellow"+" size="+"6"+">connection established!</font></html>","message",JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, yellow_options, yellow_options[0]);
				} catch (Exception exc) {
					JOptionPane.showOptionDialog(clientFrame,exc.getMessage(),"warning",JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, red_options, red_options[0]);
				}
			}
		});
		
		// 双击用户名时的事件
		// userList.addMouseListener(new MouseAdapter(){  
		//     public void mouseClicked(MouseEvent e){  
		//         if(e.getClickCount()==2){   //When double click JList  
		//             whenDbClickLst();   //Event  
		//         }  
		//     }  
		// });  
		
		// 单击断开按钮时事件
		logoutbutton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!isConnected) {
					JOptionPane.showOptionDialog(clientFrame,"<html><font color="+"red"+" size="+"6"+">already offline!</font></html>","error",JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, red_options, red_options[0]);
					return;
				}
				try {
					boolean flag = closeConnection();// 断开连接
					if (flag == false) {
						throw new Exception("<html><font color="+"red"+" size="+"6"+">error occurd while trying to go offline!</font></html>");
					}
					JOptionPane.showOptionDialog(clientFrame, "<html><font color="+"yellow"+" size="+"6"+">you are now offline!</font></html>","message",JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, yellow_options, yellow_options[0]);
				} catch (Exception exc) {
					JOptionPane.showOptionDialog(clientFrame,exc.getMessage(),"error",JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, red_options, red_options[0]);
				}
			}
		});

		// 关闭窗口时事件
		clientFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (isConnected) {
					closeConnection();// 关闭连接
				}
				System.exit(0);// 退出程序
			}
		});
	}

	/**
	 * 连接服务器
	 * 
	 * @param port
	 * @param hostIp
	 * @param name
	 */
	public boolean connectServer(int port, String hostIp, String name) {
		// 连接服务器
		try {
			socket = new Socket(hostIp, port);// 根据端口号和服务器ip建立连接
			writer = new PrintWriter(socket.getOutputStream());
			reader = new BufferedReader(new InputStreamReader(socket
					.getInputStream()));
			// 发送客户端用户基本信息(用户名和ip地址)
			sendMessage(name + "@" + socket.getLocalAddress().toString());
			// 开启接收消息的线程
			messageThread = new MessageThread(reader, textArea);
			messageThread.start();
			isConnected = true;// 已经连接上了
			return true;
		} catch (Exception e) {
			textArea.append("ClientTerminal: failed to connect with server: port[" + port + "] ip["+ hostIp
					+ "]" + "\r\n");
			isConnected = false;// 未连接上
			return false;
		}
	}

	/**
	 * 发送消息
	 * 
	 * @param message
	 */
	public void sendMessage(String message) {
		writer.println(message);
		writer.flush();
	}

	/**
	 * 客户端主动关闭连接
	 */
	@SuppressWarnings("deprecation")
	public synchronized boolean closeConnection() {
		try {
			sendMessage("CLOSE");// 发送断开连接命令给服务器
			messageThread.stop();// 停止接受消息线程
			// 释放资源
			if (reader != null) {
				reader.close();
			}
			if (writer != null) {
				writer.close();
			}
			if (socket != null) {
				socket.close();
			}
			isConnected = false;
			return true;
		} catch (IOException e1) {
			e1.printStackTrace();
			isConnected = true;
			return false;
		}
	}

	// 不断接收消息的线程
	class MessageThread extends Thread {
		private BufferedReader reader;
		private JTextArea textArea;

		// 接收消息线程的构造方法
		public MessageThread(BufferedReader reader, JTextArea textArea) {
			this.reader = reader;
			this.textArea = textArea;
		}

		// 被动的关闭连接
		public synchronized void closeCon() throws Exception {
			// 清空用户列表
			listModel.removeAllElements();
			// 被动的关闭连接释放资源
			if (reader != null) {
				reader.close();
			}
			if (writer != null) {
				writer.close();
			}
			if (socket != null) {
				socket.close();
			}
			isConnected = false;// 修改状态为断开
		}

		public void run() {
			String message = "";
			while (true) {
				try {
					message = reader.readLine();
					StringTokenizer stringTokenizer = new StringTokenizer(
							message, "/@");
					String command = stringTokenizer.nextToken();// 命令
					if (command.equals("CLOSE"))// 服务器已关闭命令
					{
						textArea.append("server closed!\r\n");
						closeCon();// 被动的关闭连接
						return;// 结束线程
					} else if (command.equals("ADD")) {// 有用户上线更新在线列表
						String username = "";
						String userIp = "";
						if ((username = stringTokenizer.nextToken()) != null
								&& (userIp = stringTokenizer.nextToken()) != null) {
							User user = new User(username, userIp);
							onLineUsers.put(username, user);
							listModel.addElement(username);
						}
					} else if (command.equals("DELETE")) {// 有用户下线更新在线列表
						String username = stringTokenizer.nextToken();
						User user = (User) onLineUsers.get(username);
						onLineUsers.remove(user);
						listModel.removeElement(username);
					} else if (command.equals("USERLIST")) {// 加载在线用户列表
						int size = Integer
								.parseInt(stringTokenizer.nextToken());
						String username = null;
						String userIp = null;
						for (int i = 0; i < size; i++) {
							username = stringTokenizer.nextToken();
							userIp = stringTokenizer.nextToken();
							User user = new User(username, userIp);
							onLineUsers.put(username, user);
							listModel.addElement(username);
						}
					} else if (command.equals("MAX")) {// 人数已达上限
						textArea.append(stringTokenizer.nextToken()
								+ stringTokenizer.nextToken() + "\r\n");
						closeCon();// 被动的关闭连接
						JOptionPane.showOptionDialog(clientFrame,"<html><font color="+"red"+" size="+"6"+">server max user reached!</font></html>","error",JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, red_options, red_options[0]);
						return;// 结束线程
					} else {// 普通消息
						textArea.append(message + "\r\n");
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
