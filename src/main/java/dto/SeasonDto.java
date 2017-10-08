package dto;

import utils.DateUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gali on 10/8/17.
 */
public class SeasonDto extends BaseDto {

    public Long id;
    public String name;
    public String creator;
    public Long chatId;
    public Boolean active;

    public SeasonDto() {
    }

    public SeasonDto(ResultSet rs) throws SQLException {

        this.id = rs.getLong("id_");
        this.creator = rs.getString("creator_");
        this.name = rs.getString("name_");
        this.chatId = rs.getLong("chat_id_");
        this.active = rs.getBoolean("active_");
        this.createDate = DateUtils.fromSqlDate(rs.getDate("create_date_"));
        this.modifyDate = DateUtils.fromSqlDate(rs.getDate("modify_date_"));
    }


    public static String fromSeasonListToString(List<SeasonDto> seasons) {
        StringBuilder sb = new StringBuilder();
        int order = 1;
        for (SeasonDto season : seasons) {
            sb.append(order);
            sb.append(". ");
            sb.append(season.name);
            sb.append(" ");
            sb.append(season.creator);
            sb.append(season.active ? " активный" : "");
            sb.append("\n");
            order++;
        }
        return sb.toString();
    }
}
