package edu.buffalo.cse.cse486586.groupmessenger1;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import android.database.MatrixCursor;
import android.content.Context;
/**
 * GroupMessengerProvider is a key-value table. Once again, please note that we do not implement
 * full support for SQL as a usual ContentProvider does. We re-purpose ContentProvider's interface
 * to use it as a key-value table.
 * 
 * Please read:
 * 
 * http://developer.android.com/guide/topics/providers/content-providers.html
 * http://developer.android.com/reference/android/content/ContentProvider.html
 * 
 * before you start to get yourself familiarized with ContentProvider.
 * 
 * There are two methods you need to implement---insert() and query(). Others are optional and
 * will not be tested.
 * 
 * @author stevko
 *
 */
public class GroupMessengerProvider extends ContentProvider {
    static final String TAG = GroupMessengerActivity.class.getSimpleName();
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // You do not need to implement this.
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        /*
         * TODO: You need to implement this method. Note that values will have two columns (a key
         * column and a value column) and one row that contains the actual (key, value) pair to be
         * inserted.
         * 
         * For actual storage, you can use any option. If you know how to use SQL, then you can use
         * SQLite. But this is not a requirement. You can use other storage options, such as the
         * internal storage option that we used in PA1. If you want to use that option, please
         * take a look at the code for PA1.
         */

        /*
         * The following code creates a file in the AVD's internal storage and stores a file.
         *
         * For more information on file I/O on Android, please take a look at
         * http://developer.android.com/training/basics/data-storage/files.html
         */
        //Reference: PA1
        //Line:69-79
        String filename = values.getAsString("key") ;
        String string = values.getAsString("value");
        FileOutputStream outputStream;

        try {
            outputStream = getContext().openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(string.getBytes());
            outputStream.close();
        } catch (Exception e) {
            Log.e(TAG, "File write failed");
        }


        Log.v("insert", values.toString());
        return uri;
    }

    @Override
    public boolean onCreate() {
        // If you need to perform any one-time initialization task, please do it here.
        return false;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        /*
         * TODO: You need to implement this method. Note that you need to return a Cursor object
         * with the right format. If the formatting is not correct, then it is not going to work.
         *
         * If you use SQLite, whatever is returned from SQLite is a Cursor object. However, you
         * still need to be careful because the formatting might still be incorrect.
         *
         * If you use a file storage option, then it is your job to build a Cursor * object. I
         * recommend building a MatrixCursor described at:
         * http://developer.android.com/reference/android/database/MatrixCursor.html
         */
        //Reference: https://stackoverflow.com/questions/14768191/how-do-i-read-the-file-content-from-the-internal-storage-android-app
        //line:115-127
        try {
                FileInputStream inputstream = getContext().openFileInput(selection);
                InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
                BufferedReader bufferedReader = new BufferedReader(inputstreamreader);
                StringBuilder stringbuilder = new StringBuilder();
                String line1="";
                String line2="";
                while ((line1 = bufferedReader.readLine()) != null)
                {
                    stringbuilder.append(line1);
                }
                inputstream.close();

                line2 = stringbuilder.toString();
                //MatrixCursor stores rows as array of objects
                //Reference: https://stackoverflow.com/questions/9435158/how-to-populate-listview-from-matrix-cursor
                //Line: 131-133
                MatrixCursor matrixCursor = new MatrixCursor(new String[] { "key", "value" });
                Log.v("line2", line2);
                matrixCursor.addRow(new Object[] { selection, line2 });
                //selection--filename(key)
                //line2--value
                Log.v("matrix", String.valueOf(matrixCursor));
                return matrixCursor;

        }
        catch (FileNotFoundException e) {
            Log.e(TAG, "File not found");
        } catch (IOException e) {
            Log.e(TAG, "File read failed");
        }
        Log.v("query", selection);
        return null;
    }
}
