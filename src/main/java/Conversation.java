import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dao.ForecastDataUtils;
import dao.MatchDataUtils;
import dto.ConversationDto;
import dto.ForecastDto;
import dto.LeagueDto;
import dto.MatchDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Messages;
import utils.QueryUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Created by gali on 9/26/17.
 */
public class Conversation {

    private final Logger log = LoggerFactory.getLogger(Conversation.class);

    public void startNewConversation(Connection connection, String user, String request, String response, Long chatId, String type) {

        int result = QueryUtils.simpleUpdate(connection, "update fx_conversation set removed_ = true where chat_id_ = ? and user_ = ? and has_answer_ = false", chatId, user);
        save(connection, chatId, user, request, response, type, null);

    }

    public String digest(Connection connection, long chatId, String text, String username, String command) {

        //LEAGUE ID IS IMPORTANT ON EACH CONVERSATION ROW
        ConversationDto conversation = activeRow(connection, chatId, username);
        String responseJson = null;
        String responseStr = null;
        Map<String, String> map = null;

        log.info("conversation type {}", conversation.type);

        //if command incorrect send refuse it
        JsonObject jsonObject = (JsonObject) new JsonParser().parse(conversation.response);
        if(jsonObject.get(command) == null) {
            return Messages.NO_SUCH_COMMAND;
        }

        if(Messages.C_LEAGUES.equals(conversation.type)) {

            map = menu();

            responseStr = map.get("str");
            responseJson = map.get("json");

            Long leagueId = jsonObject.get(command).getAsLong();

            //firstly inactivate user last conversation then save new one
            inActivateRow(connection, chatId, username);
            save(connection, chatId, username, text, responseJson, Messages.C_LEAGUE_MENU, leagueId);

            return responseStr;

        } else if(Messages.C_LEAGUE_MENU.equals(conversation.type)) {

            String menu = jsonObject.get(command).getAsString();
            if(Messages.MENU_SEND_FORECAST.equals(menu)) {

                List<MatchDto> list = MatchDataUtils.getByLeagueId(connection, conversation.leagueId);
                map = matches(list);
                responseStr = map.get("str");
                responseJson = map.get("json");

                inActivateRow(connection, chatId, username);
                save(connection, chatId, username, text, responseJson, Messages.C_FORECAST, conversation.leagueId);

                return responseStr;

            } else if(Messages.MENU_VIEW_FORECAST.equals(menu)){

                List<ForecastDto> list = ForecastDataUtils.getForecasts(connection, username, conversation.leagueId);
                inActivateRow(connection, chatId, username);
                save(connection, chatId, username, text, responseJson, Messages.C_VIEW_FORECAST, conversation.leagueId);
                Map<Integer, MatchDto> matchMap = MatchDataUtils.getMapByLeagueId(connection, conversation.leagueId);
                return ForecastDto.fromForecastList(list, matchMap, username);

            }
        }

        return Messages.NO_SUCH_COMMAND;
    }

    public int save(Connection connection, Long chatId, String username, String request, String response, String type, Long leagueId) {
        try (PreparedStatement ps = connection.prepareStatement("" +
                " insert into " +
                "   fx_conversation(id_, chat_id_, user_, request_, response_, type_, league_, has_answer_, removed_, active_, create_date_, modify_date_) " +
                "   values(nextval('fx_sequence'), ?, ?, ?, ?, ?, ?,false, false, true, now(), now())")) {
            ps.setLong(1, chatId);
            ps.setString(2, username);
            ps.setString(3, request);
            ps.setString(4, response);
            ps.setString(5, type);
            ps.setObject(6, leagueId);
            return ps.executeUpdate();
        } catch (Exception e) {
            log.error("error", e);
        }
        return 0;
    }

    public int inActivateRow(Connection connection, Long chatId, String username) {

        try (PreparedStatement ps = connection.prepareStatement("update fx_conversation set active_ = false where chat_id_ = ? and user_ = ?")) {
            ps.setLong(1, chatId);
            ps.setString(2, username);
            return ps.executeUpdate();
        } catch (Exception e) {
            log.error("error", e);
        }
        return 0;
    }

    public ConversationDto activeRow(Connection conn, long chatId, String username) {

        try (PreparedStatement ps = conn.prepareStatement("" +
                " select " +
                "   * " +
                " from " +
                "   fx_conversation " +
                " where chat_id_ = ? " +
                "       and user_ = ? " +
                "       and active_ = true " +
                "       and removed_ = false")) {
            ps.setLong(1, chatId);
            ps.setString(2, username);
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                return new ConversationDto(rs);
            }
        } catch (Exception e) {
            log.error("error", e);
        }
        return null;
    }


    public Map<String, String> menu() {

        JsonObject json = new JsonObject();
        json.addProperty("1", Messages.MENU_SEND_FORECAST);
        json.addProperty("2", Messages.MENU_VIEW_FORECAST);

        StringBuilder sb = new StringBuilder();
        sb.append("/1. " +Messages.MENU_SEND_FORECAST);
        sb.append("\n");
        sb.append("/2. " +Messages.MENU_VIEW_FORECAST);

        Map<String, String> map = new HashMap<>();

        map.put("json", json.toString());
        map.put("str", sb.toString());

        return map;
    }

    public Map<String, String> matches(List<MatchDto> list) {

        Map<String, String> map = new HashMap<>();
        map.put("json", MatchDto.fromMatchesListToJson(list));
        map.put("str", MatchDto.fromMatchesList(list));

        return map;
    }

    public String digestSimpleText(Connection connection, long chatId, String text, String username) {

        //        forecast league1 1.0-1 2.2-1 ...
        ConversationDto conversation = activeRow(connection, chatId, username);

        if(Messages.C_FORECAST.equals(conversation.type)) {

            StringTokenizer tokenizer = new StringTokenizer(text, " ");
            if (tokenizer.countTokens() > 0) {

                //delete pervious forecasts
                ForecastDataUtils.deleteForecast(connection, username, conversation.leagueId);
                Map<Integer, MatchDto> matchMap = MatchDataUtils.getMapByLeagueId(connection, conversation.leagueId);

                int matchId =1;
                while (tokenizer.hasMoreTokens()) {

                    String result = tokenizer.nextToken();
                    StringTokenizer innerTokenizer = new StringTokenizer(result, "-");

                    int homePoint = Integer.parseInt(innerTokenizer.nextToken());
                    int guestsPoint = Integer.parseInt(innerTokenizer.nextToken());

                    MatchDto matchDto = matchMap.get(matchId);
                    if (matchDto!=null && matchDto.finished == false) {
                        //insert forecase only for matches that are not played yet
                        ForecastDataUtils.save(connection, username, matchId, homePoint, guestsPoint, conversation.leagueId);
                    }
                    matchId++;
                }

                inActivateRow(connection, chatId, username);

                List<ForecastDto> list = ForecastDataUtils.getForecasts(connection, username, conversation.leagueId);
                return ForecastDto.fromForecastList(list, matchMap, username);
            }
            return Messages.FORECAST_FAILURE;

        } else {
            return Messages.NO_SUCH_COMMAND;
        }


    }
}
