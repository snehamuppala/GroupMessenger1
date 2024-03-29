package edu.buffalo.cse.cse486586.groupmessenger1;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 * 
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {
    static final String TAG = GroupMessengerActivity.class.getSimpleName();
    String[] REMOTE_PORT = {"11108", "11112", "11116", "11120","11124"};
    static final int SERVER_PORT = 10000;
	int count=0;
	//Reference: OnPTestClickListener
    //lines: 39-45
    private final Uri providerUri=buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger1.provider");

    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */

        /*
         * Calculate the port number that this AVD listens on.
         * It is just a hack that I came up with to get around the networking limitations of AVDs.
         * The explanation is provided in the PA1 spec.
         */
        //Reference PA1
        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        Log.e(TAG, myPort);

        try {
            /*
             * Create a server socket as well as a thread (AsyncTask) that listens on the server
             * port.
             *
             * AsyncTask is a simplified thread construct that Android provides. Please make sure
             * you know how it works by reading
             * http://developer.android.com/reference/android/os/AsyncTask.html
             */
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            /*
             * Log is a good way to debug your code. LogCat prints out all the messages that
             * Log class writes.
             *
             * Please read http://developer.android.com/tools/debugging/debugging-projects.html
             * and http://developer.android.com/tools/debugging/debugging-log.html
             * for more information on debugging.
             */
            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }



        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());
        
        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));
        
        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */
        //Reference: PA1
        final EditText editText = (EditText) findViewById(R.id.editText1);
        //Reference: https://developer.android.com/reference/android/widget/Button
        //Lines:113-115
        final Button button=(Button)findViewById(R.id.button4);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){

                    String msg = editText.getText().toString() + "\n";
                    editText.setText(""); // This is one way to reset the input box.
                    TextView localTextView = (TextView) findViewById(R.id.textView1);
                    localTextView.append("\t" + msg); // This is one way to display a string.
                    Log.v("local", String.valueOf(localTextView));
                    TextView remoteTextView = (TextView) findViewById(R.id.textView1);
                    remoteTextView.append("\n");
                    Log.v("remote", String.valueOf(remoteTextView));

                    /*
                     * Note that the following AsyncTask uses AsyncTask.SERIAL_EXECUTOR, not
                     * AsyncTask.THREAD_POOL_EXECUTOR as the above ServerTask does. To understand
                     * the difference, please take a look at
                     * http://developer.android.com/reference/android/os/AsyncTask.html
                     */
                    new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);
            }
        });






    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }


    //Reference: PA1
    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];


            /*
             * TODO: Fill in your server code that receives messages and passes them
             * to onProgressUpdate().
             */

            try {
                while(true){

                    //Reference: https://docs.oracle.com/javase/tutorial/networking/sockets/clientServer.html
                      /* Note: 1. Invoking serverSocket and establishing the connection- accepting connection from client
                              2. PrintWriter class prints formatted representations of objects to a text-output stream.
                              3. BufferedReader class reads text from a character-input stream
                       */
                    Socket socket = serverSocket.accept();
                    PrintWriter writer= new PrintWriter(socket.getOutputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String  MessageInput="";

                    while((MessageInput=reader.readLine()) !=null) {


                        //Reference: https://developer.android.com/reference/android/os/AsyncTask.html#publishProgress(Progress...)
                        //Note: To publish updates on the UI thread while the background computation is still running
                        publishProgress(MessageInput);
                        //https://codereview.stackexchange.com/questions/149905/sending-ack-nack-for-packets

                        writer.println("ok");

                        //Reference: https://docs.oracle.com/javase/7/docs/api/java/io/Writer.html
                        //Note: Flushes the stream
                        writer.flush();
                        break;

                    }

                    //Reference: https://docs.oracle.com/javase/7/docs/api/java/io/Writer.html
                    //Closes the socket, flushing it first
                    socket.close();

                }}
            catch (IOException e)
            {
                Log.e(TAG, "ServerTask socket IOException");
            }


            return null;
        }

        protected void onProgressUpdate(String...strings) {
            /*
             * The following code displays what is received in doInBackground().
             */
            String strReceived = strings[0].trim();
            Log.v("message", strReceived);

            //Reference: PA2 PartA Document
            ContentValues keyValueToInsert = new ContentValues();
            // inserting <”key-to-insert”, “value-to-insert”>
            keyValueToInsert.put("key", String.valueOf(count));
            Log.v("key", String.valueOf(count));
            keyValueToInsert.put("value", strReceived);
            Log.v("value",strReceived);
            count++;
            Uri newUri = getContentResolver().insert(
                    providerUri,    // assume we already created a Uri object with our provider URI
                    keyValueToInsert
            );



            TextView remoteTextView = (TextView) findViewById(R.id.textView1);
            remoteTextView.append(strReceived + "\t\n");
            Log.v("remote1", String.valueOf(remoteTextView));
            TextView localTextView = (TextView) findViewById(R.id.textView1);
            localTextView.append("\n");
            Log.v("local1", String.valueOf(localTextView));

            /*
             * The following code creates a file in the AVD's internal storage and stores a file.
             *
             * For more information on file I/O on Android, please take a look at
             * http://developer.android.com/training/basics/data-storage/files.html
             */



            return;
        }
    }

    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            for(int i=0;i<REMOTE_PORT.length;i++) {


                try {
                    String remotePort = REMOTE_PORT[i];

                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(remotePort));

                    String msgToSend = msgs[0];

                    //Reference: https://docs.oracle.com/javase/tutorial/networking/sockets/clientServer.html
                      /*Note: 1. Invoking serverSocket and establishing the connection- accepting connection from client
                              2. PrintWriter class prints formatted representations of objects to a text-output stream.
                              3. BufferedReader class reads text from a character-input stream.
                       */

                    PrintWriter writer = new PrintWriter(socket.getOutputStream());
                    writer.println(msgToSend);

                    //Reference: https://docs.oracle.com/javase/7/docs/api/java/io/Writer.html
                    //Reference: https://docs.oracle.com/javase/tutorial/networking/sockets/clientServer.html
                    //Note: Flushes the stream
                    writer.flush();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    //
                    String ACK = "";
                    //Ack the message from server when server says "ok", socket closes
                    while ((ACK = reader.readLine()) != null) {
                        if (ACK.equals("ok")) {

                            //Reference: https://docs.oracle.com/javase/7/docs/api/java/io/Writer.html
                            //Closes the socket, flushing it first
                            socket.close();
                            break;
                        }
                    }
                    /*
                     * TODO: Fill in your client code that sends out a message.
                     */

                } catch (UnknownHostException e) {
                    Log.e(TAG, "ClientTask UnknownHostException");
                } catch (IOException e) {
                    Log.e(TAG, "ClientTask socket IOException");
                }
            }
            return null;
        }
    }






}
