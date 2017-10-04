import connector.Connector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import utils.Messages;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.StringTokenizer;

/**
 * Created by gali on 9/10/17.
 */
public class BotService extends TelegramLongPollingBot {

    private final Logger log = LoggerFactory.getLogger(BotService.class);


    @Override
    public void onUpdateReceived(Update update) {

        log.info("onUpdateReceived called chat title {}");
        SendMessage message = new SendMessage();

        BasicCommand basicCommand = new BasicCommand();
        Conversation conversation = new Conversation();

        if (update.hasMessage()) {
            long chatId = update.getMessage().getChatId();
            Chat chat = update.getMessage().getChat();
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
            String filePath = null;

            if(text.startsWith(Messages.TABLE)){

                filePath = basicCommand.table(connection, chatId, text, username);
                sendPhoto(chatId, filePath);

            } else if(text.startsWith(Messages.TABLE_ALL)){

                result = basicCommand.tableAll(connection, chatId, text, username);

            } else if(text.startsWith(Messages.RESULT)){

                filePath = basicCommand.result(connection, chatId, text, username);
                sendPhoto(chatId, filePath);

            } else {

                if (text.startsWith(Messages.NEW_LEAGUE)) {

                    result = basicCommand.newLeague(connection, chatId, text, username, chat);

                } else if (text.startsWith(Messages.LEAGUE)) {

                    result = basicCommand.leagues(connection, chatId, text, username);

                } else if (text.startsWith(Messages.LEAGUE_FROM)) {

                    result = basicCommand.leaguesFrom(connection, chatId, text, username);

                } else if(text.startsWith(Messages.ADD_MATCHES)){

                    result = basicCommand.addMatches(connection, chatId, text, username);

                } else if(text.startsWith(Messages.LEAGUE_MATCHES)){

                    result = basicCommand.leagueMatches(connection, chatId, text, username, null);

                } else {

                    if(text.startsWith("/")) {
                        String command = text.substring(1);
                        result = conversation.digest(connection, chatId, text, username, command);
                    } else {
                        result = conversation.digestSimpleText(connection, chatId, text, username);
                    }
                }

                sendMessage(chatId, result);
            }
        }
    }

    public void sendMessage(Long chatId, String result) {

        SendMessage message = new SendMessage();
        log.info("send message chatId {}", chatId);
        try {
            message.setChatId(chatId);
            message.setText(result);
            sendMessage(message);
        } catch (TelegramApiException e) {
            log.error("error", e);
        }
    }

    public void sendPhoto(Long chatId, String filePath) {

        log.info("send photo chatId ~ {} filePath ~ {}", chatId, filePath);
        SendPhoto photo = new SendPhoto();
        try {
            File file = new File(filePath);
            log.info("file - {}", file.exists());
            photo.setChatId(chatId);
            photo.setNewPhoto(file);
            sendPhoto(photo);
        } catch (TelegramApiException e) {
            log.error("error", e);
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
