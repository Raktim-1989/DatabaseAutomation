package storedfunctiontesting;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.CallableStatement;

import utils.Utility;

import org.testng.Assert;

public class SFTesting {
	
	Connection con ;
	Statement st ;
	ResultSet rs ;
	ResultSet rs1;
	ResultSet rs2;
	CallableStatement cst;
	
	@BeforeClass
	void setUp() throws SQLException
	{
		con = DriverManager.getConnection("jdbc:mysql://localhost:3306/classicmodels", "root", "password");
	}
	
	/*
	 * TC001: Check SF CustomerLevel exists in the database
	 */
	@Test(priority = 1)
	public void isSFExists() throws SQLException
	{
		st = con.createStatement();
		rs = st.executeQuery("SHOW FUNCTION STATUS WHERE db = 'classicmodels' ");
		rs.next();
		Assert.assertEquals(rs.getString("Name"), "CustomerLevel");
	}
	
	
	/*
	 * TC02: check SF 'CustomerLevel' return customer level when it calls from SQL statement
	 */
	@Test(priority = 2)
	public void CustomerLevelSF() throws SQLException
	{
		//always use seperate statement object for different queries
		Statement st1 = con.createStatement();
		rs1 = st1.executeQuery("select customerName, CustomerLevel(creditLimit) from customers");
		Statement st2 = con.createStatement();
		rs2 = st2.executeQuery("select customerName, \r\n" + 
				"CASE\r\n" + 
				"  WHEN creditLimit > 50000 THEN 'PLATINUM'\r\n" + 
				"  WHEN creditLimit >= 10000 AND creditLimit <= 50000 THEN 'GOLD'\r\n" + 
				"  WHEN creditLimit <10000 THEN 'SILVER'\r\n" + 
				"END AS customerLevel FROM customers;");
		
		boolean flag = Utility.compareResultsSets(rs1, rs2);
		Assert.assertTrue(flag);
		
	}
	
	
	/*
	 * TC03: check SF 'CustomerLevel' return customer level when it calls from stored procedure GetCustomerLevel()
	 */
	@Test(priority = 3)
	public void test_CustomerLevel_with_StoredProcedure() throws SQLException
	{
		cst = con.prepareCall("{CALL GetCustomerLevel(?,?)}");
		cst.setInt(1, 131);
		cst.registerOutParameter(2, Types.VARCHAR);
		
		cst.executeQuery();
		String act_customerLevel = cst.getString(2);
		System.out.println("act_customerlevel is   " +  act_customerLevel);
		
		//Expected result query ---
		rs = con.createStatement().executeQuery("SELECT customerName, \r\n" + 
				"CASE\r\n" + 
				"  WHEN creditLimit > 50000 THEN 'PLATINUM'\r\n" + 
				"  WHEN creditLimit >= 10000 AND creditLimit <= 50000 THEN 'GOLD'\r\n" + 
				"  WHEN creditLimit <10000 THEN 'SILVER'\r\n" + 
				"END AS customerLevel FROM customers WHERE customerNumber=131");
		
	   rs.next();
	  String exp_customerLevel =  rs.getString("customerLevel");
	  Assert.assertEquals(act_customerLevel, exp_customerLevel);	
		
	}
	
	
	
	@AfterClass
	void tearDown() throws SQLException
	{
		con.close();
	}
	
	

}
