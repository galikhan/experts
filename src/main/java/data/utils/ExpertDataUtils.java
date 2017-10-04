package data.utils;

import dto.ExpertDto;
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
public class ExpertDataUtils {

    private static Logger log = LoggerFactory.getLogger(ExpertDataUtils.class);

    public static List<ExpertDto> experts(Connection connection, Long leagueId) {

        List<ExpertDto> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("select * from fx_experts where league_ = ? order by total_ desc")) {
            ps.setLong(1, leagueId);
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

}
