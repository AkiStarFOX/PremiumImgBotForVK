import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class JobChecker implements Job {
    private static final String MYSQL_URL = "jdbc:mysql://localhost:3306/users?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
    private static final String MYSQL_LOGIN = "root";
    private static final String MYSQL_PASSWORD = "root";
    private Connection connection;
    private Statement statement;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            connection = DriverManager.getConnection(MYSQL_URL, MYSQL_LOGIN, MYSQL_PASSWORD);
            statement = connection.createStatement();
            statement.execute("update freeusers set countByDay = 0");
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
