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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by tamas on 2017. 07. 23..
 */
public class Athlete2 extends HttpServlet {
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

    int tempAthId;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Connection connection = DbUtil.getConnection();

        PreparedStatement ps = null;
        ResultSet rs = null;
        Athlete2 modifyAthlete = new Athlete2();

        String modifyAthleteTrainerName = null;
        String modifyAthleteOrgName = null;
        String modifyAthleteName = new String(req.getParameter("athleteinput").getBytes("ISO-8859-1"), "UTF-8");

        try {
            ps = connection.prepareStatement("SELECT * FROM athlete WHERE athlete_name = '" + modifyAthleteName + "'");
            rs = ps.executeQuery();
            rs.next();
            modifyAthlete.setAthleteId(rs.getInt(1));
            modifyAthlete.setTrainerId(rs.getInt(2));
            modifyAthlete.setOrgId(rs.getInt(3));
            modifyAthlete.setAthleteName(rs.getString(4));
            modifyAthlete.setDob(rs.getDate(5));
            modifyAthlete.setNationality(rs.getString(6));
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        try {
            ps = connection.prepareStatement("SELECT org_name FROM organization WHERE org_id = '" + modifyAthlete.orgId + "'");
            rs = ps.executeQuery();
            rs.next();
            modifyAthleteOrgName = rs.getString(1);
            ps = connection.prepareStatement("SELECT trainer_name FROM trainer WHERE trainer_id = '" + modifyAthlete.trainerId + "'");
            rs = ps.executeQuery();
            rs.next();
            modifyAthleteTrainerName = rs.getString(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        PrintWriter writer = resp.getWriter();
        resp.setContentType("text/html");
        writer.print("<html>");
        writer.print("<body>");
        writer.print("<form action=\"/clubathlete2\" method=\"post\">\n" +
                "    <p>Futó neve</p>\n" +
                "    <input type=\"text\" name=\"athletename\" value=\"" + modifyAthlete.athleteName + "\">\n" +
                "    <p>Születési idő</p>\n" +
                "    <input type=\"date\" name=\"dob\" value=\"" + modifyAthlete.dob + "\">\n" +
                "    <p>Nemzetiség</p>\n" +
                "    <input type=\"text\" name=\"nationality\" value=\"" + modifyAthlete.nationality + "\">\n" +
                "    <p>Klub</p>\n" +
                "    <input type=\"text\" name=\"club\" value=\"" + modifyAthleteOrgName + "\">\n" +
                "    <p>Edző</p>\n" +
                "    <input type=\"text\" name=\"trainer\" value=\"" + modifyAthleteTrainerName + "\">\n" +
                "    </br></br>\n" +
                "    <input type=\"submit\">\n" +
                "</form>");

        writer.print("</body>");
        writer.print("</html>");
        writer.close();

        tempAthId = modifyAthlete.athleteId;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Connection connection = DbUtil.getConnection();
        Athlete2 modifyAthlete = new Athlete2();
        PreparedStatement ps = null;
        ResultSet rs = null;

        modifyAthlete.athleteName = new String(req.getParameter("athletename").getBytes("ISO-8859-1"), "UTF-8");

        String dobString = req.getParameter("dob");
        DateFormat df = new SimpleDateFormat("yyyy-mm-dd");
        try {
            modifyAthlete.dob = df.parse(dobString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        modifyAthlete.nationality = req.getParameter("nationality");

        String modifyAthleteOrg = req.getParameter("club");
        try {
            ps = connection.prepareStatement("SELECT org_id FROM organization WHERE org_name = '" + modifyAthleteOrg + "'");
            rs = ps.executeQuery();
            rs.next();
            modifyAthlete.orgId = rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String modifyAthleteTrainer = req.getParameter("trainer");
        try {
            ps = connection.prepareStatement("SELECT trainer_id FROM trainer WHERE trainer_name = '" + modifyAthleteTrainer + "'");
            rs = ps.executeQuery();
            rs.next();
            modifyAthlete.trainerId = rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            ps = connection.prepareStatement("UPDATE athlete SET trainer_id = '" + modifyAthlete.trainerId +
                    "', org_id = '" + modifyAthlete.orgId + "', athlete_name = '" + modifyAthlete.athleteName +
                    "', dob = '" + modifyAthlete.dob + "', nationality = '" + modifyAthlete.nationality + "' WHERE " +
                    "athlete_id = " + tempAthId);
            ps.executeUpdate();

            PrintWriter writer = resp.getWriter();
            resp.setContentType("text/html");
            writer.print("<html>");
            writer.print("<body>");
            writer.print("<a href=\"index.html\">Vissza a főoldalra</a>");
            writer.print("</body>");
            writer.print("</html>");
            writer.close();

            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
