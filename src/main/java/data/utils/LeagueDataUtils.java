package data.utils;

import dto.LeagueDto;
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
public class LeagueDataUtils {

    private static Logger log = LoggerFactory.getLogger(MatchDataUtils.class);

    public static LeagueDto findById(Connection connection, Long leagueId) {
        try (PreparedStatement ps = connection.prepareStatement("select * from fx_leagues where id_ = ?")) {
            ps.setLong(1, leagueId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                return new LeagueDto(rs);
            }
        } catch (Exception e) {
            log.error("error", e);
        }
        return null;
    }

    public static LeagueDto findByName(Connection connection, String name, Long chatId) {
        try (PreparedStatement ps = connection.prepareStatement("select * from fx_leagues where name_ = ? and chat_id_ = ?")) {
            ps.setString(1, name);
            ps.setLong(2, chatId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                return new LeagueDto(rs);
            }
        } catch (Exception e) {
            log.error("error", e);
        }
        return null;
    }

    public static List<LeagueDto> getByChatId(Connection connection, Long chatId) {

        List<LeagueDto> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("select * from fx_leagues where chat_id_ = ? order by create_date_ desc limit 10")) {
            ps.setLong(1, chatId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new LeagueDto(rs));
            }
        } catch (Exception e) {
            log.error("error", e);
        }
        return list;
    }

    public static List<LeagueDto> getByUsernameFromGroupChat(Connection connection, String username) {

        List<LeagueDto> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("select * from fx_leagues where creator_ = ? and group_chat_  =  true order by create_date_ desc limit 10")) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new LeagueDto(rs));
            }
        } catch (Exception e) {
            log.error("error", e);
        }
        return list;
    }

    public static LeagueDto getLatestLeague(Connection connection, Long chatId) {

        try (PreparedStatement ps = connection.prepareStatement("select * from fx_leagues where chat_id_ = ? order by create_date_ desc limit 1")) {
            ps.setLong(1, chatId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                return new LeagueDto(rs);
            }
        } catch (Exception e) {
            log.error("error", e);
        }
        return null;
    }

    public static LeagueDto findLastLeague(Connection connection, Long chatId) {
        try (PreparedStatement ps = connection.prepareStatement("select * from fx_leagues where chat_id_ = ? order by id_ desc limit 1")) {
            ps.setLong(1, chatId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                return new LeagueDto(rs);
            }
        } catch (Exception e) {
            log.error("error", e);
        }
        return null;
    }


}
