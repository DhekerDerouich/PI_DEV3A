package Finance.dao;

import Finance.model.MonthlySummary;
import util.Myconnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StatsDAO {

    public List<MonthlySummary> getMonthlySummaryLastMonths(int months) throws SQLException {
        List<MonthlySummary> summaries = new ArrayList<>();
        String sql = "SELECT \n" +
                "    DATE_FORMAT(dates.month_date, '%Y-%m') AS month,\n" +
                "    IFNULL(r.totalRevenue, 0) AS totalRevenue,\n" +
                "    IFNULL(d.totalExpense, 0) AS totalExpense,\n" +
                "    IFNULL(r.totalRevenue, 0) - IFNULL(d.totalExpense, 0) AS profit\n" +
                "FROM (\n" +
                "    SELECT DISTINCT DATE_FORMAT(dateRevenu, '%Y-%m-01') AS month_date\n" +
                "    FROM revenu\n" +
                "    WHERE dateRevenu >= DATE_SUB(CURDATE(), INTERVAL ? MONTH)\n" +
                ") dates\n" +
                "LEFT JOIN (\n" +
                "    SELECT \n" +
                "        DATE_FORMAT(dateRevenu, '%Y-%m') AS month,\n" +
                "        SUM(montant) AS totalRevenue\n" +
                "    FROM revenu\n" +
                "    WHERE dateRevenu >= DATE_SUB(CURDATE(), INTERVAL ? MONTH)\n" +
                "    GROUP BY month\n" +
                ") r ON DATE_FORMAT(dates.month_date, '%Y-%m') = r.month\n" +
                "LEFT JOIN (\n" +
                "    SELECT \n" +
                "        DATE_FORMAT(dateDepense, '%Y-%m') AS month,\n" +
                "        SUM(montant) AS totalExpense\n" +
                "    FROM depense\n" +
                "    WHERE dateDepense >= DATE_SUB(CURDATE(), INTERVAL ? MONTH)\n" +
                "    GROUP BY month\n" +
                ") d ON DATE_FORMAT(dates.month_date, '%Y-%m') = d.month\n" +
                "ORDER BY month ASC;";

        try (Connection conn = Myconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, months);
            ps.setInt(2, months);
            ps.setInt(3, months);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                MonthlySummary ms = new MonthlySummary();
                ms.setMonth(rs.getString("month"));
                ms.setTotalRevenue(rs.getDouble("totalRevenue"));
                ms.setTotalExpense(rs.getDouble("totalExpense"));
                ms.setProfit(rs.getDouble("profit"));
                summaries.add(ms);
            }
        }
        return summaries;
    }
}