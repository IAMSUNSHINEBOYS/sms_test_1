package com.cn.alen.util;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
public class ImRestUtils {

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
    public static String SERVER_URL = "http://172.22.43.140:8080/mng";


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


    public String postData(String url, Map<String, Object> jsonData){
        String jsonResult = null;
        HttpPost httpPost = null;
        HttpResponse result = null;
        try {
            httpPost = new HttpPost(url);
            httpPost.addHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON);
            String postData = JSONObject.fromObject(jsonData).toString();
            //String encoderJson = URLEncoder.encode(postData, HTTP.UTF_8);

            httpPost.addHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON);

            StringEntity se = new StringEntity(postData, "utf-8");
            se.setContentType(CONTENT_TYPE_TEXT_JSON);
            se.setContentEncoding("UTF-8");
            se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON));
            httpPost.setEntity(se);
            result = client.execute(httpPost);
            url = URLDecoder.decode(url, "UTF-8");

            String str = "";
            /**请求发送成功，并得到响应**/
            if (result.getStatusLine().getStatusCode() == 200) {

                try {
                    /**读取服务器返回过来的json字符串数据**/

//                  System.out.println("返回结果："+str);
                    str = EntityUtils.toString(result.getEntity());
                    jsonResult = str;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{
                System.err.println("链接错误");
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

    //注册用户
    public static String registUser(String loginName){
        String url  = SERVER_URL + "/httpservices/user-register.action";
        ImRestUtils imRestUtils = new ImRestUtils();
        Map<String, Object> user = new HashMap<String, Object>();
        user.put("loginName", loginName);
        user.put("password", "12345");
//		user.put("loginNumber", "");  //im号，选填，不填默认
        user.put("random", true);
        user.put("md5", false);
        user.put("email", "123@qq.com");
        user.put("allowEmailLogin", false);
        user.put("mobile", "12345678901");
        user.put("allowMobileLogin", "false");
        user.put("registeredTime", new Date().getTime());
        user.put("displayName", loginName);
        user.put("age", 11);
        user.put("birthday", "1992-01-01");
        user.put("sex", 1);
        user.put("grade", 1);
//		user.put("role", );
//		user.put("level", );
        Map<String, Object> sendDatas = new HashMap<String, Object>();
        String jsonString = JSONObject.fromObject(user).toString();
        sendDatas.put("jsonString", jsonString);

        String jsonData = null;
        try {
            jsonData = imRestUtils.postData(url + "?jsonString=" + URLEncoder.encode(jsonString, HTTP.UTF_8), null);
            System.out.println("注册成员结果：" + jsonData);

        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return jsonData;
    }

    /**
     *  修改用户
     */
    public static String modifyUserPwd(){
        String url  = SERVER_URL + "/httpservices/user-passwd.action";
        ImRestUtils imRestUtils = new ImRestUtils();
        Map<String, Object> user = new HashMap<String, Object>();
        user.put("loginName", "wuy");
        user.put("password", "123456");

        user.put("md5", true);

        Map<String, Object> sendDatas = new HashMap<String, Object>();
        String jsonString = JSONObject.fromObject(user).toString();
        sendDatas.put("jsonString", jsonString);

        String jsonData = null;
        try {
            jsonData = imRestUtils.postData(url + "?jsonString=" + URLEncoder.encode(jsonString, HTTP.UTF_8), null);
            System.out.println("修改密码结果：" + jsonData);

        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return jsonData;
    }

    //获取用户
    public static String getUser(String userName){
        String url  = SERVER_URL + "/httpservices/user-get.action";
        ImRestUtils imRestUtils = new ImRestUtils();
        Map<String, Object> user = new HashMap<String, Object>();
        user.put("loginName", userName);

        Map<String, Object> sendDatas = new HashMap<String, Object>();
        String jsonString = JSONObject.fromObject(user).toString();
        sendDatas.put("jsonString", jsonString);

        String jsonData = null;
        try {
            jsonData = imRestUtils.postData(url + "?jsonString=" + URLEncoder.encode(jsonString, HTTP.UTF_8), null);
            System.out.println("获取成员结果：" + jsonData);

        } catch (UnsupportedEncodingException e) {

            e.printStackTrace();
        }
        return jsonData;
    }


    //注册用户
    public static String updateUser(){
        String url  = SERVER_URL + "/httpservices/user-update.action";
        ImRestUtils imRestUtils = new ImRestUtils();
        Map<String, Object> user = new HashMap<String, Object>();
        user.put("loginName", "b-y");

//		user.put("loginNumber", "");  //im号，选填，不填默认
        user.put("flag", 0);

        user.put("email", "123@qq.com");
        user.put("allowEmailLogin", false);
        user.put("mobile", "12345678901");
        user.put("allowMobileLogin", false);

        user.put("displayName", "b-y");
        user.put("age", 11);
        user.put("birthday", "1992-02-01");
        user.put("sex", -1);

        user.put("watchword", -1);          //个性签名
        user.put("watchwordChanged", false);
        user.put("headFileName", -1);		//头像地址
        user.put("headFileNameChanged", false);
        user.put("englishName", -1);		//英文名
        user.put("englishNameChanged", false);


        Map<String, Object> sendDatas = new HashMap<String, Object>();
        String jsonString = JSONObject.fromObject(user).toString();
        sendDatas.put("jsonString", jsonString);

        String jsonData = null;
        try {
            jsonData = imRestUtils.postData(url + "?jsonString=" + URLEncoder.encode(jsonString, HTTP.UTF_8), null);
            System.out.println("更新成员结果：" + jsonData);

        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return jsonData;
    }


    //删除用户
    public static String deleteUser(String userName){
        String url  = SERVER_URL + "/httpservices/user-delete.action";
        ImRestUtils imRestUtils = new ImRestUtils();
        Map<String, Object> user = new HashMap<String, Object>();
        user.put("loginName", new String[]{userName});

        Map<String, Object> sendDatas = new HashMap<String, Object>();
        String jsonString = JSONObject.fromObject(user).toString();
        sendDatas.put("jsonString", jsonString);

        String jsonData = null;
        try {
            jsonData = imRestUtils.postData(url + "?jsonString=" + URLEncoder.encode(jsonString, HTTP.UTF_8), null);
            System.out.println("删除成员结果：" + jsonData);

        } catch (UnsupportedEncodingException e) {

            e.printStackTrace();
        }
        return jsonData;
    }

    //创建部门
    public static String createDept(String bcode, String bname, String sname){
        String url  = SERVER_URL + "/httpservices/addrbook-addBranchsToCorp.action";
        ImRestUtils imRestUtils = new ImRestUtils();
        Map<String, Object> dept = new HashMap<String, Object>();
        dept.put("corpId", "1");		//企业编号
        dept.put("corpCode", "c4124b2c-4ce3-4248-9d66-f1cf6b7fb491");	//企业代码

        dept.put("bcode", bcode);		//部门代码
        dept.put("bname", bname);     	//部门名称
        dept.put("pcode", "A002");	//父部门代码
        dept.put("sname", sname);          	//简称

        Map<String, Object> sendDatas = new HashMap<String, Object>();
        String jsonString = JSONObject.fromObject(dept).toString();
        sendDatas.put("jsonString", jsonString);

        String jsonData = null;
        try {
            jsonData = imRestUtils.postData(url + "?jsonString=" + URLEncoder.encode(jsonString, HTTP.UTF_8), null);
            System.out.println("创建部门结果：" + jsonData);
            //编号：1071
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return jsonData;
    }

    //移动部门
    public static String moveDept(){
        String url  = SERVER_URL + "/httpservices/addrbook-regulationBranchs.action";
        ImRestUtils imRestUtils = new ImRestUtils();
        Map<String, Object> dept = new HashMap<String, Object>();
        dept.put("corpId", "1");		//企业编号
        dept.put("bcode", "A001");		//部门代码
        dept.put("pcode", "A002");	//父部门代码

        Map<String, Object> sendDatas = new HashMap<String, Object>();
        String jsonString = JSONObject.fromObject(dept).toString();
        sendDatas.put("jsonString", jsonString);

        String jsonData = null;
        try {
            jsonData = imRestUtils.postData(url + "?jsonString=" + URLEncoder.encode(jsonString, HTTP.UTF_8), null);
            System.out.println("移动部门结果：" + jsonData);
            //编号：1071
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return jsonData;
    }


    //添加人员到部门
    public static String addUserToDept(List<String> loginNames){
        String url  = SERVER_URL + "/httpservices/addrbook-joinToBranchs.action";
        ImRestUtils imRestUtils = new ImRestUtils();
        Map<String, Object> dept = new HashMap<String, Object>();
        dept.put("corpId", "1");		//企业编号
        dept.put("corpCode", "c4124b2c-4ce3-4248-9d66-f1cf6b7fb491");	//企业代码

        dept.put("bcode", "A001");		//部门代码
        dept.put("loginName", loginNames);

        Map<String, Object> sendDatas = new HashMap<String, Object>();
        String jsonString = JSONObject.fromObject(dept).toString();
        sendDatas.put("jsonString", jsonString);

        String jsonData = null;
        try {
            jsonData = imRestUtils.postData(url + "?jsonString=" + URLEncoder.encode(jsonString, HTTP.UTF_8), null);
            System.out.println("添加人员到部门结果：" + jsonData);
            //编号：1071
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return jsonData;
    }

    //部门排序
    public static String sortDept(){
        String url  = SERVER_URL + "/httpservices/addrbook-branchSort.action";
        ImRestUtils imRestUtils = new ImRestUtils();

        List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();
        for(int i=0; i< 5; i++){
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("branchCode", "A10" + i);
            data.put("position", 1000*(6-i));
            datas.add(data);
        }
        Map<String, Object> sendDatas = new HashMap<String, Object>();
        String jsonString = JSONArray.fromObject(datas).toString();
        sendDatas.put("jsonString", jsonString);

        String jsonData = null;
        try {
            jsonData = imRestUtils.postData(url + "?jsonString=" + URLEncoder.encode(jsonString, HTTP.UTF_8), null);
            System.out.println("部门排序结果：" + jsonData);
            //编号：1071
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return jsonData;
    }

    //部门人员变更
    public static String updateDeptUsers(){
        String url  = SERVER_URL + "/httpservices/addrbook-regulationCorpUserFromBranch.action";
        ImRestUtils imRestUtils = new ImRestUtils();
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("flag", 0);
        data.put("corpId", 1);
        data.put("bcode", "A100");

        List<String> datas = new ArrayList<String>();
        for(int i=1; i< 5; i++){

            datas.add("imocs" + i);
        }
        data.put("loginName", datas);
        Map<String, Object> sendDatas = new HashMap<String, Object>();
        String jsonString = JSONObject.fromObject(data).toString();
        sendDatas.put("jsonString", jsonString);

        String jsonData = null;
        try {
            jsonData = imRestUtils.postData(url + "?jsonString=" + URLEncoder.encode(jsonString, HTTP.UTF_8), null);
            System.out.println("部门人员变更结果：" + jsonData);
            //编号：1071
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return jsonData;
    }

    public static String sortUsers(){
        String url  = SERVER_URL + "/httpservices/addrbook-corpUserSort.action";
        ImRestUtils imRestUtils = new ImRestUtils();

        List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();
        for(int i=1; i< 5; i++){
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("loginName", "imocs" + i);
            data.put("position", 1000*(5-i));
            datas.add(data);
        }
        Map<String, Object> sendDatas = new HashMap<String, Object>();
        String jsonString = JSONArray.fromObject(datas).toString();
        sendDatas.put("jsonString", jsonString);

        String jsonData = null;
        try {
            jsonData = imRestUtils.postData(url + "?jsonString=" + URLEncoder.encode(jsonString, HTTP.UTF_8), null);
            System.out.println("人员排序结果：" + jsonData);
            //编号：1071
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return jsonData;
    }

    public static void main(String[] args) {
		/*List<String> loginNames = new ArrayList<String>();
		for(int i=1; i<5; i++){
			registUser("imocs" + i);
			loginNames.add("imocs" + i);
		}
		addUserToDept(loginNames);*/

//		modifyUserPwd();

//		updateDeptUsers();

//		getUser("wuy");

//		updateUser();
//		getUser("b-y");

//		deleteUser("b-y");

//		createDept();
		/*for(int i=0; i < 5; i++){
			createDept("A10"+i, "测试子部门"+i, "子部门"+i);
		}*/

//		moveDept();

//		addUserToDept();

//		sortDept();

        sortUsers();
    }
}
