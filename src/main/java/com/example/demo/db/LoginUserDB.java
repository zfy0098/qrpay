package com.example.demo.db;

import com.example.demo.mode.TabLoginuser;
import com.example.demo.util.UtilsConstant;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;
import java.util.Map;

public class LoginUserDB extends DBBase{
	
	
	
	
	/**
	 *   用户注册
	 * @param obj
	 * @return
	 */
	public static int registerUser(Object[] obj){
		String sql = "insert into tab_loginuser(ID,LoginID,LoginPwd,ThreeLevel, TwoLevel , OneLevel, AgentID , RegisterTime) values (?,?,?,?,?,?,?,?)";
		return  jdbcTemplate.update(sql, obj);
	}
	
	
	/**
	 *    根据id用户用户信息
	 * @param ID
	 * @return
	 * @throws Exception
	 */
	public static TabLoginuser getLoginuserInfo(String ID) throws Exception{
		String sql = "select * from tab_loginuser where ID=?";
		Map<String,Object> map = jdbcTemplate.queryForMap(sql, new Object[]{ID});
		return UtilsConstant.mapToBean(map, TabLoginuser.class);
	}
	
	
	/**
	 *   查询登陆的手机号是否存在
	 * @param loginuser
	 * @return
	 */
	public static boolean getLoginUserInfo(TabLoginuser loginuser){
		String sql = "select * from tab_loginuser where LoginID=?";
		Map<String,Object> map = jdbcTemplate.queryForMap(sql, new Object[]{loginuser.getLoginID()});
		if(map!=null&&!map.isEmpty()){
			return true;
		}
		return false;
	}
	
	/**
	 *   查询用户信息
	 * @param userID
	 * @return
	 */
	@Cacheable(key = "T(String).valueof(#userID).concat('info')")
	public static TabLoginuser loginuser(String userID){
		String sql = "select * from tab_loginuser where LoginID=?";
		Map<String,Object> map = jdbcTemplate.queryForMap(sql, new Object[]{userID});
		
		if(map==null||map.isEmpty()){
			return null;
		}
		TabLoginuser loginUser = null;
		try {
			loginUser = UtilsConstant.mapToBean(map, TabLoginuser.class);
			return loginUser;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return loginUser;
	}
	
	
	/**
	 *   查询用户结算信息
	 * @param userID
	 * @return
	 */
	public static Map<String,Object> getUserBankCard(String userID){
		String sql = "select * from tab_pay_userbankcard where UserID= ? ";
		return jdbcTemplate.queryForMap(sql, new Object[]{userID});
	}
	
	/**
	 *   为新注册用户分配秘钥
	 * @param UserID
	 * @return
	 */
	public static int allocationTermk(String UserID){
		
		String sql="select ifnull(ID,'') as ID, ifnull(TermTMK,'') as TermTMK ,ifnull(TMK,'') as TMK from tab_key where EnAble=1 limit 0,1";
		
		Map<String,Object> map = jdbcTemplate.queryForMap(sql);
		
		String ID = map.get("ID").toString();
		String termTmkKey = map.get("TermTMK").toString();
		String tmk = map.get("TMK").toString();
		
		sql="insert into tab_termkey(ID,UserID,TmkKey,TermTmkKey) values(?,?,?,?)";
		jdbcTemplate.update(sql,new Object[]{UtilsConstant.getUUID(), UserID,tmk,termTmkKey});
		
		sql="update tab_key set EnAble=0 where ID=?";
		jdbcTemplate.update(sql , new Object[]{ID});
		return 1;
	}
	
	
	/**
	 *   查询用户的termkey
	 * @param userID
	 * @return
	 */
	public static Map<String,Object> selectTermKey(String userID){
		String sql = "select * from tab_termkey  where UserID=?";
		return jdbcTemplate.queryForMap(sql, new Object[]{userID});
	}
	
	
	public static int updateKey(String userID,String macKey){
		String sql="update tab_termkey set MacKey=? where UserID=? ";
		return jdbcTemplate.update(sql, new Object[]{macKey , userID});
	}
	
	
	/**
	 *   更新对应商户  用户信息报中的分润总额
	 * @param list
	 * @return
	 */
	public static int[] merchantProfit(List<Object[]> list){
		String sql = "update tab_loginuser set FeeAmount=FeeAmount+?  ,  FeeBalance=FeeBalance+? where ID=?";
		return jdbcTemplate.batchUpdate(sql,list);
	}
	
	
	/** 
	 *   完善用户信息
	 * @param obj
	 * @return
	 */
	public static int updateUserInfo(Object[] obj){
		String sql = "update tab_loginuser set  Name=?, IDCardNo=? , BankCardNo=? ,BankName=? ,BankSubbranch=? ,MerchantName=? ,State=? ,Address=? ,Email=? , "
				+ " BankInfoStatus=2  where LoginID=?";
		return jdbcTemplate.update(sql, obj);
	}
	
	public static int h5updateUserInfo(Object[] obj){
		String sql = "update tab_loginuser set MerchantTypeValue=?, Name=?, IDCardNo=? ,MerchantName=? ,State=? , City=?,Region=? ,BusinessLicense=? ,Address=? ,Email=? , "
				+ " MerchantBillName = ? , merchantPersonName= ? , BankInfoStatus=2  where LoginID=?";
		return jdbcTemplate.update(sql, obj);
	}
	
	
	/**
	 *   上游报件成功以后，将状态修该成通过审核
	 * @param obj
	 * @return
	 */
	public static int updateUserBankStatus(Object[] obj){
		String sql = "update tab_loginuser set BankInfoStatus = ? , PhotoStatus = ?  where LoginID=?";
		return jdbcTemplate.update(sql, obj);
	}
	
	/**
	 *   保存结算卡信息
	 * @param obj
	 * @return
	 */
	public static int saveOrUpBankInfo(Object[] obj){
		String sql = "insert into tab_pay_userbankcard (ID,UserID,AccountName,AccountNo,BankBranch,BankProv,BankCity,BankCode,BankName,SettleCreditCard,SettleBankType)"
				+ " value(?,?,?,?,?,?,?,?,?,?,?)"
				+ "on duplicate key update AccountName=?,AccountNo=?,BankBranch=?,BankProv=?,BankCity=?,BankCode=?,BankName=?,SettleCreditCard=?,SettleBankType=?";
		return jdbcTemplate.update(sql, obj);
	}
	
	/**
	 *   查询商户类型
	 * @return
	 */
	public static List<Map<String,Object>> merchantTypeList(){
		String sql = "select * from tab_pay_merchanttype";
		return jdbcTemplate.queryForList(sql);
	}
	
	/**
	 *   更新照片信息
	 * @param obj
	 * @return
	 */
	public static int updatePhotoInfo(Object[] obj){
		String sql = "update tab_loginuser set HandheldIDPhoto=? , IDCardFrontPhoto=?,IDCardReversePhoto=? , BankCardPhoto=? , BusinessPhoto=? , "
				+ " PhotoStatus=2 where LoginID=?";
		return jdbcTemplate.update(sql, obj);
	}
	
	
	/**
	 *   更新用户登录信息
	 * @param obj
	 * @return
	 */
	public static int updateUserLoginInfo(Object[] obj){
		String sql = "update tab_loginuser set LastLoginTime=? , LoginPSN=? where LoginID=?";
		return jdbcTemplate.update(sql, obj);
	}
	
	/**
	 *   修改密码
	 * @param obj
	 * @return
	 */
	public static int updatePassword(Object[] obj){
		String sql = "update tab_loginuser set LoginPwd=? where LoginID=?";
		return jdbcTemplate.update(sql, obj);
	}
	

	
	public static String getMyMerchant(String userID) {
		String sql="SELECT LoginID,ifnull(Name , '') as Name ,MerchantLeve,RegisterTime,BankInfoStatus,PhotoStatus,ThreeLevel,TwoLevel,OneLevel "
				+ " from tab_loginuser where (ThreeLevel =? or TwoLevel=? or OneLevel=?)";

		List<Map<String,Object>> list = jdbcTemplate.queryForList(sql, userID,userID,userID);
		JSONArray jsonArray=new JSONArray();
		
		for (Map<String,Object> map : list) {
			JSONObject json = new JSONObject();
			json.put("userId", map.get("LoginID"));
			json.put("name", map.get("Name"));
			json.put("registerTime", map.get("RegisterTime"));
			json.put("accountStatus", map.get("BankInfoStatus"));
			json.put("photoStatus", map.get("PhotoStatus"));
			json.put("level", map.get("MerchantLeve"));
			if(map.get("ThreeLevel")!=null && map.get("ThreeLevel").equals(userID)){
				json.put("topLevel", "1");
			}
			if(map.get("TwoLevel")!=null && map.get("TwoLevel").equals(userID)){
				json.put("topLevel", "2");
			}
			if(map.get("OneLevel")!=null && map.get("OneLevel").equals(userID)){
				json.put("topLevel", "3");
			}
			jsonArray.add(json);
		}
		return jsonArray.toString();
	}
	
	
	
	/** 
	 *		保存更新设备信息 
	 * @param obj
	 * @return
	 */
	public static int saveOrUpToken(Object[] obj){
		String sql = "INSERT INTO tab_devicetoken (UserID , DeviceToken , DeviceType ) VALUES (?,?,?)  ON DUPLICATE KEY UPDATE DeviceToken=? , DeviceType=?";
		return jdbcTemplate.update(sql, obj);
	}
	
	public static int delkToken(Object[] obj){
		String sql = "delete from tab_devicetoken where UserID=?";
		return jdbcTemplate.update(sql, obj);
	}
	
}
