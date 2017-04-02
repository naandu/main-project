package controllers;

import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import play.*;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.*;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import views.html.*;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render("Your new application is ready."));
    }
    
    public static Result details(){
    	String[] postAction = request().body().asFormUrlEncoded().get("submit");
    	String action=postAction[0];
    	if ("Login".equals(action)) {
    		DynamicForm dynamicForm=Form.form().bindFromRequest();
        	String username=dynamicForm.get("username");
        	String password=dynamicForm.get("password");
        	String sql="SELECT email,password FROM register WHERE email=? AND password=?";
        	Connection connection=play.db.DB.getConnection();
			try {
				PreparedStatement stmt = connection.prepareStatement(sql);
				stmt.setString(1, username);
				stmt.setString(2, password);
				ResultSet rs=stmt.executeQuery();
				if(rs.next()){
					return redirect(routes.Assets.at("html/success.html"));
				}
				else{
				    JOptionPane optionPane = new JOptionPane("Invalid Login",JOptionPane.WARNING_MESSAGE);
				    JDialog dialog = optionPane.createDialog("Warning!");
				    dialog.setAlwaysOnTop(true);
				    dialog.setVisible(true);
					return unauthorized("Invalid Email-ID & Password");
				}
			} catch (SQLException e) {
				e.printStackTrace();
				return ok("SQL Exception");
			}
    	}
    	else if ("Register".equals(action)) {
    		return redirect(routes.Assets.at("html/register.html"));
    	}
    	else {
    	      return badRequest("This action is not allowed");
    	}
    }
    
    public static Result data(){
    	try{
    		MultipartFormData body = request().body().asMultipartFormData();
    	    FilePart picture = body.getFile("image");
    	    File image=picture.getFile();
			FileInputStream fis=new FileInputStream(image);
			if(fis!=null){
				DynamicForm dynamicForm = Form.form().bindFromRequest();
				String title=dynamicForm.get("text1");
				String sql = "INSERT INTO details (Title,Image) VALUES (?,?)";
				Connection connection=play.db.DB.getConnection();
				PreparedStatement stmt = connection.prepareStatement(sql);
				stmt.setString(1,title);
				stmt.setBinaryStream(2,  (InputStream)fis,(int)image.length());        	
				stmt.executeUpdate();
				fis.close();
				stmt.close();
			}
			
    	}
    	catch(FileNotFoundException fn){
    		return ok("Failed");
    	}
    	catch(SQLException  se){
    		return ok("SQL Exception");
    	}
    	catch(IOException ie){
    		return ok("IO Exception");
    	}
    	
    	return ok("File Uploaded Successfully...");
    }
    
    public static Result register(){
    	try{
				DynamicForm dynamicForm = Form.form().bindFromRequest();
				String fn=dynamicForm.get("fn");
				String ln=dynamicForm.get("ln");
				String email=dynamicForm.get("email");
				String passwd=dynamicForm.get("passwd");
				String repasswd=dynamicForm.get("repasswd");
				String gender=dynamicForm.get("gender");
				String mno=dynamicForm.get("mno");
				if(passwd.equals(repasswd)){
					String sql1="SELECT * FROM register WHERE email=? OR mno=?";
					String sql = "INSERT INTO register (fn,ln,email,password,gender,mno) VALUES (?,?,?,?,?,?)";
					Connection connection=play.db.DB.getConnection();
					PreparedStatement stmt1 = connection.prepareStatement(sql1);
					stmt1.setString(1,email);
					stmt1.setString(2,mno);
					ResultSet rs=stmt1.executeQuery();
					if(rs.next()){
						return ok("User already exist with Email ID or Mobile no...");
					}
					else{
						PreparedStatement stmt = connection.prepareStatement(sql);
						stmt.setString(1,fn);
						stmt.setString(2,ln);
						stmt.setString(3,email);
						stmt.setString(4,passwd);
						stmt.setString(5,gender);
						stmt.setString(6,mno);        	
						stmt.executeUpdate();
						stmt.close();
						return redirect(routes.Assets.at("html/RegisterSuccess.html"));
					}
				}
				else{
					return ok("Password doesn't match...");
				}
    	}
    	catch(SQLException  se){
    		return ok("SQL Exception");
    	}
   
    }

}
