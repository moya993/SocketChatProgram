package chatProj;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class MainServer extends Application{

	// ExecutorService는 여러 개의 쓰레드를 효율적으로 관리하기 위한 대표적인 라이브러리입니다. 
	//Thread Pool로 쓰레드를 처리하게 되면 기본적인 쓰레드 숫자의 제한을 두기 때문에 
	//갑작스런 Client 폭증에도 쓰레드의 수에는 제한이 있어 서버의 성능 저하를 방지할 수 있습니다. 
	//한정된 자원을 '안정적으로' 관리하기 위한 대비책

	public static ExecutorService threadPool;
	public static Vector<IoMethod> clients = new Vector<IoMethod>();
	
	ServerSocket serverSocket;
	
	// 메인 서버 구동 -> accept(클라이언트 접속 대기)
	public void startServer() {
		
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress(InetAddress.getLocalHost(), 5001));
			System.out.println("바인딩완료");
		} catch (IOException e) {
			e.printStackTrace();
			if(!serverSocket.isClosed()) {
				stopServer();
			}
			return;
		}
		
		Runnable thread = new Runnable() {
			
			@Override
			public void run() {
				while(true) {
					Socket socket;
					try {
						socket = serverSocket.accept();
						clients.add(new IoMethod(socket));
						System.out.println("[메시지 수신 성공] " 
						+ socket.getRemoteSocketAddress() 
						+ ": " + Thread.currentThread().getName());
					} catch (IOException e) {
						if(!serverSocket.isClosed()) {
							stopServer();
						}
						break;
					}
					
				}
			}
		};
		threadPool = Executors.newCachedThreadPool();
		threadPool.submit(thread);
	}
	
	// 서버 중지
	public void stopServer() {
		try {
			// 현재 작동 중인 모든 소켓 닫기
			Iterator<IoMethod> iterator = clients.iterator();
			while(iterator.hasNext()) {
					IoMethod client = iterator.next();
					client.socket.close();
					iterator.remove();
			}
			// 서버 소켓 객체 닫기
			if(serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}
			
			// 쓰레드 풀 종료하기
			if(threadPool != null && !threadPool.isShutdown()) {
				threadPool.shutdown();
				System.out.println("서버 종료");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// javafx ui
	@Override
	public void start(Stage arg0) throws Exception {
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(5));
		
		TextArea textArea = new TextArea();
		textArea.setEditable(false);
		textArea.setFont(new Font("나눔고딕", 15));
		root.setCenter(textArea);
		
		Button toggleButton = new Button("시작하기");
		toggleButton.setMaxWidth(Double.MAX_VALUE);
		BorderPane.setMargin(toggleButton, new Insets(1, 0, 0, 0));
		root.setBottom(toggleButton);
		
		InetAddress IP = InetAddress.getLocalHost();
		System.out.println(IP);
		int port = 5001;
		
		toggleButton.setOnAction(event -> {
			if(toggleButton.getText().equals("시작하기")) {
				startServer();
				Platform.runLater(() -> {
					String message = String.format("[서버 시작]\n", IP, port);
					textArea.appendText(message);
					toggleButton.setText("종료하기");
				});
			} else {
				stopServer();
				Platform.runLater(() -> {
					String message = String.format("[서버 종료]\n", IP, port);
					textArea.appendText(message);
					toggleButton.setText("시작하기");
				});
			}
		});
		
		Scene scene = new Scene(root, 400, 400);
		arg0.setTitle("[ 채팅 서버 ]");
		arg0.setOnCloseRequest(event -> stopServer());
		arg0.setScene(scene);
		arg0.show();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
