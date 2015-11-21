package com.jin123d.urp;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.baidu.mobstat.StatService;
import com.jin123d.Interface.GetData;
import com.jin123d.Interface.GetNetData;
import com.jin123d.adapter.ZjsjAdapter;
import com.jin123d.models.ZjsjModels;
import com.jin123d.util.Sp;
import com.jin123d.util.netUtil;
import com.jin123d.util.urlUtil;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ZjsjActivity extends AppCompatActivity implements GetNetData {
    private String cookie;
    private String tv;
    private ProgressDialog progressDialog;
    private ListView lv_zjsj;
    private ZjsjAdapter adapter;
    List<ZjsjModels> lists;
    private Toolbar toolbar;

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case urlUtil.DATA_FAIL:
                    progressDialog.dismiss();

                    Toast.makeText(ZjsjActivity.this, getString(R.string.getDataTimeOut), Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case urlUtil.DATA_SUCCESS:
                    if (tv == null) {
                        System.out.println("空");
                    } else {
                        progressDialog.dismiss();
                        Document doc = Jsoup.parse(tv);
                        if (getString(R.string.webTitle).equals(doc.title())) {
                            Toast.makeText(ZjsjActivity.this, getString(R.string.loginFail),
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        } else {

                            Elements e2 = doc.select("table");
                            if (e2.size() > 0) {
                                Elements es = e2.get(4).select("tr");
                                if (es.size() > 1) {
                                    for (int i = 1; i < es.size(); i++) {
                                        Elements e = es.get(i).select("td");
                                        if (e.size() > 4) {
                                            String mc = e.get(2).text();
                                            String bz = e.get(3).text();
                                            String xf = e.get(4).text();
                                            ZjsjModels ZjsjModels = new ZjsjModels(mc, bz, xf);
                                            lists.add(ZjsjModels);
                                            adapter.notifyDataSetChanged();
                                        } else {
                                            Toast.makeText(ZjsjActivity.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    }
                                } else {
                                    Toast.makeText(ZjsjActivity.this, "ERROR", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            } else {
                                Toast.makeText(ZjsjActivity.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                    }
                    break;
                case urlUtil.SESSION:
                    Toast.makeText(ZjsjActivity.this, getString(R.string.loginFail), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zjsj);
        Sp sp = new Sp(ZjsjActivity.this);
        cookie = sp.getCookie();
        initView();
        //getInfo();
        getData();
    }


    private void getData() {
        new Thread() {
            public void run() {
                netUtil.getPostData(urlUtil.URL + urlUtil.URL_ZJSJ, cookie, ZjsjActivity.this);
            }
        }.start();
    }

  /*  public void getInfo() {
        new Thread() {
            public void run() {
                tv = netUtil.doPost(urlUtil.URL + urlUtil.URL_ZJSJ, cookie);
                if (urlUtil.NET_FAIL.equals(tv)) {
                    handler.sendEmptyMessage(urlUtil.DATA_FAIL);
                } else if (urlUtil.SESSION_FAIL.equals(tv)) {
                    handler.sendEmptyMessage(urlUtil.SESSION);
                } else {
                    handler.sendEmptyMessage(urlUtil.DATA_SUCCESS);
                }
            }

            ;
        }.start();
    }*/


    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        lv_zjsj = (ListView) findViewById(R.id.lv_zjsj);
        progressDialog = new ProgressDialog(ZjsjActivity.this);
        progressDialog.setMessage(getString(R.string.getData));
        progressDialog.show();
        lists = new ArrayList<>();
        adapter = new ZjsjAdapter(lists, ZjsjActivity.this);
        lv_zjsj.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        StatService.onResume(ZjsjActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        StatService.onPause(ZjsjActivity.this);
    }


    @Override
    public void getDataSuccess(String Data) {
        tv = Data;
        handler.sendEmptyMessage(urlUtil.DATA_SUCCESS);
    }

    @Override
    public void getDataFail() {
        handler.sendEmptyMessage(urlUtil.DATA_FAIL);
    }

    @Override
    public void getDataSession() {
        handler.sendEmptyMessage(urlUtil.SESSION);
    }
}
