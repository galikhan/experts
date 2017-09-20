package dto;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by gali on 9/16/17.
 */
public class ExpertDto extends BaseDto {

    public Long id;
    public String league;
    public String user;
    public Integer plus4 = 0;
    public Integer plus2 = 0;
    public Integer plus1 = 0;
    public Integer total = 0;

    public ExpertDto() {
    }

    public ExpertDto(ResultSet rs) throws SQLException {

        this.id = rs.getLong("id_");
        this.league = rs.getString("league_");
        this.user = rs.getString("user_");
        this.plus4 = rs.getInt("plus4_");
        this.plus2 = rs.getInt("plus2_");
        this.plus1 = rs.getInt("plus1_");
        this.total = rs.getInt("total_");

    }

}
