package com.doc.pychat;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    Client client;
    Thread socketThready;


    MessageAdapter messageAdapter;
    ListView messagesList;
    Button sendButton;
    EditText inputMessage;

    public static String token = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        start();

    }

    private void start() {
        sendButton = (Button) findViewById(R.id.sendButton);
        inputMessage = (EditText) findViewById(R.id.inputMessage);
        messagesList = (ListView) findViewById(R.id.messagesList) ;
        token = getIntent().getStringExtra("token");
        //sendButton.findFocus();
        messageAdapter = new MessageAdapter(this, R.layout.message_item);
        messagesList.setAdapter(messageAdapter);
        Log.d("Client socket", "Your token is " + token);
        client = new Client("192.168.31.184", 9090);
        socketThready = new Thread(client);    //Создание потока "myThready"
        socketThready.start();
    }


    public void sendMessage(View v) {
        if (client.clientSocket != null) {
            String text = inputMessage.getText().toString();
            String[] param = {"message", text, token};
            final String message = JsonParser.jsonEncrypt(param);
            inputMessage.setText("");
            Thread sendThread = new Thread(new Runnable() {
                public void run() {
                    client.sendMessage(message);
                }
            });
            sendThread.start();
        } else {
            makeToast("Connection lost!");
        }
    }

    public void addToMessageList(final Message message) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageAdapter.add(message);
                messageAdapter.notifyDataSetChanged();
                messagesList.setSelection(messageAdapter.getCount() - 1);
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
            } finally {
                timer = new Timer();
                timer.schedule(new RestoreSocket(), 0, 2000);
            }

        }


        void sendMessage(String message) {
            Log.d("Client socket", message);
            try {
                out.write(message);
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
                            Log.d("Client socket", message);
                            Message msg = JsonParser.jsonDecrypt(message);
                            addToMessageList(msg);
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

    public void makeToast(final String text) {
        runOnUiThread(new Runnable() {
            public void run() {
                final Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
                toast.show();
            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_exit) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_send) {

        } else if (id == R.id.nav_manage) {

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}