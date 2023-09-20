package me.zhangjh.emoji.generator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson2.JSONObject;
import com.bumptech.glide.Glide;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import me.zhangjh.emoji.generator.constant.BizConstant;
import me.zhangjh.emoji.generator.entity.EmojiDrawResponse;
import me.zhangjh.emoji.generator.entity.EmojiDrawSubmitted;
import me.zhangjh.emoji.generator.entity.HttpRequest;
import me.zhangjh.emoji.generator.entity.OperationUrl;
import me.zhangjh.emoji.generator.entity.Response;
import me.zhangjh.emoji.generator.util.CommonUtil;
import me.zhangjh.emoji.generator.util.HttpClientUtil;

@SuppressLint("StaticFieldLeak")
public class NetworkRequestTask extends AsyncTask<String, Void, String> {

    private final Context context;

    private final List<ImageView> imgViewList = new ArrayList<>();

    private final SearchView searchView;

    private final RecyclerView sampleView;

    private final LinearLayout resultView;

    private final LoadingDialog loadingDialog;

    private final Timer timer = new Timer();

    private final List<String> drawResList = new ArrayList<>();

    public NetworkRequestTask(Context context, ImageView imgView1,
                              SearchView searchView, RecyclerView sampleView, LinearLayout resultView, LoadingDialog loadingDialog) {
        this.context = context;
        this.searchView = searchView;
        this.imgViewList.add(imgView1);
        this.sampleView = sampleView;
        this.resultView = resultView;
        this.loadingDialog = loadingDialog;
    }

    @Override
    protected String doInBackground(String... params) {
        String query = params[0];
        // 中文先翻译
        if(CommonUtil.isChinese(query)) {
            HttpRequest httpRequest = new HttpRequest("https://wx.zhangjh.me/baidu");
            Map<String, String> reqData = new HashMap<>();
            reqData.put("text", query);
            reqData.put("from", "zh");
            reqData.put("to", "en");
            httpRequest.setReqData(JSONObject.toJSONString(reqData));
            query = HttpClientUtil.postSync(httpRequest);
        }
        if(!query.toUpperCase().startsWith("A TOK")) {
            query = "A TOK emoji of " + query;
        }
        HttpRequest promptReq = new HttpRequest(BizConstant.SUBMIT_URL);
        Map<String, String> promptReqData = new HashMap<>();
        promptReqData.put("prompt", query);
//        promptReqData.put("nums", "2");
        promptReq.setReqData(JSONObject.toJSONString(promptReqData));
        String submittedRes = HttpClientUtil.postSync(promptReq);
        Response<String> response = JSONObject.parseObject(submittedRes, Response.class);
        if(!response.getSuccess()) {
            Toast.makeText(context, response.getErrorMsg(), Toast.LENGTH_SHORT).show();
            return null;
        }
        EmojiDrawSubmitted drawSubmitted = JSONObject.parseObject(JSONObject.toJSONString(response.getData()),
                EmojiDrawSubmitted.class);
        List<OperationUrl> urls = drawSubmitted.getUrls();
        // 设置定时器，轮询获取结果
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if(CollectionUtils.isNotEmpty(urls)) {
                    for (OperationUrl url : urls) {
                        String getUrl = url.getGet();
                        EmojiDrawResponse drawResponse = getDrawRes(getUrl);
                        if(drawResponse != null) {
                            if(StringUtils.isNotEmpty(drawResponse.getError())) {
                                Toast.makeText(context, drawResponse.getError(), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if(CollectionUtils.isNotEmpty(drawResponse.getOutput())) {
                                drawResList.add(drawResponse.getOutput().get(0));
                            }
                        }
                    }
                    if(drawResList.size() == urls.size()) {
                        publishProgress();
                    }
                }
            }
        };
        // 每1s执行一次轮询
        timer.schedule(task, 1000, 1000);
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
        // 停止轮询任务，渲染结果，停止loading
        timer.cancel();
        for (int i = 0; i < drawResList.size(); i++) {
            Glide.with(context).load(drawResList.get(i)).into(imgViewList.get(i));
            imgViewList.get(i).setTag(drawResList.get(i));
        }
        sampleView.setVisibility(View.GONE);
        resultView.setVisibility(View.VISIBLE);
        searchView.clearFocus();
        loadingDialog.dismiss();
    }

    private EmojiDrawResponse getDrawRes(String url) {
        if(StringUtils.isEmpty(url)) {
            return null;
        }
        String drawRes = HttpClientUtil.getSync(BizConstant.GET_DRAW_URL + "?url=" + url);
        Response<String> response = JSONObject.parseObject(drawRes, Response.class);
        if(!response.getSuccess()) {
            Toast.makeText(context, response.getErrorMsg(), Toast.LENGTH_SHORT).show();
            return null;
        }
        EmojiDrawResponse drawResponse = JSONObject.parseObject(JSONObject.toJSONString(response.getData()),
                EmojiDrawResponse.class);
        if(StringUtils.isNotEmpty(drawResponse.getError())) {
            return drawResponse;
        }
        if(CollectionUtils.isNotEmpty(drawResponse.getOutput())) {
            return drawResponse;
        }
        return null;
    }
}
