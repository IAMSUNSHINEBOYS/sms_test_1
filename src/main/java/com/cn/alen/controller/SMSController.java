package com.cn.alen.controller;

import com.cn.alen.util.LogUtils;
import com.cn.alen.util.SendUtils;
import net.sf.json.JSONObject;
import org.apache.http.protocol.HTTP;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/sms")
public class SMSController {

    /**
     * 此处测试获取发送渠道接口
     */
    @RequestMapping("/receiver")
    @ResponseBody
    public String receiver(String appId,String appKey){
        LogUtils.logInfo("receiver:appId="+appId+" appKey="+appKey);
        String result = SendUtils.get_sendChannels(appId,appKey);
        LogUtils.logInfo("此处测试获取发送渠道接口"+result);
        return result;
    }

    /**
     * 此处开始测试发送消息接口
     */
    @RequestMapping("/sendmsg")
    @ResponseBody
    public String sendmsg(String appId,String appKey,String title,String content,String channels,String channelIds,String [] mobile){
        LogUtils.logInfo("receiver:appId="+appId+" appKey="+appKey);
        SendUtils client = new SendUtils();
        String url = SendUtils.SERVER_URL + "restful/sendmsg";   //发送消息的访问接口
        Map<String, Object> msgBody = new HashMap<String, Object>();
        msgBody.put("title", title);
        msgBody.put("isRich", "0");    //消息内容类型  0：纯文本  1：富文本，注意短信必须用0
        msgBody.put("content", content);     //该字段在代表消息内容
        msgBody.put("channels", channels);   //注：多个渠道，中间用英文逗号隔开; ydxy-app 移动消息; im 即时通消息; email 邮箱; ydxy-corp 微信企业号; ydxy-mobile 短信
        msgBody.put("channelIds", channelIds);         //短信-首易渠道
        msgBody.put("isCron", "0");            //1 定时发送; 0 及时发送;
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
        extReceiver.put("ydxy-mobile", mobile);
        //如需其他渠道，例如邮箱，则取消注释下一行代码
//		extReceiver.put("email", new String[]{"wu_yya@sina.cn", "123456@qq.com"}); //其中wu_yya@sina.cn、123456@qq.com为emc用户体系外接收邮箱账号

        msgBody.put("extReceiver", extReceiver);

        Map<String, Object> sendMap = new HashMap<String, Object>();
        sendMap.put("accountID", appId);
        sendMap.put("accountKey", appKey);
        sendMap.put("msgJson", msgBody);
        String postData = JSONObject.fromObject(msgBody).toString();
        String jsonResult = null;
        try {
            String encoderJson = URLEncoder.encode(postData, HTTP.UTF_8);
            url = url + "?accountID=" + appId + "&accountKey=" + appKey + "&msgJson=" + encoderJson;


            jsonResult = client.sendToEmc(url);
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
        LogUtils.logInfo("此处开始测试发送消息接口:"+jsonResult);
        return jsonResult;
    }
}
