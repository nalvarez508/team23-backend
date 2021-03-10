/*package com.team23.stim.controller;
import com.team23.stim.classes.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseController {
  
  Connection c = null;
  Statement stmt = null;

  DatabaseController.forName("org.postgresql.Driver");
  c = DriverManager
    .getConnection("jdbc:postgresql://localhost:5432/stimdb",
    "stim", "stim");
  c.setAutoCommit(false);
  System.out.println("Opened database successfully");
       /*
       stmt = c.createStatement();
       String sql = "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) "
          + "VALUES (1, 'Paul', 32, 'California', 20000.00 );";
       stmt.executeUpdate(sql);

       sql = "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) "
          + "VALUES (2, 'Allen', 25, 'Texas', 15000.00 );";
       stmt.executeUpdate(sql);

       sql = "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) "
          + "VALUES (3, 'Teddy', 23, 'Norway', 20000.00 );";
       stmt.executeUpdate(sql);

       sql = "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) "
          + "VALUES (4, 'Mark', 25, 'Rich-Mond ', 65000.00 );";
       stmt.executeUpdate(sql);

       stmt.close();
       c.commit();
       c.close();*//*
  catch (Exception e) {
    System.err.println( e.getClass().getName()+": "+ e.getMessage() );
  }

  public void insertMainItem(MainItem iname)
  {
    stmt = c.createStatement();

    String SubSkuList = "'{";
    for (int i=0; i<iname.getSubSkus.size(), i++)
    {
      if (i==0)
      {
        SubSkuList += iname.getSubSkus.get(i);
      }
      else
      {
        SubSkuList += (", " + iname.getSubSkus.get(i));
      }
    }
    SubSkuList += "}'";

    String input = ("INSERT INTO mainitem VALUES ('" + iname.getName() + "', " + iname.getSKU() + ", " + iname.getPrice() + ", " + SubSkuList + ");");
    stmt.executeUpdate(input);
    stmt.close();
    c.commit();
  }

  public void insertSubItem(SubItem iname)
  {
    stmt = c.createStatement();

    String input = ("INSERT INTO subitem VALUES ('" + iname.getName() + "', " + iname.getSKU() + ", " + iname.getQty() + ", " + iname.getMuq() + ", '" + iname.getType() + "');");
    stmt.executeUpdate(input);
    stmt.close();
    c.commit();
  }
}
*/