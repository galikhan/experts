import com.google.gson.JsonObject;
import dao.LeagueDataUtils;
import dao.MatchDataUtils;
import dto.ExpertDto;
import dto.ForecastDto;
import dto.LeagueDto;
import dto.MatchDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.api.objects.Chat;
import utils.Messages;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Created by gali on 9/16/17.
 */
public class BasicCommand {

    private final Logger log = LoggerFactory.getLogger(BasicCommand.class);

    public String newLeague(Connection connection, long chatId, String text, String username, Chat chat) {

        String chatTitle = chat.getTitle();
        boolean groupChat = chat.isGroupChat() || chat.isSuperGroupChat();
        log.info("grooup chaat {}", groupChat);
        StringTokenizer tokenizer = new StringTokenizer(text, " ");
        if (tokenizer.countTokens() == 2) {

            String command = tokenizer.nextToken();
            String name = tokenizer.nextToken();

            String query = "insert into fx_leagues(id_, name_, creator_, chat_id_, desc_, group_chat_, create_date_, modify_date_) values(nextval('fx_sequence'), ?, ?, ?, ?, ?, now(), now())";
            log.info("query ~ {}", query);

            try (PreparedStatement ps = connection.prepareStatement(query)) {

                ps.setString(1, name);
                ps.setString(2, username);
                ps.setLong(3, chatId);
                ps.setString(4, chatTitle);
                ps.setBoolean(5, groupChat);
                int result = ps.executeUpdate();
                if (result > 0) {
                    return Messages.SUCCESS;
                } else {
                    return Messages.ERROR;
                }

            } catch (Exception e) {
                log.error("error", e);
                return Messages.UNIQUE;
            }

        }
        return Messages.NEW_LEAGUE_FAILURE;
    }

    public LeagueDto getLeague(Connection connection, String name, String creator) {

        if (creator != null) {

            try (PreparedStatement ps = connection.prepareStatement("select * from fx_leagues where name_ = ? and creator_ = ? and group_chat_ = true")) {
                ps.setString(1, name);
                ps.setString(2, creator);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    return new LeagueDto(rs);
                }
            } catch (Exception e) {
                log.error("error", e);
            }

        } else {

            try (PreparedStatement ps = connection.prepareStatement("select * from fx_leagues where name_ = ?")) {
                ps.setString(1, name);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    return new LeagueDto(rs);
                }
            } catch (Exception e) {
                log.error("error", e);
            }

        }
        return null;
    }

    public String leagues(Connection connection, long chatId, String text, String username) {
        List<LeagueDto> list = LeagueDataUtils.getByChatId(connection, chatId);
        String result = LeagueDto.fromLeagueList("", list);
        return result;
    }

    public String addMatches(Connection connection, long chatId, String text, String username) {

        StringTokenizer tokenizer = new StringTokenizer(text, " ");
        tokenizer.nextToken();

        Long leagueId = null;
        if (tokenizer.countTokens() > 0) {
            //add_matches league1 Арсенал-Ливерпуль Челси-МЮ
            String league = tokenizer.nextToken();
            int matchId;
            try (PreparedStatement ps = connection.prepareStatement("select * from fx_leagues where name_ = ? and chat_id_ = ?")) {
                ps.setString(1, league);
                ps.setLong(2, chatId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    leagueId = rs.getLong("id_");
                } else {
                    return Messages.NO_SUCH_LEAGUE;
                }

            } catch (Exception e) {
                log.error("error", e);
            }


            matchId = 1;
            while (tokenizer.hasMoreTokens()) {
                try {

                    StringTokenizer innerTokenizer = new StringTokenizer(tokenizer.nextToken(), "-");
                    String home = innerTokenizer.nextToken();
                    String guests = innerTokenizer.nextToken();

                    String query = "insert into fx_matches(id_, league_, match_id_, home_, guests_, create_date_, modify_date_) values(nextval('fx_sequence'), ?, ?, ? ,?, now(), now())";
                    try (PreparedStatement ps = connection.prepareStatement(query)) {
                        ps.setLong(1, leagueId);
                        ps.setInt(2, matchId);
                        ps.setString(3, home);
                        ps.setString(4, guests);
                        ps.executeUpdate();
                    } catch (Exception e) {
                        log.error("error", e);
                        return Messages.ERROR;
                    }
                    matchId++;

                } catch (Exception e) {
                    log.error("error on parse String", e);
                    return Messages.ADD_MESSAGES_FAILURE;
                }
            }
            return Messages.SUCCESS;
        }

        return Messages.ADD_MESSAGES_FAILURE;
    }

    public String leagueMatches(Connection connection, long chatId, String text, String username, Long leagueId) {

        if (leagueId == null) {

            StringTokenizer tokenizer = new StringTokenizer(text, " ");
            tokenizer.nextToken();
            if (!tokenizer.hasMoreTokens()) {
                return Messages.LEAGUE_MATCHES_FAILURE;
            }

            String league = tokenizer.nextToken();
            String creator = null;
            if (tokenizer.hasMoreTokens()) {
                creator = tokenizer.nextToken();
            }

            LeagueDto leagueDto = getLeague(connection, league, creator);
            leagueId = (leagueDto != null ? leagueDto.id : null);
        }

        if (leagueId != null) {
            List<MatchDto> list = MatchDataUtils.getByLeagueId(connection, leagueId);
            return MatchDto.fromMatchesList(list);
        } else {
            return Messages.NO_SUCH_LEAGUE;
        }
    }

    public String result(Connection connection, long chatId, String text, String username) {

        StringTokenizer tokenizer = new StringTokenizer(text, " ");
        tokenizer.nextToken();

        if (tokenizer.countTokens() > 0) {
            //result league1 1.0-1
            String league = tokenizer.nextToken();
            String leagueUsername = "";
            try (PreparedStatement ps = connection.prepareStatement("select * from fx_leagues where name_ = ?")) {
                ps.setString(1, league);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    return Messages.NO_SUCH_LEAGUE;
                } else {
                    leagueUsername = rs.getString("creator_");
                }

            } catch (Exception e) {
                log.error("error", e);
            }

            if (leagueUsername.equals(username)) {

                while (tokenizer.hasMoreTokens()) {

                    String result = tokenizer.nextToken();
                    int numIndex = result.indexOf(".");
                    int matchId = 0;
                    if (numIndex > 0) {
                        matchId = Integer.parseInt(result.substring(0, numIndex));
                    }

                    result = result.substring(numIndex + 1);
                    StringTokenizer innerTokenizer = new StringTokenizer(result, "-");
                    int homePoint = Integer.parseInt(innerTokenizer.nextToken());
                    int guestsPoint = Integer.parseInt(innerTokenizer.nextToken());

                    String query = "update fx_matches set home_point_ = ?, guests_point_ = ?, finished_ = true modify_date_ = now() where league_ = ? and match_id_ = ?";
                    try (PreparedStatement ps = connection.prepareStatement(query)) {
                        ps.setInt(1, homePoint);
                        ps.setInt(2, guestsPoint);
                        ps.setString(3, league);
                        ps.setInt(4, matchId);
                        ps.executeUpdate();
                    } catch (Exception e) {
                        log.error("error", e);
                        return Messages.ERROR;
                    }

                    int recalculateTopResult = recalculateTop(connection, league, matchId, homePoint, guestsPoint);
                    if (recalculateTopResult > 0) {
                        return Messages.SUCCESS;
                    } else {
                        return Messages.ERROR;
                    }

                }

            } else {
                return Messages.CANT_UPDATE;
            }
        }
        return Messages.FAILURE;
    }


    public int recalculateTop(Connection connection, String league, int matchId, int homePoint, int guestsPoint) {

        List<ForecastDto> list = new ArrayList<>();
        String query = "select * from fx_forecasts where league_ = ? and match_id_ = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, league);
            ps.setInt(2, matchId);
            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    list.add(new ForecastDto(rs));
                }
            }
        } catch (Exception e) {
            log.error("error", e);
        }

        Map<String, ExpertDto> map = new HashMap<>();
        String select = "select * from fx_experts where league_ = ?";
        try (PreparedStatement ps1 = connection.prepareStatement(select)) {

            ps1.setString(1, league);
            ResultSet rs1 = ps1.executeQuery();
            while (rs1.next()) {
                ExpertDto ed = new ExpertDto(rs1);
                map.put(ed.user, ed);
            }
        } catch (Exception e) {
            log.error("error", e);
        }


        for (ForecastDto f : list) {

            ExpertDto expertDto = map.get(f.user);
            boolean expertExist = true;
            String expertQuery = null;

            if (expertDto == null) {
                expertExist = false;
                expertDto = new ExpertDto();
            }

            if (f.homePoint == homePoint && f.guestPoint == guestsPoint) {
                expertDto.plus4 += 1;
                expertDto.total += 4;
            } else if (pointDiff(homePoint, guestsPoint) == pointDiff(f.homePoint, f.guestPoint)) {
                expertDto.plus2 += 1;
                expertDto.total += 2;
            } else if (matchResult(homePoint, guestsPoint) == matchResult(f.homePoint, f.guestPoint)) {
                expertDto.plus1 += 1;
                expertDto.total += 1;
            }
            expertDto.user = f.user;
            expertDto.league = f.league;

            if (expertExist) {
                expertQuery = "update fx_experts set plus4_ = ?, plus2_ = ?, plus1_ = ?, total_ = ? where league_ = ? and user_ = ?";
            } else {
                expertQuery = "insert into fx_experts(id_, plus4_, plus2_, plus1_, total_, league_, user_) values(nextval('fx_sequence'), " +
                        "?, ?, ?, ?, ?, ?)";
            }

            log.info("query {}", expertQuery);
            try (PreparedStatement ps2 = connection.prepareStatement(expertQuery)) {
                ps2.setInt(1, expertDto.plus4);
                ps2.setInt(2, expertDto.plus2);
                ps2.setInt(3, expertDto.plus1);
                ps2.setInt(4, expertDto.total);
                ps2.setString(5, expertDto.league);
                ps2.setString(6, expertDto.user);
                ps2.executeUpdate();
            } catch (SQLException e) {
                log.error("error", e);
            }
        }

        return 1;
    }


    public int pointDiff(int homePoint, int guestsPoint) {
        return homePoint - guestsPoint;
    }

    public int matchResult(int homePoint, int guestsPoint) {
        int gd = homePoint - guestsPoint;
        int firstWins = 0;
        if (gd == 0) {
            firstWins = 0;
        } else if (gd > 0) {
            firstWins = 1;
        } else {
            firstWins = -1;
        }
        return firstWins;
    }

    public String leaguesFrom(Connection connection, long chatId, String text, String username) {

        StringTokenizer tokenizer = new StringTokenizer(text, " ");
        String request = tokenizer.nextToken();//command
        String creatorUsername = tokenizer.nextToken();//user creator of league

        Conversation conversation = new Conversation();

        List<LeagueDto> list = LeagueDataUtils.getByUsernameFromGroupChat(connection, creatorUsername);
        if (list.isEmpty() == false) {
            JsonObject json = LeagueDto.fromLeagueListToJson(list);
            conversation.startNewConversation(connection, username, request, json.toString(), chatId, Messages.C_LEAGUES);
            return LeagueDto.fromLeagueList("/" , list);
        }
        return Messages.NO_LEAGUES_FOR_THIS_USER;
    }


    //show latest league top table
    public String table(Connection connection, long chatId, String text, String username) {
//        StringTokenizer tokenizer = new StringTokenizer(text, " ");
//        tokenizer.nextToken();
//
//        if (tokenizer.countTokens() > 0) {
//            //result league1 1.0-1
//            String league = tokenizer.nextToken();
//            LeagueDto leagueDto = LeagueDataUtils.findByName(connection, league, chatId);
//
//            List<ExpertDto> list = new ArrayList<>();
//            try (PreparedStatement ps = connection.prepareStatement("select * from fx_experts where league_ = ? order by total_ desc")) {
//                ps.setLong(1, league);
//                ResultSet rs = ps.executeQuery();
//                while (rs.next()) {
//                    list.add(new ExpertDto(rs));
//                }
//            } catch (Exception e) {
//                log.error("error", e);
//            }
//            return ExpertDto.fromExpertsList(list);
//        }
//        return Messages.FAILURE;

        LeagueDto leagueDto = LeagueDataUtils.getLatestLeague(connection, chatId);
        return null;
    }

    public String tableAll(Connection connection, long chatId, String text, String username) {
        return null;
    }
}
