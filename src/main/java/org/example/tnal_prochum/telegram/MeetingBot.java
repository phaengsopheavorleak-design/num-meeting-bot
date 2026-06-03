package org.example.tnal_prochum.telegram;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.example.tnal_prochum.model.Meeting;
import org.example.tnal_prochum.model.Participant;
import org.example.tnal_prochum.model.Rsvp;
import org.example.tnal_prochum.service.AdminService;
import org.example.tnal_prochum.service.AnnouncementService;
import org.example.tnal_prochum.service.ParticipantService;
import org.example.tnal_prochum.service.RsvpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class MeetingBot extends TelegramLongPollingBot {

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Autowired
    private AdminService adminService;

    @Autowired
    private ParticipantService participantService;

    @Lazy
    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private RsvpService rsvpService;

    @Override
    public String getBotUsername() { return botUsername; }

    @Override
    public String getBotToken() { return botToken; }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
            String firstName = update.getCallbackQuery().getFrom().getFirstName();
            Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
            String callbackQueryId = update.getCallbackQuery().getId();

            // Remove buttons after clicking
            removeButtons(chatId, messageId);

            // Answer the callback to stop loading spinner
            answerCallback(callbackQueryId);

            handleCallback(callbackData, chatId, firstName);
            return;
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            Message msg = update.getMessage();
            String messageText = msg.getText();
            String chatId = msg.getChatId().toString();
            String firstName = msg.getFrom().getFirstName();
            String username = msg.getFrom().getUserName();

            System.out.println("📩 Message from " + firstName + " (" + chatId + "): " + messageText);

            if (messageText.equals("/start")) {
                if (!chatId.equals(adminService.getAdminChatId())) {
                    participantService.saveParticipant(chatId, firstName, username);
                }

                sendMessage(chatId,
                        "👋 Welcome " + firstName + " to NUM Meeting System!\n\n" +
                                "✅ You are now subscribed to announcements.\n" +
                                "You will receive meeting updates automatically."
                );

                if (!chatId.equals(adminService.getAdminChatId())) {
                    Optional<Meeting> meeting = rsvpService.getActiveMeeting();
                    meeting.ifPresent(m -> sendRsvpButtons(chatId,
                            "📋 There is an upcoming meeting:\n\n" +
                                    m.getTitle() + "\n\n" +
                                    "Will you attend?"
                    ));
                }

            } else if (chatId.equals(adminService.getAdminChatId())) {
                handleAdminMessage(messageText, chatId);

            } else {
                String display = (username != null) ? "@" + username : firstName;
                sendMessage(adminService.getAdminChatId(),
                        "💬 Message from " + display + " (ID: " + chatId + "):\n\n" + messageText
                );
                sendMessage(chatId, "✅ Your message has been sent to the admin.");
            }
        }
    }

    private void handleAdminMessage(String messageText, String chatId) {
        System.out.println("🔍 Admin command received: [" + messageText + "]");

        String cleanText = messageText
                .replaceAll("^[:\\s]+", "")
                .replaceAll("@\\S+", "")
                .trim();

        System.out.println("🔍 Cleaned text: [" + cleanText + "]");

        if (cleanText.startsWith("/meeting ")) {
            String content = cleanText.replace("/meeting ", "");
            String[] parts = content.split("\\|");
            if (parts.length == 2) {
                String title = parts[0].trim();
                String timeStr = parts[1].trim();
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    LocalDateTime meetingTime = LocalDateTime.parse(timeStr, formatter);
                    rsvpService.createMeeting(title, meetingTime);

                    List<Participant> all = participantService.getAllParticipants();
                    int sentCount = 0;
                    for (Participant p : all) {
                        if (!p.getChatId().equals(adminService.getAdminChatId())) {
                            sendRsvpButtons(p.getChatId(),
                                    "📢 New Meeting Announced!\n\n" +
                                            "📋 " + title + "\n" +
                                            "🕐 " + timeStr + "\n\n" +
                                            "Will you attend?"
                            );
                            sentCount++;
                        }
                    }
                    sendMessage(adminService.getAdminChatId(),
                            "✅ Meeting created and sent to " + sentCount + " participants!");

                } catch (Exception e) {
                    e.printStackTrace();
                    sendMessage(adminService.getAdminChatId(),
                            "❌ Wrong format!\n\n" +
                                    "Use: /meeting Title | 2026-05-28 10:00\n" +
                                    "Example: /meeting Team Sync | 2026-05-28 10:00"
                    );
                }
            } else {
                sendMessage(adminService.getAdminChatId(),
                        "❌ Missing | symbol!\n\n" +
                                "Use: /meeting Title | 2026-05-28 10:00"
                );
            }

        } else if (cleanText.startsWith("/reschedule ")) {
            String timeStr = cleanText.replace("/reschedule ", "").trim();
            try {
                Optional<Meeting> meetingOpt = rsvpService.getActiveMeeting();
                if (meetingOpt.isEmpty()) {
                    sendMessage(adminService.getAdminChatId(),
                            "❌ No active meeting to reschedule.\n\n" +
                                    "Create a meeting first using:\n" +
                                    "/meeting Title | 2026-06-01 10:00"
                    );
                } else {
                    Meeting meeting = meetingOpt.get();
                    String oldTime = meeting.getMeetingTime().toString();

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    LocalDateTime newTime = LocalDateTime.parse(timeStr, formatter);

                    // Update meeting time
                    meeting.setMeetingTime(newTime);

                    // Reset all reminder flags so everyone gets reminders again
                    meeting.setOneDayReminderSent(false);
                    meeting.setThreeHourReminderSent(false);
                    meeting.setFiveMinuteReminderSent(false);
                    meeting.setStartedNotificationSent(false);

                    rsvpService.saveMeeting(meeting);

                    // Notify all participants about the reschedule
                    List<Participant> all = participantService.getAllParticipants();
                    int notifiedCount = 0;
                    for (Participant p : all) {
                        if (!p.getChatId().equals(adminService.getAdminChatId())) {
                            sendMessage(p.getChatId(),
                                    "📢 Meeting Rescheduled!\n\n" +
                                            "📋 " + meeting.getTitle() + "\n" +
                                            "🕐 New time: " + timeStr + "\n\n" +
                                            "⚠️ Please take note of the new meeting time!\n" +
                                            "Your previous RSVP response is still saved."
                            );
                            notifiedCount++;
                        }
                    }

                    sendMessage(adminService.getAdminChatId(),
                            "✅ Meeting rescheduled!\n\n" +
                                    "📋 " + meeting.getTitle() + "\n" +
                                    "🕐 New time: " + timeStr + "\n\n" +
                                    "📢 Notified " + notifiedCount + " participants.\n" +
                                    "All reminders have been reset automatically."
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendMessage(adminService.getAdminChatId(),
                        "❌ Wrong format!\n\n" +
                                "Use: /reschedule 2026-06-01 11:00\n" +
                                "Example: /reschedule 2026-06-01 11:00"
                );
            }

        } else if (cleanText.startsWith("/reply ")) {
            String withoutCommand = cleanText.replace("/reply ", "");
            String[] parts = withoutCommand.split(" ", 2);
            if (parts.length == 2) {
                String targetUsername = parts[0].replace("@", "");
                String privateMessage = parts[1];
                Optional<Participant> target = participantService.findByUsername(targetUsername);
                if (target.isPresent()) {
                    sendMessage(target.get().getChatId(),
                            "📩 Message from Admin:\n\n" + privateMessage);
                    sendMessage(adminService.getAdminChatId(),
                            "✅ Private message sent to @" + targetUsername);
                } else {
                    sendMessage(adminService.getAdminChatId(),
                            "❌ Participant @" + targetUsername + " not found.");
                }
            }

        } else if (cleanText.equals("/list")) {
            List<Participant> all = participantService.getAllParticipants();
            if (all.isEmpty()) {
                sendMessage(adminService.getAdminChatId(), "No participants yet.");
            } else {
                StringBuilder sb = new StringBuilder("👥 Participants:\n\n");
                for (Participant p : all) {
                    sb.append("• ").append(p.getFirstName());
                    if (p.getUsername() != null) {
                        sb.append(" (@").append(p.getUsername()).append(")");
                    }
                    sb.append("\n");
                }
                sendMessage(adminService.getAdminChatId(), sb.toString());
            }

        } else if (cleanText.equals("/rsvp")) {
            Optional<Meeting> meetingOpt = rsvpService.getActiveMeeting();
            if (meetingOpt.isEmpty()) {
                sendMessage(adminService.getAdminChatId(), "No active meeting.");
            } else {
                Long meetingId = meetingOpt.get().getId();
                List<Rsvp> accepted = rsvpService.getByStatus(meetingId, Rsvp.Status.ACCEPT);
                List<Rsvp> declined = rsvpService.getByStatus(meetingId, Rsvp.Status.DECLINE);
                List<Rsvp> maybe = rsvpService.getByStatus(meetingId, Rsvp.Status.MAYBE);

                StringBuilder sb = new StringBuilder("📊 RSVP Status:\n\n");
                sb.append("✅ Accept: ").append(accepted.size()).append("\n");
                for (Rsvp r : accepted) sb.append("  • ").append(r.getFirstName()).append("\n");
                sb.append("\n❌ Decline: ").append(declined.size()).append("\n");
                for (Rsvp r : declined) sb.append("  • ").append(r.getFirstName()).append("\n");
                sb.append("\n🤔 Maybe: ").append(maybe.size()).append("\n");
                for (Rsvp r : maybe) sb.append("  • ").append(r.getFirstName()).append("\n");

                sendMessage(adminService.getAdminChatId(), sb.toString());
            }

        } else if (cleanText.startsWith("/setadmin ")) {
            String newAdminId = cleanText.replace("/setadmin ", "").trim();
            String oldAdminId = adminService.getAdminChatId();
            adminService.setAdminChatId(newAdminId);
            sendMessage(oldAdminId,
                    "✅ Admin role has been transferred!\n\n" +
                            "New admin ID: " + newAdminId
            );
            sendMessage(newAdminId,
                    "🎉 You are now the admin of NUM Meeting System!\n\n" +
                            "Type /help to see all available commands."
            );

        } else if (cleanText.equals("/help")) {
            sendMessage(adminService.getAdminChatId(),
                    "📋 Admin Commands:\n\n" +
                            "/meeting Title | 2026-05-28 10:00 — Create meeting\n" +
                            "/reschedule 2026-06-01 11:00 — Reschedule active meeting\n" +
                            "/reply @username message — Private message\n" +
                            "/list — Show all participants\n" +
                            "/rsvp — Show attendance status\n" +
                            "/setadmin chatid — Transfer admin to someone else\n" +
                            "Any other text — Broadcast to all"
            );

        } else {
            List<Participant> all = participantService.getAllParticipants();
            for (Participant p : all) {
                if (!p.getChatId().equals(adminService.getAdminChatId())) {
                    sendMessage(p.getChatId(), messageText);
                }
            }
            announcementService.saveAndSendAnnouncement(messageText);
            sendMessage(adminService.getAdminChatId(),
                    "✅ Announcement sent to all participants!");
        }
    }

    private void handleCallback(String callbackData, String chatId, String firstName) {
        Optional<Meeting> meetingOpt = rsvpService.getActiveMeeting();
        if (meetingOpt.isEmpty()) {
            sendMessage(chatId, "No active meeting found.");
            return;
        }

        Meeting meeting = meetingOpt.get();

        // Get the message id to remove buttons after clicking
        // We need to update the callback query to remove buttons
        switch (callbackData) {
            case "ACCEPT" -> {
                rsvpService.saveOrUpdateRsvp(chatId, firstName, Rsvp.Status.ACCEPT, meeting.getId());
                sendMessage(chatId,
                        "✅ You have accepted the meeting!\n\n" +
                                "📋 " + meeting.getTitle() + "\n\n" +
                                "We will remind you 1 day before and 3 hours before the meeting."
                );
                sendMessage(adminService.getAdminChatId(),
                        "✅ " + firstName + " accepted the meeting.");
            }
            case "DECLINE" -> {
                rsvpService.saveOrUpdateRsvp(chatId, firstName, Rsvp.Status.DECLINE, meeting.getId());
                sendMessage(chatId,
                        "❌ You have declined the meeting.\n\n" +
                                "You will not receive further reminders."
                );
                sendMessage(adminService.getAdminChatId(),
                        "❌ " + firstName + " declined the meeting.");
            }
            case "MAYBE" -> {
                rsvpService.saveOrUpdateRsvp(chatId, firstName, Rsvp.Status.MAYBE, meeting.getId());
                sendMessage(chatId,
                        "🤔 You chose Maybe.\n\n" +
                                "We will remind you again before the meeting to confirm."
                );
                sendMessage(adminService.getAdminChatId(),
                        "🤔 " + firstName + " chose Maybe.");
            }
        }
    }

    public void sendRsvpButtons(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton accept = new InlineKeyboardButton();
        accept.setText("✅ Accept");
        accept.setCallbackData("ACCEPT");

        InlineKeyboardButton maybe = new InlineKeyboardButton();
        maybe.setText("🤔 Maybe");
        maybe.setCallbackData("MAYBE");

        InlineKeyboardButton decline = new InlineKeyboardButton();
        decline.setText("❌ Decline");
        decline.setCallbackData("DECLINE");

        row.add(accept);
        row.add(maybe);
        row.add(decline);
        rows.add(row);
        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendTwoButtons(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton accept = new InlineKeyboardButton();
        accept.setText("✅ Accept");
        accept.setCallbackData("ACCEPT");

        InlineKeyboardButton decline = new InlineKeyboardButton();
        decline.setText("❌ Decline");
        decline.setCallbackData("DECLINE");

        row.add(accept);
        row.add(decline);
        rows.add(row);
        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    // Remove buttons from message after participant clicks
    private void removeButtons(String chatId, Integer messageId) {
        EditMessageReplyMarkup editMarkup = new EditMessageReplyMarkup();
        editMarkup.setChatId(chatId);
        editMarkup.setMessageId(messageId);
        editMarkup.setReplyMarkup(new InlineKeyboardMarkup(new ArrayList<>()));
        try {
            execute(editMarkup);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    // Answer callback query to stop loading spinner on button
    private void answerCallback(String callbackQueryId) {
        org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery answer =
                new org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery();
        answer.setCallbackQueryId(callbackQueryId);
        try {
            execute(answer);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}