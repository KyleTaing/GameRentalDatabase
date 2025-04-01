/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math;
import java.math.BigDecimal;
import java.util.Scanner;
import java.sql.Timestamp;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class GameRental {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of GameRental store
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public GameRental(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end GameRental

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
		 if(outputHeader){
			for(int i = 1; i <= numCol; i++){
			System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println();
			outputHeader = false;
		 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
        List<String> record = new ArrayList<String>();
		for (int i=1; i<=numCol; ++i)
			record.add(rs.getString (i));
        result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while (rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            GameRental.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      GameRental esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the GameRental object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new GameRental (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. View Profile");
                System.out.println("2. Update Profile");
                System.out.println("3. View Catalog");
                System.out.println("4. Place Rental Order");
                System.out.println("5. View Full Rental Order History");
                System.out.println("6. View Past 5 Rental Orders");
                System.out.println("7. View Rental Order Information");
                System.out.println("8. View Tracking Information");

                //the following functionalities basically used by employees & managers
                System.out.println("9. Update Tracking Information");

                //the following functionalities basically used by managers
                System.out.println("10. Update Catalog");
                System.out.println("11. Update User");

                System.out.println(".........................");
                System.out.println("20. Log out");
                switch (readChoice()){
                   case 1: 
                   viewProfile(esql, authorisedUser); 
                   break;
                   case 2: 
                   try {
                     updateProfile(esql, authorisedUser);
                  } catch (SQLException e) {
                     // Handle the SQLException here
                     e.printStackTrace();
                 }
                 break;
                   case 3: viewCatalog(esql); break;
                   case 4: 
                   try {
                     placeOrder(esql, authorisedUser);
                  } catch (SQLException e) {
                     // Handle the SQLException here
                     e.printStackTrace();
                 }
                 break;
                   case 5: viewAllOrders(esql, authorisedUser); break;
                  case 6: viewRecentOrders(esql, authorisedUser); break;
                   case 7: viewOrderInfo(esql, authorisedUser); break;
                   case 8: viewTrackingInfo(esql, authorisedUser); break;
                   case 9: 
                   try {
                     updateTrackingInfo(esql, authorisedUser);
                  } catch (SQLException e) {
                     // Handle the SQLException here
                     e.printStackTrace();
                 }
                   break;                   
                   case 10: 
                   try {
                     updateCatalog(esql, authorisedUser);
                  } catch (SQLException e) {
                     // Handle the SQLException here
                     e.printStackTrace();
                 }
                   break;
                   case 11:                      
                   try {
                     updateUser(esql, authorisedUser);
                  } catch (SQLException e) {
                     // Handle the SQLException here
                     e.printStackTrace();
                 }
                 break;



                   case 20: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user
    **/
    public static void CreateUser(GameRental esql) {
      Scanner scan = new Scanner(System.in);
      String userName;
      String password;
      String phoneNumber;
  
      try {
          System.out.print("Username: ");
          userName = scan.nextLine();
          System.out.print("Password: ");
          password = scan.nextLine();
          System.out.print("Phone Number: ");
          phoneNumber = scan.nextLine();
  
          // Check if username already exists
          String checkUserQuery = "SELECT login FROM Users WHERE login = '" + userName + "'";
          List<List<String>> existingUser = esql.executeQueryAndReturnResult(checkUserQuery);
          
          if (!existingUser.isEmpty()) {
              System.out.println("Username already exists. Please choose a different username.");
              return;
          }
  
          // Insert new user into the Users table
          String insertQuery = "INSERT INTO Users (login, password, role, favGames, phoneNum, numOverdueGames) VALUES ('" 
                               + userName + "', '" + password + "', 'customer', null, '" + phoneNumber + "', 0)";
          esql.executeUpdate(insertQuery);
          System.out.println("User created successfully!");
  
      } catch (SQLException e) {
          System.err.println("SQL Error: " + e.getMessage());
      } catch (Exception e) {
          System.err.println("Error: " + e.getMessage());
      }
  }
  


   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
    public static String LogIn(GameRental esql) {
      Scanner scan = new Scanner(System.in);
      String userName;
      String password;
      System.out.print("Please enter your username: ");
      userName = scan.nextLine();
      System.out.print("Please enter your password: ");
      password = scan.nextLine();
  
      // Check if username and password both inside the query, if true, return the login
      String query = "SELECT login FROM USERS WHERE login = '" + userName + "' AND password = '" + password + "'";
  
      try {
         List<List<String>> result = esql.executeQueryAndReturnResult(query);
         if (!result.isEmpty()) {
             // Login successful
             return result.get(0).get(0); // Assuming login is the first column
         } else {
             // Login failed
             System.out.println("Login failed. Username or password is incorrect.");
             return null;
         }
     } catch (SQLException e) {
         e.printStackTrace();
         return null;
     }
  }
  

// Rest of the functions definition go in here

   public static void viewProfile(GameRental esql, String authorisedUser) {
      System.out.println("Please select which you would like to view?");
      System.out.println("1. Favorite Games?");
      System.out.println("2. Number Overdue Games?");
      System.out.println("3. Phone Number?");
      System.out.println("9. Exit ");
      switch (readChoice()){
         // Case 1: 
         case 1: 
         printFavoriteGames(esql, authorisedUser);
         break;
         case 2: 
         // "View Overdue games"updateProfile""(esql); break;
         printOverdueGames(esql, authorisedUser);
         break;
         case 3: 
         printPhoneNumber(esql, authorisedUser);
         break;
         case 9: 
         break;
         default : System.out.println("Unrecognized choice!"); break;

      }
   }


   public static void updateProfile(GameRental esql, String authorisedUser) throws SQLException{
      Scanner scan = new Scanner(System.in);
      String userRole = "Select role FROM USERS WHERE login = '" + authorisedUser + "'";
      List<List<String>> result = esql.executeQueryAndReturnResult(userRole);
      String role = result.get(0).get(0);
      if (role != null && role.trim().equalsIgnoreCase("manager")) {
         System.out.print("Please select the user you would like to change ");
         String userName = scan.nextLine();
         if (userName == null) {
            System.out.println("No user is logged in.");
            return;
         }
         System.out.println("Please select which you would like to change?");
         System.out.println("1. Password?");
         System.out.println("2. Phone Number?");
         System.out.println("3. Username?");
         System.out.println("4. Role?");
         System.out.println("5. Overdue Games?");
         System.out.println("9. Exit ");
         switch (readChoice()){
            // Case 1: 
            case 1: 
            try {
               updatePassword(esql, userName);
           } catch (SQLException e) {
               // Handle the SQLException here
               e.printStackTrace();
           }
           break;
            case 2: 
            try {
               updatePhoneNumber(esql, userName);
           } catch (SQLException e) {
               // Handle the SQLException here
               e.printStackTrace();
           }
           break;
            case 3: 
            try {
               updateUserName(esql, userName);
           } catch (SQLException e) {
               // Handle the SQLException here
               e.printStackTrace();
           }
           break;
            case 4:
            try {
               updateRole(esql, userName);
           } catch (SQLException e) {
               // Handle the SQLException here
               e.printStackTrace();
           }
           break;
            case 5:
            try {
               updateOverdueGames(esql, userName);
           } catch (SQLException e) {
               // Handle the SQLException here
               e.printStackTrace();
           }
           break;            
           case 9: 
           break;
           default : System.out.println("Unrecognized choice!"); break;
         }
      }
      else {
         System.out.println("Please select which you would like to change?");
         System.out.println("1. Password?");
         System.out.println("2. Phone Number?");
         System.out.println("9. Exit ");
         switch (readChoice()){
            // Case 1: 
            case 1: 
            try {
               updatePassword(esql, authorisedUser);
           } catch (SQLException e) {
               // Handle the SQLException here
               e.printStackTrace();
           }
           break;
            case 2: 
            try {
               updatePhoneNumber(esql, authorisedUser);
           } catch (SQLException e) {
               // Handle the SQLException here
               e.printStackTrace();
           }
           break;
            case 9: 
            break;
            default : System.out.println("Unrecognized choice!"); 
            break;
         }
      }
   }
   

   public static void updatePassword(GameRental esql, String authorisedUser) throws SQLException{
      Scanner scan = new Scanner(System.in);
      System.out.println("What would you like to change your password to?");
      String newVal = scan.nextLine();
      String insertQuery = "UPDATE users Set password = '" + newVal + "' where login = '" + authorisedUser + "'";
      esql.executeUpdate(insertQuery);
   }
   public static void updatePhoneNumber(GameRental esql, String authorisedUser) throws SQLException{
      Scanner scan = new Scanner(System.in);
      System.out.println("What would you like to change your phone number to?");
      String newVal = scan.nextLine();
      String insertQuery = "UPDATE users Set phoneNum = '" + newVal + "' where login = '" + authorisedUser + "'";
      esql.executeUpdate(insertQuery);

   }

   public static void updateUserName(GameRental esql, String authorisedUser) throws SQLException{
      Scanner scan = new Scanner(System.in);
      System.out.println("What would you like to change your username to?");
      String newVal = scan.nextLine();
      String insertQuery = "UPDATE users Set login = '" + newVal + "' where login = '" + authorisedUser + "'";
      esql.executeUpdate(insertQuery);

   }
   public static void updateRole(GameRental esql, String authorisedUser) throws SQLException{
      Scanner scan = new Scanner(System.in);
      System.out.println("What would you like to change your role to?");
      String newVal = scan.nextLine();
      String insertQuery = "UPDATE users Set role = '" + newVal + "' where login = '" + authorisedUser + "'";
      esql.executeUpdate(insertQuery);
   }

   public static void updateOverdueGames(GameRental esql, String authorisedUser) throws SQLException{
      Scanner scan = new Scanner(System.in);
      System.out.println("What would you like to change your overdue games to?");
      String newVal = scan.nextLine();
      String insertQuery = "UPDATE users Set numOverDueGames = '" + newVal + "' where login = '" + authorisedUser + "'";
      esql.executeUpdate(insertQuery);
   }



   public static void viewCatalog(GameRental esql) {
      Scanner scan = new Scanner(System.in);
      System.out.println("How would you like to view Catalog");
      System.out.println("1. Genre");
      System.out.println("2. Price");
      System.out.println("3. Lowest to Highest Price");
      System.out.println("4. Highest to Lowest Price");
      switch(readChoice()){
         case 1: 
        findGenre(esql);
        break;
        case 2:
        findPrice(esql);
        break; 
        case 3: 
        LowtoHigh(esql);
        break;
        case 4: 
         HightoLow(esql);
       break;
       default : System.out.println("Unrecognized choice!");
       break;
      }
   }   
   public static void placeOrder(GameRental esql, String authorisedUser) throws SQLException{
      boolean finishedPurchase = false;
      List<String> gameIDs = new ArrayList<>();
      List<Integer> unitsOrdered = new ArrayList<>();
      Scanner scan = new Scanner(System.in);
      double totalPrice = 0.0;
      int totalGames = 0;
      while (!finishedPurchase) {
         System.out.println("Enter the Game ID of the game you want to purchase ");
         String gameID = scan.nextLine();
         gameIDs.add(gameID);
         System.out.println("Enter units ordered: ");
         int units = scan.nextInt();
         scan.nextLine();
         unitsOrdered.add(units);
         totalGames += units;
         totalPrice += getGamePrice(esql, gameID) * units;
         System.out.println("Do you want to buy more games? ");
         String answer = scan.nextLine();
         answer = answer.toLowerCase();
         if (answer.equals("no")) {
            finishedPurchase = true;
         }
         else if (!answer.equals("yes") && !answer.equals("no")){
            System.out.println("Wrong command, say yes or no");
            String response;
            do {
                response = scan.nextLine(); 
                response = response.toLowerCase(); 
                if (response.equals("no")) {
                  finishedPurchase = true;
                }
            } while (!response.equals("yes") && !response.equals("no")); 
            answer = response; 
        }   
      }
      Timestamp orderTimestamp = new Timestamp(System.currentTimeMillis());
      Timestamp dueDate = new Timestamp(orderTimestamp.getTime() + (30L * 24 * 60 * 60 * 1000)); // 30 days later
      int rentalID = (int)(Math.random() * (100000-5000) + 5000);
      String rentalOrderID = "gamerentalorder" + rentalID;
      String rentalOrderQuery = "SELECT rentalOrderID FROM RentalOrder WHERE rentalOrderID = '" + rentalOrderID + "'";
      List<List<String>> result = esql.executeQueryAndReturnResult(rentalOrderQuery);
      if (result.isEmpty()) {
         String rentalOrderSQL =  "INSERT INTO RentalOrder (rentalOrderID, login, noOfGames, totalPrice, orderTimestamp, dueDate) VALUES ('" 
         + rentalOrderID + "', '" + authorisedUser +  "', '" + totalGames + "', '" + totalPrice + "', '" + orderTimestamp + "', '" + dueDate + "')";
         esql.executeUpdate(rentalOrderSQL);
         String gameIDPer = "";
         int unitsOrderedPer = 0;
            for (int i = 0; i < gameIDs.size(); i++) {
               gameIDPer = gameIDs.get(i);
               unitsOrderedPer = unitsOrdered.get(i);   
               String gamesInOrderSQL = "INSERT INTO GamesInOrder (rentalOrderID, gameID, unitsOrdered) VALUES ('" + rentalOrderID + "', '" + gameIDPer + "', '" + unitsOrderedPer + "')";
               esql.executeUpdate(gamesInOrderSQL);

            }
            String trackingIDPer = "trackingid" + rentalID;
            String trackingInfoSQL = "INSERT INTO TrackingInfo (trackingID, rentalOrderID, status, currentLocation, courierName, lastUpdateDate) VALUES ('" + trackingIDPer + "', '" + rentalOrderID + "', 'Order Placed', 'Warehouse', 'CourierX', '" + orderTimestamp + "')";
            esql.executeUpdate(trackingInfoSQL);
            System.out.println("This worked");
      }
      System.out.println("The total price of all purchase are: " + totalPrice);
   }

   public static void updateUser(GameRental esql, String authorisedUser) throws SQLException{
      Scanner scan = new Scanner(System.in);
      String userRole = "Select role FROM USERS WHERE login = '" + authorisedUser + "'";
      List<List<String>> result = esql.executeQueryAndReturnResult(userRole);
      String role = result.get(0).get(0);
      if (role != null && role.trim().equalsIgnoreCase("manager")) {
         System.out.print("Please select the user you would like to change ");
         String userName = scan.nextLine();
         if (userName == null) {
            System.out.println("No user is logged in.");
            return;
         }
         System.out.println("Please select which you would like to change?");
         System.out.println("1. Password?");
         System.out.println("2. Phone Number?");
         System.out.println("3. Username?");
         System.out.println("4. Role?");
         System.out.println("5. Overdue Games?");
         System.out.println("9. Exit ");
         switch (readChoice()){
            // Case 1: 
            case 1: 
            try {
               updatePassword(esql, userName);
           } catch (SQLException e) {
               // Handle the SQLException here
               e.printStackTrace();
           }
           break;
            case 2: 
            try {
               updatePhoneNumber(esql, userName);
           } catch (SQLException e) {
               // Handle the SQLException here
               e.printStackTrace();
           }
           break;
            case 3: 
            try {
               updateUserName(esql, userName);
           } catch (SQLException e) {
               // Handle the SQLException here
               e.printStackTrace();
           }
           break;
            case 4:
            try {
               updateRole(esql, userName);
           } catch (SQLException e) {
               // Handle the SQLException here
               e.printStackTrace();
           }
           break;
            case 5:
            try {
               updateOverdueGames(esql, userName);
           } catch (SQLException e) {
               // Handle the SQLException here
               e.printStackTrace();
           }
           break;            
           case 9: 
           break;
           default : System.out.println("Unrecognized choice!"); break;
         }
      }
   }

   public static void updateCatalog(GameRental esql, String authorisedUser) throws SQLException{
      String userRole = "Select role FROM USERS WHERE login = '" + authorisedUser + "'";
      List<List<String>> result = esql.executeQueryAndReturnResult(userRole);
      String role = result.get(0).get(0);

      if (role != null && role.trim().equalsIgnoreCase("manager")) {
         Scanner scan = new Scanner(System.in);
         System.out.println("Please enter the game ID of the game you want to change");
         String newVal = scan.nextLine();
         if (ifGameExist(esql, newVal) == false) {
            return;
         }
         else {
            System.out.println("Please select which you would like to change?");
            System.out.println("1. Game Name?");
            System.out.println("2. Genre?");
            System.out.println("3. Price?");
            System.out.println("4. Description?");
            System.out.println("5. Image?");
            System.out.println("9. Exit");
            switch (readChoice()){
               case 1: 
               try {
                  updateGameName(esql, newVal);
            } catch (SQLException e) {
                  e.printStackTrace();
            }
            break;
               case 2: 
               try {
                  updateGenre(esql, newVal);
            } catch (SQLException e) {
                  e.printStackTrace();
            }
            break;
            case 3: 
               try {
                  updatePrice(esql, newVal);
            } catch (SQLException e) {
                  e.printStackTrace();
            }
            break;
               case 4: 
               try {
                  updateDescription(esql, newVal);
            } catch (SQLException e) {
                  e.printStackTrace();
            }
            break;
            case 5: 
               try {
                  updateImages(esql, newVal);
            } catch (SQLException e) {
                  e.printStackTrace();
            }
            break;
               case 9: 
               break;
               default : System.out.println("Unrecognized choice!"); 
               break;
            }
         }
      }     
   }


   // Rental Order
   public static void viewAllOrders(GameRental esql, String authorisedUser) {
      String query = "SELECT rentalOrderID FROM RentalOrder WHERE login = '" + authorisedUser + "'";
      try {
         List<List<String>> result = esql.executeQueryAndReturnResult(query);
         if (!result.isEmpty()) {
             // Loop through the result and print each element
             for (List<String> row : result) {
                 for (String rentalOrder : row) {
                     System.out.println(rentalOrder);
                 }
             }
         } else {
             System.out.println("No RentalOrderIDS found.");
         }
     } catch (SQLException e) {
         e.printStackTrace();
     }
    }
    public static void viewRecentOrders(GameRental esql, String authorisedUser) {
     String query = "SELECT rentalOrderID FROM RentalOrder WHERE login = '" + authorisedUser + "' ORDER BY orderTimestamp DESC LIMIT 5 ";
     try {
      List<List<String>> result = esql.executeQueryAndReturnResult(query);
      if (!result.isEmpty()) {
          // Loop through the result and print each element
          for (List<String> row : result) {
              for (String rentalOrder : row) {
                  System.out.println(rentalOrder);
              }
          }
      } else {
          System.out.println("No RentalOrderIDS found.");
      }
  } catch (SQLException e) {
      e.printStackTrace();
  }
    }
    public static void viewOrderInfo(GameRental esql, String authorisedUser) {
      Scanner scan = new Scanner(System.in);
      String rentID; 
      System.out.println("Enter rentalID: ");
      try {  
  rentID = scan.nextLine();
  String query = "SELECT DISTINCT R.orderTimestamp, R.dueDate, R.totalPrice, T.trackingID FROM RentalOrder R INNER JOIN TrackingInfo T ON R.rentalOrderID = T.rentalOrderID INNER JOIN GamesInOrder G On R.rentalOrderID = G.rentalOrderID INNER JOIN Catalog C ON C.gameID = G.gameID WHERE login =  '" + authorisedUser + "' AND R.rentalOrderID = '" + rentID + "' ";
  List<List<String>> result = esql.executeQueryAndReturnResult(query);
  if (!result.isEmpty()) {
      // Loop through the result and print each element
      for (List<String> row : result) {
          for (String rentalOrder : row) {
              System.out.println(rentalOrder);
          }
      }
  } else {
      System.out.println("No RentalOrderID found.");
  }
  String query2 = "SELECT C.gameName FROM GamesInOrder G INNER JOIN Catalog C ON G.gameID = C.gameID WHERE G.rentalOrderID = '" + rentID + "' Order BY C.gameName";
  List<List<String>> result2 = esql.executeQueryAndReturnResult(query2);
  if (!result.isEmpty()) {
      // Loop through the result and print each element
      for (List<String> row2 : result2) {
          for (String rentalOrder2 : row2) {
              System.out.println(rentalOrder2);
          }
      }
  } else {
      System.out.println("No RentalOrderID found.");
  }
} catch (SQLException e) {
  e.printStackTrace();
}
}

   // Trackinng Info
   public static void viewTrackingInfo(GameRental esql, String authorisedUser) {
      Scanner scan = new Scanner(System.in);
      String trackerID;
      System.out.println("Enter trackingID: ");
      try {
      trackerID = scan.nextLine();
      String query = "SELECT T.courierName, T.rentalOrderID, T.currentLocation, T.status, T.lastUpdateDate, T.additionalComments FROM TrackingInfo T INNER JOIN RentalOrder R ON R.rentalOrderID = T.rentalOrderID WHERE R.login = '" + authorisedUser + "' AND trackingID = '" + trackerID + "' ";
          List<List<String>> result = esql.executeQueryAndReturnResult(query);
          if (!result.isEmpty()) {
              // Loop through the result and print each element
              for (List<String> row : result) {
                  for (String trackOrder : row) {
                      System.out.println(trackOrder);
                  }
              }
          } else {
              System.out.println("No TrackingOrderIDS found.");
          }
      } catch (SQLException e) {
          e.printStackTrace();
      }
  }
   public static void updateTrackingInfo(GameRental esql, String authorisedUser)throws SQLException{
      String userRole = "Select role FROM USERS WHERE login = '" + authorisedUser + "'";
      List<List<String>> result = esql.executeQueryAndReturnResult(userRole);
      String role = result.get(0).get(0);
      if ((role != null && role.trim().equalsIgnoreCase("manager")) || (role != null && role.trim().equalsIgnoreCase("employee"))) {
         Scanner scan = new Scanner(System.in);
         System.out.println("Please enter the tracking ID of the order you want to change");
         String newVal = scan.nextLine();
         if (ifTrackingIDExists(esql, newVal) == false) {
            return;
         }
         else {
            System.out.println("Please select which you would like to change?");
            System.out.println("1. Status?");
            System.out.println("2. Current Location?");
            System.out.println("3. Courier Name?");
            System.out.println("4. Additional Comments?");
            System.out.println("9. Exit");
            switch (readChoice()){
               case 1:
               try {
                  updateStatus(esql, newVal);
            } catch (SQLException e) {
                  e.printStackTrace();
            }
            break;
               case 2: 
               try {
                  updateCurrentLocation(esql, newVal);
            } catch (SQLException e) {
                  e.printStackTrace();
            }
            break;
            case 3: 
               try {
                  updateCourierName(esql, newVal);
            } catch (SQLException e) {
                  e.printStackTrace();
            }
            break;
               case 4: 
               try {
                  updateAdditionalComments(esql, newVal);
            } catch (SQLException e) {
                  e.printStackTrace();
            }
            break;
               case 9: 
               break;
               default : System.out.println("Unrecognized choice!"); 
               break;
            }
         }
      }     
   }

   
   // Helper functions
   public static void printFavoriteGames(GameRental esql, String authorisedUser) {
      String query = "SELECT favGames FROM USERS where login = '" + authorisedUser + "'";
      try {
          List<List<String>> result = esql.executeQueryAndReturnResult(query);
          if (!result.isEmpty()) {
              // Loop through the result and print each element
              for (List<String> row : result) {
                  for (String favGame : row) {
                      System.out.println(favGame);
                  }
              }
          } else {
              System.out.println("No favorite games found.");
          }
      } catch (SQLException e) {
          e.printStackTrace();
      }
  }

  public static void printOverdueGames(GameRental esql, String authorisedUser) {
   String query = "SELECT numOverdueGames FROM USERS WHERE login = '" + authorisedUser + "'";
   try {
       List<List<String>> result = esql.executeQueryAndReturnResult(query);
       if (!result.isEmpty()) {
           System.out.println("Number of Overdue Games: " + result.get(0).get(0));
       } else {
           System.out.println("No data found for user: " + authorisedUser);
       }
   } catch (SQLException e) {
       e.printStackTrace();
   }
}  

public static void printPhoneNumber(GameRental esql, String authorisedUser) {
   String query = "SELECT phoneNum FROM USERS WHERE login = '" + authorisedUser + "'";
   try {
       List<List<String>> result = esql.executeQueryAndReturnResult(query);
       if (!result.isEmpty()) {
           System.out.println("Phone Number: " + result.get(0).get(0));
       } else {
           System.out.println("No data found for user: " + authorisedUser);
       }
   } catch (SQLException e) {
       e.printStackTrace();
   }
}

public static boolean ifGameExist(GameRental esql, String ID) {
   String query = "SELECT gameID FROM Catalog WHERE gameID = '" + ID + "'";
   try {
       List<List<String>> result = esql.executeQueryAndReturnResult(query);
       if (!result.isEmpty()) {
           return true;
       } else {
           return false;
       }
   } catch (SQLException e) {
       e.printStackTrace();
   }
   return false;
}

public static boolean ifTrackingIDExists(GameRental esql, String ID) {
   String query = "SELECT trackingID FROM TrackingInfo WHERE trackingID = '" + ID + "'";
   try {
       List<List<String>> result = esql.executeQueryAndReturnResult(query);
       if (!result.isEmpty()) {
           return true;
       } else {
           return false;
       }
   } catch (SQLException e) {
       e.printStackTrace();
   }
   return false;
}

public static void updateGameName(GameRental esql, String ID) throws SQLException{
   Scanner scan = new Scanner(System.in);
   System.out.println("What would you like to change the game name to?");
   String newVal = scan.nextLine();
   String insertQuery = "UPDATE catalog Set gameName = '" + newVal + "' where gameID = '" + ID + "'";
   esql.executeUpdate(insertQuery);
}

public static void updateDescription(GameRental esql, String ID) throws SQLException{
   Scanner scan = new Scanner(System.in);
   System.out.println("What would you like to change the description to?");
   String newVal = scan.nextLine();
   String insertQuery = "UPDATE catalog Set description = '" + newVal + "' where gameID = '" + ID + "'";
   esql.executeUpdate(insertQuery);
}

public static void updatePrice(GameRental esql, String ID) throws SQLException{
   Scanner scan = new Scanner(System.in);
   System.out.println("What would you like to change the game price to?");
   BigDecimal newVal = scan.nextBigDecimal();
   String insertQuery = "UPDATE catalog Set price = '" + newVal + "' where gameID = '" + ID + "'";
   esql.executeUpdate(insertQuery);
}

public static void updateGenre(GameRental esql, String ID) throws SQLException{
   Scanner scan = new Scanner(System.in);
   System.out.println("What would you like to change the game genre to?");
   String newVal = scan.nextLine();
   String insertQuery = "UPDATE catalog Set genre = '" + newVal + "' where gameID = '" + ID + "'";
   esql.executeUpdate(insertQuery);
}

public static void updateImages(GameRental esql, String ID) throws SQLException{
   Scanner scan = new Scanner(System.in);
   System.out.println("What would you like to change the game image to?");
   String newVal = scan.nextLine();
   String insertQuery = "UPDATE catalog Set imageURL = '" + newVal + "' where gameID = '" + ID + "'";
   esql.executeUpdate(insertQuery);
}


public static void HightoLow(GameRental esql){
   String query = "SELECT gameName,price FROM Catalog Order By price DESC";
   try {
   List<List<String>> result = esql.executeQueryAndReturnResult(query);
   if (!result.isEmpty()) {
      // Loop through the result and print each element
      for (List<String> row : result) {
          for (String gameName : row) {
              System.out.println(gameName);
          }
      }
  } else {
      System.out.println("No games found.");
  }
   }catch (SQLException e) {
      e.printStackTrace();
  }
 }
 public static void LowtoHigh(GameRental esql){
   String query = "SELECT gameName,price FROM Catalog Order By price ASC";
   try {
   List<List<String>> result = esql.executeQueryAndReturnResult(query);
   if (!result.isEmpty()) {
      // Loop through the result and print each element
      for (List<String> row : result) {
          for (String gameName : row) {
              System.out.println(gameName);
          }
      }
  } else {
      System.out.println("No games found.");
  }
   }catch (SQLException e) {
      e.printStackTrace();
  }
 }
 public static void findPrice(GameRental esql){
   Scanner scan = new Scanner(System.in);
   String gamePrice; 
   try {
   System.out.println("How much for a game?");
   gamePrice = scan.nextLine();
   String query = "SELECT gameName FROM Catalog WHERE price = '" + gamePrice + "'";
   List<List<String>> result = esql.executeQueryAndReturnResult(query);
   if (!result.isEmpty()) {
      // Loop through the result and print each element
      for (List<String> row : result) {
          for (String gameName : row) {
              System.out.println(gameName);
          }
      }
  } else {
      System.out.println("No games found.");
  }
   }catch (SQLException e) {
      e.printStackTrace();
  }
 }
 public static void findGenre(GameRental esql){
   Scanner scan = new Scanner(System.in);
   String genreName; 
   try {
   System.out.println("What's the name of the genre?");
   genreName = scan.nextLine();
   String query = "SELECT gameName FROM Catalog WHERE genre = '" + genreName + "'";
   List<List<String>> result = esql.executeQueryAndReturnResult(query);
   if (!result.isEmpty()) {
      // Loop through the result and print each element
      for (List<String> row : result) {
          for (String gameName : row) {
              System.out.println(gameName);
          }
      }
  } else {
      System.out.println("No games found.");
  }
   }catch (SQLException e) {
      e.printStackTrace();
  }
}

public static void updateStatus(GameRental esql, String ID) throws SQLException{
   Scanner scan = new Scanner(System.in);
   System.out.println("What would you like to change the status to?");
   String newVal = scan.nextLine();
   Timestamp orderTimestamp = new Timestamp(System.currentTimeMillis());
   String insertQuery = "UPDATE TrackingInfo Set status = '" + newVal + "' where trackingID = '" + ID + "'";
   String insertQueryTime = "UPDATE TrackingInfo Set lastUpdateDate = '" + orderTimestamp + "' where trackingID = '" + ID + "'";
   esql.executeUpdate(insertQuery);
   esql.executeUpdate(insertQueryTime);
}

public static void updateCurrentLocation(GameRental esql, String ID) throws SQLException{
   Scanner scan = new Scanner(System.in);
   System.out.println("What would you like to change the Current Location to?");
   String newVal = scan.nextLine();
   Timestamp orderTimestamp = new Timestamp(System.currentTimeMillis());
   String insertQuery = "UPDATE TrackingInfo Set currentLocation = '" + newVal + "' where trackingID = '" + ID + "'";
   String insertQueryTime = "UPDATE TrackingInfo Set lastUpdateDate = '" + orderTimestamp + "' where trackingID = '" + ID + "'";
   esql.executeUpdate(insertQuery);
   esql.executeUpdate(insertQueryTime);
}


public static void updateCourierName(GameRental esql, String ID) throws SQLException{
   Scanner scan = new Scanner(System.in);
   System.out.println("What would you like to change the Courier Name to?");
   String newVal = scan.nextLine();
   Timestamp orderTimestamp = new Timestamp(System.currentTimeMillis());
   String insertQuery = "UPDATE TrackingInfo Set courierName = '" + newVal + "' where trackingID = '" + ID + "'";
   String insertQueryTime = "UPDATE TrackingInfo Set lastUpdateDate = '" + orderTimestamp + "' where trackingID = '" + ID + "'";
   esql.executeUpdate(insertQuery);
   esql.executeUpdate(insertQueryTime);
}



public static void updateAdditionalComments(GameRental esql, String ID) throws SQLException{
   Scanner scan = new Scanner(System.in);
   System.out.println("What would you like to change the Additional Comments to?");
   String newVal = scan.nextLine();
   Timestamp orderTimestamp = new Timestamp(System.currentTimeMillis());
   String insertQuery = "UPDATE TrackingInfo Set additionalComments = '" + newVal + "' where trackingID = '" + ID + "'";
   String insertQueryTime = "UPDATE TrackingInfo Set lastUpdateDate = '" + orderTimestamp + "' where trackingID = '" + ID + "'";
   esql.executeUpdate(insertQuery);
   esql.executeUpdate(insertQueryTime);
}

// getGamePrice
public static double getGamePrice(GameRental esql, String ID) throws SQLException{
   double price = 0.0;
   String query = "Select price FROM Catalog WHERE gameID = '" + ID + "'";
   List<List<String>> result = esql.executeQueryAndReturnResult(query);
   if (!result.isEmpty() && !result.get(0).isEmpty()) {
      price = Double.parseDouble(result.get(0).get(0)); 
   }
   return price;
}




}//end GameRental

