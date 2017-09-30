package utils;

import com.google.gson.JsonObject;
import dto.ExpertDto;
import dto.ForecastDto;
import dto.LeagueDto;
import dto.MatchDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gali on 9/27/17.
 */
public class QueryUtils {

    public static Logger log = LoggerFactory.getLogger(QueryUtils.class);

    public static int simpleUpdate(Connection connection, String query, Object... params) {

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            int order = 1;
            for (Object o : params) {
                if (o instanceof String) {
                    ps.setString(order, (String) o);
                } else if (o instanceof Integer) {
                    ps.setInt(order, (Integer) o);
                } else if( o instanceof Long) {
                    ps.setLong(order, (Long) o);
                }
                order += 1;
            }
            return ps.executeUpdate();
        } catch (Exception e) {
            log.error("error", e);
        }
        return 0;
    }


}
