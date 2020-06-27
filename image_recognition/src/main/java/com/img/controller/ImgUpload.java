package com.img.controller;

import org.apache.tomcat.util.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import sun.misc.BASE64Encoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64.Encoder;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;





@RestController
@RequestMapping("/uploadImg")
public class ImgUpload {
    private final String imgPath = "C:/foodRecognition/uploads/";
    private String path;
    private String username;

    @RequestMapping(value = "/uploadImgMethod")
    public String convertStringtoImage(HttpServletRequest request) throws JSONException, SQLException {
        String imgEncodedStr = request.getParameter("image");
        String filename = request.getParameter("filename");
        username=request.getParameter("username");
        System.out.println("Filename: " + filename);
        if (imgEncodedStr != null) {
            String fileName=uploadImging(imgEncodedStr, filename);
            System.out.println("Image upload complete, Please check your directory");

            String info=socket_trans(fileName);

            return foodInformation(info);


        } else {
            return  "Image is empty";
        }
        //return foodInformation(info);
    }

    public String uploadImging(String encodedImageStr, String fileName) {

        String fileAllname = imgPath + fileName + ".jpg";
        try {
            // Base64解码图片
            byte[] imageByteArray = Base64.decodeBase64(encodedImageStr);
            FileOutputStream imageOutFile = new FileOutputStream(fileAllname);
            imageOutFile.write(imageByteArray);

            imageOutFile.close();

            System.out.println("Image Successfully Stored");
            //书写 读取上传的图片 进行算法解析（调用算法）
            //return fileName;

        } catch (FileNotFoundException fnfe) {
            System.out.println("Image Path not found" + fnfe);
        } catch (IOException ioe) {
            System.out.println("Exception while converting the Image " + ioe);
        }
        return fileName;
    }

    //图片识别
    public String socket_trans(String fileName) {
        try {
            Socket socket = new Socket("localhost", 8001);

            //获取输出流，向服务器端发送信息
            OutputStream os = socket.getOutputStream();//字节输出流
            PrintWriter pw = new PrintWriter(os);//将输出流包装为打印流
            pw.write(imgPath + fileName+".jpg");
            path=imgPath + fileName+".jpg";
            pw.flush();
            socket.shutdownOutput();//关闭输出流

            InputStream is = socket.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            String info = null;
            String nameID;
            while ((info = in.readLine()) != null) {
                System.out.println("食物名称为：" + info);
//                foodInformation(info);
                System.out.println(info);
                return info;
            }
            is.close();
            in.close();
            socket.close();
            System.out.println(info);
            //return info;
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();

        }
        return "error";
    }


    public String foodInformation(String foodId) throws SQLException {
        System.out.println(foodId);
        Connection con;
        Statement sql;
        PreparedStatement sql2;
        ResultSet res;
        String id;
        String name="";
        String Calories="";
        String Protein="";
        String Fat="";
        String Carbohydrate="";
        String dietary_fiber="";
        String url="jdbc:mysql://localhost:3306/foodrecognition?serverTimezone=UTC";
        //ImgRecognitionApplicationTests c=new ImgRecognitionApplicationTests();
        con= DriverManager.getConnection( url, "root", "123456");
        try {
            sql = con.createStatement();
            Integer it = new Integer(foodId);
            int name_id = it.intValue();
            res = sql.executeQuery("select * from foodrecognition.food where id=" + name_id);
            while (res.next()) {
                id = res.getString("id");
                name = res.getString("name");
                Calories = res.getString("Calories");
                Protein = res.getString("Protein");
                Fat = res.getString("Fat");
                Carbohydrate = res.getString("Carbohydrate");
                dietary_fiber = res.getString("dietary fiber");
                System.out.println("111111111111111111111111111111111111111111111111111111111111111111111");
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        DateFormat format1=new SimpleDateFormat("yyyy-MM-dd");
        Date date1=new Date();
        DateFormat format2=new SimpleDateFormat("HH:mm:ss");
        Date date2=new Date();
        try{
            sql2=con.prepareStatement("insert into foodrecognition.record"+" values(?,?,?,?,?,?,?,?,?,?,?)");
            sql2.setString(1,username);
            sql2.setString(2,format1.format(date1));
            sql2.setString(3,format2.format(date2));
            sql2.setString(4,path);
            sql2.setString(5,name);
            sql2.setString(6,Calories);
            sql2.setString(7,Protein);
            sql2.setString(8,Fat);
            sql2.setString(9,Carbohydrate);
            sql2.setString(10,dietary_fiber);
            sql2.setBoolean(11,false);
            System.out.println(sql2);
            sql2.executeUpdate();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        try {
            JSONObject json = new JSONObject();
            json.put("食物名称", name);
            json.put("热量（千卡）", Calories);
            json.put("蛋白质（克）", Protein);
            json.put("脂肪（克）", Fat);
            json.put("碳水化合物（克）", Carbohydrate);
            json.put("膳食纤维（克）", dietary_fiber);


            String jsons = json.toString();
            System.out.println("**********************************************************************************************");
            System.out.println(jsons);
            return jsons;
            //returns(name,Calories,Protein,Fat,Carbohydrate,dietary_fiber);

        }
        catch(JSONException e){
            e.printStackTrace();
        }
        return "error";
    }

    @RequestMapping("/login")
    public String login(HttpServletRequest request) throws SQLException
    {
        String username=request.getParameter("username");
        String pwd=request.getParameter("password");
        System.out.println(username);
        System.out.println(pwd);
        String isExist=users(username,pwd);
        return isExist;
    }
    public String users(String username,String pwd) throws SQLException {
        Connection con;
//        Statement sql;
//        ResultSet res;
        PreparedStatement sql;
        ResultSet res;
        String password="";
        String url="jdbc:mysql://localhost:3306/foodrecognition?serverTimezone=UTC";
        //ImgRecognitionApplicationTests c=new ImgRecognitionApplicationTests();
        con= DriverManager.getConnection( url, "root", "123456");
        try {
//            sql = con.createStatement();
//            res = sql.executeQuery("select password from foodrecognition.users where username=" + username);
//            while (res.next()) {
//                password = res.getString("password");
//
//                System.out.println(password);
//                System.out.println(pwd);
//        }
                String ss="select password from foodrecognition.users where username=?";
                sql=con.prepareStatement(ss);
                sql.setString(1,username);
                res=sql.executeQuery();
                while (res.next()) {
                    password = res.getString("password");

                    System.out.println(password);
                    System.out.println(pwd);
                }

        }catch (Exception e){
            e.printStackTrace();
        }
        if(pwd.equals(password)){
            return "true";
        }
        else {
            return "false";
        }
    }
    @RequestMapping("/record")
    public String record(HttpServletRequest request) throws SQLException
    {
        Connection con;
        PreparedStatement sql;
        ResultSet res;
        String username=request.getParameter("username");
        String filename=request.getParameter("filename");
        filename = imgPath + filename + ".jpg";
        String url="jdbc:mysql://localhost:3306/foodrecognition?serverTimezone=UTC";
        //ImgRecognitionApplicationTests c=new ImgRecognitionApplicationTests();
        con= DriverManager.getConnection( url, "root", "123456");
        try {
            sql = con.prepareStatement("update foodrecognition.record set mark=1 where username=? and path=?");
            sql.setString(1,username);
            sql.setString(2,filename);
            System.out.println(username);
            System.out.println(filename);
            sql.executeUpdate();
            return "true";
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return "false";

    }
    @RequestMapping("/dailymeals")
    public String dailymeals(HttpServletRequest request) throws SQLException
    {
        Connection con;
        PreparedStatement sql;
        ResultSet res;
        String username=request.getParameter("username");
        String calendar=request.getParameter("date");

        System.out.println(username);
        System.out.println(calendar);


        String url="jdbc:mysql://localhost:3306/foodrecognition?serverTimezone=UTC";
        //ImgRecognitionApplicationTests c=new ImgRecognitionApplicationTests();
        con= DriverManager.getConnection( url, "root", "123456");
        try {
            //sql = con.prepareStatement("select * from foodrecognition.record where username=? and calendar=? and mark=1");
            sql = con.prepareStatement("select * from foodrecognition.record where username=? and calendar=? and mark=1");
            sql.setString(1,username);
            sql.setString(2,calendar);

            res = sql.executeQuery();

            List<DietRecord> listData=new ArrayList<>();

            while (res.next())
            {
                DietRecord record=new DietRecord();

                record.time = res.getString("calendar")+" "+res.getString("time");
                record.filepath=res.getString("path");
                record.name = res.getString("foodname");
                record.Calories = res.getString("Calories");
                record.Protein = res.getString("Protein");
                record.Fat = res.getString("Fat");
                record.Carbohydrate = res.getString("Carbohydrate");
                record.dietary_fiber = res.getString("dietary_fiber");
                listData.add(record);
            }

            String s  = com.alibaba.fastjson.JSON.toJSONString(listData);
            System.out.println(s);
            return s;
        }catch (Exception e)
        {
            e.printStackTrace();
        }

        return "error";

    }


    public static String GetImageStr(File imgFile) {//将图片文件转化为字节数组字符串，并对其进行Base64编码处理

        InputStream in = null;

        byte[] data = null;

        //读取图片字节数组

        try {

            in = new FileInputStream(imgFile);

            data = new byte[in.available()];

            in.read(data);

            in.close();

        } catch (IOException e) {

            e.printStackTrace();

        }

        //对字节数组Base64编码

        BASE64Encoder encoder = new BASE64Encoder();

        return encoder.encode(data);//返回Base64编码过的字节数组字符串

    }


}

