package org.dualcom.xai_support;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.dualcom.xai_support.MyClass.*;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    Context context = this;
    private String response;
    public ListView list_incorrect;
    public String from = "0";
    public String to = "0";
    public String text = "0";
    public String manifest = "";
    public String datetime = "0";
    public JSONObject count_message;
    public JSONObject messages;
    public String UID = "";
    public String SID = "";
    public String USERIAL = "";
    public TextView incorrect_not_found;
    public EditText message;
    public Button btn_send;

    public LinearLayout ManifestToggle;
    public TextView ManifestText;
    public TextView ManifestLabel;
    public Boolean ManifestLock = true;

    public ArrayList<incorrect_const> incorrects = new ArrayList<incorrect_const>();
    public incorrect_box boxAdapter;

    //Проверка доступности сети
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if( activeNetworkInfo == null) return false;
        boolean res = (!activeNetworkInfo.isConnected())?false:true;
        res = (!activeNetworkInfo.isAvailable())?false:true;
        return res;
    }
    /*************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        SID = intent.getStringExtra("uid");
        USERIAL = intent.getStringExtra("serial");
        USERIAL = (USERIAL.length()>40) ? USERIAL.substring(0,40)+"..." : USERIAL;

        final LinearLayout nointernet = (LinearLayout) findViewById(R.id.nointernet);
        final TextView serial_text = (TextView) findViewById(R.id.serial);
        btn_send = (Button) findViewById(R.id.btn_send);
        ManifestToggle = (LinearLayout) findViewById(R.id.manifest_toggle);
        ManifestText = (TextView) findViewById(R.id.manifest_text);
        ManifestLabel = (TextView) findViewById(R.id.manifest_label);

        serial_text.setText((USERIAL.equals("for_all"))?"Обращение ко всем...":USERIAL); //Вывод сериала

        //Идентификация устройства
        final String SERIAL = Build.SERIAL;
        final String BRAND = Build.BRAND;
        final String MANUFACTURER = Build.MANUFACTURER;
        final String PRODUCT = Build.PRODUCT;
        UID = SERIAL+BRAND+MANUFACTURER+PRODUCT;
        UID = "admin";
        //************************

        if(isNetworkAvailable())
            new GetIncorrect().execute("get_incorrect.php", "uid=" + SID);
        else
            nointernet.setVisibility(nointernet.VISIBLE);

        final TextView Labelincorrect = (TextView) findViewById(R.id.Labelincorrect);
        message = (EditText) findViewById(R.id.message);
        final Button GO_BACK = (Button) findViewById(R.id.GO_BACK);
        incorrect_not_found = (TextView) findViewById(R.id.incorrect_not_found);

        GO_BACK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeActivity();
            }
        });

        Labelincorrect.setText(getString(R.string.incorrect_title)); //Storage.loadData(context, "NOW_GROUP")

        incorrect_not_found.setText(getString(R.string.loading));

        message.setOnEditorActionListener(new EditText.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView value, int actionId,
                                          KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
                        || (actionId == EditorInfo.IME_ACTION_DONE)) {

                    String text = value.getText().toString();

                    if(text.length() > 0){
                        message.setEnabled(false);
                        btn_send.setEnabled(false);

                        new SandIncorrect().execute("incorrectSend.php",
                                "uid=" + UID,
                                "sid=" + SID,
                                "text=" + text);
                    }

                }
                return false;
            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String text = message.getText().toString();

                if(text.length() > 0){
                    message.setEnabled(false);
                    btn_send.setEnabled(false);

                    new SandIncorrect().execute("incorrectSend.php",
                            "uid=" + UID,
                            "sid=" + SID,
                            "text=" + text);
                }

            }
        });


        ManifestToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int _scalable = 299;

                if(!ManifestLock) {
                    if (ManifestToggle.getY() < 0)
                        ManifestToggle.animate().yBy(_scalable);
                    else
                        ManifestToggle.animate().yBy(-_scalable);
                }
            }
        });

    }

    private void closeActivity() {
        this.finish();
    }

    class SandIncorrect extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String HOST = "http://rapoo.mysit.ru/android/";

            try{
                DefaultHttpClient hc = new DefaultHttpClient();
                HttpPost postMethod = new HttpPost(HOST+params[0]);
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

                int count = params.length;
                String[] param = null;

                for(int i = 1; i < count; i++) {
                    param = params[i].split("=");
                    nameValuePairs.add(new BasicNameValuePair(param[0], param[1]));
                }

                postMethod.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));

                HttpResponse httpResponse = hc.execute(postMethod);
                HttpEntity httpEntity = httpResponse.getEntity();
                response = EntityUtils.toString(httpEntity, "UTF-8");

            }catch(Exception e){
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String res) {
            super.onPostExecute(res);

            if(res.equals("true"))
                new GetIncorrect().execute("get_incorrect.php",
                        "uid=" + SID);

        }

    }

    class GetIncorrect extends AsyncTask<String, String, String> {

        @SuppressWarnings("WrongThread")
        @Override
        protected String doInBackground(String... params) {
            String HOST = "http://rapoo.mysit.ru/android/";

            try{
                DefaultHttpClient hc = new DefaultHttpClient();
                HttpPost postMethod = new HttpPost(HOST+params[0]);
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

                int count = params.length;
                String[] param = null;

                for(int i = 1; i < count; i++) {
                    param = params[i].split("=");
                    nameValuePairs.add(new BasicNameValuePair(param[0], param[1]));
                }

                postMethod.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));

                HttpResponse httpResponse = hc.execute(postMethod);
                HttpEntity httpEntity = httpResponse.getEntity();
                response = EntityUtils.toString(httpEntity, "UTF-8");

            }catch(Exception e){
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String res) {
            super.onPostExecute(res);

            if(!res.equals("false")) {

                JSONParser parser = new JSONParser();

                Object obj = null;
                try {
                    obj = parser.parse(res);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                JSONObject jsonObj = (JSONObject) obj;

                count_message = (JSONObject) jsonObj.get("count");
                int _count = Integer.parseInt(count_message.get("value").toString());

                Storage.saveData(context,"incorrectCount",_count+"");

                incorrects.clear();

                for (int i = 0; i < _count; i++) {

                    messages = (JSONObject) jsonObj.get("message" + i);
                    from = messages.get("from") + "";
                    to = messages.get("to") + "";
                    text = messages.get("message") + "";
                    manifest = messages.get("manifest") + "";
                    datetime = messages.get("datetime") + "";

                    incorrects.add(new incorrect_const(text, datetime, from));

                }

                if(manifest.length()>10) {
                    ManifestToggle.animate().yBy(100);
                    ManifestLock = false;
                    ManifestText.setText(manifest);
                    ManifestLabel.setText("Манифест собран");
                }

                boxAdapter = new incorrect_box(context, incorrects);
                list_incorrect = (ListView) findViewById(R.id.list_incorrect);
                list_incorrect.setAdapter(null);
                list_incorrect.setAdapter(boxAdapter);

                message.setText("");
                incorrect_not_found.setVisibility(View.GONE);
                message.setEnabled(true);
                btn_send.setEnabled(true);
                //Windows.Open(context,"Тест JSON",_count+"");
            }else{
                incorrect_not_found.setText(getString(R.string.incoorect_not_found));
                incorrect_not_found.setVisibility(View.VISIBLE);
            }
        }
    }

}
