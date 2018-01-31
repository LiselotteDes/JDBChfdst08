package be.vdab;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
public class Vb8_3 {
    private static final String URL = "jdbc:mysql://localhost/tuincentrum?useSSL=false";
    private static final String USER = "cursist";
    private static final String PASSWORD = "cursist";
    private static final String UPDATE_PRIJS_10_PROCENT = 
            "update planten set verkoopprijs=verkoopprijs*1.1 where verkoopprijs >=100";
    private static final String UPDATE_PRIJS_5_PROCENT = 
            "update planten set verkoopprijs=verkoopprijs*1.05 where verkoopprijs <100";
    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                Statement statement = connection.createStatement()) {
            /* (*)
            Zet de autocommit mode af. 
            Alle SQL statements die je vanaf nu op deze Connection uitvoert behoren tot één transactie.
            Dit geldt voor SQL statements die je uitvoert met een Statement, PreparedStatement en CallableStatement.
            */
            connection.setAutoCommit(false);
            // De database voert volgend statement uit binnen de transactie die je startte bij (*)
            statement.executeUpdate(UPDATE_PRIJS_10_PROCENT);
            // De db voert ook volgend statement uit binnen dezelfde transactie die je startte bij (*)
            statement.executeUpdate(UPDATE_PRIJS_5_PROCENT);
            /*
            Nadat je beide statements kan uitvoeren, doe je een commit.
            De db legt dan alle bewerkingen die de SQL statements uivoerden in de db vast.
            Als je deze regel niet uitvoert, wegens stroompanne of een exception, doet de db automatisch een rollback:
            de db maakt de bewerkingen die de SQL statements uitvoerden ongedaan.
            */
            connection.commit();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
}
