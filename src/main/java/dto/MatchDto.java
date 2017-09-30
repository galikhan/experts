package dto;

import com.google.gson.JsonObject;
import utils.DateUtils;
import utils.Messages;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by gali on 9/16/17.
 */
public class MatchDto extends BaseDto {

    public String home;
    public String guests;
    public Integer homePoint;
    public Integer guestsPoint;
    public Long league;
    public Integer matchId;
    public Boolean finished = false;

    public MatchDto(ResultSet rs) throws SQLException {

        this.home = rs.getString("home_");
        this.guests = rs.getString("guests_");
        this.homePoint = rs.getInt("home_point_");
        this.guestsPoint = rs.getInt("guests_point_");
        this.league = rs.getLong("league_");
        this.matchId = rs.getInt("match_id_");
        this.finished = rs.getBoolean("finished_");
        this.createDate = DateUtils.fromSqlDate(rs.getDate("create_date_"));
        this.modifyDate = DateUtils.fromSqlDate(rs.getDate("modify_date_"));
    }

    public static String fromMatchesList(List<MatchDto> list) {
        if(list.isEmpty()) {
            return Messages.NO_MATCHES_EXIST;
        }
        StringBuilder sb = new StringBuilder();
        int counter = 1;
        for (MatchDto m : list) {
            sb.append(counter + ". " + m.home + " - " + m.guests + " result (" + m.homePoint + "-" + m.guestsPoint + ") \n");
            counter++;
        }
        return sb.toString();
    }

    public static String fromMatchesListToJson(List<MatchDto> list) {

        JsonObject json =  new JsonObject();
        if(list.isEmpty()) {
            return Messages.NO_MATCHES_EXIST;
        }

        int counter = 1;
        for (MatchDto m : list) {
            json.addProperty(counter + "", m.matchId);
            counter++;
        }
        return json.toString();
    }

}
