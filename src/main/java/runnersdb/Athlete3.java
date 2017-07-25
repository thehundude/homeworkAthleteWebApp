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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class Athlete3 extends HttpServlet {
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

        Connection connection = DbUtil.getConnection();

        ArrayList<Athlete3> athletes = new ArrayList();

        try {
            // lekérjük a futók adatait plusz az első helyek számát
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT athlete.athlete_name, athlete.dob, athlete.nationality, COUNT(athlete_race.first_place) " +
                            "FROM athlete " +
                            "FULL OUTER JOIN athlete_race ON athlete_id = athlete_race.first_place " +
                            "GROUP BY athlete.athlete_name, athlete.dob, athlete.nationality " +
                            "ORDER BY athlete_name ASC");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Athlete3 athlete = new Athlete3();

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
                    "GROUP BY athlete.athlete_name, athlete.dob, athlete.nationality\n" +
                    "ORDER BY athlete_name ASC");

            rs = ps.executeQuery();

            for (Athlete3 item : athletes) {
                rs.next();
                item.secondPlace = rs.getInt(1);
            }

            // itt csak a harmadik helyek számát kérjük le
            ps = connection.prepareStatement("SELECT COUNT(athlete_race.third_place)\n" +
                    "FROM athlete\n" +
                    "FULL OUTER JOIN athlete_race ON athlete.athlete_id = athlete_race.third_place\n" +
                    "GROUP BY athlete.athlete_name, athlete.dob, athlete.nationality\n" +
                    "ORDER BY athlete_name ASC");

            rs = ps.executeQuery();

            for (Athlete3 item : athletes) {
                rs.next();
                item.thirdPlace = rs.getInt(1);
            }

            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // teljesítményt értékelő pontszám kiszámítása
        for (Athlete3 item : athletes) {
            item.performanceScore = item.firstPlace * 3 + item.secondPlace * 2 + item.thirdPlace * 3;
        }

        // összehasonlítás - stackoverflownak köszönhetően
        Collections.sort(athletes, new Comparator<Athlete3>() {
            public int compare(Athlete3 o1, Athlete3 o2) {
                return o2.getPerformanceScore() - o1.getPerformanceScore();
            }
        });

        for (int i = 0; i < athletes.size(); i++) {
            /* System.out.print(athletes.get(i).athleteName + "\t" + athletes.get(i).dob + "\t" + athletes.get(i).nationality +
                    "\t" + athletes.get(i).firstPlace + "\t" + athletes.get(i).secondPlace + "\t" +
                    athletes.get(i).thirdPlace + "\n"); */
            writer.print("<p>Futó neve: " + athletes.get(i).athleteName + "; születési idő: " + athletes.get(i).dob +
            "; nemzetisége: " + athletes.get(i).nationality + "; I. helyek:" + athletes.get(i).firstPlace + "; II. helyek: " +
            athletes.get(i).secondPlace + "; III. helyek: " + athletes.get(i).thirdPlace + "</p>");

        }

        writer.print("</body>");
        writer.print("</html>");
        writer.close();
    }
}
