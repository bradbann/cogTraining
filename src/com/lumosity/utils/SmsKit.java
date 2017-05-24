package com.lumosity.utils;

import com.aliyun.mns.client.CloudAccount;
import com.aliyun.mns.client.CloudTopic;
import com.aliyun.mns.client.MNSClient;
import com.aliyun.mns.common.ServiceException;
import com.aliyun.mns.model.BatchSmsAttributes;
import com.aliyun.mns.model.MessageAttributes;
import com.aliyun.mns.model.RawTopicMessage;
import com.aliyun.mns.model.TopicMessage;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;

/**
 * 发送短信工具类
 * @author Scott
 *
 */
public class SmsKit {
	/**
	 * 发送短信
	 * @param phone 手机号码
	 * @param code 验证码
	 * @return
	 */
	public static boolean send(String phone, String code) {
		
		Prop smsProp = PropKit.use("sms.properties");
		String accessId = smsProp.get("accessKeyId"); 
	    String accessKey = smsProp.get("accessKeySecret"); 
	    String accountEndpoint = smsProp.get("accountEndpoint");  
	    String topicName = smsProp.get("topicName");//主题名称
	    String templateCode = smsProp.get("templateCode");//短信模板code  
	    String signName = smsProp.get("signName");//短信签名名称
	    
	    boolean flag = true;
		/** 
         * Step 1. 获取主题引用 
         */  
		CloudAccount account = new CloudAccount(accessId, accessKey, accountEndpoint);
		MNSClient client = account.getMNSClient();
		CloudTopic topic = client.getTopicRef(topicName);
		/** 
         * Step 2. 设置SMS消息体（必须） 
         */
		RawTopicMessage msg = new RawTopicMessage();  
        msg.setMessageBody("sms-message");
        /** 
         * Step 3. 生成SMS消息属性 
         */  
        MessageAttributes messageAttributes = new MessageAttributes();  
        BatchSmsAttributes batchSmsAttributes = new BatchSmsAttributes();  
        // 3.1 设置发送短信的签名（SMSSignName）  
        batchSmsAttributes.setFreeSignName(signName);  
        // 3.2 设置发送短信使用的模板（SMSTempateCode）  
        batchSmsAttributes.setTemplateCode(templateCode);  
        // 3.3 设置发送短信所使用的模板中参数对应的值（在短信模板中定义的，没有可以不用设置）  
        BatchSmsAttributes.SmsReceiverParams smsReceiverParams = new BatchSmsAttributes.SmsReceiverParams();  
        smsReceiverParams.setParam("code", code);  
        // 3.4 增加接收短信的号码  
        batchSmsAttributes.addSmsReceiver(phone, smsReceiverParams);  
        messageAttributes.setBatchSmsAttributes(batchSmsAttributes);  
        try {  
            /** 
             * Step 4. 发布SMS消息 
             */  
            TopicMessage ret = topic.publishMessage(msg, messageAttributes);  
            System.out.println("MessageId: " + ret.getMessageId());  
            System.out.println("MessageMD5: " + ret.getMessageBodyMD5());  
        } catch (ServiceException se) {  
            System.out.println(se.getErrorCode() + se.getRequestId());  
            System.out.println(se.getMessage());  
            se.printStackTrace();  
            flag = false;
        } catch (Exception e) {  
            e.printStackTrace();  
            flag = false;
        }  
        client.close();  
		
		return flag;
	}
}
