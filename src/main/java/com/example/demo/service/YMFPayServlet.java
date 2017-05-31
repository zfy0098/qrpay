package com.example.demo.service;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.example.demo.db.LoginUserDB;
import com.example.demo.db.YMFTradeDB;
import com.example.demo.mode.TabLoginuser;
import com.example.demo.util.LoggerTool;

import com.example.demo.util.UtilsConstant;
import net.sf.json.JSONObject;


/**
 * Servlet implementation class GDMTradeServlet
 */
@WebServlet("/YMFPayServlet")
public class YMFPayServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	
	LoggerTool logger = new LoggerTool(this.getClass());
	
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public YMFPayServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
		
		request.setCharacterEncoding("utf-8");
		resp.setCharacterEncoding("utf-8");
		
		
		/** 固定码 码数据 **/
		String qrCode = request.getParameter("qrCode");
		JSONObject json = new JSONObject();
		
		if(!UtilsConstant.strIsEmpty(qrCode)){
			/** 根据码数据查询固定码信息 **/
			Map<String,Object> map = YMFTradeDB.getYMFCode(new Object[]{qrCode});
			if (map != null && !map.isEmpty()) {
				if (map.get("Valid").equals("1")) {
					//可用
					if (map.get("Binded").equals("1")&&!UtilsConstant.strIsEmpty(UtilsConstant.ObjToStr(map.get("UserID")))){
						//已绑定
						try {
							TabLoginuser user = LoginUserDB.getLoginuserInfo(UtilsConstant.ObjToStr(map.get("UserID")));
							if (user != null) {
								request.setAttribute("userID", user.getID());
								request.setAttribute("merName", user.getMerchantName());
								request.setAttribute("ymfCode", qrCode);
								request.getRequestDispatcher("/ymf/wxpay.jsp").forward(request, resp);
							}
						} catch (Exception e) {
							logger.error("固定码交易失败" + e.getMessage());
							//未绑定,跳转到公众号
							json.put("respCode", "01");
							json.put("respDesc", e.getMessage());
							resp.getWriter().write(json.toString());
							resp.getWriter().flush();
						}
					}else{
						//未绑定,跳转到公众号
						json.put("respCode", "01");
						json.put("respDesc", "This code has not binded");
						resp.getWriter().write(json.toString());
						resp.getWriter().flush();
					}
				}else{
					//不可用
					json.put("respCode", "01");
					json.put("respDesc", "This code is invalid");
					resp.getWriter().write(json.toString());
					resp.getWriter().flush();
				}
			}else{
				/** 码数据查询失败 **/
				logger.info("码数据查询失败" + qrCode);
 				json.put("respCode", "01");
				json.put("respDesc", "This code is invalid");
				resp.getWriter().write(json.toString());
				resp.getWriter().flush();
			}
		}else{
			
			logger.info("请求固定码code为空");
			
			/** 固定码code为空 **/
			json.put("respCode", "01");
			json.put("respDesc", "QRCode is null");
			resp.getWriter().write(json.toString());
			resp.getWriter().flush();
		}
	}
}
