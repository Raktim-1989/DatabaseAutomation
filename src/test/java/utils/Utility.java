package utils;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;


public class Utility {
	
	public static boolean compareResultsSets(ResultSet rs1 , ResultSet rs2) throws SQLException
	{
		while(rs1.next())
		{
			rs2.next();
			int colCount = rs1.getMetaData().getColumnCount();
			for(int i = 1; i<=colCount;i++)
			{
				if(!(StringUtils.equals(rs1.getString(i), rs2.getString(i))))
				{
					return false;
				}
				
			}
			
		}
		
		return true;
		
	}

}
