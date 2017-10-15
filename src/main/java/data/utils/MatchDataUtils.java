package data.utils;

import dto.MatchDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Messages;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gali on 9/29/17.
 */
public class MatchDataUtils {

    private static Logger log = LoggerFactory.getLogger(MatchDataUtils.class);

    public static List<MatchDto> getByLeagueId(Connection connection, Long leagueId) {

        List<MatchDto> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("select * from fx_matches where league_ = ? order by match_id_")) {
            ps.setLong(1, leagueId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new MatchDto(rs));
            }
        } catch (Exception e) {
            log.error("error", e);
        }
        return list;
    }

    public static MatchDto getByLeagueAndMatchId(Connection connection, Long leagueId, int matchId) {

        List<MatchDto> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("" +
                " select " +
                "   * " +
                " from " +
                "   fx_matches " +
                " where " +
                "   league_ = ? " +
                "   and match_id_ = ? " +
                " order by " +
                "   match_id_")) {
            ps.setLong(1, leagueId);
            ps.setInt(2, matchId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new MatchDto(rs));
            }
        } catch (Exception e) {
            log.error("error", e);
        }
        return list.size() > 0 ? list.get(0) : null;
    }

    public static Map<Integer, MatchDto> getMapByLeagueId(Connection connection, Long leagueId) {

        Map<Integer, MatchDto> map = new HashMap<>();
        List<MatchDto> list = getByLeagueId(connection, leagueId);

        for(MatchDto m:list) {
            map.put(m.matchId, m);
        }
        return map;
    }

    public static int save(Connection connection, MatchDto matchDto) {

        String query = "update fx_matches set home_point_ = ?, guests_point_ = ?, finished_ = true, modify_date_ = now() where league_ = ? and match_id_ = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, matchDto.homePoint);
            ps.setInt(2, matchDto.guestsPoint);
            ps.setLong(3, matchDto.league);
            ps.setInt(4, matchDto.matchId);
            return     ps.executeUpdate();
        } catch (Exception e) {
            log.error("error", e);
        }
        return 0;
    }
}
