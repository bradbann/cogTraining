package com.lumosity.order;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.jfinal.core.Controller;
import com.lumosity.model.Account;

public class OrderController extends Controller {

	public void index() {
		
		Account account = getSessionAttr("userInfo");
		
		int expireTime = getParaToInt("time");
		if (expireTime != 0) {
			
		}
		//付款金额WIDtotal_fee
		String price = getPara("price");
		//生成商户订单号:WIDout_trade_no
		String trade_no = (new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())) + account.getUserId();
		//生成商品名称WIDsubject
		String subject = "认知训练产品";
		//设置用户订单数据库
		
		StringBuilder orderHtml = new StringBuilder();
		
		render("/order.html");
	}
}
