package github.lianyutian.cshop.note.mq.producer.listener;

import github.lianyutian.cshop.common.utils.JsonUtil;
import github.lianyutian.cshop.note.mapper.NoteMapper;
import github.lianyutian.cshop.note.model.po.Note;
import github.lianyutian.cshop.note.mq.message.NoteUpdateMessage;
import java.nio.charset.StandardCharsets;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.stereotype.Component;

/**
 * 笔记添加消息监听
 *
 * @author lianyutian
 * @since 2025-01-16 10:59:25
 * @version 1.0
 */
@Slf4j
@Component
@AllArgsConstructor
public class NoteAddListener implements TransactionListener {

  private final NoteMapper noteMapper;

  @Override
  public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
    return LocalTransactionState.UNKNOW;
  }

  @Override
  public LocalTransactionState checkLocalTransaction(MessageExt msg) {
    return checkNoteAddStatus(msg);
  }

  private LocalTransactionState checkNoteAddStatus(Message msg) {
    try {
      String messageBody = new String(msg.getBody(), StandardCharsets.UTF_8);
      NoteUpdateMessage noteAddMessage = JsonUtil.fromJson(messageBody, NoteUpdateMessage.class);
      if (noteAddMessage == null) {
        log.error("Failed to parse NoteAddMessage from message body: {}", messageBody);
        return LocalTransactionState.ROLLBACK_MESSAGE;
      }

      Long noteId = noteAddMessage.getNoteId();
      if (noteId == null) {
        log.error("NoteId is null in NoteAddMessage: {}", noteAddMessage);
        return LocalTransactionState.ROLLBACK_MESSAGE;
      }

      Note note = noteMapper.selectById(noteId);
      if (note != null) {
        log.info("Note with id {} found, committing message", noteId);
        return LocalTransactionState.COMMIT_MESSAGE;
      } else {
        log.warn("Note with id {} not found, returning unknown state", noteId);
        return LocalTransactionState.UNKNOW;
      }
    } catch (Exception e) {
      log.error("Error processing message: {}", e.getMessage(), e);
      return LocalTransactionState.ROLLBACK_MESSAGE;
    }
  }
}
