import com.google.gson.JsonObject;
import data.utils.ExpertDataUtils;
import data.utils.FileDataUtils;
import data.utils.ForecastDataUtils;
import data.utils.LeagueDataUtils;
import data.utils.MatchDataUtils;
import data.utils.SeasonDataUtils;
import dto.ExpertDto;
import dto.FileDto;
import dto.ForecastDto;
import dto.LeagueDto;
import dto.MatchDto;
import dto.SeasonDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.api.objects.Chat;
import utils.ExpertUtils;
import utils.Messages;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
        StringTokenizer tokenizer = new StringTokenizer(text, " ");
        if (tokenizer.countTokens() == 2) {
            tokenizer.nextToken();
            String name = tokenizer.nextToken();

            SeasonDto seasonDto = SeasonDataUtils.findByChatId(connection, chatId);

            LeagueDto leagueDto = new LeagueDto();
            leagueDto.creator = username;
            leagueDto.desc = chatTitle;
            leagueDto.groupChat = groupChat;
            leagueDto.chatId = chatId;
            leagueDto.name = name;
            leagueDto.season = seasonDto.id;
            int result = LeagueDataUtils.save(connection, leagueDto);
            if (result > 0) {
                return Messages.SUCCESS;
            } else {
                return Messages.ERROR;
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
            String leagueName = tokenizer.nextToken();
            LeagueDto league = LeagueDataUtils.findByName(connection, leagueName, chatId);

            if (league != null && username.equals(league.creator)) {

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

                    MatchDto match = MatchDataUtils.getByLeagueAndMatchId(connection, league.id, matchId);
                    if (match != null && match.finished == false) {

                        MatchDto matchDto = new MatchDto();
                        matchDto.homePoint = homePoint;
                        matchDto.guestsPoint = guestsPoint;
                        matchDto.league = league.id;
                        matchDto.matchId = matchId;

                        MatchDataUtils.save(connection, matchDto);
                        recalculateTop(connection, league.id, matchId, homePoint, guestsPoint, chatId);
                    } else {
                        return Messages.MATCH_FINISHED;
                    }
                    generateTable(connection, chatId, text, username, matchId);
                    return Messages.SUCCESS;
                }

            } else {
                return Messages.CANT_UPDATE;
            }
        }
        return Messages.FAILURE;
    }


    public void recalculateTop(Connection connection, Long leagueId, int matchId, int homePoint, int guestsPoint, Long chatId) {


        int lastVersion = ExpertDataUtils.lastVersion(connection, leagueId, ExpertUtils.SINGLE);
        lastVersion += 1;
        List<ForecastDto> forecasts = ForecastDataUtils.getForecastsByMatchAndLeague(connection, matchId, leagueId);
        for (ForecastDto forecast : forecasts) {

            ExpertDto expert = new ExpertDto();
            if (forecast.homePoint == homePoint && forecast.guestsPoint == guestsPoint) {
                expert.plus4 += 1;
                expert.total += 4;
            } else if (pointDiff(homePoint, guestsPoint) == pointDiff(forecast.homePoint, forecast.guestsPoint)) {
                expert.plus2 += 1;
                expert.total += 2;
            } else if (matchResult(homePoint, guestsPoint) == matchResult(forecast.homePoint, forecast.guestsPoint)) {
                expert.plus1 += 1;
                expert.total += 1;
            }
            expert.user = forecast.user;
            expert.leagueId = forecast.leagueId;
            expert.version = lastVersion;
            expert.type = ExpertUtils.SINGLE;
            ExpertDataUtils.save(connection, expert);
        }

        LeagueDto league = LeagueDataUtils.findById(connection, leagueId);
        //newly summed results
        List<ExpertDto> experts = ExpertDataUtils.experts(connection, leagueId, lastVersion, ExpertUtils.SINGLE);
        int order = 1;
        for (ExpertDto expert : experts) {
            expert.order = order;
            ExpertDataUtils.update(connection, expert);
            order += 1;
        }

        Map<String, ExpertDto> totalMapExpert = ExpertDataUtils.seasonTableMap(connection, league.season);
        Map<String, ExpertDto> prevMapExpert = ExpertDataUtils.expertMap(connection, leagueId, lastVersion - 1);

        for (ExpertDto expert : experts) {
            ExpertDto totalExpert = totalMapExpert.get(expert.user);
            log.debug("totalExpert +4 ~ {}, +2 ~ {}, +1 ~ {}, total ~ {}", totalExpert.plus4, totalExpert.plus2, totalExpert.plus1, totalExpert.total);

            if (prevMapExpert.get(expert.user) != null) {
                ExpertDto prevExpert = prevMapExpert.get(expert.user);
                totalExpert.scale = prevExpert.order - totalExpert.order;
            }

            totalExpert.type = ExpertUtils.GLOBAL;
            totalExpert.leagueId = leagueId;
            totalExpert.version = lastVersion;

            ExpertDataUtils.save(connection, totalExpert);
        }
//        }
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
        if (league.id != null) {
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
        sbHead.append("<th>#</th>");
        sbHead.append("<th>experts</th>");
        for (MatchDto match : matches) {
            if(match.finished) {
                sbHead.append("<th>" + match.home + " (" + match.homePoint + "-" + match.guestsPoint + ")<br>" + match.guests + "</th>");
            }
        }
        sbHead.append("<th>total</th>");
        sbHead.append("<th>total(all)</th>");
        sbHead.append("<th>scale</th>");
        sbHead.append("</tr></thead>");
        sb.append(sbHead);

        //@TODO rework
        int lastVersion = ExpertDataUtils.lastVersion(connection, leagueDto.id, ExpertUtils.SINGLE);
        log.info("last version  {}, liga {}", lastVersion, leagueDto.id);

        List<ExpertDto> globalExperts = ExpertDataUtils.experts(connection, leagueDto.id, lastVersion, ExpertUtils.GLOBAL);

        List<ExpertDto> experts = ExpertDataUtils.experts(connection, leagueDto.id, lastVersion, ExpertUtils.SINGLE);
        Map<String, ExpertDto> expertMap = ExpertDto.fromExpertsListToMap(experts);

        sb.append("<tbody>");
        int order = 1;

        log.info("experts {}", globalExperts.size());
        for (ExpertDto globalExpert : globalExperts) {

            ExpertDto expert = expertMap.get(globalExpert.user);

            List<ForecastDto> forecasts = ForecastDataUtils.getForecasts(connection, expert.user, leagueDto.id);
            Map<Integer, ForecastDto> map = ForecastDto.fromForecastListToMap(forecasts);
            sb.append("" +
                    "<tr>" +
                    "<td rowspan=2>" + order + ".</td>" +
                    "<td rowspan=2>" + expert.user + "</td>");

            StringBuilder firstSpan = new StringBuilder();
            firstSpan.append("<tr>");

            int total = 0;
            log.info("matches {}", matches.size());

            for (MatchDto match : matches) {

                if(match.finished) {
                    ForecastDto forecast = map.get(match.matchId);
                    sb.append("<td>" + forecast.homePoint + " - " + forecast.guestsPoint + "</td>");

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
            }
            firstSpan.append("<td>" + total + "</td>");
            firstSpan.append("<td>" + globalExpert.total + "</td>");
            if(globalExpert.scale == 0) {
                firstSpan.append("<td></td>");
            } else if(globalExpert.scale > 0) {
                firstSpan.append("<td style=color:green>+"  + globalExpert.scale + "</td>");
            } else if(globalExpert.scale < 0) {
                firstSpan.append("<td style=color:red>" + globalExpert.scale + "</td>");
            }
            firstSpan.append("</tr>");

            sb.append("</tr>");
            sb.append(firstSpan);
            order++;
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


            try {
                Process proc = Runtime.getRuntime().exec("java -jar /Users/gali/Documents/webvector/webvector-3.4.jar file://" + fileDto.path + fileDto.htmlName + " " + fileDto.path + fileDto.name + " png");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return table(connection, chatId, null, null);
    }

    public String tableAll(Connection connection, long chatId, String text, String username) {
        return null;
    }

    public String newSeason(Connection connection, long chatId, String text, String username, Chat chat) {


        StringTokenizer tokenizer = new StringTokenizer(text, " ");
        if (tokenizer.countTokens() == 2) {

            SeasonDataUtils.disableAll(connection, chatId);

            tokenizer.nextToken();
            String name = tokenizer.nextToken();
            SeasonDto seasonDto = new SeasonDto();
            seasonDto.creator = username;
            seasonDto.chatId = chatId;
            seasonDto.name = name;
            int result = SeasonDataUtils.save(connection, seasonDto);
            if (result > 0) {
                return Messages.SUCCESS;
            } else {
                return Messages.ERROR;
            }
        }
        return Messages.NEW_SEASON_FAILURE;
    }

    public String seasons(Connection connection, long chatId, String text, String username, Chat chat) {
        List<SeasonDto> list = SeasonDataUtils.findAllByChatId(connection, chatId);
        if (list.isEmpty()) {
            return Messages.NO_SEASONS_FOR_CHAT;
        }
        return SeasonDto.fromSeasonListToString(list);
    }
}

