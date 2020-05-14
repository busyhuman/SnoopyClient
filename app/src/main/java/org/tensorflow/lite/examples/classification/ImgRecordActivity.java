package org.tensorflow.lite.examples.classification;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.examples.classification.SnoopyConnection.SnoopyHttpConnection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ImgRecordActivity extends AppCompatActivity {

    String ID, Query, nowtime;
    String[] FoodName, c_fn = new String[3];
    int[] stat_ID = new int[5];
    float[] Serving = new float[3];
    float[] f_kcal = new float[3];
    float[] carbo = new float[3];
    float[] pro = new float[3];
    float[] fat = new float[3];
    float[] Na = new float[3];
    int[] f_ID= new int[3];
    int foodNum, eatTime;
    float total_kcal, total_car, total_pro, total_fat, total_na;
    TextView t_eatTime, datetxt;
    TextView txtfdcal1,txtfdcal2,txtfdcal3;
    TextView totalCal;
    myDBHelper myHelper;
    SQLiteDatabase sqlDB;
    Cursor cursor;
    JSONArray jarray;
    JSONObject jsonObj;
    String str, str1;



    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.img_record);
        Intent intent = getIntent();


        Serving = intent.getFloatArrayExtra("Serving");
        FoodName = intent.getStringArrayExtra("FoodName");
        foodNum = intent.getIntExtra("foodNum", 0);
        ID = intent.getStringExtra("ID");
        eatTime = intent.getIntExtra("eatTime",0);
        nowtime = intent.getStringExtra("DATE");

        final Button endbtn;
        final TextView txtname1, txtname2, txtname3;
        final LinearLayout fdl1, fdl2, fdl3;

        t_eatTime = (TextView) findViewById(R.id.eatTime);

        if(eatTime==0){t_eatTime.setText("아침");}
        else if(eatTime==1){t_eatTime.setText("점심");}
        else if(eatTime==2){t_eatTime.setText("저녁");}


        ImageView back = (ImageView) findViewById(R.id.back);

        txtfdcal1 = (TextView) findViewById(R.id.txtkcal1);
        txtfdcal2 = (TextView) findViewById(R.id.txtkcal2);
        txtfdcal3 = (TextView) findViewById(R.id.txtkcal3);

        txtname1 = (TextView) findViewById(R.id.food_name1);
        txtname2 = (TextView) findViewById(R.id.food_name2);
        txtname3 = (TextView) findViewById(R.id.food_name3);

        fdl1 = (LinearLayout) findViewById(R.id.food_sel1) ;
        fdl2 = (LinearLayout) findViewById(R.id.food_sel2) ;
        fdl3 = (LinearLayout) findViewById(R.id.food_sel3) ;

        endbtn = (Button)findViewById(R.id.end_btn);

        txtname1.setText(FoodName[0]);
        txtname2.setText(FoodName[1]);
        txtname3.setText(FoodName[2]);

        datetxt = (TextView) findViewById(R.id.datetxt);

        datetxt.setText(nowtime);


            for(int i=0; i<3; i++) {
            Query = "SELECT Num, FoodName, Kcal, Carbo, Protein, Fat, Natrium FROM foods WHERE FoodName='"+ FoodName[i] +"'";
            myHelper = new myDBHelper(this);
            sqlDB = myHelper.getReadableDatabase();
            cursor = sqlDB.rawQuery(Query, null);

            while(cursor.moveToNext()){
                f_ID[i] = cursor.getInt(0);
                c_fn[i]  = cursor.getString(1);
                f_kcal[i] = cursor.getFloat(2);
                carbo[i] = cursor.getFloat(3);
                pro[i] = cursor.getFloat(4);
                fat[i] = cursor.getFloat(5);
                Na[i] = cursor.getFloat(6);
            }
            System.out.println(c_fn[i]+ " " + f_kcal[i]);

             sqlDB.close();
             cursor.close();
            }

        System.out.println("서빙 사이즈: "+Serving[0]+ " " + Serving[1]+ " " + Serving[2]);

        txtfdcal1.setText(String.valueOf(f_kcal[0]*Serving[0]));
        txtfdcal2.setText(String.valueOf(f_kcal[1]*Serving[1]));
        txtfdcal3.setText(String.valueOf(f_kcal[2]*Serving[2]));

        total_kcal = (f_kcal[0]*Serving[0])+(f_kcal[1]*Serving[1])+(f_kcal[2]*Serving[2]);

        totalCal = (TextView) findViewById(R.id.totalkcal);

        totalCal.setText(String.valueOf(total_kcal));

        Handler mHandler = new Handler(Looper.getMainLooper());

        class NewRunnable implements Runnable {


            @Override
            public void run() {
                str = SnoopyHttpConnection.makeConnection("https://busyhuman.pythonanywhere.com/stats/?format=json&user=" + ID + "&Date=" + nowtime + "&Timeslot=" + String.valueOf(eatTime),
                        "GET", null);
                System.out.println("기록화면 검색: " + str);

                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        if (!str.equals("[] ")) {
                            try {
                                jarray = new JSONArray(str); // JSONArray 생성
                                for (int i = 0; i < 3; i++) {
                                    jsonObj = jarray.getJSONObject(i);  // JSONObject 추출
                                    stat_ID[i] = jsonObj.getInt("StatsID");
                                    System.out.println(i+ " sID: " + stat_ID[i]);
                                }
                                //jsonObj = jarray.getJSONObject(0);
                                //stat_ID[0] = jsonObj.getInt("StatsID");
                                System.out.println("1 sID: " + stat_ID[1]);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        System.out.println("검색 완료");
                    }
                }, 0);
            }
        }

        Thread t = new Thread(new NewRunnable());
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }



        fdl1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Add_BookmarkActivity.class);
                intent.putExtra("FoodName", FoodName);
                intent.putExtra("eatTime", eatTime);
                intent.putExtra("f_kcal", f_kcal);
                intent.putExtra("Serving", Serving);
                intent.putExtra("DATE", nowtime);
                intent.putExtra("ID", ID);
                intent.putExtra("foodNum", 1);
                startActivity(intent);
            }
        });
        fdl2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Add_BookmarkActivity.class);
                intent.putExtra("FoodName", FoodName);
                intent.putExtra("eatTime", eatTime);
                intent.putExtra("f_kcal", f_kcal);
                intent.putExtra("DATE", nowtime);
                intent.putExtra("Serving", Serving);
                intent.putExtra("ID", ID);
                intent.putExtra("foodNum", 2);
                startActivity(intent);
            }
        });
        fdl3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Add_BookmarkActivity.class);
                intent.putExtra("FoodName", FoodName);
                intent.putExtra("eatTime", eatTime);
                intent.putExtra("Serving", Serving);
                intent.putExtra("f_kcal", f_kcal);
                intent.putExtra("DATE", nowtime);
                intent.putExtra("ID", ID);
                intent.putExtra("foodNum", 3);
                startActivity(intent);
            }
        });



        endbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                class del_Runnable implements Runnable {
                    @Override
                    public void run() {
                        for (int i = 0; i < 3; i++) {
                            System.out.println("삭제 sID: " + stat_ID[i]);
                            System.out.println("삭제" + SnoopyHttpConnection.makeConnection("https://busyhuman.pythonanywhere.com/stats/" + String.valueOf(stat_ID[i]) + "/", "DELETE", null));
                        }
                        System.out.println("삭제 완료");
                    }
                }

                Thread t = new Thread(new del_Runnable());
                t.start();
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                class add_Runnable implements Runnable {
                    @Override
                    public void run() {
                        for (int i = 0; i < 3; i++) {
                            String post = "Date=" + nowtime + "&Kcal=" + String.valueOf(f_kcal[i]*Serving[i]) + "&Carbo=" + String.valueOf(carbo[i]*Serving[i]) + "&Protein=" + String.valueOf(pro[i]*Serving[i]) + "&Fat=" + String.valueOf(fat[i]*Serving[i]) + "&Natrium=" + String.valueOf(Na[i]*Serving[i]) + "&Timeslot=" + String.valueOf(eatTime) + "&user=" + ID;
                            System.out.println("삽입: " + SnoopyHttpConnection.makeConnection("https://busyhuman.pythonanywhere.com/stats/?format=json",
                                    "POST", post));
                        }
                        System.out.println("삽입 완료");
                    }
                }
                Thread t1 = new Thread(new add_Runnable());
                t1.start();
                try {
                    t1.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                Intent intent = new Intent(getApplicationContext(), Main_StatsActivity.class);
                intent.putExtra("ID", ID);
                intent.putExtra("eatTime", eatTime);
                startActivity(intent);
                Toast.makeText(getApplicationContext(), "음식 기록 완료!", Toast.LENGTH_LONG).show();

            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

}
