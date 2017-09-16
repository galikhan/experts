import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import utils.CommandUtils;

import java.util.StringTokenizer;

/**
 * Created by gali on 9/10/17.
 */
public class Expert extends TelegramLongPollingBot {

    @Override
    public void onUpdateReceived(Update update) {
        Logger log = LoggerFactory.getLogger(Expert.class);

        log.info("onUpdateReceived called");
        SendMessage message = null;
        if(update.hasMessage()) {

            long chatId = update.getMessage().getChatId();
            String text = update.getMessage().getText();

            StringTokenizer tokenizer;
            if(!text.isEmpty()) {
                tokenizer = new StringTokenizer(text, " ");
            }

            if(text.equals(CommandUtils.SLASH_HELP)) {
                message = new SendMessage(chatId, Messages.HELP);
            } else if(text.startsWith(CommandUtils.NEW_LEAGUE)) {

            }

            System.out.println(update.getMessage().getFrom().getUserName());
            System.out.println(update.getMessage().getFrom().getFirstName());

            log.info("chatId {}, text {}", chatId, text);

            try {
                sendMessage(message) ;
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
