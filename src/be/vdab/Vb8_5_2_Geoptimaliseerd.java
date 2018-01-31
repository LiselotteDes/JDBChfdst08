package be.vdab;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.Scanner;
/*
Voorbeeld: Performantere oplossing, die meestal maar één SQL statement naar de database stuurt.
*/
public class Vb8_5_2_Geoptimaliseerd {
    private static final String URL = "jdbc:mysql://localhost/tuincentrum?useSSL=false";
    private static final String USER = "cursist";
    private static final String PASSWORD = "cursist";
    /*
    Dit statement zal de verkoopprijs van een plant wijzigen, 
    op voorwaarde dat een parameter kleiner of gelijk is aan de verkoopprijs * 1.1.
    Je zal deze parameter invullen met de nieuwe prijs.
    Het update statement zal tijdens zijn uitvoering de plant dus enkel aanpassen als nieuwe prijs maximaal 10% hoger is dan de huidige prijs.
    */
    private static final String UPDATE_PRIJS = "update planten set verkoopprijs = ? where id=? and ? <= verkoopprijs*1.1";
    private static final String SELECT_ID = "select id from planten where id=?";
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Id: ");
            int id = scanner.nextInt();
            System.out.print("Verkoopprijs: ");
            BigDecimal nieuwePrijs = scanner.nextBigDecimal();
            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                    PreparedStatement statementUpdate = connection.prepareStatement(UPDATE_PRIJS)) {
                statementUpdate.setBigDecimal(1, nieuwePrijs);
                statementUpdate.setInt(2, id);
                statementUpdate.setBigDecimal(3, nieuwePrijs);
                connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
                connection.setAutoCommit(false);
                /*
                Voert het update statement uit.
                Vraagt daarna het aantal aangepaste recordt.
                Als dit 1 is (meestal het geval), moet je geen ander statement naar de database sturen.
                Als dit 0 is (zelden het geval), zijn er twee mogelijkheiden:
                    a. De db bevat geen plant met het ingetikte plantnummer.
                    b. De nieuwe verkoopprijs is te hoog.
                */
                if (statementUpdate.executeUpdate() == 0) {
                    try (PreparedStatement statementSelect = connection.prepareStatement(SELECT_ID)) {
                        statementSelect.setInt(1, id);
                        // Zoekt de plant met het ingetikte plantnummer
                        try (ResultSet resultSet = statementSelect.executeQuery()) {
                            /*
                            Als de plant gevonden wordt, was de nieuwe verkoopprijs te hoog.
                            Als de plant niet gevonden wordt, bestond de plant niet.
                            */
                            System.out.println(resultSet.next() ? "Nieuwe verkoopprijs te hoog" : "Plant niet gevonden");
                        }
                    }
                }
                connection.commit();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }/*
        Een Scanner gooit een InputMismatchException op om aan te geven dat het symbool dat hij ontving
        niet overeenkomt met het patroon voor het verwachtte type.
        */
        catch (InputMismatchException ex) {
            System.out.println("Verkeerde invoer");
        }
    }
    
}
