package chatProj;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

public class MainClient extends Application{
	Socket socket;
	TextArea textArea;
	int port = 5001;
	String ip = "211.221.45.233";
	// 태훈씨 ip 주소 : 211.221.45.233
	// 내 ip 주소 : 220.119.26.14
	
	// 클라이언트 connect
	public void startClient(String IP, int port) {
		Thread thread = new Thread() {
			public void run() {
				try {
					socket = new Socket(IP, port);
					receive();
					System.out.println("Connect2");
				} catch (IOException e) {
					if(!socket.isClosed()) {
						stopClient();
						System.out.println("[서버 접속 실패]");
						Platform.exit();
					}
					e.printStackTrace();
				}
			}
		};
		thread.start();
	}
	
	// 클라이언트 프로그램의 작동을 종료하는 메소드
	public void stopClient() {
		try {
			if (socket != null && !socket.isClosed()) {
				socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// 서버로 부터 메시지를 전달받는 메소드
	public void receive() {
		try {
			InputStream inputStream;
			while (true) {
				inputStream = socket.getInputStream();
				byte[] data = new byte[512];
				int size = inputStream.read(data);
				while(size== -1) throw new IOException();				
				String message = new String(data, 0, size, "UTF-8");
				Platform.runLater(() -> {
					textArea.appendText(message);
				});
			} 
		}catch (IOException e) {
			stopClient();
		}
	}
	
	// 서버로 메시지를 전송하는 메소드
	public void send(String message) {
		Thread thread = new Thread() {
			public void run() {
				try {
					OutputStream out = socket.getOutputStream();
					byte[] buffer = message.getBytes("UTF-8");
					out.write(buffer);
					System.out.println(" out " + out.toString());
					out.flush();
				} catch (IOException e) {
					stopClient();
				}
			}
		};
		thread.start();
	}
	
	@Override
	public void start(Stage arg0) throws Exception {
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(5));
		
		
		HBox hbox = new HBox();
		hbox.setSpacing(5);
		
		TextField userName = new TextField();
		userName.setPrefWidth(150);
		userName.setPromptText("닉네임을 입력하세요.");
		HBox.setHgrow(userName, Priority.ALWAYS);
		
		TextField IPText = new TextField(ip);
		TextField portText = new TextField(port+"");
		portText.setPrefWidth(80);
		
		hbox.getChildren().addAll(userName, IPText, portText);
		root.setTop(hbox);
		
		textArea = new TextArea();
		textArea.setEditable(false);
		root.setCenter(textArea);
		
		TextField input = new TextField();
		input.setPrefWidth(Double.MAX_VALUE);
		input.setDisable(true);
		
		input.setOnAction(event -> {
			send(userName.getText() + ": " + input.getText() + "\n");
			input.setText("");
			input.requestFocus();
		});
		
		Button sendButton = new Button("보내기");
		sendButton.setDisable(true);
		
		sendButton.setOnAction(event -> {
			send(userName.getText() + ": " + input.getText() + "\n");
			input.setText("");
			input.requestFocus();
		});
		
		
		
		Button connectionButton = new Button("접속하기");
		
		connectionButton.setOnAction(event -> {
			if(connectionButton.getText().equals("접속하기")) {
				port = Integer.parseInt(portText.getText());
				
				startClient(IPText.getText(), port);
				Platform.runLater(() -> {
					textArea.appendText("[ 채팅방 접속 ]\n");
				});
				connectionButton.setText("종료하기");
				input.setDisable(false);
				sendButton.setDisable(false);
				input.requestFocus();
			} else {
				stopClient();
				Platform.runLater(() -> {
					textArea.appendText("[ 채팅방 퇴장 ]\n");
				});
				connectionButton.setText("접속하기");
				input.setDisable(true);
				sendButton.setDisable(true);
			}
		});
		
		BorderPane pane = new BorderPane();
		pane.setLeft(connectionButton);
		pane.setCenter(input);
		pane.setRight(sendButton);
		
		root.setBottom(pane);
		
		Scene scene = new Scene(root, 400, 400);
		arg0.setTitle("[ 채팅 클라이언트 ]");
		arg0.setScene(scene);
		arg0.setOnCloseRequest(event -> stopClient());
		arg0.show();
		
		connectionButton.requestFocus();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
