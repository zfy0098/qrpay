package com.example.demo.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

import com.sun.jersey.core.util.Base64;

public class Image64Bit {

	/**
	 * // 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
	 * 
	 * @param imgFilePath
	 * @return
	 */
	public static String GetImageStr(String imgFilePath) {
		byte[] data = null;

		// 读取图片字节数组
		try {
			InputStream in = new FileInputStream(imgFilePath);
			data = new byte[in.available()];
			in.read(data);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 对字节数组Base64编码
		return new String(Base64.encode(data));
	}

	/**
	 * 根据图片base64字符串生成图片
	 * 
	 * @param imgStr
	 * @param imgFilePath
	 * @return
	 * @throws IOException
	 */
	public static boolean GenerateImage(String imgStr, String imgFilePath) throws IOException {// 对字节数组字符串进行Base64解码并生成图片
		if (imgStr == null) // 图像数据为空
			return true;
		// Base64解码
		byte[] bytes = Base64.decode(imgStr);
		for (int i = 0; i < bytes.length; ++i) {
			if (bytes[i] < 0) {// 调整异常数据
				bytes[i] += 256;
			}
		}
		// 生成jpeg图片
		OutputStream out = new FileOutputStream(imgFilePath);
		out.write(bytes);
		out.flush();
		out.close();
		return false;
	}

	/**
	 * 
	 * // 将网络图片文件转化为字节数组字符串，并对其进行Base64编码处理
	 * 
	 * @param imageUrl
	 * @return
	 */
	public static String encodeImgageToBase64(URL imageUrl) {
		ByteArrayOutputStream outputStream = null;
		try {
			BufferedImage bufferedImage = ImageIO.read(imageUrl);
			outputStream = new ByteArrayOutputStream();
			ImageIO.write(bufferedImage, "jpg", outputStream);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 对字节数组Base64编码
		return new String(Base64.encode(outputStream.toByteArray()));
	}

	public static void main(String[] args) throws IOException {
		String string = encodeImgageToBase64(new URL("http://img.bizhi.sogou.com/images/2012/03/14/124196.jpg"));
		String path = "d:/a.jpg";
		GenerateImage(string, path);
	}
}
