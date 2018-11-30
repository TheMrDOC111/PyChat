package com.doc.pychat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class LoginActivity extends AppCompatActivity {

    Client client;
    Thread socketThready;
    TextView loginText;
    TextView passwordText;
    Button authButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginText = (TextView) findViewById(R.id.loginText);
        passwordText = (TextView) findViewById(R.id.passwodText);
        authButton = (Button) findViewById(R.id.authorizeButton);

        client = new Client("192.168.31.184", 9090);
        socketThready = new Thread(client);    //Создание потока "myThready"
        socketThready.start();
    }

    public void login(View v) {

        String[] message = {"auth", loginText.getText().toString(), passwordText.getText().toString()};
        loginText.setText("");
        passwordText.setText("");
        final String text = JsonParser.jsonEncrypt(message);


        Thread sendThread = new Thread(new Runnable() {
            public void run() {
                client.sendMessage(text);
            }
        });
        sendThread.start();


    }

    public void registration(View v) {
        String[] message = {"registration", loginText.getText().toString(), passwordText.getText().toString()};
        loginText.setText("");
        passwordText.setText("");
        final String text = JsonParser.jsonEncrypt(message);
        Thread sendThread = new Thread(new Runnable() {
            public void run() {
                client.sendMessage(text);
            }
        });
        sendThread.start();
    }

    public void toChatActivity(String token) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("token", token);
        socketThready.interrupt();
        startActivity(intent);

    }

    public void makeToast(final String text) {
        runOnUiThread(new Runnable() {
            public void run() {
                final Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
                toast.show();
            }
        });

    }


    public class Client implements Runnable {
        public void run() {
            SetClient();
        }

        public Socket clientSocket;
        private BufferedReader in;
        private BufferedWriter out;
        private String ip;
        private int port;
        Timer timer;

        Client(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }

        private void SetClient() {
            try {
                clientSocket = new Socket(ip, port);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
                out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
                Log.d("Client socket", clientSocket.toString());
                ReadMessage readMessageThread = new ReadMessage();
                readMessageThread.start();
            } catch (Exception e) {
                Log.d("Client socket", e.toString());
                clientSocket = null;
            }


            timer = new Timer();
            timer.schedule(new RestoreSocket(), 0, 2000);


        }

        void sendMessage(String message) {
            Log.d("Client socket", message);
            try {
                out.write(message + '\n');
                out.flush();
            } catch (Exception e) {
                e.printStackTrace();
                clientSocket = null;
            }
        }

        class ReadMessage extends Thread {
            @Override
            public void run() {
                String message;
                try {
                    while (true) {
                        message = in.readLine();
                        try {
                            Log.d("Client socket", "textMessage: " + message);
                            Message msg = JsonParser.jsonDecrypt(message);
                            if (msg.getStatus() && (msg.getType().equals("auth") || msg.getType().equals("registration"))) {
                                clientSocket = null;
                                timer.cancel();
                                in.close();
                                out.flush();
                                out.close();
                                toChatActivity(msg.getToken());
                            } else
                                makeToast(msg.getText());
                        } catch (Exception e) {
                            Log.e("socket", e.getMessage());
                            clientSocket = null;
                            in.close();
                            out.flush();
                            out.close();
                            break;
                        }
                    }
                } catch (IOException ex) {
                    Log.d("Client socket", ex.toString());
                    clientSocket = null;
                }
            }
        }

        class RestoreSocket extends TimerTask {
            public void run() {
                if (clientSocket == null)
                    try {
                        clientSocket = new Socket(ip, port);
                        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
                        out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
                        Log.d("Client socket", clientSocket.toString());
                        ReadMessage readMessageThread = new ReadMessage();
                        readMessageThread.start();
                    } catch (Exception e) {
                        Log.d("Client socket", e.toString());
                    }
            }
        }

    }
}
