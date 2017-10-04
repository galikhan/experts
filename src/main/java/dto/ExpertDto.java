package dto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by gali on 9/16/17.
 */
public class ExpertDto extends BaseDto {

    public Long id;
    public Long leagueId;
    public String user;
    public Integer plus4 = 0;
    public Integer plus2 = 0;
    public Integer plus1 = 0;
    public Integer total = 0;

    public ExpertDto() {
    }

    public ExpertDto(ResultSet rs) throws SQLException {

        this.id = rs.getLong("id_");
        this.leagueId = rs.getLong("league_");
        this.user = rs.getString("user_");
        this.plus4 = rs.getInt("plus4_");
        this.plus2 = rs.getInt("plus2_");
        this.plus1 = rs.getInt("plus1_");
        this.total = rs.getInt("total_");

    }

    public static String fromExpertsList(List<ExpertDto> list) {
        StringBuilder sb = new StringBuilder();
        int counter = 1;
        sb.append("#. Эксперт | Результат | Разница | Общий \n");
        for (ExpertDto m : list) {
            sb.append(counter + ". " + m.user + " | " + m.plus4 + " | " + m.plus2 + " | " + m.plus1 + " | " + m.total + "\n");
            counter++;
        }
        return sb.toString();
    }
}
