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
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

public class Server {

	private JFrame frame;
	private JTextArea contentArea;
	private JTextField txt_message;
	private JTextField txt_max;

	private JLabel sayLabel; 
	private JButton startButton;
	private JButton stopButton;
	private JButton sayButton;
	private JPanel jPanelNorth1;
	private JPanel jPanelNorth0;
	private JPanel jPanelNorth2;
	private JPanel southPanel;
	
	private JLabel portLabel;
	private JTextField portText;
	private JLabel maxLabel;
	
	private JScrollPane leftPanel;
	private JScrollPane rightPanel;
	private JSplitPane centerSplit;
	private JList userList;
	private DefaultListModel listModel;

	private ServerSocket serverSocket;
	private ServerThread serverThread;
	private ArrayList<ClientThread> clients;

	private boolean isStart = false;
	public Object[] red_options = {"<html><font color="+"RED"+">OK</font></html>"};
	public Object[] orange_options={"<html><font color="+"orange"+">OK</font></html>"};
	// public Object[] green_options = {"<html><font color="+"green"+">chat</font></html>","<html><font color="+"green"+">file</font></html>"};
	public SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//�������ڸ�ʽ
	
	// //˫���û�����Ӧ���¼�
	// protected void whenDbClickLst() {
	// 	JOptionPane.showOptionDialog(frame,"<html><font color="+"green"+" size="+"6"+">you choosed : " +userList.getSelectedValue()+"</font></html>","message",JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, green_options, green_options[0]);
	// }
	
	// ������,����ִ�����
	public static void main(String[] args) {
		new Server();
	}

	// ִ����Ϣ����
	public void send() {
		if (!isStart) {
			JOptionPane.showOptionDialog(frame,"<html><font color="+"red"+" size="+"6"+">you have yet to start the service!</font></html>","error",JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, red_options, red_options[0]);
			return;
		}
		if (clients.size() == 0) {
			JOptionPane.showOptionDialog(frame,"<html><font color="+"red"+" size="+"6"+">no user logged in yet!</font></html>","warning",JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, red_options, red_options[0]);
			return;
		}
		String message = txt_message.getText().trim();
		if (message == null || message.equals("")) {
			JOptionPane.showOptionDialog(frame,"<html><font color="+"red"+" size="+"6"+">blank messages are prohibited��</font></html>","error",JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, red_options, red_options[0]);
			return;
		}
		sendServerMessage(message);// Ⱥ����������Ϣ
		contentArea.append(df.format(new Date())+"@admin: " + txt_message.getText() + "\r\n");
		txt_message.setText(null);
	}

	// ����ŷ�
	public Server() {
		frame = new JFrame("server terminal");
		// ����JFrame��ͼ�꣺
		Toolkit tool=frame.getToolkit();
		Image myimage=tool.getImage("src\\c.jpg");
		frame.setIconImage(myimage);
		contentArea = new JTextArea();
		contentArea.setEditable(false);
		
		sayLabel = new JLabel("Message", JLabel.LEFT);
		txt_message = new JTextField(30);
		txt_max = new JTextField("50",20);
		portText = new JTextField("4444",30);
		startButton = new JButton("initiate");
		stopButton = new JButton("stop");
		sayButton = new JButton("SEND");
		stopButton.setEnabled(false);//��ʼ��Ϊ����ѡ��
		listModel = new DefaultListModel();
		userList = new JList(listModel);
		
		portLabel = new JLabel("port", JLabel.LEFT); 
		maxLabel = new JLabel("max user");
		
		southPanel = new JPanel();
		southPanel.add(sayLabel);
		southPanel.setBackground(new Color(250,128,114));
		southPanel.add(txt_message, "Center");
		sayLabel.setForeground(new Color(178, 34, 34));
		southPanel.add(sayButton, "East");
		sayButton.setForeground(new Color(238,238,0));
		sayButton.setBackground(new Color(47, 79, 79));
		
		jPanelNorth1 = new JPanel();
		jPanelNorth2 = new JPanel();
		jPanelNorth0 = new JPanel();

		leftPanel = new JScrollPane(contentArea);
		leftPanel.setBorder(new TitledBorder(null, "Messages",0, 0, null, new Color(178, 34, 34)));
		leftPanel.setBackground(new Color(255, 160, 122));
		leftPanel.setForeground(new Color(139,105,105));
		contentArea.setBackground(new Color(233,150,122));
		contentArea.setForeground(new Color(178, 34, 34));
		rightPanel = new JScrollPane(userList);
		rightPanel.setBorder(new TitledBorder(null, "Users",0, 0, null, new Color(178, 34, 34)));
		rightPanel.setBackground(new Color(255, 160, 122));
		rightPanel.setForeground(new Color(139,105,105));
		userList.setBackground(new Color(233,150,122));
		userList.setForeground(new Color(178, 34, 34));
		centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,leftPanel ,
				rightPanel);
		centerSplit.setDividerLocation(400);
		  
		// ��������� 
		jPanelNorth1.add(portLabel);
		portLabel.setForeground(new Color(238,238,0));
		//���山������
		jPanelNorth1.setBackground(new Color(165, 42, 42));
		jPanelNorth1.add(portText); 
		jPanelNorth1.add(startButton);
		jPanelNorth1.add(stopButton);
		startButton.setForeground(new Color(238,238,0));
		startButton.setBackground(new Color(47, 79, 79));
		stopButton.setForeground(new Color(238,238,0));
		stopButton.setBackground(new Color(47, 79, 79));
		
		jPanelNorth2.add(maxLabel);
		jPanelNorth2.add(txt_max);
		maxLabel.setForeground(new Color(238,238,0));
		jPanelNorth2.setBackground(new Color(165, 42, 42));
		
		jPanelNorth0.setLayout(new BoxLayout(jPanelNorth0, BoxLayout.Y_AXIS));
		jPanelNorth0.add(jPanelNorth1);
		jPanelNorth0.add(jPanelNorth2);
		frame.getContentPane().add(BorderLayout.NORTH, jPanelNorth0); 

		frame.setLayout(new BorderLayout());
		frame.add(jPanelNorth0, "North");
		frame.add(centerSplit, "Center");
		frame.add(southPanel, "South");
		frame.setSize(550, 550);
		//frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());//����ȫ��
		int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;
		int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;
		frame.setLocation((screen_width - frame.getWidth()) / 2,
				(screen_height - frame.getHeight()) / 2);
		frame.setVisible(true);

		// �رմ���ʱ�¼�
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (isStart) {
					closeServer();// �رշ�����
				}
				System.exit(0);// �˳�����
			}
		});

		// �ı��򰴻س���ʱ�¼�
		txt_message.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				send();
			}
		});

		// �������Ͱ�ťʱ�¼�
		sayButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				send();
			}
		});

		// ����������������ťʱ�¼�
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (isStart) {
					JOptionPane.showOptionDialog(frame,"<html><font color="+"red"+" size="+"6"+">service already started!</font></html>","error",JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, red_options, red_options[0]);
					return;
				}
				int max;
				int port;
				try {
					try {
						max = Integer.parseInt(txt_max.getText());
					} catch (Exception e1) {
						throw new Exception("<html><font color="+"red"+" size="+"6"+">max user number should be integer</font></html>");
					}
					if (max <= 0) {
						throw new Exception("<html><font color="+"red"+" size="+"6"+">max user number should be positive integer</font></html>");
					}
					try {
						port = Integer.parseInt(portText.getText());
					} catch (Exception e1) {
						throw new Exception("<html><font color="+"red"+" size="+"6"+">port is not recognized, must be integer</font></html>");
					}
					if (port <= 0) {
						throw new Exception("<html><font color="+"red"+" size="+"6"+">port is not recognized, must be positive integer</font></html>");
					}
					serverStart(max, port);
					contentArea.append("admin is now online! max user: " + max + ", port: " + port
							+ "\r\n");
					//JOptionPane.showOptionDialog(frame, "<html><font color="+"orange"+" size="+"6"+">server is now in use!</font></html>","message",JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, orange_options, orange_options[0]);
					startButton.setEnabled(false);
					txt_max.setEnabled(false);
					portText.setEnabled(false);
					stopButton.setEnabled(true);
				} catch (Exception exc) {
					JOptionPane.showOptionDialog(frame,exc.getMessage(),"warning",JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, red_options, red_options[0]);
				}
			}
		});
		
		// ˫���û���ʱ���¼�
		// userList.addMouseListener(new MouseAdapter(){  
		//     public void mouseClicked(MouseEvent e){  
		//         if(e.getClickCount()==2){   //When double click JList  
		//             whenDbClickLst();   //Event  
		//         }  
		//     }  
		// }); 
		
		// ����ֹͣ��������ťʱ�¼�
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!isStart) {
					JOptionPane.showOptionDialog(frame,"<html><font color="+"red"+" size="+"6"+">you have yet to start the service!</font></html>","error",JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, red_options, red_options[0]);
					return;
				}
				try {
					closeServer();
					startButton.setEnabled(true);
					txt_max.setEnabled(true);
					portText.setEnabled(true);
					stopButton.setEnabled(false);
					contentArea.append("admin logged out! connection ended.\r\n");
					JOptionPane.showOptionDialog(frame, "<html><font color="+"orange"+" size="+"6"+">service is now closed!</font></html>","message",JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, orange_options, orange_options[0]);
				} catch (Exception exc) {
					JOptionPane.showOptionDialog(frame,"<html><font color="+"red"+" size="+"6"+">error occurd while trying to go offline!</font></html>","error",JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, red_options, red_options[0]);
				}
			}
		});
	}

	// ����������
	public void serverStart(int max, int port) throws java.net.BindException {
		try {
			clients = new ArrayList<ClientThread>();
			serverSocket = new ServerSocket(port);
			serverThread = new ServerThread(serverSocket, max);
			serverThread.start();
			isStart = true;
		} catch (BindException e) {
			isStart = false;
			throw new BindException("<html><font color="+"red"+" size="+"6"+">port is in use, please switch!</font></html>");
		} catch (Exception e1) {
			e1.printStackTrace();
			isStart = false;
			throw new BindException("<html><font color="+"red"+" size="+"6"+">error starting up server!</font></html>");
		}
	}

	// �رշ�����
	@SuppressWarnings("deprecation")
	public void closeServer() {
		try {
			if (serverThread != null)
				serverThread.stop();// ֹͣ�������߳�

			for (int i = clients.size() - 1; i >= 0; i--) {
				// �����������û����͹ر�����
				clients.get(i).getWriter().println("CLOSE");
				clients.get(i).getWriter().flush();
				// �ͷ���Դ
				clients.get(i).stop();// ֹͣ����Ϊ�ͻ��˷�����߳�
				clients.get(i).reader.close();
				clients.get(i).writer.close();
				clients.get(i).socket.close();
				clients.remove(i);
			}
			if (serverSocket != null) {
				serverSocket.close();// �رշ�����������
			}
			listModel.removeAllElements();// ����û��б�
			isStart = false;
		} catch (IOException e) {
			e.printStackTrace();
			isStart = true;
		}
	}

	// Ⱥ����������Ϣ
	public void sendServerMessage(String message) {
		for (int i = clients.size() - 1; i >= 0; i--) {
			message = df.format(new Date())+"@admin: " + message;
			clients.get(i).getWriter().println(message);
			clients.get(i).getWriter().flush();
		}
	}

	// �������߳�
	class ServerThread extends Thread {
		private ServerSocket serverSocket;
		private int max;// ��������

		// �������̵߳Ĺ��췽��
		public ServerThread(ServerSocket serverSocket, int max) {
			this.serverSocket = serverSocket;
			this.max = max;
		}

		public void run() {
			while (true) {// ��ͣ�ĵȴ��ͻ��˵�����
				try {
					Socket socket = serverSocket.accept();
					if (clients.size() == max) {// ����Ѵ���������
						BufferedReader r = new BufferedReader(
								new InputStreamReader(socket.getInputStream()));
						PrintWriter w = new PrintWriter(socket
								.getOutputStream());
						// ���տͻ��˵Ļ����û���Ϣ
						String inf = r.readLine();
						StringTokenizer st = new StringTokenizer(inf, "@");
						User user = new User(st.nextToken(), st.nextToken());
						// �������ӳɹ���Ϣ
						w.println("MAX@admin: sorry, " + user.getName()+" ["+ user.getIp() + "] ��max user reached, try again later.");
						w.flush();
						// �ͷ���Դ
						r.close();
						w.close();
						socket.close();
						continue;
					}
					ClientThread client = new ClientThread(socket);
					client.start();// �����Դ˿ͻ��˷�����߳�
					clients.add(client);
					listModel.addElement(client.getUser().getName());// ���������б�
					contentArea.append(client.getUser().getName()
							+ " ["+client.getUser().getIp()+"] " + "is online!\r\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// Ϊһ���ͻ��˷�����߳�
	class ClientThread extends Thread {
		private Socket socket;
		private BufferedReader reader;
		private PrintWriter writer;
		private User user;

		public BufferedReader getReader() {
			return reader;
		}

		public PrintWriter getWriter() {
			return writer;
		}

		public User getUser() {
			return user;
		}

		// �ͻ����̵߳Ĺ��췽��
		public ClientThread(Socket socket) {
			try {
				this.socket = socket;
				reader = new BufferedReader(new InputStreamReader(socket
						.getInputStream()));
				writer = new PrintWriter(socket.getOutputStream());
				// ���տͻ��˵Ļ����û���Ϣ
				String inf = reader.readLine();
				StringTokenizer st = new StringTokenizer(inf, "@");
				user = new User(st.nextToken(), st.nextToken());
				// �������ӳɹ���Ϣ
				writer.println(user.getName() +" ["+ user.getIp()+"] " + "is now online!");
				writer.flush();
				// ������ǰ�����û���Ϣ
				if (clients.size() > 0) {
					String temp = "";
					for (int i = clients.size() - 1; i >= 0; i--) {
						temp += (clients.get(i).getUser().getName() + "/" + clients
								.get(i).getUser().getIp())
								+ "@";
					}
					writer.println("USERLIST@" + clients.size() + "@" + temp);
					writer.flush();
				}
				// �����������û����͸��û���������
				for (int i = clients.size() - 1; i >= 0; i--) {
					clients.get(i).getWriter().println(
							"ADD@" + user.getName() + user.getIp());
					clients.get(i).getWriter().flush();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@SuppressWarnings("deprecation")
		public void run() {// ���Ͻ��տͻ��˵���Ϣ�����д���
			String message = null;
			while (true) {
				try {
					message = reader.readLine();// ���տͻ�����Ϣ
					if (message.equals("CLOSE"))// ��������
					{
						contentArea.append(this.getUser().getName()
								+ " ["+this.getUser().getIp() +"] "+ "is now offline!\r\n");
						// �Ͽ������ͷ���Դ
						reader.close();
						writer.close();
						socket.close();

						// �����������û����͸��û�����������
						for (int i = clients.size() - 1; i >= 0; i--) {
							clients.get(i).getWriter().println(
									"DELETE@" + user.getName());
							clients.get(i).getWriter().flush();
						}

						listModel.removeElement(user.getName());// ���������б�

						// ɾ�������ͻ��˷����߳�
						for (int i = clients.size() - 1; i >= 0; i--) {
							if (clients.get(i).getUser() == user) {
								ClientThread temp = clients.get(i);
								clients.remove(i);// ɾ�����û��ķ����߳�
								temp.stop();// ֹͣ���������߳�
								return;
							}
						}
					} else {
						dispatcherMessage(message);// ת����Ϣ
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		// ת����Ϣ
		public void dispatcherMessage(String message) {
			StringTokenizer stringTokenizer = new StringTokenizer(message, "@");
			String source = stringTokenizer.nextToken();
			String owner = stringTokenizer.nextToken();
			String content = stringTokenizer.nextToken();
			message = df.format(new Date())+"@client< "+source+" > : " +  content;
			contentArea.append(message + "\r\n");
			if (owner.equals("ALL")) {// Ⱥ��
				for (int i = clients.size() - 1; i >= 0; i--) {
					clients.get(i).getWriter().println(message);
					clients.get(i).getWriter().flush();
				}
			}
		}
	}
}

