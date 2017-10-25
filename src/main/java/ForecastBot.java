import connector.Connector;
import dto.ForecastDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import utils.Messages;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by gali on 10/25/17.
 */
public class ForecastBot extends TelegramLongPollingBot {

    private static Logger log = LoggerFactory.getLogger(ForecastBot.class);

    @Override
    public void onUpdateReceived(Update update) {

        log.info("onUpdateReceived called chat title {}");
        SendMessage message = new SendMessage();

        BasicCommand basicCommand = new BasicCommand();
        Conversation conversation = new Conversation();

        long chatId = update.getMessage().getChatId();
        Chat chat = update.getMessage().getChat();
        String text = update.getMessage().getText();
        String username = update.getMessage().getFrom().getUserName();
        message.setChatId(chatId);

        Connection connection = null;
        try {
            connection = Connector.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String result = null;
        if (update.hasMessage()) {

            if (text.startsWith(Messages.LEAGUES_FROM)) {

                result = basicCommand.leaguesFrom(connection, chatId, text, username);

            } else {

                if (text.startsWith("/")) {
                    String command = text.substring(1);
                    result = conversation.digest(connection, chatId, text, username, command);
                } else {
                    result = conversation.digestSimpleText(connection, chatId, text, username);
                }
            }
        }

        log.info("send message chatId {}", chatId);
        try {
            message.setChatId(chatId);
            message.setText(result);
            sendMessage(message);
        } catch (TelegramApiException e) {
            log.error("error", e);
        }
    }

    @Override
    public String getBotUsername() {
        return "expert_forecast_bot";
    }

    @Override
    public String getBotToken() {
        return "413646347:AAGHI89gDkxLwEqPAQVRTtmXHR42ZP0aa5o";
    }
}
