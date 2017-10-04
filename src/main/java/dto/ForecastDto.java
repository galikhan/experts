package dto;

import utils.DateUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gali on 9/16/17.
 */
public class ForecastDto extends BaseDto {

    public Integer homePoint;
    public Integer guestsPoint;
    public Long id;
    public Long leagueId;
    public String user;
    public Integer matchId;

    public ForecastDto() {
    }

    public ForecastDto(ResultSet rs) throws SQLException {

        this.id = rs.getLong("id_");
        this.user = rs.getString("user_");
        this.homePoint = rs.getInt("home_point_");
        this.guestsPoint = rs.getInt("guests_point_");
        this.leagueId = rs.getLong("league_");
        this.matchId = rs.getInt("match_id_");
        this.createDate = DateUtils.fromSqlDate(rs.getDate("create_date_"));
        this.modifyDate = DateUtils.fromSqlDate(rs.getDate("modify_date_"));

    }

    public static String fromForecastList(List<ForecastDto> list, Map<Integer, MatchDto> matchMap, String username) {

        Map<Integer, Boolean> unusedMap = new HashMap<>();
        for (Map.Entry<Integer, MatchDto> entry : matchMap.entrySet()) {
            unusedMap.put(entry.getKey(), true);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Прогноз от " + username + "\n");
        for (ForecastDto f : list) {

            MatchDto match = matchMap.get(f.matchId);
            if (match != null) {
                sb.append(f.matchId + ". " + match.home + " " + f.homePoint + "-" + f.guestsPoint + " " + match.guests + "\n");
            } else {
                sb.append(f.matchId + ". Нет матча с таким номером\n");
            }

            if (unusedMap.get(f.matchId) != null) {
                unusedMap.put(f.matchId, false);
            }
        }

        for (Map.Entry<Integer, MatchDto> entry : matchMap.entrySet()) {
            MatchDto match = matchMap.get(entry.getKey());
            if (unusedMap.get(entry.getKey())) {
                sb.append(match.matchId + ". " + match.home + " ? - ? " + match.guests + "\n");
            }
        }

        return sb.toString();
    }

    public static Map<Integer, ForecastDto> fromForecastListToMap(List<ForecastDto> list) {

        Map<Integer, ForecastDto> map = new HashMap<>();

        for (ForecastDto f : list) {
            map.put(f.matchId, f);
        }
        return map;
    }
}