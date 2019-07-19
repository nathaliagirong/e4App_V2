package com.example.e4app.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.e4app.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private int x = 0;
    private int y = 0;
    private int mensaje = 12;
    String test = "Mensaje de prueba";
    private int[] arrayTest = new int[10000];
    ArrayList<Integer> iconList = new ArrayList<Integer>();
    StringWriter stringWriter = new StringWriter();

    String filename = "csvfile.csv";
    FileOutputStream outputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createArray();
    }

    private boolean isExternalStorageWritable(){
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            Log.i("State","Yes, it is writable!");
            return true;
        }else{
            return false;
        }
    }

    public boolean checkPermission(String permission){
        int check = ContextCompat.checkSelfPermission(this, permission);
        return (check == PackageManager.PERMISSION_GRANTED);
    }

    void createArray() {


        if(isExternalStorageWritable() && checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            File textFile = new File(Environment.getExternalStorageDirectory(), "MuchosDatos2.csv");
            try{
                FileOutputStream fos = new FileOutputStream(textFile);
                // Se crea el array
                for(x = 0 ; x < 10000 ; x++) {
                    iconList.add(x);
                }

                // Se crea el string a partir del array
                for (y = 0 ; y < iconList.size() ; y++) {
                    test += iconList.get(y);
                    test += "\n";
                }
                fos.write(test.getBytes());
                fos.close();

                Toast.makeText(this, "File Saved.", Toast.LENGTH_SHORT).show();
            }catch (IOException e){
                e.printStackTrace();
            }
        }else{
            Toast.makeText(this, "Cannot Write to External Storage.", Toast.LENGTH_SHORT).show();
        }

    }
}
