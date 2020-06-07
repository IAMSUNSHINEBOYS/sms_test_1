package com.cn.alen.util;
import net.sf.json.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class SendUtils {

    private static HttpClient client;

    private static final String APPLICATION_JSON = "application/json";

    private static final String CONTENT_TYPE_TEXT_JSON = "text/json";
    /**
     * 定义client使用的字符集
     */
    private final static String CHARSET = HTTP.UTF_8;

    /**
     * 最大连接数
     */
    public final static int MAX_TOTAL_CONNECTIONS = 400;

    /**
     * 获取连接的最大等待时间
     */
    public final static int HTTP_CLIENT_WAIT_TIMEOUT = 10000;

    /**
     * 每个路由最大连接数
     */
    public final static int MAX_ROUTE_CONNECTIONS = 200;

    /**
     * 连接超时时间
     */
    public final static int CONNECT_TIMEOUT = 40000;

    /**
     * 读取超时时间
     */
    public final static int READ_TIMEOUT = 4000;

    /** emc服务地址  */
    public static String SERVER_URL = "http://172.22.43.132:8888/";


    static{
        if(client == null){
//			client = new DefaultHttpClient();
//		    ClientConnectionManager mgr = client.getConnectionManager();
//		    HttpParams params = client.getParams();
//		    client = new DefaultHttpClient(new ThreadSafeClientConnManager(params,
//		            mgr.getSchemeRegistry()), params);
//			client = new DefaultHttpClient();

            HttpParams params = new BasicHttpParams();
            ConnManagerParams.setMaxTotalConnections(params, MAX_TOTAL_CONNECTIONS);
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, CHARSET);
            HttpProtocolParams.setUseExpectContinue(params, true);
            ConnManagerParams.setTimeout(params, HTTP_CLIENT_WAIT_TIMEOUT);
            HttpConnectionParams.setConnectionTimeout(params, CONNECT_TIMEOUT);
            HttpConnectionParams.setSoTimeout(params, READ_TIMEOUT);
            ConnPerRouteBean connPerRoute = new ConnPerRouteBean(
                    MAX_ROUTE_CONNECTIONS);
            ConnManagerParams.setMaxConnectionsPerRoute(params, connPerRoute);

            SchemeRegistry schReg = new SchemeRegistry();
            schReg.register(new Scheme("http", PlainSocketFactory
                    .getSocketFactory(), 80));
            schReg.register(new Scheme("https",
                    SSLSocketFactory.getSocketFactory(), 443));

            ClientConnectionManager conMgr = new ThreadSafeClientConnManager(
                    params, schReg);

            client = new DefaultHttpClient(conMgr, params);
        }
    }


    /**
     * 发送接口
     * @param url  发送接口url(已附带参数)
     * @return
     */
    public String sendToEmc(String url){
        String jsonResult = null;
        HttpPost httpPost = null;
        HttpResponse result = null;
        try {
            httpPost = new HttpPost(url);
            httpPost.addHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON);

            result = client.execute(httpPost);
            url = URLDecoder.decode(url, "UTF-8");
            /**请求发送成功，并得到响应**/
            if (result.getStatusLine().getStatusCode() == 200) {
                String str = "";
                try {
                    /**读取服务器返回过来的json字符串数据**/
                    str = EntityUtils.toString(result.getEntity());
//                  System.out.println("返回结果："+str);

                    jsonResult = str;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            if(httpPost != null){
                httpPost.abort();
            }
            e.printStackTrace();
        } finally{
            if (result != null) {
                EntityUtils.consumeQuietly(result.getEntity());
            }

            if(httpPost != null){
                httpPost.releaseConnection();
            }

        }
        return jsonResult;
    }

    /**
     * 获取发送渠道接口
     * @param appId  emc 接入端ID
     * @param appKey emc 接入端密钥
     * @return
     */
    public static String get_sendChannels(String appId, String appKey){
        String url = SERVER_URL + "restful/get_receiver?accountID=" + appId + "&accountKey=" + appKey;
        String msg = null;
        HttpGet request = null;
        try {
            request = new HttpGet(url);
            HttpResponse response = client.execute(request);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity httpEntity = response.getEntity();
                String responseResult = EntityUtils.toString(httpEntity);
                JSONObject obj = JSONObject.fromObject(responseResult);
                if("SUCCESS".equals(obj.getString("STATUS"))){
                    System.out.println("获取发送渠道成功，渠道JSON：" + obj.getString("DATA"));
                    msg = obj.getString("DATA");
                }else{
                    System.out.println("获取发送渠道失败，失败原因：" + obj.getString("MESSAGE"));
                }
            }
        }  catch (Exception e) {
            request.abort();
            e.printStackTrace();
        } finally{
            if(request != null){
                request.releaseConnection();
            }

        }
        return msg;
    }

    public static void main(String[] args) {

        String appId = "shiwuId";     //接入端appID
        String appKey = "shiwuKey";   //接入端appKey
        SendUtils client = new SendUtils();

        /** 此处测试获取发送渠道接口 */
        String channelDatas = get_sendChannels(appId, appKey);



        //--------------------------------分割线--------------------------------------------------------------



        /** 此处开始测试发送消息接口 */
        String url = SERVER_URL + "restful/sendmsg";   //发送消息的访问接口
        Map<String, Object> msgBody = new HashMap<String, Object>();
        msgBody.put("title", "消息标题");
        msgBody.put("isRich", "0");    //消息内容类型  0：纯文本  1：富文本，注意短信必须用0
//		msgBody.put("richContent", "<p>富文本消息内容主题</p>");   //如果消息内容类型为富文本时，该字段为必填字段，如为纯文本，不须填写。
        msgBody.put("content", "消息内容");     //该字段在代表消息内容
        msgBody.put("channels", "ydxy-mobile");   //注：多个渠道，中间用英文逗号隔开; ydxy-app 移动消息; im 即时通消息; email 邮箱; ydxy-corp 微信企业号; ydxy-mobile 短信
        msgBody.put("channelIds", "9063be90-aa2c-4ed2-b4e8-26d9033fcb42");         //短信-首易渠道
//		msgBody.put("channelIds", "77613d72-0615-403c-921c-829f354649fb");         //短信-校内
//		msgBody.put("channelIds", "1cfeca62-188c-48ed-8708-9880d9aa4f3a");         //邮件

        msgBody.put("isCron", "0");            //1 定时发送; 0 及时发送;
//		msgBody.put("sendtime", "0");          //定时发送时，需传递参数，时间格式为：yyyy-MM-dd HH:mm:ss
        msgBody.put("isForce", "1");           //1 强制状态消息，屏蔽者也可接收到; 0 普通到达消息，已屏蔽者不能接收到该消息;
        msgBody.put("typecode", "A001");          //接入端中消息类型中所配类型编码


        List<String> intReceiver = new ArrayList<String>();
        //TODO： 如需批量发送，请用以下接口先获取接收者id数组，再遍历发送
        //按部门（学院）	/restful/getUserByDept?deptid=
        //按分组 		/restful/getUserByGroup?groupid=
        intReceiver.add("00ecf122-608d-4df7-899c-1c11e73ed3bd");
        msgBody.put("intReceiver", intReceiver);            //内部接受用户; emc用户体系中，接收用户的ID


        Map<String, Object> extReceiver = new HashMap<String, Object>();

        //外部接收账号，以移动应用为例
        extReceiver.put("ydxy-mobile", new String[]{"--手机号码1--","--手机号码2--"});
        //如需其他渠道，例如邮箱，则取消注释下一行代码
//		extReceiver.put("email", new String[]{"wu_yya@sina.cn", "123456@qq.com"}); //其中wu_yya@sina.cn、123456@qq.com为emc用户体系外接收邮箱账号

        msgBody.put("extReceiver", extReceiver);

        Map<String, Object> sendMap = new HashMap<String, Object>();
        sendMap.put("accountID", appId);
        sendMap.put("accountKey", appKey);
        sendMap.put("msgJson", msgBody);
        String postData = JSONObject.fromObject(msgBody).toString();
        try {
            String encoderJson = URLEncoder.encode(postData, HTTP.UTF_8);
            url = url + "?accountID=" + appId + "&accountKey=" + appKey + "&msgJson=" + encoderJson;


            String jsonResult = client.sendToEmc(url);
            System.out.println("返回结果：" + jsonResult);
            JSONObject obj = JSONObject.fromObject(jsonResult);
            if("SUCCESS".equals(obj.get("STATUS"))){
                System.out.println("发送成功！");
            }else{
                System.out.println("发送失败，失败原因："+obj.getString("MESSAGE"));
            }
        } catch (Exception e) {

            e.printStackTrace();
        }

    }
}
