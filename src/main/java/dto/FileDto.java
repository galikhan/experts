package dto;

import utils.DateUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by gali on 10/1/17.
 */
public class FileDto extends BaseDto{

    public Long id;
    public String type;
    public Long leagueId;
    public Long chatId;
    public String name;
    public String htmlName;
    public String path;
    public Boolean active;

    public FileDto() {
    }

    public FileDto(ResultSet rs) throws SQLException {
        this.id = rs.getLong("id_");
        this.leagueId = rs.getLong("league_");
        this.chatId = rs.getLong("chat_id_");
        this.type = rs.getString("type_");
        this.name = rs.getString("name_");
        this.htmlName = rs.getString("html_name_");
        this.path = rs.getString("path_");
        this.active = rs.getBoolean("active_");
        createDate = DateUtils.fromSqlDate(rs.getDate("create_date_"));
        modifyDate = DateUtils.fromSqlDate(rs.getDate("modify_date_"));
    }

}
