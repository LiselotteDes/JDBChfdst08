package be.vdab;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.Scanner;
public class Vb8_5_1 {
    private static final String URL = "jdbc:mysql://localhost/tuincentrum?useSSL=false";
    private static final String USER = "cursist";
    private static final String PASSWORD = "cursist";
    private static final String SELECT_PRIJS = "select verkoopprijs from planten where id = ?";
    private static final String UPDATE_PRIJS = "update planten set verkoopprijs = ? where id = ?";
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Id: ");
            int id = scanner.nextInt();
            System.out.print("Verkoopprijs: ");
            BigDecimal nieuwePrijs = scanner.nextBigDecimal();
            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                    PreparedStatement statementSelect = connection.prepareStatement(SELECT_PRIJS);
                    PreparedStatement statementUpdate = connection.prepareStatement(UPDATE_PRIJS)) {
                statementSelect.setInt(1, id);
                /*
                Stelt het transaction isolation level in op Serializable
                De class Connection bevat een constante per isolation level.
                */
                connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
                // Start de transactie
                connection.setAutoCommit(false);
                /*
                Leest de plant.
                Als je die vindt, vergrendelt de db die tot het einde van de transactie.
                Op die manier kan niemand ander de verkoopprijs wijzigen terwijl jij de verkoopprijs wijzigt.
                De db doet deze vergrendeling omdat het isolation level op Serializable staat.
                */
                try (ResultSet resultSet = statementSelect.executeQuery()) {
                    if (resultSet.next()) {
                        BigDecimal oudePrijs = resultSet.getBigDecimal("verkoopprijs");
                        // Controleert of de nieuwe prijs maximaal 10% boven de oude prijs ligt
                        if (nieuwePrijs.compareTo(oudePrijs.multiply(BigDecimal.valueOf(1.1))) <= 0) {
                            statementUpdate.setBigDecimal(1, nieuwePrijs);
                            statementUpdate.setInt(2, id);
                            // Wijzigt de verkoopprijs in de database.
                            statementUpdate.executeUpdate();
                            connection.commit();
                        } else {
                            System.out.println("Nieuwe verkoopprijs te hoog");
                        }
                    } else {
                        System.out.println("Plant niet gevonden");
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } catch (InputMismatchException ex) {
            System.out.println("verkeerde invoer");
        }
    }
    
}
