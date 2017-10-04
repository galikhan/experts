import com.google.gson.JsonObject;
import data.utils.ExpertDataUtils;
import data.utils.FileDataUtils;
import data.utils.ForecastDataUtils;
import data.utils.LeagueDataUtils;
import data.utils.MatchDataUtils;
import dto.ExpertDto;
import dto.FileDto;
import dto.ForecastDto;
import dto.LeagueDto;
import dto.MatchDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.api.objects.Chat;
import utils.Messages;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
            LeagueDto leagueDto = LeagueDataUtils.findByName(connection, league, chatId);

            if (leagueDto != null && username.equals(leagueDto.creator)) {

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

                    String query = "update fx_matches set home_point_ = ?, guests_point_ = ?, finished_ = true, modify_date_ = now() where league_ = ? and match_id_ = ?";
                    try (PreparedStatement ps = connection.prepareStatement(query)) {
                        ps.setInt(1, homePoint);
                        ps.setInt(2, guestsPoint);
                        ps.setLong(3, leagueDto.id);
                        ps.setInt(4, matchId);
                        ps.executeUpdate();
                    } catch (Exception e) {
                        log.error("error", e);
                        return Messages.ERROR;
                    }

                    recalculateTop(connection, league, matchId, homePoint, guestsPoint, chatId);
                    return generateTable(connection, chatId, text, username, matchId);

                }

            } else {
                return Messages.CANT_UPDATE;
            }
        }
        return Messages.FAILURE;
    }


    public void recalculateTop(Connection connection, String leagueName, int matchId, int homePoint, int guestsPoint, Long chatId) {

        log.info("recalculateTop, {}", leagueName);
        LeagueDto league = LeagueDataUtils.findByName(connection, leagueName, chatId);
        log.info("league, {}", league.id);

        List<ForecastDto> forecasts = ForecastDataUtils.getForecastsByMatchAndLeague(connection, matchId, league.id);
        log.info("forecasts, {}", forecasts.size());

        ExpertDataUtils.deleteExperts(connection, league.id);

        for (ForecastDto f : forecasts) {

            ExpertDto expertDto = new ExpertDto();

            if (f.homePoint == homePoint && f.guestsPoint == guestsPoint) {
                expertDto.plus4 += 1;
                expertDto.total += 4;
            } else if (pointDiff(homePoint, guestsPoint) == pointDiff(f.homePoint, f.guestsPoint)) {
                expertDto.plus2 += 1;
                expertDto.total += 2;
            } else if (matchResult(homePoint, guestsPoint) == matchResult(f.homePoint, f.guestsPoint)) {
                expertDto.plus1 += 1;
                expertDto.total += 1;
            }
            expertDto.user = f.user;
            expertDto.leagueId = f.leagueId;

            String expertQuery = "insert into fx_experts(id_, plus4_, plus2_, plus1_, total_, league_, user_, modify_date_, create_date_) values(nextval('fx_sequence'), " +
                    "?, ?, ?, ?, ?, ?, now(), now())";

            log.info("query {}", expertQuery);
            try (PreparedStatement ps = connection.prepareStatement(expertQuery)) {
                ps.setInt(1, expertDto.plus4);
                ps.setInt(2, expertDto.plus2);
                ps.setInt(3, expertDto.plus1);
                ps.setInt(4, expertDto.total);
                ps.setLong(5, expertDto.leagueId);
                ps.setString(6, expertDto.user);
                ps.executeUpdate();
            } catch (SQLException e) {
                log.error("error", e);
            }
        }
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
            return LeagueDto.fromLeagueList("/", list);
        }
        return Messages.NO_LEAGUES_FOR_THIS_USER;
    }


    //show latest league top table
    public String table(Connection connection, long chatId, String text, String username) {

        LeagueDto league = LeagueDataUtils.findLastLeague(connection, chatId);
        if(league.id!=null) {
            FileDto file = FileDataUtils.findByParams(connection, chatId, league.id, Messages.FILE_TABLE);
            return file.path + file.name;
        }
        return Messages.NO_LEAGUES_FOR_THIS_CHAT;
    }

    public String generateTable(Connection connection, long chatId, String text, String username, int matchId) {

        LeagueDto leagueDto = LeagueDataUtils.getLatestLeague(connection, chatId);
        List<MatchDto> matches = MatchDataUtils.getByLeagueId(connection, leagueDto.id);

        StringBuilder sb = new StringBuilder("<html>");
        StringBuilder sbHead = new StringBuilder();
        sb.append("<style>" +
                "table {" +
                "    border-collapse: collapse;" +
                "    width: 100%;" +
                "}" +
                "th, td {" +
                "    text-align: left;" +
                "    padding: 8px;" +
                "}" +
                "tr:nth-child(even){" +
                "   background-color: #f2f2f2;" +
                "   border-bottom: 1px solid black;" +
                "}" +
                "th {" +
                "    background-color: #4CAF50;" +
                "    color: white;" +
                "}" +
                "" +
                "</style>");

        sb.append("<table>");
        sbHead.append("<thead><tr>");
        sbHead.append("<th>experts</th>");
        for (MatchDto match : matches) {
            sbHead.append("<th>" + match.home + " -\n" + match.guests + "</th>");
        }
        sbHead.append("<th>total</th>");
        sbHead.append("</tr></thead>");
        sb.append(sbHead);

        List<ExpertDto> experts = ExpertDataUtils.experts(connection, leagueDto.id);
        sb.append("<tbody>");
        for (ExpertDto expert : experts) {

            List<ForecastDto> forecasts = ForecastDataUtils.getForecasts(connection, expert.user, leagueDto.id);
            Map<Integer, ForecastDto> map = ForecastDto.fromForecastListToMap(forecasts);
            sb.append("<tr><td rowspan=2>" + expert.user + "</td>");

            StringBuilder firstSpan = new StringBuilder();
            firstSpan.append("<tr>");
            int total = 0;
            for (MatchDto match : matches) {

                ForecastDto forecast = map.get(match.matchId);

                sb.append("<td>" + forecast.homePoint + " -\n" + forecast.guestsPoint + "</td>");
                int matchPoint = 0;
                if (forecast.homePoint == match.homePoint && forecast.guestsPoint == match.guestsPoint) {
                    matchPoint = 4;
                } else if (pointDiff(match.homePoint, match.guestsPoint) == pointDiff(forecast.homePoint, forecast.guestsPoint)) {
                    matchPoint = 2;
                } else if (matchResult(match.homePoint, match.guestsPoint) == matchResult(forecast.homePoint, forecast.guestsPoint)) {
                    matchPoint = 1;
                }
                total += matchPoint;
                firstSpan.append("<td>" + matchPoint + "</td>");
            }
            sb.append("</tr>");

            firstSpan.append("<td>" + total + "</td>");
            firstSpan.append("</tr>");
            sb.append(firstSpan);
        }
        sb.append("</tbody>");
        sb.append("</html>");

        FileDataUtils.inActivatePrevFiles(connection, chatId, leagueDto.id, Messages.FILE_TABLE);

        FileDto fileDto = new FileDto();
        fileDto.type = Messages.FILE_TABLE;
        fileDto.chatId = chatId;
        fileDto.leagueId = leagueDto.id;
        fileDto.name = FileDataUtils.formatName(chatId, leagueDto.id, matchId, "png");
        fileDto.htmlName = FileDataUtils.formatName(chatId, leagueDto.id, matchId, "html");
        fileDto.path = "/Users/gali/IdeaProjects/FootBallExpertBot/files/";

        int save = FileDataUtils.save(connection, fileDto);
        if (save > 0) {
            try {
                File file = new File(fileDto.path + fileDto.htmlName);
                FileWriter write = new FileWriter(file);
                write.write(sb.toString());
                write.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

            Process proc = null;
            try {
                proc = Runtime.getRuntime().exec("java -jar /Users/gali/Documents/webvector/webvector-3.4.jar file://" + fileDto.path+fileDto.htmlName + " " + fileDto.path+fileDto.name  + " png");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return table(connection, chatId, null, null);
    }

    public String tableAll(Connection connection, long chatId, String text, String username) {
        return null;
    }

}

