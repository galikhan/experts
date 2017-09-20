import connector.Connector;
import dto.LeagueDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import utils.CommandUtils;
import utils.Messages;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by gali on 9/10/17.
 */
public class BotService extends TelegramLongPollingBot {

    private final Logger log = LoggerFactory.getLogger(BotService.class);


    @Override
    public void onUpdateReceived(Update update) {

        log.info("onUpdateReceived called");
        SendMessage message = new SendMessage();

        Expert expert = new Expert();

        if (update.hasMessage()) {

            long chatId = update.getMessage().getChatId();
            String text = update.getMessage().getText();
            String username = update.getMessage().getFrom().getUserName();
            message.setChatId(chatId);

            StringTokenizer tokenizer;

            Connection connection = null;
            try {
                connection = Connector.getConnection();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            String result = null;
            if (text.startsWith(CommandUtils.NEW_LEAGUE)) {

                result = expert.newLeague(connection, chatId, text, username);

            } else if (text.startsWith(CommandUtils.LEAGUE_LIST)) {

                result = expert.leagueList(connection, chatId, text, username);

            } else if(text.startsWith(CommandUtils.ADD_MATCHES)){

                result = expert.addMatches(connection, chatId, text, username);

            } else if(text.startsWith(CommandUtils.LEAGUE_MATCHES)){

                result = expert.leagueMatches(connection, chatId, text, username);

            } else if(text.startsWith(CommandUtils.FORECAST)){

                result = expert.forecast(connection, chatId, text, username);

            } else if(text.startsWith(CommandUtils.RESULT)){

                result = expert.result(connection, chatId, text, username);

            } else if(text.startsWith(CommandUtils.EXPERTS_TOP)){

                result = expert.expertsTop(connection, chatId, text, username);

            } else {

                result = "Нет такой команды";

            }

            log.info("chatId {}, text {}", chatId, text);
            try {
                message.setText(result);
                sendMessage(message);
            } catch (TelegramApiException e) {
                log.error("error", e);
            }
        }
    }

    @Override
    public String getBotUsername() {
        return "football_expert_bot";
    }

    @Override
    public String getBotToken() {
        return "338221102:AAEBPO5YbaemwAYjQR_l7BTaOdXcWwpfQvw";
    }
}
