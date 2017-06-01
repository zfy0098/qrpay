package com.example.demo.db;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;


public class TxDB extends DBBase {

	public static int tx(final String loginID,final String ammount,final String termSerno,final String txType){

		Object ret;
		if("0".equals(txType)){
			ret = jdbcTemplate.execute(
					new CallableStatementCreator() {
						public CallableStatement createCallableStatement(Connection con) throws SQLException {
							String storedProc = "{call tixian (?,?,?,?,?)}";// 调用的sql
							CallableStatement cs = con.prepareCall(storedProc);
							cs.setString(1, loginID);// 设置输入参数的值
							cs.setString(2, ammount);
							cs.setString(3, termSerno);
							cs.setString(4, txType);
							cs.setInt(5, Types.INTEGER);
							return cs;
						}
					}, new CallableStatementCallback() {
						public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
							cs.execute();
							return cs.getInt(5);// 获取输出参数的值
						}
					});
		}else{
			ret = jdbcTemplate.execute(
					new CallableStatementCreator() {
						public CallableStatement createCallableStatement(Connection con) throws SQLException {
							String storedProc = "{call CARD_TX (?,?,?,?)}";// 调用的sql
							CallableStatement cs = con.prepareCall(storedProc);
							cs.setString(1, loginID);// 设置输入参数的值
							cs.setString(2, ammount);
							cs.setString(3, termSerno);
							cs.setString(4, txType);
							return cs;
						}
					}, new CallableStatementCallback() {
						public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
							cs.execute();
							return cs.getInt("ret");// 获取输出参数的值
						}
					});
		}
		return Integer.parseInt(ret.toString());
	}
	
	/**
	 * 提现记录
	 * @return
	 */
	public static List<Map<String, Object>> getTxRecordList(String userId,String txType){
		String sql="select ApplyDate,ApplyMoney,TermSerno,BalanceFlag from tab_withdraw s where  ApplyUserID=? and TxType=?";
		List<Map<String,Object>> list = jdbcTemplate.queryForList(sql, new Object[]{userId,txType});
		return list;
	}
}
