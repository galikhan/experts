package data.utils;

import dto.ExpertDto;
import dto.FileDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gali on 10/1/17.
 */
public class FileDataUtils {

    private static Logger log = LoggerFactory.getLogger(FileDataUtils.class);

    public static int save(Connection connection, FileDto fileDto) {
        try (PreparedStatement ps = connection.prepareStatement("" +
                " insert into fx_files(id_, chat_id_, league_, name_, path_, active_, type_, modify_date_, create_date_) " +
                " values(nextval('fx_sequence'), ?, ?, ?, ?, true, ?, now(), now())")) {
            ps.setLong(1, fileDto.chatId);
            ps.setLong(2, fileDto.leagueId);
            ps.setString(3, fileDto.name);
            ps.setString(4, fileDto.path);
            ps.setString(5, fileDto.type);
            return ps.executeUpdate();

        } catch (Exception e) {
            log.error("error", e);
        }
        return 0;
    }

    public static int inActivatePrevFiles(Connection connection, Long chatId, Long leagueId, String type) {
        String update = "update fx_files set active_ = false where league_ = ? and chat_id_ = ? and type_ = ?";
        try (PreparedStatement ps = connection.prepareStatement(update)) {
            ps.setLong(1, leagueId);
            ps.setLong(2, chatId);
            ps.setString(3, type);
            return ps.executeUpdate();
        } catch (Exception e) {
            log.error("error", e);
        }
        return 0;
    }

    public static FileDto findByParams(Connection connection, Long chatId, Long leagueId, String type) {
        try (PreparedStatement ps = connection.prepareStatement("" +
                " select " +
                "   * " +
                " from " +
                "   fx_files" +
                " where" +
                "   chat_id_ = ? " +
                "   and league_ = ?" +
                "   and type_ = ?" +
                "   and active_ = true")) {
            ps.setLong(1, chatId);
            ps.setLong(2, leagueId);
            ps.setString(3, type);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                return new FileDto(rs);
            }
        } catch (Exception e) {
            log.error("error", e);
        }
        return null;
    }

    public static String formatName(Long chatId, Long leagueId, int matchId, String extension) {

        return chatId + "_" + leagueId + "_" + matchId + "." + extension;

    }

}
