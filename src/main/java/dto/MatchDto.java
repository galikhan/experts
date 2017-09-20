package dto;

import utils.DateUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by gali on 9/16/17.
 */
public class MatchDto extends BaseDto {

    public String home;
    public String guests;
    public Integer homePoint;
    public Integer guestsPoint;
    public String league;
    public Integer matchId;

    public MatchDto(ResultSet rs) throws SQLException {

        this.home = rs.getString("home_");
        this.guests = rs.getString("guests_");
        this.homePoint = rs.getInt("home_point_");
        this.guestsPoint = rs.getInt("guests_point_");
        this.league = rs.getString("league_");
        this.matchId = rs.getInt("match_id_");
        this.createDate = DateUtils.fromSqlDate(rs.getDate("create_date_"));
        this.modifyDate = DateUtils.fromSqlDate(rs.getDate("modify_date_"));


    }
}
