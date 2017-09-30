package dto;

import utils.DateUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by gali on 9/27/17.
 */
public class ConversationDto extends BaseDto{

    public Long id;
    public Long chatId;
    public Long leagueId;
    public String username;
    public String request;
    public String response;
    public String type;
    public Boolean hasAnswer;
    public Boolean removed;
    public Boolean active;


    public ConversationDto(ResultSet rs) throws SQLException {

        this.id = rs.getLong("id_");
        this.chatId = rs.getLong("chat_id_");
        this.leagueId = rs.getLong("league_");
        this.username = rs.getString("user_");
        this.request = rs.getString("request_");
        this.response = rs.getString("response_");
        this.type = rs.getString("type_");
        this.hasAnswer = rs.getBoolean("has_answer_");
        this.removed = rs.getBoolean("removed_");
        this.active = rs.getBoolean("active_");
        createDate = DateUtils.fromSqlDate(rs.getDate("create_date_"));
        modifyDate = DateUtils.fromSqlDate(rs.getDate("modify_date_"));

    }
}
