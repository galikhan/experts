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
    public Long chatId;
    public boolean groupChat;
    public Long season;

    public LeagueDto() {
    }

    public LeagueDto(ResultSet rs) throws SQLException {

        this.id = rs.getLong("id_");
        this.name = rs.getString("name_");
        this.creator = rs.getString("creator_");
        this.desc = rs.getString("desc_");
        this.chatId = rs.getLong("chat_id_");
        this.groupChat = rs.getBoolean("group_chat_");
        this.season = rs.getLong("season_");
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


    public static String fromLeagueList(String prefix, List<LeagueDto> list) {

        if (list.isEmpty()) {
            return Messages.NO_LEAGUES_FOR_THIS_CHAT;
        } else {
            StringBuilder sb = new StringBuilder();
            int counter = 1;
            for (LeagueDto l : list) {
                sb.append(prefix+counter + ". " + l.name + (l.desc==null? "" : " (" + l.desc + ")") + "\n");
                counter++;
            }
            return sb.toString();
        }
    }
}
