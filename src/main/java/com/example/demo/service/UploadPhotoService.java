package com.example.demo.service;

import java.io.File;
import java.io.IOException;

import com.example.demo.constant.Constant;
import com.example.demo.constant.RespCode;
import com.example.demo.db.LoginUserDB;
import com.example.demo.mode.RequestData;
import com.example.demo.mode.ResponseData;
import com.example.demo.mode.TabLoginuser;
import com.example.demo.util.*;

/**
 *    上传用户照片
 * @author a
 *
 */
public class UploadPhotoService {

	
	LoggerTool logger = new LoggerTool(this.getClass());
	
	public void UploadPhoto(TabLoginuser user, RequestData reqdata, ResponseData respdata){
		
		
		logger.info(user.getLoginID() + "上传图片信息");
		
		String imgPath = LoadPro.loadProperties("config", "imgpath");
		String imgUrl = LoadPro.loadProperties("config", "imgurl");
		
		/** 手持身份证照片  **/
		String handheldIDPhoto = reqdata.getHandheldIDPhoto();
		
		/** 身份证正面照片 **/
		String IDCardFrontPhoto = reqdata.getIDCardFrontPhoto();
		
		/** 身份证反面照片 **/
		String IDCardReversePhoto = reqdata.getIDCardReversePhoto();
		
		/** 银行卡照片 **/
		String bankCardPhoto = reqdata.getBankCardPhoto();
		
		/** 营业执照照片**/
		String businessPhoto = reqdata.getBusinessPhoto();
		
		
		String handheldIDurl = null, iDCardFront = null , iDCardReverse = null ,bankCard = null ,business = null;
		
		try {
			
			logger.info(user.getLoginID() + "保存照片信息,保存顺序：手持身份证照片，身份证正面照照片，身份证反面照片，银行卡照片，营业执照照片"); 
			
			String imgName =  UtilsConstant.getUUID();
			String postfix = ".jpg";
			
			if(!new File(imgPath + user.getLoginID() + File.separator).exists()){
				logger.info(user.getLoginID() + "保存图片的文件夹不存在，将创建文件 ，文件夹名称为该用户的手机号");
				new File(imgPath + user.getLoginID() + File.separator).mkdirs();
			}
			
			
			if(!UtilsConstant.strIsEmpty(handheldIDPhoto)){
				Image64Bit.GenerateImage(handheldIDPhoto.replace("\n", "").replace("\t", ""), imgPath + user.getLoginID() + File.separator + imgName + postfix);
				handheldIDurl = imgUrl + user.getLoginID() + File.separator  + imgName + postfix;
				logger.info(user.getLoginID() + "保存手持身份证照片成功");
			}else{
				logger.info(user.getLoginID() + "手持身份证照片为空");
			}
			
			 
			if(!UtilsConstant.strIsEmpty(IDCardFrontPhoto)){
				imgName =  UtilsConstant.getUUID();
				Image64Bit.GenerateImage(IDCardFrontPhoto.replace("\n", "").replace("\t", ""), imgPath + user.getLoginID() + File.separator + imgName + postfix);
				iDCardFront = imgUrl + user.getLoginID() + File.separator  + imgName + postfix;
				
				logger.info(user.getLoginID() + "保存身份证正面照片成功");
			} else {
				logger.info(user.getLoginID() + "身份证正面照片为空");
			}
			
			
			if(!UtilsConstant.strIsEmpty(IDCardReversePhoto)){
				imgName =  UtilsConstant.getUUID();
				Image64Bit.GenerateImage(IDCardReversePhoto.replace("\n", "").replace("\t", ""), imgPath + user.getLoginID() + File.separator + imgName + postfix);
				iDCardReverse = imgUrl + user.getLoginID() + File.separator  + imgName + postfix;
				
				logger.info(user.getLoginID() + "保存身份证反面照片成功");
			}else{
				logger.info(user.getLoginID() + "身份证反面照片为空");
			}
			
			
			if(!UtilsConstant.strIsEmpty(bankCardPhoto)){
				imgName =  UtilsConstant.getUUID();
				Image64Bit.GenerateImage(bankCardPhoto.replace("\n", "").replace("\t", ""), imgPath + user.getLoginID() + File.separator + imgName + postfix);
				bankCard = imgUrl + user.getLoginID() + File.separator  + imgName + postfix;
				
				logger.info(user.getLoginID() + "保存银行卡照片成功");
			}else{
				logger.info(user.getLoginID() + "银行卡照片为空");
			}
			
			
			if(!UtilsConstant.strIsEmpty(businessPhoto)){
				imgName =  UtilsConstant.getUUID();
				Image64Bit.GenerateImage(businessPhoto.replace("\n", "").replace("\t", ""), imgPath + user.getLoginID() + File.separator + imgName + ".jpg");
				business = imgUrl  + user.getLoginID() + File.separator  + imgName + postfix;
				
				logger.info(user.getLoginID() + "保存营业执照照片成功");
			}else{
				logger.info(user.getLoginID() + "营业执照照片为空");
			}
			
		
		} catch (IOException e) {
			logger.error(e.getMessage());
			logger.info(user.getLoginID() + "照片信息保存失败");
			respdata.setRespCode(RespCode.IMGSAVEError[0]);
			respdata.setRespDesc(RespCode.IMGSAVEError[1]);
			return ;
		}
		
		int ret = LoginUserDB.updatePhotoInfo(new Object[]{handheldIDurl , iDCardFront  , iDCardReverse ,bankCard ,business ,  user.getLoginID()});
		
		EhcacheUtil ehcache = EhcacheUtil.getInstance();
		ehcache.remove(Constant.cacheName, user.getLoginID() + "UserInfo");
		
		if(ret > 0){
			logger.info(user.getLoginID() + "上传照片成功");
			respdata.setRespCode(RespCode.SUCCESS[0]);
			respdata.setRespDesc(RespCode.SUCCESS[1]);
		}else{
			logger.info(user.getLoginID() + "上传照片更新数据库失败");
			respdata.setRespCode(RespCode.ServerDBError[0]);
			respdata.setRespDesc(RespCode.ServerDBError[1]);
		}
	}
}
