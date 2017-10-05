package org.dualcom.xai_support;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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
import org.dualcom.xai_support.MyClass.Storage;
import org.dualcom.xai_support.MyClass.Windows;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends Activity {

    Context context = this;
    private String response;
    public ListView list_incorrect;
    public String[] uids;
    public String[] froms;
    public String[] tos;
    public String from = "0";
    public String to = "0";
    public String text = "0";
    public String datetime = "0";
    public JSONObject count_message;
    public JSONObject messages;
    public String UID = "admin";
    public TextView incorrect_not_found;
    public TextView inbox;
    public EditText message;
    public LinearLayout load_list;

    public ArrayList<list_const> lists = new ArrayList<list_const>();
    public list_box boxAdapter;

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
        setContentView(R.layout.activity_list);

        context.startService(new Intent(context, GetMessage.class));

        final LinearLayout nointernet = (LinearLayout) findViewById(R.id.nointernet);
        final Button refresh = (Button) findViewById(R.id.refresh);
        inbox = (TextView) findViewById(R.id.inbox);
        load_list = (LinearLayout) findViewById(R.id.load_list);

        if(isNetworkAvailable())
            new GetIncorrect().execute("get_list_incorrect", "uid=" + UID);
        else
            nointernet.setVisibility(nointernet.VISIBLE);

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                load_list.setVisibility(load_list.VISIBLE);
                new GetIncorrect().execute("get_list_incorrect", "uid=" + UID);
            }
        });

    }

    class GetIncorrect extends AsyncTask<String, String, String> {

        @SuppressWarnings("WrongThread")
        @Override
        protected String doInBackground(String... params) {
            String HOST = "http://rapoo.mysit.ru/api?module=";

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

                lists.clear();

                uids = new String[_count];
                froms = new String[_count];
                tos = new String[_count];

                String _serial = "";
                int inbox_c = 0;

                for (int i = 0; i < _count; i++) {

                    messages = (JSONObject) jsonObj.get("message" + i);
                    uids[i] = messages.get("uid") + "";
                    froms[i] = messages.get("from") + "";
                    from = messages.get("from") + "";
                    tos[i] = messages.get("to") + "";
                    to = messages.get("to") + "";
                    text = messages.get("message") + "";
                    datetime = messages.get("datetime") + "";

                    inbox_c += (from.equals("admin")) ? 0 : 1;

                    _serial = (from.equals("admin")) ? to : from;
                    _serial = (_serial.equals("for_all")) ? "Обращение ко всем..." : _serial;

                    lists.add(new list_const(text, datetime, from, _serial));

                }

                boxAdapter = new list_box(context, lists);
                list_incorrect = (ListView) findViewById(R.id.ListMessage);
                list_incorrect.setAdapter(null);
                list_incorrect.setAdapter(boxAdapter);

                inbox.setText("Неотвеченных обращений: " + inbox_c);
                load_list.setVisibility(load_list.GONE);

                list_incorrect.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {

                        Intent intent = new Intent(ListActivity.this, MainActivity.class);
                        intent.putExtra("uid", uids[position]);
                        intent.putExtra("serial",((froms[position].equals("admin"))?tos[position]:froms[position]));
                        startActivity(intent);

                    }
                });

            }
        }
    }
}
