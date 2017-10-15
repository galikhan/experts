package dto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gali on 9/16/17.
 */
public class ExpertDto extends BaseDto {

    public Long id;
    public Long leagueId;
    public String user;
    public String type;
    public Integer plus4 = 0;
    public Integer plus2 = 0;
    public Integer plus1 = 0;
    public Integer order = 0;
    public Integer scale = 0;
    public Integer total = 0;
    public Integer version;

    public ExpertDto() {
    }

    public ExpertDto(ResultSet rs) throws SQLException {

        this.id = rs.getLong("id_");
        this.leagueId = rs.getLong("league_");
        this.user = rs.getString("user_");
        this.type = rs.getString("type_");
        this.plus4 = rs.getInt("plus4_");
        this.plus2 = rs.getInt("plus2_");
        this.plus1 = rs.getInt("plus1_");
        this.total = rs.getInt("total_");
        this.order = rs.getInt("order_");
        this.scale = rs.getInt("scale_");
        this.version = rs.getInt("version_");

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

    public static Map<String, ExpertDto> fromExpertsListToMap(List<ExpertDto> list) {

        Map<String, ExpertDto> map = new HashMap<>();
        for (ExpertDto expert : list) {
            map.put(expert.user, expert);
        }
        return map;
    }
}
