package com.lumosity.model;

import java.util.List;

import com.lumosity.model.base.BaseAccount;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class Account extends BaseAccount<Account> {
	public static final Account dao = new Account();
	
	/**根据邮箱密码查找用户**/
	public Account findByEmailAndPwd(String email, String password){
		return findFirst("select * from account where email=? and password=?", email, password);
	}
	
	/**根据邮箱查找用户**/
	public Account findByEmail(String email){
		return findFirst("select userId from account where email = ?", email);
	}
	
	/**根据手机号查找用户**/
	public Account findByPhone(String phone){
		return findFirst("select userId from account where mobileId = ?", phone);
	}
	
	/**根据邮箱或者手机号查找用户**/
	public Account isAccountExist(String accountInfo){
		return findFirst("select userId from account where email = ? or mobileId = ?", accountInfo, accountInfo);
	}
	
	/**根据手机密码查找用户**/
	public Account findByPhoneAndPwd(String phone, String password){
		return findFirst("select * from account where mobileId = ? and password = ?", phone, password);
	}
	
	/**查找所有用户**/
	public List<Account> findAll(){
		return find("select userId from account");
	}
	
	/**查找所有用户**/
	public Account findAccount(String accountInfo, String pwd){
		return findFirst("select * from account where (email=? or mobileId=?) and password=?", accountInfo ,accountInfo , pwd);
	}
	
	
	/*
	public List<Account> findByBirthday(int begin, int end){
		return find("select * from account where datediff(year,birthday,getdate()) "
									+ "between "+begin+" and "+end);
	}
	 */
}
