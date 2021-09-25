package socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
// ChatServer�� 1���� Client�� ����� �ϱ� ���ؼ� �ʿ��� ����� ������ Ŭ����
public class ChatHandler {
	
	//��Ʈ��ũ�󿡼� ����ϱ� ���� ����
	Socket socket;
	
	//�Ű������� ���� �Ѿ���� ���� �̿��ؼ� ������ �ʱ�ȭ�ϴ� ������
	public ChatHandler(Socket socket) {
		this.socket = socket;
		receive();
	}
	
	// Ŭ���̾�Ʈ�κ��� �޽����� ���� �޴� �޼ҵ�.
	public void receive() {
		Runnable thread = new Runnable() {
			
			//�ϳ��� Thread ��� ���ν� ������ �� ������ run()���� ���Ǹ� ���ش�.
			@Override
			public void run() {
				try {
					
					//Ŭ���̾�Ʈ�κ��� �ݺ������� ������ ���޹��� �� �ֵ���
					while(true) {
						// ������ ���޹ޱ� ���� InputStream ��ü ����
						InputStream in = socket.getInputStream();
						// �� ���� 512byte��ŭ ���� ���� �� �ִ� ���� ����
						byte[] buffer = new byte[512];
						// ������ Ŭ���̾�Ʈ�κ��� ��� ������ ���޹޾Ƽ� buffer�� ����ֵ��� �� ��.
						// length�� ��� �޽����� ũ�⸦ �ǹ�.
						int length = in.read(buffer);
						
						// �޽����� �о���� �� ������ �߻��ߴٸ� �˷���.
						while(length== -1) throw new IOException();
						
						System.out.println("[�޽��� ���� ����] "
								+ socket.getRemoteSocketAddress() // Ŭ���̾�Ʈ�� �ּ� ���� ���
								+ "; " + Thread.currentThread().getName()); // Thread�� ������ ����(�̸���) ���
						
						// buffer���� ���޹��� ������ message������ ��Ƽ� ����� �� �ֵ��� �ϴ°�, �ѱ��� �����ϴ� UTF-8�� ���ڵ�.
						String message = new String(buffer, 0, length, "UTF-8");
						
						// �ܼ��ϰ� �޽����� ���޸� �޴� ���� �ƴ϶�, ���޹��� �޽����� �ٸ� Ŭ���̾�Ʈ���Ե� ���� �� �ֵ��� �����.
						for(ChatHandler client : ChatServer.clients) {
							client.send(message);
						}
						
					}
				}catch (Exception e) {
					// ������ �߻����� �� ������ ó���ϴ� �κ�
					// �̷��� ��ø�� �������� try-catch������ ���� �����.
					try {
						System.out.println("[�޽��� ���� ����] "
								+ socket.getRemoteSocketAddress() // �޽����� ���� Ŭ���̾�Ʈ�� �ּ� ���
								+ ": " + Thread.currentThread().getName()); // �ش� Thread�� ���� �̸��� ���.
					} catch (Exception e2) {
					}
				}
			}
		};
		// Thread�� ���������� �����ϱ� ���ؼ� Thread�� ThreadPool�� ������ش�.
		ChatServer.threadPool.submit(thread) ;
	}
	
	
	// Ŭ���̾�Ʈ���� �޽����� �����ϴ� �޼ҵ�.
	public void send(String message) {
		Runnable thread = new Runnable() {
			
			@Override
			public void run() {
				try {
					// �޽����� ���� ���� OutputStream �̿�
					OutputStream out = socket.getOutputStream();
					byte[] buffer = message.getBytes("UTF-8");
					
					// ������ �߻����� �ʾ��� ����..
					// ���ۿ� ��� ������ �������� Ŭ���̾�Ʈ�� �������ְڴٴ� ��.
					out.write(buffer);
					// �ݵ�� flush�� ����� ������� ������ ����ٴ� ���� �˷��� �� ����.
					out.flush();
				} catch (Exception e) {
					try {
						System.out.println("[�޽��� �۽� ����] "
								+ socket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread().getName());
						
	
						ChatServer.clients.remove(ChatHandler.this);
						socket.close();
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				};
			}
		};
		ChatServer.threadPool.submit(thread);
	}
	
	
}