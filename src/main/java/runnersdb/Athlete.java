package runnersdb;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by tamas on 2017. 07. 22..
 */
public class Athlete extends HttpServlet {
    private int athleteId;
    private int trainerId;
    private int orgId;
    private String athleteName;
    private Date dob;
    private String nationality;

    private int firstPlace;
    private int secondPlace;
    private int thirdPlace;

    private int performanceScore;

    public int getAthleteId() {
        return athleteId;
    }

    public void setAthleteId(int athleteId) {
        this.athleteId = athleteId;
    }

    public int getTrainerId() {
        return trainerId;
    }

    public void setTrainerId(int trainerId) {
        this.trainerId = trainerId;
    }

    public int getOrgId() {
        return orgId;
    }

    public void setOrgId(int orgId) {
        this.orgId = orgId;
    }

    public String getAthleteName() {
        return athleteName;
    }

    public void setAthleteName(String athleteName) {
        this.athleteName = athleteName;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public int getFirstPlace() {
        return firstPlace;
    }

    public void setFirstPlace(int firstPlace) {
        this.firstPlace = firstPlace;
    }

    public int getSecondPlace() {
        return secondPlace;
    }

    public void setSecondPlace(int secondPlace) {
        this.secondPlace = secondPlace;
    }

    public int getThirdPlace() {
        return thirdPlace;
    }

    public void setThirdPlace(int thirdPlace) {
        this.thirdPlace = thirdPlace;
    }

    public int getPerformanceScore() {
        return performanceScore;
    }

    public void setPerformanceScore(int performanceScore) {
        this.performanceScore = performanceScore;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter writer = resp.getWriter();
        resp.setContentType("text/html");
        writer.print("<html>");
        writer.print("<body>");
        writer.print("A klub futói");

        // adatbázis lekérés
        Connection connection = DbUtil.getConnection();

        ArrayList<Athlete> athletes = new ArrayList();

        try {
            // lekérjük a futók adatait plusz az első helyek számát
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT athlete.athlete_name, athlete.dob, athlete.nationality, COUNT(athlete_race.first_place) " +
                            "FROM athlete " +
                            "FULL OUTER JOIN athlete_race ON athlete_id = athlete_race.first_place " +
                            "WHERE org_id = 1 " +
                            "GROUP BY athlete.athlete_name, athlete.dob, athlete.nationality " +
                            "ORDER BY athlete_name ASC");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Athlete athlete = new Athlete();

                athlete.setAthleteName(rs.getString(1));
                athlete.setDob(rs.getDate(2));
                athlete.setNationality(rs.getString(3));
                athlete.setFirstPlace(rs.getInt(4));

                athletes.add(athlete);
            }

            // itt csak a második helyek számát kérjük le
            ps = connection.prepareStatement("SELECT COUNT(athlete_race.second_place)\n" +
                    "FROM athlete\n" +
                    "FULL OUTER JOIN athlete_race ON athlete.athlete_id = athlete_race.second_place\n" +
                    "WHERE org_id = 1\n" +
                    "GROUP BY athlete.athlete_name, athlete.dob, athlete.nationality\n" +
                    "ORDER BY athlete_name ASC");

            rs = ps.executeQuery();

            for (Athlete item : athletes) {
                rs.next();
                item.secondPlace = rs.getInt(1);
            }

            // itt csak a harmadik helyek számát kérjük le
            ps = connection.prepareStatement("SELECT COUNT(athlete_race.third_place)\n" +
                    "FROM athlete\n" +
                    "FULL OUTER JOIN athlete_race ON athlete.athlete_id = athlete_race.third_place\n" +
                    "WHERE org_id = 1\n" +
                    "GROUP BY athlete.athlete_name, athlete.dob, athlete.nationality\n" +
                    "ORDER BY athlete_name ASC");

            rs = ps.executeQuery();

            for (Athlete item : athletes) {
                rs.next();
                item.thirdPlace = rs.getInt(1);
            }

            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        for (Athlete item: athletes) {
            writer.print("<p>" + item.athleteName + ", született: " + item.dob + ", nemzetisége: " + item.nationality +
            ", első helyek száma: " + item.firstPlace + ", második helyek száma: " + item.secondPlace + ", harmadik helyek száma:" +
            item.thirdPlace + "</p></br>");
        }

        // html lezárás
        writer.print("</body>");
        writer.print("</html>");
        writer.close();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Connection connection = DbUtil.getConnection();

        int athleteOrgId = 0;
        PreparedStatement ps = null;
        String param = new String(req.getParameter("club").getBytes("ISO-8859-1"), "UTF-8");
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement("SELECT org_id FROM organization WHERE org_name = '" + param + "'");
            rs = ps.executeQuery();
            rs.next();
            athleteOrgId = rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int athleteTrainerId = 0;
        String param2 = new String(req.getParameter("trainer").getBytes("ISO-8859-1"), "UTF-8");
        try {
            ps = connection.prepareStatement("SELECT trainer_id FROM trainer WHERE trainer_name = '" + param2 + "'");
            rs = ps.executeQuery();
            rs.next();
            athleteTrainerId = rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            ps = connection.prepareStatement("INSERT INTO athlete (athlete_name, dob, nationality, org_id, trainer_id)\n" +
                    "VALUES ('" + req.getParameter("athletename") + "', '" + req.getParameter("dob") + "', '" +
                    req.getParameter("nationality") + "', '" + Integer.toString(athleteOrgId) +
                    "', '" + Integer.toString(athleteTrainerId) + "')");
            ps.executeUpdate();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        PrintWriter writer = resp.getWriter();
        resp.setContentType("text/html");
        writer.print("<html>");
        writer.print("<body>");
        writer.print("<a href=\"index.html\">Vissza a főoldalra</a>");
        writer.print("</body>");
        writer.print("</html>");
        writer.close();
    }
}
