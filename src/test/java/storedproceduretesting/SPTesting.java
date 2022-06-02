package storedproceduretesting;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import utils.Utility;

import java.sql.CallableStatement;

public class SPTesting {

	Connection con = null;
	Statement st = null;
	ResultSet rs = null;
	ResultSet rs1 ;
	ResultSet rs2 ;
	CallableStatement cst;

	@BeforeClass
	void setUp() throws SQLException {
		con = DriverManager.getConnection("jdbc:mysql://localhost:3306/classicmodels", "root", "password");

	}

	/*
	 * TC001: This Test is to verify whether the correct SP name is displayed from the SP Status
	 */
	@Test(priority = 1)
	public void isSPExists() throws SQLException {

		st = con.createStatement();
		rs = st.executeQuery("SHOW PROCEDURE STATUS WHERE NAME = 'SelectAllCustomers' ");
		rs.next();

		String actualNameValue = rs.getString("Name");
		Assert.assertEquals(actualNameValue, "SelectAllCustomers");

	}
	
	/*
	 * TC002: This Test is to verify the SP 'SelectAllCustomers' display all records from 'Customers' table
	 */
	@Test(priority = 2)
	public void selectAllCustomersSP() throws SQLException
	{
		cst = con.prepareCall("{CALL SelectAllCustomers()}");
		rs1 = cst.executeQuery();  //resultset 1 comes from SP
		
		Statement st = con.createStatement();
		rs2 = st.executeQuery("select * from customers");  //resultset2 comes from testQiery
		
		boolean flag = Utility.compareResultsSets(rs1, rs2);
		Assert.assertTrue(flag);
		
	}
	/*
	 * TC003: This Test is to verify the SP 'SelectAllCustomersByCity' by passing CityName as input parameter
	 */
	@Test(priority = 3)
	public void SelectAllCustomersByCitySP() throws SQLException
	{
		cst = con.prepareCall("{CALL SelectAllCustomersByCity(?)}");
		cst.setString(1, "Singapore");
		rs1 = cst.executeQuery();    //resultset1
		
		Statement st = con.createStatement();
		rs2 = st.executeQuery("select * from customers where city = 'Singapore'");  //resultset2
		
		boolean flag = Utility.compareResultsSets(rs1, rs2);
		Assert.assertTrue(flag);
		
	}
	/*
	 * TC004: This Test is to verify the SP 'SelectAllCustomersByCityAndPin' by passing CityName & pincode as input parameters
	 */
	@Test(priority = 4)
	public void SelectAllCustomersByCityAndPinSP() throws SQLException
	{
		cst = con.prepareCall("{CALL SelectAllCustomersByCityAndPin(?,?)}");
		cst.setString(1, "Singapore");
		cst.setString(2, "079903");
		rs1 = cst.executeQuery();    //resultset1
		
		Statement st = con.createStatement();
		rs2 = st.executeQuery("select * from customers where city = 'Singapore' and postalCode = '079903'");  //resultset2
		
		boolean flag = Utility.compareResultsSets(rs1, rs2);
		Assert.assertTrue(flag);
		
	}
	/*
	 * TC005: This Test is to verify the SP get_order_by_cust' by passing input param 'custID' and output param @shipped,@cancelled,@resolved,@dispute
	 */
	@Test(priority = 5)
	public void get_order_by_custSP() throws SQLException
	{
		cst = con.prepareCall("{CALL get_order_by_cust(?,?,?,?,?)}");
		cst.setInt(1, 141);
		//setting following output params
		cst.registerOutParameter(2, Types.INTEGER);
		cst.registerOutParameter(3, Types.INTEGER);
		cst.registerOutParameter(4, Types.INTEGER);
		cst.registerOutParameter(5, Types.INTEGER);
		
		cst.executeQuery();    //resultset1
		
		int shipped = cst.getInt(2);    //get data from output paramas
		int cancelled = cst.getInt(3);
		int resolved = cst.getInt(4);
		int disputed = cst.getInt(5);
		
		System.out.println(shipped + " " + cancelled + " " + resolved + " " + disputed);
		
		//Executing the Test Query
		Statement st = con.createStatement();
		String query = "select\r\n" + 
				"(SELECT count(*) as shipped FROM orders where customerNumber = 141 AND status = 'Shipped') as Shipped,\r\n" + 
				"(SELECT count(*) as cancelled FROM orders where customerNumber = 141 AND status = 'Cancelled') as Cancelled,\r\n" + 
				"(SELECT count(*) as resolved FROM orders where customerNumber = 141 AND status = 'Resolved') as Resolved,\r\n" + 
				"(SELECT count(*) as disputed FROM orders where customerNumber = 141 AND status = 'Disputed') as Disputed;\r\n" + 
				"";
		rs = st.executeQuery(query);  //resultset2
		rs.next();
		
		int exp_shipped = rs.getInt("Shipped");
		int exp_cancelled = rs.getInt("Cancelled");
		int exp_resolved = rs.getInt("Resolved");
		int exp_disputed = rs.getInt("Disputed");
		
		if(shipped==exp_shipped && cancelled==exp_cancelled && resolved==exp_resolved && disputed==exp_disputed)
			
		{
			
			Assert.assertTrue(true);
			
		}
		else
		{
			Assert.assertTrue(false);
		}
				
	}
	
	/*
	 * TC005: This Test is to verify the SP 'GetCustomerShipping' by passing input parameter 'customerNumber'
	 * 
	*/
	@Test(priority = 5)
	public void GetCustomerShippingSP() throws SQLException
	{
		cst = con.prepareCall("{CALL GetCustomerShipping(?,?)}");
		cst.setInt(1, 112);
		cst.registerOutParameter(2, Types.VARCHAR);
		cst.executeQuery();   
		String actualShippingtime = cst.getString(2);
		
		//Executing Test Query
		Statement st = con.createStatement();
		rs = st.executeQuery(" SELECT country,\r\n" + 
				" CASE\r\n" + 
				"   WHEN country = 'USA' THEN '2-day Shipping'\r\n" + 
				"   WHEN country = 'CANADA' THEN '3-day Shipping'\r\n" + 
				"   ELSE '5-day shipping'\r\n" + 
				" END as Shippingtime\r\n" + 
				" FROM Customers where customerNumber = 112;");    
		
		rs.next();
		String expectedShippingtime = rs.getString("Shippingtime");
		
		Assert.assertEquals(actualShippingtime, expectedShippingtime);
		
	}
	
	@AfterClass
	void tearDown() throws SQLException {
		con.close();
	}

}
