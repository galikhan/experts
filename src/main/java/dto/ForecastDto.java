package dto;

import utils.DateUtils;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by gali on 9/16/17.
 */
public class ForecastDto extends BaseDto {

    public Integer homePoint;
    public Integer guestPoint;
    public Long id;
    public String league;
    public String user;
    public Integer matchId;

    public ForecastDto() {
    }

    public ForecastDto(ResultSet rs) throws SQLException {

        this.id = rs.getLong("id_");
        this.user = rs.getString("user_");
        this.homePoint = rs.getInt("home_point_");
        this.guestPoint = rs.getInt("guests_point_");
        this.league = rs.getString("league_");
        this.matchId = rs.getInt("match_id_");
        this.createDate = DateUtils.fromSqlDate(rs.getDate("create_date_"));
        this.modifyDate = DateUtils.fromSqlDate(rs.getDate("modify_date_"));

    }
}
