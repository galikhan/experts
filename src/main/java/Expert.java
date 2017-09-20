import connector.Connector;
import dto.ExpertDto;
import dto.ForecastDto;
import dto.LeagueDto;
import dto.MatchDto;
import org.omg.PortableInterceptor.TRANSPORT_RETRY;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class Expert {

    private final Logger log = LoggerFactory.getLogger(Expert.class);

    public String newLeague(Connection connection, long chatId, String text, String username) {

        StringTokenizer tokenizer = new StringTokenizer(text, " ");
        if (tokenizer.countTokens() == 2) {

            String command = tokenizer.nextToken();
            String name = tokenizer.nextToken();

            String query = "insert into fx_leagues(name_, creator_, create_date_, modify_date_) values(?, ?, now(), now())";
            log.info("query ~ {}", query);

            try (PreparedStatement ps = connection.prepareStatement(query)) {

                ps.setString(1, name);
                ps.setString(2, username);
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
        return Messages.FAILURE;
    }

    public String leagueList(Connection connection, long chatId, String text, String username) {

        List<LeagueDto> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("select * from fx_leagues order by create_date_ desc limit 5")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new LeagueDto(rs));
            }
        } catch (Exception e) {
            log.error("error", e);
        }
        return fromLeagueList(list);
    }

    public String addMatches(Connection connection, long chatId, String text, String username) {

        StringTokenizer tokenizer = new StringTokenizer(text, " ");
        tokenizer.nextToken();

        if (tokenizer.countTokens() > 0) {
            //add_matches league1 Арсенал-Ливерпуль Челси-МЮ
            String league = tokenizer.nextToken();
            int matchId;
            try (PreparedStatement ps = connection.prepareStatement("select * from fx_leagues where name_ = ?")) {
                ps.setString(1, league);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    return Messages.NO_SUCH_LEAGUE;
                }

            } catch (Exception e) {
                log.error("error", e);
            }


            matchId = 1;
            while (tokenizer.hasMoreTokens()) {
                StringTokenizer innerTokenizer = new StringTokenizer(tokenizer.nextToken(), "-");
                String home = innerTokenizer.nextToken();
                String guests = innerTokenizer.nextToken();

                String query = "insert into fx_matches(id_, league_, match_id_, home_, guests_, create_date_, modify_date_) values(nextval('fx_sequence'), ?, ?, ? ,?, now(), now())";
                try (PreparedStatement ps = connection.prepareStatement(query)) {
                    ps.setString(1, league);
                    ps.setInt(2, matchId);
                    ps.setString(3, home);
                    ps.setString(4, guests);
                    ps.executeUpdate();
                } catch (Exception e) {
                    log.error("error", e);
                    return Messages.ERROR;
                }
                matchId++;
            }
            return Messages.SUCCESS;
        }

        return Messages.FAILURE;
    }

    public String leagueMatches(Connection connection, long chatId, String text, String username) {

        StringTokenizer tokenizer = new StringTokenizer(text, " ");
        tokenizer.nextToken();
        if (!tokenizer.hasMoreTokens()) {
            return Messages.FAILURE;
        }
        String league = tokenizer.nextToken();
        List<MatchDto> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("select * from fx_matches where league_ = ? order by match_id_")) {
            ps.setString(1, league);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new MatchDto(rs));
            }
        } catch (Exception e) {
            log.error("error", e);
        }
        return fromMatchesList(list);

    }


    public String forecast(Connection connection, long chatId, String text, String username) {

        //        forecast league1 1.0-1 2.2-1 ...
        StringTokenizer tokenizer = new StringTokenizer(text, " ");
        tokenizer.nextToken();

        if (tokenizer.countTokens() > 0) {
            //add_matches league1 Арсенал-Ливерпуль Челси-МЮ
            String league = tokenizer.nextToken();
            try (PreparedStatement ps = connection.prepareStatement("select * from fx_leagues where name_ = ?")) {
                ps.setString(1, league);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    return Messages.NO_SUCH_LEAGUE;
                }

            } catch (Exception e) {
                log.error("error", e);
            }

            removeForecast(connection, username, league);

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

                String query = "insert into fx_forecasts(id_, user_, league_, match_id_, home_point_, guests_point_, create_date_, modify_date_) values(nextval('fx_sequence'), ?, ?, ? ,?, ?, now(), now())";
                try (PreparedStatement ps = connection.prepareStatement(query)) {
                    ps.setString(1, username);
                    ps.setString(2, league);
                    ps.setInt(3, matchId);
                    ps.setInt(4, homePoint);
                    ps.setInt(5, guestsPoint);
                    ps.executeUpdate();
                } catch (Exception e) {
                    log.error("error", e);
                    return Messages.ERROR;
                }
                matchId++;
            }

            List<ForecastDto> list = new ArrayList<>();
            try (PreparedStatement ps = connection.prepareStatement("select * from fx_forecasts where user_ = ? and league_ = ? order by match_id_")) {
                ps.setString(1, username);
                ps.setString(2, league);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    list.add(new ForecastDto(rs));
                }
            } catch (Exception e) {
                log.error("error", e);
            }

            Map<Integer, MatchDto> matchMap = new HashMap<>();
            try (PreparedStatement ps = connection.prepareStatement("select * from fx_matches where league_ = ? order by match_id_")) {
                ps.setString(1, league);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    MatchDto match = new MatchDto(rs);
                    matchMap.put(match.matchId, match);
                }
            } catch (Exception e) {
                log.error("error", e);
            }


            return fromForecastList(list, matchMap, username);
        }
        return Messages.FAILURE;
    }

    public int removeForecast(Connection connection, String username, String league) {
        try (PreparedStatement ps = connection.prepareStatement("delete from fx_forecasts where user_ = ? and league_ = ?")) {
            ps.setString(1, username);
            ps.setString(2, league);
            return ps.executeUpdate();
        } catch (Exception e) {
            log.error("error", e);
        }
        return 0;
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

                    String query = "update fx_matches set home_point_ = ?, guests_point_ = ?, modify_date_ = now() where league_ = ? and match_id_ = ?";
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
            try(ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    ForecastDto f = new ForecastDto();
                    f.league = rs.getString("league_");
                    f.matchId = rs.getInt("match_id_");
                    f.homePoint = rs.getInt("home_point_");
                    f.guestPoint = rs.getInt("guests_point_");
                    f.user = rs.getString("user_");
                    list.add(f);
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


        for(ForecastDto f : list) {

            ExpertDto expertDto = map.get(f.user);
            boolean expertExist = true;
            String expertQuery = null;

            if(expertDto == null) {
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

    public String expertsTop(Connection connection, long chatId, String text, String username) {
        StringTokenizer tokenizer = new StringTokenizer(text, " ");
        tokenizer.nextToken();

        if (tokenizer.countTokens() > 0) {
            //result league1 1.0-1
            String league = tokenizer.nextToken();

            List<ExpertDto> list = new ArrayList<>();
            try (PreparedStatement ps = connection.prepareStatement("select * from fx_experts where league_ = ? order by total_ desc")) {
                ps.setString(1, league);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    list.add(new ExpertDto(rs));
                }
            } catch (Exception e) {
                log.error("error", e);
            }
            return fromExpertsList(list);
        }
        return Messages.FAILURE;
    }


    private String fromForecastList(List<ForecastDto> list, Map<Integer, MatchDto> matchMap, String username) {

        Map<Integer, Boolean> unusedMap = new HashMap<>();
        for(Map.Entry<Integer, MatchDto> entry : matchMap.entrySet()) {
            unusedMap.put(entry.getKey(), true);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Прогноз от " + username + "\n");
        for (ForecastDto f : list) {

            MatchDto match = matchMap.get(f.matchId);
            if(match != null) {
                sb.append(f.matchId + ". " + match.home + " "  + f.homePoint + "-" + f.guestPoint + " " + match.guests + "\n");
            } else {
                sb.append(f.matchId + ". Нет матча с таким номером\n");
            }

            if(unusedMap.get(f.matchId)!=null) {
                unusedMap.put(f.matchId, false);
            }
        }

        for(Map.Entry<Integer, MatchDto> entry : matchMap.entrySet()) {
            MatchDto match = matchMap.get(entry.getKey());
            if(unusedMap.get(entry.getKey())) {
                sb.append(match.matchId + ". " + match.home + " ? - ? " + match.guests + "\n");
            }
        }

        return sb.toString();
    }

    public String fromLeagueList(List<LeagueDto> list) {
        StringBuilder sb = new StringBuilder();
        int counter = 1;
        for (LeagueDto l : list) {
            sb.append(counter + ". " + l.name + "\n");
            counter++;
        }
        return sb.toString();
    }


    public String fromMatchesList(List<MatchDto> list) {
        StringBuilder sb = new StringBuilder();
        int counter = 1;
        for (MatchDto m : list) {
            sb.append(counter + ". " + m.home + " - " + m.guests + " result (" + m.homePoint + "-" + m.guestsPoint + ") \n");
            counter++;
        }
        return sb.toString();
    }


    public String fromExpertsList(List<ExpertDto> list) {
        StringBuilder sb = new StringBuilder();
        int counter = 1;
        sb.append("#. Эксперт | Результат | Разница | Общий \n");
        for (ExpertDto m : list) {
            sb.append(counter + ". " + m.user + " | " + m.plus4 + " | " + m.plus2 + " | " + m.plus1 + " | " + m.total + "\n");
            counter++;
        }
        return sb.toString();
    }


}
