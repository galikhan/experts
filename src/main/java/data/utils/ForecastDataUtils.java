package data.utils;

import dto.ForecastDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gali on 9/30/17.
 */
public class ForecastDataUtils {

    private static Logger log = LoggerFactory.getLogger(MatchDataUtils.class);

    public static int deleteForecast(Connection connection, String username, Long leagueId) {

        try (PreparedStatement ps = connection.prepareStatement("" +
                "delete from fx_forecasts where league_ = ? and user_ = ?")) {
            ps.setLong(1, leagueId);
            ps.setString(2, username);
            return ps.executeUpdate();

        } catch (Exception e) {
            log.error("error", e);
        }
        return 0;
    }

    public static int save(Connection connection, String username, int matchId, int homePoint, int guestsPoint, Long leagueId) {
        String query = "insert into fx_forecasts(id_, user_, league_, match_id_, home_point_, guests_point_, create_date_, modify_date_) values(nextval('fx_sequence'), ?, ?, ? ,?, ?, now(), now())";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, username);
            ps.setLong(2, leagueId);
            ps.setInt(3, matchId);
            ps.setInt(4, homePoint);
            ps.setInt(5, guestsPoint);
            return ps.executeUpdate();
        } catch (Exception e) {
            log.error("error", e);
        }
        return 0;
    }

    public static List<ForecastDto> getForecasts(Connection connection, String username, Long leagueId) {

        List<ForecastDto> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("select * from fx_forecasts where user_ = ? and league_ = ? order by match_id_")) {
            ps.setString(1, username);
            ps.setLong(2, leagueId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new ForecastDto(rs));
            }
        } catch (Exception e) {
            log.error("error", e);
        }
        return list;
    }

    public static List<ForecastDto> getForecastsByMatchAndLeague(Connection connection, int matchId, Long leagueId) {

        List<ForecastDto> list = new ArrayList<>();
        String query = "select * from fx_forecasts where league_ = ? and match_id_ = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setLong(1, leagueId);
            ps.setInt(2, matchId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ForecastDto(rs));
                }
            }
        } catch (Exception e) {
            log.error("error", e);
        }
        return list;
    }

}
