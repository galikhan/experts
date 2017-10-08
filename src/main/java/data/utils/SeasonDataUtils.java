package data.utils;

import dto.ForecastDto;
import dto.SeasonDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gali on 10/8/17.
 */
public class SeasonDataUtils {

    private static Logger log = LoggerFactory.getLogger(SeasonDataUtils.class);

    public static int save(Connection connection, SeasonDto seasonDto) {

        String query = "insert into " +
                " fx_seasons(id_, creator_, name_, chat_id_, active_, create_date_, modify_date_)" +
                " values(nextval('fx_sequence'), ?, ?, ?, true, now(), now())";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, seasonDto.creator);
            ps.setString(2, seasonDto.name);
            ps.setLong(3, seasonDto.chatId);
            return ps.executeUpdate();
        } catch (Exception e) {
            log.error("error", e);
        }
        return 0;
    }

    public static int disableAll(Connection connection, Long chatId) {

        String query = "update fx_seasons set active_ = false where chat_id_ = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setLong(1, chatId);
            return ps.executeUpdate();
        } catch (Exception e) {
            log.error("error", e);
        }
        return 0;
    }


    public static List<SeasonDto> findAllByChatId(Connection connection, Long chatId) {

        String query = "select * from fx_seasons where chat_id_ = ? order by create_date_ desc";
        List<SeasonDto> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setLong(1, chatId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new SeasonDto(rs));
            }
        } catch (Exception e) {
            log.error("error", e);
        }
        return list;
    }

    public static SeasonDto findByChatId(Connection connection, Long chatId) {

        String query = "select * from fx_seasons where chat_id_ = ? and active_ = true";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setLong(1, chatId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                return new SeasonDto(rs);
            }
        } catch (Exception e) {
            log.error("error", e);
        }
        return null;
    }
}
