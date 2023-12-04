package com.example.spell;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    Button b;
    Button b2;
    Button b3,b4;
    TextView scoreT, nameT, highScoreT;
    String word, name, definition;
    DBHelper db;

    boolean playing = true;
    int score = 0;
    int highScore = 0;
    private static final int SPEECH_REQUEST_CODE = 18;
    TextToSpeech tts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR ){
                    tts.setLanguage(Locale.US);
                }
            }
        });
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},1);
        }
        getNewWord();


        db = new DBHelper(this);
        b = findViewById(R.id.button);
        b2 = findViewById(R.id.button2);
        b3 = findViewById(R.id.button3);
        b4 = findViewById(R.id.button4);
        scoreT = findViewById(R.id.textView);
        nameT = findViewById(R.id.textView2);
        highScoreT = findViewById(R.id.textView4);


        promptName();

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displaySpeechRecognizer();
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(playing)
                    tts.speak(word,TextToSpeech.QUEUE_FLUSH,null);
                else{
                    reset();
                }
            }
        });
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLeaderBoard();
            }
        });
        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(playing)
                    tts.speak(definition,TextToSpeech.QUEUE_FLUSH,null);
            }
        });
    }


    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0).toLowerCase(Locale.ROOT).replace(" ","");
            if(spokenText.equals(word)){
                getNewWord();
                score++;
                scoreT.setText("Score: "+score);
                tts.speak("Correct. Score "+ score,TextToSpeech.QUEUE_FLUSH,null);

            }else{
                tts.speak("Incorrect. Final Score " + score,TextToSpeech.QUEUE_FLUSH,null);
                endSession();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    void showLeaderBoard(){
        View dialogLayout = LayoutInflater.from(MainActivity.this).inflate(R.layout.leaderboard_layout,null);
        TextView name1 = dialogLayout.findViewById(R.id.name1);
        TextView name2 = dialogLayout.findViewById(R.id.name2);
        TextView name3 = dialogLayout.findViewById(R.id.name3);
        TextView score1 = dialogLayout.findViewById(R.id.score1);
        TextView score2 = dialogLayout.findViewById(R.id.score2);
        TextView score3 = dialogLayout.findViewById(R.id.score3);

        ArrayList<TextView> tvs = new ArrayList<>();
        tvs.add(name1);
        tvs.add(name2);
        tvs.add(name3);
        ArrayList<TextView> scores = new ArrayList<>();
        scores.add(score1);
        scores.add(score2);
        scores.add(score3);

        ArrayList<String> maxNames = db.getMaxNames();

        for(int i = 0; i<maxNames.size();i++){
            tvs.get(i).setText((i+1)+". "+maxNames.get(i));
            scores.get(i).setText(""+db.getScorebyName(maxNames.get(i)));
        }



        AlertDialog builder = new AlertDialog.Builder(this)
                .setTitle("Leaderboard")
                .setView(dialogLayout)
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                }).create();
        builder.show();
    }

    void endSession(){
        playing = false;
        b2.setText("Play again");
        b.setClickable(false);
        b4.setClickable(false);
        if(score>highScore){
            highScore = score;
            highScoreT.setText("High Score: "+highScore);
            db.updateData(name,score);
        }
    }

    void promptName(){
        View dialogLayout = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_layout,null);
        TextView t = dialogLayout.findViewById(R.id.dialogTextView);
        t.setText("Enter your name");
        AlertDialog builder = new AlertDialog.Builder(this)
                .setTitle("Welcome")
                .setView(dialogLayout)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EditText et = dialogLayout.findViewById(R.id.dialogEditText);
                        name = et.getText().toString();
                        nameT.setText(name);
                        if(db.nameExists(name)){
                            Toast.makeText(MainActivity.this, "Welcome Back "+name, Toast.LENGTH_SHORT).show();
                            highScore = db.getScorebyName(name);
                            highScoreT.setText("High Score: " + highScore);
                        }else{
                            Toast.makeText(MainActivity.this, "New user created ", Toast.LENGTH_SHORT).show();
                            db.insertdata(name, 0);

                        }
                    }
                }).create();
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    void getNewWord(){
        new AsyncClass().execute(""+((int)(Math.random()*6)+5));
    }

    public class AsyncClass extends AsyncTask<String, Void, JSONArray>
    {
        @Override
        protected JSONArray doInBackground(String...params){
            String len = params[0];
            String store = "";
            try {
                URL link = new URL("https://random-word-api.herokuapp.com/word?length="+len);
                URLConnection connection = link.openConnection();
                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String read;
                store = reader.readLine();
                while ((read = reader.readLine()) != null) {
                    store += read;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            JSONArray ja = null;
            try {
                ja = new JSONArray(store);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return ja;
        }

        @Override
        protected void onPostExecute(JSONArray json) {
            String s = "";
            try {
                s = (String)(json.get(0));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            word = s;
            new AsyncClass2().execute();
            Log.d("TAG",""+s);
        }
    }

    void reset(){
        score = 0;
        playing = true;
        scoreT.setText("Score: "+score);
        getNewWord();
        b2.setText("PLAY WORD");
        b.setClickable(true);
        b4.setClickable(true);
    }

    public class AsyncClass2 extends AsyncTask<String, Void, JSONArray>
    {
        @Override
        protected JSONArray doInBackground(String...params){
            String store = "";
            try {
                URL link = new URL("https://api.dictionaryapi.dev/api/v2/entries/en/"+word);
                URLConnection connection = link.openConnection();
                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String read;
                store = reader.readLine();
                while ((read = reader.readLine()) != null) {
                    store += read;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            JSONArray ja = null;
            try {
                ja = new JSONArray(store);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return ja;
        }

        @Override
        protected void onPostExecute(JSONArray json) {
            JSONObject obj = null;
            try {
                obj = (JSONObject)(json.get(0));
            } catch (Exception e) {
                definition = "No definition available";
            }
            JSONArray a = null;
            try {
                a = (JSONArray) (obj.getJSONArray("meanings"));
            } catch (Exception e) {
                definition = "No definition available";
            }
            JSONObject b = null;
            try {
                b = (JSONObject)(a.get(0));
            } catch (Exception e) {
                definition = "No definition available";
            }
            JSONArray c= null;
            try {
                c = b.getJSONArray("definitions");
            } catch (Exception e) {
                definition = "No definition available";
            }
            JSONObject d = null;
            try {
                d = (JSONObject) (c.get(0));
            } catch (Exception e) {
                definition = "No definition available";
            }
            String s = "No definition available";
            try {
                s = d.getString("definition");
            } catch (Exception e) {
                definition = "No definition available";
            }
            definition = s;
        }
    }

}