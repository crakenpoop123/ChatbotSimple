package chatbot.simple;

/**
 * Class to test the Chatbot
 *
 * @author Clark Gerrard
 * @version 14/05/26
 */

// Imports
import java.io.IOException;
import java.io.Console;

// SQL
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Array;

public class TesterClass
{
    
    String currChars;
    String predictedChars;
    
    public void testChatbot(int predictLength, String prompt) throws IOException
    {
        

        
        // Set the initial currChars
        if (prompt.length() >= predictLength)
        {
            currChars = prompt.substring(prompt.length()-predictLength, prompt.length());
        } else
        {
            currChars = prompt.substring(0, prompt.length());
        }
        
        // Print the orginal prompt. Makes the output look cleaner
        System.out.print(prompt);
        


    }
    
    public void readDatabase()
    {

        Console console = System.console();

        if (console == null)
        {
            System.out.println("Failed. Please provide a valid console");
        }
        
        char[] password = console.readPassword("enter password for user postgres: ");

        // SQL reading
        String url = "jdbc:postgresql://localhost:5432/Model";

        System.out.println("after url");


        String sql = "INSERT INTO weights(key,value) VALUES(?,?)" + 
        "ON CONFLICT (key) DO UPDATE SET value = EXCLUDED.value";
        
        System.out.println("in .forEach()");

        System.out.println("sql: " + sql);

        try (Connection conn = DriverManager.getConnection(url, "postgres", new String(password));
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

        } catch (SQLException e)
        {
            System.err.println(e.getMessage());
            System.out.println("in catch");
        }


    }

}