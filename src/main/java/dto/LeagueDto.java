package dto;

import utils.DateUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by gali on 9/16/17.
 */
public class LeagueDto extends BaseDto{

    public String name;
    public String creator;

    public LeagueDto(ResultSet rs) throws SQLException {

        this.name = rs.getString("name_");
        this.creator = rs.getString("creator_");
        this.createDate = DateUtils.fromSqlDate(rs.getDate("create_date_"));
        this.modifyDate = DateUtils.fromSqlDate(rs.getDate("modify_date_"));

    }
}
