package com.example.demo.controller;

import com.example.demo.constant.Constant;
import com.example.demo.constant.RespCode;
import com.example.demo.db.LoginUserDB;
import com.example.demo.mode.RequestData;
import com.example.demo.mode.ResponseData;
import com.example.demo.mode.TabLoginuser;
import com.example.demo.util.DESUtil;
import com.example.demo.util.EhcacheUtil;
import com.example.demo.util.LoadPro;
import com.example.demo.util.UtilsConstant;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by a on 2017/5/26.
 */
@Controller
@ResponseBody
@RequestMapping("requestentry")
public class RequestEntryController {

    Logger log  =  Logger.getLogger(this.getClass());

    @RequestMapping(value = "", method = {RequestMethod.GET , RequestMethod.POST})
    public Object requestentry(HttpServletRequest request , HttpServletResponse response){

        log.info("终端用户发起请求");

        ResponseData respData = new ResponseData();

        String reqContent = request.getParameter("data");

        String loginID = null;

        try {
            if(UtilsConstant.strIsEmpty(reqContent)){
                log.info("终端请求报文为空,停止交易");
                respData.setRespCode(RespCode.ParamsError[0]);
                respData.setRespDesc(RespCode.ParamsError[1]);
                return paraFilterReturn(respData);
            }
            log.info("请求报文：" + reqContent.replace("\n", "").replace(" ", ""));
            Map<String,Object> map = UtilsConstant.jsonToMap(JSONObject.fromObject(reqContent));
            RequestData requestData = UtilsConstant.mapToBean(map, RequestData.class);
            TabLoginuser loginuser = null;
            // 获取登录用户的手机号
            loginID = requestData.getLoginID();
            /** 发送时间  **/
            if(UtilsConstant.strIsEmpty(requestData.getSendTime())){
                log.info("发送时间sendTime为空");
                respData.setRespCode(RespCode.ParamsError[0]);
                respData.setRespDesc(RespCode.ParamsError[1]);
                return paraFilterReturn(respData);
            }

            respData.setSendSeqID(requestData.getSendTime());

            /** 请求交易类型 **/
            String Txndir = requestData.getTxndir();
            if(UtilsConstant.strIsEmpty(Txndir)){
                log.info("交易类型txndir为空");
                respData.setRespCode(RespCode.TxndirError[0]);
                respData.setRespDesc(RespCode.TxndirError[1]);
                return respData;
            }
            String trade = LoadPro.loadProperties("trade", Txndir);
            if(UtilsConstant.strIsEmpty(trade)){
                log.info("交易类型：" + Txndir + ", 系统为配置该交易类型");
                respData.setRespCode(RespCode.TxndirError[0]);
                respData.setRespDesc(RespCode.TxndirError[1]);
                return paraFilterReturn(respData);
            }
            respData.setTxndir(Txndir);

            /** 终端流水号 **/
            String sendSeqID = requestData.getSendSeqId();
            if(UtilsConstant.strIsEmpty(sendSeqID)){
                log.info("终端流水号sendSeqId为空");
                respData.setRespCode(RespCode.ParamsError[0]);
                respData.setRespDesc(RespCode.ParamsError[1]);
                return paraFilterReturn(respData);
            }
            respData.setSendSeqID(sendSeqID);

            /**  终端登录信息 **/
            String loginPSN = requestData.getTerminalInfo();
            if(UtilsConstant.strIsEmpty(loginPSN)){
                log.info("登录信息(PSN)terminalInfo为空");
                respData.setRespCode(RespCode.ParamsError[0]);
                respData.setRespDesc(RespCode.ParamsError[1]);
                return  paraFilterReturn(respData);
            }
            respData.setTerminalInfo(loginPSN);

            String className = trade.split(",")[0];
            String funName = trade.split(",")[1];
            String isNeedLogin = trade.split(",")[2];
            String isNeedMac = trade.split(",")[3];

            log.info("获取trade配置信息-------trade：" + trade);
            log.info("获取trade配置信息-------className：" + className);
            log.info("获取trade配置信息-------funName：" + funName);
            log.info("获取trade配置信息-------isNeedLogin：" + isNeedLogin);

            //  需要登录信息
            if(isNeedLogin.equals("1")){

                log.info("需要登录信息，登录的手机号为" + loginID);

                EhcacheUtil ehcache = EhcacheUtil.getInstance();

                log.info("查询数据库");
                loginuser = LoginUserDB.loginuser(loginID);
                if(loginuser == null){
                    log.info("未查到用户 " + loginID +"信息");
                    respData.setRespCode(RespCode.userDoesNotExist[0]);
                    respData.setRespDesc(RespCode.userDoesNotExist[1]);
                    return  paraFilterReturn(respData);
                }


                if(!Txndir.equals("A006")&&!"A005".equals(Txndir)){
                    if(!requestData.getTerminalInfo().equals(loginuser.getLoginPSN())){
                        log.info(loginID + "被其他设备登录 , 终端上传: " + requestData.getTerminalInfo() + ",数据库保存" +loginuser.getLoginPSN() );
                        respData.setRespCode(RespCode.LOGINError[0]);
                        respData.setRespDesc(RespCode.LOGINError[1]);
                        return  paraFilterReturn(respData);
                    }
                }

                if (isNeedMac.equals("1")) {
                    // 计算mac
                    String mac = makeMac(JSONObject.fromObject(reqContent), loginuser);
                    if (!mac.equals(requestData.getMac())) {
                        log.info("验证mac失败，终端上送mac=[" + requestData.getMac() + "],平台计算mac=" + mac);
                        respData.setRespCode(RespCode.SIGNMACError[0]);
                        respData.setRespDesc(RespCode.SIGNMACError[1]);
                        return respData;
                    }else{
                        log.info("mac校验通过 == " + mac);
                    }
                }

                //获取 函数入口
                Class<?> cls= Class.forName("com.example.demo.service." + className);
                Method m = cls.getDeclaredMethod(funName,new Class[]{ TabLoginuser.class , RequestData.class , ResponseData.class});

                m.invoke(cls.newInstance(), loginuser , requestData , respData);
            }else{
                //获取 函数入口
                Class<?> cls= Class.forName("com.example.demo.service." + className);
                Method m = cls.getDeclaredMethod(funName,new Class[]{RequestData.class,ResponseData.class});

                m.invoke(cls.newInstance(), requestData,respData);
            }
            /** 如果请求需要校验mac 那么响应报文中也需要添加mac字段. 加密数据为返回的报文 **/
            if(isNeedMac.equals("1")){
                String mac = makeMac(JSONObject.fromObject(respData), loginuser);
                log.info("响应报文中的mac" + mac);
                respData.setMac(mac);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Object obj =  paraFilterReturn(respData);
        log.info("响应报文：" + loginID  + " ======== "  + respData.getTxndir()  + "---" + obj.toString());
        return obj;
    }

    public Object paraFilterReturn(Object obj){
        Map<String,Object> map =  UtilsConstant.jsonToMap(JSONObject.fromObject(obj));
        Map<String,Object> map2 = new HashMap<String,Object>();
        if(map == null || map.size() < 0 ){
            return "";
        }
        for(String key : map.keySet()){
            String value = map.get(key) + "";
            if ((value == null || value.equals(""))) {
                continue;
            }
            map2.put(key + "", value);
        }
        return JSONObject.fromObject(map2);
    }


    /**
     *   计算mac
     * @param json
     * @param user
     * @return
     */
    public String makeMac(JSONObject json,TabLoginuser user){

        Map<String, Object> contentData = UtilsConstant.jsonToMap(json);

        String macStr = "";
        Object[] key_arr = contentData.keySet().toArray();
        Arrays.sort(key_arr);
        for (Object key : key_arr) {
            Object value = contentData.get(key);
            if (value != null&&!UtilsConstant.strIsEmpty(value.toString())){
                if (!key.equals("mac")) {
                    macStr += value.toString();
                }
            }
        }
        log.info("计算mac原文:" + macStr);
        Map<String, Object> termKey = LoginUserDB.selectTermKey(user.getID());
        String initKey = LoadPro.loadProperties("config", "DBINDEX");
        String rMac = DESUtil.mac(macStr, UtilsConstant.ObjToStr(termKey.get("MacKey")), initKey);
        return rMac;
    }
}
