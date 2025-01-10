package github.lianyutian.cshop.note.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import github.lianyutian.cshop.common.enums.BizCodeEnum;
import github.lianyutian.cshop.common.exception.BizException;
import github.lianyutian.cshop.common.interceptor.LoginInterceptor;
import github.lianyutian.cshop.common.model.LoginUserInfo;
import github.lianyutian.cshop.common.redis.RedisCache;
import github.lianyutian.cshop.common.redis.RedisLock;
import github.lianyutian.cshop.common.utils.BeanUtil;
import github.lianyutian.cshop.common.utils.JsonUtil;
import github.lianyutian.cshop.note.constant.NoteCacheKeyConstant;
import github.lianyutian.cshop.note.enums.NoteStatusEnum;
import github.lianyutian.cshop.note.mapper.NoteMapper;
import github.lianyutian.cshop.note.model.param.NoteAddParam;
import github.lianyutian.cshop.note.model.param.NoteEditParam;
import github.lianyutian.cshop.note.model.po.Note;
import github.lianyutian.cshop.note.model.vo.NoteDetailVO;
import github.lianyutian.cshop.note.service.NoteService;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 笔记服务实现类
 *
 * @author lianyutian
 * @since 2025-01-03 10:54:31
 * @version 1.0
 */
@Service
@Slf4j
@AllArgsConstructor
public class NoteServiceImpl implements NoteService {

  private final NoteMapper noteMapper;

  private final RedisLock redisLock;

  private final RedisCache redisCache;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public int addNote(NoteAddParam noteAddParam) {
    LoginUserInfo loginUserInfo = LoginInterceptor.USER_THREAD_LOCAL.get();
    Note note = BeanUtil.copy(noteAddParam, Note.class);
    note.setUserId(loginUserInfo.getId());
    note.setStatus(NoteStatusEnum.AUDITING);

    if (noteAddParam.getPublishTime() != null) {
      // TODO 后续做定时发布功能
      note.setPublishTime(new Date());
    } else {
      note.setPublishTime(new Date());
    }

    int row = noteMapper.insert(note);

    if (row > 0) {
      redisCache.increment(NoteCacheKeyConstant.NOTE_TOTAL_KEY_PREFIX + loginUserInfo.getId(), 1);
    }

    log.info("笔记服务-新增笔记：rows={}，data={}", row, JsonUtil.toJson(noteAddParam));
    return row;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public int updateNote(NoteEditParam noteEditParam) {
    // 1.查询笔记是否存在
    LoginUserInfo loginUserInfo = LoginInterceptor.USER_THREAD_LOCAL.get();

    // 这里增加分布式锁是为了保证 Redis 中缓存的笔记数据和 DB 中的笔记数据一致
    /*
    存在这种场景：假设当前缓存为空，用户A更新自己的笔记，同时用户B查询用户A的笔记。如果这时用户A还未更新数据库，用户B已经从数据库查询到了数据
    同时用户B所在线程CPU执行时间消耗完被挂起。此时用户A再更新数据库成功并将新数据写入缓存。用户B线程才继续执行，此时用户B查询到的数据是旧的。
    又去写入了缓存那么此时的缓存数据就是旧的。
     */
    String noteUpdateLockKey =
        NoteCacheKeyConstant.NOTE_UPDATE_LOCK_KEY_PREFIX + noteEditParam.getId();
    boolean locked = false;
    try {

      locked = redisLock.lock(noteUpdateLockKey);

      if (!locked) {
        log.info("笔记模块-用户修改笔记信息：用户 {} 获取锁失败, 笔记 {}", loginUserInfo.getId(), noteEditParam.getId());
        throw new BizException(BizCodeEnum.NOTE_UPDATE_LOCK_FAIL);
      }
      // 先删掉缓存，防止出现数据库更新成功但是更新缓存失败，获取用户信息时一直都是缓存的数据
      redisCache.delete(NoteCacheKeyConstant.NOTE_DETAIL_KEY_PREFIX + noteEditParam.getId());

      Note updateNote = BeanUtil.copy(noteEditParam, Note.class);

      int row =
          noteMapper.update(
              updateNote,
              new LambdaUpdateWrapper<Note>()
                  .eq(Note::getId, noteEditParam.getId())
                  .eq(Note::getUserId, loginUserInfo.getId()));

      log.info("笔记服务-更新笔记：row={}, data={}", row, JsonUtil.toJson(noteEditParam));
      return row;
    } finally {
      if (locked) {
        redisLock.unlock(noteUpdateLockKey);
      }
    }
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public int deleteNote(long id) {
    return noteMapper.deleteById(id);
  }

  @Override
  public NoteDetailVO getNoteDetail(Long noteId) {
    LoginUserInfo loginUserInfo = LoginInterceptor.USER_THREAD_LOCAL.get();

    Note note =
        noteMapper.selectOne(
            new LambdaQueryWrapper<Note>()
                .eq(Note::getId, noteId)
                .eq(Note::getUserId, loginUserInfo.getId()));
    if (note == null) {
      return null;
    }
    return BeanUtil.copy(note, NoteDetailVO.class);
  }
}
