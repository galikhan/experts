package dto;

import com.google.gson.JsonObject;
import utils.DateUtils;
import utils.Messages;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Created by gali on 9/16/17.
 */
public class LeagueDto extends BaseDto{

    public String name;
    public String creator;
    public String desc;
    public Long id;

    public LeagueDto(ResultSet rs) throws SQLException {

        this.id = rs.getLong("id_");
        this.name = rs.getString("name_");
        this.creator = rs.getString("creator_");
        this.desc = rs.getString("desc_");
        this.createDate = DateUtils.fromSqlDate(rs.getDate("create_date_"));
        this.modifyDate = DateUtils.fromSqlDate(rs.getDate("modify_date_"));

    }

    public static JsonObject fromLeagueListToJson(List<LeagueDto> list) {

        JsonObject json = new JsonObject();
        int counter = 1;
        for (LeagueDto l : list) {
            json.addProperty(String.valueOf(counter), l.id);
            counter++;
        }
        return json;
    }


    public static String fromLeagueList(List<LeagueDto> list) {

        if (list.isEmpty()) {
            return Messages.NO_LEAGUES_FOR_THIS_CHAT;
        } else {
            StringBuilder sb = new StringBuilder();
            int counter = 1;
            for (LeagueDto l : list) {
                sb.append("/"+counter + ". " + l.name + (l.desc==null? "" : " (" + l.desc + ")") + "\n");
                counter++;
            }
            return sb.toString();
        }
    }
}
