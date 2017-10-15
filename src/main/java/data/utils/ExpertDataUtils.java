package data.utils;

import dto.ExpertDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gali on 9/30/17.
 */
public class ExpertDataUtils {

    private static Logger log = LoggerFactory.getLogger(ExpertDataUtils.class);

    public static List<ExpertDto> experts(Connection connection, Long leagueId, int version, String type) {

        List<ExpertDto> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("" +
                " select " +
                "   * " +
                " from " +
                "   fx_experts " +
                " where " +
                "   league_ = ? " +
                "   and version_ = ? " +
                "   and type_ = ? " +
                "   order by total_ desc, user_ desc")) {
            ps.setLong(1, leagueId);
            ps.setInt(2, version);
            ps.setString(3, type);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new ExpertDto(rs));
            }
        } catch (Exception e) {
            log.error("error", e);
        }
        return list;
    }

    public static int deleteExperts(Connection connection, Long leagueId) {

        String select = "delete from fx_experts where league_ = ?";
        try (PreparedStatement ps = connection.prepareStatement(select)) {
            ps.setLong(1, leagueId);
            return ps.executeUpdate();
        } catch (Exception e) {
            log.error("error", e);
        }
        return 0;
    }

    public static int save(Connection connection, ExpertDto expertDto) {

        String expertQuery = "" +
                    " insert into " +
                    "   fx_experts(id_, plus4_, plus2_, plus1_, total_, league_, user_, order_, scale_, version_, type_, modify_date_, create_date_) " +
                    "   values(nextval('fx_sequence'), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now(), now())";

        try (PreparedStatement ps = connection.prepareStatement(expertQuery)) {

            ps.setInt(1, expertDto.plus4);
            ps.setInt(2, expertDto.plus2);
            ps.setInt(3, expertDto.plus1);
            ps.setInt(4, expertDto.total);
            ps.setLong(5, expertDto.leagueId);
            ps.setString(6, expertDto.user);
            ps.setInt(7, expertDto.order);
            ps.setInt(8, expertDto.scale);
            ps.setInt(9, expertDto.version);
            ps.setString(10, expertDto.type);
            return ps.executeUpdate();

        } catch (SQLException e) {
            log.error("error", e);
        }
        return 0;
    }

    public static int update(Connection connection, ExpertDto expertDto) {

        String updateQuery = "" +
                " update fx_experts " +
                "   set " +
                "       plus4_ = ?, " +
                "       plus2_ = ?, " +
                "       plus1_ = ?, " +
                "       total_ = ?, " +
                "       league_ = ?, " +
                "       user_ = ?, " +
                "       order_ = ?, " +
                "       scale_ = ?, " +
                "       version_ = ?, " +
                "       type_ = ?, " +
                "       modify_date_ = now() where id_ = ? ";

        try (PreparedStatement ps = connection.prepareStatement(updateQuery)) {

            ps.setInt(1, expertDto.plus4);
            ps.setInt(2, expertDto.plus2);
            ps.setInt(3, expertDto.plus1);
            ps.setInt(4, expertDto.total);
            ps.setLong(5, expertDto.leagueId);
            ps.setString(6, expertDto.user);
            ps.setInt(7, expertDto.order);
            ps.setInt(8, expertDto.scale);
            ps.setInt(9, expertDto.version);
            ps.setString(10, expertDto.type);
            ps.setLong(11, expertDto.id);
            return ps.executeUpdate();

        } catch (SQLException e) {
            log.error("error", e);
        }
        return 0;
    }


    public static Map<String, ExpertDto> seasonTableMap(Connection connection, Long seasonId) {
        String query = "" +
                "SELECT" +
                "  user_," +
                "  sum(plus4_) AS plus4_," +
                "  sum(plus2_) AS plus2_," +
                "  sum(plus1_) AS plus1_," +
                "  sum(total_) AS total_" +
                " FROM fx_experts" +
                " WHERE " +
                "   type_ = 'SINGLE'" +
                "   and league_ IN (SELECT id_ " +
                "                  FROM fx_leagues " +
                "                  WHERE season_ = ? )" +
                " GROUP BY user_ ORDER BY 5 desc";

        log.info("query ~ {}", query);
        log.info("season id ~ {}", seasonId);
        Map<String, ExpertDto> map = new HashMap<>();
        try (PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setLong(1, seasonId);

            ResultSet rs = ps.executeQuery();
            int order = 1;
            while(rs.next()) {

                ExpertDto expert = new ExpertDto();
                expert.total = rs.getInt("total_");
                expert.plus4 = rs.getInt("plus4_");
                expert.plus2 = rs.getInt("plus2_");
                expert.plus1 = rs.getInt("plus1_");
                expert.user = rs.getString("user_");
                expert.order = order;
                map.put(expert.user, expert);

                order++;
            }

        } catch (SQLException e) {
            log.error("error", e);
        }
        return map;
    }

    public static int lastVersion(Connection connection, Long leagueId, String type) {

        int version = 0;
        try (PreparedStatement ps = connection.prepareStatement("select version_ from fx_experts where league_ = ? and type_ = ? order by version_ desc limit 1")) {
            ps.setLong(1, leagueId);
            ps.setString(2, type);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                version = rs.getInt("version_");
            }
        } catch (Exception e) {
            log.error("error", e);
        }
        return version;

    }

    public static Map<String, ExpertDto> expertMap(Connection connection, Long leagueId, int lastVersion) {
        String query = "" +
                " SELECT" +
                "   *" +
                " FROM fx_experts" +
                " WHERE " +
                "   type_ = 'GLOBAL'" +
                "   and league_ = ? " +
                "   and version_ = ? ";

        Map<String, ExpertDto> map = new HashMap<>();
        try (PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setLong(1, leagueId);
            ps.setInt(2, lastVersion);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {

                ExpertDto expert = new ExpertDto();
                expert.total = rs.getInt("total_");
                expert.plus4 = rs.getInt("plus4_");
                expert.plus2 = rs.getInt("plus2_");
                expert.plus1 = rs.getInt("plus1_");
                expert.user = rs.getString("user_");
                expert.order = rs.getInt("order_");
                expert.version = rs.getInt("version_");
                map.put(expert.user, expert);

            }

        } catch (SQLException e) {
            log.error("error", e);
        }
        return map;
    }
}
