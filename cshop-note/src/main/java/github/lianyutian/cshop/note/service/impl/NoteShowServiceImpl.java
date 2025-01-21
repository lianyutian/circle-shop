package github.lianyutian.cshop.note.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import github.lianyutian.cshop.common.enums.BizCodeEnum;
import github.lianyutian.cshop.common.exception.BizException;
import github.lianyutian.cshop.common.model.vo.PageVO;
import github.lianyutian.cshop.common.redis.RedisCache;
import github.lianyutian.cshop.common.redis.RedisLock;
import github.lianyutian.cshop.common.utils.BeanUtil;
import github.lianyutian.cshop.common.utils.JsonUtil;
import github.lianyutian.cshop.note.constant.NoteCacheKeyConstant;
import github.lianyutian.cshop.note.enums.NoteStatusEnum;
import github.lianyutian.cshop.note.mapper.NoteMapper;
import github.lianyutian.cshop.note.model.param.NotePageParam;
import github.lianyutian.cshop.note.model.po.Note;
import github.lianyutian.cshop.note.model.vo.NoteShowVO;
import github.lianyutian.cshop.note.service.NoteShowService;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * 用户笔记展示实现
 *
 * @author lianyutian
 * @since 2025-01-09 10:18:39
 * @version 1.0
 */
@Service
@Slf4j
@AllArgsConstructor
public class NoteShowServiceImpl implements NoteShowService {

  private final RedisCache redisCache;

  private final RedisLock redisLock;

  private final NoteMapper noteMapper;

  @Override
  public NoteShowVO getNoteShow(Long noteId) {
    String noteShowKey = NoteCacheKeyConstant.NOTE_SHOW_KEY_PREFIX + noteId;
    NoteShowVO noteDetailFromCache = getNoteShowFromCache(noteShowKey);
    if (noteDetailFromCache != null) {
      return noteDetailFromCache;
    }
    NoteShowVO noteShowFromDB = getNoteShowFromDB(noteShowKey, noteId);
    if (noteShowFromDB != null) {
      redisCache.set(
          noteShowKey, noteShowFromDB, RedisCache.generateCacheExpire(), TimeUnit.MILLISECONDS);
    }
    return noteShowFromDB;
  }

  @Override
  public PageVO<NoteShowVO> getNoteShowList(NotePageParam notePageParam) {
    // 从缓存查询分页数据
    List<NoteShowVO> noteListFromCache =
        getNoteShowListFromCache(
            NoteCacheKeyConstant.NOTE_SHOW_PAGE_KEY_PREFIX
                + notePageParam.getUserId()
                + ":"
                + notePageParam.getPageNo());
    if (!CollectionUtils.isEmpty(noteListFromCache)) {
      return getNotePage(notePageParam, noteListFromCache);
    }

    List<NoteShowVO> noteListFromDB = getNoteShowListFromDB(notePageParam);
    if (!CollectionUtils.isEmpty(noteListFromDB)) {
      return getNotePage(notePageParam, noteListFromDB);
    }
    return null;
  }

  private NoteShowVO getNoteShowFromCache(String noteShowKey) {
    String noteShowFromCache = redisCache.get(noteShowKey);

    if (StringUtils.isNotBlank(noteShowFromCache)) {
      Long expire = redisCache.getExpire(noteShowKey, TimeUnit.SECONDS);
      // 如果过期时间已经在 1 小时内了就自动延期
      if (expire < RedisCache.ONE_HOUR_SECONDS) {
        // 缓存精准自动延期 2天 + 随机几个小时
        redisCache.expire(noteShowKey, RedisCache.generateCacheExpire(), TimeUnit.MILLISECONDS);
      }
      log.info("noteShowFromCache={}", noteShowFromCache);
      return JsonUtil.fromJson(noteShowFromCache, NoteShowVO.class);
    }
    return null;
  }

  private NoteShowVO getNoteShowFromDB(String noteDetailKey, Long noteId) {
    /*
    1.设置分布式锁防止超高并发请求打到数据库
    2.保证笔记数据更新时缓存和数据库的数据一致性
    */
    String noteUpdateLockKey = NoteCacheKeyConstant.NOTE_UPDATE_LOCK_KEY_PREFIX + noteId;
    boolean locked = false;
    try {
      locked = redisLock.tryLock(noteUpdateLockKey, RedisLock.UPDATE_LOCK_TIMEOUT);
      if (!locked) {
        // 再次尝试获取缓存
        NoteShowVO noteDetailFromCache = getNoteShowFromCache(noteDetailKey);
        if (noteDetailFromCache != null) {
          return noteDetailFromCache;
        }
        log.warn("【getNoteShowDetail】笔记缓存为空，查询笔记信息获取锁失败 {}", noteId);
        throw new BizException(BizCodeEnum.NOTE_INFO_LOCK_FAIL);
      }

      // 获取锁成功，查询数据库
      Note note =
          noteMapper.selectOne(
              new LambdaQueryWrapper<Note>()
                  .eq(Note::getId, noteId)
                  .eq(Note::getStatus, NoteStatusEnum.PUBLISHED.getCode()));
      if (note == null) {
        log.warn("【getNoteShowFromDB】笔记不存在 {}", noteId);
        // 防止同时间大量无效 noteId 穿透缓存打到数据库造成缓存穿透
        redisCache.set(
            noteDetailKey,
            RedisCache.EMPTY_CACHE,
            RedisCache.generateCachePenetrationExpire(),
            TimeUnit.MILLISECONDS);
        return null;
      }
      return BeanUtil.copy(note, NoteShowVO.class);
    } catch (InterruptedException e) {
      NoteShowVO noteDetailFromCache = getNoteShowFromCache(noteDetailKey);
      if (noteDetailFromCache != null) {
        return noteDetailFromCache;
      }
      log.error("【getNoteShow】尝试加锁异常，异常信息：{}", e.getMessage(), e);
      throw new BizException(BizCodeEnum.NOTE_INFO_LOCK_FAIL);
    } finally {
      if (locked) {
        redisLock.unlock(noteUpdateLockKey);
      }
    }
  }

  /**
   * 获取缓存的分页笔记信息
   *
   * @param notePageInfoKey 分页缓存key
   * @return 分页笔记信息
   */
  @SuppressWarnings("unchecked")
  private List<NoteShowVO> getNoteShowListFromCache(String notePageInfoKey) {
    String cachePageInfo = redisCache.get(notePageInfoKey);
    if (StringUtils.isNotBlank(cachePageInfo)) {
      Long expire = redisCache.getExpire(notePageInfoKey, TimeUnit.SECONDS);
      // 如果过期时间已经在 1 小时内了就自动延期
      if (expire < RedisCache.ONE_HOUR_SECONDS) {
        // 缓存精准自动延期 2天 + 随机几个小时
        redisCache.expire(notePageInfoKey, RedisCache.generateCacheExpire(), TimeUnit.MILLISECONDS);
      }
      return JsonUtil.fromJson(cachePageInfo, List.class);
    }
    return null;
  }

  private List<NoteShowVO> getNoteShowListFromDB(NotePageParam notePageParam) {
    Long userId = notePageParam.getUserId();
    // 分布式锁 key
    String noteUpdateLockKey = NoteCacheKeyConstant.NOTE_UPDATE_LOCK_KEY_PREFIX + userId;
    boolean tryLocked = false;
    // 笔记分页缓存 key
    String notePageKey =
        NoteCacheKeyConstant.NOTE_SHOW_PAGE_KEY_PREFIX + userId + ":" + notePageParam.getPageNo();
    try {
      tryLocked = redisLock.tryLock(noteUpdateLockKey, RedisCache.UPDATE_LOCK_TIMEOUT);

      if (!tryLocked) {
        /* 尝试加锁时间有 RedisCache.UPDATE_LOCK_TIMEOUT 有可能其他线程已经获取数据并写入缓存了所以这里再尝试去读下缓存 */
        List<NoteShowVO> noteListFromCache = getNoteShowListFromCache(notePageKey);
        if (!CollectionUtils.isEmpty(noteListFromCache)) {
          return noteListFromCache;
        }
        log.info("【getNoteListFromDB】笔记缓存为空，查询笔记信息获取锁失败 {}", userId);
        return null;
      }

      // 查询数据库
      LambdaQueryWrapper<Note> lambdaQueryWrapper = new LambdaQueryWrapper<>();
      lambdaQueryWrapper
          .eq(Note::getUserId, userId)
          .eq(Note::getStatus, NoteStatusEnum.PUBLISHED)
          .orderByDesc(Note::getUpdateTime);

      Page<Note> notePage = noteMapper.selectPage(notePageParam.toMpPage(), lambdaQueryWrapper);

      List<NoteShowVO> noteShowList = BeanUtil.copyList(notePage.getRecords(), NoteShowVO.class);

      if (CollectionUtils.isEmpty(noteShowList)) {
        // 防止缓存穿透
        redisCache.set(
            notePageKey,
            RedisCache.EMPTY_ARRAY_CACHE,
            RedisCache.generateCachePenetrationExpire(),
            TimeUnit.MILLISECONDS);
        return null;
      }

      redisCache.set(
          notePageKey, noteShowList, RedisCache.generateCacheExpire(), TimeUnit.MILLISECONDS);

      log.info("【getNoteListFromDB】笔记缓存为空，从数据库获取笔记信息 {}", JsonUtil.toJson(noteShowList));

      return noteShowList;
    } catch (InterruptedException e) {
      // 加锁失败
      // 这里再去尝试获取下缓存-双重检查
      List<NoteShowVO> noteListFromCache = getNoteShowListFromCache(notePageKey);
      if (!CollectionUtils.isEmpty(noteListFromCache)) {
        return noteListFromCache;
      }
      log.error("【getNoteListFromDB】尝试加锁异常，异常信息：{}", e.getMessage(), e);
      throw new BizException(BizCodeEnum.NOTE_INFO_LOCK_FAIL);
    } finally {
      if (tryLocked) {
        redisLock.unlock(noteUpdateLockKey);
      }
    }
  }

  private PageVO<NoteShowVO> getNotePage(NotePageParam notePageParam, List<NoteShowVO> noteList) {
    // 计算我的笔记列表总数
    String userNoteTotalKey =
        NoteCacheKeyConstant.NOTE_TOTAL_KEY_PREFIX + notePageParam.getUserId();
    // 笔记总条数
    Long total = redisCache.getLong(userNoteTotalKey);
    if (total == null) {
      total =
          noteMapper.selectCount(
              new LambdaQueryWrapper<Note>()
                  .eq(Note::getUserId, notePageParam.getUserId())
                  .eq(Note::getStatus, NoteStatusEnum.PUBLISHED.getCode()));
      redisCache.set(
          userNoteTotalKey, total, RedisCache.generateCacheExpire(), TimeUnit.MILLISECONDS);
    }
    // 计算总页数
    double totalPages = Math.ceil((double) total / notePageParam.getPageSize());
    return new PageVO<>(total, (long) totalPages, Collections.unmodifiableList(noteList));
  }
}
