package data.utils;

import dto.ExpertDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    public static int save(Connection connection, ExpertDto expertDto) {

        String expertQuery;
        if (expertDto.id != null) {
            expertQuery = "" +
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
                    "       modify_date_ = now() ";
        } else {
            expertQuery = "" +
                    " insert into " +
                    "   fx_experts(id_, plus4_, plus2_, plus1_, total_, league_, user_, order_, scale_, modify_date_, create_date_) " +
                    "   values(nextval('fx_sequence'), ?, ?, ?, ?, ?, ?, now(), now())";
        }
        log.info("query {}", expertQuery);

        try (PreparedStatement ps = connection.prepareStatement(expertQuery)) {

            ps.setInt(1, expertDto.plus4);
            ps.setInt(2, expertDto.plus2);
            ps.setInt(3, expertDto.plus1);
            ps.setInt(4, expertDto.total);
            ps.setLong(5, expertDto.leagueId);
            ps.setString(6, expertDto.user);
            ps.setInt(7, expertDto.order);
            ps.setInt(8, expertDto.scale);
            ps.executeUpdate();

        } catch (SQLException e) {
            log.error("error", e);
        }
        return 0;
    }

}
